package com.micsig.tbook.tbookscope.wavezone.wave; // 波形显示管理包，定义波形显示管理核心类，1

import android.graphics.Bitmap; // Android位图类，用于波形标签图像处理，1
import android.graphics.Canvas; // Android画布类，用于标准2D绘制，1
import android.graphics.Paint; // Android画笔类，用于绘制图形，1
import android.graphics.PorterDuff; // Android图像混合模式类，1
import android.graphics.PorterDuffXfermode; // Android图像混合模式转换类，1
import android.graphics.Rect; // Android矩形类，用于标签区域定义，1

import com.chillingvan.canvasgl.ICanvasGL; // OpenGL画布接口，用于硬件加速渲染，1
import com.micsig.base.Logger; // 日志工具类，用于调试输出，1
import com.micsig.tbook.scope.ScopeBase; // 示波器基础类，提供坐标转换功能，1
import com.micsig.tbook.tbookscope.GlobalVar; // 全局变量管理类，提供波形区域尺寸，1
import com.micsig.tbook.tbookscope.tools.Tools; // 工具类，提供通道位置计算，1
import com.micsig.tbook.tbookscope.util.CacheUtil; // 缓存工具类，用于持久化波形位置，1
import com.micsig.tbook.tbookscope.wavezone.IWorkMode; // 工作模式接口，定义模式切换方法，1
import com.micsig.tbook.tbookscope.wavezone.WorkModeManage; // 工作模式管理器，管理当前工作模式，1
import com.micsig.tbook.ui.util.TBookUtil; // TBook工具类，提供UI辅助功能，1
import com.micsig.tbook.ui.wavezone.IWave; // 波形接口，定义波形基础操作，1
import com.micsig.tbook.ui.wavezone.TChan; // 通道常量类，定义通道编号常量，1

import java.util.HashMap; // Java哈希映射类，用于存储波形映射，1
import java.util.Map; // Java映射接口，定义键值对存储规范，1

/**
 * 数学/参考波形管理类 - 示波器数学运算波形和参考波形的显示管理核心类
 * 
 * 【模块定位】
 * - 所属模块：wavezone.wave（波形显示区域-波形管理子模块）
 * - 核心职责：管理数学运算波形（Math1、Math2）和参考波形（Ref1-Ref8）的显示、位置、选中状态
 * - 架构层级：业务实现层，实现IWaveShowManage和IWorkMode接口
 * 
 * 【核心职责】
 * 1. 波形实例管理：使用HashMap管理多个数学/参考波形实例（Math1、Math2、Ref1-Ref8）
 * 2. 波形位置管理：管理所有数学/参考波形的垂直位置（Y轴），支持位置移动和像素偏移
 * 3. 波形选中状态管理：管理波形的选中状态，采用单选模式（同一时间只有一个波形被选中）
 * 4. 工作模式切换：支持YT模式和YTZOOM模式之间的切换，更新波形位置
 * 5. 波形可见性管理：控制各波形的显示/隐藏状态
 * 6. 波形标签管理：管理波形标签的显示位置和文本内容
 * 7. 波形位置缓存更新：将波形位置持久化到CacheUtil缓存
 * 8. 波形刷新：触发所有波形的重新绘制
 * 9. 位图资源更新：动态更新波形显示的位图资源
 * 
 * 【架构图】
 * ┌─────────────────────────────────────────────────────────────┐
 * │            WaveManage_YT（波形管理器-YT模式）               │
 * │                   (波形管理核心)                             │
 * │  - 创建MathRefWaveManage实例                                │
 * │  - 提供波形位图资源resBmp                                    │
 * │  - 协调波形显示流程                                          │
 * │  - 注册回调监听器                                            │
 * └─────────────────────────────────────────────────────────────┘
 *                              ↓ 创建和管理
 * ┌─────────────────────────────────────────────────────────────┐
 * │                  MathRefWaveManage（本类）                   │
 * │                 (数学/参考波形管理器)                        │
 * │  ┌──────────────────────────────────────────────────────┐  │
 * │  │  波形实例管理                                         │  │
 * │  │  - mapChannel: HashMap<Integer, MathRefWave>         │  │
 * │  │  - 管理10个波形实例（Math1、Math2、Ref1-Ref8）        │  │
 * │  │  - 每个波形独立管理位置、可见性、选中状态             │  │
 * │  └──────────────────────────────────────────────────────┘  │
 * │  ┌──────────────────────────────────────────────────────┐  │
 * │  │  核心方法                                             │  │
 * │  │  - draw(): 绘制所有波形                               │  │
 * │  │  - isSelected(): 查询选中波形                         │  │
 * │  │  - setOffsetY(): 设置选中波形偏移                     │  │
 * │  │  - switchWorkMode(): 切换工作模式                     │  │
 * │  │  - setVisible(): 设置波形可见性                       │  │
 * │  │  - selectCursor(): 触摸选择波形                       │  │
 * │  │  - setChannelLabel(): 设置波形标签                    │  │
 * │  └──────────────────────────────────────────────────────┘  │
 * │  ┌──────────────────────────────────────────────────────┐  │
 * │  │  资源管理                                             │  │
 * │  │  - resBmp: Bitmap[][] 波形位图资源数组                │  │
 * │  │  - updateResBmp(): 动态更新位图资源                   │  │
 * │  └──────────────────────────────────────────────────────┘  │
 * └─────────────────────────────────────────────────────────────┘
 *                              ↓ 管理实例
 * ┌─────────────────────────────────────────────────────────────┐
 * │                  MathRefWave（波形实例类）                   │
 * │                 (单个数学/参考波形实现)                      │
 * │  - 实现IWave接口                                            │
 * │  - 管理单个波形的显示、位置、状态                            │
 * │  - 提供draw()、setY()、setVisible()等方法                   │
 * └─────────────────────────────────────────────────────────────┘
 *                              ↓ 实现接口
 * ┌─────────────────────────────────────────────────────────────┐
 * │    IWaveShowManage接口              │    IWorkMode接口      │
 * │  - 波形显示管理                     │  - 工作模式切换        │
 * │  - isSelected()                    │  - switchWorkMode()   │
 * │  - setOffsetY()                    │  - YT/YTZOOM模式支持  │
 * │  - draw(Canvas/ICanvasGL)          │                        │
 * └─────────────────────────────────────────────────────────────┘
 * 
 * 【数学/参考波形管理流程】
 * 1. 初始化阶段：
 *    WaveManage_YT创建 → 传入resBmp资源 → 构造MathRefWaveManage → 
 *    遍历Math1到Ref8 → 创建MathRefWave实例 → 设置初始位置 → 存入mapChannel
 * 
 * 2. 显示更新阶段：
 *    状态变化(setSelected/setVisible/setY) → 遍历mapChannel → 调用各波形draw() → GL刷新
 * 
 * 3. 触摸选择流程：
 *    用户触摸(x,y) → selectCursor() → 遍历波形 → 检测碰撞 → 设置选中状态 → 返回选中通道号
 * 
 * 4. 位置调整流程：
 *    用户拖动 → setOffsetY() → 找到选中波形 → 更新Y坐标 → 触发重绘 → 更新缓存
 * 
 * 5. 工作模式切换流程：
 *    切换YT/YTZOOM → switchWorkMode() → 遍历所有波形 → 调用各波形switchWorkMode() → 更新位置
 * 
 * 6. 标签管理流程：
 *    设置标签 → setChannelLabel() → 找到目标波形 → 设置label → 更新位置 → 触发重绘
 * 
 * 【依赖关系】
 * - 实现接口：IWaveShowManage（波形显示管理接口）、IWorkMode（工作模式接口）
 * - 管理实例：MathRefWave（数学/参考波形实例类）
 * - 资源依赖：WaveManage_YT（提供波形位图资源resBmp）
 * - 工具依赖：ScopeBase（坐标转换）、GlobalVar（全局配置）、CacheUtil（缓存）、Tools（位置计算）
 * - 回调接口：IWave.OnMovingWaveEvent（波形移动回调）、IWave.OnSelectChangeEvent（选中状态变化回调）
 * - 通道常量：TChan（定义Math1、Math2、Ref1-Ref8等通道编号）
 * 
 * 【设计模式】
 * 1. 管理器模式（Manager Pattern）：
 *    - 作为波形实例的管理中心，统一管理多个MathRefWave实例
 *    - 提供统一的访问接口，隐藏内部实现细节
 *    - 使用HashMap存储波形实例，便于快速查找和管理
 * 
 * 2. 组合模式（Composite Pattern）：
 *    - 将多个MathRefWave实例组合成树形结构
 *    - 提供统一的操作接口（如draw()、switchWorkMode()），批量操作所有子对象
 *    - 用户无需关心单个波形实例，通过管理器统一操作
 * 
 * 3. 观察者模式（Observer Pattern）：
 *    - 通过OnSelectChangeEvent和OnMovingWaveEvent回调通知状态变化
 *    - 波形状态变化时，通知上层管理器进行相应处理
 * 
 * 4. 单选模式（Single Selection Pattern）：
 *    - 同一时间只有一个波形被选中
 *    - selectCursor()方法会取消其他波形的选中状态
 *    - setSelectCursor()方法会设置唯一选中波形
 * 
 * 【使用场景】
 * 1. 数学波形显示：管理Math1、Math2数学运算波形（如A+B、A-B、A×B、A/B等）
 * 2. 参考波形显示：管理Ref1-Ref8参考波形（用户保存的参考波形）
 * 3. 波形位置调整：用户通过拖拽调整数学/参考波形的垂直位置
 * 4. 工作模式切换：在YT和YTZOOM模式间切换时更新波形位置
 * 5. 波形选择操作：用户点击选择某个数学/参考波形进行参数调整
 * 6. 波形可见性控制：显示或隐藏特定的数学/参考波形
 * 7. 波形标签编辑：编辑数学/参考波形的标签文本
 * 
 * 【通道编号说明】
 * - Math1: TChan.Math1（数学运算波形1）
 * - Math2: TChan.Math2（数学运算波形2）
 * - Ref1: TChan.Ref1（参考波形1）
 * - Ref2: TChan.Ref2（参考波形2）
 * - Ref3: TChan.Ref3（参考波形3）
 * - Ref4: TChan.Ref4（参考波形4）
 * - Ref5: TChan.Ref5（参考波形5）
 * - Ref6: TChan.Ref6（参考波形6）
 * - Ref7: TChan.Ref7（参考波形7）
 * - Ref8: TChan.Ref8（参考波形8）
 * 
 * 【注意事项】
 * 1. 线程安全：draw()方法遍历mapChannel时需要注意并发访问
 * 2. 坐标转换：UI坐标和系统坐标需要通过ScopeBase进行转换
 * 3. 资源管理：位图资源resBmp由WaveManage_YT统一管理，本类只持有引用
 * 4. 选中状态：同一时间只有一个波形被选中，采用单选模式
 * 5. 缓存更新：波形位置变化时需要更新CacheUtil缓存，用于持久化
 * 6. 工厂重置：CacheUtil.isClickFactoryReset()标识是否为工厂重置状态
 * 
 * @author liwb
 * @version 1.0
 * @since 2017/5/19
 * @see IWaveShowManage
 * @see IWorkMode
 * @see MathRefWave
 * @see WaveManage_YT
 * @see ScopeBase
 * @see TChan
 */

