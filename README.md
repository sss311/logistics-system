好的，我来帮你总结一份完整的 README，可以直接放到 GitHub 仓库首页。

---

## README.md

```markdown
# 快递物流履约系统 (Logistics Fulfillment System)

基于牛客网 AI Coding 挑战的开放需求文档，采用 **VibeCoding（AI 辅助开发）** 模式，独立设计并实现的后端服务系统。

## 项目简介

系统覆盖快递包裹从揽收、分拣、干线运输、派送到签收的全生命周期管理，并处理收件人拒收逆向退回、包裹破损理赔、在途改址重路由、货到付款（COD）代收与结算、以及网点爆仓时的调度。

## 技术栈

- **语言**：Java 21
- **框架**：Spring Boot 3.3.4
- **ORM**：MyBatis 3.0.3
- **数据库**：MySQL 8.0
- **构建工具**：Maven
- **其他**：Lombok

## 项目结构

```
src/main/java/com/logistics/
├── LogisticsApplication.java          # Spring Boot 启动类
├── config/                            # 配置类
│   ├── DatabaseConfig.java            # 数据源配置
│   └── DatabaseInitializer.java       # 数据库初始化
├── controller/                        # 控制器层（9 个）
│   ├── SystemController.java          # 系统接口（/system/*）
│   ├── ParcelController.java          # 包裹接口（/parcels/*）
│   ├── WaybillController.java         # 运单接口（/waybills/*）
│   ├── StationController.java         # 网点接口（/stations/*）
│   ├── CourierController.java         # 快递员接口（/couriers/*）
│   ├── CustomerController.java        # 客户接口（/customers/*）
│   ├── ClaimController.java           # 理赔接口（/claims/*）
│   ├── CodRecordController.java       # COD 接口（/cod/*）
│   └── EventController.java           # 事件流接口（/events）
├── service/                           # 服务层接口（8 个）
├── service/impl/                      # 服务层实现（8 个）
├── mapper/                            # MyBatis Mapper（8 个）
├── model/                             # 实体类（8 个）
├── dto/                               # 请求 DTO（10 个）
└── common/                            # 公共类
    ├── GlobalExceptionHandler.java    # 全局异常处理
    ├── BusinessException.java         # 自定义业务异常
    ├── constant/                      # 状态枚举常量（11 个）
    └── util/                          # 工具类
        ├── AddressUtil.java           # 地址归一化
        └── AmountUtil.java            # 金额整数分处理
