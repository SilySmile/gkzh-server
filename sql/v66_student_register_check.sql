-- 学生注册校验开关：控制小程序注册是否必须填写年级、学历、毕业时间。
-- 默认 false（不校验），避免尚未升级的小程序旧版本因缺少这三项而无法注册；
-- 管理员可在 Web「学生管理」页面切换，切换后服务端立即生效，无需重启。
-- 可重复执行：已存在同键参数时不重复插入，也不会覆盖管理员已设置的值。

INSERT INTO sys_config(config_name, config_key, config_value, config_type, remark, create_by, create_time, update_by, update_time)
SELECT '学生注册校验年级学历毕业时间', 'student.register.requireProfile', 'false', 'N',
       'true=小程序注册必须填写年级、学历、毕业时间；false=不校验（兼容未升级的小程序）',
       'admin', NOW(), 'admin', NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'student.register.requireProfile');
