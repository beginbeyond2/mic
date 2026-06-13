package com.micsig.tbook.tbookscope.middleware;

import com.micsig.tbook.tbookscope.middleware.mq.MQChanSelectorManage;

import java.util.function.Consumer;

/**
 * @auother Liwb
 * @description:
 * @data:2024-2-29 11:21
 */
public class MiddleMain {
    private static final MiddleMain ourInstance = new MiddleMain();

    public static MiddleMain getIns() {
        return ourInstance;
    }

    private MiddleMainBean bean=new MiddleMainBean();
    private MiddleMain() {

    }
    private Consumer<Integer> a;
    public MQChanSelectorManage getChanSelectorManage(){

        return bean.getChanSelectorManage();
    }

    public MiddleMainBean getBean() {
        return bean;
    }

    public void setBean(MiddleMainBean bean) {
        this.bean = bean;
    }
}
