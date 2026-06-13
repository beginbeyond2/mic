
#include "scpi_cmd_calibrate.h"
#include <stdio.h>
#include <stdlib.h>
#include <string>
#include "scpi_help.h"
#include "../common/scpi-def.h"
#include "../../SCPICommandCallBackJava.h"
#include "../../Log.h"

using namespace std;

void onCALStart()
{
//#ifdef CHANNEL_DUAL
//    int chCnt = 2;
//#elif defined CHANNEL_FOUR
//    int chCnt = 4;
//#else
//#error please define Channel nums
//#endif
//    gMainWindow->CloseRef();
//    gMainWindow->CloseMath();
//    if(gMainWindow->isInZoomMode())
//    {
//        gMainWindow->setZoomMode(false);//exit zoom
//    }
//    if(gMainWindow->isInHighRefresh())
//    {
//        gMainWindow->setHighRefresh(false);
//    }
//
//    gMainWindow->OpenAllChannel();
//    for(int i=0;i<chCnt;i++)
//    {
//        CH_IDX idx = (CH_IDX)(CI_CH1+i);
//        CChannelPosResetMsg::Instance()->exec(idx);
//        CChannelDragMsg::Instance()->exec(IModuleDevice::instance()->vPosOfZero(0, idx), idx);
//    }
//    if(IModuleDevice::instance()->isInRunState(false))
//    {
//        if(IModuleDevice::instance()->flagAuto(0))
//        {
//            CGearToAutoMsg::Instance()->exec(0);
//        }
//        if(IModuleDevice::instance()->isInSingleState(0))
//        {
//            IModuleDevice::instance()->isInSingleState(1) = 0;
//            ILight::Instance()->ControlLed(ILight::LED_SINGLE, false);
//            ISystemEvent::Instance()->SetLedsVal(ILight::Instance()->GetLedsVal());
//        }
//    }
//    else
//    {
//        CGearToRunStopMsg::Instance()->exec(true);
//    }
//
//    ControlLockScreen::instance((QWidget *)gMainWindow)->lockScreen();
}

void onCALEnd()
{
//#ifdef CHANNEL_DUAL
//    int chCnt = 2;
//#elif defined CHANNEL_FOUR
//    int chCnt = 4;
//#else
//#error please define Channel nums
//#endif
//    ControlLockScreen::instance((QWidget *)gMainWindow)->unLockScreen();
//
//    for(int i=0;i<chCnt;i++)
//    {
//        CH_IDX idx = (CH_IDX)(CI_CH1+i);
//
//        CChannelPosResetMsg::Instance()->exec(idx);
//        CChannelDragMsg::Instance()->exec(IModuleDevice::instance()->vPosOfZero(0, idx), idx);
//    }
//    CGearToControlUpdateMsg *ccup = CGearToControlUpdateMsg::Instance();
//    ccup->exec(UPDATE_TRIGSTATE, NULL);
}

//向校准软件发送查询校准状态结果
void SendCALResult(scpi_t * context, string type)
{
//    FirmwareCalibrate * cali = FirmwareCalibrate::instance();
//    if(cali->isCalibrateDone())
//    {
//        QSettings *pCalResultSetting = new QSettings(LAST_CAL_RESULT_FILE,QSettings::IniFormat);
//        if(pCalResultSetting != NULL)
//        {
//            if(pCalResultSetting->value(type) == CAL_FAILED)
//            {
//                SCPI_ResultString(context,"failed");
////                printf("result failed \n");
//            }
//            else if(pCalResultSetting->value(type) == CAL_SUCCESSFUL)
//            {
//                SCPI_ResultString(context,"successful");
////                printf("result successful \n");
//            }
//            else if(pCalResultSetting->value(type).toString().length() == 0)//按理不会出现这种情况
//            {
//                SCPI_ResultString(context,"cmd error");
////                printf("result cmd error \n");
//            }
//            delete pCalResultSetting;
//            pCalResultSetting=NULL;
//        }
//    }
//    else
//    {
//        SCPI_ResultString(context,"caling");
////        printf("result caling \n");
//    }
}

