package com.gkzh.app.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gkzh.common.config.GkzhConfig;
import com.gkzh.common.exception.ServiceException;
import com.gkzh.common.utils.DateUtils;
import com.gkzh.zycck.domain.ZycckPrintTask;
import com.gkzh.zycck.domain.ZycckPrinter;
import com.gkzh.zycck.mapper.ZycckPrintTaskMapper;
import com.gkzh.zycck.service.HprtCloudService;
import com.gkzh.zycck.service.ZycckPrinterService;
import com.gkzh.zycck.domain.ZycckRecord;
import com.gkzh.zycck.mapper.ZycckRecordMapper;
import com.hprt.domain.HPRTResult;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * zycck 报告打印流程：报告 PDF -> 76mm x 130mm 位图 -> TSPL -> 汉印云任务。
 *
 * <p>报告 PDF 当前是按 76mm x 130mm 分页生成的，因此多页报告会在同一个云任务中
 * 依次发送多张同尺寸标签。</p>
 */
@Service
public class ZycckPrintService {
    private static final String STATUS_PENDING = "pending";
    private static final String STATUS_SUBMITTED = "submitted";
    private static final String STATUS_PRINTING = "printing";
    private static final String STATUS_SUCCESS = "success";
    private static final String STATUS_FAILED = "failed";

    /** 标签纸 76mm x 130mm，按汉印常见 203dpi 生成位图。 */
    private static final int DPI = 203;
    private static final int IMAGE_WIDTH = Math.round(76f / 25.4f * DPI);
    private static final int IMAGE_HEIGHT = Math.round(130f / 25.4f * DPI);
    private static final int IMAGE_WIDTH_BYTES = (IMAGE_WIDTH + 7) / 8;
    private static final int BLACK_THRESHOLD = 180;

    private final ZycckPrintTaskMapper taskMapper;
    private final ZycckRecordMapper recordMapper;
    private final ZycckPrinterService printerService;
    private final HprtCloudService hprtCloudService;
    private final ZycckReportPdfService reportPdfService;

    public ZycckPrintService(ZycckPrintTaskMapper taskMapper,
                             ZycckRecordMapper recordMapper,
                             ZycckPrinterService printerService,
                             HprtCloudService hprtCloudService,
                             ZycckReportPdfService reportPdfService) {
        this.taskMapper = taskMapper;
        this.recordMapper = recordMapper;
        this.printerService = printerService;
        this.hprtCloudService = hprtCloudService;
        this.reportPdfService = reportPdfService;
    }

    /** 学生选择打印机后提交打印。 */
    @Transactional
    public ZycckPrintTask print(Long recordId, Long userId, Long printerId) throws IOException {
        if (recordId == null) throw new ServiceException("缺少报告记录编号");
        if (userId == null) throw new ServiceException("登录信息已失效");
        ZycckPrinter printer = printablePrinter(printerId);
        // create() 同时校验记录属于当前学生，避免前端篡改 recordId 打印他人报告。
        byte[] pdf = reportPdfService.create(recordId, userId);
        return submit(recordId, printer, pdf, 0);
    }

    /** 工作人员重打历史任务；使用报告原所属用户生成报告，避免绕过报告数据权限。 */
    @Transactional
    public ZycckPrintTask reprint(Long taskId) throws IOException {
        ZycckPrintTask oldTask = findTask(taskId);
        if (oldTask.getRecordId() == null) throw new ServiceException("历史任务缺少报告记录");
        ZycckRecord record = recordMapper.selectById(oldTask.getRecordId());
        if (record == null || record.getUserId() == null) throw new ServiceException("报告记录不存在");
        ZycckPrinter printer = printablePrinter(oldTask.getPrinterId());
        byte[] pdf = reportPdfService.create(record.getRecordId(), record.getUserId());
        int retry = oldTask.getRetryCount() == null ? 1 : oldTask.getRetryCount() + 1;
        return submit(record.getRecordId(), printer, pdf, retry);
    }

