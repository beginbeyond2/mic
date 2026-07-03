package com.micsig.tbook.tbookscope.middleware; // 中间件层包声明

import com.micsig.tbook.tbookscope.tools.Tools; // 导入工具类，提供indexOf等通用方法

import java.util.List; // 导入List集合接口

/**
 * @auother Liwb
 * @description:
 * @data:2024-2-29 9:13
 */
/*
 * +=============================================================================+
 * |                    OperateOrder - 有序列表操作器                             |
 * +=============================================================================+
 * | 模块定位: 中间件层(Middleware)的有序列表排序操作工具类                        |
 * | 核心职责: 对泛型列表进行顺序调整，支持将元素移至队首或队尾                      |
 * | 架构设计: 泛型类<T>，使用synchronized保证线程安全的列表操作                   |
 * | 数据流向: 外部调用者 → OperateOrder → 内部List<T>集合                        |
 * | 依赖关系: Tools(工具类，提供indexOf查找功能)                                  |
 * | 使用场景: MQ通道优先级调整、任务队列重排序等需要动态调整元素顺序的场景          |
 * +=============================================================================+
 */
public class OperateOrder<T> {
    /**
     * 默认无参构造函数，创建不持有列表的OperateOrder实例。
     */
    public OperateOrder(){}

    private List<T> list; // 内部持有的有序列表，存储待操作的泛型元素

    /**
     * 带列表参数的构造函数，创建持有指定列表的OperateOrder实例。
     *
     * @param list 待操作的有序列表
     */
    public OperateOrder(List<T> list){
        this.list=list; // 将传入的列表赋值给内部持有的列表引用
    }

    /**
     * 将指定元素移至列表首位。如果元素已存在于列表中则移至首位，
     * 如果不存在则插入到首位。操作线程安全。
     *
     * @param item 需要移至首位的元素
     */
    public synchronized void toFirst(T item){
        int index= Tools.indexOf(list, i->i==item); // 使用工具类查找元素在列表中的索引位置（引用比较）
        if (index != -1) { // 元素在列表中存在
            list.add(0, list.remove(index)); // 先从原位置移除，再插入到索引0（首位）
        } else { // 元素在列表中不存在
            list.add(0, item); // 将新元素直接插入到索引0（首位）
        }
    }

    /**
     * 将指定元素移至列表末尾。如果元素已存在于列表中则移至末尾，
     * 如果不存在则追加到末尾。操作线程安全。
     *
     * @param item 需要移至末尾的元素
     */
    public synchronized void toEnd(T item) {
        int index = Tools.indexOf(list, i -> i == item); // 使用工具类查找元素在列表中的索引位置（引用比较）
        if (index != -1) { // 元素在列表中存在
            list.add(list.remove(index)); // 先从原位置移除，再追加到列表末尾
        } else { // 元素在列表中不存在
            list.add(item); // 将新元素直接追加到列表末尾
        }
    }

    /**
     * 获取内部持有的有序列表。
     *
     * @return List<T> 内部列表引用
     */
    public List<T> getList(){
        return list; // 返回内部列表引用
    }

    /**
     * 获取列表首位的元素。
     *
     * @return T 列表中索引0位置的元素
     */
    public T getFirst(){
        return list.get(0); // 返回列表第一个元素
    }

    /**
     * 获取列表中指定索引位置的元素。
     *
     * @param index 元素索引位置
     * @return T 指定索引位置的元素
     */
    public T getItem(int index) {
        return list.get(index); // 返回指定索引处的元素
    }
}
