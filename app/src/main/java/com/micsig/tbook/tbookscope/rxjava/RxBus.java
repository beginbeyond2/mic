package com.micsig.tbook.tbookscope.rxjava;  // 定义包名：RxJava事件总线模块

import java.util.ArrayList;  // 导入ArrayList类：动态数组
import java.util.HashMap;  // 导入HashMap类：哈希映射
import java.util.List;  // 导入List类：列表接口

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;  // 导入AndroidSchedulers类：Android线程调度器
import io.reactivex.rxjava3.annotations.NonNull;  // 导入NonNull注解：非空注解
import io.reactivex.rxjava3.core.Observable;  // 导入Observable类：被观察者
import io.reactivex.rxjava3.disposables.Disposable;  // 导入Disposable类：可释放资源
import io.reactivex.rxjava3.functions.Action;  // 导入Action接口：无参函数
import io.reactivex.rxjava3.functions.Consumer;  // 导入Consumer接口：消费者函数
import io.reactivex.rxjava3.internal.functions.Functions;  // 导入Functions类：函数工具类
import io.reactivex.rxjava3.internal.observers.LambdaObserver;  // 导入LambdaObserver类：Lambda观察者
import io.reactivex.rxjava3.subjects.PublishSubject;  // 导入PublishSubject类：发布主题
import io.reactivex.rxjava3.subjects.Subject;  // 导入Subject类：主题基类


/**
 * RxJava事件总线 - 基于RxJava3的发布/订阅事件总线实现
 * 
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：tbookscope.rxjava（RxJava事件总线模块）</li>
 *   <li>架构层级：中间件层 - 事件总线</li>
 *   <li>设计模式：单例模式 + 观察者模式</li>
 *   <li>职责类型：消息发布、消息订阅、通道管理</li>
 * </ul>
 * 
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>提供消息通道注册与注销功能</li>
 *   <li>提供消息发布功能（支持同步/异步）</li>
 *   <li>提供消息订阅功能（支持主线程回调）</li>
 *   <li>管理观察者生命周期</li>
 * </ul>
 * 
 * <p><b>事件总线架构：</b>
 * <pre>
 * ┌──────────────────────────────────────────────────────────────────────────┐
 * │   RxBus - 事件总线                                                       │
 * │                                                                          │
 * │   ┌─────────────────────────────────────────────────────────────────┐   │
 * │   │   消息通道管理                                                    │   │
 * │   │                                                                   │   │
 * │   │   maps: HashMap&lt;Object, Subject&gt;                               │   │
 * │   │       │                                                          │   │
 * │   │       ├── RxEnum.MQ_CHANNEL_ACTIVE_CHANGE → PublishSubject      │   │
 * │   │       ├── RxEnum.RIGHTLAYOUT_MATH → PublishSubject              │   │
 * │   │       ├── RxEnum.TOPLAYOUT_TRIGGER → PublishSubject             │   │
 * │   │       └── ...其他通道                                             │   │
 * │   └─────────────────────────────────────────────────────────────────┘   │
 * │                                                                          │
 * │   ┌─────────────────────────────────────────────────────────────────┐   │
 * │   │   消息流转流程                                                    │   │
 * │   │                                                                   │   │
 * │   │   发布者                        RxBus                     订阅者  │   │
 * │   │       │                          │                          │     │   │
 * │   │       │  post(msg)               │                          │     │   │
 * │   │       │ ────────────────────────→│                          │     │   │
 * │   │       │                          │  Subject.onNext(msg)     │     │   │
 * │   │       │                          │ ────────────────────────→│     │   │
 * │   │       │                          │                          │     │   │
 * │   │       │                          │  主线程回调 onNext()      │     │   │
 * │   │       │                          │ ────────────────────────→│     │   │
 * │   └─────────────────────────────────────────────────────────────────┘   │
 * │                                                                          │
 * │   ┌─────────────────────────────────────────────────────────────────┐   │
 * │   │   订阅管理                                                        │   │
 * │   │                                                                   │   │
 * │   │   disposableHashMap: 存储每个通道的所有观察者                      │   │
 * │   │   bakBeanMap: 存储每个通道的回调备份（用于重建）                    │   │
 * │   │   OnErrorMap: 存储每个通道的错误回调                               │   │
 * │   │   OnCompleteMap: 存储每个通道的完成回调                            │   │
 * │   └─────────────────────────────────────────────────────────────────┘   │
 * └──────────────────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <p><b>使用示例：</b>
 * <pre>
 * // 1. 注册消息通道
 * Observable&lt;Object&gt; observable = RxBus.getInstance().register(RxEnum.MQ_CHANNEL_ACTIVE_CHANGE);
 * 
 * // 2. 订阅消息（主线程回调）
 * Disposable disposable = RxBus.getInstance().dealObservable(
 *     RxEnum.MQ_CHANNEL_ACTIVE_CHANGE,
 *     msg -&gt; {
 *         // 处理消息
 *         MQEnum mqEnum = RxBusRegister.parseMqEnum(msg);
 *     }
 * );
 * 
 * // 3. 发布消息
 * MsgChOpenClose msg = RxBusRegister.createMsg(RxEnum.MQ_CHANNEL_ACTIVE_CHANGE, MQEnum.CH_OPEN);
 * RxBus.getInstance().post(msg);
 * 
 * // 4. 注销订阅
 * disposable.dispose();
 * RxBus.getInstance().unregister(RxEnum.MQ_CHANNEL_ACTIVE_CHANGE);
 * </pre>
 * 
 * <p><b>线程模型：</b>
 * <ul>
 *   <li>消息发布：可在任意线程调用</li>
 *   <li>消息回调：自动切换到Android主线程（UI线程）</li>
 *   <li>线程安全：Subject使用toSerialized()保证线程安全</li>
 * </ul>
 * 
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>依赖：RxJava3（响应式编程库）</li>
 *   <li>依赖：RxAndroid（Android线程调度）</li>
 *   <li>依赖：RxEnum（消息通道枚举）</li>
 *   <li>被依赖：RxBusRegister（消息总线注册器）</li>
 *   <li>被依赖：所有需要消息通信的组件</li>
 * </ul>
 * 
 * @author liwb
 * @version 1.0
 * @since 2017/5/11
 * @see RxBusRegister 消息总线注册器
 * @see RxEnum 消息通道枚举
 * @see PublishSubject RxJava发布主题
 */
