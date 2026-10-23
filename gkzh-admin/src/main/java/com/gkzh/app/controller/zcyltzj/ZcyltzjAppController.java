package com.gkzh.app.controller.zcyltzj;

import com.gkzh.common.core.controller.FrontBaseController;
import com.gkzh.common.core.domain.AjaxResult;
import com.gkzh.common.core.domain.model.StudentCheckin;
import com.gkzh.common.exception.ServiceException;
import com.gkzh.school.domain.GkzhStudent;
import com.gkzh.school.mapper.GkzhStudentMapper;
import com.gkzh.zcyltzj.domain.ZcyltzjRecord;
import com.gkzh.zcyltzj.service.ZcyltzjConfigService;
import com.gkzh.zcyltzj.service.ZcyltzjRecordService;
import com.gkzh.zcyltzj.service.ZcyltzjRuleService;
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
 * 职场压力·骰子局学生端接口。
 * 所有读写均校验当前登录学生、活动实例与游戏编号，禁止跨活动或跨学生复用记录。
 */
@RestController
@RequestMapping("/api/zcyltzj")
public class ZcyltzjAppController extends FrontBaseController {

    private final ZcyltzjRecordService recordService;
    private final ZcyltzjConfigService configService;
    private final ZcyltzjRuleService ruleService;
    /** 学生资料以服务端数据库为准，不能依赖小程序提交的姓名、学号和专业字段。 */
    private final GkzhStudentMapper studentMapper;

    public ZcyltzjAppController(ZcyltzjRecordService recordService, ZcyltzjConfigService configService,
                                ZcyltzjRuleService ruleService, GkzhStudentMapper studentMapper) {
        this.recordService = recordService;
        this.configService = configService;
        this.ruleService = ruleService;
        this.studentMapper = studentMapper;
    }

    /** 九宫格展示所需的六个点数名称。 */
    @GetMapping("/points")
    public AjaxResult points() {
        return AjaxResult.success(configService.listActivePoints());
    }

    /** 九宫格下中、下右两格的规则内容。 */
    @GetMapping("/rule-cells")
    public AjaxResult ruleCells() {
        return AjaxResult.success(ruleService.listActive());
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
        return AjaxResult.success(recordService.enter(schoolId, id(body.get("instanceId")), id(body.get("gameId")),
                student.getUserId(), profile.getStudentId(), profile.getStudentNo(), profile.getStudentName(),
                profile.getDepartmentId(), profile.getDepartmentName(), profile.getGender()));
    }

    /** 查询本局进度和结果；携带活动或游戏参数时再次核对归属。 */
    @GetMapping("/records/{id}")
    public AjaxResult record(@PathVariable Long id,
                            @RequestParam(required = false) Long instanceId,
                            @RequestParam(required = false) Long gameId) {
        ZcyltzjRecord record = recordService.get(id, getCurrentStudent().getUserId());
        if (instanceId != null && !instanceId.equals(record.getInstanceId())) {
            throw new ServiceException("该记录不属于当前活动");
        }
        if (gameId != null && !gameId.equals(record.getGameId())) {
            throw new ServiceException("该记录不属于当前游戏");
        }
        return AjaxResult.success(recordService.view(record));
    }

    /** 掷骰子：服务端生成点数并返回本局试题，重复调用返回同一点数。 */
    @PostMapping("/records/{id}/roll")
    public AjaxResult roll(@PathVariable Long id) {
        return AjaxResult.success(recordService.roll(id, getCurrentStudent().getUserId()));
    }

    /** 提交整套答案并结算压力值。 */
    @PostMapping("/records/{id}/submit")
    public AjaxResult submit(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return AjaxResult.success(recordService.submit(id, getCurrentStudent().getUserId(), answers(body)));
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> answers(Map<String, Object> body) {
        Object value = body == null ? null : body.get("answers");
        if (!(value instanceof List)) {
            throw new ServiceException("请完成全部题目后提交");
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object item : (List<Object>) value) {
            if (item instanceof Map) {
                result.add((Map<String, Object>) item);
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
