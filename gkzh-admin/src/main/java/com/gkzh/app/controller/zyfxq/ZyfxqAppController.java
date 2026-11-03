package com.gkzh.app.controller.zyfxq;

import com.gkzh.common.core.controller.FrontBaseController;
import com.gkzh.common.core.domain.AjaxResult;
import com.gkzh.common.core.domain.model.StudentCheckin;
import com.gkzh.common.exception.ServiceException;
import com.gkzh.school.domain.GkzhStudent;
import com.gkzh.school.mapper.GkzhStudentMapper;
import com.gkzh.zyfxq.domain.ZyfxqRecord;
import com.gkzh.zyfxq.service.ZyfxqGameService;
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
 * 职业飞行棋学生端接口。
 * 所有读写都校验当前登录学生、活动实例和游戏编号，禁止跨活动或跨学生复用记录。
 */
@RestController
@RequestMapping("/api/zyfxq")
public class ZyfxqAppController extends FrontBaseController {

    private final ZyfxqGameService gameService;

    /** 学生资料以服务端数据库为准，不依赖小程序提交的姓名、学号和专业字段。 */
    private final GkzhStudentMapper studentMapper;

    public ZyfxqAppController(ZyfxqGameService gameService, GkzhStudentMapper studentMapper) {
        this.gameService = gameService;
        this.studentMapper = studentMapper;
    }

    /** 扫码进入：校验学校归属并幂等创建参与记录，返回当前对局进度与棋盘。 */
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
        ZyfxqRecord record = gameService.enter(schoolId, id(data.get("instanceId")), id(data.get("gameId")),
                student.getUserId(), profile.getStudentId(), profile.getStudentNo(), profile.getStudentName(),
                profile.getDepartmentId(), profile.getDepartmentName(), profile.getGender());
        return AjaxResult.success(gameService.viewForStudent(record.getRecordId(), student.getUserId()));
    }

    /** 查询本人对局进度；携带活动或游戏参数时再次核对归属。 */
    @GetMapping("/records/{id}")
    public AjaxResult record(@PathVariable Long id,
                             @RequestParam(required = false) Long instanceId,
                             @RequestParam(required = false) Long gameId) {
        Map<String, Object> view = gameService.viewForStudent(id, currentUserId());
        if (instanceId != null && !instanceId.equals(view.get("instanceId"))) {
            throw new ServiceException("该记录不属于当前活动");
        }
        if (gameId != null && !gameId.equals(view.get("gameId"))) {
            throw new ServiceException("该记录不属于当前游戏");
        }
        return AjaxResult.success(view);
    }

    /** 点击开始游戏：固化规则参数并开始 3 分钟计时；重复调用幂等。 */
    @PostMapping("/records/{id}/start")
    public AjaxResult start(@PathVariable Long id) {
        return AjaxResult.success(gameService.start(id, currentUserId()));
    }

    /** 掷骰子：服务端生成点数并前进，返回本次点数、落点格与可能下发的题目。 */
    @PostMapping("/records/{id}/roll")
    public AjaxResult roll(@PathVariable Long id) {
        return AjaxResult.success(gameService.roll(id, currentUserId()));
    }

    /** 提交当前题目答案；optionLabel 为空或 timeout=true 表示超时未作答。 */
    @PostMapping("/records/{id}/answer")
    public AjaxResult answer(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Map<String, Object> data = body == null ? Collections.emptyMap() : body;
        Long questionId = id(data.get("questionId"));
        String optionLabel = text(data.get("optionLabel"));
        Boolean timeout = bool(data.get("timeout"));
        return AjaxResult.success(gameService.answer(id, currentUserId(), questionId, optionLabel, timeout));
    }

    /** 未通关后重新开始一局；仍复用同一条参与记录。 */
    @PostMapping("/records/{id}/restart")
    public AjaxResult restart(@PathVariable Long id) {
        return AjaxResult.success(gameService.restart(id, currentUserId()));
    }

    private Long currentUserId() {
        StudentCheckin student = getCurrentStudent();
        if (student == null || student.getUserId() == null) {
            throw new ServiceException("登录状态已失效，请重新登录");
        }
        return student.getUserId();
    }

    private static String text(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? null : text;
    }

    private static Boolean bool(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        String text = String.valueOf(value).trim();
        return "true".equalsIgnoreCase(text) || "1".equals(text);
    }

    private static Long id(Object value) {
        if (value == null || String.valueOf(value).trim().isEmpty()) {
            return null;
        }
        try {
            return Long.valueOf(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            throw new ServiceException("参数无效，请重新进入游戏");
        }
    }
}
