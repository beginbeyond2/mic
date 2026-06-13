package com.micsig.tbook.scope.Calibrate;  // 定义包名：示波器校准功能模块

import com.micsig.tbook.scope.Event.EventBase;  // 导入EventBase类：事件基类，封装事件类型和数据
import com.micsig.tbook.scope.Event.EventFactory;  // 导入EventFactory类：事件工厂，用于事件订阅和发送

import java.util.Observable;  // 导入Observable类：观察者模式中的被观察者
import java.util.Observer;  // 导入Observer接口：观察者模式中的观察者接口

/**
 * 探头校准管理器 - 探头DAC校准流程协调中心
 * 
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.Calibrate（示波器校准功能模块）</li>
 *   <li>架构层级：业务逻辑层 - 校准管理层</li>
 *   <li>设计模式：单例模式 + 观察者模式</li>
 *   <li>职责类型：探头校准流程管理、事件处理</li>
 * </ul>
 * 
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>管理探头DAC校准的整体流程（开始、执行、结束）</li>
 *   <li>协调CalibrateService执行探头校准</li>
 *   <li>处理校准完成事件</li>
 *   <li>发送探头校准结束事件通知UI层</li>
 * </ul>
 * 
 * <p><b>探头校准流程：</b>
 * <pre>
 * ┌──────────────────────────────────────────────────────────────────────────┐
 * │   ProbeCalibrate ─ 探头校准流程                                           │
 * │                                                                          │
 * │   begin(param)                                                           │
 * │        │                                                                 │
 * │        ├── 订阅校准完成事件                                               │
 * │        ├── 设置校准参数                                                   │
 * │        ├── 发送探头校准开始消息                                           │
 * │        └── 启动探头DAC校准                                                │
 * │             │                                                            │
 * │             ▼                                                            │
 * │        CalibrateService执行校准                                          │
 * │             │                                                            │
 * │             ▼                                                            │
 * │        update() 接收校准完成事件                                          │
 * │             │                                                            │
 * │             └── end() 发送校准结束事件                                    │
 * └──────────────────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <p><b>探头DAC校准说明：</b>
 * <ul>
 *   <li>探头DAC校准用于校准探头的衰减比和补偿参数</li>
 *   <li>支持不同衰减比的探头（1X、10X、100X等）</li>
 *   <li>校准结果存储在探头或示波器的非易失性存储器中</li>
 * </ul>
 * 
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>依赖：CalibrateService（校准服务，执行具体校准操作）</li>
 *   <li>依赖：EventFactory（事件工厂，事件订阅和发送）</li>
 *   <li>被依赖：UI层（监听EVENT_PROBE_CALIBRATE_END事件）</li>
 * </ul>
 * 
 * @author zhuzh
 * @version 1.0
 * @see CalibrateService 校准服务
 * @see SelfCalibrate 自校准管理器
 */
public class ProbeCalibrate implements Observer {

    /** 单例实例：探头校准管理器唯一实例 */
    static ProbeCalibrate _instance = null;  // 单例实例引用

    /**
     * 获取ProbeCalibrate单例实例
     * 
     * <p>使用懒加载模式实现单例。
     * 注意：此实现非线程安全，如需线程安全应使用双重检查锁定。
     * 
     * @return ProbeCalibrate单例实例
     */
    public static ProbeCalibrate getInstance(){
        if(_instance == null){  // 检查实例是否存在
            _instance = new ProbeCalibrate();  // 创建实例
        }
        return _instance;  // 返回单例实例
    }

    /**
     * 检查是否正在校准
     * 
     * <p>委托给CalibrateService查询当前校准状态。
     * 
     * @return true表示正在校准，false表示未在校准
     */
    public boolean isCalibrate(){
        CalibrateService calibrateService =  CalibrateService.getInstance();  // 获取校准服务单例
        return calibrateService.isCalibrate();  // 返回校准状态
    }
    
    /**
     * 开始探头校准流程
     * 
     * <p>执行以下操作：
     * <ol>
     *   <li>订阅校准项目完成事件</li>
     *   <li>设置探头校准参数</li>
     *   <li>发送探头校准开始消息</li>
     *   <li>启动探头DAC校准</li>
     * </ol>
     * 
     * @param param 校准参数对象，具体内容取决于探头类型
     */
    public void begin(Object param){

        EventFactory.addEventObserver(EventFactory.EVENT_CALIBRATE_ITEM_FINISH,this);  // 订阅校准项目完成事件
        CalibrateService.getInstance().getCalibrate(CalibrateService.PROBE_DA_CALIBRATE).setParam(param);  // 设置探头校准参数
        CalibrateService.getInstance().sendMsg(CalibrateService.CALIBRATE_BEGIN_PROBE);  // 发送探头校准开始消息
        CalibrateService.getInstance().start(CalibrateService.PROBE_DA_CALIBRATE);  // 启动探头DAC校准
    }
    
    /**
     * 结束探头校准流程
     * 
     * <p>执行以下操作：
     * <ol>
     *   <li>取消订阅校准项目完成事件</li>
     *   <li>结束校准服务</li>
     *   <li>发送探头校准结束事件</li>
     * </ol>
     */
    private void end(){
        EventFactory.delEventObserver(EventFactory.EVENT_CALIBRATE_ITEM_FINISH,this);  // 取消订阅校准项目完成事件
        CalibrateService.getInstance().end();  // 结束校准服务
        EventFactory.sendEvent(new EventBase(EventFactory.EVENT_PROBE_CALIBRATE_END));  // 发送探头校准结束事件
    }
    
    /**
     * 事件更新回调方法
     * 
     * <p>当订阅的校准项目完成事件发生时调用。
     * 收到事件后结束校准流程。
     * 
     * @param o 被观察者对象（EventFactory）
     * @param data 事件数据（EventBase对象）
     */
    @Override
    public void update(Observable o, Object data) {
        EventBase eventBase = (EventBase)data;  // 将数据转换为EventBase对象
        if(eventBase.getId() == EventFactory.EVENT_CALIBRATE_ITEM_FINISH){  // 检查是否为校准项目完成事件

            end();  // 结束校准流程
        }
    }
    
    /**
     * 获取校准错误码
     * 
     * <p>从探头DAC校准对象获取错误码，用于判断校准是否成功。
     * 
     * @return 错误码，0表示成功，非0表示失败
     */
    public int getErrorCode(){
        return CalibrateService.getInstance().getCalibrate(CalibrateService.PROBE_DA_CALIBRATE).getErrcode();  // 返回探头校准错误码
    }
}
