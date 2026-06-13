package com.micsig.tbook.tbookscope.wavezone.wave; // 波形管理包，包含通道波形管理类，1

import android.graphics.Bitmap; // Android位图类，用于图像处理，1
import android.graphics.Canvas; // Android画布类，用于绘制图形，1
import android.graphics.Rect; // Android矩形类，用于表示矩形区域，1

import com.chillingvan.canvasgl.ICanvasGL; // OpenGL画布接口，用于OpenGL绘制，1
import com.micsig.base.Logger; // 日志工具类，用于日志输出，1
import com.micsig.tbook.scope.ScopeBase; // 示波器基础类，提供基础计算方法，1
import com.micsig.tbook.tbookscope.GlobalVar; // 全局变量管理类，管理全局配置，1
import com.micsig.tbook.tbookscope.tools.Tools; // 工具类，提供通用工具方法，1
import com.micsig.tbook.tbookscope.util.CacheUtil; // 缓存工具类，管理缓存数据，1
import com.micsig.tbook.tbookscope.wavezone.IWorkMode; // 工作模式接口，定义工作模式切换，1
import com.micsig.tbook.tbookscope.wavezone.WorkModeManage; // 工作模式管理类，管理工作模式，1
import com.micsig.tbook.ui.wavezone.IWave; // 波形接口，定义波形操作，1
import com.micsig.tbook.ui.wavezone.TChan; // 通道枚举类，定义通道常量，1

import java.util.HashMap; // HashMap集合类，用于存储键值对，1
import java.util.Map; // Map接口，定义映射操作，1

/**
 * YT模式通道波形管理类 - 示波器YT模式下多通道波形管理的核心实现
 * 
 * 【模块定位】
 * - 所属模块：wavezone.wave（波形显示区域-波形管理模块）
 * - 核心职责：管理YT模式下的所有通道波形，包括通道位置、选中状态、可见性、标签等
 * - 架构层级：业务逻辑层，位于显示层和数据层之间
 * 
 * 【核心职责】
 * 1. 通道波形管理：管理多个通道的波形对象（CH1-CH4、数学通道、参考通道）
 * 2. 通道位置管理：管理YT模式下各通道的Y轴位置（垂直位置）
 * 3. 通道选中管理：管理通道的单选状态，支持通道切换
 * 4. 通道可见性管理：控制各通道的显示/隐藏状态
 * 5. 通道标签管理：管理通道标签的位置和内容
 * 6. 工作模式切换：支持YT/YTZOOM模式切换
 * 7. 波形绘制管理：协调各通道波形的绘制
 * 
 * 【架构图】
 * ┌─────────────────────────────────────────────────────────────┐
 * │            WaveZoneDisplay_YT（YT模式显示组件）               │
 * │                   (显示层控制器)                             │
 * │  - 管理YT模式整体显示                                        │
 * │  - 协调各管理器组件                                          │
 * │  - 处理用户交互事件                                          │
 * └─────────────────────────────────────────────────────────────┘
 *                              ↓ 持有引用
 * ┌─────────────────────────────────────────────────────────────┐
 * │            ChannelWaveManage_YT（YT模式通道波形管理）          │
 * │                   (通道波形管理核心)                         │
 * │  ┌──────────────────────────────────────────────────────┐  │
 * │  │  通道波形存储（mapChannel）                           │  │
 * │  │  - CH1 → ChannelWave对象                             │  │
 * │  │  - CH2 → ChannelWave对象                             │  │
 * │  │  - CH3 → ChannelWave对象                             │  │
 * │  │  - CH4 → ChannelWave对象                             │  │
 * │  │  - Math → ChannelWave对象                            │  │
 * │  │  - Ref → ChannelWave对象                             │  │
 * │  └──────────────────────────────────────────────────────┘  │
 * │  ┌──────────────────────────────────────────────────────┐  │
 * │  │  核心功能                                             │  │
 * │  │  - 通道位置管理（Y轴垂直位置）                         │  │
 * │  │  - 通道选中管理（单选模式）                           │  │
 * │  │  - 通道可见性控制                                     │  │
 * │  │  - 通道标签管理                                       │  │
 * │  │  - 工作模式切换                                       │  │
 * │  │  - 波形绘制协调                                       │  │
 * │  └──────────────────────────────────────────────────────┘  │
 * └─────────────────────────────────────────────────────────────┘
 *                              ↓ 管理对象
 * ┌─────────────────────────────────────────────────────────────┐
 * │                  ChannelWave（通道波形对象）                  │
 * │                   (单个通道波形实体)                         │
 * │  - 波形数据存储                                              │
 * │  - 位置状态管理                                              │
 * │  - 绘制逻辑实现                                              │
 * │  - 标签显示管理                                              │
 * └─────────────────────────────────────────────────────────────┘
 * 
 * 【YT模式通道波形管理流程】
 * 1. 初始化流程：
 *    构造函数 → 遍历所有通道 → 创建ChannelWave对象 → 设置初始位置 → 存入mapChannel
 * 
 * 2. 通道选择流程：
 *    用户点击 → selectCursor(x,y) → 遍历通道 → 检测点击区域 → 设置选中状态 → 返回通道号
 * 
 * 3. 通道移动流程：
 *    用户拖动 → setOffsetY(offsetY) → 查找选中通道 → 更新Y位置 → 刷新显示
 * 
 * 4. 工作模式切换流程：
 *    模式切换 → switchWorkMode(workMode) → 遍历所有通道 → 调用通道切换方法 → 更新显示
 * 
 * 5. 波形绘制流程：
 *    绘制请求 → draw(canvas) → 遍历所有通道 → 绘制可见通道 → 绘制选中通道（置顶）
 * 
 * 【依赖关系】
 * - 实现接口：IWaveShowManage（波形显示管理接口）、IWorkMode（工作模式接口）
 * - 管理对象：ChannelWave（通道波形对象）
 * - 依赖工具：Tools（工具类）、ScopeBase（示波器基础类）、CacheUtil（缓存工具）
 * - 依赖配置：GlobalVar（全局变量）、TChan（通道枚举）
 * - 上层调用：WaveZoneDisplay_YT（YT模式显示组件）
 * 
 * 【设计模式】
 * 1. 组合模式（Composite Pattern）：
 *    - ChannelWaveManage_YT管理多个ChannelWave对象
 *    - 对外提供统一的操作接口，内部协调各通道
 *    - 客户端无需关心单个通道的管理细节
 * 
 * 2. 策略模式（Strategy Pattern）：
 *    - 实现IWaveShowManage和IWorkMode接口
 *    - 不同模式（YT/XY）使用不同的管理策略
 *    - 通过接口实现模式切换
 * 
 * 3. 迭代器模式（Iterator Pattern）：
 *    - 使用HashMap存储通道，遍历操作
 *    - 提供统一的遍历接口
 * 
 * 【使用场景】
 * 1. YT模式初始化：创建YT模式显示组件时初始化通道波形管理器
 * 2. 通道切换：用户点击通道标签切换当前通道
 * 3. 通道位置调整：用户拖动波形调整垂直位置
 * 4. 通道显示控制：用户开启/关闭通道显示
 * 5. 工作模式切换：从YT模式切换到YTZOOM模式
 * 6. 波形刷新：数据更新后刷新波形显示
 * 
 * 【关键数据结构】
 * - mapChannel：HashMap<Integer, ChannelWave>，存储通道号到通道波形对象的映射
 *   Key：通道号（TChan定义的通道常量）
 *   Value：ChannelWave对象，包含波形数据和状态
 * 
 * 【线程安全】
 * - 该类不是线程安全的，外部调用需要确保在主线程执行
 * - 绘制操作需要在OpenGL线程执行
 * 
 * 【性能考虑】
 * - 使用HashMap快速查找通道，时间复杂度O(1)
 * - 遍历所有通道时使用keySet()，避免创建额外集合
 * - 绘制时先绘制未选中通道，再绘制选中通道实现置顶效果
 * 
 * 【注意事项】
 * 1. YT模式下所有通道控制Y轴位置，与XY模式不同
 * 2. 通道选中采用单选模式，同一时间只能选中一个通道
 * 3. 位置更新需要同步更新缓存，确保数据一致性
 * 4. 绘制顺序影响显示层级，选中通道需要最后绘制
 * 
 * @author liwb
 * @version 1.0
 * @since 2017/5/19
 * @see IWaveShowManage
 * @see IWorkMode
 * @see ChannelWave
 * @see WaveZoneDisplay_YT
 */

