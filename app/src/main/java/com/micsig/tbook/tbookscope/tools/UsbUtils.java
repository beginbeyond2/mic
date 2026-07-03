package com.micsig.tbook.tbookscope.tools;                    // 声明包名，属于tbookscope工具模块

import android.content.Context;                                 // 导入上下文类，用于获取系统服务
import android.os.Build;                                        // 导入Build类，用于API版本判断
import android.os.Environment;                                  // 导入环境类，用于获取存储状态常量
import android.os.storage.StorageManager;                       // 导入存储管理器，用于查询存储卷信息
import android.os.storage.StorageVolume;                        // 导入存储卷类，用于表示单个存储卷
import android.text.TextUtils;                                  // 导入文本工具类，用于字符串空判断

import androidx.annotation.RequiresApi;                         // 导入API要求注解，用于标注最低API版本

import java.io.File;                                            // 导入文件类，用于操作文件路径
import java.lang.reflect.Method;                                // 导入反射方法类，用于调用系统隐藏API

/*
 * +=============================================================================+
 * |                          UsbUtils - USB存储设备工具类                        |
 * +=============================================================================+
 * | 模块定位：tbookscope工具模块，提供USB外接存储设备检测能力                         |
 * | 核心职责：检测U盘是否存在、获取U盘挂载路径、查询存储卷状态                           |
 * | 架构设计：单例模式（饿汉式），通过反射调用系统隐藏API获取存储卷信息                       |
 * | 数据流向：Context → StorageManager → 反射获取StorageVolume[] → 遍历查找可移动设备   |
 * |           → 获取U盘路径 → 检查挂载状态 → 更新Tools.mapUdisk                       |
 * | 依赖关系：依赖android.os.storage（存储管理）、java.lang.reflect（反射）、Tools       |
 * | 使用场景：需要检测U盘是否插入并获取U盘路径时调用                                        |
 * +=============================================================================+
 */
/**
 * @auother Liwb
 * @description:
 * @data:2022-3-14 9:29
 */
public class UsbUtils {
    private static final UsbUtils mUsbUtils=new UsbUtils();      // 饿汉式单例实例，类加载时即创建
    /**
     * 获取UsbUtils单例实例
     *
     * @return UsbUtils唯一实例
     */
    public static UsbUtils getInstance(){
        return mUsbUtils;                                        // 返回饿汉式单例实例
    }

    /**
     * 通过反射获取存储卷列表
     * <p>由于getVolumeList()是系统隐藏API，无法直接调用，需通过反射方式访问</p>
     *
     * @param storageManager 存储管理器实例
     * @return 存储卷数组，获取失败时返回null
     */
    private static StorageVolume[] getVolumeList(StorageManager storageManager){
        try {
            Class clz=StorageManager.class;                      // 获取StorageManager的Class对象，用于反射调用
            Method getVolumeList=clz.getMethod("getVolumeList"); // 获取名为getVolumeList的隐藏方法
            StorageVolume[] result=(StorageVolume[])getVolumeList.invoke(storageManager); // 反射调用该方法，获取存储卷数组
            return result;                                       // 返回存储卷数组
        }catch (Exception e){
            e.printStackTrace();                                 // 反射调用失败时打印异常堆栈
        }
        return null;                                             // 异常情况下返回null
    }

    /**
     * 通过反射获取指定路径的存储卷挂载状态
     * <p>由于getVolumeState()是系统隐藏API，无法直接调用，需通过反射方式访问</p>
     *
     * @param storageManager 存储管理器实例
     * @param path           存储卷路径
     * @return 挂载状态字符串（如"mounted"），参数无效或异常时返回空字符串
     */
    private static String getVolumeState(StorageManager storageManager,String path){
        String result="";                                        // 初始化返回结果为空字符串
        if (null==storageManager || TextUtils.isEmpty(path)){    // 校验参数有效性，存储管理器或路径为空则直接返回
            return result;                                       // 参数无效时返回空字符串
        }
        try{
            Class clz=StorageManager.class;                      // 获取StorageManager的Class对象，用于反射调用
            Method getVolumeList=clz.getMethod("getVolumeState",String.class); // 获取名为getVolumeState的隐藏方法，参数为String类型
            result=(String)getVolumeList.invoke(storageManager,path); // 反射调用该方法，传入路径参数，获取挂载状态
        }catch (Exception e){
            e.printStackTrace();                                 // 反射调用失败时打印异常堆栈
        }
        return result;                                           // 返回挂载状态字符串
    }

    /**
     * 检测U盘是否存在且已挂载
     * <p>获取系统存储管理器，查找U盘路径，检查挂载状态，若已挂载则将路径记录到Tools.mapUdisk中</p>
     *
     * @param context 应用上下文
     * @return true表示U盘已存在且已挂载，false表示U盘不存在或未挂载
     */
    @RequiresApi(api = Build.VERSION_CODES.R)                    // 要求最低API版本为Android 11（R）
    public static boolean UdiskExist(Context context){
        StorageManager storageManager=(StorageManager)context.getSystemService(Context.STORAGE_SERVICE); // 通过上下文获取存储管理器系统服务
        if (storageManager==null){                               // 判断存储管理器是否获取成功
            return false;                                        // 获取失败则返回false，表示无法检测
        }
        String usbPath=getUsbPath(context,storageManager);       // 获取U盘的挂载路径
        if (UsbUtils.getVolumeState(storageManager,usbPath).equals(Environment.MEDIA_MOUNTED)){ // 检查U盘路径的挂载状态是否为"mounted"（已挂载）
            Tools.mapUdisk.put(usbPath, usbPath);                // 将U盘路径记录到全局U盘映射表中
            return true;                                         // U盘已存在且已挂载，返回true
        }
        return false;                                            // U盘未挂载或不存在，返回false

    }

    /**
     * 获取U盘的挂载路径
     * <p>遍历所有存储卷，查找可移动存储设备（即U盘），返回其文件路径</p>
     *
     * @param context        应用上下文
     * @param storageManager 存储管理器实例
     * @return U盘挂载路径，未找到时返回null
     */
    @RequiresApi(api = Build.VERSION_CODES.R)                    // 要求最低API版本为Android 11（R）
    private static String getUsbPath(Context context, StorageManager storageManager){
        String usb=null;                                         // 初始化U盘路径为null
        StorageVolume[] volumes=UsbUtils.getVolumeList(storageManager); // 通过反射获取所有存储卷列表
        for(int i=0;i<volumes.length;i++){                       // 遍历所有存储卷
            //Logger.i(Command.TAG,"usb description:"+volumes[i].getDescription(context)+", isRemovalbel:"+volumes[i].isRemovable());
            if (volumes[i].isRemovable() /*&& volumes[i].getDescription(context).contains("USB")*/){ // 判断当前存储卷是否为可移动设备（U盘）
                File file=volumes[i].getDirectory();             // 获取该存储卷的目录文件对象
                if(file != null) {                               // 确保目录文件对象不为空
                    usb = file.getPath();                        // 获取U盘的文件路径
                    break;                                       // 找到第一个可移动设备后即跳出循环
                }

//                if (file.getPath().equals("/storage/udisk")){
//                    break;
//                }

            }
        }
        return usb;                                              // 返回U盘路径，未找到则为null
    }

}
