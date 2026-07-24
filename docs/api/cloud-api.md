# 云端 API 文档

Base URL: `http://{host}:8080`

## 认证方式

| 端点类型 | 认证方式 | Header |
|---------|---------|--------|
| 用户端 | Session Cookie | Cookie: JSESSIONID=xxx |
| 设备端 | API Key | X-API-KEY: {key} |

写操作（POST/PUT/DELETE）需额外携带 CSRF Token：`X-CSRF-TOKEN: {token}`

---

## 用户模块 (`/user`)

### POST /user/login

登录（无需认证）。

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| tel | String | 是 | 手机号 |
| password | String | 是 | 密码 |

**响应**：

```json
{"status": 200, "msg": "区域ID"}     // 成功
{"status": 201, "msg": null}          // 密码错误
{"status": 202, "msg": null}          // 手机号不存在
{"status": 203, "msg": null}          // 未注册，联系管理员
```

### POST /user/register

注册（无需认证，需验证码）。

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| tel | String | 是 | 手机号 |
| name | String | 是 | 姓名 |
| password | String | 是 | 密码 |
| identcode | String | 是 | 短信验证码 |

### POST /user/getPhoneCode

获取短信验证码（无需认证）。

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| tel | String | 是 | 手机号 |

### POST /user/getIndexInfo

获取当前登录用户信息（需 Session）。

**响应**：User 对象（含 state 权限等级：1=超管, 2=边缘管理员, 3=保安）

### POST /user/message

获取当前用户通知消息（需 Session）。

**响应**：`[{"text": "消息内容", "time": "2024-01-01 12:00"}, ...]`

### POST /user/resetpassword

修改密码（需 Session）。

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| tel | String | 是 | 手机号 |
| password1 | String | 是 | 旧密码 |
| password2 | String | 是 | 新密码 |

### POST /user/getUserList

查询保安列表（需 Session）。

| 参数 | 类型 | 说明 |
|------|------|------|
| page | Integer | 页码 |
| limit | Integer | 每页条数 |
| aid | Integer | 区域 ID（可选） |
| equName | String | 姓名模糊搜索（可选） |

### POST /user/goOut

退出登录，销毁 Session。

---

## 设备模块 (`/device`)

### POST /device/getDeviceList

查询设备列表（需 Session）。

| 参数 | 类型 | 说明 |
|------|------|------|
| page | Integer | 页码 |
| limit | Integer | 每页条数 |
| aid | Integer | 区域 ID（可选） |
| equName | String | 设备名模糊搜索（可选） |

### POST /device/Device

绑定设备（需 Session）。

| 参数 | 类型 | 说明 |
|------|------|------|
| mac | String | 设备 MAC |
| node | String | 节点名称 |
| threshold | Integer | 预警阈值 |
| aid | Integer | 所属区域 |
| equmanager | String | 管理人员 UID |

### POST /device/selectDeviceInfo

查询单设备详情（需 Session）。

| 参数 | 类型 | 说明 |
|------|------|------|
| mac | String | 设备 MAC |

**响应**：设备信息 + 管理员列表 + 当前人流量 + 本月预警数

### POST /device/askImage

请求设备实时截图（需 Session，通过 MQ 下发到边缘端）。

| 参数 | 类型 | 说明 |
|------|------|------|
| mac | String | 设备 MAC |

---

## 设备通信端点（需 API Key）

### POST /normalup

边缘端定时上报统计数据。

| 参数 | 类型 | 说明 |
|------|------|------|
| mac | String | 设备 MAC |
| time | String | 时间 (yyyy-MM-dd HH:mm) |
| max | String | 区间最大值 |
| avg | String | 区间均值 |
| variance | String | 方差 |
| center | String | 中位数 |

### GET /upState

设备状态上报。

| 参数 | 类型 | 说明 |
|------|------|------|
| id | String | 设备 MAC |
| flag | String | 状态码：1=在线, 2=超阈值 |

**响应**：当前阈值（Integer）

### POST /uploadVideo

上传视频文件（multipart/form-data）。

| 参数 | 类型 | 说明 |
|------|------|------|
| file | File | 视频文件（mp4/avi/mkv/flv/mov，最大 500MB） |
| mac | String | 设备 MAC |
| inittime | String | 开始时间 |
| continuetime | String | 持续时间 |

### POST /device/getImage

上传设备截图（multipart/form-data）。

| 参数 | 类型 | 说明 |
|------|------|------|
| file | File | 图片文件（jpg/jpeg/png/gif/bmp，最大 10MB） |

### POST /addequipment

边缘端注册新设备。

| 参数 | 类型 | 说明 |
|------|------|------|
| equ_uuid | String | 设备 MAC |
| equ_othername | String | 节点名称 |
| equ_aid | Integer | 区域 ID |

---

## 视频模块（videoIO 独立部署）

### GET /video

视频播放（支持 Range 请求）。

| 参数 | 类型 | 说明 |
|------|------|------|
| path | String | 视频路径（禁止含 `..`） |

---

## 异常记录

### POST /exception

查询预警记录（需 Session）。

| 参数 | 类型 | 说明 |
|------|------|------|
| page | Integer | 页码 |
| limit | Integer | 每页条数 |
| day1 | String | 开始日期 |
| day2 | String | 结束日期 |
| aid | String | 区域 ID |

---

## 错误码

| HTTP Status | 含义 |
|------------|------|
| 200 | 成功 |
| 401 | 未认证（Session 无效） |
| 403 | 禁止（API Key 无效 / CSRF Token 无效） |
| 400 | 参数错误 |
| 500 | 服务器内部错误 |
