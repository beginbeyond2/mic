# 变更日志（CHANGELOG）

所有版本变更按倒序排列，每个版本下分类型列出条目。

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