public class RxBus {
    
    /** 消息通道映射表：存储tag到Subject的映射 */
    private HashMap<Object, Subject> maps = new HashMap<>();  // 消息通道映射表

    /** 观察者映射表：存储每个tag对应的所有观察者 */
    private HashMap<Object,List<LambdaObserver>> disposableHashMap=new HashMap<>();  // 观察者映射表
    
    /** 回调备份映射表：存储每个tag对应的所有回调备份 */
    private HashMap<Object, List<Bean>> bakBeanMap=new HashMap<>();  // 回调备份映射表

    /** 错误回调映射表：存储每个tag对应的错误回调 */
    private HashMap<Object, Consumer<Object>> OnErrorMap=new HashMap<>();  // 错误回调映射表
    
    /** 完成回调映射表：存储每个tag对应的完成回调 */
    private HashMap<Object, Action> OnCompleteMap=new HashMap<>();  // 完成回调映射表
    
    //region 单例模式
    
    /**
     * 单例持有者类：使用静态内部类实现延迟加载的单例模式
     * 
     * <p>这种方式利用了Java类加载机制保证线程安全，
     * 同时实现了延迟加载（只有在调用getInstance()时才会加载此类）。
     */
    public static class RxBusHolder {
        /** 单例实例：静态final保证全局唯一 */
        public static final RxBus rxBus = new RxBus();  // 创建单例实例
    }

    /**
     * 获取单例实例
     * 
     * <p>返回全局唯一的RxBus实例。
     * 
     * @return RxBus单例实例
     */
    public static RxBus getInstance() {
        return RxBusHolder.rxBus;  // 返回单例实例
    }
    //endregion


    /**
     * 构造方法：初始化默认回调
     * 
     * <p>为RxEnum.DEFAULT注册默认的错误回调和完成回调。
     * 其他通道如果没有注册专门的回调，将使用这些默认回调。
     */
    public RxBus() {
        OnErrorMap.put(RxEnum.DEFAULT,this::OnError);  // 注册默认错误回调
        OnCompleteMap.put(RxEnum.DEFAULT,this::OnComplete);  // 注册默认完成回调
    }

    /**
     * 默认完成回调
     * 
     * <p>当Subject完成时调用，当前为空实现。
     */
    private void OnComplete() {
//        Log.d(Tag.Debug, String.format("OnComplete" ));  // 可选的日志输出
    }

    /**
     * 默认错误回调
     * 
     * <p>当消息处理发生错误时调用，当前为空实现。
     * 
     * @param o 错误对象
     */
    private void OnError(Object o) {
//        Log.d(Tag.Debug, String.format("OnError: %s",o.getClass().getSimpleName() ));  // 可选的日志输出
    }

