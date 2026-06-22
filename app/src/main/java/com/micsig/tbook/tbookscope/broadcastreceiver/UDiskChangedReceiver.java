package com.micsig.tbook.tbookscope.broadcastreceiver;  // 广播接收器包，存放各类系统广播监听器

import android.app.Activity;  // Activity基类，用于获取STORAGE_SERVICE系统服务
import android.content.BroadcastReceiver;  // 广播接收器基类，监听系统或应用发出的广播意图
import android.content.Context;  // 上下文对象，用于获取系统服务和访问应用资源
import android.content.Intent;  // 意图对象，携带广播的action和数据信息
import android.net.Uri;  // 统一资源标识符，用于解析广播中携带的U盘路径URI
import android.os.storage.StorageManager;  // 存储管理器，用于查询存储卷信息
import android.text.TextUtils;  // 文本工具类，用于空字符串判断
import android.view.View;  // 视图基类，用于控制U盘图标的可见性常量
import android.widget.ImageView;  // 图片视图控件，用于显示/隐藏U盘图标

import com.micsig.base.Logger;  // 日志工具类，用于调试日志输出
import com.micsig.smart.Property;  // 设备属性类，封装SN/UUID等设备信息
import com.micsig.smart.PropertyManage;  // 属性管理器单例，用于读取设备属性
import com.micsig.tbook.tbookscope.rxjava.RxBus;  // RxJava事件总线，用于跨组件消息传递
import com.micsig.tbook.tbookscope.rxjava.RxEnum;  // RxJava消息通道枚举，定义UDISK_RESPONSE等通道
import com.micsig.tbook.tbookscope.tools.Tools;  // 全局工具类，维护mapUdisk等U盘状态集合
import com.micsig.tbook.tbookscope.util.CacheUtil;  // 缓存工具类，持久化U盘路径等键值对

import java.io.BufferedReader;  // 缓冲读取器，用于逐行读取/proc/mounts文件
import java.io.BufferedWriter;  // 缓冲写入器，用于向U盘文件写入系统信息
import java.io.File;  // 文件类，用于路径拼接和文件存在性检查
import java.io.FileReader;  // 文件读取器，用于读取挂载信息文件
import java.io.FileWriter;  // 文件写入器，用于创建和写入系统信息文件
import java.io.IOException;  // IO异常类，用于捕获文件读写异常

