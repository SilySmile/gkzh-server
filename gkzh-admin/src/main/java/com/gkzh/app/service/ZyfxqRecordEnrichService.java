package com.gkzh.app.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gkzh.activity.domain.week.GkzhActivityWeekInstance;
import com.gkzh.activity.service.IActivityWeekService;
import com.gkzh.school.domain.GkzhSchool;
import com.gkzh.school.domain.GkzhSchoolDepartment;
import com.gkzh.school.domain.GkzhStudent;
import com.gkzh.school.mapper.GkzhSchoolDepartmentMapper;
import com.gkzh.school.mapper.GkzhSchoolMapper;
import com.gkzh.school.mapper.GkzhStudentMapper;
import com.gkzh.zyfxq.domain.ZyfxqRecord;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 职业飞行棋参与记录展示字段补全：学校、院系、专业、学生姓名与学号。
 * 列表、统计、活动查看和 Excel 导出共用同一套规则，避免同一学生在不同页面显示不一致。
 */
@Service
public class ZyfxqRecordEnrichService {

    private final IActivityWeekService activityWeekService;
    private final GkzhSchoolMapper schoolMapper;
    private final GkzhSchoolDepartmentMapper departmentMapper;
    private final GkzhStudentMapper studentMapper;

    public ZyfxqRecordEnrichService(IActivityWeekService activityWeekService, GkzhSchoolMapper schoolMapper,
                                    GkzhSchoolDepartmentMapper departmentMapper, GkzhStudentMapper studentMapper) {
        this.activityWeekService = activityWeekService;
        this.schoolMapper = schoolMapper;
        this.departmentMapper = departmentMapper;
        this.studentMapper = studentMapper;
    }

    /** 批量补全展示字段；返回顺序与传入记录一致。 */
    public List<Map<String, Object>> enrich(List<ZyfxqRecord> records) {
        List<Map<String, Object>> rows = new ArrayList<>();
        if (records == null || records.isEmpty()) {
            return rows;
        }
        Map<Long, String> instanceNames = new HashMap<>();
        for (GkzhActivityWeekInstance instance : activityWeekService.listInstances(null)) {
            instanceNames.put(instance.getInstanceId(), instance.getTitle());
        }
        for (ZyfxqRecord record : records) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("recordId", record.getRecordId());
            row.put("instanceId", record.getInstanceId());
            row.put("instanceName", instanceNames.get(record.getInstanceId()));
            row.put("gameId", record.getGameId());
            row.put("userId", record.getUserId());
            row.put("studentId", record.getStudentId());
            row.put("schoolId", record.getSchoolId());
            row.put("departmentId", record.getDepartmentId());
            row.put("major", record.getMajor());
            row.put("attemptNo", record.getAttemptNo());
            row.put("status", record.getStatus());
            row.put("result", record.getResult());
            row.put("resultName", resultText(record.getResult()));
            row.put("position", record.getPosition());
            row.put("rollCount", record.getRollCount());
            row.put("stepTotal", record.getStepTotal());
            row.put("answerCount", record.getAnswerCount());
            row.put("correctCount", record.getCorrectCount());
            row.put("timeoutCount", record.getTimeoutCount());
            row.put("durationMs", record.getDurationMs());
            row.put("durationSeconds", record.getDurationMs() == null ? null
                    : Math.round(record.getDurationMs() / 100D) / 10D);
            row.put("scanTime", record.getScanTime());
            row.put("startTime", record.getStartTime());
            row.put("endTime", record.getEndTime());
            row.put("createTime", record.getCreateTime());
            row.put("updateTime", record.getUpdateTime());

            GkzhStudent student = resolveStudent(record);
            row.put("studentName", isBlank(record.getStudentName())
                    ? (student == null ? null : student.getStudentName()) : record.getStudentName());
            row.put("studentNo", isBlank(record.getStudentNo())
                    ? (student == null ? null : student.getStudentNo()) : record.getStudentNo());
            if (row.get("departmentId") == null && student != null) {
                row.put("departmentId", student.getDepartmentId());
            }
            row.put("gender", genderText(isBlank(record.getGender())
                    ? (student == null ? null : student.getGender()) : record.getGender()));

            GkzhSchool school = record.getSchoolId() == null ? null
                    : schoolMapper.selectGkzhSchoolBySchoolId(record.getSchoolId());
            row.put("schoolName", school == null ? null : school.getTitle());

            Long effectiveDepartmentId = (Long) row.get("departmentId");
            GkzhSchoolDepartment department = effectiveDepartmentId == null ? null
                    : departmentMapper.selectDepartmentById(effectiveDepartmentId);
            if (department != null && record.getSchoolId() != null
                    && record.getSchoolId().equals(department.getSchoolId())) {
                // 学生 department_id 指向专业；页面“院系”展示其父部门。
                GkzhSchoolDepartment college = department.getParentId() == null || department.getParentId() <= 0
                        ? department : departmentMapper.selectDepartmentById(department.getParentId());
                if (college != null && record.getSchoolId().equals(college.getSchoolId())) {
                    row.put("departmentName", college.getTitle());
                }
                row.put("major", isBlank(record.getMajor()) ? department.getTitle() : record.getMajor());
            }
            rows.add(row);
        }
        return rows;
    }

    /** 补全单条记录，供后台详情弹层使用。 */
    public Map<String, Object> enrichOne(ZyfxqRecord record) {
        List<Map<String, Object>> rows = enrich(record == null ? null : java.util.Collections.singletonList(record));
        return rows.isEmpty() ? null : rows.get(0);
    }

    /** 优先按记录中的学生编号取资料，旧记录再按同校用户回退。 */
    private GkzhStudent resolveStudent(ZyfxqRecord record) {
        if (record.getStudentId() != null) {
            GkzhStudent student = studentMapper.selectGkzhStudentByStudentId(record.getStudentId());
            if (student != null && record.getSchoolId() != null
                    && record.getSchoolId().equals(student.getSchoolId())
                    && record.getUserId() != null && record.getUserId().equals(student.getUserId())) {
                return student;
            }
        }
        if (record.getUserId() == null) {
            return null;
        }
        GkzhStudent fallback = studentMapper.selectOne(new QueryWrapper<GkzhStudent>()
                .eq("user_id", record.getUserId()).eq("school_id", record.getSchoolId())
                .eq("del_flag", "0").orderByAsc("student_id").last("LIMIT 1"));
        return fallback == null ? null : studentMapper.selectGkzhStudentByStudentId(fallback.getStudentId());
    }

    /** 统一把 0/1 或中文性别转为页面展示值。 */
    public static String genderText(String gender) {
        String value = gender == null ? "" : gender.trim();
        if ("0".equals(value) || "男".equals(value)) {
            return "男";
        }
        if ("1".equals(value) || "女".equals(value)) {
            return "女";
        }
        return "其他";
    }

    /** 对局结果中文名称：通关、未通关、对局中。 */
    public static String resultText(String result) {
        if ("win".equals(result)) {
            return "通关";
        }
        if ("lose".equals(result)) {
            return "未通关";
        }
        return "对局中";
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
