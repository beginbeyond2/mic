package com.micsig.tbook.tbookscope.middleware.command;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.micsig.base.Logger;
import com.micsig.smart.Property;
import com.micsig.smart.PropertyManage;
import com.micsig.tbook.hardware.Hardware;
import com.micsig.tbook.scope.Scope;
import com.micsig.tbook.scope.channel.Channel;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.tbookscope.BuildConfig;
import com.micsig.tbook.tbookscope.tools.LockScreenUtils;
import com.micsig.tbook.tbookscope.tools.ShellUtils;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.util.App;

import java.io.File;

public class Command_Production {
    private static final String TAG="Command_Production";
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

     private boolean isPrivateStart =false;


     public void Ware(boolean isUpdateUI){
         Logger.i(TAG,"ware");
     }
     public void Application(boolean isUpdateUI){
         Logger.i(TAG,"application");
     }


     private void displayInfo(){
         PropertyManage propertyManage = PropertyManage.getInstance();
         propertyManage.update();
         Property property = propertyManage.getProperty();
         Log.d(TAG,"type:" + property.getType());
         Log.d(TAG,"sn:" + property.getSN() + "," + property.getDisplaySN());
         Log.d(TAG,"bandWidth:" + property.getBandWidth());
     }
     public boolean WriteQ(String productID,String makeDate,String SN,String HardwareVersion,String serial,boolean isUpdateUI){
         if(!isPrivateStart) return false;

         Logger.i(TAG,"WriteQ");
         if (productID.equals("")|| makeDate.equals("")|| SN.equals("") || HardwareVersion.equals("") || serial.equals(""))
             return false;

         Logger.i(TAG,"productId:"+productID+" makeDate:"+makeDate+"  sn:"+SN+"  version:"+HardwareVersion+"  serial:"+serial);

         PropertyManage propertyManage = PropertyManage.getInstance();
         Property property = propertyManage.getProperty();
         property.setType(productID); //TEST

         property.setSN(SN); //sn
         property.setHwVersion(HardwareVersion); //1
         property.serialCodeUpgrade(serial.replace("-",""));

         String ver = App.getFwVersion(property);
         propertyManage.commit();
         Scope.getInstance().setUsbInfo(property.getType(),property.getDisplaySN(),ver);

//         displayInfo();
         return true;
     }

     public String SNQ(boolean isUpdateUI){
         Logger.i(TAG,"SNQ");
         PropertyManage propertyManage = PropertyManage.getInstance();
         propertyManage.update();
         String sn=propertyManage.getProperty().getSN();
         if (sn.length()>0){
             return sn;
         }
         else {
             return "failed";
         }
     }
    public String SysId(boolean isUpdateUI){
        return Hardware.getInstance().getSysId();
    }
     public String WDate(String param1,boolean isUpdateUI){
         Logger.i(TAG,"date:"+param1);
         if(!isPrivateStart) return "---";
         param1 = param1.replace("\n","").replace("\r","").trim();
         if (param1.equalsIgnoreCase("clear")){
             param1="";
         }
         PropertyManage propertyManage = PropertyManage.getInstance();
         propertyManage.getProperty().setDeliveryDate(param1);
         propertyManage.commit();
         return param1;
     }
     public String UUIDQ(boolean isUpdateUI){
         Logger.i(TAG,"UUIDQ");
         PropertyManage propertyManage = PropertyManage.getInstance();
         propertyManage.update();
         return propertyManage.getProperty().getUUID();
//         return HwManager.getInstance().getMachineUUID();
     }
     public String HWVersionQ(boolean isUpdateUI){
         Logger.i(TAG,"HWVersionQ");
         PropertyManage propertyManage = PropertyManage.getInstance();
         propertyManage.update();
         return propertyManage.getProperty().getHwVersion();
//         return String.valueOf(HwManager.getInstance().getHwVersion());
     }
     public String SerialiNoQ(boolean isUpdateUI){
         Logger.i(TAG,"SerialiNoQ");
         PropertyManage propertyManage = PropertyManage.getInstance();
         propertyManage.update();
         return propertyManage.getProperty().getSN();
//         return PropertyManage.getInstance().getProperty().getSN();
     }
     public boolean StringCode(String stringCode,boolean isUpdateUI){
         Logger.i(TAG,"StringCode stringCode:"+stringCode);
         if(!isPrivateStart) return false;
         boolean b=true;
         PropertyManage propertyManage = PropertyManage.getInstance();
         b = propertyManage.getProperty().serialCodeUpgrade(stringCode.replace("-",""));
         if (b) propertyManage.commit();
         Log.d(TAG,"b:" + b);
//         displayInfo();
         return b;
     }
     public boolean DisplaySeriaiNo(String outSerialNo,boolean isUpdateUI){
         Logger.i(TAG,"DisplaySeriaiNo outSerialNo:"+outSerialNo);
         if(!isPrivateStart) return false;
         boolean b=true;
         PropertyManage propertyManage = PropertyManage.getInstance();
         Property property = propertyManage.getProperty();
         property.setDisplaySN(outSerialNo);
         String ver = App.getFwVersion(property);
         propertyManage.commit();
         Scope.getInstance().setUsbInfo(property.getType(),property.getDisplaySN(),ver);
         return b;
     }
     public boolean MachineType(String machineType,boolean isUpdateUI){
         if(!isPrivateStart) return false;
         Logger.i(TAG,"MachineType machineType:"+machineType);
         boolean b=true;
         PropertyManage propertyManage = PropertyManage.getInstance();
         Property property = propertyManage.getProperty();
         property.setType(machineType);
         String ver = App.getFwVersion(property);
         propertyManage.commit();
         Scope.getInstance().setUsbInfo(property.getType(),property.getDisplaySN(),ver);
         return b;
     }