/**
 * ┌──────────────────────────────────────────────────────────────────────────────┐
 * │                                                                              │
 * │   UDiskChangedReceiver - U盘状态变化广播接收器                                │
 * │                                                                              │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │                                                                              │
 * │  【模块定位】                                                                │
 * │   所属模块：tbookscope.broadcastreceiver（广播接收器模块）                     │
 * │   架构层级：系统适配层 - 外设状态监听                                         │
 * │   设计模式：观察者模式（BroadcastReceiver） + 事件总线（RxBus）               │
 * │   职责类型：U盘热插拔事件监听、状态同步、UI通知                               │
 * │                                                                              │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │                                                                              │
 * │  【核心职责】                                                                │
 * │   1. 监听Android系统U盘挂载/卸载/弹出/移除广播                               │
 * │   2. 更新主界面U盘图标（ImageView）的显示/隐藏状态                            │
 * │   3. 将U盘挂载路径缓存到CacheUtil持久化存储                                   │
 * │   4. 维护Tools.mapUdisk全局U盘路径映射表                                     │
 * │   5. 通过RxBus发送UDISK_RESPONSE事件通知其他组件                              │
 * │   6. 读取/proc/mounts验证U盘是否真正挂载                                     │
 * │   7. 将设备系统信息（SN、UUID）保存到U盘（已注释，保留功能）                   │
 * │                                                                              │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │                                                                              │
 * │  【架构设计】                                                                │
 * │                                                                              │
 * │   Android系统                                                                │
 * │     │                                                                        │
 * │     │ ACTION_MEDIA_MOUNTED / UNMOUNTED / EJECT / REMOVED                     │
 * │     ▼                                                                        │
 * │   UDiskChangedReceiver                                                       │
 * │     │                                                                        │
 * │     ├──→ CacheUtil.putOtherMapAndSave()  ──→ 持久化U盘路径                   │
 * │     ├──→ Tools.mapUdisk.put()/remove()   ──→ 全局U盘路径映射                 │
 * │     ├──→ tvUDisk.setVisibility()         ──→ UI图标更新                      │
 * │     ├──→ RxBus.post(UDISK_RESPONSE)      ──→ 事件总线通知                    │
 * │     └──→ isMounted()                      ──→ /proc/mounts验证               │
 * │                                                                              │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │                                                                              │
 * │  【数据流向】                                                                │
 * │                                                                              │
 * │   U盘插入:                                                                   │
 * │   系统广播 → onReceive() → 过滤内部存储 → 缓存路径 → 验证挂载               │
 * │           → 更新mapUdisk → 显示图标 → RxBus通知(true)                        │
 * │                                                                              │
 * │   U盘拔出:                                                                   │
 * │   系统广播 → onReceive() → 清空缓存路径 → 移除mapUdisk条目                  │
 * │           → 隐藏图标 → RxBus通知(false)                                      │
 * │                                                                              │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │                                                                              │
 * │  【依赖关系】                                                                │
 * │   ├── CacheUtil       - 缓存工具，持久化U盘路径（key: MAIN_BOTTOM_USB_PATH） │
 * │   ├── Tools           - 全局工具类，维护mapUdisk（Map<String, String>）      │
 * │   ├── RxBus           - 事件总线，发送UDISK_RESPONSE通道消息                 │
 * │   ├── RxEnum          - 消息通道枚举，定义UDISK_RESPONSE常量                 │
 * │   ├── PropertyManage  - 属性管理器，读取设备SN和UUID                         │
 * │   └── Logger          - 日志工具，调试信息输出                               │
 * │                                                                              │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │                                                                              │
 * │  【使用示例】                                                                │
 * │   // 1. 在Activity中注册广播接收器                                           │
 * │   UDiskChangedReceiver receiver = new UDiskChangedReceiver();                │
 * │   receiver.setUDiskControl(imgUDiskIcon);                                    │
 * │   IntentFilter filter = new IntentFilter();                                  │
 * │   filter.addAction(Intent.ACTION_MEDIA_MOUNTED);                             │
 * │   filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);                           │
 * │   filter.addAction(Intent.ACTION_MEDIA_EJECT);                               │
 * │   filter.addDataScheme("file");                                              │
 * │   registerReceiver(receiver, filter);                                        │
 * │                                                                              │
 * │   // 2. 在其他组件中监听U盘状态变化                                          │
 * │   RxBus.getInstance().toObservable(RxEnum.UDISK_RESPONSE, Boolean.class)     │
 * │       .subscribe(isMounted -> {                                              │
 * │           if (isMounted) { /* U盘已挂载 */ }                                 │
 * │           else { /* U盘已拔出 */ }                                            │
 * │       });                                                                    │
 * │                                                                              │
 * ├──────────────────────────────────────────────────────────────────────────────┤
 * │                                                                              │
 * │  【注意事项】                                                                │
 * │   1. ACTION_MEDIA_MOUNTED也会触发内部存储(/storage/emulated/0)的挂载通知，    │
 * │      需要在onReceive中过滤掉，避免误判为U盘                                   │
 * │   2. 开机前已插入的U盘不会触发MOUNTED广播，需在BOOT_COMPLETED时               │
 * │      主动查询（当前仅预留入口，未实现）                                       │
 * │   3. isMounted()通过读取/proc/mounts验证挂载状态，适用于开机前插入的场景      │
 * │   4. saveSysInfo()当前被注释，功能保留用于将设备SN/UUID写入U盘                │
 * │                                                                              │
 * └──────────────────────────────────────────────────────────────────────────────┘
 *
 * @author Liwb
 * @version 1.0
 * @since 2022-4-14
 * @see BroadcastReceiver Android广播接收器基类
 * @see RxBus RxJava事件总线
 * @see RxEnum#UDISK_RESPONSE U盘响应消息通道
 * @see Tools#mapUdisk 全局U盘路径映射表
 * @see CacheUtil#MAIN_BOTTOM_USB_PATH U盘路径缓存键
 */
