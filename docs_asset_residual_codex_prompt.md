# Codex 执行提示词（资产核心管理-资产残值配置）

你是资深全栈工程师，请在 **现有代码风格与分层架构** 上实现“资产核心管理”中的“资产残值配置 + 资产定价配置”能力。请严格遵循以下任务说明，一次性交付可运行代码、SQL、接口与测试。

## 0. 先做代码库调研（必须）
1. 优先阅读并复用标准商品（SPU/SKU）已有实现链路：
   - `bajiecloud-product-server/src/main/java/com/bajiezu/cloud/product/controller/StandardProductController.java`
   - `.../service/StandardProductService*`
   - `.../controller/vo/response/StandardProductRespVO.java`
   - `.../controller/vo/StandardProductSkuVO.java`
   - `.../controller/MarketingProductController.java`（参考现有 SPU/SKU 查询模式）
2. 输出一段“现状结论”注释在 PR 描述中：
   - 当前系统里 SPU 列表查询入口
   - 按 SPU 过滤 SKU 的可复用接口/DAO
   - partner_id、is_deleted、status 的通用过滤方式

## 1. 数据库与实体层
根据以下三张表落库（若仓库已有 migration 规范，按现有规范新增 SQL 脚本）：
- `asset_residual_config`
- `asset_residual_year_config`
- `asset_residual_month_config`

要求：
1. 保持给定字段、索引、唯一键一致。
2. 所有查询默认追加 `is_deleted = 0`。
3. 全部写操作注入 `partner_id/create_by/update_by/create_time/update_time`。
4. 采用软删，不做物理删除。

## 2. 枚举与领域模型
新增并统一使用枚举（禁止魔法值）：
- 折旧规则类型：
  - `1 固定金额`
  - `2 残值比例`
- 折旧规则二级类型：
  - `1 等额`
  - `2 等差/差额`
- 使用年份：`1/2/3`
- 状态：`0 启用`，`1 禁用`

补充 DTO/VO：
- 残值配置保存请求（主表+年度+月度）
- 残值配置详情响应（含3年12个月完整结构）
- SPU 查询 SKU 列表响应（满足页面颜色/规格联动）

## 3. 接口设计（管理后台）
在资产核心管理模块新增控制器（命名贴合现有风格），至少包含：
1. `POST /asset/residual/spu/page`：SPU 分页/筛选（复用标准商品能力）
2. `POST /asset/residual/sku/list`：按 `spuId + 可选属性(颜色/规格)` 查询 SKU 列表
3. `POST /asset/residual/config/save`：保存残值配置（含月度规则）
4. `POST /asset/residual/config/detail`：配置详情
5. `POST /asset/residual/config/status/change`：启停

接口约束：
- 全部走 `CommonResult` 包装
- 参数校验用 `jakarta.validation`
- 所有错误提示文案使用需求给定中文文案

## 4. 核心计算规则（必须沉淀到 Domain Service）
实现 `ResidualValueCalculator`（或同等职责类），禁止在 Controller 里写公式。

### 4.1 设备期初价值
- 第一年第1个月：`beginValue = officialPrice`
- 同年后续月：`beginValue = 上月 residualValue`
- 第二/三年第1个月：`beginValue = 上一年 yearEndResidualValue`
- 第二/三年后续月：`beginValue = 上月 residualValue`

### 4.2 折旧金额
- 固定金额：`depreciationAmount = depreciationRuleValue`
- 残值比例：`depreciationAmount = beginValue * depreciationRuleValue`

> 注：比例字段在存储层统一为小数（如 15% 存 0.15），前端展示可转百分数。

### 4.3 月度派生值
- `residualValue = beginValue - depreciationAmount`
- `accumulatedDepreciationAmount = 当月及之前折旧金额累加`
- `currentPurchaseAmount = beginValue - accumulatedDepreciationAmount`

### 4.4 年度派生值
- `yearDepreciationAmount = 该年12个月折旧和`
- `yearEndResidualValue = yearBeginValue - yearDepreciationAmount`
- `totalPriceUpperLimit = yearBeginValue * totalPriceUpperCoefficient`
- `totalPriceLowerLimit = yearBeginValue * totalPriceLowerCoefficient`

