package com.gkzh.app.controller.nlld;

import com.gkzh.common.core.controller.FrontBaseController;
import com.gkzh.common.core.domain.AjaxResult;
import com.gkzh.common.core.domain.model.StudentCheckin;
import com.gkzh.common.exception.ServiceException;
import com.gkzh.nlld.domain.NlldRecord;
import com.gkzh.nlld.service.NlldGameService;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 能量雷达学生端接口。
 * 所有读写均校验当前登录学生、活动实例和游戏编号，禁止跨活动或跨学生复用记录。
 */
@RestController
@RequestMapping("/api/nlld")
public class NlldAppController extends FrontBaseController {

    private final NlldGameService gameService;
    /** 学生资料以服务端数据库为准，不依赖小程序提交的姓名、学号和专业字段。 */
    private final GkzhStudentMapper studentMapper;

    public NlldAppController(NlldGameService gameService, GkzhStudentMapper studentMapper) {
        this.gameService = gameService;
        this.studentMapper = studentMapper;
    }

    /** 扫码进入：校验学校归属并幂等创建参与记录，返回当前进度和关卡配置。 */
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
        NlldRecord record = gameService.enter(schoolId, id(data.get("instanceId")), id(data.get("gameId")),
                student.getUserId(), profile.getStudentId(), profile.getStudentNo(), profile.getStudentName(),
                profile.getDepartmentId(), profile.getDepartmentName(), profile.getGender());
        return AjaxResult.success(gameService.viewRecord(record.getRecordId(), student.getUserId()));
    }

    /** 查询本人测评进度；携带活动或游戏参数时再次核对归属。 */
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

    /** 提交一关的 2 件物品；第 6 关提交后返回结果并标记完成。 */
    @PostMapping("/records/{id}/answer")
    public AjaxResult answer(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Integer roundNo = integer(body, "roundNo");
        List<Long> optionIds = ids(body == null ? null : body.get("optionIds"));
        return AjaxResult.success(gameService.answer(id, currentUserId(), roundNo, optionIds));
    }

    private Long currentUserId() {
        StudentCheckin student = getCurrentStudent();
        if (student == null || student.getUserId() == null) {
            throw new ServiceException("登录状态已失效，请重新登录");
        }
        return student.getUserId();
    }

    /** 读取关卡号；缺失或非数字时给出中文提示。 */
    private static Integer integer(Map<String, Object> body, String key) {
        Object value = body == null ? null : body.get(key);
        if (value == null || String.valueOf(value).trim().isEmpty()) {
            throw new ServiceException("缺少关卡信息，请重新进入");
        }
        try {
            return Integer.parseInt(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            throw new ServiceException("关卡参数无效，请重新进入");
        }
    }

    /** 读取物品编号数组，最多取前 2 个由服务层继续校验。 */
    private static List<Long> ids(Object value) {
        List<Long> result = new ArrayList<>();
        if (value instanceof List) {
            for (Object item : (List<?>) value) {
                Long id = id(item);
                if (id != null) {
                    result.add(id);
                }
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
            throw new ServiceException("所选物品参数无效，请重新选择");
        }
    }
}
