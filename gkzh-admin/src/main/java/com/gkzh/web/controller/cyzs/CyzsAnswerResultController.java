package com.gkzh.web.controller.cyzs;

import com.gkzh.common.core.controller.BaseController;
import com.gkzh.common.core.domain.AjaxResult;
import com.gkzh.common.core.page.TableDataInfo;
import com.gkzh.common.utils.poi.ExcelUtil;
import com.gkzh.cyzs.domain.CyzsGameRound;
import com.gkzh.cyzs.service.ICyzsQuestionService;
import com.gkzh.cyzs.vo.AnswerDetailVO;
import com.gkzh.cyzs.vo.UserAnswerRecordVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 答题结果Controller
 *
 * @author gkzh
 */
@RestController
@RequestMapping("/cyzs/answer/result")
public class CyzsAnswerResultController extends BaseController {

    @Autowired
    private ICyzsQuestionService cyzsQuestionService;

    /**
     * 查询答题结果列表
     */
    @PreAuthorize("@ss.hasPermi('cyzs:result:list')")
    @GetMapping("/list")
    public TableDataInfo list(CyzsGameRound gameRound) {
        startPage();
        List<CyzsGameRound> list = cyzsQuestionService.selectGameRoundList(gameRound);
        return getDataTable(list);
    }

    /**
     * 导出答题结果列表
     */
    @PreAuthorize("@ss.hasPermi('cyzs:result:export')")
    @GetMapping("/export")
    public void export(HttpServletResponse response, CyzsGameRound gameRound) {
        List<CyzsGameRound> list = cyzsQuestionService.selectGameRoundList(gameRound);
        ExcelUtil<CyzsGameRound> util = new ExcelUtil<>(CyzsGameRound.class);
        util.exportExcel(response, list, "答题结果数据");
    }
    /**
     * 导出统计结果
     */
    @PreAuthorize("@ss.hasPermi('cyzs:result:export')")
    @PostMapping("/exportUserAnswerRecords")
    public void exportUserAnswerRecords(HttpServletResponse response) {
        List<UserAnswerRecordVO> list = cyzsQuestionService.selectUserAnswerRecordsForExport();
        ExcelUtil<UserAnswerRecordVO> util = new ExcelUtil<>(UserAnswerRecordVO.class);
        util.exportExcel(response, list, "用户答题记录数据");
    }

    /**
     * 查询答题详情列表
     */
    @PreAuthorize("@ss.hasPermi('cyzs:result:detail')")
    @GetMapping("/detail/{roundId}")
    public AjaxResult list(@PathVariable Long roundId) {
        List<AnswerDetailVO> list = cyzsQuestionService.selectAnswerDetailWithQuestionByRoundId(roundId);
        return AjaxResult.success(list);
    }
}
