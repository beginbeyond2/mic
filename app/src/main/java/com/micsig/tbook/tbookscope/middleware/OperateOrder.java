package com.micsig.tbook.tbookscope.middleware;

import com.micsig.tbook.tbookscope.tools.Tools;

import java.util.List;

/**
 * @auother Liwb
 * @description:
 * @data:2024-2-29 9:13
 */
public class OperateOrder<T> {
    public OperateOrder(){}
    private List<T> list;
    public OperateOrder(List<T> list){
        this.list=list;
    }
    public synchronized void toFirst(T item){
        int index= Tools.indexOf(list, i->i==item);
        if (index != -1) {
            list.add(0, list.remove(index));
        } else {
            list.add(0, item);
        }
    }

    public synchronized void toEnd(T item) {
        int index = Tools.indexOf(list, i -> i == item);
        if (index != -1) {
            list.add(list.remove(index));
        } else {
            list.add(item);
        }
    }
    public List<T> getList(){
        return list;
    }
    public T getFirst(){
        return list.get(0);
    }

    public T getItem(int index) {
        return list.get(index);
    }
}