public class UDiskChangedReceiver extends BroadcastReceiver {  // 继承BroadcastReceiver，监听U盘热插拔系统广播

    /** 日志标签，用于Logcat中过滤和识别本类的日志输出 */
    private static final String TAG = "UDiskChangedReceiver";  // 日志TAG，标识U盘广播接收器

    /** Linux挂载信息文件路径，用于读取当前系统所有已挂载的文件系统信息 */
    private static final String MOUNTS_FILE = "/proc/mounts";  // /proc/mounts文件，记录所有挂载点

    /** 存储管理器，用于查询系统存储卷信息（当前未使用，预留扩展） */
    private StorageManager mStorageManager;  // StorageManager实例，可通过它获取存储卷列表

    /** U盘图标控件引用，用于在U盘插入/拔出时更新图标的显示/隐藏状态 */
    private ImageView tvUDisk;  // 主界面底部的U盘图标ImageView

    /**
     * 设置U盘图标控件引用
     *
     * <p>由Activity在注册广播接收器后调用，将主界面的U盘图标ImageView
     * 注入到本接收器中，以便在收到U盘插拔广播时直接控制图标的可见性。
     *
     * @param tvUDisk 主界面U盘图标ImageView控件，不能为null
     */
    public void setUDiskControl(ImageView tvUDisk) {  // 设置U盘图标控件引用
        this.tvUDisk = tvUDisk;  // 保存ImageView引用，供onReceive中更新可见性
    }

