// ============================================================
//  模块定位：sample（采样功能模块）
//  文件路径：top/layout/sample/TopLayoutSampleMode.java
//  核心职责：采样模式子页面Fragment，管理采样类型（普通/平均/包络/峰值）选择和详情配置
//  架构设计：Fragment + RxBus观察者模式 + EventFactory事件观察者，通过Consumer订阅多种事件
//  数据流向：用户选择采样类型 → onCheckChanged → Command发送硬件指令 → Sample更新采样参数
//  依赖关系：依赖TopViewRadioGroup/SelectHorList控件、Command硬件指令、Sample采样管理、CacheUtil缓存
//  使用场景：采样功能模式子页面，用户选择采样类型和详情（平均次数/包络次数）
// ============================================================

package com.micsig.tbook.tbookscope.top.layout.sample; // 声明该类所属的包路径

import android.content.Context; // 导入上下文类
import android.os.Bundle; // 导入Bundle类
import android.view.LayoutInflater; // 导入布局填充器
import android.view.View; // 导入视图基类
import android.view.ViewGroup; // 导入视图组基类

import androidx.annotation.Nullable; // 导入可空注解
import androidx.fragment.app.Fragment; // 导入Fragment基类

import com.micsig.tbook.scope.Event.EventBase; // 导入事件基类
import com.micsig.tbook.scope.Event.EventFactory; // 导入事件工厂
import com.micsig.tbook.scope.Event.EventUIObserver; // 导入UI事件观察者
import com.micsig.tbook.scope.Sample.Sample; // 导入采样类
import com.micsig.tbook.scope.channel.ChannelFactory; // 导入通道工厂
import com.micsig.tbook.tbookscope.LoadCache; // 导入缓存加载消息
import com.micsig.tbook.tbookscope.MainActivity; // 导入主Activity
import com.micsig.tbook.tbookscope.MainViewGroup; // 导入主视图组
import com.micsig.tbook.tbookscope.R; // 导入资源ID
import com.micsig.tbook.tbookscope.main.mainbottom.MainBottomMsgTimeBase; // 导入底部时基消息
import com.micsig.tbook.tbookscope.main.mainright.MainRightMsgOthers; // 导入右侧其他消息
import com.micsig.tbook.tbookscope.middleware.command.Command; // 导入命令工厂
import com.micsig.tbook.tbookscope.middleware.command.CommandMsgToUI; // 导入命令消息到UI
import com.micsig.tbook.tbookscope.rightslipmenu.RightMsgMath; // 导入数学通道消息
import com.micsig.tbook.tbookscope.rxjava.RxBus; // 导入RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // 导入事件枚举
import com.micsig.tbook.tbookscope.tools.PlaySound; // 导入音效播放工具
import com.micsig.tbook.tbookscope.tools.Tools; // 导入通用工具类
import com.micsig.tbook.tbookscope.top.layout.trigger.TopLayoutTrigger; // 导入触发布局
import com.micsig.tbook.tbookscope.top.layout.trigger.TopMsgTrigger; // 导入触发消息
import com.micsig.tbook.tbookscope.util.CacheUtil; // 导入缓存工具
import com.micsig.tbook.tbookscope.wavezone.IWorkMode; // 导入工作模式接口
import com.micsig.tbook.tbookscope.wavezone.WorkModeBean; // 导入工作模式Bean
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel; // 导入通道数据Bean
import com.micsig.tbook.ui.top.view.radioGroup.TopViewRadioGroup; // 导入自定义单选组
import com.micsig.tbook.ui.top.view.selectHorList.TopBeanHorizontal; // 导入水平列表数据Bean
import com.micsig.tbook.ui.top.view.selectHorList.TopViewSelectHorListToHead; // 导入水平列表头部视图
import com.micsig.tbook.ui.top.view.selectHorList.TopViewSelectHorListToList; // 导入水平列表视图
import com.micsig.tbook.ui.util.StrUtil; // 导入字符串工具
import com.micsig.tbook.ui.wavezone.TChan; // 导入通道枚举

import io.reactivex.rxjava3.annotations.NonNull; // 导入非空注解
import io.reactivex.rxjava3.functions.Consumer; // 导入消费者接口


/**
 * 采样模式Fragment - 管理采样类型选择和详情配置
 * Created by Administrator on 2017/4/11.
 */
public class TopLayoutSampleMode extends Fragment { // 继承Fragment，采样模式子页面
    /** 数学微分采样类型索引 */
    private static final int MATH_DIFF_SAMPLE_TYPE_INDEX = 1; // 数学微分对应的采样类型索引
    /** 数学微分采样详情索引 */
    private static final int MATH_DIFF_SAMPLE_DETAIL_INDEX = 1; // 数学微分对应的采样详情索引

    private Context context; // 上下文对象
    /** 采样类型单选组 */
    private TopViewRadioGroup rgSample; // 采样类型选择控件
    /** 采样详情头部视图 */
    private TopViewSelectHorListToHead tvSampleDetail; // 采样详情头部
    /** 采样详情列表视图 */
    private TopViewSelectHorListToList selectListToList; // 采样详情列表

    /** 采样模式消息对象 */
    private TopMsgSampleMode msgSample; // 采样模式消息

