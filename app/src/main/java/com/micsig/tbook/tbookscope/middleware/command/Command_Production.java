package com.micsig.tbook.tbookscope.middleware.command; // 示波器中间件命令子包

import android.content.Context; // Android上下文
import android.os.Environment; // Android环境变量
import android.util.Log; // Android日志工具

import com.micsig.base.Logger; // 自定义日志工具
import com.micsig.smart.Property; // 设备属性类
import com.micsig.smart.PropertyManage; // 设备属性管理器
import com.micsig.tbook.hardware.Hardware; // 硬件接口
import com.micsig.tbook.scope.Scope; // 示波器核心作用域
import com.micsig.tbook.scope.channel.Channel; // 通道类
import com.micsig.tbook.scope.channel.ChannelFactory; // 通道工厂
import com.micsig.tbook.tbookscope.BuildConfig; // 构建配置
import com.micsig.tbook.tbookscope.tools.LockScreenUtils; // 锁屏工具
import com.micsig.tbook.tbookscope.tools.ShellUtils; // Shell命令工具
import com.micsig.tbook.tbookscope.tools.Tools; // 通用工具类
import com.micsig.tbook.tbookscope.util.App; // 应用上下文工具

import java.io.File; // 文件操作

/*
 * +-----------------------------------------------------------------------------+
 * |                         Command_Production                                  |
 * +-----------------------------------------------------------------------------+
 * | 模块定位: 示波器生产/产线命令处理模块                                        |
 * | 核心职责: 处理SCPI生产相关指令，包括固件烧写、产品信息写入/查询、SN/UUID/     |
 * |          硬件版本查询、私有模式开关、带宽设置、系统时间/清理、关机/重启/待机等 |
 * | 架构设计: 命令模式，作为Command子模块，需通过私有密钥验证后才能执行写操作     |
 * | 数据流向: SCPI指令 → 本类(验证/设置/查询) → PropertyManage/PowerManager     |
 * | 依赖关系: Command, PropertyManage, Property, Scope, Hardware, App,           |
 * |           PowerManagerUtils, LockScreenUtils, ShellUtils, Tools              |
 * | 使用场景: 产线生产测试、设备校验、远程管理设备信息时使用                     |
 * +-----------------------------------------------------------------------------+
 */
public class Command_Production {
    private static final String TAG="Command_Production"; // 日志标签
//            new SCPICommandStruct(":ware","SCPI_Production","Ware"), //固件烧写
//            new SCPICommandStruct(":application","SCPI_Production","Application"), //MCU、APP烧写
//            new SCPICommandStruct(":SYS:WRIT?","SCPI_Production","WriteQ"), //产品信息写入查询
//            new SCPICommandStruct(":SYS:SN?","SCPI_Production","SNQ"), //查询SN
//            new SCPICommandStruct(":PRIVate:UUID?","SCPI_Production","UUIDQ"), //查询唯一识别码
//            new SCPICommandStruct(":PRIVate:HWVersion?","SCPI_Production","HWVersionQ"), //查询硬件版本
//            new SCPICommandStruct(":PRIVate:SERiaino?","SCPI_Production","SeriaiNoQ"), //查询SN
//            new SCPICommandStruct(":PRIVate:STRingcode","SCPI_Production","StringCode"), //串码写入
//            new SCPICommandStruct(":PRIVate:DISPlay:SERiaino","SCPI_Production","DisplaySeriaiNo"), //写入外部SN号
//            new SCPICommandStruct(":PRIVate:MACHinetype","SCPI_Production","MachineType"), //设置设备型号
//            new SCPICommandStruct("PRIVate:STAR","SCPI_Production","Star"),// 开启私有
//            new SCPICommandStruct("PRIVate:STAR?","SCPI_PRoduction","StarQ"),//查询状态
//            new SCPICommandStruct("PRIVate:BANDwidth","SCPI_Production","BandWidth"), //设置带宽
//            new SCPICommandStruct("PRIVate:SETTing:CLEar","SCPI_Production","SettingClear"),//清楚设置
//
//            new SCPICommandStruct("INTeface:TIME","SCPI_Production","Time"), //设置系统时间
//            new SCPICommandStruct("INTeface:CLEAn","SCPI_Production","Clean"),//恢复系统设置

