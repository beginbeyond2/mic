package com.micsig.tbook.tbookscope.tools;  // 示波器工具类所在包

import android.annotation.SuppressLint;  // 抑制lint警告注解
import android.app.Activity;  // Activity基类
import android.content.Context;  // 上下文环境
import android.content.Intent;  // 意图，用于广播等
import android.graphics.Bitmap;  // 位图操作
import android.graphics.BitmapFactory;  // 位图工厂，解码图片
import android.graphics.Canvas;  // 画布，绘制图形
import android.graphics.NinePatch;  // 九宫格图片
import android.graphics.Paint;  // 画笔
import android.graphics.Rect;  // 矩形区域
import android.graphics.drawable.Drawable;  // 可绘制资源基类
import android.graphics.drawable.NinePatchDrawable;  // 九宫格可绘制资源
import android.net.Uri;  // 统一资源标识符
import android.os.Build;  // 系统版本信息
import android.os.Environment;  // 外部存储环境
import android.os.Handler;  // 消息处理器，用于延迟执行
import android.os.storage.StorageManager;  // 存储管理器
import android.os.storage.StorageVolume;  // 存储卷信息
import android.provider.Settings;  // 系统设置提供者
import android.text.TextPaint;  // 文本画笔
import android.text.TextUtils;  // 文本工具类
import android.util.Log;  // 日志工具
import android.util.TypedValue;  // 类型值转换
import android.view.View;  // 视图基类
import android.view.ViewGroup;  // 视图组基类
import android.widget.Button;  // 按钮控件
import android.widget.TextView;  // 文本控件

import androidx.annotation.IntDef;  // 整型注解定义
import androidx.annotation.RequiresApi;  // API版本要求注解
import androidx.core.content.ContextCompat;  // 兼容上下文工具
import androidx.core.content.FileProvider;  // 文件提供者，安全共享文件
import androidx.recyclerview.widget.RecyclerView;  // 列表视图

import com.google.gson.Gson;  // JSON序列化库
import com.google.gson.JsonObject;  // JSON对象
import com.google.gson.reflect.TypeToken;  // 泛型类型令牌
import com.micsig.base.DoubleUtil;  // 双精度浮点工具
import com.micsig.base.Logger;  // 自定义日志器
import com.micsig.tbook.scope.Scope;  // 示波器核心类
import com.micsig.tbook.scope.ScopeBase;  // 示波器基础参数
import com.micsig.tbook.scope.channel.BaseChannel;  // 通道基类
import com.micsig.tbook.scope.channel.Channel;  // 通道类
import com.micsig.tbook.scope.channel.ChannelFactory;  // 通道工厂，管理通道实例
import com.micsig.tbook.scope.channel.MathChannel;  // 数学运算通道
import com.micsig.tbook.scope.channel.RefChannel;  // 参考波形通道
import com.micsig.tbook.scope.horizontal.HorizontalAxis;  // 水平轴（时基）管理
import com.micsig.tbook.scope.math.MathWave;  // 数学波形定义
import com.micsig.tbook.scope.measure.Measure;  // 测量参数
import com.micsig.tbook.scope.mem.Memory;  // 内存同步操作
import com.micsig.tbook.scope.probe.BaseProbe;  // 探头基类
import com.micsig.tbook.tbookscope.GlobalVar;  // 全局变量管理
import com.micsig.tbook.tbookscope.config.ScopeConfig;  // 示波器配置
import com.micsig.tbook.tbookscope.middleware.command.Command;  // 命令中间件
import com.micsig.tbook.tbookscope.services.ExternalKeys.client.ExternalKeysProtocol;  // 外部按键协议
import com.micsig.tbook.tbookscope.util.App;  // 应用全局工具
import com.micsig.tbook.tbookscope.util.CacheUtil;  // 缓存工具，管理SharedPreferences
import com.micsig.tbook.tbookscope.wavezone.IWorkMode;  // 工作模式接口
import com.micsig.tbook.tbookscope.wavezone.WorkModeManage;  // 工作模式管理
import com.micsig.tbook.tbookscope.wavezone.measure.MeasureManage;  // 测量管理
import com.micsig.tbook.tbookscope.wavezone.wave.WaveManage;  // 波形管理
import com.micsig.tbook.ui.util.StrUtil;  // 字符串工具
import com.micsig.tbook.ui.util.TBookUtil;  // TBook通用工具
import com.micsig.tbook.ui.wavezone.TChan;  // 通道编号转换工具

import java.io.BufferedWriter;  // 缓冲写入器
import java.io.ByteArrayInputStream;  // 字节数组输入流
import java.io.ByteArrayOutputStream;  // 字节数组输出流
import java.io.File;  // 文件操作
import java.io.FileNotFoundException;  // 文件未找到异常
import java.io.FileOutputStream;  // 文件输出流
import java.io.FileWriter;  // 文件写入器
import java.io.IOException;  // IO异常
import java.io.ObjectInputStream;  // 对象输入流，用于反序列化
import java.io.ObjectOutputStream;  // 对象输出流，用于序列化
import java.lang.annotation.Retention;  // 注解保留策略
import java.lang.annotation.RetentionPolicy;  // 保留策略枚举
import java.lang.reflect.Type;  // 反射类型
import java.text.DateFormat;  // 日期格式化
import java.text.SimpleDateFormat;  // 简单日期格式化
import java.util.ArrayList;  // 动态数组
import java.util.Calendar;  // 日历类
import java.util.Date;  // 日期类
import java.util.HashMap;  // 哈希映射
import java.util.Iterator;  // 迭代器
import java.util.List;  // 列表接口
import java.util.Locale;  // 地区设置
import java.util.Map;  // 映射接口
import java.util.function.BiConsumer;  // 双参数消费者函数
import java.util.function.Predicate;  // 断言函数
import java.util.regex.Pattern;  // 正则表达式模式

/**
 * Created by liwb on 2017/9/26.
 */
/*
 * +======================================================================================+
 * |                                  类：Tools                                           |
 * +--------------------------------------------------------------------------------------+
 * | 模块定位：示波器应用通用工具类，提供全局静态工具方法                                   |
 * +--------------------------------------------------------------------------------------+
 * | 核心职责：                                                                            |
 * |   1. 文件存储路径管理（本地/U盘存储路径获取与创建）                                    |
 * |   2. 坐标转换（Scale↔Pix、Timebase↔Pix、YT↔Zoom坐标系互转）                          |
 * |   3. 通道电平计算与缓存读写                                                           |
 * |   4. 数据格式转换（字节↔十六进制、深度拷贝、JSON序列化）                               |
 * |   5. 图片保存与位图处理（九宫格、SVG转位图）                                           |
 * |   6. 测量数据更新与单位拼接                                                           |
 * |   7. UI辅助（文本绘制、控件位置调试、时间格式化）                                      |
 * +--------------------------------------------------------------------------------------+
 * | 架构设计：纯静态工具类，无状态实例化，所有方法均为static                                |
 * +--------------------------------------------------------------------------------------+
 * | 数据流向：CacheUtil(缓存) ↔ Tools(转换) ↔ UI/硬件                                    |
 * +--------------------------------------------------------------------------------------+
 * | 依赖关系：                                                                            |
 * |   - ChannelFactory：通道实例获取                                                      |
 * |   - CacheUtil：SharedPreferences缓存读写                                              |
 * |   - ScopeBase：示波器基础参数（像素/格、缩放系数等）                                   |
 * |   - HorizontalAxis：时基轴参数                                                        |
 * |   - WaveManage：波形位置管理                                                          |
 * |   - GlobalVar：全局变量（主波形区域尺寸）                                              |
 * +--------------------------------------------------------------------------------------+
 * | 使用场景：被示波器各模块广泛调用，如波形绘制、存储、测量、触发等                        |
 * +======================================================================================+
 */
public class Tools {
    private static final String TAG = "Tools";  // 日志标签
    //region  全局
    public static final int SaveType_LOCAL = 0x00;  // 存储类型：本地存储
    public static final int SaveType_UDISK = 0x01;  // 存储类型：U盘存储

    @IntDef({SaveType_LOCAL, SaveType_UDISK})  // 限定存储类型取值范围
    @Retention(RetentionPolicy.SOURCE)  // 注解仅保留在源码中
    public @interface SaveType {  // 存储类型注解接口
    }

    public static final String SaveDir_DEFAULT = "default/";  // 默认保存目录
    public static final String SaveDir_USERSET = "userset/";  // 用户设置保存目录
    public static final String SaveDir_REFWAVE = "refwave/";  // 参考波形保存目录
    public static final String SaveDir_REFDEFAULT = "refdefault/";  // 参考默认保存目录
    public static final String SaveDir_CSVWAVE = "csvwave/";  // CSV波形保存目录
    public static final String SaveDir_CSVSBT = "csvsbt/";  // CSV SBT保存目录
    public static final String SaveDir_BINWAVE = "binwave/";  // 二进制波形保存目录
    public static final String SaveDir_DCIM = "DCIM/";  // 相册保存目录

    //endregion

    //region 静态变量
    private static Rect rectText = new Rect();  // 文本测量矩形（复用）

    private static long beginTime, endTime;  // 计时用的起止时间戳
    //endregion

    /**
     * 获取文本的边界矩形
     *
     * @param text  待测量的文本字符串
     * @param paint 用于测量的画笔对象
     * @return 文本的边界矩形
     */
    public static Rect getTextRect(String text, Paint paint) {
        Rect rectText = new Rect();  // 创建新的矩形对象
        paint.getTextBounds(text, 0, text.length(), rectText);  // 用画笔测量文本边界
        return rectText;  // 返回测量结果
    }

    /**
     * 将文本中的数字替换为"0"，用于获取文本的格式模板
     *
     * @param text 原始文本
     * @return 数字被替换为"0"的格式化文本
     */
    public static String getTextFormat(String text) {
        return text.replaceAll("[0-9]", "0");  // 将所有数字字符替换为"0"
    }

    /**
     * 使当前线程休眠指定毫秒数
     *
     * @param delayTime_ms 休眠时长（毫秒）
     */
    public static void sleep(int delayTime_ms) {
        try {
            Thread.sleep(delayTime_ms);  // 线程休眠指定时间
        } catch (InterruptedException e) {
            e.printStackTrace();  // 捕获中断异常并打印堆栈
        }
    }
    public static final String SCOPE_PATH = "Oscilloscope";  // 示波器默认存储根目录名
    public static final String SMART_PATH = "smart";  // 智能存储根目录名
    /***
     * 返回默认存储路径
     * @param SaveType  存储类型
     * @return
     */
    public static String resultSavePath(@SaveType int SaveType, String SaveDir, Activity activity) {
        if (SaveDir == null || SaveDir.equals("")) SaveDir = "";  // 空值保护，保存目录为空则置空字符串
        String path = "";  // 初始化路径
        switch (SaveType) {  // 根据存储类型选择路径
            case SaveType_LOCAL: {  // 本地存储
                path = Environment.getExternalStorageDirectory().getAbsolutePath();  // 获取外部存储根路径
                Logger.i(TAG, "Path:" + path);  // 打印路径日志
            }
            break;
            case SaveType_UDISK: {  // U盘存储
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {  // Android 11及以上
                    path=getUDiskPath(App.get().getApplicationContext());  // 通过存储管理器获取U盘路径
                }else {  // Android 11以下
                    path=Environment.getExternalStorageDirectory().toString();  // 使用外部存储根路径
                }
            }
            break;
            default: {  // 默认情况
                path = Environment.getExternalStorageDirectory().toString();  // 使用外部存储根路径
            }
            break;
        }
        File f = new File(path + File.separator + SMART_PATH + File.separator);  // 检查smart目录是否存在
        if(f.exists()){  // smart目录存在
            path += File.separator + SMART_PATH + File.separator;  // 使用smart路径
        }else{  // smart目录不存在
            path += File.separator + SCOPE_PATH + File.separator;  // 使用Oscilloscope路径
        }

        File file = new File(path);  // 创建根目录文件对象
        if (!file.exists()) {  // 根目录不存在
            boolean b = file.mkdir();  // 创建根目录
            uDiskUpdate(path, activity);  // 通知媒体扫描器更新
            Logger.i(TAG, "mkdir :" + b);  // 打印创建结果日志
        }
        path = path + SaveDir;  // 拼接子目录路径
        file = new File(path);  // 创建子目录文件对象
        if (!file.exists()) {  // 子目录不存在
            boolean b = file.mkdir();  // 创建子目录
            uDiskUpdate(path, activity);  // 通知媒体扫描器更新
            Logger.i(TAG, "mkdir saveDir:" + path);  // 打印子目录创建日志
        }
        return path;  // 返回完整保存路径
    }

