package com.micsig.tbook.tbookscope.top.layout.factoryCalibration; // 出厂校准布局Fragment所在包

import android.content.Context; // 导入上下文类，用于访问系统服务
import android.content.Intent; // 导入意图类，用于Activity跳转
import android.os.Bundle; // 导入Bundle类，用于保存和恢复Fragment状态
import android.util.Log; // 导入日志类
import android.view.LayoutInflater; // 导入布局填充器，用于加载XML布局
import android.view.View; // 导入视图基类
import android.view.ViewGroup; // 导入视图组基类
import android.view.inputmethod.InputMethodManager; // 导入输入法管理器，用于隐藏软键盘
import android.widget.Button; // 导入按钮控件
import android.widget.EditText; // 导入编辑框控件
import android.widget.SeekBar; // 导入滑动条控件
import android.widget.TextView; // 导入文本视图控件
import android.widget.Toast; // 导入Toast提示类

import androidx.annotation.Nullable; // 导入可空注解
import androidx.fragment.app.Fragment; // 导入Fragment基类

import com.micsig.base.Logger; // 导入日志工具类
import com.micsig.smart.PropertyManage; // 导入属性管理类，用于读写设备属性
import com.micsig.tbook.scope.Calibrate.CabteRegister; // 导入校准寄存器类，管理校准状态
import com.micsig.tbook.scope.Calibrate.CalibrateService; // 导入校准服务类，定义校准类型常量
import com.micsig.tbook.scope.Calibrate.FactorCalibrate; // 导入出厂校准执行类
import com.micsig.tbook.scope.Data.SaveRecoverySession; // 导入会话保存恢复类
import com.micsig.tbook.scope.Calibrate.MHO68v2.HW_MHO68V2; // 导入MHO68v2硬件校准类
import com.micsig.tbook.scope.Event.EventBase; // 导入事件基类
import com.micsig.tbook.scope.Event.EventFactory; // 导入事件工厂类，用于注册和分发事件
import com.micsig.tbook.scope.Event.EventUIObserver; // 导入UI事件观察者基类
import com.micsig.tbook.scope.Scope; // 导入示波器核心类，提供FPGA信息
import com.micsig.tbook.scope.channel.Channel; // 导入通道类，提供带宽常量
import com.micsig.tbook.scope.channel.ChannelFactory; // 导入通道工厂类
import com.micsig.tbook.scope.fpga.FPGACommand; // 导入FPGA命令类
import com.micsig.tbook.scope.horizontal.HorizontalAxis; // 导入水平轴类
import com.micsig.tbook.scope.vertical.VerticalAxis; // 导入垂直轴类
import com.micsig.tbook.tbookscope.LoadCache; // 导入缓存加载消息类
import com.micsig.tbook.tbookscope.MainActivity; // 导入主Activity类
import com.micsig.tbook.tbookscope.MainMsgSlip; // 导入主界面滑动消息类
import com.micsig.tbook.tbookscope.MainViewGroup; // 导入主视图组类
import com.micsig.tbook.tbookscope.R; // 导入资源ID类
import com.micsig.tbook.tbookscope.main.dialog.DialogOk; // 导入确认对话框类
import com.micsig.tbook.tbookscope.middleware.command.Command; // 导入命令中间件
import com.micsig.tbook.tbookscope.rxjava.RxBus; // 导入响应式消息总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // 导入消息枚举定义
import com.micsig.tbook.tbookscope.tools.SaveManage; // 导入保存管理类
import com.micsig.tbook.tbookscope.tools.ScreenControls; // 导入屏幕控制类，用于锁定/解锁屏幕
import com.micsig.tbook.tbookscope.tools.Tools; // 导入通用工具类
import com.micsig.tbook.tbookscope.util.CacheUtil; // 导入缓存工具类
import com.micsig.tbook.tbookscope.util.DToast; // 导入自定义Toast类
import com.micsig.tbook.ui.util.StrUtil; // 导入字符串工具类

