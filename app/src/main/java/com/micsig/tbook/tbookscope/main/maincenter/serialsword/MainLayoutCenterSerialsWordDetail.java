package com.micsig.tbook.tbookscope.main.maincenter.serialsword;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.micsig.tbook.tbookscope.LoadCache;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.main.mainright.MainRightMsgOthers;
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightLayoutSerials;
import com.micsig.tbook.tbookscope.rightslipmenu.serials.RightMsgSerials;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.top.layout.display.TopMsgDisplay;
import com.micsig.tbook.tbookscope.top.layout.display.TopMsgDisplayTxtMix;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.tbookscope.wavezone.wave.SerialBusManage;

import java.util.HashMap;
import java.util.Objects;

import io.reactivex.rxjava3.functions.Consumer;


/**
 * ***********************************************************************************
 * * 串口总线文本详情显示Fragment容器类
 * ***********************************************************************************
 * *
 * * 【模块定位】
 * *   主界面中心区域 - 串口总线文本显示模块的具体协议显示容器和切换控制器
 * *
 * * 【核心职责】
 * *   1. 管理UART/LIN/CAN/SPI/I2C/M429/M1553B/TIP八个子Fragment的显示切换
 * *   2. 根据通道类型和串口协议类型动态切换显示对应的协议Fragment
 * *   3. 监听RxBus事件并响应缓存加载、配置变更、组合通道变更等消息
 * *   4. 处理外部按键事件，转发滚动命令到当前可见的协议Fragment
 * *   5. 处理运行停止状态，转发运行停止命令到当前可见的协议Fragment
 * *   6. 实现S12组合通道的一致性检查逻辑
 * *
 * * 【架构设计】
 * *   继承Fragment作为容器Fragment，采用嵌套Fragment架构：
 * *   - 使用ChildFragmentManager管理8个子Fragment实例
 * *   - 通过FragmentTransaction实现Fragment显示隐藏切换
 * *   - 利用RxBus订阅多个事件源，实现松耦合的消息驱动
 * *   - 使用HashMap存储通道开启状态和串口类型索引
 * *
 * * 【数据流向】
 * *   输入：
 * *     → CacheUtil读取通道开启状态、串口类型索引
 * *     → RxBus接收LoadCache、MainRightMsgOthers、RightMsgSerials等消息
 * *     → TopMsgDisplay接收组合通道变更消息
 * *   输出：
 * *     → FragmentTransaction切换显示对应的协议Fragment
 * *     → 调用子Fragment的setScrollMove和setRunStop方法
 * *
 * * 【依赖关系】
 * *   上层依赖：MainLayoutCenterSerialsWord父容器布局
 * *   下层依赖：MainLayoutCenterSerialsWordUart/Lin/Can/Spi/I2c/M429/M1553b/Tip各协议Fragment
 * *   平级依赖：CacheUtil、RxBus等工具类
 * *
 * * 【使用场景】
 * *   作为MainLayoutCenterSerialsWord的子Fragment，管理具体协议的显示切换
 * *   当用户切换通道或配置串口协议类型时，自动切换显示对应的协议文本界面
 * ***********************************************************************************
 */
public class MainLayoutCenterSerialsWordDetail extends Fragment {
    private MainLayoutCenterSerialsWordTip tipLayout;  // 提示信息Fragment，显示通道未配置时的提示文本
    private MainLayoutCenterSerialsWordUart uartLayout;  // UART协议文本显示Fragment，显示UART解码数据
    private MainLayoutCenterSerialsWordLin linLayout;  // LIN协议文本显示Fragment，显示LIN总线解码数据
    private MainLayoutCenterSerialsWordCan canLayout;  // CAN协议文本显示Fragment，显示CAN总线解码数据
    private MainLayoutCenterSerialsWordSpi spiLayout;  // SPI协议文本显示Fragment，显示SPI解码数据
    private MainLayoutCenterSerialsWordI2c i2cLayout;  // I2C协议文本显示Fragment，显示I2C解码数据
    private MainLayoutCenterSerialsWordM429 m429Layout;  // ARINC429协议文本显示Fragment，显示M429解码数据
    private MainLayoutCenterSerialsWordM1553b m1553bLayout;  // MIL-STD-1553B协议文本显示Fragment，显示1553B解码数据
    private Fragment visibleLayout;  // 当前可见的Fragment引用，用于转发滚动和运行停止命令

