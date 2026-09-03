package com.gkzh.xycc.mapper;

import java.util.List;
import java.util.Map;
import com.gkzh.xycc.domain.UserSelection;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户记录Mapper接口
 * 
 * @author gkzh
 * @date 2025-06-16
 */
public interface UserSelectionMapper 
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
    public int insertUserSelection(UserSelection userSelection);

    /**
     * 修改用户记录
     * 
     * @param userSelection 用户记录
     * @return 结果
     */
    public int updateUserSelection(UserSelection userSelection);

    /**
     * 删除用户记录
     * 
     * @param userSelectionId 用户记录主键
     * @return 结果
     */
    public int deleteUserSelectionByUserSelectionId(Long userSelectionId);

    /**
     * 批量删除用户记录
     * 
     * @param userSelectionIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteUserSelectionByUserSelectionIds(Long[] userSelectionIds);


    public UserSelection getUserSelectionByActivityIdAndUserId(@Param("activityId") Long activityId,@Param("userId") Long userId);

    /** 只要当前活动游戏没有该用户的选择结果，就不能视为已完成并跳转报告。 */
    @Select("SELECT * FROM xycc_user_selection WHERE game_id=#{gameId} AND user_id=#{userId} ORDER BY user_selection_id DESC LIMIT 1")
    UserSelection selectByGameIdAndUserId(@Param("gameId") Long gameId,@Param("userId") Long userId);

    /** 查询指定活动游戏的心愿橱窗复合编码统计。 */
    @Select("select pc.code, coalesce(stat.user_count, 0) user_count, coalesce(total.total_count, 0) total_count " +
            "from xycc_pattern_combo pc " +
            "left join (" +
            " select us.pattern_combo_code code, count(distinct gp.user_id) user_count " +
            " from gkzh_game_participation gp " +
            " join (select user_id, max(participation_id) participation_id from gkzh_game_participation where game_id = #{gameId} group by user_id) latest on latest.participation_id = gp.participation_id " +
            " join gkzh_activity_participation_record ar on ar.activity_id = gp.instance_id and ar.user_id = gp.user_id and ar.participation_type = 4 " +
            " join xycc_user_selection us on us.user_selection_id = ar.module_id " +
            " where gp.status = '1' and (us.game_id = #{gameId} or us.game_id is null) group by us.pattern_combo_code" +
            ") stat on stat.code = pc.code " +
            "left join (" +
            " select count(distinct gp.user_id) total_count " +
            " from gkzh_game_participation gp " +
            " join (select user_id, max(participation_id) participation_id from gkzh_game_participation where game_id = #{gameId} group by user_id) latest on latest.participation_id = gp.participation_id " +
            " join gkzh_activity_participation_record ar on ar.activity_id = gp.instance_id and ar.user_id = gp.user_id and ar.participation_type = 4 " +
            " join xycc_user_selection us on us.user_selection_id = ar.module_id " +
            " where gp.status = '1' and (us.game_id = #{gameId} or us.game_id is null)" +
            ") total on 1 = 1 where coalesce(stat.user_count, 0) > 0 order by coalesce(stat.user_count, 0) desc, pc.code")
    List<Map<String, Object>> selectCodeStatsByGameId(@Param("gameId") Long gameId);
}
