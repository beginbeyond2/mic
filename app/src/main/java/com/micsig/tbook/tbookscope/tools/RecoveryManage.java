package com.micsig.tbook.tbookscope.tools;  // 示波器工具类包

import android.content.Context;  // Android上下文类

import com.micsig.tbook.scope.Display.Display;  // 显示模式管理类
import com.micsig.tbook.scope.Display.DisplayXYService;  // XY显示服务类
import com.micsig.tbook.scope.Scope;  // 示波器核心控制类
import com.micsig.tbook.scope.ScopeBase;  // 示波器基础参数类
import com.micsig.tbook.scope.Trigger.Trigger;  // 触发器类
import com.micsig.tbook.scope.Trigger.TriggerFactory;  // 触发器工厂类
import com.micsig.tbook.scope.Trigger.TriggerLevel;  // 触发电平类
import com.micsig.tbook.scope.channel.Channel;  // 通道基类
import com.micsig.tbook.scope.channel.ChannelFactory;  // 通道工厂类
import com.micsig.tbook.scope.channel.MathChannel;  // 数学运算通道类
import com.micsig.tbook.scope.horizontal.HorizontalAxis;  // 水平轴控制类
import com.micsig.tbook.scope.probe.ProbeUtils;  // 探头工具类
import com.micsig.tbook.tbookscope.LoadCache;  // 载入缓存事件类
import com.micsig.tbook.tbookscope.MainActivity;  // 主Activity类
import com.micsig.tbook.tbookscope.R;  // 资源ID类
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightLayoutSerials;  // 串行解码右滑菜单类
import com.micsig.tbook.tbookscope.rxjava.RxBus;  // RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum;  // RxJava事件枚举
import com.micsig.tbook.tbookscope.top.layout.trigger.TopLayoutTrigger;  // 触发顶部布局类
import com.micsig.tbook.tbookscope.util.CacheUtil;  // 缓存工具类
import com.micsig.tbook.tbookscope.util.DToast;  // 自定义Toast提示类
import com.micsig.tbook.ui.util.TBookUtil;  // TBook通用工具类
import com.micsig.tbook.ui.wavezone.TChan;  // 通道UI映射工具类

/**
 * @auother Liwb
 * @description:
 * @data:2024-3-29 13:54
 */
/*
 * +=============================================================================+
 * |                        RecoveryManage - 配置恢复管理器                       |
 * +=============================================================================+
 * | 模块定位: tbookscope.tools 工具模块                                          |
 * | 核心职责: 负责示波器用户配置的恢复/载入，将缓存中的参数还原到FPGA及UI各子系统中        |
 * | 架构设计: 单例模式，作为配置恢复的统一入口，协调通道/触发/显示/串行解码等子模块       |
 * | 数据流向: 缓存文件(CacheUtil) → RecoveryManage → FPGA参数(Scope/Channel等)    |
 * | 依赖关系: CacheUtil(缓存读取), Scope/Channel/Trigger(FPGA控制),               |
 * |           RxBus(异步完成通知), MainActivity(UI预处理)                          |
 * | 使用场景: 开机恢复默认配置、载入用户保存的配置文件、出厂设置恢复                    |
 * +=============================================================================+
 */
public class RecoveryManage {
    //region  单例
    private static RecoveryManage ins = null;  // 单例实例引用

    /**
     * 获取RecoveryManage的单例实例。
     * <p>采用懒加载方式，首次调用时创建实例。</p>
     *
     * @return RecoveryManage单例实例
     */
    public static RecoveryManage getIns() {
        if (ins == null) {  // 判断单例是否已创建
            ins = new RecoveryManage();  // 首次访问时创建单例实例
        }
        return ins;  // 返回单例实例
    }
    //endregion

    private  boolean Loading=false;  // 标记当前是否正在载入配置
    private Context context;  // Android上下文引用，用于访问MainActivity

    /**
     * 构造函数。注册RxBus事件监听，监听配置载入完成事件。
     * <p>当收到COMPLETE事件后，重新启用FPGA命令并清除载入状态。</p>
     */
    public  RecoveryManage(){
        RxBus.getInstance().dealObservable(RxEnum.COMPLETE,this::OnComplete);  // 订阅配置载入完成事件，回调OnComplete方法
    }

