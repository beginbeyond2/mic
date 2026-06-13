package com.micsig.tbook.scope.Sample;

import android.annotation.SuppressLint;
import android.os.Build;

import androidx.annotation.IntDef;

import com.micsig.tbook.hardware.HardwareProduct;
import com.micsig.tbook.scope.Event.EventFactory;
import com.micsig.tbook.scope.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 存储深度工厂类
 * 
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.Sample（示波器采样管理模块）</li>
 *   <li>架构层级：业务逻辑层 - 工厂模式</li>
 *   <li>设计模式：工厂模式 + 单例模式</li>
 *   <li>职责类型：对象创建与管理</li>
 * </ul>
 * 
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>根据硬件型号创建对应的存储深度实现对象</li>
 *   <li>管理存储深度配置的全局状态</li>
 *   <li>提供存储深度相关的静态工具方法</li>
 *   <li>支持存储深度的动态切换</li>
 * </ul>
 * 
 * <p><b>设计目的：</b>
 * <ul>
 *   <li>封装存储深度对象的创建逻辑</li>
 *   <li>根据不同硬件型号自动选择合适的实现</li>
 *   <li>提供统一的存储深度访问入口</li>
 *   <li>支持校准模式下的存储深度强制切换</li>
 * </ul>
 * 
 * <p><b>支持的存储深度类型：</b>
 * <pre>
 * ┌────────────────────────────────────────────────────────────────┐
 * │ 存储深度类型      │ 值            │ 适用产品                    │
 * ├───────────────────┼───────────────┼────────────────────────────┤
 * │ MEM_DEPTH_1800M   │ 1,800,000,000 │ MHO68 V1/V2 高端示波器     │
 * │ MEM_DEPTH_360M    │   360,000,000 │ MHO38/MHO28 中端示波器     │
 * │ MEM_DEPTH_36M     │    36,000,000 │ MHO38/MHO28 标准示波器     │
 * │ MEM_DEPTH_18M     │    18,000,000 │ MHO68 V1/V2 扩展模式       │
 * └───────────────────┴───────────────┴────────────────────────────┘
 * </pre>
 * 
 * <p><b>产品型号与存储深度映射：</b>
 * <pre>
 * ┌─────────────────────────────────────────────────────────────────┐
 * │ 产品型号              │ 默认存储深度  │ 扩展存储深度 │ 实现类          │
 * ├───────────────────────┼───────────────┼──────────────┼─────────────────┤
 * │ RK3588_MHO68_V1       │ 1800M         │ 18M          │ MemDepth1800M   │
 * │ RK3588_MHO68_V2       │ 1800M         │ 18M          │ MemDepth1800MV2 │
 * │ RK3588_MHO38_V1       │ 360M          │ 36M          │ MemDepth360M    │
 * │ RK3588_MHO28_V1       │ 360M          │ 36M          │ MemDepth360M    │
 * └───────────────────────┴───────────────┴──────────────┴─────────────────┘
 * </pre>
 * 
 * <p><b>类结构图：</b>
 * <pre>
 * MemDepthFactory (工厂类)
 *   │
 *   ├── 创建 ──→ IMemDepth (接口)
 *   │              │
 *   │              ├── MemDepth1800M (1800M V1实现)
 *   │              ├── MemDepth1800MV2 (1800M V2实现)
 *   │              ├── MemDepth360M (360M实现)
 *   │              ├── MemDepth36M (36M实现)
 *   │              ├── MemDepth18M (18M V1实现)
 *   │              └── MemDepth18MV2 (18M V2实现)
 *   │
 *   ├── 依赖 ──→ HardwareProduct (硬件型号判断)
 *   │
 *   └── 依赖 ──→ EventFactory (事件通知)
 * </pre>
 * 
 * <p><b>使用示例：</b>
 * <pre>
 * // 获取当前配置的存储深度对象
 * IMemDepth memDepth = MemDepthFactory.getMemDepth();
 * 
 * // 获取实际采样存储深度
 * int depth = MemDepthFactory.getSampleMemDepth();
 * 
 * // 强制切换存储深度（校准模式）
 * MemDepthFactory.forceMemDepth(MemDepthFactory.MEM_DEPTH_1800M);
 * </pre>
 * 
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>依赖：HardwareProduct（硬件型号判断）</li>
 *   <li>依赖：EventFactory（事件通知）</li>
 *   <li>创建：MemDepth1800M/MemDepth1800MV2/MemDepth360M/MemDepth36M/MemDepth18M/MemDepth18MV2</li>
 * </ul>
 * 
 * <p><b>线程安全：</b>
 * <ul>
 *   <li>getMemDepth()：使用synchronized保证单例创建的线程安全</li>
 *   <li>forceMemDepth()：使用synchronized保证状态切换的线程安全</li>
 * </ul>
 * 
 * @author zhuzh
 * @version 1.0
 * @since 2018/3/13
 * @see IMemDepth 存储深度接口
 * @see MemDepth1800M 1800M点存储深度实现(V1)
 * @see MemDepth1800MV2 1800M点存储深度实现(V2)
 * @see MemDepth360M 360M点存储深度实现
 * @see MemDepth36M 36M点存储深度实现
 * @see HardwareProduct 硬件产品型号
 */