    /**
     * 广播接收回调方法 - 处理U盘状态变化事件
     *
     * <p>当系统发送U盘相关的广播时，此方法被调用。根据不同的action执行对应的处理逻辑：
     * <ul>
     *   <li>{@link Intent#ACTION_MEDIA_MOUNTED} - U盘挂载：缓存路径、更新图标、通知事件</li>
     *   <li>{@link Intent#ACTION_MEDIA_UNMOUNTED} - U盘卸载：清空缓存、隐藏图标、通知事件</li>
     *   <li>{@link Intent#ACTION_MEDIA_EJECT} - U盘弹出：同卸载处理</li>
     *   <li>BOOT_COMPLETED - 开机完成：预留入口，需主动查询U盘（未实现）</li>
     *   <li>{@link Intent#ACTION_MEDIA_REMOVED} - U盘物理移除：仅记录日志</li>
     * </ul>
     *
     * @param context 广播上下文，用于获取系统服务
     * @param intent  广播意图，包含action和U盘路径数据
     */
    @Override  // 重写BroadcastReceiver的onReceive方法
    public void onReceive(Context context, Intent intent) {  // 接收并处理U盘状态变化广播
        mStorageManager = (StorageManager) context.getSystemService(Activity.STORAGE_SERVICE);  // 获取系统存储管理器服务
        String action = intent.getAction();  // 获取广播的action字符串
        Logger.d(TAG, "action:" + action);  // 输出调试日志，记录收到的广播action

        if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {  // 判断是否为U盘挂载广播
            String mountPath = intent.getData().getPath();  // 从Intent的URI数据中提取挂载路径
            Uri data = intent.getData();  // 获取Intent中携带的URI数据（当前未使用，预留扩展）
            if ("/storage/emulated/0".equalsIgnoreCase(mountPath)) {  // 过滤内部存储的挂载通知，避免误判为U盘
                return;  // 内部存储挂载，直接返回不做处理
            }
            Logger.d(TAG, "mountPath = " + mountPath);  // 输出调试日志，记录U盘挂载路径
            if (!TextUtils.isEmpty(mountPath)) {  // 确保挂载路径非空
                // 读取到U盘路径后再做其他业务逻辑
                CacheUtil.get().putOtherMapAndSave(CacheUtil.MAIN_BOTTOM_USB_PATH, mountPath);  // 将U盘路径持久化到缓存（key: MAIN_BOTTOM_USB_PATH）
                boolean mounted = isMounted(mountPath);  // 读取/proc/mounts验证U盘是否真正挂载（当前未使用返回值，预留校验）
                tvUDisk.setVisibility(View.VISIBLE);  // 显示主界面U盘图标
                if (!Tools.mapUdisk.containsKey(mountPath)) {  // 检查全局U盘映射表中是否已存在该路径
                    Tools.mapUdisk.put(mountPath, mountPath);  // 将U盘路径添加到全局映射表（key和value均为路径字符串）
                }
                RxBus.getInstance().post(RxEnum.UDISK_RESPONSE, true);  // 通过RxBus发送U盘已挂载事件（Boolean true）
                // saveSysInfo(mountPath);  // [已注释] 将设备SN和UUID保存到U盘，功能保留
            }
        } else if (action.equals(Intent.ACTION_MEDIA_UNMOUNTED) || action.equals(Intent.ACTION_MEDIA_EJECT)) {  // 判断是否为U盘卸载或弹出广播
            Logger.d(TAG, "onReceive: " + "U盘移除了");  // 输出调试日志，记录U盘移除事件
            CacheUtil.get().putOtherMapAndSave(CacheUtil.MAIN_BOTTOM_USB_PATH, "");  // 清空缓存中的U盘路径（置为空字符串）
            String path = intent.getData().getPath();  // 从Intent的URI数据中提取被移除的U盘路径
            Tools.mapUdisk.remove(path);  // 从全局U盘映射表中移除该路径条目
            tvUDisk.setVisibility(View.GONE);  // 隐藏主界面U盘图标
            RxBus.getInstance().post(RxEnum.UDISK_RESPONSE, false);  // 通过RxBus发送U盘已拔出事件（Boolean false）
        } else if (action.equals("android.intent.action.BOOT_COMPLETED")) {  // 判断是否为开机完成广播
            Logger.d(TAG, "onReceive: " + "BOOT_COMPLETED");  // 输出调试日志，记录开机完成事件
            // 如果是开机完成，则需要调用另外的方法获取U盘的路径（开机前已插入的U盘不会触发MOUNTED广播）
        } else if (action.equals(Intent.ACTION_MEDIA_REMOVED)) {  // 判断是否为U盘物理移除广播
            Logger.d(TAG, "onReceive: " + "ACTION_MEDIA_REMOVED");  // 输出调试日志，记录U盘物理移除事件
        }
    }

    /**
     * 检查指定路径是否已挂载
     *
     * <p>通过读取Linux系统的/proc/mounts文件，逐行检查是否包含指定路径，
     * 以验证U盘是否真正挂载到文件系统中。此方法适用于开机前已插入U盘的场景，
     * 因为此时系统不会发送ACTION_MEDIA_MOUNTED广播。
     *
     * <p>/proc/mounts文件格式示例：
     * <pre>
     * /dev/block/sda1 /storage/USB_DRIVE vfat rw 0 0
     * </pre>
     * 第二列为挂载点路径，通过contains匹配即可判断。
     *
     * @param path 待检查的挂载路径，如 "/storage/USB_DRIVE"
     * @return true - 路径已挂载（/proc/mounts中包含该路径）
     *         false - 路径未挂载或读取文件异常
     */
    public static boolean isMounted(String path) {  // 静态方法，检查指定路径是否在/proc/mounts中
        boolean blnRet = false;  // 默认返回false，表示未挂载
        String strLine = null;  // 用于存储每次读取的一行文本
        BufferedReader reader = null;  // 缓冲读取器，用于逐行读取/proc/mounts
        try {
            reader = new BufferedReader(new FileReader(MOUNTS_FILE));  // 打开/proc/mounts文件进行读取
            while ((strLine = reader.readLine()) != null) {  // 逐行读取文件内容
                if (strLine.contains(path)) {  // 检查当前行是否包含目标挂载路径
                    blnRet = true;  // 找到匹配的挂载记录，设置为true
                    break;  // 已找到匹配，跳出循环无需继续读取
                }
            }
        } catch (Exception e) {  // 捕获文件读取等异常
            e.printStackTrace();  // 打印异常堆栈信息
        } finally {
            if (reader != null) {  // 确保读取器不为空时才关闭
                try {
                    reader.close();  // 关闭BufferedReader释放文件资源
                } catch (IOException e) {  // 捕获关闭时的IO异常
                    e.printStackTrace();  // 打印关闭异常堆栈信息
                }
                reader = null;  // 将引用置空，帮助GC回收
            }
        }
        return blnRet;  // 返回挂载检查结果
    }