public class ChannelWaveManage_YT implements IWaveShowManage, IWorkMode { // YT模式通道波形管理类，实现波形显示管理和工作模式接口，1
    
    /**
     * 日志标签，用于标识当前类的日志输出
     */
    private static final String TAG="ChannelWaveManage_YT"; // 日志标签，用于日志输出，1

    //region 成员变量
    /**
     * 通道波形映射表
     * Key：通道号（TChan定义的通道常量）
     * Value：ChannelWave对象，包含波形数据和状态
     * 
     * 【数据结构】
     * - 使用HashMap存储，提供O(1)的查找性能
     * - 包含所有通道：CH1-CH4、Math、Ref
     * - 每个ChannelWave对象独立管理自己的波形数据
     */
    private Map<Integer, ChannelWave> mapChannel = new HashMap<>(); // 通道波形映射表，存储通道号到通道波形对象的映射，1
//    private Bitmap bmp; // 已废弃：位图缓存，不再使用
    //endregion

    /**
     * 构造函数 - 初始化YT模式通道波形管理器
     * 
     * 【功能说明】
     * 创建并初始化所有通道的波形对象，设置初始位置和状态
     * 
     * 【参数说明】
     * @param resBmp 二维位图数组，resBmp[通道号][资源索引]，包含各通道的波形资源位图
     *               第一维：通道号（CH1-CH4、Math、Ref）
     *               第二维：资源索引（不同状态的位图）
     * @param onMovingWaveEvent 波形移动事件回调接口，用于通知波形移动事件
     * @param onSelectChangeEvent 选中状态变化事件回调接口，用于通知选中状态变化
     * 
     * 【调用时机】
     * - 创建YT模式显示组件时调用
     * - WaveZoneDisplay_YT初始化时调用
     * 
     * 【初始化流程】
     * 1. 遍历所有通道（CH1-CH4、Math、Ref）
     * 2. 为每个通道创建ChannelWave对象
     * 3. 设置通道号标识
     * 4. 设置初始Y轴位置（从配置读取）
     * 5. 设置初始选中状态为false
     * 6. 设置初始可见性为true
     * 7. 设置事件回调接口
     * 8. 初始化标签矩形区域
     * 9. 将通道对象存入mapChannel
     * 
     * 【注意事项】
     * - 所有通道初始状态为未选中、可见
     * - 初始位置从Tools.getYTChannelPositionUI()获取
     * - 事件回调接口用于通知上层UI更新
     */
    public ChannelWaveManage_YT(Bitmap[][] resBmp, IWave.OnMovingWaveEvent onMovingWaveEvent, IWave.OnSelectChangeEvent onSelectChangeEvent) { // 构造函数，初始化通道波形管理器，1
//        bmp = Bitmap.createBitmap(GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()),
//                GlobalVar.get().getWaveZoneHeight_Pix(WorkModeManage.getInstance().getmWorkMode()), Bitmap.Config.ARGB_8888); // 已废弃：创建位图缓存

        TChan.foreachChan((chNo) -> { // 遍历所有通道，chNo为通道号，1
            ChannelWave channelWave = new ChannelWave(resBmp[chNo]); // 创建通道波形对象，传入该通道的位图资源数组，1
            channelWave.setLineNameId(chNo); // 设置通道号标识，用于识别通道，1
            channelWave.setY(Tools.getYTChannelPositionUI(chNo)); // 设置初始Y轴位置，从配置读取YT模式下的通道位置，1
            channelWave.setSelected(false); // 设置初始选中状态为false，未选中，1
            channelWave.setVisible(true); // 设置初始可见性为true，默认显示，1
            channelWave.setOnMovingWaveEvent(onMovingWaveEvent); // 设置波形移动事件回调，用于通知移动事件，1
            channelWave.setOnSelectChangeEvent(onSelectChangeEvent); // 设置选中状态变化事件回调，用于通知选中变化，1
            channelWave.initLabelRect(); // 初始化标签矩形区域，用于点击检测，1
            mapChannel.put(chNo, channelWave); // 将通道波形对象存入映射表，键为通道号，1
        }); // 结束通道遍历，1
    } // 结束构造函数，1

