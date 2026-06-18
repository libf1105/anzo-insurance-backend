# 财务表结构对齐 SQL

## 背景

当前后端财务模块实体实际使用以下表：

- `t_wallet`
- `t_transaction_record`
- `t_bill`
- `t_invoice`

而现有 `database/01_init_schema.sql` 中仍保留的是早期设计：

- `wallet_transaction`
- `recharge_record`
- `bill`
- `invoice_info`

两套模型的表名、字段语义和结构都不一致。为了让当前 `anzo-insurance-backend` 可以按现有实体正常运行，建议先补齐下面这组运行时表。

## 建议执行顺序

1. 在测试库或开发库先备份原财务相关表。
2. 执行以下 SQL，补齐当前后端运行所需的 `t_*` 财务表。
3. 若后续决定完全回归 `spec/database.md` 的 `finance_*` 命名体系，再单独做统一重构迁移。

## SQL

```sql
CREATE TABLE IF NOT EXISTS `t_wallet` (
  `id` VARCHAR(36) NOT NULL,
  `enterprise_id` VARCHAR(36) NOT NULL,
  `available_balance` DECIMAL(15,2) NOT NULL DEFAULT 0.00,
  `frozen_balance` DECIMAL(15,2) NOT NULL DEFAULT 0.00,
  `total_recharge_amount` DECIMAL(15,2) NOT NULL DEFAULT 0.00,
  `total_consumption_amount` DECIMAL(15,2) NOT NULL DEFAULT 0.00,
  `total_refund_amount` DECIMAL(15,2) NOT NULL DEFAULT 0.00,
  `currency` VARCHAR(10) NOT NULL DEFAULT 'CNY',
  `min_balance_alert` DECIMAL(15,2) NOT NULL DEFAULT 0.00,
  `balance_alert_enabled` TINYINT(1) NOT NULL DEFAULT 0,
  `status` INT NOT NULL DEFAULT 0 COMMENT '0-正常,1-冻结,2-注销',
  `created_by` VARCHAR(36) DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_by` VARCHAR(36) DEFAULT NULL,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `version` INT NOT NULL DEFAULT 0,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_t_wallet_enterprise_id` (`enterprise_id`),
  KEY `idx_t_wallet_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='企业钱包表';

