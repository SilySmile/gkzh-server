package com.gkzh.web.controller.wjdc;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.gkzh.wjdc.domain.WjdcSurveyQuestionStatistics;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.gkzh.common.annotation.Log;
import com.gkzh.common.core.controller.BaseController;
import com.gkzh.common.core.domain.AjaxResult;
import com.gkzh.common.enums.BusinessType;
import com.gkzh.wjdc.domain.WjdcSurveyStatistics;
import com.gkzh.wjdc.service.IWjdcSurveyStatisticsService;
import com.gkzh.common.utils.poi.ExcelUtil;

/**
 * 问卷统计Controller
 * 
 * @author gkzh
 * @date 2025-06-17
 */
@RestController
@RequestMapping("/wjdc/statistics")
public class WjdcSurveyStatisticsController extends BaseController
{
    @Autowired
    private IWjdcSurveyStatisticsService wjdcSurveyStatisticsService;

    /**
     * 查询所有问卷的答卷数量统计
     */
    @PreAuthorize("@ss.hasPermi('wjdc:statistics:list')")
    @GetMapping("/survey/count")
    public AjaxResult getSurveyResponseCount()
    {
        List<WjdcSurveyStatistics> list = wjdcSurveyStatisticsService.selectAllSurveyResponseCount();
        return success(list);
    }

    /**
     * 查询指定问卷的答卷统计
     */
    @PreAuthorize("@ss.hasPermi('wjdc:statistics:list')")
    @GetMapping("/survey/{surveyId}")
    public AjaxResult getSurveyResponseStatistics(@PathVariable("surveyId") Long surveyId)
    {
        List<WjdcSurveyStatistics> list = wjdcSurveyStatisticsService.selectSurveyResponseStatistics(surveyId);
        return success(list);
    }

    /**
     * 查询指定问卷的所有问题统计
     */
    @PreAuthorize("@ss.hasPermi('wjdc:statistics:list')")
    @GetMapping("/survey/{surveyId}/questions")
    public AjaxResult getSurveyQuestionStatistics(@PathVariable("surveyId") Long surveyId)
    {
        List<WjdcSurveyStatistics> list = wjdcSurveyStatisticsService.selectSurveyQuestionStatistics(surveyId);
        return success(list);
    }

    /**
     * 查询问题选项统计
     */
    @PreAuthorize("@ss.hasPermi('wjdc:statistics:list')")
    @GetMapping("/question/{questionId}/options")
    public AjaxResult getQuestionOptionStatistics(@PathVariable("questionId") Long questionId)
    {
        List<WjdcSurveyStatistics> list = wjdcSurveyStatisticsService.selectQuestionOptionStatistics(questionId);
        return success(list);
    }

    /**
     * 查询填空题答案统计
     */
    @PreAuthorize("@ss.hasPermi('wjdc:statistics:list')")
    @GetMapping("/question/{questionId}/answers")
    public AjaxResult getQuestionAnswerStatistics(@PathVariable("questionId") Long questionId)
    {
        List<WjdcSurveyStatistics> list = wjdcSurveyStatisticsService.selectQuestionAnswerStatistics(questionId);
        return success(list);
    }