    /**
     * 获取临时数据保存路径
     *
     * @return 临时数据目录的绝对路径
     */
    public static String getSaveDataPath(){
        String path = "";  // 初始化路径
        path = Environment.getExternalStorageDirectory().getAbsolutePath();  // 获取外部存储根路径
        File f = new File(path + File.separator + SCOPE_PATH + File.separator + "tmpdata"+ File.separator);  // 构建临时数据目录路径
        if(!f.exists()){  // 临时数据目录不存在
            f.mkdirs();  // 递归创建目录
        }
        return f.getAbsolutePath();  // 返回绝对路径
    }

    /**
     * 获取默认保存路径（相对路径，去除根路径前缀）
     *
     * @param saveDir  保存子目录名
     * @param activity 当前Activity
     * @return 相对于外部存储根的路径
     */
    public static String resultDefaultSavePath(String saveDir, Activity activity) {
        String path = resultSavePath(Tools.SaveType_LOCAL, saveDir, activity);  // 获取本地存储完整路径
        String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();  // 获取外部存储根路径
        if (path.startsWith(rootPath)) {  // 如果路径以根路径开头
            path = path.substring(rootPath.length());  // 截取相对路径
        }
        return path;  // 返回相对路径
    }

    /**
     * 获取默认保存路径（绝对路径）
     *
     * @param saveDir  保存子目录名
     * @param activity 当前Activity
     * @return 本地存储的绝对路径
     */
    public static String resultAbDefaultSavePath(String saveDir, Activity activity) {
        return resultSavePath(Tools.SaveType_LOCAL, saveDir, activity);  // 直接返回本地存储完整路径
    }


    public static final String ACTION_MEDIA_SCANNER_SCAN_DIR = "android.intent.action.MEDIA_SCANNER_SCAN_DIR";  // 媒体扫描目录广播Action

    /**
     * 通知媒体扫描器扫描指定目录（Activity版本）
     *
     * @param path        待扫描的文件路径
     * @param mainActivity 主Activity
     * @return 是否成功发送广播
     */
    public static boolean uDiskUpdate(String path, Activity mainActivity) {
        if (mainActivity == null) return false;  // Activity为空则返回失败
        Intent mediaScanIntent = new Intent(ACTION_MEDIA_SCANNER_SCAN_DIR);  // 创建媒体扫描意图
        File f = new File(path);  // 创建文件对象
        if (f.isFile()) {  // 如果是文件（非目录）
            Uri uri = FileProvider.getUriForFile(mainActivity, "com.micsig.tbook.fileprovider", f);  // 通过FileProvider获取安全URI
            mediaScanIntent.setData(uri);  // 设置扫描目标URI
            mainActivity.sendBroadcast(mediaScanIntent);  // 发送媒体扫描广播
        }
        return true;  // 返回成功
    }

    /**
     * 通知媒体扫描器扫描指定目录（Context版本）
     *
     * @param path    待扫描的文件路径
     * @param context 上下文
     * @return 是否成功发送广播
     */
    public static boolean uDiskUpdate(String path, Context context) {
        if (context == null) return false;  // 上下文为空则返回失败
        Intent mediaScanIntent = new Intent(ACTION_MEDIA_SCANNER_SCAN_DIR);  // 创建媒体扫描意图
        File f = new File(path);  // 创建文件对象
        if (f.isFile()) {  // 如果是文件（非目录）
            Uri uri = FileProvider.getUriForFile(context, "com.micsig.tbook.fileprovider", f);  // 通过FileProvider获取安全URI
            mediaScanIntent.setData(uri);  // 设置扫描目标URI
            context.sendBroadcast(mediaScanIntent);  // 发送媒体扫描广播
        }
        return true;  // 返回成功
    }

