package com.micsig.tbook.tbookscope.scpi;

import android.os.Build; // Android版本判断(用于SharedMemory兼容性)
import android.os.SharedMemory; // Android共享内存(用于波形数据高效传输)
import android.system.ErrnoException; // 共享内存创建可能抛出的系统错误
import android.util.Log; // Android日志

import com.micsig.base.Logger; // 应用日志工具
import com.micsig.tbook.tbookscope.services.SCPI.client.ScpiOnBindService; // SCPI服务绑定接口(用于消息回传)
import com.micsig.tbook.ui.util.StrUtil; // 字符串工具(判空)

import java.lang.reflect.InvocationTargetException; // 反射调用异常
import java.lang.reflect.Method; // 反射方法(用于动态调用SCPI_xxx命令处理方法)
import java.nio.ByteBuffer; // NIO字节缓冲区(用于共享内存读写)

/**
 * Created by liwb on 2018/1/11.
 */

/*******************************************************************************
 *   +-----------------------------------------------------------------------+   *
 *   |          SCPICommandDeal - SCPI命令分发核心引擎                          |   *
 *   +-----------------------------------------------------------------------+   *
 *   |  模块定位: SCPI命令体系的Java层中枢，连接C层JNI解析与Java层各SCPI_xxx命令处理类 |   *
 *   |  核心职责: 维护SCPI命令映射表，接收JNI解析回调，通过反射分发给对应命令处理方法   |   *
 *   |  架构设计: 单例模式(静态内部类持有者)，命令表为SCPICommandStruct数组，            |   *
 *   |           每条命令映射(命令字符串,类名,方法名)，deal()通过反射调用              |   *
 *   |  数据流向: 上位机SCPI命令 → scpiParser() → JNI.scpiCommand() → deal()          |   *
 *   |           → 反射调用SCPI_xxx方法 → 结果返回 → sendMessage() → 上位机            |   *
 *   |  依赖关系: 依赖SCPIParam(参数)、所有SCPI_xxx命令处理类(反射调用)、               |   *
 *   |           ScpiOnBindService(消息回传)、SharedMemory(波形数据传输)              |   *
 *   |  使用场景: 上位机通过USB/LAN/Socket发送SCPI命令，经C层JNI解析后由本类分发执行   |   *
 *   +-----------------------------------------------------------------------+   *
 ******************************************************************************/
public class SCPICommandDeal {
    private static final String TAG="SCPICommandDeal"; // 日志标签
   static {System.loadLibrary("SCPI");} // 加载C层SCPI解析JNI库(libSCPI.so)

    //region 单例
    /** 静态内部类持有者模式实现单例，保证线程安全的延迟初始化 */
    private static class SCPICommandDealHolder {
        private static final SCPICommandDeal instance = new SCPICommandDeal(); // 单例实例
    }

    /**
     * 获取SCPICommandDeal单例实例。
     * @return 全局唯一的SCPICommandDeal实例
     */
    public static final SCPICommandDeal getInstance() {
        return SCPICommandDealHolder.instance; // 返回静态内部类持有的单例
    }
    //endregion

    private ScpiOnBindService scpiService; // SCPI服务绑定接口，用于向客户端回传命令执行结果
    private SCPIParam param; // SCPI参数对象，每条命令解析后填充此对象

    /** 共享内存大小：10MB，用于波形数据的高效传输 */
    public static final int SHARED_MEM_SIZE = 10 * 1024 * 1024; // 10MB共享内存

