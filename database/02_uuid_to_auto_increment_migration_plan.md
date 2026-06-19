# UUID To Auto Increment Migration Plan

## Goal

将当前系统中以 `UUID/String` 作为主键与关联键的设计，整体替换为以 `BIGINT AUTO_INCREMENT` 作为主键与外键的设计。

## Scope

### Core Tables

- `enterprise`
- `user`
- `customer`
- `insurance_insurer`
- `insurance_application`
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

### Backend Impact

- `BaseEntity.id` 由 `String` 改为 `Long`
- 所有实体类中的 `xxxId` 由 `String` 改为 `Long`
- 所有 DTO 中的 `id`/`xxxId` 由 `String` 改为 `Long`
- 所有 Controller 中的 `@PathVariable` / `@RequestParam` 主键参数由 `String` 改为 `Long`
- 所有 Service / Repository / QueryWrapper 条件中主键类型同步改为 `Long`
- 移除手工 `setId(IdUtil.fastSimpleUUID())` 与各类 `generateId()` 主键生成逻辑
- 保留文件对象 key、单号、业务编号等非主键业务标识的字符串生成逻辑

### Frontend Impact

- 所有 `id` / `enterpriseId` / `userId` / `policyId` / `claimId` 等从 `string` 改为 `number`
- 所有 API 方法签名中的主键参数从 `string` 改为 `number`
- 相关页面路由跳转、表格行操作、详情页查询参数全部同步

## Execution Order

1. 修改数据库初始化 SQL 与迁移 SQL
2. 修改后端 `BaseEntity` 与实体字段类型
3. 修改后端 DTO / Controller / Service / Repository 参数类型
4. 清理 UUID 主键生成逻辑
5. 修改前端 `types` / `api` / 页面组件类型
6. 全量编译并修正类型错误
7. 回归验证注册、企业、客户、投保、保单、理赔、财务、通知

## Risks

- 这是全链路破坏性改造，不能只改数据库或只改实体
- 现有接口返回结构中的 `id` 类型会从字符串变成数字
- 若数据库中已有旧数据，必须提供迁移脚本，不能只替换初始化 SQL
- 若第三方系统或页面缓存依赖 UUID 字符串，需要同步清理缓存并重新登录

## Current Status

- 已完成影响面扫描
- 已确认本次改造属于全系统级重构，不适合局部替换
- 下一步按“数据库 -> 后端 -> 前端 -> 回归验证”顺序实施