//查询上次校准时间
scpi_result_t CAL_DATEQ(scpi_t * context)
{
    //Q_UNUSED(context);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//开始校准
scpi_result_t CAL_STAR(scpi_t * context)
{
//    onCALStart();
//    SEND_RECEIVE_CMD(context);
//     const char * param1="OKAY";
//    setParam_Resutl_1String(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//退出校准
scpi_result_t CAL_QUIT(scpi_t * context)
{
//    onCALEnd();
//    SEND_RECEIVE_CMD(context);
//    const char * param1="OKAY";
//    setParam_Resutl_1String(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//停止校准，强制停止
scpi_result_t CAL_STOP(scpi_t * context)
{
    //printf("CAL_STOP stop\n");
//    FirmwareCalibrate * cali = FirmwareCalibrate::instance();
//    cali->forceEndCalibrate();
//    SEND_RECEIVE_CMD(context);
//    const char * param1="OKAY";
//    setParam_Resutl_1String(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//查询校准结果zichuan
scpi_result_t CAL_RESQ(scpi_t * context)
{
//    FirmwareCalibrate * cali = FirmwareCalibrate::instance();
//    string str = cali->getResultStr();
    //printf("CAL_RESQ----------00000000:\n string:%s strLen:%d\n",str.data(),str.length());
    //SCPI_ResultString(context,str.data());

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//零点校准
scpi_result_t CAL_ZER(scpi_t * context)
{
    //printf("CAL_ZER\n");
//    FirmwareCalibrate * cali = FirmwareCalibrate::instance();
//    cali->setCalibrateMode(true);
//    cali->startCalibrateMode(FMCID_CHZERO, 0, 0);//参数2，参数3没用
//    SEND_RECEIVE_CMD(context);
//    const char * param1="OKAY";
//    setParam_Resutl_1String(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//查询零点校准状态
scpi_result_t CAL_ZERQ(scpi_t * context)
{
    //printf("CAL_ZERQ\n");
    //SendCALResult(context,ZERO_POINT_CAL_RESULT);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//通道差异校准
scpi_result_t CAL_CHDF(scpi_t * context)
{
    //printf("CAL_CHDF\n");
//    FirmwareCalibrate * cali = FirmwareCalibrate::instance();
//    cali->setCalibrateMode(true);
//    cali->startCalibrateMode(FMCID_CHDIFF, 0, 0);//参数2，参数3没用
//    SEND_RECEIVE_CMD(context);
//    const  char * param1="OKAY";
//    setParam_Resutl_1String(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//查询通道差异校准状态
scpi_result_t CAL_CHDFQ(scpi_t * context)
{
    //printf("CAL_CHDFQ\n");
   // SendCALResult(context,CHDIFF_CAL_RESULT);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//AD相位校准
scpi_result_t CAL_ADPH(scpi_t * context)
{
    double param1;

    if (!SCPI_ParamDouble(context, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    //fprintf(stderr, "\tP1=%lf\r\n", param1);

//    FirmwareCalibrate * cali = FirmwareCalibrate::instance();
//    cali->setCalibrateMode(true);
//    cali->startCalibrateMode(FMCID_ADPHASE, (int)(param1-1), 0);//参数3没用
//    SEND_RECEIVE_CMD(context);

    setParam_1Double(context->env,context->param,param1);
//    const char * param2="OKAY";
//    setParam_Resutl_1String(context->env,context->param,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//查询AD相位校准状态
scpi_result_t CAL_ADPHQ(scpi_t * context)
{
    double param1;

    if (!SCPI_ParamDouble(context, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }

    //SendCALResult(context,ADPHASE_CAL_RESULT+QString::number((int)param1));
    setParam_1Double(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//AD增益校准
scpi_result_t CAL_ADG(scpi_t * context)
{
//    FirmwareCalibrate * cali = FirmwareCalibrate::instance();
//    cali->setCalibrateMode(true);
//    cali->startCalibrateMode(FMCID_ADGAIN, 0, 0);//参数2 参数3 没用
//    SEND_RECEIVE_CMD(context);
//    const char * param1="OKAY";
//    setParam_Resutl_1String(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//查询AD增益校准状态
scpi_result_t CAL_ADGQ(scpi_t * context)
{
   // SendCALResult(context,ADGAIN_CAL_RESULT);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//偏移量校准
scpi_result_t CAL_OFFS(scpi_t * context)
{
//    FirmwareCalibrate * cali = FirmwareCalibrate::instance();
//    cali->setCalibrateMode(true);
//    cali->startCalibrateMode(FMCID_CHCOEF, 0, 0);//参数2 参数3 没用
//    SEND_RECEIVE_CMD(context);
//    const char * param1="OKAY";
//    setParam_Resutl_1String(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//查询偏移量校准状态
scpi_result_t CAL_OFFSQ(scpi_t * context)
{
    //SendCALResult(context,OFFSET_CAL_RESULT);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//通道增益校准
scpi_result_t CAL_CHG(scpi_t * context)
{
    double param1, param2,param3,param4;

    if (!SCPI_ParamDouble(context, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    if (!SCPI_ParamDouble(context, &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    if (!SCPI_ParamDouble(context, &param3, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    if (!SCPI_ParamDouble(context, &param4, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }

    //fprintf(stderr, "\tP1=%lf\r\n", param1);
    //fprintf(stderr, "\tP1=%lf\r\n", param2);

//    FirmwareCalibrate * cali = FirmwareCalibrate::instance();
//    cali->setCalibrateMode(true);
//    cali->SetCalTag(PC_CENTER);
//    cali->startCalibrateMode(FMCID_CHGAIN, (int)(param1-1), (int)(param2-1));
//    SEND_RECEIVE_CMD(context);

    //setParam_2Double(context->env,context->param,param1,param2);
    setParam_4Double(context->env,context->param,param1,param2,param3,param4);
//    const char * param3="OKAY";
//    setParam_Resutl_1String(context->env,context->param,param3);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//查询通道增益校准状态
scpi_result_t CAL_CHGQ(scpi_t * context)
{
    double param1, param2;

    if (!SCPI_ParamDouble(context, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    if (!SCPI_ParamDouble(context, &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
//    SendCALResult(context,CHGAIN_CAL_RESULT+QString::number((int)param1)
//                  +"_Scale"+QString::number((int)param2));
    setParam_2Double(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
//通道增益校准
scpi_result_t CAL_ExCHG(scpi_t * context)
{
    double param1, param2,param3,param4;

    if (!SCPI_ParamDouble(context, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    if (!SCPI_ParamDouble(context, &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    if (!SCPI_ParamDouble(context, &param3, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    if (!SCPI_ParamDouble(context, &param4, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }

    setParam_4Double(context->env,context->param,param1,param2,param3,param4);

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//查询通道增益校准状态
scpi_result_t CAL_ExCHGQ(scpi_t * context)
{
    double param1, param2;

    if (!SCPI_ParamDouble(context, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    if (!SCPI_ParamDouble(context, &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }

    setParam_2Double(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t CAL_CHSetV(scpi_t * context){
    double param1, param2;

    if (!SCPI_ParamDouble(context, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    if (!SCPI_ParamDouble(context, &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_2Double(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

scpi_result_t CAL_CHVQ(scpi_t * context)
{
    double param1, param2;

    if (!SCPI_ParamDouble(context, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    if (!SCPI_ParamDouble(context, &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
//    SendCALResult(context,CHGAIN_CAL_RESULT+QString::number((int)param1)
//                  +"_Scale"+QString::number((int)param2));
    setParam_2Double(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

scpi_result_t CAL_CHCofit(scpi_t * context)
{
    double param1, param2,param3,param4;

    if (!SCPI_ParamDouble(context, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    if (!SCPI_ParamDouble(context, &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    if (!SCPI_ParamDouble(context, &param3, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    if (!SCPI_ParamDouble(context, &param4, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }

    //fprintf(stderr, "\tP1=%lf\r\n", param1);
    //fprintf(stderr, "\tP1=%lf\r\n", param2);

//    FirmwareCalibrate * cali = FirmwareCalibrate::instance();
//    cali->setCalibrateMode(true);
//    cali->SetCalTag(PC_CENTER);
//    cali->startCalibrateMode(FMCID_CHGAIN, (int)(param1-1), (int)(param2-1));
//    SEND_RECEIVE_CMD(context);

    //setParam_2Double(context->env,context->param,param1,param2);
    setParam_4Double(context->env,context->param,param1,param2,param3,param4);
//    const char * param3="OKAY";
//    setParam_Resutl_1String(context->env,context->param,param3);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//查询通道增益校准状态
scpi_result_t CAL_CHCofitQ(scpi_t * context)
{
    double param1, param2;

    if (!SCPI_ParamDouble(context, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    if (!SCPI_ParamDouble(context, &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
//    SendCALResult(context,CHGAIN_CAL_RESULT+QString::number((int)param1)
//                  +"_Scale"+QString::number((int)param2));
    setParam_2Double(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
//通道增益校准
scpi_result_t CAL_CHCap(scpi_t * context)
{
    double param1, param2;

    if (!SCPI_ParamDouble(context, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    if (!SCPI_ParamDouble(context, &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }

    //fprintf(stderr, "\tP1=%lf\r\n", param1);
    //fprintf(stderr, "\tP1=%lf\r\n", param2);

//    FirmwareCalibrate * cali = FirmwareCalibrate::instance();
//    cali->setCalibrateMode(true);
//    cali->SetCalTag(PC_CENTER);
//    cali->startCalibrateMode(FMCID_CHGAIN, (int)(param1-1), (int)(param2-1));
//    SEND_RECEIVE_CMD(context);

    setParam_2Double(context->env,context->param,param1,param2);
    //setParam_2Double(context->env,context->param,param1,param2);
//    const char * param3="OKAY";
//    setParam_Resutl_1String(context->env,context->param,param3);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//查询通道增益校准状态
scpi_result_t CAL_CHCapQ(scpi_t * context)
{
    double param1, param2;

    if (!SCPI_ParamDouble(context, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    if (!SCPI_ParamDouble(context, &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
//    SendCALResult(context,CHGAIN_CAL_RESULT+QString::number((int)param1)
//                  +"_Scale"+QString::number((int)param2));
    setParam_2Double(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t CAL_CapVal(scpi_t * context)
{
    int param1, param2;
    double param3;
    if (!SCPI_ParamInt(context, &param1, true)) {
        return SCPI_RES_ERR;
    }

    if(!SCPI_ParamDouble(context,&param3, true)){
        return SCPI_RES_ERR;
    }

    if (!SCPI_ParamInt(context, &param2, true)) {
        return SCPI_RES_ERR;
    }

    //setParam_2Int(context->env,context->param,param1,param2);
    setParam_2Int1Double(context->env,context->param,param1,param2,param3);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//查询通道增益校准状态
scpi_result_t CAL_CapValQ(scpi_t * context)
{

    int param1;
    double param2;
    if(!SCPI_ParamInt(context,&param1, true)){
        return SCPI_RES_ERR;
    }

    if(!SCPI_ParamDouble(context,&param2, true)){
        return SCPI_RES_ERR;
    }

    setParam_1Int1Double(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t CAL_UPCal(scpi_t * context)
{
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
//查询通道增益校准状态
scpi_result_t CAL_UPCalQ(scpi_t * context)
{
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

scpi_result_t CAL_DOWNCal(scpi_t * context)
{
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
//查询通道增益校准状态
scpi_result_t CAL_DOWNCalQ(scpi_t * context)
{
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
//触发触发零点校准
scpi_result_t CAL_TRIG_ZER(scpi_t * context)
{
    double param1;

    if (!SCPI_ParamDouble(context, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    //fprintf(stderr, "\tP1=%lf\r\n", param1);

//    FirmwareCalibrate * cali = FirmwareCalibrate::instance();
//    cali->setCalibrateMode(true);
//    cali->startCalibrateMode(FMCID_TRZERO, (int)(param1-1), 0);//参数3没用
//    SEND_RECEIVE_CMD(context);

    setParam_1Double(context->env,context->param,param1);
//    const char * param2="OKAY";
//    setParam_Resutl_1String(context->env,context->param,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//查询触发零点校准状态
scpi_result_t CAL_TRIG_ZERQ(scpi_t * context)
{
    double param1;

    if (!SCPI_ParamDouble(context, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
   // SendCALResult(context,TRIGGER_ZERO_CAL_RESULT+QString::number((int)param1));
    setParam_1Double(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
//触发触发零点校准
scpi_result_t CAL_TRIG_ZERAC(scpi_t * context)
{
    double param1;

    if (!SCPI_ParamDouble(context, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    //fprintf(stderr, "\tP1=%lf\r\n", param1);

//    FirmwareCalibrate * cali = FirmwareCalibrate::instance();
//    cali->setCalibrateMode(true);
//    cali->startCalibrateMode(FMCID_TRZEROAC, (int)(param1-1), 0);//参数3没用
//    SEND_RECEIVE_CMD(context);

    setParam_1Double(context->env,context->param,param1);
//    const char * param2="OKAY";
//    setParam_Resutl_1String(context->env,context->param,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//查询触发零点校准状态
scpi_result_t CAL_TRIG_ZERACQ(scpi_t * context)
{
    double param1;

    if (!SCPI_ParamDouble(context, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    //SendCALResult(context,TRIGGER_ZEROAC_CAL_RESULT+QString::number((int)param1));
    setParam_1Double(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//触发系数校准
scpi_result_t CAL_TRIG_COEF(scpi_t * context)
{
    double param1;

    if (!SCPI_ParamDouble(context, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    //fprintf(stderr, "\tP1=%lf\r\n", param1);

//    FirmwareCalibrate * cali = FirmwareCalibrate::instance();
//    cali->setCalibrateMode(true);
//    cali->startCalibrateMode(FMCID_TRCOEF, (int)(param1-1), 0);//参数3没用
//    SEND_RECEIVE_CMD(context);

    setParam_1Double(context->env,context->param,param1);
//    const  char * param2="OKAY";
//    setParam_Resutl_1String(context->env,context->param,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//查询触发系数校准状态
scpi_result_t CAL_TRIG_COEFQ(scpi_t * context)
{
    double param1;

    if (!SCPI_ParamDouble(context, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
   // SendCALResult(context,TRIGGER_COEF_CAL_RESULT+QString::number((int)param1));
    setParam_1Double(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//精准触发校准
scpi_result_t CAL_TRIG_PREC(scpi_t * context)
{
    double param1;

    if (!SCPI_ParamDouble(context, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    //fprintf(stderr, "\tP1=%lf\r\n", param1);

//    FirmwareCalibrate * cali = FirmwareCalibrate::instance();
//    cali->setCalibrateMode(true);
//    cali->startCalibrateMode(FMCID_TRIG_OFFSET, (int)(param1-1), 0);//参数没用
//    SEND_RECEIVE_CMD(context);

    setParam_1Double(context->env,context->param,param1);
//    const char * param2="OKAY";
//    setParam_Resutl_1String(context->env,context->param,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//查询精准触发校准状态
scpi_result_t CAL_TRIG_PRECQ(scpi_t * context)
{
    double param1;

    if (!SCPI_ParamDouble(context, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
   // SendCALResult(context,TRIGGER_PRECISE_RESULT+QString::number((int)param1));
    setParam_1Double(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//查询校准数据长度
scpi_result_t CAL_DATA_LENGQ(scpi_t * context)
{
    //Q_UNUSED(context);

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//获取校准数据
scpi_result_t CAL_DATE_GET(scpi_t * context)
{
    //Q_UNUSED(context);

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//复位校准信息(删除校准文件)
scpi_result_t CAL_FILE_RESQ(scpi_t * context)
{
//    QFile factoryFile("/firmware/scope/factoryCalibrateData.dat");
//    QFile userFile("/firmware/scope/userCalibrateData.dat");
//    QFile lastResultFile("/firmware/scope/lastcal_result.ini");
//
//    factoryFile.remove();
//    userFile.remove();
//    lastResultFile.remove();
//    SCPI_ResultString(context,"successful");
//    const char * param1="successful";
//    setParam_Resutl_1String(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//告知客户端收到命令，且解析正确，暂时只针对校准命令
void SEND_RECEIVE_CMD(scpi_t * context)
{
    SCPI_ResultString(context,"OKAY");
}
