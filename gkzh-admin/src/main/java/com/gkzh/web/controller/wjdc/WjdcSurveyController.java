package com.gkzh.web.controller.wjdc;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.IOException;

import com.gkzh.wjdc.domain.WjdcSurveyOption;
import com.gkzh.wjdc.domain.WjdcSurveyQuestion;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.gkzh.common.annotation.Log;
import com.gkzh.common.core.controller.BaseController;
import com.gkzh.common.core.domain.AjaxResult;
import com.gkzh.common.enums.BusinessType;
import com.gkzh.wjdc.domain.WjdcSurvey;
import com.gkzh.wjdc.service.IWjdcSurveyService;
import com.gkzh.common.utils.poi.ExcelUtil;
import com.gkzh.common.core.page.TableDataInfo;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * 问卷管理Controller
 * 
 * @author gkzh
 * @date 2025-06-17
 */
@RestController
@RequestMapping("/wjdc/survey")
public class WjdcSurveyController extends BaseController
{
    @Autowired
    private IWjdcSurveyService wjdcSurveyService;

    /**
     * 查询问卷管理列表
     */
    @PreAuthorize("@ss.hasPermi('wjdc:survey:list')")
    @GetMapping("/list")
    public TableDataInfo list(WjdcSurvey wjdcSurvey)
    {
        startPage();
        List<WjdcSurvey> list = wjdcSurveyService.selectWjdcSurveyList(wjdcSurvey);
        return getDataTable(list);
    }

