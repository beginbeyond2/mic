package com.micsig.tbook.scope.fpga;  // 定义包名：示波器FPGA寄存器管理模块

import android.util.Log;  // 导入Android日志类，用于调试输出

import com.micsig.base.Logger;  // 导入日志类，用于日志记录
import com.micsig.tbook.scope.Calibrate.HwConfig;  // 导入硬件配置类，用于获取硬件参数
import com.micsig.tbook.scope.Trigger.TriggerAction;  // 导入触发动作类，用于触发控制

import java.nio.ShortBuffer;  // 导入ShortBuffer类，用于操作short类型缓冲区

/**
 * FPGA点阵寄存器类 - 用于配置波形缩放和显示的点阵映射表
 *
 * <p><b>模块定位：</b>
 * <ul>
 *   <li>所属模块：scope.fpga（示波器FPGA寄存器管理模块）</li>
 *   <li>架构层级：硬件抽象层 - FPGA寄存器配置</li>
 *   <li>设计模式：继承模式（继承FPGAReg基类）</li>
 *   <li>职责类型：波形缩放配置管理器</li>
 * </ul>
 *
 * <p><b>核心职责：</b>
 * <ul>
 *   <li>配置波形放大/缩小的点阵映射表</li>
 *   <li>支持缩略视图和主视图的显示更新</li>
 *   <li>计算点阵映射参数（缩放系数和偏移）</li>
 *   <li>管理多通道的点阵配置缓存</li>
 *   <li>封装FPGA寄存器的位操作细节</li>
 * </ul>
 *
 * <p><b>设计目的：</b>
 * <ul>
 *   <li>提供波形缩放功能的硬件配置接口</li>
 *   <li>支持实时波形缩放和位置调整</li>
 *   <li>优化点阵计算性能，避免重复计算</li>
 *   <li>支持多通道独立缩放配置</li>
 * </ul>
 *
 * <p><b>寄存器结构：</b>
 * <pre>
 * FPGA_DOT_MATRIX寄存器（2052字节）
 * │
 * ├── 字节0-3：控制参数（32位）
 * │     ├── 位0：更新视图标志（UpDisSuolue）
 * │     │     └── 1=更新缩略视图，0=更新主视图
 * │     │
 * │     ├── 位1：缩放方向标志（Suoxiao）
 * │     │     └── 0=放大，1=缩小
 * │     │
 * │     ├── 位4-7：通道选择（Channel）
 * │     │     └── 通道位掩码（1<<channel）
 * │     │
 * │     └── 位8-31：保留位
 * │
 * └── 字节4-2051：点阵映射表（2048字节，1024个short值）
 *       ├── 放大模式：1024个映射点
 *       │     └── 将原始AD值映射到屏幕显示值
 *       │
 *       └── 缩小模式：根据AD最大值确定表长度
 *             └── 将屏幕显示值映射到原始AD值
 * </pre>
 *
 * <p><b>点阵映射原理：</b>
 * <ul>
 *   <li>点阵映射表用于实现波形的缩放功能</li>
 *   <li>放大模式：将原始AD值映射到更大的屏幕显示范围</li>
 *   <li>缩小模式：将原始AD值映射到更小的屏幕显示范围</li>
 *   <li>映射表通过数学公式计算生成</li>
 *   <li>超出范围的值映射到特殊值（1<<15）</li>
 * </ul>
 *
 * <p><b>点阵映射公式：</b>
 * <pre>
 * 放大模式公式：
 *   a = 16 / (X0 * X1 * K)
 *   b = 16 * (AD_MAX/2 + 1 + Z/K - Y0/(X0*K) - Y1/(X0*X1*K))
 *   映射值 = 输入值 * a + b
 *
 * 缩小模式公式：
 *   a = 16 * X0 * X1 * K
 *   b = 16 * (512 + Y0*X1 + Y1 - Z*X0*X1)
 *   映射值 = 输入值 * a + b
 *
 * 参数说明：
 *   X0：静态时档位调节引起的缩放系数
 *   X1：放大镜引起的衰减系数
 *   Y0：现在通道位置偏移
 *   Y1：放大镜引起的零点偏移
 *   Z：通道原始位置
 *   K：AD与屏幕对应关系系数
 * </pre>
 *
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>波形缩放：用户调整波形缩放比例</li>
 *   <li>位置调整：用户调整波形垂直位置</li>
 *   <li>档位切换：垂直档位切换时重新计算点阵</li>
 *   <li>缩略视图：缩略视图显示时更新点阵</li>
 *   <li>放大镜功能：局部放大显示时计算点阵</li>
 * </ul>
 *
 * <p><b>依赖关系：</b>
 * <ul>
 *   <li>继承：FPGAReg（FPGA寄存器基类）</li>
 *   <li>依赖：HwConfig（硬件配置类，提供AD最大值）</li>
 *   <li>依赖：Channel（通道类，提供通道参数）</li>
 *   <li>依赖：FPGACommand（FPGA命令处理类）</li>
 * </ul>
 *
 * <p><b>性能优化：</b>
 * <ul>
 *   <li>使用缓存机制避免重复计算点阵表</li>
 *   <li>通过参数变化检测判断是否需要重新计算</li>
 *   <li>使用ShortBuffer提高数据操作效率</li>
 *   <li>支持多通道独立缓存，减少计算次数</li>
 * </ul>
 *
 * <p><b>使用示例：</b>
 * <pre>
 * // 创建点阵寄存器实例
 * FPGAReg_DOT_MATRIX reg = new FPGAReg_DOT_MATRIX();
 *
 * // 配置放大模式（通道1）
 * reg.setUpDisSuolue(0);  // 更新主视图
 * reg.setSuoxiao(0);      // 放大模式
 * reg.setChannel(1);      // 通道1
 *
 * // 计算点阵映射表
 * MatrixParam param = new MatrixParam();
 * param.x0 = 1.0;  // 档位缩放系数
 * param.x1 = 1.0;  // 放大镜衰减系数
 * param.y0 = 0;    // 通道位置偏移
 * param.y1 = 0;    // 放大镜零点偏移
 * param.z = 0;     // 通道原始位置
 * param.k = 1.0;   // AD与屏幕对应系数
 *
 * int len = reg.dotMatrixCal(param, sourceTop, sourceBom, objTop, objBom);
 *
 * // 发送寄存器配置到FPGA
 * fpgaCommand.sendCmd(reg, len);
 * </pre>
 *
 * @author zhuzh
 * @version 1.0
 * @since 2018-3-23
 * @see FPGAReg FPGA寄存器基类
 * @see HwConfig 硬件配置类
 * @see MatrixParam 点阵参数类
 */
