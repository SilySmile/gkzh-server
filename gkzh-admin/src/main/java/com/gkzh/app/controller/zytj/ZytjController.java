package com.gkzh.app.controller.zytj;

import com.gkzh.activity.domain.GkzhActivityParticipationRecord;
import com.gkzh.activity.service.IGkzhActivityParticipationRecordService;
import com.gkzh.app.dto.MbtiChoiceRequest;
import com.gkzh.common.core.controller.FrontBaseController;
import com.gkzh.common.core.domain.AjaxResult;
import com.gkzh.common.core.domain.model.StudentCheckin;
import com.gkzh.common.utils.DateUtils;
import com.gkzh.zytj.domain.GkzhMbtiProduct;
import com.gkzh.zytj.domain.GkzhMbtiStudentChoice;
import com.gkzh.zytj.service.IGkzhMbtiProductService;
import com.gkzh.zytj.service.IGkzhMbtiStudentChoiceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 学生端职愿探究Controller
 *
 * @author gkzh
 * @date 2026-06-02
 */
@Api("学生端职愿探究")
@RestController
@RequestMapping("/api/zytj")
public class ZytjController extends FrontBaseController {

    private static final Logger logger = LoggerFactory.getLogger(ZytjController.class);

    @Autowired
    private IGkzhMbtiProductService mbtiProductService;

    @Autowired
    private IGkzhMbtiStudentChoiceService mbtiStudentChoiceService;

    @Autowired
    private IGkzhActivityParticipationRecordService activityParticipationRecordService;

    /**
     * 获取4列商品列表
     * @param activityId 活动ID
     * @return 按列分组的商品数据
     */
    @ApiOperation("获取职愿探究商品列表")
    @GetMapping("/products/{activityId}")
    public AjaxResult getMbtiProducts(@PathVariable Long activityId) {
        try {
            GkzhMbtiProduct queryParam = new GkzhMbtiProduct();
            queryParam.setStatus("0");
            List<GkzhMbtiProduct> allProducts = mbtiProductService.selectGkzhMbtiProductList(queryParam);

            Map<Long, List<GkzhMbtiProduct>> groupedProducts = allProducts.stream()
                    .collect(Collectors.groupingBy(
                            GkzhMbtiProduct::getColumnIndex,
                            LinkedHashMap::new,
                            Collectors.toList()
                    ));

            groupedProducts.values().forEach(list ->
                    list.sort(Comparator.comparing(GkzhMbtiProduct::getSortOrder))
            );

            return AjaxResult.success(groupedProducts);

        } catch (Exception e) {
            logger.error("获取职愿探究商品列表失败", e);
            return AjaxResult.error("获取商品列表失败：" + e.getMessage());
        }
    }

    /**
     * 提交选择，生成MBTI代码
     * @param request 选择请求
     * @return 生成的MBTI代码
     */
    @ApiOperation("提交职愿探究选择")
    @PostMapping("/submit")
    public AjaxResult submitChoice(@RequestBody MbtiChoiceRequest request) {
        try {
            StudentCheckin currentStudent = getCurrentStudent();
            Long currentUserId = currentStudent.getStuId();
            String currentStudentName = currentStudent.getStuName();
            String currentStudentNo = currentStudent.getStuNo();
            Long activityId = request.getActivityId();

//            GkzhMbtiStudentChoice queryParam = new GkzhMbtiStudentChoice();
//            queryParam.setStudentId(currentUserId);
//            queryParam.setActivityId(activityId);
//            List<GkzhMbtiStudentChoice> existingChoices = mbtiStudentChoiceService.selectGkzhMbtiStudentChoiceList(queryParam);
//
//            if (existingChoices != null && !existingChoices.isEmpty()) {
//                return AjaxResult.error("您已经提交过选择，不能重复提交");
//            }

            String productIds = request.getProductIds();
            if (productIds == null || productIds.trim().isEmpty()) {
                return AjaxResult.error("请选择商品");
            }

            String[] productIdArray = productIds.split(",");
            if (productIdArray.length != 4) {
                return AjaxResult.error("必须选择4个商品（每列1个）");
            }

            StringBuilder mbtiCode = new StringBuilder();
            for (String productId : productIdArray) {
                GkzhMbtiProduct product = mbtiProductService.selectGkzhMbtiProductByProductId(Long.parseLong(productId.trim()));
                if (product == null) {
                    return AjaxResult.error("商品不存在");
                }
                mbtiCode.append(product.getMbtiDimension());
            }

            GkzhMbtiStudentChoice choice = new GkzhMbtiStudentChoice();
            choice.setStudentId(currentUserId);
            choice.setStudentName(currentStudentName);
            choice.setStudentNo(currentStudentNo);
            choice.setActivityId(activityId);
            choice.setChoiceCode(mbtiCode.toString());
            choice.setProductIds(productIds);
            choice.setChoiceTime(0L);
            choice.setIsRedeemed("0");
            choice.setCreateTime(DateUtils.getNowDate());

            int result = mbtiStudentChoiceService.insertGkzhMbtiStudentChoice(choice);

            if (result > 0) {
                try {
                    GkzhActivityParticipationRecord record = new GkzhActivityParticipationRecord();
                    record.setUserId(currentStudent.getUserId());
                    record.setActivityId(activityId);
                    record.setParticipationType(8);
                    record.setParticipationTime(DateUtils.getNowDate());
                    record.setStatus(1);
                    record.setUserName(currentStudent.getStuName());   // ← 加这行
                    record.setUserCode(currentStudent.getStuNo());     // ← 加这行
                    activityParticipationRecordService.insertGkzhActivityParticipationRecord(record);

                } catch (Exception e) {
                    logger.warn("记录参与记录失败，不影响主流程", e);
                }

                Map<String, Object> resultData = new HashMap<>();
                resultData.put("choiceCode", mbtiCode.toString());
                resultData.put("choiceId", choice.getChoiceId());

                return AjaxResult.success("提交成功", resultData);
            } else {
                return AjaxResult.error("提交失败");
            }

        } catch (Exception e) {
            logger.error("提交职愿探究选择失败", e);
            return AjaxResult.error("提交失败：" + e.getMessage());
        }
    }

    /**
     * 查询当前用户的选择结果
     * @param activityId 活动ID
     * @return 选择记录
     */
    @ApiOperation("查询我的选择结果")
    @GetMapping("/result/{activityId}")
    public AjaxResult getMyResult(@PathVariable Long activityId) {
        try {
            StudentCheckin currentStudent = getCurrentStudent();
            Long currentUserId = currentStudent.getStuId();

            GkzhMbtiStudentChoice queryParam = new GkzhMbtiStudentChoice();
            queryParam.setStudentId(currentUserId);
            queryParam.setActivityId(activityId);
            List<GkzhMbtiStudentChoice> choices = mbtiStudentChoiceService.selectGkzhMbtiStudentChoiceList(queryParam);

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