    private String[] tags = {"serialsWordTipLayout", "serialsWordUartLayout"  // Fragment标签数组，用于Fragment查找和恢复
            , "serialsWordLinLayout", "serialsWordCanLayout", "serialsWordSpiLayout"
            , "serialsWordI2cLayout", "serialsWordM429Layout", "serialsWordM1553bLayout"};
    private Fragment[] fragments = new Fragment[8];  // Fragment数组，用于状态恢复时存储已存在的Fragment实例

    private int chType = ISerialsWord.TYPE_S1;  // 通道类型，默认为S1通道
    private String title;  // 通道标题，用于S12组合通道判断一致性
    private HashMap<String, Boolean> checkMap = new HashMap<>();  // 通道开启状态映射表，用于S12组合判断
    private HashMap<String, Integer> indexMap = new HashMap<>();  // 串口类型索引映射表，用于S12组合判断

    /**
     * 设置通道类型
     * @param chType 通道类型常量（TYPE_S1/TYPE_S2/TYPE_S3/TYPE_S4/TYPE_S12）
     */
    public void setChType(int chType) {
        this.chType = chType;  // 保存通道类型到成员变量
    }

    /**
     * 设置通道标题，并同步更新UART Fragment的标题
     * @param title 通道标题字符串，S12组合通道格式为"S1&S2"等
     */
    public void setTitle(String title) {
        this.title = title;  // 保存标题到成员变量
        if (uartLayout != null) {  // 如果UART Fragment已初始化
            uartLayout.setTitle(title);  // 同步更新UART Fragment的标题，用于一致性判断
        }
    }

