package com.anbao.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class FileUploadUtil {

    public static final Set<String> ALLOWED_VIDEO_EXTENSIONS = new HashSet<>(Arrays.asList("mp4", "avi", "mkv", "flv", "mov"));
    public static final Set<String> ALLOWED_IMAGE_EXTENSIONS = new HashSet<>(Arrays.asList("jpg", "jpeg", "png", "gif", "bmp"));
    public static final long MAX_VIDEO_SIZE = 500L * 1024 * 1024; // 500MB
    public static final long MAX_IMAGE_SIZE = 10L * 1024 * 1024;  // 10MB

    /**
     * 清理文件名，移除路径穿越字符
     */
    public static String sanitizeFileName(String filename) {
        if (filename == null || filename.isEmpty()) {
            return null;
        }
        // 取最后一个路径分隔符之后的部分
        int lastSep = Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\'));
        if (lastSep >= 0) {
            filename = filename.substring(lastSep + 1);
        }
        // 移除 .. 序列
        filename = filename.replace("..", "");
        // 只保留安全字符
        filename = filename.replaceAll("[^a-zA-Z0-9._\\-]", "_");
        if (filename.isEmpty() || filename.startsWith(".")) {
            return null;
        }
        return filename;
    }

    /**
     * 检查文件扩展名是否在白名单中
     */
    public static boolean isAllowedExtension(String filename, Set<String> allowedExtensions) {
        if (filename == null) return false;
        int dot = filename.lastIndexOf('.');
        if (dot < 0) return false;
        String ext = filename.substring(dot + 1).toLowerCase();
        return allowedExtensions.contains(ext);
    }

    /**
     * 检查文件大小是否在限制内
     */
    public static boolean isWithinSizeLimit(long size, long maxBytes) {
        return size > 0 && size <= maxBytes;
    }

    /**
     * 验证路径参数不包含路径穿越
     */
    public static boolean isValidPath(String path) {
        if (path == null || path.isEmpty()) return false;
        // 禁止 .. 和绝对路径
        if (path.contains("..") || path.startsWith("/") || path.startsWith("\\")) return false;
        // 禁止特殊字符
        if (path.matches(".*[;|&`$].*")) return false;
        return true;
    }
}
