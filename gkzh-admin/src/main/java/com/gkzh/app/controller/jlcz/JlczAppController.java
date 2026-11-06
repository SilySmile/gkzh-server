package com.gkzh.app.controller.jlcz;

import com.gkzh.common.core.controller.FrontBaseController;
import com.gkzh.common.core.domain.AjaxResult;
import com.gkzh.common.core.domain.model.StudentCheckin;
import com.gkzh.common.exception.ServiceException;
import com.gkzh.jlcz.domain.JlczRecord;
import com.gkzh.jlcz.service.JlczGameService;
import com.gkzh.school.domain.GkzhStudent;
import com.gkzh.school.mapper.GkzhStudentMapper;
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
 * 简历称重学生端接口。
 * 所有读写均校验当前登录学生、活动实例与游戏编号，禁止跨活动或跨学生复用记录。
 */
@RestController
@RequestMapping("/api/jlcz")
public class JlczAppController extends FrontBaseController {

    private final JlczGameService gameService;
    /** 学生资料以服务端数据库为准，不能依赖小程序提交的姓名、学号和专业字段。 */
    private final GkzhStudentMapper studentMapper;

    public JlczAppController(JlczGameService gameService, GkzhStudentMapper studentMapper) {
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

    /** 查询本人称重进度和结果；携带活动或游戏参数时再次核对归属。 */
    @GetMapping("/records/{id}")
    public AjaxResult record(@PathVariable Long id,
                             @RequestParam(required = false) Long instanceId,
                             @RequestParam(required = false) Long gameId) {
        JlczRecord record = gameService.get(id, getCurrentStudent().getUserId());
        if (instanceId != null && !instanceId.equals(record.getInstanceId())) {
            throw new ServiceException("该记录不属于当前活动");
        }
        if (gameId != null && !gameId.equals(record.getGameId())) {
            throw new ServiceException("该记录不属于当前游戏");
        }
        return AjaxResult.success(gameService.view(record));
    }

    /** 开始称重：记录开始时间，用于计算用时和限时。 */
    @PostMapping("/records/{id}/start")
    public AjaxResult start(@PathVariable Long id) {
        return AjaxResult.success(gameService.start(id, getCurrentStudent().getUserId()));
    }

    /** 提交本次勾选的砝码，由服务端计算总重与通关判定。 */
    @PostMapping("/records/{id}/submit")
    public AjaxResult submit(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return AjaxResult.success(gameService.submit(id, getCurrentStudent().getUserId(), weightNos(body)));
    }

    /** 未通关重新称重：同一参与记录累加称重次数，历史明细保留。 */
    @PostMapping("/records/{id}/restart")
    public AjaxResult restart(@PathVariable Long id) {
        return AjaxResult.success(gameService.restart(id, getCurrentStudent().getUserId()));
    }

    /** 解析 weightNos 数组，非数字值直接拒绝，避免静默忽略学生勾选。 */
    @SuppressWarnings("unchecked")
    private static List<Integer> weightNos(Map<String, Object> body) {
        Object value = body == null ? null : body.get("weightNos");
        List<Integer> result = new ArrayList<>();
        if (value == null) {
            return result;
        }
        if (!(value instanceof List)) {
            throw new ServiceException("称重数据格式无效，请刷新后重试");
        }
        for (Object item : (List<Object>) value) {
            if (item == null || String.valueOf(item).trim().isEmpty()) {
                continue;
            }
            try {
                result.add(Integer.valueOf(String.valueOf(item).trim()));
            } catch (NumberFormatException e) {
                throw new ServiceException("砝码编号无效，请刷新后重试");
            }
        }
        return result;
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
