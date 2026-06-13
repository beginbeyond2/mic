
#include "scpi_cmd_meas.h"
#include <stdio.h>
#include <stdlib.h>
#include <string>
#include "scpi_help.h"
#include "../../SCPICommandCallBackJava.h"
#include "../../Log.h"


double getValue(int idx, int typ, int delay)
{
//    MenuMeasureFrame *temp = (MenuMeasureFrame *)gMainWindow->GetMenuFrame_Help(OscilloUi::MI_MEASURE);
//    double res = temp->getMeasureValue((CH_IDX)idx,(MeasureInfoItem::MEASURE_TYPE)typ,(CH_IDX)delay);
//    return res;
    return 12.0f;
}


const char * meas_all[] = {
    "PERiod",//0
    "FREQ",
    "RISetime",
    "FALLtime",
    "DELay",
    "PDUTy",//5
    "NDUTy",
    "PWIDth",
    "NWIDth",
    "BURStw",
    "ROVershoot",//10
    "FOVershoot",
    "PHASe",
    "PKPK",
    "AMPlitude",
    "HIGH",//15
    "LOW",
    "MAX",
    "MIN",
    "RMS",
    "CRMS",//20
    "MEAN",
    "CMEan",
    "ACRMs",
    "PRATe",
    "NRATe",//25
    "TVALue",
    "M27","M28","M29","M30","M31","M32","M33",
    "M34","M35","M36","M37","M38",
    "COLVal",
    NULL
};
const char * delay_edge[] = {
    "FRISe",
    "FFALl",
    "LRISe",
    "LFALl",
    NULL
};
const char* meas_range[]={
        "SCReen",
        "CURSor",
        NULL
};
const char* meas_threshold[]={
        "PERCent",
        "ABSolute",
        NULL
};

