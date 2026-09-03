-- ============================================================================
-- 谁是职场TOP（sszctop）数据库中文注释补充迁移
-- 适用于已执行 v14 的数据库；仅补充表和字段 COMMENT，不修改任何业务数据。
-- 所有时间字段统一保存为 datetime，接口对外格式为 yyyy-MM-dd HH:mm:ss。
-- ============================================================================

ALTER TABLE gkzh_sszctop_dimension
  MODIFY dimension_id bigint NOT NULL AUTO_INCREMENT COMMENT '维度主键ID',
  MODIFY code varchar(32) NOT NULL COMMENT '维度编码（系统内部唯一标识）',
  MODIFY name varchar(64) NOT NULL COMMENT '维度中文名称',
  MODIFY description varchar(1000) DEFAULT NULL COMMENT '维度说明文案',
  MODIFY sort_order int NOT NULL DEFAULT 0 COMMENT '后台展示排序值（升序）',
  MODIFY status char(1) NOT NULL DEFAULT '0' COMMENT '状态：0正常，1停用',
  MODIFY create_time datetime DEFAULT NULL COMMENT '创建时间',
  MODIFY update_time datetime DEFAULT NULL COMMENT '更新时间',
  COMMENT='谁是职场TOP游戏维度配置表';

ALTER TABLE gkzh_sszctop_career
  MODIFY career_id bigint NOT NULL AUTO_INCREMENT COMMENT '职业主键ID',
  MODIFY name varchar(100) NOT NULL COMMENT '职业中文名称',
  MODIFY major varchar(100) DEFAULT NULL COMMENT '相关专业名称',
  MODIFY description varchar(1000) DEFAULT NULL COMMENT '职业简介',
  MODIFY sort_order int NOT NULL DEFAULT 0 COMMENT '后台展示排序值（升序）',
  MODIFY status char(1) NOT NULL DEFAULT '0' COMMENT '状态：0正常，1停用',
  MODIFY create_time datetime DEFAULT NULL COMMENT '创建时间',
  MODIFY update_time datetime DEFAULT NULL COMMENT '更新时间',
  COMMENT='谁是职场TOP职业配置表';

ALTER TABLE gkzh_sszctop_dimension_rank
  MODIFY rank_id bigint NOT NULL AUTO_INCREMENT COMMENT '维度职业排序规则主键ID',
  MODIFY dimension_id bigint NOT NULL COMMENT '维度ID，关联gkzh_sszctop_dimension',
  MODIFY career_id bigint NOT NULL COMMENT '职业ID，关联gkzh_sszctop_career',
  MODIFY rank_order int NOT NULL COMMENT '该职业在此维度下的标准名次（1为最高）',
  MODIFY description varchar(2000) DEFAULT NULL COMMENT '排序依据和职业认知说明',
  MODIFY status char(1) NOT NULL DEFAULT '0' COMMENT '状态：0正常，1停用',
  MODIFY create_time datetime DEFAULT NULL COMMENT '创建时间',
  MODIFY update_time datetime DEFAULT NULL COMMENT '更新时间',
  COMMENT='谁是职场TOP维度职业标准排序及说明表';

ALTER TABLE gkzh_sszctop_room
  MODIFY room_id bigint NOT NULL AUTO_INCREMENT COMMENT '房间主键ID',
  MODIFY room_code varchar(12) NOT NULL COMMENT '分享和加入使用的房间号',
  MODIFY instance_id bigint NOT NULL COMMENT '活动周实例ID',
  MODIFY game_id bigint NOT NULL COMMENT '活动周游戏ID',
  MODIFY owner_user_id bigint NOT NULL COMMENT '当前房主用户ID',
  MODIFY mode varchar(10) NOT NULL COMMENT '游玩模式：solo单人，team组队',
  MODIFY dimension_id bigint DEFAULT NULL COMMENT '本局选定的维度ID',
  MODIFY career_ids varchar(100) DEFAULT NULL COMMENT '本局随机展示的职业ID列表，逗号分隔',
  MODIFY shared_order_ids varchar(100) DEFAULT NULL COMMENT '用户选入排序框的职业ID顺序，逗号分隔',
  MODIFY order_version int NOT NULL DEFAULT 0 COMMENT '共享排序乐观锁版本号',
  MODIFY status varchar(16) NOT NULL DEFAULT 'waiting' COMMENT '房间状态：waiting、playing、passed、failed、abandoned',
  MODIFY create_time datetime DEFAULT NULL COMMENT '创建时间',
  MODIFY update_time datetime DEFAULT NULL COMMENT '最后更新时间',
  MODIFY finish_time datetime DEFAULT NULL COMMENT '结束或销毁时间',
  COMMENT='谁是职场TOP游戏房间表';

