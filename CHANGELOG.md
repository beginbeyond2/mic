# 变更日志（CHANGELOG）

所有版本变更按倒序排列，每个版本下分类型列出条目。

---

## v0.4.2 - 2026-07-02

### 新增

- **middleware.command包（Trigger系列）** — 为14个Java文件添加完整工程级注释
  - 类级文档：ASCII艺术框，包含模块定位、核心职责、架构设计、数据流向、依赖关系、使用场景
  - 方法文档：所有方法的完整Javadoc（@param + @return）
  - 变量注释：所有成员变量的详细中文注释
  - 逐行注释：所有代码行添加行尾`//`中文注释

- **command包Trigger系列文件列表（14个）：**
  - `Command_Trigger_IIC.java` — IIC/I2C总线触发命令模型，管理source/clock/type/addr/data1/data2/condition/levelData/levelClock数组
  - `Command_Trigger_Lin.java` — LIN总线触发命令模型（注：发现setLinType/setSource中比较对象有潜在bug）
  - `Command_Trigger_Logic.java` — 逻辑触发命令模型，8通道status数组+function/condition+time/highTime/lowTime+level数组
  - `Command_Trigger_M1553B.java` — MIL-STD-1553B总线触发命令模型
  - `Command_Trigger_M429.java` — ARINC429总线触发命令模型，包含label/data范围校验
  - `Command_Trigger_Nedge.java` — 第N边沿触发命令模型，edge范围校验1-65535
  - `Command_Trigger_Pulse.java` — 脉宽触发命令模型
  - `Command_Trigger_Runt.java` — 矮脉宽触发命令模型，纯委托模式，透传到Command.get().getTrigger_dwart()
  - `Command_Trigger_SPI.java` — SPI总线触发命令模型，Type/Data方法同步调用Command.get().getBus_spi().setType()
  - `Command_Trigger_Setup.java` — 建立保持时间触发命令模型，桩实现，所有方法体为空
  - `Command_Trigger_Slope.java` — 斜率触发命令模型，HLevel/LLevel互相约束
  - `Command_Trigger_Timeout.java` — 超时触发命令模型
  - `Command_Trigger_Uart.java` — UART总线触发命令模型
  - `Command_Trigger_Video.java` — 视频触发命令模型，Line方法根据standard校验行号范围

- **middleware.mq包** — 为5个Java文件添加完整工程级注释
  - 类级文档：ASCII艺术框，包含模块定位、核心职责、架构设计、数据流向、依赖关系、使用场景
  - 方法文档：所有方法的完整Javadoc（@param + @return）
  - 变量注释：所有成员变量的详细中文注释
  - 逐行注释：所有代码行添加行尾`//`中文注释

- **mq包文件列表（5个）：**
  - `MQBase.java` — MQ消息抽象基类，定义rxType/mqType/chan公共字段，为RxBus消息传递提供统一数据载体
  - `MQChanSelectorManage.java` — 通道选择管理器，管理通道激活/开关/排序状态，监听EventBus并通过RxBus广播
  - `MQEnum.java` — MQ消息类型枚举，定义约60个枚举值（NULL/LOAD/CH_/MATH_/REF_/SERIAL_/TRIGGER_/THRESHOLD_/COMPLETE）
  - `MsgChActiveChange.java` — 通道激活变更消息，继承MQBase，用于RxBus广播通道激活状态变化
  - `MsgChOpenClose.java` — 通道开关消息，继承MQBase，用于RxBus广播通道打开/关闭状态变化

### 影响范围

- middleware.command包（Trigger系列）的14个Java文件
- middleware.mq包的5个Java文件（含msg子包2个）
- 仅添加注释，不改变任何代码逻辑和功能行为

---

## v0.4.1 - 2026-07-02

### 新增

- **rightslipmenu.dialog包** — 为8个Java文件添加完整工程级注释
  - 类级文档：ASCII艺术框，包含模块定位、核心职责、架构设计、数据流向、依赖关系、使用场景
  - 方法文档：所有方法的完整Javadoc（@param + @return）
  - 变量注释：所有成员变量的详细中文注释
  - 逐行注释：所有代码行添加行尾`//`中文注释