    /**
     * 设置波形变化 - 更新波形高度并刷新显示
     * 
     * 【功能说明】
     * 当波形区域高度变化时，更新通道位置缓存并刷新所有通道波形
     * 
     * 【参数说明】
     * @param height 新的波形区域高度（像素）
     * 
     * 【调用时机】
     * - 波形区域高度变化时调用
     * - 屏幕旋转或分辨率变化时调用
     * - 工作模式切换导致波形区域大小变化时调用
     * 
     * 【处理流程】
     * 1. 更新所有通道的位置缓存
     * 2. 刷新所有通道的波形显示
     */
    public void setWaveChange(int height) { // 设置波形变化，更新波形高度并刷新显示，1
//        bmp = Bitmap.createScaledBitmap(bmp, GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()), height, true); // 已废弃：缩放位图
        updateChannelPosCache(); // 更新所有通道的位置缓存，同步位置数据，1
        refresh(); // 刷新所有通道波形，更新显示，1
    } // 结束setWaveChange方法，1

    /**
     * 更新通道位置缓存 - 同步通道位置到缓存
     * 
     * 【功能说明】
     * 遍历所有通道，更新标签位置和通道位置缓存
     * 
     * 【调用时机】
     * - 波形区域高度变化时调用
     * - 通道位置移动后需要同步缓存时调用
     * - 工作模式切换时调用
     * 
     * 【处理流程】
     * 1. 遍历mapChannel中的所有通道
     * 2. 更新每个通道的标签位置
     * 3. 如果不是恢复出厂设置状态，将通道位置存入缓存
     * 
     * 【注意事项】
     * - 恢复出厂设置时不更新缓存，保持初始位置
     * - 位置转换需要使用ScopeBase.changeAccuracy()进行精度转换
     */
    public void updateChannelPosCache() { // 更新通道位置缓存，同步位置数据到缓存，1
        for (Integer i : mapChannel.keySet()) { // 遍历所有通道号，i为通道号，1
            ChannelWave tem = mapChannel.get(i); // 获取通道波形对象，1
            if (tem == null) continue; // 如果对象为空，跳过本次循环，1
            tem.changeLabelPos(tem.getLabelRect().left); // 更新标签位置，保持标签的左侧位置不变，1
            if (CacheUtil.get().isClickFactoryReset()) { // 判断是否为恢复出厂设置状态，1
//                tem.setYFromEventBus((int) Math.round(108 * (tem.getLineNameID() - 2 - (tem.getLineNameID() - 9) * 1.5))); // 已废弃：恢复出厂设置时的位置计算
            } else { // 非恢复出厂设置状态，1


                Tools.putChannelPosition(tem.getLineNameID(), ScopeBase.changeAccuracy(tem.getPosY() * ScopeBase.getToUICoff())); // 将通道位置存入缓存，进行精度转换，1
            } // 结束if-else判断，1
        } // 结束for循环，1
    } // 结束updateChannelPosCache方法，1

//    private void setBmpPixs(int[] pixs) {
//        synchronized (bmp) {
//            bmp.setPixels(pixs, 0, bmp.getWidth(), 0, 0, bmp.getWidth(), bmp.getHeight());
//        }
//    }
//
//    //region IWaveShowManange接口
//    @Override
//    public Bitmap getWavesBitmap() {
//        return bmp;
//    }

    /**
     * 获取选中的通道号 - 查询当前选中的通道
     * 
     * 【功能说明】
     * 遍历所有通道，返回当前选中状态为true的通道号
     * 
     * 【返回值说明】
     * @return 选中的通道号，如果没有选中任何通道则返回-1
     *         返回值范围：-1（未选中）、CH1-CH4（0-3）、Math（4）、Ref（5）
     * 
     * 【调用时机】
     * - 需要查询当前选中通道时调用
     * - 执行通道相关操作前需要确认选中通道时调用
     * 
     * 【选中规则】
     * - 单选模式：同一时间只能有一个通道被选中
     * - 返回第一个找到的选中通道
     * - 如果没有选中通道，返回-1
     */
    @Override
    public int isSelected() { // 获取选中的通道号，实现IWaveShowManage接口，1
        int flag = -1; // 初始化返回值为-1，表示未选中任何通道，1
        for (Integer i : mapChannel.keySet()) { // 遍历所有通道号，i为通道号，1
            ChannelWave tem = mapChannel.get(i); // 获取通道波形对象，1
            if (tem != null && tem.isSelected()) { // 判断通道对象不为空且处于选中状态，1
                flag = i; // 记录选中的通道号，1
                return flag; // 立即返回选中的通道号，单选模式只需找到第一个，1
            } // 结束if判断，1
        } // 结束for循环，1
        return flag; // 返回-1，表示没有选中任何通道，1
    } // 结束isSelected方法，1

