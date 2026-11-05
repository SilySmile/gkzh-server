-- ============================================================================
-- 谁是职场TOP（sszctop）：维度、职业图片素材字段
-- 说明：只给 sszctop 专属表新增可空的图片地址字段并回填当前素材，不改动其他游戏表。
-- 脚本可重复执行；回填只处理空值，后台已替换的图片不会被覆盖。
-- 素材目录按内容哈希发布：https://zhiye.sxgkzh.cn/imgs/sszctop/4e6204c2c4d2/
-- ============================================================================
DELIMITER $$
-- 先清理同名过程，避免上一次执行中断后残留导致脚本无法重跑。
DROP PROCEDURE IF EXISTS gkzh_migrate_sszctop_image_url$$
CREATE PROCEDURE gkzh_migrate_sszctop_image_url()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'gkzh_sszctop_dimension' AND column_name = 'image_url') THEN
        ALTER TABLE gkzh_sszctop_dimension
            ADD COLUMN image_url varchar(500) DEFAULT NULL COMMENT '维度卡片图片地址，后台可替换；为空时小程序使用内置兜底图' AFTER description;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'gkzh_sszctop_career' AND column_name = 'image_url') THEN
        ALTER TABLE gkzh_sszctop_career
            ADD COLUMN image_url varchar(500) DEFAULT NULL COMMENT '职业卡片图片地址，后台可替换；为空时小程序使用内置兜底图' AFTER description;
    END IF;
END$$
DELIMITER ;
CALL gkzh_migrate_sszctop_image_url();
DROP PROCEDURE gkzh_migrate_sszctop_image_url;

-- 七个维度：按 code 回填，图片与后台名称可独立维护。
UPDATE gkzh_sszctop_dimension SET image_url = CASE code
    WHEN 'steps' THEN 'https://zhiye.sxgkzh.cn/imgs/sszctop/4e6204c2c4d2/dimension-steps.png'
    WHEN 'hours' THEN 'https://zhiye.sxgkzh.cn/imgs/sszctop/4e6204c2c4d2/dimension-hours.png'
    WHEN 'pressure' THEN 'https://zhiye.sxgkzh.cn/imgs/sszctop/4e6204c2c4d2/dimension-pressure.png'
    WHEN 'skill' THEN 'https://zhiye.sxgkzh.cn/imgs/sszctop/4e6204c2c4d2/dimension-skill.png'
    WHEN 'prospect' THEN 'https://zhiye.sxgkzh.cn/imgs/sszctop/4e6204c2c4d2/dimension-prospect.png'
    WHEN 'social' THEN 'https://zhiye.sxgkzh.cn/imgs/sszctop/4e6204c2c4d2/dimension-social.png'
    WHEN 'travel' THEN 'https://zhiye.sxgkzh.cn/imgs/sszctop/4e6204c2c4d2/dimension-travel.png'
END
WHERE (image_url IS NULL OR image_url = '')
  AND code IN ('steps','hours','pressure','skill','prospect','social','travel');

-- 十二个职业：按职业名称回填，与 gkzh_sszctop_career.name 唯一键一致。
UPDATE gkzh_sszctop_career SET image_url = CASE name
    WHEN '人工智能工程师' THEN 'https://zhiye.sxgkzh.cn/imgs/sszctop/4e6204c2c4d2/career-ai-engineer.png'
    WHEN '数据分析师' THEN 'https://zhiye.sxgkzh.cn/imgs/sszctop/4e6204c2c4d2/career-data-analyst.png'
    WHEN '临床医生' THEN 'https://zhiye.sxgkzh.cn/imgs/sszctop/4e6204c2c4d2/career-clinician.png'
    WHEN '企业经理' THEN 'https://zhiye.sxgkzh.cn/imgs/sszctop/4e6204c2c4d2/career-manager.png'
    WHEN '新闻记者' THEN 'https://zhiye.sxgkzh.cn/imgs/sszctop/4e6204c2c4d2/career-journalist.png'
    WHEN '律师' THEN 'https://zhiye.sxgkzh.cn/imgs/sszctop/4e6204c2c4d2/career-lawyer.png'
    WHEN '金融分析师' THEN 'https://zhiye.sxgkzh.cn/imgs/sszctop/4e6204c2c4d2/career-financial-analyst.png'
    WHEN '中小学教师' THEN 'https://zhiye.sxgkzh.cn/imgs/sszctop/4e6204c2c4d2/career-teacher.png'
    WHEN '农业技术推广员' THEN 'https://zhiye.sxgkzh.cn/imgs/sszctop/4e6204c2c4d2/career-agri-extension.png'
    WHEN '视觉设计师' THEN 'https://zhiye.sxgkzh.cn/imgs/sszctop/4e6204c2c4d2/career-designer.png'
    WHEN '博物馆讲解员' THEN 'https://zhiye.sxgkzh.cn/imgs/sszctop/4e6204c2c4d2/career-museum-guide.png'
    WHEN '综合管理类公务员' THEN 'https://zhiye.sxgkzh.cn/imgs/sszctop/4e6204c2c4d2/career-civil-servant.png'
END
WHERE (image_url IS NULL OR image_url = '')
  AND name IN ('人工智能工程师','数据分析师','临床医生','企业经理','新闻记者','律师','金融分析师',
               '中小学教师','农业技术推广员','视觉设计师','博物馆讲解员','综合管理类公务员');