    /** 查询汉印云端任务状态并同步本地状态。 */
    @Transactional
    public ZycckPrintTask refreshTask(Long taskId) {
        ZycckPrintTask task = findTask(taskId);
        if (!StringUtils.hasText(task.getPrintId())) throw new ServiceException("任务尚未提交到汉印云端");
        HPRTResult<Integer> result = hprtCloudService.queryPrinterTask(task.getEquipmentSn(), task.getPrintId());
        ensureSuccess(result, "查询打印任务状态失败");
        task.setStatus(mapCloudStatus(result.getData()));
        if (STATUS_SUCCESS.equals(task.getStatus())) task.setPrintTime(DateUtils.getNowDate());
        task.setUpdateTime(DateUtils.getNowDate());
        taskMapper.updateById(task);
        return task;
    }

    public List<ZycckPrintTask> listTasks(Long printerId, String status, Long recordId) {
        QueryWrapper<ZycckPrintTask> query = new QueryWrapper<>();
        if (printerId != null) query.eq("printer_id", printerId);
        if (StringUtils.hasText(status)) query.eq("status", status.trim());
        if (recordId != null) query.eq("record_id", recordId);
        query.orderByDesc("create_time").orderByDesc("task_id");
        return taskMapper.selectList(query);
    }

    public void verifyTaskOwner(Long taskId, Long userId) {
        ZycckPrintTask task = findTask(taskId);
        ZycckRecord record = task.getRecordId() == null ? null : recordMapper.selectById(task.getRecordId());
        if (record == null || userId == null || !userId.equals(record.getUserId())) {
            throw new ServiceException("打印任务不存在");
        }
    }
    public ZycckPrintTask findTask(Long taskId) {
        if (taskId == null) throw new ServiceException("缺少打印任务编号");
        ZycckPrintTask task = taskMapper.selectById(taskId);
        if (task == null) throw new ServiceException("打印任务不存在");
        return task;
    }

    private ZycckPrintTask submit(Long recordId, ZycckPrinter printer, byte[] pdf, int retryCount) throws IOException {
        String token = UUID.randomUUID().toString().replace("-", "");
        Path directory = Path.of(GkzhConfig.getProfile(), "zycck-print", token);
        Files.createDirectories(directory);
        Path pdfPath = directory.resolve("report.pdf");
        Files.write(pdfPath, pdf);

        List<BufferedImage> pages = renderPages(pdf);
        List<String> imageUrls = new ArrayList<>();
        StringBuilder tspl = new StringBuilder();
        for (int i = 0; i < pages.size(); i++) {
            BufferedImage image = pages.get(i);
            Path imagePath = directory.resolve("page-" + (i + 1) + ".png");
            ImageIO.write(image, "png", imagePath.toFile());
            imageUrls.add("/profile/zycck-print/" + token + "/page-" + (i + 1) + ".png");
            appendTsplLabel(tspl, image);
        }
        if (pages.isEmpty()) throw new ServiceException("报告没有可打印页面");

        Date now = DateUtils.getNowDate();
        ZycckPrintTask task = new ZycckPrintTask();
        task.setRecordId(recordId);
        task.setPrinterId(printer.getPrinterId());
        task.setEquipmentSn(printer.getEquipmentSn());
        task.setOrderNo("ZYCCK-" + token);
        task.setPrintType("TSPL");
        task.setSourcePdfUrl("/profile/zycck-print/" + token + "/report.pdf");
        task.setSourceImageUrl(imageUrls.get(0));
        task.setStatus(STATUS_PENDING);
        task.setRetryCount(retryCount);
        task.setCreateTime(now);
        task.setUpdateTime(now);
        taskMapper.insert(task);

        try {
            HPRTResult<String> result = hprtCloudService.sendPrinterTask(printer.getEquipmentSn(), tspl.toString(), task.getOrderNo());
            ensureSuccess(result, "提交汉印打印任务失败");
            if (!StringUtils.hasText(result.getData())) throw new ServiceException("汉印未返回打印任务编号");
            task.setPrintId(result.getData());
            task.setStatus(STATUS_SUBMITTED);
            task.setUpdateTime(DateUtils.getNowDate());
            taskMapper.updateById(task);
            return task;
        } catch (RuntimeException ex) {
            task.setStatus(STATUS_FAILED);
            task.setErrorMessage(limitMessage(ex.getMessage()));
            task.setUpdateTime(DateUtils.getNowDate());
            taskMapper.updateById(task);
            throw ex;
        }
    }

