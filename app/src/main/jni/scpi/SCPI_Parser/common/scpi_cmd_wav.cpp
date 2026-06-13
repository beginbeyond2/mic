
#include "scpi_cmd_wav.h"
#include <stdio.h>
#include <stdlib.h>
#include "scpi_help.h"
#include "../../SCPICommandCallBackJava.h"
#include "../../Log.h"


scpi_result_t  WAV_DATA(scpi_t * context)
{
//    Q_UNUSED(context);
//    ERROR_XY_MODE;
//    char szBuffer[4096];
//    int rlen  = CWavefrom::Instance()->ReadData(szBuffer,4096);
//    if(rlen>0)
//    {
//        SCPI_ResultData(context,szBuffer,rlen);
//        return SCPI_RES_OK;
//    }
//    else
//    {
//        SCPI_ResultData(context,szBuffer,0);
//    }
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t  WAV_DATA_BINQ(scpi_t* context){
    return WAV_DATA(context);
}
scpi_result_t  WAV_DATA_HEXQ(scpi_t* context){
    return WAV_DATA(context);
}
scpi_result_t  WAV_DATA_ASCIIQ(scpi_t* context){
    return WAV_DATA(context);
}

scpi_result_t  WAV_MODE(scpi_t * context)
{
//    Q_UNUSED(context);
//    ERROR_XY_MODE;
    int param1;
    if (!SCPI_ParamChoice(context, WaveMode, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
//    CWavefrom::Instance()->SetMode((CWavefrom::WAVE_MODE)param1);
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
//��ѯ���εĶ�ȡģʽ
scpi_result_t  WAV_MODEQ(scpi_t * context)
{
//    Q_UNUSED(context);
//    ERROR_XY_MODE;
//    int idx = CWavefrom::Instance()->GetMode();
//
//    SCPI_ResultString(context,WaveMode[idx]);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//��ѯȫ���Ĳ��β���
scpi_result_t  WAV_PREQ(scpi_t * context)
{
//    Q_UNUSED(context);
//    ERROR_XY_MODE;
//    int idx = CWavefrom::Instance()->GetMode();
//    SCPI_ResultString(context,WaveMode[idx]);
//    idx = CWavefrom::Instance()->GetWAVLEN();
//    SCPI_ResultInt(context,idx);
//    double dx = CWavefrom::Instance()->GetXINCQ();
//    SCPI_ResultDouble(context,dx);
//    dx = CWavefrom::Instance()->GetXORQ();
//    SCPI_ResultDouble(context,dx);
//    dx = CWavefrom::Instance()->GetXREFQ();
//    SCPI_ResultDouble(context,dx);
//    dx = CWavefrom::Instance()->GetYINCQ();
//    SCPI_ResultDouble(context,dx);
//    dx = CWavefrom::Instance()->GetYORQ();
//    SCPI_ResultDouble(context,dx);
//    dx = CWavefrom::Instance()->GetYREFQ();
//    SCPI_ResultDouble(context,dx);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
//���ò��ζ�ȡ��ͨ��Դ
scpi_result_t  WAV_SOUR(scpi_t * context)
{
//    Q_UNUSED(context);
//    ERROR_XY_MODE;
    int param1;
    if (!SCPI_ParamChoice(context, allCh, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
//    if(param1>=0 && param1<4)
//    {
//        CWavefrom::Instance()->SetSource(param1);
//        return SCPI_RES_OK;
//    }

    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_ERR;
}
//��ѯ���ζ�ȡ��ͨ��Դ
scpi_result_t  WAV_SOURQ(scpi_t * context)
{
//    Q_UNUSED(context);
//    ERROR_XY_MODE;
//    int idx = CWavefrom::Instance()->GetSource();
    const char * strChan[]={
        "CHAN1",
        "CHAN2",
        "CHAN3",
        "CHAN4"
    };
//    SCPI_ResultString(context,strChan[idx]);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
const char* format[]={
      "WORD",
      "BYTE",
      "ASCii",
      NULL
};
scpi_result_t  WAV_FORMAT(scpi_t * context){
    int param1;
    if (!SCPI_ParamChoice(context,format,&param1,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t  WAV_FORMATQ(scpi_t * context){
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
//�����ڴ��в��α���ȡ����ʼλ��
scpi_result_t  WAV_STAR(scpi_t * context)
{
//    Q_UNUSED(context);
//    ERROR_XY_MODE;
    int param;
    if (!SCPI_ParamInt(context, &param, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
//    CWavefrom::Instance()->SetStartPos(param);
    setParam_1Int(context->env,context->param,param);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
//��ѯ�ڴ��в��α���ȡ����ʼλ��
scpi_result_t  WAV_STARQ(scpi_t * context)
{
//    Q_UNUSED(context);
//    ERROR_XY_MODE;
//    int idx = CWavefrom::Instance()->GetStartPos();
//    SCPI_ResultInt(context,idx);

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
//�����ڴ��в��α���ȡ��ֹͣλ��
scpi_result_t  WAV_STOP(scpi_t * context)
{
//    Q_UNUSED(context);
//    ERROR_XY_MODE;
    int param;
    if (!SCPI_ParamInt(context, &param, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
//    CWavefrom::Instance()->SetStopPos(param);
    setParam_1Int(context->env,context->param,param);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
//��ѯ�ڴ��в��α���ȡ��ֹͣλ��
scpi_result_t  WAV_STOPQ(scpi_t * context)
{
//    Q_UNUSED(context);
//    ERROR_XY_MODE;
//    int idx = CWavefrom::Instance()->GetStopPos();
//    SCPI_ResultInt(context,idx);

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
//��ѯָ��Դx���������������ʱ���
scpi_result_t  WAV_XINCQ(scpi_t * context)
{
//    Q_UNUSED(context);
//    ERROR_XY_MODE;
//    double dx = CWavefrom::Instance()->GetXINCQ();
//    SCPI_ResultDouble(context,dx);

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
//��ѯָ��Դx����Ӵ����㵽�ο�ʱ���׼��ʱ��
scpi_result_t  WAV_XORQ(scpi_t * context)
{
//    Q_UNUSED(context);
//    double dx = CWavefrom::Instance()->GetXORQ();
//    SCPI_ResultDouble(context,dx);

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
//��ѯָ��Դx���������ݵ�Ĳο�ʱ���׼
scpi_result_t  WAV_XREFQ(scpi_t * context)
{
//    Q_UNUSED(context);
//    ERROR_XY_MODE;
//    double dx = CWavefrom::Instance()->GetXREFQ();
//    SCPI_ResultDouble(context,dx);

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
//��ѯָ��Դy���������������
scpi_result_t  WAV_YINCQ(scpi_t * context)
{
//    Q_UNUSED(context);
//    ERROR_XY_MODE;
//    double dx = CWavefrom::Instance()->GetYINCQ();
//    SCPI_ResultDouble(context,dx);

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
//��ѯָ��Դy����Ӵ����㵽�ο�ʱ���׼��ʱ��
scpi_result_t  WAV_YORQ(scpi_t * context)
{
//    Q_UNUSED(context);
//    ERROR_XY_MODE;
//    double dx = CWavefrom::Instance()->GetYORQ();
//    SCPI_ResultDouble(context,dx);

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

scpi_result_t  WAV_YREFQ(scpi_t * context)//��ѯָ��Դy���������ݵ�Ĳο�ʱ���׼
{
//    Q_UNUSED(context);
//    ERROR_XY_MODE;
//    double dx = CWavefrom::Instance()->GetYREFQ();
//    SCPI_ResultDouble(context,dx);

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}


