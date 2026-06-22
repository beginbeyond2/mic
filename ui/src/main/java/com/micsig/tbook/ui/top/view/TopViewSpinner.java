package com.micsig.tbook.ui.top.view; // 包名：顶部视图组件包，存放顶部面板相关的自定义视图

import android.annotation.SuppressLint; // 注解：用于抑制lint警告，如NewApi等
import android.content.Context; // 上下文对象，用于访问系统资源和服务
import android.content.res.TypedArray; // 类型化数组，用于读取XML自定义属性
import android.util.AttributeSet; // 属性集，用于XML布局中声明的属性
import android.view.Gravity; // 对齐方式常量，如CENTER_VERTICAL
import android.view.View; // 视图基类，提供inflate等静态方法
import android.view.ViewGroup; // 视图组基类，提供布局参数
import android.widget.AdapterView; // 列表项点击事件接口，如OnItemSelectedListener
import android.widget.ArrayAdapter; // 数组适配器，将字符串数组绑定到Spinner等列表控件
import android.widget.BaseAdapter; // 适配器基类
import android.widget.LinearLayout; // 线性布局，本类的父类
import android.widget.RadioButton; // 单选按钮（本文件未使用，属于冗余导入）
import android.widget.Spinner; // 下拉列表控件
import android.widget.TextView; // 文本视图控件

import androidx.annotation.Nullable; // 注解：标记参数可为null

import com.micsig.base.Logger; // 日志工具类
import com.micsig.tbook.ui.R; // UI模块资源类，包含布局、样式、颜色等资源ID
import com.micsig.tbook.ui.util.FileBeanToStr; // FileBean转字符串工具类，提取显示名称或路径
import com.molihuan.pathselector.entity.FileBean; // 文件实体类，来自pathselector库，封装文件路径和显示名

import org.w3c.dom.Text; // DOM文本节点（本文件未使用，属于冗余导入）

import java.util.ArrayList; // 动态数组列表，用于存储数据集合

/**
 * <pre>
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║                        TopViewSpinner - 带标题的下拉选择视图                    ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  模块定位：com.micsig.tbook.ui.top.view                                     ║
 * ║  所属层级：UI层 → 顶部面板 → 自定义视图组件                                     ║
 * ║  核心职责：提供"标题 + Spinner下拉列表"的组合视图，支持文件目录选择等场景           ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  架构设计：                                                                  ║
 * ║    - 继承 LinearLayout，水平排列标题(TextView)和下拉列表(Spinner)              ║
 * ║    - 通过 XML 自定义属性(headWidth/editWidth)控制标题和Spinner的宽度           ║
 * ║    - 使用 ArrayAdapter<String> 绑定显示数据，FileBean 存储原始数据             ║
 * ║    - 通过 onItemSelectListener 回调接口向外部通知选中项变化                     ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  数据流向：                                                                  ║
 * ║    外部调用setData() → FileBean列表 → FileBeanToStr提取显示名 →               ║
 * ║    ArrayAdapter绑定到Spinner → 用户选择 → onItemSelectListener回调FileBean   ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  依赖关系：                                                                  ║
 * ║    - FileBean (com.molihuan.pathselector.entity) : 文件实体数据模型            ║
 * ║    - FileBeanToStr (com.micsig.tbook.ui.util) : FileBean→显示名/路径转换      ║
 * ║    - R.layout.view_top_spinner : 布局文件(merge根，含headView+spinner)        ║
 * ║    - R.styleable.TopViewSpinner : XML自定义属性(headWidth, editWidth)         ║
 * ║    - ArrayAdapter : Spinner数据适配器                                         ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  使用示例：                                                                  ║
 * ║    // XML布局中声明                                                          ║
 * ║    &lt;com.micsig.tbook.ui.top.view.TopViewSpinner                             ║
 * ║        android:id="@+id/dirSpinner"                                          ║
 * ║        app:headWidth="100px"                                                 ║
 * ║        app:editWidth="600px" /&gt;                                              ║
 * ║                                                                              ║
 * ║    // Java代码中设置数据                                                      ║
 * ║    TopViewSpinner spinner = findViewById(R.id.dirSpinner);                   ║
 * ║    spinner.setData("保存目录", fileBeanList,                                  ║
 * ║        R.layout.layout_item_for_save_directory, fileBean -> {                ║
 * ║            // 处理选中项                                                      ║
 * ║        });                                                                   ║
 * ║                                                                              ║
 * ║    // 获取选中项路径                                                          ║
 * ║    String path = spinner.getSelectItem();                                    ║
 * ║    String displayName = spinner.getDisPlaySelectItem();                      ║
 * ║                                                                              ║
 * ║    // 设置只读模式                                                            ║
 * ║    spinner.setReadOnly(false);                                               ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  XML自定义属性：                                                              ║
 * ║    - headWidth (dimension) : 标题文本宽度，默认100px                           ║
 * ║    - editWidth (dimension) : Spinner下拉列表宽度，默认200px                    ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║  典型使用场景：                                                                ║
 * ║    TopLayoutSavePicture / TopLayoutSaveSession / TopLayoutSaveCsv 等          ║
 * ║    保存对话框中的目录选择Spinner                                               ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 * </pre>
 *
 * @author Micsig Technology
 * @version 1.0
 * @since 2024
 * @see FileBean 文件实体类，封装文件路径和显示名称
 * @see FileBeanToStr FileBean与字符串列表转换工具
 * @see Spinner Android下拉列表控件
 */