public class MemDepthFactory {

    /**
     * 私有构造函数
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>防止外部实例化</li>
     *   <li>工厂类采用静态方法设计，不需要创建实例</li>
     * </ul>
     */
    private MemDepthFactory() {
    }

    /**
     * 存储深度类型注解定义
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>使用@IntDef定义存储深度类型的合法值</li>
     *   <li>编译时检查参数类型，防止传入非法值</li>
     *   <li>保留策略为SOURCE，仅在编译时生效</li>
     * </ul>
     * 
     * <p><b>使用示例：</b>
     * <pre>
     * public void setDepth(@MEM_DEPTH int depth) {
     *     // depth只能是MEM_DEPTH_1800M/MEM_DEPTH_360M/MEM_DEPTH_36M/MEM_DEPTH_18M
     * }
     * </pre>
     */
    @IntDef({ MEM_DEPTH_1800M, MEM_DEPTH_18M,MEM_DEPTH_360M,MEM_DEPTH_36M})
    @Retention(RetentionPolicy.SOURCE)
    public @interface MEM_DEPTH {}


    /**
     * 1800M点存储深度常量
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>高端示波器最大存储深度</li>
     *   <li>适用于MHO68 V1/V2系列</li>
     * </ul>
     */
    public static final int MEM_DEPTH_1800M = 1800_000_000;
    
    /**
     * 18M点存储深度常量
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>MHO68系列的扩展存储深度模式</li>
     *   <li>用于特定场景下的存储深度配置</li>
     * </ul>
     */
    public static final int MEM_DEPTH_18M = 18_000_000;
    
    /**
     * 360M点存储深度常量
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>中端示波器最大存储深度</li>
     *   <li>适用于MHO38/MHO28系列</li>
     * </ul>
     */
    public static final int MEM_DEPTH_360M = 360_000_000;
    
    /**
     * 36M点存储深度常量
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>MHO38/MHO28系列的扩展存储深度模式</li>
     *   <li>标准示波器的存储深度</li>
     * </ul>
     */
    public static final int MEM_DEPTH_36M = 36_000_000;
    
    /**
     * 最大存储深度常量
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>当前系统支持的最大存储深度</li>
     *   <li>等于MEM_DEPTH_1800M</li>
     * </ul>
     */
    public static final int MEM_DEPTH_MAX = MEM_DEPTH_1800M;
    
    /**
     * 当前设置的存储深度值
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>保存当前系统配置的存储深度</li>
     *   <li>初始化为默认存储深度</li>
     * </ul>
     * 
     * <p><b>修改方式：</b>
     * <ul>
     *   <li>setMemDepth()：普通设置</li>
     *   <li>forceMemDepth()：强制设置并触发更新</li>
     * </ul>
     */
    private static int memDepthSet = MemDepthFactory.getDefaultMemDepth();
    
    /**
     * 设置存储深度值
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>更新当前存储深度配置值</li>
     *   <li>不触发存储深度对象重建</li>
     *   <li>下次调用getMemDepth()时生效</li>
     * </ul>
     * 
     * <p><b>注意：</b>此方法仅修改配置值，不触发事件通知
     * 
     * @param memDepth 存储深度值
     */
    public static void setMemDepth(int memDepth){
        memDepthSet = memDepth;
    }
    
    /**
     * 存储深度对象单例
     * 
     * <p><b>业务含义：</b>
     * <ul>
     *   <li>缓存当前创建的存储深度对象</li>
     *   <li>避免重复创建对象</li>
     * </ul>
     * 
     * <p><b>生命周期：</b>
     * <ul>
     *   <li>首次调用getMemDepth()时创建</li>
     *   <li>调用forceMemDepth()时可能重建</li>
     * </ul>
     */
    private static IMemDepth memDepthObj = null;
    
