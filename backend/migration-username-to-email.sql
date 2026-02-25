-- 从 username 迁移到 email + nickname（若已有数据需先备份）
-- 1. 添加新列
ALTER TABLE `user` ADD COLUMN email VARCHAR(100) NULL;
ALTER TABLE `user` ADD COLUMN nickname VARCHAR(50) NULL;
-- 2. 迁移数据（username 作为临时 email）
UPDATE `user` SET email = CONCAT(username, '@migrated.local'), nickname = CONCAT('用户', user_id) WHERE email IS NULL;
-- 3. 设置非空与唯一
ALTER TABLE `user` MODIFY email VARCHAR(100) NOT NULL;
ALTER TABLE `user` MODIFY nickname VARCHAR(50) NOT NULL DEFAULT '用户';
ALTER TABLE `user` ADD UNIQUE KEY uk_email (email);
-- 4. 删除 username
ALTER TABLE `user` DROP COLUMN username;
