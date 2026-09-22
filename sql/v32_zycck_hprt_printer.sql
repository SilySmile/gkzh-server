-- zycck 汉印云打印机及打印任务表（MySQL 5.7+，可重复执行）

CREATE TABLE IF NOT EXISTS gkzh_zycck_printer (
    printer_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '本地打印机编号',
    printer_name VARCHAR(100) NOT NULL COMMENT '本地打印机名称',
    cloud_name VARCHAR(100) DEFAULT NULL COMMENT '汉印云端设备名称',
    equipment_sn VARCHAR(100) NOT NULL COMMENT '汉印设备序列号',
    equipment_secret VARCHAR(255) DEFAULT NULL COMMENT '汉印设备密钥，仅服务端保存',
    model_name VARCHAR(100) DEFAULT NULL COMMENT '打印机型号',
    status INT DEFAULT NULL COMMENT '汉印设备状态，1 在线，0 离线',
    enabled CHAR(1) NOT NULL DEFAULT '0' COMMENT '是否禁用，0 启用，1 禁用',
    bound_status CHAR(1) NOT NULL DEFAULT '0' COMMENT '绑定状态，0 已绑定，1 已解绑',
    last_status_sync_time DATETIME DEFAULT NULL COMMENT '最近状态同步时间',
    last_sync_time DATETIME DEFAULT NULL COMMENT '最近云端同步时间',
    create_time DATETIME DEFAULT NULL COMMENT '创建时间',
    update_time DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (printer_id),
    UNIQUE KEY uk_zycck_printer_equipment_sn (equipment_sn),
    KEY idx_zycck_printer_available (enabled, bound_status),
    KEY idx_zycck_printer_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='未来职业猜猜看汉印打印机';

CREATE TABLE IF NOT EXISTS gkzh_zycck_print_task (
    task_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '打印任务编号',
    record_id BIGINT DEFAULT NULL COMMENT '职业探索报告记录编号',
    printer_id BIGINT DEFAULT NULL COMMENT '本地打印机编号',
    equipment_sn VARCHAR(100) NOT NULL COMMENT '汉印设备序列号',
    print_id VARCHAR(100) DEFAULT NULL COMMENT '汉印云端打印任务编号',
    order_no VARCHAR(100) DEFAULT NULL COMMENT '业务订单号',
    print_type VARCHAR(20) NOT NULL DEFAULT 'TSPL' COMMENT '打印指令类型',
    source_pdf_url VARCHAR(500) DEFAULT NULL COMMENT '原始报告 PDF 地址',
    source_image_url VARCHAR(500) DEFAULT NULL COMMENT '打印图片地址',
    status VARCHAR(30) NOT NULL DEFAULT 'pending' COMMENT '任务状态',
    error_message VARCHAR(1000) DEFAULT NULL COMMENT '失败原因',
    retry_count INT NOT NULL DEFAULT 0 COMMENT '重试次数',
    create_time DATETIME DEFAULT NULL COMMENT '创建时间',
    print_time DATETIME DEFAULT NULL COMMENT '打印完成时间',
    update_time DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (task_id),
    KEY idx_zycck_print_task_record (record_id),
    KEY idx_zycck_print_task_printer (printer_id),
    KEY idx_zycck_print_task_print_id (print_id),
    KEY idx_zycck_print_task_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='未来职业猜猜看打印任务';
