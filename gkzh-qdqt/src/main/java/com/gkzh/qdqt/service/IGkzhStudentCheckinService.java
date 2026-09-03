package com.gkzh.qdqt.service;

import java.util.List;

import com.gkzh.common.core.domain.model.StudentCheckin;
import com.gkzh.qdqt.domain.GkzhStudentCheckin;
import com.gkzh.qdqt.dto.*;
import com.gkzh.qdqt.vo.GkzhStudentCheckinExportVO;

/**
 * 签到签退Service接口
 * 
 * @author gkzh
 * @date 2025-06-22
 */
public interface IGkzhStudentCheckinService 
{
    /**
     * 查询签到签退
     * 
     * @param checkinId 签到签退主键
     * @return 签到签退
     */
    public GkzhStudentCheckin selectGkzhStudentCheckinByCheckinId(Long checkinId);

    /**
     * 查询签到签退列表
     * 
     * @param gkzhStudentCheckinDTO 签到签退
     * @return 签到签退集合
     */
    public List<GkzhStudentCheckinExportVO> selectGkzhStudentCheckinList(GkzhStudentCheckinDTO gkzhStudentCheckinDTO);

    /**
     * 新增签到签退
     * 
     * @param gkzhStudentCheckin 签到签退
     * @return 结果
     */
    public int insertGkzhStudentCheckin(GkzhStudentCheckin gkzhStudentCheckin);

    /**
     * 修改签到签退
     * 
     * @param gkzhStudentCheckin 签到签退
     * @return 结果
     */
    public int updateGkzhStudentCheckin(GkzhStudentCheckin gkzhStudentCheckin);

    /**
     * 批量删除签到签退
     * 
     * @param checkinIds 需要删除的签到签退主键集合
     * @return 结果
     */
    public int deleteGkzhStudentCheckinByCheckinIds(Long[] checkinIds);

    /**
     * 删除签到签退信息
     * 
     * @param checkinId 签到签退主键
     * @return 结果
     */
    public int deleteGkzhStudentCheckinByCheckinId(Long checkinId);


    public StudentTokenResponse checkin(StudentLoginRequest request);

    public boolean signout(StudentCheckin studentCheckin, Long activityId);


    /**
     * 学生注册（仅注册，不含签到）
     */
    public StudentTokenResponse register(StudentRegisterRequest request);

    /**
     * 学生登录（仅登录，不含签到）
     */
    public StudentTokenResponse login(StudentLoginSimpleRequest request);
}


