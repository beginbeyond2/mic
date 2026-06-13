package com.micsig.tbook.scope.Calibrate;  // 定义包名：示波器校准功能模块

import android.os.SystemClock;  // 导入SystemClock类：Android系统时钟，用于测量时间
import android.util.Log;  // 导入Log类：Android日志输出工具

import com.micsig.base.Utils;  // 导入Utils类：基础工具类，提供CRC校验等功能
import com.micsig.tbook.hardware.Hardware;  // 导入Hardware类：硬件操作接口，提供E2PROM读写等功能
import com.micsig.tbook.scope.Scope;  // 导入Scope类：示波器核心管理，提供通道数量等信息
import com.micsig.tbook.scope.fpga.FPGACommand;  // 导入FPGACommand类：FPGA命令管理器

import java.io.IOException;  // 导入IOException类：IO异常
import java.nio.ByteBuffer;  // 导入ByteBuffer类：字节缓冲区，用于序列化校准数据
import java.nio.ByteOrder;  // 导入ByteOrder类：字节序定义（大端/小端）
import java.nio.charset.StandardCharsets;  // 导入StandardCharsets类：标准字符集
import java.text.SimpleDateFormat;  // 导入SimpleDateFormat类：日期格式化
import java.util.ArrayList;  // 导入ArrayList类：动态数组
import java.util.Arrays;  // 导入Arrays类：数组操作工具
import java.util.Date;  // 导入Date类：日期类
import java.util.List;  // 导入List类：列表接口

/**
 * 硬件操作抽象基类 - 校准数据存储与硬件控制核心实现
 * 
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.Calibrate（示波器校准功能模块）</li>
 *   <li>架构层级：硬件抽象层 - 硬件操作基类</li>
 *   <li>设计模式：模板方法模式 + 抽象工厂模式</li>
 *   <li>职责类型：校准数据存储、硬件控制、序列化/反序列化</li>
 * </ul>
 * 
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>实现IHW接口的通用方法</li>
 *   <li>管理校准数据的存储和加载（E2PROM）</li>
 *   <li>提供校准数据的序列化/反序列化方法</li>
 *   <li>定义子类需要实现的抽象方法</li>
 * </ul>
 * 
 * <p><b>校准数据存储结构：</b>
 * <pre>
 * ┌──────────────────────────────────────────────────────────────────────────┐
 * │   校准数据存储布局（E2PROM）                                               │
 * │                                                                          │
 * │   出厂校准数据区（FACTORY_CALIBRATE_ADDR起始）：                           │
 * │   ├── CRC16（2字节）                                                     │
 * │   ├── 数据长度（4字节）                                                  │
 * │   ├── 通道数识别码（4字节）                                              │
 * │   └── 校准系数数据                                                       │
 * │                                                                          │
 * │   用户校准数据区（FACTORY_CALIBRATE_ADDR + maxCofitSize起始）：            │
 * │   ├── CRC16（2字节）                                                     │
 * │   ├── 数据长度（4字节）                                                  │
 * │   ├── 通道数识别码（4字节）                                              │
 * │   └── 校准系数数据                                                       │
 * │                                                                          │
 * │   校准状态数据区（用户校准数据区 + maxCofitSize起始）：                     │
 * │   └── 各校准项目的完成状态                                               │
 * └──────────────────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <p><b>继承关系：</b>
 * <ul>
 *   <li>实现：IHW（硬件操作接口）</li>
 *   <li>子类：具体硬件型号的实现类（如MHO1002HW等）</li>
 * </ul>
 * 
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>依赖：Hardware（硬件操作接口，E2PROM读写）</li>
 *   <li>依赖：FPGACommand（FPGA命令管理器）</li>
 *   <li>依赖：Scope（示波器核心管理）</li>
 * </ul>
 * 
 * @author zhuzh
 * @version 1.0
 * @see IHW 硬件操作接口
 * @see Hardware 硬件操作类
 */
public abstract class HW implements IHW {

    /** 日志标签 */
    private static final String TAG = "HW";  // 日志输出标签
    
    /** 移位寄存器初始值（通用型号） */
    protected static final long SHIFTREG_INIT_VAL = 0xCDCDCDCDCDCDCDCDL;  // 移位寄存器默认初始值
    
    /** 移位寄存器初始值（MHO10008型号） */
    protected static final long SHIFTREG_INIT_VAL_MHO10008 = 0xFFFFFFFFFFFFFFFFL;  // MHO10008特殊初始值
    
    /** FPGA数量：决定循环执行命令的次数 */
    protected int fpgaNums = 2;  // 默认为2个FPGA


    /**
     * 构造方法：初始化硬件操作基类
     * 
     * @param fpgaNums FPGA数量
     */
    public HW(int fpgaNums){
        this.fpgaNums = fpgaNums;  // 保存FPGA数量
        bakCofitBuf = ByteBuffer.allocate(getMaxCofitSize() + 1024);  // 分配校准系数备份缓冲区
    }

    /**
     * 发送移位寄存器值
     * 
     * <p>将64位移位寄存器值发送到硬件。
     * 
     * @param val 64位移位寄存器值
     */
    public void sendShiftRegister(long val){
        Hardware.getInstance().sendShiftRegister( val, 64);  // 发送64位移位寄存器值
    }
    
    /**
     * 设置通道PGA增益值
     * 
     * <p>为每个FPGA的4个通道设置PGA（AD8370）增益值。
     * 
     * @param val 增益值数组，长度应为 fpgaNums * 4
     */
    public void setChPgaGain(int []val){
        FPGACommand cmd = FPGACommand.getInstance();  // 获取FPGA命令管理器单例
        for(int i=0;i<fpgaNums;i++){  // 遍历所有FPGA
            cmd.SendAD8370Data(i, Arrays.copyOfRange(val,i*4,4 + i * 4));  // 发送当前FPGA的4个通道PGA数据
        }
    }
    