    /**
     * 导出问卷管理列表
     */
    @PreAuthorize("@ss.hasPermi('wjdc:survey:export')")
    @Log(title = "问卷管理", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public void export(HttpServletResponse response, WjdcSurvey wjdcSurvey)
    {
        List<WjdcSurvey> list = wjdcSurveyService.selectWjdcSurveyList(wjdcSurvey);
        ExcelUtil<WjdcSurvey> util = new ExcelUtil<WjdcSurvey>(WjdcSurvey.class);
        util.exportExcel(response, list, "问卷管理数据");
    }

    /**
     * 导出问卷详细信息（Excel格式）
     */
    @PreAuthorize("@ss.hasPermi('wjdc:survey:export')")
    @Log(title = "问卷管理", businessType = BusinessType.EXPORT)
    @PostMapping("/exportDetail")
    public void exportDetail(HttpServletResponse response, WjdcSurvey wjdcSurvey) throws IOException
    {
        List<WjdcSurvey> list = wjdcSurveyService.selectWjdcSurveyListWithDetails(wjdcSurvey);
        
        // 创建工作簿
        try (Workbook workbook = new XSSFWorkbook()) {
            
            // 1. 问卷基本信息工作表
            Sheet surveySheet = workbook.createSheet("问卷基本信息");
            Row headerRow = surveySheet.createRow(0);
            String[] headers = {"编号", "问卷标题", "问卷说明", "状态", "开始时间", "结束时间", "创建时间"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }
            
            int rowNum = 1;
            for (WjdcSurvey survey : list) {
                Row row = surveySheet.createRow(rowNum++);
                row.createCell(0).setCellValue(survey.getId());
                row.createCell(1).setCellValue(survey.getTitle());
                row.createCell(2).setCellValue(survey.getDescription());
                row.createCell(3).setCellValue("1".equals(survey.getStatus()) ? "启用" : "禁用");
                row.createCell(4).setCellValue(survey.getStartTime());
                row.createCell(5).setCellValue(survey.getEndTime());
                row.createCell(6).setCellValue(survey.getCreatedAt());
            }
            
            // 2. 问题信息工作表
            Sheet questionSheet = workbook.createSheet("问题信息");
            Row questionHeaderRow = questionSheet.createRow(0);
            String[] questionHeaders = {"问卷ID", "问卷标题", "问题ID", "排序", "问题标题", "问题类型", "是否必填"};
            for (int i = 0; i < questionHeaders.length; i++) {
                Cell cell = questionHeaderRow.createCell(i);
                cell.setCellValue(questionHeaders[i]);
            }
            
            int questionRowNum = 1;
            for (WjdcSurvey survey : list) {
                if (survey.getQuestions() != null) {
                    for (WjdcSurveyQuestion question : survey.getQuestions()) {
                        Row row = questionSheet.createRow(questionRowNum++);
                        row.createCell(0).setCellValue(survey.getId());
                        row.createCell(1).setCellValue(survey.getTitle());
                        row.createCell(2).setCellValue(question.getId());
                        row.createCell(3).setCellValue(question.getSortOrder());
                        row.createCell(4).setCellValue(question.getQuestionTitle());
                        row.createCell(5).setCellValue("1".equals(question.getQuestionType()) ? "单选题" : 
                                                      "2".equals(question.getQuestionType()) ? "多选题" : "填空题");
                        row.createCell(6).setCellValue("1".equals(question.getRequired()) ? "是" : "否");
                    }
                }
            }
            
            // 3. 选项信息工作表
            Sheet optionSheet = workbook.createSheet("选项信息");
            Row optionHeaderRow = optionSheet.createRow(0);
            String[] optionHeaders = {"问卷ID", "问卷标题", "问题ID", "问题标题", "选项ID", "排序", "选项内容"};
            for (int i = 0; i < optionHeaders.length; i++) {
                Cell cell = optionHeaderRow.createCell(i);
                cell.setCellValue(optionHeaders[i]);
            }
            
            int optionRowNum = 1;
            for (WjdcSurvey survey : list) {
                if (survey.getQuestions() != null) {
                    for (WjdcSurveyQuestion question : survey.getQuestions()) {
                        if (question.getOptions() != null) {
                            for (WjdcSurveyOption option : question.getOptions()) {
                                Row row = optionSheet.createRow(optionRowNum++);
                                row.createCell(0).setCellValue(survey.getId());
                                row.createCell(1).setCellValue(survey.getTitle());
                                row.createCell(2).setCellValue(question.getId());
                                row.createCell(3).setCellValue(question.getQuestionTitle());
                                row.createCell(4).setCellValue(option.getId());
                                row.createCell(5).setCellValue(option.getSortOrder());
                                row.createCell(6).setCellValue(option.getOptionText());
                            }
                        }
                    }
                }
            }
            
            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=survey_detail_" + System.currentTimeMillis() + ".xlsx");
            
            // 写入响应流
            workbook.write(response.getOutputStream());
        }
    }

    /**
     * 获取问卷管理详细信息
     */
    @PreAuthorize("@ss.hasPermi('wjdc:survey:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(wjdcSurveyService.selectWjdcSurveyById(id));
    }

    /**
     * 新增问卷管理
     */
    @PreAuthorize("@ss.hasPermi('wjdc:survey:add')")
    @Log(title = "问卷管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody WjdcSurvey wjdcSurvey)
    {
        return toAjax(wjdcSurveyService.insertWjdcSurvey(wjdcSurvey));
    }

    /**
     * 修改问卷管理
     */
    @PreAuthorize("@ss.hasPermi('wjdc:survey:edit')")
    @Log(title = "问卷管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody WjdcSurvey wjdcSurvey)
    {
        return toAjax(wjdcSurveyService.updateWjdcSurvey(wjdcSurvey));
    }

    /**
     * 删除问卷管理
     */
    @PreAuthorize("@ss.hasPermi('wjdc:survey:remove')")
    @Log(title = "问卷管理", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(wjdcSurveyService.deleteWjdcSurveyByIds(ids));
    }

    /**
     * 复制问卷
     */
    @PreAuthorize("@ss.hasPermi('wjdc:survey:add')")
    @Log(title = "问卷管理", businessType = BusinessType.INSERT)
    @ApiOperation("复制问卷")
    @PostMapping("/copy/{surveyId}")
    public AjaxResult copySurvey(@PathVariable Long surveyId) {
        try {
            Long newSurveyId = wjdcSurveyService.copySurvey(surveyId);
            return AjaxResult.success("复制成功", newSurveyId);
        } catch (Exception e) {
            return AjaxResult.error("复制失败: " + e.getMessage());
        }
    }
}
