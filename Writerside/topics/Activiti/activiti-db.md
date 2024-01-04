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

主要用于存储流程的设计模型

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

### 2. ACT_RE_DEPLOYMENT表
部署信息表，主要用于存储流程定义的部署信息。Activiti中一次部署可以添加多个资源，资源保存在ACT_GE_BYTEARRAY表中,部署信息保存在这个表中。

|字段|类型|说明|
|:---|:---|:---|
|ID_|varchar(64)|部署记录ID（主键）|
|NAME_|varchar(255)|部署名称|
|CATEGORY_|varchar(255)|部署分类|
|KEY_|varchar(255)|流程模型标识|
|TENANT_ID_|varchar(255)|租户ID|
|DEPLOY_TIME_|timestamp|部署时间|
|ENGINE_VERSION_|varchar(255)|引擎版本|


### 3 ACT_RE_PROCDEF表
流程定义数据表，该表主要用于存储流程定义信息。部署流程是，除了将流程定义文件存储到资源表之外，还会解析流程定义文件内容，生产流程定义保存在改表中。

|字段|类型|说明|
|:---|:---|:---|
|ID_|varchar(64)|流程定义ID（主键）|
|REV_|int|版本(用于乐观锁)|
|CATEGORY_|varchar(255)|流程定义分类|
|NAME_|varchar(255)|流程定义名称|
|KEY_|varchar(255)|流程定义key|
|VERSION_|int|流程定义版本|
|DEPLOYMENT_ID_|varchar(64)|部署ID，与部署表ACT_RE_DEPLOYMENT的主键关联|
|RESOURCE_NAME_|varchar(4000)|资源名称|
|DGRM_RESOURCE_NAME_|varchar(4000)|流程定义对应的流程图的资源名称|
|DESCRIPTION_|varchar(4000)|流程定义描述|
|HAS_START_FORM_KEY_|boolean|是否有开始表单标识|
|HAS_GRAPHICAL_NOTATION_|boolean|是否有图形信息|
|SUSPENSION_STATE_|int|流程定义状态的挂起状态|
|TENANT_ID_|varchar(255)|租户ID|
|ENGINE_VERSION_|varchar(255)|引擎版本|


## 身份数据表

身份数据表是以ACT_ID_开头，主要用于存储用户、组、用户组之间的关系。

### 1. ACT_ID_USER表
用户表，该表主要用于存储用户信息。

|字段|类型|说明|
|:---|:---|:---|
|ID_|varchar(64)|用户ID（主键）|
|REV_|int|版本(用于乐观锁)|
|FIRST_|varchar(255)|用户名|
|LAST_|varchar(255)|用户姓|
|EMAIL_|varchar(255)|用户邮箱|
|PWD_|varchar(255)|用户密码|
|PICTURE_ID_|varchar(64)|用户头像ID,对应ACT_GE_BYTEARRAY资源表中的字段ID_|


### 2. ACT_ID_INFO表
用户账号信息表，该表主要用于存储用户账号信息。ACTIVITI 将信息分为用户、用户账号和用户信息3种信息类型。

|字段|类型|说明|
|:---|:---|:---|
|ID_|varchar(64)|账号ID（主键）|
|REV_|int|版本(用于乐观锁)|
|USER_ID_|varchar(64)|用户ID，与ACT_ID_USER表的主键ID_关联|
|TYPE_|varchar(64)|账号类型，可以设置用户账号(account)，用户信息(userinfo) 和(null)三种值|
|KEY_|varchar(255)|账号键值KEY|
|VALUE_|varchar(255)|用户信息的Value|
|PASSWORD_|varchar(255)|账号密码|
|PARENT_ID_|varchar(255)|父账号ID|


### 3. ACT_ID_GROUP表
用户组表，该表主要用于存储用户组信息。

|字段|类型|说明|
|:---|:---|:---|
|ID_|varchar(64)|用户组ID（主键）|
|REV_|int|版本(用于乐观锁)|
|NAME_|varchar(255)|用户组名称|
|TYPE_|varchar(255)|用户组类型|


