package com.anbao.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.sql.*;

/**
 * 一次性密码迁移脚本
 * 将数据库中所有明文密码转换为 BCrypt 哈希
 * 
 * 使用方式：
 *   设置环境变量 ANBAO_DB_URL, ANBAO_DB_USERNAME, ANBAO_DB_PASSWORD
 *   然后运行: java -cp "target/classes:target/dependency/*" com.anbao.utils.PasswordMigration
 * 
 * 注意：
 *   - BCrypt 哈希以 $2a$ 开头，长度为 60 字符
 *   - 脚本会跳过已经是 BCrypt 格式的密码
 *   - 建议先备份数据库再运行
 *   - 运行前确保 user 表的 password 列至少为 VARCHAR(60)，参见 migration/001_password_bcrypt.sql
 */
public class PasswordMigration {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public static void main(String[] args) {
        String url = System.getenv("ANBAO_DB_URL");
        String username = System.getenv("ANBAO_DB_USERNAME");
        String password = System.getenv("ANBAO_DB_PASSWORD");

        if (url == null || username == null) {
            // Fallback for local dev
            url = "jdbc:mysql://localhost:3306/anbao?characterEncoding=utf-8";
            username = "root";
            password = password != null ? password : "";
        }

        System.out.println("=== 密码迁移脚本 ===");
        System.out.println("连接数据库: " + url);

        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL驱动未找到: " + e.getMessage());
            return;
        }

        int migrated = 0;
        int skipped = 0;
        int errors = 0;

        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            conn.setAutoCommit(false);

            // 读取所有用户
            String selectSql = "SELECT uid, password FROM user WHERE password IS NOT NULL AND password != ''";
            String updateSql = "UPDATE user SET password = ? WHERE uid = ?";

            try (PreparedStatement selectStmt = conn.prepareStatement(selectSql);
                 PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                 ResultSet rs = selectStmt.executeQuery()) {

                while (rs.next()) {
                    String uid = rs.getString("uid");
                    String plainPassword = rs.getString("password");

                    // 跳过已经是 BCrypt 格式的密码
                    if (plainPassword.startsWith("$2a$") || plainPassword.startsWith("$2b$")) {
                        skipped++;
                        continue;
                    }

                    try {
                        String hashed = encoder.encode(plainPassword);
                        updateStmt.setString(1, hashed);
                        updateStmt.setString(2, uid);
                        updateStmt.executeUpdate();
                        migrated++;
                        System.out.println("  迁移成功: uid=" + uid);
                    } catch (Exception e) {
                        errors++;
                        System.err.println("  迁移失败: uid=" + uid + ", error=" + e.getMessage());
                    }
                }
            }

            conn.commit();
            System.out.println("\n=== 迁移完成 ===");
            System.out.println("成功迁移: " + migrated + " 个用户");
            System.out.println("已跳过(已是BCrypt): " + skipped + " 个用户");
            System.out.println("失败: " + errors + " 个用户");

        } catch (SQLException e) {
            System.err.println("数据库连接失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