    /**
     * 查询指定通道的可见性 - 检查通道是否可见
     * 
     * 【功能说明】
     * 查询指定通道号的可见性状态
     * 
     * 【参数说明】
     * @param chNo 通道号，取值范围：CH1-CH4（0-3）、Math（4）、Ref（5）
     * 
     * 【返回值说明】
     * @return true表示通道可见，false表示通道不可见或通道不存在
     * 
     * 【调用时机】
     * - 需要判断通道是否显示时调用
     * - 绘制前检查通道可见性时调用
     * - UI更新需要确认通道状态时调用
     */
    public boolean isVisible(int chNo) { // 查询指定通道的可见性，1
        for (Integer i : mapChannel.keySet()) { // 遍历所有通道号，i为通道号，1
            ChannelWave tem = mapChannel.get(i); // 获取通道波形对象，1
            if (tem != null && tem.getLineNameID() == chNo) { // 判断通道对象不为空且通道号匹配，1
                return tem.getVisible(); // 返回该通道的可见性状态，1
            } // 结束if判断，1
        } // 结束for循环，1
        return false; // 通道不存在，返回false，1
    } // 结束isVisible方法，1

    /**
     * 设置波形Y轴偏移 - 移动选中通道的波形
     * 
     * 【功能说明】
     * 将当前选中通道的波形在Y轴方向移动指定偏移量
     * 
     * 【参数说明】
     * @param offsetY Y轴偏移量（像素），正值向上移动，负值向下移动
     * 
     * 【调用时机】
     * - 用户拖动波形调整垂直位置时调用
     * - 波形位置需要程序化调整时调用
     * 
     * 【移动规则】
     * - 只移动当前选中的通道
     * - 移动方向：offsetY为正向上，为负向下
     * - 实际位置 = 当前位置 - offsetY
     * 
     * 【注意事项】
     * - 该方法只影响选中通道，其他通道不受影响
     * - 移动后需要调用refresh()刷新显示
     */
    @Override
    public void setOffsetY(int offsetY) { // 设置波形Y轴偏移，实现IWaveShowManage接口，1
        for (ChannelWave c : mapChannel.values()) { // 遍历所有通道波形对象，c为通道波形对象，1
            if (c.isSelected()) { // 判断通道是否被选中，1
                c.setY(c.getY() - offsetY); // 设置新的Y位置，当前位置减去偏移量，1
            } // 结束if判断，1
        } // 结束for循环，1
    } // 结束setOffsetY方法，1


    /**
     * 设置指定通道的Y轴位置 - 直接设置通道位置
     * 
     * 【功能说明】
     * 直接设置指定通道号的Y轴位置，不经过事件总线
     * 
     * 【参数说明】
     * @param chNo 通道号，取值范围：CH1-CH4（0-3）、Math（4）、Ref（5）
     * @param y Y轴位置（像素），相对于波形区域顶部的距离
     * 
     * 【调用时机】
     * - 需要程序化设置通道位置时调用
     * - 恢复通道位置时调用
     * - 初始化通道位置时调用
     * 
     * 【注意事项】
     * - 该方法直接设置位置，不触发事件总线通知
     * - 与setYFromEventBus的区别：不经过事件总线
     */
    public void setY(int chNo, double y) { // 设置指定通道的Y轴位置，1
        for (ChannelWave c : mapChannel.values()) { // 遍历所有通道波形对象，c为通道波形对象，1
            if (c.getLineNameID() == chNo) { // 判断通道号是否匹配，1
                c.setY(y); // 设置通道的Y轴位置，1
            } // 结束if判断，1
        } // 结束for循环，1
    } // 结束setY方法，1


    /**
     * 通过事件总线设置通道Y轴位置 - 从事件总线接收位置设置
     * 
     * 【功能说明】
     * 从事件总线接收位置设置请求，设置指定通道的Y轴位置
     * 
     * 【参数说明】
     * @param chNo 通道号，取值范围：CH1-CH4（0-3）、Math（4）、Ref（5）
     * @param y Y轴位置（像素），相对于波形区域顶部的距离
     * 
     * 【调用时机】
     * - 从事件总线接收到位置设置事件时调用
     * - 跨组件位置同步时调用
     * 
     * 【与setY的区别】
     * - setY：直接设置，不经过事件总线
     * - setYFromEventBus：从事件总线接收，可能触发额外的同步逻辑
     */
    public void setYFromEventBus(int chNo, double y) { // 通过事件总线设置通道Y轴位置，1
        for (ChannelWave c : mapChannel.values()) { // 遍历所有通道波形对象，c为通道波形对象，1
            if (c.getLineNameID() == chNo) { // 判断通道号是否匹配，1
                c.setYFromEventBus(y); // 调用通道的事件总线设置方法，可能触发额外逻辑，1
            } // 结束if判断，1
        } // 结束for循环，1
    } // 结束setYFromEventBus方法，1

    /**
     * 获取指定通道的Y轴位置 - 查询通道位置
     * 
     * 【功能说明】
     * 获取指定通道号的Y轴位置（内部坐标）
     * 
     * 【参数说明】
     * @param chNo 通道号，取值范围：CH1-CH4（0-3）、Math（4）、Ref（5）
     * 
     * 【返回值说明】
     * @return Y轴位置（内部坐标），如果通道不存在返回0
     * 
     * 【调用时机】
     * - 需要查询通道位置时调用
     * - 位置同步时调用
     * 
     * 【注意事项】
     * - 返回的是内部坐标，不是UI坐标
     * - 需要UI坐标时使用getUIY()方法
     */
    public double getY(int chNo) { // 获取指定通道的Y轴位置，1
        for (ChannelWave c : mapChannel.values()) { // 遍历所有通道波形对象，c为通道波形对象，1
            if (c.getLineNameID() == chNo) { // 判断通道号是否匹配，1
                return c.getY(); // 返回通道的Y轴位置，1
            } // 结束if判断，1
        } // 结束for循环，1
        return 0; // 通道不存在，返回0，1
    } // 结束getY方法，1