public class FPGAReg_DOT_MATRIX extends FPGAReg{  // 继承FPGAReg基类，复用寄存器操作方法
    
    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量
    // ═══════════════════════════════════════════════════════════════════════════════
    
    /**
     * Short类型缓冲区
     * 用于操作点阵映射表数据（short类型）
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>将ByteBuffer转换为ShortBuffer，方便操作short类型数据</li>
     *   <li>点阵映射表中的每个值都是short类型（16位）</li>
     *   <li>通过put方法设置映射表的值</li>
     *   <li>提高数据操作效率，避免类型转换</li>
     * </ul>
     */
    private ShortBuffer shortBuffer;  // 成员变量：Short类型缓冲区，用于操作点阵映射表
    
    /**
     * 点阵参数对象
     * 用于存储当前点阵配置参数
     *
     * <p><b>参数说明：</b>
     * <ul>
     *   <li>x0：静态时档位调节引起的缩放系数</li>
     *   <li>x1：放大镜引起的衰减系数</li>
     *   <li>y0：现在通道位置偏移</li>
     *   <li>y1：放大镜引起的零点偏移</li>
     *   <li>z：通道原始位置</li>
     *   <li>k：AD与屏幕对应关系系数</li>
     *   <li>bChange：参数变化标志</li>
     * </ul>
     */
    public MatrixParam matrixParam;  // 成员变量：点阵参数对象，存储当前配置参数
    
    /**
     * 硬件配置实例
     * 用于获取硬件相关参数（如AD最大值）
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>获取AD转换器的最大值（AD_MAX_VAL）</li>
     *   <li>用于计算点阵映射表的长度和范围</li>
     *   <li>单例模式，全局唯一实例</li>
     * </ul>
     */
    private HwConfig hwConfig;  // 成员变量：硬件配置实例，获取硬件参数
    
    // ═══════════════════════════════════════════════════════════════════════════════
    // 构造方法
    // ═══════════════════════════════════════════════════════════════════════════════
    
    /**
     * 构造函数 - 初始化点阵寄存器
     *
     * <p><b>初始化参数：</b>
     * <ul>
     *   <li>寄存器地址：FPGA_DOT_MATRIX（点阵配置寄存器）</li>
     *   <li>寄存器大小：2052字节（4字节控制参数 + 2048字节映射表）</li>
     * </ul>
     *
     * <p><b>初始化操作：</b>
     * <ul>
     *   <li>调用父类构造函数，设置寄存器地址和大小</li>
     *   <li>创建ShortBuffer，用于操作映射表数据</li>
     *   <li>创建MatrixParam对象，存储点阵参数</li>
     *   <li>获取HwConfig实例，用于获取硬件参数</li>
     * </ul>
     *
     * <p><b>寄存器大小说明：</b>
     * <ul>
     *   <li>4字节：控制参数（视图更新标志、缩放方向、通道选择）</li>
     *   <li>2048字节：点阵映射表（1024个short值，每个2字节）</li>
     *   <li>总计：2052字节</li>
     * </ul>
     */
    public FPGAReg_DOT_MATRIX(){  // 构造方法：初始化点阵寄存器
        // 调用父类构造函数，设置寄存器地址和大小
        // FPGA_DOT_MATRIX：点阵配置寄存器地址
        // 2052：寄存器大小（4字节控制参数 + 2048字节映射表）
        super(FPGAReg.FPGA_DOT_MATRIX,2052);  // 调用父类构造函数，设置寄存器ID为FPGA_DOT_MATRIX，大小2052字节
        
        // 将ByteBuffer转换为ShortBuffer，方便操作short类型数据
        // byteBuffer是父类FPGAReg中定义的字节缓冲区
        // asShortBuffer()方法将字节视图转换为short视图
        shortBuffer = byteBuffer.asShortBuffer();  // 创建ShortBuffer，用于操作点阵映射表数据
        
        // 创建点阵参数对象，用于存储当前配置参数
        matrixParam = new MatrixParam();  // 创建MatrixParam对象，初始化点阵参数
        
        // 获取硬件配置实例，用于获取硬件参数（如AD最大值）
        hwConfig = HwConfig.getInstance();  // 获取HwConfig单例实例
    }  // 构造方法结束
    
    // ═══════════════════════════════════════════════════════════════════════════════
    // 公有方法 - 控制参数设置
    // ═══════════════════════════════════════════════════════════════════════════════
    