    /**
     * 创建Fragment视图
     * @param inflater 布局填充器
     * @param container 父容器
     * @param savedInstanceState 保存的状态
     * @return 填充后的视图
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) { // 创建视图
        return inflater.inflate(R.layout.layout_sample_mode, container, false); // 填充采样模式布局
    }

    /**
     * 视图创建完成后的初始化
     * @param view 根视图
     * @param savedInstanceState 保存的状态
     */
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) { // 视图创建完成回调
        this.context = getActivity(); // 获取Activity上下文
        initView(view); // 初始化视图
        initControl(); // 初始化事件控制
    }

    /**
     * 初始化视图组件
     * @param view 根视图
     */
    private void initView(View view) { // 初始化视图方法
        rgSample = (TopViewRadioGroup) view.findViewById(R.id.topViewSample); // 获取采样类型单选组
        rgSample.setData(R.string.sampleMode, R.array.sampleMode, onCheckChangedListener); // 设置采样类型选项
        tvSampleDetail = (TopViewSelectHorListToHead) view.findViewById(R.id.sampleSelectListToHead); // 获取详情头部视图
        tvSampleDetail.setData("", "", selectListToHeadListener); // 设置详情头部数据
        selectListToList = (TopViewSelectHorListToList) ((MainActivity) context).findViewById(R.id.selectListToList); // 获取详情列表视图
        selectListToList.setData(R.id.sampleSelectListToHead, R.array.sampleDetail, selectListToListListener); // 设置详情列表数据

        msgSample = new TopMsgSampleMode(); // 创建采样模式消息
        msgSample.setSample(rgSample.getSelected()); // 设置采样类型
        msgSample.setDetail(tvSampleDetail.getText()); // 设置详情文本
    }

    /**
     * 初始化RxBus事件订阅和EventFactory观察者
     */
    private void initControl() { // 初始化事件控制
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache); // 订阅缓存加载事件
        RxBus.getInstance().getObservable(RxEnum.COMMAND_TO_UI).subscribe(consumerCommandToUI); // 订阅命令到UI事件
        RxBus.getInstance().getObservable(RxEnum.WAVEZONE_WORKMODE_CHANGE).subscribe(consumerWorkModeChange); // 订阅工作模式变更事件
        RxBus.getInstance().getObservable(RxEnum.MAINBOTTOM_TIMEBASE).subscribe(consumerMainBottomTimeBase); // 订阅时基变更事件
        RxBus.getInstance().getObservable(RxEnum.MAINRIGHT_OTHERS).subscribe(consumerMainRightOthers); // 订阅右侧其他事件
        RxBus.getInstance().getObservable(RxEnum.TOPLAYOUT_TRIGGER).subscribe(consumerTopTrigger); // 订阅触发事件
        RxBus.getInstance().getObservable(RxEnum.RIGHTLAYOUT_MATH).subscribe(consumerRightMath); // 订阅数学通道事件
        RxBus.getInstance().getObservable(RxEnum.TOPLAYOUT_SAMPLESEGMENTED).subscribe(consumerSampleSegmented); // 订阅分段存储事件
//        RxBus.getInstance().getObservable(RxEnum.MAINCENTER_CHANNEL_SELECT).subscribe(consumerChannelSelect);

        EventFactory.addEventObserver(EventFactory.EVENT_SAMPLE_TYPE, eventUIObserver); // 注册采样类型事件观察者
    }

    /**
     * 从缓存恢复采样模式状态
     */
    private void setCache() { // 从缓存恢复状态
//        isWorkModeXY = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_COMMON_TIMEBASE) == 1;
//        isSerials = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S1) || CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S2)
//                || CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER) == TopLayoutTrigger.DETAIL_S1
//                || CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER) == TopLayoutTrigger.DETAIL_S2;
//        isSegment = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_SEGMENTED_STATE) == 0;
//        int channelSelect = CacheUtil.get().getInt(CacheUtil.MAIN_CENTER_CHANNELS_SELECT);
//        int mathType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_TYPE);
//        if ((channelSelect >= 0 && channelSelect <= 3)
//                || (channelSelect == 4 && mathType != CacheUtil.MATHTYPE_FFT)) {
//            //当当前通道为普通通道或数学的非fft通道时，判断是否为慢时基状态
//            isSlow = Tools.isSlowTimeBase(CacheUtil.get().getString(CacheUtil.MAIN_BOTTOM_TIMEBASE_NORMAL_SCALE));
//        }
//        boolean visible = !(isWorkModeXY || isSerials || isSlow || isSegment);
//        rgSample.setEnabled(1, visible);
//        rgSample.setEnabled(2, visible);
//        rgSample.setEnabled(3, !isWorkModeXY);
        setSampleEnable(false); // 设置采样选项启用状态
        int sampleIndex; // 采样类型索引
        int detailIndex; // 详情索引
        if (isMathAmDiff() && rgSample.isEnabled(MATH_DIFF_SAMPLE_TYPE_INDEX)) { // 如果是数学微分模式
            sampleIndex = MATH_DIFF_SAMPLE_TYPE_INDEX; // 使用微分采样类型索引
            detailIndex = MATH_DIFF_SAMPLE_DETAIL_INDEX; // 使用微分详情索引
        } else { // 普通模式
            sampleIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_MODE); // 从缓存读取采样类型
            detailIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_MODE_DETAIL_INDEX); // 从缓存读取详情索引