    private ZycckPrinter printablePrinter(Long printerId) {
        ZycckPrinter printer = printerService.findById(printerId);
        if (!ZycckPrinterService.ENABLED.equals(printer.getEnabled())) throw new ServiceException("打印机已禁用");
        if (!ZycckPrinterService.BOUND.equals(printer.getBoundStatus())) throw new ServiceException("打印机未绑定");
        if (!Integer.valueOf(1).equals(printer.getStatus())) throw new ServiceException("打印机当前不在线");
        return printer;
    }

    private List<BufferedImage> renderPages(byte[] pdf) throws IOException {
        List<BufferedImage> pages = new ArrayList<>();
        try (PDDocument document = PDDocument.load(pdf)) {
            PDFRenderer renderer = new PDFRenderer(document);
            for (int i = 0; i < document.getNumberOfPages(); i++) {
                BufferedImage rendered = renderer.renderImageWithDPI(i, DPI);
                BufferedImage target = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_BYTE_GRAY);
                Graphics2D graphics = target.createGraphics();
                graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                graphics.drawImage(rendered, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT, null);
                graphics.dispose();
                pages.add(target);
            }
        }
        return pages;
    }

    private void appendTsplLabel(StringBuilder content, BufferedImage image) throws IOException {
        content.append("SIZE 76 mm,130 mm\r\n")
                .append("GAP 2 mm,0\r\n")
                .append("CLS\r\n")
                .append("BITMAP 0,0,")
                .append(IMAGE_WIDTH_BYTES).append(',').append(IMAGE_HEIGHT).append(",0,")
                .append(toBitmapHex(image)).append("\r\n")
                .append("PRINT 1\r\n");
    }

    private String toBitmapHex(BufferedImage image) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(IMAGE_WIDTH_BYTES * IMAGE_HEIGHT);
        for (int y = 0; y < IMAGE_HEIGHT; y++) {
            for (int byteX = 0; byteX < IMAGE_WIDTH_BYTES; byteX++) {
                int value = 0;
                for (int bit = 0; bit < 8; bit++) {
                    int x = byteX * 8 + bit;
                    if (x < IMAGE_WIDTH && (image.getRaster().getSample(x, y, 0) & 0xff) < BLACK_THRESHOLD) {
                        value |= 0x80 >> bit;
                    }
                }
                bytes.write(value);
            }
        }
        byte[] bitmap = bytes.toByteArray();
        StringBuilder hex = new StringBuilder(bitmap.length * 2);
        for (byte value : bitmap) hex.append(String.format("%02X", value & 0xff));
        return hex.toString();
    }

    private String mapCloudStatus(Integer status) {
        if (status == null) return STATUS_SUBMITTED;
        // 汉印任务状态：-2 超时取消，-1 固件自动取消，0 待打印，1 已打印。
        if (status == 0) return STATUS_SUBMITTED;
        if (status == 1) return STATUS_SUCCESS;
        return STATUS_FAILED;
    }

    private void ensureSuccess(HPRTResult<?> result, String defaultMessage) {
        if (result == null || !Boolean.TRUE.equals(result.getStatus())) {
            String message = result == null ? null : result.getMsg();
            throw new ServiceException(StringUtils.hasText(message) ? message : defaultMessage);
        }
    }

    private String limitMessage(String message) {
        if (!StringUtils.hasText(message)) return "打印任务提交失败";
        return message.length() > 1000 ? message.substring(0, 1000) : message;
    }
}