    /**
     * 创建Fragment视图
     * @param inflater 布局填充器
     * @param container 父容器视图组
     * @param savedInstanceState 保存的状态Bundle
     * @return 填充后的视图对象
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_maincenter_serialsword_detail, container, false);  // 填充详情容器布局文件
    }

    /**
     * 视图创建完成回调，初始化布局和控制器
     * @param view 创建的视图对象
     * @param savedInstanceState 保存的状态Bundle
     */
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        initLayout(savedInstanceState);  // 初始化布局，创建或恢复所有子Fragment
        initControl();  // 初始化控制器，订阅RxBus事件
    }

    /**
     * 初始化控制器，订阅RxBus事件
     */
    private void initControl() {
        RxBus.getInstance().getObservable(RxEnum.MAIN_LOAD_CACHE).subscribe(consumerLoadCache);  // 订阅缓存加载事件
        RxBus.getInstance().getObservable(RxEnum.MAINRIGHT_OTHERS).subscribe(consumerMainRightOther);  // 订阅右侧其他消息事件
        RxBus.getInstance().getObservable(RxEnum.RIGHTLAYOUT_SERIALS).subscribe(consumerRightSerials);  // 订阅右侧串口消息事件
        RxBus.getInstance().getObservable(RxEnum.TOPLAYOUT_DISPLAY).subscribe(consumerShowLayout);  // 订阅顶部显示事件，用于组合通道变更
    }

    /**
     * 初始化布局，创建或恢复八个协议Fragment实例
     * @param savedInstanceState 保存的状态Bundle对象，用于Fragment恢复
     */
    private void initLayout(Bundle savedInstanceState) {
        if (savedInstanceState != null) {  // 如果有保存的状态
            for (int i = 0; i < tags.length; i++) {  // 循环查找已存在的Fragment
                fragments[i] = getChildFragmentManager().findFragmentByTag(tags[i]);  // 通过标签查找Fragment
            }
        }
        tipLayout = fragments[0] == null ? new MainLayoutCenterSerialsWordTip() : (MainLayoutCenterSerialsWordTip) fragments[0];  // 创建或恢复Tip Fragment
        tipLayout.setChType(chType);  // 设置Tip Fragment的通道类型
        uartLayout = fragments[1] == null ? new MainLayoutCenterSerialsWordUart() : (MainLayoutCenterSerialsWordUart) fragments[1];  // 创建或恢复UART Fragment
        uartLayout.setChType(chType);  // 设置UART Fragment的通道类型
        linLayout = fragments[2] == null ? new MainLayoutCenterSerialsWordLin() : (MainLayoutCenterSerialsWordLin) fragments[2];  // 创建或恢复LIN Fragment
        linLayout.setChType(chType);  // 设置LIN Fragment的通道类型
        canLayout = fragments[3] == null ? new MainLayoutCenterSerialsWordCan() : (MainLayoutCenterSerialsWordCan) fragments[3];  // 创建或恢复CAN Fragment
        canLayout.setChType(chType);  // 设置CAN Fragment的通道类型
        spiLayout = fragments[4] == null ? new MainLayoutCenterSerialsWordSpi() : (MainLayoutCenterSerialsWordSpi) fragments[4];  // 创建或恢复SPI Fragment
        spiLayout.setChType(chType);  // 设置SPI Fragment的通道类型
        i2cLayout = fragments[5] == null ? new MainLayoutCenterSerialsWordI2c() : (MainLayoutCenterSerialsWordI2c) fragments[5];  // 创建或恢复I2C Fragment
        i2cLayout.setChType(chType);  // 设置I2C Fragment的通道类型
        m429Layout = fragments[6] == null ? new MainLayoutCenterSerialsWordM429() : (MainLayoutCenterSerialsWordM429) fragments[6];  // 创建或恢复M429 Fragment
        m429Layout.setChType(chType);  // 设置M429 Fragment的通道类型
        m1553bLayout = fragments[7] == null ? new MainLayoutCenterSerialsWordM1553b() : (MainLayoutCenterSerialsWordM1553b) fragments[7];  // 创建或恢复1553B Fragment
        m1553bLayout.setChType(chType);  // 设置1553B Fragment的通道类型

        if (savedInstanceState == null) {  // 如果是新创建状态
            getChildFragmentManager().beginTransaction()  // 开启子Fragment事务
                    .add(R.id.fragmentLayout, tipLayout, tags[0])  // 添加Tip Fragment
                    .add(R.id.fragmentLayout, uartLayout, tags[1])  // 添加UART Fragment
                    .add(R.id.fragmentLayout, linLayout, tags[2])  // 添加LIN Fragment
                    .add(R.id.fragmentLayout, canLayout, tags[3])  // 添加CAN Fragment
                    .add(R.id.fragmentLayout, spiLayout, tags[4])  // 添加SPI Fragment
                    .add(R.id.fragmentLayout, i2cLayout, tags[5])  // 添加I2C Fragment
                    .add(R.id.fragmentLayout, m429Layout, tags[6])  // 添加M429 Fragment
                    .add(R.id.fragmentLayout, m1553bLayout, tags[7])  // 添加1553B Fragment
                    .hide(uartLayout)  // 隐藏UART Fragment
                    .hide(linLayout)  // 隐藏LIN Fragment
                    .hide(canLayout)  // 隐藏CAN Fragment
                    .hide(spiLayout)  // 隐藏SPI Fragment
                    .hide(i2cLayout)  // 隐藏I2C Fragment
                    .hide(m429Layout)  // 隐藏M429 Fragment
                    .hide(m1553bLayout)  // 隐藏1553B Fragment
                    .commitAllowingStateLoss();  // 提交事务，允许状态丢失
        }
        visibleLayout = tipLayout;  // 默认显示Tip Fragment
    }

    private void setCache() {
        boolean s1Check = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S1);
        boolean s2Check = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S2);
        boolean s3Check = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S3);
        boolean s4Check = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S4);
        int s1Index = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + CacheUtil.S1);
        int s2Index = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + CacheUtil.S2);
        int s3Index = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + CacheUtil.S3);
        int s4Index = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + CacheUtil.S4);
        checkMap.put("S1", s1Check);
        checkMap.put("S2", s2Check);
        checkMap.put("S3", s3Check);
        checkMap.put("S4", s4Check);
        indexMap.put("S1", s1Index);
        indexMap.put("S2", s2Index);
        indexMap.put("S3", s3Index);
        indexMap.put("S4", s4Index);
        boolean check = false;
        int index = 0;
        if (chType == ISerialsWord.TYPE_S1) {
            check = s1Check;
            index = s1Index;
        } else if (chType == ISerialsWord.TYPE_S2) {
            check = s2Check;
            index = s2Index;
        } else if (chType == ISerialsWord.TYPE_S3) {
            check = s3Check;
            index = s3Index;
        } else if (chType == ISerialsWord.TYPE_S4) {
            check = s4Check;
            index = s4Index;
        } else if (chType == ISerialsWord.TYPE_S12) {
            check = getS12Check();
            index = getS12Index();
            SerialBusManage.getInstance().getSerialTxtBuffer(ISerialsWord.TYPE_S12)
                    .setOpenS1S2(check, index + 1);
            if ( isSelectAndOpen() && check==false){
                tipLayout.setTip(R.string.serialsWordUartTip);
            }else {
                tipLayout.setTip(R.string.serialsWordTip);
            }
        }

        getChildFragmentManager().beginTransaction()
                .hide(tipLayout)
                .hide(uartLayout)
                .hide(linLayout)
                .hide(canLayout)
                .hide(spiLayout)
                .hide(i2cLayout)
                .hide(m429Layout)
                .hide(m1553bLayout)
                .commitAllowingStateLoss();
        if (check) {
            switch (index) {
                case RightLayoutSerials.SERIALS_UART:
                    getChildFragmentManager().beginTransaction().show(uartLayout).commitAllowingStateLoss();
                    visibleLayout = uartLayout;
                    break;
                case RightLayoutSerials.SERIALS_LIN:
                    getChildFragmentManager().beginTransaction().show(linLayout).commitAllowingStateLoss();
                    visibleLayout = linLayout;
                    break;
                case RightLayoutSerials.SERIALS_CAN:
                    getChildFragmentManager().beginTransaction().show(canLayout).commitAllowingStateLoss();
                    visibleLayout = canLayout;
                    break;
                case RightLayoutSerials.SERIALS_SPI:
                    getChildFragmentManager().beginTransaction().show(spiLayout).commitAllowingStateLoss();
                    visibleLayout = spiLayout;
                    break;
                case RightLayoutSerials.SERIALS_I2C:
                    getChildFragmentManager().beginTransaction().show(i2cLayout).commitAllowingStateLoss();
                    visibleLayout = i2cLayout;
                    break;
                case RightLayoutSerials.SERIALS_M429:
                    getChildFragmentManager().beginTransaction().show(m429Layout).commitAllowingStateLoss();
                    visibleLayout = m429Layout;
                    break;
                case RightLayoutSerials.SERIALS_M1553B:
                    getChildFragmentManager().beginTransaction().show(m1553bLayout).commitAllowingStateLoss();
                    visibleLayout = m1553bLayout;
                    break;
                case -1:
                    getChildFragmentManager().beginTransaction().show(tipLayout).commitAllowingStateLoss();
                    visibleLayout = tipLayout;
                    break;
            }
        } else {
            getChildFragmentManager().beginTransaction().show(tipLayout).commitAllowingStateLoss();
            visibleLayout = tipLayout;
        }
    }

    private boolean isSelectAndOpen(){
        boolean s1Check = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S1);
        boolean s2Check = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S2);
        boolean s3Check = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S3);
        boolean s4Check = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S4);
        boolean s1Select = CacheUtil.get().getBoolean(CacheUtil.SERIAL_TXT_SELECT + CacheUtil.S1);
        boolean s2Select = CacheUtil.get().getBoolean(CacheUtil.SERIAL_TXT_SELECT + CacheUtil.S2);
        boolean s3Select = CacheUtil.get().getBoolean(CacheUtil.SERIAL_TXT_SELECT + CacheUtil.S3);
        boolean s4Select = CacheUtil.get().getBoolean(CacheUtil.SERIAL_TXT_SELECT + CacheUtil.S4);
        boolean[] select = {s1Select, s2Select, s3Select, s4Select};
        boolean[] check = {s1Check, s2Check, s3Check, s4Check};

        for(int i=0;i<select.length;i++){
            if (select[i] && check[i]==false){
                return false;
            }
        }
        return true;
    }
    private boolean getS12Check() {
        String[] titles = title.split("&");
        boolean finalCheck = true;
        boolean finalIndex = true;
        String preKey = "";
        String nowKey = "";
        for (String s : titles) {
            if (checkMap.containsKey(s)) {
                finalCheck = finalCheck && Boolean.TRUE.equals(checkMap.get(s));
            }
            if (indexMap.containsKey(s)) {
                preKey = nowKey;
                nowKey = s;
                if(preKey.isEmpty()) continue;
                finalIndex = finalIndex && (Objects.equals(indexMap.get(preKey), indexMap.get(nowKey)));
            }
        }
        return finalCheck && finalIndex;
    }

    private int getS12Index() {
        String[] titles = title.split("&");
        int finalIndex = -1;
        for (String s : titles) {
            if (indexMap.containsKey(s) && indexMap.get(s) != null) {
                finalIndex = indexMap.get(s);
                break;

            }
        }
        return finalIndex;
    }

    public void setScrollMove(int moveCount) {
        if (visibleLayout == uartLayout) {
            uartLayout.setScrollMove(moveCount);
        } else if (visibleLayout == linLayout) {
            linLayout.setScrollMove(moveCount);
        } else if (visibleLayout == canLayout) {
            canLayout.setScrollMove(moveCount);
        } else if (visibleLayout == spiLayout) {
            spiLayout.setScrollMove(moveCount);
        } else if (visibleLayout == i2cLayout) {
            i2cLayout.setScrollMove(moveCount);
        } else if (visibleLayout == m429Layout) {
            m429Layout.setScrollMove(moveCount);
        } else if (visibleLayout == m1553bLayout) {
            m1553bLayout.setScrollMove(moveCount);
        }
    }

    public void setRunStop(boolean run) {
        boolean s1Check = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S1);
        boolean s2Check = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S2);
        boolean s3Check = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S3);
        boolean s4Check = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S4);
        int s1Index = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + CacheUtil.S1);
        int s2Index = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + CacheUtil.S2);
        int s3Index = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + CacheUtil.S3);
        int s4Index = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_SERIALS + CacheUtil.S4);
        boolean check = false;
        int index = 0;
        if (chType == ISerialsWord.TYPE_S1) {
            check = s1Check;
            index = s1Index;
        } else if (chType == ISerialsWord.TYPE_S2) {
            check = s2Check;
            index = s2Index;
        } else if (chType == ISerialsWord.TYPE_S3) {
            check = s3Check;
            index = s3Index;
        } else if (chType == ISerialsWord.TYPE_S4) {
            check = s4Check;
            index = s4Index;
        } else if (chType == ISerialsWord.TYPE_S12) {
            check = getS12Check();
            index = getS12Index();
        }