     private boolean isPrivateStart =false; // 私有模式是否已开启


     /**
      * 固件烧写命令（当前仅记录日志）
      *
      * @param isUpdateUI  是否同步更新UI界面
      */
     public void Ware(boolean isUpdateUI){
         Logger.i(TAG,"ware"); // 记录固件烧写日志
     }

     /**
      * 应用程序烧写命令（当前仅记录日志）
      *
      * @param isUpdateUI  是否同步更新UI界面
      */
     public void Application(boolean isUpdateUI){
         Logger.i(TAG,"application"); // 记录应用程序烧写日志
     }


     /**
      * 显示设备信息（调试用）
      */
     private void displayInfo(){
         PropertyManage propertyManage = PropertyManage.getInstance(); // 获取属性管理器实例
         propertyManage.update(); // 刷新属性数据
         Property property = propertyManage.getProperty(); // 获取属性对象
         Log.d(TAG,"type:" + property.getType()); // 打印设备类型
         Log.d(TAG,"sn:" + property.getSN() + "," + property.getDisplaySN()); // 打印SN号
         Log.d(TAG,"bandWidth:" + property.getBandWidth()); // 打印带宽
     }

     /**
      * 写入产品信息并查询写入结果
      *
      * @param productID        产品ID
      * @param makeDate         生产日期
      * @param SN               序列号
      * @param HardwareVersion  硬件版本
      * @param serial           串码
      * @param isUpdateUI       是否同步更新UI界面
      * @return 写入是否成功
      */
     public boolean WriteQ(String productID,String makeDate,String SN,String HardwareVersion,String serial,boolean isUpdateUI){
         if(!isPrivateStart) return false; // 私有模式未开启，拒绝写入

         Logger.i(TAG,"WriteQ"); // 记录写入查询日志
         if (productID.equals("")|| makeDate.equals("")|| SN.equals("") || HardwareVersion.equals("") || serial.equals("")) // 校验参数是否为空
             return false; // 参数为空，写入失败

         Logger.i(TAG,"productId:"+productID+" makeDate:"+makeDate+"  sn:"+SN+"  version:"+HardwareVersion+"  serial:"+serial); // 打印写入参数

         PropertyManage propertyManage = PropertyManage.getInstance(); // 获取属性管理器实例
         Property property = propertyManage.getProperty(); // 获取属性对象
         property.setType(productID); //TEST // 设置产品类型

         property.setSN(SN); //sn // 设置序列号
         property.setHwVersion(HardwareVersion); //1 // 设置硬件版本
         property.serialCodeUpgrade(serial.replace("-","")); // 设置串码（去除横杠）

         String ver = App.getFwVersion(property); // 获取固件版本
         propertyManage.commit(); // 提交属性变更
         Scope.getInstance().setUsbInfo(property.getType(),property.getDisplaySN(),ver); // 更新USB设备信息

//         displayInfo();
         return true; // 写入成功
     }

     /**
      * 查询设备序列号（SN）
      *
      * @param isUpdateUI  是否同步更新UI界面
      * @return SN号字符串，查询失败返回"failed"
      */
     public String SNQ(boolean isUpdateUI){
         Logger.i(TAG,"SNQ"); // 记录SN查询日志
         PropertyManage propertyManage = PropertyManage.getInstance(); // 获取属性管理器实例
         propertyManage.update(); // 刷新属性数据
         String sn=propertyManage.getProperty().getSN(); // 获取SN号
         if (sn.length()>0){ // SN号非空
             return sn; // 返回SN号
         }
         else { // SN号为空
             return "failed"; // 返回失败标识
         }
     }

     /**
      * 查询系统ID
      *
      * @param isUpdateUI  是否同步更新UI界面
      * @return 系统ID字符串
      */
    public String SysId(boolean isUpdateUI){
        return Hardware.getInstance().getSysId(); // 从硬件接口获取系统ID
    }

