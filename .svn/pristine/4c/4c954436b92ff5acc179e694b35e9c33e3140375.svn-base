//
// Created by liwb on 2019/2/20.
//

#ifndef TBOOKSCOPE_SCPI_CMD_PRODUCTION_H
#define TBOOKSCOPE_SCPI_CMD_PRODUCTION_H
#include "../inc/scpi.h"

#ifdef  __cplusplus
extern "C" {
#endif
    scpi_result_t PRO_WARE(scpi_t * context); //////固件烧写
    scpi_result_t PRO_APP(scpi_t * context); //////MCU、APP烧写
    scpi_result_t PRO_SYS_WRITEQ(scpi_t * context); ////产品信息写入查询
    scpi_result_t PRO_SYS_SNQ(scpi_t * context); ////查询SN
    scpi_result_t PRO_SYS_IDQ(scpi_t * context); ////查询ID
    scpi_result_t PRO_SYS_WDATE(scpi_t * context);///写入生产日期
    scpi_result_t PRO_PRIV_UUIDQ(scpi_t * context); ////查询唯一识别码
    scpi_result_t PRO_PRIV_HWVERSIONQ(scpi_t * context); ////查询硬件版本
    scpi_result_t PRO_PRIV_SERIAINOQ(scpi_t * context); ////查询SN
    scpi_result_t PRO_PRIV_STRINGCODE(scpi_t * context); ////串码写入
    scpi_result_t PRO_PRIV_DISPLAY_SERIAINO(scpi_t * context);  ////写入外部SN号
    scpi_result_t PRO_PRIV_MACHINETYPE(scpi_t * context); ////设置设备型号
    scpi_result_t PRO_PRIV_STAR(scpi_t *context);// 开启私有
    scpi_result_t PRO_PRIV_STARQ(scpi_t * context);//查询私有状态
    scpi_result_t PRO_PRIV_STOP(scpi_t * context); //关闭私有
    scpi_result_t PRO_PRIV_BANDWIDTH(scpi_t * context);//设置带宽
    scpi_result_t PRO_PRIV_SETT_CLE(scpi_t * context);//清楚设置
    scpi_result_t PRO_INT_TIME(scpi_t * context);  //设置系统时间
    scpi_result_t PRO_INT_CLEA(scpi_t * context); //恢复系统配置
    scpi_result_t COMMON_CoreIdnQ(scpi_t * context); //
    scpi_result_t PRO_INT_SHUTDOWN(scpi_t * context);  //关机
    scpi_result_t PRO_INT_REBOOT(scpi_t * context);  //重启
    scpi_result_t PRO_INT_STANDBY(scpi_t * context);  //待机
    scpi_result_t PRO_INT_WAKEUP(scpi_t * context);  //唤醒
    scpi_result_t PRO_INT_LOCK(scpi_t * context);  //锁屏
    scpi_result_t PRO_INT_UNLOCK(scpi_t * context);  //锁屏
    scpi_result_t PRO_SYS_TEMPERATUREQ(scpi_t * context); //温度
    scpi_result_t PRO_SYS_FPGA_TEMPERATUREQ(scpi_t* context); //
    scpi_result_t PRO_SYS_FPGA_STATUSQ(scpi_t* context); //

#ifdef  __cplusplus
}
#endif

#endif //TBOOKSCOPE_SCPI_CMD_PRODUCTION_H
