package com.anbao.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码工具类 - 基于 BCrypt 的密码哈希与验证
 */
public class PasswordUtil {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /**
     * 对明文密码进行 BCrypt 哈希
     * @param raw 明文密码
     * @return BCrypt 哈希值
     */
    public static String hash(String raw) {
        return encoder.encode(raw);
    }

    /**
     * 验证明文密码是否匹配已存储的哈希值
     * @param raw 明文密码
     * @param hashed 已存储的 BCrypt 哈希值
     * @return 匹配返回 true，否则返回 false
     */
    public static boolean verify(String raw, String hashed) {
        if (raw == null || hashed == null) {
            return false;
        }
        return encoder.matches(raw, hashed);
    }
}