    /**
     * 初始化方法
     * 
     * <p>当前为空实现，可扩展用于初始化操作。
     */
    public void init(){}

    /**
     * 注册消息通道
     * 
     * <p>根据传递的tag类型返回特定类型的被观察者（Observable）。
     * 如果该tag已经注册，会先注销旧的通道再注册新的。
     * 
     * <p>创建的Subject使用toSerialized()保证线程安全。
     * 
     * @param <T> 消息类型
     * @param tag 消息通道标识（通常是RxEnum枚举值）
     * @return 被观察者对象，可用于订阅消息
     */
    public <T> Observable<T> register(@NonNull Object tag) {
        if (maps.containsKey(tag)) {  // 检查是否已注册
            unregister(tag);  // 已注册则先注销
        }
        Subject<T> subject = PublishSubject.<T>create().toSerialized();  // 创建线程安全的Subject
        maps.put(tag, subject);  // 存入映射表
        return subject;  // 返回被观察者
    }
    
    /**
     * 注销消息通道
     * 
     * <p>注销指定tag的消息通道，释放相关资源。
     * 如果Subject已完成或没有观察者，会释放所有观察者资源。
     * 
     * @param tag 消息通道标识
     */
    @SuppressWarnings("unchecked")
    public void unregister(@NonNull Object tag) {
        Subject subjects = maps.get(tag);  // 获取Subject
        if (subjects != null) {  // 检查Subject是否存在
            List<LambdaObserver> dList=disposableHashMap.get(tag);  // 获取观察者列表
            if (dList!=null) {  // 检查观察者列表是否存在
                if (subjects.hasComplete()) {  // Subject已完成
                    for (int i = 0; i < dList.size(); i++) {  // 遍历观察者
                        subjects.onSubscribe(dList.get(i));  // 通知订阅完成
                    }
                    dList.clear();  // 清空观察者列表
                }
                if (subjects.hasObservers() == false) {  // Subject没有观察者
                    for (int i = 0; i < dList.size(); i++) {  // 遍历观察者
                        dList.get(i).dispose();  // 释放观察者资源
                    }
                    dList.clear();  // 清空观察者列表
                }
            }
            maps.remove(tag);  // 从映射表中移除
        }
    }

    /**
     * 获取被观察者
     * 
     * <p>根据tag获取对应的被观察者对象。
     * 
     * @param <T> 消息类型
     * @param tag 消息通道标识
     * @return 被观察者对象，如果不存在则返回null
     */
    public  <T> Observable getObservable(Object tag) {
        Subject subjects = maps.get(tag);  // 获取Subject
        if (subjects != null) {  // 检查Subject是否存在
            return subjects;  // 返回被观察者
        }
        return null;  // 不存在返回null
    }

    /**
     * 处理被观察者（简化版）
     * 
     * <p>订阅指定tag的消息，使用默认的错误回调和完成回调。
     * 回调会在Android主线程执行。
     * 
     * @param <T> 消息类型
     * @param tag 消息通道标识
     * @param OnNext 消息回调（成功时调用）
     * @return Disposable对象，可用于取消订阅
     */
    public <T> Disposable dealObservable(Object tag, Consumer<T> OnNext){
        Consumer<T> onError= (Consumer<T>) OnErrorMap.get(tag);  // 获取错误回调
        if (onError==null){  // 如果没有注册专门的错误回调
            onError= (Consumer<T>) OnErrorMap.get(RxEnum.DEFAULT);  // 使用默认错误回调
        }
        Action onComplete=  OnCompleteMap.get(tag);  // 获取完成回调
        if (onComplete==null){  // 如果没有注册专门的完成回调
            onComplete=  OnCompleteMap.get(RxEnum.DEFAULT);  // 使用默认完成回调
        }
        return dealObservable(tag,OnNext,onError,onComplete);  // 调用完整版本
    }