public class MathRefWaveManage implements IWaveShowManage, IWorkMode { // 实现 IWaveShowManage 和 IWorkMode 接口，定义数学/参考波形管理核心类，1
    
    // ================================ 常量定义区域 ================================
    
    /**
     * 日志标签
     * 用于调试输出，标识本类的日志来源
     */
    private static final String TAG = "MathRefWaveManage"; // 日志标签常量，值为"MathRefWaveManage"，用于调试输出，1
    
    // ================================ 属性定义区域 ================================
    
    /**
     * 波形位图资源数组
     * 存储所有数学/参考波形的位图资源
     * 二维数组结构：
     * - 第一维：通道索引（对应Math1、Math2、Ref1-Ref8）
     * - 第二维：位图状态（对应MathRefWave中的6种状态）
     * 由WaveManage_YT提供，本类只持有引用
     */
    private Bitmap[][] resBmp; // 波形位图资源数组，存储所有通道的位图资源，由WaveManage_YT提供，1

    // ================================ 私有变量区域 ================================
    
    /**
     * 波形实例映射表
     * 使用HashMap存储所有数学/参考波形实例
     * 键：通道编号（Integer），对应TChan定义的通道常量
     * 值：MathRefWave实例，管理单个波形的显示和状态
     * 包含的波形：
     * - Math1（数学运算波形1）
     * - Math2（数学运算波形2）
     * - Ref1-Ref8（参考波形1-8）
     */
    private Map<Integer, MathRefWave> mapChannel = new HashMap<>(); // 波形实例映射表，使用HashMap存储所有数学/参考波形实例，键为通道编号，值为MathRefWave实例，1
    
    // 已注释的变量，保留供参考
//    private Bitmap bmp; // 绘制缓冲位图（已废弃），1
//    private Canvas mCanvas; // 画布对象（已废弃），1
//    private Paint paint; // 绘制画笔（已废弃），1
    
    // ================================ 构造方法区域 ================================
    
    /**
     * 构造方法 - 初始化数学/参考波形管理器
     * 
     * 【功能说明】
     * 创建MathRefWaveManage实例，初始化所有数学/参考波形实例
     * 遍历Math1到Ref8，为每个通道创建MathRefWave实例并设置初始状态
     * 
     * 【参数说明】
     * @param resBmp 波形位图资源二维数组，由WaveManage_YT提供
     *               第一维：通道索引（对应各通道）
     *               第二维：位图状态（6种状态位图）
     * @param onMovingWaveEvent 波形移动事件回调接口
     *                           当波形位置发生变化时触发回调
     * @param onSelectChangeEvent 选中状态变化事件回调接口
     *                             当波形选中状态发生变化时触发回调
     * 
     * 【初始化流程】
     * 1. 保存位图资源引用
     * 2. 遍历Math1到Ref8通道（使用TChan.foreachMath1ToR8）
     * 3. 为每个通道创建MathRefWave实例
     * 4. 设置通道名称ID（用于标识）
     * 5. 设置初始Y位置（使用Tools.getYTChannelPositionUI计算）
     * 6. 设置默认选中状态为true（初始化时选中）
     * 7. 设置默认可见性为false（初始化时隐藏）
     * 8. 注册移动事件回调
     * 9. 注册选中状态变化回调
     * 10. 初始化标签矩形区域
     * 11. 将实例存入mapChannel映射表
     * 
     * 【使用示例】
     * Bitmap[][] resBmp = waveManage_YT.getResBmp();
     * MathRefWaveManage manager = new MathRefWaveManage(
     *     resBmp,
     *     onMovingWaveEvent,
     *     onSelectChangeEvent
     * );
     */
    public MathRefWaveManage(Bitmap[][] resBmp, IWave.OnMovingWaveEvent onMovingWaveEvent, IWave.OnSelectChangeEvent onSelectChangeEvent) { // 构造方法，初始化数学/参考波形管理器，接收位图资源和回调接口，1
        this.resBmp = resBmp; // 保存位图资源引用，用于后续波形显示，1
        TChan.foreachMath1ToR8((chNo)-> { // 遍历Math1到Ref8通道，使用TChan.foreachMath1ToR8方法，1
            MathRefWave channelWave = new MathRefWave(this.resBmp[chNo]); // 创建MathRefWave实例，传入对应通道的位图资源，1
            channelWave.setLineNameId(chNo); // 设置通道名称ID，用于标识波形所属通道，1
            channelWave.setY(Tools.getYTChannelPositionUI(chNo)); // 设置初始Y位置，使用Tools计算YT模式下的通道位置，1
            channelWave.setSelected(true); // 设置默认选中状态为true，初始化时选中该波形，1
            channelWave.setVisible(false); // 设置默认可见性为false，初始化时隐藏波形，1
            channelWave.setOnMovingWaveEvent(onMovingWaveEvent); // 注册波形移动事件回调，用于通知位置变化，1
            channelWave.setOnSelectChangeEvent(onSelectChangeEvent); // 注册选中状态变化回调，用于通知选中状态变化，1
            channelWave.initLabelRect(); // 初始化标签矩形区域，用于触摸检测和位置管理，1
            mapChannel.put(chNo, channelWave); // 将波形实例存入映射表，键为通道编号，值为MathRefWave实例，1
        }); // 结束遍历回调，1
    } // 结束构造方法，1

    // ================================ 波形变化处理方法区域 ================================
    