const char * meas_cursor[]={
        "NONE",
        "X1",
        "X2",
        NULL
};
//��������
scpi_result_t MEAS_OPEN(scpi_t * context)
{
//
// //Q_UNUSED(context);

//    ERROR_XY_MODE;
    int param1;
    if (!SCPI_ParamChoice(context, meas_all, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    switch(param1)
    {
    case 4://delay
        {
            int param2;
            if (!SCPI_ParamChoice(context, allCh, &param2, true))
            {
                dealCallBack_ParamError(context);
                return SCPI_RES_ERR;
            }
            //CH_IDX ch1 = (CH_IDX)getCh(param2);

            int param3;
            if (!SCPI_ParamChoice(context, allCh, &param3, true))
            {
                dealCallBack_ParamError(context);
                return SCPI_RES_ERR;
            }
            //CH_IDX ch2 = (CH_IDX)getCh(param3);

            int param4;
            if (!SCPI_ParamChoice(context, delay_edge, &param4, true))
            {
                dealCallBack_ParamError(context);
                return SCPI_RES_ERR;
            }
//            MenuConfigMeasureDelay::EDGE edge1=MenuConfigMeasureDelay::UI_FIRST_RISE_EDGE;
//            switch(param4)
//            {
//            case 0:edge1=MenuConfigMeasureDelay::UI_FIRST_RISE_EDGE;break;
//            case 1:edge1=MenuConfigMeasureDelay::UI_FIRST_FALL_EDGE;break;
//            case 2:edge1=MenuConfigMeasureDelay::UI_LAST_RISE_EDGE;break;
//            case 3:edge1=MenuConfigMeasureDelay::UI_LAST_FALL_EDGE;break;
//            default:break;
//            }

            int param5;
            if (!SCPI_ParamChoice(context, delay_edge, &param5, true))
            {
                dealCallBack_ParamError(context);
                return SCPI_RES_ERR;
            }
//            MenuConfigMeasureDelay::EDGE edge2=MenuConfigMeasureDelay::UI_FIRST_RISE_EDGE;
//            switch(param5)
//            {
//            case 0:edge2=MenuConfigMeasureDelay::UI_FIRST_RISE_EDGE;break;
//            case 1:edge2=MenuConfigMeasureDelay::UI_FIRST_FALL_EDGE;break;
//            case 2:edge2=MenuConfigMeasureDelay::UI_LAST_RISE_EDGE;break;
//            case 3:edge2=MenuConfigMeasureDelay::UI_LAST_FALL_EDGE;break;
//            default:break;
//            }
//            gMainWindow->SCPI_OpenMeasure(MIT_DELAY,ch1,ch2,edge1,edge2);
        setParam_5Int(context->env,context->param,param1,param2,param3,param4,param5);
        dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
            break;
        }
    case 12://phase �������Ӧ
        {
            int param2;
            if (!SCPI_ParamChoice(context, allCh, &param2, true))
            {
                dealCallBack_ParamError(context);
                return SCPI_RES_ERR;
            }
           // CH_IDX ch1 = (CH_IDX)getCh(param2);

            int param3;
            if (!SCPI_ParamChoice(context, allCh, &param3, true))
            {
                dealCallBack_ParamError(context);
                return SCPI_RES_ERR;
            }
//            CH_IDX ch2 = (CH_IDX)getCh(param3);
//            gMainWindow->SCPI_OpenMeasure(MIT_PHASE,ch1,ch2);
        setParam_5Int(context->env,context->param,param1,param2,param3,-1,-1);
        dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
            break;
        }
    case 0:/*SCPI_OPEN_MEASURE(MIT_PERIOD);   */            SCPI_MEASURE_PARAM;break;
    case 1:/*SCPI_OPEN_MEASURE(MIT_FREQ);                */ SCPI_MEASURE_PARAM;break;
    case 2:/*SCPI_OPEN_MEASURE(MIT_RISE_TIME);           */ SCPI_MEASURE_PARAM;break;
    case 3:/*SCPI_OPEN_MEASURE(MIT_FALL_TIME);           */ SCPI_MEASURE_PARAM;break;
    case 5:/*SCPI_OPEN_MEASURE(MIT_POSITIVE_DUTY_CYCLE); */ SCPI_MEASURE_PARAM;break;
    case 6:/*SCPI_OPEN_MEASURE(MIT_NEGATIVE_DUTY_CYCLE); */ SCPI_MEASURE_PARAM;break;
    case 7:/*SCPI_OPEN_MEASURE(MIT_POSITIVE_PULSE_WIDTH);*/ SCPI_MEASURE_PARAM;break;
    case 8:/*SCPI_OPEN_MEASURE(MIT_NEGATIVE_PULSE_WIDTH);*/ SCPI_MEASURE_PARAM;break;
    case 9:/*SCPI_OPEN_MEASURE(MIT_BURST_WIDTH);         */ SCPI_MEASURE_PARAM;break;
    case 10:/*SCPI_OPEN_MEASURE(MIT_POSITIVE_OVERSHOOT); */ SCPI_MEASURE_PARAM;break;
    case 11:/*SCPI_OPEN_MEASURE(MIT_NEGATIVE_OVERSHOOT); */ SCPI_MEASURE_PARAM;break;
    case 13:/*SCPI_OPEN_MEASURE(MIT_PK_PK);              */ SCPI_MEASURE_PARAM;break;
    case 14:/*SCPI_OPEN_MEASURE(MIT_AMPLITUDE);          */ SCPI_MEASURE_PARAM;break;
    case 15:/*SCPI_OPEN_MEASURE(MIT_HIGH);               */ SCPI_MEASURE_PARAM;break;
    case 16:/*SCPI_OPEN_MEASURE(MIT_LOW);                */ SCPI_MEASURE_PARAM;break;
    case 17:/*SCPI_OPEN_MEASURE(MIT_MAX);                */ SCPI_MEASURE_PARAM;break;
    case 18:/*SCPI_OPEN_MEASURE(MIT_MIN);                */ SCPI_MEASURE_PARAM;break;
    case 19:/*SCPI_OPEN_MEASURE(MIT_RMS);                */ SCPI_MEASURE_PARAM;break;
    case 20:/*SCPI_OPEN_MEASURE(MIT_CYCLE_RMS);          */ SCPI_MEASURE_PARAM;break;
    case 21:/*SCPI_OPEN_MEASURE(MIT_MEAN);               */ SCPI_MEASURE_PARAM;break;
    case 22:/*SCPI_OPEN_MEASURE(MIT_CYCLE_MEAN);         */ SCPI_MEASURE_PARAM;break;
    case 23:/* SCPI_OPEN_MEASURE(MIT_ACRMS);             */ SCPI_MEASURE_PARAM;break;
    case 24:/*SCPI_OPEN_MEASURE(MIT_Positive_rate);      */ SCPI_MEASURE_PARAM;break;
    case 25:/*SCPI_OPEN_MEASURE(MIT_negative_rate);      */ SCPI_MEASURE_PARAM;break;
    case 26:{
        int cnt = SCPI_ParamCount(context);
        int param2 = -1;
        double param3 = 0;
        int param4 = 0;
        int param5 = 0;
        if (!SCPI_ParamChoice(context, allCh, &param2, true)){
            dealCallBack_ParamError(context);
            return SCPI_RES_ERR;
        }

        if (!SCPI_ParamDouble(context, &param3, true)) {
            dealCallBack_ParamError(context);
            return SCPI_RES_ERR;
        }

        if (!SCPI_ParamInt(context,&param4,true)){
            dealCallBack_ParamError(context);
            return SCPI_RES_ERR;
        }

        if(!SCPI_ParamChoice(context,meas_cursor,&param5,true)){
            param5 = 0;
        }

        setParam_4Int1Double(context->env,context->param,param1,param2,param4,param5,param3);
        dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    }
    break;
    case 39://
    {
        int param2;
        if (!SCPI_ParamChoice(context, allCh, &param2, true))
        {
            dealCallBack_ParamError(context);
            return SCPI_RES_ERR;
        }
       // CH_IDX ch1 = (CH_IDX)getCh(param2);
        int param3;
        if (!SCPI_ParamInt(context,&param3,true)){
            dealCallBack_ParamError(context);
            return SCPI_RES_ERR;
        }
        //printf("ch1 = %d,param3 = %d\n",ch1,param3);
//        if(Channel::isDynamicCh(ch1)
//                &&param3>= 0 && param3 < IModuleDevice::instance()->horizonPixelsOfOutputWindow()){
//            CWaveMsg::Instance()->EnableNonstandardMeasure(ch1,param3);
//        }
//        else
//        {
//           return SCPI_RES_ERR;
//        }
        setParam_5Int(context->env,context->param,param1,param2,param3,-1,-1);
        dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
        break;
    }
    default:break;
    }
    return SCPI_RES_OK;
}

//�رղ�����
scpi_result_t MEAS_CLOS(scpi_t * context)
{
//
// //Q_UNUSED(context);

//    ERROR_XY_MODE;
    int param1;
    if (!SCPI_ParamChoice(context, meas_all, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    switch(param1)
    {
    case 0:/*SCPI_CLOSE_MEASURE(MIT_PERIOD);              */  SCPI_MEASURE_PARAM; break;
    case 1:/*SCPI_CLOSE_MEASURE(MIT_FREQ);                */  SCPI_MEASURE_PARAM; break;
    case 2:/*SCPI_CLOSE_MEASURE(MIT_RISE_TIME);           */  SCPI_MEASURE_PARAM; break;
    case 3:/*SCPI_CLOSE_MEASURE(MIT_FALL_TIME);           */  SCPI_MEASURE_PARAM; break;
    case 4:/*SCPI_CLOSE_MEASURE(MIT_DELAY);               */  SCPI_MEASURE_PARAM; break;
    case 5:/*SCPI_CLOSE_MEASURE(MIT_POSITIVE_DUTY_CYCLE); */  SCPI_MEASURE_PARAM; break;
    case 6:/*SCPI_CLOSE_MEASURE(MIT_NEGATIVE_DUTY_CYCLE); */  SCPI_MEASURE_PARAM; break;
    case 7:/*SCPI_CLOSE_MEASURE(MIT_POSITIVE_PULSE_WIDTH);*/  SCPI_MEASURE_PARAM; break;
    case 8:/*SCPI_CLOSE_MEASURE(MIT_NEGATIVE_PULSE_WIDTH);*/  SCPI_MEASURE_PARAM; break;
    case 9:/*SCPI_CLOSE_MEASURE(MIT_BURST_WIDTH);         */  SCPI_MEASURE_PARAM; break;
    case 10:/*SCPI_CLOSE_MEASURE(MIT_POSITIVE_OVERSHOOT); */  SCPI_MEASURE_PARAM; break;
    case 11:/*SCPI_CLOSE_MEASURE(MIT_NEGATIVE_OVERSHOOT); */  SCPI_MEASURE_PARAM; break;
    case 12:/*SCPI_CLOSE_MEASURE(MIT_PHASE);              */  SCPI_MEASURE_PARAM; break;
    case 13:/*SCPI_CLOSE_MEASURE(MIT_PK_PK);              */  SCPI_MEASURE_PARAM; break;
    case 14:/*SCPI_CLOSE_MEASURE(MIT_AMPLITUDE);          */  SCPI_MEASURE_PARAM; break;
    case 15:/*SCPI_CLOSE_MEASURE(MIT_HIGH);               */  SCPI_MEASURE_PARAM; break;
    case 16:/*SCPI_CLOSE_MEASURE(MIT_LOW);                */  SCPI_MEASURE_PARAM; break;
    case 17:/*SCPI_CLOSE_MEASURE(MIT_MAX);                */  SCPI_MEASURE_PARAM; break;
    case 18:/*SCPI_CLOSE_MEASURE(MIT_MIN);                */  SCPI_MEASURE_PARAM; break;
    case 19:/*SCPI_CLOSE_MEASURE(MIT_RMS);                */  SCPI_MEASURE_PARAM; break;
    case 20:/*SCPI_CLOSE_MEASURE(MIT_CYCLE_RMS);          */  SCPI_MEASURE_PARAM; break;
    case 21:/*SCPI_CLOSE_MEASURE(MIT_MEAN);               */  SCPI_MEASURE_PARAM; break;
    case 22:/*SCPI_CLOSE_MEASURE(MIT_CYCLE_MEAN);         */  SCPI_MEASURE_PARAM; break;
    case 23:/* SCPI_CLOSE_MEASURE(MIT_ACRMS);             */  SCPI_MEASURE_PARAM; break;
    case 24:/*SCPI_CLOSE_MEASURE(MIT_Positive_rate);      */  SCPI_MEASURE_PARAM; break;
    case 25:/*SCPI_CLOSE_MEASURE(MIT_negative_rate);      */  SCPI_MEASURE_PARAM; break;
    case 26: SCPI_MEASURE_PARAM; break;
    case 39://
    {
        int param2;
        if (!SCPI_ParamChoice(context, allCh, &param2, true))
        {
            dealCallBack_ParamError(context);
            return SCPI_RES_ERR;
        }
//        CH_IDX ch1 = (CH_IDX)getCh(param2);
//
//        if(Channel::isDynamicCh(ch1)){
//            CWaveMsg::Instance()->DisableNonstandardMeasure(ch1);
//        }
//        else
//        {
//           return SCPI_RES_ERR;
//        }
        setParam_2Int(context->env,context->param,param1,param2);
        dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
        break;
    }
    default:break;
    }
    return SCPI_RES_ERR;
}

//��ѯָ��ͨ�����ε����ڲ���ֵ
scpi_result_t MEAS_PERQ(scpi_t * context)
{
//    ERROR_XY_MODEQ;
//    IModuleChannel *mc = dynamic_cast<IModuleChannel *>(
//        ProcessCenter::IModuleRegisterDesk::module(
//        ProcessCenter::MI_CHANNEL));
//    CH_IDX idx = mc->active();
//    SCPI_GET_MEASURE_IDX;
//
//    double res = getValue(idx,MeasureInfoItem::MTYPE_PERIOD,0);
//    SCPI_ResultDouble(context,res);
    int  param1;
    if (!SCPI_ParamChoice(context, allCh, &param1, true))
    {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR CH!");
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//��ѯָ��ͨ�����ε�Ƶ�ʲ���ֵ
scpi_result_t MEAS_FREQQ(scpi_t * context)
{
//    ERROR_XY_MODEQ;
//    IModuleChannel *mc = dynamic_cast<IModuleChannel *>(
//        ProcessCenter::IModuleRegisterDesk::module(
//        ProcessCenter::MI_CHANNEL));
//    CH_IDX idx = mc->active();
//    SCPI_GET_MEASURE_IDX;
//
//    double res = getValue(idx,MeasureInfoItem::MTYPE_FREQUENCY,0);
//    SCPI_ResultDouble(context,res);
    int  param1;
    if (!SCPI_ParamChoice(context, allCh, &param1, true))
    {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR CH!");
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//��ѯָ��ͨ�����ε�����ʱ�����ֵ
scpi_result_t MEAS_RISQ(scpi_t * context)
{
//    ERROR_XY_MODEQ;
//    IModuleChannel *mc = dynamic_cast<IModuleChannel *>(
//        ProcessCenter::IModuleRegisterDesk::module(
//        ProcessCenter::MI_CHANNEL));
//    CH_IDX idx = mc->active();
//    SCPI_GET_MEASURE_IDX;
//
//    double res = getValue(idx,MeasureInfoItem::MTYPE_RISETIME,0);
//    SCPI_ResultDouble(context,res);
    int  param1;
    if (!SCPI_ParamChoice(context, allCh, &param1, true))
    {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR CH!");
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//��ѯָ��ͨ�����ε��½�ʱ�����ֵ
scpi_result_t MEAS_FALLQ(scpi_t * context)
{
//    ERROR_XY_MODEQ;
//    IModuleChannel *mc = dynamic_cast<IModuleChannel *>(
//        ProcessCenter::IModuleRegisterDesk::module(
//        ProcessCenter::MI_CHANNEL));
//    CH_IDX idx = mc->active();
//    SCPI_GET_MEASURE_IDX;
//
//    double res = getValue(idx,MeasureInfoItem::MTYPE_FALLTIME,0);
//    SCPI_ResultDouble(context,res);
    int  param1;
    if (!SCPI_ParamChoice(context, allCh, &param1, true))
    {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR CH!");
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//��ѯָ��ͨ�����ε���ռ�ձȲ���ֵ
scpi_result_t MEAS_PDUTQ(scpi_t * context)
{
//    ERROR_XY_MODEQ;
//    IModuleChannel *mc = dynamic_cast<IModuleChannel *>(
//        ProcessCenter::IModuleRegisterDesk::module(
//        ProcessCenter::MI_CHANNEL));
//    CH_IDX idx = mc->active();
//    SCPI_GET_MEASURE_IDX;
//    double res = getValue(idx,MeasureInfoItem::MTYPE_PDC,0);
//    SCPI_ResultDouble(context,res);
    int  param1;
    if (!SCPI_ParamChoice(context, allCh, &param1, true))
    {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR CH!");
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//��ѯָ��ͨ�����εĸ�ռ�ձȲ���ֵ
scpi_result_t MEAS_NDUTQ(scpi_t * context)
{
//    ERROR_XY_MODEQ;
//    IModuleChannel *mc = dynamic_cast<IModuleChannel *>(
//        ProcessCenter::IModuleRegisterDesk::module(
//        ProcessCenter::MI_CHANNEL));
//    CH_IDX idx = mc->active();
//    SCPI_GET_MEASURE_IDX;
//
//    double res = getValue(idx,MeasureInfoItem::MTYPE_NDC,0);
//    SCPI_ResultDouble(context,res);
    int  param1;
    if (!SCPI_ParamChoice(context, allCh, &param1, true))
    {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR CH!");
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//��ѯָ��ͨ�����ε����������ֵ
scpi_result_t MEAS_PWIDQ(scpi_t * context)
{
//    ERROR_XY_MODEQ;
//    IModuleChannel *mc = dynamic_cast<IModuleChannel *>(
//        ProcessCenter::IModuleRegisterDesk::module(
//        ProcessCenter::MI_CHANNEL));
//    CH_IDX idx = mc->active();
//    SCPI_GET_MEASURE_IDX;
//
//    double res = getValue(idx,MeasureInfoItem::MTYPE_PPW,0);
//    SCPI_ResultDouble(context,res);
    int  param1;
    if (!SCPI_ParamChoice(context, allCh, &param1, true))
    {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR CH!");
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//��ѯָ��ͨ�����εĸ��������ֵ
scpi_result_t MEAS_NWIDQ(scpi_t * context)
{
//    ERROR_XY_MODEQ;
//    IModuleChannel *mc = dynamic_cast<IModuleChannel *>(
//        ProcessCenter::IModuleRegisterDesk::module(
//        ProcessCenter::MI_CHANNEL));
//    CH_IDX idx = mc->active();
//    SCPI_GET_MEASURE_IDX;
//    double res = getValue(idx,MeasureInfoItem::MTYPE_NPW,0);
//    SCPI_ResultDouble(context,res);
    int  param1;
    if (!SCPI_ParamChoice(context, allCh, &param1, true))
    {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR CH!");
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//��ѯָ��ͨ�����ε�ͻ�������Ȳ���ֵ
scpi_result_t MEAS_BURSQ(scpi_t * context)
{
//    ERROR_XY_MODEQ;
//    IModuleChannel *mc = dynamic_cast<IModuleChannel *>(
//        ProcessCenter::IModuleRegisterDesk::module(
//        ProcessCenter::MI_CHANNEL));
//    CH_IDX idx = mc->active();
//    SCPI_GET_MEASURE_IDX;
//
//    double res = getValue(idx,MeasureInfoItem::MTYPE_BPW,0);
//    SCPI_ResultDouble(context,res);
    int  param1;
    if (!SCPI_ParamChoice(context, allCh, &param1, true))
    {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR CH!");
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//��ѯָ��ͨ�����ε�����������ֵ
scpi_result_t MEAS_POVQ(scpi_t * context)
{
//    ERROR_XY_MODEQ;
//    IModuleChannel *mc = dynamic_cast<IModuleChannel *>(
//        ProcessCenter::IModuleRegisterDesk::module(
//        ProcessCenter::MI_CHANNEL));
//    CH_IDX idx = mc->active();
//    SCPI_GET_MEASURE_IDX;
//    double res = getValue(idx,MeasureInfoItem::MTYPE_POS,0);
//    SCPI_ResultDouble(context,res);
    int  param1;
    if (!SCPI_ParamChoice(context, allCh, &param1, true))
    {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR CH!");
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//��ѯָ��ͨ�����εĸ���������ֵ
scpi_result_t MEAS_NOVQ(scpi_t * context)
{
//    ERROR_XY_MODEQ;
//    IModuleChannel *mc = dynamic_cast<IModuleChannel *>(
//        ProcessCenter::IModuleRegisterDesk::module(
//        ProcessCenter::MI_CHANNEL));
//    CH_IDX idx = mc->active();
//    SCPI_GET_MEASURE_IDX;
//    double res = getValue(idx,MeasureInfoItem::MTYPE_NOS,0);
//    SCPI_ResultDouble(context,res);
    int  param1;
    if (!SCPI_ParamChoice(context, allCh, &param1, true))
    {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR CH!");
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//��ѯָ��ͨ�����εķ��ֵ
scpi_result_t MEAS_PKPKQ(scpi_t * context)
{
//    ERROR_XY_MODEQ;
//    IModuleChannel *mc = dynamic_cast<IModuleChannel *>(
//        ProcessCenter::IModuleRegisterDesk::module(
//        ProcessCenter::MI_CHANNEL));
//    CH_IDX idx = mc->active();
//    SCPI_GET_MEASURE_IDX;
//    double res = getValue(idx,MeasureInfoItem::MTYPE_PKPK,0);
//    SCPI_ResultDouble(context,res);
    int  param1;
    if (!SCPI_ParamChoice(context, allCh, &param1, true))
    {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR CH!");
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//��ѯָ��ͨ�����εķ��Ȳ���ֵ
scpi_result_t MEAS_AMPQ(scpi_t * context)
{
//    ERROR_XY_MODEQ;
//    IModuleChannel *mc = dynamic_cast<IModuleChannel *>(
//        ProcessCenter::IModuleRegisterDesk::module(
//        ProcessCenter::MI_CHANNEL));
//    CH_IDX idx = mc->active();
//    SCPI_GET_MEASURE_IDX;
//    double res = getValue(idx,MeasureInfoItem::MTYPE_AMP,0);
//    SCPI_ResultDouble(context,res);
    int  param1;
    if (!SCPI_ParamChoice(context, allCh, &param1, true))
    {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR CH!");
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//��ѯָ��ͨ�����εĸ�ֵ
scpi_result_t MEAS_HIGHQ(scpi_t * context)
{
//    ERROR_XY_MODEQ;
//    IModuleChannel *mc = dynamic_cast<IModuleChannel *>(
//        ProcessCenter::IModuleRegisterDesk::module(
//        ProcessCenter::MI_CHANNEL));
//    CH_IDX idx = mc->active();
//    SCPI_GET_MEASURE_IDX;
//    double res = getValue(idx,MeasureInfoItem::MTYPE_HIGH,0);
//    SCPI_ResultDouble(context,res);
    int  param1;
    if (!SCPI_ParamChoice(context, allCh, &param1, true))
    {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR CH!");
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//��ѯָ��ͨ�����εĵ�ֵ
scpi_result_t MEAS_LOWQ(scpi_t * context)
{
//    ERROR_XY_MODEQ;
//    IModuleChannel *mc = dynamic_cast<IModuleChannel *>(
//        ProcessCenter::IModuleRegisterDesk::module(
//        ProcessCenter::MI_CHANNEL));
//    CH_IDX idx = mc->active();
//    SCPI_GET_MEASURE_IDX;
//    double res = getValue(idx,MeasureInfoItem::MTYPE_LOW,0);
//    SCPI_ResultDouble(context,res);
    int  param1;
    if (!SCPI_ParamChoice(context, allCh, &param1, true))
    {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR CH!");
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//��ѯָ��ͨ�����ε����ֵ
scpi_result_t MEAS_MAXQ(scpi_t * context)
{
//    ERROR_XY_MODEQ;
//    IModuleChannel *mc = dynamic_cast<IModuleChannel *>(
//        ProcessCenter::IModuleRegisterDesk::module(
//        ProcessCenter::MI_CHANNEL));
//    CH_IDX idx = mc->active();
//    SCPI_GET_MEASURE_IDX;
//    double res = getValue(idx,MeasureInfoItem::MTYPE_MAX,0);
//    SCPI_ResultDouble(context,res);
    int  param1;
    if (!SCPI_ParamChoice(context, allCh, &param1, true))
    {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR CH!");
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//��ѯָ��ͨ�����ε���Сֵ
scpi_result_t MEAS_MINQ(scpi_t * context)
{
//    ERROR_XY_MODEQ;
//    IModuleChannel *mc = dynamic_cast<IModuleChannel *>(
//        ProcessCenter::IModuleRegisterDesk::module(
//        ProcessCenter::MI_CHANNEL));
//    CH_IDX idx = mc->active();
//    SCPI_GET_MEASURE_IDX;
//    double res = getValue(idx,MeasureInfoItem::MTYPE_MIN,0);
//    SCPI_ResultDouble(context,res);
    int  param1;
    if (!SCPI_ParamChoice(context, allCh, &param1, true))
    {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR CH!");
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//��ѯָ��ͨ�����εľ�����ֵ
scpi_result_t MEAS_RMSQ(scpi_t * context)
{
//    ERROR_XY_MODEQ;
//    IModuleChannel *mc = dynamic_cast<IModuleChannel *>(
//        ProcessCenter::IModuleRegisterDesk::module(
//        ProcessCenter::MI_CHANNEL));
//    CH_IDX idx = mc->active();
//    SCPI_GET_MEASURE_IDX;
//    double res = getValue(idx,MeasureInfoItem::MTYPE_RMS,0);
//    SCPI_ResultDouble(context,res);
    int  param1;
    if (!SCPI_ParamChoice(context, allCh, &param1, true))
    {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR CH!");
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//��ѯָ��ͨ�����ε����ھ�����ֵ
scpi_result_t MEAS_CRMSQ(scpi_t * context)
{

//    ERROR_XY_MODEQ;
//    IModuleChannel *mc = dynamic_cast<IModuleChannel *>(
//        ProcessCenter::IModuleRegisterDesk::module(
//        ProcessCenter::MI_CHANNEL));
//    CH_IDX idx = mc->active();
//    SCPI_GET_MEASURE_IDX;
//    double res = getValue(idx,MeasureInfoItem::MTYPE_CRMS,0);
//    SCPI_ResultDouble(context,res);
    int  param1;
    if (!SCPI_ParamChoice(context, allCh, &param1, true))
    {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR CH!");
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//��ѯָ��ͨ�����ε�ƽ��ֵ
scpi_result_t MEAS_MEANQ(scpi_t * context)
{
//    ERROR_XY_MODEQ;
//    IModuleChannel *mc = dynamic_cast<IModuleChannel *>(
//        ProcessCenter::IModuleRegisterDesk::module(
//        ProcessCenter::MI_CHANNEL));
//    CH_IDX idx = mc->active();
//    SCPI_GET_MEASURE_IDX;
//    double res = getValue(idx,MeasureInfoItem::MTYPE_MEAN,0);
//    SCPI_ResultDouble(context,res);
    int  param1;
    if (!SCPI_ParamChoice(context, allCh, &param1, true))
    {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR CH!");
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}


//��ѯָ��ͨ�����ε�����ƽ��ֵ
scpi_result_t MEAS_CMEQ(scpi_t * context)
{
//    ERROR_XY_MODEQ;
//    IModuleChannel *mc = dynamic_cast<IModuleChannel *>(
//        ProcessCenter::IModuleRegisterDesk::module(
//        ProcessCenter::MI_CHANNEL));
//    CH_IDX idx = mc->active();
//    SCPI_GET_MEASURE_IDX;
//    double res = getValue(idx,MeasureInfoItem::MTYPE_CMEAN,0);
//    SCPI_ResultDouble(context,res);
    int  param1;
    if (!SCPI_ParamChoice(context, allCh, &param1, true))
    {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR CH!");
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;

}
scpi_result_t MEAS_ACRMQ(scpi_t * context){
    int  param1;
    if (!SCPI_ParamChoice(context, allCh, &param1, true))
    {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR CH!");
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t MEAS_PRATQ(scpi_t * context){
    int  param1;
    if (!SCPI_ParamChoice(context, allCh, &param1, true))
    {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR CH!");
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t MEAS_NRATQ(scpi_t * context){
    int  param1;
    if (!SCPI_ParamChoice(context, allCh, &param1, true))
    {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR CH!");
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

scpi_result_t MEAS_COLVQ(scpi_t *context)
{
//    ERROR_XY_MODEQ;
//    IModuleChannel *mc = dynamic_cast<IModuleChannel *>(
//        ProcessCenter::IModuleRegisterDesk::module(
//        ProcessCenter::MI_CHANNEL));
//    CH_IDX idx = mc->active();

    int  param1;
    if (!SCPI_ParamChoice(context, allCh, &param1, true))
    {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR CH!");
        return SCPI_RES_ERR;
    }
//    idx = (CH_IDX)getCh(param1);
//    if(!Channel::isDynamicCh(idx))
//    {
//        DEBUG_RETURN("ERROR CH!");
//        return SCPI_RES_ERR;
//    }
//    double res = CWaveMsg::Instance()->GetNonstandardMeasureValue(idx);
//    SCPI_ResultDouble(context,res);
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}


//��ѯͨ�����ӳٲ����Ľ��
scpi_result_t MEAS_DELQ(scpi_t * context)
{
//    ERROR_XY_MODEQ;
    int param1;
    if (!SCPI_ParamChoice(context, allCh, &param1, true))
    {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR PARAM!");
        return SCPI_RES_ERR;
    }
//    CH_IDX idx = (CH_IDX)getCh(param1);
//    if(!Channel::isDynamicCh(idx) && idx!=CI_MATH)
//    {
//        DEBUG_RETURN("ERROR CH!");
//        return SCPI_RES_ERR;
//    }

    int param2;
    if (!SCPI_ParamChoice(context, allCh, &param2, true))
    {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR PARAM!");
        return SCPI_RES_ERR;
    }
//    CH_IDX idx2 = (CH_IDX)getCh(param2);
//    if(!Channel::isDynamicCh(idx2) && idx2!=CI_MATH)
//    {
//        DEBUG_RETURN("ERROR CH!");
//        return SCPI_RES_ERR;
//    }
//
//    double res = getValue(idx,MeasureInfoItem::MTYPE_DELAY,idx2);
//    SCPI_ResultDouble(context,res);
    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}


//��ѯָ��ͨ������λ������Ľ��
scpi_result_t MEAS_PHASQ(scpi_t * context)
{
   // ERROR_XY_MODEQ;
    int param1;
    if (!SCPI_ParamChoice(context, allCh, &param1, true))
    {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR PARAM!");
        return SCPI_RES_ERR;
    }
//    CH_IDX idx = (CH_IDX)getCh(param1);
//    if(!Channel::isDynamicCh(idx) && idx!=CI_MATH)
//    {
//        DEBUG_RETURN("ERROR CH!");
//        return SCPI_RES_ERR;
//    }

    int param2;
    if (!SCPI_ParamChoice(context, allCh, &param2, true))
    {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR PARAM!");
        return SCPI_RES_ERR;
    }
//    CH_IDX idx2 = (CH_IDX)getCh(param2);
//    if(!Channel::isDynamicCh(idx2) && idx2!=CI_MATH)
//    {
//        DEBUG_RETURN("ERROR CH!");
//        return SCPI_RES_ERR;
//    }
//    double res = getValue(idx,MeasureInfoItem::MTYPE_PHASE,idx2);
//    SCPI_ResultDouble(context,res);
    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

const char * meas_cle[] = {
    "ITEM1",
    "ITEM2",
    "ITEM3",
    "ITEM4",
    "ITEM5",
    "ITEM6",
    "ITEM7",
    "ITEM8",
    "ITEM9",
    "ITEM10",
    "ITEM11",
    "ITEM12",
    "ITEM13",
    "ITEM14",
    "ITEM15",
    "ITEM16",
    "ITEM17",
    "ITEM18",
    "ITEM19",
    "ITEM20",
    "ITEM21",
    "ITEM22",
    "ITEM23",
    "ITEM24",
    "ITEM25",
    "ITEM26",
    "ITEM27",
    "ITEM28",
    "ITEM29",
    "ITEM30",
    "ITEM31",
    "ITEM32",
    "ITEM33",
    "ITEM34",
    "ITEM35",
    "ITEM36",
    "ITEM37",
    "ITEM38",
    "ITEM39",
    "ITEM40",
    "ALL",
    NULL
};
//������Ĳ������е���һ���������
scpi_result_t MEAS_CLE(scpi_t * context)
{
   // ERROR_XY_MODEQ;
    int param1;
    if (!SCPI_ParamChoice(context, meas_cle, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }

//    MenuMeasureFrame *temp = (MenuMeasureFrame *)gMainWindow->GetMenuFrame_Help(OscilloUi::MI_MEASURE);
//    temp->CloseMeasure(param1);//param1����������涨�ܸ��������������ѡ������
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}


//��ѯָ��ͨ�����ε����
scpi_result_t MEAS_AREQ(scpi_t * context){
    //SEND_RECEIVE_CMD(context);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
//��ѯָ��ͨ�����ε��������
scpi_result_t MEAS_CARQ(scpi_t * context){
    //SEND_RECEIVE_CMD(context);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;}




scpi_result_t MEAS_ADIS(scpi_t * context)
{
    bool param1;
    if (!SCPI_ParamBool(context,&param1,true))
    {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
//   // bool bVisible = gMainWindow->fullMeasBox->IsInWork();
//    MenuBottom *tem=gMainWindow-> GetMenuBottom();
//    tem->setALLMeasureDisplay(param1);
    setParam_1Boolean(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
//��ѯȫ����������ر�
scpi_result_t MEAS_ADISQ(scpi_t * context)
{

//    MenuBottom *tem=gMainWindow-> GetMenuBottom();
//    bool bVisible=tem->getAllMeasureDisplay();
//    SCPI_ResultBool(context,bVisible);

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
//���ò�����Χ
scpi_result_t MEAS_SCOP(scpi_t * context){
    //Q_UNUSED(context);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;}
//��ѯ������Χ
scpi_result_t MEAS_SCOPQ(scpi_t * context){
    //SEND_RECEIVE_CMD(context);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;}


const char * count_ch[]={
        "CLOSe",
        "CH1",
        "CH2",
        "CH3",
        "CH4",
        NULL
};
scpi_result_t MEAS_COUNTER_SOUR(scpi_t * context)
{
    //Q_UNUSED(context);
    int param1;
    if (!SCPI_ParamChoice(context, count_ch, &param1, true))
    {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR PARAM!");
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
//��ѯԴ
scpi_result_t MEAS_COUNTER_SOURQ(scpi_t * context)
{

    //Q_UNUSED(context);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
const char* count_mode[]={
        "FREQuency",
        "PERiod",
        "TOTalize",
        NULL
};
scpi_result_t MEAS_COUNTER_MODE(scpi_t * context){
    int param1;
    if (!SCPI_ParamChoice(context, count_mode, &param1, true))
    {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR PARAM!");
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t MEAS_COUNTER_MODEQ(scpi_t * context){
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//��ѯƵ�ʼ�
scpi_result_t MEAS_COUNTER_VALQ(scpi_t* context)
{

    //Q_UNUSED(context);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
//������Դ
scpi_result_t MEAS_ITEM(scpi_t* context)
{
    //Q_UNUSED(context);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//��ѯ��Դ
scpi_result_t MEAS_ITEMQ(scpi_t* context)
{
    //Q_UNUSED(context);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

scpi_result_t MEAS_TVALUE(scpi_t* context){

    int param2;
    if (!SCPI_ParamChoice(context, allCh, &param2, true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }

    double param1;
    if(!SCPI_ParamDouble(context,&param1, true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }

    int param3;
    if(!SCPI_ParamInt(context,&param3, true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    int param5 = 0;

    if(!SCPI_ParamChoice(context,meas_cursor,&param5,true)){
        param5 = 0;
    }

    setParam_3Int1Double(context->env,context->param,param2,param3,param5,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

scpi_result_t MEAS_TVALUEQ(scpi_t* context){
    int  param2;
    if (!SCPI_ParamChoice(context, allCh, &param2, true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }

    LOGD(" %s param2: %d ",__FUNCTION__ ,param2);

    setParam_1Int(context->env,context->param,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

scpi_result_t MEAS_STAT_DISP(scpi_t * context){
    bool param1;
    if (!SCPI_ParamBool(context,&param1, true))
    {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR PARAM!");
        return SCPI_RES_ERR;
    }
    setParam_1Boolean(context->env,context->param,param1);

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t MEAS_STAT_DISPQ(scpi_t * context){
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t MEAS_STAT_RES(scpi_t * context){
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t  MEAS_STAT_MEAN(scpi_t * context){
    bool param1;
    if (!SCPI_ParamBool(context,&param1, true))
    {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR PARAM!");
        return SCPI_RES_ERR;
    }
    setParam_1Boolean(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t  MEAS_STAT_MEANQ(scpi_t * context){
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t  MEAS_STAT_MAX(scpi_t * context){
    bool param1;
    if (!SCPI_ParamBool(context,&param1, true))
    {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR PARAM!");
        return SCPI_RES_ERR;
    }
    setParam_1Boolean(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t  MEAS_STAT_MAXQ(scpi_t * context){
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t  MEAS_STAT_MIN(scpi_t * context){
    bool param1;
    if (!SCPI_ParamBool(context,&param1, true))
    {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR PARAM!");
        return SCPI_RES_ERR;
    }
    setParam_1Boolean(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t  MEAS_STAT_MINQ(scpi_t * context){
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t  MEAS_STAT_DEV(scpi_t * context){
    bool param1;
    if (!SCPI_ParamBool(context,&param1, true))
    {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR PARAM!");
        return SCPI_RES_ERR;
    }
    setParam_1Boolean(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t  MEAS_STAT_DEVQ(scpi_t * context){
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t  MEAS_STAT_COUNT(scpi_t * context){
    bool param1;
    if (!SCPI_ParamBool(context,&param1, true))
    {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR PARAM!");
        return SCPI_RES_ERR;
    }
    setParam_1Boolean(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t  MEAS_STAT_COUNTQ(scpi_t * context){
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t  MEAS_STAT_VIEWQ(scpi_t * context){
    int param1;
    if (!SCPI_ParamChoice(context,meas_all,&param1,true)){
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR PARAM!");
        return SCPI_RES_ERR;
    }
    int param2;
    if (!SCPI_ParamChoice(context,allCh,&param2,true)){
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR PARAM!");
        return SCPI_RES_ERR;
    }
    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t  MEAS_STAT_MEAN_VIEWQ(scpi_t * context){
    return MEAS_STAT_VIEWQ(context);
}
scpi_result_t  MEAS_STAT_MAX_VIEWQ(scpi_t * context){
    return MEAS_STAT_VIEWQ(context);
}
scpi_result_t  MEAS_STAT_MIN_VIEWQ(scpi_t * context){
    return MEAS_STAT_VIEWQ(context);
}
scpi_result_t  MEAS_STAT_DEV_VIEWQ(scpi_t * context){
    return MEAS_STAT_VIEWQ(context);
}
scpi_result_t  MEAS_STAT_COUNT_VIEWQ(scpi_t * context){
    return MEAS_STAT_VIEWQ(context);
}
scpi_result_t  MEAS_STAT_CURRENT_VIEWQ(scpi_t * context){
    return MEAS_STAT_VIEWQ(context);
}


scpi_result_t MEAS_SETTING_IND(scpi_t* context){
    bool param1;
    if (!SCPI_ParamBool(context,&param1, true))
    {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR PARAM!");
        return SCPI_RES_ERR;
    }
    setParam_1Boolean(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

scpi_result_t MEAS_SETTING_RANGE(scpi_t* context){
    int param1;
    if (!SCPI_ParamChoice(context,meas_range,&param1,true)){
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR PARAM!");
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t MEAS_SETTING_THRESHOLD(scpi_t* context){
    int param1;
    if (!SCPI_ParamChoice(context,meas_threshold,&param1,true)){
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR PARAM!");
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t MEAS_SETTING_HIGH(scpi_t* context){
    const char* param = NULL;
    size_t len=0;
    if (!SCPI_ParamString(context,&param,&len,true)){
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR PARAM!");
        return SCPI_RES_ERR;
    }
    std::string param1(param,len);
    setParam_1String(context->env,context->param,param1.c_str());
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t MEAS_SETTING_MID(scpi_t* context){
    return MEAS_SETTING_HIGH(context);
}
scpi_result_t MEAS_SETTING_LOW(scpi_t* context){
    return MEAS_SETTING_HIGH(context);
}


scpi_result_t MEAS_SETTING_INDQ(scpi_t* context){
    return MEAS_STAT_VIEWQ(context);
}
scpi_result_t MEAS_SETTING_RANGEQ(scpi_t* context){
    return MEAS_STAT_VIEWQ(context);
}
scpi_result_t MEAS_SETTING_THRESHOLDQ(scpi_t* context){
    return MEAS_STAT_VIEWQ(context);
}
scpi_result_t MEAS_SETTING_HIGHQ(scpi_t* context){
    return MEAS_STAT_VIEWQ(context);
}
scpi_result_t MEAS_SETTING_MIDQ(scpi_t* context){
    return MEAS_STAT_VIEWQ(context);
}
scpi_result_t MEAS_SETTING_LOWQ(scpi_t* context){
    return MEAS_STAT_VIEWQ(context);
}
scpi_result_t MEAS_LISTQ(scpi_t* context){
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

scpi_result_t MEAS_ADDNEW(scpi_t* context){
    const char* param = NULL;
    size_t len=0;
    if  (!SCPI_ParamString(context,&param,&len,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    if(len == 5 || len == 6){
        char * endptr;
        param += 4;
        int idx = std::strtol(param,&endptr,10);
        if(endptr != param && ((endptr - param) <= 2)){
            setParam_1Int(context->env,context->param,idx);
            dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
            return SCPI_RES_OK;
        }
    }
    return SCPI_RES_ERR;
}

scpi_result_t MEAS_DELETE(scpi_t* context){
    return MEAS_ADDNEW(context);
}

scpi_result_t MEAS_MEASX_TYPEQ(scpi_t* context){

    int param11 = 0;
    if (!SCPI_CmdParamInt(context,&param11)
        || param11 < 1
        || param11 > 40){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env, context->param, param11);
    dealCallBack(context->env, context->obj, context->param, context->scpi_command_index);
    return SCPI_RES_OK;
}

scpi_result_t MEAS_MEASX_TYPE(scpi_t* context){

    int param11 = 0;
    if (!SCPI_CmdParamInt(context,&param11)
        || param11 < 1
        || param11 > 40){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }

    int param1;
    if (!SCPI_ParamChoice(context, meas_all, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }

    switch(param1) {
        case 4://delay
        {
            int param2;
            if (!SCPI_ParamChoice(context, allCh, &param2, true)) {
                dealCallBack_ParamError(context);
                return SCPI_RES_ERR;
            }
            int param3;
            if (!SCPI_ParamChoice(context, allCh, &param3, true)) {
                dealCallBack_ParamError(context);
                return SCPI_RES_ERR;
            }
            int param4;
            if (!SCPI_ParamChoice(context, delay_edge, &param4, true)) {
                dealCallBack_ParamError(context);
                return SCPI_RES_ERR;
            }
            int param5;
            if (!SCPI_ParamChoice(context, delay_edge, &param5, true)) {
                dealCallBack_ParamError(context);
                return SCPI_RES_ERR;
            }
            setParam_6Int(context->env, context->param, param11,param1, param2, param3, param4, param5);
            dealCallBack(context->env, context->obj, context->param, context->scpi_command_index);
            break;
        }
        case 12://phase �������Ӧ
        {
            int param2;
            if (!SCPI_ParamChoice(context, allCh, &param2, true)) {
                dealCallBack_ParamError(context);
                return SCPI_RES_ERR;
            }

            int param3;
            if (!SCPI_ParamChoice(context, allCh, &param3, true)) {
                dealCallBack_ParamError(context);
                return SCPI_RES_ERR;
            }
            setParam_5Int(context->env, context->param, param11,param1, param2, param3,  -1);
            dealCallBack(context->env, context->obj, context->param, context->scpi_command_index);
            break;
        }
        case 26:
        {
            int param2 = -1;
            double param3 = 0;
            int param4 = 0;
            int param5 = 0;
            if (!SCPI_ParamChoice(context, allCh, &param2, true)){
                dealCallBack_ParamError(context);
                return SCPI_RES_ERR;
            }

            if (!SCPI_ParamDouble(context, &param3, true)) {
                dealCallBack_ParamError(context);
                return SCPI_RES_ERR;
            }

            if (!SCPI_ParamInt(context,&param4,true)){
                dealCallBack_ParamError(context);
                return SCPI_RES_ERR;
            }

            if(!SCPI_ParamChoice(context,meas_cursor,&param5,true)){
                param5 = 0;
            }

            setParam_5Int1Double(context->env,context->param,param11,param1,param2,param4,param5,param3);
            dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
            break;
        }
        default:
            setParam_2Int(context->env, context->param, param11,param1);
            dealCallBack(context->env, context->obj, context->param, context->scpi_command_index);
            break;
    }
    return SCPI_RES_OK;
}

scpi_result_t MEAS_MEASX_SOURCE1Q(scpi_t* context){

    return MEAS_MEASX_TYPEQ(context);
}

scpi_result_t MEAS_MEASX_SOURCE1(scpi_t* context){
    int param1;
    if (!SCPI_CmdParamInt(context,&param1)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    int param2;
    if (!SCPI_ParamChoice(context, allCh, &param2, true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_2Int(context->env, context->param, param1,  param2);
    dealCallBack(context->env, context->obj, context->param, context->scpi_command_index);
    return SCPI_RES_OK;
}

scpi_result_t MEAS_MEASX_SOURCE2Q(scpi_t* context){
    return MEAS_MEASX_TYPEQ(context);
}

scpi_result_t MEAS_MEASX_SOURCE2(scpi_t* context){

    return MEAS_MEASX_SOURCE1(context);
}

scpi_result_t MEAS_MEASX_VALUEQ(scpi_t* context){
    return MEAS_MEASX_TYPEQ(context);
}

scpi_result_t MEAS_MEASX_UNITQ(scpi_t* context){
    return MEAS_MEASX_TYPEQ(context);
}

scpi_result_t MEAS_MEASX_VALIDQ(scpi_t* context){
    return MEAS_MEASX_TYPEQ(context);
}

scpi_result_t MEAS_MEASX_EDGE1Q(scpi_t* context){
    return MEAS_MEASX_TYPEQ(context);
}

scpi_result_t MEAS_MEASX_EDGE1(scpi_t* context){
    int param1;
    if (!SCPI_CmdParamInt(context,&param1)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    int param2;
    if (!SCPI_ParamChoice(context, delay_edge, &param2, true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_2Int(context->env, context->param, param1,  param2);
    dealCallBack(context->env, context->obj, context->param, context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t MEAS_MEASX_EDGE2Q(scpi_t* context){
    return MEAS_MEASX_TYPEQ(context);
}
scpi_result_t MEAS_MEASX_EDGE2(scpi_t* context){
    return MEAS_MEASX_EDGE1(context);
}
scpi_result_t MEAS_MEASX_CURSORQ(scpi_t* context){
    return MEAS_MEASX_TYPEQ(context);
}
scpi_result_t MEAS_MEASX_CURSOR(scpi_t* context){
    int param1;
    if (!SCPI_CmdParamInt(context,&param1)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    int param2;
    if (!SCPI_ParamChoice(context, meas_cursor, &param2, true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_2Int(context->env, context->param, param1,  param2);
    dealCallBack(context->env, context->obj, context->param, context->scpi_command_index);
    return SCPI_RES_OK;
}

scpi_result_t MEAS_MEASX_VVLUE(scpi_t* context){
    int param1;
    if (!SCPI_CmdParamInt(context,&param1)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    double  param2;
    if (!SCPI_ParamDouble(context, &param2, true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_2Int(context->env, context->param, param1,  param2);
    dealCallBack(context->env, context->obj, context->param, context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t MEAS_MEASX_VVLUEQ(scpi_t* context){
    return MEAS_MEASX_TYPEQ(context);
}