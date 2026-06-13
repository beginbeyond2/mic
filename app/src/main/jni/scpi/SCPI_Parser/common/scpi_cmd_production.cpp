//
// Created by liwb on 2019/2/20.
//

#include "scpi_cmd_production.h"
#include <stdio.h>
#include <stdlib.h>
#include <memory.h>
#include <string>
#include "scpi_cmd_calibrate.h"
#include "scpi_help.h"
#include "../../SCPICommandCallBackJava.h"
#include "../../Log.h"

//////固件烧写
scpi_result_t PRO_WARE(scpi_t * context){
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
//////MCU、APP烧写
scpi_result_t PRO_APP(scpi_t * context){
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
////产品信息写入查询
scpi_result_t PRO_SYS_WRITEQ(scpi_t * context){
    const char* param = NULL;
    size_t len=0;
    if  (!SCPI_ParamString(context,&param,&len,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    std::string param1(param,len);

    len = 0;
    if  (!SCPI_ParamString(context,&param,&len,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    std::string param2(param,len);

    len = 0;
    if  (!SCPI_ParamString(context,&param,&len,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    std::string param3(param,len);

    len = 0;
    if  (!SCPI_ParamString(context,&param,&len,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    std::string param4(param,len);

    len = 0;
    if  (!SCPI_ParamString(context,&param,&len,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    std::string param5(param,len);
    setParam_5String(context->env,context->param,param1.c_str(),param2.c_str(),param3.c_str(),param4.c_str(),param5.c_str());
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);

    return SCPI_RES_OK;
}
////查询SN
scpi_result_t PRO_SYS_SNQ(scpi_t * context){
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t PRO_SYS_IDQ(scpi_t * context){
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
///写入生产日期
scpi_result_t PRO_SYS_WDATE(scpi_t * context)
{
    const char* param = NULL;
    size_t len=0;
    if  (!SCPI_ParamString(context,&param,&len,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    std::string param1(param,len);
    setParam_1String(context->env,context->param,param1.c_str());
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
////查询唯一识别码
scpi_result_t PRO_PRIV_UUIDQ(scpi_t * context){
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
////查询硬件版本
scpi_result_t PRO_PRIV_HWVERSIONQ(scpi_t * context){
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
////查询SN
scpi_result_t PRO_PRIV_SERIAINOQ(scpi_t * context){
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
////串码写入
scpi_result_t PRO_PRIV_STRINGCODE(scpi_t * context){
    const char* param=NULL;
    size_t len=0;
    if  (!SCPI_ParamString(context,&param,&len,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    std::string param1(param,len);
    setParam_1String(context->env,context->param,param1.c_str());
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
////写入外部SN号
scpi_result_t PRO_PRIV_DISPLAY_SERIAINO(scpi_t * context){
    const char* param =NULL;
    size_t len=0;
    if  (!SCPI_ParamString(context,&param,&len,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    std::string param1(param,len);
    setParam_1String(context->env,context->param,param1.c_str());
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
////设置设备型号
scpi_result_t PRO_PRIV_MACHINETYPE(scpi_t * context){
    const char* param=NULL;
    size_t len=0;
    if  (!SCPI_ParamString(context,&param,&len,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    std::string param1(param,len);
    setParam_1String(context->env,context->param,param1.c_str());
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
// 开启私有
scpi_result_t PRO_PRIV_STAR(scpi_t *context){
    const char* param = NULL;
    size_t len=0;
    if  (!SCPI_ParamString(context,&param,&len,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    std::string param1(param,len);
    setParam_1String(context->env,context->param,param1.c_str());
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
//查询私有状态
scpi_result_t PRO_PRIV_STARQ(scpi_t * context){
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//关闭私有
scpi_result_t PRO_PRIV_STOP(scpi_t * context){
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
//设置带宽
scpi_result_t PRO_PRIV_BANDWIDTH(scpi_t * context){
    int param1;
    if (!SCPI_ParamInt(context,&param1,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//清楚设置
scpi_result_t PRO_PRIV_SETT_CLE(scpi_t * context){
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//设置系统时间
scpi_result_t PRO_INT_TIME(scpi_t * context){
    const char* param=NULL;
    size_t len=0;
    if  (!SCPI_ParamString(context,&param,&len,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    std::string param1(param,len);

    len = 0;
    if  (!SCPI_ParamString(context,&param,&len,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    std::string param2(param,len);

    if  (!SCPI_ParamString(context,&param,&len,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    std::string param3(param,len);
    len = 0;
    if  (!SCPI_ParamString(context,&param,&len,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    std::string param4(param,len);
    len = 0;
    if  (!SCPI_ParamString(context,&param,&len,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    std::string param5(param,len);
    setParam_5String(context->env,context->param,param1.c_str(),param2.c_str(),param3.c_str(),param4.c_str(),param5.c_str());

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//恢复系统配置
scpi_result_t PRO_INT_CLEA(scpi_t * context)
{
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

scpi_result_t COMMON_CoreIdnQ(scpi_t * context){
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t PRO_INT_SHUTDOWN(scpi_t * context)  //关机
{
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t PRO_INT_REBOOT(scpi_t * context)  //重启
{
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t PRO_INT_STANDBY(scpi_t * context)  //待机
{
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t PRO_INT_WAKEUP(scpi_t * context)  //唤醒
{
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t PRO_INT_LOCK(scpi_t * context)  //锁屏
{
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t PRO_INT_UNLOCK(scpi_t * context)  //锁屏
{
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t PRO_SYS_TEMPERATUREQ(scpi_t * context){
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

scpi_result_t PRO_SYS_FPGA_TEMPERATUREQ(scpi_t* context){
    return PRO_SYS_TEMPERATUREQ(context);
}
scpi_result_t PRO_SYS_FPGA_STATUSQ(scpi_t* context){
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;;
}