    /**
     * 通道电源使能控制
     * 
     * <p>控制通道模拟电路的电源开关。默认为空实现，子类可覆盖。
     * 
     * @param bEnable true表示使能电源，false表示关闭电源
     */
    @Override
    public void ChPowerEnable(boolean bEnable) {
        // 默认为空实现，子类可覆盖
    }


    /**
     * 微秒级延时
     * 
     * <p>使用Thread.sleep实现微秒级延时。
     * 
     * @param us 延时时间（微秒）
     */
    protected void usleep(int us){
        try {
            Thread.sleep(us/1000,(us % 1000)*1000);  // 转换为毫秒和纳秒进行休眠
        } catch (InterruptedException e) {
            e.printStackTrace();  // 打印中断异常
        }
    }

    // ==================== 校准数据存储常量 ====================
    
    /** 出厂校准数据文件名 */
    protected static final String FACTORY_CALIBRATE_NAME = "factoryCalibrateData";  // 出厂校准数据标识
    
    /** 用户校准数据文件名 */
    protected static final String USER_CALIBRATE_NAME = "userCalibrateData";  // 用户校准数据标识

    /** 出厂校准数据E2PROM起始地址 */
    private static final int FACTORY_CALIBRATE_ADDR = 0x5600;  // E2PROM地址

    /** 通道数识别码基础值（低8位必须为0） */
    private final int CODE_CH_CNT=0x12E0_5500;  // 用于验证校准数据与通道数匹配

    /** 校准数据格式版本号常量 */
    public static final int CODE_VER_1 = 1;  // 版本1
    public static final int CODE_VER_2 = 2;  // 版本2
    public static final int CODE_VER_3 = 3;  // 版本3
    public static final int CODE_VER = CODE_VER_3 ;  // 当前使用的版本号

    /** 版本号掩码（bit[16:20]） */
    private final int CODE_VER_MASK = 0x001F_0000;  // 用于提取版本号
    
    /** 当前校准数据版本号 */
    private volatile int code_ver = 0;  // 使用volatile保证多线程可见性

    /**
     * 获取校准数据版本号
     * 
     * @return 版本号
     */
    public int getVer(){
        return code_ver;  // 返回当前版本号
    }
    
    /**
     * 设置校准数据版本号
     * 
     * @param v 版本号
     */
    public void setVer(int v){
        code_ver = v;  // 设置版本号
    }


    /**
     * 获取校准数据E2PROM地址
     * 
     * <p>根据文件名返回对应的E2PROM地址。
     * 
     * @param fileName 校准数据文件名
     * @return E2PROM地址
     */
    private int getCalibrateAddr(final String fileName){
        if(FACTORY_CALIBRATE_NAME.equals(fileName)){  // 出厂校准数据
            return getFactoryCalibrateAddr();  // 返回出厂校准地址
        }else if(USER_CALIBRATE_NAME.equals(fileName)){  // 用户校准数据
            return getUserCalibrateAddr();  // 返回用户校准地址
        }
        return getFactoryCalibrateAddr();  // 默认返回出厂校准地址
    }

    /**
     * 获取出厂校准数据地址
     * 
     * @return 出厂校准数据E2PROM地址
     */
    protected int getFactoryCalibrateAddr(){
        return FACTORY_CALIBRATE_ADDR;  // 返回固定的出厂校准地址
    }
    
    /**
     * 获取校准系数最大尺寸（抽象方法）
     * 
     * <p>由子类实现，返回校准系数数据的最大字节数。
     * 
     * @return 校准系数最大尺寸（字节）
     */
    protected abstract int getMaxCofitSize();

    /**
     * 获取用户校准数据地址
     * 
     * <p>用户校准数据紧跟在出厂校准数据之后。
     * 
     * @return 用户校准数据E2PROM地址
     */
    protected int getUserCalibrateAddr(){
        return getFactoryCalibrateAddr() + getMaxCofitSize();  // 出厂校准地址 + 校准数据大小
    }

    /**
     * 获取校准状态数据地址
     * 
     * <p>校准状态数据紧跟在用户校准数据之后。
     * 
     * @return 校准状态数据E2PROM地址
     */
    protected int getCalibrateStateAddr(){
        return getUserCalibrateAddr() + getMaxCofitSize();  // 用户校准地址 + 校准数据大小
    }
    
    /**
     * 获取用户Flash最大地址（抽象方法）
     * 
     * @return 用户Flash最大地址
     */
    protected abstract int getUseFlashMax();
    
    /**
     * 获取Flash最大地址（抽象方法）
     * 
     * @return Flash最大地址
     */
    protected abstract int getFlashMax();


    /**
     * 写入数据到E2PROM
     * 
     * @param addr E2PROM地址
     * @param data 要写入的数据
     * @return 写入的字节数，<=0表示失败
     */
    protected int write(int addr,byte[] data){
        Hardware hw = Hardware.getInstance();  // 获取硬件操作单例
        int r = hw.writeE2PROM(addr,data);  // 写入E2PROM
        if(r <= 0){
            Log.e(TAG,"save writeE2PROM : " + Integer.toHexString(addr));  // 输出错误日志
        }
        return r;  // 返回写入结果
    }

