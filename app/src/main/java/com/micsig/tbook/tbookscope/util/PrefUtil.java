package com.micsig.tbook.tbookscope.util; // 工具包，存放示波器应用的各类工具类

import android.app.Application; // Application基类，用于获取全局上下文
import android.content.Context; // Android上下文，用于获取SharedPreferences
import android.content.SharedPreferences; // 轻量级键值对存储API

/**
 * ┌─────────────────────────────────────────────────────────────────────────────┐
 * │                          PrefUtil - SharedPreferences工具类                  │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 模块定位：MHO示波器Android应用 → 工具模块(util) → 键值对持久化                 │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 核心职责：                                                                    │
 * │   1. 封装SharedPreferences，提供统一的键值对读写接口                           │
 * │   2. 支持String/boolean/int/float/long五种基本数据类型的读写                   │
 * │   3. 提供remove/contains/clear等管理操作                                      │
 * │   4. 所有写操作使用commit()同步提交，确保数据即时持久化                         │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 架构设计：                                                                    │
 * │   - 静态工具类，所有方法均为static                                              │
 * │   - 使用"tBookScope"作为SharedPreferences文件名                               │
 * │   - 模式为MODE_PRIVATE（仅本应用可访问）                                       │
 * │   - 初始化必须在Application.onCreate()中调用init()                             │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 数据流向：                                                                    │
 * │   调用方 → PrefUtil.putXxx(key, value) → SharedPreferences.Editor.commit()  │
 * │   调用方 ← PrefUtil.getXxx(key) ← SharedPreferences.getXxx(key, default)    │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 依赖关系：                                                                    │
 * │   - SharedPreferences：Android轻量级持久化存储                                 │
 * │   - Application：提供全局上下文用于初始化SharedPreferences                      │
 * ├─────────────────────────────────────────────────────────────────────────────┤
 * │ 使用示例：                                                                    │
 * │   // 初始化（在Application.onCreate中）                                        │
 * │   PrefUtil.init(application);                                                 │
 * │   // 写入数据                                                                  │
 * │   PrefUtil.putString("key_channel", "CH1");                                   │
 * │   PrefUtil.putBoolean("key_enabled", true);                                   │
 * │   // 读取数据                                                                  │
 * │   String channel = PrefUtil.getString("key_channel");                         │
 * │   boolean enabled = PrefUtil.getBoolean("key_enabled", false);                │
 * └─────────────────────────────────────────────────────────────────────────────┘
 */
public class PrefUtil {
    /** SharedPreferences实例，存储文件名为"tBookScope"，模式为MODE_PRIVATE */
    public static SharedPreferences settings; // 全局SharedPreferences实例，init()中初始化

    /**
     * 初始化SharedPreferences。
     * 必须在Application.onCreate()中调用，否则其他方法将抛出NullPointerException。
     *
     * @param application 应用Application对象，用于获取SharedPreferences实例
     */
    public static void init(Application application) { // 初始化方法
        settings = application.getSharedPreferences("tBookScope", Context.MODE_PRIVATE); // 获取名为"tBookScope"的SharedPreferences，私有模式
    }

    /**
     * 判断SharedPreferences中是否包含指定key
     * @param key 要查找的键名
     * @return true=存在该key；false=不存在
     */
    public static boolean contains(String key) { // 检查key是否存在
        return settings.contains(key); // 委托给SharedPreferences.contains()
    }

    /**
     * 从SharedPreferences中移除指定key的键值对
     * 使用commit()同步提交，确保立即生效
     * @param key 要移除的键名
     */
    public static void remove(String key) { // 移除指定key
        settings.edit().remove(key).commit(); // 同步移除并提交
    }

    /**
     * 读取String值，默认返回空字符串""
     * @param key 要读取的键名
     * @return 对应的String值，若key不存在则返回""
     */
    public static String getString(String key) { // 读取String（无默认值版本）
        return getString(key, ""); // 委托给带默认值版本，默认值为空字符串
    }

    /**
     * 读取String值，可指定默认值
     * @param key          要读取的键名
     * @param defaultValue key不存在时返回的默认值
     * @return 对应的String值，若key不存在则返回defaultValue
     */
    public static String getString(String key, final String defaultValue) { // 读取String（带默认值版本）
        return settings.getString(key, defaultValue); // 委托给SharedPreferences.getString()
    }

