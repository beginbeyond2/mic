package com.micsig.tbook.tbookscope.main.maincenter; // 定义包路径,位于主界面中心区域的测试调试模块

/*
 * ========================================================================================
 *                                                                                  *
 *                     MainLayoutCenterTestJZ                                            *
 *                         主界面中心JZ型号测试调试布局                                   *
 *                                                                                  *
 * ========================================================================================
 * 
 * 【模块定位】
 *   位于main.maincenter包中,作为示波器JZ型号硬件调试功能的UI布局容器,
 *   负责提供AD(模数转换器)增益和偏移参数的调试界面,是硬件调试工具的可视化交互界面
 * 
 * 【核心职责】
 *   1. 提供通道选择功能(支持8个通道)
 *   2. 提供AD增益/偏移模式切换功能
 *   3. 提供AD增益和偏移参数的实时调节(通过SeekBar滑动条)
 *   4. 提供PGA(可编程增益放大器)参数调节功能
 *   5. 支持参数的保存和加载(使用SharedPreferences持久化)
 *   6. 支持可拖拽移动的悬浮窗口功能
 *   7. 监听通道状态变化事件并自动更新UI显示
 * 
 * 【架构设计】
 *   采用自定义RelativeLayout布局容器模式,内部封装多个SeekBar调节控件和RadioButton选择控件。
 *   使用SharedPreferences进行参数持久化存储,支持保存和加载用户调试参数。
 *   通过EventFactory事件工厂监听通道状态变化(阻抗类型、垂直档位、通道开关等),自动更新UI。
 *   实现触摸事件拦截和处理,支持整个窗口的拖拽移动功能。
 *   采用直接FPGA寄存器写入方式,实时调整AD增益和偏移参数。
 * 
 * 【数据流向】
 *   用户操作(滑动SeekBar/点击RadioButton) → FPGACommand(FPGA寄存器写入)
 *   → AD芯片参数更新 → 硬件响应 → 采样数据变化
 *   用户点击保存按钮 → SharedPreferences持久化存储 → 下次启动加载恢复参数
 *   通道状态变化事件 → EventFactory通知 → EventUIObserver观察者 → UI自动更新
 * 
 * 【依赖关系】
 *   依赖: FPGACommand(FPGA命令)、CabteRegister(校准寄存器)、ChannelFactory(通道工厂)
 *         SharedPreferences(参数持久化)、EventFactory(事件工厂)、Scope(示波器实例)
 *   被依赖: 硬件调试相关功能模块、测试人员操作界面
 * 
 * 【使用场景】
 *   1. 硬件调试人员在生产测试时需要调整AD增益和偏移参数
 *   2. 校准过程中需要微调各个通道的增益和偏移校准值
 *   3. 故障诊断时需要检查和调整PGA增益参数
 *   4. 需要保存调试参数以便下次快速恢复设置
 *   5. 调试窗口需要拖拽移动到屏幕合适位置
 */

import static android.content.Context.MODE_PRIVATE; // 导入SharedPreferences模式常量

import android.annotation.SuppressLint; // 导入抑制lint警告注解
import android.content.Context; // 导入Android上下文类
import android.content.SharedPreferences; // 导入SharedPreferences持久化存储类
import android.graphics.Color; // 导入颜色类
import android.graphics.Rect; // 导入矩形类
import android.graphics.drawable.GradientDrawable; // 导入渐变图形类
import android.os.Build; // 导入系统版本类
import android.util.AttributeSet; // 导入属性集类
import android.util.Log; // 导入日志类
import android.view.MotionEvent; // 导入触摸事件类
import android.view.View; // 导入视图基类
import android.widget.Button; // 导入按钮控件
import android.widget.CheckBox; // 导入复选框控件
import android.widget.RadioButton; // 导入单选按钮控件
import android.widget.RadioGroup; // 导入单选按钮组控件
import android.widget.RelativeLayout; // 导入相对布局类
import android.widget.SeekBar; // 导入滑动条控件
import android.widget.TextView; // 导入文本视图控件
import android.widget.Toast; // 导入Toast提示类

