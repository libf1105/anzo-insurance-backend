-- 说明:
-- 1. 该脚本用于将当前开发库重置为“自增主键版本”
-- 2. 会删除现有业务表数据，请先确认库中数据可清空
-- 3. 执行顺序:
--    mysql -u<user> -p anzo_insurance_dev < 03_reset_schema_for_auto_increment.sql
--    mysql -u<user> -p anzo_insurance_dev < 01_init_schema.sql

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `t_claim_process_record`;
DROP TABLE IF EXISTS `t_claim_material`;
DROP TABLE IF EXISTS `t_claim`;
DROP TABLE IF EXISTS `t_policy`;
DROP TABLE IF EXISTS `insurance_application`;
DROP TABLE IF EXISTS `application_template`;
DROP TABLE IF EXISTS `t_invoice`;
DROP TABLE IF EXISTS `t_bill`;
DROP TABLE IF EXISTS `t_transaction_record`;
DROP TABLE IF EXISTS `t_wallet`;
DROP TABLE IF EXISTS `notifications`;
DROP TABLE IF EXISTS `enterprise_files`;
DROP TABLE IF EXISTS `customer`;
DROP TABLE IF EXISTS `user`;
DROP TABLE IF EXISTS `insurance_insurer`;
DROP TABLE IF EXISTS `data_dict`;
DROP TABLE IF EXISTS `enterprise`;

SET FOREIGN_KEY_CHECKS = 1;