//            if (!rgSample.getEnabled(sampleIndex)) {
//                sampleIndex = 0;
//            }
        }
        rgSample.setSelectedIndex(sampleIndex); // 设置采样类型选中项
        setDetailEnable(sampleIndex); // 设置详情启用状态

        Command.get().getSample().Type(sampleIndex, false); // 发送采样类型命令
        Command.get().getSample().Mean(detailIndex, false); // 发送均值命令
        Command.get().getSample().Envelop(detailIndex, false); // 发送包络命令

        Sample.getInstance().setSampleType(matchSample(sampleIndex)); // 设置采样类型
        Sample.getInstance().setSampleNum(getSampleNum()); // 设置采样数量

        msgSample.setSample(rgSample.getSelected()); // 更新消息采样类型
        msgSample.setDetail(tvSampleDetail.getText()); // 更新消息详情
        msgSample.setSampleDetailIndex(detailIndex); // 更新消息详情索引
        sendMsgSample(false); // 发送采样模式消息
    }

    /**
     * 获取采样数量（从详情文本解析）
     * @return 采样数量
     */
    private int getSampleNum() { // 获取采样数量
        String s = tvSampleDetail.getText().replace("∞", "512"); // 将无穷符号替换为512
        if (StrUtil.isEmpty(s)) { // 如果文本为空
            return 2; // 默认返回2
        } else { // 文本非空
            return Integer.parseInt(s); // 解析为整数
        }
    }

    /**
     * 将UI采样类型索引映射为硬件采样类型常量
     * @param sample UI采样类型索引
     * @return 硬件采样类型常量
     */
    private int matchSample(int sample) { // 映射UI索引到硬件常量
        if (sample == 1) return Sample.SAMPLE_TYPE_AVERAGE; // 1→平均
        if (sample == 2) return Sample.SAMPLE_TYPE_ENVEL; // 2→包络
        if (sample == 3) return Sample.SAMPLE_TYPE_PEAK; // 3→峰值
        return Sample.SAMPLE_TYPE_NORMAL; // 0→普通
    }

    /**
     * 将硬件采样类型常量映射为UI采样类型索引
     * @param scopeSample 硬件采样类型常量
     * @return UI采样类型索引
     */
    private int unMatchSample(int scopeSample) { // 映射硬件常量到UI索引
        if (scopeSample == Sample.SAMPLE_TYPE_AVERAGE) return 1; // 平均→1
        if (scopeSample == Sample.SAMPLE_TYPE_ENVEL) return 2; // 包络→2
        if (scopeSample == Sample.SAMPLE_TYPE_PEAK) return 3; // 峰值→3
        return 0; // 普通→0
    }

    /**
     * 设置详情文本的启用状态
     * @param sampleIndex 采样类型索引
     */
    private void setDetailEnable(int sampleIndex) { // 设置详情启用状态
        if (sampleIndex != 1 && sampleIndex != 2) { // 如果不是平均或包络
            tvSampleDetail.setText(""); // 清空详情文本
            tvSampleDetail.setEnabled(false); // 禁用详情
        } else { // 平均或包络
            int detailIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_MODE_DETAIL_INDEX); // 读取详情索引
            String[] sampleDetailArray = getEnvelList(); // 获取包络列表
            tvSampleDetail.setText(sampleDetailArray[detailIndex]); // 设置详情文本
            tvSampleDetail.setEnabled(true); // 启用详情
        }
    }

    /**
     * 获取平均次数列表（去掉包络列表最后一项）
     * @return 平均次数字符串数组
     */
    private String[] getAveList() { // 获取平均次数列表
        String[] sampleDetailArray = getEnvelList(); // 获取包络列表
        String[] strings = new String[sampleDetailArray.length - 1]; // 创建少一项的数组
        for (int i = 0; i < strings.length; i++) { // 遍历
            strings[i] = sampleDetailArray[i]; // 复制前N-1项
        }
        return strings; // 返回平均列表
    }

    /**
     * 获取包络次数列表
     * @return 包络次数字符串数组
     */
    private String[] getEnvelList() { // 获取包络次数列表
        return context.getResources().getStringArray(R.array.sampleDetail); // 从资源获取
    }

    /**
     * 发送采样模式消息到RxBus
     * @param isFromEventBus 是否来自EventBus
     */
    private void sendMsgSample(boolean isFromEventBus) { // 发送采样模式消息
        msgSample.setFromEventBus(isFromEventBus); // 设置来源标志
        RxBus.getInstance().post(RxEnum.TOPLAYOUT_SAMPLEMODE, msgSample); // 通过RxBus发送消息
    }

    /**
     * 判断当前是否选中了数学菜单下高级数学中的微分项
     * @return true表示是数学微分模式
     */
    private boolean isMathAmDiff() { // 判断是否为数学微分模式
        final boolean[] isMathAmDiff = {false}; // 结果数组（用于lambda）
        TChan.foreachMath(mathChan -> { // 遍历所有数学通道
            boolean mathCheck = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_MATH + mathChan); // 数学通道是否开启
            boolean isMathAm = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_TYPE + mathChan) == CacheUtil.MATHTYPE_AM; // 是否为高级数学
            boolean amFormulaHave = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_MATH_AM_FORMULA_DIFF_HAVE + mathChan); // 微分公式是否存在
            boolean amFormulaReset = CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_MATH_AM_FORMULA_DIFF_RESET + mathChan); // 微分重置标志
            boolean amFormula = CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_MATH_AM_FORMULA).contains("ch"); // 公式是否包含通道
            isMathAmDiff[0] = (mathCheck && isMathAm && amFormulaHave && amFormulaReset && amFormula) || isMathAmDiff[0]; // 逻辑或合并结果
        });
        return isMathAmDiff[0]; // 返回判断结果