public class TopViewSpinner extends LinearLayout { // 继承LinearLayout，实现水平排列的标题+Spinner组合视图

    /** 上下文对象，用于获取资源、inflate布局等操作 */
    private Context context;

    /** 标题文本区域的宽度（单位：像素），通过XML属性headWidth配置，默认100px */
    private int headWidth;

    /** Spinner下拉列表的宽度（单位：像素），通过XML属性editWidth配置，默认200px */
    private int spinnerWidth;

    /** 标题文本内容，显示在Spinner左侧的TextView中 */
    private String titleString;

    /** 显示名称字符串列表，由FileBeanToStr从beanList提取，用于ArrayAdapter绑定到Spinner */
    private ArrayList<String> dataList = new ArrayList<>();

    /** FileBean原始数据列表，保存完整的文件信息（路径+显示名），用于回调时传递选中项 */
    private ArrayList<FileBean> beanList = new ArrayList<>();

    /** 选中项变化监听器，当Spinner选中项改变时回调，传递选中的FileBean对象 */
    private onItemSelectListener itemSelectListener;

    /** Spinner的数据适配器，将dataList中的显示名称字符串绑定到Spinner列表项 */
    private ArrayAdapter<String> adapterForSpinner;

    /** 当前选中项的位置索引，初始值为INVALID_POSITION（-1），表示无选中项 */
    private int selectPosition = AdapterView.INVALID_POSITION;

    /** 标题文本视图，显示在Spinner左侧，如"保存目录"等标签文字 */
    private TextView headTxtView;

    /** 下拉列表控件，展示可选文件目录列表，用户点击后弹出下拉选项 */
    private Spinner spinner;

    /** Spinner列表项的布局资源ID，由setData()传入，决定下拉项的视觉样式，默认-1表示未设置 */
    private int itemLayoutId = -1;

    /**
     * 选中项变化监听器接口
     *
     * <p>当Spinner的选中项发生变化时，通过此接口通知外部调用者。</p>
     * <p>回调参数为选中的FileBean对象，包含文件的完整路径和显示名称。</p>
     *
     * <p>使用示例：</p>
     * <pre>
     * TopViewSpinner.onItemSelectListener listener = fileBean -&gt; {
     *     String path = fileBean.getPath();         // 获取选中文件路径
     *     String name = fileBean.getDisplayName();   // 获取选中文件显示名
     * };
     * </pre>
     */
    public interface onItemSelectListener { // 选中项变化监听器接口定义
        /**
         * 选中项变化回调方法
         *
         * @param str 选中的FileBean对象，包含文件路径和显示名称
         */
        void onItemSelected(FileBean str); // 回调方法，传递选中的FileBean
    }

