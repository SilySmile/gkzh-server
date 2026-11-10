package com.gkzh.app.controller.zytj;

import com.gkzh.common.core.controller.FrontBaseController;
import com.gkzh.common.core.domain.AjaxResult;
import com.gkzh.common.core.domain.model.StudentCheckin;
import com.gkzh.common.exception.ServiceException;
import com.gkzh.school.domain.GkzhStudent;
import com.gkzh.school.mapper.GkzhStudentMapper;
import com.gkzh.zytj.domain.ZytjRecord;
import com.gkzh.zytj.service.ZytjGameService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 职愿探究学生端接口。
 * 所有读写均校验当前登录学生、活动实例与游戏编号，禁止跨活动或跨学生复用记录。
 */
@RestController
@RequestMapping("/api/zytj")
public class ZytjAppController extends FrontBaseController {

    private final ZytjGameService gameService;
    /** 学生资料以服务端数据库为准，不能依赖小程序提交的姓名、学号和专业字段。 */
    private final GkzhStudentMapper studentMapper;

    public ZytjAppController(ZytjGameService gameService, GkzhStudentMapper studentMapper) {
        this.gameService = gameService;
        this.studentMapper = studentMapper;
    }

    /** 扫码进入：校验学校归属并幂等创建参与记录。 */
    @PostMapping("/records/enter")
    public AjaxResult enter(@RequestBody Map<String, Object> body) {
        StudentCheckin student = getCurrentStudent();
        Long schoolId = id(body.get("schoolId"));
        GkzhStudent profile = schoolId == null || student == null || student.getStuId() == null ? null
                : studentMapper.selectGkzhStudentByStudentId(student.getStuId());
        if (profile == null || !schoolId.equals(profile.getSchoolId()) || student.getUserId() == null
                || !student.getUserId().equals(profile.getUserId())) {
            throw new ServiceException("学生档案与当前学校不一致，请重新登录后扫码");
        }
        // department_id 是专业（二级部门）编号，专业名称由学生查询关联部门给出。
        return AjaxResult.success(gameService.enter(schoolId, id(body.get("instanceId")), id(body.get("gameId")),
                student.getUserId(), profile.getStudentId(), profile.getStudentNo(), profile.getStudentName(),
                profile.getDepartmentId(), profile.getDepartmentName(), profile.getGender()));
    }

    /** 查询本人选择进度和职业探索卡；携带活动或游戏参数时再次核对归属。 */
    @GetMapping("/records/{id}")
    public AjaxResult record(@PathVariable Long id,
                             @RequestParam(required = false) Long instanceId,
                             @RequestParam(required = false) Long gameId) {
        ZytjRecord record = gameService.get(id, getCurrentStudent().getUserId());
        if (instanceId != null && !instanceId.equals(record.getInstanceId())) {
            throw new ServiceException("该记录不属于当前活动");
        }
        if (gameId != null && !gameId.equals(record.getGameId())) {
            throw new ServiceException("该记录不属于当前游戏");
        }
        return AjaxResult.success(gameService.view(record));
    }

    /** 进入选择页：记录开始时间，30 秒限时由服务端按该时间计算。 */
    @PostMapping("/records/{id}/start")
    public AjaxResult start(@PathVariable Long id) {
        return AjaxResult.success(gameService.start(id, getCurrentStudent().getUserId()));
    }

    /**
     * 提交选择：body 中 itemCodes 为按列序的物品编码数组，长度 4，未选中的列可为空。
     * 到点自动提交时带 timeout=true，服务端补全后照常生成职业探索卡。
     */
    @PostMapping("/records/{id}/submit")
    public AjaxResult submit(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        List<String> itemCodes = codes(body == null ? null : body.get("itemCodes"));
        boolean timeout = body != null && Boolean.parseBoolean(String.valueOf(body.get("timeout")));
        return AjaxResult.success(gameService.submit(id, getCurrentStudent().getUserId(), itemCodes, timeout));
    }

    private static List<String> codes(Object value) {
        List<String> list = new ArrayList<>();
        if (!(value instanceof List)) {
            return list;
        }
        for (Object item : (List<?>) value) {
            list.add(item == null ? null : String.valueOf(item).trim());
        }
        return list;
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