//        Logger.i("MainlayoutCenterSerialsWordDetail:setRunStop" + "chType:" + chType + " check:" + check+" index:"+index);
        if (check) {
            switch (index) {
                case RightLayoutSerials.SERIALS_UART:
                    uartLayout.setRunStop(run);
                    break;
                case RightLayoutSerials.SERIALS_LIN:
                    linLayout.setRunStop(run);
                    break;
                case RightLayoutSerials.SERIALS_CAN:
                    canLayout.setRunStop(run);
                    break;
                case RightLayoutSerials.SERIALS_SPI:
                    spiLayout.setRunStop(run);
                    break;
                case RightLayoutSerials.SERIALS_I2C:
                    i2cLayout.setRunStop(run);
                    break;
                case RightLayoutSerials.SERIALS_M429:
                    m429Layout.setRunStop(run);
                    break;
                case RightLayoutSerials.SERIALS_M1553B:
                    m1553bLayout.setRunStop(run);
                    break;
            }
        }
    }

    private Consumer<LoadCache> consumerLoadCache = new Consumer<LoadCache>() {
        @Override
        public void accept(LoadCache loadCache) throws Exception {
            setCache();
        }
    };

    private Consumer<MainRightMsgOthers> consumerMainRightOther = new Consumer<MainRightMsgOthers>() {
        @Override
        public void accept(MainRightMsgOthers mainRightMsgOthers) throws Exception {
            setCache();
        }
    };

    private Consumer<RightMsgSerials> consumerRightSerials = new Consumer<RightMsgSerials>() {
        @Override
        public void accept(RightMsgSerials rightMsgSerials) throws Exception {
            if ((rightMsgSerials.isSerials1() && chType == ISerialsWord.TYPE_S1)
                    || (rightMsgSerials.isSerials2() && chType == ISerialsWord.TYPE_S2)
                    || (rightMsgSerials.isSerials3() && chType == ISerialsWord.TYPE_S3)
                    || (rightMsgSerials.isSerials4() && chType == ISerialsWord.TYPE_S4)
                    || chType == ISerialsWord.TYPE_S12) {
                setCache();
            }
        }
    };

    private Consumer<TopMsgDisplay> consumerShowLayout = new Consumer<TopMsgDisplay>() {
        @Override
        public void accept(TopMsgDisplay topMsgDisplay) throws Exception {
            if (topMsgDisplay != null && topMsgDisplay.getDisplayDetail() instanceof TopMsgDisplayTxtMix) {
                setCache();
            }
        }
    };
}