    /**
     * 设置更新视图标志
     * 设置是更新缩略视图还是主视图
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>控制FPGA更新哪个视图的点阵映射表</li>
     *   <li>1：更新缩略视图（Thumbnail View）</li>
     *   <li>0：更新主视图（Main View）</li>
     *   <li>寄存器位：位0（最低位）</li>
     * </ul>
     *
     * <p><b>应用场景：</b>
     * <ul>
     *   <li>缩略视图缩放：更新缩略视图的点阵映射表</li>
     *   <li>主视图缩放：更新主视图的点阵映射表</li>
     *   <li>视图切换：切换显示视图时更新对应视图</li>
     * </ul>
     *
     * @param val 更新视图标志值（1=缩略视图，0=主视图）
     */
    //1: 更新缩略视图；0：更新主视图
    public void setUpDisSuolue(int val){  // 公有方法：设置更新视图标志
        // 调用父类setVal方法，设置寄存器值
        // 参数1：字索引=0（第一个字）
        // 参数2：起始位=0（位0，最低位）
        // 参数3：位宽度=1（1位）
        // 参数4：更新视图标志值
        setVal(0,0,1,val);  // 设置位0的值，宽度为1位，值为val（更新视图标志）
    }  // 方法结束
    
    /**
     * 设置缩放方向标志
     * 设置是放大还是缩小模式
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>控制FPGA使用放大还是缩小模式的点阵映射表</li>
     *   <li>0：放大模式（Zoom In）</li>
     *   <li>1：缩小模式（Zoom Out）</li>
     *   <li>寄存器位：位1</li>
     * </ul>
     *
     * <p><b>缩放模式说明：</b>
     * <ul>
     *   <li>放大模式：将原始AD值映射到更大的屏幕显示范围</li>
     *   <li>缩小模式：将原始AD值映射到更小的屏幕显示范围</li>
     *   <li>放大模式使用1024个映射点</li>
     *   <li>缩小模式使用AD_MAX_VAL+1个映射点</li>
     * </ul>
     *
     * @param val 缩放方向标志值（0=放大，1=缩小）
     */
    //0：放大；1：缩小；
    public void setSuoxiao(int val){  // 公有方法：设置缩放方向标志
        // 调用父类setVal方法，设置寄存器值
        // 参数1：字索引=0（第一个字）
        // 参数2：起始位=1（位1）
        // 参数3：位宽度=1（1位）
        // 参数4：缩放方向标志值
        setVal(0,1,1,val);  // 设置位1的值，宽度为1位，值为val（缩放方向标志）
    }  // 方法结束
    
    /**
     * 设置通道选择
     * 设置要更新点阵映射表的通道
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>控制FPGA更新哪个通道的点阵映射表</li>
     *   <li>通道值：1<<channel（通道位掩码）</li>
     *   <li>寄存器位：位4-7（4位）</li>
     *   <li>支持多通道同时更新（位掩码组合）</li>
     * </ul>
     *
     * <p><b>通道值说明：</b>
     * <ul>
     *   <li>通道1：1<<0 = 1（二进制0001）</li>
     *   <li>通道2：1<<1 = 2（二进制0010）</li>
     *   <li>通道3：1<<2 = 4（二进制0100）</li>
     *   <li>通道4：1<<3 = 8（二进制1000）</li>
     *   <li>多通道：位掩码组合（如通道1+2 = 3）</li>
     * </ul>
     *
     * @param val 通道选择值（通道位掩码，1<<channel）
     */
    //=1<<channel;
    public void setChannel(int val){  // 公有方法：设置通道选择
        // 调用父类setVal方法，设置寄存器值
        // 参数1：字索引=0（第一个字）
        // 参数2：起始位=4（位4）
        // 参数3：位宽度=4（4位）
        // 参数4：通道选择值
        setVal(0,4,4,val);  // 设置位4-7的值，宽度为4位，值为val（通道选择）
    }  // 方法结束
    
    // ═══════════════════════════════════════════════════════════════════════════════
    // 私有方法 - 辅助计算
    // ═══════════════════════════════════════════════════════════════════════════════
    
    /**
     * 获取点阵映射表的偏移量
     * 计算点阵映射表在寄存器中的起始位置
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>点阵映射表位于控制参数之后</li>
     *   <li>控制参数占用FPGA_REG_HEADER_LEN + 4字节</li>
     *   <li>偏移量以short为单位（除以2）</li>
     *   <li>用于ShortBuffer的索引计算</li>
     * </ul>
     *
     * <p><b>偏移量计算：</b>
     * <pre>
     * 偏移量 = (FPGA_REG_HEADER_LEN + 4) / 2
     * FPGA_REG_HEADER_LEN：FPGA寄存器头长度
     * 4：控制参数长度（4字节）
     * /2：转换为short单位（每个short占2字节）
     * </pre>
     *
     * @return 点阵映射表的偏移量（以short为单位）
     */
    private int getOffset(){  // 私有方法：获取点阵映射表的偏移量
        // 计算点阵映射表的起始位置
        // FPGA_REG_HEADER_LEN：FPGA寄存器头长度
        // 4：控制参数长度（4字节）
        // /2：转换为short单位（每个short占2字节）
        return (FPGAReg.FPGA_REG_HEADER_LEN + 4)/2;  // 返回偏移量（short单位）
    }  // 方法结束
    