    /**
     * 获取存储深度对象（单例模式）
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>获取当前配置的存储深度实现对象</li>
     *   <li>使用双重检查锁定保证线程安全</li>
     *   <li>延迟初始化，首次调用时创建对象</li>
     * </ul>
     * 
     * <p><b>调用场景：</b>
     * <ul>
     *   <li>需要获取存储深度配置时</li>
     *   <li>需要计算实际采样存储深度时</li>
     *   <li>需要获取存储深度档位信息时</li>
     * </ul>
     * 
     * @return 存储深度实现对象
     */
    public static IMemDepth getMemDepth(){
        synchronized (MemDepthFactory.class) {
            if (memDepthObj == null) {
                memDepthObj = getMemDepth(memDepthSet);
            }
        }
        return memDepthObj;
    }
    
    /**
     * 强制设置存储深度（校准模式专用）
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>强制切换存储深度配置</li>
     *   <li>重建存储深度对象</li>
     *   <li>触发存储深度变化事件</li>
     *   <li>发送强制存储深度变化通知</li>
     * </ul>
     * 
     * <p><b>处理流程：</b>
     * <pre>
     * 1. 检查新值是否与当前值不同
     *    └─→ 相同则跳过处理
     * 2. 更新存储深度配置值
     * 3. 重新创建存储深度对象
     * 4. 调用forceMemDepthChange()触发更新
     * 5. 发送EVENT_FORCE_MEM_DEPTH事件通知
     * </pre>
     * 
     * <p><b>业务场景：</b>
     * <ul>
     *   <li>校准模式下需要特定存储深度</li>
     *   <li>硬件配置变化时强制更新</li>
     *   <li>测试模式下切换存储深度</li>
     * </ul>
     * 
     * <p><b>线程安全：</b>使用synchronized保证状态切换的原子性
     * 
     * @param memDepth 目标存储深度值
     */
    public static void forceMemDepth(@MEM_DEPTH int memDepth){
        boolean bRet = false;
        synchronized (MemDepthFactory.class){
            if(memDepth != memDepthSet){
                memDepthSet = memDepth;
                memDepthObj = getMemDepth(memDepthSet);
                memDepthObj.forceMemDepthChange();
                bRet = true;
            }
        }
        if(bRet){
            EventFactory.sendEvent(EventFactory.EVENT_FORCE_MEM_DEPTH,true);
        }
    }
    
    /**
     * 获取当前设置的存储深度值
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>返回当前配置的存储深度值</li>
     *   <li>不创建存储深度对象</li>
     * </ul>
     * 
     * @return 当前存储深度值
     */
    public static int getMemDepthSet(){
        return memDepthSet;
    }

    /**
     * 获取指定存储深度的实现对象（工具方法）
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>为水平参考等功能提供存储深度对象</li>
     *   <li>不影响全局存储深度配置</li>
     *   <li>每次调用都创建新对象</li>
     * </ul>
     * 
     * <p><b>使用场景：</b>
     * <ul>
     *   <li>水平参考波形处理</li>
     *   <li>需要临时计算不同存储深度下的参数</li>
     * </ul>
     * 
     * @param memDepth 存储深度值
     * @return 存储深度实现对象
     */
    public static IMemDepth getMemDepthObj(@MEM_DEPTH int memDepth) {
        return getMemDepth(memDepth);
    }

    /**
     * 根据存储深度值创建对应的实现对象
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>工厂方法：根据存储深度值创建具体实现</li>
     *   <li>结合硬件型号选择合适的实现类</li>
     * </ul>
     * 
     * <p><b>创建逻辑：</b>
     * <pre>
     * ┌─────────────────────────────────────────────────────────────────┐
     * │ 存储深度值      │ 硬件型号        │ 创建的实现类                │
     * ├─────────────────┼─────────────────┼─────────────────────────────┤
     * │ MEM_DEPTH_1800M │ MHO68_V1        │ MemDepth1800M               │
     * │ MEM_DEPTH_1800M │ MHO68_V2        │ MemDepth1800MV2             │
     * │ MEM_DEPTH_360M  │ 任意            │ MemDepth360M                │
     * │ MEM_DEPTH_36M   │ 任意            │ MemDepth36M                 │
     * │ MEM_DEPTH_18M   │ MHO68_V1        │ MemDepth18M                 │
     * │ MEM_DEPTH_18M   │ MHO68_V2        │ MemDepth18MV2               │
     * └─────────────────┴─────────────────┴─────────────────────────────┘
     * </pre>
     * 
     * @param memDepth 存储深度值
     * @return 存储深度实现对象，未匹配时返回MemDepth1800M
     */
    @SuppressLint("SwitchIntDef")
    private static IMemDepth getMemDepth(@MEM_DEPTH int memDepth){
        IMemDepth obj = null;
        switch(memDepth){
            default:
            case MEM_DEPTH_1800M:
                if(HardwareProduct.isMHO68V1()){
                    obj = new MemDepth1800M(MEM_DEPTH_1800M);
                }else if(HardwareProduct.isMHO68V2()){
                    obj = new MemDepth1800MV2(MEM_DEPTH_1800M);
                }
                break;
            case MEM_DEPTH_360M:
                obj = new MemDepth360M(MEM_DEPTH_360M);
                break;
            case MEM_DEPTH_36M:
                obj = new MemDepth36M(MEM_DEPTH_36M);
                break;
            case MEM_DEPTH_18M:
                if(HardwareProduct.isMHO68V1()){
                    obj = new MemDepth18M(MEM_DEPTH_18M);
                }else if(HardwareProduct.isMHO68V2()){
                    obj = new MemDepth18MV2(MEM_DEPTH_18M);
                }
                break;
        }
        return obj;
    }
    