    /**
     * 获取指定通道的UI Y轴位置 - 查询通道UI坐标
     * 
     * 【功能说明】
     * 获取指定通道号的Y轴位置（UI坐标），经过精度转换和系数转换
     * 
     * 【参数说明】
     * @param chNo 通道号，取值范围：CH1-CH4（0-3）、Math（4）、Ref（5）
     * 
     * 【返回值说明】
     * @return Y轴位置（UI坐标），如果通道不存在返回0
     * 
     * 【调用时机】
     * - 需要获取通道的UI显示位置时调用
     * - UI更新时调用
     * 
     * 【坐标转换】
     * - 内部坐标 → UI坐标：posY * toUICoff → changeAccuracy
     * - ScopeBase.getToUICoff()：获取转换系数
     * - ScopeBase.changeAccuracy()：精度转换
     */
    public double getUIY(int chNo) { // 获取指定通道的UI Y轴位置，1
        for (ChannelWave c : mapChannel.values()) { // 遍历所有通道波形对象，c为通道波形对象，1
            if (c.getLineNameID() == chNo) { // 判断通道号是否匹配，1
                return ScopeBase.changeAccuracy(c.getPosY() * ScopeBase.getToUICoff()); // 转换为UI坐标并返回，1
            } // 结束if判断，1
        } // 结束for循环，1
        return 0; // 通道不存在，返回0，1
    } // 结束getUIY方法，1

    /**
     * 绘制波形到Canvas - 使用Android Canvas绘制
     * 
     * 【功能说明】
     * 将所有可见通道波形绘制到Canvas上，选中通道最后绘制实现置顶效果
     * 
     * 【参数说明】
     * @param canvas Android Canvas对象，用于绘制图形
     * 
     * 【调用时机】
     * - 视图绘制时调用
     * - 需要使用Canvas绘制时调用
     * 
     * 【绘制顺序】
     * 1. 遍历绘制所有通道（包括选中通道）
     * 2. 再次绘制选中通道（实现置顶效果）
     * 
     * 【注意事项】
     * - 选中通道绘制两次，确保在最上层显示
     * - 未判断可见性，所有通道都会绘制
     */
    @Override
    public void draw(Canvas canvas) { // 绘制波形到Canvas，实现IWaveShowManage接口，1
        for (ChannelWave c : mapChannel.values()) { // 遍历所有通道波形对象，c为通道波形对象，1
            c.draw(canvas); // 绘制通道波形到Canvas，1
        } // 结束for循环，1
        int flag = isSelected(); // 获取选中的通道号，1
        if (flag != -1) { // 判断是否有选中的通道，1
            mapChannel.get(flag).draw(canvas); // 再次绘制选中通道，实现置顶效果，1
        } // 结束if判断，1
    } // 结束draw方法，1

    /**
     * 绘制波形到OpenGL Canvas - 使用OpenGL绘制
     * 
     * 【功能说明】
     * 将所有可见通道波形绘制到OpenGL Canvas上，选中通道最后绘制实现置顶效果
     * 
     * 【参数说明】
     * @param canvas OpenGL Canvas对象（ICanvasGL），用于OpenGL绘制
     * 
     * 【调用时机】
     * - OpenGL渲染时调用
     * - 需要高性能绘制时调用
     * 
     * 【绘制顺序】
     * 1. 遍历绘制所有可见通道
     * 2. 再次绘制选中通道（实现置顶效果）
     * 
     * 【注意事项】
     * - 只绘制可见通道（getVisible()为true）
     * - 选中通道绘制两次，确保在最上层显示
     * - OpenGL绘制性能优于Canvas绘制
     */
    @Override
    public void draw(ICanvasGL canvas) { // 绘制波形到OpenGL Canvas，实现IWaveShowManage接口，1
        for (ChannelWave c : mapChannel.values()) { // 遍历所有通道波形对象，c为通道波形对象，1
            if (c.getVisible()) c.draw(canvas); // 判断通道可见，则绘制通道波形，1
        } // 结束for循环，1
        int flag = isSelected(); // 获取选中的通道号，1
        if (flag != -1) { // 判断是否有选中的通道，1
            mapChannel.get(flag).draw(canvas); // 再次绘制选中通道，实现置顶效果，1
        } // 结束if判断，1
    } // 结束draw方法，1

    //region IWorkMode接口

    /**
     * 切换工作模式 - 切换到指定工作模式
     * 
     * 【功能说明】
     * 将所有通道切换到指定的工作模式（YT/YTZOOM/XY）
     * 
     * 【参数说明】
     * @param workMode 目标工作模式，取值范围：
     *                 - WorkMode_YT（0x00）：YT模式
     *                 - WorkMode_YTZOOM（0x01）：YT缩放模式
     *                 - WorkMode_XY（0x02）：XY模式
     * 
     * 【调用时机】
     * - 用户切换工作模式时调用
     * - WorkModeManage管理器切换模式时调用
     * 
     * 【处理流程】
     * 1. 遍历所有通道
     * 2. 调用每个通道的switchWorkMode方法
     * 3. 通道内部更新显示状态
     * 
     * 【注意事项】
     * - 该方法会更新所有通道的工作模式
     * - 模式切换会影响通道的显示方式
     */
    @Override
    public void switchWorkMode(@WorkMode int workMode) { // 切换工作模式，实现IWorkMode接口，1
        for (ChannelWave c : mapChannel.values()) { // 遍历所有通道波形对象，c为通道波形对象，1
            c.switchWorkMode(workMode); // 调用通道的工作模式切换方法，1
        } // 结束for循环，1
    } // 结束switchWorkMode方法，1