    /**
     * 计算放大模式的点阵映射表
     * 生成放大模式的点阵映射表数据
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>计算放大模式的点阵映射表</li>
     *   <li>映射表长度：1024个short值</li>
     *   <li>将输入值映射到屏幕显示值</li>
     *   <li>超出范围的值映射到特殊值（1<<15）</li>
     * </ul>
     *
     * <p><b>映射公式：</b>
     * <pre>
     * 映射值 = 输入值 * a + b
     * a = 16 / (X0 * X1 * K)
     * b = 16 * (AD_MAX/2 + 1 + Z/K - Y0/(X0*K) - Y1/(X0*X1*K))
     * </pre>
     *
     * <p><b>参数说明：</b>
     * <ul>
     *   <li>a：缩放系数</li>
     *   <li>b：偏移值</li>
     *   <li>upLimit：上限值（超出范围的上界）</li>
     *   <li>dowLimit：下限值（超出范围的下界）</li>
     * </ul>
     *
     * <p><b>映射表生成：</b>
     * <ul>
     *   <li>输入范围：-512到+511（1024个点）</li>
     *   <li>每个点计算映射值</li>
     *   <li>超出范围的点映射到特殊值（1<<15）</li>
     *   <li>范围内的点映射到计算值</li>
     * </ul>
     *
     * @param a 缩放系数
     * @param b 偏移值
     * @param upLimit 上限值
     * @param dowLimit 下限值
     * @return 映射表长度（字节）
     */
    //计算点阵放大时的表格
    int calTable_fangda(double a, double b, double upLimit,double dowLimit) {  // 私有方法：计算放大模式的点阵映射表
        // 调整上限和下限，增加0.01的容差
        upLimit -= 0.01;  // 上限减去0.01，增加容差
        dowLimit += 0.01;  // 下限加上0.01，增加容差
        
        // 定义映射表长度：1024个点
        int tableLen = 1024;  // 映射表长度：1024个short值
        
        // 获取映射表的起始索引（偏移量）
        int idx = getOffset();  // 获取点阵映射表的偏移量
        
        // 循环计算映射表的每个点
        // i：映射表索引（0到1023）
        // j：输入值（-512到+511）
        for(int i=0,j=-512; i<tableLen; i++,j++)  // 循环：遍历映射表的每个点
        {
            // 计算映射值：输入值 * a + b
            double k=(double)j*a+b;  // 计算映射值：j * a + b
            
            // 判断映射值是否超出范围
            if(k<dowLimit || k>upLimit)  // 条件判断：映射值超出范围
                // 超出范围的值映射到特殊值（1<<15）
                // table[i] =(short) (1<<15); //超出原始值范围的，全映射到这个值
                shortBuffer.put(i+idx,(short)(1<<15));  // 设置映射表值：超出范围，映射到特殊值（1<<15）
            else  // 条件分支：映射值在范围内
                // 范围内的值映射到计算值（四舍五入）
                shortBuffer.put(i+idx,(short)Math.round(k));  // 设置映射表值：范围内，映射到计算值（四舍五入）
                //table[i] =(short) k;
        }  // 循环结束
        
        // 返回映射表长度（字节）：1024 * 2 = 2048字节
        return tableLen*2;  // 返回映射表长度（字节）
    }  // 方法结束
    
    /**
     * 计算缩小模式的点阵映射表
     * 生成缩小模式的点阵映射表数据
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>计算缩小模式的点阵映射表</li>
     *   <li>映射表长度：AD_MAX_VAL+1个short值</li>
     *   <li>将输入值映射到屏幕显示值</li>
     *   <li>超出范围的值映射到特殊值（1<<15）</li>
     * </ul>
     *
     * <p><b>映射公式：</b>
     * <pre>
     * 映射值 = 输入值 * a + b
     * a = 16 * X0 * X1 * K
     * b = 16 * (512 + Y0*X1 + Y1 - Z*X0*X1)
     * </pre>
     *
     * <p><b>参数说明：</b>
     * <ul>
     *   <li>a：缩放系数</li>
     *   <li>b：偏移值</li>
     *   <li>upLimit：上限值（超出范围的上界）</li>
     *   <li>dowLimit：下限值（超出范围的下界）</li>
     * </ul>
     *
     * <p><b>映射表生成：</b>
     * <ul>
     *   <li>输入范围：-AD_MAX/2到+AD_MAX/2</li>
     *   <li>每个点计算映射值</li>
     *   <li>超出范围的点映射到特殊值（1<<15）</li>
     *   <li>范围内的点映射到计算值</li>
     * </ul>
     *
     * @param a 缩放系数
     * @param b 偏移值
     * @param upLimit 上限值
     * @param dowLimit 下限值
     * @return 映射表长度（字节）
     */
    //计算点阵缩小时的表格
    int calTable_suoxiao(double a, double b, double upLimit,double dowLimit) {  // 私有方法：计算缩小模式的点阵映射表
        // 调整上限和下限，增加0.01的容差
        upLimit -= 0.01;  // 上限减去0.01，增加容差
        dowLimit += 0.01;  // 下限加上0.01，增加容差
        
        // 定义映射表长度：AD_MAX_VAL+1个点
        int tableLen = hwConfig.getAdMaxVal()+1;  // 映射表长度：AD最大值+1
        
        // 获取映射表的起始索引（偏移量）
        int idx = getOffset();  // 获取点阵映射表的偏移量
        
        // 循环计算映射表的每个点
        // i：映射表索引（0到tableLen-1）
        // j：输入值（-tableLen/2到+tableLen/2-1）
        for(int i=0,j=-(tableLen/2); i<tableLen; i++,j++)  // 循环：遍历映射表的每个点
        {
            // 计算映射值：输入值 * a + b
            double k=(double)j*a+b;  // 计算映射值：j * a + b
            
            // 判断映射值是否超出范围
            if(k<dowLimit || k>upLimit)  // 条件判断：映射值超出范围
                // 超出范围的值映射到特殊值（1<<15）
                // table[i] = (short)(1<<15); //超出原始值范围的，全映射到这个值
                shortBuffer.put(i+idx,(short)(1<<15));  // 设置映射表值：超出范围，映射到特殊值（1<<15）
            else  // 条件分支：映射值在范围内
                // 范围内的值映射到计算值（四舍五入）
                // table[i] = (short)k;
                shortBuffer.put(i+idx,(short)Math.round(k));  // 设置映射表值：范围内，映射到计算值（四舍五入）
        }  // 循环结束
        
        // 返回映射表长度（字节）：tableLen * 2字节
        return tableLen*2;  // 返回映射表长度（字节）
    }  // 方法结束
    
