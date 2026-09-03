package com.gkzh.common.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 二维码生成工具类
 * 
 * @author gkzh
 * @date 2025-06-22
 */
public class QRCodeUtils {

    /**
     * 生成二维码图片
     * 
     * @param content 二维码内容
     * @param width 图片宽度
     * @param height 图片高度
     * @return 二维码图片字节数组
     * @throws WriterException
     * @throws IOException
     */
    public static byte[] generateQRCode(String content, int width, int height) throws WriterException, IOException {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("二维码内容不能为空");
        }
        
        // 验证输入参数
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("图片尺寸必须大于0");
        }
        
        // 设置二维码参数
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 2);

        // 创建二维码写入器
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints);

        // 创建图片 - 使用ARGB格式确保透明度支持
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        
        // 设置抗锯齿和渲染质量
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // 设置白色背景
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, width, height);
        
        // 设置黑色前景
        graphics.setColor(Color.BLACK);

        // 绘制二维码
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (bitMatrix.get(x, y)) {
                    graphics.fillRect(x, y, 1, 1);
                }
            }
        }

        graphics.dispose();

        // 转换为字节数组
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        boolean writeSuccess = ImageIO.write(image, "PNG", outputStream);
        
        if (!writeSuccess) {
            throw new IOException("无法将图片写入PNG格式");
        }
        
        byte[] result = outputStream.toByteArray();
        outputStream.close();
        
        // 验证生成的字节数组
        if (result == null || result.length == 0) {
            throw new IOException("生成的二维码字节数组为空");
        }
        
        return result;
    }

    /**
     * 生成活动二维码内容
     * 
     * @param activityId 活动ID
     * @param baseUrl 基础URL
     * @return 二维码内容
     */
    public static String generateActivityQRContent(Long activityId, String baseUrl) {
        return baseUrl + "/pages/activity/index?activityId=" + activityId;
    }

    /**
     * 生成默认尺寸的二维码
     * 
     * @param content 二维码内容
     * @return 二维码图片字节数组
     * @throws WriterException
     * @throws IOException
     */
    public static byte[] generateQRCode(String content) throws WriterException, IOException {
        return generateQRCode(content, 300, 300);
    }
} 