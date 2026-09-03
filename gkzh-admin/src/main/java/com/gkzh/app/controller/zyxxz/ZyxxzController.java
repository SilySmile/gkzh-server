package com.gkzh.app.controller.zyxxz;

import com.gkzh.activity.domain.GkzhActivityParticipationRecord;
import com.gkzh.activity.service.IGkzhActivityParticipationRecordService;
import com.gkzh.common.core.controller.FrontBaseController;
import com.gkzh.common.core.domain.AjaxResult;
import com.gkzh.common.core.domain.model.StudentCheckin;
import com.gkzh.common.utils.DateUtils;
import com.gkzh.zyxxz.domain.GkzhZyxxzStudentChoice;
import com.gkzh.zyxxz.service.IGkzhZyxxzStudentChoiceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 学生端职业信息站Controller
 *
 * @author gkzh
 * @date 2026-06-04
 */
@Api("学生端职业信息站")
@RestController
@RequestMapping("/api/zyxxz")
public class ZyxxzController extends FrontBaseController {

    private static final Logger logger = LoggerFactory.getLogger(ZyxxzController.class);

    @Autowired
    private IGkzhZyxxzStudentChoiceService zyxxzStudentChoiceService;

    @Autowired
    private IGkzhActivityParticipationRecordService activityParticipationRecordService;

    /**
     * 提交职业信息渠道选择
     */
    @ApiOperation("提交职业信息渠道选择")
    @PostMapping("/submit")
    public AjaxResult submitChoice(@RequestBody Map<String, Object> request) {
        try {
            StudentCheckin currentStudent = getCurrentStudent();
            Long currentUserId = currentStudent.getStuId();
            String currentStudentName = currentStudent.getStuName();
            String currentStudentNo = currentStudent.getStuNo();

            Long activityId = Long.valueOf(request.get("activityId").toString());
            String commonChannel = (String) request.get("commonChannel");
            String trustedChannel = (String) request.get("trustedChannel");
            String obstacles = (String) request.get("obstacles");

            if (commonChannel == null || commonChannel.trim().isEmpty()) {
                return AjaxResult.error("请选择最常用求职信息渠道");
            }
            if (trustedChannel == null || trustedChannel.trim().isEmpty()) {
                return AjaxResult.error("请选择最信任求职信息渠道");
            }

            GkzhZyxxzStudentChoice choice = new GkzhZyxxzStudentChoice();
            choice.setStudentId(currentUserId);
            choice.setStudentName(currentStudentName);
            choice.setStudentNo(currentStudentNo);
            choice.setActivityId(activityId);
            choice.setCommonChannel(commonChannel);
            choice.setTrustedChannel(trustedChannel);
            choice.setObstacles(obstacles);
            choice.setCreateTime(DateUtils.getNowDate());

            int result = zyxxzStudentChoiceService.insertGkzhZyxxzStudentChoice(choice);

            if (result > 0) {
                try {
                    GkzhActivityParticipationRecord record = new GkzhActivityParticipationRecord();
                    record.setUserId(currentStudent.getUserId());
                    record.setActivityId(activityId);
                    record.setParticipationType(9);
                    record.setParticipationTime(DateUtils.getNowDate());
                    record.setStatus(1);
                    record.setUserName(currentStudent.getStuName());
                    record.setUserCode(currentStudent.getStuNo());
                    activityParticipationRecordService.insertGkzhActivityParticipationRecord(record);
                } catch (Exception e) {
                    logger.warn("记录参与记录失败，不影响主流程", e);
                }

                return AjaxResult.success("提交成功");
            } else {
                return AjaxResult.error("提交失败");
            }

        } catch (Exception e) {
            logger.error("提交职业信息渠道选择失败", e);
            return AjaxResult.error("提交失败：" + e.getMessage());
        }
    }

    /**
     * 查询当前用户的选择结果
     */
    @ApiOperation("查询我的选择结果")
    @GetMapping("/result/{activityId}")
    public AjaxResult getMyResult(@PathVariable Long activityId) {
        try {
            StudentCheckin currentStudent = getCurrentStudent();
            Long currentUserId = currentStudent.getStuId();

            GkzhZyxxzStudentChoice queryParam = new GkzhZyxxzStudentChoice();
            queryParam.setStudentId(currentUserId);
            queryParam.setActivityId(activityId);
            List<GkzhZyxxzStudentChoice> choices = zyxxzStudentChoiceService.selectGkzhZyxxzStudentChoiceList(queryParam);

            if (choices != null && !choices.isEmpty()) {
                return AjaxResult.success(choices.get(0));
            } else {
                return AjaxResult.error("未找到选择记录");
            }

        } catch (Exception e) {
            logger.error("查询选择结果失败", e);
            return AjaxResult.error("查询失败：" + e.getMessage());
        }
    }
}
