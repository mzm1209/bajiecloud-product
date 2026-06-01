# 营销商品 SKU 租期价格库存配置接口 JSON Schema

本文档覆盖本次 SKU 维度租赁方式/租期价格库存配置改造涉及的写入和查询接口。

## 约定

- 所有接口均为 `POST`，请求体和响应体均为 JSON。
- 金额字段均为 `元 * 10000` 后的整数，例如 `1.11` 元传/返 `11100`。
- 后端不对 SKU 租期价格库存金额做换算、计算或强制覆盖性校验。
- 响应统一包裹为 `CommonResult<T>`，本文档按常见结构描述为：
  - `code`：业务状态码；
  - `msg`：业务提示；
  - `data`：接口业务数据。

## 公共 Schema 定义

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "$defs": {
    "CommonResultBoolean": {
      "type": "object",
      "additionalProperties": true,
      "properties": {
        "code": { "type": "integer", "description": "业务状态码" },
        "msg": { "type": ["string", "null"], "description": "业务提示" },
        "data": { "type": "boolean", "description": "是否成功" }
      },
      "required": ["code", "data"]
    },
    "LongIdRequest": {
      "type": "object",
      "additionalProperties": false,
      "properties": {
        "id": { "type": "integer", "format": "int64", "description": "ID" }
      },
      "required": ["id"]
    },
    "IdAndName": {
      "type": "object",
      "additionalProperties": true,
      "properties": {
        "id": { "type": ["integer", "null"], "format": "int64" },
        "name": { "type": ["string", "null"] }
      }
    },
    "AreaCodeAndName": {
      "type": "object",
      "additionalProperties": true,
      "properties": {
        "areaCode": { "type": ["string", "null"], "description": "地区编码" },
        "areaName": { "type": ["string", "null"], "description": "地区名称" },
        "shippingCost": { "type": ["integer", "null"], "format": "int64", "description": "地区对应的邮费" }
      }
    },
    "MarketingProductPropertyValue": {
      "type": "object",
      "additionalProperties": true,
      "properties": {
        "productPropertyValueId": { "type": ["integer", "null"], "format": "int64", "description": "商品属性值ID；自定义属性值可为空" },
        "value": { "type": ["string", "null"], "description": "商品属性值" },
        "sort": { "type": ["integer", "null"], "default": 0, "description": "排序" },
        "picUrl": { "type": ["string", "null"], "description": "图片" },
        "marketingCornerText": { "type": ["string", "null"], "description": "营销角标文案" },
        "unqKey": { "type": ["string", "null"], "description": "内部唯一键，前端通常无需传入" }
      }
    },
    "MarketingProductProperty": {
      "type": "object",
      "additionalProperties": true,
      "properties": {
        "propertyId": { "type": ["integer", "null"], "format": "int64", "description": "商品属性ID" },
        "propertyName": { "type": ["string", "null"], "description": "商品属性名称；详情出参返回" },
        "sort": { "type": ["integer", "null"], "default": 0, "description": "排序" },
        "isAddPropertyPic": { "type": ["integer", "null"], "enum": [0, 1, null], "description": "是否添加属性图：0-否，1-是" },
        "isAddMarketingCorner": { "type": ["integer", "null"], "enum": [0, 1, null], "description": "是否添加营销角标：0-否，1-是" },
        "isSkuProperty": { "type": ["integer", "null"], "enum": [0, 1, null], "description": "是否 SKU 销售属性：0-否，1-是" },
        "propertyValues": {
          "type": ["array", "null"],
          "items": { "$ref": "#/$defs/MarketingProductPropertyValue" },
          "description": "商品属性值"
        }
      }
    },
    "MarketingProductRentalMethod": {
      "type": "object",
      "additionalProperties": true,
      "properties": {
        "rentalMethod": { "type": ["integer", "null"], "enum": [1, 2, null], "description": "租赁方式：1-租完归还，2-灵活租" },
        "rentalMethodName": { "type": ["string", "null"], "description": "租赁方式名称；详情出参返回，入参可不传" },
        "rentalPeriods": {
          "type": ["array", "null"],
          "items": { "type": "integer", "enum": [3, 6, 12] },
          "uniqueItems": true,
          "description": "租期，单位月，可选 3、6、12"
        }
      }
    },
    "SkuPropertyValue": {
      "type": "object",
      "additionalProperties": true,
      "properties": {
        "propertyId": { "type": ["integer", "null"], "format": "int64", "description": "属性ID" },
        "propertyName": { "type": ["string", "null"], "description": "属性名；详情出参返回" },
        "propertyValueId": { "type": ["integer", "null"], "format": "int64", "description": "属性值ID" },
        "propertyValue": { "type": ["string", "null"], "description": "属性值" }
      }
    },
    "MarketingProductSkuRentalMethodProperty": {
      "type": "object",
      "additionalProperties": true,
      "properties": {
        "rentalMethod": { "type": ["integer", "null"], "enum": [1, 2, null], "description": "租赁方式：1-租完归还，2-灵活租" },
        "rentalMethodName": { "type": ["string", "null"], "description": "租赁方式名称；详情出参返回，入参可不传" },
        "rentalPeriodMonth": { "type": ["integer", "null"], "description": "租期，单位月" },
        "totalRent": { "type": ["integer", "null"], "format": "int64", "minimum": 0, "description": "总租金，金额按元*10000存储" },
        "monthlyRent": { "type": ["integer", "null"], "format": "int64", "minimum": 0, "description": "月租金，金额按元*10000存储" },
        "dailyRent": { "type": ["integer", "null"], "format": "int64", "minimum": 0, "description": "日租金，金额按元*10000存储" },
        "buyoutAmount": { "type": ["integer", "null"], "format": "int64", "minimum": 0, "description": "到期购买金/买断金，金额按元*10000存储" },
        "premium": { "type": ["integer", "null"], "format": "int64", "minimum": 0, "description": "溢价金，金额按元*10000存储" },
        "stock": { "type": ["integer", "null"], "minimum": 0, "description": "库存" }
      }
    },
    "MarketingProductSku": {
      "type": "object",
      "additionalProperties": true,
      "properties": {
        "id": { "type": ["integer", "null"], "format": "int64", "description": "SKU ID；新增时为空，编辑/详情时返回或传入" },
        "officialPrice": { "type": ["integer", "null"], "format": "int64", "description": "官网价/采购价" },
        "totalPriceFactor": { "type": ["number", "null"], "description": "商品总价系数" },
        "totalRentFactor": { "type": ["number", "null"], "description": "总租金系数" },
        "totalPrice": { "type": ["integer", "null"], "format": "int64", "description": "商品总价" },
        "totalRent": { "type": ["integer", "null"], "format": "int64", "description": "旧 SKU 总租金字段，本期保留" },
        "buyoutAmount": { "type": ["integer", "null"], "format": "int64", "description": "旧 SKU 买断金额字段，本期保留" },
        "dailyRent": { "type": ["integer", "null"], "format": "int64", "description": "旧 SKU 日租金字段，本期保留" },
        "stock": { "type": ["integer", "null"], "description": "旧 SKU 库存字段，本期保留" },
        "premium": { "type": ["integer", "null"], "format": "int64", "description": "旧 SKU 溢价金字段，本期保留" },
        "suggestedRetailPrice": { "type": ["integer", "null"], "format": "int64", "description": "建议售价" },
        "strikethroughPrice": { "type": ["integer", "null"], "format": "int64", "description": "划线价" },
        "cashUsageRatio": { "type": ["number", "null"], "description": "现金使用比例" },
        "pointsUsageRatio": { "type": ["number", "null"], "description": "积分使用比例" },
        "pointsCount": { "type": ["integer", "null"], "description": "积分数量" },
        "cashPrice": { "type": ["integer", "null"], "format": "int64", "description": "现金价格" },
        "propertyValues": {
          "type": ["array", "null"],
          "items": { "$ref": "#/$defs/SkuPropertyValue" },
          "description": "SKU 属性"
        },
        "rentalMethodProperties": {
          "type": ["array", "null"],
          "items": { "$ref": "#/$defs/MarketingProductSkuRentalMethodProperty" },
          "description": "SKU 租赁方式租期价格库存配置"
        },
        "skuCode": { "type": ["string", "null"], "description": "内部 SKU 编码，前端通常无需传入" }
      }
    },
    "MarketingProductMutationRequest": {
      "type": "object",
      "additionalProperties": true,
      "properties": {
        "operateType": { "type": ["integer", "null"], "enum": [1, 2, null], "description": "操作类型：1-保存草稿，2-提交" },
        "type": { "type": ["integer", "null"], "enum": [1, 2, 3, 4, 5, null], "description": "商品类型：1-租赁商品，2-售卖商品，3-回收商品，4-实物商品，5-虚拟商品" },
        "standardProductSpuId": { "type": ["integer", "null"], "format": "int64", "description": "标准商品ID" },
        "name": { "type": ["string", "null"], "description": "商品名称" },
        "productCondition": { "type": ["integer", "null"], "enum": [0, 1, null], "description": "商品成色：0-全新，1-非全新" },
        "monitorAttribute": { "type": ["integer", "null"], "enum": [0, 1, null], "description": "商品监控属性：0-监管，1-非监管" },
        "mainPicUrls": { "type": ["array", "null"], "items": { "type": "string" }, "description": "商品主图" },
        "carouselPicUrls": { "type": ["array", "null"], "items": { "type": "string" }, "description": "商详轮播图" },
        "videoUrls": { "type": ["array", "null"], "items": { "type": "string" }, "description": "商品视频" },
        "detailPicUrls": { "type": ["array", "null"], "items": { "type": "string" }, "description": "商详介绍图" },
        "detailTagIds": { "type": ["array", "null"], "items": { "type": "integer", "format": "int64" }, "description": "商详标签ID" },
        "skuTagIds": { "type": ["array", "null"], "items": { "type": "integer", "format": "int64" }, "description": "SKU页标签ID" },
        "spuProperties": { "type": ["array", "null"], "items": { "$ref": "#/$defs/MarketingProductProperty" }, "description": "SPU 属性" },
        "rentalMethods": { "type": ["array", "null"], "items": { "$ref": "#/$defs/MarketingProductRentalMethod" }, "description": "SPU 支持的租赁方式/租期配置" },
        "minBuybackPrice": { "type": ["integer", "null"], "format": "int64", "description": "最低回收价" },
        "maxBuybackPrice": { "type": ["integer", "null"], "format": "int64", "description": "最高回收价" },
        "receivingAddress": { "type": ["string", "null"], "description": "收货地址" },
        "valueAddedIds": { "type": ["array", "null"], "items": { "type": "integer", "format": "int64" }, "description": "增值服务ID" },
        "showPages": { "type": ["array", "null"], "items": { "type": "integer", "enum": [0, 1] }, "description": "展示页面：0-SKU页，1-确认订单页" },
        "isDefaultSelected": { "type": ["integer", "null"], "enum": [0, 1, null], "description": "是否默认勾选：0-否，1-是" },
        "defaultSelectedValueAddedId": { "type": ["integer", "null"], "format": "int64", "description": "默认勾选增值服务ID" },
        "compensationRuleId": { "type": ["integer", "null"], "format": "int64", "description": "补偿规则ID" },
        "shippingWay": { "type": ["integer", "null"], "enum": [1, 2, 3, null], "description": "发货方式：1-快递，2-同城配送，3-门店自提" },
        "shippingTemplateId": { "type": ["integer", "null"], "format": "int64", "description": "快递模版ID" },
        "shippingAreaCodes": { "type": ["array", "null"], "items": { "type": "string" }, "description": "发货地区编码" },
        "shelvingWay": { "type": ["integer", "null"], "enum": [1, 2, 3, null], "description": "上架方式：1-自动上架，2-手动上架，3-预约上架" },
        "shelvingTime": { "type": ["string", "null"], "format": "date-time", "description": "上架时间" },
        "shelvingChannelIds": { "type": ["array", "null"], "items": { "type": "integer", "format": "int64" }, "description": "上架渠道ID" },
        "skus": { "type": ["array", "null"], "items": { "$ref": "#/$defs/MarketingProductSku" }, "description": "SPU 下 SKU 信息" }
      }
    }
  }
}
```

## 1. 平台新增营销商品

- 接口：`POST /product/mk/add`
- 入参：`MarketingProductAddReqVO`
- 出参：`CommonResult<Boolean>`

### Request Schema

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "$ref": "#/$defs/MarketingProductMutationRequest"
}
```