    /**
     * 设置波形变化 - 处理波形区域高度变化
     * 
     * 【功能说明】
     * 当波形区域高度发生变化时，更新所有波形的位置缓存并触发刷新
     * 用于响应波形区域尺寸调整
     * 
     * 【参数说明】
     * @param height 新的波形区域高度（像素）
     * 
     * 【处理流程】
     * 1. 更新所有波形的位置缓存
     * 2. 触发所有波形刷新
     * 
     * 【调用时机】
     * - 波形区域尺寸调整时
     * - 工作模式切换导致区域尺寸变化时
     * 
     * 【注意事项】
     * 已注释的代码为旧的位图缩放逻辑，已废弃
     */
    public void setWaveChange(int  height) { // 设置波形变化方法，处理波形区域高度变化，参数为新的高度值，1
//        bmp = Bitmap.createScaledBitmap(bmp, GlobalVar.get().getWaveZoneWidth_Pix(WorkModeManage.getInstance().getmWorkMode()), height, true); // 已废弃：缩放位图到新尺寸，1
//        mCanvas.setBitmap(bmp); // 已废弃：设置画布绑定的新位图，1
        updateChannelPosCache(); // 更新所有波形的位置缓存，将当前位置持久化到CacheUtil，1
        refresh(); // 触发所有波形刷新，重新绘制波形标签，1
    } // 结束setWaveChange方法，1
    
    /**
     * 更新通道位置缓存 - 持久化波形位置到缓存
     * 
     * 【功能说明】
     * 将所有波形的当前位置更新到CacheUtil缓存中
     * 用于持久化波形位置，支持恢复和工厂重置
     * 
     * 【处理流程】
     * 1. 遍历mapChannel中的所有波形实例
     * 2. 更新每个波形的标签位置
     * 3. 根据工厂重置状态决定处理方式：
     *    - 如果是工厂重置状态：使用EventBus方式设置默认位置
     *    - 如果不是工厂重置状态：将位置保存到CacheUtil
     * 4. 重置工厂重置标识为false
     * 
     * 【工厂重置处理】
     * 当isClickFactoryReset()为true时，表示执行工厂重置
     * 使用公式计算默认位置：
     * Y = 108 * (通道ID - 2 - (通道ID - 9) * 1.5)
     * 该公式根据通道类型计算不同的默认位置
     * 
     * 【位置计算公式说明】
     * - Math1（通道ID=3）：Y = 108 * (3 - 2 - (3 - 9) * 1.5) = 108 * (1 + 9) = 1080
     * - Math2（通道ID=4）：Y = 108 * (4 - 2 - (4 - 9) * 1.5) = 108 * (2 + 7.5) = 1026
     * - Ref1-Ref8：类似计算
     * 
     * 【调用时机】
     * - 波形区域尺寸变化时
     * - 工厂重置执行时
     * - 波形位置需要持久化时
     */
    private void updateChannelPosCache() { // 更新通道位置缓存方法，持久化波形位置到CacheUtil，1
        for (Integer i : mapChannel.keySet()) { // 遍历mapChannel的所有键（通道编号），1
            MathRefWave tem = mapChannel.get(i); // 获取对应通道的MathRefWave实例，1
            if (tem == null) continue; // 如果实例为null，跳过本次循环，继续下一个，1
            tem.changeLabelPos(tem.getLabelRect().left); // 更新波形的标签位置，使用当前标签矩形的左边界，1
            if (CacheUtil.get().isClickFactoryReset()) { // 检查是否为工厂重置状态，如果为true则执行工厂重置逻辑，1
                tem.setYFromEventBus((int) Math.round(108 * (tem.getLineNameID() - 2 - (tem.getLineNameID() - 9) * 1.5))); // 使用公式计算默认位置并通过EventBus设置，工厂重置时恢复默认位置，1
            } else { // 如果不是工厂重置状态，执行正常的位置保存逻辑，1
                Tools.putChannelPosition(tem.getLineNameID(), ScopeBase.changeAccuracy(tem.getPosY() * ScopeBase.getToUICoff())); // 将当前位置保存到CacheUtil，使用Tools.putChannelPosition方法，1
            } // 结束工厂重置判断，1
        } // 结束遍历循环，1
        CacheUtil.get().setClickFactoryReset(false); // 重置工厂重置标识为false，表示工厂重置已完成，1
    } // 结束updateChannelPosCache方法，1
    
    /**
     * 更新位图资源 - 动态更新指定通道的位图资源
     * 
     * 【功能说明】
     * 更新指定通道的波形位图资源，通常在主题切换或资源重新加载时调用
     * 
     * 【参数说明】
     * @param chIndex 通道索引，对应TChan定义的通道编号
     *                如：TChan.Math1、TChan.Math2、TChan.Ref1等
     * @param resBmp 新的波形位图资源二维数组
     *               包含所有通道的位图资源
     * 
     * 【处理流程】
     * 1. 更新本类的位图资源引用
     * 2. 从mapChannel获取指定通道的波形实例
     * 3. 如果实例存在，调用其changeBitMap方法更新位图
     * 
     * 【调用时机】
     * - 主题切换时，需要更新波形显示样式
     * - 资源重新加载时
     * - 显示样式更新时
     * 
     * 【使用示例】
     * manager.updateResBmp(TChan.Math1, newResBmp);
     */
    public void updateResBmp(int chIndex, Bitmap[][] resBmp) { // 更新位图资源方法，动态更新指定通道的位图资源，参数为通道索引和新的位图资源数组，1
        this.resBmp = resBmp; // 更新本类的位图资源引用，保存新的位图资源数组，1
        MathRefWave mathRefWave = mapChannel.get(chIndex); // 从mapChannel获取指定通道的MathRefWave实例，1
        if (mathRefWave == null) return; // 如果实例为null，直接返回，不执行后续操作，1
        mathRefWave.changeBitMap(this.resBmp[chIndex]); // 调用波形实例的changeBitMap方法，更新其位图资源，1
    } // 结束updateResBmp方法，1

    // ================================ IWorkMode接口实现区域 ================================
    
    /**
     * 切换工作模式 - IWorkMode接口实现
     * 
     * 【功能说明】
     * 根据工作模式更新所有数学/参考波形的垂直位置
     * 不同工作模式下波形区域高度不同，需要重新计算位置
     * 
     * 【参数说明】
     * @param workMode 目标工作模式，使用@WorkMode注解约束取值范围
     *                 取值范围：
     *                 - IWorkMode.WorkMode_YT：YT模式
     *                 - IWorkMode.WorkMode_YTZOOM：YT缩放模式
     *                 - IWorkMode.WorkMode_XY：XY模式
     *                 - IWorkMode.WorkMode_None：无模式
     * 
     * 【调用时机】
     * - 用户切换工作模式时
     * - WorkModeManage通知模式变化时
     * - 初始化显示组件时
     * 
     * 【处理逻辑】
     * 遍历所有波形实例，调用各自的switchWorkMode方法
     * 每个波形实例会根据模式更新自己的位置：
     * - YT模式：使用YT通道位置
     * - YTZOOM模式：使用缩放通道位置
     * 
     * 【使用示例】
     * manager.switchWorkMode(IWorkMode.WorkMode_YT);
     * manager.switchWorkMode(IWorkMode.WorkMode_YTZOOM);
     */
    @Override // 标记为接口方法实现，1
    public void switchWorkMode(@WorkMode int workMode) { // 切换工作模式方法，IWorkMode接口实现，参数为目标工作模式标识，1
        for (MathRefWave c : mapChannel.values()) { // 遍历mapChannel中的所有MathRefWave实例，1
            c.switchWorkMode(workMode); // 调用每个波形实例的switchWorkMode方法，更新其位置，1
        } // 结束遍历循环，1
    } // 结束switchWorkMode方法，1

    // ================================ IWaveShowManage接口实现区域 ================================
    
    /**
     * 绘制波形 - IWaveShowManage接口实现（Canvas版本）
     * 
     * 【功能说明】
     * 使用Android Canvas绘制所有数学/参考波形
     * 先绘制未选中的波形，再绘制选中的波形（确保选中波形在最上层）
     * 
     * 【参数说明】
     * @param canvas Android画布对象，用于绑定绘制目标
     *               由调用方提供，通常是View的onDraw方法传入
     * 
     * 【绘制流程】
     * 1. 遍历所有波形实例，调用各自的draw方法
     * 2. 查询当前选中的波形
     * 3. 如果有选中波形，再次绘制（确保在最上层）
     * 
     * 【绘制顺序说明】
     * - 第一轮绘制：绘制所有波形（包括选中和未选中）
     * - 第二轮绘制：如果有选中波形，再次绘制选中波形
     * 这样确保选中波形始终显示在最上层，不会被其他波形遮挡
     * 
     * 【调用时机】
     * - View的onDraw方法中被调用，每帧绘制时
     * - 波形数据更新后需要重绘时
     * - 波形位置、参数调整后需要刷新时
     * 
     * 【注意事项】
     * 当前使用OpenGL版本绘制，此Canvas版本保留用于兼容性
     */
    @Override // 标记为接口方法实现，1
    public void draw(Canvas canvas) { // 绘制波形方法（Canvas版本），IWaveShowManage接口实现，参数为Android画布对象，1
        for (MathRefWave c : mapChannel.values()) { // 遍历mapChannel中的所有MathRefWave实例，1
            c.draw(canvas); // 调用每个波形实例的draw方法，绘制波形标签，1
        } // 结束遍历循环，1
        int flag = isSelected(); // 查询当前选中的波形通道号，返回-1表示无选中，1
        if (flag != -1) { // 如果有选中波形（flag不为-1），执行选中波形再次绘制，1
            mapChannel.get(flag).draw(canvas); // 再次绘制选中波形，确保其显示在最上层，1
        } // 结束选中判断，1
    } // 结束draw方法（Canvas版本），1
    