    /**
     * 通知媒体扫描器扫描指定文件（使用ACTION_MEDIA_SCANNER_SCAN_FILE）
     *
     * @param path    待扫描的文件路径
     * @param context 上下文
     * @return 是否成功发送广播
     */
    public static boolean uDiskUpdateFile(String path, Context context) {
        if (context == null) return false;  // 上下文为空则返回失败
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);  // 创建文件扫描意图
        mediaScanIntent.setData(Uri.fromFile(new File(path)));  // 设置文件URI
        context.sendBroadcast(mediaScanIntent);  // 发送媒体扫描广播
        return true;  // 返回成功
    }

    /***
     *返回当前U盘的挂载路径
     * @return
     */
    public static List<String> getAllExternalSdcardPath() {

        List<String> list = new ArrayList<>();  // 创建路径列表
        for (String path : mapUdisk.values()) {  // 遍历U盘映射表中的所有路径
            list.add(path);  // 将路径添加到列表
        }

        return list;  // 返回所有U盘路径列表
    }


    /**
     * UI 转 channelFactory
     *
     * @param channel UI通道编号（0-8）
     * @return 对应的ChannelFactory通道常量
     */
    public static int UIChannelToChannelFactory(int channel) {
        switch (channel) {  // 根据UI通道编号映射
            case 0:  // UI通道0
                return ChannelFactory.CH1;  // 对应物理通道1
            case 1:  // UI通道1
                return ChannelFactory.CH2;  // 对应物理通道2
            case 2:  // UI通道2
                return ChannelFactory.CH3;  // 对应物理通道3
            case 3:  // UI通道3
                return ChannelFactory.CH4;  // 对应物理通道4
            case 4:  // UI通道4
                return ChannelFactory.MATH1;  // 对应数学通道1
            case 5:  // UI通道5
                return ChannelFactory.REF1;  // 对应参考通道1
            case 6:  // UI通道6
                return ChannelFactory.REF2;  // 对应参考通道2
            case 7:  // UI通道7
                return ChannelFactory.REF3;  // 对应参考通道3
            case 8:  // UI通道8
                return ChannelFactory.REF4;  // 对应参考通道4
        }
        return ChannelFactory.CH1;  // 默认返回通道1
    }

    public static final int LevelType_Normal = 0;  // 电平类型：普通
    public static final int LevelType_High = 1;  // 电平类型：高
    public static final int LevelType_Low = 2;  // 电平类型：低

    public static final int LevelMode_Normal = 0;  // 电平模式：普通
    public static final int LevelMode_Bus = 1;  // 电平模式：总线

    /**
     * 输入当前通道和当前电平的距离view顶部的px像素值，得到当前通道电平的V/A值
     *
     * @param ch        1-4
     * @param levelType 0，普通，1搞，2地
     * @param levelMode 0，普通，1 总线
     */
    public static String getChannelLevel(int ch, int levelType, int levelMode) {
        Channel channel = ChannelFactory.getDynamicChannel(ch - 1);  // 获取动态通道对象（ch-1转为0基索引）
        String key = CacheUtil.TRIGGER_CHANNEL;  // 默认使用触发通道缓存键
        if (levelMode == LevelMode_Bus) {  // 总线模式
            key = CacheUtil.VALUE_CHANNEL;  // 使用值通道缓存键
        }
        if (levelType == LevelType_High) {  // 高电平类型
            key += CacheUtil.HIGH;  // 追加高电平后缀
        }
        key += ch;  // 追加通道编号
        double val = getYTLevelCache(key);  // 从缓存获取YT模式电平像素值
        double chEvery = 1;  // 每像素对应的电压/电流值，默认为1
//        if (ch == CacheUtil.CH1) {
//            chEvery = ChannelFactory.getDynamicChannel(ChannelFactory.CH1 + ch - 1)
//                    .getVScaleIdVal(CacheUtil.get().getInt(CacheUtil.MAIN_RIGHT_CH1_VSCALEID));
//            chEvery *= TBookUtil.getDoubleFromX(CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_PROBE_MULTIPLE + CacheUtil.CH1));
//            chEvery /= ScopeBase.getVerticalPerGridPixels();
//        } else if (ch == CacheUtil.CH2) {
//            chEvery = ChannelFactory.getDynamicChannel(ChannelFactory.CH1 + ch - 1)
//                    .getVScaleIdVal(CacheUtil.get().getInt(CacheUtil.MAIN_RIGHT_CH2_VSCALEID));
//            chEvery *= TBookUtil.getDoubleFromX(CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_PROBE_MULTIPLE + CacheUtil.CH2));
//            chEvery /= ScopeBase.getVerticalPerGridPixels();
//        } else if (ch == CacheUtil.CH3) {
//            chEvery = ChannelFactory.getDynamicChannel(ChannelFactory.CH1 + ch - 1)
//                    .getVScaleIdVal(CacheUtil.get().getInt(CacheUtil.MAIN_RIGHT_CH3_VSCALEID));
//            chEvery *= TBookUtil.getDoubleFromX(CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_PROBE_MULTIPLE + CacheUtil.CH3));
//            chEvery /= ScopeBase.getVerticalPerGridPixels();
//        } else if (ch == CacheUtil.CH4) {
//            chEvery = ChannelFactory.getDynamicChannel(ChannelFactory.CH1 + ch - 1)
//                    .getVScaleIdVal(CacheUtil.get().getInt(CacheUtil.MAIN_RIGHT_CH4_VSCALEID));
//            chEvery *= TBookUtil.getDoubleFromX(CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_PROBE_MULTIPLE + CacheUtil.CH4));
//            chEvery /= ScopeBase.getVerticalPerGridPixels();
//        }
        String unit = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_CH_PROBE_TYPE + ch) == 0 ? "V" : "A";  // 根据探头类型确定单位
        unit = Tools.getChanProbeTypeUnit(ch - 1) == 0 ? "V" : "A";  // 再次通过通道探头类型确认单位
        if (TChan.isChan(ch)) {  // 如果是模拟通道
            Channel chan = ChannelFactory.getDynamicChannel(TChan.toFpgaChNo(ch));  // 获取对应的FPGA通道
            if (chan == null) return "";  // 通道为空则返回空字符串
//            chEvery = chan.getVScaleIdVal(CacheUtil.get().getInt(CacheUtil.MAIN_CHAN_V_SCALE_ID + ch)) *;
//            chEvery *= TBookUtil.getDoubleFromX(CacheUtil.get().getString(CacheUtil.RIGHT_SLIP_CH_PROBE_MULTIPLE + ch));
            chEvery = chan.getVScaleVal();  // 获取通道垂直档位值
            chEvery /= ScopeBase.getVerticalPerGridPixels() * ScopeBase.getToUICoff();  // 转换为每像素对应的电压值
//            return TBookUtil.getFourFromD_Trim0(ScopeBase.changeAccuracy(chEvery * val)) + unit;
            return TBookUtil.getFourFromD_Trim0(chEvery * val) + unit;  // 计算电平值并格式化，拼接单位
        } else if (ch == TChan.Ch8 + 1) {  // 如果是外部触发通道
            double temp = getExtTriggerValCache(key);  // 获取外部触发电平值
            unit = "V";  // 外部触发单位固定为伏特
            return TBookUtil.getFourFromD_Trim0(temp) + unit;  // 格式化并返回
        } else {  // 其他通道
            return TBookUtil.getFourFromD_Trim0(chEvery * val) + unit;  // 计算电平值并格式化
        }
    }

    /**
     * 将电压/电流值转换为屏幕像素位置
     *
     * @param chIdx 通道索引（ChannelFactory常量）
     * @param val   电压/电流值
     * @return 对应的屏幕Y轴像素位置
     */
    public static double ScaleToPix(int chIdx,double val){
        switch (chIdx){  // 根据通道类型分别处理
            case ChannelFactory.CH1:  // 物理通道1
            case ChannelFactory.CH2:  // 物理通道2
            case ChannelFactory.CH3:  // 物理通道3
            case ChannelFactory.CH4:  // 物理通道4
            case ChannelFactory.CH5:  // 物理通道5
            case ChannelFactory.CH6:  // 物理通道6
            case ChannelFactory.CH7:  // 物理通道7
            case ChannelFactory.CH8:  // 物理通道8
            {
                Channel channel = ChannelFactory.getDynamicChannel(chIdx);  // 获取动态通道
                double curPos =  (DoubleUtil.divide( val , channel.getVerticalPerPix()));  // 值除以每像素电压值得到偏移像素
                double chPos=( GlobalVar.get().getMainWave().y/2- WaveManage.get().getPositionY(TChan.toUiChNo(chIdx)));  // 通道零点相对屏幕中心的偏移
                double pix=GlobalVar.get().getMainWave().y/2-curPos-chPos;  // 最终像素位置 = 屏幕中心 - 值偏移 - 通道零点偏移
//                Log.d("Tag.Debug", String.format("Tools.ScaleToPix: height/2:%s, val:%s,verPerPix:%s, curPos:%s,chPos:%s pix:%s",GlobalVar.get().getMainWave().y/2, val,channel.getVerticalPerPix(),curPos,chPos,pix ));
                return pix;  // 返回像素位置
            }
            case ChannelFactory.MATH1:  // 数学通道1
            case ChannelFactory.MATH2:  // 数学通道2
            case ChannelFactory.MATH3:  // 数学通道3
            case ChannelFactory.MATH4:  // 数学通道4
            case ChannelFactory.MATH5:  // 数学通道5
            case ChannelFactory.MATH6:  // 数学通道6
            case ChannelFactory.MATH7:  // 数学通道7
            case ChannelFactory.MATH8:  // 数学通道8
            {
                MathChannel math = ChannelFactory.getMathChannel(chIdx);  // 获取数学通道
                double curPos =(val / math.getVerticalPerPix());  // 值除以每像素电压值得到偏移像素
                double chPos= ( GlobalVar.get().getMainWave().y/2- WaveManage.get().getPositionY(TChan.toUiChNo(chIdx)));  // 通道零点相对屏幕中心的偏移
                double pix=GlobalVar.get().getMainWave().y/2-curPos-chPos;  // 最终像素位置
//                Log.d("Tag.Debug", String.format("Tools.ScaleToPix: curPos:%s, chPos:%s ,pix:%s",curPos,chPos,pix ));
                return pix;  // 返回像素位置
            }
            case ChannelFactory.REF1:  // 参考通道1
            case ChannelFactory.REF2:  // 参考通道2
            case ChannelFactory.REF3:  // 参考通道3
            case ChannelFactory.REF4:  // 参考通道4
            case ChannelFactory.REF5:  // 参考通道5
            case ChannelFactory.REF6:  // 参考通道6
            case ChannelFactory.REF7:  // 参考通道7
            case ChannelFactory.REF8:  // 参考通道8
            {
                RefChannel ref=ChannelFactory.getRefChannel(chIdx);  // 获取参考通道
                double curPos=(val/ ref.getVerticalPerPix());  // 值除以每像素电压值得到偏移像素
                double chPos= ( GlobalVar.get().getMainWave().y/2- WaveManage.get().getPositionY(TChan.toUiChNo(chIdx)));  // 通道零点相对屏幕中心的偏移
                double pix=GlobalVar.get().getMainWave().y/2-curPos-chPos;  // 最终像素位置
//                Log.d("Tag.Debug", String.format("Tools.ScaleToPix: curPos:%s, chPos:%s ,pix:%s",curPos,chPos,pix ));
                return pix;  // 返回像素位置
            }
            default:  //S1,S2 就保存原来不变
                return 0;  // 其他通道返回0

        }
    }

    /**
     * 将时间值转换为屏幕像素位置（按通道类型分派）
     *
     * @param chIdx 通道索引
     * @param val   时间值（秒）
     * @return 对应的屏幕X轴像素位置
     */
    public static long TimebaseToPix(int chIdx,double val)
    {
        switch (chIdx){  // 根据通道类型分别处理
            case ChannelFactory.CH1:  // 物理通道1
            case ChannelFactory.CH2:  // 物理通道2
            case ChannelFactory.CH3:  // 物理通道3
            case ChannelFactory.CH4:  // 物理通道4
            case ChannelFactory.CH5:  // 物理通道5
            case ChannelFactory.CH6:  // 物理通道6
            case ChannelFactory.CH7:  // 物理通道7
            case ChannelFactory.CH8:  // 物理通道8
            case ChannelFactory.S1:  // 串行通道1
            case ChannelFactory.S2:  // 串行通道2
            case ChannelFactory.S3:  // 串行通道3
            case ChannelFactory.S4:  // 串行通道4
            {
                return TimebaseToPix(val);  // 普通通道直接使用标准时基转像素
            }
            case ChannelFactory.MATH1:  // 数学通道1
            case ChannelFactory.MATH2:  // 数学通道2
            case ChannelFactory.MATH3:  // 数学通道3
            case ChannelFactory.MATH4:  // 数学通道4
            case ChannelFactory.MATH5:  // 数学通道5
            case ChannelFactory.MATH6:  // 数学通道6
            case ChannelFactory.MATH7:  // 数学通道7
            case ChannelFactory.MATH8:  // 数学通道8
            {
                MathChannel math=ChannelFactory.getMathChannel(chIdx);  // 获取数学通道
                if  (math.getMathType()== MathWave.MATH_FFTWAVE){  // FFT类型数学通道
                    long pix= math.getHorizontalAxisMathFFT().SCPIQueryPixInScreenFromTImePosVal(val);  // FFT水平轴查询像素位置
                    long dd= math.getHorizontalAxisMathFFT().getXPosOfView();  // 获取FFT视图偏移
                    long pos=GlobalVar.get().getMainWave().x/2+pix-dd;  // 计算屏幕像素位置
                    return pos;  // 返回FFT通道像素位置
                }else {  // 非FFT数学通道
                    return TimebaseToPix(val);  // 使用标准时基转像素
                }
            }
            case ChannelFactory.REF1:  // 参考通道1
            case ChannelFactory.REF2:  // 参考通道2
            case ChannelFactory.REF3:  // 参考通道3
            case ChannelFactory.REF4:  // 参考通道4
            case ChannelFactory.REF5:  // 参考通道5
            case ChannelFactory.REF6:  // 参考通道6
            case ChannelFactory.REF7:  // 参考通道7
            case ChannelFactory.REF8:  // 参考通道8
            default:  // 默认
            {
                RefChannel ref= ChannelFactory.getRefChannel(chIdx);  // 获取参考通道
//                if (ref.getRefType()==2){ //fft
////                    Log.d("Tag.Debug", String.format("Tools.TimebaseToPix: %s,%s",val,ref.getRefTimePerPix() ));
//                    long pix=(long)( val/ ref.getRefTimePerPix());
////                    long tt= ref.getRefMovPix();
//                    long dd= ref.getTimePoseOfViewPix();
//                    long pos=GlobalVar.get().getMainWave().x/2+pix-dd;
//                    return pos;
//                }else {
                if(ref!=null){  // 参考通道非空
                    long pix=(long)Math.round(val/ref.getRefTimePerPix());  // 时间值除以每像素时间值得到偏移像素
                    long tt=ref.getTimePoseOfViewPix();  // 获取参考通道视图偏移
//                    Log.d("Tag.Debug", String.format("Tools.TimebaseToPix: %s,%s",pix,tt ));
                    long pos= GlobalVar.get().getMainWave().x/2+pix-tt;  // 计算屏幕像素位置
                    return pos;  // 返回参考通道像素位置
                }else {  // 参考通道为空
                    return 0;  // 返回0
                }

//                }
            }
        }

    }

    /**
     * 将时间值转换为屏幕像素位置（标准通道）
     *
     * @param val 时间值（秒）
     * @return 对应的屏幕X轴像素位置
     */
    public static long TimebaseToPix(double val){
        int WPIID=1;  // 波形位置ID，默认1（缩放模式）
        if (Command.get().getDisplay().ZoomQ()==false){  // 非缩放模式
            WPIID=HorizontalAxis.WPI_STANDARD;  // 使用标准波形位置ID
        }
        long l=HorizontalAxis.TimebaseToPix(WPIID,val);  // 水平轴将时间转换为像素偏移
        long dd= HorizontalAxis.getInstance().getTimePoseOfViewPix();  // 获取当前视图时间偏移像素
        //逻辑通道，中心展开
//        int verBase= CacheUtil.get().getInt(CacheUtil.TOP_SLIP_DISPLAY_COMMON_HORREF);
//        if (verBase==0 ){
//            dd=0;
//        }
        l=GlobalVar.get().getMainWave().x/2+l-dd;  // 屏幕中心 + 时间偏移 - 视图偏移
//        Logger.i(Command.TAG,"TimebaseToPix pix:"+l+",time:"+val);
        return l;  // 返回像素位置
    }

    /**
     * 将屏幕像素偏移转换为时间值
     *
     * @param offsetX 屏幕X轴像素偏移
     * @return 对应的时间值（秒）
     */
    public static double PixToTimebase(long offsetX){
        offsetX=GlobalVar.get().getMainWave().x/2-offsetX;  // 将屏幕坐标转换为以中心为原点的偏移
        int WPIID=1;  // 波形位置ID，默认1（缩放模式）
        if (Command.get().getDisplay().ZoomQ()==false){  // 非缩放模式
            WPIID=HorizontalAxis.WPI_STANDARD;  // 使用标准波形位置ID
        }
        double d=HorizontalAxis.PixToTimebase(WPIID,offsetX);  // 水平轴将像素偏移转换为时间值
//        Logger.i(Command.TAG,"PixToTimebase val:"+d+",pix:"+offsetX+",isYT:"+(WPIID==0));

        return d;  // 返回时间值
    }



    /**
     * 返回阈值电平值
     *
     * @param ch   当前通道
     * @param pxY  当前的像素位置
     * @param dang 档位
     * @return 返回阈值电平值
     */
    public static int getChannelDiscreetVoltageLevel(int ch, long pxY, int dang) {
        double chEvery = ChannelFactory.getDynamicChannel(ch - 1).getAdPix();  // 获取通道每AD码对应的像素数
        int Level = (int) ((127 - pxY) / chEvery);  // 计算离散电平值（127为AD中值）
        Logger.i(TAG, "discreetVoltageLevel:" + Level + "=ChEvery:" + chEvery + "/pxY:" + pxY);  // 打印调试日志
        return Level;  // 返回离散电平值
    }

    /**
     * 比较两个字节数组的前len个字节是否相等
     *
     * @param data1 第一个字节数组
     * @param data2 第二个字节数组
     * @param len   比较的字节数
     * @return true表示前len个字节全部相等
     */
    public static boolean memcmp(byte[] data1, byte[] data2, int len) {
        if (data1 == null && data2 == null) {  // 两个数组都为空
            return true;  // 视为相等
        }
        if (data1 == null || data2 == null) {  // 其中一个为空
            return false;  // 不相等
        }
        if (data1 == data2) {  // 同一引用
            return true;  // 相等
        }

        boolean bEquals = true;  // 默认相等
        int i;  // 循环变量
        for (i = 0; i < data1.length && i < data2.length && i < len; i++) {  // 逐字节比较
            if (data1[i] != data2[i]) {  // 发现不等字节
                bEquals = false;  // 标记不等
                break;  // 跳出循环
            }
        }

        return bEquals;  // 返回比较结果
    }


    /**
     * 是否是慢时基
     * 不管当前，只根据模拟通道判断
     */
    public static boolean isSlowTimeBase() {
        String timeBaseScale;  // 时基档位字符串
//        if (ChannelFactory.REF1 == ChannelFactory.getChActivate()
//                || ChannelFactory.REF2 == ChannelFactory.getChActivate()
//                || ChannelFactory.REF3 == ChannelFactory.getChActivate()
//                || ChannelFactory.REF4 == ChannelFactory.getChActivate()
//                || ChannelFactory.REF5 == ChannelFactory.getChActivate()
//                || ChannelFactory.REF6 == ChannelFactory.getChActivate()
//                || ChannelFactory.REF7 == ChannelFactory.getChActivate()
//                || ChannelFactory.REF8 == ChannelFactory.getChActivate()
//                || ChannelFactory.isMath_FFT_Ch(ChannelFactory.getChActivate())
//        ) {
//            return false;
//        } else {
            timeBaseScale = CacheUtil.get().getString(CacheUtil.MAIN_BOTTOM_TIMEBASE_NORMAL_SCALE);  // 从缓存获取当前时基档位
            //大于等于200ms时，为慢时基状态
            return TBookUtil.getPsFromTime(timeBaseScale) / 1000 >= 100 * 1000 * 1000;  // 时基>=200ms(100*1000*1000ns)为慢时基
//        }
    }

    /**
     * 判断指定时基档位是否为慢时基
     *
     * @param timeBaseScale 时基档位字符串
     * @return true表示慢时基（>=200ms）
     */
    public static boolean isSlowTimeBase(String timeBaseScale) {
        return TBookUtil.getPsFromTime(timeBaseScale) / 1000 >= 100 * 1000 * 1000;  // 时基>=200ms为慢时基
    }

    /**
     * 判断当前是否为差值档位（采样率较高时的精细时基）
     *
     * @return true表示差值档位
     */
    public static boolean isChaZhiDang() {

        Scope scope  = Scope.getInstance();  // 获取示波器实例
        int cnt = scope.getChannelSampOnCnt(true);  // 获取当前采样通道数
        String timeBaseScale = CacheUtil.get().getString(CacheUtil.MAIN_BOTTOM_TIMEBASE_NORMAL_SCALE);  // 获取时基档位
        long timeBaseNs = TBookUtil.getPsFromTime(timeBaseScale) / 1000;  // 转换为纳秒
        if(cnt < 8){  // 采样通道数小于8
            return timeBaseNs <= 10;  // 时基<=10ns为差值档位
        }else{  // 采样通道数>=8
            return timeBaseNs <= 20;  // 时基<=20ns为差值档位
        }
    }

    /**
     * 打印当前调用位置的行号信息（调试用）
     */
    public static void __printLine() {
        StackTraceElement[] trace = new Throwable().getStackTrace();  // 获取调用栈
        // 下标为0的元素是上一行语句的信息, 下标为1的才是调用printLine的地方的信息
        StackTraceElement tmp = trace[1];  // 获取调用者栈帧
        System.out.println(tmp.getClassName() + "." + tmp.getMethodName()  // 打印类名.方法名
                + "(" + tmp.getFileName() + ":" + tmp.getLineNumber() + ")");  // 打印(文件名:行号)
    }

    /**
     * 保存位图到Pictures目录（不覆盖已有文件）
     *
     * @param bitmap  待保存的位图
     * @param name    文件名
     * @param context 上下文
     * @return true表示保存成功
     */
    public static boolean saveImg(Bitmap bitmap, String name, Context context) {
        try {
            String sdcardPath = System.getenv("EXTERNAL_STORAGE");      //获得sd卡路径
            String dir = sdcardPath + "/Pictures/";                    //图片保存的文件夹名
            File file = new File(dir);                                 //已File来构建
            if (!file.exists()) {                                     //如果不存在  就mkdirs()创建此文件夹
                file.mkdirs();  // 创建目录
            }
            //Log.i("SaveImg", "file uri==>" + dir);
            File mFile = new File(dir + name);                        //将要保存的图片文件
            if (mFile.exists()) {  // 文件已存在
                //Toast.makeText(context, "该图片已存在!", Toast.LENGTH_SHORT).show();
                return false;  // 不覆盖，返回失败
            }

            FileOutputStream outputStream = new FileOutputStream(mFile);     //构建输出流
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);  //compress到输出outputStream
            Uri uri = Uri.fromFile(mFile);                                  //获得图片的uri
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri)); //发送广播通知更新图库，这样系统图库可以找到这张图片
            return true;  // 保存成功

        } catch (FileNotFoundException e) {
            e.printStackTrace();  // 打印异常堆栈
        }
        return false;  // 保存失败
    }

    /**
     * 保存位图到Pictures目录（覆盖已有文件，PNG格式）
     *
     * @param bitmap  待保存的位图
     * @param name    文件名
     * @param context 上下文
     * @return true表示保存成功
     */
    public static boolean saveImgOverlap(Bitmap bitmap, String name, Context context) {
        try {
            String sdcardPath = System.getenv("EXTERNAL_STORAGE");      //获得sd卡路径
            String dir = sdcardPath + "/Pictures/";                    //图片保存的文件夹名
            File file = new File(dir);                                 //已File来构建
            if (!file.exists()) {                                     //如果不存在  就mkdirs()创建此文件夹
                file.mkdirs();  // 创建目录
            }
            //Log.i("SaveImg", "file uri==>" + dir);
            File mFile = new File(dir + name);                        //将要保存的图片文件
            if (mFile.exists()) {  // 文件已存在
                //Toast.makeText(context, "该图片已存在!", Toast.LENGTH_SHORT).show();
                mFile.delete();  // 删除旧文件
                //return false;
            }

            FileOutputStream outputStream = new FileOutputStream(mFile);     //构建输出流
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);  //compress到输出outputStream

            try {
                outputStream.flush();  // 刷新输出流
                outputStream.close();  // 关闭输出流
            } catch (IOException e) {
                e.printStackTrace();  // 打印IO异常堆栈
            }

            Uri uri = Uri.fromFile(mFile);                                  //获得图片的uri
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri)); //发送广播通知更新图库，这样系统图库可以找到这张图片
            Memory.Sync();  // 同步内存，确保数据写入存储
            return true;  // 保存成功

        } catch (FileNotFoundException e) {
            e.printStackTrace();  // 打印异常堆栈
        }
        return false;  // 保存失败
    }

    /**
     * 根据当前日期时间生成名称字符串
     *
     * @return 格式化的日期时间字符串
     */
    public static String genNameByDateTime() {
        SimpleDateFormat sTimeFormat = new SimpleDateFormat("yyyy-MM-dd  hh:mm:ss");  // 日期时间格式化器
        String date = sTimeFormat.format(new Date());  // 格式化当前时间
        return date;  // 返回格式化后的日期时间字符串
    }

    /**
     * 记录开始时间戳
     */
    public static void beginTime() {
        beginTime = System.currentTimeMillis();  // 记录当前时间戳
    }

    /**
     * 计算从beginTime到当前的耗时
     *
     * @return 耗时毫秒数
     */
    public static long endTime() {
        endTime = System.currentTimeMillis();  // 记录结束时间戳
        return endTime - beginTime;  // 返回耗时差值
    }

    /**
     * 回调接口，用于计时包装执行
     */
    public interface CallBack {
        void doSomething();  // 执行某操作的回调方法
    }

    /**
     * 计时包装执行，测量回调方法耗时
     *
     * @param callBack 待执行的回调
     */
    @SuppressLint("DefaultLocale")
    public static void countTime(CallBack callBack) {
        long beginTime = System.currentTimeMillis();  // 记录开始时间
        callBack.doSomething();  // 执行回调方法
        long endTime = System.currentTimeMillis();  // 记录结束时间
        Logger.i(TAG, String.format("耗时：%d ms", endTime - beginTime));  // 打印耗时日志
    }


    //判断文件是否存在
    /**
     * 判断指定路径的文件是否存在
     *
     * @param strFile 文件路径
     * @return true表示文件存在
     */
    public static boolean fileIsExists(String strFile) {
        try {
            File f = new File(strFile);  // 创建文件对象
            if (!f.exists()) {  // 文件不存在
                return false;  // 返回false
            }
        } catch (Exception e) {  // 捕获异常
            return false;  // 异常时返回false
        }
        return true;  // 文件存在返回true
    }

    /**
     * 将整数转换为ASCII字符，不可显示字符替换为指定字符串
     *
     * @param num     整数值
     * @param replace 不可显示时的替换字符串
     * @return ASCII字符或替换字符串
     */
    public static String getASCIIFromInt(int num, String replace) {
        char s1 = ((num >= 32 && num <= 126) || (num >= 0xA1 & num <= 0xFF && num != 0xAD)) ? (char) num : '\0';  // 判断是否为可显示ASCII字符
        if (s1 == '\0') {  // 不可显示字符
            return replace;  // 返回替换字符串
        } else {  // 可显示字符
            return String.valueOf(s1);  // 返回对应字符
        }
    }

    /**
     * 深度复制List（通过序列化/反序列化实现）
     *
     * @param src 源列表
     * @param <T> 列表元素类型，必须实现Serializable
     * @return 深拷贝后的新列表
     * @throws IOException            IO异常
     * @throws ClassNotFoundException 类未找到异常
     */
    public static <T> List<T> deepCopy(List<T> src) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();  // 创建字节数组输出流
        ObjectOutputStream out = new ObjectOutputStream(byteOut);  // 创建对象输出流
        out.writeObject(src);  // 将源列表序列化

        ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());  // 从字节数组创建输入流
        ObjectInputStream in = new ObjectInputStream(byteIn);  // 创建对象输入流
        @SuppressWarnings("unchecked")
        List<T> dest = (List<T>) in.readObject();  // 反序列化为新列表
        return dest;  // 返回深拷贝列表
    }

    /**
     * 深度复制单个对象（通过序列化/反序列化实现）
     *
     * @param object 源对象
     * @param <T>    对象类型，必须实现Serializable
     * @return 深拷贝后的新对象
     * @throws IOException            IO异常
     * @throws ClassNotFoundException 类未找到异常
     */
    public static <T> T deepCopy(T object) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();  // 创建字节数组输出流
        ObjectOutputStream oos = new ObjectOutputStream(bos);  // 创建对象输出流
        oos.writeObject(object);  // 将对象序列化
        oos.flush();  // 刷新输出流
        oos.close();  // 关闭对象输出流
        bos.close();  // 关闭字节数组输出流
        byte[] byteArray = bos.toByteArray();  // 获取序列化字节数组
        ByteArrayInputStream bis = new ByteArrayInputStream(byteArray);  // 从字节数组创建输入流
        ObjectInputStream ois = new ObjectInputStream(bis);  // 创建对象输入流
        return (T) ois.readObject();  // 反序列化并返回
    }


    /**
     * 通过JSON序列化/反序列化将List转换为ArrayList（实现深拷贝）
     *
     * @param sourceList 源列表
     * @param clazz      目标元素类型
     * @param <T>        元素类型
     * @return 深拷贝后的ArrayList
     */
    public static <T> ArrayList<T> jsonToArrayList(List<T> sourceList, Class<T> clazz) {
        Gson gson = new Gson();  // 创建Gson实例
        String json = gson.toJson(sourceList);  // 将源列表序列化为JSON字符串

        Type type = new TypeToken<ArrayList<JsonObject>>() {  // 获取ArrayList<JsonObject>的类型令牌
        }.getType();
        ArrayList<JsonObject> jsonObjects = new Gson().fromJson(json, type);  // 反序列化为JsonObject列表

        ArrayList<T> arrayList = new ArrayList<>();  // 创建目标ArrayList
        for (JsonObject jsonObject : jsonObjects) {  // 遍历每个JsonObject
            arrayList.add(new Gson().fromJson(jsonObject, clazz));  // 逐个反序列化为目标类型
        }
        return arrayList;  // 返回转换后的ArrayList
    }

    private static String[] Hexs = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};  // 十六进制字符查找表
    private static StringBuilder hex = new StringBuilder();  // 复用的字符串构建器

    /**
     * 将单个字节转换为两位十六进制字符串（线程安全）
     *
     * @param b 待转换的字节
     * @return 两位十六进制字符串，如"FF"
     */
    public static synchronized String ByteToHexString(byte b) {
        hex.delete(0, hex.length());  // 清空构建器
        int i32 = b & 0x00FF;  // 取低8位，避免符号位扩展
        hex.append(Hexs[i32 / 16]);  // 追加高4位对应的十六进制字符
        hex.append(Hexs[i32 % 16]);  // 追加低4位对应的十六进制字符
        return hex.toString();  // 返回十六进制字符串
    }

    /**
     * 将单个字节转换为十六进制字符串（可指定位数）
     *
     * @param b   待转换的字节
     * @param bit 位数（>4显示两位，否则显示一位）
     * @return 十六进制字符串
     */
    public static String ByteToHexString(byte b, int bit) {
        StringBuilder hex = new StringBuilder();  // 创建局部字符串构建器
        int i32 = b & 0x00FF;  // 取低8位
        if (bit > 4) hex.append(Hexs[i32 / 16]);  // 位数>4时追加高位
        hex.append(Hexs[i32 % 16]);  // 追加低位
        return hex.toString();  // 返回十六进制字符串
    }

    /**
     * 将十六进制字符串转换为整数
     *
     * @param hexString 十六进制字符串
     * @return 对应的整数值，转换失败返回0
     */
    public static int HexStringToInt(String hexString){
        int result=0;  // 初始化结果
        try {
            result=Integer.parseInt(hexString.trim(),16);  // 以16进制解析字符串
        }catch (Exception e){
            e.printStackTrace();  // 打印异常堆栈
        }
        return result;  // 返回解析结果
    }

    /**
     * 将十六进制字符串转换为长整数
     *
     * @param hexString 十六进制字符串
     * @return 对应的长整数值，转换失败返回0
     */
    public static long HexStringToLong(String hexString){
        long result=0;  // 初始化结果
        try {
            result=Long.parseLong(hexString.trim(),16);  // 以16进制解析字符串
        }catch (Exception e){
            e.printStackTrace();  // 打印异常堆栈
        }
        return result;  // 返回解析结果
    }

    /**
     * 追加内容到文件末尾
     *
     * @param pathName 文件路径
     * @param content  待追加的内容
     */
    public static void AppendContentToFile(String pathName, String content) {
//        if (isDebug) return;

        FileWriter writer = null;  // 文件写入器
        try {
            // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
            writer = new FileWriter(pathName, true);  // 以追加模式打开文件
            writer.write(content + "\n");  // 写入内容并换行
        } catch (IOException e) {
            e.printStackTrace();  // 打印IO异常堆栈
        } finally {
            try {
                if (writer != null) {  // 写入器非空
                    writer.close();  // 关闭写入器
                }
            } catch (IOException e) {
                e.printStackTrace();  // 打印关闭异常堆栈
            }
        }
    }

    /**
     * 将StringBuilder内容保存到文件（追加模式）
     *
     * @param pathName 文件路径
     * @param sb       待保存的StringBuilder
     */
    public static void saveStringBuild(String pathName, StringBuilder sb) {
//        FileOutputStream fos = null;
//        try {
//            fos = new FileOutputStream(pathName);
//            fos.write(sb.toString().getBytes());
//            fos.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(pathName, true));  // 以追加模式创建缓冲写入器
            bw.write(sb.toString());  // 写入StringBuilder内容
            bw.flush();  // 刷新缓冲
            bw.close();  // 关闭写入器
            Memory.Sync();  // 同步内存
        } catch (IOException e) {
            e.printStackTrace();  // 打印IO异常堆栈
        }
    }

    public static Map<String, String> mapUdisk = new HashMap<>();  // U盘挂载路径映射表

    /**
     * 检查是否有U盘插入
     *
     * @return true表示有U盘
     */
    public static boolean checkUdiskState() {
        return mapUdisk.size() > 0;  // 映射表非空表示有U盘
    }

    /**
     * 判断是否存在U盘（通过StorageManager检测）
     *
     * @param context 上下文
     * @return true表示存在U盘
     */
    public static boolean isExistUDisk(Context context){
//        Log.d("Tag.Debug", String.format("isExistUDisk: %b", Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)));
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){  // 外部存储已挂载
            StorageManager storageManager=(StorageManager)context.getSystemService(Context.STORAGE_SERVICE);  // 获取存储管理器
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {  // Android 7.0及以上
                List<StorageVolume> storageVolumes= storageManager.getStorageVolumes();  // 获取所有存储卷
                for(StorageVolume volume :storageVolumes){  // 遍历存储卷
//                    Log.d("Tag.Debug", String.format("isExistUDisk: %s",volume.toString() ));
                    if (!volume.isEmulated() && !volume.isPrimary() ){  // 非模拟且非主存储（即为U盘）
                        return true;  // 存在U盘
                    }
                }
            }
        }else {  // 外部存储未挂载
            return false;  // 不存在U盘
        }
        return false;  // 默认不存在
    }

    /**
     * 获取U盘挂载路径（Android 11+）
     *
     * @param context 上下文
     * @return U盘路径，无U盘时返回外部存储根路径
     */
    @RequiresApi(api = Build.VERSION_CODES.R)
    public static String getUDiskPath(Context context){
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){  // 外部存储已挂载
            StorageManager storageManager=(StorageManager)context.getSystemService(Context.STORAGE_SERVICE);  // 获取存储管理器
                List<StorageVolume> storageVolumes= storageManager.getStorageVolumes();  // 获取所有存储卷
                for(StorageVolume volume :storageVolumes){  // 遍历存储卷
                    if (!volume.isEmulated() && !volume.isPrimary() ){  // 非模拟且非主存储
                        String path=volume.getDirectory().getAbsolutePath();  // 获取U盘绝对路径
//                        Log.d("Tag.Debug", String.format("getUDiskPath: %s",path ));
                        return path;  // 返回U盘路径
                    }
                }

        }
        return Environment.getExternalStorageDirectory().getAbsolutePath();  // 无U盘返回外部存储根路径
    }


    /**
     * 获取U盘路径（从映射表中取第一个）
     *
     * @return U盘路径，无U盘时返回null
     */
    public static String getUdiskPath() {
        String udiskPath = null;  // 初始化路径为空
        for (String upath : mapUdisk.values()) {  // 遍历U盘映射表
            udiskPath = upath;  // 取第一个路径
            break;  // 跳出循环
        }
        return udiskPath;  // 返回U盘路径
    }

    /**
     * 获取YT模式下通道的垂直位置（UI像素坐标）
     *
     * @param chIdx 通道索引（UI编号）
     * @return 通道垂直位置（像素）
     */
    public static double getYTChannelPositionUI(int chIdx) {
        double y = 0;  // 初始化位置
        if (TChan.isChan(chIdx)) {  // 模拟通道
            y = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_CH_Y_POSITION + chIdx);  // 从缓存获取通道Y位置
        } else if (TChan.isMath(chIdx)) {  // 数学通道
            int mathType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_TYPE + chIdx);  // 获取数学运算类型
            if (mathType == CacheUtil.MATHTYPE_DW) {  // 数字滤波类型
                y = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_MATH_DW_Y_POSITION + chIdx);  // 获取DW类型Y位置
            } else if (mathType == CacheUtil.MATHTYPE_AXB) {  // A×B运算类型
                y = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_MATH_AXB_Y_POSITION + chIdx);  // 获取AXB类型Y位置
            } else if (mathType == CacheUtil.MATHTYPE_AM) {  // 算术运算类型
                y = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_MATH_AM_Y_POSITION + chIdx);  // 获取AM类型Y位置
            } else {  // FFT类型
                if (CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_FFT_TYPE_ID + chIdx) == 1) {  // FFT dB类型
                    y = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_MATH_FFT_DB_Y_POSITION + chIdx);  // 获取FFT dB Y位置
                } else {  // FFT RMS类型
                    y = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_MATH_FFT_RMS_Y_POSITION + chIdx);  // 获取FFT RMS Y位置
                }
            }
        } else if (TChan.isRef(chIdx)) {  // 参考通道
            y = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_REF_Y_POSITION + chIdx);  // 获取参考通道Y位置
        }
        return y;  // 返回通道垂直位置
    }

    /**
     * 获取缩放模式下通道的垂直位置
     *
     * @param chIdx 通道索引
     * @return 缩放模式下的通道垂直位置
     */
    public static double getZoomChannelPositionUI(int chIdx) {
        return ScopeBase.changeAccuracy(getYTChannelPositionUI(chIdx) * ScopeBase.getZoomHeight() / ScopeBase.getHeight());  // YT位置按缩放比例换算
    }

    // 250 到 -250 坐标系
    /**
     * 获取YT模式下通道的垂直位置（250到-250坐标系）
     *
     * @param chIdx 通道索引
     * @return 转换后的垂直位置
     */
    public static double getYTChannelPosition(int chIdx) {
        return ScopeBase.getNewHeight() * 1.0 / 2 - getYTChannelPositionUI(chIdx);  // 从UI坐标转换到250坐标系
    }

    //
    /**
     * 获取当前模式（YT/Zoom）下的通道垂直位置
     *
     * @param chIdx 通道索引
     * @return 通道垂直位置（像素）
     */
    public static double getChannelPositionUI(int chIdx) {
        return YT2Zoom(getYTChannelPositionUI(chIdx));  // YT坐标转换为Zoom坐标
    }

    /**
     * 判断当前是否处于缩放模式
     *
     * @return true表示缩放模式
     */
    public static boolean isZoom() {
        return CacheUtil.get().getBoolean(CacheUtil.MAIN_BOTTOM_SLIP_ZOOM);  // 从缓存获取缩放状态
    }

    /**
     * 将缩放模式坐标转换为YT模式坐标
     *
     * @param y 缩放模式下的Y坐标
     * @return YT模式下的Y坐标
     */
    public static double Zoom2YT(double y) {

        if (isZoom()) {  // 缩放模式
            y = ScopeBase.changeAccuracy(y * ScopeBase.getHeight() / ScopeBase.getZoomHeight());  // 按比例转换
        }
        return y;  // 返回YT坐标
    }

    /**
     * 将YT模式坐标转换为缩放模式坐标
     *
     * @param y YT模式下的Y坐标
     * @return 缩放模式下的Y坐标
     */
    public static double YT2Zoom(double y) {
        if (isZoom()) {  // 缩放模式
            y = ScopeBase.changeAccuracy(y * ScopeBase.getZoomHeight() / ScopeBase.getHeight());  // 按比例转换
        }
        return y;  // 返回Zoom坐标
    }

    //修改通道位置 , 转换到YT坐标 UI pos (0 - height)
    /**
     * 修改通道位置（自动处理YT/Zoom坐标转换）
     *
     * @param chIdx 通道索引
     * @param pos   新位置（当前模式坐标）
     */
    public static void putChannelPosition(int chIdx, double pos) {
        //保存的是 屏幕上像素位置
        if (getChannelPositionUI(chIdx) != pos) {  // 位置发生变化
            putYTChannelPosition(chIdx, Zoom2YT(pos));  // 转换为YT坐标后保存
        }
    }

    //UI 坐标
    /**
     * 保存YT模式下的通道垂直位置到缓存
     *
     * @param chIdx 通道索引
     * @param pos   YT模式下的垂直位置
     */
    public static void putYTChannelPosition(int chIdx, double pos) {
        if (TChan.isChan(chIdx)) {  // 模拟通道
            CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_CH_Y_POSITION + chIdx, String.valueOf(pos));  // 保存通道Y位置
        } else if (TChan.isMath(chIdx)) {  // 数学通道
            int mathType = CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_TYPE + chIdx);  // 获取数学运算类型
            if (mathType == CacheUtil.MATHTYPE_DW) {  // 数字滤波类型
                CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_MATH_DW_Y_POSITION + chIdx, String.valueOf(pos));  // 保存DW类型Y位置
            } else if (mathType == CacheUtil.MATHTYPE_AXB) {  // A×B运算类型
                CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_MATH_AXB_Y_POSITION + chIdx, String.valueOf(pos));  // 保存AXB类型Y位置
            } else if (mathType == CacheUtil.MATHTYPE_AM) {  // 算术运算类型
                CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_MATH_AM_Y_POSITION + chIdx, String.valueOf(pos));  // 保存AM类型Y位置
            } else {  // FFT类型
                if (CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_FFT_TYPE_ID + chIdx) == 1) {  // FFT dB类型
                    CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_MATH_FFT_DB_Y_POSITION + chIdx, String.valueOf(pos));  // 保存FFT dB Y位置
                } else {  // FFT RMS类型
                    CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_MATH_FFT_RMS_Y_POSITION + chIdx, String.valueOf(pos));  // 保存FFT RMS Y位置
                }
            }
        } else if (TChan.isRef(chIdx)) {  // 参考通道
            CacheUtil.get().putMap(CacheUtil.MAIN_WAVE_REF_Y_POSITION + chIdx, String.valueOf(pos));  // 保存参考通道Y位置
        }
    }


    //YT模式触发电平
    /**
     * 获取YT模式触发电平的缓存值（转换为UI像素坐标）
     *
     * @param key 缓存键
     * @return UI像素坐标值
     */
    public static double getYTLevelCache(String key) {
        return ScopeBase.changeAccuracy(CacheUtil.get().getDouble(key) * ScopeBase.getToUICoff());  // 缓存值×UI系数并精度修正
    }

    /**
     * 获得电平的cache值（根据工作模式自动调整）
     *
     * @param key 缓存键
     * @return 电平像素值
     */
    public static double getLevelCache(String key) {
        if (WorkModeManage.getInstance().getmWorkMode() == IWorkMode.WorkMode_YTZOOM) {  // YT缩放模式
            return getYTLevelCache(key) * 3 / 4;  // 缩放模式下电平值按3/4缩放
        } else {  // 普通YT模式
            return getYTLevelCache(key) ;  // 直接返回YT电平值
        }
    }

    /**
     * 转换成外部触发对应的像素位置 底部0---顶部1000
     *
     * @param pos 原始像素位置
     * @return 转换后的像素位置
     */
    private static double transFormTriggerInput(double pos) {
        double temp = pos;  // 保存原始位置
        if (WorkModeManage.getInstance().getmWorkMode() == IWorkMode.WorkMode_YTZOOM) {  // YT缩放模式
            temp = Math.round(ScopeBase.getNewZoomHeight() - temp * GlobalVar.get().toZoomCoef());  // 缩放模式下翻转并缩放
        } else {  // 普通YT模式
            temp = ScopeBase.getNewHeight() - temp;  // 翻转坐标（底部0→顶部最大）
        }
        return temp;  // 返回转换后的位置
    }

    /**
     * 获取当前外部触发的幅度值
     *
     * @param key 缓存键
     * @return 外部触发幅度值（伏特）
     */
    public static double getExtTriggerValCache(String key) {
        double temp = transFormTriggerInput(Tools.getYTLevelCache(key));  // 转换触发电平像素位置
        if (WorkModeManage.getInstance().getmWorkMode() == IWorkMode.WorkMode_YTZOOM) {  // YT缩放模式
            return 5.0f * temp / ScopeBase.getNewZoomHeight();  // 缩放模式下按5V/缩放高度计算
        } else {  // 普通YT模式
            return 5.0f * temp / ScopeBase.getNewHeight();  // 按5V/高度计算
        }
    }

    /**
     * 保存电平的cache值（根据工作模式自动调整系数）
     *
     * @param key   缓存键
     * @param value 电平像素值
     */
    public static void putLevelCache(String key, double value) {
        if (getLevelCache(key) != value) {  // 值发生变化才保存
            if (WorkModeManage.getInstance().getmWorkMode() == IWorkMode.WorkMode_YTZOOM) {  // YT缩放模式
                CacheUtil.get().putMap(key, String.valueOf(((value * 4 / 3) * ScopeBase.getToFPGACoff())));  // 缩放模式：先反缩放(×4/3)再转FPGA系数
            } else {  // 普通YT模式
                CacheUtil.get().putMap(key, String.valueOf((value * ScopeBase.getToFPGACoff())));  // 直接转FPGA系数
            }
        }


    }


    /**
     * 获取通道可移动范围（从屏幕中心向上/下可移动的像素数）
     *
     * @param ch IWave.CH1 - IWave.CH4
     * @return 返回该通道当前的可滑动的上下限。从屏幕正中间开始向上或向下可移动的像素px值
     */
    public static int getChRange(int ch) {
        Channel channel = ChannelFactory.getDynamicChannel(ch - TChan.Ch1 + ChannelFactory.CH1);  // 获取动态通道对象
        if(channel != null){  // 通道非空
            return Scope.vSpanOfView(channel.getResistanceType(),channel.getVScaleVal()/channel.getProbeRate());  // 计算通道可视范围
        }
        return 0;  // 通道为空返回0
    }

    /**
     * 判断系统是否使用24小时制
     *
     * @return true表示24小时制
     */
    public static boolean is24HourFormat() {
        String value = Settings.System.getString(App.get().getContentResolver(), Settings.System.TIME_12_24);  // 从系统设置获取时间格式
        if (value == null) {  // 系统设置未指定
            Locale locale = App.get().getResources().getConfiguration().locale;  // 获取当前地区
            DateFormat natural = DateFormat.getTimeInstance(DateFormat.LONG, locale);  // 获取该地区的时间格式
            if (natural instanceof SimpleDateFormat) {  // 是SimpleDateFormat
                SimpleDateFormat sdf = (SimpleDateFormat) natural;  // 强制转换
                String pattern = sdf.toPattern();  // 获取格式模式

                if (pattern.indexOf('H') >= 0) {  // 模式包含大写H（24小时制标记）
                    value = "24";  // 设为24小时制
                } else {  // 不包含大写H
                    value = "12";  // 设为12小时制
                }
            } else {  // 非SimpleDateFormat
                value = "12";  // 默认12小时制
            }
            return value.equals("24");  // 返回是否24小时制
        }
        return value.equals("24");  // 返回是否24小时制
    }

    /**
     * 获取当前时间字符串（HH:mm格式）
     *
     * @return 格式化的时间字符串
     */
    public static String getCurTime() {
        Calendar calendar = Calendar.getInstance();  // 获取日历实例
        int hourInt = Tools.is24HourFormat()  // 判断24小时制
                ? calendar.get(Calendar.HOUR_OF_DAY) : calendar.get(Calendar.HOUR);  // 获取对应格式的小时
        int minInt = calendar.get(Calendar.MINUTE);  // 获取分钟
        String hourStr = String.valueOf(hourInt < 10 ? "0" + hourInt : hourInt);  // 小时补零
        String minStr = String.valueOf(minInt < 10 ? "0" + minInt : minInt);  // 分钟补零
        return hourStr + ":" + minStr;  // 返回HH:mm格式时间
    }

    /**
     * 打印控件位置信息（相对父布局，调试用）
     *
     * @param layoutView 待打印的ViewGroup
     */
    public static void PrintControlsLocation(ViewGroup layoutView){
        if (ExternalKeysProtocol.isPrintDebugLocation) {  // 调试位置打印开关开启
            String title="";  // 标题（空）
            new Handler().postDelayed(() -> {  // 延迟1秒执行，等待布局完成
                Log.d(TAG, String.format("============== location %s===============",title));  // 打印标题
                getViewRectAndSubView(layoutView);  // 递归打印子视图位置
                Log.d(TAG, "=================================================");  // 打印分隔线
            }, 1000);  // 延迟1000ms
        }
    }

    /**
     * 打印控件位置信息（全屏坐标，带标题）
     *
     * @param title      标题
     * @param layoutView 待打印的ViewGroup
     */
    public static void PrintControlsLocation(String title,ViewGroup layoutView){
        if (ExternalKeysProtocol.isPrintDebugLocation) {  // 调试位置打印开关开启
            new Handler().postDelayed(() -> {  // 延迟1秒执行
                Log.d(TAG, String.format("============== location %s===============",title));  // 打印标题
                getViewRectAndSubView(title, layoutView);  // 递归打印子视图位置（带标题）
                Log.d(TAG, "=================================================");  // 打印分隔线
            }, 1000);  // 延迟1000ms
        }
    }

    //父控件中的位置
    /**
     * 递归打印ViewGroup中所有子视图的位置信息（相对父布局坐标）
     *
     * @param layoutView 根ViewGroup
     */
    private static void getViewRectAndSubView(ViewGroup layoutView){
        int viewCount=layoutView.getChildCount();  // 获取子视图数量
        for(int i=0;i<viewCount;i++){  // 遍历所有子视图
            View view=layoutView.getChildAt(i);  // 获取第i个子视图

            if (view instanceof ViewGroup){  // 是ViewGroup，递归处理
//                Log.d(TAG, String.format("----------enter subview %s------------ ",view.getClass().getSimpleName()));
                getViewRectAndSubView ((ViewGroup)view);  // 递归进入子ViewGroup
            }else if (view instanceof RecyclerView){  // 是RecyclerView
                int count= ((RecyclerView)(view)).getChildCount();  // 获取RecyclerView子项数量
                for(int j=0;j<count;j++){  // 遍历RecyclerView子项
                    View v= ((RecyclerView)(view)).getChildAt(j);  // 获取子项视图
//                    Rect r= getViewRect(view);
                    Rect r=new Rect(view.getLeft(),view.getTop(),view.getRight(),view.getBottom());  // 获取视图矩形
                    if (r.width()==0 || r.height()==0)continue;  // 跳过零尺寸视图
                    String rect=String.format("(%d,%d - %d,%d)",r.left,r.top,r.width(),r.height());  // 格式化矩形信息
                    String name=view.getClass().getSimpleName();  // 获取视图类名
                    Log.d(TAG, String.format("getViewRectAndSubView: name:%s ,rect:%s",name,rect));  // 打印位置信息
                }
            }
            else{  // 普通视图
//                Rect r= getViewRect(view);
                Rect r=new Rect(view.getLeft(),view.getTop(),view.getRight(),view.getBottom());  // 获取视图矩形


                if (r.width()==0 || r.height()==0) continue;  // 跳过零尺寸视图
                String rect=String.format("%d,%d - %d,%d",(int)r.left,(int)r.top,(int)r.width(),(int)r.height());  // 格式化矩形信息
                String name=view.getClass().getSimpleName();  // 获取视图类名
                if (view instanceof TextView){  // 是TextView
                    name=((TextView) view).getText().toString();  // 使用文本内容作为名称
                }
                Log.d(TAG, String.format("getViewRectAndSubView: name:%s ,rect:%s",name,rect));  // 打印位置信息
            }

        }
    }

    /**
     * 递归打印ViewGroup中所有子视图的位置信息（全屏坐标，带标题）
     *
     * @param title      标题
     * @param layoutView 根ViewGroup
     */
    private static void getViewRectAndSubView(String title,ViewGroup layoutView){

        int viewCount=layoutView.getChildCount();  // 获取子视图数量
        for(int i=0;i<viewCount;i++){  // 遍历所有子视图
            View view=layoutView.getChildAt(i);  // 获取第i个子视图

            if (view instanceof ViewGroup){  // 是ViewGroup，递归处理
//                Log.d(TAG, String.format("----------enter subview %s------------ ",view.getClass().getSimpleName()));
                getViewRectAndSubView (title,(ViewGroup)view);  // 递归进入子ViewGroup
            }else if (view instanceof RecyclerView){  // 是RecyclerView
                int count= ((RecyclerView)(view)).getChildCount();  // 获取RecyclerView子项数量
                for(int j=0;j<count;j++){  // 遍历RecyclerView子项
                    View v= ((RecyclerView)(view)).getChildAt(j);  // 获取子项视图
                    Rect r= getViewRect(view);  // 获取全屏坐标矩形
                    if (r.width()==0 || r.height()==0)continue;  // 跳过零尺寸视图
                    String rect=String.format("(%d,%d - %d,%d)",r.left,r.top,r.width(),r.height());  // 格式化矩形信息
                    String name=view.getClass().getSimpleName();  // 获取视图类名
                    Log.d(TAG, String.format("getViewRectAndSubView: name:%s ,rect:%s",name,rect));  // 打印位置信息
                }
            }
            else{  // 普通视图
                Rect r= getViewRect(view);  // 获取全屏坐标矩形
                if (r.width()==0 || r.height()==0) continue;  // 跳过零尺寸视图
                String rect=String.format("%d,%d - %d,%d",r.left,r.top,r.width(),r.height());  // 格式化矩形信息
                String name=view.getClass().getSimpleName();  // 获取视图类名
                if (view instanceof TextView){  // 是TextView
                    name=((TextView) view).getText().toString();  // 使用文本内容作为名称
                }
                Log.d(TAG, String.format("getViewRectAndSubView: name:%s ,rect:%s",name,rect));  // 打印位置信息
            }

        }

    }

    /**
     * 遍历ViewGroup查找所有Button，并通过回调返回其位置和文本
     *
     * @param layoutView  根ViewGroup
     * @param OnConsumer  回调函数，接收Rect和按钮文本
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void getViewRectByButton(ViewGroup layoutView, BiConsumer<Rect,String> OnConsumer){
        int viewCount=layoutView.getChildCount();  // 获取子视图数量
        for(int i=0;i<viewCount;i++){  // 遍历所有子视图
            View view=layoutView.getChildAt(i);  // 获取第i个子视图
            if (view instanceof  ViewGroup){  // 是ViewGroup，递归处理
                getViewRectByButton((ViewGroup)view,OnConsumer);  // 递归进入子ViewGroup

            }
            else {  // 非ViewGroup

                Rect r= getViewRect(view);  // 获取全屏坐标矩形
                if (r.width()==0 || r.height()==0 || view.getVisibility()==View.GONE || view.isEnabled()==false ) continue;  // 跳过不可见/禁用/零尺寸视图
                String rect=String.format("%d,%d - %d,%d",r.left,r.top,r.width(),r.height());  // 格式化矩形信息
                String name=view.getClass().getSimpleName();  // 获取视图类名
                if (view instanceof Button){  // 是Button
                    name=((Button)view).getText().toString();  // 获取按钮文本
                    OnConsumer.accept(r,name);  // 通过回调返回按钮位置和文本
                }

            }
        }
    }

    /**
     * 获取View在屏幕上的矩形区域
     *
     * @param v 目标View
     * @return 屏幕坐标矩形
     */
    public static Rect getViewRect(View v) {
        int[] location = new int[2];  // 创建位置数组
        v.getLocationOnScreen(location);  // 获取View在屏幕上的位置
        int x = location[0];  // X坐标
        int y = location[1];  // Y坐标
        Rect rect = new Rect(x, y, x + v.getWidth(), y + v.getHeight());  // 构建屏幕坐标矩形
//        Log.d("Debug", String.format("getViewRect: %s", rect.toString() ));
        return rect;  // 返回矩形
    }



    /**
     * 判断是否启用自动量程功能
     *
     * @return true表示启用自动量程
     */
    public static boolean isEnableAutoRange() {
        boolean isSerials = CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S1) || CacheUtil.get().getBoolean(CacheUtil.MAIN_RIGHT_SERIAL + CacheUtil.S2);  // 是否有串行通道开启
        return (ScopeConfig.getConfig().isEnableAutoRange() || App.IsDebug()) && !isSerials;  // 配置启用或调试模式，且无串行通道
    }


    /**
     * 在列表中查找满足条件的元素索引
     *
     * @param list      待搜索列表
     * @param predicate 断言条件
     * @param <T>       元素类型
     * @return 满足条件的元素索引，未找到返回-1
     */
    public static < T > int indexOf(List< T > list, Predicate<? super T> predicate) {
        int idx = 0;  // 索引计数器
        for (Iterator< T > iter = list.iterator(); iter.hasNext(); idx++) {  // 遍历列表
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {  // Android 7.0+
                if (predicate.test(iter.next())) {  // 测试当前元素
                    return idx;  // 找到则返回索引
                }
            }
        }

        return -1;  // 未找到返回-1
    }

    /**
     * 在数组中查找满足条件的元素索引
     *
     * @param list      待搜索数组
     * @param predicate 断言条件
     * @param <T>       元素类型
     * @return 满足条件的元素索引，未找到返回-1
     */
    public static <T> int indexOf(T[] list,Predicate<? super T> predicate){
        for(int i=0;i<list.length;i++){  // 遍历数组
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {  // Android 7.0+
                if (predicate.test((list[i]))){  // 测试当前元素
                    return i;  // 找到则返回索引
                }
            }
        }
        return -1;  // 未找到返回-1
    }

    /**
     * 将列表中的元素从fromIndex移动到toIndex位置
     *
     * @param list      目标列表
     * @param fromIndex 原索引
     * @param toIndex   目标索引
     * @param <T>       元素类型
     */
    public static <T> void insertElement(List<T> list, int fromIndex, int toIndex) {
        if (fromIndex < 0 || fromIndex >= list.size()  // 原索引越界
                || toIndex < 0 || toIndex >= list.size()  // 目标索引越界
                || fromIndex == toIndex) {  // 索引相同
            return;  // 不操作
        }
        T element = list.remove(fromIndex);  // 移除原位置元素
        int newIndex = (fromIndex < toIndex) ? toIndex - 1 : toIndex;  // 计算新位置（移除后索引偏移修正）
        list.add(newIndex, element);  // 插入到新位置
    }

    /**
     * 交换列表中两个位置的元素
     *
     * @param list      目标列表
     * @param fromIndex 第一个索引
     * @param toIndex   第二个索引
     * @param <T>       元素类型
     */
    public static <T> void swapElement(List<T> list, int fromIndex, int toIndex) {
        if (fromIndex < 0 || fromIndex >= list.size()  // 索引越界
                || toIndex < 0 || toIndex >= list.size()  // 索引越界
                || fromIndex == toIndex) {  // 索引相同
            return;  // 不操作
        }
        T element = list.get(fromIndex);  // 保存第一个元素
        list.set(fromIndex, list.get(toIndex));  // 将第二个元素放到第一个位置
        list.set(toIndex, element);  // 将第一个元素放到第二个位置
    }


    private static final Pattern NUMBER_PATTERN=Pattern.compile("-?\\d+(\\.\\d+)?");  // 数值正则模式：可选负号+整数+可选小数
    /**
     * 判断字符串是否为数值格式
     *
     * @param s 待判断字符串
     * @return true表示是数值
     */
    public static boolean isNumeric(String s){
        return s!=null & NUMBER_PATTERN.matcher(s).matches();  // 非空且匹配数值模式
    }

    /**
     * 读取九宫格位图资源（保持原始密度，不缩放）
     *
     * @param resId 资源ID
     * @return 九宫格位图
     */
    public static Bitmap readNineBmp(int resId){
        BitmapFactory.Options options=new BitmapFactory.Options();  // 创建解码选项
        TypedValue value=new TypedValue();  // 创建类型值
        App.get().getResources().openRawResource(resId,value);  // 打开原始资源并获取密度信息
        options.inTargetDensity=value.density;  // 设置目标密度为资源原始密度
        options.inScaled=false;  // 禁止缩放
        Bitmap b= BitmapFactory.decodeResource(App.get().getResources(), resId,options );  // 解码资源为位图

       return b;  // 返回位图
    }

    /**
     * 将SVG/矢量图资源转换为位图
     *
     * @param resId 资源ID
     * @return 转换后的位图
     */
    public static Bitmap readSvgBmp(int resId) {
        Context context = App.get().getApplicationContext();  // 获取应用上下文
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)  // Android 5.0以下
            return BitmapFactory.decodeResource(context.getResources(), resId);  // 直接解码资源
        Drawable drawable = ContextCompat.getDrawable(context, resId);  // 获取Drawable
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);  // 创建ARGB_8888位图
        Canvas canvas = new Canvas(bitmap);  // 创建画布
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());  // 设置Drawable边界
        drawable.draw(canvas);  // 将Drawable绘制到画布
        return bitmap;  // 返回位图
    }

    /**
     * 将九宫格位图绘制为指定尺寸的位图（支持旋转和透明度）
     *
     * @param bmp    九宫格源位图
     * @param width  目标宽度
     * @param height 目标高度
     * @param rotate 旋转角度
     * @param alpha  透明度（0-255）
     * @return 绘制后的位图，非九宫格返回null
     */
    public static   Bitmap getNineBmp(Bitmap bmp,int width,int height,float rotate,int alpha){

        if (NinePatch.isNinePatchChunk(bmp.getNinePatchChunk())){  // 验证是否为九宫格位图
            NinePatchDrawable ninePatchDrawable=new NinePatchDrawable(App.get().getResources(),bmp,bmp.getNinePatchChunk(),new Rect(),null);  // 创建九宫格Drawable
            if (width==0){  // 宽度为0
                width=1;  // 最小设为1
            }
            if (height==0){  // 高度为0
                height=1;  // 最小设为1
            }
            Bitmap b=Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);  // 创建目标位图
            Canvas canvas=new Canvas(b);  // 创建画布
            canvas.save();  // 保存画布状态
            ninePatchDrawable.setBounds(0, 0, canvas.getWidth(),canvas.getHeight());  // 设置九宫格边界
            ninePatchDrawable.setAlpha(alpha);  // 设置透明度
            ninePatchDrawable.draw(canvas);  // 绘制九宫格
            canvas.rotate(rotate,b.getWidth()/2,b.getHeight()/2);  // 旋转画布
            canvas.restore();  // 恢复画布状态

            return b;  // 返回绘制后的位图
        }
        return null;  // 非九宫格返回null
    }

    //region 更新测量数据
    /**
     * 更新测量数据并添加单位与削波标记
     *
     * @param chIdx       通道索引
     * @param measureType 测量类型
     * @param val         测量值
     * @return 带单位和削波标记的测量结果字符串
     */
    public static String updateMeasureData(int chIdx,int measureType, float val){
        Measure measure = getHardwareMeasure(chIdx);  // 获取通道的测量配置
        String value= addUnit(chIdx,measureType,val);  // 添加单位
        String result= clippingProcess(value,measureType+Measure.MeasureType.MEASURE_PERIOD,measure.getClipping());  // 处理削波标记
        return result;  // 返回最终结果
    }

    /**
     * 根据通道类型获取对应的Measure对象
     *
     * @param chId 通道索引
     * @return Measure对象，通道无效时返回null
     */
    private static Measure getHardwareMeasure(int chId) {
        BaseChannel baseChannel = null;  // 通道基类引用
        if (ChannelFactory.isDynamicCh(chId)) {  // 动态通道（物理通道）
            baseChannel = ChannelFactory.getDynamicChannel(chId);  // 获取动态通道
        } else if (ChannelFactory.isMathCh(chId)) {  // 数学通道
            baseChannel = ChannelFactory.getMathChannel(chId);  // 获取数学通道
        } else if (ChannelFactory.isRefCh(chId)) {  // 参考通道
            baseChannel = ChannelFactory.getRefChannel(chId);  // 获取参考通道
        }
        if (baseChannel != null) {  // 通道有效
            return baseChannel.getMeasure();  // 返回测量配置
        }
        return null;  // 无效通道返回null
    }
    private static final String MEASURE_DATA_INIT = "----";  // 测量数据初始/无效显示值

    /**
     * 根据削波类型为测量数据添加削波标记（"?"）
     *
     * @param sData   测量数据字符串
     * @param itemId  测量项ID
     * @param clipping 削波类型（0正常/1正削/2负削/3双削/4无波）
     * @return 添加削波标记后的字符串
     */
    private static String clippingProcess(String sData, int itemId, int clipping) {
        switch (clipping) {  // 根据削波类型处理
            case 1://正削
                switch (itemId) {  // 正削时，部分测量项不标记
                    case Measure.MeasureType.MEASURE_NEGATIVE_OVERSHOOT:  // 负过冲
                    case Measure.MeasureType.MEASURE_LOW:  // 低值
                    case Measure.MeasureType.MEASURE_MIN:  // 最小值
                        break;  // 这些项不标记
                    default:  // 其他项
                        if (!sData.equals(MEASURE_DATA_INIT)) sData += "?";  // 非初始值添加"?"标记
                        break;
                }
                break;
            case 2://负削
                switch (itemId) {  // 负削时，部分测量项不标记
                    case Measure.MeasureType.MEASURE_POSITIVE_OVERSHOOT:  // 正过冲
                    case Measure.MeasureType.MEASURE_HIGH:  // 高值
                    case Measure.MeasureType.MEASURE_MAX:  // 最大值
                        break;  // 这些项不标记
                    default:  // 其他项
                        if (!sData.equals(MEASURE_DATA_INIT)) sData += "?";  // 非初始值添加"?"标记
                        break;
                }
                break;
            case 3://双削
                if (!sData.equals(MEASURE_DATA_INIT)) sData += "?";  // 所有项都标记
                break;
            case 4://无波
                if (!sData.equals(MEASURE_DATA_INIT)) sData += "?";  // 所有项都标记
                break;
            case 0://正常
            default:  // 默认
                break;  // 不添加标记
        }
        return sData;  // 返回处理后的字符串
    }

    /**
     * 根据测量类型为测量值添加对应单位
     *
     * @param ch          通道索引
     * @param measureType 测量类型ID
     * @param val         测量值
     * @return 带单位的测量值字符串
     */
    public static String addUnit(int ch, int measureType, float val) {
        switch (measureType) {  // 根据测量类型添加单位
            case MeasureManage.IMeasure.MeasureId_Freq:  // 频率
                return TBookUtil.getFourFromD_(val) + "Hz";  // Hz单位
            case MeasureManage.IMeasure.MeasureId_DutyAdd:  // 正占空比
            case MeasureManage.IMeasure.MeasureId_DutySub:  // 负占空比
            case MeasureManage.IMeasure.MeasureId_ROV:  // 上升过冲
            case MeasureManage.IMeasure.MeasureId_FOV:  // 下降过冲
                //%不显示m、k、M等前缀，保留2位小数
                return TBookUtil.getPoint2FromD_noscale(val * 100) + "%";  // 百分比单位（×100）
            case MeasureManage.IMeasure.MeasureId_Phase:  // 相位
                String d = TBookUtil.getFourFromD_(val);  // 格式化相位值
                if ("-0f".equals(d) || "0f".equals(d)) {  // 负零或零
                    d = "0";  // 统一为"0"
                }
                return d + "°";  // 度数单位
            case MeasureManage.IMeasure.MeasureId_Period:  // 周期
            case MeasureManage.IMeasure.MeasureId_RiseTime:  // 上升时间
            case MeasureManage.IMeasure.MeasureId_FallTime:  // 下降时间
            case MeasureManage.IMeasure.MeasureId_Delay:  // 延迟
            case MeasureManage.IMeasure.MeasureId_WidthAdd:  // 正脉宽
            case MeasureManage.IMeasure.MeasureId_WidthSub:  // 负脉宽
            case MeasureManage.IMeasure.MeasureId_BurstW:  // 猝发脉宽
            case MeasureManage.IMeasure.MeasureId_TVALUE:  // T值
                return TBookUtil.getFourFromD_(val) + "s";  // 秒单位
            case MeasureManage.IMeasure.MeasureId_PKPK:  // 峰峰值
            case MeasureManage.IMeasure.MeasureId_Amp:  // 幅度
            case MeasureManage.IMeasure.MeasureId_High:  // 高值
            case MeasureManage.IMeasure.MeasureId_Low:  // 低值
            case MeasureManage.IMeasure.MeasureId_Max:  // 最大值
            case MeasureManage.IMeasure.MeasureId_Min:  // 最小值
            case MeasureManage.IMeasure.MeasureId_RMS:  // 均方根
            case MeasureManage.IMeasure.MeasureId_CRMS:  // 周期均方根
            case MeasureManage.IMeasure.MeasureId_Mean:  // 平均值
            case MeasureManage.IMeasure.MeasureId_CMean:  // 周期平均值
            case MeasureManage.IMeasure.MeasureId_ACRMS:  // 交流均方根
                return TBookUtil.getFourFromD_(val) + ChannelFactory.getProbeType(ch);  // 电压/电流单位（根据探头类型）
            case MeasureManage.IMeasure.MeasureId_PostitiveRate:  // 正斜率
            case MeasureManage.IMeasure.MeasureId_NegativeRate:  // 负斜率
                return TBookUtil.getFourFromD_(val) + ChannelFactory.getProbeType(ch) + "/s";  // 电压/电流每秒
        }
        return "";  // 未知类型返回空字符串
    }
    //endregion


    /**
     * 在画布上绘制文本（自动截断超宽文本）
     *
     * @param canvas 画布
     * @param text   待绘制文本
     * @param left   左边距
     * @param y      基线Y坐标
     * @param width  可用宽度
     * @param paint  画笔
     */
    public static void drawText(Canvas canvas,String text,float left,float y,int width,Paint paint){
        float textWidth=paint.measureText(text);  // 测量文本宽度
        String info=text;  // 待绘制文本
        if (width<textWidth){  // 文本超出可用宽度
            info= TextUtils.ellipsize(text,new TextPaint(paint),width,TextUtils.TruncateAt.END).toString();  // 末尾省略截断
            if (left<0){  // 左边距为负
                left=0;  // 修正为0
            }
        }
        canvas.drawText(info,left,y,paint);  // 绘制文本
    }

    /**
     * 在画布上绘制文本（自动截断超宽文本，同时使用TextPaint和Paint绘制）
     *
     * @param canvas    画布
     * @param text      待绘制文本
     * @param left      左边距
     * @param y         基线Y坐标
     * @param width     可用宽度
     * @param paint     画笔
     * @param textPaint 文本画笔
     */
    public static void drawText(Canvas canvas,String text,float left,float y,int width,Paint paint,TextPaint textPaint){
        float textWidth=paint.measureText(text);  // 测量文本宽度
        String info=text;  // 待绘制文本
        if (width<textWidth){  // 文本超出可用宽度
            info= TextUtils.ellipsize(text,new TextPaint(paint),width,TextUtils.TruncateAt.END).toString();  // 末尾省略截断
            if (left<0){  // 左边距为负
                left=0;  // 修正为0
            }
        }
        canvas.drawText(info,left,y,textPaint);  // 使用TextPaint绘制
        canvas.drawText(info,left,y,paint);  // 使用Paint绘制
    }

    //region probes
    /**
     * 获取通道探头类型单位（通过通道索引）
     *
     * @param chIdx 通道索引
     * @return 0=电压(V)，1=电流(A)
     */
    public static int getChanProbeTypeUnit(int chIdx){
        return getChanProbeTypeUnit(ChannelFactory.getDynamicChannel(chIdx));  // 委托给Channel版本
    }

    /**
     * 获取通道探头类型单位（通过Channel对象）
     *
     * @param channel 通道对象
     * @return 0=电压(V)，1=电流(A)
     */
    public static   int getChanProbeTypeUnit(Channel channel){
        if (channel!=null) {  // 通道非空
            BaseProbe baseProbe = channel.getProbe();  // 获取探头对象
            if (isProbeInterface(channel) && baseProbe != null) {  // 是接口探头且探头非空
                return channel.getProbe().getProbeType();  // 返回探头类型
            } else {  // 非接口探头
                return channel.getProbeType();  // 返回通道探头类型
            }
        }
        return 0;  // 通道为空返回0（电压）
    }


    /**
     * 判断通道是否连接了智能接口探头
     *
     * @param channel 通道对象
     * @return true表示连接了智能探头
     */
    public static boolean isProbeInterface(Channel channel){
        if (channel.getProbe()!=null) {  // 探头非空
            //Log.d("Tag.Debug", String.format("isProbeInterface: %b,%s", channel.isAutoProbe(), channel.getProbe().getSN()));
            return channel != null && channel.isAutoProbe() && !StrUtil.isEmpty(channel.getProbe().getSN());  // 通道非空、自动探头模式、探头有序列号
        }
        return false;  // 探头为空返回false
    }

    /**
     * 生成基于当前日期的名称字符串（yyyyMMdd格式）
     *
     * @return 日期名称字符串
     */
    public static String generateName() {
        Date date = new Date(System.currentTimeMillis());  // 获取当前时间
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");  // 日期格式化器
        String str_time = sdf.format(date);  // 格式化为yyyyMMdd
        return str_time;  // 返回日期名称
    }




    //endregion
}