### 4. ACT_ID_MEMBERSHIP表
用户组成员表，该表主要用于存储用户组和用户之间的关系。

|字段|类型|说明|
|:---|:---|:---|
|USER_ID_|varchar(64)|用户ID，与ACT_ID_USER表的主键ID_关联|
|GROUP_ID_|varchar(64)|用户组ID，与ACT_ID_GROUP表的主键ID_关联|

## 运行时数据表

运行时数据表是以ACT_RU_开头，主要用于存储流程实例、任务、变量、异步任务、定时任务等运行时数据。


### 1. ACT_RU_EXECUTION表
运行时流程执行实例表，该表主要用于存储运行时的执行实例。流程启动时，会生成一个流程实例，以及相应的执行实例。流程实例和执行实例都存储在ACT_RU_EXECUTION表中。如果流程实例只生成一个执行实例，就只存储一条数据，这条数据既是流程实例也是执行实例。

|字段|类型|说明|
|:---|:---|:---|
|ID_|varchar(64)|执行实例ID（主键）|
|REV_|int|版本(用于乐观锁)|
|PROC_INST_ID_|varchar(64)|流程实例ID，与ACT_RU_EXECUTION表的主键ID_关联|
|BUSINESS_KEY_|varchar(255)|业务主键|
|PARENT_ID_|varchar(64)|父执行实例ID，与ACT_RU_EXECUTION表的主键ID_关联|
|PROC_DEF_ID_|varchar(64)|流程定义ID，与ACT_RE_PROCDEF表的主键ID_关联|
|SUPER_EXEC_|varchar(64)|父执行实例ID，与ACT_RU_EXECUTION表的主键ID|
|ROOT_PROC_INST_ID_|varchar(64)|根流程实例ID，与ACT_RU_EXECUTION表的主键ID_关联|
|ACT_ID_|varchar(255)|当前活动ID，与ACT_RE_DEPLOYMENT表的主键ID_关联|
|IS_ACTIVE_|TINYINT|是否为活跃的执行实例|
|IS_CONCURRENT_|TINYINT|是否为并行的执行实例|
|IS_SCOPE_|TINYINT|是否为父作用域|
|IS_EVENT_SCOPE_|TINYINT|是否为事件作用域|
|IS_MI_ROOT_|TINYINT|是否多实例根执行流|
|SUSPENSION_STATE_|int|挂起状态|
|CACHED_ENT_STATE_|int|缓存状态|
|TENANT_ID_|varchar(255)|租户ID|
|NAME_|varchar(255)|执行实例名称|
|START_TIME_|datetime|开始时间|
|START_USER_ID_|varchar(255)|实例启动用户|
|LOCK_TIME_|datetime|锁定时间|
|IS_COUNT_ENABLED_|TINYINT|是否开启计数|
|EVT_SUBSCR_COUNT_|int|事件订阅数量|
|TASK_COUNT_|int|任务数量|
|JOB_COUNT_|int|定时任务数量|
|TIMER_JOB_COUNT_|int|定时任务数量|
|SUSP_JOB_COUNT_|int|挂起任务数量|
|DEADLET_JOB_COUNT_|int|过期任务数量|
|VAR_COUNT_|int|流程变量数量|
|ID_LINK_COUNT_|int|ID链接数量|



### 2.ACT_RU_TASK表
运行时任务节点表，存储流程实例的运行时任务节点信息。

|字段|类型|说明|
|:---|:---|:---|
|ID_|varchar(64)|任务ID（主键）|
|REV_|int(11)|版本(用于乐观锁)|
|EXECUTION_ID_|varchar(64)|执行实例ID，与ACT_RU_EXECUTION表的主键|
|PROC_INST_ID_|varchar(64)|流程实例ID|
|PROC_DEF_ID_|varchar(64)|流程定义ID|
|NAME_|varchar(255)|任务实例名称|
|PARENT_TASK_ID_|varchar(64)|父任务实例ID|
|DESCRIPTION_|varchar(4000)|节点描述|
|TASK_DEF_KEY_|varchar(255)|节点标识|
|OWNER_|varchar(255)|拥有者|
|ASSIGNEE_|varchar(255)|审批人|
|DELEGATION_|varchar(64)|委托类型|
|PRIORITY_|int(11)|优先级|
|CREATE_TIME_|timestamp|创建时间|
|DUE_DATE_|datetime|过期时间|
|CATEGORY_|varchar(255)|分类|
|SUSPENSION_STATE_|int(11)|挂起状态|
|TENANT_ID_|varchar(255)|租户ID|
|FORM_KEY_|varchar(255)|表单模型KEY|
|CLAIM_TIME_|datetime(3)|认领时间|


