
#include "scpi_cmd_tim.h"
#include <stdio.h>
#include <stdlib.h>
#include "scpi_help.h"
#include "../../SCPICommandCallBackJava.h"
#include "../../Log.h"

//����ˮƽʱ����λ
scpi_result_t TIM_EXT(scpi_t * context)
{
//    Channel::IModuleChannel *mc= dynamic_cast<Channel::IModuleChannel *>(
//           ProcessCenter::IModuleRegisterDesk::module(ProcessCenter::MI_CHANNEL));
//    if((isRefCh(mc->active()))
//    || (mc->active() == CI_MATH && CMathWave::Instance()->GetMathType() == 2))
//    {//�ο����Ρ�FFT����ʱ���޸���Ч
//        return SCPI_RES_ERR;
//    }
    double  param1;
    if (!SCPI_ParamDouble(context, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
//    SCPI_CLOSE_MENU;
//    TimeScaleId after = IModuleDevice::instance()->timeToId(param1);
//    if(after<TSI_MIN || after > TSI_MAX)
//    {
//        return SCPI_RES_ERR;
//    }
//    TimeScaleId now = IModuleDevice::instance()->timeScaleIdOfView(false, Wave::WPI_STANDARD);
//    gMainWindow->getFocuserTipsObj(AdjustFocuser::AFTYPE_XGEAR)
//            ->EnableObj() = true;
//    CGearToSnSMsg *gtsnsm = CGearToSnSMsg::Instance();
//    //�������²㵵λ��id˳�����෴�ģ�����ڵ�λ�����������з�����
//    //�������ﴦ��λƫ��ʱҪ��������ܱ�֤��ȷ
//    gtsnsm->exec(now-after);
//    if(IModuleDevice::instance()->flagAuto(0))
//        CGearToAutoMsg::Instance()->exec(0);//�˳�auto

    setParam_1Double(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//����ˮƽʱ����λ
scpi_result_t TIM_PLUS_EXT(scpi_t * context)
{
//    Channel::IModuleChannel *mc= dynamic_cast<Channel::IModuleChannel *>(
//           ProcessCenter::IModuleRegisterDesk::module(ProcessCenter::MI_CHANNEL));
//    if((isRefCh(mc->active()))
//    || (mc->active() == CI_MATH && CMathWave::Instance()->GetMathType() == 2))
//    {//�ο����Ρ�FFT����ʱ���޸���Ч
//        return SCPI_RES_ERR;
//    }
    int  param1;
    if (!SCPI_ParamInt(context, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
//    SCPI_CLOSE_MENU;
//
//
//    gMainWindow->getFocuserTipsObj(AdjustFocuser::AFTYPE_XGEAR)
//            ->EnableObj() = true;
//    CGearToSnSMsg *gtsnsm = CGearToSnSMsg::Instance();
//    //�������²㵵λ��id˳�����෴�ģ�����ڵ�λ�����������з�����
//    //�������ﴦ��λƫ��ʱҪ��������ܱ�֤��ȷ
//    gtsnsm->exec(param1);
//    if(IModuleDevice::instance()->flagAuto(0))
//        CGearToAutoMsg::Instance()->exec(0);//�˳�auto

    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//��ѯˮƽʱ����λ
scpi_result_t TIM_EXTQ(scpi_t * context)
{
//    double res = IModuleDevice::instance()->timeScale(IModuleDevice::instance()->timeScaleIdOfView(false, Wave::WPI_STANDARD));
//    SCPI_ResultDouble(context,res);

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}


const char * tim_mode[] = {
    "YT",
    "ROLL",
    "XY",
    NULL
};
//������Ļʱ����ʾ��ʽ
scpi_result_t TIM_MODE(scpi_t * context)
{
    int param1;
    if (!SCPI_ParamChoice(context, tim_mode, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
//    SCPI_CLOSE_MENU;
//    MenuDisplaycommonFrame *temp = (MenuDisplaycommonFrame*)gMainWindow->GetMenuFrame_Help(OscilloUi::MI_DISPLAY);
//    if(temp)
//    {
//        switch(param1)
//        {
//        case 0:
//            temp->when_btYTTimeBase_clicked();
//            temp->when_btROLLDisable_clicked();
//            break;
//        case 1:
//            //temp->when_btROLLTimeBase_clicked();
//            temp->when_btROLLEnable_clicked();
//            break;
//        case 2:
//            temp->when_btXYTimeBase_clicked();
//            temp->when_btROLLDisable_clicked();
//            break;
//        default:
//            break;
//        }
//    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//��ѯ��Ļʱ����ʾ��ʽ
scpi_result_t TIM_MODEQ(scpi_t * context)
{
//    CDisplayMsg *cdisplaym = CDisplayMsg::Instance();
//
//    paramTimeBase ptb/*(confTbValue)*/;
//    cdisplaym->exec(DP_TIMEBASE,
//                    &ptb,
//                    false);
//    //printf("-liwb- read param:%d\r\n",ptb.mode);
//    if (gMainWindow->isInXYMode()==true)
//    {
//        SCPI_ResultString(context,tim_mode[2]);
//
//    }
//    else{
//        switch(ptb.mode)
//        {
//        case MenuDisplaycommonFrame::TB_YT:
//            SCPI_ResultString(context,tim_mode[0]);
//            break;
//        case MenuDisplaycommonFrame::TB_SCOLL:
//            SCPI_ResultString(context,tim_mode[1]);
//            break;
//        case MenuDisplaycommonFrame::TB_XY:
//            SCPI_ResultString(context,tim_mode[2]);
//            break;
//        default:
//            break;
//        }
//    }

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

scpi_result_t TIM_ROLL_DISP(scpi_t * context){
    bool param1;
    if (!SCPI_ParamBool(context,&param1,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_1Boolean(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t TIM_ROLL_DISPQ(scpi_t * context){
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//���ò�����ʾ��ˮƽƫ��
scpi_result_t TIM_OFFS(scpi_t * context)
{
//    ERROR_XY_MODE;
//    ERROR_ROLL_MODE;
//    CH_IDX chIdx = IModuleChannel::createInstance()->active();
//    if(isRefCh(chIdx)
//    || (chIdx == CI_MATH && CMathWave::Instance()->GetMathType() == 2)) {
//        return SCPI_RES_ERR;//�ο����Ρ�FFT����ʱ���޸���Ч
//    }
    double  param1;
    if (!SCPI_ParamDouble(context, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
//
//    IModuleDevice *md = IModuleDevice::instance();
//    Wave::WavePanelId panelId = Wave::WPI_STANDARD;
//    if(md->DispZoom(0))
//        panelId = Wave::WPI_LARGE;
//    S64 _param = param1*1e13;
//    S64 timePos = md->timePosOfView(0, panelId);
//    S64 unit = md->timeScale(md->timeScaleIdOfView(0, panelId))*1e13/MS_GRID_PerH;
//    S64 offset = (timePos-_param)/unit;
//    TriggerTimePosMsg::instance()->exec_ex(offset);
    setParam_1Double(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//���ò�����ʾ��ˮƽƫ��
scpi_result_t TIM_PLUS_OFFS(scpi_t * context)
{
//    ERROR_XY_MODE;
//    ERROR_ROLL_MODE;
//    CH_IDX chIdx = IModuleChannel::createInstance()->active();
//    if(isRefCh(chIdx)
//    || (chIdx == CI_MATH && CMathWave::Instance()->GetMathType() == 2)) {
//        return SCPI_RES_ERR;//�ο����Ρ�FFT����ʱ���޸���Ч
//    }
    int  param1;
    if (!SCPI_ParamInt(context, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
//
//
//    TriggerTimePosMsg::instance()->exec_ex(param1);
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//��ѯ������ʾ��ˮƽƫ��
scpi_result_t TIM_OFFSQ(scpi_t * context)
{
//    Wave::WavePanelId id= Wave::WPI_STANDARD;
//    if(IModuleDevice::instance()->DispZoom(0))
//        id = Wave::WPI_LARGE;
//    S64 timePoseMain = IModuleDevice::instance()->timePosOfView(0, id);//��λ��0.1ps
//
//    SCPI_ResultDouble(context,1.0*timePoseMain/1e13);

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

scpi_result_t TIM_ZOO_SCA(scpi_t * context){
    double  param1;
    if (!SCPI_ParamDouble(context, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_1Double(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t TIM_ZOO_SCAQ(scpi_t * context){
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t TIM_LISTQ(scpi_t* context){
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//�򿪻�ر�ͨ��1��ͨ��2��XYģʽ��ʾ
scpi_result_t TIM_XY1_DISP(scpi_t * context){
//    Q_UNUSED(context);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;}
//��ѯͨ��1��ͨ��2��XYģʽ��ʾ
scpi_result_t TIM_XY1_DISPQ(scpi_t * context){
    //SEND_RECEIVE_CMD(context);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;}

