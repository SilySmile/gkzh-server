package com.gkzh.app.controller.lottery;

import java.io.IOException;
import java.util.Base64;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.gkzh.common.core.controller.FrontBaseController;
import com.gkzh.common.core.domain.AjaxResult;
import com.gkzh.common.utils.QRCodeUtils;
import com.gkzh.lottery.domain.LotteryRecord;
import com.gkzh.lottery.service.ILotteryRecordService;

/** 用户中奖凭证二维码，仅允许中奖用户查看自己的记录。 */
@RestController
@RequestMapping("/api/lottery/my")
public class PrizeQrController extends FrontBaseController {
    @Autowired private ILotteryRecordService recordService;

    @GetMapping("/qr/{recordId}")
    public void qr(@PathVariable Long recordId, HttpServletResponse response) throws IOException {
        LotteryRecord record = recordService.selectLotteryRecordByRecordId(recordId);
        if (record == null || "谢谢参与".equals(record.getPrizeTitle())
                || !getCurrentStudent().getUserId().equals(record.getUserId())) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND); return;
        }
        try {
            byte[] bytes = QRCodeUtils.generateQRCode("GKZH_PRIZE:" + recordId, 360, 360);
            response.setContentType("image/png"); response.getOutputStream().write(bytes);
        } catch (Exception e) { response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "二维码生成失败"); }
    }

    /** 以 data URI 返回二维码，兼容微信小程序无法直接转换二进制图片的环境。 */
    @GetMapping("/qr-data/{recordId}")
    public AjaxResult qrData(@PathVariable Long recordId) {
        LotteryRecord record = recordService.selectLotteryRecordByRecordId(recordId);
        if (record == null || "谢谢参与".equals(record.getPrizeTitle())
                || !getCurrentStudent().getUserId().equals(record.getUserId())) {
            return AjaxResult.error(404, "奖品记录不存在");
        }
        try {
            byte[] bytes = QRCodeUtils.generateQRCode("GKZH_PRIZE:" + recordId, 360, 360);
            return AjaxResult.success("data:image/png;base64," + Base64.getEncoder().encodeToString(bytes));
        } catch (Exception e) {
            return AjaxResult.error("二维码生成失败");
        }
    }
}
