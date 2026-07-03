package com.micsig.tbook.tbookscope.rightslipmenu.dialog;



/**
 * Created by yangj on 2017/5/3.
 */

/*
 * +=============================================================================+
 * |                       DialogRefRecallBean                                    |
 * |-----------------------------------------------------------------------------|
 * | 模块定位 : 右侧滑菜单 -> 参考波形调出对话框的数据模型（Bean）                     |
 * | 核心职责 : 封装单个参考波形文件的所有展示与交互属性                                |
 * | 架构设计 : 标准 POJO/Bean 模式，提供全字段 getter/setter                         |
 * | 数据流向 : DialogRefRecall.getList() 创建 → DialogRefRecallAdapter 绑定展示 →   |
 * |           DialogRefRecall 读取选中状态与路径                                    |
 * | 依赖关系 : 无外部依赖，纯数据模型                                               |
 * | 使用场景 : 作为 DialogRefRecall/ForEightRefRecallAdapter 的列表数据项             |
 * +=============================================================================+
 */
public class DialogRefRecallBean {
    private int index;                                                          // 列表中的索引位置
    private String title;                                                       // 波形文件名（去除后缀）
    private String time;                                                        // 格式化的最后修改时间
    private boolean select;                                                     // 是否被选中
    private String pathFile;                                                    // 波形文件的绝对路径
    public long lastModifyTime;                                                 // 最后修改时间戳（用于排序）

    /**
     * 全参数构造方法
     * @param index         列表索引
     * @param title         文件名标题
     * @param time          格式化修改时间
     * @param pathFile      文件绝对路径
     * @param lastModifyTime最后修改时间戳
     * @param select        是否选中
     */
    public DialogRefRecallBean(int index, String title, String time,String pathFile,long lastModifyTime, boolean select) {
        this.index = index;                                                     // 设置索引
        this.title = title;                                                     // 设置标题
        this.time = time;                                                       // 设置时间
        this.pathFile=pathFile;                                                 // 设置文件路径
        this.select = select;                                                   // 设置选中状态
        this.lastModifyTime=lastModifyTime;                                     // 设置修改时间戳
    }

    /**
     * 无参构造方法
     */
   public   DialogRefRecallBean(){}

    /**
     * 获取列表索引
     * @return 索引值
     */
    public int getIndex() {
        return index;                                                           // 返回索引
    }

    /**
     * 设置列表索引
     * @param index 索引值
     */
    public void setIndex(int index) {
        this.index = index;                                                     // 设置索引
    }

    /**
     * 获取文件名标题
     * @return 标题字符串
     */
    public String getTitle() {
        return title;                                                           // 返回标题
    }

    /**
     * 设置文件名标题
     * @param title 标题字符串
     */
    public void setTitle(String title) {
        this.title = title;                                                     // 设置标题
    }

    /**
     * 获取格式化修改时间
     * @return 时间字符串
     */
    public String getTime() {
        return time;                                                            // 返回时间
    }

    /**
     * 设置格式化修改时间
     * @param time 时间字符串
     */
    public void setTime(String time) {
        this.time = time;                                                       // 设置时间
    }

    /**
     * 判断是否被选中
     * @return 选中返回 true
     */
    public boolean isSelect() {
        return select;                                                          // 返回选中状态
    }

    /**
     * 设置选中状态
     * @param select true=选中
     */
    public void setSelect(boolean select) {
        this.select = select;                                                   // 设置选中状态
    }

    /**
     * 获取文件绝对路径
     * @return 路径字符串
     */
    public String getPathFile() {
        return pathFile;                                                        // 返回文件路径
    }

    /**
     * 设置文件绝对路径
     * @param pathFile 路径字符串
     */
    public void setPathFile(String pathFile) {
        this.pathFile = pathFile;                                               // 设置文件路径
    }

    /**
     * 获取最后修改时间戳
     * @return 时间戳（毫秒）
     */
    public long getLastModifyTime() {
        return lastModifyTime;                                                  // 返回修改时间戳
    }

    /**
     * 设置最后修改时间戳
     * @param lastModifyTime 时间戳（毫秒）
     */
    public void setLastModifyTime(long lastModifyTime) {
        this.lastModifyTime = lastModifyTime;                                   // 设置修改时间戳
    }
}