### 3.ACT_RU_VARIABLE表
运行时流程变量表，存储流程实例的运行时变量信息,包括流程实例变量、执行实例变量和任务实例变量。

|字段|类型|说明|
|:---|:---|:---|
|ID_|varchar(64)|变量ID（主键）|
|REV_|int|版本(用于乐观锁)|
|TYPE_|varchar(255)|变量类型|
|NAME_|varchar(255)|变量名称|
|EXECUTION_ID_|varchar(64)|执行实例ID|
|PROC_INST_ID_|varchar(64)|流程实例ID|
|TASK_ID_|varchar(64)|任务实例ID|
|BYTEARRAY_ID_|varchar(64)|复杂变量值存储在资源表中，这里存储了关联的资源ID|
|DOUBLE_|double|存储小数类型的变量值|
|LONG_|bigint|存储整型的变量值|
|TEXT_|varchar(4000)|存储字符串类型的变量值|
|TEXT2_|varchar(4000)|存储的是JPA持久化对象是，才有值。值为对象ID|


### 4.ACT_RU_IDENTITY表
运行时流程和身份关系表,主要用于存储运行时流程实例、任务实例和用户的关系信息。

|字段|类型|说明|
|:---|:---|:---|
|ID_|varchar(64)|主键|
|REV_|int|版本(用于乐观锁)|
|GROUP_ID_|varchar(255)|用户组ID|
|TYPE_|varchar(255)|关系类型：assignee,candidate等|
|USER_ID_|varchar(255)|用户ID|
|TASK_ID_|varchar(64)|任务实例ID|
|PROC_INST_ID_|varchar(64)|流程实例ID|
|PROC_DEF_ID_|varchar(64)|流程定义ID|


### 5.ACT_RU_JOB表
运行时作业表，存储正在执行的作业的数据。

|字段|类型|说明|
|:---|:---|:---|
|ID_|varchar(64)|主键|
|REV_|int|版本(用于乐观锁)|
|TYPE_|varchar(255)|任务类型|
|LOCK_EXP_TIME_|timestamp|锁过期时间|
|LOCK_OWNER_|varchar(255)|锁拥有者|
|EXCLUSIVE_|boolean|是否独占|
|EXECUTION_ID_|varchar(64)|执行实例ID|
|PROCESS_INSTANCE_ID_|varchar(64)|流程实例ID|
|PROC_DEF_ID_|varchar(64)|流程定义ID|
|RETRIES_|int|重试次数|
|EXCEPTION_STACK_ID_|varchar(64)|异常堆栈ID|
|EXCEPTION_MSG_|varchar(4000)|异常信息|
|DUEDATE_|timestamp|定时任务执行截止时间|
|REPEAT_|varchar(255)|重复执行信息，如重复次数等|
|HANDLER_TYPE_|varchar(255)|任务处理器类型|
|HANDLER_CFG_|varchar(4000)|任务处理器配置|
|TENANT_ID_|varchar(255)|租户ID|


### 6.ACT_RU_DEADLETTER_JOB表
运行时无法执行的作业表，用于存储Activiti无法执行的作业数据。