    /**
     * 获取实际采样存储深度（自动获取通道数）
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>便捷方法：直接获取当前配置的实际采样存储深度</li>
     *   <li>内部调用getMemDepth().getSampleMemDepth()</li>
     * </ul>
     * 
     * @return 实际采样存储深度（单位：点）
     */
    public static int getSampleMemDepth(){
        return getMemDepth().getSampleMemDepth();
    }
    
    /**
     * 获取实际采样存储深度（指定通道数）
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>便捷方法：获取指定通道数下的实际采样存储深度</li>
     *   <li>内部调用getMemDepth().getSampleMemDepth(chCnt)</li>
     * </ul>
     * 
     * @param chCnt 采样通道数
     * @return 实际采样存储深度（单位：点）
     */
    public static int getSampleMemDepth(int chCnt){
        return getMemDepth().getSampleMemDepth(chCnt);
    }
    
    /**
     * 获取默认存储深度
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>根据产品型号返回默认存储深度</li>
     *   <li>用于系统初始化时设置存储深度</li>
     * </ul>
     * 
     * <p><b>返回值映射：</b>
     * <pre>
     * ┌─────────────────────────────────────────────────────────────────┐
     * │ 产品型号              │ 返回值        │ 说明                   │
     * ├───────────────────────┼───────────────┼─────────────────────────┤
     * │ RK3588_MHO68_V1       │ MEM_DEPTH_1800M │ 高端示波器最大深度   │
     * │ RK3588_MHO68_V2       │ MEM_DEPTH_1800M │ 高端示波器最大深度   │
     * │ RK3588_MHO38_V1       │ MEM_DEPTH_360M  │ 中端示波器最大深度   │
     * │ RK3588_MHO28_V1       │ MEM_DEPTH_360M  │ 中端示波器最大深度   │
     * │ 其他                  │ MEM_DEPTH_1800M │ 默认最大深度         │
     * └───────────────────────┴───────────────┴─────────────────────────┘
     * </pre>
     * 
     * @return 默认存储深度值
     */
    public static int getDefaultMemDepth(){
        switch (Build.PRODUCT){
            default:
            case HardwareProduct.RK3588_MHO68_V1:
            case HardwareProduct.RK3588_MHO68_V2:
                return MEM_DEPTH_1800M;
            case HardwareProduct.RK3588_MHO38_V1:
            case HardwareProduct.RK3588_MHO28_V1:
                return MEM_DEPTH_360M;
        }
    }
    
    /**
     * 获取扩展默认存储深度
     * 
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>根据产品型号返回扩展存储深度</li>
     *   <li>用于特定场景下的存储深度配置</li>
     * </ul>
     * 
     * <p><b>返回值映射：</b>
     * <pre>
     * ┌─────────────────────────────────────────────────────────────────┐
     * │ 产品型号              │ 返回值       │ 说明                    │
     * ├───────────────────────┼──────────────┼──────────────────────────┤
     * │ RK3588_MHO68_V1       │ MEM_DEPTH_18M │ 高端示波器扩展深度     │
     * │ RK3588_MHO68_V2       │ MEM_DEPTH_18M │ 高端示波器扩展深度     │
     * │ RK3588_MHO38_V1       │ MEM_DEPTH_36M │ 中端示波器扩展深度     │
     * │ RK3588_MHO28_V1       │ MEM_DEPTH_36M │ 中端示波器扩展深度     │
     * │ 其他                  │ MEM_DEPTH_18M │ 默认扩展深度           │
     * └───────────────────────┴──────────────┴──────────────────────────┘
     * </pre>
     * 
     * @return 扩展默认存储深度值
     */
    public static int getDefaultMemDepthEx(){
        switch (Build.PRODUCT){
            default:
            case HardwareProduct.RK3588_MHO68_V1:
            case HardwareProduct.RK3588_MHO68_V2:
                return MEM_DEPTH_18M;
            case HardwareProduct.RK3588_MHO38_V1:
            case HardwareProduct.RK3588_MHO28_V1:
                return MEM_DEPTH_36M;
        }
    }
}