    /**
     * 单参数构造方法
     *
     * <p>用于在Java代码中动态创建TopViewSpinner实例。</p>
     * <p>委托给三参数构造方法，attrs和defStyleAttr使用默认值。</p>
     *
     * @param context 上下文对象，用于访问系统资源和服务
     */
    public TopViewSpinner(Context context) { // 单参数构造方法
        this(context, null); // 委托给两参数构造方法，attrs传null
    }

    /**
     * 两参数构造方法
     *
     * <p>用于在XML布局中声明TopViewSpinner时由系统反射调用。</p>
     * <p>委托给三参数构造方法，defStyleAttr使用默认值0。</p>
     *
     * @param context 上下文对象
     * @param attrs   XML属性集，包含布局文件中声明的属性（如headWidth、editWidth）
     */
    public TopViewSpinner(Context context, @Nullable AttributeSet attrs) { // 两参数构造方法
        this(context, attrs, 0); // 委托给三参数构造方法，defStyleAttr传0
    }

    /**
     * 三参数构造方法（主构造方法）
     *
     * <p>所有构造方法最终委托到此方法，完成视图的初始化工作。</p>
     * <p>初始化流程：保存上下文 → 调用initView()加载布局和读取自定义属性。</p>
     *
     * @param context      上下文对象
     * @param attrs        XML属性集，可能为null
     * @param defStyleAttr 默认样式属性，0表示不使用默认样式
     */
    public TopViewSpinner(Context context, @Nullable AttributeSet attrs, int defStyleAttr) { // 三参数构造方法
        super(context, attrs, defStyleAttr); // 调用父类LinearLayout的构造方法
        this.context = context; // 保存上下文引用
        initView(attrs, defStyleAttr); // 初始化视图：加载布局、读取属性、设置布局参数
    }

