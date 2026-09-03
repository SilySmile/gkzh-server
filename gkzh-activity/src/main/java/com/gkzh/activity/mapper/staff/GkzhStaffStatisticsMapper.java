package com.gkzh.activity.mapper.staff;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface GkzhStaffStatisticsMapper {
    @Select("select us.pattern_combo_code as code, count(distinct us.user_id) as userCount, round(count(distinct us.user_id) * 100.0 / nullif((select count(distinct us2.user_id) from xycc_user_selection us2 join gkzh_student s2 on s2.user_id = us2.user_id where s2.school_id = #{schoolId}), 0), 2) as probability, (select group_concat(case h.code when 'R' then '现实型' when 'I' then '研究型' when 'A' then '艺术型' when 'S' then '社会型' when 'E' then '企业型' when 'C' then '常规型' end order by field(h.code, 'R','I','A','S','E','C') separator '+') from xycc_holland_code h where locate(h.code, us.pattern_combo_code) > 0) as codeSummary from xycc_user_selection us join gkzh_student s on s.user_id = us.user_id where s.school_id = #{schoolId} group by us.pattern_combo_code order by userCount desc")
    List<Map<String, Object>> selectCodeStatistics(Long schoolId);
}