    /**
     * 配置载入完成回调。
     * <p>重新启用FPGA命令通道，并将Loading状态置为false。</p>
     *
     * @param obj 事件携带的对象（未使用）
     */
    private void OnComplete(Object obj) {
        Scope.getInstance().enableCommand(true);  // 重新启用FPGA命令通道
        Loading=false;  // 清除载入中状态标记
    }

    /**
     * 查询当前是否正在载入配置。
     *
     * @return true表示正在载入，false表示未在载入
     */
    public boolean isLoading(){
        return Loading;  // 返回当前载入状态
    }

    /**
     * 初始化RecoveryManage，传入Android上下文。
     * <p>上下文用于后续调用MainActivity的预处理方法。</p>
     *
     * @param context Android上下文，通常为MainActivity实例
     */
    public void init(Context context){
        this.context=context;  // 保存上下文引用
    }

    /**
     * 从指定文件路径载入用户配置参数。
     * <p>载入流程：禁用FPGA命令 → 预处理UI → 读取缓存 → 加载FPGA参数 → 通知主界面刷新 → 发送完成事件。</p>
     * <p>若配置文件读取失败，则清空缓存并恢复默认水平轴参数。</p>
     *
     * @param pathFile 配置文件路径
     * @throws InterruptedException 线程中断异常
     */
    public void loadParam(String pathFile) throws InterruptedException {
        Loading =true;  // 标记正在载入配置
        Scope.getInstance().enableCommand(false);  // 禁用FPGA命令通道，防止载入过程中发送命令
        ((MainActivity) context).preMainLoadCahceProcess();  // 调用MainActivity的载入前预处理流程

        if (!SaveManage.getInstance().loadUserSet(pathFile, CacheUtil.get().getCacheMap())) {  // 尝试从文件加载用户配置到缓存Map
            //配置载入失败则清空配置载入默认配置值
            CacheUtil.get().clearCacheMap();  // 清空缓存Map中的所有参数
            HorizontalAxis horizontalAxis = HorizontalAxis.getInstance();  // 获取水平轴单例
            horizontalAxis.setTimeScaleIdOfView(HorizontalAxis.WPI_STANDARD, HorizontalAxis.TSI_2mS);  // 恢复默认时基档位为2ms
            horizontalAxis.setTimePosOfView(HorizontalAxis.WPI_STANDARD, 0);  // 恢复默认水平位置为0
            DToast.get().show(R.string.saveRecoveryFileIsNotExist);  // 提示用户配置文件不存在
        }

//        ((MainActivity) context).updateMainLoadCaheProcess();
//        ((MainActivity) context).postMainLoadCacheProcess();
        loadFpgaParam();  // 将缓存参数写入FPGA各子系统
        RxBus.getInstance().post(RxEnum.MAIN_LOAD_CACHE,new LoadCache());  // 发送主界面载入缓存事件，通知UI刷新
        RxBus.getInstance().post(RxEnum.COMPLETE,new Object());  // 发送配置载入完成事件，触发OnComplete回调
    }

    /**
     * 将缓存中的参数依次加载到FPGA各子系统。
     * <p>加载顺序：示波器状态 → 工作模式 → 通道参数 → 数学通道 → 串行解码。</p>
     */
    private void loadFpgaParam(){
        loadScopeState();  // 加载示波器运行状态
        loadWorkMode();  // 加载工作模式（YT/XY/缩放/滚动）
        loadChannel();  // 加载通道参数（耦合/探头/带宽/垂直/触发等）
        loadMath();  // 加载数学运算通道参数
        loadSerials();  // 加载串行解码参数
    }

    /**
     * 加载示波器运行状态。
     * <p>临时设置为运行状态以使checkCacheParam生效，完成后恢复原始运行/停止状态。</p>
     */
    private void loadScopeState() {
        Scope mScope=Scope.getInstance();  // 获取Scope单例
        boolean bRun = mScope.isRun();  // 保存当前运行状态
        mScope.setRun(true);  // 临时设置为运行状态，确保checkCacheParam能正常执行
        CacheUtil.get().checkCacheParam();  // 检查并修正缓存参数的合法性
        mScope.setRun(bRun);  // 恢复原始运行/停止状态
    }

