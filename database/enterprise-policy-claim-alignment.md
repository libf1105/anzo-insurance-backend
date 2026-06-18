# 企业信息、保单导出与理赔通知对齐说明

## 背景

本轮按模块继续完善后，发现以下设计存在错位：

- 企业信息页前端已使用 `address`、`description`，但后端实体与初始化 SQL 未完整映射
- 企业充值与余额变动记录页面需要真实后端接口闭环，而不是仅靠占位数据
- 保单列表已有导出入口，但后端导出接口未实现
- 理赔状态流转已具备业务动作，但缺少消息通知联动

## 本轮调整

- 企业表补充字段映射：
  - `address`
  - `description`
- 企业充值接口改为接收 JSON 请求体，并同步更新企业余额、钱包流水与余额通知
- 企业余额记录接口改为返回真实交易记录分页结构
- 保单模块补充 CSV 导出
- 理赔与保单关键状态动作补充通知发送

## 存量库 SQL

如果数据库已初始化过，请执行以下变更：

```sql
ALTER TABLE `enterprise`
  ADD COLUMN `address` VARCHAR(500) NULL COMMENT '企业地址' AFTER `contact_email`,
  ADD COLUMN `description` TEXT NULL COMMENT '企业介绍' AFTER `address`;
```

## 说明

- 如果 `enterprise.address` 或 `enterprise.description` 已存在，请跳过对应 `ADD COLUMN`
- 保单导出当前为 `CSV`，便于直接落地与后续管理端复用
- 理赔/保单通知沿用现有 `notifications` 表，无需新增表结构
