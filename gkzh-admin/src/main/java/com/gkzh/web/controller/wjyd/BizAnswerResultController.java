package com.gkzh.web.controller.wjyd;

import com.gkzh.common.core.controller.BaseController;
import com.gkzh.common.core.domain.AjaxResult;
import com.gkzh.common.core.page.TableDataInfo;
import com.gkzh.common.utils.poi.ExcelUtil;
import com.gkzh.wjyd.domain.BizAnswerDetail;
import com.gkzh.wjyd.domain.BizGameRound;
import com.gkzh.wjyd.dto.GameRoundQueryDTO;
import com.gkzh.wjyd.service.IBizQuestionService;
import com.gkzh.wjyd.vo.AnswerDetailVO;
import com.gkzh.wjyd.vo.QuestionStatisticsVO;
import com.gkzh.wjyd.vo.UserAnswerRecordVO;
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
@RequestMapping("/answer/result")
public class BizAnswerResultController extends BaseController {

    @Autowired
    private IBizQuestionService bizQuestionService;

    /**
     * 查询答题结果列表
     */
    @PreAuthorize("@ss.hasPermi('wjyd:result:list')")
    @GetMapping("/list")
    public TableDataInfo list(GameRoundQueryDTO queryDTO) {
        startPage();
        List<BizGameRound> list = bizQuestionService.selectGameRoundList(queryDTO);
        return getDataTable(list);
    }

    /**
     * 导出答题结果列表
     */
    @PreAuthorize("@ss.hasPermi('wjyd:result:export')")
    @GetMapping("/export")
    public void export(HttpServletResponse response, GameRoundQueryDTO queryDTO) {
        List<BizGameRound> list = bizQuestionService.selectGameRoundList(queryDTO);
        ExcelUtil<BizGameRound> util = new ExcelUtil<>(BizGameRound.class);
        util.exportExcel(response, list, "答题结果数据");
    }
    /**
     * 导出统计结果
     */
    @PreAuthorize("@ss.hasPermi('wjyd:result:export')")
    @PostMapping("/exportUserAnswerRecords")
    public void exportUserAnswerRecords(HttpServletResponse response,GameRoundQueryDTO queryDTO) {
        List<UserAnswerRecordVO> list = bizQuestionService.selectUserAnswerRecordsForExport(queryDTO);
        ExcelUtil<UserAnswerRecordVO> util = new ExcelUtil<>(UserAnswerRecordVO.class);
        util.exportExcel(response, list, "用户答题记录数据");
    }

    /**
     * 查询答题详情列表
     */
    @PreAuthorize("@ss.hasPermi('wjyd:result:detail')")
    @GetMapping("/detail/{roundId}")
    public AjaxResult list(@PathVariable Long roundId) {
        List<AnswerDetailVO> list = bizQuestionService.selectAnswerDetailWithQuestionByRoundId(roundId);
        return AjaxResult.success(list);
    }
}
