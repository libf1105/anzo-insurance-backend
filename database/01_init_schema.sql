-- 货物运输险在线投保系统数据库初始化脚本
-- 版本: 1.0.0
-- 创建时间: 2024-01-15

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `anzo_insurance_dev` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `anzo_insurance_dev`;

-- 企业表
CREATE TABLE IF NOT EXISTS `enterprise` (
    `id` VARCHAR(36) PRIMARY KEY COMMENT '企业ID，UUID',
    `name` VARCHAR(100) NOT NULL COMMENT '企业名称',
    `credit_code` VARCHAR(18) UNIQUE NOT NULL COMMENT '统一社会信用代码',
    `contact_name` VARCHAR(50) NOT NULL COMMENT '联系人姓名',
    `contact_phone` VARCHAR(20) NOT NULL COMMENT '联系人手机',
    `contact_email` VARCHAR(100) COMMENT '联系人邮箱',
    `license_url` VARCHAR(500) COMMENT '营业执照URL',
    `status` ENUM('PENDING_REVIEW', 'ACTIVE', 'REJECTED', 'DISABLED', 'EXPIRED') DEFAULT 'PENDING_REVIEW',
    `balance` DECIMAL(15,2) DEFAULT 0 COMMENT '钱包余额(元)',
    `frozen_balance` DECIMAL(15,2) DEFAULT 0 COMMENT '冻结金额(元)',
    `total_recharged` DECIMAL(15,2) DEFAULT 0 COMMENT '累计充值',
    `total_consumed` DECIMAL(15,2) DEFAULT 0 COMMENT '累计消费',
    `review_at` DATETIME COMMENT '审核时间',
    `review_by` VARCHAR(36) COMMENT '审核人',
    `review_remark` TEXT COMMENT '审核备注',
    `created_by` VARCHAR(36) COMMENT '创建人',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_by` VARCHAR(36) COMMENT '更新人',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `version` INT DEFAULT 0 COMMENT '版本号',
    `deleted` BOOLEAN DEFAULT FALSE COMMENT '逻辑删除',
    INDEX `idx_status` (`status`),
    INDEX `idx_credit_code` (`credit_code`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='企业表';

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` VARCHAR(36) PRIMARY KEY COMMENT '用户ID，UUID',
    `enterprise_id` VARCHAR(36) NOT NULL COMMENT '企业ID',
    `username` VARCHAR(100) UNIQUE NOT NULL COMMENT '用户名(手机/邮箱)',
    `password_hash` VARCHAR(255) NOT NULL COMMENT '密码哈希',
    `real_name` VARCHAR(50) NOT NULL COMMENT '真实姓名',
    `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
    `email` VARCHAR(100) COMMENT '邮箱',
    `role` ENUM('SUPER_ADMIN', 'ADMIN', 'OPERATOR', 'FINANCE') DEFAULT 'OPERATOR',
    `status` ENUM('ACTIVE', 'DISABLED') DEFAULT 'ACTIVE',
    `last_login_at` DATETIME COMMENT '最后登录时间',
    `created_by` VARCHAR(36) COMMENT '创建人',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_by` VARCHAR(36) COMMENT '更新人',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `version` INT DEFAULT 0 COMMENT '版本号',
    `deleted` BOOLEAN DEFAULT FALSE COMMENT '逻辑删除',
    FOREIGN KEY (`enterprise_id`) REFERENCES `enterprise`(`id`) ON DELETE CASCADE,
    INDEX `idx_enterprise_id` (`enterprise_id`),
    INDEX `idx_username` (`username`),
    INDEX `idx_role` (`role`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 客户表
CREATE TABLE IF NOT EXISTS `customer` (
    `id` VARCHAR(36) PRIMARY KEY,
    `enterprise_id` VARCHAR(36) NOT NULL,
    `name` VARCHAR(100) NOT NULL COMMENT '客户企业名称',
    `credit_code` VARCHAR(18) COMMENT '统一社会信用代码',
    `contact_name` VARCHAR(50) NOT NULL,
    `contact_phone` VARCHAR(20) NOT NULL,
    `contact_email` VARCHAR(100),
    `address` VARCHAR(500),
    `country` VARCHAR(50),
    `city` VARCHAR(50),
    `status` ENUM('ACTIVE', 'DISABLED') DEFAULT 'ACTIVE',
    `created_by` VARCHAR(36) COMMENT '创建人',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_by` VARCHAR(36) COMMENT '更新人',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `version` INT DEFAULT 0 COMMENT '版本号',
    `deleted` BOOLEAN DEFAULT FALSE COMMENT '逻辑删除',
    FOREIGN KEY (`enterprise_id`) REFERENCES `enterprise`(`id`) ON DELETE CASCADE,
    INDEX `idx_enterprise_id` (`enterprise_id`),
    INDEX `idx_name` (`name`),
    INDEX `idx_credit_code` (`credit_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户表';

-- 投保申请表
CREATE TABLE IF NOT EXISTS `insurance_application` (
    `id` VARCHAR(36) PRIMARY KEY,
    `enterprise_id` VARCHAR(36) NOT NULL,
    `application_no` VARCHAR(50) UNIQUE NOT NULL COMMENT '投保单号: APP{yyyyMMdd}{6位}',
    
    -- Step 1: 基础信息
    `trade_direction` ENUM('IMPORT', 'EXPORT', 'DOMESTIC') NOT NULL,
    `transport_type` ENUM('SEA', 'AIR', 'RAIL', 'ROAD', 'MULTIMODAL') NOT NULL,
    `insurance_product` ENUM('CARGO', 'LIABILITY') NOT NULL,
    `insurer_id` VARCHAR(36) COMMENT '保司ID',
    `insurer_name` VARCHAR(100),
    `applicant_id` VARCHAR(36) NOT NULL COMMENT '投保人ID',
    `insured_id` VARCHAR(36) NOT NULL COMMENT '被保险人ID',
    
    -- Step 2: 运输信息
    `departure_country` VARCHAR(50) NOT NULL,
    `departure_city` VARCHAR(50) NOT NULL,
    `arrival_country` VARCHAR(50) NOT NULL,
    `arrival_city` VARCHAR(50) NOT NULL,
    `departure_date` DATE NOT NULL,
    `arrival_date` DATE,
    `transport_details` JSON COMMENT '运输详情(JSON结构)',
    
    -- Step 3: 货物信息
    `cargo_name` VARCHAR(200) NOT NULL,
    `cargo_category` VARCHAR(50) NOT NULL,
    `packing_type` VARCHAR(50) NOT NULL,
    `packing_quantity` INT NOT NULL,
    `shipping_mark` VARCHAR(500),
    `currency` VARCHAR(10) DEFAULT 'CNY',
    `insurance_amount` DECIMAL(15,2) NOT NULL,
    `invoice_amount` DECIMAL(15,2),
    `addition_ratio` DECIMAL(5,2) DEFAULT 1.10 COMMENT '加成比例',
    `deductible` DECIMAL(15,2) COMMENT '免赔额',
    `special_terms` TEXT,
    
    -- 费用信息
    `premium` DECIMAL(15,2) COMMENT '保费',
    `premium_currency` VARCHAR(10) DEFAULT 'CNY',
    `rate` DECIMAL(10,6) COMMENT '费率',
    
    -- 状态信息
    `status` ENUM('DRAFT', 'SUBMITTED', 'UNDERWRITING', 'UNDERWRITTEN', 'ACTIVE', 'EXPIRED', 'CANCELLED', 'MODIFYING') DEFAULT 'DRAFT',
    `reject_reason` TEXT,
    
    -- 时间戳
    `created_by` VARCHAR(36) COMMENT '创建人',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_by` VARCHAR(36) COMMENT '更新人',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `submitted_at` DATETIME,
    `underwriting_at` DATETIME,
    `effective_at` DATETIME,
    `expired_at` DATETIME,
    `version` INT DEFAULT 0 COMMENT '版本号',
    `deleted` BOOLEAN DEFAULT FALSE COMMENT '逻辑删除',
    
    FOREIGN KEY (`enterprise_id`) REFERENCES `enterprise`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`applicant_id`) REFERENCES `customer`(`id`),
    FOREIGN KEY (`insured_id`) REFERENCES `customer`(`id`),
    INDEX `idx_enterprise_id` (`enterprise_id`),
    INDEX `idx_application_no` (`application_no`),
    INDEX `idx_status` (`status`),
    INDEX `idx_departure_date` (`departure_date`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='投保申请表';

-- 保单表
CREATE TABLE IF NOT EXISTS `policy` (
    `id` VARCHAR(36) PRIMARY KEY,
    `policy_no` VARCHAR(50) UNIQUE NOT NULL COMMENT '保单号: POL{yyyyMMdd}{6位}',
    `application_id` VARCHAR(36) NOT NULL,
    `enterprise_id` VARCHAR(36) NOT NULL,
    
    -- 基础信息
    `trade_direction` ENUM('IMPORT', 'EXPORT', 'DOMESTIC') NOT NULL,
    `transport_type` ENUM('SEA', 'AIR', 'RAIL', 'ROAD', 'MULTIMODAL') NOT NULL,
    `insurance_product` ENUM('CARGO', 'LIABILITY') NOT NULL,
    `insurer_id` VARCHAR(36) NOT NULL,
    `insurer_name` VARCHAR(100) NOT NULL,
    `applicant_name` VARCHAR(100) NOT NULL,
    `insured_name` VARCHAR(100) NOT NULL,
    
    -- 运输信息
    `departure_place` VARCHAR(100) NOT NULL,
    `arrival_place` VARCHAR(100) NOT NULL,
    `departure_date` DATE NOT NULL,
    `arrival_date` DATE,
    `transport_details` JSON,
    
    -- 货物信息
    `cargo_name` VARCHAR(200) NOT NULL,
    `cargo_category` VARCHAR(50) NOT NULL,
    `packing_type` VARCHAR(50) NOT NULL,
    `packing_quantity` INT NOT NULL,
    `insurance_amount` DECIMAL(15,2) NOT NULL,
    `insurance_currency` VARCHAR(10) DEFAULT 'CNY',
    
    -- 费用信息
    `premium` DECIMAL(15,2) NOT NULL,
    `premium_currency` VARCHAR(10) DEFAULT 'CNY',
    `rate` DECIMAL(10,6) NOT NULL,
    `deductible` DECIMAL(15,2),
    
    -- 状态信息
    `status` ENUM('DRAFT', 'SUBMITTED', 'UNDERWRITING', 'UNDERWRITTEN', 'ACTIVE', 'EXPIRED', 'CANCELLED', 'MODIFYING') NOT NULL,
    `effective_date` DATE NOT NULL,
    `expiry_date` DATE NOT NULL,
    `cancel_reason` TEXT,
    `cancel_date` DATETIME,
    
    -- PDF文件
    `pdf_url` VARCHAR(500),
    
    -- 时间戳
    `issued_at` DATETIME NOT NULL,
    `created_by` VARCHAR(36) COMMENT '创建人',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_by` VARCHAR(36) COMMENT '更新人',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `version` INT DEFAULT 0 COMMENT '版本号',
    `deleted` BOOLEAN DEFAULT FALSE COMMENT '逻辑删除',
    
    FOREIGN KEY (`application_id`) REFERENCES `insurance_application`(`id`),
    FOREIGN KEY (`enterprise_id`) REFERENCES `enterprise`(`id`) ON DELETE CASCADE,
    INDEX `idx_policy_no` (`policy_no`),
    INDEX `idx_enterprise_id` (`enterprise_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_effective_date` (`effective_date`),
    INDEX `idx_insurer_id` (`insurer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='保单表';

-- 数据字典表
CREATE TABLE IF NOT EXISTS `data_dict` (
    `id` VARCHAR(36) PRIMARY KEY,
    `dict_type` VARCHAR(50) NOT NULL COMMENT '字典类型',
    `dict_code` VARCHAR(50) NOT NULL COMMENT '字典代码',
    `dict_name` VARCHAR(100) NOT NULL COMMENT '字典名称',
    `parent_id` VARCHAR(36) COMMENT '父级ID',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `status` ENUM('ACTIVE', 'DISABLED') DEFAULT 'ACTIVE',
    `created_by` VARCHAR(36) COMMENT '创建人',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_by` VARCHAR(36) COMMENT '更新人',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `version` INT DEFAULT 0 COMMENT '版本号',
    `deleted` BOOLEAN DEFAULT FALSE COMMENT '逻辑删除',
    INDEX `idx_dict_type` (`dict_type`),
    INDEX `idx_dict_code` (`dict_code`),
    UNIQUE KEY `uk_type_code` (`dict_type`, `dict_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据字典表';

-- 插入基础数据字典
INSERT INTO `data_dict` (`id`, `dict_type`, `dict_code`, `dict_name`, `sort_order`, `status`) VALUES
-- 贸易方向
(UUID(), 'TRADE_DIRECTION', 'IMPORT', '进口', 1, 'ACTIVE'),
(UUID(), 'TRADE_DIRECTION', 'EXPORT', '出口', 2, 'ACTIVE'),
(UUID(), 'TRADE_DIRECTION', 'DOMESTIC', '国内', 3, 'ACTIVE'),

-- 运输方式
(UUID(), 'TRANSPORT_TYPE', 'SEA', '海运', 1, 'ACTIVE'),
(UUID(), 'TRANSPORT_TYPE', 'AIR', '空运', 2, 'ACTIVE'),
(UUID(), 'TRANSPORT_TYPE', 'RAIL', '铁路', 3, 'ACTIVE'),
(UUID(), 'TRANSPORT_TYPE', 'ROAD', '公路', 4, 'ACTIVE'),
(UUID(), 'TRANSPORT_TYPE', 'MULTIMODAL', '多式联运', 5, 'ACTIVE'),

-- 保险产品
(UUID(), 'INSURANCE_PRODUCT', 'CARGO', '货运险', 1, 'ACTIVE'),
(UUID(), 'INSURANCE_PRODUCT', 'LIABILITY', '责任险', 2, 'ACTIVE'),

-- 企业状态
(UUID(), 'ENTERPRISE_STATUS', 'PENDING_REVIEW', '待审核', 1, 'ACTIVE'),
(UUID(), 'ENTERPRISE_STATUS', 'ACTIVE', '已生效', 2, 'ACTIVE'),
(UUID(), 'ENTERPRISE_STATUS', 'REJECTED', '已拒绝', 3, 'ACTIVE'),
(UUID(), 'ENTERPRISE_STATUS', 'DISABLED', '已禁用', 4, 'ACTIVE'),
(UUID(), 'ENTERPRISE_STATUS', 'EXPIRED', '已过期', 5, 'ACTIVE'),

-- 用户状态
(UUID(), 'USER_STATUS', 'ACTIVE', '启用', 1, 'ACTIVE'),
(UUID(), 'USER_STATUS', 'DISABLED', '禁用', 2, 'ACTIVE'),

-- 用户角色
(UUID(), 'USER_ROLE', 'SUPER_ADMIN', '超级管理员', 1, 'ACTIVE'),
(UUID(), 'USER_ROLE', 'ADMIN', '管理员', 2, 'ACTIVE'),
(UUID(), 'USER_ROLE', 'OPERATOR', '操作员', 3, 'ACTIVE'),
(UUID(), 'USER_ROLE', 'FINANCE', '财务员', 4, 'ACTIVE'),

-- 投保状态
(UUID(), 'APPLICATION_STATUS', 'DRAFT', '草稿', 1, 'ACTIVE'),
(UUID(), 'APPLICATION_STATUS', 'SUBMITTED', '已提交', 2, 'ACTIVE'),
(UUID(), 'APPLICATION_STATUS', 'UNDERWRITING', '核保中', 3, 'ACTIVE'),
(UUID(), 'APPLICATION_STATUS', 'UNDERWRITTEN', '已承保', 4, 'ACTIVE'),
(UUID(), 'APPLICATION_STATUS', 'ACTIVE', '已生效', 5, 'ACTIVE'),
(UUID(), 'APPLICATION_STATUS', 'EXPIRED', '已过期', 6, 'ACTIVE'),
(UUID(), 'APPLICATION_STATUS', 'CANCELLED', '已取消', 7, 'ACTIVE'),
(UUID(), 'APPLICATION_STATUS', 'MODIFYING', '修改中', 8, 'ACTIVE');

-- 创建超级管理员账号和测试企业（仅用于开发环境）
INSERT INTO `enterprise` (`id`, `name`, `credit_code`, `contact_name`, `contact_phone`, `status`, `balance`) VALUES
('00000000-0000-0000-0000-000000000001', '安佐科技（开发）', '91310000100000000X', '管理员', '13800000001', 'ACTIVE', 1000000);

-- 密码: Admin123456 (BCrypt加密)
INSERT INTO `user` (`id`, `enterprise_id`, `username`, `password_hash`, `real_name`, `phone`, `email`, `role`, `status`) VALUES
('00000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000001', 'admin@anzo.com', 
 '$2a$10$g.s5lcV1YiST9lKGf2V8R.TLKNc84ETqDz/nBfS2fm0oJ09JrRf6O', 
 '系统管理员', '13800000001', 'admin@anzo.com', 'SUPER_ADMIN', 'ACTIVE');

-- 插入测试客户数据
INSERT INTO `customer` (`id`, `enterprise_id`, `name`, `credit_code`, `contact_name`, `contact_phone`, `status`) VALUES
(UUID(), '00000000-0000-0000-0000-000000000001', 'ABC进出口有限公司', '91310000100000001X', '张经理', '13800138001', 'ACTIVE'),
(UUID(), '00000000-0000-0000-0000-000000000001', 'XYZ科技有限公司', '91310000100000002X', '李总监', '13800138002', 'ACTIVE'),
(UUID(), '00000000-0000-0000-0000-000000000001', '北京物流有限公司', '91310000100000003X', '王主管', '13800138003', 'ACTIVE');

-- 投保草稿表
CREATE TABLE application_draft (
    id VARCHAR(36) PRIMARY KEY,
    enterprise_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    current_step TINYINT DEFAULT 1 COMMENT '当前步骤(1-3)',
    step1_data JSON COMMENT '步骤1数据',
    step2_data JSON COMMENT '步骤2数据',
    step3_data JSON COMMENT '步骤3数据',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    expired_at DATETIME COMMENT '过期时间(7天后)',
    FOREIGN KEY (enterprise_id) REFERENCES enterprise(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES user(id),
    INDEX idx_enterprise_id (enterprise_id),
    INDEX idx_expired_at (expired_at)
);

-- 投保模板表
CREATE TABLE application_template (
    id VARCHAR(36) PRIMARY KEY,
    enterprise_id VARCHAR(36) NOT NULL,
    name VARCHAR(100) NOT NULL COMMENT '模板名称',
    trade_direction ENUM('IMPORT', 'EXPORT', 'DOMESTIC') NOT NULL,
    transport_type ENUM('SEA', 'AIR', 'RAIL', 'ROAD', 'MULTIMODAL') NOT NULL,
    insurance_product ENUM('CARGO', 'LIABILITY') NOT NULL,
    insurer_id VARCHAR(36),
    applicant_id VARCHAR(36),
    insured_id VARCHAR(36),
    departure_country VARCHAR(50),
    departure_city VARCHAR(50),
    arrival_country VARCHAR(50),
    arrival_city VARCHAR(50),
    cargo_category VARCHAR(50),
    packing_type VARCHAR(50),
    addition_ratio DECIMAL(5,2) DEFAULT 1.10,
    special_terms TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (enterprise_id) REFERENCES enterprise(id) ON DELETE CASCADE,
    INDEX idx_enterprise_id (enterprise_id)
);
```

#### 3.2.3 保单管理表

```sql
-- 保单表
CREATE TABLE policy (
    id VARCHAR(36) PRIMARY KEY,
    policy_no VARCHAR(50) UNIQUE NOT NULL COMMENT '保单号: POL{yyyyMMdd}{6位}',
    application_id VARCHAR(36) NOT NULL,
    enterprise_id VARCHAR(36) NOT NULL,
    
    -- 基础信息
    trade_direction ENUM('IMPORT', 'EXPORT', 'DOMESTIC') NOT NULL,
    transport_type ENUM('SEA', 'AIR', 'RAIL', 'ROAD', 'MULTIMODAL') NOT NULL,
    insurance_product ENUM('CARGO', 'LIABILITY') NOT NULL,
    insurer_id VARCHAR(36) NOT NULL,
    insurer_name VARCHAR(100) NOT NULL,
    applicant_name VARCHAR(100) NOT NULL,
    insured_name VARCHAR(100) NOT NULL,
    
    -- 运输信息
    departure_place VARCHAR(100) NOT NULL,
    arrival_place VARCHAR(100) NOT NULL,
    departure_date DATE NOT NULL,
    arrival_date DATE,
    transport_details JSON,
    
    -- 货物信息
    cargo_name VARCHAR(200) NOT NULL,
    cargo_category VARCHAR(50) NOT NULL,
    packing_type VARCHAR(50) NOT NULL,
    packing_quantity INT NOT NULL,
    insurance_amount DECIMAL(15,2) NOT NULL,
    insurance_currency VARCHAR(10) DEFAULT 'CNY',
    
    -- 费用信息
    premium DECIMAL(15,2) NOT NULL,
    premium_currency VARCHAR(10) DEFAULT 'CNY',
    rate DECIMAL(10,6) NOT NULL,
    deductible DECIMAL(15,2),
    
    -- 状态信息
    status ENUM('DRAFT', 'SUBMITTED', 'UNDERWRITING', 'UNDERWRITTEN', 'ACTIVE', 'EXPIRED', 'CANCELLED', 'MODIFYING') NOT NULL,
    effective_date DATE NOT NULL,
    expiry_date DATE NOT NULL,
    cancel_reason TEXT,
    cancel_date DATETIME,
    
    -- PDF文件
    pdf_url VARCHAR(500),
    
    -- 时间戳
    issued_at DATETIME NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (application_id) REFERENCES insurance_application(id),
    FOREIGN KEY (enterprise_id) REFERENCES enterprise(id) ON DELETE CASCADE,
    INDEX idx_policy_no (policy_no),
    INDEX idx_enterprise_id (enterprise_id),
    INDEX idx_status (status),
    INDEX idx_effective_date (effective_date),
    INDEX idx_insurer_id (insurer_id)
);
```

#### 3.2.4 财务管理表

```sql
-- 钱包交易流水表
CREATE TABLE wallet_transaction (
    id VARCHAR(36) PRIMARY KEY,
    enterprise_id VARCHAR(36) NOT NULL,
    type ENUM('RECHARGE', 'PREMIUM_PAID', 'PREMIUM_REFUND', 'CLAIM_PAID', 'BALANCE_ADJUST') NOT NULL,
    amount DECIMAL(15,2) NOT NULL COMMENT '正数收入，负数支出',
    balance_before DECIMAL(15,2) NOT NULL,
    balance_after DECIMAL(15,2) NOT NULL,
    reference_id VARCHAR(36) COMMENT '关联业务ID',
    reference_type ENUM('APPLICATION', 'POLICY', 'CLAIM', 'RECHARGE') COMMENT '业务类型',
    remark TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (enterprise_id) REFERENCES enterprise(id) ON DELETE CASCADE,
    INDEX idx_enterprise_id (enterprise_id),
    INDEX idx_type (type),
    INDEX idx_created_at (created_at),
    INDEX idx_reference (reference_type, reference_id)
);

-- 充值记录表
CREATE TABLE recharge_record (
    id VARCHAR(36) PRIMARY KEY,
    enterprise_id VARCHAR(36) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    currency VARCHAR(10) DEFAULT 'CNY',
    payment_method ENUM('BANK_TRANSFER', 'ONLINE_PAYMENT') DEFAULT 'BANK_TRANSFER',
    payment_proof_url VARCHAR(500) COMMENT '转账凭证URL',
    status ENUM('PENDING', 'SUCCESS', 'FAILED', 'CANCELLED') DEFAULT 'PENDING',
    transaction_no VARCHAR(100) COMMENT '交易流水号',
    remark TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    completed_at DATETIME,
    FOREIGN KEY (enterprise_id) REFERENCES enterprise(id) ON DELETE CASCADE,
    INDEX idx_enterprise_id (enterprise_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
);

-- 账单表
CREATE TABLE bill (
    id VARCHAR(36) PRIMARY KEY,
    enterprise_id VARCHAR(36) NOT NULL,
    period VARCHAR(7) NOT NULL COMMENT '账单周期: 2026-05',
    total_premium DECIMAL(15,2) DEFAULT 0 COMMENT '保费总额',
    total_refund DECIMAL(15,2) DEFAULT 0 COMMENT '退费总额',
    total_adjust DECIMAL(15,2) DEFAULT 0 COMMENT '调整总额',
    net_amount DECIMAL(15,2) NOT NULL COMMENT '应付净额',
    invoice_status ENUM('NOT_APPLIED', 'APPLIED', 'INVOICED', 'CANCELLED') DEFAULT 'NOT_APPLIED',
    invoice_amount DECIMAL(15,2) DEFAULT 0 COMMENT '已开票金额',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (enterprise_id) REFERENCES enterprise(id) ON DELETE CASCADE,
    UNIQUE KEY uk_enterprise_period (enterprise_id, period),
    INDEX idx_period (period)
);

-- 发票信息表
CREATE TABLE invoice_info (
    id VARCHAR(36) PRIMARY KEY,
    enterprise_id VARCHAR(36) NOT NULL UNIQUE,
    title VARCHAR(200) NOT NULL COMMENT '发票抬头',
    tax_id VARCHAR(50) NOT NULL COMMENT '纳税人识别号',
    address VARCHAR(500) COMMENT '地址',
    phone VARCHAR(20),
    bank_name VARCHAR(100) COMMENT '开户银行',
    bank_account VARCHAR(100) COMMENT '银行账号',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (enterprise_id) REFERENCES enterprise(id) ON DELETE CASCADE
);
```

#### 3.2.5 理赔管理表

```sql
-- 理赔案件表
CREATE TABLE claim (
    id VARCHAR(36) PRIMARY KEY,
    claim_no VARCHAR(50) UNIQUE NOT NULL COMMENT '理赔号: CLM{yyyyMMdd}{6位}',
    policy_id VARCHAR(36) NOT NULL,
    enterprise_id VARCHAR(36) NOT NULL,
    
    -- 出险信息
    accident_date DATE NOT NULL,
    accident_place VARCHAR(200) NOT NULL,
    accident_description TEXT NOT NULL,
    estimated_loss DECIMAL(15,2) COMMENT '预估损失',
    estimated_loss_currency VARCHAR(10) DEFAULT 'CNY',
    
    -- 状态信息
    status ENUM('REPORTED', 'MATERIAL_REVIEW', 'MATERIAL_PENDING', 'SURVEYING', 'NEGOTIATING', 'SETTLED', 'REJECTED', 'WITHDRAWN') DEFAULT 'REPORTED',
    reject_reason TEXT,
    settlement_amount DECIMAL(15,2) COMMENT '赔付金额',
    
    -- 时间戳
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    settled_at DATETIME,
    
    FOREIGN KEY (policy_id) REFERENCES policy(id),
    FOREIGN KEY (enterprise_id) REFERENCES enterprise(id) ON DELETE CASCADE,
    INDEX idx_claim_no (claim_no),
    INDEX idx_enterprise_id (enterprise_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
);

-- 理赔材料表
CREATE TABLE claim_material (
    id VARCHAR(36) PRIMARY KEY,
    claim_id VARCHAR(36) NOT NULL,
    type ENUM('LOSS_PROOF', 'TRANSPORT_DOC', 'LOSS_LIST', 'OTHER') NOT NULL,
    file_name VARCHAR(200) NOT NULL,
    file_url VARCHAR(500) NOT NULL,
    uploaded_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (claim_id) REFERENCES claim(id) ON DELETE CASCADE,
    INDEX idx_claim_id (claim_id)
);

-- 理赔事件表
CREATE TABLE claim_event (
    id VARCHAR(36) PRIMARY KEY,
    claim_id VARCHAR(36) NOT NULL,
    status VARCHAR(50) NOT NULL,
    operator VARCHAR(100) NOT NULL,
    remark TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (claim_id) REFERENCES claim(id) ON DELETE CASCADE,
    INDEX idx_claim_id (claim_id),
    INDEX idx_created_at (created_at)
);
```

#### 3.2.6 系统管理表

```sql
-- 保司表
CREATE TABLE insurer (
    id VARCHAR(36) PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL COMMENT '保司代码',
    name VARCHAR(100) NOT NULL COMMENT '保司名称',
    api_endpoint VARCHAR(500) COMMENT 'API地址',
    api_key VARCHAR(500) COMMENT 'API密钥',
    api_secret VARCHAR(500),
    status ENUM('ACTIVE', 'DISABLED') DEFAULT 'ACTIVE',
    config JSON COMMENT '配置信息(JSON)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_code (code),
    INDEX idx_status (status)
);

-- 风控规则表
CREATE TABLE risk_rule (
    id VARCHAR(36) PRIMARY KEY,
    rule_code VARCHAR(50) UNIQUE NOT NULL COMMENT '规则代码',
    rule_name VARCHAR(100) NOT NULL,
    rule_type ENUM('LIMIT', 'DETECTION', 'WARNING', 'BLOCK') NOT NULL,
    condition_expression TEXT NOT NULL COMMENT '条件表达式',
    action ENUM('REQUIRE_REVIEW', 'WARN', 'BLOCK', 'NOTIFY') NOT NULL,
    priority INT DEFAULT 100 COMMENT '优先级(越小越高)',
    status ENUM('ACTIVE', 'DISABLED') DEFAULT 'ACTIVE',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_rule_code (rule_code),
    INDEX idx_status (status)
);

-- 角色权限表
CREATE TABLE role (
    id VARCHAR(36) PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL COMMENT '角色代码',
    name VARCHAR(100) NOT NULL,
    description TEXT,
    permissions JSON COMMENT '权限配置(JSON)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 数据字典表
CREATE TABLE data_dict (
    id VARCHAR(36) PRIMARY KEY,
    dict_type VARCHAR(50) NOT NULL COMMENT '字典类型',
    dict_code VARCHAR(50) NOT NULL COMMENT '字典代码',
    dict_name VARCHAR(100) NOT NULL COMMENT '字典名称',
    parent_id VARCHAR(36) COMMENT '父级ID',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status ENUM('ACTIVE', 'DISABLED') DEFAULT 'ACTIVE',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_dict_type (dict_type),
    INDEX idx_dict_code (dict_code),
    UNIQUE KEY uk_type_code (dict_type, dict_code)
);
```

#### 3.2.7 消息中心表

```sql
-- 消息表
CREATE TABLE message (
    id VARCHAR(36) PRIMARY KEY,
    enterprise_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) COMMENT '接收用户ID',
    type ENUM('SYSTEM', 'BUSINESS', 'CLAIM', 'FINANCE') NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    reference_id VARCHAR(36) COMMENT '关联业务ID',
    reference_type VARCHAR(50) COMMENT '业务类型',
    is_read BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    read_at DATETIME,
    FOREIGN KEY (enterprise_id) REFERENCES enterprise(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES user(id),
    INDEX idx_enterprise_id (enterprise_id),
    INDEX idx_user_id (user_id),
    INDEX idx_is_read (is_read),
    INDEX idx_created_at (created_at)
);

-- 操作日志表
CREATE TABLE operation_log (
    id VARCHAR(36) PRIMARY KEY,
    enterprise_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    operation_type VARCHAR(50) NOT NULL COMMENT '操作类型',
    resource_type VARCHAR(50) NOT NULL COMMENT '资源类型',
    resource_id VARCHAR(36) COMMENT '资源ID',
    operation_detail TEXT COMMENT '操作详情',
    ip_address VARCHAR(45) COMMENT 'IP地址',
    user_agent TEXT COMMENT '用户代理',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (enterprise_id) REFERENCES enterprise(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES user(id),
    INDEX idx_enterprise_id (enterprise_id),
    INDEX idx_user_id (user_id),
    INDEX idx_operation_type (operation_type),
    INDEX idx_created_at (created_at)
);