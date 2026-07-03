package com.micsig.tbook.tbookscope.top.layout.userset; // 用户设置模块包声明


/*
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │ 模块定位：用户设置 → 存储/恢复 → 数据模型                                  │
 * │ 核心职责：封装"存储/恢复"功能的单条数据记录，包含索引号和名称                 │
 * │ 架构设计：POJO数据类，作为SaveRecoveryAdapter列表项的数据源                 │
 * │ 数据流向：SaveRecoveryUtil → SaveRecovery(本类) → SaveRecoveryAdapter      │
 * │ 依赖关系：无外部依赖，纯数据载体                                           │
 * │ 使用场景：用户设置界面中"存储/恢复"列表的每一项数据                          │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */

/**
 * 存储/恢复数据模型类，封装单条存储记录的索引和名称。
 * <p>
 * 每个SaveRecovery对象代表一个存储槽位，包含：
 * <ul>
 *   <li>index - 存储槽位索引（0~9）</li>
 *   <li>name - 存储槽位名称（用户自定义）</li>
 * </ul>
 *
 * @author yangj
 * @since 2017/4/27
 */
public class SaveRecovery  {
    /** 存储槽位索引号 */
    private int index; // 存储槽位索引号
    /** 存储槽位名称 */
    private String name; // 存储槽位名称

    /**
     * 构造方法，创建一个存储/恢复记录。
     *
     * @param index 存储槽位索引号
     * @param name  存储槽位名称
     */
    public SaveRecovery(int index, String name) { // 构造方法，初始化索引和名称
        this.index = index; // 设置索引号
        this.name = name; // 设置名称
    }

    /**
     * 获取存储槽位索引号。
     *
     * @return 索引号
     */
    public int getIndex() { // 获取索引号
        return index; // 返回索引号
    }

    /**
     * 设置存储槽位索引号。
     *
     * @param index 索引号
     */
    public void setIndex(int index) { // 设置索引号
        this.index = index; // 赋值索引号
    }

    /**
     * 获取存储槽位名称。
     *
     * @return 名称字符串
     */
    public String getName() { // 获取名称
        return name; // 返回名称
    }

    /**
     * 设置存储槽位名称。
     *
     * @param name 名称字符串
     */
    public void setName(String name) { // 设置名称
        this.name = name; // 赋值名称
    }

    /**
     * 返回对象的字符串表示，用于调试日志。
     *
     * @return 包含index和name的字符串
     */
    @Override // 覆写toString方法
    public String toString() { // 返回字符串表示
        return "SaveRecovery{" + // 返回类名和左花括号
                "index=" + index + // 拼接索引号
                ", name='" + name + '\'' + // 拼接名称
                '}'; // 右花括号
    }
}
