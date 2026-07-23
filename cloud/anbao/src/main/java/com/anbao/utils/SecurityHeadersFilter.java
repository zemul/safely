package com.anbao.utils;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SecurityHeadersFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) res;

        // 防止点击劫持
        response.setHeader("X-Frame-Options", "SAMEORIGIN");
        // 防止 MIME 类型嗅探
        response.setHeader("X-Content-Type-Options", "nosniff");
        // XSS 保护（旧浏览器）
        response.setHeader("X-XSS-Protection", "1; mode=block");
        // 内容安全策略
        response.setHeader("Content-Security-Policy",
                "default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; connect-src 'self' ws: wss:;");
        // 严格传输安全（仅 HTTPS 环境生效）
        response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        // 引用策略
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        // 权限策略
        response.setHeader("Permissions-Policy", "camera=(), microphone=(), geolocation=()");

        chain.doFilter(req, res);
    }

    @Override
    public void destroy() {}
}
