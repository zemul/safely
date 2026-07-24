# 安全机制说明

## 认证与授权

### 用户端（浏览器）

- **方式**：Session-based 认证
- **拦截器**：`AuthInterceptor` 校验 `httpSession.getAttribute("user")`
- **未认证响应**：HTTP 401 `{"status":401,"msg":"unauthorized"}`
- **排除路径**：`/user/login`、`/user/register`、`/user/getPhoneCode`

### 设备端（边缘节点）

- **方式**：API Key 认证
- **拦截器**：`ApiKeyInterceptor` 校验 `X-API-KEY` 请求头
- **配置**：环境变量 `DEVICE_API_KEY`
- **覆盖路径**：`/normalup`、`/upState`、`/uploadVideo`、`/addequipment`、`/device/getImage`

### WebSocket

- **方式**：连接时验证 `?token=` 查询参数
- **无效连接**：立即关闭，CloseCode = `VIOLATED_POLICY`

## 密码安全

| 措施 | 实现 |
|------|------|
| 哈希算法 | BCrypt（`BCryptPasswordEncoder`） |
| 存储格式 | `$2a$10$...`（60 字符） |
| 工具类 | `PasswordUtil.hash()` / `PasswordUtil.verify()` |
| 明文禁止 | 注册、修改密码时自动哈希 |

迁移工具：`PasswordMigration.java`（将旧明文密码批量转换为 BCrypt）

## CSRF 防护

- **过滤器**：`CsrfFilter`（注册在 `web.xml`）
- **机制**：服务端生成 Token 存入 Session，通过响应头 `X-CSRF-TOKEN` 返回
- **验证**：POST/PUT/DELETE 请求必须携带 `X-CSRF-TOKEN` 头
- **豁免**：GET/HEAD/OPTIONS 请求、无 Session 的设备端请求

前端集成示例：

```javascript
// 从响应头获取 token
var csrfToken = '';
$.ajaxSetup({
    complete: function(xhr) {
        var token = xhr.getResponseHeader('X-CSRF-TOKEN');
        if (token) csrfToken = token;
    },
    beforeSend: function(xhr) {
        if (csrfToken) xhr.setRequestHeader('X-CSRF-TOKEN', csrfToken);
    }
});
```

## XSS 防护

| 层面 | 措施 |
|------|------|
| 后端 | 安全响应头 `Content-Security-Policy` |
| 前端 | `escapeHtml()` 函数转义动态内容 |
| 前端 | `.text()` 替代 `.html()` 渲染数据 |
| CSP | `script-src 'self' 'unsafe-inline'`（限制外部脚本） |

## 安全响应头

`SecurityHeadersFilter` 为所有响应添加：

| Header | 值 | 作用 |
|--------|-----|------|
| X-Frame-Options | SAMEORIGIN | 防止点击劫持 |
| X-Content-Type-Options | nosniff | 防止 MIME 嗅探 |
| X-XSS-Protection | 1; mode=block | 浏览器 XSS 过滤 |
| Content-Security-Policy | 限制性策略 | 防止代码注入 |
| Strict-Transport-Security | max-age=31536000 | 强制 HTTPS |
| Referrer-Policy | strict-origin-when-cross-origin | 限制引用信息 |
| Permissions-Policy | camera=(), microphone=() | 禁用敏感 API |

## 文件上传安全

`FileUploadUtil` 提供：

- **文件名清理**：移除 `..`、路径分隔符，只保留安全字符
- **扩展名白名单**：视频 (mp4/avi/mkv/flv/mov)、图片 (jpg/jpeg/png/gif/bmp)
- **大小限制**：视频 500MB、图片 10MB
- **路径验证**：`isValidPath()` 拒绝路径穿越

## SQL 注入防护

| 层面 | 措施 |
|------|------|
| MyBatis | 参数化查询 `#{}` |
| orderByClause | `SqlSafeUtil.validateOrderBy()` 正则白名单 |
| Prophet 模块 | `pd.read_sql_query(sql, db, params=[mac])` |

## CORS 配置

- **集中管理**：`springmvc.xml` 中 `<mvc:cors>` 配置
- **移除**：所有 `@CrossOrigin(origins = {"*"})` 注解
- **允许域名**：通过 `CORS_ALLOWED_ORIGINS` 环境变量配置
- **凭证支持**：`allow-credentials="true"`

## 消息队列安全

### HMAC-SHA256 签名

边缘端和云端之间的 MQ 消息使用 HMAC 签名：

```
消息格式: payload|hmac_sha256_hex
```

- **签名密钥**：`MQ_SIGNING_SECRET` 环境变量
- **边缘→云**：`realtime_upload` 签名，`MessageConsumer` 验证
- **云→边缘**：`sendFanoutGb` 签名，`callback` 验证
- **向后兼容**：过渡期接受无签名消息

## 凭据管理

- 所有密码/密钥通过环境变量注入
- `.env` 文件已加入 `.gitignore`
- 提供 `.env.example` 模板（不含实际值）
- 阿里云 AccessKey 建议使用 RAM 子账号 + 最小权限

## Redis 认证

- 云端：`redis.properties` 中 `redis.password=${REDIS_PASSWORD:}`
- 边缘端：`config.py` 中 `RedisPassword = os.environ.get("REDIS_PASSWORD", "")`
- 连接时自动根据密码是否为空决定是否认证