     public void Star(String jiamiKey,boolean isUpdateUI){
         Logger.i(TAG,"Star");
         if (isPrivateStart) return;
         PropertyManage propertyManage = PropertyManage.getInstance();
         propertyManage.update();
         String s= propertyManage.getProperty().getPrivateUUID().trim();
         jiamiKey = jiamiKey.replaceAll("\n","").replaceAll("\r","").trim();
         Logger.i(TAG,"Star jiamiKey:"+jiamiKey+"  s:"+s);
         if (jiamiKey.equalsIgnoreCase(s)){
             Logger.i(TAG,"Star privatestate:"+isPrivateStart);
             isPrivateStart=true;
         }
     }
     public boolean StarQ(boolean isUpdateUI){
         Logger.i(TAG,"StarQ state:"+isPrivateStart);
         return isPrivateStart;
     }

     public void Stop(boolean isUpdateUI){
         Logger.i(TAG,"Stop");
         isPrivateStart=false;
     }

     public void BandWidth(int bandwidth,boolean isUpdateUI){
         Logger.i(TAG,"BandWidth:" + bandwidth);

//         if(BuildConfig.DEBUG){
//             Channel.setMaxBandWidth(bandwidth*1000*1000L);
//             Channel channel = ChannelFactory.getDynamicChannel(ChannelFactory.CH1);
//             if(channel != null) {
//                 channel.bandWidthTypeChange();
//             }
//         }

         if(!isPrivateStart) return;

         PropertyManage propertyManage = PropertyManage.getInstance();
         propertyManage.getProperty().setBandWidth(bandwidth);
         propertyManage.commit();
//         displayInfo();
     }

     public void SettingClear(boolean isUpdateUI){
         Logger.i(TAG,"setting clear!");
         if(!isPrivateStart) return;
         PropertyManage.getInstance().clear();
     }

     public void Time(String s1,String s2,String s3 ,String s4,String s5,boolean isUpdateUI){

     }

     //清楚clean
     public void Clean(boolean isUpdateUI){

         Logger.i(TAG,"integer:clean");
         String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + Tools.SMART_PATH + File.separator;
         Logger.i(TAG,"path:"+path);
         ShellUtils.execCommand("rm -rf "+path,false);

         path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + Tools.SCOPE_PATH + File.separator;
         Logger.i(TAG,"path:"+path);
         ShellUtils.execCommand("rm -rf "+path,false);

         path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/RecordScreen/";
         ShellUtils.execCommand("rm -rf "+path,false);

         path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/";
         ShellUtils.execCommand("rm -rf "+path,false);

         path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Pictures/";
         ShellUtils.execCommand("rm -rf "+path,false);

         path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.appMarkerCache/";
         ShellUtils.execCommand("rm -rf "+path,false);
//        FileUtils.deleteDir(path);
         Tools.uDiskUpdate(path, App.get().getApplicationContext());
     }

    public void Shutdown(){
        PowerManagerUtils.shutdown(App.get().getApplicationContext());
    }

    public void Restart(){
        PowerManagerUtils.reboot(App.get().getApplicationContext());
    }

    public void Standby(){
        PowerManagerUtils.enterStandby(App.get().getApplicationContext());
    }

    public void Wakeup(){
        PowerManagerUtils.exitStandby(App.get().getApplicationContext());
    }

    public void Lock(){
        Context context = App.get().getApplicationContext();
        if(!LockScreenUtils.isLockScreen(context)){
            LockScreenUtils.setLockScreen(context,true);
        }
    }

    public void Unlock(){
        Context context = App.get().getApplicationContext();
        if(LockScreenUtils.isLockScreen(context)){
            LockScreenUtils.setLockScreen(context,false);
        }
    }

}