//        return
//                CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_MATH + TChan.Ch1)
//                && CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_TYPE) == CacheUtil.MATHTYPE_AM
//                && CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_MATH_AM_FORMULA_DIFF_HAVE)
//                && CacheUtil.get().getBoolean(CacheUtil.RIGHT_SLIP_MATH_AM_FORMULA_DIFF_RESET)
//                && CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_MATH_AM_FORMULA).contains("ch");
    }

    /**
     * 缓存加载消费者 - 恢复采样模式状态
     */
    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() { // 缓存加载消费者
        @Override
        public void accept(@NonNull LoadCache loadCache) throws Exception { // 接收缓存加载事件
            setCache(); // 从缓存恢复状态
            CacheUtil.get().setLoadMenuState(CacheUtil.LOAD_TopLayoutSampleMode, true); // 标记采样模式页面缓存加载完成
        }
    };


    //region 设置sample中后三个选项的可点击性
    /**
     * 工作模式变更消费者 - 根据XY/YT模式调整采样选项可用性
     */
    private Consumer<WorkModeBean> consumerWorkModeChange = new Consumer<WorkModeBean>() { // 工作模式变更消费者
        @Override
        public void accept(WorkModeBean workModeBean) throws Exception { // 接收工作模式变更
            switch (workModeBean.getNextWorkMode()) { // 根据下一工作模式
                case IWorkMode.WorkMode_YT: // YT模式
                case IWorkMode.WorkMode_YTZOOM: // YT缩放模式
                    isWorkModeXY = false; // 非XY模式
                    setSampleEnable(workModeBean.isFromEventBus()); // 更新采样选项可用性
                    break;
                case IWorkMode.WorkMode_XY: // XY模式
                    isWorkModeXY = true; // XY模式
                    setSampleEnable(workModeBean.isFromEventBus()); // 更新采样选项可用性
                    break;
            }
        }
    };

    /**
     * 时基变更消费者 - 根据时基速度调整采样选项可用性
     */
    private Consumer<MainBottomMsgTimeBase> consumerMainBottomTimeBase = new Consumer<MainBottomMsgTimeBase>() { // 时基变更消费者
        @Override
        public void accept(MainBottomMsgTimeBase msgTimeBase) throws Exception { // 接收时基变更
            if (msgTimeBase.getType() == MainBottomMsgTimeBase.TYPE_NORMAL) { // 如果是普通时基
                isSlow = Tools.isSlowTimeBase(); // 判断是否为慢时基
            }
            setSampleEnable(msgTimeBase.isFromEventBus()); // 更新采样选项可用性
        }
    };

    /**
     * 右侧其他消息消费者 - 根据串行通道状态调整采样选项
     */
    private Consumer<MainRightMsgOthers> consumerMainRightOthers = new Consumer<MainRightMsgOthers>() { // 右侧其他消息消费者
        @Override
        public void accept(MainRightMsgOthers msgOthers) throws Exception { // 接收右侧其他消息
            boolean btnS1 = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S1); // 串行1是否开启
            boolean btnS2 = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S2); // 串行2是否开启
            boolean btnS3 = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S3); // 串行3是否开启
            boolean btnS4 = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S4); // 串行4是否开启
            int trigger = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER); // 触发类型
            isSerials = btnS1 || btnS2 || btnS3 || btnS4 // 任意串行通道开启
                    || trigger == TopLayoutTrigger.DETAIL_S1 || trigger == TopLayoutTrigger.DETAIL_S2 // 或串行触发
                    || trigger == TopLayoutTrigger.DETAIL_S3 || trigger == TopLayoutTrigger.DETAIL_S4; // 或串行触发
            setSampleEnable(false); // 更新采样选项可用性
            if (isMathAmDiff() && rgSample.isEnabled(MATH_DIFF_SAMPLE_TYPE_INDEX)) { // 数学微分模式
                if (rgSample.getSelected().getIndex() != MATH_DIFF_SAMPLE_TYPE_INDEX) { // 如果当前不是微分
                    rgSample.setSelectedIndex(MATH_DIFF_SAMPLE_TYPE_INDEX); // 切换到微分
                    onCheckChanged(rgSample, rgSample.getSelected(), false, false, false); // 触发变更
                }
                String[] envelList = getEnvelList(); // 获取包络列表
                if (!tvSampleDetail.getText().equals(envelList[MATH_DIFF_SAMPLE_DETAIL_INDEX])) { // 如果详情不匹配
                    tvSampleDetail.setText(envelList[MATH_DIFF_SAMPLE_DETAIL_INDEX]); // 更新详情
                    onTextChanged(new TopBeanHorizontal(MATH_DIFF_SAMPLE_DETAIL_INDEX // 触发文本变更
                            , envelList[MATH_DIFF_SAMPLE_DETAIL_INDEX]), false, false);
                }
            } else { // 普通模式
                int indexSample = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_MODE); // 读取采样类型索引
                int sampleDetailIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_MODE_DETAIL_INDEX); // 读取详情索引
                if (rgSample.getSelected().getIndex() != indexSample) { // 如果类型不匹配
                    rgSample.setSelectedIndex(indexSample); // 更新类型
                    onCheckChanged(rgSample, rgSample.getSelected(), false, false, true); // 触发变更
                }
                String[] envelList = getEnvelList(); // 获取包络列表
                if (!tvSampleDetail.getText().equals(envelList[sampleDetailIndex])) { // 如果详情不匹配
                    tvSampleDetail.setText(envelList[sampleDetailIndex]); // 更新详情
                    onTextChanged(new TopBeanHorizontal(sampleDetailIndex // 触发文本变更
                            , envelList[sampleDetailIndex]), false, true);
                }
            }
        }
    };

    /**
     * 触发消息消费者 - 根据触发类型调整采样选项
     */
    private Consumer<TopMsgTrigger> consumerTopTrigger = new Consumer<TopMsgTrigger>() { // 触发消息消费者
        @Override
        public void accept(TopMsgTrigger topMsgTrigger) throws Exception { // 接收触发消息
            if (topMsgTrigger.getTriggerTitle().isRxMsgSelect()) { // 如果触发标题被选中
                boolean btnS1 = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S1); // 串行1状态
                boolean btnS2 = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S2); // 串行2状态
                boolean btnS3 = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S3); // 串行3状态
                boolean btnS4 = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S4); // 串行4状态
                int trigger = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER); // 触发类型
                isSerials = btnS1 || btnS2 || btnS3 || btnS4 // 任意串行开启
                        || trigger == TopLayoutTrigger.DETAIL_S1 || trigger == TopLayoutTrigger.DETAIL_S2 // 串行触发
                        || trigger == TopLayoutTrigger.DETAIL_S3 || trigger == TopLayoutTrigger.DETAIL_S4;; // 串行触发
                setSampleEnable(topMsgTrigger.isFromEventBus()); // 更新采样选项可用性
            }
        }
    };

    /**
     * 数学通道消息消费者 - 根据数学通道类型调整采样选项
     */
    private Consumer<RightMsgMath> consumerRightMath = new Consumer<RightMsgMath>() { // 数学通道消息消费者
        @Override
        public void accept(RightMsgMath rightMsgMath) throws Exception { // 接收数学通道消息
            if (isMathAmDiff() && rgSample.isEnabled(MATH_DIFF_SAMPLE_TYPE_INDEX)) { // 数学微分模式
                if (rgSample.getSelected().getIndex() != MATH_DIFF_SAMPLE_TYPE_INDEX) { // 如果不是微分
                    rgSample.setSelectedIndex(MATH_DIFF_SAMPLE_TYPE_INDEX); // 切换到微分
                    onCheckChanged(rgSample, rgSample.getSelected(), false, false, false); // 触发变更
                }
                String[] envelList = getEnvelList(); // 获取包络列表
                if (!tvSampleDetail.getText().equals(envelList[MATH_DIFF_SAMPLE_DETAIL_INDEX])) { // 详情不匹配
                    tvSampleDetail.setText(envelList[MATH_DIFF_SAMPLE_DETAIL_INDEX]); // 更新详情
                    onTextChanged(new TopBeanHorizontal(MATH_DIFF_SAMPLE_DETAIL_INDEX // 触发文本变更
                            , envelList[MATH_DIFF_SAMPLE_DETAIL_INDEX]), false, false);
                }
            } else { // 普通模式
                int indexSample = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_MODE); // 读取采样类型
                int sampleDetailIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_MODE_DETAIL_INDEX); // 读取详情索引
                if (rgSample.getSelected().getIndex() != indexSample) { // 类型不匹配
                    rgSample.setSelectedIndex(indexSample); // 更新类型
                    onCheckChanged(rgSample, rgSample.getSelected(), false, false, true); // 触发变更
                }
                String[] envelList = getEnvelList(); // 获取包络列表
                if (!tvSampleDetail.getText().equals(envelList[sampleDetailIndex])) { // 详情不匹配
                    tvSampleDetail.setText(envelList[sampleDetailIndex]); // 更新详情
                    onTextChanged(new TopBeanHorizontal(sampleDetailIndex // 触发文本变更
                            , envelList[sampleDetailIndex]), false, true);
                }
            }
        }
    };

    /**
     * 分段存储消息消费者 - 根据分段存储状态调整采样选项
     */
    private Consumer<TopMsgSampleSegmented> consumerSampleSegmented = new Consumer<TopMsgSampleSegmented>() { // 分段存储消息消费者
        @Override
        public void accept(TopMsgSampleSegmented msgSampleSegmented) throws Exception { // 接收分段存储消息
            isSegment = msgSampleSegmented.getState().isValue(); // 获取分段存储开关状态
            setSampleEnable(msgSampleSegmented.isFromEventBus()); // 更新采样选项可用性
        }
    };