- **dialog包文件列表（8个）：**
  - `DialogBandWidth.java` — 带宽设置对话框，支持MHz/KHz单位切换、SeekBar+RadioButton组合调节带宽值
  - `DialogBandWidthHz.java` — 带宽频率设置对话框，大/小刻度滚轮联动调节，自动换算Hz/s/us/ns单位
  - `DialogBaudRate.java` — 波特率设置对话框，预设选择+自定义数字键盘输入，支持B/s/KB/s/MB/s单位
  - `DialogChannelLabel.java` — 通道标签设置对话框，4列网格选择+自定义文本键盘输入，支持通道/Math/Ref来源
  - `DialogChannelLabelAdapter.java` — 通道标签网格适配器，RecyclerView+GridLayoutManager渲染，None/自定义项跨2列
  - `DialogLoadRefCsvWave.java` — 加载Ref CSV波形对话框，顶部Ref与底部通道CheckBox双向映射，支持CSV通道筛选
  - `DialogMathFFTPersist.java` — Math FFT持久化设置对话框，预设列表选择持久化次数，缓存选择值
  - `DialogProbeInterface.java` — 探头接口设置对话框，MSP/MRCP/MOIP/MDP差异化显示，支持带宽选择和校准操作

### 影响范围

- 右侧滑出菜单对话框模块（rightslipmenu.dialog包）的8个Java文件
- 仅添加注释，不改变任何代码逻辑和功能行为

---

## v0.4.0 - 2026-07-01

### 新增

- **wavezone.display包** — 为7个Java文件添加完整工程级注释
  - 类级文档：ASCII艺术框，包含模块定位、核心职责、架构设计、数据流向、依赖关系、使用场景
  - 方法文档：所有方法的完整Javadoc（@param + @return）
  - 变量注释：所有成员变量的详细中文注释
  - 逐行注释：所有代码行添加行尾`//`中文注释

- **display包文件列表（7个）：**
  - `ICursorManage.java` — 光标管理接口，定义10个方法契约（行/列光标可见性、选中、移动、跟踪等）
  - `IGrid.java` — 网格显示接口，定义4个网格样式常量（十字线/十字点/全点/边框）和5个方法
  - `IWaveControl.java` — 波形控制接口，定义6个方法（光标微调移动、位置初始化、设置/获取光标位置）
  - `MsgCursorVisible.java` — 光标可见性消息POJO类，3个boolean字段（isYt/isShu/visible）
  - `Cursor_impIWave.java` — 光标线实现类（637行），实现IWave和IWorkMode接口，绘制虚线/实线光标、标识图标
  - `CursorLabel.java` — 光标测量标签绘制类（1791行），绘制测量参数标签、计算YT/XY模式测量值、标签跟随/固定显示
  - `RulerManage.java` — 标尺管理类（430行），单例模式+事件驱动，绘制垂直/水平标尺刻度，支持YT/XY模式切换

### 影响范围

- 波形显示模块（wavezone.display包）的7个Java文件
- 仅添加注释，不改变任何代码逻辑和功能行为

---

## v0.3.0 - 2026-06-23

### 新增

- **serials包及bean子包** — 为34个Java文件添加完整工程级注释
  - 类级文档：ASCII艺术框，包含模块定位、核心职责、架构设计、数据流向、依赖关系、使用场景
  - 方法文档：所有方法的完整Javadoc（@param + @return）
  - 变量注释：所有成员变量的详细中文注释
  - 逐行注释：所有代码行添加行尾`//`中文注释

- **serials根包（6个文件）：**
  - `Serials.java` — 串行协议选项实体类，继承RxMsgSelect实现Parcelable
  - `SerialsAdapter.java` — RecyclerView适配器，内部Holder模式
  - `SerialsDetailFlag.java` — 纯常量接口，定义协议类型和详情子类型标志
  - `SerialsUtils.java` — 纯静态工具类，进制转换、SPI掩码/数据解析、CAN DLC映射
  - `TopMsgTriggerSerials.java` — 串行触发消息封装类，实现ITriggerDetail
  - `TopLayoutTriggerSerials.java` — 串行触发主布局Fragment（1792行），管理协议选择列表和详情Fragment切换

- **bean子包必处理文件（16个）：**
  - `DataBean.java` — 数据封装Bean（进制+值）
  - `ISerialsDetail.java` — 空标记接口
  - `SerialsDetailArinc429Data.java` — ARINC429数据详情
  - `SerialsDetailArinc429Label.java` — ARINC429标签详情
  - `SerialsDetailArinc429LabelData.java` — ARINC429标签+数据详情
  - `SerialsDetailArinc429LabelSdi.java` — ARINC429标签+SDI详情
  - `SerialsDetailArinc429LabelSsm.java` — ARINC429标签+SSM详情
  - `SerialsDetailArinc429Sdi.java` — ARINC429 SDI详情
  - `SerialsDetailArinc429Ssm.java` — ARINC429 SSM详情
  - `SerialsDetailCanDataId.java` — CAN数据帧ID详情
  - `SerialsDetailCanIdData.java` — CAN ID+数据详情
  - `SerialsDetailCanRdId.java` — CAN远程数据ID详情
  - `SerialsDetailCanRemoteId.java` — CAN远程帧ID详情
  - `SerialsDetailI2c10WriteFrame.java` — I2C 10位写帧详情
  - `SerialsDetailI2cFrame1.java` — I2C帧1详情
  - `SerialsDetailI2cFrame2.java` — I2C帧2详情

