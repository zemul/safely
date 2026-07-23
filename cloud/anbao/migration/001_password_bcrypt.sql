-- 密码列扩展为 BCrypt 长度 (60 chars)
-- 执行前请备份数据库!

ALTER TABLE `user` MODIFY COLUMN `password` VARCHAR(60) DEFAULT NULL;

-- 验证：运行 PasswordMigration.java 后，所有密码应以 $2a$ 开头
-- SELECT uid, LEFT(password, 4) FROM user;