import com.micsig.tbook.hardware.HardwareProduct; // 导入硬件产品类
import com.micsig.tbook.scope.Action.UiMessage; // 导入UI消息类
import com.micsig.tbook.scope.Calibrate.CabteRegister; // 导入校准寄存器类
import com.micsig.tbook.scope.Event.EventBase; // 导入事件基类
import com.micsig.tbook.scope.Event.EventFactory; // 导入事件工厂类
import com.micsig.tbook.scope.Event.EventUIObserver; // 导入UI事件观察者类
import com.micsig.tbook.scope.Sample.Sample; // 导入采样类
import com.micsig.tbook.scope.Scope; // 导入示波器类
import com.micsig.tbook.scope.ScopeMessage; // 导入示波器消息类
import com.micsig.tbook.scope.channel.Channel; // 导入通道类
import com.micsig.tbook.scope.channel.ChannelFactory; // 导入通道工厂类
import com.micsig.tbook.scope.fpga.FPGACommand; // 导入FPGA命令类
import com.micsig.tbook.scope.fpga.FPGA_CHAZHI_COEF; // 导入FPGA插值系数类
import com.micsig.tbook.scope.probe.BaseProbe; // 导入探头基类
import com.micsig.tbook.scope.probe.ProbeFactory; // 导入探头工厂类
import com.micsig.tbook.scope.surface.XDmaDevManage; // 导入DMA设备管理类
import com.micsig.tbook.tbookscope.GlobalVar; // 导入全局变量类
import com.micsig.tbook.tbookscope.R; // 导入资源类


/**
 * 主界面中心JZ型号测试调试布局类
 * 
 * 用于JZ型号示波器的硬件调试功能,提供AD增益、偏移和PGA参数的实时调节界面。
 * 支持通道选择、参数调节、保存加载和窗口拖拽移动等交互功能。
 * 主要面向硬件调试人员和生产测试场景使用。
 * 
 * @author micsig
 * @version 1.0
 */
public class MainLayoutCenterTestJZ extends RelativeLayout {

    /** 日志标签,用于调试日志输出 */
    private static final String TAG = "MainLayoutCenterTest";

    /** Android上下文对象 */
    private Context context;

    /** AD参数显示文本视图: A组参数 */
    TextView tvadfsA, tvadfsB, tvadfsC, tvadfsD, tvPGA; // AD参数显示文本视图控件数组(A/B/C/D组和PGA)

    /** AD参数调节滑动条: A/B/C/D组和PGA */
    SeekBar adfsSeekBarA, adfsSeekBarB, adfsSeekBarC, adfsSeekBarD, pgaseekBarA; // AD参数调节滑动条控件数组

    /** AD调试模式复选框 */
    CheckBox cbAdDebug; // AD调试模式复选框控件

    /** 通道选择单选按钮: 支持8个通道 */
    private RadioButton rbCh1, rbCh2, rbCh3, rbCh4, rbCh5, rbCh6, rbCh7, rbCh8; // 通道选择单选按钮控件数组(1-8通道)

    /** AD调节模式单选按钮: 增益模式和偏移模式 */
    private RadioButton rbAdGain, rbAdOffset; // AD调节模式单选按钮控件(增益/偏移)

    /** SharedPreferences持久化存储对象,用于保存调试参数 */
    SharedPreferences sharedPreferences; // SharedPreferences持久化存储对象

    /** 保存按钮和加载按钮 */
    Button btnSave, btnLoad; // 保存和加载按钮控件

    /**
     * 构造方法(单参数)
     * 
     * @param context Android上下文对象
     */
    public MainLayoutCenterTestJZ(Context context) {
        this(context, null); // 调用双参数构造方法
    }

    /**
     * 构造方法(双参数)
     * 
     * @param context Android上下文对象
     * @param attrs 属性集对象
     */
    public MainLayoutCenterTestJZ(Context context, AttributeSet attrs) {
        this(context, attrs, 0); // 调用三参数构造方法
    }