import java.io.File; // 导入文件类
import java.net.Inet6Address; // 导入IPv6地址类
import java.net.InetAddress; // 导入IP地址基类
import java.net.NetworkInterface; // 导入网络接口类
import java.net.SocketException; // 导入Socket异常类
import java.text.SimpleDateFormat; // 导入简单日期格式化类
import java.util.ArrayList; // 导入动态数组类
import java.util.Arrays; // 导入数组工具类
import java.util.Date; // 导入日期类
import java.util.Enumeration; // 导入枚举类
import java.util.HashMap; // 导入哈希映射类
import java.util.Map; // 导入映射接口
import java.util.Objects; // 导入对象工具类
import java.util.List; // 导入列表接口

import io.reactivex.rxjava3.annotations.NonNull; // 导入非空注解
import io.reactivex.rxjava3.functions.Consumer; // 导入RxJava消费者接口


/**
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │ 模块定位：出厂校准(Factory Calibration)页面Fragment                          │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 核心职责：提供出厂校准界面，包括启动校准、重置校准数据、显示校准状态和FPGA信息 │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 架构设计：独立Fragment，通过EventUIObserver接收校准进度和结果事件            │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 数据流向：FactorCalibrate(校准执行) → EventUIObserver → UI更新             │
 * │           用户操作 → FactorCalibrate/CabteRegister → 硬件校准               │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 依赖关系：FactorCalibrate(校准执行)、CabteRegister(校准寄存器)、             │
 * │           ScreenControls(屏幕锁定)、PropertyManage(属性管理)                │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 使用场景：工厂模式下进行示波器出厂校准，包括零点、增益、偏移、电容等校准项     │
 * └─────────────────────────────────────────────────────────────────────────────┘
 *
 * Created by yangj on 2018/7/25.
 */

public class TopLayoutFactoryCalibration extends Fragment { // 出厂校准页面Fragment
    private Context context; // 上下文对象，用于访问系统服务和资源
    private Button btnTop; // 启动校准按钮
    private Button btnReset; // 重置校准数据按钮
    private Button btnDeliveryDate; // 设置出厂日期按钮
    private Button btnBandWidth; // 设置带宽按钮
    private TextView fpgatv,tvCabteTime; // FPGA版本信息文本、校准时间文本



    private TextView tvTopZeroTitle,tvTopZeroTitleDetail,tvNetip; // 零点校准标题/详情、网络IP文本
    private TextView tvCenterChGainTitle, tvCenterChGainTitleDetail; // 通道增益校准标题/详情
    private TextView tvCenterChOffsetTitle, tvCenterChOffsetTitleDetail; // 偏移量校准标题/详情

    private TextView tvCenterChCapTitle, tvCenterChCapTitleDetail; // 电容校准标题/详情

    private TextView tvCenterChGainTitle_2, tvCenterChGainTitleDetail_2; // 通道增益校准2标题/详情
    private TextView tipsFactoryAdjust; // 校准进度提示文本
    private DialogOk dialogOk; // 确认对话框
    private EditText editText; // 密码输入框



    private String calibration, unCalibration; // "已校准"/"未校准"字符串缓存

    private Button btnTest; // 测试按钮



    private List<TextView>tvTitles = new ArrayList<>(); // 校准项标题文本列表
    private List<TextView>tvDetails = new ArrayList<>(); // 校准项详情文本列表

    private CabteRegister cabteRegister; // 校准寄存器实例，管理校准状态

