package com.gkzh.web.controller.activity;

import com.gkzh.activity.domain.GkzhActivity;
import com.gkzh.activity.service.IGkzhActivityService;
import com.gkzh.common.utils.QRCodeUtils;
import com.gkzh.common.core.controller.BaseController;
import com.gkzh.common.core.domain.AjaxResult;
import com.google.zxing.WriterException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * 活动二维码Controller
 * 
 * @author gkzh
 * @date 2025-06-22
 */
@RestController
@RequestMapping("/activity/qrcode")
public class QRCodeController extends BaseController {

    @Autowired
    private IGkzhActivityService gkzhActivityService;

    /**
     * 下载活动二维码
     * 
     * @param activityId 活动ID
     * @return 二维码图片
     */
    @GetMapping("/download/{activityId}")
    public ResponseEntity<byte[]> downloadQRCode(@PathVariable Long activityId) {
        try {
            // 查询活动信息
            GkzhActivity activity = gkzhActivityService.selectGkzhActivityByActivityId(activityId);
            if (activity == null) {
                return ResponseEntity.notFound().build();
            }

            // 生成二维码
            byte[] qrCodeBytes = QRCodeUtils.generateQRCode(activity.getQrCode());

            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentDispositionFormData("attachment", "activity_qrcode_" + activityId + ".png");

            return new ResponseEntity<>(qrCodeBytes, headers, HttpStatus.OK);
        } catch (WriterException | IOException e) {
            logger.error("生成二维码失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 获取活动二维码内容
     * 
     * @param activityId 活动ID
     * @return 二维码内容
     */
    @GetMapping("/content/{activityId}")
    public AjaxResult getQRCodeContent(@PathVariable Long activityId) {
        GkzhActivity activity = gkzhActivityService.selectGkzhActivityByActivityId(activityId);
        if (activity == null) {
            return AjaxResult.error("活动不存在");
        }
        return AjaxResult.success(activity.getQrCode());
    }

    /**
     * 重新生成活动二维码
     * 
     * @param activityId 活动ID
     * @return 结果
     */
    @PostMapping("/regenerate/{activityId}")
    public AjaxResult regenerateQRCode(@PathVariable Long activityId) {
        try {
            GkzhActivity activity = gkzhActivityService.selectGkzhActivityByActivityId(activityId);
            if (activity == null) {
                return AjaxResult.error("活动不存在");
            }

            // 重新生成二维码内容
            String qrContent = QRCodeUtils.generateActivityQRContent(activityId,
                "http://localhost:8080"); // 这里应该从配置中获取
            activity.setQrCode(qrContent);
            
            int result = gkzhActivityService.updateGkzhActivity(activity);
            if (result > 0) {
                return AjaxResult.success("二维码重新生成成功");
            } else {
                return AjaxResult.error("二维码重新生成失败");
            }
        } catch (Exception e) {
            logger.error("重新生成二维码失败", e);
            return AjaxResult.error("二维码重新生成失败：" + e.getMessage());
        }
    }
} 