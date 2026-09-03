package com.gkzh.web.controller.activity;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.gkzh.common.annotation.Log;
import com.gkzh.common.annotation.Anonymous;
import com.gkzh.common.core.controller.BaseController;
import com.gkzh.common.core.domain.AjaxResult;
import com.gkzh.common.enums.BusinessType;
import com.gkzh.activity.domain.GkzhActivity;
import com.gkzh.activity.service.IGkzhActivityService;
import com.gkzh.common.utils.poi.ExcelUtil;
import com.gkzh.common.core.page.TableDataInfo;
import com.gkzh.common.utils.QRCodeUtils;
import com.google.zxing.WriterException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

/**
 * 活动举办Controller
 * 
 * @author gkzh
 * @date 2025-06-22
 */
@RestController
@RequestMapping("/activity/activity")
public class GkzhActivityController extends BaseController
{
    @Autowired
    private IGkzhActivityService gkzhActivityService;
    
    @Value("${app.qr-code.base-url}")
    private String qrCodeBaseUrl;

    /**
     * 查询活动举办列表
     */
    @PreAuthorize("@ss.hasPermi('activity:activity:list')")
    @GetMapping("/list")
    public TableDataInfo list(GkzhActivity gkzhActivity)
    {
        startPage();
        List<GkzhActivity> list = gkzhActivityService.selectGkzhActivityList(gkzhActivity);
        return getDataTable(list);
    }

    /**
     * 导出活动举办列表
     */
    @PreAuthorize("@ss.hasPermi('activity:activity:export')")
    @Log(title = "活动举办", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, GkzhActivity gkzhActivity)
    {
        List<GkzhActivity> list = gkzhActivityService.selectGkzhActivityList(gkzhActivity);
        ExcelUtil<GkzhActivity> util = new ExcelUtil<GkzhActivity>(GkzhActivity.class);
        util.exportExcel(response, list, "活动举办数据");
    }

    /**
     * 获取活动举办详细信息
     */
    @PreAuthorize("@ss.hasPermi('activity:activity:query')")
    @GetMapping(value = "/{activityId}")
    public AjaxResult getInfo(@PathVariable("activityId") Long activityId)
    {
        return success(gkzhActivityService.selectGkzhActivityByActivityId(activityId));
    }

    /**
     * 新增活动举办
     */
    @PreAuthorize("@ss.hasPermi('activity:activity:add')")
    @Log(title = "活动举办", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody GkzhActivity gkzhActivity)
    {
        gkzhActivity.setCreateBy(getUsername());
        return toAjax(gkzhActivityService.insertGkzhActivity(gkzhActivity));
    }

    /**
     * 修改活动举办
     */
    @PreAuthorize("@ss.hasPermi('activity:activity:edit')")
    @Log(title = "活动举办", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody GkzhActivity gkzhActivity)
    {
        gkzhActivity.setUpdateBy(getUsername());
        return toAjax(gkzhActivityService.updateGkzhActivity(gkzhActivity));
    }

    /**
     * 删除活动举办
     */
    @PreAuthorize("@ss.hasPermi('activity:activity:remove')")
    @Log(title = "活动举办", businessType = BusinessType.DELETE)
	@DeleteMapping("/{activityIds}")
    public AjaxResult remove(@PathVariable Long[] activityIds)
    {
        return toAjax(gkzhActivityService.deleteGkzhActivityByActivityIds(activityIds));
    }

    /**
     * 复制活动
     */
    @PreAuthorize("@ss.hasPermi('activity:activity:add')")
    @Log(title = "活动举办", businessType = BusinessType.INSERT)
    @PostMapping("/copy/{activityId}")
    public AjaxResult copy(@PathVariable("activityId") Long activityId) {
        GkzhActivity originalActivity = gkzhActivityService.selectGkzhActivityByActivityId(activityId);
        if (originalActivity == null) {
            return AjaxResult.error("活动不存在");
        }

        // 清除主键和二维码信息，设置新活动名称
        originalActivity.setActivityId(null);
        originalActivity.setQrCode(null);
        originalActivity.setTitle(originalActivity.getTitle() + "(副本)");

        return toAjax(gkzhActivityService.insertGkzhActivity(originalActivity));
    }