    /**
     * 计算点阵映射表
     * 根据缩放方向计算点阵映射表
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>根据缩放方向选择放大或缩小模式</li>
     *   <li>计算缩放系数和偏移值</li>
     *   <li>调用对应的计算方法生成映射表</li>
     *   <li>返回映射表长度</li>
     * </ul>
     *
     * <p><b>缩放方向判断：</b>
     * <pre>
     * 判断条件：X0 * X1 * K > 0.99999
     * - 大于0.99999：放大模式
     * - 小于等于0.99999：缩小模式
     * </pre>
     *
     * <p><b>放大模式公式：</b>
     * <pre>
     * a = 16 / (X0 * X1 * K)
     * b = 16 * (AD_MAX/2 + 1 + Z/K - Y0/(X0*K) - Y1/(X0*X1*K))
     * </pre>
     *
     * <p><b>缩小模式公式：</b>
     * <pre>
     * a = 16 * X0 * X1 * K
     * b = 16 * (512 + Y0*X1 + Y1 - Z*X0*X1)
     * </pre>
     *
     * @param parm 点阵参数对象
     * @param upLimit 上限值
     * @param downLimit 下限值
     * @param direct 缩放方向（true=放大，false=缩小）
     * @return 映射表长度（字节）
     */
    //direct=true放大，false缩小
    int dotMatrix(MatrixParam parm, int upLimit,int downLimit,boolean direct)  // 私有方法：计算点阵映射表
    {
        // 计算判断值：X0 * X1 * K
        double judge=parm.x0*parm.x1*parm.k;  // 计算判断值：用于确定缩放方向
        
        // 根据缩放方向选择计算模式
        if(direct)  // 条件判断：放大模式
        {
            // 放大模式计算
            // 公式：a = 16 / (X0 * X1 * K)
            // 公式：b = 16 * (AD_MAX/2 + 1 + Z/K - Y0/(X0*K) - Y1/(X0*X1*K))
            
            // 计算缩放系数a
            //a = (16)/(X0*X1*K)；
            double a = (double)(16.0/judge);  // 计算缩放系数a：16 / judge
            
            // 计算偏移值b
            //b =16*(128+ Z/K- Y0/(X0*K)- Y1/(X0*X1*K))；
            double b = (double)(16.0*((hwConfig.getAdMaxVal()/2 + 1) + parm.z/parm.k- parm.y0/(parm.x0*parm.k) - parm.y1/judge));  // 计算偏移值b
            
            // 调用放大模式计算方法，生成映射表
            return calTable_fangda(a, b, upLimit*16+15, downLimit*16);  // 调用放大模式计算方法，返回映射表长度
        }  // 放大模式结束
        else  // 条件分支：缩小模式
        {
            // 缩小模式计算
            // 公式：a = 16 * X0 * X1 * K
            // 公式：b = 16 * (512 + Y0*X1 + Y1 - Z*X0*X1)
            
            // 计算缩放系数a
            //a = 16*X0*X1*K；
            double a=(double)(16.0*judge);  // 计算缩放系数a：16 * judge
            
            // 计算偏移值b
            //b = 16*(256+Y0*X1+Y1-Z*X0*X1)；
            double b=(double)(16.0*(512+parm.y0*parm.x1+parm.y1-parm.z*parm.x0*parm.x1));  // 计算偏移值b
            
            // 调用缩小模式计算方法，生成映射表
            return calTable_suoxiao(a, b, upLimit*16+15, downLimit*16);  // 调用缩小模式计算方法，返回映射表长度
        }  // 缩小模式结束
    }  // 方法结束
    
    /**
     * 判断两个double值是否相等
     * 用于比较点阵参数是否变化
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>比较两个double值是否相等</li>
     *   <li>使用容差1e-10判断相等</li>
     *   <li>用于检测点阵参数是否变化</li>
     *   <li>避免浮点数精度问题</li>
     * </ul>
     *
     * <p><b>判断逻辑：</b>
     * <pre>
     * if(Math.abs(a-b) < 1e-10)
     *     return true;  // 相等
     * else
     *     return false;  // 不相等
     * </pre>
     *
     * @param a 第一个double值
     * @param b 第二个double值
     * @return 是否相等（true=相等，false=不相等）
     */
    boolean equ_double(double a, double b)  // 私有方法：判断两个double值是否相等
    {
        // 判断两个值的绝对差是否小于容差1e-10
        if(Math.abs(a-b) < 1e-10)  // 条件判断：绝对差小于容差
            return true;  // 返回true：两个值相等
        else  // 条件分支：绝对差大于容差
            return false;  // 返回false：两个值不相等
    }  // 方法结束
    