ALTER TABLE gkzh_sszctop_room_member
  MODIFY member_id bigint NOT NULL AUTO_INCREMENT COMMENT '房间成员主键ID',
  MODIFY room_id bigint NOT NULL COMMENT '所属房间ID',
  MODIFY user_id bigint NOT NULL COMMENT '系统用户ID',
  MODIFY student_id bigint DEFAULT NULL COMMENT '学生档案ID',
  MODIFY student_name varchar(100) DEFAULT NULL COMMENT '学生姓名快照',
  MODIFY student_no varchar(50) DEFAULT NULL COMMENT '学生学号快照',
  MODIFY confirm_status varchar(16) NOT NULL DEFAULT 'waiting' COMMENT '状态：waiting未准备/未确认，ready已准备，confirmed已确认',
  MODIFY confirm_version int DEFAULT NULL COMMENT '确认时对应的排序版本号',
  MODIFY confirmed_at datetime DEFAULT NULL COMMENT '确认排序时间',
  MODIFY removed_reason varchar(100) DEFAULT NULL COMMENT '移出原因：left主动退出，disconnected断链等',
  MODIFY removed_time datetime DEFAULT NULL COMMENT '移出房间时间',
  MODIFY create_time datetime DEFAULT NULL COMMENT '加入房间时间',
  COMMENT='谁是职场TOP房间成员及准备确认状态表';

ALTER TABLE gkzh_sszctop_student_report
  MODIFY report_id bigint NOT NULL AUTO_INCREMENT COMMENT '个人报告主键ID',
  MODIFY room_id bigint NOT NULL COMMENT '来源房间ID',
  MODIFY instance_id bigint NOT NULL COMMENT '活动周实例ID',
  MODIFY game_id bigint NOT NULL COMMENT '活动周游戏ID',
  MODIFY user_id bigint NOT NULL COMMENT '系统用户ID',
  MODIFY student_id bigint DEFAULT NULL COMMENT '学生档案ID',
  MODIFY dimension_snapshot text NOT NULL COMMENT '结算时的维度快照JSON',
  MODIFY careers_snapshot text NOT NULL COMMENT '结算时的职业快照JSON',
  MODIFY shared_order_snapshot varchar(100) NOT NULL COMMENT '用户最终排序职业ID顺序',
  MODIFY standard_order_snapshot varchar(100) NOT NULL COMMENT '标准正确排序职业ID顺序',
  MODIFY result varchar(16) NOT NULL COMMENT '结算结果：passed成功，failed失败',
  MODIFY report_json text DEFAULT NULL COMMENT '个人报告扩展JSON数据',
  MODIFY create_time datetime DEFAULT NULL COMMENT '报告生成时间',
  COMMENT='谁是职场TOP每位学生独立报告快照表';

ALTER TABLE gkzh_sszctop_room_log
  MODIFY log_id bigint NOT NULL AUTO_INCREMENT COMMENT '房间日志主键ID',
  MODIFY instance_id bigint NOT NULL COMMENT '活动周实例ID',
  MODIFY game_id bigint NOT NULL COMMENT '活动周游戏ID',
  MODIFY room_id bigint NOT NULL COMMENT '所属房间ID',
  MODIFY room_code varchar(12) NOT NULL COMMENT '房间号快照',
  MODIFY user_id bigint DEFAULT NULL COMMENT '操作用户ID；系统事件可为空',
  MODIFY student_id bigint DEFAULT NULL COMMENT '操作学生ID；系统事件可为空',
  MODIFY event_type varchar(50) NOT NULL COMMENT '内部事件编码，后台展示时转换为中文',
  MODIFY content varchar(1000) DEFAULT NULL COMMENT '事件详细内容',
  MODIFY create_time datetime DEFAULT NULL COMMENT '日志发生时间',
  COMMENT='谁是职场TOP房间游玩操作日志表';