|字段|类型|说明|
|:---|:---|:---|
|ID_|varchar(64)|主键|
|REV_|int|版本(用于乐观锁)|
|TYPE_|varchar(255)|任务类型|
|EXCLUSIVE_|boolean|是否独占|
|EXECUTION_ID_|varchar(64)|执行实例ID|
|PROCESS_INSTANCE_ID_|varchar(64)|流程实例ID|
|PROC_DEF_ID_|varchar(64)|流程定义ID|
|EXCEPTION_STACK_ID_|varchar(64)|异常堆栈ID|
|EXCEPTION_MSG_|varchar(4000)|异常信息|
|DUEDATE_|timestamp|定时任务执行截止时间|
|REPEAT_|varchar(255)|重复执行信息，如重复次数等|
|HANDLER_TYPE_|varchar(255)|任务处理器类型|
|HANDLER_CFG_|varchar(4000)|任务处理器配置|
|TENANT_ID_|varchar(255)|租户ID|


### 7.ACT_RU_SUSPENDED_JOB表
运行时挂起的作业表，用于存储Activiti挂起的作业数据。

|字段|类型|说明|
|:---|:---|:---|
|ID_|varchar(64)|挂起的定时任务ID(主键)|
|REV_|int|版本(用于乐观锁)|
|TYPE_|varchar(255)|任务类型|
|EXCLUSIVE_|boolean|是否独占|
|EXECUTION_ID_|varchar(64)|执行实例ID|
|PROCESS_INSTANCE_ID_|varchar(64)|流程实例ID|
|PROC_DEF_ID_|varchar(64)|流程定义ID|
|RETRIES_|int|重试次数|
|EXCEPTION_STACK_ID_|varchar(64)|异常堆栈ID|
|EXCEPTION_MSG_|varchar(4000)|异常信息|
|DUEDATE_|timestamp|定时任务执行截止时间|
|REPEAT_|varchar(255)|重复执行信息，如重复次数等|
|HANDLER_TYPE_|varchar(255)|任务处理器类型|
|HANDLER_CFG_|varchar(4000)|任务处理器配置|
|TENANT_ID_|varchar(255)|租户ID|


### 8.ACT_RU_TIMER_JOB表
运行时定时器作业表，用于存储流程运行时的定时任务数据。

流程执行到中间定时器事件节点或带有边界定时器事件的节点时，会生成一个定时任务，并将数据存储到该表中。

|字段|类型|说明|
|:---|:---|:---|
|ID_|varchar(64)|定时任务ID(主键)|
|REV_|int|版本(用于乐观锁)|
|TYPE_|varchar(255)|任务类型|
|LOCK_EXP_TIME|timestamp|锁定释放事件|
|LOCK_OWNER_|varchar(255)|锁定拥有者|
|EXCLUSIVE_|boolean|是否独占|
|EXECUTION_ID_|varchar(64)|执行实例ID|
|PROCESS_INSTANCE_ID_|varchar(64)|流程实例ID|
|PROC_DEF_ID_|varchar(64)|流程定义ID|
|RETRIES_|int|重试次数|
|EXCEPTION_STACK_ID_|varchar(64)|异常堆栈ID|
|EXCEPTION_MSG_|varchar(4000)|异常信息|
|DUEDATE_|timestamp|定时任务执行截止时间|
|REPEAT_|varchar(255)|重复执行信息，如重复次数等|
|HANDLER_TYPE_|varchar(255)|任务处理器类型|
|HANDLER_CFG_|varchar(4000)|任务处理器配置|
|TENANT_ID_|varchar(255)|租户ID|


### 9.ACT_RU_EVENT_SUBSCR表
运行时事件订阅表，用于存储流程运行时的事件订阅。

流程执行到事件节点时，会在该表插入事件订阅，这些事件订阅决定事件的触发。

|字段|类型|说明|
|:---|:---|:---|
|ID_|varchar(64)|事件订阅ID(主键)|
|REV_|int|版本(用于乐观锁)|
|EVENT_TYPE_|varchar(255)|事件类型|
|EVENT_NAME_|varchar(255)|事件名称|
|EXECUTION_ID_|varchar(64)|执行实例ID|
|PROC_INST_ID_|varchar(64)|流程实例ID|
|ACTIVITY_ID_|varchar(64)|具体事件ID|
|CONFIGURATION_|varchar(255)|配置属性|
|CREATED_|timestamp|创建时间|
|PROC_DEF_ID_|varchar(64)|流程定义ID|
|TENANT_ID_|varchar(255)|租户ID|





