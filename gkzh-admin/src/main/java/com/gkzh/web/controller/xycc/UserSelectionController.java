package com.gkzh.web.controller.xycc;

import java.util.List;
import java.time.LocalDate;
import javax.servlet.http.HttpServletResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.gkzh.common.annotation.Log;
import com.gkzh.common.core.controller.BaseController;
import com.gkzh.common.core.domain.AjaxResult;
import com.gkzh.common.enums.BusinessType;
import com.gkzh.common.exception.ServiceException;
import com.gkzh.xycc.domain.UserSelection;
import com.gkzh.xycc.domain.dto.XyccStatisticsQuery;
import com.gkzh.xycc.service.IUserSelectionService;
import com.gkzh.xycc.service.XyccStatisticsService;
import com.gkzh.common.utils.poi.ExcelUtil;
import com.gkzh.common.core.page.TableDataInfo;

/**
 * 用户记录Controller
 * 
 * @author gkzh
 * @date 2025-06-16
 */
@RestController
@RequestMapping("/xycc/selection")
public class UserSelectionController extends BaseController
{
    @Autowired
    private IUserSelectionService userSelectionService;

    @Autowired
    private XyccStatisticsService statisticsService;

    /**
     * 用户记录列表：按学校、活动、院系、专业、性别、编码和完成日期筛选。
     * 返回学生、学校、院系、专业、所选图案和霍兰德类型，替代只展示账号字段的旧列表。
     */
    @PreAuthorize("@ss.hasPermi('xycc:selection:list')")
    @GetMapping("/records")
    public AjaxResult records(@RequestParam(required = false) Long instanceId,
                              @RequestParam(required = false) Long schoolId,
                              @RequestParam(required = false) Long departmentId,
                              @RequestParam(required = false) String major,
                              @RequestParam(required = false) String gender,
                              @RequestParam(required = false) String studentNo,
                              @RequestParam(required = false) String code,
                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                              @RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "10") Integer pageSize) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new ServiceException("开始日期不能晚于结束日期");
        }
        XyccStatisticsQuery query = new XyccStatisticsQuery();
        query.setInstanceId(instanceId);
        query.setSchoolId(schoolId);
        query.setDepartmentId(departmentId);
        query.setMajor(trimToNull(major));
        query.setGenderValue(trimToNull(gender));
        query.setStudentNo(trimToNull(studentNo));
        String codeValue = trimToNull(code);
        query.setCode(codeValue == null ? null : codeValue.toUpperCase());
        query.setStartDate(startDate);
        query.setEndDate(endDate == null ? null : endDate.plusDays(1));
        return AjaxResult.success(statisticsService.records(query, pageNum, pageSize));
    }

    /**
     * 查询用户记录列表
     */
    @PreAuthorize("@ss.hasPermi('xycc:selection:list')")
    @GetMapping("/list")
    public TableDataInfo list(UserSelection userSelection)
    {
        startPage();
        List<UserSelection> list = userSelectionService.selectUserSelectionList(userSelection);
        return getDataTable(list);
    }

    /**
     * 导出用户记录列表
     */
    @PreAuthorize("@ss.hasPermi('xycc:selection:export')")
    @Log(title = "用户记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, UserSelection userSelection)
    {
        List<UserSelection> list = userSelectionService.selectUserSelectionList(userSelection);
        ExcelUtil<UserSelection> util = new ExcelUtil<UserSelection>(UserSelection.class);
        util.exportExcel(response, list, "用户记录数据");
    }

    /**
     * 获取用户记录详细信息
     */
    @PreAuthorize("@ss.hasPermi('xycc:selection:query')")
    @GetMapping(value = "/{userSelectionId}")
    public AjaxResult getInfo(@PathVariable("userSelectionId") Long userSelectionId)
    {
        return success(userSelectionService.selectUserSelectionByUserSelectionId(userSelectionId));
    }



    /**
     * 删除用户记录
     */
    @PreAuthorize("@ss.hasPermi('xycc:selection:remove')")
    @Log(title = "用户记录", businessType = BusinessType.DELETE)
	@DeleteMapping("/{userSelectionIds}")
    public AjaxResult remove(@PathVariable Long[] userSelectionIds)
    {
        return toAjax(userSelectionService.deleteUserSelectionByUserSelectionIds(userSelectionIds));
    }

    private static String trimToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }
}