    /**
     * 通道索引查找表
     * 将通道位掩码转换为索引值
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>将通道位掩码转换为缓存数组索引</li>
     *   <li>通道1（位掩码1）：索引0</li>
     *   <li>通道2（位掩码2）：索引1</li>
     *   <li>通道3（位掩码4）：索引2</li>
     *   <li>通道4（位掩码8）：索引3</li>
     * </ul>
     *
     * <p><b>映射关系：</b>
     * <pre>
     * 通道位掩码 -> 缓存索引
     * 1 (通道1) -> 0
     * 2 (通道2) -> 1
     * 4 (通道3) -> 2
     * 8 (通道4) -> 3
     * </pre>
     *
     * @param ch 通道位掩码（1、2、4、8）
     * @return 缓存索引（0、1、2、3）
     */
    int table_ch_serch(int ch)  // 私有方法：通道索引查找
    {
        // 根据通道位掩码返回对应的缓存索引
        switch(ch){  // switch语句：根据通道位掩码选择索引
            case 1:  // case分支：通道1（位掩码1）
                return 0;  // 返回索引0
            case 2:  // case分支：通道2（位掩码2）
                return 1;  // 返回索引1
            case 4:  // case分支：通道3（位掩码4）
                return 2;  // 返回索引2
            case 8:  // case分支：通道4（位掩码8）
            default:  // default分支：默认情况
                return 3;  // 返回索引3
        }  // switch语句结束
    }  // 方法结束
    
    // ═══════════════════════════════════════════════════════════════════════════════
    // 成员变量 - 参数缓存
    // ═══════════════════════════════════════════════════════════════════════════════
    
    /**
     * 点阵参数缓存数组
     * 用于缓存各通道的点阵参数，避免重复计算
     *
     * <p><b>缓存结构：</b>
     * <ul>
     *   <li>二维数组：[8][2]</li>
     *   <li>第一维：通道索引（最多8个通道）</li>
     *   <li>第二维：视图选择（0=主视图，1=缩略视图）</li>
     *   <li>每个元素：MatrixParam对象</li>
     * </ul>
     *
     * <p><b>缓存目的：</b>
     * <ul>
     *   <li>避免重复计算点阵映射表</li>
     *   <li>通过参数变化检测判断是否需要重新计算</li>
     *   <li>提高性能，减少FPGA通信次数</li>
     *   <li>支持多通道独立缓存</li>
     * </ul>
     */
    MatrixParam [][] parmBakStore = {  // 成员变量：点阵参数缓存数组
            {new MatrixParam(),new MatrixParam()},  // 通道0缓存：主视图和缩略视图
            {new MatrixParam(),new MatrixParam()},  // 通道1缓存：主视图和缩略视图
            {new MatrixParam(),new MatrixParam()},  // 通道2缓存：主视图和缩略视图
            {new MatrixParam(),new MatrixParam()},  // 通道3缓存：主视图和缩略视图
            {new MatrixParam(),new MatrixParam()},  // 通道4缓存：主视图和缩略视图
            {new MatrixParam(),new MatrixParam()},  // 通道5缓存：主视图和缩略视图
            {new MatrixParam(),new MatrixParam()},  // 通道6缓存：主视图和缩略视图
            {new MatrixParam(),new MatrixParam()}   // 通道7缓存：主视图和缩略视图
    };  // 缓存数组初始化结束
    
    // ═══════════════════════════════════════════════════════════════════════════════
    // 公有方法 - 缓存管理
    // ═══════════════════════════════════════════════════════════════════════════════
    
    /**
     * 重置所有缓存的变化标志
     * 强制所有缓存参数标记为已变化
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>将所有缓存的bChange标志设置为true</li>
     *   <li>强制重新计算所有通道的点阵映射表</li>
     *   <li>用于初始化或强制更新场景</li>
     * </ul>
     *
     * <p><b>应用场景：</b>
     * <ul>
     *   <li>系统初始化：初始化时重置所有缓存</li>
     *   <li>强制更新：需要强制更新所有点阵时</li>
     *   <li>配置重置：重置示波器配置时</li>
     * </ul>
     */
    public void resetBak(){  // 公有方法：重置所有缓存的变化标志
        // 循环遍历所有通道的缓存
        for(int i=0;i<parmBakStore.length;i++){  // 循环：遍历通道索引
            // 循环遍历每个通道的两个视图缓存
            for(int j=0;j<parmBakStore[i].length;j++){  // 循环：遍历视图索引
                // 将变化标志设置为true，强制重新计算
                parmBakStore[i][j].bChange = true ;  // 设置变化标志为true
            }  // 内层循环结束
        }  // 外层循环结束
    }  // 方法结束
    
    /**
     * 标记指定通道的点阵参数已变化
     * 强制指定通道重新计算点阵映射表
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>将指定通道的两个视图缓存的变化标志设置为true</li>
     *   <li>强制重新计算指定通道的点阵映射表</li>
     *   <li>用于通道配置变化场景</li>
     * </ul>
     *
     * <p><b>应用场景：</b>
     * <ul>
     *   <li>通道参数变化：通道垂直档位或位置变化时</li>
     *   <li>通道切换：切换通道显示时</li>
     *   <li>强制更新：需要强制更新指定通道时</li>
     * </ul>
     *
     * @param chIdx 通道索引
     */
    public void dotMatrixChange(int chIdx){  // 公有方法：标记指定通道的点阵参数已变化
        // 将指定通道的主视图缓存变化标志设置为true
        parmBakStore[chIdx][0].bChange = true;  // 设置主视图缓存变化标志为true
        // 将指定通道的缩略视图缓存变化标志设置为true
        parmBakStore[chIdx][1].bChange = true;  // 设置缩略视图缓存变化标志为true
    }  // 方法结束
    
