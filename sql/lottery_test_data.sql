-- 抽奖模块测试数据

-- 插入抽奖活动测试数据
INSERT INTO `lottery_activity` (`activity_id`, `title`, `description`, `start_time`, `end_time`, `status`, `activity_type`, `location`, `organizer`, `contact_person`, `contact_phone`, `create_by`, `create_time`) VALUES
(1, '2024年校园文化节抽奖', '校园文化节抽奖活动，丰厚奖品等你来拿！', '2024-12-20 14:00:00', '2024-12-20 15:00:00', 1, 1, '学校大礼堂', '学生处', '张老师', '13800138000', 'admin', NOW()),
(2, '新年抽奖活动', '新年抽奖活动，祝大家新年快乐！', '2024-12-31 20:00:00', '2024-12-31 22:00:00', 1, 1, '线上活动', '团委', '李老师', '13900139000', 'admin', NOW());

-- 插入抽奖奖品测试数据
INSERT INTO `lottery_prize` (`prize_id`, `activity_id`, `title`, `image_url`, `stock`, `probability`, `sort_order`, `create_by`, `create_time`) VALUES
(1, 1, '一等奖', '/static/lottery/fangche.png', 1, 0.05, 1, 'admin', NOW()),
(2, 1, '二等奖', '/static/lottery/huwaishoubiao.png', 3, 0.10, 2, 'admin', NOW()),
(3, 1, '三等奖', '/static/lottery/jiguancejuyi.png', 5, 0.15, 3, 'admin', NOW()),
(4, 1, '四等奖', '/static/lottery/jiguancejuyi.png', 10, 0.20, 4, 'admin', NOW()),
(5, 1, '五等奖', '/static/lottery/huwaishoubiao.png', 20, 0.25, 5, 'admin', NOW()),
(6, 1, '六等奖', '/static/lottery/fangche.png', 50, 0.25, 6, 'admin', NOW()),
(7, 2, '特等奖', '/static/lottery/fangche.png', 1, 0.02, 1, 'admin', NOW()),
(8, 2, '一等奖', '/static/lottery/huwaishoubiao.png', 2, 0.08, 2, 'admin', NOW()),
(9, 2, '二等奖', '/static/lottery/jiguancejuyi.png', 5, 0.15, 3, 'admin', NOW()),
(10, 2, '三等奖', '/static/lottery/jiguancejuyi.png', 10, 0.25, 4, 'admin', NOW()),
(11, 2, '参与奖', '/static/lottery/huwaishoubiao.png', 100, 0.50, 5, 'admin', NOW());

-- 插入抽奖记录测试数据
INSERT INTO `lottery_record` (`record_id`, `user_id`, `user_name`, `nick_name`, `activity_id`, `activity_title`, `prize_id`, `prize_title`, `result_name`, `created_at`, `ip`, `create_by`, `create_time`) VALUES
(1, 1001, '张三', '张三', 1, '2024年校园文化节抽奖', 3, '三等奖', '三等奖', '2024-12-20 14:15:00', '192.168.1.100', 'system', NOW()),
(2, 1002, '李四', '李四', 1, '2024年校园文化节抽奖', 5, '五等奖', '五等奖', '2024-12-20 14:20:00', '192.168.1.101', 'system', NOW()),
(3, 1003, '王五', '王五', 1, '2024年校园文化节抽奖', 6, '六等奖', '六等奖', '2024-12-20 14:25:00', '192.168.1.102', 'system', NOW()),
(4, 1004, '赵六', '赵六', 2, '新年抽奖活动', 9, '二等奖', '二等奖', '2024-12-31 20:10:00', '192.168.1.103', 'system', NOW()),
(5, 1005, '钱七', '钱七', 2, '新年抽奖活动', 11, '参与奖', '参与奖', '2024-12-31 20:15:00', '192.168.1.104', 'system', NOW()); 