package com.gkzh.app.controller.lottery;

import com.gkzh.common.config.GkzhConfig;
import com.gkzh.common.utils.QRCodeUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.nio.file.Files;

/** 公开奖品核销码图片。二维码内容仅包含记录编号，工作人员核销接口仍需登录和学校权限。 */
@RestController
@RequestMapping("/profile/lottery/qr")
public class PrizeQrPublicController {
    @GetMapping(value = "/{recordId}.png", produces = MediaType.IMAGE_PNG_VALUE)
    public void image(@PathVariable Long recordId, HttpServletResponse response) throws Exception {
        File dir = new File(GkzhConfig.getProfile(), "lottery-qr");
        if (!dir.exists()) dir.mkdirs();
        File file = new File(dir, "prize-" + recordId + ".png");
        if (!file.exists() || file.length() == 0) {
            Files.write(file.toPath(), QRCodeUtils.generateQRCode("GKZH_PRIZE:" + recordId, 360, 360));
        }
        response.setContentType(MediaType.IMAGE_PNG_VALUE);
        response.setHeader("Cache-Control", "public, max-age=86400");
        Files.copy(file.toPath(), response.getOutputStream());
    }
}