    /**
     * 构造方法(三参数)
     * 
     * 完成布局初始化,设置背景样式,初始化SharedPreferences持久化存储。
     * 
     * @param context Android上下文对象
     * @param attrs 属性集对象
     * @param defStyleAttr 默认样式属性
     */
    public MainLayoutCenterTestJZ(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr); // 调用父类构造方法
        this.context = context; // 保存上下文对象
        initView(attrs, defStyleAttr); // 初始化视图组件
        GradientDrawable shape = new GradientDrawable(); // 创建渐变图形对象用于设置背景
        shape.setColor(Color.argb(0.5f, 0.f, 0.f, 0.f)); // 设置背景颜色为半透明黑色
        shape.setStroke(2, Color.GRAY); // 设置边框宽度为2像素,颜色为灰色
        shape.setCornerRadius(4); // 设置圆角大小为4像素

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // 判断系统版本是否大于等于Android Q(10.0)
            shape.setPadding(1, 1, 1, 1); // 设置图形内边距
        }

        setBackground(shape); // 将渐变图形设置为背景

        sharedPreferences = context.getSharedPreferences("MainLayoutCenterTestJZ", MODE_PRIVATE); // 初始化SharedPreferences持久化存储,文件名为MainLayoutCenterTestJZ
    }

    /**
     * 初始化视图组件
     * 
     * 加载布局文件,初始化各种UI控件(按钮、单选按钮、文本视图、滑动条),
     * 设置点击监听和滑动条监听,注册事件观察者。
     * 
     * @param attrs 属性集对象
     * @param defStyleAttr 默认样式属性
     */
    private void initView(AttributeSet attrs, int defStyleAttr) {
        View.inflate(context, R.layout.layout_maincenter_test_jz, this); // 加载布局文件到当前视图

        btnSave = findViewById(R.id.btnSavejz); // 获取保存按钮控件
        btnLoad = findViewById(R.id.btnLoadjz); // 获取加载按钮控件
        btnSave.setOnClickListener(new OnClickListener() { // 设置保存按钮点击监听
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedPreferences.edit(); // 获取SharedPreferences编辑器对象
                SeekBar[] sb = {adfsSeekBarA, adfsSeekBarB, adfsSeekBarC, adfsSeekBarD}; // 创建滑动条数组
                String pre_key = rbAdOffset.isChecked() ? "adOffset" : "adGain"; // 根据当前模式确定存储键前缀(偏移模式为adOffset,增益模式为adGain)
                for (int i = 0; i < sb.length; i++) { // 遍历滑动条数组
                    int val = sb[i].getProgress(); // 获取滑动条当前进度值
                    editor.putInt(pre_key + i, val); // 将进度值保存到SharedPreferences,键为pre_key+i
                }
                editor.apply(); // 应用编辑器修改,异步保存数据
            }
        });

        btnLoad.setOnClickListener(new OnClickListener() { // 设置加载按钮点击监听
            @Override
            public void onClick(View v) {
                SeekBar[] sb = {adfsSeekBarA, adfsSeekBarB, adfsSeekBarC, adfsSeekBarD}; // 创建滑动条数组
                String pre_key = rbAdOffset.isChecked() ? "adOffset" : "adGain"; // 根据当前模式确定存储键前缀(偏移模式为adOffset,增益模式为adGain)
                for (int i = 0; i < sb.length; i++) { // 遍历滑动条数组
                    int val = sharedPreferences.getInt(pre_key + i, -1); // 从SharedPreferences读取保存的进度值,默认值为-1
                    if (val >= 0) { // 判断读取的值是否有效(大于等于0)
                        sb[i].setProgress(val); // 设置滑动条进度为读取的值
                    }
                }
            }
        });

        rbCh1 = (RadioButton) findViewById(R.id.testChannelsCh1jz); // 获取通道1单选按钮控件
        rbCh2 = (RadioButton) findViewById(R.id.testChannelsCh2jz); // 获取通道2单选按钮控件
        rbCh3 = (RadioButton) findViewById(R.id.testChannelsCh3jz); // 获取通道3单选按钮控件
        rbCh4 = (RadioButton) findViewById(R.id.testChannelsCh4jz); // 获取通道4单选按钮控件
        rbCh5 = (RadioButton) findViewById(R.id.testChannelsCh5jz); // 获取通道5单选按钮控件
        rbCh6 = (RadioButton) findViewById(R.id.testChannelsCh6jz); // 获取通道6单选按钮控件
        rbCh7 = (RadioButton) findViewById(R.id.testChannelsCh7jz); // 获取通道7单选按钮控件
        rbCh8 = (RadioButton) findViewById(R.id.testChannelsCh8jz); // 获取通道8单选按钮控件

        rbAdGain = (RadioButton) findViewById(R.id.adGainjz); // 获取AD增益模式单选按钮控件
        rbAdOffset = (RadioButton) findViewById(R.id.adOffsetjz); // 获取AD偏移模式单选按钮控件

        cbAdDebug = findViewById(R.id.adDebug); // 获取AD调试模式复选框控件

        rbCh1.setOnClickListener(onClickListener); // 设置通道1单选按钮点击监听
        rbCh2.setOnClickListener(onClickListener); // 设置通道2单选按钮点击监听
        rbCh3.setOnClickListener(onClickListener); // 设置通道3单选按钮点击监听
        rbCh4.setOnClickListener(onClickListener); // 设置通道4单选按钮点击监听
        rbCh5.setOnClickListener(onClickListener); // 设置通道5单选按钮点击监听
        rbCh6.setOnClickListener(onClickListener); // 设置通道6单选按钮点击监听
        rbCh7.setOnClickListener(onClickListener); // 设置通道7单选按钮点击监听
        rbCh8.setOnClickListener(onClickListener); // 设置通道8单选按钮点击监听

        rbAdGain.setOnClickListener(onClickListener); // 设置AD增益模式单选按钮点击监听
        rbAdOffset.setOnClickListener(onClickListener); // 设置AD偏移模式单选按钮点击监听

        tvadfsA = findViewById(R.id.tvadfsA); // 获取AD参数A组显示文本视图控件
        tvadfsB = findViewById(R.id.tvadfsB); // 获取AD参数B组显示文本视图控件
        tvadfsC = findViewById(R.id.tvadfsC); // 获取AD参数C组显示文本视图控件
        tvadfsD = findViewById(R.id.tvadfsD); // 获取AD参数D组显示文本视图控件
        tvPGA = findViewById(R.id.tvPGA); // 获取PGA参数显示文本视图控件
        adfsSeekBarA = findViewById(R.id.adfsseekBarA); // 获取AD参数A组滑动条控件
        adfsSeekBarB = findViewById(R.id.adfsseekBarB); // 获取AD参数B组滑动条控件
        adfsSeekBarC = findViewById(R.id.adfsseekBarC); // 获取AD参数C组滑动条控件
        adfsSeekBarD = findViewById(R.id.adfsseekBarD); // 获取AD参数D组滑动条控件
        pgaseekBarA = findViewById(R.id.pgaseekBarA); // 获取PGA参数滑动条控件

        adfsSeekBarA.setOnSeekBarChangeListener(onSeekBarChangeListener); // 设置AD参数A组滑动条进度变化监听
        adfsSeekBarB.setOnSeekBarChangeListener(onSeekBarChangeListener); // 设置AD参数B组滑动条进度变化监听
        adfsSeekBarC.setOnSeekBarChangeListener(onSeekBarChangeListener); // 设置AD参数C组滑动条进度变化监听
        adfsSeekBarD.setOnSeekBarChangeListener(onSeekBarChangeListener); // 设置AD参数D组滑动条进度变化监听
        pgaseekBarA.setOnSeekBarChangeListener(onSeekBarChangeListener); // 设置PGA参数滑动条进度变化监听

        setClickable(true); // 设置视图为可点击状态
        updateView(); // 更新视图显示
        EventFactory.addEventObserver(EventFactory.EVENT_CHANNEL_RESISTANCETYPE, eventUIObserver); // 注册通道阻抗类型变化事件观察者
        EventFactory.addEventObserver(EventFactory.EVENT_CHANNEL_VSCALE, eventUIObserver); // 注册通道垂直档位变化事件观察者
        EventFactory.addEventObserver(EventFactory.EVENT_CHANNEL_VSCALE_USER, eventUIObserver); // 注册用户垂直档位变化事件观察者
        EventFactory.addEventObserver(EventFactory.EVENT_CHANNEL_OPEN, eventUIObserver); // 注册通道打开事件观察者
        EventFactory.addEventObserver(EventFactory.EVENT_CHANNEL_CLOSE, eventUIObserver); // 注册通道关闭事件观察者
    }

    /**
     * UI事件观察者
     * 
     * 监听通道状态变化事件(阻抗类型、垂直档位、通道开关等),延迟100毫秒后自动更新UI显示。
     */
    private EventUIObserver eventUIObserver = new EventUIObserver() {
        /**
         * 观察者更新回调方法
         * 
         * @param data 事件数据对象
         */
        @Override
        public void update(Object data) {
            btnSave.postDelayed(new Runnable() { // 延迟100毫秒后在UI线程中执行任务
                @Override
                public void run() {
                    updateView(); // 更新视图显示
                }
            }, 100); // 延迟时间为100毫秒
        }
    };

    /**
     * 更新视图显示
     * 
     * 根据当前选中的通道和AD调节模式,更新滑动条范围和初始值,并显示对应的参数名称和值。
     */
    private void updateView() {
        Channel channel = ChannelFactory.getDynamicChannel(getChIdx()); // 根据当前选中的通道索引获取通道对象
        if (channel != null) { // 判断通道对象是否为null
            TextView[] tv = {tvadfsA, tvadfsB, tvadfsC, tvadfsD}; // 创建文本视图数组
            SeekBar[] sb = {adfsSeekBarA, adfsSeekBarB, adfsSeekBarC, adfsSeekBarD}; // 创建滑动条数组

            String[] tt = {"0x8C", "0x8A", "0x94", "0x92"}; // 定义偏移模式下的寄存器地址标签数组
            if (rbAdGain.isChecked()) { // 判断是否为增益模式
                tt[0] = "0x32"; // 修改第1个寄存器地址标签为增益模式地址
                tt[1] = "0x30"; // 修改第2个寄存器地址标签为增益模式地址
            }
            for (int i = 0; i < tv.length; i++) { // 遍历文本视图和滑动条数组
                int val = rbAdGain.isChecked() ? 0xA000 : 0; // 根据当前模式确定初始值(增益模式为0xA000,偏移模式为0)

                sb[i].setMin(0); // 设置滑动条最小值为0
                sb[i].setMax(rbAdGain.isChecked() ? 0xFFFF : 0x7FF); // 设置滑动条最大值(增益模式为0xFFFF,偏移模式为0x7FF)
                sb[i].setProgress(val); // 设置滑动条当前进度为初始值
                tv[i].setText(tt[i] + ":" + Integer.toHexString(val & 0xFFFF)); // 更新文本视图显示内容为寄存器地址和参数值(十六进制格式)
            }
        }
    }

    /**
     * 滑动条进度变化监听器
     * 
     * 监听AD参数滑动条和PGA滑动条的进度变化事件,实时将参数值写入FPGA寄存器,
     * 并更新对应的文本视图显示。
     */
    private SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        /**
         * 进度变化回调方法
         * 
         * 当滑动条进度发生变化时调用,根据滑动条ID判断是AD参数还是PGA参数,
         * 并将对应的参数值写入FPGA寄存器,更新文本视图显示。
         * 
         * @param seekBar 滑动条控件对象
         * @param progress 当前进度值
         * @param fromUser 是否来自用户操作
         */
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            Scope scope = Scope.getInstance(); // 获取示波器实例对象
            int cnt = scope.getChannelSampOnCnt() / 4; // 获取采样通道数量(除以4)
            Channel channel = ChannelFactory.getDynamicChannel(getChIdx()); // 根据当前选中的通道索引获取通道对象
            CabteRegister cabteRegister = CabteRegister.getInstance(); // 获取校准寄存器实例对象
            int adIdx = -1; // 初始化AD参数索引为-1(默认无效值)
            int chIdx = channel.getChId(); // 获取通道ID
            switch (seekBar.getId()) { // 根据滑动条ID判断是哪个参数滑动条
                default: // 默认情况
                case R.id.adfsseekBarA: // A组滑动条
                    adIdx = 0; // 设置AD参数索引为0(A组)
                    break;
                case R.id.adfsseekBarB: // B组滑动条
                    adIdx = 1; // 设置AD参数索引为1(B组)
                    break;
                case R.id.adfsseekBarC: // C组滑动条
                    adIdx = 2; // 设置AD参数索引为2(C组)
                    break;
                case R.id.adfsseekBarD: // D组滑动条
                    adIdx = 3; // 设置AD参数索引为3(D组)
                    break;
                case R.id.pgaseekBarA: // PGA滑动条
                    break; // 不设置AD参数索引,保持-1
            }

            if (adIdx >= 0) { // 判断AD参数索引是否有效(大于等于0)
                String[] tt = {"0x8C", "0x8A", "0x94", "0x92"}; // 定义偏移模式下的寄存器地址标签数组
                if (rbAdGain.isChecked()) { // 判断是否为增益模式
                    tt[0] = "0x32"; // 修改第1个寄存器地址标签为增益模式地址
                    tt[1] = "0x30"; // 修改第2个寄存器地址标签为增益模式地址
                    FPGACommand.getInstance().writeAD_gain(chIdx / 4, chIdx % 4 / 2, adIdx, progress & 0xFFFF); // 写入AD增益参数到FPGA寄存器
                } else { // 偏移模式
                    FPGACommand.getInstance().writeAD_offset(chIdx / 4, chIdx % 4 / 2, adIdx, progress & 0x7FF); // 写入AD偏移参数到FPGA寄存器
                }

                TextView[] tv = {tvadfsA, tvadfsB, tvadfsC, tvadfsD}; // 创建文本视图数组
                SeekBar[] sb = {adfsSeekBarA, adfsSeekBarB, adfsSeekBarC, adfsSeekBarD}; // 创建滑动条数组

                tv[adIdx].setText(tt[adIdx] + ":" + Integer.toHexString(sb[adIdx].getProgress() & 0xFFFF)); // 更新对应AD参数文本视图显示内容
            } else { // AD参数索引无效,为PGA参数滑动条
                FPGACommand fpgaCommand = FPGACommand.getInstance(); // 获取FPGA命令实例对象
                int pgaVal = progress | 0x200; // 将PGA进度值与0x200进行或运算,生成PGA参数值
                fpgaCommand.setDebugPGA(pgaVal); // 设置调试PGA参数值
                FPGACommand.getInstance().SendAD8370Data(0, new int[]{pgaVal, pgaVal, pgaVal, pgaVal}); // 发送PGA数据到第0组AD8370芯片(4个通道)
                FPGACommand.getInstance().SendAD8370Data(1, new int[]{pgaVal, pgaVal, pgaVal, pgaVal}); // 发送PGA数据到第1组AD8370芯片(4个通道)
                tvPGA.setText("P:" + Integer.toHexString(progress)); // 更新PGA参数文本视图显示内容
            }
        }

        /**
         * 开始触摸滑动条回调方法
         * 
         * 当用户开始触摸滑动条时调用,此处未实现具体逻辑。
         * 
         * @param seekBar 滑动条控件对象
         */
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // 用户开始触摸滑动条,此处未实现具体逻辑
        }

        /**
         * 停止触摸滑动条回调方法
         * 
         * 当用户停止触摸滑动条时调用,此处未实现具体逻辑。
         * 
         * @param seekBar 滑动条控件对象
         */
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            // 用户停止触摸滑动条,此处未实现具体逻辑
        }
    };

    /**
     * 点击事件监听器
     * 
     * 监听单选按钮的点击事件,当增益/偏移模式切换时更新滑动条范围。
     */
    private OnClickListener onClickListener = new OnClickListener() {
        /**
         * 点击回调方法
         * 
         * 处理单选按钮点击事件,当增益/偏移模式切换时更新滑动条范围。
         * 
         * @param v 被点击的视图对象
         */
        @Override
        public void onClick(View v) {
            if (v.getId() == rbAdGain.getId() || v.getId() == rbAdOffset.getId()) { // 判断是否为增益或偏移模式单选按钮被点击
                SeekBar[] sb = {adfsSeekBarA, adfsSeekBarB, adfsSeekBarC, adfsSeekBarD}; // 创建滑动条数组
                for (SeekBar seekBar : sb) { // 遍历滑动条数组
                    seekBar.setMin(0); // 设置滑动条最小值为0
                    seekBar.setMax(rbAdGain.isChecked() ? 0xFFFF : 0x7FF); // 设置滑动条最大值(增益模式为0xFFFF,偏移模式为0x7FF)
                }
            }
        }
    };

    /**
     * 获取当前选中的通道索引
     * 
     * 检查所有通道单选按钮的选中状态,返回当前选中通道的索引(0-7)。
     * 
     * @return 当前选中通道的索引(0-7)
     */
    private int getChIdx() {
        RadioButton[] radioButtons = {rbCh1, rbCh2, rbCh3, rbCh4, rbCh5, rbCh6, rbCh7, rbCh8}; // 创建单选按钮数组(包含8个通道单选按钮)
        int chidx = 0; // 初始化通道索引为0
        for (int i = 0; i < radioButtons.length; i++) { // 遍历单选按钮数组
            if (radioButtons[i].isChecked()) { // 判断单选按钮是否被选中
                chidx = i; // 设置通道索引为当前遍历索引
                break; // 跳出循环
            }
        }
        return chidx; // 返回当前选中通道的索引
    }

    /** 触摸按下时的X坐标 */
    float downX, downY; // 触摸按下时的坐标(X/Y)

    /** 触摸移动时的X坐标 */
    float moveX, moveY; // 触摸移动时的坐标(X/Y)

    /**
     * 拦截触摸事件
     * 
     * 判断触摸事件是否为滑动操作,如果是滑动则拦截并交给onTouchEvent处理,
     * 否则传递给子视图处理。用于实现窗口的拖拽移动功能。
     * 
     * @param ev 触摸事件对象
     * @return 是否拦截触摸事件,滑动返回true,其他情况调用父类方法
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) { // 根据触摸事件类型判断
            case MotionEvent.ACTION_DOWN: // 触摸按下事件
                downX = ev.getRawX(); // 记录触摸按下时的原始X坐标
                downY = ev.getRawY(); // 记录触摸按下时的原始Y坐标
                left = (int) this.getX(); // 记录视图当前的X位置(左边距)
                top = (int) this.getY(); // 记录视图当前的Y位置(上边距)
                break;
            case MotionEvent.ACTION_MOVE: // 触摸移动事件
                moveX = ev.getRawX(); // 获取触摸移动时的原始X坐标
                moveY = ev.getRawY(); // 获取触摸移动时的原始Y坐标
                if (Math.abs(moveX - downX) > 5 || Math.abs(moveY - downY) > 5) { // 判断移动距离是否超过5像素(判定为滑动操作)
                    return true; // 拦截触摸事件,交给onTouchEvent处理
                }
                break;
            case MotionEvent.ACTION_UP: // 触摸抬起事件
                break;
        }
        return super.onInterceptTouchEvent(ev); // 调用父类方法处理其他触摸事件
    }

    /** 视图左边距和上边距 */
    int left, top; // 视图位置变量(左边距和上边距)

    /**
     * 处理触摸事件
     * 
     * 实现窗口的拖拽移动功能,根据触摸移动距离更新视图位置,
     * 并限制视图位置在屏幕范围内。
     * 
     * @param event 触摸事件对象
     * @return 是否消费触摸事件,此处始终返回true表示消费事件
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) { // 根据触摸事件类型判断
            case MotionEvent.ACTION_DOWN: // 触摸按下事件
                downX = event.getRawX(); // 记录触摸按下时的原始X坐标
                downY = event.getRawY(); // 记录触摸按下时的原始Y坐标
                break;
            case MotionEvent.ACTION_MOVE: // 触摸移动事件
                moveX = event.getRawX(); // 获取触摸移动时的原始X坐标
                moveY = event.getRawY(); // 获取触摸移动时的原始Y坐标

                Rect screen = GlobalVar.get().getScreen(); // 获取屏幕矩形区域
                float tmpX = getX() + (moveX - downX); // 计算新的X位置(当前位置加上移动距离)
                if (tmpX < screen.left) { // 判断新X位置是否超出屏幕左边
                    tmpX = screen.left; // 将新X位置限制为屏幕左边
                }
                if (tmpX + getWidth() > screen.right) { // 判断新X位置是否超出屏幕右边
                    tmpX = screen.right - getWidth(); // 将新X位置限制为屏幕右边减去视图宽度
                }

                float tmpY = getY() + (moveY - downY); // 计算新的Y位置(当前位置加上移动距离)
                if (tmpY < screen.top) { // 判断新Y位置是否超出屏幕上边
                    tmpY = screen.top; // 将新Y位置限制为屏幕上边
                }
                if (tmpY + getHeight() > screen.bottom) { // 判断新Y位置是否超出屏幕下边
                    tmpY = screen.bottom - getHeight(); // 将新Y位置限制为屏幕下边减去视图高度
                }
                this.setX(tmpX); // 更新视图的X位置
                this.setY(tmpY); // 更新视图的Y位置
                downX = moveX; // 更新触摸按下X坐标为当前移动X坐标
                downY = moveY; // 更新触摸按下Y坐标为当前移动Y坐标
                break;
            case MotionEvent.ACTION_UP: // 触摸抬起事件
                if (left != this.getX() || top != this.getY()) { // 判断视图位置是否发生变化
                    // 视图位置已改变,此处未实现具体逻辑
                }
                break;
        }
        return true; // 返回true表示消费触摸事件
    }

    /**
     * 设置视图位置
     * 
     * 将视图移动到指定的屏幕坐标位置。
     * 
     * @param x X坐标位置
     * @param y Y坐标位置
     */
    public void setLocation(int x, int y) {
        setX(x); // 设置视图的X位置
        setY(y); // 设置视图的Y位置
    }

    /** 矩形对象,用于判断点是否在视图范围内 */
    private Rect r = new Rect(); // 创建矩形对象用于范围判断

    /**
     * 判断点是否在视图范围内
     * 
     * 根据给定的屏幕坐标判断该点是否在当前视图的可见范围内。
     * 
     * @param x X坐标
     * @param y Y坐标
     * @return 是否在视图范围内,true表示在范围内且视图可见,false表示不在范围内或视图不可见
     */
    public boolean containsPoint(int x, int y) {
        r.set((int) getX(), (int) getY(), (int) getX() + getWidth(), (int) getY() + getHeight()); // 设置矩形范围为视图的边界
        return x > r.left && x < r.right && y > r.top && y < r.bottom && getVisibility() == View.VISIBLE; // 判断点是否在矩形范围内且视图是否可见
    }

    /**
     * 判断触摸按下点是否在视图范围内
     * 
     * 根据触摸事件的坐标和动作类型判断是否为视图范围内的按下事件。
     * 
     * @param event 触摸事件对象
     * @return 是否为视图范围内的按下事件,true表示在范围内且为按下事件且视图可见,false表示不符合条件
     */
    public boolean containsDownPoint(MotionEvent event) {
        int x = (int) event.getX(); // 获取触摸事件的X坐标
        int y = (int) event.getY(); // 获取触摸事件的Y坐标
        r.set((int) getX(), (int) getY(), (int) getX() + getWidth(), (int) getY() + getHeight()); // 设置矩形范围为视图的边界
        boolean b = x > r.left && x < r.right && y > r.top && y < r.bottom && getVisibility() == View.VISIBLE && event.getAction() == MotionEvent.ACTION_DOWN; // 判断触摸点是否在范围内且为按下事件且视图可见
        return b; // 返回判断结果
    }
}