//    private Consumer<MainCenterMsgChannels> consumerChannelSelect = new Consumer<MainCenterMsgChannels>() {
//        @Override
//        public void accept(MainCenterMsgChannels msgChannels) throws Exception {
//            setSampleEnable(msgChannels.isFromEventBus());
//        }
//    };

    /** 是否是XY模式，为true时123置灰 */
    private boolean isWorkModeXY = false; // XY模式标志
    /** 总线解码是否开启，为true时12置灰 */
    private boolean isSerials = false; // 串行总线标志
    /** 是否是慢时基模式，为true时12置灰 */
    private boolean isSlow = false; // 慢时基标志
    /** 是否是分段存储开启模式，为true时12置灰 */
    private boolean isSegment = false; // 分段存储标志
    /** 是否是插值档，为true时2置灰 */
    private boolean isChaZhiDang = false; // 插值档标志

    /**
     * 设置采样选项的可点击性，根据各种模式状态综合判断
     * @param isFromEventBus 是否来自EventBus
     */
    private void setSampleEnable(boolean isFromEventBus) { // 设置采样选项可用性
        isWorkModeXY = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_COMMON_TIMEBASE) == 1; // 读取XY模式状态
        isSerials = ChannelFactory.isChOpen(ChannelFactory.S1) // 串行1是否开启
                || ChannelFactory.isChOpen(ChannelFactory.S2) // 串行2是否开启
                || ChannelFactory.isChOpen(ChannelFactory.S3) // 串行3是否开启
                || ChannelFactory.isChOpen(ChannelFactory.S4) // 串行4是否开启
                || CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER) == TopLayoutTrigger.DETAIL_S1 // 串行触发1
                || CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER) == TopLayoutTrigger.DETAIL_S2 // 串行触发2
                || CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER) == TopLayoutTrigger.DETAIL_S3 // 串行触发3
                || CacheUtil.get().getInt(CacheUtil.TOP_SLIP_TRIGGER) == TopLayoutTrigger.DETAIL_S4; // 串行触发4

        isSlow = Tools.isSlowTimeBase(); // 判断是否为慢时基
        isSegment = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_SEGMENTED_STATE) == 0; // 分段存储是否开启
        isChaZhiDang = Tools.isChaZhiDang(); // 是否为插值档
        boolean changeEnable = false; // 启用状态变化标志
        boolean b1 = msgSample.setSampleEnable(true, true, true, true); // 先全部启用
        if (b1) { // 如果有变化
            changeEnable = true; // 标记变化
        }
        if (isWorkModeXY) { // XY模式
            boolean b = msgSample.setSampleEnable(false, false, false); // 禁用123
            if (b) { // 如果有变化
                changeEnable = true; // 标记变化
            }
        }
        if (isSerials) { // 串行模式
            boolean b = msgSample.setSampleEnable(false, false); // 禁用12
            if (b) { // 如果有变化
                changeEnable = true; // 标记变化
            }
        }
        if (isSlow) { // 慢时基
            boolean b = msgSample.setSampleEnable(false, false); // 禁用12
            if (b) { // 如果有变化
                changeEnable = true; // 标记变化
            }
        }
        if (isSegment) { // 分段存储
            boolean b = msgSample.setSampleEnable(false, false); // 禁用12
            if (b) { // 如果有变化
                changeEnable = true; // 标记变化
            }
        }
        if (isChaZhiDang) { // 插值档
            boolean b = msgSample.setSampleEnable(2, false); // 禁用2
            if (b) { // 如果有变化
                changeEnable = true; // 标记变化
            }
        }
        //由于sample的可点击性改变，发送消息
        if (isMathAmDiff() && msgSample.getSampleEnable()[MATH_DIFF_SAMPLE_TYPE_INDEX]) { // 数学微分模式
            boolean msgSend = false; // 消息发送标志
            if (rgSample.getSelected().getIndex() != MATH_DIFF_SAMPLE_TYPE_INDEX) { // 类型不匹配
                rgSample.setSelectedIndex(MATH_DIFF_SAMPLE_TYPE_INDEX); // 切换到微分
                onCheckChanged(rgSample, rgSample.getSelected(), isFromEventBus, false, false); // 触发变更
                msgSend = true; // 标记已发送
            }
            String[] envelList = getEnvelList(); // 获取包络列表
            if (!tvSampleDetail.getText().equals(envelList[MATH_DIFF_SAMPLE_DETAIL_INDEX])) { // 详情不匹配
                tvSampleDetail.setText(envelList[MATH_DIFF_SAMPLE_DETAIL_INDEX]); // 更新详情
                onTextChanged(new TopBeanHorizontal(MATH_DIFF_SAMPLE_DETAIL_INDEX // 触发文本变更
                        , envelList[MATH_DIFF_SAMPLE_DETAIL_INDEX]), isFromEventBus, false);
                msgSend = true; // 标记已发送
            }
            if (!msgSend) { // 如果没有发送过消息
                sendMsgSample(isFromEventBus); // 发送采样模式消息
            }
        } else { // 普通模式
            int indexSample = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_MODE); // 读取采样类型
            if (msgSample.getSampleEnable()[indexSample]) { // 如果该类型可用
                boolean msgSend = false; // 消息发送标志
                int sampleDetailIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_MODE_DETAIL_INDEX); // 读取详情索引
                if (rgSample.getSelected().getIndex() != indexSample) { // 类型不匹配
                    rgSample.setSelectedIndex(indexSample); // 更新类型
                    onCheckChanged(rgSample, rgSample.getSelected(), false, false, true); // 触发变更
                    msgSend = true; // 标记已发送
                }
                String[] envelList = getEnvelList(); // 获取包络列表
                if (!tvSampleDetail.getText().equals(envelList[sampleDetailIndex])) { // 详情不匹配
                    tvSampleDetail.setText(envelList[sampleDetailIndex]); // 更新详情
                    onTextChanged(new TopBeanHorizontal(sampleDetailIndex // 触发文本变更
                            , envelList[sampleDetailIndex]), isFromEventBus, true);
                    msgSend = true; // 标记已发送
                }
                if (!msgSend) { // 如果没有发送过消息
                    sendMsgSample(isFromEventBus); // 发送采样模式消息
                }
            } else if (!msgSample.getSampleEnable()[rgSample.getSelected().getIndex()]) { // 当前选中不可用
                rgSample.setSelectedIndex(0); // 切换到普通模式
                onCheckChanged(rgSample, rgSample.getSelected(), isFromEventBus, false, true); // 触发变更
            } else { // 当前选中可用但启用状态变化
                if (changeEnable) { // 如果启用状态有变化
                    sendMsgSample(isFromEventBus); // 发送采样模式消息
                }
            }
        }
        rgSample.setEnabled(1, msgSample.getSampleEnable()[1]); // 设置平均选项启用状态
        rgSample.setEnabled(2, msgSample.getSampleEnable()[2]); // 设置包络选项启用状态
        rgSample.setEnabled(3, msgSample.getSampleEnable()[3]); // 设置峰值选项启用状态