    /**
     * 查询选项打分题统计详情
     */
    @PreAuthorize("@ss.hasPermi('wjdc:statistics:list')")
    @GetMapping("/question/{questionId}/score-options")
    public AjaxResult getQuestionScoreOptionStatistics(@PathVariable("questionId") Long questionId) {
        List<WjdcSurveyStatistics> list = wjdcSurveyStatisticsService.selectQuestionOptionScoreStatistics(questionId);
        return success(list);
    }
    /**
     * 导出问卷统计
     */
    @PreAuthorize("@ss.hasPermi('wjdc:statistics:export')")
    @Log(title = "问卷统计", businessType = BusinessType.EXPORT)
    @PostMapping("/export/{surveyId}")
    public void export(HttpServletResponse response, @PathVariable("surveyId") Long surveyId) throws IOException {
        // 获取完整统计信息
        List<WjdcSurveyQuestionStatistics> statistics = wjdcSurveyStatisticsService.getSurveyFullStatistics(surveyId);

        // 获取问卷标题
        List<WjdcSurveyStatistics> surveyStats = wjdcSurveyStatisticsService.selectSurveyResponseStatistics(surveyId);
        String surveyTitle = (surveyStats != null && !surveyStats.isEmpty()) ? surveyStats.get(0).getSurveyTitle() : "问卷统计";

        // 创建Excel工作簿
        Workbook workbook = new SXSSFWorkbook();

        Sheet sheet = workbook.createSheet("问卷统计");
        // 设置样式
        CellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 16);
        titleStyle.setFont(titleFont);
        titleStyle.setAlignment(HorizontalAlignment.CENTER);

        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);

        // 写入标题
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(surveyTitle + "-统计");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));

        int rowNum = 2;

        // 写入每个问题的统计信息
        for (int i = 0; i < statistics.size(); i++) {
            WjdcSurveyQuestionStatistics questionStat = statistics.get(i);

            // 写入问题标题
            Row questionRow = sheet.createRow(rowNum++);
            Cell questionCell = questionRow.createCell(0);
            questionCell.setCellValue((i + 1) + "、" + questionStat.getQuestionTitle() + questionStat.getQuestionTypeName());

            if ("1".equals(questionStat.getQuestionType()) || "2".equals(questionStat.getQuestionType())) {
                // 选择题处理
                // 表头
                Row headerRow = sheet.createRow(rowNum++);
                String[] headers = {"选项", "小计", "比例"};
                for (int j = 0; j < headers.length; j++) {
                    Cell cell = headerRow.createCell(j);
                    cell.setCellValue(headers[j]);
                    cell.setCellStyle(headerStyle);
                }

                // 选项数据
                List<WjdcSurveyStatistics> options = questionStat.getOptions();
                for (WjdcSurveyStatistics option : options) {
                    Row dataRow = sheet.createRow(rowNum++);
                    dataRow.createCell(0).setCellValue(option.getOptionText());
                    dataRow.createCell(1).setCellValue(option.getSelectCount() != null ? option.getSelectCount() : 0);
                    dataRow.createCell(2).setCellValue(option.getSelectPercentage() != null ? option.getSelectPercentage() : "0%");

                    // 设置数据样式
                    for (int j = 0; j < 3; j++) {
                        dataRow.getCell(j).setCellStyle(dataStyle);
                    }
                }

                // 有效填写人数
                Row totalRow = sheet.createRow(rowNum++);
                totalRow.createCell(0).setCellValue("本题有效填写人数");
                totalRow.createCell(1).setCellValue(questionStat.getTotalResponses());

                // 设置样式
                for (int j = 0; j < 2; j++) {
                    totalRow.getCell(j).setCellStyle(dataStyle);
                }

            } else if ("3".equals(questionStat.getQuestionType()) || "4".equals(questionStat.getQuestionType())) {
                // 填空题处理
                // 表头
                Row headerRow = sheet.createRow(rowNum++);
                String[] headers = {"答案内容", "小计", "比例"};
                for (int j = 0; j < headers.length; j++) {
                    Cell cell = headerRow.createCell(j);
                    cell.setCellValue(headers[j]);
                    cell.setCellStyle(headerStyle);
                }

                // 答案数据
                List<WjdcSurveyStatistics> answers = questionStat.getAnswers();
                for (WjdcSurveyStatistics answer : answers) {
                    Row dataRow = sheet.createRow(rowNum++);
                    dataRow.createCell(0).setCellValue(answer.getAnswerText());
                    dataRow.createCell(1).setCellValue(answer.getAnswerCount());

                    // 计算比例：答案数量 / 该题总答题人数
                    String percentage = "0%";
                    if (questionStat.getTotalResponses() != null && questionStat.getTotalResponses() > 0) {
                        double ratio = (double) answer.getAnswerCount() / questionStat.getTotalResponses() * 100;
                        percentage = String.format("%.2f", ratio) + "%";
                    }
                    dataRow.createCell(2).setCellValue(percentage);

                    // 设置数据样式
                    for (int j = 0; j < 3; j++) {
                        dataRow.getCell(j).setCellStyle(dataStyle);
                    }
                }
                // 有效填写人数
                Row totalRow = sheet.createRow(rowNum++);
                totalRow.createCell(0).setCellValue("本题有效填写人数");
                totalRow.createCell(1).setCellValue(questionStat.getTotalResponses());
                // 设置样式
                for (int j = 0; j < 2; j++) {
                    totalRow.getCell(j).setCellStyle(dataStyle);
                }
            }else if ("5".equals(questionStat.getQuestionType())) {
                // 选项打分题处理
                // 表头
                Row headerRow = sheet.createRow(rowNum++);
                String[] headers = {"选项", "平均数", "标准差"};
                for (int j = 0; j < headers.length; j++) {
                    Cell cell = headerRow.createCell(j);
                    cell.setCellValue(headers[j]);
                    cell.setCellStyle(headerStyle);
                }

                // 答案数据
                List<WjdcSurveyStatistics> answers = questionStat.getAnswers();
                for (WjdcSurveyStatistics answer : answers) {
                    Row dataRow = sheet.createRow(rowNum++);
                    dataRow.createCell(0).setCellValue(answer.getOptionText());
                    
                    // 平均数（存储在averageScore字段中）
                    double average = answer.getAverageScore() != null ? answer.getAverageScore() : 0.0;
                    dataRow.createCell(1).setCellValue(String.format("%.2f", average));
                    
                    // 标准差（存储在standardDeviation字段中）
                    double stdDev = answer.getStandardDeviation() != null ? answer.getStandardDeviation() : 0.0;
                    dataRow.createCell(2).setCellValue(String.format("%.2f", stdDev));

                    // 设置数据样式
                    for (int j = 0; j < 3; j++) {
                        dataRow.getCell(j).setCellStyle(dataStyle);
                    }
                }
                // 有效填写人数
                Row totalRow = sheet.createRow(rowNum++);
                totalRow.createCell(0).setCellValue("本题有效填写人数");
                totalRow.createCell(1).setCellValue(questionStat.getTotalResponses());
                // 设置样式
                for (int j = 0; j < 2; j++) {
                    totalRow.getCell(j).setCellStyle(dataStyle);
                }
            }

            // 空行分隔
            rowNum++;
        }

        sheet.setColumnWidth(0, 30 * 256);  // 选项/答案内容列
        sheet.setColumnWidth(1, 15 * 256);  // 小计列
        sheet.setColumnWidth(2, 15 * 256);  // 比例列
        sheet.setColumnWidth(3, 20 * 256);  // 备用列

        // 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(surveyTitle + "-统计.xlsx", "UTF-8"));

        // 写入响应
        workbook.write(response.getOutputStream());
        workbook.close();
    }
} 