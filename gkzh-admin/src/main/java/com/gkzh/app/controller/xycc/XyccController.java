package com.gkzh.app.controller.xycc;

import com.gkzh.app.dto.CodeRequest;
import com.gkzh.app.dto.XyccResult;
import com.gkzh.common.core.controller.FrontBaseController;
import com.gkzh.common.core.domain.AjaxResult;
import com.gkzh.app.dto.PatternSelected;
import com.gkzh.common.utils.DateUtils;
import com.gkzh.xycc.domain.Career;
import com.gkzh.xycc.domain.UserSelection;
import com.gkzh.xycc.domain.WorkEnv;
import com.gkzh.xycc.service.IPatternComboService;
import com.gkzh.xycc.service.IPatternService;
import com.gkzh.xycc.service.IUserSelectionService;
import com.gkzh.xycc.service.IHollandCodeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@Api("心愿橱窗")
@RestController
@RequestMapping("/api/xycc")
public class XyccController extends FrontBaseController {
    @Autowired
    private IPatternService patternService;

    @Autowired
    private IUserSelectionService userSelectionService;

    @Autowired
    private IPatternComboService patternComboService;

    @Autowired
    private IHollandCodeService hollandCodeService;
    @ApiOperation("获取心愿橱窗列表")
    @GetMapping("/{activityId}")
    public AjaxResult getXyccInfo(@PathVariable Long activityId)
    {
        return AjaxResult.success(patternService.selectPatternList(null));
    }

    @ApiOperation("获取霍兰德编码解释列表")
    @GetMapping("/codes")
    public AjaxResult codes()
    {
        return AjaxResult.success(hollandCodeService.listCodes());
    }

    @ApiOperation("获取单个霍兰德编码解释")
    @GetMapping("/code/{code}")
    public AjaxResult code(@PathVariable String code)
    {
        return AjaxResult.success(hollandCodeService.getCode(code));
    }


    @ApiOperation("提交选择")
    @PostMapping("/selected")
    public AjaxResult selectPattern(@RequestBody PatternSelected patternSelected)
    {

        UserSelection userSelection = new UserSelection();

        userSelection.setCreatedAt(DateUtils.getNowDate());
        userSelection.setPatternIds(patternSelected.getPatternIds());
        userSelection.setPatternComboCode(patternSelected.getCodeGroup());
        userSelection.setUserId(getCurrentStudent().getUserId());
        userSelection.setGameId(patternSelected.getGameId());
        userSelection.setUserName(getCurrentStudentNo());
        userSelection.setNickName(getCurrentStudentName());
        Long activityId = patternSelected.getActivityId();
        return AjaxResult.success(userSelectionService.insertUserSelection(activityId,userSelection));
    }

    @ApiOperation("获取结果")
    @PostMapping("/result")
    public AjaxResult getResult(@RequestBody Map map){
        Long activityId = Long.valueOf(map.get("activityId").toString());
        //根据activityId、userId查询用户选择的patternIds
        Long userId = getCurrentStudent().getUserId();
        Map xyccResult = patternComboService.getXyccResult(activityId, userId);
        return AjaxResult.success(xyccResult);
    }

}