### Response Schema

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "$ref": "#/$defs/CommonResultBoolean"
}
```

### Request 示例

```json
{
  "operateType": 2,
  "type": 1,
  "standardProductSpuId": 1001,
  "name": "示例租赁商品",
  "productCondition": 0,
  "monitorAttribute": 0,
  "mainPicUrls": ["https://example.com/main.jpg"],
  "carouselPicUrls": ["https://example.com/carousel.jpg"],
  "videoUrls": [],
  "detailPicUrls": ["https://example.com/detail.jpg"],
  "detailTagIds": [11, 12],
  "skuTagIds": [21],
  "spuProperties": [
    {
      "propertyId": 2001,
      "sort": 1,
      "isAddPropertyPic": 1,
      "isAddMarketingCorner": 0,
      "isSkuProperty": 1,
      "propertyValues": [
        {
          "productPropertyValueId": 3001,
          "value": "黑色",
          "sort": 1,
          "picUrl": "https://example.com/black.jpg",
          "marketingCornerText": null
        }
      ]
    }
  ],
  "rentalMethods": [
    {
      "rentalMethod": 1,
      "rentalPeriods": [3, 6, 12]
    }
  ],
  "valueAddedIds": [4001],
  "showPages": [0, 1],
  "isDefaultSelected": 1,
  "defaultSelectedValueAddedId": 4001,
  "compensationRuleId": 5001,
  "shippingWay": 1,
  "shippingTemplateId": 6001,
  "shippingAreaCodes": ["110000"],
  "shelvingWay": 1,
  "shelvingTime": "2026-06-01T10:00:00Z",
  "shelvingChannelIds": [7001],
  "skus": [
    {
      "officialPrice": 100000000,
      "totalPriceFactor": 1.0,
      "totalRentFactor": 1.0,
      "totalPrice": 100000000,
      "totalRent": 3000000,
      "buyoutAmount": 10000000,
      "dailyRent": 33300,
      "stock": 10,
      "premium": 50000,
      "suggestedRetailPrice": 120000000,
      "strikethroughPrice": 130000000,
      "cashUsageRatio": 1.0,
      "pointsUsageRatio": 0.0,
      "pointsCount": 0,
      "cashPrice": 100000000,
      "propertyValues": [
        {
          "propertyId": 2001,
          "propertyValueId": 3001,
          "propertyValue": "黑色"
        }
      ],
      "rentalMethodProperties": [
        {
          "rentalMethod": 1,
          "rentalPeriodMonth": 3,
          "totalRent": 3000000,
          "monthlyRent": 1000000,
          "dailyRent": 33300,
          "buyoutAmount": 10000000,
          "premium": 50000,
          "stock": 10
        },
        {
          "rentalMethod": 1,
          "rentalPeriodMonth": 6,
          "totalRent": 5400000,
          "monthlyRent": 900000,
          "dailyRent": 30000,
          "buyoutAmount": 9000000,
          "premium": 30000,
          "stock": 20
        }
      ]
    }
  ]
}
```

## 2. 平台编辑营销商品

- 接口：`POST /product/mk/mod`
- 入参：`MarketingProductModReqVO`，在新增入参基础上额外增加 `id`
- 出参：`CommonResult<Boolean>`

### Request Schema

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "allOf": [
    { "$ref": "#/$defs/MarketingProductMutationRequest" },
    {
      "type": "object",
      "additionalProperties": true,
      "properties": {
        "id": { "type": "integer", "format": "int64", "description": "营销商品ID" }
      },
      "required": ["id"]
    }
  ]
}
```

