#include "scpi_cmd_chan.h"
#include <stdio.h>
#include <stdlib.h>
#include <cstring>
#include <string>
#include "scpi_help.h"
#include "../../SCPICommandCallBackJava.h"
#include "../../Log.h"
const char * chan_prty[] = {
    "VOL",
    "CUR",
    NULL
};

const char * chan_coup[] = {
    "DC",
    "AC",
    "GND",
    NULL
};
const char * chan_inp[] = {
    "MEGA",
    "FIFTy",
    NULL
};
const char * chan_band[] = {
        "FULL"
        ,"200M"
        ,"20M"
        , "HIGH"
        , "LOW"
        , NULL
};
const char * chan_vref[] = {
        "CENTer",
        "ZERO",
        NULL
};

//�򿪻�ر�ָ��ͨ��
scpi_result_t CHAN_NUM_DISP(scpi_t * context)
{
    //��ӡ��allch������
    ////ERROR_XY_MODE;
    LOGD("setparam");
    int param1;
    if (SCPI_CmdParamInt(context,&param1)==false){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    bool  param2;
    if (!SCPI_ParamBool(context, &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_1Int1Boolean(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return deal_CHAN_DISP(param1,param2);
}

//�򿪻�ر�ͨ���ķ�����ʾ
scpi_result_t CHAN_NUM_INV(scpi_t * context)
{
    //ERROR_XY_MODE;
    int param1;
    if (SCPI_CmdParamInt(context,  &param1)==false) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    bool  param2;
    if (!SCPI_ParamBool(context, &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_1Int1Boolean(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return deal_CHAN_INV(param1,param2);

}

//����ͨ����̽������
scpi_result_t CHAN_NUM_PRTY(scpi_t * context)
{
    //ERROR_XY_MODE;
    int param1;
    if (SCPI_CmdParamInt(context, &param1)==false) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
//    CH_IDX idx = (CH_IDX)getCh(param1);
//    if(!Channel::isDynamicCh(idx)) {
//        return SCPI_RES_ERR;
//    }
    int param2;
    if (!SCPI_ParamChoice(context, chan_prty, &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    //deal_CHAN_PRTY(param1,param2);
    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//����̽ͷ��˥����
scpi_result_t CHAN_NUM_PROB(scpi_t * context)
{
    //ERROR_XY_MODE;
    int param1;
    if (!SCPI_CmdParamInt(context, &param1)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    double  param2;
    if (!SCPI_ParamDouble(context, &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    //deal_CHAN_PROB(param1,param2);
    setParam_1Int1Double(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}


//����ͨ��������Ϸ�ʽ
scpi_result_t CHAN_NUM_COUP(scpi_t * context)
{
    //ERROR_XY_MODE;
    int param1;
    if (!SCPI_CmdParamInt(context, &param1)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    int param2;
    if (!SCPI_ParamChoice(context, chan_coup, &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    //deal_CHAN_COUP(param1,param2);
    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}


//����ͨ��������ʾ�Ĵ�ֱ��λ  δʵ��
scpi_result_t CHAN_NUM_SCAL(scpi_t * context)
{
    //ERROR_XY_MODE;
    int param1;
    if (!SCPI_CmdParamInt(context, &param1)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    double param2;
    if (!SCPI_ParamDouble(context, &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_1Int1Double(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return deal_CHAN_SCAL(param1,param2);
}

//����ͨ��������ʾ�Ĵ�ֱƫ��
scpi_result_t CHAN_NUM_POS(scpi_t * context)
{
    //ERROR_XY_MODE;
    int param1;
    if (!SCPI_CmdParamInt(context, &param1)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    double param2;
    if (!SCPI_ParamDouble(context, &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_1Int1Double(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return deal_CHAN_POS(param1,param2);
}

//�򿪻�ر�ָ��ͨ���Ĵ�ֱ��λ΢������ δʵ��
scpi_result_t CHAN_NUM_VERN(scpi_t * context)
{
    //ERROR_XY_MODEQ;
    int param1;
    if (SCPI_CmdParamInt(context, &param1)==false) {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR PARAM!");
        return SCPI_RES_ERR;
    }
    bool param2;
    if (!SCPI_ParamBool(context,&param2,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_1Int1Boolean(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return deal_CHAN_VERN(param1,param2);
}

//��ȡͨ�����ε���λ��

scpi_result_t CHAN_NUM_PC(scpi_t * context)
{
    //ERROR_XY_MODEQ;
    int param1;
    if (SCPI_CmdParamInt(context, &param1)==false) {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR PARAM!");
        return SCPI_RES_ERR;
    }
    int param2;
    if (!SCPI_ParamInt(context,&param2,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return deal_CHAN_PC(param1,param2);
}

//�����迹
scpi_result_t CHAN_NUM_INP(scpi_t* context){
    //SCPI_ResultString(context,"ERROR_NO_SUPPORT!");
    int param1;
    if (!SCPI_CmdParamInt(context, &param1)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    int param2;
    if (!SCPI_ParamChoice(context, chan_inp, &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_ERR;
}

//��ѯͨ���򿪻�ر�

scpi_result_t CHAN_NUM_DISPQ(scpi_t * context)
{
    //ERROR_XY_MODEQ;
    int param1;
    if (SCPI_CmdParamInt(context, &param1)==false) {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR PARAM!");
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
   return  deal_CHAN_DISPQ(param1,context);
}

//��ѯͨ���ķ�����ʾ

scpi_result_t CHAN_NUM_INVQ(scpi_t * context)
{
    //ERROR_XY_MODEQ;
    int param1;
    if (SCPI_CmdParamInt(context, &param1)==false) {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR PARAM!");
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return deal_CHAN_INVQ(param1,context);
}

//��ѯͨ����̽������

scpi_result_t CHAN_NUM_PRTYQ(scpi_t * context)
{
    //ERROR_XY_MODEQ;
    int param1;
    if (SCPI_CmdParamInt(context, &param1)==false) {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR PARAM!");
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return deal_CHAN_PRTYQ(param1,context);
}

//��ѯ̽ͷ��˥����

scpi_result_t CHAN_NUM_PROBQ(scpi_t * context)
{
    //ERROR_XY_MODEQ;
    int param1;
    if (SCPI_CmdParamInt(context, &param1)==false) {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR PARAM!");
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return deal_CHAN_PROBQ(param1,context);
}


//��ѯͨ��������Ϸ�ʽ

scpi_result_t CHAN_NUM_COUPQ(scpi_t * context)
{
    //ERROR_XY_MODEQ;
    int param1;
    if (SCPI_CmdParamInt(context, &param1)==false) {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR PARAM!");
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return deal_CHAN_COUPQ(param1,context);
}

//��ѯͨ��������ʾ�Ĵ�ֱ��λ

scpi_result_t CHAN_NUM_SCALQ(scpi_t * context)
{
    //ERROR_XY_MODEQ;
    int param1;
    if (SCPI_CmdParamInt(context, &param1)==false) {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR PARAM!");
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return deal_CHAN_SCALQ(param1,context);
}

//��ѯͨ��������ʾ�Ĵ�ֱƫ��

scpi_result_t CHAN_NUM_POSQ(scpi_t * context)
{
    //ERROR_XY_MODEQ;
    int param1;
    if (SCPI_CmdParamInt(context, &param1)==false) {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR PARAM!");
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return deal_CHAN_POSQ(param1,context);
}

//��ѯָ��ͨ���Ĵ�ֱ��λ΢������

scpi_result_t CHAN_NUM_VERNQ(scpi_t * context)
{
    //ERROR_XY_MODEQ;
    int param1;
    if (SCPI_CmdParamInt(context, &param1)==false) {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR PARAM!");
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return deal_CHAN_VERNQ(param1,context);
}

//��ѯͨ�����ε���λ��״̬

scpi_result_t CHAN_NUM_PCQ(scpi_t * context)
{
    //ERROR_XY_MODEQ;
    int param1;
    if (SCPI_CmdParamInt(context, &param1)==false) {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR PARAM!");
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return deal_CHAN_PCQ(param1,context);
}

//�����迹
scpi_result_t CHAN_NUM_INPQ(scpi_t* context){
    //SCPI_ResultString(context,"ERROR_NO_SUPPORT!");
    int param1;
    if (SCPI_CmdParamInt(context, &param1)==false) {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR PARAM!");
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_ERR;
}

scpi_result_t CHAN_NUM_BAND(scpi_t* context){

    int paramCount=SCPI_ParamCount(context);
    int param1=0;
    if (paramCount==3 || paramCount==2){
        if (!SCPI_CmdParamInt(context, &param1)) {
            dealCallBack_ParamError(context);
            return SCPI_RES_ERR;
        }

        int param2;//param2��������chan_band[]
        if (!SCPI_ParamChoice(context, chan_band, &param2, true)) {
            dealCallBack_ParamError(context);
            return SCPI_RES_ERR;
        }

        double param3=0;
        if (param2==3 || param2==4){
            if (!SCPI_ParamDouble(context, &param3, true)) {
                dealCallBack_ParamError(context);
                return SCPI_RES_ERR;
            }
        }
        //deal_CHAN_BAND(param1,param2,param3);
        setParam_2Int1Double(context->env,context->param,param1,param2,param3);
        dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    }
    return SCPI_RES_OK;
}
scpi_result_t CHAN_NUM_BANDQ(scpi_t* context){
    int param1;
    if (SCPI_CmdParamInt(context, &param1)==false) {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR PARAM!");
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t CHAN_NUM_EXT(scpi_t* context){
    int param1;
    if (!SCPI_CmdParamInt(context,  &param1)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    double param2;
    if (!SCPI_ParamDouble(context, &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_1Int1Double(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    //deal_CHAN_SCAL(param1,param2);
    return SCPI_RES_OK;
}
scpi_result_t CHAN_NUM_PLUS_EXT(scpi_t* context){
    int param1;
    if (!SCPI_CmdParamInt(context,  &param1)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    int param2;
    if (!SCPI_ParamInt(context, &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    //deal_CHAN_SCAL_PLUS(param1,param2);
    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t CHAN_NUM_EXTQ(scpi_t* context){
    int param1;
    if (SCPI_CmdParamInt(context, &param1)==false) {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR PARAM!");
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t CHAN_NUM_VREF(scpi_t* context){
    int param1;
    if (!SCPI_CmdParamInt(context,  &param1)) {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR PARAM!");
        return SCPI_RES_ERR;
    }
    int param2;
    if (!SCPI_ParamChoice(context,chan_vref, &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    //deal_CHAN_POSQ(param1,context);
    return SCPI_RES_OK;
}
scpi_result_t CHAN_NUM_VREFQ(scpi_t* context){
    int param1;
    if (SCPI_CmdParamInt(context, &param1)==false) {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR PARAM!");
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

scpi_result_t CHAN_NUM_LAB(scpi_t* context){
    int param1;
    if (SCPI_CmdParamInt(context, &param1)==false) {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR PARAM!");
        return SCPI_RES_ERR;
    }

    const char * param = NULL;
    size_t len=0;
    if  (!SCPI_ParamString(context,&param,&len,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    std::string param2(param,len);
    setParam_1Int1String(context->env,context->param,param1,param2.c_str());
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;

}
scpi_result_t CHAN_NUM_LABQ(scpi_t* context){
    int param1;
    if (SCPI_CmdParamInt(context, &param1)==false) {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR PARAM!");
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t CHAN_NUM_LAB_CLEAR(scpi_t* context){
    int param1;
    if (!SCPI_CmdParamInt(context,  &param1)) {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR PARAM!");
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t CHAN_NUM_CURR(scpi_t* context){
    int param1;
    if (!SCPI_CmdParamInt(context,  &param1)) {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR PARAM!");
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

scpi_result_t CHAN_NUM_DELAY(scpi_t* context)
{
    int param1;
    if (!SCPI_CmdParamInt(context, &param1)) {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR PARAM!");
        return SCPI_RES_ERR;
    }
    double param2;
    if (!SCPI_ParamDouble(context,&param2,true)){
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR PARAM!");
        return SCPI_RES_ERR;
    }
    setParam_1Int1Double(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t CHAN_NUM_DELAYQ(scpi_t* context)
{
    int param1;
    if (SCPI_CmdParamInt(context, &param1)==false) {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR PARAM!");
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}


//ͨ���Ĵ򿪻�ر�

scpi_result_t CHAN_DISP(scpi_t * context)
{
    //ERROR_XY_MODE;
    int param1;
    if (!SCPI_ParamChoice(context, allCh, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    bool  param2;
    if (!SCPI_ParamBool(context, &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    //deal_CHAN_DISP(param1,param2);
    setParam_1Int1Boolean(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//��ѯͨ���Ĵ򿪻�ر�
scpi_result_t CHAN_DISPQ(scpi_t * context)
{
    //ERROR_XY_MODEQ;
    int param1;
    if (!SCPI_ParamChoice(context, allCh, &param1, true)) {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR PARAM!");
        return SCPI_RES_ERR;
    }
    //deal_CHAN_DISPQ(param1,context);
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//�򿪻�ر�ͨ���ķ�����ʾ
scpi_result_t CHAN_INV(scpi_t * context)
{
    //ERROR_XY_MODE;
    int param1;
    if (!SCPI_ParamChoice(context, allCh, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    bool  param2;
    if (!SCPI_ParamBool(context, &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    //deal_CHAN_INV(param1,param2);
    setParam_1Int1Boolean(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//��ѯͨ���ķ�����ʾ
scpi_result_t CHAN_INVQ(scpi_t * context)
{
    //ERROR_XY_MODEQ;
    int param1;
    if (!SCPI_ParamChoice(context, allCh, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    //deal_CHAN_INVQ(param1,context);
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}


//����ͨ���Ĵ�������
scpi_result_t CHAN_BAND(scpi_t * context)
{
    //ERROR_XY_MODE;
    int paramCount=SCPI_ParamCount(context);
    int param1=0;
    if (paramCount==3 || paramCount==2){
         if (!SCPI_ParamChoice(context, allCh, &param1, true)) {
             dealCallBack_ParamError(context);
            return SCPI_RES_ERR;
        }

    int param2;//param2��������chan_band[]
    if (!SCPI_ParamChoice(context, chan_band, &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }

    double param3=0;
    if (param2==1 || param2==2){
        if (!SCPI_ParamDouble(context, &param3, true)) {
            dealCallBack_ParamError(context);
            return SCPI_RES_ERR;
        }
    }
    //deal_CHAN_BAND(param1,param2,param3);
        setParam_2Int1Double(context->env,context->param,param1,param2,param3);
        dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    }
    return SCPI_RES_OK;
}

/**
 *��ѯͨ���Ĵ�������
 *�¾�Э����ͨ�����޹ء�Ĭ��ch1
*/
scpi_result_t CHAN_BANDQ(scpi_t * context)
{    
    //ERROR_XY_MODEQ;
    int param1=0;
    if (!SCPI_ParamChoice(context, allCh, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    //deal_CHAN_BANDQ(param1,context);
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t  CHAN_BAND_VALUEQ(scpi_t* context){
    return CHAN_BANDQ(context);
}


//����ͨ����̽������

scpi_result_t CHAN_PRTY(scpi_t * context)
{
    //ERROR_XY_MODE;
    int param1;
    if (!SCPI_ParamChoice(context, allCh, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    int param2;
    if (!SCPI_ParamChoice(context, chan_prty, &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    //deal_CHAN_PRTY(param1,param2);
    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//��ѯͨ����̽������
scpi_result_t CHAN_PRTYQ(scpi_t * context)
{
    //ERROR_XY_MODEQ;
    int param1;
    if (!SCPI_ParamChoice(context, allCh, &param1, true)) {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR PARAM!");
        return SCPI_RES_ERR;
    }
    //deal_CHAN_PRTYQ(param1,context);
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//����̽ͷ��˥����
scpi_result_t CHAN_PROB(scpi_t * context)
{
    //ERROR_XY_MODE;
    int param1;
    if (!SCPI_ParamChoice(context, allCh, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    double  param2;
    if (!SCPI_ParamDouble(context, &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_1Int1Double(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    //deal_CHAN_PROB(param1,param2);
    return SCPI_RES_OK;
}

//��ѯ̽ͷ��˥����
scpi_result_t CHAN_PROBQ(scpi_t * context)
{
    //ERROR_XY_MODEQ;
    int param1;
    if (!SCPI_ParamChoice(context, allCh, &param1, true)) {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR PARAM!");
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    //deal_CHAN_PROBQ(param1,context);
    return SCPI_RES_OK;
}



//����ͨ��������Ϸ�ʽ
scpi_result_t CHAN_COUP(scpi_t * context)
{
    //ERROR_XY_MODE;
    int param1;
    if (!SCPI_ParamChoice(context, allCh, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    int param2;
    if (!SCPI_ParamChoice(context, chan_coup, &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    //deal_CHAN_COUP(param1,param2);
    return SCPI_RES_OK;
}

//��ѯͨ��������Ϸ�ʽ
scpi_result_t CHAN_COUPQ(scpi_t * context)
{
    //ERROR_XY_MODEQ;
    int param1;
    if (!SCPI_ParamChoice(context, allCh, &param1, true)) {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR PARAM!");
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    //deal_CHAN_COUPQ(param1,context);
    return SCPI_RES_OK;
}



//����ͨ���������迹
scpi_result_t CHAN_INP(scpi_t * context)
{
    int param1;
    if (!SCPI_ParamChoice(context, allCh, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    int param2;
    if (!SCPI_ParamChoice(context, chan_inp, &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//��ѯͨ���������迹
scpi_result_t CHAN_INPQ(scpi_t * context)
{
    int param1;
    if (!SCPI_ParamChoice(context, allCh, &param1, true)) {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR PARAM!");
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;

}

scpi_result_t CHAN_PLUS_EXT(scpi_t * context)//����ָ��ͨ��������ʾ�Ĵ�ֱ��λ
{
    //ERROR_XY_MODE;
    int param1;
    if (!SCPI_ParamChoice(context, allCh, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    int param2;
    if (!SCPI_ParamInt(context, &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    //deal_CHAN_SCAL_PLUS(param1,param2);
    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//����ָ��ͨ��������ʾ�Ĵ�ֱ��λ
scpi_result_t CHAN_EXT(scpi_t * context)
{
    //ERROR_XY_MODE;
    int param1;
    if (!SCPI_ParamChoice(context, allCh, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    double param2;
    if (!SCPI_ParamDouble(context, &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_1Int1Double(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    //deal_CHAN_SCAL(param1,param2);
    return SCPI_RES_OK;
}

//��ѯָ��ͨ��������ʾ�Ĵ�ֱ��λ
scpi_result_t CHAN_EXTQ(scpi_t * context)
{
    //ERROR_XY_MODEQ;
    int param1;
    if (!SCPI_ParamChoice(context, allCh, &param1, true)) {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR PARAM!");
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    //deal_CHAN_SCALQ(param1,context);
    return SCPI_RES_OK;
}

//����ָ��ͨ��������ʾ�Ĵ�ֱƫ��
scpi_result_t CHAN_PLUS_POS(scpi_t * context)
{
    //ERROR_XY_MODE;
    int param1;
    if (!SCPI_ParamChoice(context, allCh, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    int param2;
    if (!SCPI_ParamInt(context, &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    //deal_CHAN_POS_PLUS(param1,param2);
    return SCPI_RES_OK;
}

//����ָ��ͨ��������ʾ�Ĵ�ֱƫ��
scpi_result_t CHAN_POS(scpi_t * context)
{
    //ERROR_XY_MODE;
    int param1;
    if (!SCPI_ParamChoice(context, allCh, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    double param2;
    if (!SCPI_ParamDouble(context, &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_1Int1Double(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    //deal_CHAN_POS(param1,param2);
    return SCPI_RES_OK;
}

//��ѯָ��ͨ��������ʾ�Ĵ�ֱƫ��
scpi_result_t CHAN_POSQ(scpi_t * context)
{
    //ERROR_XY_MODEQ;
    int param1;
    if (!SCPI_ParamChoice(context, allCh, &param1, true)) {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR PARAM!");
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    //deal_CHAN_POSQ(param1,context);
    return SCPI_RES_OK;
}

/** �򿪻�ر�ָ��ͨ���Ĵ�ֱ��λ΢������ */
scpi_result_t CHAN_VERN(scpi_t * context)
{
    //Q_UNUSED(context);
    int param1;
    if (SCPI_ParamChoice(context, allCh,&param1,true)==false) {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR PARAM!");
        return SCPI_RES_ERR;
    }
    bool param2;
    if (!SCPI_ParamBool(context,&param2,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_1Int1Boolean(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

/** ��ѯָ��ͨ���Ĵ�ֱ��λ΢�����ܵĴ򿪻�ر� */
scpi_result_t CHAN_VERNQ(scpi_t * context)
{
    int param1;
    if (SCPI_ParamChoice(context,allCh, &param1,true)==false) {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR PARAM!");
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

scpi_result_t CHAN_DELAY(scpi_t* context){
    int param1;
    if (SCPI_ParamChoice(context,allCh, &param1,true)==false) {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR PARAM!");
        return SCPI_RES_ERR;
    }
    double param2;
    if (SCPI_ParamDouble(context,&param2,true)==false){
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR PARAM!");
        return SCPI_RES_ERR;
    }
    setParam_1Int1Double(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t CHAN_DELAYQ(scpi_t* context){
    int param1;
    if (SCPI_ParamChoice(context,allCh, &param1,true)==false) {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR PARAM!");
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t CHAN_OFFSET(scpi_t* context){
    int param1;
    if (SCPI_ParamChoice(context,allCh, &param1,true)==false) {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR PARAM!");
        return SCPI_RES_ERR;
    }
    double param2;
    if (SCPI_ParamDouble(context,&param2,true)==false){
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR PARAM!");
        return SCPI_RES_ERR;
    }
    setParam_1Int1Double(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t CHAN_OFFSETQ(scpi_t* context){
    int param1;
    if (SCPI_ParamChoice(context,allCh, &param1,true)==false) {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR PARAM!");
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

scpi_result_t CHAN_COUNTQ(scpi_t* context){
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

scpi_result_t CHAN_VREF(scpi_t * context)//设置垂直展开基准
{
    int param1;
    if (!SCPI_ParamChoice(context, allCh, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    int param2;
    if (!SCPI_ParamChoice(context,chan_vref, &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    //deal_CHAN_POS(param1,param2);
    return SCPI_RES_OK;
}
scpi_result_t CHAN_VREFQ(scpi_t * context)//查询垂直展开基准
{
    int param1;
    if (!SCPI_ParamChoice(context, allCh, &param1, true)) {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR PARAM!");
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    //deal_CHAN_POSQ(param1,context);
    return SCPI_RES_OK;
}
scpi_result_t CHAN_LAB(scpi_t* context){
    int param1;
    if (!SCPI_ParamChoice(context, allCh, &param1, true)) {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR PARAM!");
        return SCPI_RES_ERR;
    }

    const char* param = NULL;
    size_t len=0;
    if  (!SCPI_ParamString(context,&param,&len,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    std::string param2(param,len);
    setParam_1Int1String(context->env,context->param,param1,param2.c_str());
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t CHAN_LABQ(scpi_t* context){
    int param1;
    if (!SCPI_ParamChoice(context, allCh, &param1, true)) {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR PARAM!");
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

scpi_result_t CHAN_LAB_CLEAR(scpi_t* context){
    int param1;
    if (!SCPI_ParamChoice(context, allCh, &param1, true)) {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR PARAM!");
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

scpi_result_t CHAN_CURR(scpi_t * context){
    int param1;
    if (!SCPI_ParamChoice(context, allCh, &param1, true)) {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR PARAM!");
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t CHAN_CURRQ(scpi_t * context){
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t CHAN_BAND_IS200MQ(scpi_t * context)
{
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t CHAN_BAND_MAXQ(scpi_t * context){
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t CHAN_PROBE_INFOQ(scpi_t* context)
{
    int param1;
    if (!SCPI_ParamChoice(context, allCh, &param1, true)) {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR PARAM!");
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

/**
 *����ִ�в���
 *
 */

//ͨ���򿪻�ر�
scpi_result_t deal_CHAN_DISP(int chNo,bool param2)
{
    //ERROR_XY_MODE;
//    CH_IDX idx = (CH_IDX)getCh(chNo);
//    if(!Channel::isDynamicCh(idx)) {
//        return SCPI_RES_ERR;
//    }
//    //SCPI_CLOSE_MENU;
//    IModuleChannel *imc = IModuleChannel::createInstance();
//    if(param2 != imc->isChOpened(idx))
//    {
//        gMainWindow->getChannelPannelEx()->dealChBtnClicked(idx);
//    }


    return SCPI_RES_OK;
}

//�򿪻�ر�ͨ���ķ�����ʾ
scpi_result_t deal_CHAN_INV(int chNo,bool param2)
{
    //ERROR_XY_MODE;
//    CH_IDX idx = (CH_IDX)getCh(chNo);
//    if(!Channel::isDynamicCh(idx)) {
//        return SCPI_RES_ERR;
//    }
    ////SCPI_CLOSE_MENU;
//    CChannelSetMsg *csetm = CChannelSetMsg::Instance();
//    CChannelGetMsg *cgetm = CChannelGetMsg::Instance();
//    paramInverse inverseValue(false, idx);
//    cgetm->exec(CH_INVERSE, &inverseValue);
//    if(inverseValue.isEnabled == true && param2 == false)
//    {
//        inverseValue.isEnabled = false;
//        csetm->exec(CH_INVERSE, &inverseValue);
//        gMainWindow->GetMenuChannel()->updateInv_Level(idx);
//    }
//    else if(inverseValue.isEnabled == false && param2 == true)
//    {
//        inverseValue.isEnabled = true;
//        csetm->exec(CH_INVERSE, &inverseValue);
//        gMainWindow->GetMenuChannel()->updateInv_Level(idx);
//    }
    return SCPI_RES_OK;
}

//����ͨ����̽������
scpi_result_t deal_CHAN_PRTY(int chNo,int param2)
{
    //ERROR_XY_MODE;
//    CH_IDX idx = (CH_IDX)getCh(chNo);
//    if(!Channel::isDynamicCh(idx)) {
//        return SCPI_RES_ERR;
//    }
    //SCPI_CLOSE_MENU;
//    CChannelSetMsg *csetm = CChannelSetMsg::Instance();
//    CChannelGetMsg *cgetm = CChannelGetMsg::Instance();
//    paramProbeType probeType(idx, Device::VUI_VOL);
//    cgetm->exec(CH_PROBETYPE, &probeType);
//
//    if(probeType.type == Device::VUI_VOL
//    && param2 == 0)
//    {
//        probeType.type = Device::VUI_CURRENT;
//        csetm->exec(CH_PROBETYPE, &probeType);
//        CGearToControlUpdateMsg::Instance()->exec(UPDATE_TRIGSTATE, NULL);
//        gMainWindow->getChannelPannelEx(0)->refreshButtonState(probeType.ch);
//    }
//    else if(probeType.type == Device::VUI_CURRENT
//         && param2 == 1)
//    {
//        probeType.type = Device::VUI_VOL;
//        csetm->exec(CH_PROBETYPE, &probeType);
//        CGearToControlUpdateMsg::Instance()->exec(UPDATE_TRIGSTATE, NULL);
//        gMainWindow->getChannelPannelEx(0)->refreshButtonState(probeType.ch);
//    }
    return SCPI_RES_OK;
}

//����̽ͷ��˥����
scpi_result_t deal_CHAN_PROB(int chNo,double param2)
{
    const double param2bytes[]={0.001,0.002,0.005,0.01,0.02,0.05,
                0.1,0.2,0.5,1,2,5,10,20,50,100,200,500,1000,5000,
                               10000};
    //ERROR_XY_MODE;
//    CH_IDX idx = (CH_IDX)getCh(chNo);
//    if(!Channel::isDynamicCh(idx)) {
//        return SCPI_RES_ERR;
//    }
//#if 1
//    bool flag=false;
//
//    for(int i=0;i<(int)(sizeof(param2bytes)/sizeof(double));i++)
//    {
//        if (param2==param2bytes[i])
//        {
//            flag=true;break;
//        }
//    }
//
//    if (flag==false)
//    {
//        return SCPI_RES_ERR;}
//#else
//    //���趨��̽�뱶������У�飬����ȷ��Χֵ����Ч
//    bool bError = false;
//    if(param2 > 10000) param2 = 10000;
//    else if(param2 < 0.001) param2 = 0.001;
//    else
//    {
//        int param = param2*1000;
//        while(param > 9)
//        {
//            if(!(param % 10)) param = param/10;
//            else break;
//        }
//        switch(param)
//        {
//            case 1:
//            case 2:
//            case 5: bError = false; break;
//            default: bError = true; break;
//        }
//    }
//    if(bError)
//    {
//        DEBUG_RETURN("ERROR PARAM!");
//        return SCPI_RES_ERR;
//    }
//#endif
//    //SCPI_CLOSE_MENU;
//    CChannelSetMsg *csetm = CChannelSetMsg::Instance();
//    paramProbeRadio pbValue;
//    pbValue.ch = idx;
//    pbValue.radio = param2;
//    csetm->exec(CH_PROBEMUTI, &pbValue);
//    gMainWindow->getChannelPannelEx(0)->refreshButtonState(pbValue.ch);
    return SCPI_RES_OK;
}


//����ͨ��������Ϸ�ʽ

scpi_result_t deal_CHAN_COUP(int chNo,int param2)
{

    //ERROR_XY_MODE;
//    CH_IDX idx = (CH_IDX)getCh(chNo);
//    if(!Channel::isDynamicCh(idx)) {
//        return SCPI_RES_ERR;
//    }
//    //SCPI_CLOSE_MENU;
//    CChannelSetMsg *csetm = CChannelSetMsg::Instance();
//    paramCouple coupleValue(Device::CH_COUP_DC, idx);
//    switch(param2)
//    {
//    case 0:
//        coupleValue.couple = CH_COUP_DC;
//        break;
//    case 1:
//        coupleValue.couple = CH_COUP_AC;
//        break;
//    case 2:
//        coupleValue.couple = CH_COUP_GND;
//        break;
//    default:
//        break;
//    }
//    csetm->exec(CH_COUPLE, &coupleValue);
    return SCPI_RES_OK;
}


/** ����ͨ��������ʾ�Ĵ�ֱ��λ δʵ��*/
scpi_result_t deal_CHAN_SCAL(int chNo,double param2)
{
    //ERROR_XY_MODE;
//    CH_IDX ch = (CH_IDX)getCh(chNo);
//    if(!Channel::isDynamicCh(ch)) {
//        return SCPI_RES_ERR;
//    }
//    double real = param2/IModuleDevice::instance()->probeRatio(false, ch);
//    _YDANG_CL after = (_YDANG_CL)(IDescVertical::vScaleToFloat(real));
//
//    if(real - IModuleDevice::instance()->vScale(after) > 1e-4)
//    {//�Ǳ�׼��λ�趨ֵ������:�Ǳ굵ʱafterΪDANG_MIN������real������һ������1e-4
//        //DEBUG_RETURN("ERROR PARAM!");
//        return SCPI_RES_ERR;
//    }
//
//    if(IModuleChannel::createInstance()->isChOpened(ch)) {
//        _YDANG_CL now = IModuleDevice::instance()->vScaleId(false, ch);
//        CGearToVmVMsg *gtvmvm = CGearToVmVMsg::Instance();
//        gtvmvm->exec(after-now, ch);
//    }
    return SCPI_RES_OK;
}
scpi_result_t deal_CHAN_SCAL_PLUS(int chNo,int param2)
{
    //ERROR_XY_MODE;
//    CH_IDX ch = (CH_IDX)getCh(chNo);
//    if(!Channel::isDynamicCh(ch)) {
//        return SCPI_RES_ERR;
//    }
//
//    if(IModuleChannel::createInstance()->isChOpened(ch)) {
//        CGearToVmVMsg *gtvmvm = CGearToVmVMsg::Instance();
//        gtvmvm->exec(param2, ch);
//    }
    return SCPI_RES_OK;
}

/** ����ͨ��������ʾ�Ĵ�ֱƫ�� */
scpi_result_t deal_CHAN_POS_PLUS(int chNo,int param2)
{
    //ERROR_XY_MODE;
//    CH_IDX ch = (CH_IDX)getCh(chNo);
//    if(!Channel::isDynamicCh(ch)) {
//        return SCPI_RES_ERR;
//    }
//    if(IModuleChannel::createInstance()->isChOpened(ch)) {
//
//        CChannelDragMsg::Instance()->exec_ex(param2, ch);
//    }
    return SCPI_RES_OK;
}

/** ����ͨ��������ʾ�Ĵ�ֱƫ�� */
scpi_result_t deal_CHAN_POS(int chNo,double param2)
{
    //ERROR_XY_MODE;
//    CH_IDX ch = (CH_IDX)getCh(chNo);
//    if(!Channel::isDynamicCh(ch)) {
//        return SCPI_RES_ERR;
//    }
//    if(IModuleChannel::createInstance()->isChOpened(ch)) {
//        IModuleDevice *md = IModuleDevice::instance();
//        float vValue = md->vScale(md->vScaleId(false, ch)) * md->probeRatio(false, ch)/50;
//        int afterPos = param2/vValue;
//        int nowPos = md->vPosOfZero(false, ch);
//        if(md->DispZoom(0)) {
//            afterPos = IDescVertical::vPosOfZeroConvesionToZoomFanda(afterPos);
//            nowPos = IDescVertical::vPosOfZeroConvesionToZoomFanda(nowPos);
//        }
//        CChannelDragMsg::Instance()->exec_ex(afterPos-nowPos, ch);
//    }
    return SCPI_RES_OK;
}


/** �򿪻�ر�ָ��ͨ���Ĵ�ֱ��λ΢������ */
scpi_result_t deal_CHAN_VERN(int chNo,bool param2)
{
    //Q_UNUSED(chNo);
    //Q_UNUSED(param2);
    return SCPI_RES_OK;
}

/** ��ȡͨ�����ε���λ�� */
scpi_result_t deal_CHAN_PC(int chNo,int param2)
{
    //Q_UNUSED(chNo);
    //Q_UNUSED(param2);
    return SCPI_RES_OK;
}
/**
  * ���ô���
 * @brief deal_CHAN_BAND
 * @param chNo
 * @param param2
 * @param param3
 * @return
 */
scpi_result_t deal_CHAN_BAND(int chNo,int param2,int param3){
    //ERROR_XY_MODE;
//    CH_IDX idx = (CH_IDX)getCh(chNo);
//    if(!Channel::isDynamicCh(idx)) {
//        return SCPI_RES_ERR;
//    }
//    CChannelGetMsg *cgetm = CChannelGetMsg::Instance();
//    paramBandWidth bndwdValue(OscilloUi::BANDTH_FULL, 30000, idx);
//    cgetm->exec(CH_BANDWIDTH, &bndwdValue);
//    bool bChange = false;
//    //SCPI_CLOSE_MENU;
//    double min_fc = 30000;
//    double max_fc = IDevConfig::Instance()->GetBandWidth() * 1e6;
//    if(param3 < min_fc) param3 = min_fc;
//    else if(param3 > max_fc) param3 = max_fc;
//
//    switch(param2)
//    {
//        case 0:
//        {
//            if(bndwdValue.bandWidth != OscilloUi::BANDTH_FULL)
//            {
//                bndwdValue.bandWidth = OscilloUi::BANDTH_FULL;
//                bChange = true;
//            }
//            break;
//        }
//        case 1:
//        {
//            if(bndwdValue.bandWidth != OscilloUi::BANDTH_20M)
//            {
//                bndwdValue.bandWidth = OscilloUi::BANDTH_20M;
//                bChange = true;
//            }
//            break;
//        }
//        case 2:
//        {
//            if(bndwdValue.bandWidth != OscilloUi::BANDTH_HIGH
//            || qAbs(bndwdValue.fc - param3) > 0.9)
//            {
//                bndwdValue.bandWidth = OscilloUi::BANDTH_HIGH;
//                bndwdValue.fc = param3;
//                IModuleDevice::instance()->ditFiltering_Fc(idx,false) = param3;
//                bChange = true;
//            }
//            break;
//        }
//        case 3:
//        {
//            if(bndwdValue.bandWidth != OscilloUi::BANDTH_LOW
//            || qAbs(bndwdValue.fc - param3) > 0.9)
//            {
//                bndwdValue.bandWidth = OscilloUi::BANDTH_LOW;
//                bndwdValue.fc = param3;
//                IModuleDevice::instance()->ditFiltering_Fc(idx,false) = param3;
//                bChange = true;
//            }
//            break;
//        }
//        default: break;
//    }
//    if(bChange)
//    {
//        if(gMainWindow->smWidget->isEnabled()
//        && gMainWindow->smWidget->SubMenuId() == SUBM_BANDTH)
//            gMainWindow->smWidget->OnShow(false, SUBM_BANDTH);//�ر��Ӳ˵�
//        MenuChannelFrame *& mCh = gMainWindow->GetMenuChannel();
//        if(mCh->GetCurChID() != idx) mCh->SetCurChID(idx);
//        mCh->updateBand(bndwdValue.bandWidth);
//    }

    return SCPI_RES_OK;
}

/**
  *�����迹
 * @brief deal_ChAN_INP
 * @param chNo
 * @param param2
 * @return
 */
scpi_result_t deal_ChAN_INP(int chNo,int param2){
    //Q_UNUSED(chNo);
    //Q_UNUSED(param2);
    return SCPI_RES_OK;
}



/**
 *��ѯͨ���򿪻�ر�
 */
scpi_result_t deal_CHAN_DISPQ(int chNo,scpi_t * context)
{
    //ERROR_XY_MODEQ;
//    CH_IDX idx = (CH_IDX)getCh(chNo);
//    if(!Channel::isDynamicCh(idx)) {
//        DEBUG_RETURN("ERROR CH!");
//        return SCPI_RES_ERR;
//    }
//
//    IModuleChannel *imc = IModuleChannel::createInstance();
//    SCPI_ResultBool(context,imc->isChOpened(idx));
    return SCPI_RES_OK;
}

/**
 *��ѯͨ���ķ�����ʾ
 */
scpi_result_t deal_CHAN_INVQ(int chNo,scpi_t * context)
{
    //ERROR_XY_MODEQ;
//    CH_IDX idx = (CH_IDX)getCh(chNo);
//    if(!Channel::isDynamicCh(idx)) {
//        return SCPI_RES_ERR;
//    }
//
//    CChannelGetMsg *cgetm = CChannelGetMsg::Instance();
//    paramInverse inverseValue(false, idx);
//    cgetm->exec(CH_INVERSE, &inverseValue);
//    SCPI_ResultBool(context,inverseValue.isEnabled);
    return SCPI_RES_OK;
}

/**
 *��ѯͨ����̽������
 */

scpi_result_t deal_CHAN_PRTYQ(int chNo,scpi_t * context)
{
    //ERROR_XY_MODEQ;
//    CH_IDX idx = (CH_IDX)getCh(chNo);
//    if(!Channel::isDynamicCh(idx)) {
//        DEBUG_RETURN("ERROR CH!");
//        return SCPI_RES_ERR;
//    }
//
//    CChannelGetMsg *cgetm = CChannelGetMsg::Instance();
//    paramProbeType probeType(idx, Device::VUI_VOL);
//    cgetm->exec(CH_PROBETYPE, &probeType);
//
//    if(Device::VUI_VOL == probeType.type)
//    {
//        SCPI_ResultString(context,chan_prty[1]);
//    }
//    else if(Device::VUI_CURRENT == probeType.type)
//    {
//        SCPI_ResultString(context,chan_prty[0]);
//    }
//    else
//    {
//        DEBUG_RETURN("UNKNOW ERROR!");
//    }
    return SCPI_RES_OK;
}


/**
 *��ѯ̽ͷ��˥����
*/


scpi_result_t deal_CHAN_PROBQ(int chNo,scpi_t * context)
{
    //ERROR_XY_MODEQ;
//    CH_IDX idx = (CH_IDX)getCh(chNo);
//    if(!Channel::isDynamicCh(idx)) {
//        DEBUG_RETURN("ERROR CH!");
//        return SCPI_RES_ERR;
//    }
//
//    CChannelGetMsg *cgetm = CChannelGetMsg::Instance();
//    paramProbeRadio pbValue;
//    pbValue.ch = idx;
//    cgetm->exec(CH_PROBEMUTI, &pbValue);
//    SCPI_ResultDouble(context,pbValue.radio);
    return SCPI_RES_OK;
}

/**
 *��ѯͨ��������Ϸ�ʽ
*/

scpi_result_t deal_CHAN_COUPQ(int chNo,scpi_t * context)
{
    //ERROR_XY_MODEQ;
//    CH_IDX idx = (CH_IDX)getCh(chNo);
//    if(!Channel::isDynamicCh(idx)) {
//        DEBUG_RETURN("ERROR CH!");
//        return SCPI_RES_ERR;
//    }
//
//    CChannelGetMsg *cgetm = CChannelGetMsg::Instance();
//    paramCouple coupleValue(CH_COUP_DC, idx);
//    cgetm->exec(CH_COUPLE, &coupleValue);
//    switch(coupleValue.couple)
//    {
//    case CH_COUP_DC:
//        SCPI_ResultString(context,chan_coup[0]);
//        break;
//    case CH_COUP_AC:
//        SCPI_ResultString(context,chan_coup[1]);
//        break;
//    case CH_COUP_GND:
//        SCPI_ResultString(context,chan_coup[2]);
//        break;
//    default:
//        break;
//    }
    return SCPI_RES_OK;
}


/**
 *��ѯͨ��������ʾ�Ĵ�ֱ��λ
*/

scpi_result_t deal_CHAN_SCALQ(int chNo,scpi_t * context)
{
    //ERROR_XY_MODEQ;
//    CH_IDX ch = (CH_IDX)getCh(chNo);
//    if(!Channel::isDynamicCh(ch)) {
//        //DEBUG_RETURN("ERROR CH!");
//        return SCPI_RES_ERR;
//    }
//
//    if(IModuleChannel::createInstance()->isChOpened(ch)) {
//        Device::IModuleDevice *md = Device::IModuleDevice::instance();
//        float vValue = md->vScale(md->vScaleId(false, ch)) * md->probeRatio(false, ch);
//        SCPI_ResultDouble(context, vValue);
//    }
//    else
//    {
//       // DEBUG_RETURN("ERROR CH NOT OPEN!");
//        return SCPI_RES_ERR;
//    }

    return SCPI_RES_OK;
}

/**
 *��ѯͨ��������ʾ�Ĵ�ֱƫ��
*/

scpi_result_t deal_CHAN_POSQ(int chNo,scpi_t * context)
{
    //ERROR_XY_MODEQ;
//    CH_IDX ch = (CH_IDX)getCh(chNo);
//    if(!Channel::isDynamicCh(ch)) {
//        DEBUG_RETURN("ERROR CH!");
//        return SCPI_RES_ERR;
//    }
//
//    if(IModuleChannel::createInstance()->isChOpened(ch)) {
//        IModuleChannel *mc = IModuleChannel::createInstance();
//        int pos = mc->getGeographInfo(ch);
//        if(IModuleDevice::instance()->DispZoom(0)) {
//            pos = IDescVertical::ZoomFandaConvesionTovPosOfZero(pos);
//        }
//        IModuleDevice *md = IModuleDevice::instance();
//        float vValue = md->vScale(md->vScaleId(false, ch))
//                       * md->probeRatio(false, ch)/50*pos;
//        SCPI_ResultDouble(context, vValue);
//    }
//    else
//    {
//        DEBUG_RETURN("ERROR CH NOT OPEN!");
//        return SCPI_RES_ERR;
//    }

    return SCPI_RES_OK;
}

/**
 *��ѯָ��ͨ���Ĵ�ֱ��λ΢������
*/

scpi_result_t deal_CHAN_VERNQ(int chNo,scpi_t * context)
{
    //Q_UNUSED(chNo);
    SEND_RECEIVE_CMD(context);
    return SCPI_RES_OK;
}

/**
 *��ѯͨ�����ε���λ��״̬
 */

scpi_result_t deal_CHAN_PCQ(int chNo,scpi_t * context)
{
    //Q_UNUSED(chNo);
    //Q_UNUSED(context);
    return SCPI_RES_OK;
}

/**
  * ��ѯͨ������
 * @brief deal_CHAN_BANDQ
 * @param chNo
 * @param context
 * @return ��ѯ״̬
 */
scpi_result_t deal_CHAN_BANDQ(int chNo,scpi_t* context){
    //ERROR_XY_MODEQ;
//    CH_IDX idx = (CH_IDX)getCh(chNo);
//    if(!Channel::isDynamicCh(idx)) {
//        DEBUG_RETURN("ERROR CH!");
//        return SCPI_RES_ERR;
//    }
//
//    CChannelGetMsg *cgetm = CChannelGetMsg::Instance();
//    paramBandWidth bndwdValue(OscilloUi::BANDTH_FULL, 30000, idx);
//    cgetm->exec(CH_BANDWIDTH, &bndwdValue);
//
//    if(OscilloUi::BANDTH_FULL == bndwdValue.bandWidth)
//    {
//        SCPI_ResultString(context, chan_band[0]);
//    }
//    else if(OscilloUi::BANDTH_20M == bndwdValue.bandWidth)
//    {
//        SCPI_ResultString(context, chan_band[1]);
//    }
//    else if(OscilloUi::BANDTH_HIGH == bndwdValue.bandWidth)
//    {
//        SCPI_ResultString(context, chan_band[2]);
//    }
//    else if(OscilloUi::BANDTH_LOW == bndwdValue.bandWidth)
//    {
//        SCPI_ResultString(context, chan_band[3]);
//    }
//    else
//    {
//        DEBUG_RETURN("NOTHing");
//        return SCPI_RES_ERR;
//    }
//    SCPI_ResultDouble(context, bndwdValue.fc);

    return SCPI_RES_OK;
}

/**
  *��ѯ�迹
 * @brief deal_ChAN_INPQ
 * @param chNo
 * @param param2
 * @return
 */
scpi_result_t deal_ChAN_INPQ(int chNo,int param2){
    //Q_UNUSED(chNo);
    //Q_UNUSED(param2);
    return SCPI_RES_OK;
}
