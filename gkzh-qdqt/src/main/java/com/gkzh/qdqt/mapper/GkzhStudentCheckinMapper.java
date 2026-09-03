package com.gkzh.qdqt.mapper;

import java.util.List;
import com.gkzh.qdqt.domain.GkzhStudentCheckin;
import com.gkzh.qdqt.dto.GkzhStudentCheckinDTO;
import com.gkzh.qdqt.vo.GkzhStudentCheckinExportVO;
import org.apache.ibatis.annotations.Param;

/**
 * 签到签退Mapper接口
 * 
 * @author gkzh
 * @date 2025-06-22
 */
public interface GkzhStudentCheckinMapper 
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
     * @param gkzhStudentCheckin 签到签退
     * @return 签到签退集合
     */
    public List<GkzhStudentCheckin> selectGkzhStudentCheckinList(GkzhStudentCheckin gkzhStudentCheckin);
    public List<GkzhStudentCheckinExportVO> selectGkzhStudentCheckinList2(GkzhStudentCheckinDTO gkzhStudentCheckinDTO);

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
     * 删除签到签退
     * 
     * @param checkinId 签到签退主键
     * @return 结果
     */
    public int deleteGkzhStudentCheckinByCheckinId(Long checkinId);

    /**
     * 批量删除签到签退
     * 
     * @param checkinIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteGkzhStudentCheckinByCheckinIds(Long[] checkinIds);


    public GkzhStudentCheckin selectGkzhStudentCheckinByStudentIdAndActivityId(@Param("stuId") Long stuId, @Param("activityId") Long activityId);
}
