package com.micsig.tbook.tbookscope.rightslipmenu.dialog;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;

import com.micsig.tbook.tbookscope.MainActivity;
import com.micsig.tbook.tbookscope.MainViewGroup;
import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.PlaySound;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.TopDialogNumberKeyBoard;
import com.micsig.tbook.ui.rightslipmenu.RightBeanSelect;
import com.micsig.tbook.ui.rightslipmenu.RightViewSelect;
import com.micsig.tbook.ui.rightslipmenu.RightViewUserDefineEdit;
import com.micsig.tbook.ui.util.StrUtil;

/**
 * Created by yangj on 2017/5/4.
 */

/*
 * +----------------------------------------------------------------------+
 * |                        DialogBaudRate                                |
 * |                       波特率设置对话框                                 |
 * +----------------------------------------------------------------------+
 * | 模块定位: 右侧滑出菜单 -> 对话框子包                                    |
 * | 核心职责: 提供示波器串口波特率的预设选择和自定义输入界面，                   |
 * |          支持B/s/KB/s/MB/s单位，范围1200~8MB/s                         |
 * | 架构设计: 继承AbsoluteLayout的自定义对话框视图，                          |
 * |          内嵌RightViewSelect预设列表+RightViewUserDefineEdit自定义输入，  |
 * |          自定义输入通过TopDialogNumberKeyBoard数字键盘完成               |
 * | 数据流向: 外部调用setData()传入当前波特率 -> 用户选择预设或自定义输入 ->    |
 * |          OnDismissListener回调返回最终波特率字符串                       |
 * | 依赖关系: RxBus(对话框开关事件), PlaySound(按键音效),                    |
 * |          TopDialogNumberKeyBoard(数字键盘对话框), StrUtil(字符串工具)    |
 * | 使用场景: 串口通信波特率设置菜单项点击后弹出                              |
 * +----------------------------------------------------------------------+
 */
public class DialogBaudRate extends AbsoluteLayout {
    private Context context;  // 上下文引用
    private RightViewSelect baudRate;  // 波特率预设选择列表
    private RightViewUserDefineEdit defineEdit;  // 自定义波特率输入控件
    private TopDialogNumberKeyBoard dialogKeyBoard;  // 数字键盘对话框（懒加载）
    private double preDouble;  // 上一次波特率的数值部分
    private String preBs;  // 上一次波特率的单位部分（B/s, KB/s, MB/s）
    private OnDismissListener onDismissListener;  // 对话框关闭回调监听器

    private ViewGroup rootViewGroup;  // 根视图组

    /**
     * 对话框关闭监听接口。
     * <p>
     * 当波特率选择完成时回调onDismiss返回结果。
     */
    public interface OnDismissListener {
        /** 对话框关闭时回调，返回最终波特率字符串 */
        void onDismiss(String result);
    }

    /**
     * 单参数构造方法，委托给两参数构造。
     *
     * @param context 上下文
     */
    public DialogBaudRate(Context context) {
        this(context, null);  // 委托给两参数构造
    }

    /**
     * 两参数构造方法，委托给三参数构造。
     *
     * @param context 上下文
     * @param attrs   属性集
     */
    public DialogBaudRate(Context context, AttributeSet attrs) {
        this(context, attrs, 0);  // 委托给三参数构造
    }