    /**
     * 保存设备系统信息到U盘
     *
     * <p>将设备的SN（序列号）和UUID（唯一标识码）写入U盘根目录下的文本文件。
     * 文件名格式为"{displaySn}.txt"，内容包含SN和Code(UUID)两行信息。
     * 仅在文件不存在时创建并写入，避免重复写入覆盖。
     *
     * <p><b>当前状态：</b>此方法在onReceive中被注释调用，功能保留但未启用。
     *
     * <p>输出文件示例：
     * <pre>
     * SN:MHO20220001( MHO20220001)
     * Code:550e8400-e29b-41d4-a716-446655440000
     * </pre>
     *
     * @param path U盘挂载路径，如 "/storage/USB_DRIVE"，文件将创建在此路径下
     */
    private static void saveSysInfo(String path) {  // 静态私有方法，将设备SN和UUID保存到U盘
        PropertyManage propertyManage = PropertyManage.getInstance();  // 获取属性管理器单例
        propertyManage.update();  // 刷新属性数据，确保读取最新的设备信息
        Property property = propertyManage.getProperty();  // 获取设备属性对象
        String uuid = property.getUUID();  // 获取设备UUID（唯一标识码）
        String sn = property.getSN();  // 获取设备SN（序列号，原始值）
        String displaySn = property.getDisplaySN();  // 获取设备显示用SN（可能不同于原始SN）
        if (sn != null) sn = sn.trim();  // 去除SN字符串首尾空白字符
        if (displaySn != null) displaySn = displaySn.trim();  // 去除显示SN字符串首尾空白字符
        if (displaySn == null || displaySn.isEmpty()) {  // 如果显示SN为空
            displaySn = sn;  // 回退使用原始SN作为显示SN和文件名
        }
        if (!path.endsWith(File.separator)) {  // 检查路径是否以文件分隔符结尾
            path += File.separator;  // 如果不是，追加分隔符，确保路径格式正确
        }
        path += displaySn + ".txt";  // 拼接完整文件路径：U盘路径 + 显示SN + .txt后缀
        File file = new File(path);  // 创建File对象表示目标文件
        if (!file.exists()) {  // 仅在文件不存在时创建，避免重复写入
            try {
                FileWriter fw = new FileWriter(file);  // 创建文件写入器
                BufferedWriter bufw = new BufferedWriter(fw);  // 包装为缓冲写入器，提高写入效率
                bufw.write("SN:" + displaySn + "( " + sn + ")");  // 写入第一行：显示SN（原始SN）
                bufw.newLine();  // 写入换行符
                bufw.write("Code:" + uuid);  // 写入第二行：UUID标识码
                bufw.flush();  // 刷新缓冲区，确保数据写入磁盘
                fw.close();  // 关闭文件写入器，释放资源
            } catch (IOException e) {  // 捕获文件写入IO异常
                e.printStackTrace();  // 打印异常堆栈信息
            }
        }

    }
}
