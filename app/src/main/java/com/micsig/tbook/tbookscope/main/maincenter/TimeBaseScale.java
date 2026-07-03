package com.micsig.tbook.tbookscope.main.maincenter; // 定义包路径为主界面中心区域时基模块

/**
 * Created by yangj on 2018/4/9.
 */

/*
 * 
 *                             TimeBaseScale                                     
 *                          时基档位数据模型类                                    
 * 
 * 【模块定位】                                                                   
 *   位于main.maincenter包中,作为示波器时基选择功能的核心数据模型               
 * 【核心职责】                                                                   
 *   1. 存储时基档位索引(index)                                                  
 *   2. 存储时基显示字符串(scale)                                                
 * 【使用场景】                                                                   
 *   在时基选择对话框中,每个对象对应一个可选时基档位                            
 * 
 */
public class TimeBaseScale {
    private int index; // 时基档位索引
    private String scale; // 时基显示字符串如"1ms"

    public TimeBaseScale(int index, String scale) {
        this.index = index; // 设置时基档位索引
        this.scale = scale; // 设置显示字符串
    }

    public int getIndex() {
        return index; // 返回时基档位索引
    }

    public void setIndex(int index) {
        this.index = index; // 更新时基档位索引
    }

    public String getScale() {
        return scale; // 返回时基显示字符串
    }

    public void setScale(String scale) {
        this.scale = scale; // 更新时基显示字符串
    }

    @Override
    public String toString() {
        return "TimeBaseScale{" +
                "index=" + index +
                ", scale='" + scale + '\'' +
                '}';
    }
}