- **bean子包额外文件（12个）：**
  - `SerialsDetailI2cNoAckInAdr.java` — I2C地址无应答详情
  - `SerialsDetailI2cRomData.java` — I2C ROM数据详情
  - `SerialsDetailLinFrameId.java` — LIN帧ID详情
  - `SerialsDetailLinIdData.java` — LIN ID+数据详情
  - `SerialsDetailM1553bCsWord.java` — 1553B指令/状态字详情
  - `SerialsDetailM1553bDataWord.java` — 1553B数据字详情
  - `SerialsDetailM1553bRtAddr.java` — 1553B远程终端地址详情
  - `SerialsDetailSpiData.java` — SPI数据详情
  - `SerialsDetailUart0Data.java` — UART通道0数据详情
  - `SerialsDetailUart1Data.java` — UART通道1数据详情
  - `SerialsDetailUartData.java` — UART通用数据详情
  - `SerialsDetailUartxData.java` — UART扩展通道数据详情

### 影响范围

- 串行触发功能模块（serials包及bean子包）的34个Java文件
- 仅添加注释，不改变任何代码逻辑和功能行为

---

## v0.2.1 - 2026-06-23

### 修改

- **top/layout/save/TopLayoutInvokeSetting.java** — 补充缺失的行尾注释
  - 字段声明：添加日志标签、Activity上下文、设置文件路径下拉选择器、调用按钮、浏览按钮、仅显示文件复选框、设置文件路径集合、Ref/Csv加载对话框、仅显示文件标志、对话框Toast、详情消息监听器、文件选择器的行尾注释
  - 方法签名：添加初始化视图控件、初始化事件控制、添加选中路径到集合、添加路径到设置文件集合、处理路径添加去重、获取设置文件列表、保存路径到缓存、删除无效路径项、删除空设置文件项、处理浏览操作、处理浏览点击、载入设置文件、恢复缓存状态、从缓存恢复设置文件路径、发送消息通知父级、设置详情消息监听器、获取保存详情的行尾注释
  - 特殊行：添加捕获中断异常、最终处理的行尾注释

- **top/layout/save/TopLayoutInvokeWav.java** — 补充缺失的行尾注释
  - 字段声明：添加日志标签、Activity上下文、WAV文件路径下拉选择器、调用按钮、浏览按钮、仅显示文件复选框、WAV文件路径集合、Ref/Csv加载对话框、仅显示文件标志、对话框Toast、详情消息监听器、文件选择器的行尾注释
  - 方法签名：添加初始化视图控件、初始化事件控制、添加选中路径到集合、添加路径到WAV文件集合、处理路径添加去重、获取WAV文件列表、保存WAV路径到缓存、删除无效路径项、删除空WAV文件项、处理浏览操作、处理浏览点击、从文件载入Ref、载入WAV文件到Ref通道、创建Ref召回数据Bean、设置Ref通道标签、设置Ref通道对象标签、获取可用的Ref通道编号、获取Ref时间刻度字符串、恢复缓存状态、从缓存恢复WAV文件路径、发送消息通知父级、设置详情消息监听器、获取保存详情的行尾注释

### 影响范围

- 保存/调用功能模块（top/layout/save包）的2个Java文件
- 仅补充行尾注释，不改变任何代码逻辑和功能行为
- 其余18个文件已在之前的会话中完成注释，无需修改

---

## v0.2.0 - 2026-06-18

### 修改

- **util/App.java** — 添加完整工程级注释
  - 类级文档：ASCII艺术框，包含模块定位、核心职责、架构设计、数据流向、依赖关系、使用示例、线程安全说明
  - 方法文档：onCreate()、checkProduct()、setUSBDeviceInfo()、setDebugMode()、setMainActivity()等所有方法的完整Javadoc
  - 变量注释：TAG、productType、usbVid、usbPid、debugMode、mainActivity等所有成员变量的详细注释
  - 逐行注释：所有代码行添加行尾注释