### Response Schema

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "$ref": "#/$defs/CommonResultBoolean"
}
```

### Request 示例

```json
{
  "id": 10001,
  "operateType": 2,
  "type": 1,
  "standardProductSpuId": 1001,
  "name": "示例租赁商品-编辑后",
  "productCondition": 0,
  "monitorAttribute": 0,
  "mainPicUrls": ["https://example.com/main.jpg"],
  "carouselPicUrls": ["https://example.com/carousel.jpg"],
  "videoUrls": [],
  "detailPicUrls": ["https://example.com/detail.jpg"],
  "spuProperties": [],
  "rentalMethods": [
    {
      "rentalMethod": 1,
      "rentalPeriods": [3, 6]
    }
  ],
  "skus": [
    {
      "id": 90001,
      "officialPrice": 100000000,
      "propertyValues": [],
      "rentalMethodProperties": [
        {
          "rentalMethod": 1,
          "rentalPeriodMonth": 3,
          "totalRent": 3300000,
          "monthlyRent": 1100000,
          "dailyRent": 36600,
          "buyoutAmount": 10000000,
          "premium": 50000,
          "stock": 15
        }
      ]
    }
  ]
}
```

## 3. 平台营销商品详情查询

- 接口：`POST /product/mk/detail`
- 入参：`LongIdReqVO`
- 出参：`CommonResult<MarketingProductDetailRespVO>`

### Request Schema

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "$ref": "#/$defs/LongIdRequest"
}
```