## 5. 校验规则（后端强校验）
严格按以下文案抛错：
1. 官网价 `> 0`：`官网价必须大于 0，请重新输入`
2. 固定金额：
   - `< 0`：`折旧金额不能为负数`
   - `> 当期设备价值`：`折旧金额不能超过当期设备价值，请重新输入`
3. 残值比例：
   - 不在 `[0,1]`：`折旧比例需在 0-100% 之间`
   - 计算后折旧金额超限：`按比例计算的折旧金额超过了当期设备价值，请调整比例`
4. 月度残值小于0：`折旧金额过大，会导致设备残值为负数，请调整`
5. 上下限系数：
   - 任一 `<=0` 或 `lower>=upper`：`设备总价下限高于设备总价上限，请调整`

## 6. 与“资产定价配置”联动
新增“资产定价配置”领域对象/接口时，直接复用残值计算结果：
- 第一/二/三年设备价值取各年 `yearBeginValue`
- 到期购买金：`设备总价 - 当年折旧总额`
- 校验“设备总价上下限区间”必须使用残值配置中的 `totalPriceLowerLimit/UpperLimit`

并实现以下校验文案：
- `设备总价系数必须大于 0，请重新输入`
- `设备总价计算结果为 0 或负数，请检查系数`
- `设备总价超出允许范围`
- `设备总价低于允许范围`
- `总租金系数不能为负数，请重新输入`
- `总租金计算结果为负数，请检查系数`
- `月租金 / 日租金不能为负数，请检查总租金系数`
- `到期购买金不能为负数，请检查总价或折旧配置`
- 预警：`当前定价低于设备残值，存在亏损风险，请确认`

## 7. UI 对齐（按设计图）
实现前端页面（若仓库含前端工程）：
1. 顶部筛选：SPU、颜色、规格、重置、查询
2. Tab：`资产残值配置` / `资产定价配置`
3. 残值配置区：
   - 官网价输入
   - 折旧规则类型 + 二级类型
   - 批量填充按钮（可对当前年12个月规则值一键填充）
4. 年度折叠面板（第一/二/三年）：
   - 上下限系数输入
   - 显示设备期初价值、设备残值
   - 12个月表格列：使用月份、设备价值、折旧规则、折旧金额、设备残值、当期购买金
5. 保存设置按钮

交互要求：
- 切换规则类型时，月度输入框单位自动切换（元/%）
- 输入变化后实时重算月度/年度派生值
- 明确高亮校验错误并阻止提交

## 8. 事务与并发
1. `config/save` 全链路事务，保证主表/年表/月表一致。
2. 利用唯一键 `uk_sku_partner_deleted` 保证同一 SKU 同合作商仅一条启用配置。
3. 保存时采用“先查后更 + 乐观策略”（版本号或更新时间比对，按项目现状选型）。

## 9. 测试要求（必须）
至少补充：
1. 单元测试：
   - 固定金额等额
   - 固定金额等差
   - 残值比例等额
   - 残值比例差额
   - 边界值（0、100%、残值为0临界）
2. 集成测试：
   - `save -> detail` 数据闭环
   - `spu -> sku` 查询链路
   - 不合法输入文案断言

## 10. 交付清单
最终提交需包含：
1. SQL migration
2. 后端 controller/service/domain/repository/entity/mapper
3. 前端页面与 API 对接（若仓库包含前端）
4. 单元测试 + 集成测试
5. README 或模块说明（写明公式、比例存储约定、校验规则）

## 11. 实现约束
- 禁止硬编码 partnerId，必须从登录态上下文获取。
- 金额统一 `BigDecimal`，禁止 `double/float`。
- BigDecimal 运算必须显式 `scale + roundingMode`。
- 禁止 N+1 查询，月度数据按配置ID批量查询。
- 输出日志需包含配置ID、skuId、partnerId，便于审计。

请按以上步骤实施，并在提交说明中给出：
- 关键类清单
- 公式实现位置
- 校验实现位置
- 已执行测试命令与结果
