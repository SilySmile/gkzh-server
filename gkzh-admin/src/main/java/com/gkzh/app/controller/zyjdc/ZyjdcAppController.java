package com.gkzh.app.controller.zyjdc;

import com.gkzh.common.core.controller.FrontBaseController;
import com.gkzh.common.core.domain.AjaxResult;
import com.gkzh.common.core.domain.model.StudentCheckin;
import com.gkzh.common.exception.ServiceException;
import com.gkzh.school.domain.GkzhStudent;
import com.gkzh.school.mapper.GkzhStudentMapper;
import com.gkzh.zyjdc.domain.ZyjdcRecord;
import com.gkzh.zyjdc.service.ZyjdcConfigService;
import com.gkzh.zyjdc.service.ZyjdcGameService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

/**
 * 职业角斗场学生端接口。
 * 所有读写均校验当前登录学生、活动实例和游戏编号，禁止跨活动或跨学生复用记录。
 */
@RestController
@RequestMapping("/api/zyjdc")
public class ZyjdcAppController extends FrontBaseController {

    private final ZyjdcGameService gameService;
    private final ZyjdcConfigService configService;
    /** 学生资料以服务端数据库为准，不依赖小程序提交的姓名、学号和专业字段。 */
    private final GkzhStudentMapper studentMapper;

    public ZyjdcAppController(ZyjdcGameService gameService, ZyjdcConfigService configService,
                              GkzhStudentMapper studentMapper) {
        this.gameService = gameService;
        this.configService = configService;
        this.studentMapper = studentMapper;
    }

    /** 8 个动物角色，含头像、职场定位、生存法则和能力说明。 */
    @GetMapping("/roles")
    public AjaxResult roles() {
        return AjaxResult.success(configService.listActive());
    }

    /** 扫码进入：校验学校归属并幂等创建参与记录。 */
    @PostMapping("/records/enter")
    public AjaxResult enter(@RequestBody Map<String, Object> body) {
        Map<String, Object> data = body == null ? Collections.emptyMap() : body;
        StudentCheckin student = getCurrentStudent();
        Long schoolId = id(data.get("schoolId"));
        GkzhStudent profile = schoolId == null || student == null || student.getStuId() == null ? null
                : studentMapper.selectGkzhStudentByStudentId(student.getStuId());
        if (profile == null || !schoolId.equals(profile.getSchoolId()) || student.getUserId() == null
                || !student.getUserId().equals(profile.getUserId())) {
            throw new ServiceException("学生档案与当前学校不一致，请重新登录后扫码");
        }
        // department_id 是专业（二级部门）编号，专业名称由学生查询关联部门给出。
        ZyjdcRecord record = gameService.enter(schoolId, id(data.get("instanceId")), id(data.get("gameId")),
                student.getUserId(), profile.getStudentId(), profile.getStudentNo(), profile.getStudentName(),
                profile.getDepartmentId(), profile.getDepartmentName(), profile.getGender());
        // 再次扫码可能恢复进行中的对局；通过事务读取方法补做超时结算，避免在事务外更新记录。
        return AjaxResult.success(gameService.viewRecord(record.getRecordId(), student.getUserId()));
    }

    /** 查询本人对局进度；携带活动或游戏参数时再次核对归属。 */
    @GetMapping("/records/{id}")
    public AjaxResult record(@PathVariable Long id,
                             @RequestParam(required = false) Long instanceId,
                             @RequestParam(required = false) Long gameId) {
        Map<String, Object> view = gameService.viewRecord(id, currentUserId());
        if (instanceId != null && !instanceId.equals(view.get("instanceId"))) {
            throw new ServiceException("该记录不属于当前活动");
        }
        if (gameId != null && !gameId.equals(view.get("gameId"))) {
            throw new ServiceException("该记录不属于当前游戏");
        }
        return AjaxResult.success(view);
    }

    /** 选择动物角色；对局开始后不能更换。 */
    @PostMapping("/records/{id}/role")
    public AjaxResult selectRole(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return AjaxResult.success(gameService.selectRole(id, currentUserId(), id(body == null ? null : body.get("roleId"))));
    }

    /** 开始对局：生成棋盘并启动 3 分钟计时。 */
    @PostMapping("/records/{id}/start")
    public AjaxResult start(@PathVariable Long id) {
        return AjaxResult.success(gameService.start(id, currentUserId()));
    }

    /** 学生落子：服务端校验后执行学生一步和电脑一步。 */
    @PostMapping("/records/{id}/move")
    public AjaxResult move(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        int fromRow = integer(body, "fromRow");
        int fromCol = integer(body, "fromCol");
        int toRow = integer(body, "toRow");
        int toCol = integer(body, "toCol");
        return AjaxResult.success(gameService.move(id, currentUserId(), fromRow, fromCol, toRow, toCol));
    }

    /** 失败或平局后重开一局。 */
    @PostMapping("/records/{id}/retry")
    public AjaxResult retry(@PathVariable Long id) {
        return AjaxResult.success(gameService.retry(id, currentUserId()));
    }

    private Long currentUserId() {
        StudentCheckin student = getCurrentStudent();
        if (student == null || student.getUserId() == null) {
            throw new ServiceException("登录状态已失效，请重新登录");
        }
        return student.getUserId();
    }

    private static int integer(Map<String, Object> body, String key) {
        Object value = body == null ? null : body.get(key);
        if (value == null || String.valueOf(value).trim().isEmpty()) {
            throw new ServiceException("请选择要移动的棋子和目标位置");
        }
        try {
            return Integer.parseInt(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            throw new ServiceException("棋盘位置参数无效，请重新选择");
        }
    }

    private static Long id(Object value) {
        if (value == null || String.valueOf(value).trim().isEmpty()) {
            return null;
        }
        try {
            return Long.valueOf(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            throw new ServiceException("活动参数无效，请重新扫码进入");
        }
    }
}