    /**
     * 写入String键值对
     * 使用commit()同步提交，确保数据即时持久化
     * @param key   键名
     * @param value 要存储的字符串值
     */
    public static void putString(final String key, final String value) { // 写入String
        settings.edit().putString(key, value).commit(); // 同步写入并提交
    }

    /**
     * 读取boolean值
     * @param key          要读取的键名
     * @param defaultValue key不存在时返回的默认值
     * @return 对应的boolean值，若key不存在则返回defaultValue
     */
    public static boolean getBoolean(final String key, final boolean defaultValue) { // 读取boolean
        return settings.getBoolean(key, defaultValue); // 委托给SharedPreferences.getBoolean()
    }

    /**
     * 判断SharedPreferences中是否包含指定key（与contains方法功能相同）
     * @param key 要查找的键名
     * @return true=存在该key；false=不存在
     */
    public static boolean hasKey(final String key) { // 检查key是否存在（别名方法）
        return settings.contains(key); // 委托给SharedPreferences.contains()
    }

    /**
     * 写入boolean键值对
     * 使用commit()同步提交，确保数据即时持久化
     * @param key   键名
     * @param value 要存储的布尔值
     */
    public static void putBoolean(final String key, final boolean value) { // 写入boolean
        settings.edit().putBoolean(key, value).commit(); // 同步写入并提交
    }

    /**
     * 写入int键值对
     * 使用commit()同步提交，确保数据即时持久化
     * @param key   键名
     * @param value 要存储的整数值
     */
    public static void putInt(final String key, final int value) { // 写入int
        settings.edit().putInt(key, value).commit(); // 同步写入并提交
    }

    /**
     * 读取int值
     * @param key          要读取的键名
     * @param defaultValue key不存在时返回的默认值
     * @return 对应的int值，若key不存在则返回defaultValue
     */
    public static int getInt(final String key, final int defaultValue) { // 读取int
        return settings.getInt(key, defaultValue); // 委托给SharedPreferences.getInt()
    }

    /**
     * 写入float键值对
     * 使用commit()同步提交，确保数据即时持久化
     * @param key   键名
     * @param value 要存储的浮点数值
     */
    public static void putFloat(final String key, final float value) { // 写入float
        settings.edit().putFloat(key, value).commit(); // 同步写入并提交
    }

    /**
     * 读取float值
     * @param key          要读取的键名
     * @param defaultValue key不存在时返回的默认值
     * @return 对应的float值，若key不存在则返回defaultValue
     */
    public static float getFloat(final String key, final float defaultValue) { // 读取float
        return settings.getFloat(key, defaultValue); // 委托给SharedPreferences.getFloat()
    }

    /**
     * 写入long键值对
     * 使用commit()同步提交，确保数据即时持久化
     * @param key   键名
     * @param value 要存储的长整数值
     */
    public static void putLong(final String key, final long value) { // 写入long
        settings.edit().putLong(key, value).commit(); // 同步写入并提交
    }

    /**
     * 读取long值，默认返回0
     * @param key 要读取的键名
     * @return 对应的long值，若key不存在则返回0
     */
    public static long getLong(final String key) { // 读取long（无默认值版本）
        return getLong(key, 0); // 委托给带默认值版本，默认值为0
    }

    /**
     * 读取long值，可指定默认值
     * @param key          要读取的键名
     * @param defaultValue key不存在时返回的默认值
     * @return 对应的long值，若key不存在则返回defaultValue
     */
    public static long getLong(final String key, final long defaultValue) { // 读取long（带默认值版本）
        return settings.getLong(key, defaultValue); // 委托给SharedPreferences.getLong()
    }

    /**
     * 清空指定SharedPreferences实例中的所有键值对
     * 使用commit()同步提交，确保立即生效
     * @param context 未使用（历史遗留参数）
     * @param p       要清空的SharedPreferences实例
     */
    public static void clear(Context context, final SharedPreferences p) { // 清空指定SharedPreferences
        final SharedPreferences.Editor editor = p.edit(); // 获取编辑器
        editor.clear(); // 清空所有键值对
        editor.commit(); // 同步提交
    }

    /**
     * 清空默认SharedPreferences（"tBookScope"）中的所有键值对
     * 使用commit()同步提交，确保立即生效
     * 注意：此操作不可逆，将删除所有已保存的配置
     */
    public static void clear() { // 清空默认SharedPreferences
        settings.edit().clear().commit(); // 清空所有键值对并同步提交
    }
}
