-- 系统配置表
CREATE TABLE IF NOT EXISTS system_config (
                                             `key` VARCHAR(20) PRIMARY KEY,
                                             `value` VARCHAR(50)
);

-- 包裹表
CREATE TABLE IF NOT EXISTS parcels (
                                       id VARCHAR(20) PRIMARY KEY,
                                       sender VARCHAR(20) NOT NULL,
                                       receiver VARCHAR(20) NOT NULL,
                                       category VARCHAR(10) DEFAULT '普通',
                                       declared_value INT DEFAULT 0,
                                       priority VARCHAR(10) DEFAULT '普通',
                                       is_cod TINYINT(1) DEFAULT 0,
                                       cod_amount INT DEFAULT 0,
                                       address VARCHAR(200) DEFAULT '',
                                       status VARCHAR(20) DEFAULT '待揽收',
                                       current_station VARCHAR(20) DEFAULT NULL,
                                       zone CHAR(1) DEFAULT NULL,
                                       courier_id VARCHAR(20) DEFAULT NULL,
                                       waybill_id VARCHAR(20) DEFAULT NULL,
                                       created_at BIGINT DEFAULT 0,
                                       fail_count INT DEFAULT 0,
                                       last_station_time BIGINT DEFAULT 0,
                                       last_station_clock BIGINT DEFAULT 0   -- ← 新增这一行
);

-- 运单表
-- 运单表
CREATE TABLE IF NOT EXISTS waybills (
                                        id VARCHAR(20) PRIMARY KEY,
                                        parcel_id VARCHAR(20),
                                        route_legs TEXT,
                                        current_station VARCHAR(20) DEFAULT '',
                                        deliv_target VARCHAR(20) DEFAULT '',
                                        promised_at BIGINT DEFAULT 0,
                                        status VARCHAR(20) DEFAULT '已创建'
);

-- 快递员表
CREATE TABLE IF NOT EXISTS couriers (
                                        id VARCHAR(20) PRIMARY KEY,
                                        station_id VARCHAR(20) DEFAULT '',
                                        daily_quota INT DEFAULT 5,
                                        active_count INT DEFAULT 0,
                                        status VARCHAR(20) DEFAULT '待审核',
                                        fail_history TEXT
);

-- 网点表
CREATE TABLE IF NOT EXISTS stations (
                                        id VARCHAR(20) PRIMARY KEY,
                                        name VARCHAR(50) DEFAULT '',
                                        capacity INT DEFAULT 0,
                                        in_stock INT DEFAULT 0,
                                        fault_count INT DEFAULT 0,
                                        status VARCHAR(20) DEFAULT '正常'
);
-- 客户表
CREATE TABLE IF NOT EXISTS customers (
                                         id VARCHAR(20) PRIMARY KEY,
                                         credit_level VARCHAR(5) DEFAULT '铜',
                                         cod_balance INT DEFAULT 0,
                                         status VARCHAR(20) DEFAULT '正常'
);

-- 理赔单表
CREATE TABLE IF NOT EXISTS claims (
                                      id VARCHAR(20) PRIMARY KEY,
                                      parcel_id VARCHAR(20),
                                      amount INT DEFAULT 0,
                                      degree VARCHAR(10) DEFAULT '',
                                      status VARCHAR(20) DEFAULT '已受理',
                                      created_at BIGINT DEFAULT 0
);

-- COD表
CREATE TABLE IF NOT EXISTS cod_records (
                                           id VARCHAR(20) PRIMARY KEY,
                                           parcel_id VARCHAR(20),
                                           amount INT DEFAULT 0,
                                           status VARCHAR(20) DEFAULT '待收款'
);

-- 事件流表
CREATE TABLE IF NOT EXISTS events (
                                      id INT PRIMARY KEY AUTO_INCREMENT,
                                      type VARCHAR(30),
                                      data TEXT,
                                      timestamp BIGINT DEFAULT 0
);