    /**
     * 判断点阵参数是否变化
     * 检查点阵参数是否需要重新计算
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>比较当前参数与缓存参数</li>
     *   <li>如果参数相同且变化标志为false，返回false（不需要重新计算）</li>
     *   <li>如果参数不同或变化标志为true，返回true（需要重新计算）</li>
     *   <li>更新缓存参数</li>
     * </ul>
     *
     * <p><b>判断逻辑：</b>
     * <pre>
     * 1. 获取缓存参数对象
     * 2. 比较所有参数是否相同
     * 3. 检查变化标志
     * 4. 如果相同且未变化，返回false
     * 5. 如果不同或已变化，更新缓存并返回true
     * </pre>
     *
     * <p><b>参数比较：</b>
     * <ul>
     *   <li>x0：静态时档位调节引起的缩放系数</li>
     *   <li>x1：放大镜引起的衰减系数</li>
     *   <li>y0：现在通道位置偏移</li>
     *   <li>y1：放大镜引起的零点偏移</li>
     *   <li>z：通道原始位置</li>
     *   <li>k：AD与屏幕对应关系系数</li>
     * </ul>
     *
     * @param parm 当前点阵参数对象
     * @param sel 视图选择（0=主视图，1=缩略视图）
     * @param sendCh 发送通道索引
     * @return 是否需要重新计算（true=需要，false=不需要）
     */
    public boolean judgeDotMatrixChange(MatrixParam parm,  int sel, int sendCh)  // 公有方法：判断点阵参数是否变化
    {
        // 备份fpga的4个table的信息
        // 获取缓存参数对象
        MatrixParam parmBak = parmBakStore[sendCh][sel];//parmBakStore[table_ch_serch(sendCh)][sel];  // 获取缓存参数对象
        
        // 比较所有参数是否相同
        if(equ_double(parmBak.x0,parm.x0)         && equ_double(parmBak.x1,parm.x1)  // 比较x0和x1
                && equ_double(parmBak.y0,parm.y0) && equ_double(parmBak.y1,parm.y1)  // 比较y0和y1
                && equ_double(parmBak.z,parm.z)   && equ_double(parmBak.k,parm.k)    // 比较z和k
                && !parmBak.bChange)  // 检查变化标志
        {
            // 参数相同且未变化，不需要重新计算
            return false;  // 返回false：不需要重新计算
        }  // 条件判断结束
        else  // 条件分支：参数不同或已变化
        {
            // 参数不同或已变化，需要重新计算
            // 更新缓存参数
            
            // 将变化标志设置为false，标记已处理
            parmBak.bChange = false;  // 设置变化标志为false
            
            // 更新缓存的所有参数
            parmBak.x0 = parm.x0;  // 更新x0：静态时档位调节引起的缩放系数
            parmBak.x1 = parm.x1;  // 更新x1：放大镜引起的衰减系数
            parmBak.y0 = parm.y0;  // 更新y0：现在通道位置偏移
            parmBak.y1 = parm.y1;  // 更新y1：放大镜引起的零点偏移
            parmBak.z  = parm.z;   // 更新z：通道原始位置
            parmBak.k  = parm.k;   // 更新k：AD与屏幕对应关系系数
            
            // 调试日志（已注释）
//            Logger.d("judgeDotMatrixChange: ch = " + sendCh
//                    + ",sel = " + sel
//                    + ",k = " + parm.k
//                    + ",x0 = " + parm.x0
//                    + ",x1 = " + parm.x1
//                    + ",y0 = " + parm.y0
//                    + ",y1 = " + parm.y1
//                    + ",z = " + parm.z);
            
            // 返回true：需要重新计算
            return true;  // 返回true：需要重新计算
        }  // 条件分支结束
    }  // 方法结束
    
    /**
     * 计算点阵映射表
     * 根据点阵参数和范围计算点阵映射表
     *
     * <p><b>功能说明：</b>
     * <ul>
     *   <li>根据点阵参数判断缩放方向</li>
     *   <li>调用对应的计算方法生成映射表</li>
     *   <li>设置缩放方向标志</li>
     *   <li>返回映射表总长度</li>
     * </ul>
     *
     * <p><b>缩放方向判断：</b>
     * <pre>
     * 判断条件：X0 * X1 * K > 0.99999
     * - 大于0.99999：放大模式，使用sourceTop和sourceBom作为范围
     * - 小于等于0.99999：缩小模式，使用objTop和objBom作为范围
     * </pre>
     *
     * <p><b>返回值说明：</b>
     * <ul>
     *   <li>映射表长度 + 4字节控制参数</li>
     *   <li>总长度用于FPGA数据传输</li>
     * </ul>
     *
     * @param matrixParm 点阵参数对象
     * @param sourceTop 源上限值
     * @param sourceBom 源下限值
     * @param objTop 目标上限值
     * @param objBom 目标下限值
     * @return 映射表总长度（字节）
     */
    public int  dotMatrixCal(MatrixParam matrixParm,  // 公有方法：计算点阵映射表
                      int sourceTop, int sourceBom, int objTop, int objBom)
    {
        // 定义映射表长度变量
        int len;  // 映射表长度
        
        // 计算判断值：X0 * X1 * K
        double judge=matrixParm.x0*matrixParm.x1*matrixParm.k;  // 计算判断值：用于确定缩放方向
        
        // 根据判断值选择缩放模式
        if(judge > 0.99999)  // 条件判断：放大模式
        {
            // 放大模式
            // 调用dotMatrix方法，计算放大模式的映射表
            len = dotMatrix(matrixParm, sourceTop, sourceBom, true);  // 计算放大模式映射表，返回长度
            //sendParm.suoxiao = 0;
            // 设置缩放方向标志为放大（0）
            setSuoxiao(0);  // 设置缩放方向标志为放大（0）
            
        }  // 放大模式结束
        else  // 条件分支：缩小模式
        {
            // 缩小模式
            // 调用dotMatrix方法，计算缩小模式的映射表
            len = dotMatrix(matrixParm, objTop, objBom, false);  // 计算缩小模式映射表，返回长度
            //sendParm.suoxiao = 1;
            // 设置缩放方向标志为缩小（1）
            setSuoxiao(1);  // 设置缩放方向标志为缩小（1）
            
        }  // 缩小模式结束
        
        // 返回映射表总长度：映射表长度 + 4字节控制参数
        return len + 4;  // 返回总长度（映射表长度 + 控制参数4字节）
        //memcpy(buf, &sendParm, 4);//拷贝0xA1命令信息到数据列中
        //Firmware_Ctrl->fpga_seri_ctrl.SendCmdToFpga(FPGA_DOT_MATRIX, buf, len+4);
    }  // 方法结束
    