    /**
     * 绘制波形 - IWaveShowManage接口实现（OpenGL版本）
     * 
     * 【功能说明】
     * 使用OpenGL绘制所有可见的数学/参考波形
     * 只绘制可见波形，节省渲染资源
     * 先绘制未选中的波形，再绘制选中的波形（确保选中波形在最上层）
     * 
     * 【参数说明】
     * @param canvas OpenGL画布接口对象，用于硬件加速渲染
     *               由GLSurfaceView的渲染线程提供
     * 
     * 【绘制流程】
     * 1. 遍历所有波形实例
     * 2. 只绘制可见波形（visible为true）
     * 3. 查询当前选中的波形
     * 4. 如果有选中波形，再次绘制（确保在最上层）
     * 
     * 【绘制顺序说明】
     * - 第一轮绘制：只绘制可见波形（节省资源）
     * - 第二轮绘制：如果有选中波形，再次绘制选中波形
     * 这样确保选中波形始终显示在最上层，且只绘制可见波形提高性能
     * 
     * 【调用时机】
     * - GLSurfaceView的渲染回调中被调用
     * - 需要高性能渲染时使用
     * - 每帧渲染时调用
     * 
     * 【性能考虑】
     * - 只绘制可见波形，节省GPU渲染资源
     * - OpenGL渲染比Canvas渲染更快
     * - 支持硬件加速
     */
    @Override // 标记为接口方法实现，1
    public void draw(ICanvasGL canvas) { // 绘制波形方法（OpenGL版本），IWaveShowManage接口实现，参数为OpenGL画布接口对象，1
        for (MathRefWave c : mapChannel.values()) { // 遍历mapChannel中的所有MathRefWave实例，1
            if (c.getVisible() == true) c.draw(canvas); // 如果波形可见（visible为true），调用其draw方法进行绘制，1
        } // 结束遍历循环，1
        int flag = isSelected(); // 查询当前选中的波形通道号，返回-1表示无选中，1
        if (flag != -1) { // 如果有选中波形（flag不为-1），执行选中波形再次绘制，1
            mapChannel.get(flag).draw(canvas); // 再次绘制选中波形，确保其显示在最上层，1
        } // 结束选中判断，1
    } // 结束draw方法（OpenGL版本），1
    
    /**
     * 设置波形可见性 - 控制指定波形的显示/隐藏状态
     * 
     * 【功能说明】
     * 设置指定通道波形的显示或隐藏状态
     * 
     * 【参数说明】
     * @param ChNo 通道编号，对应TChan定义的通道常量
     *             如：TChan.Math1、TChan.Math2、TChan.Ref1等
     * @param visible 可见性状态
     *                true：显示波形
     *                false：隐藏波形
     * 
     * 【处理流程】
     * 1. 遍历所有波形实例
     * 2. 找到匹配通道编号的波形
     * 3. 设置其可见性状态
     * 
     * 【调用时机】
     * - 用户通过UI显示/隐藏数学/参考波形时
     * - 加载保存的波形配置时
     * - 波形功能启用/禁用时
     * 
     * 【使用示例】
     * manager.setVisible(TChan.Math1, true);  // 显示Math1波形
     * manager.setVisible(TChan.Ref1, false);  // 隐藏Ref1波形
     */
    public void setVisible(int ChNo, boolean visible) { // 设置波形可见性方法，控制指定通道波形的显示/隐藏状态，参数为通道编号和可见性状态，1
        for (MathRefWave c : mapChannel.values()) { // 遍历mapChannel中的所有MathRefWave实例，1
            if (c.getLineNameID() == ChNo) { // 如果波形实例的通道ID匹配目标通道编号，1
                c.setVisible(visible); // 设置波形实例的可见性状态，true为显示，false为隐藏，1
            } // 结束通道匹配判断，1
        } // 结束遍历循环，1
    } // 结束setVisible方法，1
    
    /**
     * 查询选中波形 - IWaveShowManage接口实现
     * 
     * 【功能说明】
     * 返回当前选中的波形通道编号
     * 采用单选模式，同一时间只有一个波形被选中
     * 
     * 【返回值】
     * @return int - 选中的波形通道编号
     *         -1：表示没有选中任何波形
     *         其他值：选中的波形通道编号（对应TChan定义）
     * 
     * 【处理流程】
     * 1. 初始化flag为-1（表示无选中）
     * 2. 遍历所有波形实例
     * 3. 检查每个波形的选中状态
     * 4. 如果找到选中波形，返回其通道编号
     * 5. 如果遍历结束未找到，返回-1
     * 
     * 【调用时机】
     * - 用户点击波形标签时，查询当前选中状态
     * - 调整波形参数时，确认操作的目标波形
     * - 绘制选中高亮效果时，确定需要高亮的波形
     * - draw方法中确定需要再次绘制的波形
     * 
     * 【单选模式说明】
     * 本方法假设同一时间只有一个波形被选中
     * 如果有多个波形被选中（异常情况），返回第一个找到的选中波形
     */
    @Override // 标记为接口方法实现，1
    public int isSelected() { // 查询选中波形方法，IWaveShowManage接口实现，返回当前选中的波形通道编号，1
        int flag = -1; // 初始化flag为-1，表示无选中波形，1
        for (Integer i : mapChannel.keySet()) { // 遍历mapChannel的所有键（通道编号），1
            MathRefWave tem = mapChannel.get(i); // 获取对应通道的MathRefWave实例，1
            if (tem.isSelected() == true) { // 如果波形实例的选中状态为true，表示该波形被选中，1
                flag = i; // 将flag设置为当前通道编号，记录选中波形，1
                return flag; // 立即返回选中波形的通道编号，结束方法，1
            } // 结束选中状态判断，1
        } // 结束遍历循环，1
        return flag; // 返回flag，如果未找到选中波形则返回-1，1
    } // 结束isSelected方法，1
    
    /**
     * 设置波形Y偏移 - IWaveShowManage接口实现
     * 
     * 【功能说明】
     * 设置选中波形在Y轴的偏移位置
     * 用于调整选中波形的垂直位置（拖动波形上下移动）
     * 
     * 【参数说明】
     * @param offsetY Y轴偏移量（像素）
     *                正值：波形向下移动
     *                负值：波形向上移动
     * 
     * 【处理流程】
     * 1. 遍历所有波形实例
     * 2. 找到选中状态的波形
     * 3. 更新其Y坐标（当前位置减去偏移量）
     * 
     * 【调用时机】
     * - 用户拖动选中波形调整垂直位置时
     * - 编程调整波形位置时
     * - 波形位置微调时
     * 
     * 【使用示例】
     * manager.setOffsetY(10);   // 选中波形向下移动10像素
     * manager.setOffsetY(-5);   // 选中波形向上移动5像素
     * 
     * 【注意事项】
     * 只影响选中波形，其他波形位置不变
     * 偏移量是相对于当前位置的增量
     */
    @Override // 标记为接口方法实现，1
    public void setOffsetY(int offsetY) { // 设置波形Y偏移方法，IWaveShowManage接口实现，参数为Y轴偏移量（像素），1
        for (MathRefWave c : mapChannel.values()) { // 遍历mapChannel中的所有MathRefWave实例，1
            if (c.isSelected() == true) { // 如果波形实例的选中状态为true，表示该波形被选中，1
                c.setY(c.getY() - offsetY); // 设置波形的新Y坐标，当前位置减去偏移量，实现波形移动，1
            } // 结束选中状态判断，1
        } // 结束遍历循环，1
    } // 结束setOffsetY方法，1
    
    // ================================ 波形位置管理方法区域 ================================
    
