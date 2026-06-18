# 最终对齐与收口说明

## 目标

本文件用于汇总本轮模块化完善过程中已经落地的数据库与设计对齐项，作为最终收口说明。

当前项目已经完成以下主线闭环：

- 企业注册、审核与企业信息维护
- 团队成员与账号管理
- 客户管理
- 投保申请、草稿、模板与提交
- 保单查询、导出、撤销、退保、续保入口
- 理赔报案、查询、补充材料与导出
- 财务钱包、流水、账单、发票与导出
- 通知中心、管理端企业审核、分析看板

## 已落地的结构对齐

### 1. 企业信息字段对齐

企业侧已经补充以下字段映射，并在代码中被实际使用：

- `enterprise.address`
- `enterprise.description`

对应增量 SQL 已记录在：

- `enterprise-policy-claim-alignment.md`

如存量库尚未执行，可使用：

```sql
ALTER TABLE `enterprise`
  ADD COLUMN `address` VARCHAR(500) NULL COMMENT '企业地址' AFTER `contact_email`,
  ADD COLUMN `description` TEXT NULL COMMENT '企业介绍' AFTER `address`;
```

### 2. 财务运行时表对齐

当前后端财务模块实际运行依赖以下表：

- `t_wallet`
- `t_transaction_record`
- `t_bill`
- `t_invoice`

完整建表 SQL 已记录在：

- `finance-schema-alignment.md`

说明：

- 当前采用“新增运行时表”的方式与老表并存，避免直接破坏早期 `wallet_transaction`、`recharge_record`、`bill`、`invoice_info` 等旧结构。
- 如果后续决定统一回到 `spec` 中的正式命名体系，建议单独安排一次结构迁移，而不是在本轮业务收口中继续混改。

### 3. 初始化建库脚本已重建完成

`01_init_schema.sql` 已从历史上的“SQL + Markdown 混合文档”重建为可直接执行的纯 SQL 初始化脚本，当前已覆盖：

- `enterprise`
- `user`
- `customer`
- `insurance_insurer`
- `insurance_application`
- `application_draft`
- `application_template`
- `t_policy`
- `t_claim`
- `t_claim_material`
- `t_claim_process_record`
- `t_wallet`
- `t_transaction_record`
- `t_bill`
- `t_invoice`
- `notifications`
- `enterprise_files`
- `data_dict`

补充说明：

- `insurance_insurer` 已纳入初始化脚本，并补充了默认保司数据，和当前前端投保页/保单页使用的保司 ID 保持一致。
- `role`、`risk_rule`、`operation_log` 等表当前不属于后端运行时必需依赖，未纳入主初始化脚本，避免引入与现代码脱节的冗余结构。
- 当前如果是新环境建库，直接执行 `01_init_schema.sql` 即可完成主业务所需的表、索引、外键与基础种子数据初始化。

## 本轮最终收口未新增额外 DDL

本轮最后阶段主要完成的是页面、交互、接口和模块流程闭环，包括：

- 管理端企业审核页独立化
- 通知中心运营能力增强
- 客户统计与 CSV 导入
- 团队成员统计、分页和权限边界
- 工作台快捷入口改为真实业务跳转
- 数据分析导出
- 保单续保入口接入投保表单

除已重建并补齐的 `01_init_schema.sql` 外，上述页面和流程收口没有再额外引入必须单独执行的数据库 DDL。

## 建议执行顺序

如果需要在新环境初始化或对齐旧环境，建议按以下顺序处理：

1. 新环境直接执行 `01_init_schema.sql`
2. 如果是历史存量库，再按需参考 `enterprise-policy-claim-alignment.md` 与 `finance-schema-alignment.md` 做增量核对
3. 完成后启动后端与前端进行联调

## 当前结论

从数据库与设计对齐角度看，当前系统已经具备支撑主要业务闭环的结构基础。

剩余事项主要属于后续增强，而非本轮主流程阻断项，例如：

- 审计日志独立表
- 大批量导入的服务端异步处理
- 分析报表专属后端聚合表或物化视图
- 旧版财务表向新运行时表的正式迁移
