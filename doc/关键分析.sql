-- 20251207
-- 吕梁学院
-- 就业意向与规划情况交叉分析
SELECT
    i.option_text as 就业方向,
    p.option_text as 规划情况,
    cd.count as 人数,
    it.total_count as 该方向总人数,
    ROUND(cd.count * 100.0 / it.total_count, 2) as 百分比
FROM (
         -- 交叉统计
         SELECT
             a82.answer_text as intent_answer_id,
             a84.answer_text as planning_answer_id,
             COUNT(*) as count
         FROM wjdc_survey_answer a82
             INNER JOIN wjdc_survey_answer a84 ON a82.response_id = a84.response_id
         WHERE a82.question_id = 82
           AND a84.question_id = 84
           AND a82.answer_text IS NOT NULL AND a82.answer_text != ''
           AND a84.answer_text IS NOT NULL AND a84.answer_text != ''
         GROUP BY a82.answer_text, a84.answer_text
     ) cd
         LEFT JOIN wjdc_survey_option i ON cd.intent_answer_id = CAST(i.id AS CHAR) AND i.question_id = 82
         LEFT JOIN wjdc_survey_option p ON cd.planning_answer_id = CAST(p.id AS CHAR) AND p.question_id = 84
         LEFT JOIN (
    -- 计算每个就业方向的总人数
    SELECT
        intent_answer_id,
        SUM(count) as total_count
    FROM (
             SELECT
                 a82.answer_text as intent_answer_id,
                 a84.answer_text as planning_answer_id,
                 COUNT(*) as count
             FROM wjdc_survey_answer a82
                 INNER JOIN wjdc_survey_answer a84 ON a82.response_id = a84.response_id
             WHERE a82.question_id = 82
               AND a84.question_id = 84
               AND a82.answer_text IS NOT NULL AND a82.answer_text != ''
               AND a84.answer_text IS NOT NULL AND a84.answer_text != ''
             GROUP BY a82.answer_text, a84.answer_text
         ) t
    GROUP BY intent_answer_id
) it ON cd.intent_answer_id = it.intent_answer_id
WHERE i.option_text IS NOT NULL AND p.option_text IS NOT NULL
ORDER BY
    CASE i.option_text
        WHEN '国内升学' THEN 1
        WHEN '出国留学' THEN 2
        WHEN '就业' THEN 3
        WHEN '创业' THEN 4
        ELSE 5
        END,
    CASE p.option_text
        WHEN '做了规划，并在稳步执行中' THEN 1
        WHEN '做了规划，不能坚持执行' THEN 2
        WHEN '只有大概的计划' THEN 3
        WHEN '从未做过' THEN 4
        ELSE 5
        END;