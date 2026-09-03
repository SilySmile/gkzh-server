package com.gkzh.xycc.service;

import java.util.List;
import com.gkzh.xycc.domain.UserSelection;

/**
 * 用户记录Service接口
 * 
 * @author gkzh
 * @date 2025-06-16
 */
public interface IUserSelectionService 
{
    /**
     * 查询用户记录
     * 
     * @param userSelectionId 用户记录主键
     * @return 用户记录
     */
    public UserSelection selectUserSelectionByUserSelectionId(Long userSelectionId);

    /**
     * 查询用户记录列表
     * 
     * @param userSelection 用户记录
     * @return 用户记录集合
     */
    public List<UserSelection> selectUserSelectionList(UserSelection userSelection);

    /**
     * 新增用户记录
     * 
     * @param userSelection 用户记录
     * @return 结果
     */
    public int insertUserSelection(Long activityId,UserSelection userSelection);

    /**
     * 修改用户记录
     * 
     * @param userSelection 用户记录
     * @return 结果
     */
    public int updateUserSelection(UserSelection userSelection);

    /**
     * 批量删除用户记录
     * 
     * @param userSelectionIds 需要删除的用户记录主键集合
     * @return 结果
     */
    public int deleteUserSelectionByUserSelectionIds(Long[] userSelectionIds);

    /**
     * 删除用户记录信息
     * 
     * @param userSelectionId 用户记录主键
     * @return 结果
     */
    public int deleteUserSelectionByUserSelectionId(Long userSelectionId);
}