    /**
     * 设置波形Y坐标 - 直接设置指定波形的Y位置
     * 
     * 【功能说明】
     * 直接设置指定通道波形的Y坐标位置
     * 不通过选中状态，直接指定目标通道
     * 
     * 【参数说明】
     * @param ch 通道编号，对应TChan定义的通道常量
     *           如：TChan.Math1、TChan.Math2、TChan.Ref1等
     * @param y Y坐标值（屏幕像素坐标）
     *          表示波形在屏幕上的垂直位置
     * 
     * 【处理流程】
     * 1. 遍历所有波形实例
     * 2. 找到匹配通道编号的波形
     * 3. 设置其Y坐标
     * 
     * 【调用时机】
     * - 加载保存的波形位置配置时
     * - 编程设置波形位置时
     * - 恢复波形位置时
     * 
     * 【使用示例】
     * manager.setY(TChan.Math1, 200);  // 设置Math1波形Y坐标为200像素
     */
    public void setY(int ch, double y) { // 设置波形Y坐标方法，直接设置指定通道波形的Y位置，参数为通道编号和Y坐标值，1
        for (MathRefWave c : mapChannel.values()) { // 遍历mapChannel中的所有MathRefWave实例，1
            if (c.getLineNameID() == ch) { // 如果波形实例的通道ID匹配目标通道编号，1
                c.setY(y); // 设置波形实例的Y坐标，直接设置新位置，1
            } // 结束通道匹配判断，1
        } // 结束遍历循环，1
    } // 结束setY方法，1
    
    /**
     * 设置波形Y坐标（来自EventBus） - 避免EventBus消息循环
     * 
     * 【功能说明】
     * 由EventBus触发的Y坐标设置，不触发EventBus回调
     * 避免EventBus消息循环（设置位置→发送EventBus→接收EventBus→设置位置→...）
     * 
     * 【参数说明】
     * @param ch 通道编号，对应TChan定义的通道常量
     *           如：TChan.Math1、TChan.Math2、TChan.Ref1等
     * @param y Y坐标值（屏幕像素坐标）
     *          表示波形在屏幕上的垂直位置
     * 
     * 【处理流程】
     * 1. 遍历所有波形实例
     * 2. 找到匹配通道编号的波形
     * 3. 调用其setYFromEventBus方法（标识来自EventBus）
     * 
     * 【与setY的区别】
     * - setY：正常设置，会触发EventBus回调
     * - setYFromEventBus：标识来自EventBus，回调时isFromEventBus参数为true
     * 
     * 【调用时机】
     * - 接收EventBus消息设置波形位置时
     * - 工厂重置恢复默认位置时
     * - 需要避免EventBus循环时
     * 
     * 【使用示例】
     * // EventBus消息处理中调用
     * manager.setYFromEventBus(TChan.Math1, 150);
     */
    public void setYFromEventBus(int ch, double y) { // 设置波形Y坐标方法（来自EventBus），避免EventBus消息循环，参数为通道编号和Y坐标值，1
        for (MathRefWave c : mapChannel.values()) { // 遍历mapChannel中的所有MathRefWave实例，1
            if (c.getLineNameID() == ch) { // 如果波形实例的通道ID匹配目标通道编号，1
                c.setYFromEventBus(y); // 调用波形实例的setYFromEventBus方法，标识来自EventBus，避免消息循环，1
            } // 结束通道匹配判断，1
        } // 结束遍历循环，1
    } // 结束setYFromEventBus方法，1
    
    /**
     * 获取波形Y坐标 - 查询指定波形的Y位置
     * 
     * 【功能说明】
     * 返回指定通道波形的Y坐标位置（屏幕像素坐标）
     * 
     * 【参数说明】
     * @param ch 通道编号，对应TChan定义的通道常量
     *           如：TChan.Math1、TChan.Math2、TChan.Ref1等
     * 
     * 【返回值】
     * @return double - Y坐标值（像素）
     *         如果找不到指定通道，返回0
     * 
     * 【处理流程】
     * 1. 遍历所有波形实例
     * 2. 找到匹配通道编号的波形
     * 3. 返回其Y坐标
     * 4. 如果未找到，返回0
     * 
     * 【调用时机】
     * - 查询波形位置时
     * - 保存波形位置配置时
     * - 计算波形相对位置时
     * 
     * 【使用示例】
     * double y = manager.getY(TChan.Math1);  // 获取Math1波形Y坐标
     */
    public double getY(int ch) { // 获取波形Y坐标方法，查询指定通道波形的Y位置，参数为通道编号，返回Y坐标值，1
        for (MathRefWave c : mapChannel.values()) { // 遍历mapChannel中的所有MathRefWave实例，1
            if (c.getLineNameID() == ch) { // 如果波形实例的通道ID匹配目标通道编号，1
                return c.getY(); // 返回波形实例的Y坐标，结束方法，1
            } // 结束通道匹配判断，1
        } // 结束遍历循环，1
        return 0; // 如果未找到匹配通道，返回0作为默认值，1
    } // 结束getY方法，1
    
    /**
     * 获取波形UI Y坐标 - 查询指定波形的UI坐标位置
     * 
     * 【功能说明】
     * 返回指定通道波形的UI坐标位置
     * 将系统坐标转换为UI坐标后返回
     * 
     * 【参数说明】
     * @param ch 通道编号，对应TChan定义的通道常量
     *           如：TChan.Math1、TChan.Math2、TChan.Ref1等
     * 
     * 【返回值】
     * @return double - UI Y坐标值（像素）
     *         经过ScopeBase坐标转换后的UI坐标
     *         如果找不到指定通道，返回0
     * 
     * 【处理流程】
     * 1. 遍历所有波形实例
     * 2. 找到匹配通道编号的波形
     * 3. 获取其系统坐标（posY）
     * 4. 通过ScopeBase转换为UI坐标
     * 5. 返回转换后的坐标
     * 
     * 【坐标转换说明】
     * - posY：系统内部坐标（1000对应系统）
     * - UI坐标：屏幕像素坐标
     * - 转换公式：UI坐标 = posY * ScopeBase.getToUICoff()
     * - ScopeBase.changeAccuracy()：精度处理
     * 
     * 【调用时机】
     * - 显示波形位置时
     * - 计算波形显示位置时
     * - UI界面更新时
     * 
     * 【使用示例】
     * double uiY = manager.getUIY(TChan.Math1);  // 获取Math1波形UI坐标
     */
    public double getUIY(int ch) { // 获取波形UI Y坐标方法，查询指定通道波形的UI坐标位置，参数为通道编号，返回UI Y坐标值，1
        for (MathRefWave c : mapChannel.values()) { // 遍历mapChannel中的所有MathRefWave实例，1
            if (c.getLineNameID() == ch) { // 如果波形实例的通道ID匹配目标通道编号，1
                return ScopeBase.changeAccuracy(c.getPosY() * ScopeBase.getToUICoff()); // 获取系统坐标posY，乘以转换系数，经过精度处理后返回UI坐标，1
            } // 结束通道匹配判断，1
        } // 结束遍历循环，1
        return 0; // 如果未找到匹配通道，返回0作为默认值，1
    } // 结束getUIY方法，1
    
    /**
     * 查询波形可见性 - 检查指定波形的显示状态
     * 
     * 【功能说明】
     * 返回指定通道波形的可见性状态
     * 
     * 【参数说明】
     * @param ch 通道编号，对应TChan定义的通道常量
     *           如：TChan.Math1、TChan.Math2、TChan.Ref1等
     * 
     * 【返回值】
     * @return boolean - 可见性状态
     *         true：波形可见（正在显示）
     *         false：波形隐藏（未显示）
     *         如果找不到指定通道，返回false
     * 
     * 【处理流程】
     * 1. 遍历所有波形实例
     * 2. 找到匹配通道编号的波形
     * 3. 返回其可见性状态
     * 4. 如果未找到，返回false
     * 
     * 【调用时机】
     * - 查询波形显示状态时
     * - 判断是否需要绘制波形时
     * - 保存波形配置时
     * 
     * 【使用示例】
     * boolean visible = manager.isVisible(TChan.Math1);  // 检查Math1是否可见
     * if (visible) {
     *     // Math1波形正在显示
     * }
     */
    public boolean isVisible(int ch) { // 查询波形可见性方法，检查指定通道波形的显示状态，参数为通道编号，返回可见性状态，1
        for (MathRefWave c : mapChannel.values()) { // 遍历mapChannel中的所有MathRefWave实例，1
            if (c.getLineNameID() == ch) { // 如果波形实例的通道ID匹配目标通道编号，1
                return c.getVisible(); // 返回波形实例的可见性状态，true为可见，false为隐藏，1
            } // 结束通道匹配判断，1
        } // 结束遍历循环，1
        return false; // 如果未找到匹配通道，返回false作为默认值，表示隐藏状态，1
    } // 结束isVisible方法，1
    