    //endregion

    /**
     * 设置通道可见性 - 控制通道显示/隐藏
     * 
     * 【功能说明】
     * 设置指定通道的可见性状态
     * 
     * 【参数说明】
     * @param ChNo 通道号，取值范围：CH1-CH4（0-3）、Math（4）、Ref（5）
     * @param visible 可见性状态，true表示显示，false表示隐藏
     * 
     * 【调用时机】
     * - 用户开启/关闭通道时调用
     * - 需要程序化控制通道显示时调用
     * 
     * 【注意事项】
     * - 隐藏的通道不会被绘制
     * - 隐藏的通道仍然存在于mapChannel中
     */
    public void setVisible(int ChNo, boolean visible) { // 设置通道可见性，1
        for (ChannelWave c : mapChannel.values()) { // 遍历所有通道波形对象，c为通道波形对象，1
            if (c.getLineNameID() == ChNo) { // 判断通道号是否匹配，1
                c.setVisible(visible); // 设置通道的可见性状态，1
            } // 结束if判断，1
        } // 结束for循环，1
    } // 结束setVisible方法，1

    /**
     * 设置通道标签 - 设置通道的标签文本
     * 
     * 【功能说明】
     * 设置指定通道的标签文本内容
     * 
     * 【参数说明】
     * @param ChNo 通道号，取值范围：CH1-CH4（0-3）、Math（4）、Ref（5）
     * @param label 标签文本内容
     * 
     * 【调用时机】
     * - 用户修改通道标签时调用
     * - 需要程序化设置通道标签时调用
     * 
     * 【注意事项】
     * - 标签会显示在通道波形旁边
     * - 标签内容可以是通道名称、测量值等
     */
    public void setChannelLabel(int ChNo, String label) { // 设置通道标签，1
        for (ChannelWave c : mapChannel.values()) { // 遍历所有通道波形对象，c为通道波形对象，1
            if (c.getLineNameID() == ChNo) { // 判断通道号是否匹配，1
                c.setLabel(label); // 设置通道的标签文本，1
            } // 结束if判断，1
        } // 结束for循环，1
    } // 结束setChannelLabel方法，1

    /**
     * 获取通道标签 - 查询通道的标签文本
     * 
     * 【功能说明】
     * 获取指定通道的标签文本内容
     * 
     * 【参数说明】
     * @param chNo 通道号，取值范围：CH1-CH4（0-3）、Math（4）、Ref（5）
     * 
     * 【返回值说明】
     * @return 标签文本内容，如果通道不存在返回空字符串
     * 
     * 【调用时机】
     * - 需要查询通道标签时调用
     * - UI显示通道标签时调用
     */
    public String getChannelLabel(int chNo) { // 获取通道标签，1
        String label = ""; // 初始化标签为空字符串，1
        for (ChannelWave c : mapChannel.values()) { // 遍历所有通道波形对象，c为通道波形对象，1
            if (c.getLineNameID() != chNo) continue; // 通道号不匹配，跳过本次循环，1
            label = c.getLabel(); // 获取通道的标签文本，1
            break; // 找到匹配通道，退出循环，1
        } // 结束for循环，1
        return label; // 返回标签文本，1
    } // 结束getChannelLabel方法，1

    /**
     * 获取通道标签矩形区域 - 查询标签的显示区域
     * 
     * 【功能说明】
     * 获取指定通道标签的矩形显示区域，用于点击检测
     * 
     * 【参数说明】
     * @param chNo 通道号，取值范围：CH1-CH4（0-3）、Math（4）、Ref（5）
     * 
     * 【返回值说明】
     * @return 标签矩形区域（Rect），如果通道不存在返回空矩形
     * 
     * 【调用时机】
     * - 需要检测标签点击时调用
     * - 需要获取标签显示位置时调用
     * 
     * 【注意事项】
     * - 返回的矩形区域用于点击检测
     * - 矩形坐标相对于波形区域
     */
    public Rect getLabelRect(int chNo) { // 获取通道标签矩形区域，1
        Rect rect = new Rect(); // 创建空矩形对象，1
        for (ChannelWave c : mapChannel.values()) { // 遍历所有通道波形对象，c为通道波形对象，1
            if (c.getLineNameID() != chNo) continue; // 通道号不匹配，跳过本次循环，1
            rect = c.getLabelRect(); // 获取通道的标签矩形区域，1
            break; // 找到匹配通道，退出循环，1
        } // 结束for循环，1
        return rect; // 返回标签矩形区域，1
    } // 结束getLabelRect方法，1

    /**
     * 标签选中的通道号
     * 用于记录用户点击标签时选中的通道
     */
    private int labelSelectChan = -1; // 标签选中的通道号，初始值为-1表示未选中，1
    
    /**
     * 检测点击位置是否在标签区域内 - 标签点击检测
     * 
     * 【功能说明】
     * 检测指定坐标是否在某个可见通道的标签区域内
     * 
     * 【参数说明】
     * @param x X坐标（像素），相对于波形区域
     * @param y Y坐标（像素），相对于波形区域
     * 
     * 【返回值说明】
     * @return true表示点击在标签区域内，false表示不在标签区域内
     * 
     * 【调用时机】
     * - 用户点击波形区域时调用
     * - 需要检测标签点击时调用
     * 
     * 【处理流程】
     * 1. 遍历所有通道
     * 2. 检查通道是否可见
     * 3. 检查点击坐标是否在标签矩形内
     * 4. 如果在标签内，记录通道号并返回true
     * 
     * 【注意事项】
     * - 只检测可见通道的标签
     * - 找到第一个匹配的标签即返回
     * - labelSelectChan记录选中的通道号
     */
    public boolean contains(int x, int y) { // 检测点击位置是否在标签区域内，1
        boolean isContains = false; // 初始化包含标志为false，1
        for (ChannelWave c : mapChannel.values()) { // 遍历所有通道波形对象，c为通道波形对象，1
            if (c.getVisible() && c.getLabelRect().contains(x, y)) { // 判断通道可见且点击在标签区域内，1
                labelSelectChan = c.getLineNameID(); // 记录选中的通道号，1
                isContains = true; // 设置包含标志为true，1
                break; // 找到匹配标签，退出循环，1
            } // 结束if判断，1
        } // 结束for循环，1
        return isContains; // 返回是否包含在标签区域内，1
    } // 结束contains方法，1