## 历史数据表
历史数据表是以ACT_HI_开头的,用于存储历史流程实例、变量和任务等历史记录。


### 1.ACT_HI_PROCINST表
历史流程实例表，用于存储流程实例的历史数据。流程启动后，保存ACT_RU_EXECUTION表的同时，会将流程实例写入ACT_HI_PROCINST表。

|字段|类型|说明|
|:---|:---|:---|
|ID_|varchar(64)|流程实例ID(主键)|
|PROC_INST_ID_|varchar(64)|流程实例ID，与ID_值相同|
|BUSINESS_KEY_|varchar(255)|业务主键|
|PROC_DEF_ID_|varchar(64)|流程定义ID|
|START_TIME_|timestamp|开始时间|
|END_TIME_|timestamp|结束时间|
|DURATION_|bigint|持续时间(毫秒)|
|START_USER_ID_|varchar(255)|启动用户ID|
|START_ACT_ID_|varchar(255)|启动节点ID|
|END_ACT_ID_|varchar(255)|结束节点ID|
|SUPER_PROCESS_INSTANCE_ID_|varchar(64)|父流程实例ID|
|DELETE_REASON_|varchar(4000)|删除原因|
|TENANT_ID_|varchar(255)|租户ID|
|NAME_|varchar(255)|流程实例名称|



### 2.ACT_HI_ACTINST表
历史节点实例表，用于存储流程实例的节点历史数据。通过这个表，可以追踪最完整的流程信息。

|字段|类型|说明|
|:---|:---|:---|
|ID_|varchar(64)|节点实例ID(主键)|
|PROC_DEF_ID_|varchar(64)|流程定义ID|
|PROC_INST_ID_|varchar(64)|流程实例ID|
|EXECUTION_ID_|varchar(64)|执行实例ID|
|ACT_ID_|varchar(255)|节点ID|
|TASK_ID_|varchar(64)|任务ID|
|CALL_PROC_INST_ID_|varchar(64)|调用流程实例ID|
|ACT_NAME_|varchar(255)|节点名称|
|ACT_TYPE_|varchar(255)|节点类型|
|ASSIGNEE_|varchar(255)|任务办理人|
|START_TIME_|timestamp|开始时间|
|END_TIME_|timestamp|结束时间|
|DURATION_|bigint|持续时间(毫秒)|
|DELETE_REASON_|varchar(4000)|删除原因|
|TENANT_ID_|varchar(255)|租户ID|

### 3.ACT_HI_TASKINST表
历史任务实例表，用于存储流程实例的任务历史数据。当流程执行到某个节点时，会向该表中写入历史任务数据。

|字段|类型|说明|
|:---|:---|:---|
|ID_|varchar(64)|任务实例ID(主键)|
|PROC_DEF_ID_|varchar(64)|流程定义ID|
|TASK_DEF_KEY_|varchar(255)|任务定义KEY|
|PROC_INST_ID_|varchar(64)|流程实例ID|
|EXECUTION_ID_|varchar(64)|执行实例ID|
|NAME_|varchar(255)|任务名称|
|PARENT_TASK_ID_|varchar(64)|父任务ID|
|DESCRIPTION_|varchar(4000)|任务描述|
|OWNER_|varchar(255)|任务拥有者|
|ASSIGNEE_|varchar(255)|任务办理人|
|START_TIME_|timestamp|开始时间|
|CLAIM_TIME_|timestamp|认领时间|
|END_TIME_|timestamp|结束时间|
|DURATION_|bigint|持续时间(毫秒)|
|DELETE_REASON_|varchar(4000)|删除原因|
|PRIORITY_|int|任务优先级|
|DUE_DATE_|timestamp|任务截止日期|
|FORM_KEY_|varchar(255)|任务表单KEY|
|CATEGORY_|varchar(255)|任务分类|
|TENANT_ID_|varchar(255)|租户ID|