    /**
     * 加载工作模式参数。
     * <p>根据缓存中的时基模式索引，恢复YT模式或XY模式。</p>
     * <p>YT模式下还会恢复缩放状态和滚动模式下的水平位置。</p>
     */
    private void loadWorkMode(){
        int ytIdx=CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_COMMON_TIMEBASE);  // 读取时基模式索引（0=YT, 1=XY）
        if (ytIdx==0){  // YT模式
            Display.getInstance().setDisplayMode(Display.DISPLAY_YT);  // 设置显示模式为YT
            boolean isZoom = CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_ZOOM);  // 读取缩放开关状态
            Scope.getInstance().setZoom(isZoom);  // 设置缩放模式

            boolean isRoll= CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_COMMON_ROLL)==0;  // 读取滚动模式状态（0=滚动）
            if (isRoll==false){  // 非滚动模式时恢复水平位置
                long l = CacheUtil.get().getLong(CacheUtil.MAIN_WAVE_TIMEBASE_POSITION_NORMAL);  // 读取正常模式下的水平位置偏移
                HorizontalAxis.getInstance().setTimePoseOfViewPix(ScopeBase.getWidth() / 2 - l);  // 计算并设置水平位置像素偏移（屏幕中心减去缓存偏移）
            }
        }else if (ytIdx==1){  // XY模式
            Display.getInstance().setDisplayMode(Display.DISPLAY_XY);  // 设置显示模式为XY
        }
    }

    /**
     * 加载所有通道参数。
     * <p>遍历每个UI通道，从缓存中读取并恢复：开关/反相/耦合/探头/带宽/垂直档位/偏移/延迟/位置/触发电平等参数。</p>
     */
    private void loadChannel() {
        TChan.foreachChan((uiCh)->{  // 遍历所有UI通道
            int fpgaCh=TChan.toFpgaChNo(uiCh);  // 将UI通道号转换为FPGA通道号
            Channel chan= ChannelFactory.getDynamicChannel(fpgaCh);  // 根据FPGA通道号获取通道对象

            boolean isOpen=CacheUtil.get().getBoolean(CacheUtil.MAIN_CHANNEL_OPEN_STATE+uiCh);  // 读取通道开关状态
            boolean isInvert=CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_CH_INVERT+uiCh);  // 读取通道反相状态
            int coupleIdx=CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_CH_COUPLE+uiCh);  // 读取耦合类型索引
            int probeIdx=CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_CH_PROBE_TYPE+uiCh);  // 读取探头类型索引
            String probeMul=CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_PROBE_MULTIPLE+uiCh);  // 读取探头倍率字符串
            String probeMulDef=CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_PROBE_MULTIPLE_USERDEFINE+uiCh);  // 读取用户自定义探头倍率
            int bandwidthIdx=CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_CH_BANDWIDTH+uiCh);  // 读取带宽限制类型索引
            String bandwidthH=CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_BANDWIDTH_HIGH_EDIT+uiCh);  // 读取高通带宽编辑值
            String bandwidthL=CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_BANDWIDTH_LOW_EDIT+uiCh);  // 读取低通带宽编辑值