    // ================================ 图像混合模式定义区域 ================================
    
    /**
     * 清除模式 - 用于清除画布内容
     * 使用PorterDuff.Mode.CLEAR模式，完全清除像素
     * 已废弃，保留供参考
     */
    private PorterDuffXfermode clearMode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR); // 清除模式，用于清除画布内容，已废弃，1
    
    /**
     * 源模式 - 用于绘制源图像
     * 使用PorterDuff.Mode.SRC模式，直接绘制源图像
     * 已废弃，保留供参考
     */
    private PorterDuffXfermode srcMode = new PorterDuffXfermode(PorterDuff.Mode.SRC); // 源模式，用于绘制源图像，已废弃，1
    
    // ================================ 已废弃方法区域 ================================
    
//    @Override // 已废弃的接口方法标记，1
//    public Bitmap getWavesBitmap() { // 已废弃：获取波形位图方法，返回波形图，1
//        paint.setXfermode(clearMode); // 已废弃：设置清除模式，1
//        mCanvas.drawPaint(paint); // 已废弃：清空画布，1
//        paint.setXfermode(srcMode); // 已废弃：设置源模式，1
//        for (MathRefWave m : mapChannel.values()) { // 已废弃：遍历所有波形实例，1
//            if (m.isSelected()) continue; // 已废弃：跳过选中波形，先绘制未选中波形，1
////            synchronized (m.getWaveBmp()) { // 已废弃：同步锁定位图，1
////                mCanvas.drawBitmap(m.getWaveBmp(), 0, 0, paint); // 已废弃：绘制波形位图，1
////            } // 已废弃：结束同步块，1
//        } // 已废弃：结束遍历循环，1
//        for (MathRefWave m : mapChannel.values()) { // 已废弃：遍历所有波形实例，1
//            if (m.isSelected()) { // 已废弃：如果波形被选中，1
////                synchronized (m.getWaveBmp()) { // 已废弃：同步锁定位图，1
////                    mCanvas.drawBitmap(m.getWaveBmp(), 0, 0, paint); // 已废弃：绘制选中波形位图，1
////                } // 已废弃：结束同步块，1
//            } // 已废弃：结束选中判断，1
//        } // 已废弃：结束遍历循环，1
//        return bmp; // 已废弃：返回合成后的波形位图，1
//    } // 已废弃：结束getWavesBitmap方法，1

    // ================================ 波形选择方法区域 ================================
    
    /**
     * 选择波形（触摸检测） - 根据触摸坐标选择波形
     * 
     * 【功能说明】
     * 根据触摸坐标(x, y)检测并选择对应的波形
     * 采用单选模式，选中一个波形会取消其他波形的选中状态
     * 
     * 【参数说明】
     * @param x 触摸点X坐标（像素）
     * @param y 触摸点Y坐标（像素）
     * 
     * 【返回值】
     * @return int - 选中的波形通道编号
     *         -1：表示没有选中任何波形（触摸点不在任何波形上）
     *         其他值：选中的波形通道编号（对应TChan定义）
     * 
     * 【处理流程】
     * 1. 初始化selectIndex为-1（表示无选中）
     * 2. 遍历所有波形实例
     * 3. 跳过不可见的波形
     * 4. 调用波形的selectCursor方法检测触摸碰撞
     * 5. 如果碰撞成功，设置该波形为选中状态，取消其他波形选中状态
     * 6. 返回选中波形的通道编号
     * 7. 如果遍历结束未找到，返回-1
     * 
     * 【碰撞检测说明】
     * selectCursor方法会判断触摸点是否在波形标签区域内
     * 如果在区域内，返回true，表示碰撞成功
     * 
     * 【单选模式说明】
     * 选中一个波形时，会自动取消其他波形的选中状态
     * 确保同一时间只有一个波形被选中
     * 
     * 【调用时机】
     * - 用户触摸波形区域选择波形时
     * - 处理触摸事件时
     * - 波形选择交互时
     * 
     * 【使用示例】
     * int selected = manager.selectCursor(100, 200);
     * if (selected != -1) {
     *     // 有波形被选中，selected为选中波形通道号
     * }
     */
    public int selectCursor(int x, int y) { // 选择波形方法（触摸检测），根据触摸坐标选择波形，参数为触摸点X和Y坐标，返回选中波形通道编号，1
        int selectIndex = -1; // 初始化selectIndex为-1，表示无选中波形，1
        for (int i : mapChannel.keySet()) { // 遍历mapChannel的所有键（通道编号），1
            MathRefWave c = mapChannel.get(i); // 获取对应通道的MathRefWave实例，1
//            Logger.i(TAG, "c.name:"+c.getLineNameID()+"  c.visible:"+c.getVisible()); // 已注释的调试日志，输出波形名称和可见性，1
            if (c.getVisible() == false) continue; // 如果波形不可见（visible为false），跳过本次循环，继续下一个波形，1
            if (c.selectCursor(x, y)) { // 调用波形实例的selectCursor方法检测触摸碰撞，如果返回true表示碰撞成功，1
                c.setSelected(true); // 设置该波形为选中状态，显示选中样式，1
                selectIndex = i; // 记录选中波形的通道编号，1
                return selectIndex; // 立即返回选中波形的通道编号，结束方法，1
            }else{ // 如果碰撞检测失败（触摸点不在该波形上），1
                c.setSelected(false); // 设置该波形为未选中状态，取消选中，1
            } // 结束碰撞检测判断，1
        } // 结束遍历循环，1
//        Logger.i(TAG, "selectIndex:"+selectIndex); // 已注释的调试日志，输出选中结果，1
        return selectIndex; // 返回selectIndex，如果未找到选中波形则返回-1，1
    } // 结束selectCursor方法，1
    
    /**
     * 选择波形（触摸检测，排除当前通道） - 根据触摸坐标选择波形，排除指定通道
     * 
     * 【功能说明】
     * 根据触摸坐标(x, y)检测并选择对应的波形
     * 排除指定的当前通道，用于处理特殊选择场景
     * 
     * 【参数说明】
     * @param x 触摸点X坐标（像素）
     * @param y 触摸点Y坐标（像素）
     * @param curChan 当前通道编号，需要排除的通道
     *                用于避免选择当前正在操作的通道
     * 
     * 【返回值】
     * @return int - 选中的波形通道编号
     *         -1：表示没有选中任何波形
     *         其他值：选中的波形通道编号
     * 
     * 【处理流程】
     * 1. 初始化selectIndex为-1
     * 2. 遍历所有波形实例
     * 3. 跳过不可见的波形
     * 4. 调用波形的selectCursor方法检测碰撞
     * 5. 如果碰撞成功且不是当前通道，设置为选中状态
     * 6. 如果碰撞成功且是当前通道，只记录但不改变选中状态
     * 7. 如果碰撞失败，设置为未选中状态
     * 8. 返回选中结果
     * 
     * 【与selectCursor(x, y)的区别】
     * - selectCursor(x, y)：标准选择，不排除任何通道
     * - selectCursor(x, y, curChan)：排除当前通道，用于特殊场景
     * 
     * 【使用场景】
     * - 处理波形标签拖动时，避免选择自己
     * - 处理波形位置调整时，排除当前操作波形
     * - 特殊交互场景需要排除特定通道
     * 
     * 【使用示例】
     * // 当前正在操作Math1，排除Math1的选择
     * int selected = manager.selectCursor(100, 200, TChan.Math1);
     */
    public int selectCursor(int x, int y,int curChan) { // 选择波形方法（触摸检测，排除当前通道），根据触摸坐标选择波形，排除指定通道，参数为触摸点坐标和当前通道编号，返回选中波形通道编号，1
        int selectIndex = -1; // 初始化selectIndex为-1，表示无选中波形，1
        for (int i : mapChannel.keySet()) { // 遍历mapChannel的所有键（通道编号），1
            MathRefWave c = mapChannel.get(i); // 获取对应通道的MathRefWave实例，1
            if (c.getVisible() == false) continue; // 如果波形不可见（visible为false），跳过本次循环，继续下一个波形，1
            if (c.selectCursor(x, y) && i!=curChan) { // 调用波形实例的selectCursor方法检测碰撞，如果碰撞成功且通道编号不等于当前通道，1
                c.setSelected(true); // 设置该波形为选中状态，显示选中样式，1
                selectIndex = i; // 记录选中波形的通道编号，1
                return selectIndex; // 立即返回选中波形的通道编号，结束方法，1
            }else if (c.selectCursor(x,y)){ // 如果碰撞成功但通道编号等于当前通道（触摸点在当前通道波形上），1
                selectIndex=i; // 记录当前通道编号，但不改变选中状态，1
                return selectIndex; // 返回当前通道编号，结束方法，1
            } // 结束碰撞检测判断，1
            else{ // 如果碰撞检测失败（触摸点不在该波形上），1
                c.setSelected(false); // 设置该波形为未选中状态，取消选中，1
            } // 结束碰撞检测else分支，1
        } // 结束遍历循环，1
        return selectIndex; // 返回selectIndex，如果未找到选中波形则返回-1，1
    } // 结束selectCursor方法（带排除参数），1
    
    /**
     * 设置选中波形 - 直接设置指定波形为选中状态
     * 
     * 【功能说明】
     * 直接设置指定通道波形为选中状态
     * 同时取消其他所有波形的选中状态（单选模式）
     * 
     * 【参数说明】
     * @param ChNo 要选中的通道编号，对应TChan定义的通道常量
     *             如：TChan.Math1、TChan.Math2、TChan.Ref1等
     * 
     * 【处理流程】
     * 1. 遍历所有波形实例
     * 2. 如果通道编号匹配，设置为选中状态
     * 3. 如果通道编号不匹配，设置为未选中状态
     * 
     * 【单选模式说明】
     * 此方法确保同一时间只有一个波形被选中
     * 设置指定波形为选中，其他所有波形为未选中
     * 
     * 【调用时机】
     * - 编程设置选中波形时
     * - UI按钮选择波形时
     * - 恢复选中状态时
     * - 初始化选中波形时
     * 
     * 【使用示例】
     * manager.setSelectCursor(TChan.Math1);  // 设置Math1为选中波形
     * manager.setSelectCursor(TChan.Ref1);   // 设置Ref1为选中波形
     */
    public void setSelectCursor(int ChNo) { // 设置选中波形方法，直接设置指定波形为选中状态，参数为要选中的通道编号，1
        for (Integer i : mapChannel.keySet()) { // 遍历mapChannel的所有键（通道编号），1
            MathRefWave tem = mapChannel.get(i); // 获取对应通道的MathRefWave实例，1
            if (tem.getLineNameID() == ChNo) { // 如果波形实例的通道ID匹配目标通道编号，1
                tem.setSelected(true); // 设置该波形为选中状态，显示选中样式，1
            } else { // 如果波形实例的通道ID不匹配目标通道编号，1
                tem.setSelected(false); // 设置该波形为未选中状态，取消选中，1
            } // 结束通道匹配判断，1
        } // 结束遍历循环，1
    } // 结束setSelectCursor方法，1
    
    /**
     * 移动波形像素 - 按像素偏移移动选中波形
     * 
     * 【功能说明】
     * 按像素偏移量移动选中波形的位置
     * 用于波形拖动交互，实现波形位置调整
     * 
     * 【参数说明】
     * @param px Y方向像素偏移量
     *           正值：波形向下移动
     *           负值：波形向上移动
     * 
     * 【处理流程】
     * 1. 遍历所有波形实例
     * 2. 找到选中状态的波形
     * 3. 调用其movePix方法移动位置
     * 4. 找到后立即结束遍历（只有一个选中波形）
     * 
     * 【调用时机】
     * - 用户拖动波形调整位置时
     * - 波形位置微调时
     * - 处理拖动事件时
     * 
     * 【使用示例】
     * manager.movePix(10);   // 选中波形向下移动10像素
     * manager.movePix(-5);   // 选中波形向上移动5像素
     * 
     * 【注意事项】
     * 只影响选中波形，其他波形位置不变
     * 找到选中波形后立即结束，提高效率
     */
    public void movePix(int px) { // 移动波形像素方法，按像素偏移移动选中波形，参数为Y方向像素偏移量，1
        for (Integer i : mapChannel.keySet()) { // 遍历mapChannel的所有键（通道编号），1
            MathRefWave tem = mapChannel.get(i); // 获取对应通道的MathRefWave实例，1
            if (tem.isSelected()) { // 如果波形实例的选中状态为true，表示该波形被选中，1
                tem.movePix(px); // 调用波形实例的movePix方法，按偏移量移动位置，1
                break; // 找到选中波形后立即结束遍历，提高效率（单选模式下只有一个选中波形），1
            } // 结束选中状态判断，1
        } // 结束遍历循环，1
    } // 结束movePix方法，1
    
    // ================================ 波形刷新方法区域 ================================
    
    /**
     * 刷新所有波形 - 触发所有波形重新绘制
     * 
     * 【功能说明】
     * 触发所有数学/参考波形的重新绘制
     * 用于强制更新波形显示
     * 
     * 【处理流程】
     * 1. 遍历mapChannel中的所有波形实例
     * 2. 调用每个波形实例的refresh方法
     * 3. 每个波形实例会触发重绘
     * 
     * 【调用时机】
     * - 波形区域尺寸变化时
     * - 波形显示样式更新时
     * - 强制刷新显示时
     * - 主题切换时
     * 
     * 【使用示例】
     * manager.refresh();  // 刷新所有波形显示
     */
    public void refresh(){ // 刷新所有波形方法，触发所有波形重新绘制，无参数，1
        for(Integer i:mapChannel.keySet()){ // 遍历mapChannel的所有键（通道编号），1
            MathRefWave tem=mapChannel.get(i); // 获取对应通道的MathRefWave实例，1
            tem.refresh(); // 调用波形实例的refresh方法，触发重绘，1
        } // 结束遍历循环，1
    } // 结束refresh方法，1

    // ================================ 波形标签管理方法区域 ================================
    
    /**
     * 设置波形标签 - 设置指定波形的标签文本
     * 
     * 【功能说明】
     * 设置指定通道波形的标签文本内容
     * 标签显示在波形标签区域，用于标识波形
     * 
     * 【参数说明】
     * @param ChNo 通道编号，对应TChan定义的通道常量
     *             如：TChan.Math1、TChan.Math2、TChan.Ref1等
     * @param label 标签文本内容
     *              如："Math1"、"Ref1"、"A+B"等
     * 
     * 【处理流程】
     * 1. 遍历所有波形实例
     * 2. 找到匹配通道编号的波形
     * 3. 设置其标签文本
     * 
     * 【调用时机】
     * - 用户编辑波形标签时
     * - 加载保存的波形配置时
     * - 数学运算表达式更新时
     * 
     * 【使用示例】
     * manager.setChannelLabel(TChan.Math1, "A+B");  // 设置Math1标签为"A+B"
     * manager.setChannelLabel(TChan.Ref1, "参考1");  // 设置Ref1标签为"参考1"
     */
    public void setChannelLabel(int ChNo, String label) { // 设置波形标签方法，设置指定波形的标签文本，参数为通道编号和标签文本，1
        for (MathRefWave c : mapChannel.values()) { // 遍历mapChannel中的所有MathRefWave实例，1
            if (c.getLineNameID() == ChNo) { // 如果波形实例的通道ID匹配目标通道编号，1
                c.setLabel(label); // 设置波形实例的标签文本，更新显示内容，1
            } // 结束通道匹配判断，1
        } // 结束遍历循环，1
    } // 结束setChannelLabel方法，1
    
    /**
     * 获取波形标签 - 查询指定波形的标签文本
     * 
     * 【功能说明】
     * 返回指定通道波形的标签文本内容
     * 
     * 【参数说明】
     * @param chNo 通道编号，对应TChan定义的通道常量
     *             如：TChan.Math1、TChan.Math2、TChan.Ref1等
     * 
     * 【返回值】
     * @return String - 标签文本内容
     *         如果找不到指定通道，返回空字符串""
     * 
     * 【处理流程】
     * 1. 初始化label为空字符串
     * 2. 遍历所有波形实例
     * 3. 找到匹配通道编号的波形
     * 4. 获取其标签文本
     * 5. 找到后立即结束遍历
     * 
     * 【调用时机】
     * - 查询波形标签时
     * - 保存波形配置时
     * - 显示波形信息时
     * 
     * 【使用示例】
     * String label = manager.getChannelLabel(TChan.Math1);  // 获取Math1标签
     */
    public String getChannelLabel(int chNo) { // 获取波形标签方法，查询指定波形的标签文本，参数为通道编号，返回标签文本，1
        String label = ""; // 初始化label为空字符串，作为默认返回值，1
        for (MathRefWave c : mapChannel.values()) { // 遍历mapChannel中的所有MathRefWave实例，1
            if (c.getLineNameID() != chNo) continue; // 如果波形实例的通道ID不匹配目标通道编号，跳过本次循环，继续下一个，1
            label = c.getLabel(); // 获取波形实例的标签文本，赋值给label变量，1
            break; // 找到匹配波形后立即结束遍历，提高效率，1
        } // 结束遍历循环，1
        return label; // 返回标签文本，如果未找到匹配通道则返回空字符串，1
    } // 结束getChannelLabel方法，1
    
    /**
     * 获取标签矩形区域 - 查询指定波形的标签显示区域
     * 
     * 【功能说明】
     * 返回指定通道波形的标签矩形区域
     * 用于触摸检测和标签位置管理
     * 
     * 【参数说明】
     * @param chNo 通道编号，对应TChan定义的通道常量
     *             如：TChan.Math1、TChan.Math2、TChan.Ref1等
     * 
     * 【返回值】
     * @return Rect - 标签矩形区域对象
     *         包含标签的left、top、right、bottom坐标
     *         如果找不到指定通道，返回空矩形
     * 
     * 【处理流程】
     * 1. 创建空矩形对象
     * 2. 遍历所有波形实例
     * 3. 找到匹配通道编号的波形
     * 4. 获取其标签矩形区域
     * 5. 找到后立即结束遍历
     * 
     * 【调用时机】
     * - 触摸检测判断是否点击标签时
     * - 计算标签位置时
     * - 标签拖动处理时
     * 
     * 【使用示例】
     * Rect rect = manager.getLabelRect(TChan.Math1);  // 获取Math1标签矩形
     * if (rect.contains(x, y)) {
     *     // 触摸点在Math1标签区域内
     * }
     */
    public Rect getLabelRect(int chNo) { // 获取标签矩形区域方法，查询指定波形的标签显示区域，参数为通道编号，返回标签矩形对象，1
        Rect rect = new Rect(); // 创建空矩形对象，作为返回值容器，1
        for (MathRefWave c : mapChannel.values()) { // 遍历mapChannel中的所有MathRefWave实例，1
            if (c.getLineNameID() != chNo) continue; // 如果波形实例的通道ID不匹配目标通道编号，跳过本次循环，继续下一个，1
            rect = c.getLabelRect(); // 获取波形实例的标签矩形区域，赋值给rect变量，1
            break; // 找到匹配波形后立即结束遍历，提高效率，1
        } // 结束遍历循环，1
        return rect; // 返回标签矩形区域，如果未找到匹配通道则返回空矩形，1
    } // 结束getLabelRect方法，1
    
    // ================================ 标签选择状态管理区域 ================================
    
    /**
     * 标签选中通道缓存
     * 用于记录当前通过标签选中的通道编号
     * 在contains方法中设置，在getLabelSelectChan方法中返回
     */
    private int labelSelectChan = -1; // 标签选中通道缓存，记录当前通过标签选中的通道编号，初始值为-1表示无选中，1
    
    /**
     * 检测触摸点是否在标签区域 - 判断触摸点是否落在某个波形标签上
     * 
     * 【功能说明】
     * 检测指定坐标点是否落在某个可见波形的标签区域内
     * 用于处理标签触摸事件和标签选择
     * 
     * 【参数说明】
     * @param x 触摸点X坐标（像素）
     * @param y 触摸点Y坐标（像素）
     * 
     * 【返回值】
     * @return boolean - 是否在标签区域内
     *         true：触摸点在某个波形标签区域内
     *         false：触摸点不在任何波形标签区域内
     * 
     * 【处理流程】
     * 1. 初始化isContains为false
     * 2. 遍历所有波形实例
     * 3. 只检测可见波形的标签区域
     * 4. 调用标签矩形的contains方法检测碰撞
     * 5. 如果碰撞成功，记录选中通道编号并返回true
     * 6. 如果遍历结束未找到，返回false
     * 
     * 【副作用】
     * 此方法会更新labelSelectChan变量
     * 如果检测成功，labelSelectChan设置为对应通道编号
     * 
     * 【调用时机】
     * - 处理标签触摸事件时
     * - 判断是否点击标签时
     * - 标签拖动开始检测时
     * 
     * 【使用示例】
     * if (manager.contains(100, 200)) {
     *     int selectedChan = manager.getLabelSelectChan();
     *     // 触摸点在selectedChan通道的标签上
     * }
     */
    public boolean contains(int x, int y) { // 检测触摸点是否在标签区域方法，判断触摸点是否落在某个波形标签上，参数为触摸点坐标，返回是否在标签区域内，1
        boolean isContains = false; // 初始化isContains为false，表示不在标签区域内，1
        for (MathRefWave c : mapChannel.values()) { // 遍历mapChannel中的所有MathRefWave实例，1
            if (c.getVisible() && c.getLabelRect().contains(x, y)) { // 如果波形可见且标签矩形包含触摸点，1
                isContains = true; // 设置isContains为true，表示触摸点在标签区域内，1
                labelSelectChan = c.getLineNameID(); // 记录选中通道编号到labelSelectChan缓存，1
                break; // 找到匹配波形后立即结束遍历，提高效率，1
            } // 结束可见性和碰撞检测判断，1
        } // 结束遍历循环，1
        return isContains; // 返回检测结果，true表示在标签区域内，false表示不在，1
    } // 结束contains方法，1
    
    /**
     * 获取标签选中通道 - 返回当前通过标签选中的通道编号
     * 
     * 【功能说明】
     * 返回当前通过contains方法检测到的选中通道编号
     * 需要先调用contains方法进行检测
     * 
     * 【返回值】
     * @return int - 标签选中通道编号
     *         -1：表示没有选中任何通道（未调用contains或检测失败）
     *         其他值：选中的通道编号（对应TChan定义）
     * 
     * 【使用说明】
     * 此方法返回的是contains方法检测到的通道编号
     * 需要先调用contains(x, y)进行检测，然后调用此方法获取结果
     * 
     * 【调用时机】
     * - contains方法返回true后，获取选中的通道
     * - 处理标签触摸事件时，确定目标通道
     * - 标签拖动处理时，确定拖动的波形
     * 
     * 【使用示例】
     * if (manager.contains(100, 200)) {
     *     int selectedChan = manager.getLabelSelectChan();
     *     // selectedChan为选中的通道编号
     * }
     */
    public int getLabelSelectChan() { // 获取标签选中通道方法，返回当前通过标签选中的通道编号，无参数，返回选中通道编号，1
        return labelSelectChan; // 返回labelSelectChan缓存值，-1表示无选中，其他值表示选中通道编号，1
    } // 结束getLabelSelectChan方法，1
    
    /**
     * 改变标签位置 - 更新指定波形的标签水平位置
     * 
     * 【功能说明】
     * 更新指定通道波形的标签水平位置（X坐标）
     * 用于标签拖动交互，实现标签位置调整
     * 
     * 【参数说明】
     * @param selectChan 目标通道编号，对应TChan定义的通道常量
     *                   如：TChan.Math1、TChan.Math2、TChan.Ref1等
     * @param x 标签新的X坐标位置（像素）
     *          表示标签的水平位置
     * 
     * 【处理流程】
     * 1. 遍历所有波形实例
     * 2. 找到匹配通道编号的波形
     * 3. 调用其changeLabelPos方法更新标签位置
     * 4. 找到后立即结束遍历
     * 
     * 【调用时机】
     * - 用户拖动标签调整位置时
     * - 标签位置需要更新时
     * - 处理标签拖动事件时
     * 
     * 【使用示例】
     * manager.changeLabelPos(TChan.Math1, 150);  // 设置Math1标签X坐标为150
     * 
     * 【注意事项】
     * changeLabelPos方法会进行边界检查
     * 标签位置会被限制在合理范围内
     * 位置会自动保存到CacheUtil缓存
     */
    public void changeLabelPos(int selectChan, int x) { // 改变标签位置方法，更新指定波形的标签水平位置，参数为目标通道编号和新的X坐标，1
        for (MathRefWave c : mapChannel.values()) { // 遍历mapChannel中的所有MathRefWave实例，1
            if (selectChan == c.getLineNameID()) { // 如果波形实例的通道ID匹配目标通道编号，1
                c.changeLabelPos(x); // 调用波形实例的changeLabelPos方法，更新标签位置，1
                break; // 找到匹配波形后立即结束遍历，提高效率，1
            } // 结束通道匹配判断，1
        } // 结束遍历循环，1
    } // 结束changeLabelPos方法，1

} // 结束MathRefWaveManage类定义，1