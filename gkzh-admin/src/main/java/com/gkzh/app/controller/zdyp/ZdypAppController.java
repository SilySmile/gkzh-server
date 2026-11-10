package com.gkzh.app.controller.zdyp;

import com.gkzh.common.core.controller.FrontBaseController;
import com.gkzh.common.core.domain.AjaxResult;
import com.gkzh.common.core.domain.model.StudentCheckin;
import com.gkzh.common.exception.ServiceException;
import com.gkzh.school.domain.GkzhStudent;
import com.gkzh.school.mapper.GkzhStudentMapper;
import com.gkzh.zdyp.domain.ZdypRecord;
import com.gkzh.zdyp.domain.dto.ZdypPlacement;
import com.gkzh.zdyp.service.ZdypGameService;
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
 * 职得一拼学生端接口。
 * 所有读写均校验当前登录学生、活动实例与游戏编号，禁止跨活动或跨学生复用记录。
 */
@RestController
@RequestMapping("/api/zdyp")
public class ZdypAppController extends FrontBaseController {

    private final ZdypGameService gameService;
    /** 学生资料以服务端数据库为准，不能依赖小程序提交的姓名、学号和专业字段。 */
    private final GkzhStudentMapper studentMapper;

    public ZdypAppController(ZdypGameService gameService, GkzhStudentMapper studentMapper) {
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

    /** 查询本人参与进度、岗位目录与最近一次结果；携带活动或游戏参数时再次核对归属。 */
    @GetMapping("/records/{id}")
    public AjaxResult record(@PathVariable Long id,
                             @RequestParam(required = false) Long instanceId,
                             @RequestParam(required = false) Long gameId) {
        ZdypRecord record = gameService.get(id, getCurrentStudent().getUserId());
        if (instanceId != null && !instanceId.equals(record.getInstanceId())) {
            throw new ServiceException("该记录不属于当前活动");
        }
        if (gameId != null && !gameId.equals(record.getGameId())) {
            throw new ServiceException("该记录不属于当前游戏");
        }
        return AjaxResult.success(gameService.view(record));
    }

    /** 选择岗位并开始一次闯关，返回四张卡片与服务端截止时间。 */
    @PostMapping("/records/{id}/start")
    public AjaxResult start(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Object value = body == null ? null : body.get("careerCode");
        String careerCode = value == null ? null : String.valueOf(value).trim();
        return AjaxResult.success(gameService.start(id, getCurrentStudent().getUserId(), careerCode));
    }

    /** 提交四个卡槽的卡片放置，服务端判定并返回解析与结果。 */
    @PostMapping("/records/{id}/submit")
    public AjaxResult submit(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Integer attemptNo = integer(body == null ? null : body.get("attemptNo"));
        List<ZdypPlacement> placements = new ArrayList<>();
        Object raw = body == null ? null : body.get("placements");
        if (raw instanceof List) {
            for (Object item : (List<?>) raw) {
                if (!(item instanceof Map)) {
                    throw new ServiceException("提交内容不完整，请重新选择卡片");
                }
                Map<?, ?> map = (Map<?, ?>) item;
                ZdypPlacement placement = new ZdypPlacement();
                Object slot = map.get("slotCode");
                placement.setSlotCode(slot == null ? null : String.valueOf(slot).trim());
                placement.setCardId(id(map.get("cardId")));
                placement.setElapsedMs(integer(map.get("elapsedMs")) == null ? null
                        : integer(map.get("elapsedMs")).longValue());
                placements.add(placement);
            }
        }
        return AjaxResult.success(gameService.submit(id, getCurrentStudent().getUserId(), attemptNo, placements));
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

    private static Integer integer(Object value) {
        if (value == null || String.valueOf(value).trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.valueOf(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            throw new ServiceException("提交参数无效，请重新开始本局");
        }
    }
}
