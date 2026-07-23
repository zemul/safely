package com.anbao.utils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * CSRF 防护过滤器
 * 
 * 工作方式：
 * 1. 对每个有 session 的请求，生成 CSRF token 并存入 session
 * 2. 通过响应头 X-CSRF-TOKEN 返回 token 给前端
 * 3. 对 POST/PUT/DELETE 等修改请求，验证请求头 X-CSRF-TOKEN 是否匹配
 * 4. GET/HEAD/OPTIONS 请求不验证（幂等操作）
 */
public class CsrfFilter implements Filter {

    private static final String CSRF_TOKEN_ATTR = "_csrf_token";
    private static final String CSRF_HEADER = "X-CSRF-TOKEN";
    private static final SecureRandom random = new SecureRandom();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String method = request.getMethod().toUpperCase();

        // 幂等请求不验证 CSRF
        if ("GET".equals(method) || "HEAD".equals(method) || "OPTIONS".equals(method)) {
            // 确保 token 存在并通过响应头返回
            String token = getOrCreateToken(request);
            response.setHeader(CSRF_HEADER, token);
            chain.doFilter(request, response);
            return;
        }

        // 对写操作验证 CSRF token
        HttpSession session = request.getSession(false);
        if (session == null) {
            // 无 session 的设备端请求（已在 interceptor 中排除认证），放行
            chain.doFilter(request, response);
            return;
        }

        String sessionToken = (String) session.getAttribute(CSRF_TOKEN_ATTR);
        String requestToken = request.getHeader(CSRF_HEADER);

        // 如果 session 中没有 token（新登录），生成并放行
        if (sessionToken == null) {
            String newToken = generateToken();
            session.setAttribute(CSRF_TOKEN_ATTR, newToken);
            response.setHeader(CSRF_HEADER, newToken);
            chain.doFilter(request, response);
            return;
        }

        // 验证 token
        if (sessionToken.equals(requestToken)) {
            response.setHeader(CSRF_HEADER, sessionToken);
            chain.doFilter(request, response);
        } else {
            response.setStatus(403);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"status\":403,\"msg\":\"CSRF token invalid\"}");
        }
    }

    @Override
    public void destroy() {}

    private String getOrCreateToken(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return "";
        }
        String token = (String) session.getAttribute(CSRF_TOKEN_ATTR);
        if (token == null) {
            token = generateToken();
            session.setAttribute(CSRF_TOKEN_ATTR, token);
        }
        return token;
    }

    private static String generateToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