    /**
     * 处理被观察者（完整版）
     * 
     * <p>订阅指定tag的消息，使用指定的回调。
     * 回调会在Android主线程执行。
     * 
     * <p>内部会创建LambdaObserver并存储，以便后续管理。
     * 
     * @param <T> 消息类型
     * @param tag 消息通道标识
     * @param OnNext 消息回调（成功时调用）
     * @param OnError 错误回调（发生错误时调用）
     * @param OnComplete 完成回调（Subject完成时调用）
     * @return Disposable对象，可用于取消订阅；如果Subject不存在则返回null
     */
    private  <T> Disposable dealObservable(Object tag,Consumer<T> OnNext,Consumer<T> OnError,Action OnComplete){
        Subject subject=maps.get(tag);  // 获取Subject
        if (subject!=null){  // 检查Subject是否存在
            LambdaObserver dis=new LambdaObserver(OnNext,OnError,OnComplete, Functions.emptyConsumer());  // 创建Lambda观察者
            subject.observeOn(AndroidSchedulers.mainThread()).subscribe(dis);  // 在主线程观察并订阅


            addDestroyMap(tag,dis);  // 添加到销毁映射表
            addBakMap(tag,OnNext,OnError,OnComplete);  // 添加到备份映射表
            return dis;  // 返回Disposable
        }
        return null;  // Subject不存在返回null
    }
    
    /**
     * 添加观察者到销毁映射表
     * 
     * <p>将观察者添加到对应tag的列表中，以便后续统一管理和释放。
     * 
     * @param <T> 消息类型
     * @param tag 消息通道标识
     * @param dis LambdaObserver观察者
     */
    private <T> void addDestroyMap(Object tag, LambdaObserver dis){
        if (disposableHashMap.containsKey(tag)==false){  // 检查是否已存在该tag的列表
            disposableHashMap.put(tag,new ArrayList<>());  // 不存在则创建新列表
        }
        List<LambdaObserver> list= disposableHashMap.get(tag);  // 获取列表
        list.add(dis);  // 添加观察者
        disposableHashMap.put(tag,list);  // 更新映射表
    }

    /**
     * 添加回调到备份映射表
     * 
     * <p>将回调封装成Bean并添加到对应tag的列表中，用于后续重建订阅。
     * 
     * @param <T> 消息类型
     * @param tag 消息通道标识
     * @param OnNext 消息回调
     * @param OnError 错误回调
     * @param OnComplete 完成回调
     */
    private <T> void addBakMap(Object tag, Consumer<T> OnNext, Consumer<T> OnError, Action OnComplete){
        if (bakBeanMap.containsKey(tag)==false){  // 检查是否已存在该tag的列表
            bakBeanMap.put(tag,new ArrayList<>());  // 不存在则创建新列表
        }
        List<Bean> list=bakBeanMap.get(tag);  // 获取列表
        list.add(new Bean(OnNext,OnError,OnComplete));  // 添加Bean
        bakBeanMap.put(tag,list);  // 更新映射表
    }


    /**
     * 发布消息（使用消息对象作为tag）
     * 
     * <p>使用消息对象本身作为tag发布消息。
     * 适用于消息类型与通道类型相同的场景。
     * 
     * @param o 消息对象，同时作为tag和消息内容
     */
    @SuppressWarnings("unchecked")
    public void post(@NonNull Object o) {
        post(o, o);  // 调用双参数版本
    }
    
    /**
     * 发布消息（指定tag）
     * 
     * <p>向指定tag的消息通道发布消息。
     * 所有订阅了该tag的观察者都会收到消息。
     * 
     * @param tag 消息通道标识
     * @param o 消息对象
     */
    @SuppressWarnings("unchecked")
    public void post(@NonNull Object tag, @NonNull Object o) {
        Subject subjects = maps.get(tag);  // 获取Subject
        if (subjects == null) {  // 检查Subject是否存在
            System.out.println("post:null");  // Subject不存在，输出调试信息
        }
        if (subjects != null) {  // Subject存在
            subjects.onNext(o);  // 发送消息给所有观察者
        }
    }

    /**
     * 回调封装类
     * 
     * <p>封装消息回调、错误回调和完成回调。
     * 用于存储回调备份，以便后续重建订阅。
     * 
     * @param <T> 消息类型
     */
    class Bean<T>{

        /** 消息回调：成功时调用 */
        Consumer<? super T> onNext;  // 消息回调
        
        /** 错误回调：发生错误时调用 */
        Consumer<? super T> onError;  // 错误回调
        
        /** 完成回调：Subject完成时调用 */
        Action onComplete;  // 完成回调
        
        /**
         * 构造方法：初始化回调封装
         * 
         * @param onNext 消息回调
         * @param onError 错误回调
         * @param onComplete 完成回调
         */
        public Bean(Consumer<? super T> onNext,Consumer<? super T> onError,Action onComplete){
            this.onNext=onNext;  // 设置消息回调
            this.onError=onError;  // 设置错误回调
            this.onComplete=onComplete;  // 设置完成回调
        }
    }

}