     /**
      * 设置或清除出厂日期
      *
      * @param param1      日期字符串，"clear"表示清除
      * @param isUpdateUI  是否同步更新UI界面
      * @return 设置后的日期字符串
      */
     public String WDate(String param1,boolean isUpdateUI){
         Logger.i(TAG,"date:"+param1); // 记录日期设置日志
         if(!isPrivateStart) return "---"; // 私有模式未开启，返回占位符
         param1 = param1.replace("\n","").replace("\r","").trim(); // 去除换行符和空白
         if (param1.equalsIgnoreCase("clear")){ // 判断是否为清除指令
             param1=""; // 清空日期
         }
         PropertyManage propertyManage = PropertyManage.getInstance(); // 获取属性管理器实例
         propertyManage.getProperty().setDeliveryDate(param1); // 设置出厂日期
         propertyManage.commit(); // 提交属性变更
         return param1; // 返回设置后的日期
     }

     /**
      * 查询设备唯一识别码（UUID）
      *
      * @param isUpdateUI  是否同步更新UI界面
      * @return UUID字符串
      */
     public String UUIDQ(boolean isUpdateUI){
         Logger.i(TAG,"UUIDQ"); // 记录UUID查询日志
         PropertyManage propertyManage = PropertyManage.getInstance(); // 获取属性管理器实例
         propertyManage.update(); // 刷新属性数据
         return propertyManage.getProperty().getUUID(); // 返回UUID
//         return HwManager.getInstance().getMachineUUID();
     }

     /**
      * 查询硬件版本号
      *
      * @param isUpdateUI  是否同步更新UI界面
      * @return 硬件版本字符串
      */
     public String HWVersionQ(boolean isUpdateUI){
         Logger.i(TAG,"HWVersionQ"); // 记录硬件版本查询日志
         PropertyManage propertyManage = PropertyManage.getInstance(); // 获取属性管理器实例
         propertyManage.update(); // 刷新属性数据
         return propertyManage.getProperty().getHwVersion(); // 返回硬件版本
//         return String.valueOf(HwManager.getInstance().getHwVersion());
     }

     /**
      * 查询序列号
      *
      * @param isUpdateUI  是否同步更新UI界面
      * @return 序列号字符串
      */
     public String SerialiNoQ(boolean isUpdateUI){
         Logger.i(TAG,"SerialiNoQ"); // 记录序列号查询日志
         PropertyManage propertyManage = PropertyManage.getInstance(); // 获取属性管理器实例
         propertyManage.update(); // 刷新属性数据
         return propertyManage.getProperty().getSN(); // 返回序列号
//         return PropertyManage.getInstance().getProperty().getSN();
     }

     /**
      * 写入串码
      *
      * @param stringCode  串码字符串
      * @param isUpdateUI  是否同步更新UI界面
      * @return 写入是否成功
      */
     public boolean StringCode(String stringCode,boolean isUpdateUI){
         Logger.i(TAG,"StringCode stringCode:"+stringCode); // 记录串码写入日志
         if(!isPrivateStart) return false; // 私有模式未开启，拒绝写入
         boolean b=true; // 操作结果标志
         PropertyManage propertyManage = PropertyManage.getInstance(); // 获取属性管理器实例
         b = propertyManage.getProperty().serialCodeUpgrade(stringCode.replace("-","")); // 写入串码（去除横杠）
         if (b) propertyManage.commit(); // 写入成功则提交变更
         Log.d(TAG,"b:" + b); // 打印操作结果
//         displayInfo();
         return b; // 返回操作结果
     }