    /**
     * 获取标签选中的通道号 - 查询标签点击选中的通道
     * 
     * 【功能说明】
     * 获取最近一次标签点击选中的通道号
     * 
     * 【返回值说明】
     * @return 选中的通道号，如果没有选中返回-1
     * 
     * 【调用时机】
     * - contains()返回true后调用
     * - 需要获取标签点击选中的通道时调用
     * 
     * 【注意事项】
     * - 该值在contains()方法中更新
     * - 初始值为-1，表示未选中任何通道
     */
    public int getLabelSelectChan() { // 获取标签选中的通道号，1
        return labelSelectChan; // 返回标签选中的通道号，1
    } // 结束getLabelSelectChan方法，1

    /**
     * 改变标签位置 - 移动指定通道的标签
     * 
     * 【功能说明】
     * 改变指定通道标签的X轴位置
     * 
     * 【参数说明】
     * @param selectChan 选中的通道号，取值范围：CH1-CH4（0-3）、Math（4）、Ref（5）
     * @param x 新的X坐标（像素），相对于波形区域
     * 
     * 【调用时机】
     * - 用户拖动标签时调用
     * - 需要程序化移动标签时调用
     * 
     * 【注意事项】
     * - 只改变X轴位置，Y轴位置不变
     * - 标签位置会影响标签显示区域
     */
    public void changeLabelPos(int selectChan, int x) { // 改变标签位置，1
        for (ChannelWave c : mapChannel.values()) { // 遍历所有通道波形对象，c为通道波形对象，1
            if (selectChan == c.getLineNameID()) { // 判断通道号是否匹配，1
                c.changeLabelPos(x); // 调用通道的标签位置改变方法，1
                break; // 找到匹配通道，退出循环，1
            } // 结束if判断，1
        } // 结束for循环，1
    } // 结束changeLabelPos方法，1

    /**
     * 选择光标位置的通道 - 通过坐标选择通道
     * 
     * 【功能说明】
     * 检测指定坐标位置是否在某个可见通道的光标区域内，并设置选中状态
     * 
     * 【参数说明】
     * @param x X坐标（像素），相对于波形区域
     * @param y Y坐标（像素），相对于波形区域
     * 
     * 【返回值说明】
     * @return 选中的通道号，如果没有选中任何通道返回-1
     * 
     * 【调用时机】
     * - 用户点击波形区域选择通道时调用
     * - 需要通过坐标选择通道时调用
     * 
     * 【处理流程】
     * 1. 遍历所有通道
     * 2. 检查通道是否可见
     * 3. 检测坐标是否在通道光标区域内
     * 4. 如果在光标区域内，设置为选中状态并返回通道号
     * 5. 如果不在光标区域内，设置为未选中状态
     * 
     * 【选中规则】
     * - 单选模式：同一时间只能选中一个通道
     * - 点击光标区域内选中，点击其他区域取消选中
     * - 返回第一个匹配的通道号
     * 
     * 【注意事项】
     * - 只检测可见通道
     * - 选中的通道会被设置为选中状态
     * - 未选中的通道会被取消选中状态
     */
    public int selectCursor(int x, int y) { // 选择光标位置的通道，单参数版本，1
        int selectIndex = -1; // 初始化选中索引为-1，表示未选中，1
        for (int i : mapChannel.keySet()) { // 遍历所有通道号，i为通道号，1
            ChannelWave c = mapChannel.get(i); // 获取通道波形对象，1
//            Logger.i(TAG,"c.name:"+c.getLineNameID()+"  c.visible:"+c.getVisible()); // 已废弃：日志输出
            if (c == null || !c.getVisible()) continue; // 通道对象为空或不可见，跳过本次循环，1
            if (c.selectCursor(x, y)) { // 判断坐标是否在通道光标区域内，1
                c.setSelected(true); // 设置通道为选中状态，1
                selectIndex = i; // 记录选中的通道号，1
                return selectIndex; // 返回选中的通道号，1
            }else{ // 坐标不在光标区域内，1
                c.setSelected(false); // 设置通道为未选中状态，1
            } // 结束if-else判断，1
        } // 结束for循环，1
//        Logger.i(TAG,"selectIndex:"+selectIndex); // 已废弃：日志输出
        return selectIndex; // 返回选中的通道号，-1表示未选中，1
    } // 结束selectCursor方法，单参数版本，1
    