//            String probBandwidth= CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_PROBE_BANDWIDTH+uiCh);
            int verBase= CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_CH_VERTICALBASE+uiCh);  // 读取垂直基准模式
            String labelIdx=CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_LABEL+uiCh);  // 读取通道标签索引
            String labelDef=CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_LABEL_USERDEFINE+uiCh);  // 读取用户自定义标签
            String delay= CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_DELAY+uiCh);  // 读取通道延迟字符串
            String offset =CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_OFFSET+uiCh);  // 读取通道偏移字符串
            boolean fine=CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_CH_FINE_ENABLE+uiCh);  // 读取细调开关状态
            String fineExtent=CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_FINE_EXTENT+uiCh);  // 读取细调范围字符串

            chan.setOpen(isOpen);  // 设置通道开关状态
            chan.setInvert(isInvert);  // 设置通道反相状态
            chan.setCoupleType(coupleIdx);  // 设置耦合类型（AC/DC/GND）
            chan.setProbeType(probeIdx);  // 设置探头类型
            chan.setProbeRate(TBookUtil.getDoubleFromX(probeMul));  // 将探头倍率字符串转换为数值并设置
            switch (bandwidthIdx){  // 根据带宽限制类型设置对应带宽值
                case Channel.BANDWIDTH_TYPE_FULL:{  // 全带宽
                    chan.setBandWidthType(bandwidthIdx,Channel.getMaxBandWidth());  // 设置为最大带宽
                }break;
                case Channel.BANDWIDTH_TYPE_200M:{  // 200MHz带宽限制
                    chan.setBandWidthType(bandwidthIdx,200*1e6);  // 设置200MHz带宽
                }break;
                case Channel.BANDWIDTH_TYPE_300M:{  // 300MHz带宽限制
                    chan.setBandWidthType(bandwidthIdx,300*1e6);  // 设置300MHz带宽
                }break;
                case Channel.BANDWIDTH_TYPE_HIGHPASS:{  // 高通滤波
                    chan.setBandWidthType(bandwidthIdx, TBookUtil.getMHzFromHz(bandwidthH)*1e6);  // 将高通频率转换为Hz并设置
                }break;
                case Channel.BANDWIDTH_TYPE_LOWPASS:{  // 低通滤波
                    chan.setBandWidthType(bandwidthIdx,TBookUtil.getMHzFromHz(bandwidthL)*1e6);  // 将低通频率转换为Hz并设置
                }break;
                case Channel.BANDWIDTH_TYPE_20M:{  // 20MHz带宽限制
                    chan.setBandWidthType(bandwidthIdx,20*1e6);  // 设置20MHz带宽
                }break;
            }
            chan.setVerticalMode(verBase);  // 设置垂直基准模式
            chan.setChOffsetVal(TBookUtil.getDoubleFromM(offset.replace("A","").replace("V","").replace(" ","")));  // 去除单位后解析偏移值并设置
            chan.setDelay((int)(TBookUtil.getDoubleFromM(delay.replace("s", "")) * 1e12 + 0.1));  // 将延迟从秒转换为皮秒并设置（加0.1用于四舍五入）
            chan.setVScaleVal(TBookUtil.getDoubleFromM(fineExtent));  // 设置细调垂直档位值

            //位置
            double pos=0;  // 通道垂直位置像素值
            if (isYt()){  // YT模式下恢复通道位置
                int h = (isZoom() ? ScopeBase.getNewZoomHeight() : ScopeBase.getNewHeight()) / 2;  // 计算波形区域半高（缩放或正常模式）
                pos = h - Tools.getChannelPositionUI(uiCh);  // 计算通道位置（半高减去UI位置偏移）
                chan.setPos(pos);  // 设置通道垂直位置
            }else if (isXy()){  // XY模式下恢复XY位置
                if (uiCh==TChan.Ch1){  // CH1对应X轴
                    pos= CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_CH_XY_POSITION + TChan.Ch1);  // 读取CH1的XY位置缓存值
                    DisplayXYService.getInstance().setX(ScopeBase.getXYWidth() / 2 - (int) Math.round(pos));  // 设置X轴位置（中心减去偏移）
                }else if (uiCh==TChan.Ch2){  // CH2对应Y轴
                    pos=CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_CH_XY_POSITION + TChan.Ch2);  // 读取CH2的XY位置缓存值
                    DisplayXYService.getInstance().setY(ScopeBase.getXYWidth() / 2 - (int) Math.round(pos));  // 设置Y轴位置（中心减去偏移）
                }
            }

            //档位
            int vScaleId=CacheUtil.get().getInt(CacheUtil.MAIN_CHAN_V_SCALE_ID +uiCh);  // 读取垂直档位ID
            chan.setVScaleId(vScaleId);  // 设置垂直档位ID
            int val = Scope.vSpanOfView(chan.getResistanceType(),chan.getVScaleVal() / chan.getProbeRate());  // 根据阻抗类型和实际档位值计算视图范围
            chan.setVRange(-val, val);  // 设置垂直显示范围（对称）

        },(ui)->ChannelFactory.getDynamicChannel(TChan.toFpgaChNo(ui))==null);  // 过滤条件：跳过未实例化的通道


        //触发电平
        Trigger trigger = TriggerFactory.getInstance().getTrigger();  // 获取触发器实例
        int src=CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER_SOURCE);  // 读取触发源通道索引
        int trigType= CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER);  // 读取触发类型索引
        if (trigType== TopLayoutTrigger.DETAIL_SLOPE || trigType==TopLayoutTrigger.DETAIL_RUNT){  // 斜率触发或矮脉冲触发需要双电平
            TriggerLevel triggerLevel = trigger.getTriggerLevel(TriggerLevel.TRIGGER_LEVEL_HIGH, src);  // 获取高触发电平对象
            triggerLevel.setPos(Tools.getYTLevelCache(CacheUtil.TRIGGER_CHANNEL_H + src),true);  // 设置高触发电平位置
            triggerLevel = trigger.getTriggerLevel(TriggerLevel.TRIGGER_LEVEL_LOW, src);  // 获取低触发电平对象
            triggerLevel.setPos(Tools.getYTLevelCache(CacheUtil.TRIGGER_CHANNEL + src), true);  // 设置低触发电平位置
        }else {  // 其他触发类型只需单电平
            double vol = Tools.getYTLevelCache(CacheUtil.TRIGGER_CHANNEL + src);  // 读取触发电平缓存值
            TriggerLevel triggerLevel = TriggerFactory.getInstance().getTrigger().getTriggerLevel(src);  // 获取触发电平对象
            triggerLevel.setPos(vol, true);  // 设置触发电平位置
        }
    }

    /**
     * 加载数学运算通道参数。
     * <p>当前为预留接口，遍历数学通道但未执行具体参数恢复逻辑。</p>
     */
    private void loadMath(){
        TChan.foreachMath((uiMath)->{  // 遍历所有数学通道
            int fpgaCh=TChan.toFpgaChNo(uiMath);  // 将UI数学通道号转换为FPGA通道号
            MathChannel math= ChannelFactory.getMathChannel(fpgaCh);  // 获取数学通道对象


        });
    }

    /**
     * 加载串行解码参数。
     * <p>遍历所有串行解码通道，根据解码协议类型（M429/CAN/SPI/I2C/UART/LIN/1553B）恢复对应信号源的阈值电平。</p>
     */
    private void loadSerials(){
        TChan.foreachSerial((uiS)->{  // 遍历所有串行解码通道
            //正常的属性设置

            //阈值电平
            int discreetType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + uiS);  // 读取当前串行解码通道的协议类型
            if (discreetType == RightLayoutSerials.SERIALS_M429) {  // M429协议
                int src = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M429_SOURCE + TChan.toSerialNumber(uiS));  // 读取M429信号源通道
                setDiscreetPos(src);  // 设置该信号源的阈值电平
            } else if (discreetType == RightLayoutSerials.SERIALS_CAN) {  // CAN/SPI协议
                int srcCLK = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_SPI_CLK + TChan.toSerialNumber(uiS));  // 读取SPI时钟源通道
                int srcData = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_SPI_DATA + TChan.toSerialNumber(uiS));  // 读取SPI数据源通道
                boolean csEnable = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_SERIALS_SPI_CSSWITCH + TChan.toSerialNumber(uiS));  // 读取SPI片选开关状态
                if (csEnable) {  // 片选使能时
                    int srcCs = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_SPI_CS + TChan.toSerialNumber(uiS));  // 读取SPI片选源通道
                    setDiscreetPos(srcCs);  // 设置片选信号源的阈值电平
                }
                setDiscreetPos(srcCLK);  // 设置时钟信号源的阈值电平
                setDiscreetPos(srcData);  // 设置数据信号源的阈值电平

            } else if (discreetType == RightLayoutSerials.SERIALS_I2C) {  // I2C协议
                int srcData = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_I2C_SDA + TChan.toSerialNumber(uiS));  // 读取I2C数据线(SDA)源通道
                int srcCLK = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_I2C_SCL + TChan.toSerialNumber(uiS));  // 读取I2C时钟线(SCL)源通道
                setDiscreetPos(srcData);  // 设置SDA信号源的阈值电平
                setDiscreetPos(srcCLK);  // 设置SCL信号源的阈值电平
            } else if (discreetType == RightLayoutSerials.SERIALS_UART) {  // UART协议
                int src = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_UART_RX + TChan.toSerialNumber(uiS));  // 读取UART接收源通道
                setDiscreetPos(src);  // 设置接收信号源的阈值电平
            } else if (discreetType == RightLayoutSerials.SERIALS_LIN) {  // LIN协议
                int src = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_LIN_SOURCE + TChan.toSerialNumber(uiS));  // 读取LIN信号源通道
                setDiscreetPos(src);  // 设置LIN信号源的阈值电平
            } else if (discreetType == RightLayoutSerials.SERIALS_CAN) {  // CAN协议（重复分支）
                int src = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_CAN_SOURCE + TChan.toSerialNumber(uiS));  // 读取CAN信号源通道
                setDiscreetPos(src);  // 设置CAN信号源的阈值电平
            } else if (discreetType == RightLayoutSerials.SERIALS_M429) {  // M429协议（重复分支）
                int src = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M429_SOURCE + TChan.toSerialNumber(uiS));  // 读取M429信号源通道
                setDiscreetPos(src);  // 设置M429信号源的阈值电平
            } else if (discreetType == RightLayoutSerials.SERIALS_M1553B) {  // MIL-STD-1553B协议
                int src = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M1553B_SOURCE + TChan.toSerialNumber(uiS));  // 读取1553B信号源通道
                setDiscreetPos(src);  // 设置1553B信号源的阈值电平
            }

        });
    }

    /**
     * 设置指定FPGA通道的阈值电平（主/副电平）。
     * <p>根据通道是否为M429高低电平模式，分别设置主电平和副电平。</p>
     *
     * @param fpgaChan FPGA通道号
     */
    private void setDiscreetPos(int fpgaChan){
        int chIdx=TChan.toUiChNo(fpgaChan);  // 将FPGA通道号转换为UI通道号
        Channel channel = ChannelFactory.getDynamicChannel(fpgaChan);  // 获取对应的通道对象
        if (channel != null) {  // 通道存在时才设置
            double vol1 = Tools.getYTLevelCache(CacheUtil.VALUE_CHANNEL + chIdx);  // 读取该通道的阈值电平1
            {
                if (isValueLevelHighAndLow(chIdx)) {  // 判断是否为M429高低电平模式
                    channel.setBusSecondaryLevel(vol1);  // M429模式下vol1作为副电平
                } else {
                    channel.setBusPrimaryLevel(vol1);  // 普通模式下vol1作为主电平
                }
            }

            double vol2 = Tools.getYTLevelCache(CacheUtil.VALUE_CHANNEL_H + chIdx);  // 读取该通道的阈值电平2（高电平）
            {
                if (isValueLevelHighAndLow(chIdx)) {  // 判断是否为M429高低电平模式
                    channel.setBusPrimaryLevel(vol2);  // M429模式下vol2作为主电平
                } else {
                    channel.setBusSecondaryLevel(vol2);  // 普通模式下vol2作为副电平
                }
            }
        }
    }

    /**
     * 判断指定UI通道是否为M429协议的高低电平模式。
     * <p>遍历所有串行解码通道，检查是否存在M429协议且信号源匹配指定通道的情况。</p>
     *
     * @param uiChan UI通道号
     * @return true表示该通道处于M429高低电平模式，false表示否
     */
    public static boolean isValueLevelHighAndLow(int uiChan) {
        boolean b= TChan.foreachSerialResult((uiCh)->{  // 遍历所有串行解码通道并返回匹配结果
                    int type=CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + TChan.toSerialNumber(uiCh));  // 读取串行解码协议类型
                    int src= CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS_M429_SOURCE + TChan.toSerialNumber(uiCh));  // 读取M429信号源通道
                    boolean b1=type==RightLayoutSerials.SERIALS_M429 && src==TChan.toFpgaChNo(uiChan);  // 判断协议为M429且信号源匹配
                    return b1;  // 返回匹配结果
                });
        return b;  // 返回最终判断结果
    }

    /**
     * 判断当前是否为YT（时基）显示模式。
     *
     * @return true表示YT模式，false表示非YT模式
     */
    private boolean isYt(){
        return CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_COMMON_TIMEBASE)==0;  // 时基模式索引为0即为YT模式
    }

    /**
     * 判断当前是否为XY显示模式。
     *
     * @return true表示XY模式，false表示非XY模式
     */
    private boolean isXy(){
        return CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_COMMON_TIMEBASE)==1;  // 时基模式索引为1即为XY模式
    }

    /**
     * 判断当前是否开启缩放模式。
     *
     * @return true表示缩放模式开启，false表示未开启
     */
    private boolean isZoom(){
        boolean isZoom = CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_ZOOM);  // 从缓存读取缩放开关状态
        return isZoom;  // 返回缩放模式状态
    }

    /**
     * 判断当前是否为滚动模式。
     *
     * @return true表示滚动模式，false表示非滚动模式
     */
    private boolean isRoll(){
        boolean isRoll= CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_COMMON_ROLL)==0;  // 滚动模式索引为0即为滚动模式
        return isRoll;  // 返回滚动模式状态
    }

}
