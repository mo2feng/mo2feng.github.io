# Activiti中的数据库设计和模型映射

<show-structure for="chapter,procedure" depth="2"/>


## 通用数据表
通用数据表指Activiti中以ACT_GE_ 开头的表，用于存放流程和业务的通用资源数据

### 1. 资源表 （ACT_GE_BYTEARRAY）
用于存放流程或业务的通用资源数据。

|字段|类型|说明|
|:---|:---|:---|
|ID_|varchar(64)|资源ID（主键）|
|REV_|int|版本(用于乐观锁)|
|NAME_|varchar(255)|资源名称|
|DEPLOYMENT_ID_|varchar(64)|部署ID，与部署表ACT_RE_DEPLOYMENT的主键关联|
|BYTES_|longblob|资源内容，最大存储4GB|
|GENERATED_|tinyint|是否是Activiti自动生成的资源|


### 2. 属性表 （ACT_GE_PROPERTY）

用于存储整个工作流引擎级别的属性数据。

|字段|类型|说明|
|:---|:---|:---|
|NAME_|varchar(64)|属性名称|
|VALUE_|varchar(300)|属性值|
|REV_|int|版本(用于乐观锁)|


## 流程存储表
通用数据表指Activiti中以ACT_RE_ 开头的表，用于存放流程定义和部署信息等。

### 1.ACT_RE_MODEL表

|字段|类型|说明|
|:---|:---|:---|
|ID_|varchar(64)|模型ID|
|REV_|int|版本(用于乐观锁)|
|NAME_|varchar(255)|模型名称|
|KEY_|varchar(255)|模型key|
|CATEGORY_|varchar(255)|模型分类|
|CREATE_TIME_|timestamp|创建时间|
|LAST_UPDATE_TIME_|timestamp|最后更新时间|
|VERSION_|int|模型版本|
|META_INFO_|varchar(4000)|元数据信息|
|DEPLOYMENT_ID_|varchar(64)|部署ID，与部署表ACT_RE_DEPLOYMENT的主键关联|
|EDITOR_SOURCE_VALUE_ID_|varchar(64)|提供给用户存储私有定义图片，对应ACT_GE_BYTEARRAY的主键ID_，表示该模型生成的模型定义文件（JSON格式数据）|
|EDITOR_SOURCE_EXTRA_VALUE_ID_|varchar(64)|提供给用户存储私有定义图片，对应ACT_GE_BYTEARRAY的主键ID_，表示该模型生成的图片文件|
|TENANT_ID_|varchar(255)|租户ID|


## 身份数据表


## 运行时数据表


## 历史数据表