    /**
     * 私有构造函数(单例模式)。
     * 初始化SCPIParam参数对象，并在Android 8.1+上创建共享内存区域。
     */
    public SCPICommandDeal(){
        param=new SCPIParam(); // 创建SCPI参数对象
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) { // Android 8.1+才支持SharedMemory
            try {
                sharedMem= SharedMemory.create("wave_tran",SHARED_MEM_SIZE); // 创建10MB共享内存，名称"wave_tran"
            } catch (ErrnoException e) {
                e.printStackTrace(); // 共享内存创建失败时打印异常
            }
        }
    }

    /**
     * 初始化SCPI服务绑定接口。
     * 在SCPI服务启动时调用，设置消息回传通道。
     * @param scpiService SCPI服务绑定接口
     */
    public void init(ScpiOnBindService scpiService){
        this.scpiService=scpiService; // 保存SCPI服务引用，用于后续sendMessage回传结果
    }

    /**
     * SCPI命令映射结构体。
     * 每个实例对应一条SCPI命令的映射关系：
     * command = SCPI命令字符串(如":CHANnel:DISPlay")
     * clasz  = 命令处理类名(如"SCPI_Channel")
     * method = 命令处理方法名(如"Display")
     */
    class SCPICommandStruct{
        private String command; // SCPI命令字符串
        private  String clasz; // 命令处理类名(在com.micsig.tbook.tbookscope.scpi包下)
        private String method; // 命令处理方法名

        /**
         * 构造SCPI命令映射结构体。
         * @param command SCPI命令字符串
         * @param clasz   命令处理类名
         * @param method  命令处理方法名
         */
        public SCPICommandStruct(String command,String clasz,String method){
             this.command=command; // 保存命令字符串
            this.clasz=clasz; // 保存处理类名
            this.method=method; // 保存处理方法名
        }
    }

    //region scpi_commands

    /**
     * SCPI命令映射表：命令字符串 → (处理类名, 处理方法名)
     * C层JNI解析SCPI命令后返回commandIndex，本数组通过索引定位对应的处理类和方法。
     * 数组索引与C层scpi_commands数组的索引必须一一对应。
     */
    private SCPICommandStruct[] scpi_commands = {
            //公有命令
            new SCPICommandStruct("*CLS","SCPI_Common","CLS"),//清除状态
            new SCPICommandStruct("*ESE","SCPI_Common","ESE"),//设置事件使能寄存器
            new SCPICommandStruct("*ESE?","SCPI_Common","ESEQ"),//查询事件使能寄存器
            new SCPICommandStruct("*ESR","SCPI_Common","ESR"),//设置事件状态寄存器
            new SCPICommandStruct("*IDN?","SCPI_Common","IDNQ"),//查询设备标识
            new SCPICommandStruct("*OPC","SCPI_Common","OPC"),//设置操作完成
            new SCPICommandStruct("*OPC?","SCPI_Common","OPCQ"),//查询操作完成
            new SCPICommandStruct("*RST","SCPI_Common","RST"),//复位设备
            new SCPICommandStruct("*SRE","SCPI_Common","SRE"),//设置服务请求使能
            new SCPICommandStruct("*SRE?","SCPI_Common","SREQ"),//查询服务请求使能
            new SCPICommandStruct("*STB?","SCPI_Common","STBQ"),//查询状态字节
            new SCPICommandStruct("*TST?","SCPI_Common","TSTQ"),//自检查询
            new SCPICommandStruct("*WAI","SCPI_Common","WAI"),//等待操作完成

            //菜单功能命令
//            new SCPICommandStruct(":AUTO","SCPI_FunctionMenu","Auto"),
//            new SCPICommandStruct(":RUN","SCPI_FunctionMenu","Run"),

            new SCPICommandStruct(":AUTO","SCPI_FunctionMenu","Auto"),//自动
            new SCPICommandStruct(":RUN","SCPI_FunctionMenu","Run"),//使示波器开始运行，符合触发条件，开始采集数据
            new SCPICommandStruct(":STOP","SCPI_FunctionMenu","Stop"),//使示波器停止运行，数据采集停止
            new SCPICommandStruct(":SINGle","SCPI_FunctionMenu","Single"),//将示波器设置为单序列，示波器捕获并显示单次采集
            new SCPICommandStruct(":MULTiple","SCPI_FunctionMenu","Multiple"),//将示波器设置为连续触发方式
            new SCPICommandStruct(":BEEP","SCPI_FunctionMenu","Beep"),//设置示波器的蜂鸣状态
            new SCPICommandStruct(":VERSion?","SCPI_FunctionMenu","VersionQ"),//获取SCPI版本
            //校准命令 CAL
            new SCPICommandStruct(":CALibrate:DATE?","SCPI_Calibrate","DateQ"),//查询上次校准时间
            new SCPICommandStruct(":CALibrate:STARt","SCPI_Calibrate","Start"),//开始校准
            new SCPICommandStruct(":CALibrate:QUIT","SCPI_Calibrate","Quit"),//退出校准，校准完成后的操作
            new SCPICommandStruct(":CALibrate:STOP","SCPI_Calibrate","Stop"),//停止校准，强制停止
            new SCPICommandStruct(":CALibrate:RESult?","SCPI_Calibrate","ResultQ"),//查询校准结果
            new SCPICommandStruct(":CALibrate:ZERopoint","SCPI_Calibrate","ZeroPoint"),//零点校准
            new SCPICommandStruct(":CALibrate:ZERopoint?","SCPI_Calibrate","ZeroPointQ"),//查询零点校准状态
            new SCPICommandStruct(":CALibrate:CHDF","SCPI_Calibrate","Chdf"),//通道差异校准
            new SCPICommandStruct(":CALibrate:CHDF?","SCPI_Calibrate","ChdfQ"),//查询通道差异校准状态
            new SCPICommandStruct(":CALibrate:ADPHa","SCPI_Calibrate","Adpha"),//AD相位校准
            new SCPICommandStruct(":CALibrate:ADPHa?","SCPI_Calibrate","AdphaQ"),//查询AD相位校准状态
            new SCPICommandStruct(":CALibrate:ADGain","SCPI_Calibrate","AdGain"),//AD增益校准
            new SCPICommandStruct(":CALibrate:ADGain?","SCPI_Calibrate","AdGinQ"),//查询AD增益校准状态

            new SCPICommandStruct(":CALibrate:OFFSet","SCPI_Calibrate","Offset"),//偏移量校准
            new SCPICommandStruct(":CALibrate:OFFSet?","SCPI_Calibrate","OffsetQ"),//查询偏移量校准状态
            new SCPICommandStruct(":CALibrate:CHGain","SCPI_Calibrate","ChGain"),//通道增益校准
            new SCPICommandStruct(":CALibrate:CHGain?","SCPI_Calibrate","ChGainQ"),//查询通道增益校准状态
            new SCPICommandStruct(":CALibrate:XCHGain","SCPI_Calibrate","ExChGain"),//通道增益校准
            new SCPICommandStruct(":CALibrate:XCHGain?","SCPI_Calibrate","ExChGainQ"),//查询通道增益校准状态
            new SCPICommandStruct(":CALibrate:CHSetVal","SCPI_Calibrate","ChSetVal"),//查询通道增益校准状态
            new SCPICommandStruct(":CALibrate:CHVal?","SCPI_Calibrate","ChValQ"),//查询通道增益校准状态
            new SCPICommandStruct(":CALibrate:CHCofit","SCPI_Calibrate","ChCofit"),//通道增益校准
            new SCPICommandStruct(":CALibrate:CHCofit?","SCPI_Calibrate","ChCofitQ"),//查询通道增益校准状态
            new SCPICommandStruct(":CALibrate:CHCap","SCPI_Calibrate","ChCap"),//通道增益校准
            new SCPICommandStruct(":CALibrate:CHCap?","SCPI_Calibrate","ChCapQ"),//查询通道增益校准状态
            new SCPICommandStruct(":CALibrate:CapVal","SCPI_Calibrate","CapVal"),//通道增益校准
            new SCPICommandStruct(":CALibrate:CapVal?","SCPI_Calibrate","CapValQ"),//查询通道增益校准状态
            new SCPICommandStruct(":CALibrate:UPCal","SCPI_Calibrate","UpCal"),//通道增益校准
            new SCPICommandStruct(":CALibrate:UPCal?","SCPI_Calibrate","UpCalQ"),//查询通道增益校准状态
            new SCPICommandStruct(":CALibrate:DOWNCal","SCPI_Calibrate","DownCal"),//通道增益校准
            new SCPICommandStruct(":CALibrate:DOWNCal?","SCPI_Calibrate","DownCalQ"),//查询通道增益校准状态
            new SCPICommandStruct(":CALibrate:TRIGger:ZERopoint","SCPI_Calibrate","Trigger_ZeroPoint"),//触发触发零点校准
            new SCPICommandStruct(":CALibrate:TRIGger:ZERopoint?","SCPI_Calibrate","Trigger_ZeroPointQ"),//查询触发零点校准状态
            new SCPICommandStruct(":CALibrate:TRIGger:AC:ZERopoint","SCPI_Calibrate","Trigger_AC_ZeroPoint"),//触发触发零点校准
            new SCPICommandStruct(":CALibrate:TRIGger:AC:ZERopoint?","SCPI_Calibrate","Trigger_AC_ZeroPointQ"),//查询触发零点校准状态
            new SCPICommandStruct(":CALibrate:TRIGger:COEFficient","SCPI_Calibrate","Trigger_Coefficient"),//触发系数校准
            new SCPICommandStruct(":CALibrate:TRIGger:COEFficient?","SCPI_Calibrate","Trigger_CoefficientQ"),//查询触发系数校准状态
            new SCPICommandStruct(":CALibrate:TRIGger:PRECise","SCPI_Calibrate","Trigger_Precise"),//精准触发校准
            new SCPICommandStruct(":CALibrate:TRIGger:PRECise?","SCPI_Calibrate","Trigger_PreciseQ"),//查询精准触发校准状态
            new SCPICommandStruct(":CALibrate:DATE:LENGth?","SCPI_Calibrate","Date_LengthQ"),//查询校准数据长度
            new SCPICommandStruct(":CALibrate:DATE:GET","SCPI_Calibrate","Date_Get"),//获取校准数据
            new SCPICommandStruct(":CALibrate:FILE:RESet?","SCPI_Calibrate","File_ResetQ"),//获取校准数据

            //波形命令 WAV
            new SCPICommandStruct(":WAVeform:SOURce","SCPI_Waveform","Source"),//设置波形读取的通道源
            new SCPICommandStruct(":WAVeform:SOURce?","SCPI_Waveform","SourceQ"),//查询波形读取的通道源
            new SCPICommandStruct(":WAVeform:MODE","SCPI_Waveform","Mode"),//设置波形的读取模式
            new SCPICommandStruct(":WAVeform:MODE?","SCPI_Waveform","ModeQ"),//查询波形的读取模式
            new SCPICommandStruct(":WAVeform:FORMat","SCPI_Waveform","Format"),//设置波形的读取模式
            new SCPICommandStruct(":WAVeform:FORMat?","SCPI_Waveform","FormatQ"),//查询波形的读取模式
            new SCPICommandStruct(":WAVeform:STARt","SCPI_Waveform","Start"),//设置内存中波形被读取的起始位置
            new SCPICommandStruct(":WAVeform:STARt?","SCPI_Waveform","StartQ"),//查询内存中波形被读取的起始位置
            new SCPICommandStruct(":WAVeform:STOP","SCPI_Waveform","Stop"),//设置内存中波形被读取的停止位置
            new SCPICommandStruct(":WAVeform:STOP?","SCPI_Waveform","StopQ"),//查询内存中波形被读取的停止位置
            new SCPICommandStruct(":WAVeform:DATA?","SCPI_Waveform","DataQ"),//读取波形数据
            new SCPICommandStruct(":WAVeform:DATA:BIN?","SCPI_Waveform","DataBinQ"),//读取波形数据
            new SCPICommandStruct(":WAVeform:DATA:HEX?","SCPI_Waveform","DataHexQ"),//读取波形数据16进制
            new SCPICommandStruct(":WAVeform:DATA:ASCIi?","SCPI_Waveform","DataAsciiQ"),//读取波形数据ascii
            new SCPICommandStruct(":WAVeform:PREamble?","SCPI_Waveform","PreambleQ"),//查询全部的波形参数
            new SCPICommandStruct(":WAVeform:XINCrement?","SCPI_Waveform","XincrementQ"),//查询指定源x方向上相邻两点的时间差
            new SCPICommandStruct(":WAVeform:XORigin?","SCPI_Waveform","XoriginQ"),//查询指定源x方向从触发点到参考时间基准的时间
            new SCPICommandStruct(":WAVeform:XREFerence?","SCPI_Waveform","XreferenceQ"),//查询指定源x方向上数据点的参考时间基准
            new SCPICommandStruct(":WAVeform:YINCrement?","SCPI_Waveform","YincrementQ"),//查询指定源y方向上相邻两点的时间差
            new SCPICommandStruct(":WAVeform:YORigin?","SCPI_Waveform","YoriginQ"),//查询指定源y方向从触发点到参考时间基准的时间
            new SCPICommandStruct(":WAVeform:YREFerence?","SCPI_Waveform","YReferenceQ"),//查询指定源y方向上数据点的参考时间基准

//            new SCPICommandStruct(":WAVeform:BEGin","SCPI_Waveform","Begin"),//启动波形的读取

//            new SCPICommandStruct(":WAVeform:END","SCPI_Waveform","End"),//停止波形的读取


//            new SCPICommandStruct(":WAVeform:RESet","SCPI_Waveform","Reset"),//复位波形的读取


//            new SCPICommandStruct(":WAVeform:STATus?","SCPI_Waveform","StatusQ"),//查询当前的波形读取状态



            //菜单命令 MEN
            new SCPICommandStruct(":MENU:AUTO","SCPI_Menu","Auto"),//自动
            new SCPICommandStruct(":MENU:AUTO?","SCPI_Menu","AutoQ"),//自动
            new SCPICommandStruct(":MENU:RUN","SCPI_Menu","Run"),//使示波器开始运行，符合触发条件，开始采集数据
            new SCPICommandStruct(":MENU:STOP","SCPI_Menu","Stop"),//使示波器停止运行，数据采集停止
            new SCPICommandStruct(":MENU:SINGle","SCPI_Menu","Single"),//将示波器设置为单序列，示波器捕获并显示单次采集
            new SCPICommandStruct(":MENU:MULTiple","SCPI_Menu","Multiple"),//将示波器设置为连续触发方式
            new SCPICommandStruct(":MENU:BEEP","SCPI_Menu","Beep"),//设置示波器的蜂鸣状态
            new SCPICommandStruct(":MENU:HALF:CHANnel","SCPI_Menu","Half_Channel"),//将通道位置设置为垂直零点位置（波形显示区垂直中心）
            new SCPICommandStruct(":MENU:HALF:TRIGpos","SCPI_Menu","TrigPos"),//设置触发位置到屏幕中间
            new SCPICommandStruct(":MENU:HALF:XCURsor","SCPI_Menu","Xcursor"),//设置通道的垂直光标在50%处
            new SCPICommandStruct(":MENU:HALF:YCURsor","SCPI_Menu","Ycursor"),//设置通道的水平光标在50%处
            new SCPICommandStruct(":MENU:HALF:LEVel","SCPI_Menu","Level"),//将触发电平设置为触发信号幅值的中间位置
            new SCPICommandStruct(":MENU:HOMepage","SCPI_Menu","HomePage"),//设置示波器回到主界面
            new SCPICommandStruct(":MENU:RETurn","SCPI_Menu","Return"),//设置退出示波器程序，返回主界面
            new SCPICommandStruct(":MENU:LOCK","SCPI_Menu","Lock"),//锁定示波器屏幕
            new SCPICommandStruct(":MENU:LOCK?","SCPI_Menu","LockQ"),//锁定示波器屏幕
            new SCPICommandStruct(":MENU:UNLock","SCPI_Menu","Unlock"),//解锁示波器屏幕
            new SCPICommandStruct(":MENU:COUNter","SCPI_Menu","Counter"),//频率计的打开与关闭
            new SCPICommandStruct(":MENU:COUNter?","SCPI_Menu","CounterQ"),//频率计的打开与关闭查询
            new SCPICommandStruct(":MENU:RESet","SCPI_Menu","Reset"),//恢复出厂设置
            new SCPICommandStruct(":MENU:MEASure","SCPI_Menu","Measure"),//打开测量菜单
            new SCPICommandStruct(":MENU:TRIGger","SCPI_Menu","Trigger"),//打开触发菜单
            new SCPICommandStruct(":MENU:CHANnel","SCPI_Menu","Channel"),//打开通道
            new SCPICommandStruct(":MENU:CHANnel?","SCPI_Menu","ChannelQ"),//查询通道菜单是否打开
            new SCPICommandStruct(":MENU:QUICk","SCPI_Menu","Quick"),//打开底部滑动菜单
            new SCPICommandStruct(":MENU:QUICk?","SCPI_Menu","QuickQ"),//查询底部滑动菜单是否打开
            new SCPICommandStruct(":MENU:MAIN","SCPI_Menu","Main"),//打开主菜单
            new SCPICommandStruct(":MENU:MAIN?","SCPI_Menu","MainQ"),//查询主菜单是否打开
            new SCPICommandStruct(":MENU:AUX:TRIGger","SCPI_Menu","Aux_trigger"), //userSet->aux->trigger
            new SCPICommandStruct(":MENU:AUX:TRIGger?","SCPI_Menu","Aux_triggerQ"),
            new SCPICommandStruct(":MENU:AUX:CLOCk","SCPI_Menu","Aux_clock"),  //userSet->aux->clock
            new SCPICommandStruct(":MENU:AUX:CLOCk?","SCPI_Menu","Aux_clockQ"),
            new SCPICommandStruct(":MENU:AUX:INPutres","SCPI_Menu","Aux_Inputres"), //userSet->aux->inputres
            new SCPICommandStruct(":MENU:AUX:INPutres?","SCPI_Menu","Aux_InputresQ"),

            //采样命令 旧版
            new SCPICommandStruct(":SAMPle:TYPE","SCPI_Sample","Type"),//设置采样方式
            new SCPICommandStruct(":SAMPle:TYPE?","SCPI_Sample","TypeQ"),//查询采样方式
            new SCPICommandStruct(":SAMPle:MEAN","SCPI_Sample","Mean"),//设置平均采样次数。所设置的值为2的整数倍数。
            new SCPICommandStruct(":SAMPle:MEAN?","SCPI_Sample","MeanQ"),//查询平均采样次数
            //采样命令 新版：SAMP->acquire
            new SCPICommandStruct(":ACQuire:TYPE","SCPI_Sample","Type"),//设置采样方式
            new SCPICommandStruct(":ACQuire:TYPE?","SCPI_Sample","TypeQ"),//查询采样方式
            new SCPICommandStruct(":ACQuire:MEAN","SCPI_Sample","Mean"),//设置平均采样次数。所设置的值为2的整数倍数。
            new SCPICommandStruct(":ACQuire:MEAN?","SCPI_Sample","MeanQ"),//查询平均采样次数
            new SCPICommandStruct(":ACQuire:ENVelop","SCPI_Sample","Envelop"),//设置包络采样次数。所设置的值为2的整数倍数或无穷
            new SCPICommandStruct(":ACQuire:ENVelop?","SCPI_Sample","EnvelopQ"),//查询包络采样次数
            new SCPICommandStruct(":ACQuire:SEGMented","SCPI_Sample","SegMented"),//设置分段存储开启与关闭
            new SCPICommandStruct(":ACQuire:SEGMented?","SCPI_Sample","SegMentedQ"),//设置分段存储开启与关闭
            new SCPICommandStruct(":ACQuire:SEGMented:NO?","SCPI_Sample","SegmentedNoQ"),//查询已存在段数
            new SCPICommandStruct(":ACQuire:SEGMented:QTY","SCPI_Sample","SegmentedQTY"),//设置段数
            new SCPICommandStruct(":ACQuire:SEGMented:QTY?","SCPI_Sample","SegmentedQTYQ"),//查询段数
            new SCPICommandStruct(":ACQuire:SEGMented:IS10000?","SCPI_Sample","SegmentedIs10000Q"),//是否有1W段
            new SCPICommandStruct(":ACQuire:SEGMented:Max?","SCPI_Sample","SegmentedMaxQ"),//最大段数
            new SCPICommandStruct(":ACQuire:SEGMented:DISType","SCPI_Sample","SegmentedDisplayType"),//设置显示类型
            new SCPICommandStruct(":ACQuire:SEGMented:DISType?","SCPI_Sample","SegmentedDisplayTypeQ"),//查询显示类型
            new SCPICommandStruct(":ACQuire:SEGMented:ORDer","SCPI_Sample","SegmentedOrder"),//set play order
            new SCPICommandStruct(":ACQuire:SEGMented:ORDer?","SCPI_Sample","SegmentedOrderQ"),//query play order
            new SCPICommandStruct(":ACQuire:SEGMented:PLAY","SCPI_Sample","SegmentedPlay"),//segment play
            new SCPICommandStruct(":ACQuire:SEGMented:STOP","SCPI_Sample","SegmentedStop"),//segment stop
            new SCPICommandStruct(":ACQuire:SEGMented:FRA1","SCPI_Sample","SegmentedFra1"),//set segment frame1
            new SCPICommandStruct(":ACQuire:SEGMented:FRA1?","SCPI_Sample","SegmentedFra1Q"),//query segment frame1
            new SCPICommandStruct(":ACQuire:SEGMented:FRA2","SCPI_Sample","SegmentedFra2"),//set segment frame2
            new SCPICommandStruct(":ACQuire:SEGMented:FRA2?","SCPI_Sample","SegmentedFra2Q"),//query segment frame2
            new SCPICommandStruct(":ACQuire:SEGMented:FRA3","SCPI_Sample","SegmentedFra3"),//set segment frame3
            new SCPICommandStruct(":ACQuire:SEGMented:FRA3?","SCPI_Sample","SegmentedFra3Q"),//query segment frame3
            new SCPICommandStruct(":ACQuire:SEGMented:PLAY:SPED","SCPI_Sample","SegmentedPlaySpeed"),//set play speed
            new SCPICommandStruct(":ACQuire:SEGMented:PLAY:SPED?","SCPI_Sample","SegmentedPlaySpeedQ"),//query play speed

            new SCPICommandStruct(":ACQuire:SRATe?","SCPI_Sample","SrateQ"),//查询当前的采样率
            new SCPICommandStruct(":ACQuire:DEPSelect","SCPI_Sample","MdepthSelect"),//设置当前存储尝试
            new SCPICommandStruct(":ACQuire:DEPSelect?","SCPI_Sample","MdepthSelectQ"),//查询示波器当前存储深度
            new SCPICommandStruct(":ACQuire:DEPSelect:INDEx","SCPI_Sample","MdepthSelectIndex"),//设置当前存储尝试
            new SCPICommandStruct(":ACQuire:DEPth?","SCPI_Sample","MdepthQ"),//查询示波器当前存储深度
            new SCPICommandStruct(":ACQuire:DEPth:RANGe?","SCPI_Sample","RangeQ"),//查询示波器当前存储深度
            new SCPICommandStruct(":ACQuire:DEPth:INIT?","SCPI_Sample","InitQ"),//查询示波器原始存储深度

            //通道命令 CHAN 1.0协议
            new SCPICommandStruct(":CHANnel:DISPlay","SCPI_Channel","Display"),//通道的打开或关闭
            new SCPICommandStruct(":CHANnel:DISPlay?","SCPI_Channel","DisplayQ"),//查询通道的打开或关闭
            new SCPICommandStruct(":CHANnel:INVerse","SCPI_Channel","Inverse"),//打开或关闭通道的反相显示
            new SCPICommandStruct(":CHANnel:INVerse?","SCPI_Channel","InverseQ"),//查询通道的反相显示
            new SCPICommandStruct(":CHANnel:INVert","SCPI_Channel","Inverse"),//打开或关闭通道的反相显示
            new SCPICommandStruct(":CHANnel:INVert?","SCPI_Channel","InverseQ"),//查询通道的反相显示
            new SCPICommandStruct(":CHANnel:BAND","SCPI_Channel","Band"),//设置通道的带宽限制
            new SCPICommandStruct(":CHANnel:BAND?","SCPI_Channel","BandQ"),//查询通道的带宽限制
            new SCPICommandStruct(":CHANnel:BAND:VALUe?","SCPI_Channel","BandValueQ"),//查询通道的带宽限制
            new SCPICommandStruct(":CHANnel:PRTY","SCPI_Channel","Prty"),//设置通道的探针类型
            new SCPICommandStruct(":CHANnel:PRTY?","SCPI_Channel","PrtyQ"),//查询通道的探针类型
            new SCPICommandStruct(":CHANnel:PROBe","SCPI_Channel","Probe"),//设置探头的衰减比
            new SCPICommandStruct(":CHANnel:PROBe?","SCPI_Channel","ProbeQ"),//查询探头的衰减比
            new SCPICommandStruct(":CHANnel:COUPle","SCPI_Channel","Couple"),//设置通道输入耦合方式
            new SCPICommandStruct(":CHANnel:COUPle?","SCPI_Channel","CoupleQ"),//查询通道输入耦合方式
            new SCPICommandStruct(":CHANnel:INPutres","SCPI_Channel","Inputres"),//设置通道的输入阻抗
            new SCPICommandStruct(":CHANnel:INPutres?","SCPI_Channel","InputresQ"),//查询通道的输入阻抗
            new SCPICommandStruct(":CHANnel:EXTent","SCPI_Channel","Extent"),//设置指定通道波形显示的垂直档位
            new SCPICommandStruct(":CHANnel:PLUS:EXTent","SCPI_Channel","Plus_Extent"),
            new SCPICommandStruct(":CHANnel:EXTent?","SCPI_Channel","ExtentQ"),//查询指定通道波形显示的垂直档位
            new SCPICommandStruct(":CHANnel:SCALe","SCPI_Channel","Extent"),//设置指定通道波形显示的垂直档位
            new SCPICommandStruct(":CHANnel:PLUS:SCALe","SCPI_Channel","Plus_Extent"),
            new SCPICommandStruct(":CHANnel:SCALe?","SCPI_Channel","ExtentQ"),//查询指定通道波形显示的垂直档位
            new SCPICommandStruct(":CHANnel:POSition","SCPI_Channel","Position"),//设置指定通道波形显示的垂直偏移
            new SCPICommandStruct(":CHANnel:PLUS:POSition","SCPI_Channel","Plus_Position"),//设置指定通道波形显示的垂直偏移
            new SCPICommandStruct(":CHANnel:POSition?","SCPI_Channel","PositionQ"),//查询指定通道波形显示的垂直偏移
            new SCPICommandStruct(":CHANnel:VERNier","SCPI_Channel","Vernier"),//打开或关闭指定通道的垂直档位微调功能
            new SCPICommandStruct(":CHANnel:VERNier?","SCPI_Channel","VernierQ"),//查询指定通道的垂直档位微调功能的打开或关闭
            new SCPICommandStruct(":CHANnel:VREF","SCPI_Channel","Vref"),//设置垂直展开基准
            new SCPICommandStruct(":CHANnel:VREF?","SCPI_Channel","VrefQ"),//查询垂直展开基准
            new SCPICommandStruct(":CHANnel:LABel","SCPI_Channel","Label"),//设置垂直展开基准
            new SCPICommandStruct(":CHANnel:LABel?","SCPI_Channel","LabelQ"),//查询垂直展开基准
            new SCPICommandStruct(":CHANnel:LABel:CLEAr","SCPI_Channel","Clear"),//查询垂直展开基准
            new SCPICommandStruct(":CHANnel:DELAy","SCPI_Channel","Delay"),//设置垂直展开基准
            new SCPICommandStruct(":CHANnel:DELAy?","SCPI_Channel","DelayQ"),//查询垂直展开基准
            new SCPICommandStruct(":CHANnel:OFFSet","SCPI_Channel","Offset"),//设置垂直展开基准
            new SCPICommandStruct(":CHANnel:OFFSet?","SCPI_Channel","OffsetQ"),//查询垂直展开基准
            new SCPICommandStruct(":CHANnel:COUNt?","SCPI_Channel","CountQ"),//获取逻辑通道总数
            new SCPICommandStruct(":CURRent:CHANnel","SCPI_Channel","Current"),//设置当前活动通道
            new SCPICommandStruct(":CURRent:CHANnel?","SCPI_Channel","CurrentQ"),//查询当前活动通道
            new SCPICommandStruct(":CHANnel:BAND:IS200M?","SCPI_Channel","Is200MQ"), //是否有200M按钮
            new SCPICommandStruct(":CHANnel:BAND:MAX?","SCPI_Channel","MaxQ"),//最大带宽
            new SCPICommandStruct(":CHANnel:PROBE:INFO?","SCPI_Channel","ProbeInfoQ"), //探针信息
            //1.1版协议
            new SCPICommandStruct(":CHANnel#:DISPlay","SCPI_Channel","Display"), //通道打开或关闭
            new SCPICommandStruct(":CHANnel#:INVerse","SCPI_Channel","Inverse"), //打开或关闭通道的反相显示
            new SCPICommandStruct(":CHANnel#:INVert","SCPI_Channel","Inverse"), //打开或关闭通道的反相显示
            new SCPICommandStruct(":CHANnel#:BAND","SCPI_Channel","Band"),//设置通道的带宽限制
            new SCPICommandStruct(":CHANnel#:BAND?","SCPI_Channel","BandQ"),//查询通道的带宽限制
            new SCPICommandStruct(":CHANnel#:PRTY","SCPI_Channel","Prty"), //设置通道的探针类型
            new SCPICommandStruct(":CHANnel#:PROBe","SCPI_Channel","Probe"),  //设置探头的衰减比
            new SCPICommandStruct(":CHANnel#:COUPle","SCPI_Channel","Couple"),  //设置通道输入耦合方式
            new SCPICommandStruct(":CHANnel#:SCALe","SCPI_Channel","Extent"),  //设置通道波形显示的垂直档位
            new SCPICommandStruct(":CHANnel#:POSition","SCPI_Channel","Position"),  //设置通道波形显示的垂直偏移
            new SCPICommandStruct(":CHANnel#:VERNier","SCPI_Channel","Vernier"), //打开或关闭指定通道的垂直档位微调功能
            new SCPICommandStruct(":CHANnel#:PC","SCPI_Channel","Pc"),   //获取通道波形到上位机
            new SCPICommandStruct(":CHANnel#:INPutres","SCPI_Channel","Inputres"),   //设置阻抗
            new SCPICommandStruct(":CHANnel#:EXTent","SCPI_Channel","Extent"),//设置指定通道波形显示的垂直档位
            new SCPICommandStruct(":CHANnel#:PLUS:EXTent","SCPI_Channel","Plus_Extent"),
            new SCPICommandStruct(":CHANnel#:EXTent?","SCPI_Channel","ExtentQ"),//查询指定通道波形显示的垂直档位
            new SCPICommandStruct(":CHANnel#:SCALe","SCPI_Channel","Extent"),//设置指定通道波形显示的垂直档位
            new SCPICommandStruct(":CHANnel#:PLUS:SCALe","SCPI_Channel","Plus_Extent"),
            new SCPICommandStruct(":CHANnel#:SCALe?","SCPI_Channel","ExtentQ"),//查询指定通道波形显示的垂直档位

            new SCPICommandStruct(":CHANnel#:DISPlay?","SCPI_Channel","DisplayQ"), //通道打开或关闭
            new SCPICommandStruct(":CHANnel#:INVerse?","SCPI_Channel","InverseQ"), //打开或关闭通道的反相显示
            new SCPICommandStruct(":CHANnel#:INVert?","SCPI_Channel","InverseQ"), //打开或关闭通道的反相显示
            new SCPICommandStruct(":CHANnel#:PRTY?","SCPI_Channel","PrtyQ"), //设置通道的探针类型
            new SCPICommandStruct(":CHANnel#:PROBe?","SCPI_Channel","ProbeQ"),  //设置探头的衰减比
            new SCPICommandStruct(":CHANnel#:COUPle?","SCPI_Channel","CoupleQ"),  //设置通道输入耦合方式
            new SCPICommandStruct(":CHANnel#:SCALe?","SCPI_Channel","ExtentQ"),  //设置通道波形显示的垂直档位
            new SCPICommandStruct(":CHANnel#:POSition?","SCPI_Channel","PositionQ"),  //设置通道波形显示的垂直偏移
            new SCPICommandStruct(":CHANnel#:VERNier?","SCPI_Channel","VernierQ"), //打开或关闭指定通道的垂直档位微调功能
            new SCPICommandStruct(":CHANnel#:PC?","SCPI_Channel","PCQ"),   //获取通道波形到上位机
            new SCPICommandStruct(":CHANnel#:INPutres?","SCPI_Channel","InputresQ"),   //查询阻抗状态
            new SCPICommandStruct(":CHANnel#:VREF","SCPI_Channel","Vref"),//设置垂直展开基准
            new SCPICommandStruct(":CHANnel#:VREF?","SCPI_Channel","VrefQ"),//查询垂直展开基准
            new SCPICommandStruct(":CHANnel#:LABel","SCPI_Channel","Label"),//设置标签
            new SCPICommandStruct(":CHANnel#:LABel?","SCPI_Channel","LabelQ"),//查询标签
            new SCPICommandStruct(":CHANnel#:LABel:CLEAr","SCPI_Channel","Clear"),//查询垂直展开基准
            new SCPICommandStruct(":CHANnel#:DELAy","SCPI_Channel","Delay"),//设置垂直展开基准
            new SCPICommandStruct(":CHANnel#:DELAy?","SCPI_Channel","DelayQ"),//查询垂直展开基准
            new SCPICommandStruct(":CHANnel#:CURRent","SCPI_Channel","Current"),//设置当前活动通道
            //数学命令 MATH
            new SCPICommandStruct(":MATH:DISPlay","SCPI_Math","Display"),//打开或关闭数学运算
            new SCPICommandStruct(":MATH:DISPlay?","SCPI_Math","DisplayQ"),//查询数学运算打开或关闭
            new SCPICommandStruct(":MATH:MODE","SCPI_Math","Mode"),//选择数学运算类型
            new SCPICommandStruct(":MATH:MODE?","SCPI_Math","ModeQ"),//查询数学运算类型
            new SCPICommandStruct(":MATH:VREF","SCPI_Math","VRef"),//选择数学运算类型
            new SCPICommandStruct(":MATH:VREF?","SCPI_Math","VRefQ"),//查询数学运算类型
            //base double wave
            new SCPICommandStruct(":MATH:BASE:SOU1","SCPI_Math_BASE","S1"),//选择加法运算的信源1
            new SCPICommandStruct(":MATH:BASE:SOU1?","SCPI_Math_BASE","S1Q"),//查询加法运算的信源1
            new SCPICommandStruct(":MATH:BASE:SOU2","SCPI_Math_BASE","S2"),//选择加法运算的信源2
            new SCPICommandStruct(":MATH:BASE:SOU2?","SCPI_Math_BASE","S2Q"),//查询加法运算的信源2
            new SCPICommandStruct(":MATH:BASE:EXTent","SCPI_Math_BASE","Extent"),//设置加法运算结果的垂直档位
            new SCPICommandStruct(":MATH:BASE:EXTent?","SCPI_Math_BASE","ExtentQ"),//查询加法运算结果的垂直档位
            new SCPICommandStruct(":MATH:BASE:VSCale","SCPI_Math_BASE","Extent"),//设置加法运算结果的垂直档位
            new SCPICommandStruct(":MATH:BASE:VSCale?","SCPI_Math_BASE","ExtentQ"),//查询加法运算结果的垂直档位
            new SCPICommandStruct(":MATH:BASE:VPOSition","SCPI_Math_BASE","Offset"),//设置加法运算结果的垂直偏移
            new SCPICommandStruct(":MATH:BASE:VPOSition?","SCPI_Math_BASE","OffsetQ"),//查询加法运算结果的垂直偏移
            new SCPICommandStruct(":MATH:BASE:OPERator","SCPI_Math_BASE","Operator"),//设置运算符
            new SCPICommandStruct(":MATH:BASE:OPERator?","SCPI_Math_BASE","OperatorQ"),//查询运算符

            //FFT
            new SCPICommandStruct(":MATH:FFT:SOURce","SCPI_Math_FFT","Source"),//选择FFT运算的信源
            new SCPICommandStruct(":MATH:FFT:SOURce?","SCPI_Math_FFT","SourceQ"),//查询FFT运算的信源
            new SCPICommandStruct(":MATH:FFT:WINDow","SCPI_Math_FFT","Window"),//选择FFT运算的窗函数
            new SCPICommandStruct(":MATH:FFT:WINDow?","SCPI_Math_FFT","WindowQ"),//查询FFT运算的窗函数
            new SCPICommandStruct(":MATH:FFT:TYPE","SCPI_Math_FFT","Type"),//选择FFT波形的显示方式
            new SCPICommandStruct(":MATH:FFT:TYPE?","SCPI_Math_FFT","TypeQ"),//查询FFT波形的显示方式
            new SCPICommandStruct(":MATH:FFT:EXTent","SCPI_Math_FFT","Extent"),//设置FFT运算结果的垂直档位
            new SCPICommandStruct(":MATH:FFT:EXTent?","SCPI_Math_FFT","ExtentQ"),//查询FFT运算结果的垂直档位
            new SCPICommandStruct(":MATH:FFT:VSCale","SCPI_Math_FFT","Extent"),//设置FFT运算结果的垂直档位
            new SCPICommandStruct(":MATH:FFT:VSCale?","SCPI_Math_FFT","ExtentQ"),//查询FFT运算结果的垂直档位
            new SCPICommandStruct(":MATH:FFT:VPOSiton","SCPI_Math_FFT","Offset"),//设置FFT运算结果的垂直偏移
            new SCPICommandStruct(":MATH:FFT:VPOSiton?","SCPI_Math_FFT","OffsetQ"),//查询FFT运算结果的垂直偏移
            new SCPICommandStruct(":MATH:FFT:HSCAle","SCPI_Math_FFT","HsCale"),//设置FFT运算结果的垂直偏移
            new SCPICommandStruct(":MATH:FFT:HSCAle?","SCPI_Math_FFT","HsCaleQ"),//查询FFT运算结果的垂直偏移
            new SCPICommandStruct(":MATH:FFT:HPOSition","SCPI_Math_FFT","Position"),//设置FFT运算结果的垂直偏移
            new SCPICommandStruct(":MATH:FFT:HPOSition?","SCPI_Math_FFT","PositionQ"),//查询FFT运算结果的垂直偏移
            //AX+B
            new SCPICommandStruct(":MATH:AX+B:SOURce","SCPI_Math_AXB","Source"),//选择AXB运算的信源
            new SCPICommandStruct(":MATH:AX+B:SOURce?","SCPI_Math_AXB","SourceQ"),//查询AXB运算的信源
            new SCPICommandStruct(":MATH:AX+B:A","SCPI_Math_AXB","A"),//选择AXB运算的信源
            new SCPICommandStruct(":MATH:AX+B:A?","SCPI_Math_AXB","AQ"),//查询AXB运算的信源
            new SCPICommandStruct(":MATH:AX+B:B","SCPI_Math_AXB","B"),//选择AXB运算的信源
            new SCPICommandStruct(":MATH:AX+B:B?","SCPI_Math_AXB","BQ"),//查询AXB运算的信源
            new SCPICommandStruct(":MATH:AX+B:UNIT","SCPI_Math_AXB","Unit"),//选择AXB运算的信源
            new SCPICommandStruct(":MATH:AX+B:UNIT?","SCPI_Math_AXB","UnitQ"),//查询AXB运算的信源

            new SCPICommandStruct(":MATH:AX+B:EXTent","SCPI_Math_AXB","Extent"),//设置AXB运算结果的垂直档位
            new SCPICommandStruct(":MATH:AX+B:EXTent?","SCPI_Math_AXB","ExtentQ"),//查询AXB运算结果的垂直档位
            new SCPICommandStruct(":MATH:AX+B:VSCale","SCPI_Math_AXB","Extent"),//设置AXB运算结果的垂直档位
            new SCPICommandStruct(":MATH:AX+B:VSCale?","SCPI_Math_AXB","ExtentQ"),//查询AXB运算结果的垂直档位
            new SCPICommandStruct(":MATH:AX+B:VPOSiton","SCPI_Math_AXB","Offset"),//设置AXB运算结果的垂直偏移
            new SCPICommandStruct(":MATH:AX+B:VPOSiton?","SCPI_Math_AXB","OffsetQ"),//查询AXB运算结果的垂直偏移
            //Advanced
            new SCPICommandStruct(":MATH:ADVanced:EXPRession", "SCPI_Math_Advanced","Expression"),
            new SCPICommandStruct(":MATH:ADVanced:EXPRession?","SCPI_Math_Advanced","ExpressionQ"),
            new SCPICommandStruct(":MATH:ADVanced:VAR1", "SCPI_Math_Advanced","Var1"),
            new SCPICommandStruct(":MATH:ADVanced:VAR1?", "SCPI_Math_Advanced","Var1Q"),
            new SCPICommandStruct(":MATH:ADVanced:VAR2", "SCPI_Math_Advanced","Var2"),
            new SCPICommandStruct(":MATH:ADVanced:VAR2?", "SCPI_Math_Advanced","Var2Q"),
            new SCPICommandStruct(":MATH:ADVanced:EXTent", "SCPI_Math_Advanced","Extent"),
            new SCPICommandStruct(":MATH:ADVanced:EXTent?","SCPI_Math_Advanced","ExtentQ"),
            new SCPICommandStruct(":MATH:ADVanced:VSCale", "SCPI_Math_Advanced","Extent"),
            new SCPICommandStruct(":MATH:ADVanced:VSCale?","SCPI_Math_Advanced","ExtentQ"),
            new SCPICommandStruct(":MATH:ADVanced:VPOSiton", "SCPI_Math_Advanced","Offset"),
            new SCPICommandStruct(":MATH:ADVanced:VPOSiton?","SCPI_Math_Advanced","OffsetQ"),
            new SCPICommandStruct(":MATH:ADVanced:UNIT", "SCPI_Math_Advanced","Unit"),
            new SCPICommandStruct(":MATH:ADVanced:UNIT?", "SCPI_Math_Advanced","UnitQ"),
            //math sample query
            new SCPICommandStruct( ":MATH:SRATe?","SCPI_Math_Sample","SRateQ"),
            new SCPICommandStruct( ":MATH:DEPth?", "SCPI_Math_Sample","MDepthQ"),

            //math sample query

            //光标命令 CURS
            new SCPICommandStruct(":CURSor:HORizontal","SCPI_Cursor","Horizontal"),//打开或关闭水平光标功能
            new SCPICommandStruct(":CURSor:HORizontal?","SCPI_Cursor","HorizontalQ"),//查询水平光标功能
            new SCPICommandStruct(":CURSor:VERTical","SCPI_Cursor","Vertical"),//打开或关闭垂直光标功能
            new SCPICommandStruct(":CURSor:VERTical?","SCPI_Cursor","VerticalQ"),//查询垂直光标功能
            new SCPICommandStruct(":CURSor:CX1","SCPI_Cursor","Cx1"),//设置垂直光标A的位置
            new SCPICommandStruct(":CURSor:PLUS:CXA","SCPI_Cursor","Plus_Cxa"),//设置垂直光标A的位置
            new SCPICommandStruct(":CURSor:CX1?","SCPI_Cursor","Cx1Q"),//查询垂直光标A的位置
            new SCPICommandStruct(":CURSor:CX2","SCPI_Cursor","Cx2"),//设置垂直光标B的位置
            new SCPICommandStruct(":CURSor:PLUS:CXB","SCPI_Cursor","Plus_Cxb"),//设置垂直光标B的位置
            new SCPICommandStruct(":CURSor:CX2?","SCPI_Cursor","Cx2Q"),//查询垂直光标B的位置
            new SCPICommandStruct(":CURSor:CY1","SCPI_Cursor","CY1"),//设置水平光标A的位置
            new SCPICommandStruct(":CURSor:PLUS:CYA","SCPI_Cursor","PLUS_CYA"),//设置水平光标A的位置
            new SCPICommandStruct(":CURSor:CY1?","SCPI_Cursor","CY1Q"),//查询水平光标A的位置
            new SCPICommandStruct(":CURSor:CY2","SCPI_Cursor","CY2"),//设置水平光标B的位置
            new SCPICommandStruct(":CURSor:PLUS:CYB","SCPI_Cursor","PLUS_CYB"),//设置水平光标B的位置
            new SCPICommandStruct(":CURSor:CY2?","SCPI_Cursor","CY2Q"),//查询水平光标B的位置
            new SCPICommandStruct(":CURSor:X1Value","SCPI_Cursor","X1Value"),//查询垂直光标A的X值
            new SCPICommandStruct(":CURSor:X2Value","SCPI_Cursor","X2Value"),//查询垂直光标B的X值
            new SCPICommandStruct(":CURSor:X1Value?","SCPI_Cursor","X1ValueQ"),//查询垂直光标A的X值
            new SCPICommandStruct(":CURSor:X2Value?","SCPI_Cursor","X2ValueQ"),//查询垂直光标B的X值
            new SCPICommandStruct(":CURSor:Y1Value","SCPI_Cursor","Y1Value"),//查询水平光标A的Y值
            new SCPICommandStruct(":CURSor:Y2Value","SCPI_Cursor","Y2Value"),//查询水平光标B的Y值
            new SCPICommandStruct(":CURSor:Y1Value?","SCPI_Cursor","Y1ValueQ"),//查询水平光标A的Y值
            new SCPICommandStruct(":CURSor:Y2Value?","SCPI_Cursor","Y2ValueQ"),//查询水平光标B的Y值
            new SCPICommandStruct(":CURSor:XDELta?","SCPI_Cursor","XdeltaQ"),//查询垂直光标A和B之间的差值，单位与水平单位相同
            new SCPICommandStruct(":CURSor:YDELta?","SCPI_Cursor","YdeltaQ"),//查询水平光标A和B之间的差值，单位与垂直单位相同
            new SCPICommandStruct(":CURSor:RATIo?","SCPI_Cursor","RatioQ"),//查询水平光标A和B之间的差值与垂直光标A和B之间的差值之间的比值
            new SCPICommandStruct(":CURSor:SOURce","SCPI_Cursor","Source"),//设置光标测量的通道源
            new SCPICommandStruct(":CURSor:SOURce?","SCPI_Cursor","SourceQ"),//查询光标测量的通道源
            new SCPICommandStruct(":CURSor:FREQ?","SCPI_Cursor","FreqQ"),//查询垂直光标x1和x2之间的1/x，单位HZ
            new SCPICommandStruct(":CURSor:TRACe","SCPI_Cursor","Trace"),//光标跟踪
            new SCPICommandStruct(":CURSor:TRACe?","SCPI_Cursor","TraceQ"),//光标跟踪
            //显示命令 DISP
            new SCPICommandStruct(":DISPlay:WAVeform","SCPI_Display","WaveForm"),//设置屏幕中波形的显示方式
            new SCPICommandStruct(":DISPlay:WAVeform?","SCPI_Display","WaveFormQ"),//查询屏幕中波形的显示方式
            new SCPICommandStruct(":DISPlay:BACKGROUND","SCPI_Display","Background"),//设置屏幕中波形的显示方式
            new SCPICommandStruct(":DISPlay:BACKGROUND?","SCPI_Display","BackgroundQ"),//查询屏幕中波形的显示方式
            new SCPICommandStruct(":DISPlay:BRIGhtness","SCPI_Display","Brightness"),//设置屏幕中波形显示的亮度
            new SCPICommandStruct(":DISPlay:BRIGhtness?","SCPI_Display","BrightnessQ"),//查询屏幕中波形显示的亮度
            new SCPICommandStruct(":DISPlay:GRATicule","SCPI_Display","Graticule"),//设置屏幕显示的网格类型
            new SCPICommandStruct(":DISPlay:GRATicule?","SCPI_Display","GraticuleQ"),//查询屏幕显示的网格类型
            new SCPICommandStruct(":DISPlay:INTensity","SCPI_Display","Intensity"),//设置屏幕中网格显示的亮度
            new SCPICommandStruct(":DISPlay:INTensity?","SCPI_Display","IntensityQ"),//查询屏幕中网格显示的亮度
            new SCPICommandStruct(":DISPlay:PERSist:MODE","SCPI_Display","Persist_Mode"),//设置余辉显示模式
            new SCPICommandStruct(":DISPlay:PERSist:MODE?","SCPI_Display","Persist_ModeQ"),//查询余辉显示模式
            new SCPICommandStruct(":DISPlay:PERSist:ADJust","SCPI_Display","Persist_Adjust"),//设置余辉普通显示模式下余辉时间
            new SCPICommandStruct(":DISPlay:PERSist:ADJust?","SCPI_Display","Persist_AdjustQ"),//查询余辉普通显示模式下余辉时间
            new SCPICommandStruct(":DISPlay:PERSist:CLEar","SCPI_Display","Persist_Clear"),//清除余辉显示
            new SCPICommandStruct(":DISPlay:HIGH","SCPI_Display","High"),//打开或关闭高刷新
            new SCPICommandStruct(":DISPlay:HIGH?","SCPI_Display","HighQ"),//查询高刷新打开或关闭
            new SCPICommandStruct(":DISPlay:HORRef","SCPI_Display","HorRef"),//设置屏幕水平展开中心模式
            new SCPICommandStruct(":DISPlay:HORRef?","SCPI_Display","HorRefQ"),//查询屏幕水平展开中心模式
            new SCPICommandStruct(":DISPlay:ZOOM","SCPI_Display","Zoom"),//打开或关闭ZOOM
            new SCPICommandStruct(":DISPlay:ZOOM?","SCPI_Display","ZoomQ"),//查询ZOOM打开或关闭
            new SCPICommandStruct(":DISPlay:CCT","SCPI_Display","CCT"),//打开或关闭CCT
            new SCPICommandStruct(":DISPlay:CCT?","SCPI_Display","CCTQ"),//查询CCT打开或关闭
            //测量命令 MEAS
            new SCPICommandStruct(":MEASure:PERiod?","SCPI_Measure","PeriodQ"),//查询指定通道波形的周期测量值
            new SCPICommandStruct(":MEASure:FREQuency?","SCPI_Measure","FreQuencyQ"),//查询指定通道波形的频率测量值
            new SCPICommandStruct(":MEASure:RISetime?","SCPI_Measure","RiseTimeQ"),//查询指定通道波形的上升时间测量值
            new SCPICommandStruct(":MEASure:FALLtime?","SCPI_Measure","FallTimeQ"),//查询指定通道波形的下降时间测量值
            new SCPICommandStruct(":MEASure:DELay?","SCPI_Measure","DelayQ"),//查询通道间延迟测量的结果
            new SCPICommandStruct(":MEASure:PDUTy?","SCPI_Measure","PDutyQ"),//查询指定通道波形的正占空比测量值
            new SCPICommandStruct(":MEASure:NDUTy?","SCPI_Measure","NDutyQ"),//查询指定通道波形的负占空比测量值
            new SCPICommandStruct(":MEASure:PWIDth?","SCPI_Measure","PWidthQ"),//查询指定通道波形的正脉宽测量值
            new SCPICommandStruct(":MEASure:NWIDth?","SCPI_Measure","NWidthQ"),//查询指定通道波形的负脉宽测量值
            new SCPICommandStruct(":MEASure:BURStw?","SCPI_Measure","BurstWidthQ"),//查询指定通道波形的突发脉冲宽度测量值
            new SCPICommandStruct(":MEASure:ROV?","SCPI_Measure","RovQ"),//查询指定通道波形的正向超调测量值
            new SCPICommandStruct(":MEASure:FOV?","SCPI_Measure","FovQ"),//查询指定通道波形的负向超调测量值
            new SCPICommandStruct(":MEASure:PHASe?","SCPI_Measure","PhaseQ"),//查询指定通道间相位差测量的结果
            new SCPICommandStruct(":MEASure:PKPK?","SCPI_Measure","PkpkQ"),//查询指定通道波形的峰峰值
            new SCPICommandStruct(":MEASure:AMP?","SCPI_Measure","AmpQ"),//查询指定通道波形的幅度测量值
            new SCPICommandStruct(":MEASure:HIGH?","SCPI_Measure","HighQ"),//查询指定通道波形的高值
            new SCPICommandStruct(":MEASure:LOW?","SCPI_Measure","LowQ"),//查询指定通道波形的低值
            new SCPICommandStruct(":MEASure:MAX?","SCPI_Measure","MaxQ"),//查询指定通道波形的最大值
            new SCPICommandStruct(":MEASure:MIN?","SCPI_Measure","MinQ"),//查询指定通道波形的最小值
            new SCPICommandStruct(":MEASure:RMS?","SCPI_Measure","RmsQ"),//查询指定通道波形的均方根值
            new SCPICommandStruct(":MEASure:CRMS?","SCPI_Measure","CrmsQ"),//查询指定通道波形的周期均方根值
            new SCPICommandStruct(":MEASure:MEAN?","SCPI_Measure","MeanQ"),//查询指定通道波形的平均值
            new SCPICommandStruct(":MEASure:CMEan?","SCPI_Measure","CMeanQ"),//查询指定通道波形的周期平均值
            new SCPICommandStruct(":MEASure:ACRMs?","SCPI_Measure","ACRMSQ"),//
            new SCPICommandStruct(":MEASure:PRATe?","SCPI_Measure","PRATEQ"),//
            new SCPICommandStruct(":MEASure:NRATe?","SCPI_Measure","NRATEQ"),//
            new SCPICommandStruct(":MEASure:COLVal?","SCPI_Measure","ColValQ"),
            //查询指定列上值
            new SCPICommandStruct(":MEASure:AREa?","SCPI_Measure","AreaQ"),//查询指定通道波形的面积
            new SCPICommandStruct(":MEASure:CARea?","SCPI_Measure","CareaQ"),//查询指定通道波形的周期面积
            new SCPICommandStruct(":MEASure:CLEAr","SCPI_Measure","Clear"),//清除打开的测量项中的任一项或所有项
            new SCPICommandStruct(":MEASure:OPEN","SCPI_Measure","Open"),//打开测量项
            new SCPICommandStruct(":MEASure:CLOSe","SCPI_Measure","Close"),//关闭测量项
            new SCPICommandStruct(":MEASure:ADISplay","SCPI_Measure","Adislay"),//打开或关闭全部测量
            new SCPICommandStruct(":MEASure:ADISplay?","SCPI_Measure","AdisplayQ"),//查询全部测量打开或关闭
            new SCPICommandStruct(":MEASure:SCOPe","SCPI_Measure","Scope"),//设置测量范围
            new SCPICommandStruct(":MEASure:SCOPe?","SCPI_Measure","ScopeQ"),//查询测量范围
            new SCPICommandStruct(":MEASure:COUNter:SOURce","SCPI_Measure","Counter_Source"),//设置源
            new SCPICommandStruct(":MEASure:COUNter:SOURce?","SCPI_Measure","Counter_SourceQ"),//设置源
            new SCPICommandStruct(":MEASure:COUNter:MODE","SCPI_Measure","Counter_Mode"),
            new SCPICommandStruct(":MEASure:COUNter:MODE?","SCPI_Measure","Counter_ModeQ"),
            new SCPICommandStruct(":MEASure:COUNter:VALue?","SCPI_Measure","Counter_ValueQ"),//查询频率计结果
            new SCPICommandStruct(":MEASure:ITEM","SCPI_Measure","Item"),//设置信源
            new SCPICommandStruct(":MEASure:ITEM?","SCPI_Measure","ItemQ"),//设置信源

            new SCPICommandStruct(":MEASure:TVALue","SCPI_Measure","TVALue"),
            new SCPICommandStruct(":MEASure:TVALue?","SCPI_Measure","TVALueQ"),

            //测量统计功能 2022-12-1
            new SCPICommandStruct(":MEASure:STATistic:DISPlay","SCPI_Measure_Statistic","Display"),//打开或关闭统计功能
            new SCPICommandStruct(":MEASure:STATistic:DISPlay?","SCPI_Measure_Statistic","DisplayQ"),//查询打开状态
            new SCPICommandStruct(":MEASure:STATistic:RESet","SCPI_Measure_Statistic","Reset"),//重新统计
            new SCPICommandStruct(":MEASure:STATistic:MEAN","SCPI_Measure_Statistic","Mean"),//打开或关闭平均值
            new SCPICommandStruct(":MEASure:STATistic:MEAN?","SCPI_Measure_Statistic","MeanQ"),//查询打开状态
            new SCPICommandStruct(":MEASure:STATistic:MAX","SCPI_Measure_Statistic","Max"),//打开或关闭最大值
            new SCPICommandStruct(":MEASure:STATistic:MAX?","SCPI_Measure_Statistic","MaxQ"),//查询打开状态
            new SCPICommandStruct(":MEASure:STATistic:MIN","SCPI_Measure_Statistic","Min"),//打开或关闭最小值
            new SCPICommandStruct(":MEASure:STATistic:MIN?","SCPI_Measure_Statistic","MinQ"),//查询打开状态
            new SCPICommandStruct(":MEASure:STATistic:DEV","SCPI_Measure_Statistic","Dev"),//打开或关闭delta
            new SCPICommandStruct(":MEASure:STATistic:DEV?","SCPI_Measure_Statistic","DevQ"),//查询打开状态
            new SCPICommandStruct(":MEASure:STATistic:COUNt","SCPI_Measure_Statistic","Count"),//打开或关闭平均值
            new SCPICommandStruct(":MEASure:STATistic:COUNt?","SCPI_Measure_Statistic","CountQ"),//查询打开状态
            new SCPICommandStruct(":MEASure:STATistic:VIEW?","SCPI_Measure_Statistic","ViewQ"), //询问统计项目数值
            new SCPICommandStruct(":MEASure:STATistic:MEAN:VIEW?","SCPI_Measure_Statistic","Mean_ViewQ"), //询问统计项目数值
            new SCPICommandStruct(":MEASure:STATistic:MAX:VIEW?","SCPI_Measure_Statistic","Max_ViewQ"), //询问统计项目数值
            new SCPICommandStruct(":MEASure:STATistic:MIN:VIEW?","SCPI_Measure_Statistic","Min_ViewQ"), //询问统计项目数值
            new SCPICommandStruct(":MEASure:STATistic:DEV:VIEW?","SCPI_Measure_Statistic","Dev_ViewQ"), //询问统计项目数值
            new SCPICommandStruct(":MEASure:STATistic:COUNt:VIEW?","SCPI_Measure_Statistic","Count_ViewQ"), //询问统计项目数值
            new SCPICommandStruct(":MEASure:STATistic:CURRent:VIEW?","SCPI_Measure_Statistic","Current_ViewQ"), //询问统计项目数值
            // 测量 setting 未实现
            new SCPICommandStruct(":MEASure:SETTing:INDicator","SCPI_Measure_Statistic","Indicator"), //
            new SCPICommandStruct(":MEASure:SETTing:INDicator?","SCPI_Measure_Statistic","IndicatorQ"), //
            new SCPICommandStruct(":MEASure:SETTing:RANGe","SCPI_Measure_Statistic","Range"), //
            new SCPICommandStruct(":MEASure:SETTing:RANGe?","SCPI_Measure_Statistic","RangeQ"), //
            new SCPICommandStruct(":MEASure:SETTing:ThReshold","SCPI_Measure_Statistic","Threshold"), //
            new SCPICommandStruct(":MEASure:SETTing:ThReshold?","SCPI_Measure_Statistic","ThresholdQ"), //
            new SCPICommandStruct(":MEASure:SETTing:HIGH","SCPI_Measure_Statistic","High"), //
            new SCPICommandStruct(":MEASure:SETTing:HIGH?","SCPI_Measure_Statistic","HighQ"), //
            new SCPICommandStruct(":MEASure:SETTing:MID","SCPI_Measure_Statistic","Mid"), //
            new SCPICommandStruct(":MEASure:SETTing:MID?","SCPI_Measure_Statistic","MidQ"), //
            new SCPICommandStruct(":MEASure:SETTing:LOW","SCPI_Measure_Statistic","Low"), //
            new SCPICommandStruct(":MEASure:SETTing:LOW?","SCPI_Measure_Statistic","LowQ"), //


            new SCPICommandStruct(":MEASure:LIST?","SCPI_Measure","ListQ"),
            new SCPICommandStruct(":MEASure:ADDNew", "SCPI_Measure","AddNew"),
            new SCPICommandStruct(":MEASure:DELete", "SCPI_Measure","Delete"),

            new SCPICommandStruct(":MEASure:MEAS#:TYPe","SCPI_Measure","XType"),
            new SCPICommandStruct(":MEASure:MEAS#:TYPe?", "SCPI_Measure","XTypeQ"),
            new SCPICommandStruct(":MEASure:MEAS#:SOURce1", "SCPI_Measure","XSOURce1"),
            new SCPICommandStruct(":MEASure:MEAS#:SOURce1?", "SCPI_Measure","XSOURce1Q"),
            new SCPICommandStruct(":MEASure:MEAS#:SOURce2", "SCPI_Measure","XSOURce2"),
            new SCPICommandStruct(":MEASure:MEAS#:SOURce2?", "SCPI_Measure","XSOURce2Q"),

            new SCPICommandStruct(":MEASure:MEAS#:VALue?", "SCPI_Measure","XVALueQ"),
            new SCPICommandStruct(":MEASure:MEAS#:UNIT?", "SCPI_Measure","XUnitQ"),
            new SCPICommandStruct(":MEASure:MEAS#:VALID?", "SCPI_Measure","XValidQ"),
            new SCPICommandStruct(":MEASure:MEAS#:EDGE1", "SCPI_Measure","XEdge1"),
            new SCPICommandStruct(":MEASure:MEAS#:EDGE1?", "SCPI_Measure","XEdge1Q"),
            new SCPICommandStruct(":MEASure:MEAS#:EDGE2", "SCPI_Measure","XEdge2"),
            new SCPICommandStruct(":MEASure:MEAS#:EDGE2?", "SCPI_Measure","XEdge2Q"),
            new SCPICommandStruct(":MEASure:MEAS#:CURSor", "SCPI_Measure","XCURSor"),
            new SCPICommandStruct(":MEASure:MEAS#:CURSor?", "SCPI_Measure","XCURSorQ"),
            new SCPICommandStruct(":MEASure:MEAS#:VVLue", "SCPI_Measure","XVVLue"),
            new SCPICommandStruct(":MEASure:MEAS#:VVLue?", "SCPI_Measure","XVVLueQ"),



            //触发命令 TRIG
            new SCPICommandStruct(":TRIGger:TYPE","SCPI_Trigger","Type"),//选择触发类型
            new SCPICommandStruct(":TRIGger:TYPE?","SCPI_Trigger","TypeQ"),//查询返回当前使用的触发类型
            new SCPICommandStruct(":TRIGger:HOLDoff","SCPI_Trigger","HoldOff"),//设置触发释抑时间
            new SCPICommandStruct(":TRIGger:HOLDoff?","SCPI_Trigger","HoldOffQ"),//查询以科学计数形式返回触发释抑时间
            new SCPICommandStruct(":TRIGger:MODE","SCPI_Trigger","Mode"),//设置触发方式：自动或普通
            new SCPICommandStruct(":TRIGger:MODE?","SCPI_Trigger","ModeQ"),//查询触发方式
            new SCPICommandStruct(":TRIGger:STATus?","SCPI_Trigger","StatusQ"),//查询当前的触发状态
            new SCPICommandStruct(":TRIGger:EXTErnal:ISTRigger?","SCPI_Trigger","IsExternalTriggerQ"),//是否存在外部触发
            new SCPICommandStruct(":TRIGger:EXTErnal:ISCLock?","SCPI_Trigger","IsExternalClockQ"),//是否存在外部触发
            new SCPICommandStruct(":TRIGger:EXTErnal:HasDialog?","SCPI_Trigger","HasDialogQ"),//外部触发是否有数据,即：对话框是否显示
            new SCPICommandStruct(":TRIGger:EXTErnal:DialogSet","SCPI_Trigger","DialogSet"),//外部触发对话框选择
            //Trigger edge
            new SCPICommandStruct(":TRIGger:EDGE:SOURce","SCPI_Trigger_Edge","Source"),//选择边沿触发的触发源
            new SCPICommandStruct(":TRIGger:EDGE:SOURce?","SCPI_Trigger_Edge","SourceQ"),//查询边沿触发的触发源
            new SCPICommandStruct(":TRIGger:EDGE:SLOPe","SCPI_Trigger_Edge","Slope"),//选择边沿触发的边沿类型
            new SCPICommandStruct(":TRIGger:EDGE:SLOPe?","SCPI_Trigger_Edge","SlopeQ"),//查询边沿触发的边沿类型
            new SCPICommandStruct(":TRIGger:EDGE:LEVel","SCPI_Trigger_Edge","Level"),//设置边沿触发时的触发电平
            new SCPICommandStruct(":TRIGger:EDGE:PLUS:LEVel","SCPI_Trigger_Edge","Plus_Level"),//设置边沿触发时的触发电平
            new SCPICommandStruct(":TRIGger:EDGE:LEVel?","SCPI_Trigger_Edge","LevelQ"),//查询边沿触发时的触发电平
            new SCPICommandStruct(":TRIGger:EDGE:COUPle","SCPI_Trigger_Edge","Couple"),//设置边沿触发耦合方式。
            new SCPICommandStruct(":TRIGger:EDGE:COUPle?","SCPI_Trigger_Edge","CoupleQ"),//查询边沿触发耦合方式。
            //Trigger pulse
            new SCPICommandStruct(":TRIGger:PULSe:SOURce","SCPI_Trigger_Pulse","Source"),//选择脉宽触发的触发源
            new SCPICommandStruct(":TRIGger:PULSe:SOURce?","SCPI_Trigger_Pulse","SourceQ"),//查询脉宽触发的触发源
            new SCPICommandStruct(":TRIGger:PULSe:POLarity","SCPI_Trigger_Pulse","Polarity"),//设置脉宽触发的极性
            new SCPICommandStruct(":TRIGger:PULSe:POLarity?","SCPI_Trigger_Pulse","PolarityQ"),//查询脉宽触发的极性
            new SCPICommandStruct(":TRIGger:PULSe:WIDTh","SCPI_Trigger_Pulse","Width"),//设置脉宽触发时的脉冲宽度值
            new SCPICommandStruct(":TRIGger:PULSe:WIDTh?","SCPI_Trigger_Pulse","WidthQ"),//查询脉宽触发时的脉冲宽度值
            new SCPICommandStruct(":TRIGger:PULSe:CONDition","SCPI_Trigger_Pulse","Condition"),//设置脉宽触发条件
            new SCPICommandStruct(":TRIGger:PULSe:CONDition?","SCPI_Trigger_Pulse","ConditionQ"),//查询脉宽触发条件
            new SCPICommandStruct(":TRIGger:PULSe:LEVel","SCPI_Trigger_Pulse","Level"),//设置脉宽触发时的触发电平
            new SCPICommandStruct(":TRIGger:PULSe:PLUS:LEVel","SCPI_Trigger_Pulse","Plus_Level"),//设置脉宽触发时的触发电平
            new SCPICommandStruct(":TRIGger:PULSe:LEVel?","SCPI_Trigger_Pulse","LevelQ"),//查询脉宽触发时的触发电平
            //Trigger logic
            new SCPICommandStruct(":TRIGger:LOGic:STATus","SCPI_Trigger_Logic","Status"),//设置逻辑触发中通道的逻辑状态
            new SCPICommandStruct(":TRIGger:LOGic:STATus?","SCPI_Trigger_Logic","StatusQ"),//查询逻辑触发中通道的逻辑状态
            new SCPICommandStruct(":TRIGger:LOGic:FUNCtion","SCPI_Trigger_Logic","Function"),//设置逻辑触发的比较函数
            new SCPICommandStruct(":TRIGger:LOGic:FUNCtion?","SCPI_Trigger_Logic","FunctionQ"),//查询逻辑触发的比较函数
            new SCPICommandStruct(":TRIGger:LOGic:CONDition","SCPI_Trigger_Logic","Condition"),//设置逻辑触发条件
            new SCPICommandStruct(":TRIGger:LOGic:CONDition?","SCPI_Trigger_Logic","ConditionQ"),//查询逻辑触发条件
            new SCPICommandStruct(":TRIGger:LOGic:TIME","SCPI_Trigger_Logic","Time"),//设置触发逻辑时间
            new SCPICommandStruct(":TRIGger:LOGic:TIME?","SCPI_Trigger_Logic","TimeQ"),//查询触发逻辑时间
            new SCPICommandStruct(":TRIGger:LOGic:LEVel","SCPI_Trigger_Logic","Level"),//设置逻辑触发时的各通道触发电平
            new SCPICommandStruct(":TRIGger:LOGic:PLUS:LEVel","SCPI_Trigger_Logic","Plus_Level"),//设置逻辑触发时的各通道触发电平
            new SCPICommandStruct(":TRIGger:LOGic:LEVel?","SCPI_Trigger_Logic","LevelQ"),//查询逻辑触发时的各通道触发电平
            //Trigger B
            new SCPICommandStruct(":TRIGger:B:SOURce","SCPI_Trigger_B","Source"),//选择B触发的触发源
            new SCPICommandStruct(":TRIGger:B:SOURce?","SCPI_Trigger_B","SourceQ"),//查询B触发的触发源
            new SCPICommandStruct(":TRIGger:B:EDGE","SCPI_Trigger_B","Edge"),//设置B触发的触发斜率
            new SCPICommandStruct(":TRIGger:B:EDGE?","SCPI_Trigger_B","EdgeQ"),//查询B触发的触发斜率
            new SCPICommandStruct(":TRIGger:B:COUPle","SCPI_Trigger_B","Couple"),//选择B触发耦合方式
            new SCPICommandStruct(":TRIGger:B:COUPle?","SCPI_Trigger_B","CoupleQ"),//查询B触发耦合方式
            new SCPICommandStruct(":TRIGger:B:SEQuence","SCPI_Trigger_B","Sequence"),//设置B触发的触发类型（B在A后触发时间/事件）
            new SCPICommandStruct(":TRIGger:B:SEQuence?","SCPI_Trigger_B","SequenceQ"),//查询B触发的触发类型
            new SCPICommandStruct(":TRIGger:B:LEVel","SCPI_Trigger_B","Level"),//设置B触发时的触发电平
            new SCPICommandStruct(":TRIGger:B:LEVel?","SCPI_Trigger_B","LevelQ"),//查询B触发时的触发电平

            //trigger dwart换名为trigger runt 2016.12.8
            new SCPICommandStruct(":TRIGger:RUNT:SOURce","SCPI_Trigger_Runt","Source"),//设置矮脉宽触发的触发源
            new SCPICommandStruct(":TRIGger:RUNT:SOURce?","SCPI_Trigger_Runt","SourceQ"),//查询矮脉宽触发的触发源
            new SCPICommandStruct(":TRIGger:RUNT:POLArity","SCPI_Trigger_Runt","Polarity"),//设置矮脉宽触发的脉冲极性
            new SCPICommandStruct(":TRIGger:RUNT:POLArity?","SCPI_Trigger_Runt","PolarityQ"),//查询矮脉宽触发的脉冲极性
            new SCPICommandStruct(":TRIGger:RUNT:CONDition","SCPI_Trigger_Runt","Condition"),//设置矮脉宽触发的脉宽限制条件
            new SCPICommandStruct(":TRIGger:RUNT:CONDition?","SCPI_Trigger_Runt","ConditionQ"),//查询矮脉宽触发的脉宽限制条件
            new SCPICommandStruct(":TRIGger:RUNT:HTIMe","SCPI_Trigger_Runt","HTime"),//设置矮脉宽触发时的时间上限
            new SCPICommandStruct(":TRIGger:RUNT:HTIMe?","SCPI_Trigger_Runt","HTimeQ"),//查询矮脉宽触发时的时间上限
            new SCPICommandStruct(":TRIGger:RUNT:LTIMe","SCPI_Trigger_Runt","LTime"),//设置矮脉宽触发时的时间下限
            new SCPICommandStruct(":TRIGger:RUNT:LTIMe?","SCPI_Trigger_Runt","LTimeQ"),//查询矮脉宽触发时的时间下限
            new SCPICommandStruct(":TRIGger:RUNT:BTIMe","SCPI_Trigger_Runt","BTime"),//设置矮脉宽触发时的时间区间
            new SCPICommandStruct(":TRIGger:RUNT:BTIMe?","SCPI_Trigger_Runt","BTimeQ"),//查询矮脉宽触发时的时间上限或下限
            new SCPICommandStruct(":TRIGger:RUNT:HLEVel","SCPI_Trigger_Runt","HLevel"),//设置矮脉宽触发时的高电平
            new SCPICommandStruct(":TRIGger:RUNT:PLUS:HLEVel","SCPI_Trigger_Runt","Plus_HLevel"),//设置矮脉宽触发时的高电平
            new SCPICommandStruct(":TRIGger:RUNT:HLEVel?","SCPI_Trigger_Runt","HLevelQ"),//查询矮脉宽触发时的高电平
            new SCPICommandStruct(":TRIGger:RUNT:LLEVel","SCPI_Trigger_Runt","LLevel"),//设置矮脉宽触发时的低电平
            new SCPICommandStruct(":TRIGger:RUNT:PLUS:LLEVel","SCPI_Trigger_Runt","Plus_LLevel"),//设置矮脉宽触发时的低电平
            new SCPICommandStruct(":TRIGger:RUNT:LLEVel?","SCPI_Trigger_Runt","LLevelQ"),//查询矮脉宽触发时的低电平
            //Trigger dwart 协议1.0
            new SCPICommandStruct(":TRIGger:DWARt:SOURce","SCPI_Trigger_Dwart","Source"),//设置矮脉宽触发的触发源
            new SCPICommandStruct(":TRIGger:DWARt:SOURce?","SCPI_Trigger_Dwart","SourceQ"),//查询矮脉宽触发的触发源
            new SCPICommandStruct(":TRIGger:DWARt:POLarity","SCPI_Trigger_Dwart","Polarity"),//设置矮脉宽触发的脉冲极性
            new SCPICommandStruct(":TRIGger:DWARt:POLarity?","SCPI_Trigger_Dwart","PolarityQ"),//查询矮脉宽触发的脉冲极性
            new SCPICommandStruct(":TRIGger:DWARt:CONDition","SCPI_Trigger_Dwart","Condition"),//设置矮脉宽触发的脉宽限制条件
            new SCPICommandStruct(":TRIGger:DWARt:CONDition?","SCPI_Trigger_Dwart","ConditionQ"),//查询矮脉宽触发的脉宽限制条件
            new SCPICommandStruct(":TRIGger:DWARt:HTIMe","SCPI_Trigger_Dwart","HTime"),//设置矮脉宽触发时的时间上限
            new SCPICommandStruct(":TRIGger:DWARt:HTIMe?","SCPI_Trigger_Dwart","HTimeQ"),//查询矮脉宽触发时的时间上限
            new SCPICommandStruct(":TRIGger:DWARt:LTIMe","SCPI_Trigger_Dwart","LTime"),//设置矮脉宽触发时的时间下限
            new SCPICommandStruct(":TRIGger:DWARt:LTIMe?","SCPI_Trigger_Dwart","LTimeQ"),//查询矮脉宽触发时的时间下限
            new SCPICommandStruct(":TRIGger:DWARt:BTIMe","SCPI_Trigger_Dwart","BTime"),//设置矮脉宽触发时的时间区间
            new SCPICommandStruct(":TRIGger:DWARt:BTIMe?","SCPI_Trigger_Dwart","BTimeQ"),//查询矮脉宽触发时的时间上限或下限
            new SCPICommandStruct(":TRIGger:DWARt:HLEVel","SCPI_Trigger_Dwart","HLevel"),//设置矮脉宽触发时的高电平
            new SCPICommandStruct(":TRIGger:DWARt:PLUS:HLEVel","SCPI_Trigger_Dwart","Plus_HLevel"),//设置矮脉宽触发时的高电平
            new SCPICommandStruct(":TRIGger:DWARt:HLEVel?","SCPI_Trigger_Dwart","HLevelQ"),//查询矮脉宽触发时的高电平
            new SCPICommandStruct(":TRIGger:DWARt:LLEVel","SCPI_Trigger_Dwart","LLevel"),//设置矮脉宽触发时的低电平
            new SCPICommandStruct(":TRIGger:DWARt:PLUS:LLEVel","SCPI_Trigger_Dwart","Plus_LLevel"),//设置矮脉宽触发时的低电平
            new SCPICommandStruct(":TRIGger:DWARt:LLEVel?","SCPI_Trigger_Dwart","LLevelQ"),//查询矮脉宽触发时的低电平
            //Trgger slope
            new SCPICommandStruct(":TRIGger:SLOPe:SOURce","SCPI_Trigger_Slope","Source"),//设置斜率触发的触发源
            new SCPICommandStruct(":TRIGger:SLOPe:SOURce?","SCPI_Trigger_Slope","SourceQ"),//查询斜率触发的触发源
            new SCPICommandStruct(":TRIGger:SLOPe:EDGE","SCPI_Trigger_Slope","Edge"),//设置斜率触发沿
            new SCPICommandStruct(":TRIGger:SLOPe:EDGE?","SCPI_Trigger_Slope","EdgeQ"),//查询斜率触发沿
            new SCPICommandStruct(":TRIGger:SLOPe:CONDition","SCPI_Trigger_Slope","Condition"),//设置斜率触发的限制条件
            new SCPICommandStruct(":TRIGger:SLOPe:CONDition?","SCPI_Trigger_Slope","ConditionQ"),//查询斜率触发的限制条件
            new SCPICommandStruct(":TRIGger:SLOPe:HTIMe","SCPI_Trigger_Slope","HTime"),//设置斜率触发时的时间上限
            new SCPICommandStruct(":TRIGger:SLOPe:HTIMe?","SCPI_Trigger_Slope","HTimeQ"),//查询斜率触发时的时间上限
            new SCPICommandStruct(":TRIGger:SLOPe:LTIMe","SCPI_Trigger_Slope","LTime"),//设置斜率触发时的时间下限
            new SCPICommandStruct(":TRIGger:SLOPe:LTIMe?","SCPI_Trigger_Slope","LTimeQ"),//查询斜率触发时的时间下限
            new SCPICommandStruct(":TRIGger:SLOPe:BTIMe","SCPI_Trigger_Slope","BTime"),//设置斜率触发时的时间区间
            new SCPICommandStruct(":TRIGger:SLOPe:BTIMe?","SCPI_Trigger_Slope","BTimeQ"),//查询斜率触发时的时间上限或下限
            new SCPICommandStruct(":TRIGger:SLOPe:HLEVel","SCPI_Trigger_Slope","HLevel"),//设置斜率触发时的高电平
            new SCPICommandStruct(":TRIGger:SLOPe:PLUS:HLEVel","SCPI_Trigger_Slope","Plus_HLevel"),//设置斜率触发时的高电平
            new SCPICommandStruct(":TRIGger:SLOPe:HLEVel?","SCPI_Trigger_Slope","HLevelQ"),//查询斜率触发时的高电平
            new SCPICommandStruct(":TRIGger:SLOPe:LLEVel","SCPI_Trigger_Slope","LLevel"),//设置斜率触发时的低电平
            new SCPICommandStruct(":TRIGger:SLOPe:PLUS:LLEVel","SCPI_Trigger_Slope","Plus_LLevel"),//设置斜率触发时的低电平
            new SCPICommandStruct(":TRIGger:SLOPe:LLEVel?","SCPI_Trigger_Slope","LLevelQ"),//查询斜率触发时的低电平
            //Trigger timeout
            new SCPICommandStruct(":TRIGger:TIMeout:SOURce","SCPI_Trigger_Timeout","Source"),//设置超时触发的触发源
            new SCPICommandStruct(":TRIGger:TIMeout:SOURce?","SCPI_Trigger_Timeout","SourceQ"),//查询超时触发的触发源
            new SCPICommandStruct(":TRIGger:TIMeout:POLarity","SCPI_Trigger_Timeout","Polarity"),//设置超时触发极性
            new SCPICommandStruct(":TRIGger:TIMeout:POLarity?","SCPI_Trigger_Timeout","PolarityQ"),//查询超时触发极性
            new SCPICommandStruct(":TRIGger:TIMeout:TIME","SCPI_Trigger_Timeout","Time"),//设置超时触发的超时时间
            new SCPICommandStruct(":TRIGger:TIMeout:TIME?","SCPI_Trigger_Timeout","TimeQ"),//查询超时触发的超时时间
            new SCPICommandStruct(":TRIGger:TIMeout:LEVel","SCPI_Trigger_Timeout","Level"),//设置超时触发电平
            new SCPICommandStruct(":TRIGger:TIMeout:LEVel?","SCPI_Trigger_Timeout","LevelQ"),//查询超时触发电平

            //Trigger nedge
            new SCPICommandStruct(":TRIGger:NEDGe:SOURce","SCPI_Trigger_Nedge","Source"),//设置第N边沿触发的触发源
            new SCPICommandStruct(":TRIGger:NEDGe:SOURce?","SCPI_Trigger_Nedge","SourceQ"),//查询第N边沿触发的触发源
            new SCPICommandStruct(":TRIGger:NEDGe:SLOPe","SCPI_Trigger_Nedge","Slope"),//设置第N边沿触发的边沿类型
            new SCPICommandStruct(":TRIGger:NEDGe:SLOPe?","SCPI_Trigger_Nedge","SlopeQ"),//查询第N边沿触发的边沿类型
            new SCPICommandStruct(":TRIGger:NEDGe:IDLE","SCPI_Trigger_Nedge","Idle"),//设置第N边沿触发中开始边沿计数之前的空闲时间
            new SCPICommandStruct(":TRIGger:NEDGe:IDLE?","SCPI_Trigger_Nedge","IdleQ"),//查询第N边沿触发中开始边沿计数之前的空闲时间
            new SCPICommandStruct(":TRIGger:NEDGe:EDGE","SCPI_Trigger_Nedge","Edge"),//设置第N边沿触发的N的数值
            new SCPICommandStruct(":TRIGger:NEDGe:EDGE?","SCPI_Trigger_Nedge","EdgeQ"),//查询第N边沿触发的N的数值
            new SCPICommandStruct(":TRIGger:NEDGe:LEVel","SCPI_Trigger_Nedge","Level"),//设置第N边沿触发时的触发电平
            new SCPICommandStruct(":TRIGger:NEDGe:PLUS:LEVel","SCPI_Trigger_Nedge","Plus_Level"),//设置第N边沿触发时的触发电平
            new SCPICommandStruct(":TRIGger:NEDGe:LEVel?","SCPI_Trigger_Nedge","LevelQ"),//查询第N边沿触发时的触发电平
            //Trigger setup
            new SCPICommandStruct(":TRIGger:SETup:CLOCk","SCPI_Trigger_Setup","Clock"),//设置建立保持时间触发的时钟信号源
            new SCPICommandStruct(":TRIGger:SETup:CLOCk?","SCPI_Trigger_Setup","ClockQ"),//查询建立保持时间触发的时钟信号源
            new SCPICommandStruct(":TRIGger:SETup:DATA","SCPI_Trigger_Setup","Data"),//设置建立保持时间触发的数据信号源
            new SCPICommandStruct(":TRIGger:SETup:DATA?","SCPI_Trigger_Setup","DataQ"),//查询建立保持时间触发的数据信号源
            new SCPICommandStruct(":TRIGger:SETup:CEDGe","SCPI_Trigger_Setup","Cedge"),//设置建立保持时间触发的时钟边沿类型
            new SCPICommandStruct(":TRIGger:SETup:CEDGe?","SCPI_Trigger_Setup","CedgeQ"),//查询建立保持时间触发的时钟边沿类型
            new SCPICommandStruct(":TRIGger:SETup:STIMe","SCPI_Trigger_Setup","STime"),//设置建立保持时间触发的建立时间
            new SCPICommandStruct(":TRIGger:SETup:STIMe?","SCPI_Trigger_Setup","STimeQ"),//查询建立保持时间触发的建立时间
            new SCPICommandStruct(":TRIGger:SETup:HTIMe","SCPI_Trigger_Setup","HTime"),//设置建立保持时间触发的保持时间
            new SCPICommandStruct(":TRIGger:SETup:HTIMe?","SCPI_Trigger_Setup","HTimeQ"),//查询建立保持时间触发的保持时间
            new SCPICommandStruct(":TRIGger:SETup:CLEVel","SCPI_Trigger_Setup","CLevel"),//设置建立保持时间触发的时钟源触发电平
            new SCPICommandStruct(":TRIGger:SETup:CLEVel?","SCPI_Trigger_Setup","CLevelQ"),//查询建立保持时间触发的时钟源触发电平
            new SCPICommandStruct(":TRIGger:SETup:DLEVel","SCPI_Trigger_Setup","DLevel"),//设置建立保持时间触发的数据源触发电平
            new SCPICommandStruct(":TRIGger:SETup:DLEVel?","SCPI_Trigger_Setup","DLevelQ"),//查询建立保持时间触发的数据源触发电平
            //Trigger video
            new SCPICommandStruct(":TRIGger:VIDeo:SOURce","SCPI_Trigger_Video","Source"),//设置视频触发的触发源
            new SCPICommandStruct(":TRIGger:VIDeo:SOURce?","SCPI_Trigger_Video","SourceQ"),//查询视频触发的触发源
            new SCPICommandStruct(":TRIGger:VIDeo:POLarity","SCPI_Trigger_Video","Polarity"),//设置视频触发的极性
            new SCPICommandStruct(":TRIGger:VIDeo:POLarity?","SCPI_Trigger_Video","PolarityQ"),//查询视频触发的极性
            new SCPICommandStruct(":TRIGger:VIDeo:STANdard","SCPI_Trigger_Video","Standard"),//设置视频触发时的视频标准
            new SCPICommandStruct(":TRIGger:VIDeo:STANdard?","SCPI_Trigger_Video","StandardQ"),//查询视频触发时的视频标准
            new SCPICommandStruct(":TRIGger:VIDeo:MODE","SCPI_Trigger_Video","Amode"),//设置触发标准为PAL、SECAm、NESC、1080I时视频触发的同步类型
            new SCPICommandStruct(":TRIGger:VIDeo:MODE?","SCPI_Trigger_Video","AmodeQ"),//查询触发标准为PAL、SECAm、NESC、1080I时视频触发的同步类型
            new SCPICommandStruct(":TRIGger:VIDeo:BMODe","SCPI_Trigger_Video","Bmode"),//设置触发标准为720P、1080P时视频触发的同步类型
            new SCPICommandStruct(":TRIGger:VIDeo:BMODe?","SCPI_Trigger_Video","BmodeQ"),//查询触发标准为720P、1080P时视频触发的同步类型
            new SCPICommandStruct(":TRIGger:VIDeo:FREQuence","SCPI_Trigger_Video","Afrequence"),//设置触发标准为720P、1080I时视频触发的信号频率
            new SCPICommandStruct(":TRIGger:VIDeo:FREQuence?","SCPI_Trigger_Video","AfrequenceQ"),//查询触发标准为720P、1080I时视频触发的信号频率
            new SCPICommandStruct(":TRIGger:VIDeo:BFRequence","SCPI_Trigger_Video","Bfrequence"),//设置触发标准为1080P时视频触发的信号频率
            new SCPICommandStruct(":TRIGger:VIDeo:BFRequence?","SCPI_Trigger_Video","BfrequenceQ"),//查询触发标准为1080P时视频触发的信号频率
            new SCPICommandStruct(":TRIGger:VIDeo:LINE","SCPI_Trigger_Video","Line"),//设置视频触发行号
            new SCPICommandStruct(":TRIGger:VIDeo:LINE?","SCPI_Trigger_Video","LineQ"),//查询视频触发行号
            //Trigger uart
            new SCPICommandStruct(":TRIGger:UART:SOURce","SCPI_Trigger_Uart","Source"),//设置UART触发的触发源
            new SCPICommandStruct(":TRIGger:UART:SOURce?","SCPI_Trigger_Uart","SourceQ"),//查询UART触发的触发源
            new SCPICommandStruct(":TRIGger:UART:TYPE","SCPI_Trigger_Uart","Type"),//设置UART触发的触发条件
            new SCPICommandStruct(":TRIGger:UART:TYPE?","SCPI_Trigger_Uart","TypeQ"),//查询UART触发的触发条件
            new SCPICommandStruct(":TRIGger:UART:RELation","SCPI_Trigger_Uart","Relation"),//当UART总线触发条件选择为DATA、0:DATA、1:DATA、X:DATA时，设置UART总线触发关系
            new SCPICommandStruct(":TRIGger:UART:RELation?","SCPI_Trigger_Uart","RelationQ"),//当UART总线触发条件选择为DATA、0:DATA、1:DATA、X:DATA时，查询UART总线触发关系
            new SCPICommandStruct(":TRIGger:UART:DATA","SCPI_Trigger_Uart","Data"),//当UART总线触发条件选择为DATA、0:DATA、1:DATA、X:DATA时，设置UART总线触发数据。
            new SCPICommandStruct(":TRIGger:UART:DATA?","SCPI_Trigger_Uart","DataQ"),//当UART总线触发条件选择为DATA、0:DATA、1:DATA、X:DATA时，查询UART总线触发数据。

            //Trigger lin
            new SCPICommandStruct(":TRIGger:LIN:SOURce","SCPI_Trigger_Lin","Source"),//设置LIN触发的触发源
            new SCPICommandStruct(":TRIGger:LIN:SOURce?","SCPI_Trigger_Lin","SourceQ"),//查询LIN触发的触发源
            new SCPICommandStruct(":TRIGger:LIN:TYPE","SCPI_Trigger_Lin","Type"),//设置LIN触发的触发条件
            new SCPICommandStruct(":TRIGger:LIN:TYPE?","SCPI_Trigger_Lin","TypeQ"),//查询LIN触发的触发条件
            new SCPICommandStruct(":TRIGger:LIN:ID","SCPI_Trigger_Lin","Id"),//当LIN总线触发条件为FID或IDATa时，设置LIN触发的触发ID值
            new SCPICommandStruct(":TRIGger:LIN:ID?","SCPI_Trigger_Lin","IdQ"),//当LIN总线触发条件为FID或IDATa时，查询LIN触发的触发ID值
            new SCPICommandStruct(":TRIGger:LIN:DATA","SCPI_Trigger_Lin","Data"),//当LIN总线触发条件为IDATa时，设置LIN触发的触发数据
            new SCPICommandStruct(":TRIGger:LIN:DATA?","SCPI_Trigger_Lin","DataQ"),//当LIN总线触发条件为IDATa时，查询LIN触发的触发数据

            //Trigger can
            new SCPICommandStruct(":TRIGger:CAN:SOURce","SCPI_Trigger_Can","Source"),//设置CAN触发的触发源
            new SCPICommandStruct(":TRIGger:CAN:SOURce?","SCPI_Trigger_Can","SourceQ"),//查询CAN触发的触发源
            new SCPICommandStruct(":TRIGger:CAN:TYPE","SCPI_Trigger_Can","Type"),//设置CAN触发的触发条件
            new SCPICommandStruct(":TRIGger:CAN:TYPE?","SCPI_Trigger_Can","TypeQ"),//查询CAN触发的触发条件
            new SCPICommandStruct(":TRIGger:CAN:ID","SCPI_Trigger_Can","Id"),//当CAN触发的触发条件为RFID、DFID、IDATa或RDID时，设置CAN触发的触发ID值
            new SCPICommandStruct(":TRIGger:CAN:ID?","SCPI_Trigger_Can","IdQ"),//当CAN触发的触发条件为RFID、DFID、IDATa或RDID时，查询CAN触发的触发ID值
            new SCPICommandStruct(":TRIGger:CAN:DLC","SCPI_Trigger_Can","DLC"),//当CAN 触发的触发条件为IDATa时，设置CAN触发的DLC值
            new SCPICommandStruct(":TRIGger:CAN:DLC?","SCPI_Trigger_Can","DLCQ"),//当CAN 触发的触发条件为IDATa时，查询CAN触发的DLC值
            new SCPICommandStruct(":TRIGger:CAN:DATA","SCPI_Trigger_Can","Data"),//当CAN 触发的触发条件为IDATa时，设置CAN触发的触发数据值
            new SCPICommandStruct(":TRIGger:CAN:DATA?","SCPI_Trigger_Can","DataQ"),//当CAN 触发的触发条件为IDATa时，查询CAN触发的触发数据值

            //Trigger spi
            new SCPICommandStruct(":TRIGger:SPI:TYPE","SCPI_Trigger_SPI","Type"),//设置SPI触发下的数据值
            new SCPICommandStruct(":TRIGger:SPI:TYPE?","SCPI_Trigger_SPI","TypeQ"),//查询SPI触发下的数据值
            new SCPICommandStruct(":TRIGger:SPI:DATA","SCPI_Trigger_SPI","Data"),//设置SPI触发下的数据值
            new SCPICommandStruct(":TRIGger:SPI:DATA?","SCPI_Trigger_SPI","DataQ"),//查询SPI触发下的数据值
            new SCPICommandStruct(":TRIGger:SPI:SOURce","SCPI_Trigger_SPI","Source"),//设置SPI触发的触发源
            new SCPICommandStruct(":TRIGger:SPI:SOURce?","SCPI_Trigger_SPI","SourceQ"),//查询SPI触发的触发源

            //Trigger iic
            new SCPICommandStruct(":TRIGger:IIC:SOURce","SCPI_Trigger_IIC","Source"),//设置IIC触发的触发源
            new SCPICommandStruct(":TRIGger:IIC:SOURce?","SCPI_Trigger_IIC","SourceQ"),//查询IIC触发的触发源
            new SCPICommandStruct(":TRIGger:IIC:TYPE","SCPI_Trigger_IIC","Type"),//设置IIC触发的触发类型
            new SCPICommandStruct(":TRIGger:IIC:TYPE?","SCPI_Trigger_IIC","TypeQ"),//查询IIC触发的触发类型
            new SCPICommandStruct(":TRIGger:IIC:ADDRess","SCPI_Trigger_IIC","Address"),//当IIC触发条件为NACKaddress、FRAM1或FRAM2时，设置IIC总线触发的触发地址
            new SCPICommandStruct(":TRIGger:IIC:ADDRess?","SCPI_Trigger_IIC","AddressQ"),//当IIC触发条件为NACKaddress、FRAM1或FRAM2时，查询IIC总线触发的触发地址
            new SCPICommandStruct(":TRIGger:IIC:RELation","SCPI_Trigger_IIC","Relation"),//当IIC触发条件为RDATa时，设置IIC总线触发的触发关系
            new SCPICommandStruct(":TRIGger:IIC:RELation?","SCPI_Trigger_IIC","RelationQ"),//当IIC触发条件为RDATa时，查询IIC总线触发的触发关系
            new SCPICommandStruct(":TRIGger:IIC:DATA","SCPI_Trigger_IIC","Data1"),//当IIC触发条件为RDATa、FRAM1或FRAM2时，设置IIC总线触发的触发数据
            new SCPICommandStruct(":TRIGger:IIC:DATA?","SCPI_Trigger_IIC","Data1Q"),//当IIC触发条件为RDATa、FRAM1或FRAM2时，查询IIC总线触发的触发数据
            new SCPICommandStruct(":TRIGger:IIC:DATA2","SCPI_Trigger_IIC","Data2"),//当IIC触发条件为RDATa、FRAM1或FRAM2时，设置IIC总线触发的触发数据
            new SCPICommandStruct(":TRIGger:IIC:DATA2?","SCPI_Trigger_IIC","Data2Q"),//当IIC触发条件为RDATa、FRAM1或FRAM2时，查询IIC总线触发的触发数据

            //Trigger 1553B
            new SCPICommandStruct(":TRIGger:1553B:SOURce","SCPI_Trigger_1553B","Source"),//设置1553B触发的触发源
            new SCPICommandStruct(":TRIGger:1553B:SOURce?","SCPI_Trigger_1553B","SourceQ"),//查询1553B触发的触发源
            new SCPICommandStruct(":TRIGger:1553B:TYPE","SCPI_Trigger_1553B","Type"),//设置1553B触发条件
            new SCPICommandStruct(":TRIGger:1553B:TYPE?","SCPI_Trigger_1553B","TypeQ"),//查询1553B触发条件
            new SCPICommandStruct(":TRIGger:1553B:CSWOrd","SCPI_Trigger_1553B","CsWord"),//设置1553B命令字
            new SCPICommandStruct(":TRIGger:1553B:CSWOrd?","SCPI_Trigger_1553B","CsWordQ"),//查询1553B命令字
            new SCPICommandStruct(":TRIGger:1553B:DWORd","SCPI_Trigger_1553B","DWord"),//设置1553B数据字
            new SCPICommandStruct(":TRIGger:1553B:DWORd?","SCPI_Trigger_1553B","DWordQ"),//查询1553B数据字
            new SCPICommandStruct(":TRIGger:1553B:RTADdress","SCPI_Trigger_1553B","RtAddress"),//设置1553B RT地址
            new SCPICommandStruct(":TRIGger:1553B:RTADdress?","SCPI_Trigger_1553B","RtAddressQ"),//查询1553B RT地址

            //Trigger arinc429
            new SCPICommandStruct(":TRIGger:429:SOURce","SCPI_Trigger_429","Source"),//设置429触发的触发源
            new SCPICommandStruct(":TRIGger:429:SOURce?","SCPI_Trigger_429","SourceQ"),//查询429触发的触发源
            new SCPICommandStruct(":TRIGger:429:TYPE","SCPI_Trigger_429","Type"),//设置429触发条件
            new SCPICommandStruct(":TRIGger:429:TYPE?","SCPI_Trigger_429","TypeQ"),//查询429触发条件
            new SCPICommandStruct(":TRIGger:429:WORD","SCPI_Trigger_429","Word"),//设置429触发字
            new SCPICommandStruct(":TRIGger:429:WORD?","SCPI_Trigger_429","WordQ"),//查询429触发字
            new SCPICommandStruct(":TRIGger:429:LABEl","SCPI_Trigger_429","Label"),//设置429标签
            new SCPICommandStruct(":TRIGger:429:LABEl?","SCPI_Trigger_429","LabelQ"),//查询429标签
            new SCPICommandStruct(":TRIGger:429:SDI","SCPI_Trigger_429","Sdi"),//设置429 SDI
            new SCPICommandStruct(":TRIGger:429:SDI?","SCPI_Trigger_429","SdiQ"),//查询429 SDI
            new SCPICommandStruct(":TRIGger:429:DATA","SCPI_Trigger_429","data"),//设置429数据
            new SCPICommandStruct(":TRIGger:429:DATA?","SCPI_Trigger_429","dataQ"),//查询429数据
            new SCPICommandStruct(":TRIGger:429:SSM","SCPI_Trigger_429","Ssm"),//设置429 SSM
            new SCPICommandStruct(":TRIGger:429:SSM?","SCPI_Trigger_429","SsmQ"),//查询429 SSM


            //时基命令 TIMebase
            new SCPICommandStruct(":TIMebase:EXTent","SCPI_Timebase","Extent"),//设置水平时基档位
            new SCPICommandStruct(":TIMebase:PLUS:EXTent","SCPI_Timebase","Plus_Extent"),//设置水平时基档位
            new SCPICommandStruct(":TIMebase:EXTent?","SCPI_Timebase","ExtentQ"),//查询水平时基档位
            new SCPICommandStruct(":TIMebase:MODE","SCPI_Timebase","Mode"),//设置屏幕时基显示方式
            new SCPICommandStruct(":TIMebase:MODE?","SCPI_Timebase","ModeQ"),//查询屏幕时基显示方式
            new SCPICommandStruct(":TIMebase:ROLL:DISPlay","SCPI_Timebase","Roll_Display"),//滚屏设置
            new SCPICommandStruct(":TIMebase:ROLL:DISPlay?","SCPI_Timebase","Roll_DisplayQ"),//滚屏查询
            new SCPICommandStruct(":TIMebase:XY1:DISPlay","SCPI_Timebase","XY1_Display"),//打开或关闭通道1和通道2的XY模式显示
            new SCPICommandStruct(":TIMebase:XY1:DISPlay?","SCPI_Timebase","XY1_DisplayQ"),//查询通道1和通道2的XY模式显示
            //timerbase:offset 协议1.1 换名为 timerbase:position 2016.12.8
            new SCPICommandStruct(":TIMebase:POSition","SCPI_Timebase","Position"),//设置波形显示的水平偏移
            new SCPICommandStruct(":TIMebase:OFFSet","SCPI_Timebase","Offset"),//设置波形显示的水平偏移
            new SCPICommandStruct(":TIMebase:PLUS:OFFSet","SCPI_Timebase","Plus_Offset"),//设置波形显示的水平偏移
            new SCPICommandStruct(":TIMebase:PLUS:POSition","SCPI_Timebase","Plus_Position"),//设置波形显示的水平偏移
            new SCPICommandStruct(":TIMebase:POSition?","SCPI_Timebase","PositionQ"),//查询波形显示的水平偏移
            new SCPICommandStruct(":TIMebase:OFFSet?","SCPI_Timebase","OffsetQ"),//查询波形显示的水平偏移
            new SCPICommandStruct(":TIMebase:ZOOm:SCAle","SCPI_Timebase","Scale"),//设置ZOOM缩放
            new SCPICommandStruct(":TIMebase:ZOOm:SCAle?","SCPI_Timebase","ScaleQ"),//查询ZOOM缩放
            new SCPICommandStruct(":TIMebase:LIST?","SCPI_Timebase","ListQ"),//查询时基列表

            //存储命令 STORage
            new SCPICommandStruct(":STORage:SAVE","SCPI_Storage","Save"),//存储指定通道的波形到指定位置
            new SCPICommandStruct(":STORage:LOAD","SCPI_Storage","Load"),//载入ref
            new SCPICommandStruct(":STORage:CAPTure","SCPI_Storage","Capture"),//屏幕截图
            new SCPICommandStruct(":STORage:CAPTure:TIME","SCPI_Storage","Capture_Time"),//屏幕截图延时
            new SCPICommandStruct(":STORage:CAPTure:TIME?","SCPI_Storage","Capture_TimeQ"),//查询屏幕截图延时
            new SCPICommandStruct(":STORage:CAPTure:INCOlor","SCPI_Storage","Capture_Incolor"),//屏幕截图反色
            new SCPICommandStruct(":STORage:CAPTure:INCOlorQ","SCPI_Storage","Capture_IncolorQ"),//查询屏幕截图反色
            new SCPICommandStruct(":STORage:CAPTure:THUMbnail","SCPI_Storage","Capture_Thumbnail"),//屏幕截图缩略图
            new SCPICommandStruct(":STORage:CAPTure:THUMbnail?","SCPI_Storage","Capture_ThumbnailQ"),//查询屏幕截图缩略图
            new SCPICommandStruct(":STORage:CAPTure:STARt","SCPI_Storage","Capture_Start"),//开始屏幕截图
            new SCPICommandStruct(":STORage:DEPTh","SCPI_Storage","Depth"),//设置示波器存储深度
            new SCPICommandStruct(":STORage:DEPTh?","SCPI_Storage","DepthQ"),//查询示波器存储深度
            new SCPICommandStruct(":STORage:CONSave:FILename","SCPI_Storage","ConSave"),//存储示波器设置
            new SCPICommandStruct(":STORage:CONSave:STARt","SCPI_Storage","ConSave_start"),//开始存储示波器设置
            new SCPICommandStruct(":STORage:CONLoad:FILename","SCPI_Storage","ConLoad"),//调用示波器设置
            new SCPICommandStruct(":STORage:RECord","SCPI_Storage","Record"),//设置示波器录制功能的打开与关闭
            new SCPICommandStruct(":STORage:RECord?","SCPI_Storage","RecordQ"),//查询示波器录制功能的打开与关闭
            new SCPICommandStruct(":STORage:PLAY","SCPI_Storage","Play"),//设置示波器回放功能的打开和关闭
            new SCPICommandStruct(":STORage:PLAY?","SCPI_Storage","PlayQ"),//查询示波器回放功能的打开和关闭
            new SCPICommandStruct(":STORage:PLAY:SPEed","SCPI_Storage","Play_Speed"),//设置示波器回放快进选项
            new SCPICommandStruct(":STORage:PLAY:SPEed?","SCPI_Storage","Play_SpeedQ"),//查询示波器回放快进选项
            new SCPICommandStruct(":STORage:PLAY:BACK","SCPI_Storage","Play_Back"),//设置示波器回放后退选项
            new SCPICommandStruct(":STORage:PLAY:BACK?","SCPI_Storage","Play_backQ"),//查询示波器回放后退选项
            new SCPICommandStruct(":STORage:SAVE:SOURce", "SCPI_Storage","Save_Source"),//设置存储源
            new SCPICommandStruct(":STORage:SAVE:SOURce?", "SCPI_Storage","Save_SourceQ"),//查询存储源
            new SCPICommandStruct(":STORage:SAVE:LOCAtion", "SCPI_Storage","Save_Location"),//设置存储位置
            new SCPICommandStruct(":STORage:SAVE:LOCAtion?", "SCPI_Storage","Save_LocationQ"),//查询存储位置
            new SCPICommandStruct(":STORage:SAVE:TYPE", "SCPI_Storage","Save_Type"),//设置存储类型
            new SCPICommandStruct(":STORage:SAVE:TYPE?", "SCPI_Storage","Save_TypeQ"),//查询存储类型
            new SCPICommandStruct(":STORage:SAVE:FILename","SCPI_Storage","Save_Filename"),//设置存储文件名
            new SCPICommandStruct(":STORage:SAVE:FILename?", "SCPI_Storage","Save_FilenameQ"),//查询存储文件名
            new SCPICommandStruct(":STORage:SAVE:START", "SCPI_Storage","Save_Start"),//开始存储
            new SCPICommandStruct(":STORage:SAVE:ALLSegments", "SCPI_Storage","Save_ALLSegments"),//存储所有分段
            new SCPICommandStruct(":STORage:SAVE:ALLSegments?", "SCPI_Storage","Save_ALLSegmentsQ"),//查询存储所有分段

            new SCPICommandStruct(":STORage:Data:Type", "SCPI_Storage","Save_DataType"),//设置数据类型
            new SCPICommandStruct(":STORage:Data:Status?", "SCPI_Storage","Save_DataStatusQ"),//查询数据存储状态
            new SCPICommandStruct(":STORage:Data:CSV?", "SCPI_Storage","Save_DataCSVQ"),//查询CSV数据
            new SCPICommandStruct(":STORage:Data:PNG?", "SCPI_Storage","Save_DataPNGQ"),//查询PNG截图数据
            new SCPICommandStruct(":STORage:Data:MSS?", "SCPI_Storage","Save_DataMSSQ"),//查询MSS截图数据


            //PASS/FAIL命令 MASK
            new SCPICommandStruct(":MASK:SOURce","SCPI_Mask","Source"),//设置pass/fial测试的通道源
            new SCPICommandStruct(":MASK:SOURce?","SCPI_Mask","SourceQ"),//查询pass/fial测试的通道源
            new SCPICommandStruct(":MASK:RANGe","SCPI_Mask","Range"),//设置模板测试的测试区域
            new SCPICommandStruct(":MASK:RANGe?","SCPI_Mask","RangeQ"),//查询模板测试的测试区域
            new SCPICommandStruct(":MASK:STATistic","SCPI_Mask","Statistic"),//打开或关闭pass/fail测试时的统计功能状态，统计信息包括通过、失败、和总的测试帧数
            new SCPICommandStruct(":MASK:STATistic?","SCPI_Mask","StatisticQ"),//查询pass/fail测试时的统计功能状态打开或关闭
            new SCPICommandStruct(":MASK:RESet","SCPI_Mask","Reset"),//复位模板测试统计信息
            new SCPICommandStruct(":MASK:SOOutput","SCPI_Mask","SoOutput"),//打开或关闭"输出即停"
            new SCPICommandStruct(":MASK:SOOutput?","SCPI_Mask","SoOutputQ"),//查询"输出即停"打开或关闭
            new SCPICommandStruct(":MASK:AUXout","SCPI_Mask","AuxOut"),//打开模板测试的完成响应
            new SCPICommandStruct(":MASK:AUXout?","SCPI_Mask","AuxOutQ"),//查询模板测试的完成响应
            new SCPICommandStruct(":MASK:ENABle","SCPI_Mask","Enable"),//打开或关闭模板测试
            new SCPICommandStruct(":MASK:ENABle?","SCPI_Mask","EnableQ"),//查询模板测试打开或关闭
            new SCPICommandStruct(":MASK:OPERate","SCPI_Mask","Operate"),//控制pass/fail测试的运行和停止
            new SCPICommandStruct(":MASK:OPERate?","SCPI_Mask","OperateQ"),//查询pass/fail测试的运行和停止
            new SCPICommandStruct(":MASK:X","SCPI_Mask","X"),//设置pass/fail测试的规则中的"水平调整"参数
            new SCPICommandStruct(":MASK:X?","SCPI_Mask","XQ"),//查询pass/fail测试的规则中的"水平调整"参数
            new SCPICommandStruct(":MASK:Y","SCPI_Mask","Y"),//设置pass/fail测试的规则中的"垂直调整"参数
            new SCPICommandStruct(":MASK:Y?","SCPI_Mask","YQ"),//查询pass/fail测试的规则中的"垂直调整"参数

            //参考波形命令 REF
            new SCPICommandStruct(":REFerence:DISPlay","SCPI_Reference","Display"),//打开或关闭REF功能
            new SCPICommandStruct(":REFerence:DISPlay?","SCPI_Reference","DisplayQ"),//查询REF功能打开或关闭
            new SCPICommandStruct(":REFerence:ENABle","SCPI_Reference","Enable"),//打开或关闭指定的参考通道
            new SCPICommandStruct(":REFerence:ENABle?","SCPI_Reference","EnableQ"),//查询指定的参考通道打开或关闭
            new SCPICommandStruct(":REFerence:HSCale","SCPI_Reference","Hscale"),//设置参考通道的水平档位
            new SCPICommandStruct(":REFerence:PLUS:HSCale","SCPI_Reference","Plus_Hscale"),//设置参考通道的水平档位
            new SCPICommandStruct(":REFerence:HSCale?","SCPI_Reference","HscaleQ"),//查询参考通道的水平档位
            new SCPICommandStruct(":REFerence:VSCale","SCPI_Reference","Vscale"),//设置参考通道的垂直档位
            new SCPICommandStruct(":REFerence:PLUS:VSCale","SCPI_Reference","Plus_Vscale"),//设置参考通道的垂直档位
            new SCPICommandStruct(":REFerence:VSCale?","SCPI_Reference","VscaleQ"),//查询参考通道的垂直档位
            new SCPICommandStruct(":REFerence:CURRent","SCPI_Reference","Current"),//选择当前参考通道
            new SCPICommandStruct(":REFerence:PLUS:HOFFset","SCPI_Reference","Plus_Hoffset"),//设置水平偏移
            new SCPICommandStruct(":REFerence:PLUS:VOFFset","SCPI_Reference","Plus_Voffset"),//设置垂直偏移
            //1.1新添加 2016.12.8
            new SCPICommandStruct(":REFerence:POSition","SCPI_Reference","Position"),//设置垂直偏移
            new SCPICommandStruct(":REFerence:POSition?","SCPI_Reference","PositionQ"),//查询垂直偏移
            new SCPICommandStruct(":REFerence:TIMebase:POSition","SCPI_Reference","Timebase_Position"),//设置水平偏移
            new SCPICommandStruct(":REFerence:TIMebase:POSition?","SCPI_Reference","Timebase_PositionQ"),//查询水平偏移
            new SCPICommandStruct(":REFerence:PLUS:TIMebase:POSition","SCPI_Reference","Plus_Timebase_Position"),//设置水平偏移
            new SCPICommandStruct(":REFerence:PLUS:POSition","SCPI_Reference","Plus_position"),//设置垂直偏移
            new SCPICommandStruct(":REF:SRATe?","SCPI_Reference","REF_SRateQ"),//查询采样率
            new SCPICommandStruct(":REF:MDEPth?","SCPI_Reference","REF_MDepthQ"),//查询存储深度
            new SCPICommandStruct(":CURRent:REFerence?","SCPI_Reference","Curr_ref"),//查询当前参考通道
            //ref# 2022.8.10
            new SCPICommandStruct(":REFerence#:ENABle","SCPI_Reference","Enable"),//打开或关闭指定的参考通道
            new SCPICommandStruct(":REFerence#:ENABle?","SCPI_Reference","EnableQ"),//查询指定的参考通道打开或关闭
            new SCPICommandStruct(":REFerence#:HSCale","SCPI_Reference","Hscale"),//设置参考通道的水平档位
            new SCPICommandStruct(":REFerence#:PLUS:HSCale","SCPI_Reference","Plus_Hscale"),//设置参考通道的水平档位
            new SCPICommandStruct(":REFerence#:HSCale?","SCPI_Reference","HscaleQ"),//查询参考通道的水平档位
            new SCPICommandStruct(":REFerence#:VSCale","SCPI_Reference","Vscale"),//设置参考通道的垂直档位
            new SCPICommandStruct(":REFerence#:PLUS:VSCale","SCPI_Reference","Plus_Vscale"),//设置参考通道的垂直档位
            new SCPICommandStruct(":REFerence#:VSCale?","SCPI_Reference","VscaleQ"),//查询参考通道的垂直档位
            new SCPICommandStruct(":REFerence#:CURRent","SCPI_Reference","Current"),//选择当前参考通道
            new SCPICommandStruct(":REFerence#:VPOSition","SCPI_Reference","Position"),//设置垂直偏移
            new SCPICommandStruct(":REFerence#:VPOSition?","SCPI_Reference","PositionQ"),//查询垂直偏移
            new SCPICommandStruct(":REFerence#:HPOSition","SCPI_Reference","Timebase_Position"),//设置水平偏移
            new SCPICommandStruct(":REFerence#:HPOSition?","SCPI_Reference","Timebase_PositionQ"),//查询水平偏移
            new SCPICommandStruct(":REFerence#:PLUS:VPOSition","SCPI_Reference","Plus_position"),//设置垂直偏移
            new SCPICommandStruct(":REFerence#:PLUS:HPOSition","SCPI_Reference","Plus_Timebase_Position"),//设置水平偏移
            new SCPICommandStruct(":REF#:SRATe?","SCPI_Reference","REF_SRateQ"),//查询采样率
            new SCPICommandStruct(":REF#:MDEPth?","SCPI_Reference","REF_MDepthQ"),//查询存储深度

            //AUTO
            new SCPICommandStruct(":AUTO:SET:CHANnel","SCPI_Auto","Set_Channel"),//设置自动打开通道
            new SCPICommandStruct(":AUTO:SET:CHANnel?","SCPI_Auto","Set_ChannelQ"),//查询自动打开通道
            new SCPICommandStruct(":AUTO:SET:LEVel","SCPI_Auto","Set_Level"),//设置门限电平
            new SCPICommandStruct(":AUTO:SET:LEVel?","SCPI_Auto","Set_LevelQ"),//查询门限电平
            new SCPICommandStruct(":AUTO:SET:SOURce","SCPI_Auto","Set_Source"),//设置触发源
            new SCPICommandStruct(":AUTO:SET:SOURce?","SCPI_Auto","Set_SourceQ"),//查询触发源

            new SCPICommandStruct(":AUTO:RANge","SCPI_Auto","Range"),//设置自动量程
            new SCPICommandStruct(":AUTO:RANge?","SCPI_Auto","RangeQ"),//查询自动量程
            new SCPICommandStruct(":AUTO:RANge:VERtical","SCPI_Auto","Range_Vertical"),//设置自动垂直
            new SCPICommandStruct(":AUTO:RANge:VERtical?","SCPI_Auto","Range_VerticalQ"),//查询自动垂直
            new SCPICommandStruct(":AUTO:RANge:HORizontal","SCPI_Auto","Range_Horizontal"),//设置自动水平
            new SCPICommandStruct(":AUTO:RANge:HORizontal?","SCPI_Auto","Range_HorizontalQ"),//查询自动水平
            new SCPICommandStruct(":AUTO:RANge:LEVel","SCPI_Auto","Range_Level"),//设置自动量程
            new SCPICommandStruct(":AUTO:RANge:LEVel?","SCPI_Auto","Range_LevelQ"),//查询自动量程

            //bus
            new SCPICommandStruct(":BUS#:DISPlay","SCPI_Bus","Display"),//设置总线显示
            new SCPICommandStruct(":BUS#:DISPlay?","SCPI_Bus","DisplayQ"), //查询总线显示
            new SCPICommandStruct(":BUS#:TYPE","SCPI_Bus","Type"),//设置总线类型
            new SCPICommandStruct(":BUS#:TYPE?","SCPI_Bus","TypeQ"),//查询总线类型
            new SCPICommandStruct(":BUS#:MODE","SCPI_Bus","Mode"),//设置总线模式
            new SCPICommandStruct(":BUS#:MODE?","SCPI_Bus","ModeQ"),//查询总线模式
            new SCPICommandStruct(":BUS#:LEVel","SCPI_Bus","Level"),//设置总线触发电平
            new SCPICommandStruct(":BUS#:LEVel?","SCPI_Bus","LevelQ"),//查询总线触发电平
            new SCPICommandStruct(":BUS#:HLEVel","SCPI_Bus","HLevel"),//设置总线高触发电平
            new SCPICommandStruct(":BUS#:HLEVel?","SCPI_Bus","HLevelQ"),//查询总线高触发电平
            new SCPICommandStruct(":BUS#:LLEVel","SCPI_Bus","LLevel"),//设置总线低触发电平
            new SCPICommandStruct(":BUS#:LLEVel?","SCPI_Bus","LLevelQ"),//查询总线低触发电平
            new SCPICommandStruct(":BUS#:Data?","SCPI_Bus","DataQ"),//查询总线解码数据

            new SCPICommandStruct(":BUS#:UART:RX","SCPI_Bus_Uart","Rx"),//设置UART RX通道
            new SCPICommandStruct(":BUS#:UART:RX?","SCPI_Bus_Uart","RxQ"),//查询UART RX通道
            new SCPICommandStruct(":BUS#:UART:IDLElvl","SCPI_Bus_Uart","IdLevel"),//设置UART空闲电平
            new SCPICommandStruct(":BUS#:UART:IDLElvl?","SCPI_Bus_Uart","IdLevelQ"),//查询UART空闲电平
            new SCPICommandStruct(":BUS#:UART:BAUDrate","SCPI_Bus_Uart","BaudRate"),//设置UART波特率
            new SCPICommandStruct(":BUS#:UART:BAUDrate?","SCPI_Bus_Uart","BaudRateQ"),//查询UART波特率
            new SCPICommandStruct(":BUS#:UART:CHECK","SCPI_Bus_Uart","Check"),//设置UART校验方式
            new SCPICommandStruct(":BUS#:UART:CHECK?","SCPI_Bus_Uart","CheckQ"),//查询UART校验方式
            new SCPICommandStruct(":BUS#:UART:USERbaud","SCPI_Bus_Uart","UserBaud"),//设置UART自定义波特率
            new SCPICommandStruct(":BUS#:UART:USERbaud?","SCPI_Bus_Uart","UserBaudQ"),//查询UART自定义波特率
            new SCPICommandStruct(":BUS#:UART:WIDTh","SCPI_Bus_Uart","Width"),//设置UART数据位宽度
            new SCPICommandStruct(":BUS#:UART:WIDTh?","SCPI_Bus_Uart","WidthQ"),//查询UART数据位宽度
            new SCPICommandStruct(":BUS#:UART:DISPlay","SCPI_Bus_Uart","Display"),//设置UART显示格式
            new SCPICommandStruct(":BUS#:UART:DISPlay?","SCPI_Bus_Uart","DisplayQ"),//查询UART显示格式
            new SCPICommandStruct(":BUS#:UART:LEVel","SCPI_Trigger_Uart","Level"),//设置UART触发时的触发电平
            new SCPICommandStruct(":BUS#:UART:LEVel?","SCPI_Trigger_Uart","LevelQ"),//查询UART触发时的触发电平

            new SCPICommandStruct(":BUS#:LIN:CHANnel","SCPI_Bus_Lin","Channel"),//设置LIN通道
            new SCPICommandStruct(":BUS#:LIN:CHANnel?","SCPI_Bus_Lin","ChannelQ"),//查询LIN通道
            new SCPICommandStruct(":BUS#:LIN:IDLElvl","SCPI_Bus_Lin","IdLevel"),//设置LIN空闲电平
            new SCPICommandStruct(":BUS#:LIN:IDLElvl?","SCPI_Bus_Lin","IdLevelQ"),//查询LIN空闲电平
            new SCPICommandStruct(":BUS#:LIN:BAUDrate","SCPI_Bus_Lin","BaudRate"),//设置LIN波特率
            new SCPICommandStruct(":BUS#:LIN:BAUDrate?","SCPI_Bus_Lin","BaudRateQ"),//查询LIN波特率
            new SCPICommandStruct(":BUS#:LIN:USERbaud","SCPI_Bus_Lin","UserBaud"),//设置LIN自定义波特率
            new SCPICommandStruct(":BUS#:LIN:USERbaud?","SCPI_Bus_Lin","UserBaudQ"),//查询LIN自定义波特率
            new SCPICommandStruct(":BUS#:LIN:LEVel","SCPI_Trigger_Lin","Level"),//设置LIN触发时的触发电平
            new SCPICommandStruct(":BUS#:LIN:LEVel?","SCPI_Trigger_Lin","LevelQ"),//查询LIN触发时的触发电平

            new SCPICommandStruct(":BUS#:SPI:CLK","SCPI_Bus_Spi","Clk"),//设置SPI时钟通道
            new SCPICommandStruct(":BUS#:SPI:CLK?","SCPI_Bus_Spi","ClkQ"),//查询SPI时钟通道
            new SCPICommandStruct(":BUS#:SPI:DATA","SCPI_Bus_Spi","Data"),//设置SPI数据通道
            new SCPICommandStruct(":BUS#:SPI:DATA?","SCPI_Bus_Spi","DataQ"),//查询SPI数据通道
            new SCPICommandStruct(":BUS#:SPI:WIDTh","SCPI_Bus_Spi","Width"),//设置SPI位宽
            new SCPICommandStruct(":BUS#:SPI:WIDTh?","SCPI_Bus_Spi","WidthQ"),//查询SPI位宽
            new SCPICommandStruct(":BUS#:SPI:IDLElvl","SCPI_Bus_Spi","IdLevel"),//设置SPI空闲电平
            new SCPICommandStruct(":BUS#:SPI:IDLElvl?","SCPI_Bus_Spi","IdLevelQ"),//查询SPI空闲电平
            new SCPICommandStruct(":BUS#:SPI:SLOPe","SCPI_Bus_Spi","Slope"),//设置SPI边沿
            new SCPICommandStruct(":BUS#:SPI:SLOPe?","SCPI_Bus_Spi","SlopeQ"),//查询SPI边沿
            new SCPICommandStruct(":BUS#:SPI:CS","SCPI_Bus_Spi","CS"),//设置SPI片选使能
            new SCPICommandStruct(":BUS#:SPI:CS?","SCPI_Bus_Spi","CSQ"),//查询SPI片选使能
            new SCPICommandStruct(":BUS#:SPI:CS:SOURce","SCPI_Bus_Spi","Source"),//设置SPI片选源
            new SCPICommandStruct(":BUS#:SPI:CS:SOURce?","SCPI_Bus_Spi","SourceQ"),//查询SPI片选源
            new SCPICommandStruct(":BUS#:SPI:CS:IDLElvl","SCPI_Bus_Spi","Idlelvl"),//设置SPI片选空闲电平
            new SCPICommandStruct(":BUS#:SPI:CS:IDLElvl?","SCPI_Bus_Spi","IdlelvlQ"),//查询SPI片选空闲电平
            new SCPICommandStruct(":BUS#:SPI:CLKLevel","SCPI_Trigger_SPI","LevelCLK"),//设置SPI触发时的时钟触发电平
            new SCPICommandStruct(":BUS#:SPI:CLKLevel?","SCPI_Trigger_SPI","LevelCLKQ"),//查询SPI触发时的时钟触发电平
            new SCPICommandStruct(":BUS#:SPI:DATLevel","SCPI_Trigger_SPI","LevelData"),//设置SPI触发时的数据触发电平
            new SCPICommandStruct(":BUS#:SPI:DATLevel?","SCPI_Trigger_SPI","LevelDataQ"),//查询SPI触发时的数据触发电平
            new SCPICommandStruct(":BUS#:SPI:CSLEvel","SCPI_Trigger_SPI","LevelCS"),//设置SPI触发时的片选触发电平
            new SCPICommandStruct(":BUS#:SPI:CSLEvel?","SCPI_Trigger_SPI","LevelCSQ"),//查询SPI触发时的片选触发电平

            new SCPICommandStruct(":BUS#:CAN:CHANnel","SCPI_Bus_Can","Channel"),//设置CAN通道
            new SCPICommandStruct(":BUS#:CAN:CHANnel?","SCPI_Bus_Can","ChannelQ"),//查询CAN通道
            new SCPICommandStruct(":BUS#:CAN:SIGNal","SCPI_Bus_Can","Signal"),//设置CAN信号线
            new SCPICommandStruct(":BUS#:CAN:SIGNal?","SCPI_Bus_Can","SignalQ"),//查询CAN信号线
            new SCPICommandStruct(":BUS#:CAN:BAUDrate","SCPI_Bus_Can","BaudRate"),//设置CAN波特率
            new SCPICommandStruct(":BUS#:CAN:BAUDrate?","SCPI_Bus_Can","BaudRateQ"),//查询CAN波特率
            new SCPICommandStruct(":BUS#:CAN:USERbaud","SCPI_Bus_Can","UserBaud"),//设置CAN自定义波特率
            new SCPICommandStruct(":BUS#:CAN:USERbaud?","SCPI_Bus_Can","UserBaudQ"),//查询CAN自定义波特率
            new SCPICommandStruct(":BUS#:CAN:SAMPlepoint","SCPI_Bus_Can","SAMPlepoint"),//设置CAN采样点
            new SCPICommandStruct(":BUS#:CAN:SAMPlepoint?","SCPI_Bus_Can","SAMPlepointQ"),//查询CAN采样点
            new SCPICommandStruct(":BUS#:CAN:FDBAudrate","SCPI_Bus_Can","FDBAudrate"),//设置CAN FD波特率
            new SCPICommandStruct(":BUS#:CAN:FDBAudrate?","SCPI_Bus_Can","FDBAudrateQ"),//查询CAN FD波特率
            new SCPICommandStruct(":BUS#:CAN:FDUSerbaud","SCPI_Bus_Can","FDUSerbaud"),//设置CAN FD自定义波特率
            new SCPICommandStruct(":BUS#:CAN:FDUSerbaud?","SCPI_Bus_Can","FDUSerbaudQ"),//查询CAN FD自定义波特率
            new SCPICommandStruct(":BUS#:CAN:FDSAmplepoint","SCPI_Bus_Can","FDSAmplepoint"),//设置CAN FD采样点
            new SCPICommandStruct(":BUS#:CAN:FDSAmplepoint?","SCPI_Bus_Can","FDSAmplepointQ"),//查询CAN FD采样点
            new SCPICommandStruct(":BUS#:CAN:LEVel","SCPI_Trigger_Can","Level"),//设置CAN触发时的触发电平
            new SCPICommandStruct(":BUS#:CAN:LEVel?","SCPI_Trigger_Can","LevelQ"),//查询CAN触发时的触发电平
            new SCPICommandStruct(":BUS#:CAN:ISO","SCPI_Bus_Can","ISO"),//设置CAN的ISO
            new SCPICommandStruct(":BUS#:CAN:ISO?","SCPI_Bus_Can","ISOQ"),//查询CAN的ISO

            new SCPICommandStruct(":BUS#:IIC:SDA","SCPI_Bus_IIC","SDA"),//设置IIC SDA通道
            new SCPICommandStruct(":BUS#:IIC:SDA?","SCPI_Bus_IIC","SDAQ"),//查询IIC SDA通道
            new SCPICommandStruct(":BUS#:IIC:SCL","SCPI_Bus_IIC","SCL"),//设置IIC SCL通道
            new SCPICommandStruct(":BUS#:IIC:SCL?","SCPI_Bus_IIC","SCLQ"),//查询IIC SCL通道
            new SCPICommandStruct(":BUS#:IIC:CLKLevel","SCPI_Trigger_IIC","LevelClock"),//设置IIC触发时的时钟触发电平
            new SCPICommandStruct(":BUS#:IIC:CLKLevel?","SCPI_Trigger_IIC","LevelClockQ"),//查询IIC触发时的时钟触发电平
            new SCPICommandStruct(":BUS#:IIC:DATLevel","SCPI_Trigger_IIC","LevelData"),//设置IIC触发时的数据触发电平
            new SCPICommandStruct(":BUS#:IIC:DATLevel?","SCPI_Trigger_IIC","LevelDataQ"),//查询IIC触发时的数据触发电平

            new SCPICommandStruct(":BUS#:1553B:SOURce","SCPI_Bus_1553B","Channel"),//设置1553B通道源
            new SCPICommandStruct(":BUS#:1553B:SOURce?","SCPI_Bus_1553B","ChannelQ"),//查询1553B通道源
            new SCPICommandStruct(":BUS#:1553B:DISPlay","SCPI_Bus_1553B","Display"),//设置1553B显示格式
            new SCPICommandStruct(":BUS#:1553B:DISPlay?","SCPI_Bus_1553B","DisplayQ"),//查询1553B显示格式
            new SCPICommandStruct(":BUS#:1553B:LEVEl","SCPI_Trigger_1553B","Level"),//设置1553B触发电平
            new SCPICommandStruct(":BUS#:1553B:LEVEl?","SCPI_Trigger_1553B","LevelQ"),//查询1553B触发电平

            //429总线命令
            new SCPICommandStruct(":BUS#:429:SOURce","SCPI_Bus_429","Source"),//设置429通道源
            new SCPICommandStruct(":BUS#:429:SOURce?","SCPI_Bus_429","SourceQ"),//查询429通道源
            new SCPICommandStruct(":BUS#:429:FORMat","SCPI_Bus_429","Format"),//设置429格式
            new SCPICommandStruct(":BUS#:429:FORMat?","SCPI_Bus_429","FormatQ"),//查询429格式
            new SCPICommandStruct(":BUS#:429:DISPlay","SCPI_Bus_429","Display"),//设置429显示格式
            new SCPICommandStruct(":BUS#:429:DISPlay?","SCPI_Bus_429","DisplayQ"),//查询429显示格式
            new SCPICommandStruct(":BUS#:429:BANDrate","SCPI_Bus_429","BandRate"),//设置429波特率
            new SCPICommandStruct(":BUS#:429:BANDrate?","SCPI_Bus_429","BandRateQ"),//查询429波特率
            new SCPICommandStruct(":BUS#:429:HLEVel","SCPI_Trigger_429","LevelHigh"),//设置429高触发电平
            new SCPICommandStruct(":BUS#:429:HLEVel?","SCPI_Trigger_429","LevelHighQ"),//查询429高触发电平
            new SCPICommandStruct(":BUS#:429:LLEVel","SCPI_Trigger_429","LevelLow"),//设置429低触发电平
            new SCPICommandStruct(":BUS#:429:LLEVel?","SCPI_Trigger_429","LevelLowQ"),//查询429低触发电平

            //生产校准SCPI
            new SCPICommandStruct(":ware","SCPI_Production","Ware"), //固件烧写
            new SCPICommandStruct(":application","SCPI_Production","Application"), //MCU、APP烧写
            new SCPICommandStruct(":SYS:WRIT?","SCPI_Production","WriteQ"), //产品信息写入查询
            new SCPICommandStruct(":SYS:SN?","SCPI_Production","SNQ"), //查询SN
            new SCPICommandStruct(":SYS:WDATe","SCPI_Production","WDate"), //写入生产日期
            new SCPICommandStruct(":PRIVate:UUID?","SCPI_Production","UUIDQ"), //查询唯一识别码
            new SCPICommandStruct(":PRIVate:HWVersion?","SCPI_Production","HWVersionQ"), //查询硬件版本
            new SCPICommandStruct(":PRIVate:SERiaino?","SCPI_Production","SeriaiNoQ"), //查询SN
            new SCPICommandStruct(":PRIVate:STRingcode","SCPI_Production","StringCode"), //串码写入
            new SCPICommandStruct(":PRIVate:DISPlay:SERiaino","SCPI_Production","DisplaySeriaiNo"), //写入外部SN号
            new SCPICommandStruct(":PRIVate:MACHinetype","SCPI_Production","MachineType"), //设置设备型号
            new SCPICommandStruct("PRIVate:STAR","SCPI_Production","Star"),// 开启私有
            new SCPICommandStruct("PRIVate:STATe?","SCPI_Production","StarQ"),//查询状态
            new SCPICommandStruct("PRIVate:STOP","SCPI_Production","Stop"), //关闭私有
            new SCPICommandStruct("PRIVate:BANDwidth","SCPI_Production","BandWidth"), //设置带宽
            new SCPICommandStruct("PRIVate:SETTing:CLEar","SCPI_Production","SettingClear"),//清除设置
            new SCPICommandStruct(":PRIVate:SYSID?","SCPI_Production","SysIdQ"), //查询SN
            new SCPICommandStruct("INTeface:TIME","SCPI_Production","Time"), //设置系统时间
            new SCPICommandStruct("INTeface:CLEAn","SCPI_Production","Clean"),//恢复系统设置
            new SCPICommandStruct("INTeface:SHUTdown","SCPI_Production","Shutdown"),//关机
            new SCPICommandStruct("INTeface:RESTart","SCPI_Production","Restart"),//重启
            new SCPICommandStruct("INTeface:STANdby","SCPI_Production","Standby"),//休眠
            new SCPICommandStruct("INTeface:WAKEup","SCPI_Production","Wakeup"),//唤醒
            new SCPICommandStruct("INTeface:LOCK","SCPI_Production","Lock"),//锁屏
            new SCPICommandStruct("INTeface:UNLock","SCPI_Production","Unlock"),//解锁
            new SCPICommandStruct(":SYS:Temperature?","SCPI_Production","SysTemperatureQ"),//查询系统温度
            new SCPICommandStruct(":SYS:FPGA:Temperature?","SCPI_Production","FpgaTemperatureQ"),//查询FPGA温度
            new SCPICommandStruct(":SYS:FPGA:Status?","SCPI_Production","SysFpgaStatusQ"),//查询FPGA状态

    };

    //endregion

    /** 当前正在处理的SCPI命令字符串 */
    private String curCommand=""; // 当前命令
    /** 当前批量命令的总数(分号分隔) */
    private int commandCount=0; // 命令总数
    /** 当前命令在批量命令中的索引(0-based) */
    private int commandIdx=0; // 当前命令索引
    /** 命令结果拼接缓冲区，多条命令的结果用分号拼接 */
    private StringBuilder sb=new StringBuilder(); // 结果缓冲区
    /** Android共享内存句柄，用于波形数据的高效传输(10MB) */
    private SharedMemory sharedMem; // 共享内存

    /**
     * 获取共享内存句柄(线程安全)。
     * 波形数据查询时，SCPI_Waveform通过此方法获取共享内存进行数据写入。
     * @return 共享内存对象
     */
    public synchronized SharedMemory getSharedMem(){
        return sharedMem; // 返回共享内存句柄
    }

    /**
     * 将波形数据写入共享内存(线程安全)。
     * 数据格式: [4字节长度][SCPI结果字符串][波形二进制数据][\r\n]
     * @param sb          SCPI结果字符串的StringBuilder
     * @param waveLen     波形数据长度(点数)
     * @param waveBak     波形二进制数据的ByteBuffer
     * @param dotLength   每个数据点的字节数
     */
    public synchronized void writeShareMem(StringBuilder sb, int waveLen, ByteBuffer waveBak, final int dotLength){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) { // Android 8.1+才支持SharedMemory
            try {
                ByteBuffer bf= sharedMem.mapReadWrite(); // 映射共享内存为可读写ByteBuffer
                //Log.d("Tag.Debug", String.format("SCPICommandDeal.writeShareMem: bf.cap:%s ,wave:limit:%s",bf.capacity(),waveBak.limit() ));

                bf.putInt(waveLen*dotLength+13); // 写入总长度(波形字节数+13字节头部)
                bf.put(sb.toString().getBytes()); // 写入SCPI结果字符串
                if (waveLen>0) { // 有波形数据时
                    bf.put(waveBak); // 写入波形二进制数据
                }
                bf.put("\r\n".getBytes()); // 写入结束标记\r\n
                SharedMemory.unmap(bf); // 解除共享内存映射
            } catch (ErrnoException e) {
                e.printStackTrace(); // 共享内存操作异常时打印堆栈
            }
        }
    }

    /**
     * SCPI命令解析入口(线程安全)。
     * 将SCPI命令字符串按分号分隔为多条子命令，逐条交给C层JNI解析。
     * JNI解析完成后会回调deal()方法进行命令分发。
     * @param command SCPI命令字符串(多条命令用分号分隔)
     */
    public synchronized void scpiParser(String command){

        sb.setLength(0); // 清空结果缓冲区
        String[] commands= command.split(";"); // 按分号分隔多条命令
        commandCount=commands.length; // 记录命令总数
        //Log.d("Tag.Debug", String.format("SCPICommandDeal.scpiParser count:%s",commandCount));
        for(int i=0;i<commands.length;i++) { // 逐条处理每条子命令
            commandIdx=i; // 记录当前命令索引
            this.curCommand = commands[i]; // 保存当前命令字符串
            //Log.d("Tag.Debug", String.format("SCPICommandDeal.scpiParser: %s", curCommand));
            param.clearData(); // 清空参数对象(重置为默认值)
            scpiCommand(curCommand+"\n", param); // 调用C层JNI解析命令(末尾加换行符)
        }
    }

    /**
     * C层SCPI解析JNI方法。
     * 该方法由C层libSCPI.so实现，解析SCPI命令字符串后回调deal()方法。
     * @param command SCPI命令字符串(含换行符)
     * @param param   SCPI参数对象，C层解析后填充此对象
     */
    public native void scpiCommand(String command,SCPIParam param);

    /**
     * SCPI命令分发核心方法 - 由C层JNI回调。
     * 根据C层解析得到的commandIndex，从scpi_commands数组查找对应的命令映射，
     * 通过Java反射机制调用SCPI_xxx命令处理类的静态方法。
     *
     * 错误处理:
     *   commandIndex=-1: 命令语法错误
     *   commandIndex=-2: 命令参数错误
     *   其他负值: 未知错误
     *
     * 结果处理:
     *   返回StringBuilder: 包含波形数据，sendMessage("",true)
     *   返回其他对象: 普通字符串结果，sendMessage(o.toString(),false)
     *   返回null: 命令执行成功但无返回值(如设置命令)
     *
     * @param param C层解析后填充的SCPI参数对象
     */
    public void deal(SCPIParam param) {
        if (param.commandIndex < 0){ // C层返回负值表示解析错误
            Logger.i(TAG,"ErrorEnum:SCPI command: "+ param.commandIndex + "," + curCommand); // 记录错误日志
            switch (param.commandIndex){ // 根据错误码分类处理
                case -1:{ // 命令语法错误
                    Logger.i(TAG,"Error:SCPI Command Error! " ); // 记录命令错误日志
                    if (curCommand.contains("?")) { // 查询命令错误时返回错误信息
//                        scpiService.sendMessage("Error:SCPI Command error!\r\n");
                        sendMessage("Error:SCPI Command error!",false); // 发送命令错误响应
                    }else { // 设置命令错误时发送空响应
//                        scpiService.sendMessage("");fa
                        sendMessage(null,false); // 发送空响应
                    }
                }break;
                case -2:{ // 命令参数错误
                    Logger.i(TAG,"Error:SCPI Param Error!" ); // 记录参数错误日志
                    if (curCommand.contains("?")) { // 查询命令参数错误时返回错误信息
//                        scpiService.sendMessage("Error:SCPI param error!\r\n");
                        sendMessage("Error:SCPI param error!",false); // 发送参数错误响应
                    }else { // 设置命令参数错误时发送空响应
//                        scpiService.sendMessage("");
                        sendMessage(null,false); // 发送空响应
                    }
                }break;
                default:{ // 其他未知错误
//                    scpiService.sendMessage("");
                    sendMessage(null,false); // 发送空响应
                }break;
            }

            return; // 错误处理后直接返回，不执行命令
        }
        Logger.i(TAG,"deal commandIndex:"+param.commandIndex+"  command:"+scpi_commands[param.commandIndex].command); // 记录命令分发日志
        SCPICommandStruct scpiCommand=scpi_commands[param.commandIndex]; // 根据索引获取命令映射结构体
        Logger.i(TAG,"class:"+scpiCommand.clasz+" method:"+scpiCommand.method); // 记录反射调用目标
        Class threadClazz = null; // 反射目标类引用
        try {
            threadClazz = Class.forName("com.micsig.tbook.tbookscope.scpi."+scpiCommand.clasz); // 反射加载命令处理类
            Method method = threadClazz.getMethod(scpiCommand.method, SCPIParam.class); // 反射获取命令处理方法
            Object o= method.invoke(null,param); // 反射调用静态方法(传入SCPIParam参数)
            if (o!=null) { // 方法返回非空结果
//                Logger.i(TAG, "object :" + o.toString());
                //发送出去
                if(o instanceof StringBuilder){ // 返回StringBuilder表示包含波形数据
//                    scpiService.sendMessage(o.toString());
                        sendMessage("",true); // 发送波形数据(空字符串头+波形标记true)
                    }else { // 返回普通对象(字符串)
                        sendMessage(o.toString(),false); // 发送普通字符串结果
                    }
                }else { // 方法返回null(设置命令通常无返回值)
//                    scpiService.sendMessage(o.toString() + "\r\n");
                    sendMessage(o.toString(),false); // 发送"null"字符串
                }
            }else { // 反射调用返回null(不应该发生)
//                scpiService.sendMessage("");
                sendMessage(null,false); // 发送空响应
            }
        } catch (ClassNotFoundException e) { // 命令处理类未找到
            e.printStackTrace(); // 打印异常堆栈
        } catch (NoSuchMethodException e) { // 命令处理方法未找到
            e.printStackTrace(); // 打印异常堆栈
        } catch (IllegalAccessException e) { // 反射访问权限不足
            e.printStackTrace(); // 打印异常堆栈
        } catch (InvocationTargetException e) { // 反射调用目标方法抛出异常
            e.printStackTrace(); // 打印异常堆栈
        }
    }

    /**
     * SCPI命令结果发送方法(内部使用)。
     * 负责将单条命令的执行结果拼接到结果缓冲区sb中，
     * 当所有命令处理完毕(最后一条命令)时，通过scpiService一次性发送给客户端。
     *
     * 拼接规则:
     *   多条命令结果之间用分号";"分隔
     *   最后一条命令结果后追加\r\n(普通响应)或分号(波形响应)
     *   微符号"渭"替换为"u"(编码兼容处理)
     *
     * @param result            命令执行结果字符串(null表示空响应)
     * @param isContainWaveForm 是否包含波形数据(来自SharedMemory)
     */
    private void sendMessage(String result,boolean isContainWaveForm){
        //Log.d("Tag.Debug", String.format("SCPICommandDeal.sendMessage: %s",commandIdx ));
        if (StrUtil.isEmpty(result) == false) { // 结果非空时拼接
            if (sb.length() >= 1) { // 缓冲区已有内容时加分号分隔
                sb.append(";"); // 多条命令结果间加分号
            }
            sb.append(result); // 拼接当前命令结果
        }
        if (commandIdx== commandCount-1){ // 最后一条命令时发送全部结果
            if (sb.length()==0){ // 缓冲区为空(所有命令均无返回值)
                sb.append(""); // 追加空字符串
                scpiService.sendMessage(sb.toString(),isContainWaveForm); // 发送空响应
                if (isContainWaveForm) return; // 波形数据已发送，直接返回
                //Log.d("Tag.Debug", String.format("SCPICommandDeal.sendMessage: %s",sb.toString() ));
            }else { // 缓冲区有内容
                if (isContainWaveForm){ // 包含波形数据
                    sb.append(";"); // 波形响应末尾加分号
                }else { // 普通文本响应
                    sb.append("\r\n"); // 追加SCPI标准结束标记\r\n
                }
                String content= sb.toString().replace("渭","u"); // 微符号"渭"替换为"u"(编码兼容)
                scpiService.sendMessage(content,isContainWaveForm); // 通过SCPI服务发送最终结果
                //Log.d("Tag.Debug", String.format("SCPICommandDeal.sendMessage: %s",sb.toString() ));
            }
        }
    }
}