    /**
     * 三参数构造方法，初始化上下文并调用init()加载布局。
     *
     * @param context      上下文
     * @param attrs        属性集
     * @param defStyleAttr 默认样式属性
     */
    public DialogBaudRate(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);  // 调用父类构造
        this.context = context;               // 保存上下文引用
        init();                               // 执行初始化
    }

    //[366, 366]	335	170

    /**
     * 初始化对话框布局和交互逻辑。
     * <p>
     * 加载dialog_baudrate布局，设置外部区域点击关闭监听，
     * 初始化内部视图控件，然后隐藏对话框。
     */
    private void init() {
        setClickable(true);  // 设置可点击，拦截触摸事件
        rootViewGroup =(ViewGroup) inflate(context, R.layout.dialog_baudrate, this);  // 填充布局

        rootViewGroup.findViewById(R.id.outView).setOnTouchListener(new OnTouchListener() {  // 设置外部区域触摸监听
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hide();      // 直接隐藏对话框（不回调结果）
                return false;  // 不消费触摸事件
            }
        });
        initView(rootViewGroup);  // 初始化内部视图控件
        hide();  // 默认隐藏对话框
    }

    /**
     * 初始化内部视图控件，绑定ID和事件监听器。
     *
     * @param view 根视图，用于查找子控件
     */
    private void initView(View view) {
        defineEdit = (RightViewUserDefineEdit) view.findViewById(R.id.defineEdit);  // 自定义输入控件
        baudRate = (RightViewSelect) view.findViewById(R.id.baudRate);  // 预设选择列表
        baudRate.setDoubleCheckEqual(true);  // 启用double值精确匹配（避免浮点误差导致选中失败）
        baudRate.setOnItemClick2Listener(onItemClick2Listener);  // 预设项点击监听
        defineEdit.setOnEditClickListener(onEditClickListener);  // 自定义输入点击监听
    }

    /**
     * 显示对话框，并通过RxBus通知对话框打开事件。
     */
    public void show() {
        setVisibility(VISIBLE);  // 设置视图可见
        RxBus.getInstance().post(RxEnum.DIALOG_OPEN, MainViewGroup.DIALOG_BAUDRATE);  // 发送对话框打开事件
        Tools.PrintControlsLocation("DialogBaudRate",rootViewGroup);  // 打印控件位置调试信息
    }

    /**
     * 隐藏对话框，并通过RxBus通知对话框关闭事件。
     */
    public void hide() {
        setVisibility(GONE);  // 设置视图不可见且不占位
        RxBus.getInstance().post(RxEnum.DIALOG_CLOSE, MainViewGroup.DIALOG_BAUDRATE);  // 发送对话框关闭事件
    }

    /**
     * 设置对话框数据并显示。
     * <p>
     * 解析传入的波特率字符串，判断是否匹配预设列表中的选项，
     * 匹配则选中预设项，否则显示在自定义输入框中。
     *
     * @param chIdx            通道索引，用于设置控件颜色
     * @param preString        当前波特率字符串（如"9600B/s"或"1.5MB/s"）
     * @param onDismissListener 关闭回调监听器
     */
    public void setData(int chIdx,String preString, OnDismissListener onDismissListener) {
        baudRate.setControlColorByChIdx(chIdx);  // 根据通道索引设置控件颜色
        if (!StrUtil.isEmpty(preString)) {  // 当前波特率非空
            String number = preString.replace(TopDialogNumberKeyBoard.KEYBOARD_KBS, "").replace(TopDialogNumberKeyBoard.KEYBOARD_MBS, "").replace(TopDialogNumberKeyBoard.KEYBOARD_BS, "");  // 去除单位后提取数值
            preDouble = Double.parseDouble(number);  // 解析数值部分
            preBs = preString.replace(number, "");  // 提取单位部分
        } else {  // 当前波特率为空
            preDouble = 0;  // 默认数值0
            preBs = TopDialogNumberKeyBoard.KEYBOARD_KBS;  // 默认单位KB/s
        }
        this.onDismissListener = onDismissListener;  // 保存关闭监听器
        boolean flag = false;  // 标记是否匹配预设项
        for (RightBeanSelect item : baudRate.getList()) {  // 遍历预设列表
            if (item.getText().equals(preString)) {  // 文本完全匹配
                flag = true;  // 标记匹配成功
                break;  // 跳出循环
            }
        }
        if (flag) {  // 匹配预设项
            baudRate.setPreString(preString);  // 选中对应的预设项
            defineEdit.setText("");  // 清空自定义输入
        } else {  // 不匹配预设项
            baudRate.clearSelect();  // 清除预设选中状态
            defineEdit.setText(preString);  // 显示在自定义输入框
        }

        show();  // 显示对话框
    }

    /**
     * 处理选择结果，隐藏对话框并回调监听器。
     *
     * @param result 最终波特率字符串
     */
    private void onResult(String result) {
        hide();  // 隐藏对话框
        if (onDismissListener != null) {  // 如果设置了关闭监听器
            onDismissListener.onDismiss(result);  // 回调结果
        }
    }

    /**
     * 自定义输入控件的点击监听器。
     * <p>
     * 点击自定义输入框时，清除预设选中状态并弹出数字键盘对话框。
     */
    private RightViewUserDefineEdit.OnEditClickListener onEditClickListener = new RightViewUserDefineEdit.OnEditClickListener() {
        @Override
        public void onEditClick(RightViewUserDefineEdit view, String text) {
            baudRate.clearSelect();  // 清除预设选中状态
            if (dialogKeyBoard == null) {  // 数字键盘对话框懒加载
                dialogKeyBoard = (TopDialogNumberKeyBoard) ((MainActivity) context).findViewById(R.id.dialogNumberKeyBoard);  // 从MainActivity获取
            }
            dialogKeyBoard.setBaudRateData(preDouble, preBs, 1200, 8 * 1000 * 1000, onKeyBoardDismissListener);  // 设置键盘数据（范围1200~8MB/s）
        }
    };

    /**
     * 预设列表项点击监听器。
     * <p>
     * 点击预设项时：如果是普通预设项则直接返回结果；
     * 如果是"自定义"项则弹出数字键盘对话框。
     */
    private RightViewSelect.OnItemClick2Listener onItemClick2Listener = new RightViewSelect.OnItemClick2Listener() {
        @Override
        public void onItemClick2(int parentsViewId, View itemView, RightBeanSelect item) {
            PlaySound.getInstance().playButton();  // 播放按键音效
            if (parentsViewId == baudRate.getId()) {  // 确认是波特率列表的点击
                if (!item.isUserDefine(context)) {  // 非自定义项
                    onResult(item.getText());  // 直接返回预设值
                } else {  // 自定义项
                    if (dialogKeyBoard == null) {  // 数字键盘对话框懒加载
                        dialogKeyBoard = (TopDialogNumberKeyBoard) ((MainActivity) context).findViewById(R.id.dialogNumberKeyBoard);  // 从MainActivity获取
                    }
                    dialogKeyBoard.setBaudRateData(preDouble, preBs, 1200, 8 * 1000 * 1000, onKeyBoardDismissListener);  // 设置键盘数据
                }
            }
        }
    };

    /**
     * 数字键盘对话框关闭监听器。
     * <p>
     * 当数字键盘关闭时，解析输入结果：
     * 1. 提取单位（MB/s, KB/s, B/s）
     * 2. 截断数值（最多4位有效数字，含小数点最多5位）
     * 3. 拼接单位后回调结果
     */
    private TopDialogNumberKeyBoard.OnDismissListener onKeyBoardDismissListener = new TopDialogNumberKeyBoard.OnDismissListener() {
        @Override
        public void onDismiss(String result) {
            String unit = "";  // 单位字符串
            if (result.contains(TopDialogNumberKeyBoard.KEYBOARD_MBS)) {  // 包含MB/s
                unit = TopDialogNumberKeyBoard.KEYBOARD_MBS;  // 提取MB/s单位
                result = result.replace(TopDialogNumberKeyBoard.KEYBOARD_MBS, "");  // 去除单位
            } else if (result.contains(TopDialogNumberKeyBoard.KEYBOARD_KBS)) {  // 包含KB/s
                unit = TopDialogNumberKeyBoard.KEYBOARD_KBS;  // 提取KB/s单位
                result = result.replace(TopDialogNumberKeyBoard.KEYBOARD_KBS, "");  // 去除单位
            } else {  // 默认B/s
                unit = TopDialogNumberKeyBoard.KEYBOARD_BS;  // 使用B/s单位
                result = result.replace(TopDialogNumberKeyBoard.KEYBOARD_BS, "");  // 去除单位
            }

            if (result.length() > 4 && result.substring(0, 4).contains(".")) {  // 数值超4位且前4位含小数点
                result = result.substring(0, 5);  // 保留小数点后1位，共5位
            } else if (result.length() > 4) {  // 数值超4位且无小数点
                result = result.substring(0, 4);  // 保留前4位
            }

            onResult(result + unit);  // 拼接数值和单位后回调
        }
    };
}