//        boolean visible = !(isWorkModeXY || isSerials || isSlow || isSegment);
//        msgSample.setSampleEnable(visible, visible, !isWorkModeXY);
//        //当总可见性为不可见，且不是非xy模式下的3，则改变选项...
//        if (!visible && !(rgSample.getSelected().getIndex() == 3 && !isWorkModeXY)) {
//            if (rgSample.getSelected().getIndex() != 0) {
//                rgSample.setSelectedIndex(0);
//                onCheckChanged(rgSample, rgSample.getSelected(), isFromEventBus, false, true);
//            } else {
//                sendMsgSample(isFromEventBus);
//            }
//        } else {
//            //由于sample的可点击性改变，发送消息
//            if (isMathAmDiff() && visible) {
//                boolean msgSend = false;
//                if (rgSample.getSelected().getIndex() != MATH_DIFF_SAMPLE_TYPE_INDEX) {
//                    rgSample.setSelectedIndex(MATH_DIFF_SAMPLE_TYPE_INDEX);
//                    onCheckChanged(rgSample, rgSample.getSelected(), isFromEventBus, false, false);
//                    msgSend = true;
//                }
//                String[] envelList = getEnvelList();
//                if (!tvSampleDetail.getText().equals(envelList[MATH_DIFF_SAMPLE_DETAIL_INDEX])) {
//                    tvSampleDetail.setText(envelList[MATH_DIFF_SAMPLE_DETAIL_INDEX]);
//                    onTextChanged(new TopBeanHorizontal(MATH_DIFF_SAMPLE_DETAIL_INDEX
//                            , envelList[MATH_DIFF_SAMPLE_DETAIL_INDEX]), isFromEventBus, false);
//                    msgSend = true;
//                }
//                if (!msgSend) {
//                    sendMsgSample(isFromEventBus);
//                }
//            } else {
//                boolean msgSend = false;
//                int indexSample = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_MODE);
//                int sampleDetailIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_MODE_DETAIL_INDEX);
//                if (rgSample.getSelected().getIndex() != indexSample) {
//                    rgSample.setSelectedIndex(indexSample);
//                    onCheckChanged(rgSample, rgSample.getSelected(), false, false, true);
//                    msgSend = true;
//                }
//                String[] envelList = getEnvelList();
//                if (!tvSampleDetail.getText().equals(envelList[sampleDetailIndex])) {
//                    tvSampleDetail.setText(envelList[sampleDetailIndex]);
//                    onTextChanged(new TopBeanHorizontal(sampleDetailIndex
//                            , envelList[sampleDetailIndex]), isFromEventBus, true);
//                    msgSend = true;
//                }
//                if (!msgSend) {
//                    sendMsgSample(isFromEventBus);
//                }
//            }
//        }
//        boolean aveChange = rgSample.setEnabled(1, visible);
//        boolean envelChange = rgSample.setEnabled(2, visible);
//        boolean peakChange = rgSample.setEnabled(3, !isWorkModeXY);
//        rgSample.setEnabled(1, msgSample.getSampleEnable()[1]);
//        rgSample.setEnabled(2, msgSample.getSampleEnable()[2]);
//        rgSample.setEnabled(3, msgSample.getSampleEnable()[3]);
    }
    //endregion

    /**
     * 命令到UI消费者 - 处理硬件返回的采样类型/均值/包络命令
     */
    private Consumer<CommandMsgToUI> consumerCommandToUI = new Consumer<CommandMsgToUI>() { // 命令到UI消费者
        @Override
        public void accept(CommandMsgToUI commandMsgToUI) throws Exception { // 接收命令消息
            switch (commandMsgToUI.getFlag()) { // 根据命令标志分发
                case CommandMsgToUI.FLAG_SAMPLE_TYPE: { // 采样类型命令
                    rgSample.setSelectedIndex(Integer.parseInt(commandMsgToUI.getParam())); // 更新选中索引
                    onCheckChanged(rgSample, rgSample.getSelected(), false, true, true); // 触发变更
                    break;
                }
                case CommandMsgToUI.FLAG_SAMPLE_MEAN: { // 均值命令
                    if (rgSample.getSelected().getIndex() == 1) { // 如果当前是平均模式
                        int index = Integer.parseInt(commandMsgToUI.getParam()); // 解析索引
                        String[] sampleDetail = getEnvelList(); // 获取详情列表
                        if (index >= 0 && index <= sampleDetail.length - 2) { // 索引有效（平均不含最后一项）
                            tvSampleDetail.setText(sampleDetail[index]); // 更新详情文本
                            onTextChanged(new TopBeanHorizontal(index // 触发文本变更
                                    , sampleDetail[index]), false, true);
                            break;
                        }
                    }
                    break;
                }
                case CommandMsgToUI.FLAG_SAMPLE_ENVELOP: { // 包络命令
                    if (rgSample.getSelected().getIndex() == 2) { // 如果当前是包络模式
                        int index = Integer.parseInt(commandMsgToUI.getParam()); // 解析索引
                        String[] sampleDetail = getEnvelList(); // 获取详情列表
                        if (index >= 0 && index <= sampleDetail.length - 1) { // 索引有效
                            tvSampleDetail.setText(sampleDetail[index]); // 更新详情文本
                            onTextChanged(new TopBeanHorizontal(index // 触发文本变更
                                    , sampleDetail[index]), false, true);
                            break;
                        }
                    }
                    break;
                }
            }
        }
    };

    /**
     * 采样类型单选组变更监听器
     */
    private TopViewRadioGroup.OnCheckChangedListener onCheckChangedListener = new TopViewRadioGroup.OnCheckChangedListener() { // 采样类型变更监听器
        @Override
        public void onClickSound(boolean isCheckedSuccess) { // 选中音效回调
            PlaySound.getInstance().playButton(); // 播放按键音效
        }

        @Override
        public void onPrompt(TopViewRadioGroup view) { // 提示回调（空实现）
        }

        @Override
        public void onClick(TopViewRadioGroup view, TopBeanChannel item) { // 选中回调
            onCheckChanged(view, item, false, true, true); // 触发变更处理
        }
    };

    /**
     * 详情头部点击监听器 - 弹出详情选择列表
     */
    private TopViewSelectHorListToHead.OnClickListener selectListToHeadListener = new TopViewSelectHorListToHead.OnClickListener() { // 详情头部点击监听器
        @Override
        public void onClick(View v) { // 点击回调
            PlaySound.getInstance().playButton(); // 播放按键音效
            String[] sampleDetail = getEnvelList(); // 获取详情列表
            if (rgSample.getSelected().getIndex() == 1) { // 平均模式
                selectListToList.setData(R.id.sampleSelectListToHead, getAveList(), selectListToListListener); // 设置平均列表
                int sampleDetailIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_MODE_DETAIL_INDEX); // 读取详情索引
                selectListToList.setSelected(sampleDetailIndex); // 设置选中项
                selectListToList.show(306); // 显示列表
            } else if (rgSample.getSelected().getIndex() == 2) { // 包络模式
                selectListToList.setData(R.id.sampleSelectListToHead, getEnvelList(), selectListToListListener); // 设置包络列表
                int sampleDetailIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_MODE_DETAIL_INDEX); // 读取详情索引
                selectListToList.setSelected(sampleDetailIndex); // 设置选中项
                selectListToList.show(306); // 显示列表
            }
        }
    };

    /**
     * 详情列表选择监听器
     */
    private TopViewSelectHorListToList.OnDialogChangedListener selectListToListListener = new TopViewSelectHorListToList.OnDialogChangedListener() { // 详情列表选择监听器
        @Override
        public void checkChanged(int headViewId, TopBeanHorizontal item) { // 选中回调
            PlaySound.getInstance().playButton(); // 播放按键音效
            onTextChanged(item, false, true); // 触发文本变更
        }

        @Override
        public void onShow() { // 列表显示回调
            RxBus.getInstance().post(RxEnum.DIALOG_OPEN, MainViewGroup.DIALOG_AFTERGLOW); // 通知余辉弹窗打开
        }

        @Override
        public void onHide() { // 列表隐藏回调
            RxBus.getInstance().post(RxEnum.DIALOG_CLOSE, MainViewGroup.DIALOG_AFTERGLOW); // 通知余辉弹窗关闭
        }
    };

    /**
     * 事件观察者 - 监听采样类型变更事件
     */
    private EventUIObserver eventUIObserver = new EventUIObserver() { // 事件观察者
        @Override
        public void update(Object data) { // 事件更新回调
            if (((EventBase) data).getId() == EventFactory.EVENT_SAMPLE_TYPE) { // 如果是采样类型事件
                if (rgSample.getSelected().getIndex() != unMatchSample(Sample.getInstance().getSampleType())) { // 如果与硬件不同
                    rgSample.setSelectedIndex(unMatchSample(Sample.getInstance().getSampleType())); // 更新选中索引
                    onCheckChanged(rgSample, rgSample.getSelected(), true, false, true); // 触发变更
                }
            }
        }
    };

    /**
     * 采样类型变更核心处理
     * @param view 触发变更的视图
     * @param item 选中的项
     * @param isFromEventBus 是否来自EventBus
     * @param isUser 是否来自用户操作
     * @param setCache 是否保存到缓存
     */
    private void onCheckChanged(TopViewRadioGroup view, TopBeanChannel item, boolean isFromEventBus, boolean isUser, boolean setCache) { // 采样类型变更核心处理
        if (view.getId() == R.id.topViewSample) { // 如果是采样类型单选组
            if (isUser) { // 如果是用户操作
                CacheUtil.get().putMap(CacheUtil.RIGHT_SLIP_MATH_AM_FORMULA_DIFF_RESET, String.valueOf(false)); // 清除微分重置标志
            }
            int sampleDetailIndex = CacheUtil.get().getInt(CacheUtil.TOP_SLIP_SAMPLE_MODE_DETAIL_INDEX); // 读取详情索引
            String[] sampleDetailArray = getEnvelList(); // 获取详情列表
            if (item.getIndex() == 1 && sampleDetailIndex == sampleDetailArray.length - 1) { // 平均模式但选了最后一项
                sampleDetailIndex--; // 退回一项
                tvSampleDetail.setText(selectListToList.getSelected().getText()); // 更新详情文本
                onTextChanged(new TopBeanHorizontal(sampleDetailIndex // 触发文本变更
                        , sampleDetailArray[sampleDetailIndex]), isFromEventBus, setCache);
            }
            setDetailEnable(item.getIndex()); // 设置详情启用状态
            if (setCache) { // 如果需要保存缓存
                CacheUtil.get().putMap(CacheUtil.TOP_SLIP_SAMPLE_MODE, String.valueOf(item.getIndex())); // 保存采样类型到缓存
            }
            Command.get().getSample().Type(item.getIndex(), false); // 发送采样类型命令
            if (!isFromEventBus) { // 如果非EventBus来源
                Sample.getInstance().setSampleType(matchSample(item.getIndex())); // 更新采样类型
            }
            if (item.getIndex() == 1) {//平均
                if (!isFromEventBus) { // 非EventBus来源
                    Sample.getInstance().setSampleNum(getSampleNum()); // 更新采样数量
                }
                Command.get().getSample().Mean(sampleDetailIndex, false); // 发送均值命令
            } else if (item.getIndex() == 2) {//包絡
                if (!isFromEventBus) { // 非EventBus来源
                    Sample.getInstance().setSampleNum(getSampleNum()); // 更新采样数量
                }
                Command.get().getSample().Envelop(sampleDetailIndex, false); // 发送包络命令
            }
            msgSample.setSample(item); // 更新消息采样类型
            msgSample.setDetail(tvSampleDetail.getText()); // 更新消息详情
            msgSample.setSampleDetailIndex(sampleDetailIndex); // 更新消息详情索引
            sendMsgSample(isFromEventBus); // 发送采样模式消息
        }
    }

    /**
     * 详情文本变更处理
     * @param item 选中的水平列表项
     * @param isFromEventBus 是否来自EventBus
     * @param setCache 是否保存到缓存
     */
    private void onTextChanged(TopBeanHorizontal item, boolean isFromEventBus, boolean setCache) { // 详情文本变更处理
        if (rgSample.getSelected().getIndex() != 1 && rgSample.getSelected().getIndex() != 2) { // 非平均/包络
            tvSampleDetail.setText(""); // 清空详情
            sendMsgSample(isFromEventBus); // 发送消息
            Command.get().getSample().Mean(0,false); // 重置均值
            Command.get().getSample().Envelop(0,false); // 重置包络
            return; // 返回
        } else { // 平均或包络
            tvSampleDetail.setText(item.getText()); // 更新详情文本
            if (rgSample.getSelected().getIndex()==1) { // 平均模式
                Command.get().getSample().Mean(item.getIndex(), false); // 发送均值命令
            }else if (rgSample.getSelected().getIndex()==2) { // 包络模式
                Command.get().getSample().Envelop(item.getIndex(), false); // 发送包络命令
            }
        }
        if (setCache) { // 如果需要保存缓存
            CacheUtil.get().putMap(CacheUtil.TOP_SLIP_SAMPLE_MODE_DETAIL_INDEX, String.valueOf(item.getIndex())); // 保存详情索引到缓存
        }
        Sample.getInstance().setSampleNum(getSampleNum()); // 更新采样数量
        msgSample.setDetail(tvSampleDetail.getText()); // 更新消息详情
        msgSample.setSampleDetailIndex(item.getIndex()); // 更新消息详情索引
        sendMsgSample(isFromEventBus); // 发送采样模式消息
    }
}
