-- 职业猜猜看参与记录补齐专业资料；无新表、无新字段。
-- gkzh_student.department_id 是专业（二级部门）编号，专业的 parent_id 才是院系编号。
-- 旧记录只能按当前同校学生档案补齐，无法还原学生参与当时已发生变更的专业。
START TRANSACTION;

UPDATE gkzh_zycck_record r
LEFT JOIN gkzh_student student ON student.student_id = COALESCE(
    (SELECT s.student_id FROM gkzh_student s
     WHERE s.student_id = r.student_id AND s.user_id = r.user_id
       AND s.school_id = r.school_id AND s.del_flag = '0'),
    (SELECT s.student_id FROM gkzh_student s
     WHERE s.user_id = r.user_id AND s.school_id = r.school_id AND s.del_flag = '0'
     ORDER BY s.student_id LIMIT 1))
LEFT JOIN gkzh_school_department major_dept
    ON major_dept.department_id = COALESCE(r.department_id, student.department_id)
   AND major_dept.school_id = r.school_id
SET r.department_id = COALESCE(r.department_id, student.department_id),
    r.major = CASE WHEN r.major IS NULL OR TRIM(r.major) = ''
                   THEN CASE WHEN major_dept.parent_id > 0 THEN major_dept.title ELSE NULL END
                   ELSE r.major END
WHERE r.game_type = 'zycck' AND (r.department_id IS NULL OR r.major IS NULL OR TRIM(r.major) = '');

COMMIT;