### 4.ACT_HI_DETAIL表
历史详情表，用于存储流程实例的变量历史数据。默认情况下，Activiti不保存流程明细数据，除非将历史数据配置为full。

|字段|类型|说明|
|:---|:---|:---|
|ID_|varchar(64)|主键|
|TYPE_|varchar(255)|变量类型|
|PROC_INST_ID_|varchar(64)|流程实例ID|
|EXECUTION_ID_|varchar(64)|执行实例ID|
|TASK_ID_|varchar(64)|任务实例ID|
|ACT_INST_ID_|varchar(64)|活动实例ID|
|NAME_|varchar(255)|变量名称|
|VAR_TYPE_|varchar(100)|变量类型|
|REV_|int|版本号|
|TIME_|timestamp|时间戳|
|BYTEARRAY_ID_|varchar(64)|字节数组ID|
|DOUBLE_|double|双精度浮点数|
|LONG_|bigint|长整型|
|TEXT_|varchar(4000)|文本类型|
|TEXT2_|varchar(4000)|存储JPA持久化对象的ID|

### 5.ACT_HI_VARINST表
历史变量表，用于存储历史流程的变量。

|字段|类型|说明|
|:---|:---|:---|
|ID_|varchar(64)|主键|
|PROC_INST_ID_|varchar(64)|流程实例ID|
|EXECUTION_ID_|varchar(64)|执行实例ID|
|TASK_ID_|varchar(64)|任务实例ID|
|NAME_|varchar(255)|变量名称|
|VAR_TYPE_|varchar(100)|变量类型|
|REV_|int|版本（用于乐观锁）|
|BYTEARRAY_ID_|varchar(64)|二进制数据ID,关联资源表|
|DOUBLE_|double|双精度浮点数|
|LONG_|bigint|长整型|
|TEXT_|varchar(4000)|文本类型|
|TEXT2_|varchar(4000)|存储JPA持久化对象的ID|
|CREATE_TIME_|timestamp|创建时间|
|LAST_UPDATED_TIME_|timestamp|最后更新时间|

### 6.ACT_HI_IDENTITYLINK表
历史流程与身份关系表，用于存储历史流程实例、任务实例与参与者之间的关联关系。

|字段|类型|说明|
|:---|:---|:---|
|ID_|varchar(64)|主键|
|GROUP_ID_|varchar(255)|用户组ID|
|TYPE_|varchar(255)|关系类型|
|USER_ID_|varchar(255)|用户ID|
|TASK_ID_|varchar(64)|任务实例ID|
|PROC_INST_ID_|varchar(64)|流程实例ID|

### 7.ACT_HI_COMMENT表
历史流程评论表，用于存储历史流程中通过TaskService添加的评论信息。

|字段|类型|说明|
|:---|:---|:---|
|ID_|varchar(64)|主键|
|TYPE_|varchar(255)|评论类型|
|TIME_|timestamp|评论时间|
|USER_ID_|varchar(255)|评论人ID|
|TASK_ID_|varchar(64)|任务实例ID|
|PROC_INST_ID_|varchar(64)|流程实例ID|
|ACTION_|varchar(255)|行为类型|
|MESSAGE_|varchar(4000)|处理意见|
|FULL_MSG_|LONGBLOB|全部消息（二进制）|

### 8.ACT_HI_ATTACHMENT表
历史流程附件表，用于存储通过TaskService添加的附件记录。

|字段|类型|说明|
|:---|:---|:---|
|ID_|varchar(64)|主键|
|REV_|int|版本（用于乐观锁）|
|USER_ID_|varchar(255)|上传人ID|
|NAME_|varchar(255)|附件名称|
|DESCRIPTION_|varchar(4000)|附件描述|
|TYPE_|varchar(255)|附件类型|
|TASK_ID_|varchar(64)|任务实例ID|
|PROC_INST_ID_|varchar(64)|流程实例ID|
|URL_|varchar(4000)|附件URL|
|CONTENT_ID_|varchar(64)|附件内容ID，关联ACT_GE_BYTEARRAY资源表|
|TIME_|datetime|上传时间|