    /**
     * 下载活动二维码
     */
    @PreAuthorize("@ss.hasPermi('activity:activity:query')")
    @GetMapping("/qrcode/{activityId}")
    public ResponseEntity<byte[]> downloadQRCode(@PathVariable Long activityId, HttpServletRequest request) {
        try {
            logger.info("开始下载二维码，activityId: {}", activityId);
            
            // 查询活动信息
            GkzhActivity activity = gkzhActivityService.selectGkzhActivityByActivityId(activityId);
            if (activity == null) {
                logger.error("活动不存在，activityId: {}", activityId);
                return ResponseEntity.notFound().build();
            }

            // 检查二维码内容
            String qrContent = activity.getQrCode();
            if (qrContent == null || qrContent.trim().isEmpty()) {
                logger.error("活动二维码内容为空，activityId: {}", activityId);
                // 尝试重新生成二维码内容
                qrContent = QRCodeUtils.generateActivityQRContent(activityId, qrCodeBaseUrl);
                logger.info("重新生成二维码内容: {}", qrContent);
            }

            logger.info("生成二维码，activityId: {}, content: {}", activityId, qrContent);

            // 生成二维码
            byte[] qrCodeBytes = QRCodeUtils.generateQRCode(qrContent, 400, 400); // 使用更大的尺寸

            if (qrCodeBytes == null || qrCodeBytes.length == 0) {
                logger.error("二维码生成失败，返回空字节数组，activityId: {}", activityId);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

            // 验证PNG文件头
            if (qrCodeBytes.length < 8 || 
                qrCodeBytes[0] != (byte) 0x89 || 
                qrCodeBytes[1] != (byte) 0x50 || 
                qrCodeBytes[2] != (byte) 0x4E || 
                qrCodeBytes[3] != (byte) 0x47) {
                logger.error("生成的二维码不是有效的PNG文件，activityId: {}", activityId);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentDispositionFormData("attachment", "activity_qrcode_" + activityId + ".png");
            headers.setContentLength(qrCodeBytes.length);
            headers.setCacheControl("no-cache");
            headers.setPragma("no-cache");
            headers.set("Content-Transfer-Encoding", "binary");
            headers.set("Accept-Ranges", "bytes");
            headers.set("Content-Disposition", "attachment; filename=\"activity_qrcode_" + activityId + ".png\"");

            logger.info("二维码生成成功，activityId: {}, 大小: {} bytes", activityId, qrCodeBytes.length);

            return new ResponseEntity<>(qrCodeBytes, headers, HttpStatus.OK);
        } catch (WriterException e) {
            logger.error("生成二维码失败（WriterException），activityId: {}, 错误: {}", activityId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (IOException e) {
            logger.error("生成二维码失败（IOException），activityId: {}, 错误: {}", activityId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            logger.error("生成二维码失败（未知异常），activityId: {}, 错误: {}", activityId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 获取活动二维码内容
     */
    @PreAuthorize("@ss.hasPermi('activity:activity:query')")
    @GetMapping("/qrcode/content/{activityId}")
    public AjaxResult getQRCodeContent(@PathVariable Long activityId) {
        GkzhActivity activity = gkzhActivityService.selectGkzhActivityByActivityId(activityId);
        if (activity == null) {
            return AjaxResult.error("活动不存在");
        }
        return success(activity.getQrCode());
    }

    /**
     * 在线预览活动二维码
     */
    @Anonymous
    @GetMapping("/qrcode/preview/{activityId}")
    public ResponseEntity<byte[]> previewQRCode(@PathVariable Long activityId) {
        try {
            logger.info("开始预览二维码，activityId: {}", activityId);
            
            // 查询活动信息
            GkzhActivity activity = gkzhActivityService.selectGkzhActivityByActivityId(activityId);
            if (activity == null) {
                logger.error("活动不存在，activityId: {}", activityId);
                return ResponseEntity.notFound().build();
            }

            // 检查二维码内容
            String qrContent = activity.getQrCode();
            if (qrContent == null || qrContent.trim().isEmpty()) {
                logger.error("活动二维码内容为空，activityId: {}", activityId);
                // 尝试重新生成二维码内容
                qrContent = QRCodeUtils.generateActivityQRContent(activityId, qrCodeBaseUrl);
                logger.info("重新生成二维码内容: {}", qrContent);
            }

            logger.info("预览二维码，activityId: {}, content: {}", activityId, qrContent);

            // 生成二维码
            byte[] qrCodeBytes = QRCodeUtils.generateQRCode(qrContent, 400, 400); // 使用更大的尺寸

            if (qrCodeBytes == null || qrCodeBytes.length == 0) {
                logger.error("二维码生成失败，返回空字节数组，activityId: {}", activityId);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

            // 验证PNG文件头
            if (qrCodeBytes.length < 8 || 
                qrCodeBytes[0] != (byte) 0x89 || 
                qrCodeBytes[1] != (byte) 0x50 || 
                qrCodeBytes[2] != (byte) 0x4E || 
                qrCodeBytes[3] != (byte) 0x47) {
                logger.error("生成的二维码不是有效的PNG文件，activityId: {}", activityId);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

            // 设置响应头 - 预览模式，不下载
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentLength(qrCodeBytes.length);
            headers.setCacheControl("no-cache");
            headers.setPragma("no-cache");

            logger.info("二维码预览生成成功，activityId: {}, 大小: {} bytes", activityId, qrCodeBytes.length);

            return new ResponseEntity<>(qrCodeBytes, headers, HttpStatus.OK);
        } catch (WriterException e) {
            logger.error("生成二维码预览失败（WriterException），activityId: {}, 错误: {}", activityId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (IOException e) {
            logger.error("生成二维码预览失败（IOException），activityId: {}, 错误: {}", activityId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            logger.error("生成二维码预览失败（未知异常），activityId: {}, 错误: {}", activityId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 重新生成活动二维码
     */
    @PreAuthorize("@ss.hasPermi('activity:activity:edit')")
    @Log(title = "重新生成活动二维码", businessType = BusinessType.UPDATE)
    @PostMapping("/qrcode/regenerate/{activityId}")
    public AjaxResult regenerateQRCode(@PathVariable Long activityId) {
        try {
            GkzhActivity activity = gkzhActivityService.selectGkzhActivityByActivityId(activityId);
            if (activity == null) {
                return AjaxResult.error("活动不存在");
            }

            // 重新生成二维码内容 - 使用配置中的基础URL
            String qrContent = QRCodeUtils.generateActivityQRContent(activityId, qrCodeBaseUrl);
            activity.setQrCode(qrContent);
            
            int result = gkzhActivityService.updateGkzhActivity(activity);
            if (result > 0) {
                return success("二维码重新生成成功");
            } else {
                return error("二维码重新生成失败");
            }
        } catch (Exception e) {
            logger.error("重新生成二维码失败", e);
            return error("二维码重新生成失败：" + e.getMessage());
        }
    }

    /**
     * 下载模块二维码
     */
    @PreAuthorize("@ss.hasPermi('activity:activity:query')")
    @GetMapping("/module/qrcode/download/{activityId}/{moduleType}")
    public ResponseEntity<byte[]> downloadModuleQRCode(@PathVariable Long activityId, @PathVariable String moduleType) {
        return generateModuleQRCodeResponse(activityId, moduleType, true);
    }

    /**
     * 在线预览模块二维码
     */
    @Anonymous
    @GetMapping("/module/qrcode/preview/{activityId}/{moduleType}")
    public ResponseEntity<byte[]> previewModuleQRCode(@PathVariable Long activityId, @PathVariable String moduleType) {
        return generateModuleQRCodeResponse(activityId, moduleType, false);
    }

    /**
     * 生成模块二维码响应
     * @param activityId 活动ID
     * @param moduleType 模块类型
     * @param isDownload 是否为下载模式
     * @return ResponseEntity<byte[]>
     */
    private ResponseEntity<byte[]> generateModuleQRCodeResponse(Long activityId, String moduleType, boolean isDownload) {
        try {
            logger.info("开始{}模块二维码，activityId: {}, moduleType: {}",
                       isDownload ? "下载" : "预览", activityId, moduleType);

            // 查询活动信息
            GkzhActivity activity = gkzhActivityService.selectGkzhActivityByActivityId(activityId);
            if (activity == null) {
                logger.error("活动不存在，activityId: {}", activityId);
                return ResponseEntity.notFound().build();
            }

            // 生成模块二维码内容
            StringBuilder pages = new StringBuilder();
            pages.append(qrCodeBaseUrl);

            switch(moduleType){
                case "check-in":
                    pages.append("/pages/checkin/index");
                    break;
                case "survey":
                    pages.append("/pages/wjdc/index");
                    break;
                case "mind-window":
                    pages.append("/pages/xycc/index");
                    break;
                case "lottery":
                    pages.append("/pages/lottery/index");
                    break;
                case "check-out":
                    pages.append("/pages/activity/index");
                    break;
                case "wjyd":
                    pages.append("/pages/wjyd/index");
                    break;
                case "cyzs":
                    pages.append("/pages/cyzs/index");
                    break;
                case "zytj":
                    pages.append("/pages/zytj/index");
                    break;
                case "sszctop":
                    pages.append("/pages/sszctop/index");
                    break;
                case "zyxxz":
                    pages.append("/pages/zyxxz/channel");
                    break;
                default:
                    // 如果 moduleType 不是预定义的类型，可能是具体模块ID，使用通用路径
                    pages.append("/pages/activity/index");
                    break;
            }

            // 添加查询参数
            pages.append("?activityId=").append(activityId);
            if (!moduleType.equals("check-in") && !moduleType.equals("survey") &&
                !moduleType.equals("mind-window") && !moduleType.equals("lottery") &&
                !moduleType.equals("check-out") && !moduleType.equals("wjyd") && !moduleType.equals("zytj") && !moduleType.equals("sszctop") && !moduleType.equals("zyxxz")) {
                pages.append("&moduleType=").append(moduleType);
            }

            // 使用小程序专用文本码，避免微信识别为网页链接后跳转到 Web 页面。
            String qrContent = "GKZH_MP:" + pages.substring(qrCodeBaseUrl.length());
            logger.info("生成模块二维码内容: {}", qrContent);

            // 生成二维码
            byte[] qrCodeBytes = QRCodeUtils.generateQRCode(qrContent, 400, 400);

            if (qrCodeBytes == null || qrCodeBytes.length == 0) {
                logger.error("模块二维码生成失败，返回空字节数组，activityId: {}, moduleType: {}", activityId, moduleType);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

            // 验证PNG文件头
            if (qrCodeBytes.length < 8 ||
                    qrCodeBytes[0] != (byte) 0x89 ||
                    qrCodeBytes[1] != (byte) 0x50 ||
                    qrCodeBytes[2] != (byte) 0x4E ||
                    qrCodeBytes[3] != (byte) 0x47) {
                logger.error("生成的模块二维码不是有效的PNG文件，activityId: {}, moduleType: {}", activityId, moduleType);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentLength(qrCodeBytes.length);
            headers.setCacheControl("no-cache");
            headers.setPragma("no-cache");

            if (isDownload) {
                String safeFileName = "module_qrcode_" + activityId + "_" + moduleType + ".png";
                headers.setContentDispositionFormData("attachment", safeFileName);
                headers.set("Content-Transfer-Encoding", "binary");
                headers.set("Accept-Ranges", "bytes");
            }

            logger.info("模块二维码{}成功，activityId: {}, moduleType: {}, 大小: {} bytes",
                       isDownload ? "下载" : "预览", activityId, moduleType, qrCodeBytes.length);

            return new ResponseEntity<>(qrCodeBytes, headers, HttpStatus.OK);
        } catch (WriterException e) {
            logger.error("生成模块二维码失败（WriterException），activityId: {}, moduleType: {}, 错误: {}",
                        activityId, moduleType, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (IOException e) {
            logger.error("生成模块二维码失败（IOException），activityId: {}, moduleType: {}, 错误: {}",
                        activityId, moduleType, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            logger.error("生成模块二维码失败（未知异常），activityId: {}, moduleType: {}, 错误: {}",
                        activityId, moduleType, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