    /**
     * 选择光标位置的通道（排除当前通道） - 通过坐标选择通道，支持排除指定通道
     * 
     * 【功能说明】
     * 检测指定坐标位置是否在某个可见通道的光标区域内，支持排除指定通道
     * 
     * 【参数说明】
     * @param x X坐标（像素），相对于波形区域
     * @param y Y坐标（像素），相对于波形区域
     * @param curChan 当前通道号，用于排除当前通道的优先选择
     * 
     * 【返回值说明】
     * @return 选中的通道号，如果没有选中任何通道返回-1
     * 
     * 【调用时机】
     * - 用户点击波形区域选择通道，需要排除当前通道时调用
     * - 需要通过坐标选择通道且排除指定通道时调用
     * 
     * 【处理流程】
     * 1. 遍历所有通道
     * 2. 检查通道是否可见
     * 3. 检测坐标是否在通道光标区域内
     * 4. 如果在光标区域内且不是当前通道，设置为选中状态并返回
     * 5. 如果在光标区域内且是当前通道，返回但不改变选中状态
     * 6. 如果不在光标区域内，设置为未选中状态
     * 
     * 【选中规则】
     * - 优先选择非当前通道
     * - 如果只有当前通道在光标区域内，返回当前通道号
     * - 单选模式：同一时间只能选中一个通道
     * 
     * 【注意事项】
     * - 只检测可见通道
     * - curChan参数用于实现通道切换时的优先选择逻辑
     */
    public int selectCursor(int x,int y,int curChan){ // 选择光标位置的通道，三参数版本，支持排除当前通道，1
        int selectIndex = -1; // 初始化选中索引为-1，表示未选中，1
        for (int i : mapChannel.keySet()) { // 遍历所有通道号，i为通道号，1
            ChannelWave c = mapChannel.get(i); // 获取通道波形对象，1
            if (c.getVisible() == false) continue; // 通道不可见，跳过本次循环，1
            if (c.selectCursor(x, y) && i!=curChan) { // 判断坐标在光标区域内且不是当前通道，1
                c.setSelected(true); // 设置通道为选中状态，1
                selectIndex = i; // 记录选中的通道号，1
                return selectIndex; // 返回选中的通道号，1
            }else if (c.selectCursor(x, y)){ // 坐标在光标区域内但是当前通道，1
                selectIndex=i; // 记录当前通道号，1
                return selectIndex; // 返回当前通道号，1
            } // 结束if-else判断，1
            else{ // 坐标不在光标区域内，1
                c.setSelected(false); // 设置通道为未选中状态，1
            } // 结束else判断，1
        } // 结束for循环，1
        return selectIndex; // 返回选中的通道号，-1表示未选中，1
    } // 结束selectCursor方法，三参数版本，1
    
    /**
     * 设置选中通道 - 直接设置指定通道为选中状态
     * 
     * 【功能说明】
     * 直接设置指定通道号为选中状态，其他通道设置为未选中状态
     * 
     * 【参数说明】
     * @param ChNo 要选中的通道号，取值范围：CH1-CH4（0-3）、Math（4）、Ref（5）
     * 
     * 【调用时机】
     * - 需要程序化设置选中通道时调用
     * - UI切换通道时调用
     * 
     * 【处理流程】
     * 1. 遍历所有通道
     * 2. 如果通道号匹配，设置为选中状态
     * 3. 如果通道号不匹配，设置为未选中状态
     * 
     * 【注意事项】
     * - 单选模式：只有指定通道被选中
     * - 其他通道会被取消选中状态
     */
    public void setSelectCursor(int ChNo) { // 设置选中通道，1
        for (Integer i : mapChannel.keySet()) { // 遍历所有通道号，i为通道号，1
            ChannelWave tem = mapChannel.get(i); // 获取通道波形对象，1
            if (tem == null) continue; // 通道对象为空，跳过本次循环，1
            tem.setSelected(tem.getLineNameID() == ChNo); // 设置选中状态，通道号匹配则为true，否则为false，1
        } // 结束for循环，1
    } // 结束setSelectCursor方法，1

    /**
     * 移动选中通道的像素位置 - 水平移动波形
     * 
     * 【功能说明】
     * 将当前选中的通道波形在水平方向移动指定像素数
     * 
     * 【参数说明】
     * @param px 水平移动像素数，正值向右移动，负值向左移动
     * 
     * 【调用时机】
     * - 用户水平拖动波形时调用
     * - 需要程序化水平移动波形时调用
     * 
     * 【处理流程】
     * 1. 遍历所有通道
     * 2. 查找选中状态的通道
     * 3. 调用通道的movePix方法移动波形
     * 4. 找到选中通道后退出循环
     * 
     * 【注意事项】
     * - 只移动选中的通道
     * - 移动后需要刷新显示
     */
    public void movePix(int px) { // 移动选中通道的像素位置，1
        for (Integer i : mapChannel.keySet()) { // 遍历所有通道号，i为通道号，1
            ChannelWave tem = mapChannel.get(i); // 获取通道波形对象，1
            if (tem == null) continue; // 通道对象为空，跳过本次循环，1
            if (tem.isSelected()) { // 判断通道是否被选中，1
                tem.movePix(px); // 调用通道的像素移动方法，水平移动波形，1
                break; // 找到选中通道，退出循环，1
            } // 结束if判断，1
        } // 结束for循环，1
    } // 结束movePix方法，1
    //endregion

    /**
     * 刷新所有通道波形 - 更新波形显示
     * 
     * 【功能说明】
     * 遍历所有通道，调用每个通道的刷新方法，更新波形显示
     * 
     * 【调用时机】
     * - 波形数据更新后调用
     * - 工作模式切换后调用
     * - 通道位置变化后调用
     * - 需要重新绘制波形时调用
     * 
     * 【处理流程】
     * 1. 遍历mapChannel中的所有通道
     * 2. 调用每个通道的refresh方法
     * 3. 通道内部更新波形数据和显示
     * 
     * 【注意事项】
     * - 该方法会刷新所有通道，包括不可见通道
     * - 刷新后需要重新绘制才能看到效果
     */
    public void refresh() { // 刷新所有通道波形，1
        for (Integer i : mapChannel.keySet()) { // 遍历所有通道号，i为通道号，1
            ChannelWave tem = mapChannel.get(i); // 获取通道波形对象，1
            if (tem == null) continue; // 通道对象为空，跳过本次循环，1
            tem.refresh(); // 调用通道的刷新方法，更新波形显示，1
        } // 结束for循环，1
    } // 结束refresh方法，1
} // 结束ChannelWaveManage_YT类定义，1