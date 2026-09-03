package com.gkzh.xycc.service.impl;

import java.util.List;

import com.gkzh.activity.domain.GkzhActivityParticipationRecord;
import com.gkzh.activity.service.IGkzhActivityParticipationRecordService;
import com.gkzh.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.gkzh.xycc.mapper.UserSelectionMapper;
import com.gkzh.xycc.domain.UserSelection;
import com.gkzh.xycc.service.IUserSelectionService;

/**
 * 用户记录Service业务层处理
 * 
 * @author gkzh
 * @date 2025-06-16
 */
@Service
public class UserSelectionServiceImpl implements IUserSelectionService 
{
    @Autowired
    private UserSelectionMapper userSelectionMapper;

    @Autowired
    private IGkzhActivityParticipationRecordService activityParticipationRecordService;
    /**
     * 查询用户记录
     * 
     * @param userSelectionId 用户记录主键
     * @return 用户记录
     */
    @Override
    public UserSelection selectUserSelectionByUserSelectionId(Long userSelectionId)
    {
        return userSelectionMapper.selectUserSelectionByUserSelectionId(userSelectionId);
    }

    /**
     * 查询用户记录列表
     * 
     * @param userSelection 用户记录
     * @return 用户记录
     */
    @Override
    public List<UserSelection> selectUserSelectionList(UserSelection userSelection)
    {
        return userSelectionMapper.selectUserSelectionList(userSelection);
    }

    /**
     * 新增用户记录
     * 
     * @param userSelection 用户记录
     * @return 结果
     */
    @Override
    public int insertUserSelection(Long activityId,UserSelection userSelection)
    {

        //重复提交要限制
//        Boolean participated = activityParticipationRecordService.isParticipated(userSelection.getUserId(), activityId, 4);
//        if (participated){
//            throw new RuntimeException("您已参与过本关卡");
//        }
        //1.插入用户选择记录表
        userSelectionMapper.insertUserSelection(userSelection);
        Long moduleId = userSelection.getUserSelectionId();
        //2.插入参与活动记录表
        GkzhActivityParticipationRecord record = new GkzhActivityParticipationRecord();
        record.setParticipationTime(DateUtils.getNowDate());
        record.setActivityId(activityId);
        record.setResult("心愿橱窗完成");
        record.setParticipationType(4);
        record.setUserId(userSelection.getUserId());
        record.setUserCode(userSelection.getUserName());
        record.setUserName(userSelection.getNickName());
        record.setModuleId(moduleId);
        activityParticipationRecordService.insertGkzhActivityParticipationRecord(record);
        return 1;
    }

    /**
     * 修改用户记录
     * 
     * @param userSelection 用户记录
     * @return 结果
     */
    @Override
    public int updateUserSelection(UserSelection userSelection)
    {
        return userSelectionMapper.updateUserSelection(userSelection);
    }

    /**
     * 批量删除用户记录
     * 
     * @param userSelectionIds 需要删除的用户记录主键
     * @return 结果
     */
    @Override
    public int deleteUserSelectionByUserSelectionIds(Long[] userSelectionIds)
    {
        return userSelectionMapper.deleteUserSelectionByUserSelectionIds(userSelectionIds);
    }

    /**
     * 删除用户记录信息
     * 
     * @param userSelectionId 用户记录主键
     * @return 结果
     */
    @Override
    public int deleteUserSelectionByUserSelectionId(Long userSelectionId)
    {
        return userSelectionMapper.deleteUserSelectionByUserSelectionId(userSelectionId);
    }
}