```

## 核心功能

### 七大实体管理

| 实体 | 功能 |
|------|------|
| 包裹（Parcel） | 全生命周期管理：创建、揽收、分拣、推进、派单、派送、签收、拒收、改址、报损 |
| 运单（Waybill） | 路由段记录、当前网点、时效承诺、路由追加/重算/冻结/结算 |
| 网点（Station） | 容量与在仓量、入站出站、停用启用、质量预警 |
| 快递员（Courier） | 日配额与在途件数、审核、派件占额、释放、异常审查、重分配 |
| 客户（Customer） | 寄件人/收件人双角色、信用等级、代收余额 |
| 理赔单（Claim） | 破损/丢件赔付凭证、定损、赔付、驳回、结案 |
| 代收货款（COD） | 签收时收款、对账、回款给寄件人、拒收时关闭 |

### 三条业务主线

1. **正向履约线**：创建 → 揽收 → 分拣 → 推进 → 派单 → 派送 → 签收
2. **异常处理线**：派送失败、收件人拒收、运输破损、在途改址、网点停用
3. **财务结算线**：COD 收款/对账/回款、理赔定损/赔付

### 四大高级规则

| 规则 | 触发条件 | 触发动作 |
|------|---------|---------|
| 网点滞留自动推进 | 包裹在某网点滞留 ≥ 24 小时 | 自动推进到下一站 |
| 快递员异常审查 | 6 小时内累计派送失败 4 次 | 快递员转入“异常审查”，在途包裹收回 |
| 理赔超时自动定损 | 理赔受理 48 小时未定损 | 按全损金额（100%）自动定损 |
| 网点质量预警 | 网点累计责任 ≥ 3 次 | 网点状态变为“质量预警” |

## API 接口列表

### 系统接口
| 方法 | 路径 | 功能 |
|------|------|------|
| GET | / | 健康探活 |
| POST | /system/reset | 重置全部数据 |
| GET | /system/clock | 查询逻辑时钟 |
| POST | /system/clock/tick | 推进逻辑时钟 |

### 包裹接口（/parcels）
| 方法 | 路径 | 功能 |
|------|------|------|
| POST | /parcels | 创建包裹 |
| GET | /parcels | 查询包裹列表 |
| GET | /parcels/{id} | 查询包裹详情 |
| POST | /parcels/{id}/pickup | 揽收 |
| POST | /parcels/{id}/sort | 分拣 |
| POST | /parcels/{id}/advance | 推进 |
| POST | /parcels/{id}/assign-courier | 派单 |
| POST | /parcels/{id}/deliver | 派送 |
| POST | /parcels/{id}/deliver-fail | 派送失败 |
| POST | /parcels/{id}/sign | 签收 |
| POST | /parcels/{id}/reject | 拒收 |
| POST | /parcels/{id}/redirect | 改址 |
| POST | /parcels/{id}/report-damage | 上报破损 |

### 运单接口（/waybills）
| 方法 | 路径 | 功能 |
|------|------|------|
| GET | /waybills | 查询运单列表 |
| GET | /waybills/{id} | 查询运单详情 |
| GET | /waybills/{id}/route | 查询运单路由 |
| POST | /waybills/{id}/append-leg | 追加路由段 |
| POST | /waybills/{id}/recalc-route | 重算路由 |
| POST | /waybills/{id}/freeze | 理赔冻结 |
| POST | /waybills/{id}/settle | 运单结算 |

### 其他接口
- **网点**：/stations — CRUD + 停用/启用/解除预警/查询在仓包裹
- **快递员**：/couriers — CRUD + 审核/异常审查/重分配
- **客户**：/customers — CRUD + 信用调整
- **理赔**：/claims — 查询/定损/赔付/驳回
- **COD**：/cod — 查询/对账/回款

## 快速启动

### 环境要求
- JDK 21
- Maven 3.6+
- MySQL 8.0+

### 创建数据库
```sql
CREATE DATABASE logistics CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 修改数据库配置
编辑 `src/main/resources/application.properties`，修改数据库连接信息：
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/logistics?useSSL=false&serverTimezone=UTC&characterEncoding=utf-8
spring.datasource.username=root
spring.datasource.password=your_password_here
```

### 启动项目
```bash
mvn spring-boot:run
```

### 初始化数据
```bash
curl -X POST http://localhost:8000/system/reset
```

## 开发模式

本项目采用 **VibeCoding（AI 辅助开发）** 模式完成：

- **架构设计**：由开发者独立完成，包括四层架构设计、实体关系建模、状态流转设计
- **代码生成**：使用 AI 工具（DeepSeek）辅助生成 Controller、Service、Mapper、Model 层代码
- **代码审查**：开发者负责审查所有 AI 生成的代码，验证业务逻辑正确性
- **测试验证**：使用 curl 和 TestAutoAdvance 逐接口验证功能正确性
- **Bug 修复**：发现的 bug 由开发者定位根因，AI 辅助生成修复代码

**开发周期**：约 12 小时（包含需求分析、架构设计、代码实现、测试验证、Bug 修复）

## 项目来源

本项目基于牛客网 AI Coding 挑战的一道开放需求文档。需求给出了业务约束、状态枚举、接口列表等开放规范，由开发者独立完成架构设计、数据库建模、全部接口实现和业务逻辑闭环。

## 许可证

本项目仅用于学习和展示目的。
```

---

这份 README 已经完整覆盖了项目简介、技术栈、项目结构、核心功能、API 列表、快速启动指南、开发模式说明和项目来源。可以直接复制粘贴到你的 GitHub 仓库中。