- **util/DToast.java** — 添加完整工程级注释
  - 类级文档：ASCII艺术框，包含模块定位、核心职责、架构设计（Handler消息机制）、数据流向、依赖关系、使用示例
  - 方法文档：get()、show()、cancel()、handleMessage()等所有方法的完整Javadoc
  - 变量注释：mToast、mContext、mHandler、SHOW、HIDE等所有成员变量和常量的详细注释
  - 逐行注释：所有代码行添加行尾注释

- **util/FileSelector.java** — 添加完整工程级注释
  - 类级文档：ASCII艺术框，包含模块定位、核心职责、架构设计、数据流向、依赖关系、使用示例
  - 方法文档：show()、hide()、setFileSelectListener()等所有方法的完整Javadoc
  - 变量注释：所有成员变量的详细注释
  - 逐行注释：所有代码行添加行尾注释

- **util/HybridCryptoManager.java** — 添加完整工程级注释
  - 类级文档：ASCII艺术框，包含模块定位、核心职责、架构设计（RSA+AES混合加密）、数据流向、依赖关系、使用示例、线程安全说明
  - 方法文档：generateKeyPair()、encrypt()、decrypt()、exportPublicKey()、importPublicKey()、saveEncryptedFile()、loadEncryptedFile()等所有方法的完整Javadoc
  - 变量注释：所有成员变量和常量的详细注释
  - 逐行注释：所有代码行添加行尾注释

- **util/CacheUtil.java** — 添加完整工程级注释
  - 类级文档：ASCII艺术框，包含模块定位、核心职责、架构设计、数据流向、依赖关系、缓存结构、使用示例、线程安全说明
  - 方法文档：get()、getInt()、getDouble()、getLong()、getString()、getBoolean()、putMap()、putOtherMap()、getValueFromInit()、getValueFromInit1()、initStateCacheLoad()、setLoadMenuState()、isLoadParamComplete()、startUp()、checkCacheParam()、checkMSSStoreMap()、checkLoadRefFile()等所有方法的完整Javadoc
  - 变量注释：map、otherMap、mapLoadProcess、loadComplete、clickFactoryReset、serialsId、bRefTimebase、isChange、time等所有成员变量的详细注释
  - 常量注释：200+缓存Key常量（MAIN_LEFT_*、RIGHT_SLIP_*、TOP_SLIP_*、LOAD_*等）全部添加行尾注释
  - 逐行注释：关键方法体内部添加逐行行尾注释

### 影响范围

- 工具模块（util包）的5个Java文件
- 仅添加注释，不改变任何代码逻辑和功能行为

---

## v0.1.1 - 2026-06-18

### 修改

- **first/BootCompletedReceiver.java** — 添加完整工程级注释
  - 类级文档：ASCII艺术框，包含模块定位、核心职责、架构设计、数据流向、依赖关系、使用示例、注意事项
  - 方法文档：onReceive()、ms_sleep()的完整Javadoc，含功能说明、处理流程、参数说明、注意事项
  - 变量注释：TAG、ACTION、ACTION_SHUTDOWN、ACTION_STANDBY、ACTION_STANDBY_OUT、bAutoRange、bAuto、handler的详细注释
  - 逐行注释：所有代码行添加行尾注释

- **first/FirstActivity.java** — 添加完整工程级注释
  - 类级文档：ASCII艺术框，包含模块定位、核心职责、架构设计、数据流向、依赖关系、使用示例、注意事项
  - 方法文档：onCreate()、onActivityResult()、getParamsType()、requestPermissions()、isValidProduct()的完整Javadoc
  - 变量注释：TAG、REQUEST_CODE_OVERLAY、onShowStartListener的详细注释
  - 逐行注释：所有代码行添加行尾注释

- **first/LocaleChangeReceiver.java** — 添加完整工程级注释
  - 类级文档：ASCII艺术框，包含模块定位、核心职责、架构设计、数据流向、依赖关系、使用示例、注意事项
  - 方法文档：onReceive()的完整Javadoc
  - 逐行注释：所有代码行添加行尾注释

- **first/SplashScreenSurfaceView.java** — 添加完整工程级注释
  - 类级文档：ASCII艺术框，包含模块定位、核心职责、架构设计（渲染循环）、数据流向、依赖关系、使用示例、注意事项
  - 方法文档：构造函数、initView()、surfaceCreated()、surfaceChanged()、stop()、surfaceDestroyed()的完整Javadoc
  - 变量注释：TAG、holder、context、bmp、drawThread、x、y、isRun、dotY、bmpSrc、ScreenDes、run的详细注释
  - 逐行注释：所有代码行添加行尾注释

### 影响范围

- 启动模块（first包）的4个Java文件
- 仅添加注释，不改变任何代码逻辑和功能行为
