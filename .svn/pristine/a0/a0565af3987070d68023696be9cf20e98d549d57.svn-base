
#include "scpi_cmd_ref.h"
#include <stdio.h>
#include <stdlib.h>
#include "scpi_help.h"
#include "../../SCPICommandCallBackJava.h"
#include "../../Log.h"

//�򿪻�ر�REF����
scpi_result_t REF_DISP(scpi_t * context)
{
//    ERROR_XY_MODE;
    bool  param1;
    if (!SCPI_ParamBool(context, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
//    SCPI_CLOSE_MENU;
//    if(param1 == false)
//    {
//        gMainWindow->GetMenuRef()->CloseAllRef();
//    }
//    else
//    {
//        gMainWindow->GetMenuRef()->OpenAllRef();
//    }

    setParam_1Boolean(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//��ѯREF���ܴ򿪻�ر�
scpi_result_t REF_DISPQ(scpi_t * context)
{
//    if(gMainWindow->isInXYMode())
//    {
//        SCPI_ResultBool(context,false);
//        return SCPI_RES_ERR;
//    }
//    IModuleChannel *imc = IModuleChannel::createInstance();
//    if(imc->isChOpened(CI_REF1)
//    || imc->isChOpened(CI_REF2)
//    || imc->isChOpened(CI_REF3)
//    || imc->isChOpened(CI_REF4))
//    {
//        SCPI_ResultBool(context,true);
//    }
//    else
//    {
//        SCPI_ResultBool(context,false);
//    }

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//�򿪻�ر�ָ���Ĳο�ͨ��
scpi_result_t REF_ENAB(scpi_t * context)
{
//    ERROR_XY_MODE;
    int param1;
    if (!SCPI_ParamChoice(context, allCh, &param1, true)) {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR PARAM!");
        return SCPI_RES_ERR;
    }
//    CH_IDX ch = (CH_IDX)getCh(param1);
//    if(!Channel::isRefCh(ch)) {
//        DEBUG_RETURN("ERROR CH!");
//        return SCPI_RES_ERR;
//    }
    bool param2;
    if (!SCPI_ParamBool(context, &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }

    setParam_1Int1Boolean(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);

//    SCPI_CLOSE_MENU;
//    MenuRefFrame* temp = gMainWindow->GetMenuRef();
//    switch(ch)
//    {
//    case CI_REF1:
//        if(param2==true)
//        {
//            temp->WhenRefClicked(CI_REF1,true);
//        }
//        else
//        {
//            temp->WhenRefClicked(CI_REF1,false);
//        }
//        break;
//    case CI_REF2:
//        if(param2==true)
//        {
//            temp->WhenRefClicked(CI_REF2,true);
//        }
//        else
//        {
//            temp->WhenRefClicked(CI_REF2,false);
//        }
//        break;
//    case CI_REF3:
//        if(param2==true)
//        {
//            temp->WhenRefClicked(CI_REF3,true);
//        }
//        else
//        {
//            temp->WhenRefClicked(CI_REF3,false);
//        }
//        break;
//    case CI_REF4:
//        if(param2 == true)
//        {
//            temp->WhenRefClicked(CI_REF4,true);
//        }
//        else
//        {
//            temp->WhenRefClicked(CI_REF4,false);
//        }
//        break;
//    default:
//        break;
//    }
    return SCPI_RES_OK;
}

//��ѯָ���Ĳο�ͨ���򿪻�ر�
scpi_result_t REF_ENABQ(scpi_t * context)
{
//    if(gMainWindow->isInXYMode())
//    {
//        SCPI_ResultBool(context,false);
//        return SCPI_RES_ERR;
//    }
    int param1;
    if (!SCPI_ParamChoice(context, allCh, &param1, true)) {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR PARAM!");
        return SCPI_RES_ERR;
    }
//    CH_IDX ch = (CH_IDX)getCh(param1);
//    if(!Channel::isRefCh(ch)) {
//        DEBUG_RETURN("ERROR CH!");
//        return SCPI_RES_ERR;
//    }
//
//    IModuleChannel *imc = IModuleChannel::createInstance();
//    SCPI_ResultBool(context,imc->isChOpened(ch));
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//���òο�ͨ����ˮƽ��λ
scpi_result_t REF_HSC(scpi_t * context)
{
//    ERROR_XY_MODE;
    int param1;
    if (!SCPI_ParamChoice(context, allCh, &param1, true)) {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR PARAM!");
        return SCPI_RES_ERR;
    }
//    CH_IDX idx = (CH_IDX)getCh(param1);
//    if(!Channel::isRefCh(idx)) {
//        DEBUG_RETURN("ERROR CH!");
//        return SCPI_RES_ERR;
//    }
//    if(!IModuleChannel::createInstance()->isChOpened(idx)) {
//        DEBUG_RETURN("REF CH NOT OPEN!");
//        return SCPI_RES_ERR;
//    }
    double param2;
    if (!SCPI_ParamDouble(context, &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }

//    int after = IModuleDevice::instance()->timeToId(param2);
//    if(after<TSI_MIN || after > TSI_MAX)
//    {
//        return SCPI_RES_ERR;
//    }
//
//    CRefWaveChannel * pRefWaveChannel = CRefWave::Instance()->GetRefWaveChannel(idx);
//    if(pRefWaveChannel)
//    {
//        //printf("-liwb- deal wave\n");
//        if(pRefWaveChannel->IsFft())
//        {
//            //printf("-liwb- refWaveChannel Error!\n");
//            return SCPI_RES_ERR;
//        }
//        int now = pRefWaveChannel->GetCurrentTimeScaleId();
//        CGearToSnSMsg *gtsnsm = CGearToSnSMsg::Instance();
//        //�������²㵵λ��id˳�����෴�ģ�����ڵ�λ�����������з�����
//        //�������ﴦ��λƫ��ʱҪ��������ܱ�֤��ȷ
//        gtsnsm->exec_auto(now-after,idx);
//        if(IModuleDevice::instance()->flagAuto(0))
//            CGearToAutoMsg::Instance()->exec(0);//�˳�auto
//    }
    setParam_1Int1Double(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
//���òο�ͨ����ˮƽ��λ
scpi_result_t REF_PLUS_HSC(scpi_t * context)
{
//    ERROR_XY_MODE;
    int param1;
    if (!SCPI_ParamChoice(context, allCh, &param1, true)) {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR PARAM!");
        return SCPI_RES_ERR;
    }
//    CH_IDX idx = (CH_IDX)getCh(param1);
//    if(!Channel::isRefCh(idx)) {
//        DEBUG_RETURN("ERROR CH!");
//        return SCPI_RES_ERR;
//    }
//    if(!IModuleChannel::createInstance()->isChOpened(idx)) {
//        DEBUG_RETURN("REF CH NOT OPEN!");
//        return SCPI_RES_ERR;
//    }
    int param2;
    if (!SCPI_ParamInt(context, &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }


//    CRefWaveChannel * pRefWaveChannel = CRefWave::Instance()->GetRefWaveChannel(idx);
//    if(pRefWaveChannel)
//    {
//        if(pRefWaveChannel->IsFft())
//        {
//            return SCPI_RES_ERR;
//        }
//
//        CGearToSnSMsg *gtsnsm = CGearToSnSMsg::Instance();
//        //�������²㵵λ��id˳�����෴�ģ�����ڵ�λ�����������з�����
//        //�������ﴦ��λƫ��ʱҪ��������ܱ�֤��ȷ
//        gtsnsm->exec_auto(param2,idx);
//        if(IModuleDevice::instance()->flagAuto(0))
//            CGearToAutoMsg::Instance()->exec(0);//�˳�auto
//    }
    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//��ѯ�ο�ͨ����ˮƽ��λ
scpi_result_t REF_HSCQ(scpi_t * context)
{
//    ERROR_XY_MODEQ;
    int param1;
    if (!SCPI_ParamChoice(context, allCh, &param1, true)) {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR PARAM!");
        return SCPI_RES_ERR;
    }
//    CH_IDX idx = (CH_IDX)getCh(param1);
//    if(!Channel::isRefCh(idx)) {
//        DEBUG_RETURN("ERROR CH!");
//        return SCPI_RES_ERR;
//    }
//    if(!IModuleChannel::createInstance()->isChOpened(idx)) {
//        DEBUG_RETURN("REF CH NOT OPEN!");
//        return SCPI_RES_ERR;
//    }
//    CRefWaveChannel * pRefWaveChannel = CRefWave::Instance()->GetRefWaveChannel(idx);
//    if(pRefWaveChannel)
//    {
//        if(pRefWaveChannel->IsFft())
//        {
//            DEBUG_RETURN("ERROR fft mode can't support!");
//            return SCPI_RES_ERR;
//        }
//        SCPI_ResultDouble(context,pRefWaveChannel->GetCurrentTimeScale());
//    }
//    else
//    {
//        DEBUG_RETURN("ERROR UNKNOW!");
//    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
     return SCPI_RES_OK;
}

//���òο�ͨ���Ĵ�ֱ��λ
scpi_result_t REF_VSC(scpi_t * context)
{
//    ERROR_XY_MODE;
    int param1;
    if (!SCPI_ParamChoice(context, allCh, &param1, true)) {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR PARAM!");
        return SCPI_RES_ERR;
    }
//    CH_IDX idx = (CH_IDX)getCh(param1);
//    if(!Channel::isRefCh(idx)) {
//        return SCPI_RES_ERR;
//    }
//    if(!IModuleChannel::createInstance()->isChOpened(idx)) {
//        DEBUG_RETURN("REF CH NOT OPEN!");
//        return SCPI_RES_ERR;
//    }
    double  param2;
    if (!SCPI_ParamDouble(context, &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }

//    CRefWaveChannel * pRefWaveChannel = CRefWave::Instance()->GetRefWaveChannel(idx);
//    if(pRefWaveChannel)
//    {
//        CWaveZoomRefChVerticalMsg *cRefZoomm =
//                                  CWaveZoomRefChVerticalMsg::Instance();
//        _YDANG_CL after =
//                (_YDANG_CL)(IDescVertical::vScaleToFloat(param2/pRefWaveChannel->GetCurrentProbe()));
//        if(after<DANG_MIN || after > DANG_MAX)
//        {
//            return SCPI_RES_ERR;
//        }
//        int now = pRefWaveChannel->GetCurrentVolScaleId();
//        cRefZoomm->exec(idx,after-now);
//    }
    setParam_1Int1Double(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
//���òο�ͨ���Ĵ�ֱ��λ
scpi_result_t REF_PLUS_VSC(scpi_t * context)
{
//    ERROR_XY_MODE;
    int param1;
    if (!SCPI_ParamChoice(context, allCh, &param1, true)) {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR PARAM!");
        return SCPI_RES_ERR;
    }
//    CH_IDX idx = (CH_IDX)getCh(param1);
//    if(!Channel::isRefCh(idx)) {
//        return SCPI_RES_ERR;
//    }
//    if(!IModuleChannel::createInstance()->isChOpened(idx)) {
//        DEBUG_RETURN("REF CH NOT OPEN!");
//        return SCPI_RES_ERR;
//    }
    int  param2;
    if (!SCPI_ParamInt(context, &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }

//    CRefWaveChannel * pRefWaveChannel = CRefWave::Instance()->GetRefWaveChannel(idx);
//    if(pRefWaveChannel)
//    {
//        CWaveZoomRefChVerticalMsg *cRefZoomm =
//                                  CWaveZoomRefChVerticalMsg::Instance();
//
//        cRefZoomm->exec(idx,param2);
//    }
    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//��ѯ�ο�ͨ���Ĵ�ֱ��λ
scpi_result_t REF_VSCQ(scpi_t * context)
{
//    ERROR_XY_MODEQ;
    int param1;
    if (!SCPI_ParamChoice(context, allCh, &param1, true)) {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR PARAM!");
        return SCPI_RES_ERR;
    }
//    CH_IDX idx = (CH_IDX)getCh(param1);
//    if(!Channel::isRefCh(idx)) {
//        DEBUG_RETURN("ERROR CH!");
//        return SCPI_RES_ERR;
//    }
//    if(!IModuleChannel::createInstance()->isChOpened(idx)) {
//        DEBUG_RETURN("REF CH NOT OPEN!");
//        return SCPI_RES_ERR;
//    }
//    CRefWaveChannel * pRefWaveChannel = CRefWave::Instance()->GetRefWaveChannel(idx);
//    if(pRefWaveChannel)
//    {
//        int now = pRefWaveChannel->GetCurrentVolScaleId();
//        SCPI_ResultDouble(context, IModuleDevice::instance()->vScale((_YDANG_CL)now)
////                          * pRefWaveChannel->GetProbe());
//                          * pRefWaveChannel->GetCurrentProbe());
//    }
//    else
//    {
//        DEBUG_RETURN("UNKNOW ERROR!");
//    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//ѡ��ǰ�ο�ͨ��
scpi_result_t REF_CURR(scpi_t * context)
{
//    ERROR_XY_MODE;
    int param1;
    if (!SCPI_ParamChoice(context, allCh, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
//    CH_IDX idx = (CH_IDX)getCh(param1);
//    if(!Channel::isRefCh(idx)) {
//        return SCPI_RES_ERR;
//    }
//    if(!IModuleChannel::createInstance()->isChOpened(idx)) {
//        DEBUG_RETURN("REF CH NOT OPEN!");
//        return SCPI_RES_ERR;
//    }
//    gMainWindow->channelSelIndiactor->SelCh(idx);
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t REF_PLUS_HOFF(scpi_t * context) //ˮƽ�Ӽ�1
{
//    ERROR_XY_MODE;
    int param1;
    if (!SCPI_ParamChoice(context, allCh, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    int  param2;
    if (!SCPI_ParamInt(context, &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
//    CH_IDX idx = (CH_IDX)getCh(param1);
//    if(!Channel::isRefCh(idx)) {
//        return SCPI_RES_ERR;
//    }
//    if(!IModuleChannel::createInstance()->isChOpened(idx)) {
//        DEBUG_RETURN("REF CH NOT OPEN!");
//        return SCPI_RES_ERR;
//    }
//    CWaveMoveRefChHorizonalMsg::Instance()->exec(idx,param2);
    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

scpi_result_t REF_PLUS_VOFF(scpi_t * context) //��ֱ�Ӽ�1
{
//    ERROR_XY_MODE;
    int param1;
    if (!SCPI_ParamChoice(context, allCh, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    int  param2;
    if (!SCPI_ParamInt(context, &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }

//    if(param2 == 0) param2 = -1;
//    else if(param2 == 1) param2 = 1;
//    else return SCPI_RES_ERR;
//
//    CH_IDX idx = (CH_IDX)getCh(param1);
//    if(!Channel::isRefCh(idx)) {
//        return SCPI_RES_ERR;
//    }
//    if(!IModuleChannel::createInstance()->isChOpened(idx)) {
//        DEBUG_RETURN("REF CH NOT OPEN!");
//        return SCPI_RES_ERR;
//    }
//    CChannelDragMsg *cdm = CChannelDragMsg::Instance();
//    cdm->exec_ex(param2, idx);
    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//���ô�ֱ
scpi_result_t REF_POS(scpi_t * context)
{
//    ERROR_XY_MODE;
//    Q_UNUSED(context);

    int param1;
    if(!SCPI_ParamChoice(context,allCh,&param1,true))
    {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    double param2;
    if (!SCPI_ParamDouble(context,&param2,true))
    {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    //��Ҫ�л�����ǰͨ��
//    CH_IDX idx = (CH_IDX)getCh(param1);
//    if(!Channel::isRefCh(idx)) {
//        return SCPI_RES_ERR;
//    }
//    if(!IModuleChannel::createInstance()->isChOpened(idx)) {
//        DEBUG_RETURN("REF CH NOT OPEN!");
//        return SCPI_RES_ERR;
//    }
//    gMainWindow->channelSelIndiactor->SelCh(idx);
//
//    CChannelDragMsg *cdm = CChannelDragMsg::Instance();
//    gMainWindow->setMoveFlg(MainWindow::MOVE_CH);
//    cdm->exec(param2);
    setParam_1Int1Double(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;

}
scpi_result_t REF_TIM_POS(scpi_t * context) //����ˮƽ
{
//    ERROR_XY_MODE;
//    Q_UNUSED(context);
    //��Ҫ�л�����ǰͨ��
    int param1;
    if(!SCPI_ParamChoice(context,allCh,&param1,true))
    {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    double param2;
    if (!SCPI_ParamDouble(context,&param2,true))
    {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }

        //��Ҫ�л�����ǰͨ��
//    CH_IDX idx = (CH_IDX)getCh(param1);
//    if(!Channel::isRefCh(idx)) {
//        return SCPI_RES_ERR;
//    }
//    if(!IModuleChannel::createInstance()->isChOpened(idx)) {
//        DEBUG_RETURN("REF CH NOT OPEN!");
//        return SCPI_RES_ERR;
//    }
//    gMainWindow->channelSelIndiactor->SelCh(idx);
//    //printf("-liwb- param1:%d param2:%d\n",param1,param2);
//
//    TriggerTimePosMsg *cttpm = TriggerTimePosMsg::instance();
//    cttpm->exec(param2);
    setParam_1Int1Double(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;

}
scpi_result_t REF_PLUS_TIM_POS(scpi_t * context) //����ˮƽ
{
//    Q_UNUSED(context);
//    ERROR_XY_MODE;
    int param1;
    if(!SCPI_ParamChoice(context,allCh,&param1,true))
    {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }

    int param2;
    if (!SCPI_ParamInt(context,&param2,true))
    {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
//    if (param2<0) param2=-1;
//    else if (param2>=0) param2=1;
//
//    //��Ҫ�л�����ǰͨ��
//    CH_IDX idx = (CH_IDX)getCh(param1);
//    if(!Channel::isRefCh(idx)) {
//        return SCPI_RES_ERR;
//    }
//    if(!IModuleChannel::createInstance()->isChOpened(idx)) {
//        DEBUG_RETURN("REF CH NOT OPEN!");
//        return SCPI_RES_ERR;
//    }
//    gMainWindow->channelSelIndiactor->SelCh(idx);
//
//    TriggerTimePosMsg *cttpm = TriggerTimePosMsg::instance();
//    cttpm->exec(param2);
    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;

}
scpi_result_t REF_PLUS_POS(scpi_t * context) //���ô�ֱ
{
//    Q_UNUSED(context);
//    ERROR_XY_MODE;
    int param1;
    if(!SCPI_ParamChoice(context,allCh,&param1,true))
    {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }

    int param2;
    if (!SCPI_ParamInt(context,&param2,true))
    {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
//    if (param2<0) param2=-1;
//    else if (param2>=0) param2=1;
//
//    //��Ҫ�л�����ǰͨ��
//    CH_IDX idx = (CH_IDX)getCh(param1);
//    if(!Channel::isRefCh(idx)) {
//        return SCPI_RES_ERR;
//    }
//    if(!IModuleChannel::createInstance()->isChOpened(idx)) {
//        DEBUG_RETURN("REF CH NOT OPEN!");
//        return SCPI_RES_ERR;
//    }
//    gMainWindow->channelSelIndiactor->SelCh(idx);
//
//
//    CChannelDragMsg *cdm = CChannelDragMsg::Instance();
//    gMainWindow->setMoveFlg(MainWindow::MOVE_CH);
//    cdm->exec(param2);
    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;

}

scpi_result_t REF_POSQ(scpi_t * context){
    int param1;
    if(!SCPI_ParamChoice(context,allCh,&param1,true))
    {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t REF_TIM_POSQ(scpi_t * context)
{
    int param1;
    if(!SCPI_ParamChoice(context,allCh,&param1,true))
    {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t REF_SRATQ(scpi_t * context)
{
    int param1;
    if (!SCPI_CmdParamInt(context,&param1)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t REF_MDEPQ(scpi_t * context)
{
    int param1;
    if (!SCPI_CmdParamInt(context,&param1)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

scpi_result_t CURR_REF(scpi_t *context){
    int param1;
    if (!SCPI_ParamChoice(context,allCh,&param1,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//num, 为了和上面的返回索一致，返回时+5，即 ch1,ch2,ch3,ch4,math,ref1
scpi_result_t  REF_NUM_ENAB(scpi_t * context){
    int param1;
    if (!SCPI_CmdParamInt(context,&param1)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    param1+=5;

    bool param2;
    if (!SCPI_ParamBool(context, &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }

    setParam_1Int1Boolean(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t  REF_NUM_ENABQ(scpi_t * context){
    int param1;
    if (!SCPI_CmdParamInt(context,&param1)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    param1+=5;
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t  REF_NUM_HSC(scpi_t * context){
    int param1;
    if (!SCPI_CmdParamInt(context,&param1)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    param1+=5;
    double param2;
    if (!SCPI_ParamDouble(context, &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_1Int1Double(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t  REF_NUM_PLUS_HSC(scpi_t * context){
    int param1;
    if (!SCPI_CmdParamInt(context,&param1)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    param1+=5;

    int param2;
    if (!SCPI_ParamInt(context, &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t  REF_NUM_HSCQ(scpi_t * context){
    int param1;
    if (!SCPI_CmdParamInt(context,&param1)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    param1+=5;
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t  REF_NUM_VSC(scpi_t * context){
    int param1;
    if (!SCPI_CmdParamInt(context,&param1)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    param1+=5;

    double  param2;
    if (!SCPI_ParamDouble(context, &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_1Int1Double(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t  REF_NUM_PLUS_VSC(scpi_t * context){
    int param1;
    if (!SCPI_CmdParamInt(context,&param1)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    param1+=5;

    int  param2;
    if (!SCPI_ParamInt(context, &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t  REF_NUM_VSCQ(scpi_t * context){
    int param1;
    if (!SCPI_CmdParamInt(context,&param1)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    param1+=5;
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t  REF_NUM_CURR(scpi_t * context){
    int param1;
    if (!SCPI_CmdParamInt(context,&param1)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    param1+=5;

    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t  REF_NUM_POS(scpi_t * context){
    int param1;
    if (!SCPI_CmdParamInt(context,&param1)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    param1+=5;

    double param2;
    if (!SCPI_ParamDouble(context,&param2,true))
    {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_1Int1Double(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t  REF_NUM_POSQ(scpi_t * context){
    int param1;
    if (!SCPI_CmdParamInt(context,&param1)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    param1+=5;
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t  REF_NUM_TIM_POS(scpi_t * context){
    int param1;
    if (!SCPI_CmdParamInt(context,&param1)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    param1+=5;

    double param2;
    if (!SCPI_ParamDouble(context,&param2,true))
    {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }

    setParam_1Int1Double(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t  REF_NUM_TIM_POSQ(scpi_t * context){
    int param1;
    if (!SCPI_CmdParamInt(context,&param1)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    param1+=5;
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t  REF_NUM_PLUS_POS(scpi_t * context){
    int param1;
    if (!SCPI_CmdParamInt(context,&param1)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    param1+=5;

    int param2;
    if (!SCPI_ParamInt(context,&param2,true))
    {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t  REF_NUM_PLUS_TIM_POS(scpi_t * context){
    int param1;
    if (!SCPI_CmdParamInt(context,&param1)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    param1+=5;

    int param2;
    if (!SCPI_ParamInt(context,&param2,true))
    {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t  REF_NUM_SRATQ(scpi_t * context){
    int param1;
    if (!SCPI_CmdParamInt(context,&param1)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    param1+=5;
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t  REF_NUM_MDEPQ(scpi_t * context){
    int param1;
    if (!SCPI_CmdParamInt(context,&param1)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    param1+=5;
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