### Response Schema

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "additionalProperties": true,
  "properties": {
    "code": { "type": "integer", "description": "业务状态码" },
    "msg": { "type": ["string", "null"], "description": "业务提示" },
    "data": {
      "type": ["object", "null"],
      "additionalProperties": true,
      "properties": {
        "id": { "type": ["integer", "null"], "format": "int64", "description": "商品ID" },
        "code": { "type": ["string", "null"], "description": "商品编码" },
        "type": { "type": ["integer", "null"], "enum": [1, 2, 3, 4, 5, null], "description": "商品类型" },
        "name": { "type": ["string", "null"], "description": "营销商品名称" },
        "productCondition": { "type": ["integer", "null"], "enum": [0, 1, null], "description": "商品成色" },
        "monitorAttribute": { "type": ["integer", "null"], "enum": [0, 1, null], "description": "商品监控属性" },
        "standardProductSpuId": { "type": ["integer", "null"], "format": "int64" },
        "standardProductSpuCode": { "type": ["string", "null"] },
        "standardProductSpuName": { "type": ["string", "null"] },
        "businessCategoryName": { "type": ["string", "null"] },
        "marketingCategoryName": { "type": ["string", "null"] },
        "brandName": { "type": ["string", "null"] },
        "mainPicUrls": { "type": ["array", "null"], "items": { "type": "string" } },
        "carouselPicUrls": { "type": ["array", "null"], "items": { "type": "string" } },
        "videoUrls": { "type": ["array", "null"], "items": { "type": "string" } },
        "detailPicUrls": { "type": ["array", "null"], "items": { "type": "string" } },
        "detailTags": { "type": ["array", "null"], "items": { "$ref": "#/$defs/IdAndName" } },
        "skuTags": { "type": ["array", "null"], "items": { "$ref": "#/$defs/IdAndName" } },
        "spuProperties": { "type": ["array", "null"], "items": { "$ref": "#/$defs/MarketingProductProperty" } },
        "rentalMethods": { "type": ["array", "null"], "items": { "$ref": "#/$defs/MarketingProductRentalMethod" }, "description": "SPU 支持的租赁方式/租期配置" },
        "minBuybackPrice": { "type": ["integer", "null"], "format": "int64" },
        "maxBuybackPrice": { "type": ["integer", "null"], "format": "int64" },
        "valueAddedList": { "type": ["array", "null"], "items": { "$ref": "#/$defs/IdAndName" } },
        "showPages": { "type": ["array", "null"], "items": { "type": "integer", "enum": [0, 1] } },
        "isDefaultSelected": { "type": ["integer", "null"], "enum": [0, 1, null] },
        "defaultSelectedValueAddedId": { "type": ["integer", "null"], "format": "int64" },
        "defaultSelectedValueAddedName": { "type": ["string", "null"] },
        "compensationRuleId": { "type": ["integer", "null"], "format": "int64" },
        "shippingWay": { "type": ["integer", "null"], "enum": [1, 2, 3, null] },
        "shippingTemplateId": { "type": ["integer", "null"], "format": "int64" },
        "shippingTemplateName": { "type": ["string", "null"] },
        "shippingAreaCodes": { "type": ["array", "null"], "items": { "$ref": "#/$defs/AreaCodeAndName" } },
        "receivingAddress": { "type": ["string", "null"] },
        "shelvingWay": { "type": ["integer", "null"], "enum": [1, 2, 3, null] },
        "shelvingTime": { "type": ["string", "null"], "format": "date-time" },
        "shelvingChannels": { "type": ["array", "null"], "items": { "$ref": "#/$defs/IdAndName" } },
        "skus": { "type": ["array", "null"], "items": { "$ref": "#/$defs/MarketingProductSku" }, "description": "SPU 下 SKU 信息，包含 rentalMethodProperties" },
        "approveStatus": { "type": ["integer", "null"], "enum": [0, 1, 2, null] },
        "shelvesStatus": { "type": ["integer", "null"], "enum": [0, 1, 2, null] },
        "isDraft": { "type": ["integer", "null"], "enum": [0, 1, null] }
      }
    }
  },
  "required": ["code", "data"]
}
```

### Response 片段示例

```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "id": 10001,
    "type": 1,
    "name": "示例租赁商品",
    "rentalMethods": [
      {
        "rentalMethod": 1,
        "rentalMethodName": "租完归还",
        "rentalPeriods": [3, 6, 12]
      }
    ],
    "skus": [
      {
        "id": 90001,
        "propertyValues": [],
        "rentalMethodProperties": [
          {
            "rentalMethod": 1,
            "rentalMethodName": "租完归还",
            "rentalPeriodMonth": 3,
            "totalRent": 3000000,
            "monthlyRent": 1000000,
            "dailyRent": 33300,
            "buyoutAmount": 10000000,
            "premium": 50000,
            "stock": 10
          }
        ]
      }
    ]
  }
}
```

## 4. APP SPU 详情查询

- 接口：`POST /app/product/spu/detail`
- 入参：`AppProductSpuDetailReqVO`
- 出参：`CommonResult<AppProductSpuDetailRespVO>`

### Request Schema

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "additionalProperties": false,
  "properties": {
    "spuId": { "type": "integer", "format": "int64", "description": "SPU ID" }
  },
  "required": ["spuId"]
}
```