    // ═══════════════════════════════════════════════════════════════════════════════
    // 内部类 - 点阵参数
    // ═══════════════════════════════════════════════════════════════════════════════
    
    /**
     * 点阵参数类 - 用于存储点阵映射计算所需的参数
     *
     * <p><b>参数说明：</b>
     * <ul>
     *   <li>x0：静态时档位调节引起的缩放系数</li>
     *   <li>x1：放大镜引起的衰减系数</li>
     *   <li>y0：现在通道位置偏移</li>
     *   <li>y1：放大镜引起的零点偏移</li>
     *   <li>z：通道原始位置</li>
     *   <li>k：AD与屏幕对应关系系数</li>
     *   <li>bChange：参数变化标志</li>
     * </ul>
     *
     * <p><b>参数用途：</b>
     * <ul>
     *   <li>用于计算点阵映射表的缩放系数和偏移值</li>
     *   <li>用于判断是否需要重新计算点阵映射表</li>
     *   <li>用于缓存点阵配置参数</li>
     * </ul>
     */
    class MatrixParam{  // 内部类：点阵参数类
        
        /**
         * 参数变化标志
         * 用于判断是否需要重新计算点阵映射表
         *
         * <p><b>标志说明：</b>
         * <ul>
         *   <li>true：参数已变化，需要重新计算</li>
         *   <li>false：参数未变化，不需要重新计算</li>
         * </ul>
         */
        public boolean bChange;  // 成员变量：参数变化标志
        
        /**
         * 静态时档位调节引起的缩放系数
         * 用于计算点阵映射表的缩放比例
         *
         * <p><b>参数说明：</b>
         * <ul>
         *   <li>档位调节会影响波形的显示比例</li>
         *   <li>x0用于调整档位变化引起的缩放</li>
         *   <li>通常与垂直档位相关</li>
         * </ul>
         */
        public double x0;//静态时档位调节引起的缩放
        
        /**
         * 放大镜引起的衰减系数
         * 用于计算放大镜功能的衰减比例
         *
         * <p><b>参数说明：</b>
         * <ul>
         *   <li>放大镜功能会衰减波形显示</li>
         *   <li>x1用于调整放大镜引起的衰减</li>
         *   <li>通常与放大镜倍率相关</li>
         * </ul>
         */
        public double x1;//放大镜引起的衰减
        
        /**
         * 现在通道位置偏移
         * 用于计算点阵映射表的垂直偏移
         *
         * <p><b>参数说明：</b>
         * <ul>
         *   <li>通道位置调整会影响波形的垂直位置</li>
         *   <li>y0用于调整通道位置引起的偏移</li>
         *   <li>通常与垂直位置相关</li>
         * </ul>
         */
        public double y0;//现在通道位置
        
        /**
         * 放大镜引起的零点偏移
         * 用于计算放大镜功能的零点偏移
         *
         * <p><b>参数说明：</b>
         * <ul>
         *   <li>放大镜功能会引起零点偏移</li>
         *   <li>y1用于调整放大镜引起的零点偏移</li>
         *   <li>通常与放大镜位置相关</li>
         * </ul>
         */
        public double y1;//放大镜引起的零点偏移
        
        /**
         * 通道原始位置
         * 用于计算点阵映射表的原始位置偏移
         *
         * <p><b>参数说明：</b>
         * <ul>
         *   <li>通道原始位置是通道的初始位置</li>
         *   <li>z用于调整原始位置引起的偏移</li>
         *   <li>通常与通道初始位置相关</li>
         * </ul>
         */
        public double z;//通道原始位置
        
        /**
         * AD与屏幕对应关系系数
         * 用于计算AD值到屏幕值的转换系数
         *
         * <p><b>参数说明：</b>
         * <ul>
         *   <li>k是AD值与屏幕显示值的转换系数</li>
         *   <li>用于将AD采样值映射到屏幕显示值</li>
         *   <li>通常与屏幕分辨率和AD范围相关</li>
         * </ul>
         */
        public double k;//AD与屏幕对应关系
        
        /**
         * 构造函数 - 初始化点阵参数
         *
         * <p><b>初始化参数：</b>
         * <ul>
         *   <li>bChange：参数变化标志</li>
         *   <li>x1：放大镜衰减系数（初始化为0）</li>
         * </ul>
         *
         * @param bChange 参数变化标志
         */
        public MatrixParam(boolean bChange){  // 构造方法：初始化点阵参数
            // 设置参数变化标志
            this.bChange = bChange;  // 设置变化标志
            // 初始化放大镜衰减系数为0
            x1 = 0;  // 初始化x1为0
        }  // 构造方法结束
        
        /**
         * 默认构造函数 - 初始化点阵参数
         * 默认参数变化标志为true
         *
         * <p><b>初始化参数：</b>
         * <ul>
         *   <li>bChange：true（默认需要重新计算）</li>
         * </ul>
         */
        public MatrixParam(){  // 默认构造方法：初始化点阵参数
            // 调用带参数的构造函数，设置变化标志为true
            this(true);  // 调用构造函数，参数为true
        }  // 构造方法结束
    }  // 内部类结束
}  // FPGAReg_DOT_MATRIX类结束