    /**
     * 从E2PROM读取数据
     * 
     * @param addr E2PROM地址
     * @param data 用于存储读取数据的缓冲区
     * @return 读取的字节数，<=0表示失败
     */
    protected int read(int addr,byte[] data){
        Hardware hw = Hardware.getInstance();  // 获取硬件操作单例
        int r = hw.readE2PROM(addr,data);  // 读取E2PROM
        if(r <= 0){
            Log.e(TAG,"save readE2PROM : " + Integer.toHexString(addr));  // 输出错误日志
        }
        return r;  // 返回读取结果
    }

    /**
     * 获取通道数识别码
     * 
     * <p>生成包含版本号和通道数的识别码，用于验证校准数据的兼容性。
     * 
     * @param chCnt 通道数量
     * @return 通道数识别码
     */
    private int getChNumCode(int chCnt){
        int v = getVer();  // 获取当前版本号
        if(v == 0){  // 如果版本号为0（未设置）
            v = CODE_VER;  // 使用默认版本号
        }
        return (CODE_CH_CNT + (v << 16)) + chCnt;  // 组合识别码：基础值 + 版本号 + 通道数
    }
    
    /**
     * 读取校准参数
     * 
     * <p>从E2PROM读取校准数据，验证CRC和通道数识别码，然后反序列化到内存。
     * 
     * @param fileName 校准数据文件名
     * @return 0表示成功，非0表示失败
     */
    protected int readCalibrateParam(final String fileName) {

        int xlen = 0;  // 读取长度
        int maxCofitSize = getMaxCofitSize();  // 获取校准数据最大尺寸
        byte[] bytes = new byte[maxCofitSize];  // 分配读取缓冲区
        xlen = read(getCalibrateAddr(fileName),bytes);  // 从E2PROM读取数据

        if(xlen > 0 && bytes != null) {  // 检查读取是否成功
            ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);  // 包装字节数组
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);  // 设置小端字节序

