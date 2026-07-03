package com.micsig.tbook.tbookscope.top.layout.userset; // 用户设置模块包声明

import android.content.Context; // 导入上下文类
import android.content.SharedPreferences; // 导入SharedPreferences持久化存储

import com.micsig.tbook.tbookscope.util.App; // 导入应用全局实例
import com.micsig.tbook.tbookscope.util.CacheUtil; // 导入缓存工具类

import java.util.HashMap; // 导入哈希映射

/*
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │ 模块定位：用户设置 → 存储/恢复 → 持久化工具                                │
 * │ 核心职责：提供存储/恢复数据的SharedPreferences读写操作                       │
 * │ 架构设计：工具类（纯静态方法），封装SharedPreferences的存取逻辑             │
 * │ 数据流向：CacheUtil(内存缓存) ←→ SaveRecoveryUtil(本类) ←→ SharedPrefs    │
 * │ 依赖关系：依赖App全局实例、CacheUtil缓存工具                               │
 * │ 使用场景：用户点击存储/恢复时，将当前配置写入/读出SharedPreferences          │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */

/**
 * 存储/恢复持久化工具类，提供将当前配置写入SharedPreferences和从SharedPreferences读取配置的静态方法。
 * <p>
 * 支持最多{@link #SAVE_RECOVERY_NUMBER}个存储槽位，每个槽位使用独立的SharedPreferences文件。
 *
 * @author yangj
 * @since 2017/7/17
 */
public class SaveRecoveryUtil {
    /** 最大存储/恢复槽位数量 */
    public static final int SAVE_RECOVERY_NUMBER = 10; // 最大存储/恢复槽位数量
    /** SharedPreferences文件名前缀 */
    private static final String SAVE_RECOVERY_STRING = "SAVE_RECOVERY_"; // SharedPreferences文件名前缀

    /**
     * 从SharedPreferences读取指定槽位的配置数据。
     * <p>
     * 读取时以CacheUtil中的当前缓存为默认值，若SharedPreferences中无对应键则使用默认值。
     *
     * @param index 存储槽位索引（0~9）
     * @return 包含所有配置键值对的HashMap
     */
    public static HashMap<String, String> getSaveRecoveryData(int index) { // 从SharedPreferences读取指定槽位配置
        HashMap<String, String> resultMap = new HashMap<>(); // 创建结果映射
        if (index >= 0 && index < SAVE_RECOVERY_NUMBER) { // 检查索引范围有效性
            SharedPreferences preferences = App.get().getSharedPreferences(SAVE_RECOVERY_STRING + index, Context.MODE_PRIVATE); // 获取对应槽位的SharedPreferences
            HashMap<String, String> curMap = CacheUtil.get().getCacheMap(); // 获取当前内存缓存作为默认值
            for (String s : curMap.keySet()) { // 遍历所有缓存键
                resultMap.put(s, preferences.getString(s, curMap.get(s))); // 读取SharedPreferences值，无则用缓存默认值
            }
        }
        return resultMap; // 返回结果映射
    }

    /**
     * 将当前配置数据写入SharedPreferences的指定槽位。
     * <p>
     * 写入时从CacheUtil获取当前内存中的所有配置键值对，逐一写入SharedPreferences。
     *
     * @param index 存储槽位索引（0~9）
     */
    public static void putSaveRecoveryData(int index) { // 将当前配置写入SharedPreferences指定槽位
        if (index >= 0 && index < SAVE_RECOVERY_NUMBER) { // 检查索引范围有效性
            SharedPreferences preferences = App.get().getSharedPreferences(SAVE_RECOVERY_STRING + index, Context.MODE_PRIVATE); // 获取对应槽位的SharedPreferences
            HashMap<String, String> curMap = CacheUtil.get().getCacheMap(); // 获取当前内存缓存
            for (String s : curMap.keySet()) { // 遍历所有缓存键
                preferences.edit().putString(s, curMap.get(s)).commit(); // 写入键值对并同步提交
            }
        }
    }
}
