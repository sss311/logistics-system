## 项目结构

### 启动类
| 文件 | 说明 |
|------|------|
| `LogisticsApplication.java` | Spring Boot 启动类，包含 `@SpringBootApplication` 和 `@MapperScan` |

### 配置层（config/）
| 文件 | 说明 |
|------|------|
| `DatabaseConfig.java` | MySQL 数据源配置，定义 `DataSource`、`SqlSessionFactory`、`TransactionManager` |
| `DatabaseInitializer.java` | 数据库表结构初始化，使用 `CREATE TABLE IF NOT EXISTS` |

### 控制器层（controller/）
| 文件 | 路径前缀 | 功能 |
|------|---------|------|
| `SystemController.java` | `/system/*` | 健康探活、逻辑时钟查询/推进、系统重置 |
| `ParcelController.java` | `/parcels/*` | 包裹全生命周期：创建、揽收、分拣、推进、派单、派送、签收、拒收、改址、报损、派送失败 |
| `WaybillController.java` | `/waybills/*` | 运单查询、路由查询、路由追加/重算、冻结、结算 |
| `StationController.java` | `/stations/*` | 网点 CRUD、停用/启用、解除预警、在仓包裹查询 |
| `CourierController.java` | `/couriers/*` | 快递员 CRUD、审核通过、异常审查、重分配 |
| `CustomerController.java` | `/customers/*` | 客户 CRUD、信用等级调整 |
| `ClaimController.java` | `/claims/*` | 理赔单查询、定损、赔付、驳回 |
| `CodRecordController.java` | `/cod/*` | 代收货款查询、对账、回款 |
| `EventController.java` | `/events` | 履约关键节点事件流（SSE） |

### 服务层（service/ + service/impl/）
| 接口 | 实现类 | 职责 |
|------|-------|------|
| `ParcelService` | `ParcelServiceImpl` | 包裹全生命周期业务逻辑 |
| `WaybillService` | `WaybillServiceImpl` | 运单创建、路由管理、状态流转 |
| `StationService` | `StationServiceImpl` | 网点管理、容量检查、停用处置 |
| `CourierService` | `CourierServiceImpl` | 快递员管理、配额检查、异常审查 |
| `CustomerService` | `CustomerServiceImpl` | 客户管理、信用调整、余额更新 |
| `ClaimService` | `ClaimServiceImpl` | 理赔全流程：定损、赔付、驳回、结案 |
| `CodRecordService` | `CodRecordServiceImpl` | COD 收款、对账、回款 |
| `SystemService` | `SystemServiceImpl` | 系统重置、逻辑时钟、高级规则检查 |

### 数据访问层（mapper/）
| 文件 | 对应实体 | 主要操作 |
|------|---------|---------|
| `ParcelMapper` | 包裹 | insert、selectById、selectAll、selectByStation、selectByCourier、update、deleteAll |
| `WaybillMapper` | 运单 | insert、selectById、selectByParcelId、selectAll、update、updateStatus |
| `StationMapper` | 网点 | insert、selectById、selectAll、updateInStock、updateFaultCount、updateStatus |
| `CourierMapper` | 快递员 | insert、selectById、selectAll、selectAvailable、updateActiveCount、updateStatus、updateFailHistory、reassign |
| `CustomerMapper` | 客户 | insert、selectById、selectAll、updateCreditLevel、updateCodBalance、updateStatus |
| `ClaimMapper` | 理赔单 | insert、selectById、selectAll、updateAmountAndStatus、updateStatus |
| `CodRecordMapper` | COD | insert、selectById、selectAll、updateStatus |
| `SystemMapper` | 系统配置 | selectClock、upsertClock |

### 实体层（model/）
| 文件 | 对应数据库表 | 核心字段数 |
|------|------------|:---:|
| `Parcel.java` | `parcels` | 18 个字段 |
| `Waybill.java` | `waybills` | 7 个字段 |
| `Station.java` | `stations` | 6 个字段 |
| `Courier.java` | `couriers` | 6 个字段 |
| `Customer.java` | `customers` | 4 个字段 |
| `Claim.java` | `claims` | 6 个字段 |
| `CodRecord.java` | `cod_records` | 4 个字段 |
| `Event.java` | `events` | 4 个字段 |

### 请求 DTO（dto/）
| 文件 | 对应接口 |
|------|---------|
| `ParcelCreateRequest.java` | POST /parcels |
| `ParcelPickupRequest.java` | POST /parcels/{id}/pickup |
| `ParcelSortRequest.java` | POST /parcels/{id}/sort |
| `ParcelRedirectRequest.java` | POST /parcels/{id}/redirect |
| `ParcelDamageRequest.java` | POST /parcels/{id}/report-damage |
| `ClockTickRequest.java` | POST /system/clock/tick |
| `CourierCreateRequest.java` | POST /couriers |
| `CustomerCreateRequest.java` | POST /customers |
| `CustomerCreditRequest.java` | POST /customers/{id}/adjust-credit |
| `StationCreateRequest.java` | POST /stations |

### 公共层（common/）
| 文件 | 说明 |
|------|------|
| `GlobalExceptionHandler.java` | 全局异常处理，统一错误响应格式 `{"error": "..."}` |
| `BusinessException.java` | 自定义业务异常，支持状态码和错误信息 |
| `constant/ParcelStatus.java` | 包裹状态枚举（11 个状态） |
| `constant/WaybillStatus.java` | 运单状态枚举（8 个状态） |
| `constant/StationStatus.java` | 网点状态枚举（5 个状态） |
| `constant/CourierStatus.java` | 快递员状态枚举（6 个状态） |
| `constant/ClaimStatus.java` | 理赔单状态枚举（5 个状态） |
| `constant/CodStatus.java` | COD 状态枚举（5 个状态） |
| `constant/CustomerStatus.java` | 客户状态枚举（4 个状态） |
| `constant/CategoryEnum.java` | 品类枚举（3 种） |
| `constant/PriorityEnum.java` | 优先级枚举（2 种） |
| `constant/CreditLevelEnum.java` | 信用等级枚举（3 种） |
| `constant/BusinessConstants.java` | 业务参数常量（11 个硬性数值） |
| `util/AddressUtil.java` | 地址归一化工具 |
| `util/AmountUtil.java` | 金额整数分处理工具 |