            int len = bytes.length;  // 数据长度
            if (len >= maxCofitSize) {  // 检查数据长度是否足够
                /*
                 * 读取校准数据，排列顺序如下：
                 * short crc；
                 * int len；
                 * int 通道数的识别码
                 * 校准数据
                 */
                short crc = byteBuffer.getShort(0);  // 读取CRC校验值
                int dataLen = byteBuffer.getInt(2);  // 读取数据长度
                int chNumCode = byteBuffer.getInt(6);  // 读取通道数识别码
                if(Utils.CRC16(byteBuffer.array(), 2, len - 2) == crc  // 验证CRC
                        && dataLen == len){  // 验证数据长度
                    int n = Scope.getInstance().getChNum();  // 获取当前通道数
                    code_ver = (chNumCode & CODE_VER_MASK) >> 16;  // 提取版本号
                    if((chNumCode & (~CODE_VER_MASK))  // 验证识别码（忽略版本号）
                            == (getChNumCode(n) & (~CODE_VER_MASK))){
                        int idx = ByteBuffer2cofit(byteBuffer, 10);  // 反序列化校准系数
                        Log.i(TAG, "完成校准数据文件的读取: " + fileName + ",idx:" + idx + ",Ver:" + code_ver);  // 输出成功日志
                        return 0;  // 返回成功
                    }else{
                        Log.i(TAG, "完成校准数据文件的读取: " + fileName + ",chNumCode:" + chNumCode + ",Ver:" + code_ver);  // 输出识别码不匹配日志
                    }
                }else{
                    Log.e(TAG,"wavelen:" + dataLen + ",crc:" +crc);  // 输出CRC错误日志
                }
            }
        }

        Log.e(TAG, "read "+" error!!," +xlen);  // 输出错误日志
        return 1;  // 返回失败
    }
    
    /**
     * 保存校准参数
     * 
     * <p>将内存中的校准系数序列化并写入E2PROM。
     * 
     * @param fileName 校准数据文件名
     * @return true表示成功，false表示失败
     */
    private synchronized boolean saveCalibrateParam(final String fileName)
    {
        int maxCofitSize = getMaxCofitSize();  // 获取校准数据最大尺寸
        ByteBuffer byteBuffer=ByteBuffer.allocate(maxCofitSize);  // 分配缓冲区
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);  // 设置小端字节序

        /*
         * 存储校准数据，排列顺序如下：
         * short crc；
         * int len；
         * int 通道数的识别码
         * 校准数据
         */
        byteBuffer.putInt(2, byteBuffer.limit());  // 写入数据长度
        byteBuffer.putInt(6, getChNumCode(Scope.getInstance().getChNum()));  // 写入通道数识别码

        int idx = cofit2ByteBuffer(byteBuffer, 10,true);  // 序列化校准系数

        short crc16 = Utils.CRC16(byteBuffer.array(), 2, byteBuffer.limit()-2);  // 计算CRC校验值
        byteBuffer.putShort(0, crc16);  // 写入CRC

        long s = SystemClock.elapsedRealtime();  // 记录开始时间
        int xlen = write(getCalibrateAddr(fileName),byteBuffer.array());  // 写入E2PROM

        if(xlen > 0){  // 检查写入是否成功
            Log.i(TAG,"完成校准数据文件的存储: "+fileName + ",idx:" + idx + ",s:" + (SystemClock.elapsedRealtime() - s));  // 输出成功日志
            return true;  // 返回成功
        }else {
            Log.e(TAG,"saveCalibrateParam");  // 输出错误日志
            return false;  // 返回失败
        }

    }

    /**
     * 加载校准数据
     * 
     * <p>优先加载用户校准数据，失败则加载出厂校准数据。
     * 
     * @return 0=用户校准数据加载成功，1=出厂校准数据加载成功，-1=全部失败
     */
    @Override
    public int loadCalibration(){
        int error = readCalibrateParam(USER_CALIBRATE_NAME);  // 尝试读取用户校准数据
        if(error != 0){  // 用户校准数据读取失败
            Log.e(TAG,"read user calibrate param fail,error namber="+error);  // 输出错误日志
            error = readCalibrateParam(FACTORY_CALIBRATE_NAME);  // 尝试读取出厂校准数据
            if(error != 0){  // 出厂校准数据也读取失败
                Log.e(TAG,"read factory calibrate param fail,error namber="+error);  // 输出错误日志
                defaultVal();  // 恢复默认校准系数
                clearCalibrationState();  // 清除校准状态
                return -1;  // 返回全部失败
            }
            Log.i(TAG,"read factory calibrate param succesful");  // 输出成功日志
            return 1;  // 返回出厂校准数据加载成功
        }
        Log.i(TAG,"read User calibrate param succesful");  // 输出成功日志
        return 0;  // 返回用户校准数据加载成功
    }
    
    /**
     * 保存校准数据
     * 
     * <p>同时保存出厂和用户校准数据。
     * 
     * @return true表示全部成功，false表示有失败
     */
    @Override
    public boolean saveCalibration(){
        return saveFactoryCalibration() && saveUserCalibration();  // 同时保存两种校准数据
    }
    
    /**
     * 保存出厂校准数据
     * 
     * @return true表示成功，false表示失败
     */
    @Override
    public boolean saveFactoryCalibration(){
        return saveCalibrateParam(FACTORY_CALIBRATE_NAME);  // 保存出厂校准数据
    }
    
    /**
     * 保存用户校准数据
     * 
     * @return true表示成功，false表示失败
     */
    @Override
    public boolean saveUserCalibration(){
        return saveCalibrateParam(USER_CALIBRATE_NAME);  // 保存用户校准数据
    }

    /**
     * 备份校准寄存器
     * 
     * <p>将当前校准系数备份到内存缓冲区，用于校准失败时恢复。
     */
    @Override
    public void backUpCabteRegister(){
        cofit2ByteBuffer(bakCofitBuf, 0);  // 序列化校准系数到备份缓冲区
    }
    
    /**
     * 恢复校准寄存器
     * 
     * <p>从备份缓冲区恢复校准系数。
     */
    @Override
    public void restoreCabteRegister(){
        ByteBuffer2cofit(bakCofitBuf, 0);  // 从备份缓冲区反序列化校准系数
    }

    /** 校准系数备份缓冲区 */
    private ByteBuffer bakCofitBuf;  // 用于存储校准系数的备份

    /**
     * 设置默认校准系数值（抽象方法）
     * 
     * <p>由子类实现，设置所有校准系数为默认值。
     */
    protected abstract void defaultVal();

    /**
     * 从ByteBuffer反序列化校准系数（抽象方法）
     * 
     * @param byteBuffer 字节缓冲区
     * @param idx 起始索引
     * @return 下一个可用索引
     */
    protected abstract int ByteBuffer2cofit(ByteBuffer byteBuffer, int idx);

    /**
     * 序列化校准系数到ByteBuffer（不保存时间戳）
     * 
     * @param byteBuffer 字节缓冲区
     * @param idx 起始索引
     * @return 下一个可用索引
     */
    protected int cofit2ByteBuffer(ByteBuffer byteBuffer, int idx){
        return cofit2ByteBuffer(byteBuffer,idx,false);  // 调用完整版本，不保存时间戳
    }

    /**
     * 序列化校准系数到ByteBuffer（抽象方法）
     * 
     * @param byteBuffer 字节缓冲区
     * @param idx 起始索引
     * @param bSave true表示保存时间戳
     * @return 下一个可用索引
     */
    protected abstract int cofit2ByteBuffer(ByteBuffer byteBuffer, int idx,boolean bSave);
    
    // ==================== 一维short数组反序列化 ====================
    
    /**
     * 从ByteBuffer读取一维short数组
     * 
     * @param cofitArray 目标数组
     * @param buf 字节缓冲区
     * @param idx 起始索引
     * @return 下一个可用索引
     */
    protected int getShortCoef1(short []cofitArray, ByteBuffer buf, int idx){
        buf.position(idx);  // 设置缓冲区位置
        for(int i=0;i<cofitArray.length;i++) {  // 遍历数组
            cofitArray[i] = buf.getShort();  // 读取short值
        }
        return buf.position();  // 返回当前位置
    }
    
    // ==================== 二维short数组反序列化 ====================
    
    /**
     * 从ByteBuffer读取二维short数组
     */
    protected int getShortCoef2(short [][]cofitArray, ByteBuffer buf, int idx){
        buf.position(idx);  // 设置缓冲区位置
        for(int i=0;i<cofitArray.length;i++) {  // 遍历第一维
            for (int j = 0; j < cofitArray[i].length; j++) {  // 遍历第二维
                cofitArray[i][j] = buf.getShort();  // 读取short值
            }
        }
        return buf.position();  // 返回当前位置
    }
    
    // ==================== 三维short数组反序列化 ====================
    
    /**
     * 从ByteBuffer读取三维short数组
     */
    protected int getShortCoef3(short [][][]cofitArray, ByteBuffer buf, int idx){
        buf.position(idx);  // 设置缓冲区位置
        for(int i=0;i<cofitArray.length;i++) {  // 遍历第一维
            for (int j = 0; j < cofitArray[i].length; j++) {  // 遍历第二维
                for (int k = 0; k < cofitArray[i][j].length; k++) {  // 遍历第三维
                    cofitArray[i][j][k] = buf.getShort();  // 读取short值
                }
            }
        }
        return buf.position();  // 返回当前位置
    }
    
    // ==================== 一维float数组反序列化 ====================
    
    /**
     * 从ByteBuffer读取一维float数组
     */
    protected int getFloatCoef1(float []cofitArray, ByteBuffer buf, int idx){
        buf.position(idx);  // 设置缓冲区位置
        for(int i=0;i<cofitArray.length;i++) {  // 遍历数组
            cofitArray[i] = buf.getFloat();  // 读取float值
        }
        return buf.position();  // 返回当前位置
    }
    
    // ==================== 二维float数组反序列化 ====================
    
    /**
     * 从ByteBuffer读取二维float数组
     */
    protected int getFloatCoef2(float [][]cofitArray, ByteBuffer buf, int idx){
        buf.position(idx);  // 设置缓冲区位置
        for(int i=0;i<cofitArray.length;i++) {  // 遍历第一维
            for (int j = 0; j < cofitArray[i].length; j++) {  // 遍历第二维
                cofitArray[i][j] = buf.getFloat();  // 读取float值
            }
        }
        return buf.position();  // 返回当前位置
    }
    
    // ==================== 三维float数组反序列化 ====================
    
    /**
     * 从ByteBuffer读取三维float数组
     */
    protected int getFloatCoef3(float [][][]cofitArray, ByteBuffer buf, int idx){
        buf.position(idx);  // 设置缓冲区位置
        for(int i=0;i<cofitArray.length;i++) {  // 遍历第一维
            for (int j = 0; j < cofitArray[i].length; j++) {  // 遍历第二维
                for (int k = 0; k < cofitArray[i][j].length; k++) {  // 遍历第三维
                    cofitArray[i][j][k] = buf.getFloat();  // 读取float值
                }
            }
        }
        return buf.position();  // 返回当前位置
    }
    
    // ==================== 四维float数组反序列化 ====================
    
    /**
     * 从ByteBuffer读取四维float数组
     */
    protected int getFloatCoef4(float [][][][]cofitArray, ByteBuffer buf, int idx){
        buf.position(idx);  // 设置缓冲区位置
        for(int i=0;i<cofitArray.length;i++) {  // 遍历第一维
            for (int j = 0; j < cofitArray[i].length; j++) {  // 遍历第二维
                for (int k = 0; k < cofitArray[i][j].length; k++) {  // 遍历第三维
                    for(int l=0;l<cofitArray[i][j][k].length;l++) {  // 遍历第四维
                        cofitArray[i][j][k][l] = buf.getFloat();  // 读取float值
                    }
                }
            }
        }
        return buf.position();  // 返回当前位置
    }
    
    // ==================== 四维short数组反序列化 ====================
    
    /**
     * 从ByteBuffer读取四维short数组
     */
    protected int getShortCoef4(short [][][][]cofitArray, ByteBuffer buf, int idx){
        buf.position(idx);  // 设置缓冲区位置
        for(int i=0;i<cofitArray.length;i++) {  // 遍历第一维
            for (int j = 0; j < cofitArray[i].length; j++) {  // 遍历第二维
                for (int k = 0; k < cofitArray[i][j].length; k++) {  // 遍历第三维
                    for (int l = 0; l < cofitArray[i][j][k].length; l++) {  // 遍历第四维
                        cofitArray[i][j][k][l] = buf.getShort();  // 读取short值
                    }
                }
            }
        }
        return buf.position();  // 返回当前位置
    }
    
    // ==================== 一维byte数组反序列化 ====================
    
    /**
     * 从ByteBuffer读取一维byte数组
     */
    protected int getByteCoef1(byte []cofitArray, ByteBuffer buf, int idx){
        buf.position(idx);  // 设置缓冲区位置
        for(int i=0;i<cofitArray.length;i++) {  // 遍历数组
            cofitArray[i] = buf.get();  // 读取byte值
        }
        return buf.position();  // 返回当前位置
    }
    
    // ==================== 二维byte数组反序列化 ====================
    
    /**
     * 从ByteBuffer读取二维byte数组
     */
    protected int getByteCoef2(byte [][]cofitArray, ByteBuffer buf, int idx){
        buf.position(idx);  // 设置缓冲区位置
        for(int i=0;i<cofitArray.length;i++) {  // 遍历第一维
            for (int j = 0; j < cofitArray[i].length; j++) {  // 遍历第二维
                cofitArray[i][j] = buf.get();  // 读取byte值
            }
        }
        return buf.position();  // 返回当前位置
    }
    
    // ==================== 一维short数组序列化 ====================
    
    /**
     * 将一维short数组写入ByteBuffer
     */
    protected int setShortCoef1(short []cofitArray, ByteBuffer buf, int idx){
        buf.position(idx);  // 设置缓冲区位置
        for(int i=0;i<cofitArray.length;i++) {  // 遍历数组
            buf.putShort(cofitArray[i]);  // 写入short值
        }
        return buf.position();  // 返回当前位置
    }
    
    // ==================== 二维short数组序列化 ====================
    
    /**
     * 将二维short数组写入ByteBuffer
     */
    protected int setShortCoef2(short [][]cofitArray, ByteBuffer buf, int idx){
        buf.position(idx);  // 设置缓冲区位置
        for(int i=0;i<cofitArray.length;i++) {  // 遍历第一维
            for (int j = 0; j < cofitArray[i].length; j++) {  // 遍历第二维
                buf.putShort(cofitArray[i][j]);  // 写入short值
            }
        }
        return buf.position();  // 返回当前位置
    }
    
    // ==================== 三维short数组序列化 ====================
    
    /**
     * 将三维short数组写入ByteBuffer
     */
    protected int setShortCoef3(short [][][]cofitArray, ByteBuffer buf, int idx){
        buf.position(idx);  // 设置缓冲区位置
        for(int i=0;i<cofitArray.length;i++) {  // 遍历第一维
            for (int j = 0; j < cofitArray[i].length; j++) {  // 遍历第二维
                for (int k = 0; k < cofitArray[i][j].length; k++) {  // 遍历第三维
                    buf.putShort(cofitArray[i][j][k]);  // 写入short值
                }
            }
        }
        return buf.position();  // 返回当前位置
    }
    
    // ==================== 一维float数组序列化 ====================
    
    /**
     * 将一维float数组写入ByteBuffer
     */
    protected int setFloatCoef1(float []cofitArray, ByteBuffer buf, int idx){
        buf.position(idx);  // 设置缓冲区位置
        for(int i=0;i<cofitArray.length;i++) {  // 遍历数组
            buf.putFloat(cofitArray[i]);  // 写入float值
        }
        return buf.position();  // 返回当前位置
    }
    
    // ==================== 二维float数组序列化 ====================
    
    /**
     * 将二维float数组写入ByteBuffer
     */
    protected int setFloatCoef2(float [][]cofitArray, ByteBuffer buf, int idx){
        buf.position(idx);  // 设置缓冲区位置
        for(int i=0;i<cofitArray.length;i++) {  // 遍历第一维
            for (int j = 0; j < cofitArray[i].length; j++) {  // 遍历第二维
                buf.putFloat(cofitArray[i][j]);  // 写入float值
            }
        }
        return buf.position();  // 返回当前位置
    }
    
    // ==================== 三维float数组序列化 ====================
    
    /**
     * 将三维float数组写入ByteBuffer
     */
    protected int setFloatCoef3(float [][][]cofitArray, ByteBuffer buf, int idx){
        buf.position(idx);  // 设置缓冲区位置
        for(int i=0;i<cofitArray.length;i++) {  // 遍历第一维
            for (int j = 0; j < cofitArray[i].length; j++) {  // 遍历第二维
                for (int k = 0; k < cofitArray[i][j].length; k++) {  // 遍历第三维
                    buf.putFloat(cofitArray[i][j][k]);  // 写入float值
                }
            }
        }
        return buf.position();  // 返回当前位置
    }
    
    // ==================== 四维float数组序列化 ====================
    
    /**
     * 将四维float数组写入ByteBuffer
     */
    protected int setFloatCoef4(float [][][][]cofitArray, ByteBuffer buf, int idx){
        buf.position(idx);  // 设置缓冲区位置
        for(int i=0;i<cofitArray.length;i++) {  // 遍历第一维
            for (int j = 0; j < cofitArray[i].length; j++) {  // 遍历第二维
                for (int k = 0; k < cofitArray[i][j].length; k++) {  // 遍历第三维
                    for(int l=0;l < cofitArray[i][j][k].length;l ++) {  // 遍历第四维
                        buf.putFloat(cofitArray[i][j][k][l]);  // 写入float值
                    }
                }
            }
        }
        return buf.position();  // 返回当前位置
    }
    
    // ==================== 四维short数组序列化 ====================
    
    /**
     * 将四维short数组写入ByteBuffer
     */
    protected int setShortCoef4(short [][][][]cofitArray, ByteBuffer buf, int idx){
        buf.position(idx);  // 设置缓冲区位置
        for(int i=0;i<cofitArray.length;i++) {  // 遍历第一维
            for (int j = 0; j < cofitArray[i].length; j++) {  // 遍历第二维
                for (int k = 0; k < cofitArray[i][j].length; k++) {  // 遍历第三维
                    for(int l=0;l < cofitArray[i][j][k].length;l ++) {  // 遍历第四维
                        buf.putShort(cofitArray[i][j][k][l]);  // 写入short值
                    }
                }
            }
        }
        return buf.position();  // 返回当前位置
    }
    
    // ==================== 一维byte数组序列化 ====================
    
    /**
     * 将一维byte数组写入ByteBuffer
     */
    protected int setByteCoef1(byte []cofitArray, ByteBuffer buf, int idx){
        buf.position(idx);  // 设置缓冲区位置
        for(int i=0;i<cofitArray.length;i++) {  // 遍历数组
            buf.put(cofitArray[i]);  // 写入byte值
        }
        return buf.position();  // 返回当前位置
    }
    
    // ==================== 二维byte数组序列化 ====================
    
    /**
     * 将二维byte数组写入ByteBuffer
     */
    protected int setByteCoef2(byte [][]cofitArray, ByteBuffer buf, int idx){
        buf.position(idx);  // 设置缓冲区位置
        for(int i=0;i<cofitArray.length;i++) {  // 遍历第一维
            for (int j = 0; j < cofitArray[i].length; j++) {  // 遍历第二维
                buf.put(cofitArray[i][j]);  // 写入byte值
            }
        }
        return buf.position();  // 返回当前位置
    }
    
    // ==================== 一维int数组序列化 ====================
    
    /**
     * 将一维int数组写入ByteBuffer
     */
    protected int setIntCoef1(int [] cofitArray,ByteBuffer buf,int idx){
        buf.position(idx);  // 设置缓冲区位置
        for(int i=0;i<cofitArray.length;i++){  // 遍历数组
            buf.putInt(cofitArray[i]);  // 写入int值
        }
        return buf.position();  // 返回当前位置
    }

    // ==================== 三维int数组序列化 ====================
    
    /**
     * 将三维int数组写入ByteBuffer
     */
    protected int setIntCoef3(int [][][]cofitArray, ByteBuffer buf, int idx){
        buf.position(idx);  // 设置缓冲区位置
        for(int i=0;i<cofitArray.length;i++) {  // 遍历第一维
            for (int j = 0; j < cofitArray[i].length; j++) {  // 遍历第二维
                for (int k = 0; k < cofitArray[i][j].length; k++) {  // 遍历第三维
                    buf.putInt(cofitArray[i][j][k]);  // 写入int值
                }
            }
        }
        return buf.position();  // 返回当前位置
    }
    
    // ==================== 三维int数组反序列化 ====================
    
    /**
     * 从ByteBuffer读取三维int数组
     */
    protected int getIntCoef3(int [][][]cofitArray, ByteBuffer buf, int idx){
        buf.position(idx);  // 设置缓冲区位置
        for(int i=0;i<cofitArray.length;i++) {  // 遍历第一维
            for (int j = 0; j < cofitArray[i].length; j++) {  // 遍历第二维
                for (int k = 0; k < cofitArray[i][j].length; k++) {  // 遍历第三维
                    cofitArray[i][j][k] = buf.getInt();  // 读取int值
                }
            }
        }
        return buf.position();  // 返回当前位置
    }
    
    // ==================== 一维int数组反序列化 ====================
    
    /**
     * 从ByteBuffer读取一维int数组
     */
    protected int getIntCoef1(int []cofitArray, ByteBuffer buf, int idx){
        buf.position(idx);  // 设置缓冲区位置
        for(int i=0;i<cofitArray.length;i++) {  // 遍历数组
            cofitArray[i] = buf.getInt();  // 读取int值
        }
        return buf.position();  // 返回当前位置
    }

    // ==================== 字符串序列化 ====================
    
    /**
     * 将字符串写入ByteBuffer
     * 
     * <p>格式：魔数(4字节) + 长度(4字节) + 字符串数据
     * 
     * @param str 要写入的字符串
     * @param buf 字节缓冲区
     * @param idx 起始索引
     * @return 下一个可用索引
     */
    protected int setString(String str,ByteBuffer buf,int idx){
        buf.position(idx);  // 设置缓冲区位置
        byte[] bytes = str.getBytes(StandardCharsets.US_ASCII);  // 转换为ASCII字节数组
        if(bytes != null) {
            buf.putInt(0x20250617);  // 写入魔数（标识字符串数据）
            buf.putInt(bytes.length);  // 写入字符串长度
            buf.put(bytes);  // 写入字符串数据
        }
        return buf.position();  // 返回当前位置
    }
    
    /**
     * 从ByteBuffer读取字符串
     * 
     * @param sb 用于存储读取的字符串
     * @param buf 字节缓冲区
     * @param idx 起始索引
     * @return 下一个可用索引
     */
    protected int getString(StringBuilder sb,ByteBuffer buf,int idx){
        buf.position(idx);  // 设置缓冲区位置
        if(0x20250617 == buf.getInt()) {  // 验证魔数
            int l = buf.getInt();  // 读取字符串长度
            if (l > 0 && l < 128) {  // 检查长度是否有效
                byte[] bytes = new byte[l];  // 分配缓冲区
                buf.get(bytes);  // 读取字符串数据
                sb.append(new String(bytes, StandardCharsets.US_ASCII));  // 转换为字符串并追加
            }
        }
        return buf.position();  // 返回当前位置
    }


    // ==================== 比例档位常量 ====================
    
    /** 比例档位1 */
    public static final int RATIO_DANG_1 = 0;  // 档位索引0
    
    /** 比例档位2 */
    public static final int RATIO_DANG_2 = RATIO_DANG_1 + 1;  // 档位索引1
    
    /** 比例档位3 */
    public static final int RATIO_DANG_3 = RATIO_DANG_2 + 1;  // 档位索引2
    
    /** 比例档位4 */
    public static final int RATIO_DANG_4 = RATIO_DANG_3 + 1;  // 档位索引3


    /**
     * 获取当前时间字符串
     * 
     * <p>格式：yyyy-MM-dd HH:mm:ss
     * 
     * @return 格式化的时间字符串
     */
    protected String getNowTime(){
        Date now = new Date();  // 获取当前日期
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  // 创建日期格式化器
        return sdf.format(now);  // 返回格式化的时间字符串
    }


    // ==================== 抽象方法：由子类实现 ====================
    
    /**
     * 设置通道默认校准系数（抽象方法）
     * 
     * @param ch 通道索引
     * @param dang 档位索引
     * @param resistanceType 电阻类型
     */
    public abstract void defaultVal_coefChannel(int ch,int dang,int resistanceType);

    /**
     * 计算通道校准系数（抽象方法）
     * 
     * @param chIdx 通道索引
     * @param scaleVal 档位值
     * @param idx 系数索引
     * @return 计算出的校准系数
     */
    public abstract double calc_coefChannel(int chIdx,double scaleVal,int idx);

    /**
     * 计算PGA增益（抽象方法）
     * 
     * @param cnt 通道数量
     * @param chIdx 通道索引
     * @param resistanceType 电阻类型
     * @param scaleVal 档位值
     * @param result 结果数组
     */
    public abstract void calc_pga_gain(int cnt,int chIdx,int resistanceType,double scaleVal,int []result);

    /**
     * 计算PGA满量程增益（抽象方法）
     * 
     * @param chIdx 通道索引
     * @param scaleVal 档位值
     * @param result 结果数组
     */
    public abstract void calc_pga_fs_gain(int chIdx,double scaleVal,int []result);

    /**
     * 获取通道校准系数（抽象方法）
     * 
     * @param chIdx 通道索引
     * @param dang 档位索引
     * @param idx 系数索引
     * @return 校准系数值
     */
    public abstract float getChannelCoef(int chIdx,int dang,int idx);
    
    /**
     * 设置通道校准系数（抽象方法）
     * 
     * @param chIdx 通道索引
     * @param dang 档位索引
     * @param idx 系数索引
     * @param val 校准系数值
     */
    public abstract void setChannelCoef(int chIdx,int dang,int idx,float val);

    /**
     * 设置通道零点校准值（抽象方法）
     * 
     * @param chIdx 通道索引
     * @param dwIdx 档位索引
     * @param pga PGA值
     * @param val 零点校准值
     */
    public abstract void setChannelZero(int chIdx,int dwIdx,int pga,float val);

    /**
     * 获取通道零点校准值（抽象方法）
     * 
     * @param chIdx 通道索引
     * @param dwIdx 档位索引
     * @param pga PGA值
     * @return 零点校准值
     */
    public abstract float getChannelZero(int chIdx,int dwIdx,int pga);

    /**
     * 获取通道电容高值（抽象方法）
     * 
     * @param chIdx 通道索引
     * @param dang 档位索引
     * @return 电容高值
     */
    public abstract int getChCapacitanceHigh(int chIdx,int dang);

    /**
     * 设置通道电容高值（抽象方法）
     * 
     * @param chIdx 通道索引
     * @param dang 档位索引
     * @param val 电容高值
     */
    public abstract void setChCapacitanceHigh(int chIdx,int dang ,int val);
    
    /**
     * 设置AD偏移值（抽象方法）
     * 
     * @param chIdx 通道索引
     * @param chMode 通道模式
     * @param adIdx AD索引
     * @param val 偏移值
     */
    public abstract void setAdOffset(int chIdx,int chMode,int adIdx,int val);
    
    /**
     * 获取AD偏移值（抽象方法）
     * 
     * @param chIdx 通道索引
     * @param chMode 通道模式
     * @param adIdx AD索引
     * @return 偏移值
     */
    public abstract int getAdOffset(int chIdx,int chMode,int adIdx);
    
    /**
     * 设置通道增益值（抽象方法）
     * 
     * @param chIdx 通道索引
     * @param resistanceType 电阻类型
     * @param vIdx 电压索引
     * @param chMode 通道模式
     * @param adIdx AD索引
     * @param val 增益值
     */
    public abstract void setChGain(int chIdx,int resistanceType,int vIdx,int chMode,int adIdx,int val);
    
    /**
     * 获取通道增益值（抽象方法）
     * 
     * @param chIdx 通道索引
     * @param resistanceType 电阻类型
     * @param vIdx 电压索引
     * @param chMode 通道模式
     * @param adIdx AD索引
     * @return 增益值
     */
    public abstract int getChGain(int chIdx,int resistanceType,int vIdx,int chMode,int adIdx);

    /**
     * 获取垂直量程（抽象方法）
     * 
     * @param resistanceType 电阻类型
     * @param dang 档位索引
     * @return 垂直量程值
     */
    public abstract double getVerticalRange(int resistanceType,int dang);
    
    /**
     * 将比例索引转换为档位索引（抽象方法）
     * 
     * @param resistanceType 电阻类型
     * @param idx 比例索引
     * @return 档位索引
     */
    public abstract int getRatioIdx2Dang(int resistanceType,int idx);
    
    /**
     * 根据电压获取比例索引（抽象方法）
     * 
     * @param resistanceType 电阻类型
     * @param v 电压值
     * @return 比例索引
     */
    public abstract int getRatioIdx(int resistanceType,double v);
    
    /**
     * 获取默认通道系数（扩展版本）（抽象方法）
     * 
     * @param chIdx 通道索引
     * @param dangwei 档位索引
     * @param pgaVal PGA值
     * @return 默认系数值
     */
    public abstract double vol_ChannelCoef_defaultEx(int chIdx,int dangwei,int pgaVal);
    
    /**
     * 获取默认通道系数（抽象方法）
     * 
     * @param resistanceType 电阻类型
     * @param dang 档位索引
     * @return 默认系数值
     */
    public abstract double vol_ChannelCoef_default(int resistanceType,int dang);
    
    /**
     * 计算增益（抽象方法）
     * 
     * @param flag 标志位
     */
    public abstract void calcGain(int flag);
    
    /**
     * 计算PGA步进（抽象方法）
     * 
     * @param flag 标志位
     */
    public abstract void calcPgaSetp(int flag);
    
    /**
     * 获取校准时间
     * 
     * @return 校准时间字符串
     */
    public String getCabteTime(){
        return cabteTime;  // 返回校准时间
    }
    
    /** 校准时间字符串 */
    protected String cabteTime="";  // 存储最近一次校准的时间

    /** 校准项目列表 */
    protected List<String> calibrateItems = new ArrayList<>();  // 存储所有校准项目名称
    
    /**
     * 添加所有校准项目
     * 
     * @param list 校准项目列表
     */
    protected void addItemAll(List<String> list){
        calibrateItems.addAll(list);  // 添加所有项目到列表
    }
    
    /**
     * 获取校准项目列表
     * 
     * @return 校准项目名称列表
     */
    @Override
    public List<String> getCalibrationItems(){
        return calibrateItems;  // 返回校准项目列表
    }
}