    /**
     * 初始化视图
     *
     * <p>完成以下初始化工作：</p>
     * <ol>
     *   <li>加载布局文件 view_top_spinner.xml（merge布局，含headView和spinner）</li>
     *   <li>读取XML自定义属性（headWidth、editWidth）</li>
     *   <li>设置LinearLayout为水平方向、垂直居中对齐</li>
     *   <li>查找子视图引用（headTxtView、spinner）</li>
     *   <li>调用updateView()更新视图显示</li>
     * </ol>
     *
     * @param attrs        XML属性集，用于读取自定义属性
     * @param defStyleAttr 默认样式属性
     */
    private void initView(AttributeSet attrs, int defStyleAttr) { // 初始化视图方法
        View.inflate(context, R.layout.view_top_spinner, this); // 加载布局文件到当前LinearLayout中
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.TopViewSpinner); // 获取自定义属性的类型化数组
        headWidth = ta.getDimensionPixelSize(R.styleable.TopViewSpinner_headWidth, 100); // 读取标题宽度属性，默认100px
        spinnerWidth = ta.getDimensionPixelSize(R.styleable.TopViewSpinner_editWidth, 200); // 读取Spinner宽度属性，默认200px
        ta.recycle(); // 回收TypedArray，避免内存泄漏
        setOrientation(HORIZONTAL); // 设置LinearLayout为水平方向排列
        setGravity(Gravity.CENTER_VERTICAL); // 设置子视图垂直居中对齐
        headTxtView = findViewById(R.id.headView); // 查找标题文本视图
        spinner = findViewById(R.id.action_spinner); // 查找Spinner下拉列表控件
        updateView(); // 更新视图显示（设置文本、宽度、适配器等）
    }

    /**
     * 设置数据并初始化视图
     *
     * <p>此方法是TopViewSpinner的主要数据入口，将标题、数据列表、布局和监听器一次性设置。</p>
     * <p>调用后会将FileBean列表转换为显示名称字符串列表，并刷新Spinner的显示。</p>
     *
     * <p>数据转换流程：</p>
     * <pre>
     * ArrayList&lt;FileBean&gt; beanList → FileBeanToStr.getDisPlayStrList() → ArrayList&lt;String&gt; dataList
     * </pre>
     *
     * @param title         标题文本，显示在Spinner左侧，如"保存目录"
     * @param dataList      FileBean数据列表，包含可选的文件项信息
     * @param itemLayoutId  Spinner列表项的布局资源ID，如R.layout.layout_item_for_save_directory
     * @param listener      选中项变化监听器，当用户选择不同项时回调；可为null
     */
    public void setData(String title, ArrayList<FileBean> dataList, int itemLayoutId, onItemSelectListener listener) { // 设置数据方法
        this.titleString = title; // 保存标题文本
        this.beanList = dataList; // 保存FileBean原始数据列表
        this.dataList = FileBeanToStr.getDisPlayStrList(dataList); // 将FileBean列表转换为显示名称字符串列表
        this.itemSelectListener = listener; // 保存选中项变化监听器
        this.itemLayoutId = itemLayoutId; // 保存列表项布局资源ID
        updateView(); // 刷新视图显示
    }

    /**
     * 更新数据列表并设置选中项
     *
     * <p>在已初始化适配器的情况下，动态更新Spinner的数据源和选中项。</p>
     * <p>与setData()不同，此方法不会重新创建适配器，而是清空并重新填充现有适配器。</p>
     *
     * <p>选中项逻辑：</p>
     * <ul>
     *   <li>如果selectStr为null或不在beanList中 → 选中第一项（位置0）</li>
     *   <li>如果selectStr在beanList中 → 选中该项对应的显示名称位置</li>
     * </ul>
     *
     * @param dataList  新的FileBean数据列表
     * @param selectStr 需要选中的FileBean项，可为null表示选中第一项
     */
    public void updateDataList(ArrayList<FileBean> dataList, FileBean selectStr) { // 更新数据列表方法
        this.dataList = FileBeanToStr.getDisPlayStrList(dataList); // 将新的FileBean列表转换为显示名称字符串列表
        this.beanList = dataList; // 更新FileBean原始数据列表
        adapterForSpinner.clear(); // 清空适配器中的旧数据
        adapterForSpinner.addAll(this.dataList); // 将新的显示名称列表添加到适配器
        adapterForSpinner.notifyDataSetChanged(); // 通知适配器数据已变更，刷新Spinner显示
        if ((selectStr == null) || (!beanList.contains(selectStr))) { // 判断选中项是否为null或不在列表中
            spinner.setSelection(0); // 选中第一项作为默认值
        } else { // 选中项在列表中
            spinner.setSelection(this.dataList.indexOf(selectStr.getPath())); // 根据路径在显示列表中查找位置并选中
        }
    }

    /**
     * 更新视图显示
     *
     * <p>根据当前的数据和属性配置，刷新所有子视图的显示状态。</p>
     * <p>包括：设置标题文本、调整标题和Spinner的宽度、创建适配器并绑定到Spinner、注册选中监听器。</p>
     *
     * <p>注意：每次调用此方法都会重新创建ArrayAdapter，适用于数据初始化场景。</p>
     * <p>如需增量更新数据，应使用updateDataList()方法。</p>
     */
    private void updateView() { // 更新视图显示方法
        headTxtView.setText(titleString); // 设置标题文本内容
        LinearLayout.LayoutParams headParams = (LayoutParams) headTxtView.getLayoutParams(); // 获取标题视图的布局参数
        headParams.width = headWidth; // 设置标题视图宽度为自定义属性值
        headTxtView.setLayoutParams(headParams); // 应用新的布局参数到标题视图

        LinearLayout.LayoutParams spinnerParams = (LayoutParams) spinner.getLayoutParams(); // 获取Spinner的布局参数
        spinnerParams.width = spinnerWidth; // 设置Spinner宽度为自定义属性值
        spinner.setLayoutParams(spinnerParams); // 应用新的布局参数到Spinner


//        adapterForSpinner = new ArrayAdapter<>(context, R.layout.layout_item_for_save_directory, dataList); // 旧代码：使用固定布局
        adapterForSpinner = new ArrayAdapter<>(context, itemLayoutId, dataList); // 创建ArrayAdapter，使用外部传入的列表项布局
        spinner.setAdapter(adapterForSpinner); // 将适配器绑定到Spinner
        spinner.setOnItemSelectedListener(onItemSelectedListener); // 注册Spinner选中项变化监听器

    }

    /**
     * Spinner选中项变化监听器实例
     *
     * <p>当用户在Spinner中选择不同的项时触发。</p>
     * <p>功能：</p>
     * <ol>
     *   <li>记录当前选中项的位置索引到selectPosition</li>
     *   <li>如果外部注册了onItemSelectListener，则回调传递选中的FileBean对象</li>
     * </ol>
     *
     * <p>注意：回调传递的是beanList中的FileBean对象，而非显示名称字符串，
     * 这样外部可以直接获取文件的完整信息（路径、显示名等）。</p>
     */
    private final AdapterView.OnItemSelectedListener onItemSelectedListener = new AdapterView.OnItemSelectedListener() { // 创建选中项监听器实例
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { // 选中项变化回调
            //选中的保存目录 // 原有注释：选中的保存目录
            selectPosition = position; // 记录当前选中项的位置索引
            if (itemSelectListener != null) { // 检查外部是否注册了监听器
//                itemSelectListener.onItemSelected(adapterForSpinner.getItem(position)); // 旧代码：回调显示名称字符串
                itemSelectListener.onItemSelected(beanList.get(position)); // 回调选中的FileBean对象，包含完整文件信息
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) { // 未选中任何项的回调
            // 当Spinner下拉菜单消失且未选中任何项时触发，此处无需处理 // 无操作
        }
    };

//    public String getSelect() { // 旧代码：获取选中项的显示名称字符串（已弃用）
//        return adapterForSpinner.getItem(selectPosition); // 返回选中位置的显示名称
//    }


    /**
     * 获取当前选中项的位置索引
     *
     * <p>直接委托给Spinner.getSelectedItemPosition()获取。</p>
     *
     * @return 选中项的位置索引（0-based）；若无选中项返回INVALID_POSITION(-1)
     */
    public int getSelectPosition() { // 获取选中项位置方法
        return spinner.getSelectedItemPosition(); // 返回Spinner当前选中项的位置
    }

    /**
     * 获取当前选中项的文件路径
     *
     * <p>根据Spinner的选中位置，从beanList中获取对应FileBean的路径。</p>
     *
     * <p>返回值逻辑：</p>
     * <ul>
     *   <li>有选中项且选中项不为null → 返回beanList对应位置的FileBean.getPath()</li>
     *   <li>无选中项但beanList有数据 → 返回第一项的路径（兜底策略）</li>
     *   <li>无选中项且beanList为空 → 返回空字符串""</li>
     * </ul>
     *
     * @return 选中项的文件绝对路径字符串；无数据时返回空字符串""
     */
    public String getSelectItem() { // 获取选中项路径方法
        int pos = spinner.getSelectedItemPosition(); // 获取当前选中项位置
        if (pos != AdapterView.INVALID_POSITION) { // 判断是否有有效选中项
            if (spinner.getSelectedItem() == null) { // 判断选中项对象是否为null
                return ""; // 选中项为null，返回空字符串
            }
            return beanList.get(pos).getPath(); // 返回选中项FileBean的文件路径
        } else { // 无有效选中项
            if (beanList.size() > 0) { // 判断beanList是否有数据
                return beanList.get(0).getPath(); // 兜底返回第一项的路径
            } else { // beanList为空
                return ""; // 无数据，返回空字符串
            }
        }
    }

    /**
     * 获取当前选中项的显示名称
     *
     * <p>根据Spinner的选中位置，从beanList中获取对应FileBean的显示名称。</p>
     *
     * <p>返回值逻辑（与getSelectItem()一致，仅返回值不同）：</p>
     * <ul>
     *   <li>有选中项且选中项不为null → 返回beanList对应位置的FileBean.getDisplayName()</li>
     *   <li>无选中项但beanList有数据 → 返回第一项的显示名称（兜底策略）</li>
     *   <li>无选中项且beanList为空 → 返回空字符串""</li>
     * </ul>
     *
     * @return 选中项的文件显示名称字符串；无数据时返回空字符串""
     * @see #getSelectItem() 获取选中项的文件路径
     */
    public String getDisPlaySelectItem() { // 获取选中项显示名称方法
        int pos = spinner.getSelectedItemPosition(); // 获取当前选中项位置
        if (pos != AdapterView.INVALID_POSITION) { // 判断是否有有效选中项
            if (spinner.getSelectedItem() == null) { // 判断选中项对象是否为null
                return ""; // 选中项为null，返回空字符串
            }
            return beanList.get(pos).getDisplayName(); // 返回选中项FileBean的显示名称
        } else { // 无有效选中项
            if (beanList.size() > 0) { // 判断beanList是否有数据
                return beanList.get(0).getDisplayName(); // 兜底返回第一项的显示名称
            } else { // beanList为空
                return ""; // 无数据，返回空字符串
            }
        }
    }


    /**
     * 获取内部Spinner控件引用
     *
     * <p>暴露内部的Spinner控件，允许外部进行更细粒度的操作，
     * 如设置特定属性、注册额外监听器等。</p>
     *
     * @return 内部Spinner控件实例
     */
    public Spinner getSpinner() { // 获取Spinner控件方法
        return spinner; // 返回内部Spinner引用
    }


    /**
     * 设置只读模式
     *
     * <p>控制Spinner的可用状态和视觉样式：</p>
     * <ul>
     *   <li>只读模式（enabled=false）：禁用Spinner交互，选中项文本显示为禁用色(textColorNewTopViewDisable)</li>
     *   <li>可编辑模式（enabled=true）：启用Spinner交互，选中项文本显示为正常色(colorChCommon)</li>
     * </ul>
     *
     * <p>注意：此方法同时修改Spinner的enabled状态和选中项文本的颜色，
     * 确保视觉反馈与交互状态一致。</p>
     *
     * @param enabled true表示可编辑模式，false表示只读模式
     */
    @SuppressLint("ResourceType") // 抑制lint警告：使用硬编码颜色资源ID
    public void setReadOnly(boolean enabled) { // 设置只读模式方法
        super.setEnabled(enabled); // 调用父类设置整体可用状态
        TextView selectedView = (TextView) spinner.getSelectedView(); // 获取Spinner当前选中项的视图，强转为TextView
        if(!enabled){ // 进入只读模式
            spinner.setEnabled(false); // 禁用Spinner交互
            if(selectedView!=null){ // 检查选中项视图是否存在（Spinner可能尚未渲染）
                selectedView.setTextColor(context.getResources().getColor(com.micsig.tbook.ui.R.color.textColorNewTopViewDisable)); // 设置禁用状态文本颜色
            }
        }else { // 进入可编辑模式
            selectedView.setTextColor(context.getResources().getColor(com.micsig.tbook.ui.R.color.colorChCommon)); // 设置正常状态文本颜色
            spinner.setEnabled(true); // 启用Spinner交互
        }
    }
}