### Response Schema

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "additionalProperties": true,
  "properties": {
    "code": { "type": "integer", "description": "业务状态码" },
    "msg": { "type": ["string", "null"], "description": "业务提示" },
    "data": {
      "type": ["object", "null"],
      "additionalProperties": true,
      "properties": {
        "spuId": { "type": ["integer", "null"], "format": "int64" },
        "name": { "type": ["string", "null"] },
        "brand": { "type": ["string", "null"] },
        "model": { "type": ["string", "null"] },
        "condition": { "type": ["integer", "null"], "enum": [0, 1, null] },
        "mainPicUrls": { "type": ["array", "null"], "items": { "type": "string" } },
        "carouselPicUrls": { "type": ["array", "null"], "items": { "type": "string" } },
        "videoUrls": { "type": ["array", "null"], "items": { "type": "string" } },
        "detailPicUrls": { "type": ["array", "null"], "items": { "type": "string" } },
        "defaultSkuId": { "type": ["integer", "null"], "format": "int64" },
        "dailyRent": { "type": ["integer", "null"], "format": "int64", "description": "旧顶层日租金字段，兼容保留" },
        "officialPrice": { "type": ["integer", "null"], "format": "int64" },
        "strikethroughPrice": { "type": ["integer", "null"], "format": "int64" },
        "properties": {
          "type": ["array", "null"],
          "items": {
            "type": "object",
            "additionalProperties": true,
            "properties": {
              "propertyId": { "type": ["integer", "null"], "format": "int64" },
              "propertyName": { "type": ["string", "null"] },
              "propertyValue": { "type": ["string", "null"] },
              "picUrl": { "type": ["string", "null"] },
              "marketingCornerText": { "type": ["string", "null"] }
            }
          }
        },
        "rentalMethods": {
          "type": ["array", "null"],
          "items": {
            "type": "object",
            "additionalProperties": true,
            "properties": {
              "rentalMethod": { "type": ["integer", "null"], "enum": [1, 2, null] },
              "rentalMethodName": { "type": ["string", "null"] },
              "rentalPeriods": { "type": ["array", "null"], "items": { "type": "integer" } }
            }
          },
          "description": "SPU 支持的租赁方式/租期配置，保持原结构"
        },
        "skuRentalMethodProperties": {
          "type": ["array", "null"],
          "items": {
            "type": "object",
            "additionalProperties": true,
            "properties": {
              "skuId": { "type": ["integer", "null"], "format": "int64", "description": "SKU ID" },
              "rentalMethod": { "type": ["integer", "null"], "enum": [1, 2, null], "description": "租赁方式：1-租完归还，2-灵活租" },
              "rentalMethodName": { "type": ["string", "null"], "description": "租赁方式名称" },
              "rentalPeriodMonth": { "type": ["integer", "null"], "description": "租期，单位月" },
              "totalRent": { "type": ["integer", "null"], "format": "int64", "description": "总租金，金额按元*10000存储" },
              "monthlyRent": { "type": ["integer", "null"], "format": "int64", "description": "月租金，金额按元*10000存储" },
              "dailyRent": { "type": ["integer", "null"], "format": "int64", "description": "日租金，金额按元*10000存储" },
              "buyoutAmount": { "type": ["integer", "null"], "format": "int64", "description": "到期购买金/买断金，金额按元*10000存储" },
              "premium": { "type": ["integer", "null"], "format": "int64", "description": "溢价金，金额按元*10000存储" },
              "stock": { "type": ["integer", "null"], "description": "库存" }
            }
          },
          "description": "SKU 级租赁方式租期价格库存配置"
        },
        "valueAddedList": {
          "type": ["array", "null"],
          "items": {
            "type": "object",
            "additionalProperties": true,
            "properties": {
              "id": { "type": ["integer", "null"], "format": "int64" },
              "name": { "type": ["string", "null"] },
              "price": { "type": ["integer", "null"], "format": "int64" },
              "isDefault": { "type": ["integer", "null"], "enum": [0, 1, null] },
              "serviceTypes": { "type": ["string", "null"] },
              "effectiveChannels": { "type": ["string", "null"] },
              "compensationStandard": { "type": ["integer", "null"] },
              "compensationLevel": { "type": ["integer", "null"] },
              "compensationLevelLimits": { "type": ["string", "null"] },
              "slightCompensationRatio": { "type": ["integer", "null"] },
              "mediumCompensationRatio": { "type": ["integer", "null"] },
              "severeCompensationRatio": { "type": ["integer", "null"] },
              "scrapCompensationRatio": { "type": ["integer", "null"] },
              "compensationAmount": { "type": ["integer", "null"], "format": "int64" },
              "compensationAmountRatio": { "type": ["integer", "null"] },
              "compensationAmountRules": {
                "type": ["array", "null"],
                "items": {
                  "type": "object",
                  "additionalProperties": true,
                  "properties": {
                    "id": { "type": ["integer", "null"], "format": "int64" },
                    "compensationAmount": { "type": ["integer", "null"], "format": "int64" },
                    "compensationAmountRatio": { "type": ["integer", "null"] },
                    "sortOrder": { "type": ["integer", "null"] }
                  }
                }
              },
              "saleLimits": { "type": ["string", "null"] },
              "annualLimitPurchaseCount": { "type": ["integer", "null"] },
              "monthlyLimitPurchaseCount": { "type": ["integer", "null"] },
              "dailyLimitPurchaseCount": { "type": ["integer", "null"] },
              "accessCondition": { "type": ["integer", "null"] },
              "accessConditionLimits": { "type": ["string", "null"] },
              "accessConditionBreachAmount": { "type": ["integer", "null"], "format": "int64" },
              "accessConditionBreachCount": { "type": ["integer", "null"] }
            }
          }
        }
      }
    }
  },
  "required": ["code", "data"]
}
```

### Response 片段示例

```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "spuId": 10001,
    "name": "示例租赁商品",
    "defaultSkuId": 90001,
    "dailyRent": 33300,
    "rentalMethods": [
      {
        "rentalMethod": 1,
        "rentalMethodName": "租完归还",
        "rentalPeriods": [3, 6, 12]
      }
    ],
    "skuRentalMethodProperties": [
      {
        "skuId": 90001,
        "rentalMethod": 1,
        "rentalMethodName": "租完归还",
        "rentalPeriodMonth": 3,
        "totalRent": 3000000,
        "monthlyRent": 1000000,
        "dailyRent": 33300,
        "buyoutAmount": 10000000,
        "premium": 50000,
        "stock": 10
      }
    ]
  }
}
```