     /**
      * 写入外部显示SN号
      *
      * @param outSerialNo 外部SN号
      * @param isUpdateUI  是否同步更新UI界面
      * @return 写入是否成功
      */
     public boolean DisplaySeriaiNo(String outSerialNo,boolean isUpdateUI){
         Logger.i(TAG,"DisplaySeriaiNo outSerialNo:"+outSerialNo); // 记录外部SN号写入日志
         if(!isPrivateStart) return false; // 私有模式未开启，拒绝写入
         boolean b=true; // 操作结果标志
         PropertyManage propertyManage = PropertyManage.getInstance(); // 获取属性管理器实例
         Property property = propertyManage.getProperty(); // 获取属性对象
         property.setDisplaySN(outSerialNo); // 设置外部显示SN号
         String ver = App.getFwVersion(property); // 获取固件版本
         propertyManage.commit(); // 提交属性变更
         Scope.getInstance().setUsbInfo(property.getType(),property.getDisplaySN(),ver); // 更新USB设备信息
         return b; // 返回操作结果
     }

     /**
      * 设置设备型号
      *
      * @param machineType 设备型号字符串
      * @param isUpdateUI  是否同步更新UI界面
      * @return 设置是否成功
      */
     public boolean MachineType(String machineType,boolean isUpdateUI){
         if(!isPrivateStart) return false; // 私有模式未开启，拒绝设置
         Logger.i(TAG,"MachineType machineType:"+machineType); // 记录型号设置日志
         boolean b=true; // 操作结果标志
         PropertyManage propertyManage = PropertyManage.getInstance(); // 获取属性管理器实例
         Property property = propertyManage.getProperty(); // 获取属性对象
         property.setType(machineType); // 设置设备型号
         String ver = App.getFwVersion(property); // 获取固件版本
         propertyManage.commit(); // 提交属性变更
         Scope.getInstance().setUsbInfo(property.getType(),property.getDisplaySN(),ver); // 更新USB设备信息
         return b; // 返回操作结果
     }

     /**
      * 开启私有模式（需密钥验证）
      *
      * @param jiamiKey    加密密钥
      * @param isUpdateUI  是否同步更新UI界面
      */
     public void Star(String jiamiKey,boolean isUpdateUI){
         Logger.i(TAG,"Star"); // 记录私有模式开启日志
         if (isPrivateStart) return; // 已经开启则直接返回
         PropertyManage propertyManage = PropertyManage.getInstance(); // 获取属性管理器实例
         propertyManage.update(); // 刷新属性数据
         String s= propertyManage.getProperty().getPrivateUUID().trim(); // 获取存储的私有UUID
         jiamiKey = jiamiKey.replaceAll("\n","").replaceAll("\r","").trim(); // 去除密钥中的换行和空白
         Logger.i(TAG,"Star jiamiKey:"+jiamiKey+"  s:"+s); // 打印密钥和UUID
         if (jiamiKey.equalsIgnoreCase(s)){ // 比较密钥和UUID
             Logger.i(TAG,"Star privatestate:"+isPrivateStart); // 记录验证通过日志
             isPrivateStart=true; // 开启私有模式
         }
     }

     /**
      * 查询私有模式状态
      *
      * @param isUpdateUI  是否同步更新UI界面
      * @return 私有模式是否开启
      */
     public boolean StarQ(boolean isUpdateUI){
         Logger.i(TAG,"StarQ state:"+isPrivateStart); // 记录私有模式状态查询日志
         return isPrivateStart; // 返回私有模式状态
     }

     /**
      * 停止私有模式
      *
      * @param isUpdateUI  是否同步更新UI界面
      */
     public void Stop(boolean isUpdateUI){
         Logger.i(TAG,"Stop"); // 记录私有模式停止日志
         isPrivateStart=false; // 关闭私有模式
     }

     /**
      * 设置设备带宽
      *
      * @param bandwidth   带宽值（MHz）
      * @param isUpdateUI  是否同步更新UI界面
      */
     public void BandWidth(int bandwidth,boolean isUpdateUI){
         Logger.i(TAG,"BandWidth:" + bandwidth); // 记录带宽设置日志

//         if(BuildConfig.DEBUG){
//             Channel.setMaxBandWidth(bandwidth*1000*1000L);
//             Channel channel = ChannelFactory.getDynamicChannel(ChannelFactory.CH1);
//             if(channel != null) {
//                 channel.bandWidthTypeChange();
//             }
//         }

         if(!isPrivateStart) return; // 私有模式未开启，拒绝设置

         PropertyManage propertyManage = PropertyManage.getInstance(); // 获取属性管理器实例
         propertyManage.getProperty().setBandWidth(bandwidth); // 设置带宽
         propertyManage.commit(); // 提交属性变更
//         displayInfo();
     }

