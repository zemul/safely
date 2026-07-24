-- Safely 数据库初始化脚本
-- 云端数据库: anbao
-- 边缘端数据库: py3

-- ============================================================
-- 云端数据库
-- ============================================================

CREATE DATABASE IF NOT EXISTS anbao DEFAULT CHARACTER SET utf8mb4;
USE anbao;

-- 区域表
CREATE TABLE IF NOT EXISTS area (
    aid INT AUTO_INCREMENT PRIMARY KEY,
    addr VARCHAR(255),
    name VARCHAR(100),
    tel VARCHAR(20)
) ENGINE=InnoDB;

-- 用户表
CREATE TABLE IF NOT EXISTS user (
    uid VARCHAR(64) PRIMARY KEY,
    tel VARCHAR(20),
    name VARCHAR(100),
    password VARCHAR(255),
    state INT DEFAULT 1,
    email VARCHAR(100),
    aid INT,
    addr VARCHAR(255)
) ENGINE=InnoDB;

-- 设备表 (monitored = 监控设备)
CREATE TABLE IF NOT EXISTS monitored (
    mac VARCHAR(64) PRIMARY KEY,
    aid INT,
    node VARCHAR(100),
    state VARCHAR(20) DEFAULT 'offline',
    threshold INT DEFAULT 30,
    createtime VARCHAR(30),
    time VARCHAR(30),
    num INT DEFAULT 0
) ENGINE=InnoDB;

-- 流量数据表
CREATE TABLE IF NOT EXISTS flowdata (
    mac VARCHAR(64),
    time DATETIME,
    avg DOUBLE,
    max INT,
    center DOUBLE,
    variance DOUBLE,
    KEY idx_mac_time (mac, time)
) ENGINE=InnoDB;

-- 异常事件表
CREATE TABLE IF NOT EXISTS exception (
    eid VARCHAR(64) PRIMARY KEY,
    inittime VARCHAR(30),
    continuetime VARCHAR(30),
    videourl VARCHAR(500),
    mac VARCHAR(64)
) ENGINE=InnoDB;

-- 用户-设备关联表
CREATE TABLE IF NOT EXISTS user_mac (
    uid VARCHAR(64),
    mac VARCHAR(64),
    PRIMARY KEY (uid, mac)
) ENGINE=InnoDB;

-- 消息表
CREATE TABLE IF NOT EXISTS message (
    uid VARCHAR(64),
    content TEXT,
    time DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_uid (uid)
) ENGINE=InnoDB;

-- 预测结果表
CREATE TABLE IF NOT EXISTS forecast (
    mac VARCHAR(64),
    time DATETIME,
    avg DOUBLE,
    KEY idx_mac_time (mac, time)
) ENGINE=InnoDB;

-- ============================================================
-- 插入测试数据
-- ============================================================

-- 测试区域
INSERT INTO area (addr, name, tel) VALUES ('测试广场A区', '管理员', '13800000001');

-- 测试用户 (密码: admin123, BCrypt加密)
INSERT INTO user (uid, tel, name, password, state, email, aid)
VALUES ('admin', '13800000001', '管理员',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        1, 'admin@test.com', 1);

-- 测试设备
INSERT INTO monitored (mac, aid, node, state, threshold, createtime)
VALUES ('test-device-001', 1, '测试摄像头', 'offline', 30, NOW());

-- 关联用户和设备
INSERT INTO user_mac (uid, mac) VALUES ('admin', 'test-device-001');

-- ============================================================
-- 边缘端数据库 (如果边缘端连同一个MySQL)
-- ============================================================

CREATE DATABASE IF NOT EXISTS py3 DEFAULT CHARACTER SET utf8mb4;
USE py3;

-- 边缘端用同样的 monitored 表查设备列表
CREATE TABLE IF NOT EXISTS monitored (
    mac VARCHAR(64) PRIMARY KEY,
    aid INT,
    node VARCHAR(100),
    state VARCHAR(20) DEFAULT 'offline',
    threshold INT DEFAULT 30,
    createtime VARCHAR(30),
    time VARCHAR(30),
    num INT DEFAULT 0
) ENGINE=InnoDB;

INSERT INTO monitored (mac, aid, node, state, threshold, createtime)
VALUES ('test-device-001', 1, '测试摄像头', 'offline', 30, NOW());