CREATE TABLE IF NOT EXISTS `t_transaction_record` (
  `id` VARCHAR(36) NOT NULL,
  `transaction_no` VARCHAR(64) NOT NULL,
  `enterprise_id` VARCHAR(36) NOT NULL,
  `transaction_type` INT NOT NULL COMMENT '1-充值,2-投保扣费,3-退费,4-退款,5-调整,6-冻结,7-解冻',
  `amount` DECIMAL(15,2) NOT NULL DEFAULT 0.00,
  `currency` VARCHAR(10) NOT NULL DEFAULT 'CNY',
  `balance_before` DECIMAL(15,2) NOT NULL DEFAULT 0.00,
  `balance_after` DECIMAL(15,2) NOT NULL DEFAULT 0.00,
  `frozen_before` DECIMAL(15,2) NOT NULL DEFAULT 0.00,
  `frozen_after` DECIMAL(15,2) NOT NULL DEFAULT 0.00,
  `related_business_id` VARCHAR(36) DEFAULT NULL,
  `related_business_type` VARCHAR(32) DEFAULT NULL COMMENT 'policy,recharge,refund,adjustment,other',
  `related_business_desc` VARCHAR(255) DEFAULT NULL,
  `status` INT NOT NULL DEFAULT 0 COMMENT '0-待处理,1-成功,2-失败,3-取消',
  `transaction_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `completed_time` DATETIME DEFAULT NULL,
  `payment_method` VARCHAR(32) DEFAULT NULL COMMENT 'wallet_balance,bank_transfer,wechat_pay,alipay',
  `payment_no` VARCHAR(100) DEFAULT NULL,
  `remark` VARCHAR(500) DEFAULT NULL,
  `operator_user_id` VARCHAR(36) DEFAULT NULL,
  `operator_user_name` VARCHAR(100) DEFAULT NULL,
  `is_manual` TINYINT(1) NOT NULL DEFAULT 0,
  `audit_status` INT NOT NULL DEFAULT 0 COMMENT '0-无需审核,1-待审核,2-审核通过,3-审核拒绝',
  `audit_opinion` VARCHAR(500) DEFAULT NULL,
  `audit_time` DATETIME DEFAULT NULL,
  `auditor_user_id` VARCHAR(36) DEFAULT NULL,
  `auditor_user_name` VARCHAR(100) DEFAULT NULL,
  `created_by` VARCHAR(36) DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_by` VARCHAR(36) DEFAULT NULL,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `version` INT NOT NULL DEFAULT 0,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_t_transaction_record_no` (`transaction_no`),
  KEY `idx_t_transaction_record_enterprise_id` (`enterprise_id`),
  KEY `idx_t_transaction_record_type` (`transaction_type`),
  KEY `idx_t_transaction_record_business` (`related_business_type`, `related_business_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='交易流水表';

CREATE TABLE IF NOT EXISTS `t_bill` (
  `id` VARCHAR(36) NOT NULL,
  `bill_no` VARCHAR(64) NOT NULL,
  `enterprise_id` VARCHAR(36) NOT NULL,
  `bill_type` INT NOT NULL DEFAULT 1 COMMENT '1-月度账单,2-自定义账单,3-对账单',
  `period_start_date` DATE DEFAULT NULL,
  `period_end_date` DATE DEFAULT NULL,
  `generation_date` DATE DEFAULT NULL,
  `total_amount` DECIMAL(15,2) NOT NULL DEFAULT 0.00,
  `paid_amount` DECIMAL(15,2) NOT NULL DEFAULT 0.00,
  `deduction_amount` DECIMAL(15,2) NOT NULL DEFAULT 0.00,
  `receivable_amount` DECIMAL(15,2) NOT NULL DEFAULT 0.00,
  `currency` VARCHAR(10) NOT NULL DEFAULT 'CNY',
  `status` INT NOT NULL DEFAULT 0 COMMENT '0-草稿,1-已生成,2-已发送,3-已确认,4-已支付,5-已过期,6-已作废',
  `sent_time` DATETIME DEFAULT NULL,
  `confirmed_time` DATETIME DEFAULT NULL,
  `paid_time` DATETIME DEFAULT NULL,
  `payment_method` VARCHAR(32) DEFAULT NULL,
  `payment_no` VARCHAR(100) DEFAULT NULL,
  `overdue_days` INT DEFAULT 0,
  `overdue_fee` DECIMAL(15,2) NOT NULL DEFAULT 0.00,
  `due_date` DATE DEFAULT NULL,
  `remark` VARCHAR(500) DEFAULT NULL,
  `bill_details` TEXT DEFAULT NULL,
  `reconciliation_status` INT NOT NULL DEFAULT 0 COMMENT '0-未对账,1-已对账,2-有差异',
  `reconciliation_time` DATETIME DEFAULT NULL,
  `reconciliation_user_id` VARCHAR(36) DEFAULT NULL,
  `reconciliation_user_name` VARCHAR(100) DEFAULT NULL,
  `reconciliation_diff_desc` VARCHAR(500) DEFAULT NULL,
  `attachment_url` VARCHAR(500) DEFAULT NULL,
  `attachment_name` VARCHAR(255) DEFAULT NULL,
  `attachment_size` BIGINT DEFAULT NULL,
  `created_by` VARCHAR(36) DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_by` VARCHAR(36) DEFAULT NULL,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `version` INT NOT NULL DEFAULT 0,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_t_bill_no` (`bill_no`),
  KEY `idx_t_bill_enterprise_id` (`enterprise_id`),
  KEY `idx_t_bill_period` (`period_start_date`, `period_end_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='账单表';

CREATE TABLE IF NOT EXISTS `t_invoice` (
  `id` VARCHAR(36) NOT NULL,
  `invoice_no` VARCHAR(64) NOT NULL,
  `enterprise_id` VARCHAR(36) NOT NULL,
  `invoice_type` INT NOT NULL COMMENT '1-增值税普通发票,2-增值税专用发票,3-电子普通发票,4-电子专用发票',
  `invoice_title` VARCHAR(200) NOT NULL,
  `taxpayer_id` VARCHAR(50) DEFAULT NULL,
  `address` VARCHAR(500) DEFAULT NULL,
  `phone` VARCHAR(30) DEFAULT NULL,
  `bank_name` VARCHAR(100) DEFAULT NULL,
  `bank_account` VARCHAR(100) DEFAULT NULL,
  `invoice_amount` DECIMAL(15,2) NOT NULL DEFAULT 0.00,
  `tax_amount` DECIMAL(15,2) NOT NULL DEFAULT 0.00,
  `total_amount` DECIMAL(15,2) NOT NULL DEFAULT 0.00,
  `currency` VARCHAR(10) NOT NULL DEFAULT 'CNY',
  `invoice_content` VARCHAR(500) DEFAULT NULL,
  `status` INT NOT NULL DEFAULT 0 COMMENT '0-草稿,1-待审核,2-审核通过,3-审核拒绝,4-已开票,5-已作废,6-已重开',
  `application_time` DATETIME DEFAULT NULL,
  `audit_time` DATETIME DEFAULT NULL,
  `invoice_time` DATETIME DEFAULT NULL,
  `invoice_code` VARCHAR(50) DEFAULT NULL,
  `invoice_number` VARCHAR(50) DEFAULT NULL,
  `invoice_date` DATE DEFAULT NULL,
  `issuer` VARCHAR(100) DEFAULT NULL,
  `payee` VARCHAR(100) DEFAULT NULL,
  `reviewer` VARCHAR(100) DEFAULT NULL,
  `audit_opinion` VARCHAR(500) DEFAULT NULL,
  `auditor_user_id` VARCHAR(36) DEFAULT NULL,
  `auditor_user_name` VARCHAR(100) DEFAULT NULL,
  `cancel_reason` VARCHAR(500) DEFAULT NULL,
  `cancel_time` DATETIME DEFAULT NULL,
  `canceller_user_id` VARCHAR(36) DEFAULT NULL,
  `canceller_user_name` VARCHAR(100) DEFAULT NULL,
  `reissue_invoice_id` VARCHAR(36) DEFAULT NULL,
  `original_invoice_id` VARCHAR(36) DEFAULT NULL,
  `invoice_file_url` VARCHAR(500) DEFAULT NULL,
  `invoice_file_name` VARCHAR(255) DEFAULT NULL,
  `invoice_file_size` BIGINT DEFAULT NULL,
  `related_business_type` VARCHAR(32) DEFAULT NULL,
  `related_business_id` VARCHAR(36) DEFAULT NULL,
  `related_business_desc` VARCHAR(255) DEFAULT NULL,
  `remark` VARCHAR(500) DEFAULT NULL,
  `is_red_invoice` TINYINT(1) NOT NULL DEFAULT 0,
  `red_invoice_reason` VARCHAR(500) DEFAULT NULL,
  `red_invoice_no` VARCHAR(64) DEFAULT NULL,
  `delivery_method` VARCHAR(32) DEFAULT NULL COMMENT 'electronic,express,self_pickup',
  `recipient_name` VARCHAR(100) DEFAULT NULL,
  `recipient_phone` VARCHAR(30) DEFAULT NULL,
  `recipient_address` VARCHAR(500) DEFAULT NULL,
  `tracking_no` VARCHAR(100) DEFAULT NULL,
  `delivery_time` DATETIME DEFAULT NULL,
  `receive_time` DATETIME DEFAULT NULL,
  `created_by` VARCHAR(36) DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_by` VARCHAR(36) DEFAULT NULL,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `version` INT NOT NULL DEFAULT 0,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_t_invoice_no` (`invoice_no`),
  KEY `idx_t_invoice_enterprise_id` (`enterprise_id`),
  KEY `idx_t_invoice_related_business` (`related_business_type`, `related_business_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='发票表';
```

## 说明

- 本次 SQL 采用“新增运行时表”的方式，不直接覆盖旧版 `bill`、`wallet_transaction`、`invoice_info` 等早期表。
- 这样可以先保证当前后端财务模块可运行，避免对旧数据结构做高风险破坏性修改。
- 若后续确认要统一成 `spec/database.md` 的正式命名规则，建议单独做一次“实体、Mapper、SQL、接口文档”统一重构。