     /**
      * 清除设备属性设置
      *
      * @param isUpdateUI  是否同步更新UI界面
      */
     public void SettingClear(boolean isUpdateUI){
         Logger.i(TAG,"setting clear!"); // 记录设置清除日志
         if(!isPrivateStart) return; // 私有模式未开启，拒绝操作
         PropertyManage.getInstance().clear(); // 清除所有属性设置
     }

     /**
      * 设置系统时间（当前未实现）
      *
      * @param s1          时间参数1
      * @param s2          时间参数2
      * @param s3          时间参数3
      * @param s4          时间参数4
      * @param s5          时间参数5
      * @param isUpdateUI  是否同步更新UI界面
      */
     public void Time(String s1,String s2,String s3 ,String s4,String s5,boolean isUpdateUI){

     }

     //清楚clean
     /**
      * 清除系统数据（删除示波器相关存储文件）
      *
      * @param isUpdateUI  是否同步更新UI界面
      */
     public void Clean(boolean isUpdateUI){

         Logger.i(TAG,"integer:clean"); // 记录清理日志
         String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + Tools.SMART_PATH + File.separator; // 构建Smart路径
         Logger.i(TAG,"path:"+path); // 打印路径
         ShellUtils.execCommand("rm -rf "+path,false); // 删除Smart目录

         path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + Tools.SCOPE_PATH + File.separator; // 构建Scope路径
         Logger.i(TAG,"path:"+path); // 打印路径
         ShellUtils.execCommand("rm -rf "+path,false); // 删除Scope目录

         path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/RecordScreen/"; // 构建录屏路径
         ShellUtils.execCommand("rm -rf "+path,false); // 删除录屏目录

         path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/"; // 构建DCIM路径
         ShellUtils.execCommand("rm -rf "+path,false); // 删除DCIM目录

         path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Pictures/"; // 构建Pictures路径
         ShellUtils.execCommand("rm -rf "+path,false); // 删除Pictures目录

         path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.appMarkerCache/"; // 构建缓存路径
         ShellUtils.execCommand("rm -rf "+path,false); // 删除缓存目录
//        FileUtils.deleteDir(path);
         Tools.uDiskUpdate(path, App.get().getApplicationContext()); // 通知U盘更新
     }

     /**
      * 关机
      */
    public void Shutdown(){
        PowerManagerUtils.shutdown(App.get().getApplicationContext()); // 调用电源管理工具关机
    }

     /**
      * 重启
      */
    public void Restart(){
        PowerManagerUtils.reboot(App.get().getApplicationContext()); // 调用电源管理工具重启
    }

     /**
      * 进入待机模式
      */
    public void Standby(){
        PowerManagerUtils.enterStandby(App.get().getApplicationContext()); // 调用电源管理工具进入待机
    }

     /**
      * 唤醒（退出待机模式）
      */
    public void Wakeup(){
        PowerManagerUtils.exitStandby(App.get().getApplicationContext()); // 调用电源管理工具退出待机
    }

     /**
      * 锁定屏幕
      */
    public void Lock(){
        Context context = App.get().getApplicationContext(); // 获取应用上下文
        if(!LockScreenUtils.isLockScreen(context)){ // 当前未锁屏
            LockScreenUtils.setLockScreen(context,true); // 设置锁屏
        }
    }

     /**
      * 解锁屏幕
      */
    public void Unlock(){
        Context context = App.get().getApplicationContext(); // 获取应用上下文
        if(LockScreenUtils.isLockScreen(context)){ // 当前已锁屏
            LockScreenUtils.setLockScreen(context,false); // 解除锁屏
        }
    }

}
