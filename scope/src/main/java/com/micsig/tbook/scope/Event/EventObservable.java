package com.micsig.tbook.scope.Event;                                                     // 包声明：事件模块

import java.util.Observable;                                                              // 导入：被观察者类

/**
 * ╔══════════════════════════════════════════════════════════════════════════════╗
 * ║              EventObservable - 事件被观察者类                                  ║
 * ╠══════════════════════════════════════════════════════════════════════════════╣
 * ║ 【模块定位】                                                                 ║
 * ║   Event模块的事件被观察者类，位于Event包下，                                   ║
 * ║   继承自Java标准库的Observable类。                                            ║
 * ║                                                                              ║
 * ║ 【核心职责】                                                                 ║
 * ║   1. 继承Observable类，提供事件通知功能                                       ║
 * ║   2. 重写notifyObservers方法，自动调用setChanged()                           ║
 * ║   3. 简化事件通知流程，避免忘记调用setChanged()                               ║
 * ║                                                                              ║
 * ║ 【为什么需要这个类】                                                         ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                        Observable使用说明                             │ ║
 * ║   │                                                                      │ ║
 * ║   │   【标准Observable使用流程】                                           │ ║
 * ║   │   1. 调用setChanged()标记状态已改变                                   │ ║
 * ║   │   2. 调用notifyObservers()通知观察者                                  │ ║
 * ║   │                                                                      │ ║
 * ║   │   【问题】                                                             │ ║
 * ║   │   如果忘记调用setChanged()，notifyObservers()不会通知观察者           │ ║
 * ║   │   这是Observable类的设计，必须先调用setChanged()                      │ ║
 * ║   │                                                                      │ ║
 * ║   │   【解决方案】                                                         │ ║
 * ║   │   EventObservable重写notifyObservers()方法                           │ ║
 * ║   │   在方法内部自动调用setChanged()                                      │ ║
 * ║   │   这样调用者就不需要手动调用setChanged()                               │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【与EventFactory的关系】                                                     ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │                        EventFactory使用EventObservable               │ ║
 * ║   │                                                                      │ ║
 * ║   │   EventFactory维护一个EventObservable数组：                          │ ║
 * ║   │   private Observable[] observables = new EventObservable[EVENT_CNT]; │ ║
 * ║   │                                                                      │ ║
 * ║   │   每个事件类型对应一个EventObservable实例                             │ ║
 * ║   │   当发送事件时，调用EventObservable.notifyObservers()                │ ║
 * ║   │   自动设置changed标志并通知所有观察者                                 │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【使用示例】                                                                 ║
 * ║   ┌──────────────────────────────────────────────────────────────────────┐ ║
 * ║   │   // 创建EventObservable实例                                          │ ║
 * ║   │   EventObservable observable = new EventObservable();                │ ║
 * ║   │                                                                      │ ║
 * ║   │   // 注册观察者                                                       │ ║
 * ║   │   observable.addObserver(observer);                                  │ ║
 * ║   │                                                                      │ ║
 * ║   │   // 通知观察者（不需要手动调用setChanged()）                          │ ║
 * ║   │   observable.notifyObservers(eventData);                             │ ║
 * ║   │                                                                      │ ║
 * ║   │   // 等价于标准Observable的用法：                                     │ ║
 * ║   │   // observable.setChanged();                                        │ ║
 * ║   │   // observable.notifyObservers(eventData);                          │ ║
 * ║   └──────────────────────────────────────────────────────────────────────┘ ║
 * ║                                                                              ║
 * ║ 【设计模式】                                                                 ║
 * ║   - 观察者模式：作为被观察者，管理观察者列表并通知观察者                     ║
 * ║   - 模板方法模式：重写父类方法，添加额外逻辑                                ║
 * ║                                                                              ║
 * ║ 【线程安全】                                                                 ║
 * ║   - 继承自Observable，线程安全由父类保证                                    ║
 * ║   - notifyObservers()方法是同步的                                          ║
 * ║                                                                              ║
 * ║ 【依赖关系】                                                                 ║
 * ║   - Observable: Java标准库的被观察者类                                     ║
 * ║   - EventFactory: 事件工厂，创建和管理EventObservable实例                 ║
 * ║   - Observer: 观察者接口                                                  ║
 * ║                                                                              ║
 * ║ 【作者】 MHO项目组                                                           ║
 * ╚══════════════════════════════════════════════════════════════════════════════╝
 */

/**
 * 事件被观察者类
 * 继承自Observable，重写notifyObservers方法
 * 自动调用setChanged()，简化事件通知流程
 *
 * <p><b>主要功能：</b></p>
 * <ul>
 *   <li>继承Observable类</li>
 *   <li>重写notifyObservers()方法</li>
 *   <li>自动设置changed标志</li>
 * </ul>
 *
 * <p><b>使用示例：</b></p>
 * <pre>
 * // 在EventFactory中使用
 * EventObservable observable = new EventObservable();
 * observable.addObserver(observer);
 * observable.notifyObservers(eventBase); // 自动调用setChanged()
 * </pre>
 *
 * @see Observable
 * @see EventFactory
 */
public class EventObservable extends Observable {                                          // 类声明：事件被观察者类，继承自Observable

    /**
     * 通知观察者（无参数）
     * 重写父类方法，在通知前自动调用setChanged()
     *
     * <p><b>为什么需要重写：</b></p>
     * <ul>
     *   <li>Observable.notifyObservers()只有在changed标志为true时才会通知观察者</li>
     *   <li>changed标志需要通过setChanged()方法设置</li>
     *   <li>重写后自动调用setChanged()，简化调用流程</li>
     * </ul>
     */
    @Override
    public void notifyObservers() {                                                        // 重写方法：通知观察者（无参数）
        setChanged();                                                                      // 设置changed标志为true，标记状态已改变
        super.notifyObservers();                                                           // 调用父类的notifyObservers()方法通知所有观察者
    }                                                                                       // 方法结束

    /**
     * 通知观察者（带参数）
     * 重写父类方法，在通知前自动调用setChanged()
     *
     * <p><b>参数说明：</b></p>
     * <ul>
     *   <li>data: 传递给观察者的数据对象</li>
     *   <li>在示波器项目中，通常是EventBase对象</li>
     * </ul>
     *
     * <p><b>为什么需要重写：</b></p>
     * <ul>
     *   <li>Observable.notifyObservers(Object)只有在changed标志为true时才会通知观察者</li>
     *   <li>changed标志需要通过setChanged()方法设置</li>
     *   <li>重写后自动调用setChanged()，简化调用流程</li>
     * </ul>
     *
     * @param data 传递给观察者的数据对象
     */
    @Override
    public void notifyObservers(Object data) {                                             // 重写方法：通知观察者（带参数）
        setChanged();                                                                      // 设置changed标志为true，标记状态已改变
        super.notifyObservers(data);                                                       // 调用父类的notifyObservers(Object)方法通知所有观察者，并传递数据对象
    }                                                                                       // 方法结束
}                                                                                           // 类结束
