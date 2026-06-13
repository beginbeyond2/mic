package com.micsig.tbook.tbookscope.top.layout.trigger.serials;

/**
 * Created by yangj on 2017/4/27.
 */

public interface SerialsDetailFlag {
    int UART = 0;
    int LIN = 1;
    int CAN = 2;
    int SPI = 3;
    int I2C = 4;
    int ARINC429 = 5;
    int M1553B = 6;

    int NULL = -1;
    int UART_DATA = 0;
    int UART_0DATA = 1;
    int UART_1DATA = 2;
    int UART_XDATA = 3;
    int LIN_FRAMEID = 4;
    int LIN_IDDATA = 5;
    int CAN_REMOTEID = 6;
    int CAN_DATAID = 7;
    int CAN_RDID = 8;
    int CAN_IDDATA = 9;
    int SPI_DATA = 10;
    int I2C_NOACKINADR = 11;
    int I2C_FRAME1 = 12;
    int I2C_FRAME2 = 13;
    int I2C_ROMDATA = 14;
    int I2C_10WRITEFRAME = 15;
    int ARINC429_LABEL = 16;
    int ARINC429_SDI = 17;
    int ARINC429_DATA = 18;
    int ARINC429_SSM = 19;
    int ARINC429_LABELSDI = 20;
    int ARINC429_LABELDATA = 21;
    int ARINC429_LABELSSM = 22;
    int M1553B_CSWORD = 23;//指令/状态字
    int M1553B_RTADDR = 24;//远程终端地址
    int M1553B_DATAWORD = 25;//数据字同步头
    int LIN_PARITY_ERROR= 26;
    int LIN_CHECKSUM_ERROR= 27;
}