    /**
     * 创建Fragment视图，加载布局文件
     *
     * @param inflater           布局填充器
     * @param container          父视图容器
     * @param savedInstanceState 保存的实例状态
     * @return 填充后的视图
     */
    @Nullable // 标注返回值可为空
    @Override // 覆写Fragment的onCreateView方法
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) { // 创建视图
        return inflater.inflate(R.layout.layout_factorycalibration, container, false); // 加载出厂校准布局文件
    }

    /**
     * 视图创建完成后的初始化操作
     *
     * @param view               Fragment的根视图
     * @param savedInstanceState 保存的实例状态
     */
    @Override // 覆写Fragment的onViewCreated方法
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) { // 视图创建完成回调
        this.context = getActivity(); // 获取Activity上下文
        cabteRegister = CabteRegister.getInstance(); // 获取校准寄存器单例
        initView(view); // 初始化视图控件
        initControl(); // 初始化事件监听和消息订阅
        diaplayFpga(); // 启动FPGA信息定时刷新

    }
    /**
     * 获取所有有效的以太网网卡IP地址
     *
     * @return 网卡名称和IP地址的字符串数组，无有效网卡时返回null
     */
    public static String[] getAllNetInterface() { // 获取所有有效以太网网卡
        ArrayList<String> availableInterface = new ArrayList<>(); // 存储有效网卡信息的列表
        String[] interfaces = null; // 返回结果数组
        try { // 开始异常捕获
            //获取本地设备的所有网络接口
            Enumeration nis = NetworkInterface.getNetworkInterfaces(); // 枚举所有网络接口
            InetAddress ia = null; // IP地址引用
            while (nis.hasMoreElements()) { // 遍历所有网络接口
                NetworkInterface ni = (NetworkInterface) nis.nextElement(); // 获取当前网络接口
                Enumeration<InetAddress> ias = ni.getInetAddresses(); // 枚举当前接口的所有IP地址
                while (ias.hasMoreElements()) { // 遍历所有IP地址
                    ia = ias.nextElement(); // 获取当前IP地址
                    if (ia instanceof Inet6Address) { // 是IPv6地址
                        continue;// skip ipv6 // 跳过IPv6地址
                    }
                    String ip = ia.getHostAddress(); // 获取IP地址字符串
                    // 过滤掉127段的ip地址
                    if (!"127.0.0.1".equals(ip)) { // 非回环地址
                        if (ni.getName().substring(0, 3).equals("eth")) {//筛选出以太网 // 网卡名以"eth"开头，为以太网卡
                            availableInterface.add(ni.getName() + ":" + ip); // 添加网卡名和IP到列表
                        }
                    }
                }
            }

        } catch (SocketException e) { // 捕获Socket异常
            e.printStackTrace(); // 打印异常堆栈
        }
        int size = availableInterface.size(); // 获取有效网卡数量
        if (size > 0) { // 有有效网卡
            interfaces = new String[size]; // 创建结果数组
            for (int i = 0; i < size; i++) { // 遍历列表
                interfaces[i] = availableInterface.get(i); // 填充结果数组
            }
        }
        return interfaces; // 返回结果数组
    }

    /**
     * 初始化视图控件，绑定布局中的UI元素和事件监听器
     *
     * @param view Fragment的根视图
     */
    private void initView(View view) { // 初始化视图控件
        editText = (EditText) view.findViewById(R.id.editText1); // 查找密码输入框
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() { // 设置焦点变化监听器
            @Override // 覆写onFocusChange方法
            public void onFocusChange(View v, boolean hasFocus) { // 焦点变化回调
                if(!hasFocus){ // 失去焦点
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE); // 获取输入法管理器
                    imm.hideSoftInputFromWindow(editText.getWindowToken(), 0); // 隐藏软键盘
                }
            }
        });

        btnReset = (Button) view.findViewById(R.id.btnReset); // 查找重置按钮

        btnTop = (Button) view.findViewById(R.id.topBtn); // 查找启动校准按钮

        tvNetip = (TextView)view.findViewById(R.id.netip); // 查找网络IP文本
        tvTopZeroTitle = (TextView) view.findViewById(R.id.topZeroTitle); // 查找零点校准标题
        tvTopZeroTitleDetail = (TextView) view.findViewById(R.id.topZeroDetail); // 查找零点校准详情
        tvCenterChGainTitle = (TextView) view.findViewById(R.id.centerChGainTitle); // 查找通道增益标题
        tvCenterChGainTitleDetail = (TextView) view.findViewById(R.id.centerChGainTitleDetail); // 查找通道增益详情

        tvCenterChOffsetTitle = (TextView) view.findViewById(R.id.centerChOffsetTitle); // 查找偏移量标题
        tvCenterChOffsetTitleDetail = (TextView) view.findViewById(R.id.centerChOffsetTitleDetail); // 查找偏移量详情

        tvCenterChCapTitle = (TextView) view.findViewById(R.id.centerChCapTitle); // 查找电容标题
        tvCenterChCapTitleDetail = (TextView) view.findViewById(R.id.centerChCapTitleDetail); // 查找电容详情

        tvCenterChGainTitle_2 = (TextView) view.findViewById(R.id.centerChGainTitle_2); // 查找通道增益2标题
        tvCenterChGainTitleDetail_2 = (TextView) view.findViewById(R.id.centerChGainTitleDetail_2); // 查找通道增益2详情

        tipsFactoryAdjust = (TextView) getActivity().findViewById(R.id.tipsFactoryAdjust); // 查找校准进度提示文本

        fpgatv = (TextView)view.findViewById(R.id.fpgatv); // 查找FPGA版本信息文本
        tvCabteTime = view.findViewById(R.id.tvCabteTime); // 查找校准时间文本

        btnDeliveryDate = view.findViewById(R.id.btnDeliveryDate); // 查找设置出厂日期按钮
        btnDeliveryDate.setOnClickListener(new View.OnClickListener() { // 设置点击监听器
            @Override // 覆写onClick方法
            public void onClick(View v) { // 点击回调
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd"); // 创建日期格式化器
                Date date = new Date(); // 获取当前日期
                PropertyManage propertyManage = PropertyManage.getInstance(); // 获取属性管理器单例
                propertyManage.getProperty().setDeliveryDate(simpleDateFormat.format(date)); // 设置出厂日期属性
                propertyManage.commit(); // 提交属性变更
                Toast.makeText(context,"设置后重启",Toast.LENGTH_LONG).show(); // 提示需要重启

            }
        });

        btnBandWidth = view.findViewById(R.id.btnBandWidth); // 查找设置带宽按钮
        btnBandWidth.setOnClickListener(new View.OnClickListener() { // 设置点击监听器
            @Override // 覆写onClick方法
            public void onClick(View v) { // 点击回调
                PropertyManage propertyManage = PropertyManage.getInstance(); // 获取属性管理器单例
                propertyManage.getProperty().setBandWidth((int)(Channel.MAX_BANDWIDTH/1000000)); // 设置最大带宽属性（Hz转MHz）
                propertyManage.commit(); // 提交属性变更
                Toast.makeText(context,"设置后重启",Toast.LENGTH_LONG).show(); // 提示需要重启
            }
        });


        btnTest = view.findViewById(R.id.btnTest); // 查找测试按钮
        btnTest.setOnClickListener(new View.OnClickListener() { // 设置点击监听器
            @Override // 覆写onClick方法
            public void onClick(View v) { // 点击回调
//                FPGACommand fpgaCommand = FPGACommand.getInstance(); // 被注释掉的调试代码
//                fpgaCommand.resetBak(0); // 被注释掉的调试代码
//                fpgaCommand.cmdFpgaDotMatrix(1); // 被注释掉的调试代码
            }
        });





        dialogOk = (DialogOk) getActivity().findViewById(R.id.dialogOk); // 查找确认对话框
        btnReset.setOnClickListener(onClickListener); // 设置重置按钮点击监听器
        btnTop.setOnClickListener(onClickListener); // 设置启动校准按钮点击监听器


        String[] strArray = getAllNetInterface(); // 获取所有以太网网卡IP
        if(strArray != null) { // 有有效网卡
            tvNetip.setText(String.join(",", strArray)); // 显示网卡IP，逗号分隔
        }





        tvTitles.addAll(Arrays.asList(tvTopZeroTitle,tvCenterChGainTitle,tvCenterChOffsetTitle,tvCenterChCapTitle,tvCenterChGainTitle_2)); // 添加所有校准项标题到列表
        tvDetails.addAll(Arrays.asList(tvTopZeroTitleDetail, // 添加所有校准项详情到列表
                tvCenterChGainTitleDetail,tvCenterChOffsetTitleDetail,tvCenterChCapTitleDetail,tvCenterChGainTitleDetail_2));

        onRefresCalibrationState(); // 刷新校准状态显示
    }


    String toastStr = ""; // Toast字符串缓存（未使用）
    /**
     * 线程休眠辅助方法
     *
     * @param ms 休眠时间（毫秒）
     */
    private void ms_sleep(long ms){ // 线程休眠
        try { // 开始异常捕获
            Thread.sleep(ms); // 休眠指定毫秒数
        } catch (InterruptedException e) { // 捕获中断异常
            throw new RuntimeException(e); // 转换为运行时异常抛出
        }
    }

    /**
     * 刷新校准状态显示，遍历所有校准项并更新标题和详情文本
     */
    private void onRefresCalibrationState(){ // 刷新校准状态显示
        List<String> list = cabteRegister.getCalibrationItems(); // 获取所有校准项名称列表
        for(int i=0;i<list.size();i++){ // 遍历所有校准项
            String str  = "未校准"; // 默认状态为未校准
            StringBuilder sb = new StringBuilder(); // 校准详情字符串构建器
            if(cabteRegister.getCalibrationItemState(i,sb)){ // 检查校准项状态，已校准返回true
                str = "已校准"; // 更新状态为已校准
            }
            tvTitles.get(i).setText(list.get(i) + " " + str); // 设置标题文本（名称+状态）
            tvDetails.get(i).setText(sb); // 设置详情文本
        }
    }




    /**
     * 初始化消息订阅和事件观察者
     */
    private void initControl() { // 初始化事件控制
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache); // 订阅缓存加载消息
        EventFactory.addEventObserver(EventFactory.EVENT_FACTOR_CALIBRATE_BEGIN, eventUIObserver); // 注册校准开始事件观察者
        EventFactory.addEventObserver(EventFactory.EVENT_FACTOR_CALIBRATE_ITEM_START, eventUIObserver); // 注册校准项开始事件观察者
        EventFactory.addEventObserver(EventFactory.EVENT_FACTOR_CALIBRATE_END, eventUIObserver); // 注册校准结束事件观察者
        EventFactory.addEventObserver(EventFactory.EVENT_FPGA_LOAD_OK,eventUIObserver); // 注册FPGA加载完成事件观察者
    }

    /**
     * 从缓存恢复UI状态，刷新校准状态显示
     */
    private void setCache() { // 从缓存恢复状态

        onRefresCalibrationState(); // 刷新校准状态显示
    }

    /**
     * 根据布尔值返回"已校准"或"未校准"字符串
     *
     * @param b true返回"已校准"，false返回"未校准"
     * @return 校准状态字符串
     */
    private String getString(boolean b) { // 获取校准状态字符串
        if (StrUtil.isEmpty(calibration)) { // "已校准"字符串未缓存
            calibration = context.getString(R.string.calibration); // 从资源加载"已校准"
        }
        if (StrUtil.isEmpty(unCalibration)) { // "未校准"字符串未缓存
            unCalibration = context.getString(R.string.unCalibration); // 从资源加载"未校准"
        }
        return b ? calibration : unCalibration; // 根据布尔值返回对应字符串
    }

    /**
     * 缓存加载消息消费者，当收到缓存加载事件时恢复UI状态
     */
    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() { // 缓存加载消息消费者
        @Override // 覆写Consumer的accept方法
        public void accept(@NonNull LoadCache loadCache) throws Exception { // 处理缓存加载消息
            setCache(); // 从缓存恢复UI状态
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_TopLayoutFactoryCalibration, true); // 标记本页面缓存已加载完成
        }
    };



    /**
     * 按钮点击监听器，处理启动校准和重置校准数据的点击事件
     */
    private View.OnClickListener onClickListener = new View.OnClickListener() { // 按钮点击监听器
        @Override // 覆写onClick方法
        public void onClick(View v) { // 点击回调
             if (v.getId() == btnTop.getId()) { // 启动校准按钮
                 FactorCalibrate.getInstance().begin_upPard(); // 启动出厂校准流程
                 Logger.i("calibration:click"); // 记录日志
                 RxBus.getInstance().post(RxEnum.MAIN_SLIP_FROM_OTHER, new MainMsgSlip(MainViewGroup.TOPSLIP, false)); // 关闭顶部滑动面板
            }else if(v.getId() == btnReset.getId()){ // 重置校准数据按钮
                 String userInput = editText.getText().toString(); // 获取用户输入的密码
                 // 处理用户输入
                 if(userInput.equalsIgnoreCase("micsig_123")) { // 密码正确
                     editText.setText(""); // 清空密码输入框
                     calibrationReset(); // 执行校准数据重置
                     Toast.makeText(context,"清空校准数据完成",Toast.LENGTH_LONG).show(); // 提示重置完成
                 }else{ // 密码错误
                     Toast.makeText(context,"密码错误",Toast.LENGTH_LONG).show(); // 提示密码错误
                 }
            }


        }
    };

    /**
     * 执行校准数据重置操作
     * 重置校准寄存器默认值、保存出厂校准参数、清除校准状态
     */
    private void calibrationReset(){ // 校准数据重置
        CabteRegister cabteRegister = CabteRegister.getInstance(); // 获取校准寄存器单例
        FactorCalibrate factorCalibrate = FactorCalibrate.getInstance(); // 获取出厂校准单例
        cabteRegister.rstDefaultVal(); // 重置校准寄存器为默认值
        cabteRegister.saveFactoryCalibrateParam(); // 保存出厂校准参数
        cabteRegister.clearCalibrationState(); // 清除校准状态
        cabteRegister.saveCalibrationStateParam(); // 保存校准状态参数
        editText.postDelayed(new Runnable() { // 延迟10秒后刷新UI
            @Override // 覆写run方法
            public void run() { // 延迟执行
                setCache(); // 刷新校准状态显示
            }
        },10000); // 延迟10秒
    }




    /**
     * 事件UI观察者，处理校准开始、校准项开始、校准结束和FPGA加载完成事件
     */
    private EventUIObserver eventUIObserver = new EventUIObserver() { // 事件UI观察者
        @Override // 覆写update方法
        public void update(Object data) { // 事件更新回调
            EventBase eventBase = (EventBase) data; // 转换为事件基类
            if (eventBase.getId() == EventFactory.EVENT_FACTOR_CALIBRATE_BEGIN) { // 校准开始事件
                Logger.i("calibration:begin"); // 记录日志
                Command.get().getMeasure().factoryCalibration(); // 通过命令中间件执行出厂校准测量
                tipsFactoryAdjust.setVisibility(View.VISIBLE); // 显示校准进度提示
                //锁屏幕，锁键盘
                ScreenControls.getInstance().lockScreen(ScreenControls.LOCK_FACTORY_ADJUST); // 锁定屏幕和键盘

            } else if (eventBase.getId() == EventFactory.EVENT_FACTOR_CALIBRATE_END) { // 校准结束事件
                tipsFactoryAdjust.setVisibility(View.GONE); // 隐藏校准进度提示
                Logger.i("calibration:end"); // 记录日志
                //解锁屏幕，解锁键盘
                ScreenControls.getInstance().unLockScreen(ScreenControls.LOCK_FACTORY_ADJUST); // 解锁屏幕和键盘

                Bundle bundle = (Bundle) eventBase.getData(); // 获取事件数据Bundle
                int err = bundle.getInt(FactorCalibrate.ERROR_KEY); // 获取错误码
                boolean b = err == 0; // 错误码为0表示成功
                Logger.i("FactoryCalibration:" + (b ? "success" : "failed")); // 记录校准结果日志
                int count = FactorCalibrate.getJIaoZhunCnt(); // 获取校准项数量
                if (count > 0) { // 有校准项
                    String result = b ? "成功" : ("失败,\n错误代码:" + err); // 构建结果字符串
                    String dialogMsg = "此次校准("; // 对话框消息前缀
                    for (int i = 0; i < count; i++) { // 遍历所有校准项
                        int calibrationId = FactorCalibrate.getCablicationID(i); // 获取校准项ID
                        int id = FactorCalibrate.getCablicationID(i); // 获取校准项ID（重复获取）
                        ArrayList<String> stringList = // 获取校准项详细数据列表
                                bundle.getStringArrayList(CalibrateService.getInstance().getCalibrate(id).getTAG()); // 通过校准服务获取TAG对应的详细数据
                        Logger.i("<<<========================================================================================"); // 日志分隔线
                        if (stringList != null) { // 详细数据不为空
                            for (String x : stringList // 遍历详细数据
                            ) {
                                Logger.i(x); // 记录每条详细数据
                            }
                        }
                        Logger.i("========================================================================================>>>"); // 日志分隔线
                        String symbol = i != 0 ? "、" : ""; // 非首项添加顿号分隔
                        switch (calibrationId) { // 根据校准项ID确定名称
                            case CalibrateService.ZERO_CALIBRATE: // 零点校准

                                dialogMsg += symbol + "零点校准"; // 添加零点校准名称
                                break; // 结束零点校准处理
                            case CalibrateService.CHCOEF_CALIBRATE: { // 偏移量系数校准

                                dialogMsg += symbol + "偏移量系数校准"; // 添加偏移量系数校准名称
                            }
                                break; // 结束偏移量系数校准处理
                            case CalibrateService.CHGAIN_CALIBRATE: { // 通道增益校准
                                //档位很多，需要全部都校准成功才显示"已校准"

                                dialogMsg += symbol + "通道增益校准"; // 添加通道增益校准名称
                            }
                                break; // 结束通道增益校准处理
                            case CalibrateService.CHGAIN_CALIBRATE_EX: { // 扩展通道增益校准

                                dialogMsg += symbol + "通道增益校准"; // 添加通道增益校准名称
                            }
                            break; // 结束扩展通道增益校准处理
                            case CalibrateService.CHCAP_CALIBRATE: { // 电容校准

                                dialogMsg += symbol + "电容校准"; // 添加电容校准名称
                            }
                                break; // 结束电容校准处理
                        }
                    }
                    onRefresCalibrationState(); // 刷新校准状态显示
                    dialogMsg += ")" + result; // 拼接结果到对话框消息
                    dialogOk.setData(dialogMsg, null, null); // 设置对话框内容
                    CabteRegister.getInstance().saveCalibrationStateParam(); // 保存校准状态参数
                }
            } else if (eventBase.getId() == EventFactory.EVENT_FACTOR_CALIBRATE_ITEM_START) { // 校准项开始事件
                int index = (int) eventBase.getData(); // 获取校准项索引
                Logger.i("calibration:" + index); // 记录校准项索引日志
                String s = ""; // 校准项名称
                switch (index) { // 根据索引确定名称
                    case CalibrateService.ZERO_CALIBRATE: // 零点校准
                        s = "零点校准"; // 设置名称
                        break; // 结束
                    case CalibrateService.CHCOEF_CALIBRATE: // 偏移量系数校准
                        s = "偏移量系数校准"; // 设置名称
                        break; // 结束
                    case CalibrateService.CHGAIN_CALIBRATE_EX: // 扩展通道增益校准
                    case CalibrateService.CHGAIN_CALIBRATE: // 通道增益校准
                        s = "通道增益校准"; // 设置名称
                        break; // 结束
                    case CalibrateService.CHCAP_CALIBRATE: // 电容校准
                        s = "电容校准"; // 设置名称
                        break; // 结束
                }
                tipsFactoryAdjust.setText(s + "正在进行中。。。"); // 更新校准进度提示文本
            } else if (eventBase.getId() == EventFactory.EVENT_FPGA_LOAD_OK){ // FPGA加载完成事件

            }
        }
    };

    /**
     * 定时刷新FPGA版本信息、校准时间和网络IP
     * 每3秒刷新一次，通过postDelayed实现循环
     */
    private void diaplayFpga(){ // 定时刷新FPGA信息
        fpgatv.postDelayed(new Runnable() { // 延迟3秒后执行
            @Override // 覆写run方法
            public void run() { // 延迟执行
                fpgatv.setText("fpga ver:" + Scope.fpgaVer // 设置FPGA版本号
                        + ",\nT:" + Scope.fpgaTemperature1 + ","  + Scope.fpgaTemperature2 + "°C" // 设置FPGA温度
                        + ",\n" + Scope.fanSpeed[0] + "," + Scope.fanSpeed[1] + "," + Scope.fanSpeed[2] + "," + Scope.fanSpeed[3] // 设置风扇转速
                        );

                tvCabteTime.setText("time:" + CabteRegister.getInstance().getCabteTime()); // 设置校准时间

                String[] strArray = getAllNetInterface(); // 获取所有以太网网卡IP
                if(strArray != null) { // 有有效网卡
                    tvNetip.setText(String.join(",", strArray)); // 更新网络IP显示
                }

                diaplayFpga(); // 递归调用，实现定时循环刷新
            }
        },3000); // 延迟3秒
    }



}
