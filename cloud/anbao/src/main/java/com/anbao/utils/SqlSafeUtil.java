package com.anbao.utils;

import java.util.regex.Pattern;

/**
 * SQL 安全工具类 - 防止 ORDER BY 注入
 */
public class SqlSafeUtil {

    // 只允许列名(字母数字下划线)、ASC/DESC、逗号、空格
    private static final Pattern SAFE_ORDER_BY = Pattern.compile("^[a-zA-Z0-9_]+(\\s+(ASC|DESC))?(\\s*,\\s*[a-zA-Z0-9_]+(\\s+(ASC|DESC))?)*$", Pattern.CASE_INSENSITIVE);

    /**
     * 验证 ORDER BY 子句是否安全
     * @param orderByClause 排序子句
     * @return 安全返回原值，不安全返回 null
     */
    public static String validateOrderBy(String orderByClause) {
        if (orderByClause == null || orderByClause.trim().isEmpty()) {
            return null;
        }
        if (SAFE_ORDER_BY.matcher(orderByClause.trim()).matches()) {
            return orderByClause;
        }
        // 不安全的 orderBy，记录日志并拒绝
        System.err.println("[SECURITY] Rejected unsafe orderByClause: " + orderByClause);
        return null;
    }
}
