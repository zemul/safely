package com.anbao.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * 设备端 API Key 认证拦截器
 * 设备请求通过 X-API-KEY 请求头传递 API Key
 */
public class ApiKeyInterceptor implements HandlerInterceptor {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String API_KEY_HEADER = "X-API-KEY";
    private static final String API_KEY;

    static {
        String key = System.getenv("DEVICE_API_KEY");
        if (key == null || key.trim().isEmpty()) {
            // 生产环境必须设置，开发环境使用默认值
            key = "dev-unsafe-default-key";
            System.err.println("[WARN] DEVICE_API_KEY not set, using unsafe default. Set this in production!");
        }
        API_KEY = key;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String providedKey = request.getHeader(API_KEY_HEADER);
        if (providedKey == null) {
            // 也支持通过查询参数传递（兼容旧设备）
            providedKey = request.getParameter("api_key");
        }

        if (API_KEY.equals(providedKey)) {
            return true;
        }

        response.setStatus(403);
        response.setContentType("application/json;charset=UTF-8");
        Map<String, Object> result = new HashMap<>();
        result.put("status", 403);
        result.put("msg", "invalid api key");
        response.getWriter().write(mapper.writeValueAsString(result));
        return false;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {}

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {}
}
