#include "scpi_cmd_trig.h"
#include <stdio.h>
#include <stdlib.h>
#include <cstring>
#include <string>
#include "scpi_help.h"
#include "../../SCPICommandCallBackJava.h"
#include "../../Log.h"

//??????????λ?????λV
void getLevelPos(scpi_t * context,int ch)
{
//    CH_IDX chIdx;
//    TriggerLevel::TRIGGER_TYPE triggerType;
//    TriggerLevel::IModuleTriggerLevel::extractInstanceId(ch, &chIdx, &triggerType);
//
//    ChvParam cp;
//    cp.chId = chIdx;
//
//    IModuleDevice *md = IModuleDevice::instance();
//    cp.vValue = md->vScale(md->vScaleId(false, chIdx)) * md->probeRatio(false, chIdx);
//    cp.vUnit = md->adUnit(chIdx);
//
//    TriggerLevel::IModuleTriggerLevel *mt = NULL;
//    mt = TriggerLevel::IModuleTriggerLevel::createInstance();
//    int graphInfo = mt->getGeographInfo(ch);
//    int vPosZero = IModuleDevice::instance()->vPosOfZero(0, chIdx);
//
//    if(IModuleDevice::instance()->DispZoom(0))
//    {
//        graphInfo = Device::IDescVertical::ZoomFandaConvesionTovPosOfZero(graphInfo);
//    }
//    float trigPos = graphInfo - vPosZero;
//    trigPos = trigPos*cp.vValue/MS_GRID_PerV;
//    SCPI_ResultDouble(context,trigPos);
}

//??????????λ?????λV
void setLevelPos(scpi_t * context, int ch, double param1)
{
//
// //Q_UNUSED(context);

//    CH_IDX chIdx;
//    TriggerLevel::TRIGGER_TYPE triggerType;
//    TriggerLevel::IModuleTriggerLevel::extractInstanceId(ch, &chIdx, &triggerType);
//
//    ChvParam cp;
//    cp.chId = chIdx;
//
//    IModuleDevice *md = IModuleDevice::instance();
//    cp.vValue = md->vScale(md->vScaleId(false, chIdx)) * md->probeRatio(false, chIdx);
//    cp.vUnit = md->adUnit(chIdx);
//
//    TriggerLevel::IModuleTriggerLevel *mt = NULL;
//    mt = TriggerLevel::IModuleTriggerLevel::createInstance();
//    int graphInfo = mt->getGeographInfo(ch);
//    int vPosZero = IModuleDevice::instance()->vPosOfZero(0, chIdx);
//
//    if(IModuleDevice::instance()->DispZoom(0))
//    {
//        graphInfo = Device::IDescVertical::ZoomFandaConvesionTovPosOfZero(graphInfo);
//    }
//    float trigPos = graphInfo - vPosZero;
//    trigPos = trigPos*cp.vValue/MS_GRID_PerV;
//    float offset = param1 - trigPos;
//    int iOffset = offset/(cp.vValue/MS_GRID_PerV);
//
//    gMainWindow->setMoveFlg(MainWindow::MOVE_LEVEL);
//    CTriggerLevelDragMsg *cdm = CTriggerLevelDragMsg::Instance();
//    cdm->exec(iOffset,ch);
}
void setLevelPosPLUS(scpi_t * context, int ch, int param1)
{
//
// //Q_UNUSED(context);

//
//    CTriggerLevelDragMsg *cdm = CTriggerLevelDragMsg::Instance();
//    cdm->exec(param1,ch);
}

//??????????????????λ?????λV??dwart slope
void setAnotherLev(int idx, int levTp)
{
//    CTriggerLevelDragMsg *cdm = CTriggerLevelDragMsg::Instance();
//    IModuleTriggerLevel *mtl = IModuleTriggerLevel::createInstance();
//    //?????????????????????????????????????
//    int zero = IModuleDevice::instance()->vPosOfZero(0);
//    if(IModuleDevice::instance()->DispZoom(false))
//        zero = IDescVertical::vPosOfZeroConvesionToZoomFanda(zero);
//    int highInstance = IModuleTriggerLevel::instanceId((CH_IDX)idx, TT_High);
//    int lowInstance = IModuleTriggerLevel::instanceId((CH_IDX)idx, TT_LOW);
//    int highPos = mtl->getGeographInfo(highInstance)-zero;
//    int lowPos = mtl->getGeographInfo(lowInstance)-zero;
//    //?ж????????????????????????????
//    if(levTp == TT_LOW
//    && highPos < lowPos)
//    {
//        int movePos = highPos - lowPos;
//        if(movePos != 0) cdm->exec(movePos, lowInstance);
//    }
//    else if(levTp == TT_High
//         && lowPos > highPos)
//    {
//        int movePos = lowPos - highPos;
//        if(movePos != 0) cdm->exec(movePos, highInstance);
//    }
}

//????????,???????????
void setTrigSource(int idx,int trigTp)
{
//    SCPI_CLOSE_MENU;
//    CTriggerSetMsg         *csetm = CTriggerSetMsg::Instance();
//    CTriggerLevelSwitchMsg *cswitchm = CTriggerLevelSwitchMsg::Instance();
//    paramCh chValue(idx);
//    csetm->exec(TR_CH, &chValue, (TriggerType)trigTp);
//    //??trigger level??
//    gMainWindow->hideAllTrig();
//    cswitchm->exec(TriggerLevel::IModuleTriggerLevel::instanceId((CH_IDX)idx, TriggerLevel::TT_NORMAL));
}

const char * trig_serial_source[]={
        "S1",
        "S2",
        NULL
};
scpi_result_t querySerial(scpi_t * context){
    int param1;
    if (!SCPI_ParamChoice(context, trig_serial_source, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

const char * trig_type[] = {
    "COMMon",
    "EDGE",
    "PULSe",
    "LOGic",
    "NEDGe",
    "RUNT",
    "SLOPe",
    "TIMeout",
    "VIDeo",
    "S1",
    "S2",
    NULL
};
//????????
scpi_result_t TRIG_TYPE(scpi_t * context)
{
//    ERROR_XY_MODE;
    int param1;
    if (!SCPI_ParamChoice(context, trig_type, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }

//    TRIGGERUIDEFINE::paramTrigType trigType;
//    CTriggerSetMsg         *csetm = CTriggerSetMsg::Instance();
//    MENU_ID tmp;
//    switch(param1)
//    {
//    case 0:
//        trigType.type = TRIG_TYPE_EDGE;
//        tmp=MI_TRIGGER_EDGE;
//        break;
//    case 1:
//       trigType.type = TRIG_TYPE_PULSE;
//       tmp=MI_TRIGGER_PULSE;
//        break;
//    case 2:
//        trigType.type = TRIG_TYPE_LOGIC;
//        tmp=MI_TRIGGER_LOGIC;
//        break;
//    case 3:
//        trigType.type = TRIG_TYPE_LOW_PULSE;
//        tmp=MI_TRIGGER_DPULSE;
//        break;
//    case 4:
//        trigType.type = TRIG_TYPE_SLOPE;
//        tmp=MI_TRIGGER_SLOPE;
//        break;
//    case 5:
//        trigType.type = TRIG_TYPE_TIMEOUT;
//        tmp=MI_TRIGGER_TIMEOUT;
//        break;
//    case 6:
//        trigType.type = TRIG_TYPE_NEDGE;
//        tmp=MI_TRIGGER_NEDGE;
//        break;
//    case 7:
//        trigType.type = TRIG_TYPE_VIDEO;
//        tmp=MI_TRIGGER_VIDEO;
//        break;
//    default:
//        return SCPI_RES_ERR;
//    }
//
//    IDescTriggerCommon &objTriggerCommon = IDescTrigger::triggerDesc(TRIG_TYPE_START_ID);
//    TriggerType curTrigType = objTriggerCommon.triggerType(0);
//    if(curTrigType!=trigType.type)
//    {
//        SCPI_CLOSE_MENU;
//        csetm->exec(TR_TYPE, &trigType, trigType.type);
//        MenuItemFrame * w = gMainWindow->GetMenuFrame_Help(tmp);;
//        if(w)
//        {
//            w->OnInitFrame();
//        }
//        CGearToControlUpdateMsg *ccup = CGearToControlUpdateMsg::Instance();
//        ccup->exec(UPDATE_TRIGSTATE, NULL);
//        gMainWindow->getChannelPannelEx()->refreshCtrlTrigLevel();
//    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//????????????????????
scpi_result_t TRIG_TYPEQ(scpi_t * context)
{
//    ERROR_XY_MODEQ;
//    IDescTriggerCommon &objTriggerCommon = IDescTrigger::triggerDesc(TRIG_TYPE_START_ID);
//    TriggerType trigType = objTriggerCommon.triggerType(0);
//    switch(trigType)
//    {
//    case TRIG_TYPE_EDGE:
//        SCPI_ResultString(context,trig_type[0]);
//        break;
//    case TRIG_TYPE_PULSE:
//        SCPI_ResultString(context,trig_type[1]);
//        break;
//    case TRIG_TYPE_LOGIC:
//        SCPI_ResultString(context,trig_type[2]);
//        break;
//    case TRIG_TYPE_LOW_PULSE:
//        SCPI_ResultString(context,trig_type[3]);
//        break;
//    case TRIG_TYPE_SLOPE:
//        SCPI_ResultString(context,trig_type[4]);
//        break;
//    case TRIG_TYPE_TIMEOUT:
//        SCPI_ResultString(context,trig_type[5]);
//        break;
//    case TRIG_TYPE_NEDGE:
//        SCPI_ResultString(context,trig_type[6]);
//        break;
//    case TRIG_TYPE_VIDEO:
//        SCPI_ResultString(context,trig_type[7]);
//        break;
//    default:
//        break;
//    }

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//??????????????
scpi_result_t TRIG_HOLD(scpi_t * context)
{
//    ERROR_XY_MODE;
    double  param1;
    if (!SCPI_ParamDouble(context, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
//    if (param1>10 || param1<0.0000002) return SCPI_RES_ERR;
//
//    SCPI_CLOSE_MENU;
//    CTriggerSetMsg *csetm = CTriggerSetMsg::Instance();
//    paramTrigHoldOffTime trigHoldOffTimeValue(param1*1e9);
//    csetm->exec(TR_HOLDOFFTIME, &trigHoldOffTimeValue, TRIG_TYPE_EDGE);
    setParam_1Double(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//????????????????????????????
scpi_result_t TRIG_HOLDQ(scpi_t * context)
{
//    ERROR_XY_MODEQ;
//    CTriggerGetMsg *cgetm = CTriggerGetMsg::Instance();
//    double _time=0;
//    paramTrigHoldOffTime trigHoldOffTimeValue(_time);
//
//    cgetm->exec(TR_HOLDOFFTIME, &trigHoldOffTimeValue, TRIG_TYPE_EDGE);
//    S64 temp = trigHoldOffTimeValue.time;
//    if(temp <200) temp = 200;
//
//    SCPI_ResultDouble(context,temp*1e-9);

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

const char * trig_mode[] = {
    "AUTO",
    "NORMal",
    NULL
};
//????????????????????
scpi_result_t TRIG_MODE(scpi_t * context)
{
//    ERROR_XY_MODE;
    int param1;
    if (!SCPI_ParamChoice(context, trig_mode, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
//    TriggerMode mode=TM_AUTO;
//    switch(param1)
//    {
//    case 0:
//        mode=TM_AUTO;
//        break;
//    case 1:
//        mode=TM_NORMAL;
//        break;
//    default:
//        break;
//    }
//
//    SCPI_CLOSE_MENU;
//    paramMode trigMode(mode);
//    CTriggerSetMsg *csetm = CTriggerSetMsg::Instance();
//    csetm->exec(TR_MODE, &trigMode, TRIG_TYPE_START_ID);
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//??????????
scpi_result_t TRIG_MODEQ(scpi_t * context)
{
//    ERROR_XY_MODEQ;
//    paramMode trigMode;
//    CTriggerGetMsg *cgetm = CTriggerGetMsg::Instance();
//    cgetm->exec(TR_MODE, &trigMode, TRIG_TYPE_START_ID);
//    switch(trigMode.mode)
//    {
//    case TM_AUTO:
//        SCPI_ResultString(context,trig_mode[0]);
//        break;
//    case TM_NORMAL:
//        SCPI_ResultString(context,trig_mode[1]);
//        break;
//    default:
//        break;
//    }

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

const char * trig_stat[] = {
    "STOP",
    "RUN",
    "WAIT",
    "AUTO",
    NULL
};
//?????????????
scpi_result_t TRIG_STATQ(scpi_t * context)
{
//    if(IModuleDevice::instance()->flagAuto(0))
//    {
//        SCPI_ResultString(context,trig_stat[3]);
//        return SCPI_RES_OK;
//    }
//    int index = gMainWindow->maintopbar->getRunState();
//    switch(index)
//    {
//    case 0:
//        SCPI_ResultString(context,trig_stat[0]);
//        break;
//    case 1:
//        SCPI_ResultString(context,trig_stat[1]);
//        break;
//    case 2:
//        SCPI_ResultString(context,trig_stat[2]);
//        break;
//    default:
//        break;
//    }

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t TRIG_IS_EXTERNAL_TRIGGERQ(scpi_t* context){
    return TRIG_STATQ(context);
}
scpi_result_t TRIG_IS_EXTERNAL_CLOCKQ(scpi_t* context){
    return TRIG_STATQ(context);
}
scpi_result_t TRIG_HAS_EXTERNAL_DIALOGQ(scpi_t* context){
    return TRIG_STATQ(context);
}
scpi_result_t TRIG_EXTERNAL_DIALOG_SET(scpi_t* context){
    bool  param1;
    if (!SCPI_ParamBool(context,&param1,true)){
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR PARAM!");
        return SCPI_RES_ERR;
    }
    setParam_1Boolean(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
//Trigger edge
//???????????????
scpi_result_t TRIG_EDGE_SOUR(scpi_t * context)
{
//    ERROR_XY_MODE;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_EDGE,false);
//    SCPI_GET_PARAM1_CHIDX;
//
//    setTrigSource(idx,TRIG_TYPE_EDGE);
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

//????????????????
scpi_result_t TRIG_EDGE_SOURQ(scpi_t * context)
{
//    ERROR_XY_MODEQ;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_EDGE,true);
//
//    CTriggerGetMsg         *cgetm = CTriggerGetMsg::Instance();
//    paramCh chValue(MenuEdgeFrame::SOURCE_CH1);
//    cgetm->exec(TR_CH, &chValue, TRIG_TYPE_EDGE);
//    switch(chValue.src1_ch)
//    {
//    case MenuEdgeFrame::SOURCE_CH1:
//        SCPI_ResultString(context,"CH1");
//        break;
//    case MenuEdgeFrame::SOURCE_CH2:
//        SCPI_ResultString(context,"CH2");
//        break;
//    case MenuEdgeFrame::SOURCE_CH3:
//        SCPI_ResultString(context,"CH3");
//        break;
//    case MenuEdgeFrame::SOURCE_CH4:
//        SCPI_ResultString(context,"CH4");
//        break;
//    default:
//        DEBUG_RETURN("ERROR UNKNOW!");
//        break;
//    }

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}


const char * trig_edge_slop[] = {
    "RISE",
    "FALL",
    "DUAL",
    NULL
};
//??????????????????
scpi_result_t TRIG_EDGE_SLOP(scpi_t * context)
{
//    ERROR_XY_MODE;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_EDGE,false);

    int param1;
    if (!SCPI_ParamChoice(context, trig_edge_slop, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }

//    EdgeType tp=TET_ASC;
//    switch(param1)
//    {
//    case 0:
//        tp=TET_ASC;
//        break;
//    case 1:
//        tp=TET_DSC;
//        break;
//    case 2:
//        tp=TET_DUAL;
//        break;
//    default:
//        break;
//    }
//
//    SCPI_CLOSE_MENU;
//    CTriggerSetMsg *csetm = CTriggerSetMsg::Instance();
//    paramEdgeType modValue(tp);
//    csetm->exec(TR_EDGETYPE, &modValue, TRIG_TYPE_EDGE);
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//???????????????????
scpi_result_t TRIG_EDGE_SLOPQ(scpi_t * context)
{
//    ERROR_XY_MODEQ;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_EDGE,true);
//
//    CTriggerGetMsg *cgetm = CTriggerGetMsg::Instance();
//    paramEdgeType modValue(TET_ASC);
//    cgetm->exec(TR_EDGETYPE, &modValue, TRIG_TYPE_EDGE);
//    switch(modValue.edgeTy)
//    {
//    case TET_ASC:
//        SCPI_ResultString(context,trig_edge_slop[0]);
//        break;
//    case TET_DSC:
//        SCPI_ResultString(context,trig_edge_slop[1]);
//        break;
//    case TET_DUAL:
//        SCPI_ResultString(context,trig_edge_slop[2]);
//        break;
//    default:
//        break;
//    }

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//???????????????????
scpi_result_t TRIG_EDGE_LEV(scpi_t * context)
{
//    ERROR_XY_MODE;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_EDGE,false);
    double param1;
    if (!SCPI_ParamDouble(context, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }

//    SCPI_CLOSE_MENU;
//    TriggerLevel::IModuleTriggerLevel *mt = NULL;
//    mt = TriggerLevel::IModuleTriggerLevel::createInstance();
//    int ch = mt->active();
//    setLevelPos(context,ch,param1);
    setParam_1Double(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
//???????????????????
scpi_result_t TRIG_EDGE_PLUS_LEV(scpi_t * context)
{
//    ERROR_XY_MODE;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_EDGE,false);
    int param1;
    if (!SCPI_ParamInt(context, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }

//    SCPI_CLOSE_MENU;
//    TriggerLevel::IModuleTriggerLevel *mt = NULL;
//    mt = TriggerLevel::IModuleTriggerLevel::createInstance();
//    int ch = mt->active();
//    setLevelPosPLUS(context,ch,param1);
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//???????????????????
scpi_result_t TRIG_EDGE_LEVQ(scpi_t * context)
{
//    ERROR_XY_MODEQ;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_EDGE,true);
//
//    TriggerLevel::IModuleTriggerLevel *mt = NULL;
//    mt = TriggerLevel::IModuleTriggerLevel::createInstance();
//    int ch = mt->active();
//    getLevelPos(context,ch);

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}


const char * trig_edge_coup[] = {
    "DC",
    "AC",
    "HFRej",
    "LFRej",
    "NOISerej",
    NULL
};
//???????????????
scpi_result_t TRIG_EDGE_COUP(scpi_t * context)
{
//    ERROR_XY_MODE;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_EDGE,false);

    int param1;
    if (!SCPI_ParamChoice(context, trig_edge_coup, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }

//    _TRIG_COUPLING cp=COUPLING_DIRECT;
//    switch(param1)
//    {
//    case 0:
//        cp=COUPLING_DIRECT;
//        break;
//    case 1:
//        cp=COUPLING_AC;
//        break;
//    case 2:
//        cp=COUPLING_HFRS;
//        break;
//    case 3:
//        cp=COUPLING_LFRS;
//        break;
//    case 4:
//        cp=COUPLING_NOISERS;
//        break;
//    default:
//        break;
//    }
//
//    SCPI_CLOSE_MENU;
//    paramCouple coupleValue(cp);
//    CTriggerSetMsg *csetm = CTriggerSetMsg::Instance();
//    csetm->exec(TR_COUPLING, &coupleValue, TRIG_TYPE_EDGE);
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//???????????????
scpi_result_t TRIG_EDGE_COUPQ(scpi_t * context)
{
//    ERROR_XY_MODEQ;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_EDGE,true);
//
//    paramCouple coupleValue(COUPLING_DIRECT);
//    CTriggerGetMsg *cgetm = CTriggerGetMsg::Instance();
//    cgetm->exec(TR_COUPLING, &coupleValue, TRIG_TYPE_EDGE);
//    switch(coupleValue.couple)
//    {
//    case COUPLING_DIRECT:
//        SCPI_ResultString(context,trig_edge_coup[0]);
//        break;
//    case COUPLING_AC:
//        SCPI_ResultString(context,trig_edge_coup[1]);
//        break;
//    case COUPLING_HFRS:
//        SCPI_ResultString(context,trig_edge_coup[2]);
//        break;
//    case COUPLING_LFRS:
//        SCPI_ResultString(context,trig_edge_coup[3]);
//        break;
//    case COUPLING_NOISERS:
//        SCPI_ResultString(context,trig_edge_coup[4]);
//        break;
//    default:
//        break;
//    }

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//Trigger pulse
//???????????????
scpi_result_t TRIG_PULS_SOUR(scpi_t * context)
{
//    ERROR_XY_MODE;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_PULSE,false);
//    SCPI_GET_PARAM1_CHIDX;
//
//    setTrigSource(idx,TRIG_TYPE_PULSE);
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

//???????????????
scpi_result_t TRIG_PULS_SOURQ(scpi_t * context)
{
//    ERROR_XY_MODEQ;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_PULSE,true);
//
//    CTriggerGetMsg         *cgetm = CTriggerGetMsg::Instance();
//    paramCh chValue(MenuPulseFrame::SOURCE_CH1);
//    cgetm->exec(TR_CH, &chValue, TRIG_TYPE_PULSE);
//    switch(chValue.src1_ch)
//    {
//    case MenuPulseFrame::SOURCE_CH1:
//        SCPI_ResultString(context,"CH1");
//        break;
//    case MenuPulseFrame::SOURCE_CH2:
//        SCPI_ResultString(context,"CH2");
//        break;
//    case MenuPulseFrame::SOURCE_CH3:
//        SCPI_ResultString(context,"CH3");
//        break;
//    case MenuPulseFrame::SOURCE_CH4:
//        SCPI_ResultString(context,"CH4");
//        break;
//    default:
//        DEBUG_RETURN("ERROR UNKNOW!");
//        break;
//    }

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

const char * trig_puls_pol[] = {
    "POSitive",
    "NEGative",
    NULL
};
//???????????????
scpi_result_t TRIG_PULS_POL(scpi_t * context)
{
//    ERROR_XY_MODE;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_PULSE,false);
    int param1;
    if (!SCPI_ParamChoice(context, trig_puls_pol, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }

//    TrigglePolarity pl=TRIG_POLARITY_POSITIVE;
//    switch(param1)
//    {
//    case 0:
//        pl=TRIG_POLARITY_POSITIVE;
//        break;
//    case 1:
//        pl=TRIG_POLARITY_NEGATIVE;
//        break;
//    default:
//        break;
//    }
//
//    SCPI_CLOSE_MENU;
//    CTriggerSetMsg *csetm = CTriggerSetMsg::Instance();
//    paramPolality polalityValue(pl);
//    csetm->exec(TR_POLARITY, &polalityValue, TRIG_TYPE_PULSE);
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//??????????????
scpi_result_t TRIG_PULS_POLQ(scpi_t * context)
{
//    ERROR_XY_MODEQ;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_PULSE,true);
//
//    CTriggerGetMsg *cgetm = CTriggerGetMsg::Instance();
//    paramPolality polalityValue(TRIG_POLARITY_POSITIVE);
//    cgetm->exec(TR_POLARITY, &polalityValue, TRIG_TYPE_PULSE);
//    switch(polalityValue.pl)
//    {
//    case TRIG_POLARITY_POSITIVE:
//        SCPI_ResultString(context,trig_puls_pol[0]);
//        break;
//    case TRIG_POLARITY_NEGATIVE:
//        SCPI_ResultString(context,trig_puls_pol[1]);
//        break;
//    default:
//        break;
//    }

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//????????????????????
scpi_result_t TRIG_PULS_WIDT(scpi_t * context)
{
//    ERROR_XY_MODE;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_PULSE,false);

    double param1;
    if (!SCPI_ParamDouble(context, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
//    if (param1>10 || param1<0.00000004) return SCPI_RES_ERR;
//
//    SCPI_CLOSE_MENU;
//    CTriggerSetMsg *csetm = CTriggerSetMsg::Instance();
//    paramTrigWidth trigWidthValue(param1*1e9);
//    csetm->exec(TR_WITDH, &trigWidthValue, TRIG_TYPE_PULSE);
    setParam_1Double(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}


//???????????????????
scpi_result_t TRIG_PULS_WIDTQ(scpi_t * context)
{
//    ERROR_XY_MODEQ;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_PULSE,true);
//    CTriggerGetMsg *cgetm = CTriggerGetMsg::Instance();
//    paramTrigWidth trigWidthValue(8);
//    cgetm->exec(TR_WITDH, &trigWidthValue, TRIG_TYPE_PULSE);
//    SCPI_ResultDouble(context,trigWidthValue.width*1e-9);

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

const char * trig_puls_cond[] = {
    "LESS",
    "GREat",
    "EQUal",
    "UNEQual",
    NULL
};
//??????????????
scpi_result_t TRIG_PULS_COND(scpi_t * context)
{
//    ERROR_XY_MODE;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_PULSE,false);

    int param1;
    if (!SCPI_ParamChoice(context, trig_puls_cond, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }

//    PulseOperator op=TPO_LESSER;
//    switch(param1)
//    {
//    case 0:
//        op=TPO_LESSER;
//        break;
//    case 1:
//        op=TPO_GREATER;
//        break;
//    case 2:
//        op=TPO_EQUAL;
//        break;
//    case 3:
//        op=TPO_NOT_EQUAL;
//        break;
//    default:
//        break;
//    }
//
//    SCPI_CLOSE_MENU;
//    CTriggerSetMsg *csetm = CTriggerSetMsg::Instance();
//    paramCondition conditionValue(op);
//    csetm->exec(TR_CONDITION, &conditionValue, TRIG_TYPE_PULSE);
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//?????????????
scpi_result_t TRIG_PULS_CONDQ(scpi_t * context)
{
//    ERROR_XY_MODEQ;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_PULSE,true);
//    CTriggerGetMsg *cgetm = CTriggerGetMsg::Instance();
//    paramCondition conditionValue(TPO_LESSER);
//    cgetm->exec(TR_CONDITION, &conditionValue, TRIG_TYPE_PULSE);
//    switch(conditionValue.cnd)
//    {
//    case TPO_LESSER:
//        SCPI_ResultString(context,trig_puls_cond[0]);
//        break;
//    case TPO_GREATER:
//        SCPI_ResultString(context,trig_puls_cond[1]);
//        break;
//    case TPO_EQUAL:
//        SCPI_ResultString(context,trig_puls_cond[2]);
//        break;
//    case TPO_NOT_EQUAL:
//        SCPI_ResultString(context,trig_puls_cond[3]);
//        break;
//    default:
//        break;
//    }

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//???????????????????
scpi_result_t TRIG_PULS_LEV(scpi_t * context)
{
//    ERROR_XY_MODE;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_PULSE,false);
    double param1;
    if (!SCPI_ParamDouble(context, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }

//    TriggerLevel::IModuleTriggerLevel *mt = NULL;
//    mt = TriggerLevel::IModuleTriggerLevel::createInstance();
//    int ch = mt->active();
//    setLevelPos(context,ch,param1);
    setParam_1Double(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
//???????????????????
scpi_result_t TRIG_PULS_PLUS_LEV(scpi_t * context)
{
//    ERROR_XY_MODE;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_PULSE,false);
    int param1;
    if (!SCPI_ParamInt(context, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }

//    TriggerLevel::IModuleTriggerLevel *mt = NULL;
//    mt = TriggerLevel::IModuleTriggerLevel::createInstance();
//    int ch = mt->active();
//    setLevelPosPLUS(context,ch,param1);
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//??????????????????
scpi_result_t TRIG_PULS_LEVQ(scpi_t * context)
{
//    ERROR_XY_MODEQ;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_PULSE,true);
//
//    TriggerLevel::IModuleTriggerLevel *mt = NULL;
//    mt = TriggerLevel::IModuleTriggerLevel::createInstance();
//    int ch = mt->active();
//    getLevelPos(context,ch);

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//Trigger logic
const char * trig_log_stat[] = {
    "HIGH",
    "LOW",
    "NONE",
    NULL
};
//???????????????????????
scpi_result_t TRIG_LOG_STAT(scpi_t * context)
{
//    ERROR_XY_MODE;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_LOGIC,false);
    int param1;
    if (!SCPI_ParamChoice(context, allCh, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
//    CH_IDX idx = (CH_IDX)getCh(param1);
//    if(!Channel::isDynamicCh(idx)) {
//        return SCPI_RES_ERR;
//    }

    int param2;
    if (!SCPI_ParamChoice(context, trig_log_stat, &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }

//    SCPI_CLOSE_MENU;
//    TrigglePolarity pl=(TrigglePolarity)-1;
//    if(param2==0)
//    {
//        pl=TRIG_POLARITY_GAO;
//    }
//    else if(param2==1)
//    {
//        pl=TRIG_POLARITY_DI;
//    }
//
//    CTriggerSetMsg *csetm = CTriggerSetMsg::Instance();
//    CTriggerLevelSwitchMsg *cswitchm = CTriggerLevelSwitchMsg::Instance();
//    paramLogicPolality lgPlValue(pl,
//                                 (MenuLogicFrame::CFG_TRIG_LOGIC_CHID)idx);
//    csetm->exec(TR_POLARITY,&lgPlValue, TRIG_TYPE_LOGIC);
//
//    //??trigger level??
//    TriggerLevel::IModuleTriggerLevel *mt = TriggerLevel::IModuleTriggerLevel::createInstance();
//    int id = TriggerLevel::IModuleTriggerLevel::instanceId((Channel::CH_IDX)(idx),
//                                                           TriggerLevel::TT_NORMAL);
//    if(mt->isChOpened(id))
//    {
//        if(pl==-1)
//        {
//            mt->setActive(id);
//            cswitchm->exec(TriggerLevel::IModuleTriggerLevel::instanceId(
//                            idx, TriggerLevel::TT_NORMAL));
//        }
//        else if(mt->active() != id)
//        {
//            cswitchm->exec(TriggerLevel::IModuleTriggerLevel::instanceId(
//                            idx, TriggerLevel::TT_NORMAL));
//        }
//    }
//    else
//    {
//        if(pl!=-1)
//        {
//            cswitchm->exec(TriggerLevel::IModuleTriggerLevel::instanceId(
//                            idx, TriggerLevel::TT_NORMAL));
//        }
//    }
    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//??????????????????????
scpi_result_t TRIG_LOG_STATQ(scpi_t * context)
{
//    ERROR_XY_MODEQ;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_LOGIC,true);
//    SCPI_GET_PARAM1_CHIDX;
//
//    CTriggerGetMsg *cgetm = CTriggerGetMsg::Instance();
//    paramLogicPolality lgPlValue(TRIG_POLARITY_GAO, idx);
//    cgetm->exec(TR_POLARITY, &lgPlValue, TRIG_TYPE_LOGIC);
//    switch(lgPlValue.pl)
//    {
//    case MenuLogicFrame::CHSTATE_HIGH:
//        SCPI_ResultString(context,trig_log_stat[0]);
//        break;
//    case MenuLogicFrame::CHSTATE_LOW:
//        SCPI_ResultString(context,trig_log_stat[1]);
//        break;
////    case MenuLogicFrame::CHSTATE_DISABLE:
////        SCPI_ResultString(context,trig_log_stat[2]);
////        break;
//    default:
//        SCPI_ResultString(context,trig_log_stat[2]);
//        break;
//    }
    int param1;
    if (!SCPI_ParamChoice(context, allCh, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}


const char * trig_log_fun[] = {
    "AND",
    "OR",
    "NAND",
    "NOR",
    NULL
};
//??????????????????
scpi_result_t TRIG_LOG_FUNC(scpi_t * context)
{
//    ERROR_XY_MODE;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_LOGIC,false);

    int param1;
    if (!SCPI_ParamChoice(context, trig_log_fun, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }

//    LogicOperator lo=TLO_AND;
//    switch(param1)
//    {
//    case 0:
//        lo=TLO_AND;
//        break;
//    case 1:
//        lo=TLO_OR;
//        break;
//    case 2:
//        lo=TLO_NAND;
//        break;
//    case 3:
//        lo=TLO_NOR;
//        break;
//    default:
//        break;
//    }
//
//    SCPI_CLOSE_MENU;
//    CTriggerSetMsg *csetm = CTriggerSetMsg::Instance();
//    paramLgOp lgOpValue(lo);
//    csetm->exec(TR_OPERATOR, &lgOpValue, TRIG_TYPE_LOGIC);
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//?????????????????
scpi_result_t TRIG_LOG_FUNCQ(scpi_t * context)
{
//    ERROR_XY_MODEQ;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_LOGIC,true);
//
//    CTriggerGetMsg *cgetm = CTriggerGetMsg::Instance();
//    paramLgOp lgOpValue(TLO_AND);
//    cgetm->exec(TR_OPERATOR, &lgOpValue, TRIG_TYPE_LOGIC);
//    switch(lgOpValue.op)
//    {
//    case TLO_AND:
//        SCPI_ResultString(context,trig_log_fun[0]);
//        break;
//    case TLO_OR:
//        SCPI_ResultString(context,trig_log_fun[1]);
//        break;
//    case TLO_NAND:
//        SCPI_ResultString(context,trig_log_fun[2]);
//        break;
//    case TLO_NOR:
//        SCPI_ResultString(context,trig_log_fun[3]);
//        break;
//    default:
//        break;
//    }

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}


const char * trig_log_cond[] = {
    "LESS",
    "GREat",
    "EQUal",
    "UNEQual",
    "TRUE",
    "FALSe",
    NULL
};
//???????????????
scpi_result_t TRIG_LOG_COND(scpi_t * context)
{
//    ERROR_XY_MODE;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_LOGIC,false);

    int param1;
    if (!SCPI_ParamChoice(context, trig_log_cond, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
//    LogicCondition lc=TLC_LESSER;
//    switch(param1)
//    {
//    case 0:
//        lc=TLC_LESSER;
//        break;
//    case 1:
//        lc=TLC_GREATER;
//        break;
//    case 2:
//        lc=TLC_EQUAL;
//        break;
//    case 3:
//        lc=TLC_NOT_EQUAL;
//        break;
//    case 4:
//        lc=TLC_TRUE;
//        break;
//    case 5:
//        lc=TLC_FALSE;
//        break;
//    default:
//        break;
//    }
//
//    SCPI_CLOSE_MENU;
//    CTriggerSetMsg *csetm = CTriggerSetMsg::Instance();
//    paramCondition conditionValue(lc);
//    csetm->exec(TR_CONDITION, &conditionValue, TRIG_TYPE_LOGIC);
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}


//??????????????
scpi_result_t TRIG_LOG_CONDQ(scpi_t * context)
{
//    ERROR_XY_MODEQ;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_LOGIC,true);
//
//    CTriggerGetMsg *cgetm = CTriggerGetMsg::Instance();
//    paramCondition conditionValue(TLC_LESSER);
//    cgetm->exec(TR_CONDITION, &conditionValue, TRIG_TYPE_LOGIC);
//    switch(conditionValue.cnd)
//    {
//    case TLC_LESSER:
//        SCPI_ResultString(context,trig_log_cond[0]);
//        break;
//    case TLC_GREATER:
//        SCPI_ResultString(context,trig_log_cond[1]);
//        break;
//    case TLC_EQUAL:
//        SCPI_ResultString(context,trig_log_cond[2]);
//        break;
//    case TLC_NOT_EQUAL:
//        SCPI_ResultString(context,trig_log_cond[3]);
//        break;
//    case TLC_TRUE:
//        SCPI_ResultString(context,trig_log_cond[4]);
//        break;
//    case TLC_FALSE:
//        SCPI_ResultString(context,trig_log_cond[5]);
//        break;
//    default:
//        break;
//    }

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}


//?????????????
scpi_result_t TRIG_LOG_TIME(scpi_t * context)
{
//    ERROR_XY_MODE;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_LOGIC,false);

    double param1;
    if (!SCPI_ParamDouble(context, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
//    if (param1>10 || param1<0.0000002)return SCPI_RES_ERR;
//
//
//    SCPI_CLOSE_MENU;
//    CTriggerSetMsg *csetm = CTriggerSetMsg::Instance();
//    paramTrigWidth trigWidthValue(param1*1e9);
//    csetm->exec(TR_WITDH, &trigWidthValue, TRIG_TYPE_LOGIC);
    setParam_1Double(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//?????????????
scpi_result_t TRIG_LOG_TIMEQ(scpi_t * context)
{
//    ERROR_XY_MODEQ;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_LOGIC,true);
//    CTriggerGetMsg *cgetm = CTriggerGetMsg::Instance();
//    paramTrigWidth trigWidthValue(0);
//    cgetm->exec(TR_WITDH, &trigWidthValue, TRIG_TYPE_LOGIC);
//    SCPI_ResultDouble(context,trigWidthValue.width*1e-9);

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//?????????????????????????
scpi_result_t TRIG_LOG_LEV(scpi_t * context)
{
//    ERROR_XY_MODE;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_LOGIC,false);
    int param1;
    if (!SCPI_ParamChoice(context, allCh, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
//    CH_IDX idx = (CH_IDX)getCh(param1);
//    if(!Channel::isDynamicCh(idx)) {
//        return SCPI_RES_ERR;
//    }

    double param2;
    if (!SCPI_ParamDouble(context, &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }

//    int ch = TriggerLevel::
//                      IModuleTriggerLevel::
//                      instanceId(idx, TriggerLevel::TT_NORMAL);
//    SCPI_CLOSE_MENU;
//    setLevelPos(context,ch,param2);
    setParam_1Int1Double(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
//?????????????????????????
scpi_result_t TRIG_LOG_PLUS_LEV(scpi_t * context)
{
//    ERROR_XY_MODE;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_LOGIC,false);
    int param1;
    if (!SCPI_ParamChoice(context, allCh, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
//    CH_IDX idx = (CH_IDX)getCh(param1);
//    if(!Channel::isDynamicCh(idx)) {
//        return SCPI_RES_ERR;
//    }

    int param2;
    if (!SCPI_ParamInt(context, &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }

//    int ch = TriggerLevel::
//                      IModuleTriggerLevel::
//                      instanceId(idx, TriggerLevel::TT_NORMAL);
//    SCPI_CLOSE_MENU;
//    setLevelPosPLUS(context,ch,param2);
    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//????????????????????????
scpi_result_t TRIG_LOG_LEVQ(scpi_t * context)
{
//    ERROR_XY_MODEQ;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_LOGIC,true);
    int param1;
    if (!SCPI_ParamChoice(context, allCh, &param1, true)) {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR PARAM!");
        return SCPI_RES_ERR;
    }
//    CH_IDX idx = (CH_IDX)getCh(param1);
//    if(!Channel::isDynamicCh(idx)) {
//        return SCPI_RES_ERR;
//    }

//    int ch = TriggerLevel::
//                      IModuleTriggerLevel::
//                      instanceId(idx, TriggerLevel::TT_NORMAL);
//    getLevelPos(context,ch);
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//Trigger dwart
//?????????????????
scpi_result_t TRIG_DWAR_SOUR(scpi_t * context)
{
//    ERROR_XY_MODE;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_LOW_PULSE,false);
//    SCPI_GET_PARAM1_CHIDX;
//    CTriggerGetMsg         *cgetm = CTriggerGetMsg::Instance();
//    paramCh chValue(MenuDPulseFrame::SOURCE_CH1);
//    cgetm->exec(TR_CH, &chValue, TRIG_TYPE_LOW_PULSE);
//    if(chValue.src1_ch==idx)return SCPI_RES_OK;
//
//    SCPI_CLOSE_MENU;
//    CTriggerSetMsg         *csetm = CTriggerSetMsg::Instance();
//    chValue.src1_ch=idx;
//    csetm->exec(TR_CH, &chValue, TRIG_TYPE_LOW_PULSE);
//    //??trigger level??
//    CTriggerLevelSwitchMsg *cswitchm = CTriggerLevelSwitchMsg::Instance();
//    int highInstance = TriggerLevel::IModuleTriggerLevel::instanceId(idx, TriggerLevel::TT_High);
//    int lowInstance = TriggerLevel::IModuleTriggerLevel::instanceId(idx, TriggerLevel::TT_LOW);
//    gMainWindow->hideAllTrig();
//    cswitchm->exec(lowInstance);
//    cswitchm->exec(highInstance);
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

//?????????????????
scpi_result_t TRIG_DWAR_SOURQ(scpi_t * context)
{
//    ERROR_XY_MODEQ;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_LOW_PULSE,true);
//
//    CTriggerGetMsg         *cgetm = CTriggerGetMsg::Instance();
//    paramCh chValue(MenuDPulseFrame::SOURCE_CH1);
//    cgetm->exec(TR_CH, &chValue, TRIG_TYPE_LOW_PULSE);
//    switch(chValue.src1_ch)
//    {
//    case MenuDPulseFrame::SOURCE_CH1:
//        SCPI_ResultString(context,"CH1");
//        break;
//    case MenuDPulseFrame::SOURCE_CH2:
//        SCPI_ResultString(context,"CH2");
//        break;
//    case MenuDPulseFrame::SOURCE_CH3:
//        SCPI_ResultString(context,"CH3");
//        break;
//    case MenuDPulseFrame::SOURCE_CH4:
//        SCPI_ResultString(context,"CH4");
//        break;
//    default:
//        DEBUG_RETURN("ERROR UNKNOW!");
//        break;
//    }

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}


const char * trig_dwar_pol[] = {
    "POSItive",
    "NEGAtive",
    "EITHer",
    NULL
};
//????????????????弫??
scpi_result_t TRIG_DWAR_POL(scpi_t * context)
{
//    ERROR_XY_MODE;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_LOW_PULSE,false);

    int param1;
    if (!SCPI_ParamChoice(context, trig_dwar_pol, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }

//    TrigglePolarity pl=TRIG_POLARITY_POSITIVE;
//    switch(param1)
//    {
//    case 0:
//        pl=TRIG_POLARITY_DWARF_POSITIVE;
//        break;
//    case 1:
//        pl=TRIG_POLARITY_DWARF_NEGATIVE;
//        break;
//    case 2:
//        pl=TRIG_POLARITY_DWARF_EITHER;
//        break;
//    default:
//        break;
//    }
//
//    SCPI_CLOSE_MENU;
//    CTriggerSetMsg *csetm = CTriggerSetMsg::Instance();
//    paramPolality polalityValue(pl);
//    csetm->exec(TR_POLARITY, &polalityValue, TRIG_TYPE_LOW_PULSE);
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;

}

//????????????????弫??
scpi_result_t TRIG_DWAR_POLQ(scpi_t * context)
{
//    ERROR_XY_MODEQ;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_LOW_PULSE,true);
//
//    CTriggerGetMsg *cgetm = CTriggerGetMsg::Instance();
//    paramPolality polalityValue(TRIG_POLARITY_DWARF_POSITIVE);
//    cgetm->exec(TR_POLARITY, &polalityValue, TRIG_TYPE_LOW_PULSE);
//
//    switch(polalityValue.pl)
//    {
//    case TRIG_POLARITY_DWARF_POSITIVE:
//        SCPI_ResultString(context,trig_dwar_pol[0]);
//        break;
//    case TRIG_POLARITY_DWARF_NEGATIVE:
//        SCPI_ResultString(context,trig_dwar_pol[1]);
//        break;
//    case TRIG_POLARITY_DWARF_EITHER:
//        SCPI_ResultString(context,trig_dwar_pol[2]);
//        break;
//    default:
//        break;
//    }

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

const char * trig_dwar_cond[] = {
    "LESS",
    "GREAt",
    "BETWeen",
    "NONE",
    NULL
};
//????????????????
scpi_result_t TRIG_DWAR_COND(scpi_t * context)
{
//    ERROR_XY_MODE;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_LOW_PULSE,false);

    int param1;
    if (!SCPI_ParamChoice(context, trig_dwar_cond, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }

//    DPulseOperator op=DTPO_LESSER;
//    switch(param1)
//    {
//    case 0:
//        op=DTPO_LESSER;
//        break;
//    case 1:
//        op=DTPO_GREATER;
//        break;
//    case 2:
//        op=DTPO_BETWEEN;
//        break;
//    case 3:
//        op=DTPO_NONE;
//        break;
//    default:
//        break;
//    }
//
//    SCPI_CLOSE_MENU;
//    CTriggerSetMsg *csetm = CTriggerSetMsg::Instance();
//    paramCondition conditionValue(op);
//    csetm->exec(TR_CONDITION, &conditionValue, TRIG_TYPE_LOW_PULSE);
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//???????????????
scpi_result_t TRIG_DWAR_CONDQ(scpi_t * context)
{
//    ERROR_XY_MODEQ;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_LOW_PULSE,true);
//
//    CTriggerGetMsg *cgetm = CTriggerGetMsg::Instance();
//    paramCondition conditionValue(DTPO_LESSER);
//    cgetm->exec(TR_CONDITION, &conditionValue, TRIG_TYPE_LOW_PULSE);
//
//    switch(conditionValue.cnd)
//    {
//    case DTPO_LESSER:
//        SCPI_ResultString(context,trig_dwar_cond[0]);
//        break;
//    case DTPO_GREATER:
//        SCPI_ResultString(context,trig_dwar_cond[1]);
//        break;
//    case DTPO_BETWEEN:
//        SCPI_ResultString(context,trig_dwar_cond[2]);
//        break;
//    case DTPO_NONE:
//        SCPI_ResultString(context,trig_dwar_cond[3]);
//        break;
//    default:
//        break;
//    }

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//?????????????????????
scpi_result_t TRIG_DWAR_HTIM(scpi_t * context)
{
//    ERROR_XY_MODE;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_LOW_PULSE,false);
//    SCPI_CHECK_CONDITION(TRIG_TYPE_LOW_PULSE,DTPO_LESSER,false);
    double param1;
    if (!SCPI_ParamDouble(context, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
//    SCPI_CLOSE_MENU;
//    SCPI_SET_BETWEEN_VALUE(param1,8,10e9);
//    CTriggerSetMsg *csetm = CTriggerSetMsg::Instance();
//    CTriggerGetMsg *cgetm = CTriggerGetMsg::Instance();
//    paramTrigWidth trigWidthValue(-1,param1);
//    paramTrigWidth temp;
//    cgetm->exec(TR_WITDH, &temp, TRIG_TYPE_LOW_PULSE);
//    if(temp.width>param1)
//    {
//        trigWidthValue.width=param1;
//    }
//    csetm->exec(TR_WITDH, &trigWidthValue, TRIG_TYPE_LOW_PULSE);
    setParam_1Double(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//?????????????????????
scpi_result_t TRIG_DWAR_HTIMQ(scpi_t * context)
{
//    ERROR_XY_MODEQ;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_LOW_PULSE,true);
//    SCPI_CHECK_CONDITION(TRIG_TYPE_LOW_PULSE,DTPO_LESSER,true);
//    CTriggerGetMsg *cgetm = CTriggerGetMsg::Instance();
//    paramTrigWidth trigWidthValue(-1,-1);
//    cgetm->exec(TR_WITDH, &trigWidthValue, TRIG_TYPE_LOW_PULSE);
//    SCPI_ResultDouble(context,trigWidthValue.widthMax);

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//?????????????????????
scpi_result_t TRIG_DWAR_LTIM(scpi_t * context)
{
//    ERROR_XY_MODE;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_LOW_PULSE,false);
//    SCPI_CHECK_CONDITION(TRIG_TYPE_LOW_PULSE,DTPO_GREATER,false);
    double param1;
    if (!SCPI_ParamDouble(context, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
//    SCPI_CLOSE_MENU;
//    SCPI_SET_BETWEEN_VALUE(param1,8,10e9);
//    CTriggerSetMsg *csetm = CTriggerSetMsg::Instance();
//    CTriggerGetMsg *cgetm = CTriggerGetMsg::Instance();
//    paramTrigWidth trigWidthValue(param1,-1);
//    paramTrigWidth temp;
//    cgetm->exec(TR_WITDH, &temp, TRIG_TYPE_LOW_PULSE);
//    if(temp.widthMax<param1)
//    {
//        trigWidthValue.widthMax=param1;
//    }
//    csetm->exec(TR_WITDH, &trigWidthValue, TRIG_TYPE_LOW_PULSE);
    setParam_1Double(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//?????????????????????
scpi_result_t TRIG_DWAR_LTIMQ(scpi_t * context)
{
//    ERROR_XY_MODEQ;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_LOW_PULSE,true);
//    SCPI_CHECK_CONDITION(TRIG_TYPE_LOW_PULSE,DTPO_GREATER,true);
//    CTriggerGetMsg *cgetm = CTriggerGetMsg::Instance();
//    paramTrigWidth trigWidthValue(8,-1);
//    cgetm->exec(TR_WITDH, &trigWidthValue, TRIG_TYPE_LOW_PULSE);
//    SCPI_ResultDouble(context,trigWidthValue.width);

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//?????????????????????
scpi_result_t TRIG_DWAR_BTIM(scpi_t * context)
{
//    ERROR_XY_MODEQ;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_LOW_PULSE,false);
//    SCPI_CHECK_CONDITION(TRIG_TYPE_LOW_PULSE,DTPO_BETWEEN,false);
    double param1;
    if (!SCPI_ParamDouble(context, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    double param2;
    if (!SCPI_ParamDouble(context, &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    if(param1<param2)
    {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    //?????????s???λ??????ns
    double param3=param1*1000000000;
    double param4=param2*1000000000;
    if (param3<8 || param3>10000000000 || param4<8 ||param4>10000000000)
    {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }

//    SCPI_CLOSE_MENU;
//    CTriggerSetMsg *csetm = CTriggerSetMsg::Instance();
//    paramTrigWidth trigWidthValue(param2,param1);
//    csetm->exec(TR_WITDH, &trigWidthValue, TRIG_TYPE_LOW_PULSE);
    setParam_2Double(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

const char * trig_dwar_btim[] = {
    "HIGH",
    "LOW",
    NULL
};
//??????????????????????????
scpi_result_t TRIG_DWAR_BTIMQ(scpi_t * context)
{
//        ERROR_XY_MODEQ;
//        SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_LOW_PULSE,true);
//        SCPI_CHECK_CONDITION(TRIG_TYPE_LOW_PULSE,DTPO_BETWEEN,true);

        int param1;
        if (!SCPI_ParamChoice(context, trig_dwar_btim, &param1, true)) {
            dealCallBack_ParamError(context);
            return SCPI_RES_ERR;
        }

//        CTriggerGetMsg *cgetm = CTriggerGetMsg::Instance();
//        paramTrigWidth trigWidthValue(-1,-1);
//        cgetm->exec(TR_WITDH, &trigWidthValue, TRIG_TYPE_LOW_PULSE);
//        switch(param1)
//        {
//        case 0:
//            SCPI_ResultDouble(context,trigWidthValue.widthMax);
//            return SCPI_RES_OK;
//        case 1:
//            SCPI_ResultDouble(context,trigWidthValue.width);
//            return SCPI_RES_OK;
//        default:
//            break;
//        }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//?????????????????
scpi_result_t TRIG_DWAR_HLEV(scpi_t * context)
{
//    ERROR_XY_MODE;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_LOW_PULSE,false);
    double param1;
    if (!SCPI_ParamDouble(context, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }

//    SCPI_CLOSE_MENU;
//    CTriggerGetMsg         *cgetm = CTriggerGetMsg::Instance();
//    paramCh chValue(MenuDPulseFrame::SOURCE_CH1);
//    cgetm->exec(TR_CH, &chValue, TRIG_TYPE_LOW_PULSE);
//    int highInstance = IModuleTriggerLevel::instanceId((CH_IDX)chValue.src1_ch, TT_High);
//
//    setLevelPos(context,highInstance,param1);
//    setAnotherLev(chValue.src1_ch,TT_LOW);
    setParam_1Double(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
//?????????????????
scpi_result_t TRIG_DWAR_PLUS_HLEV(scpi_t * context)
{
//    ERROR_XY_MODE;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_LOW_PULSE,false);
    int param1;
    if (!SCPI_ParamInt(context, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }

//    SCPI_CLOSE_MENU;
//    CTriggerGetMsg         *cgetm = CTriggerGetMsg::Instance();
//    paramCh chValue(MenuDPulseFrame::SOURCE_CH1);
//    cgetm->exec(TR_CH, &chValue, TRIG_TYPE_LOW_PULSE);
//    int highInstance = IModuleTriggerLevel::instanceId((CH_IDX)chValue.src1_ch, TT_High);
//
//    setLevelPosPLUS(context,highInstance,param1);
//    setAnotherLev(chValue.src1_ch,TT_LOW);
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//?????????????????
scpi_result_t TRIG_DWAR_HLEVQ(scpi_t * context)
{
//    ERROR_XY_MODEQ;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_LOW_PULSE,true);
//    CTriggerGetMsg         *cgetm = CTriggerGetMsg::Instance();
//    paramCh chValue(MenuDPulseFrame::SOURCE_CH1);
//    cgetm->exec(TR_CH, &chValue, TRIG_TYPE_LOW_PULSE);
//    int highInstance = IModuleTriggerLevel::instanceId((CH_IDX)chValue.src1_ch, TT_High);
//    getLevelPos(context,highInstance);

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//?????????????????
scpi_result_t TRIG_DWAR_LLEV(scpi_t * context)
{
//    ERROR_XY_MODE;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_LOW_PULSE,false);
    double param1;
    if (!SCPI_ParamDouble(context, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }

//    SCPI_CLOSE_MENU;
//    CTriggerGetMsg         *cgetm = CTriggerGetMsg::Instance();
//    paramCh chValue(MenuDPulseFrame::SOURCE_CH1);
//    cgetm->exec(TR_CH, &chValue, TRIG_TYPE_LOW_PULSE);
//    int highInstance = IModuleTriggerLevel::instanceId((CH_IDX)chValue.src1_ch, TT_LOW);
//
//    setLevelPos(context,highInstance,param1);
//    setAnotherLev(chValue.src1_ch,TT_High);
    setParam_1Double(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
//?????????????????
scpi_result_t TRIG_DWAR_PLUS_LLEV(scpi_t * context)
{
//    ERROR_XY_MODE;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_LOW_PULSE,false);
    int param1;
    if (!SCPI_ParamInt(context, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }

//    SCPI_CLOSE_MENU;
//    CTriggerGetMsg         *cgetm = CTriggerGetMsg::Instance();
//    paramCh chValue(MenuDPulseFrame::SOURCE_CH1);
//    cgetm->exec(TR_CH, &chValue, TRIG_TYPE_LOW_PULSE);
//    int highInstance = IModuleTriggerLevel::instanceId((CH_IDX)chValue.src1_ch, TT_LOW);
//
//    setLevelPosPLUS(context,highInstance,param1);
//    setAnotherLev(chValue.src1_ch,TT_High);
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//?????????????????
scpi_result_t TRIG_DWAR_LLEVQ(scpi_t * context)
{
//    ERROR_XY_MODEQ;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_LOW_PULSE,true);
//    CTriggerGetMsg         *cgetm = CTriggerGetMsg::Instance();
//    paramCh chValue(MenuDPulseFrame::SOURCE_CH1);
//    cgetm->exec(TR_CH, &chValue, TRIG_TYPE_LOW_PULSE);
//    int highInstance = IModuleTriggerLevel::instanceId((CH_IDX)chValue.src1_ch, TT_LOW);
//    getLevelPos(context,highInstance);

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}


//Trigger slope
//????б???????????
scpi_result_t TRIG_SLOP_SOUR(scpi_t * context)
{
//
// //Q_UNUSED(context);

//    ERROR_XY_MODE;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_SLOPE,false);
//    SCPI_GET_PARAM1_CHIDX;
//    CTriggerGetMsg         *cgetm = CTriggerGetMsg::Instance();
//    paramCh chValue(MenuSlopeFrame::SOURCE_CH1);
//    cgetm->exec(TR_CH, &chValue, TRIG_TYPE_SLOPE);
//    if(chValue.src1_ch==idx)return SCPI_RES_OK;
//
//    SCPI_CLOSE_MENU;
//    CTriggerSetMsg         *csetm = CTriggerSetMsg::Instance();
//    chValue.src1_ch=idx;
//    csetm->exec(TR_CH, &chValue, TRIG_TYPE_SLOPE);
//    //??trigger level??
//    CTriggerLevelSwitchMsg *cswitchm = CTriggerLevelSwitchMsg::Instance();
//    int highInstance = TriggerLevel::IModuleTriggerLevel::instanceId(idx, TriggerLevel::TT_High);
//    int lowInstance = TriggerLevel::IModuleTriggerLevel::instanceId(idx, TriggerLevel::TT_LOW);
//    gMainWindow->hideAllTrig();
//    cswitchm->exec(lowInstance);
//    cswitchm->exec(highInstance);
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

//???б???????????
scpi_result_t TRIG_SLOP_SOURQ(scpi_t * context)
{
//    ERROR_XY_MODEQ;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_SLOPE,true);
//
//    CTriggerGetMsg         *cgetm = CTriggerGetMsg::Instance();
//    paramCh chValue(MenuSlopeFrame::SOURCE_CH1);
//    cgetm->exec(TR_CH, &chValue, TRIG_TYPE_SLOPE);
//    switch(chValue.src1_ch)
//    {
//    case MenuSlopeFrame::SOURCE_CH1:
//        SCPI_ResultString(context,"CH1");
//        break;
//    case MenuSlopeFrame::SOURCE_CH2:
//        SCPI_ResultString(context,"CH2");
//        break;
//    case MenuSlopeFrame::SOURCE_CH3:
//        SCPI_ResultString(context,"CH3");
//        break;
//    case MenuSlopeFrame::SOURCE_CH4:
//        SCPI_ResultString(context,"CH4");
//        break;
//    default:
//        DEBUG_RETURN("ERROR UNKNOW!");
//        break;
//    }

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

const char * trig_slop_edge[] = {
    "RISE",
    "FALL",
    "EITHer",
    NULL
};
//????б???????
scpi_result_t TRIG_SLOP_EDGE(scpi_t * context)
{
//    ERROR_XY_MODE;
//    IDescTriggerCommon &objTriggerCommon = IDescTrigger::triggerDesc(TRIG_TYPE_START_ID);
//    TriggerType trigType = objTriggerCommon.triggerType(0);
//    if(trigType!=TRIG_TYPE_SLOPE)
//    {
//        return SCPI_RES_ERR;
//    }

    int param1;
    if (!SCPI_ParamChoice(context, trig_slop_edge, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }

//    TrigglePolarity pl=TRIG_POLARITY_SLOP_POSITIVE;
//    switch(param1)
//    {
//    case 0:
//        pl=TRIG_POLARITY_SLOP_POSITIVE;
//        break;
//    case 1:
//        pl=TRIG_POLARITY_SLOP_NEGATIVE;
//        break;
//    case 2:
//        pl=TRIG_POLARITY_SLOP_EITHER;
//        break;
//    default:
//        break;
//    }
//
//    SCPI_CLOSE_MENU;
//    CTriggerSetMsg *csetm = CTriggerSetMsg::Instance();
//    paramPolality modValue(pl);
//    csetm->exec(TR_POLARITY, &modValue, TRIG_TYPE_SLOPE);
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//???б???????
scpi_result_t TRIG_SLOP_EDGEQ(scpi_t * context)
{
//    ERROR_XY_MODEQ;
//    IDescTriggerCommon &objTriggerCommon = IDescTrigger::triggerDesc(TRIG_TYPE_START_ID);
//    TriggerType trigType = objTriggerCommon.triggerType(0);
//    if(trigType!=TRIG_TYPE_SLOPE)
//    {
//        DEBUG_RETURN("ERROR TRIG TYPE!");
//        return SCPI_RES_ERR;
//    }
//
//    CTriggerGetMsg *cgetm = CTriggerGetMsg::Instance();
//    paramPolality modValue(TRIG_POLARITY_SLOP_POSITIVE);
//    cgetm->exec(TR_POLARITY, &modValue, TRIG_TYPE_SLOPE);
//    switch(modValue.pl)
//    {
//    case TRIG_POLARITY_SLOP_POSITIVE:
//        SCPI_ResultString(context,trig_slop_edge[0]);
//        break;
//    case TRIG_POLARITY_SLOP_NEGATIVE:
//        SCPI_ResultString(context,trig_slop_edge[1]);
//        break;
//    case TRIG_POLARITY_SLOP_EITHER:
//        SCPI_ResultString(context,trig_slop_edge[2]);
//        break;
//    default:
//        break;
//    }

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

const char * trig_slop_cond[] = {
    "LESS",
    "GREat",
    "BETWeen",
    NULL
};
//????б???????????????
scpi_result_t TRIG_SLOP_COND(scpi_t * context)
{
//    ERROR_XY_MODE;
//    IDescTriggerCommon &objTriggerCommon = IDescTrigger::triggerDesc(TRIG_TYPE_START_ID);
//    TriggerType trigType = objTriggerCommon.triggerType(0);
//    if(trigType!=TRIG_TYPE_SLOPE)
//    {
//        return SCPI_RES_ERR;
//    }

    int param1;
    if (!SCPI_ParamChoice(context, trig_slop_cond, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }

//    SlopeOperator op=TSLO_LESSER;
//    switch(param1)
//    {
//    case 0:
//        op=TSLO_LESSER;
//        break;
//    case 1:
//        op=TSLO_GREATER;
//        break;
//    case 2:
//        op=TSLO_BETWEEN;
//        break;
//    default:
//        break;
//    }
//
//    SCPI_CLOSE_MENU;
//    CTriggerSetMsg *csetm = CTriggerSetMsg::Instance();
//    paramCondition conditionValue(op);
//    csetm->exec(TR_CONDITION, &conditionValue, TRIG_TYPE_SLOPE);
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//???б???????????????
scpi_result_t TRIG_SLOP_CONDQ(scpi_t * context)
{
//    ERROR_XY_MODEQ;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_SLOPE,true);
//
//    CTriggerGetMsg *cgetm = CTriggerGetMsg::Instance();
//    paramCondition conditionValue(TSLO_LESSER);
//    cgetm->exec(TR_CONDITION, &conditionValue, TRIG_TYPE_SLOPE);
//    switch(conditionValue.cnd)
//    {
//    case TSLO_LESSER:
//        SCPI_ResultString(context,trig_slop_cond[0]);
//        break;
//    case TSLO_GREATER:
//        SCPI_ResultString(context,trig_slop_cond[1]);
//        break;
//    case TSLO_BETWEEN:
//        SCPI_ResultString(context,trig_slop_cond[2]);
//        break;
//    default:
//        break;
//    }

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//????б???????????????
scpi_result_t TRIG_SLOP_HTIM(scpi_t * context)
{
//    ERROR_XY_MODE;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_SLOPE,false);
//    SCPI_CHECK_CONDITION(TRIG_TYPE_SLOPE,TSLO_LESSER,false);
    double param1;
    if (!SCPI_ParamDouble(context, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
//    SCPI_CLOSE_MENU;
//    SCPI_SET_BETWEEN_VALUE(param1,8,10e9);
//    CTriggerSetMsg *csetm = CTriggerSetMsg::Instance();
//    CTriggerGetMsg *cgetm = CTriggerGetMsg::Instance();
//    paramTrigWidth trigWidthValue(-1,param1);
//    paramTrigWidth temp;
//    cgetm->exec(TR_WITDH, &temp, TRIG_TYPE_SLOPE);
//    if(temp.width>param1)
//    {
//        trigWidthValue.width=param1;
//    }
//    csetm->exec(TR_WITDH, &trigWidthValue, TRIG_TYPE_SLOPE);
    setParam_1Double(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//???б???????????????
scpi_result_t TRIG_SLOP_HTIMQ(scpi_t * context)
{
//    ERROR_XY_MODEQ;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_SLOPE,true);
//    SCPI_CHECK_CONDITION(TRIG_TYPE_SLOPE,TSLO_LESSER,true);
//    CTriggerGetMsg *cgetm = CTriggerGetMsg::Instance();
//    paramTrigWidth trigWidthValue(-1,10e9);
//    cgetm->exec(TR_WITDH, &trigWidthValue, TRIG_TYPE_SLOPE);
//    SCPI_ResultDouble(context,trigWidthValue.widthMax);

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//????б???????????????
scpi_result_t TRIG_SLOP_LTIM(scpi_t * context)
{
//    ERROR_XY_MODEQ;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_SLOPE,false);
//    SCPI_CHECK_CONDITION(TRIG_TYPE_SLOPE,TSLO_GREATER,false);
    double param1;
    if (!SCPI_ParamDouble(context, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
//    SCPI_CLOSE_MENU;
//    SCPI_SET_BETWEEN_VALUE(param1,8,10e9);
//    CTriggerSetMsg *csetm = CTriggerSetMsg::Instance();
//    CTriggerGetMsg *cgetm = CTriggerGetMsg::Instance();
//    paramTrigWidth trigWidthValue(param1,-1);
//    paramTrigWidth temp;
//    cgetm->exec(TR_WITDH, &temp, TRIG_TYPE_SLOPE);
//    if(temp.widthMax<param1)
//    {
//        trigWidthValue.widthMax=param1;
//    }
//    csetm->exec(TR_WITDH, &trigWidthValue, TRIG_TYPE_SLOPE);
    setParam_1Double(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//???б???????????????
scpi_result_t TRIG_SLOP_LTIMQ(scpi_t * context)
{
//    ERROR_XY_MODEQ;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_SLOPE,true);
//    SCPI_CHECK_CONDITION(TRIG_TYPE_SLOPE,TSLO_GREATER,true);
//    CTriggerGetMsg *cgetm = CTriggerGetMsg::Instance();
//    paramTrigWidth trigWidthValue(8,-1);
//    cgetm->exec(TR_WITDH, &trigWidthValue, TRIG_TYPE_SLOPE);
//    SCPI_ResultDouble(context,trigWidthValue.width);

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//????б???????????????
scpi_result_t TRIG_SLOP_BTIM(scpi_t * context)
{
//    ERROR_XY_MODEQ;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_SLOPE,false);
//    SCPI_CHECK_CONDITION(TRIG_TYPE_SLOPE,TSLO_BETWEEN,false);
    double param1;
    if (!SCPI_ParamDouble(context, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    double param2;
    if (!SCPI_ParamDouble(context, &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    //?????????s???λ??????ns
    double param3=param1*1000000000;
    double param4=param2*1000000000;
    if (param3<8 || param3>10000000000 || param4<8 ||param4>10000000000)
    {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;}

//    SCPI_CLOSE_MENU;
//    CTriggerSetMsg *csetm = CTriggerSetMsg::Instance();
//    paramTrigWidth trigWidthValue(param2,param1);
//    csetm->exec(TR_WITDH, &trigWidthValue, TRIG_TYPE_SLOPE);
    setParam_2Double(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

const char * trig_slop_btim[] = {
    "HIGH",
    "LOW",
    NULL
};
//???б????????????????????
scpi_result_t TRIG_SLOP_BTIMQ(scpi_t * context)
{
//        ERROR_XY_MODE;
//        SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_SLOPE,true);
//        SCPI_CHECK_CONDITION(TRIG_TYPE_SLOPE,TSLO_BETWEEN,true);

        int param1;
        if (!SCPI_ParamChoice(context, trig_slop_btim, &param1, true)) {
            dealCallBack_ParamError(context);
            return SCPI_RES_ERR;
        }

//        CTriggerGetMsg *cgetm = CTriggerGetMsg::Instance();
//        paramTrigWidth trigWidthValue(-1,-1);
//        cgetm->exec(TR_WITDH, &trigWidthValue, TRIG_TYPE_SLOPE);
//        switch(param1)
//        {
//        case 0:
//            SCPI_ResultDouble(context,trigWidthValue.widthMax);
//            return SCPI_RES_OK;
//        case 1:
//            SCPI_ResultDouble(context,trigWidthValue.width);
//            return SCPI_RES_OK;
//        default:
//            break;
//        }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//????б???????????
scpi_result_t TRIG_SLOP_HLEV(scpi_t * context)
{
//    ERROR_XY_MODE;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_SLOPE,false);
    double param1;
    if (!SCPI_ParamDouble(context, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }

//    CTriggerGetMsg         *cgetm = CTriggerGetMsg::Instance();
//    paramCh chValue(MenuDPulseFrame::SOURCE_CH1);
//    cgetm->exec(TR_CH, &chValue, TRIG_TYPE_SLOPE);
//    int highInstance = IModuleTriggerLevel::instanceId((CH_IDX)chValue.src1_ch, TT_High);
//
//    SCPI_CLOSE_MENU;
//    setLevelPos(context,highInstance,param1);
//    setAnotherLev(chValue.src1_ch,TT_LOW);
    setParam_1Double(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
//????б???????????
scpi_result_t TRIG_SLOP_PLUS_HLEV(scpi_t * context)
{
//    ERROR_XY_MODE;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_SLOPE,false);
    int param1;
    if (!SCPI_ParamInt(context, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }

//    CTriggerGetMsg         *cgetm = CTriggerGetMsg::Instance();
//    paramCh chValue(MenuDPulseFrame::SOURCE_CH1);
//    cgetm->exec(TR_CH, &chValue, TRIG_TYPE_SLOPE);
//    int highInstance = IModuleTriggerLevel::instanceId((CH_IDX)chValue.src1_ch, TT_High);
//
//    SCPI_CLOSE_MENU;
//    setLevelPosPLUS(context,highInstance,param1);
//    setAnotherLev(chValue.src1_ch,TT_LOW);
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//???б???????????
scpi_result_t TRIG_SLOP_HLEVQ(scpi_t * context)
{
//    ERROR_XY_MODEQ;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_SLOPE,true);
//    CTriggerGetMsg         *cgetm = CTriggerGetMsg::Instance();
//    paramCh chValue(MenuDPulseFrame::SOURCE_CH1);
//    cgetm->exec(TR_CH, &chValue, TRIG_TYPE_SLOPE);
//    int highInstance = IModuleTriggerLevel::instanceId((CH_IDX)chValue.src1_ch, TT_High);
//    getLevelPos(context,highInstance);

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//????б???????????
scpi_result_t TRIG_SLOP_PLUS_LLEV(scpi_t * context)
{
//    ERROR_XY_MODE;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_SLOPE,false);
    int param1;
    if (!SCPI_ParamInt(context, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }

//    CTriggerGetMsg         *cgetm = CTriggerGetMsg::Instance();
//    paramCh chValue(MenuDPulseFrame::SOURCE_CH1);
//    cgetm->exec(TR_CH, &chValue, TRIG_TYPE_SLOPE);
//    int highInstance = IModuleTriggerLevel::instanceId((CH_IDX)chValue.src1_ch, TT_LOW);
//
//    SCPI_CLOSE_MENU;
//    setLevelPosPLUS(context,highInstance,param1);
//    setAnotherLev(chValue.src1_ch,TT_High);
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//????б???????????
scpi_result_t TRIG_SLOP_LLEV(scpi_t * context)
{
//    ERROR_XY_MODE;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_SLOPE,false);
    double param1;
    if (!SCPI_ParamDouble(context, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }

//    CTriggerGetMsg         *cgetm = CTriggerGetMsg::Instance();
//    paramCh chValue(MenuDPulseFrame::SOURCE_CH1);
//    cgetm->exec(TR_CH, &chValue, TRIG_TYPE_SLOPE);
//    int highInstance = IModuleTriggerLevel::instanceId((CH_IDX)chValue.src1_ch, TT_LOW);
//
//    SCPI_CLOSE_MENU;
//    setLevelPos(context,highInstance,param1);
//    setAnotherLev(chValue.src1_ch,TT_High);
    setParam_1Double(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//???б???????????
scpi_result_t TRIG_SLOP_LLEVQ(scpi_t * context)
{
//    ERROR_XY_MODEQ;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_SLOPE,true);
//    CTriggerGetMsg         *cgetm = CTriggerGetMsg::Instance();
//    paramCh chValue(MenuDPulseFrame::SOURCE_CH1);
//    cgetm->exec(TR_CH, &chValue, TRIG_TYPE_SLOPE);
//    int highInstance = IModuleTriggerLevel::instanceId((CH_IDX)chValue.src1_ch, TT_LOW);
//    getLevelPos(context,highInstance);

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//Trigger timeout
//???ó????????????
scpi_result_t TRIG_TIM_SOUR(scpi_t * context)
{
//
// //Q_UNUSED(context);

//    ERROR_XY_MODE;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_TIMEOUT,false);
//    SCPI_GET_PARAM1_CHIDX;
//    setTrigSource(idx,TRIG_TYPE_TIMEOUT);
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

//????????????????
scpi_result_t TRIG_TIM_SOURQ(scpi_t * context)
{
//    ERROR_XY_MODE;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_TIMEOUT,true);
//    CTriggerGetMsg         *cgetm = CTriggerGetMsg::Instance();
//    paramCh chValue(MenuTimeOutFrame::SOURCE_CH1);
//    cgetm->exec(TR_CH, &chValue, TRIG_TYPE_TIMEOUT);
//    switch(chValue.src1_ch)
//    {
//    case MenuTimeOutFrame::SOURCE_CH1:
//        SCPI_ResultString(context,"CH1");
//        break;
//    case MenuTimeOutFrame::SOURCE_CH2:
//        SCPI_ResultString(context,"CH2");
//        break;
//    case MenuTimeOutFrame::SOURCE_CH3:
//        SCPI_ResultString(context,"CH3");
//        break;
//    case MenuTimeOutFrame::SOURCE_CH4:
//        SCPI_ResultString(context,"CH4");
//        break;
//    default:
//        DEBUG_RETURN("ERROR UNKNOW!");
//        break;
//    }

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

const char * trig_tim_pol[] = {
    "POSitive",
    "NEGative",
    "EITHer",
    NULL
};
//???ó??????????
scpi_result_t TRIG_TIM_POL(scpi_t * context)
{
//
// //Q_UNUSED(context);

//    ERROR_XY_MODE;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_TIMEOUT,false);

    int param1;
    if (!SCPI_ParamChoice(context, trig_tim_pol, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }

//    TrigglePolarity pl=TRIG_POLARITY_TIMEOUT_POSITIVE;
//    switch(param1)
//    {
//    case 0:
//        pl=TRIG_POLARITY_TIMEOUT_POSITIVE;
//        break;
//    case 1:
//        pl=TRIG_POLARITY_TIMEOUT_NEGATIVE;
//        break;
//    case 2:
//        pl=TRIG_POLARITY_TIMEOUT_EITHER;
//        break;
//    default:
//        break;
//    }
//
//    SCPI_CLOSE_MENU;
//    CTriggerSetMsg *csetm = CTriggerSetMsg::Instance();
//    paramPolality modValue(pl);
//    csetm->exec(TR_POLARITY, &modValue, TRIG_TYPE_TIMEOUT);
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//??????????????
scpi_result_t TRIG_TIM_POLQ(scpi_t * context)
{
//    ERROR_XY_MODE;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_TIMEOUT,true);
//    CTriggerGetMsg *cgetm = CTriggerGetMsg::Instance();
//    paramPolality modValue(TRIG_POLARITY_TIMEOUT_POSITIVE);
//    cgetm->exec(TR_POLARITY, &modValue, TRIG_TYPE_TIMEOUT);
//    switch(modValue.pl)
//    {
//    case TRIG_POLARITY_TIMEOUT_POSITIVE:
//        SCPI_ResultString(context,trig_tim_pol[0]);
//        break;
//    case TRIG_POLARITY_TIMEOUT_NEGATIVE:
//        SCPI_ResultString(context,trig_tim_pol[1]);
//        break;
//    case TRIG_POLARITY_TIMEOUT_EITHER:
//        SCPI_ResultString(context,trig_tim_pol[2]);
//        break;
//    default:
//        break;
//    }

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//???ó?????????????
scpi_result_t TRIG_TIM_TIME(scpi_t * context)
{
//    ERROR_XY_MODE;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_TIMEOUT,false);
    double param1;
    if (!SCPI_ParamDouble(context, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }

//    SCPI_CLOSE_MENU;
//    CTriggerSetMsg *csetm = CTriggerSetMsg::Instance();
//    paramTrigWidth trigWidthValue(param1*1e9);
//    csetm->exec(TR_WITDH, &trigWidthValue, TRIG_TYPE_TIMEOUT);
    setParam_1Double(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//?????????????????
scpi_result_t TRIG_TIM_TIMEQ(scpi_t * context)
{
//    ERROR_XY_MODE;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_TIMEOUT,true);
//    CTriggerGetMsg *cgetm = CTriggerGetMsg::Instance();
//    paramTrigWidth trigWidthValue(8);
//    cgetm->exec(TR_WITDH, &trigWidthValue, TRIG_TYPE_TIMEOUT);
//    SCPI_ResultDouble(context,trigWidthValue.width*1e-9);

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t TRIG_TIM_LEV(scpi_t * context){
    double param1;
    if (!SCPI_ParamDouble(context, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_1Double(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t TRIG_TIM_LEVQ(scpi_t * context){
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//Trigger nedge
//?????N?????????????
scpi_result_t TRIG_NEDG_SOUR(scpi_t * context)
{
//
// //Q_UNUSED(context);

//    ERROR_XY_MODE;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_NEDGE,false);
//    SCPI_GET_PARAM1_CHIDX;
//    setTrigSource(idx,TRIG_TYPE_NEDGE);
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

//?????N?????????????
scpi_result_t TRIG_NEDG_SOURQ(scpi_t * context)
{
//    ERROR_XY_MODE;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_NEDGE,true);
//    CTriggerGetMsg         *cgetm = CTriggerGetMsg::Instance();
//    paramCh chValue(MenuNEdgeFrame::SOURCE_CH1);
//    cgetm->exec(TR_CH, &chValue, TRIG_TYPE_NEDGE);
//    switch(chValue.src1_ch)
//    {
//    case MenuNEdgeFrame::SOURCE_CH1:
//        SCPI_ResultString(context,"CH1");
//        break;
//    case MenuNEdgeFrame::SOURCE_CH2:
//        SCPI_ResultString(context,"CH2");
//        break;
//    case MenuNEdgeFrame::SOURCE_CH3:
//        SCPI_ResultString(context,"CH3");
//        break;
//    case MenuNEdgeFrame::SOURCE_CH4:
//        SCPI_ResultString(context,"CH4");
//        break;
//    default:
//        DEBUG_RETURN("ERROR UNKNOW!");
//        break;
//    }

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

const char * trig_nedg_slop[] = {
    "RISE",
    "FALL",
    NULL
};
//?????N????????????????
scpi_result_t TRIG_NEDG_SLOP(scpi_t * context)
{
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_NEDGE,false);
    int param1;
    if (!SCPI_ParamChoice(context, trig_nedg_slop, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }

//    TrigglePolarity pl=TRIG_POLARITY_nEDGE_RAISE;
//    switch(param1)
//    {
//    case 0:
//        pl=TRIG_POLARITY_nEDGE_RAISE;
//        break;
//    case 1:
//        pl=TRIG_POLARITY_nEDGE_FALL;
//        break;
//    default:
//        break;
//    }
//
//    SCPI_CLOSE_MENU;
//    CTriggerSetMsg *csetm = CTriggerSetMsg::Instance();
//    paramPolality modValue(pl);
//    csetm->exec(TR_POLARITY, &modValue, TRIG_TYPE_NEDGE);
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//?????N????????????????
scpi_result_t TRIG_NEDG_SLOPQ(scpi_t * context)
{
//    ERROR_XY_MODE;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_NEDGE,true);
//    CTriggerGetMsg *cgetm = CTriggerGetMsg::Instance();
//    paramPolality modValue(TRIG_POLARITY_nEDGE_RAISE);
//    cgetm->exec(TR_POLARITY, &modValue, TRIG_TYPE_NEDGE);
//    switch(modValue.pl)
//    {
//    case TRIG_POLARITY_nEDGE_RAISE:
//        SCPI_ResultString(context,trig_nedg_slop[0]);
//        break;
//    case TRIG_POLARITY_nEDGE_FALL:
//        SCPI_ResultString(context,trig_nedg_slop[1]);
//        break;
//    default:
//        break;
//    }

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//?????N????????п???????????????????
scpi_result_t TRIG_NEDG_IDLE(scpi_t * context)
{
//    ERROR_XY_MODE;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_NEDGE,false);
    double param1;
    if (!SCPI_ParamDouble(context, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
//    if (param1>10 || param1<0.000000008)return SCPI_RES_ERR;

//    SCPI_CLOSE_MENU;
//    CTriggerSetMsg *csetm = CTriggerSetMsg::Instance();
//    paramTrigWidth trigWidthValue(param1*1e9, -1);//width????idle???widthMax?????N??
//    csetm->exec(TR_WITDH, &trigWidthValue, TRIG_TYPE_NEDGE);
    setParam_1Double(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//?????N????????п???????????????????
scpi_result_t TRIG_NEDG_IDLEQ(scpi_t * context)
{
//    ERROR_XY_MODE;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_NEDGE,true);
//    CTriggerGetMsg *cgetm = CTriggerGetMsg::Instance();
//    paramTrigWidth trigWidthValue(8, -1);//width????idle???widthMax?????N??
//    cgetm->exec(TR_WITDH, &trigWidthValue, TRIG_TYPE_NEDGE);
//    SCPI_ResultDouble(context,trigWidthValue.width*1e-9);

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//?????N?????????N?????
scpi_result_t TRIG_NEDG_EDGE(scpi_t * context)
{
//    ERROR_XY_MODE;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_NEDGE,false);
    int param1;
    if (!SCPI_ParamInt(context, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
//    if (param1<1 || param1>65536) return SCPI_RES_ERR;
//    SCPI_CLOSE_MENU;
//    CTriggerSetMsg *csetm = CTriggerSetMsg::Instance();
//    paramTrigWidth trigWidthValue(-1, param1);//width????idle???widthMax?????N??
//    csetm->exec(TR_WITDH, &trigWidthValue, TRIG_TYPE_NEDGE);
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//?????N?????????N?????
scpi_result_t TRIG_NEDG_EDGEQ(scpi_t * context)
{
//    ERROR_XY_MODE;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_NEDGE,true);
//    CTriggerGetMsg *cgetm = CTriggerGetMsg::Instance();
//    paramTrigWidth trigWidthValue(-1, 1);//width????idle???widthMax?????N??
//    cgetm->exec(TR_WITDH, &trigWidthValue, TRIG_TYPE_NEDGE);
//    SCPI_ResultInt(context,trigWidthValue.widthMax);

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//?????N????????????????
scpi_result_t TRIG_NEDG_PLUS_LEV(scpi_t * context)
{
//    ERROR_XY_MODE;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_NEDGE,false);

    int param1;
    if (!SCPI_ParamInt(context, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }

//    TriggerLevel::IModuleTriggerLevel *mt = NULL;
//    mt = TriggerLevel::IModuleTriggerLevel::createInstance();
//    int ch = mt->active();
//    setLevelPosPLUS(context,ch,param1);
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
//?????N????????????????
scpi_result_t TRIG_NEDG_LEV(scpi_t * context)
{
//    ERROR_XY_MODE;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_NEDGE,false);

    double param1;
    if (!SCPI_ParamDouble(context, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }

//    TriggerLevel::IModuleTriggerLevel *mt = NULL;
//    mt = TriggerLevel::IModuleTriggerLevel::createInstance();
//    int ch = mt->active();
//    setLevelPos(context,ch,param1);
    setParam_1Double(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//?????N????????????????
scpi_result_t TRIG_NEDG_LEVQ(scpi_t * context)
{
//    ERROR_XY_MODEQ;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_NEDGE,true);
//
//    TriggerLevel::IModuleTriggerLevel *mt = NULL;
//    mt = TriggerLevel::IModuleTriggerLevel::createInstance();
//    int ch = mt->active();
//    getLevelPos(context,ch);

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//Trigger video
//?????????????????
scpi_result_t TRIG_VID_SOUR(scpi_t * context)
{
//
// //Q_UNUSED(context);

//    ERROR_XY_MODE;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_VIDEO,false);
//    SCPI_GET_PARAM1_CHIDX;
//    SCPI_CLOSE_MENU;
//    CTriggerSetMsg         *csetm = CTriggerSetMsg::Instance();
//    paramCh chValue(idx);
//    csetm->exec(TR_CH, &chValue, TRIG_TYPE_VIDEO);
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

//????????????????
scpi_result_t TRIG_VID_SOURQ(scpi_t * context)
{
//    ERROR_XY_MODE;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_VIDEO,true);
//    CTriggerGetMsg         *cgetm = CTriggerGetMsg::Instance();
//    paramCh chValue(MenuVideoFrame::SOURCE_CH1);
//    cgetm->exec(TR_CH, &chValue, TRIG_TYPE_VIDEO);
//    switch(chValue.src1_ch)
//    {
//    case MenuVideoFrame::SOURCE_CH1:
//        SCPI_ResultString(context,"CH1");
//        break;
//    case MenuVideoFrame::SOURCE_CH2:
//        SCPI_ResultString(context,"CH2");
//        break;
//    case MenuVideoFrame::SOURCE_CH3:
//        SCPI_ResultString(context,"CH3");
//        break;
//    case MenuVideoFrame::SOURCE_CH4:
//        SCPI_ResultString(context,"CH4");
//        break;
//    default:
//        DEBUG_RETURN("ERROR UNKNOW!");
//        break;
//    }

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

const char * trig_vid_pol[] = {
    "POSitive",
    "NEGative",
    NULL
};
//????????????????
scpi_result_t TRIG_VID_POL(scpi_t * context)
{
//
// //Q_UNUSED(context);

//    ERROR_XY_MODE;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_VIDEO,false);

    int param1;
    if (!SCPI_ParamChoice(context, trig_vid_pol, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }

//    TrigglePolarity pl=TRIG_POLARITY_POSITIVE;
//    switch(param1)
//    {
//    case 0:
//        pl=TRIG_POLARITY_POSITIVE;
//        break;
//    case 1:
//        pl=TRIG_POLARITY_NEGATIVE;
//        break;
//    default:
//        break;
//    }
//
//    SCPI_CLOSE_MENU;
//    CTriggerSetMsg *csetm = CTriggerSetMsg::Instance();
//    paramPolality modValue(pl);
//    csetm->exec(TR_POLARITY, &modValue, TRIG_TYPE_VIDEO);
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//???????????????
scpi_result_t TRIG_VID_POLQ(scpi_t * context)
{
//    ERROR_XY_MODE;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_VIDEO,true);
//    CTriggerGetMsg *cgetm = CTriggerGetMsg::Instance();
//    paramPolality modValue(TRIG_POLARITY_POSITIVE);
//    cgetm->exec(TR_POLARITY, &modValue, TRIG_TYPE_VIDEO);
//    switch(modValue.pl)
//    {
//    case TRIG_POLARITY_POSITIVE:
//        SCPI_ResultString(context,trig_vid_pol[0]);
//        break;
//    case TRIG_POLARITY_NEGATIVE:
//        SCPI_ResultString(context,trig_vid_pol[1]);
//        break;
//    default:
//        break;
//    }

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

const char * trig_vid_stan[] = {
    "PAL",
    "SECam",
    "NTSC",
    "720P",
    "1080I",
    "1080P",
    NULL
};
//????????????????????
scpi_result_t TRIG_VID_STAN(scpi_t * context)
{
//
// //Q_UNUSED(context);

//    ERROR_XY_MODE;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_VIDEO,false);

    int param1;
    if (!SCPI_ParamChoice(context, trig_vid_stan, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }

//    VideoStandard st=TVS_625_PAL;
//    switch(param1)
//    {
//    case 0:
//        st=TVS_625_PAL;
//        break;
//    case 1:
//        st=TVS_SECAM;
//        break;
//    case 2:
//        st=TVS_525_NTSC;
//        break;
//    case 3:
//        st=TVS_720P;
//        break;
//    case 4:
//        printf("set stand 1080I\n");
//        st=TVS_1080I;
//        break;
//    case 5:
//        printf("set stand 1080P\n");
//        st=TVS_1080P;
//        break;
//    default:
//        break;
//    }
//
//    SCPI_CLOSE_MENU;
//    CTriggerSetMsg *csetm = CTriggerSetMsg::Instance();
//    paramStd  stdValue(st);
//    csetm->exec(TR_STANDARD, &stdValue, TRIG_TYPE_VIDEO);
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//???????????????????
scpi_result_t TRIG_VID_STANQ(scpi_t * context)
{
//    ERROR_XY_MODE;
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_VIDEO,true);
//    CTriggerGetMsg *cgetm = CTriggerGetMsg::Instance();
//    paramStd  stdValue(TVS_625_PAL);
//    cgetm->exec(TR_STANDARD, &stdValue, TRIG_TYPE_VIDEO);
//    switch(stdValue.std)
//    {
//    case TVS_625_PAL:
//        SCPI_ResultString(context,trig_vid_stan[0]);
//        break;
//    case TVS_SECAM:
//        SCPI_ResultString(context,trig_vid_stan[1]);
//        break;
//    case TVS_525_NTSC:
//        SCPI_ResultString(context,trig_vid_stan[2]);
//        break;
//    case TVS_720P:
//        SCPI_ResultString(context,trig_vid_stan[3]);
//        break;
//    case TVS_1080I:
//        SCPI_ResultString(context,trig_vid_stan[4]);
//        break;
//    case TVS_1080P:
//        SCPI_ResultString(context,trig_vid_stan[5]);
//        break;
//    default:
//        break;
//    }

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

const char * trig_vid_amod[] = {
    "ODDField",
    "EVENfield",
    "ALLField",
    "ALLLine",
    "LINE",
    NULL
};
//???????????PAL??SECAm??NESC??1080I?????????????????
scpi_result_t TRIG_VID_AMOD(scpi_t * context)
{
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_VIDEO,false);
//    CTriggerGetMsg *cgetm = CTriggerGetMsg::Instance();
//    paramStd  stdValue(TVS_625_PAL);
//    cgetm->exec(TR_STANDARD, &stdValue, TRIG_TYPE_VIDEO);
//    switch(stdValue.std)
//    {
//    case TVS_625_PAL:
//    case TVS_SECAM:
//    case TVS_525_NTSC:
//    case TVS_1080I:
//        break;
//    case TVS_720P:
//    case TVS_1080P:
//        return SCPI_RES_OK;
//        break;
//    default:
//        break;
//    }

    int param1;
    if (!SCPI_ParamChoice(context, trig_vid_amod, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }

//    SCPI_CLOSE_MENU;
//    VideoCondition cd=TVC_ODD_FIELD;
//    switch(param1)
//    {
//        case 0:cd=TVC_ODD_FIELD;break;
//        case 1:cd=TVC_EVEN_FIELD;break;
//        case 2:cd=TVC_ALL_FIELD;break;
//        case 3:cd=TVC_ALL_LINES;break;
//        case 4:cd=TVC_LINE;break;
//        default:return SCPI_RES_OK;
//    }
//
//    paramVideoTrig  triggerValue(cd);
//    CTriggerSetMsg *csetm = CTriggerSetMsg::Instance();
//    csetm->exec(TR_VIDEOTRIGGER, &triggerValue, TRIG_TYPE_VIDEO);
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//???????????PAL??SECAm??NESC??1080I?????????????????
scpi_result_t TRIG_VID_AMODQ(scpi_t * context)
{
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_VIDEO,true);
//    CTriggerGetMsg *cgetm = CTriggerGetMsg::Instance();
//    paramStd  stdValue(TVS_625_PAL);
//    cgetm->exec(TR_STANDARD, &stdValue, TRIG_TYPE_VIDEO);
//    switch(stdValue.std)
//    {
//    case TVS_625_PAL:
//    case TVS_SECAM:
//    case TVS_525_NTSC:
//    case TVS_1080I:
//        break;
//    case TVS_720P:
//    case TVS_1080P:
//    default:
//        DEBUG_RETURN("ERROR STANDARD!");
//        break;
//    }
//
//    paramVideoTrig  triggerValue(TVC_ODD_FIELD);
//    cgetm->exec(TR_VIDEOTRIGGER, &triggerValue, TRIG_TYPE_VIDEO);
//    printf("triggerValue=%d\n",triggerValue.trigPlace);
//    switch(triggerValue.trigPlace)
//    {
//        case TVC_ODD_FIELD:SCPI_ResultString(context,trig_vid_amod[0]);break;
//        case TVC_EVEN_FIELD:SCPI_ResultString(context,trig_vid_amod[1]);break;
//        case TVC_ALL_FIELD:SCPI_ResultString(context,trig_vid_amod[2]);break;
//        case TVC_ALL_LINES:SCPI_ResultString(context,trig_vid_amod[3]);break;
//        case TVC_LINE:SCPI_ResultString(context,trig_vid_amod[4]);break;
//        default:DEBUG_RETURN("ERROR CONDITION!");break;
//    }

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

const char * trig_vid_bmod[] = {
    "ALLField",
    "ALLLine",
    "LINE",
    NULL
};
//???????????720P??1080P?????????????????
scpi_result_t TRIG_VID_BMOD(scpi_t * context)
{
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_VIDEO,false);
//    CTriggerGetMsg *cgetm = CTriggerGetMsg::Instance();
//    paramStd  stdValue(TVS_625_PAL);
//    cgetm->exec(TR_STANDARD, &stdValue, TRIG_TYPE_VIDEO);
//    switch(stdValue.std)
//    {
//    case TVS_720P:
//    case TVS_1080P:
//        break;
//    case TVS_625_PAL:
//    case TVS_SECAM:
//    case TVS_525_NTSC:
//    case TVS_1080I:
//    default:
//        return SCPI_RES_OK;
//    }

    int param1;
    if (!SCPI_ParamChoice(context, trig_vid_bmod, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
//    SCPI_CLOSE_MENU;
//    MenuVideoFrame *temp=(MenuVideoFrame*)gMainWindow->GetMenuFrame_Help(MI_TRIGGER_VIDEO);
//    temp->setConfigTrigger(param1);

    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//???????????720P??1080P?????????????????
scpi_result_t TRIG_VID_BMODQ(scpi_t * context)
{
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_VIDEO,true);
//    CTriggerGetMsg *cgetm = CTriggerGetMsg::Instance();
//    paramStd  stdValue(TVS_625_PAL);
//    cgetm->exec(TR_STANDARD, &stdValue, TRIG_TYPE_VIDEO);
//    switch(stdValue.std)
//    {
//    case TVS_720P:
//    case TVS_1080P:
//        break;
//    case TVS_625_PAL:
//    case TVS_SECAM:
//    case TVS_525_NTSC:
//    case TVS_1080I:
//    default:
//        DEBUG_RETURN("ERROR STANDARD!");
//        return SCPI_RES_OK;
//    }
//
//    paramVideoTrig  triggerValue(TVC_ODD_FIELD);
//    cgetm->exec(TR_VIDEOTRIGGER, &triggerValue, TRIG_TYPE_VIDEO);
//    printf("triggerValue=%d\n",triggerValue.trigPlace);
//    switch(triggerValue.trigPlace)
//    {
//        case TVC_ALL_FIELD:SCPI_ResultString(context,trig_vid_bmod[0]);break;
//        case TVC_ALL_LINES:SCPI_ResultString(context,trig_vid_bmod[1]);break;
//        case TVC_LINE:SCPI_ResultString(context,trig_vid_bmod[2]);break;
//        default:DEBUG_RETURN("ERROR CONDITION!");break;
//    }

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

const char * trig_vid_afr[] = {
    "60Hz",
    "50Hz",
    "30Hz",
    "25Hz",
    "24Hz",
    NULL
};
//???????????720P??1080I????????????????
scpi_result_t TRIG_VID_AFR(scpi_t * context)
{
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_VIDEO,false);
//    CTriggerGetMsg *cgetm = CTriggerGetMsg::Instance();
//    paramStd  stdValue(TVS_625_PAL);
//    cgetm->exec(TR_STANDARD, &stdValue, TRIG_TYPE_VIDEO);
//    switch(stdValue.std)
//    {
//    case TVS_720P:
//    case TVS_1080I:
//        break;
//    case TVS_625_PAL:
//    case TVS_SECAM:
//    case TVS_525_NTSC:
//    case TVS_1080P:
//    default:
//        return SCPI_RES_OK;
//    }

    int param1;
    if (!SCPI_ParamChoice(context, trig_vid_afr, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
//    VideoFreq fr=TVF_60HZ ;
//    switch(param1)
//    {
//        case 0:fr=TVF_60HZ ;break;
//        case 1:fr=TVF_50HZ ;break;
//        default:return SCPI_RES_OK;
//    }
//    SCPI_CLOSE_MENU;
//    CTriggerSetMsg *csetm = CTriggerSetMsg::Instance();
//    paramFreq  freqValue(fr);
//    csetm->exec(TR_FREQ, &freqValue, TRIG_TYPE_VIDEO);
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//???????????720P??1080I????????????????
scpi_result_t TRIG_VID_AFRQ(scpi_t * context)
{
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_VIDEO,false);
//    CTriggerGetMsg *cgetm = CTriggerGetMsg::Instance();
//    paramStd  stdValue(TVS_625_PAL);
//    cgetm->exec(TR_STANDARD, &stdValue, TRIG_TYPE_VIDEO);
//    switch(stdValue.std)
//    {
//    case TVS_720P:
//    case TVS_1080I:
//        break;
//    case TVS_625_PAL:
//    case TVS_SECAM:
//    case TVS_525_NTSC:
//    case TVS_1080P:
//    default:
//        DEBUG_RETURN("ERROR STANDARD!");
//        return SCPI_RES_OK;
//    }
//    paramFreq  freqValue(TVF_60HZ);
//    cgetm->exec(TR_FREQ, &freqValue, TRIG_TYPE_VIDEO);
//    switch(freqValue.freqPlace)
//    {
//        case TVF_60HZ:SCPI_ResultString(context,trig_vid_afr[0]);break;
//        case TVF_50HZ:SCPI_ResultString(context,trig_vid_afr[1]);break;
//        default:DEBUG_RETURN("ERROR FRE!"); break;
//    }

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

const char * trig_vid_bfr[] = {
    "60Hz",
    "50Hz",
    "30Hz",
    "25Hz",
    "24Hz",
    NULL
};
//???????????1080P????????????????
scpi_result_t TRIG_VID_BFR(scpi_t * context)
{
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_VIDEO,false);
//    CTriggerGetMsg *cgetm = CTriggerGetMsg::Instance();
//    paramStd  stdValue(TVS_625_PAL);
//    cgetm->exec(TR_STANDARD, &stdValue, TRIG_TYPE_VIDEO);
//    switch(stdValue.std)
//    {
//    case TVS_1080P:
//        break;
//    case TVS_720P:
//    case TVS_1080I:
//    case TVS_625_PAL:
//    case TVS_SECAM:
//    case TVS_525_NTSC:
//    default:
//        return SCPI_RES_OK;
//    }

    int param1;
    if (!SCPI_ParamChoice(context, trig_vid_bfr, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }

//    VideoFreq fr=TVF_60HZ ;
//    switch(param1)
//    {
//        case 0:fr=TVF_60HZ ;break;
//        case 1:fr=TVF_50HZ ;break;
//        case 2:fr=TVF_30HZ ;break;
//        case 3:fr=TVF_25HZ ;break;
//        case 4:fr=TVF_24HZ ;break;
//        default:return SCPI_RES_OK;
//    }
//    SCPI_CLOSE_MENU;
//    CTriggerSetMsg *csetm = CTriggerSetMsg::Instance();
//    paramFreq  freqValue(fr);
//    csetm->exec(TR_FREQ, &freqValue, TRIG_TYPE_VIDEO);
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//???????????1080P????????????????
scpi_result_t TRIG_VID_BFRQ(scpi_t * context)
{
//    SCPI_CHECK_TRIG_TYPE(TRIG_TYPE_VIDEO,false);
//    CTriggerGetMsg *cgetm = CTriggerGetMsg::Instance();
//    paramStd  stdValue(TVS_625_PAL);
//    cgetm->exec(TR_STANDARD, &stdValue, TRIG_TYPE_VIDEO);
//    switch(stdValue.std)
//    {
//    case TVS_1080P:
//        break;
//    case TVS_720P:
//    case TVS_1080I:
//    case TVS_625_PAL:
//    case TVS_SECAM:
//    case TVS_525_NTSC:
//    default:
//        DEBUG_RETURN("ERROR STANDARD!");
//        return SCPI_RES_OK;
//    }
//    paramFreq  freqValue(TVF_60HZ);
//    cgetm->exec(TR_FREQ, &freqValue, TRIG_TYPE_VIDEO);
//    switch(freqValue.freqPlace)
//    {
//        case TVF_60HZ:SCPI_ResultString(context,trig_vid_bfr[0]);break;
//        case TVF_50HZ:SCPI_ResultString(context,trig_vid_bfr[1]);break;
//        case TVF_30HZ:SCPI_ResultString(context,trig_vid_bfr[2]);break;
//        case TVF_25HZ:SCPI_ResultString(context,trig_vid_bfr[3]);break;
//        case TVF_24HZ:SCPI_ResultString(context,trig_vid_bfr[4]);break;
//        default:DEBUG_RETURN("ERROR FRE!"); break;
//    }

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t TRIG_VID_LINE(scpi_t * context){
    int param1;
    if (!SCPI_ParamInt(context, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t TRIG_VID_LINEQ(scpi_t * context){
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//Trigger B
//???B??????????
scpi_result_t TRIG_B_SOUR(scpi_t * context){
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;}
//???B??????????
scpi_result_t TRIG_B_SOURQ(scpi_t * context){
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;}
//????B?????????б??
scpi_result_t TRIG_B_EDGE(scpi_t * context){
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;}
//???B?????????б??
scpi_result_t TRIG_B_EDGEQ(scpi_t * context){
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;}
//???B?????????
scpi_result_t TRIG_B_COUP(scpi_t * context){
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;}
//???B?????????
scpi_result_t TRIG_B_COUPQ(scpi_t * context){
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;}
//????B??????????????B??A??????/?????
scpi_result_t TRIG_B_SEQ(scpi_t * context){
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;}
//???B?????????????
scpi_result_t TRIG_B_SEQQ(scpi_t * context){
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;}
//????B?????????????
scpi_result_t TRIG_B_LEV(scpi_t * context){
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;}
//???B?????????????
scpi_result_t TRIG_B_LEVQ(scpi_t * context){
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;}

//Trigger setup
//?????????????????????????
scpi_result_t TRIG_SET_CLOC(scpi_t * context){
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;}
//?????????????????????????
scpi_result_t TRIG_SET_CLOCQ(scpi_t * context){
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;}
//??????????????????????????
scpi_result_t TRIG_SET_DATA(scpi_t * context){
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;}
//??????????????????????????
scpi_result_t TRIG_SET_DATAQ(scpi_t * context){
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;}
 //????????????????????????????
scpi_result_t TRIG_SET_CEDG(scpi_t * context){
             //Q_UNUSED(context);
     return SCPI_RES_OK;}
//????????????????????????????
scpi_result_t TRIG_SET_CEDGQ(scpi_t * context){
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;}
//????????????????????????
scpi_result_t TRIG_SET_STIM(scpi_t * context){
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;}
//????????????????????????
scpi_result_t TRIG_SET_STIMQ(scpi_t * context){
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;}
//????????????????????????
scpi_result_t TRIG_SET_HTIM(scpi_t * context){
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;}
//????????????????????????
scpi_result_t TRIG_SET_HTIMQ(scpi_t * context){
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;}
//?????????????????????????????
scpi_result_t TRIG_SET_CLEV(scpi_t * context){
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;}
//?????????????????????????????
scpi_result_t TRIG_SET_CLEVQ(scpi_t * context){
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;}
//??????????????????????????????
scpi_result_t TRIG_SET_DLEV(scpi_t * context){
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;}
//??????????????????????????????
scpi_result_t TRIG_SET_DLEVQ(scpi_t * context){
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;}

//Trigger uart
//????UART??????????
scpi_result_t TRIG_UART_SOUR(scpi_t * context){
    return querySerial(context);
}
//???UART??????????
scpi_result_t TRIG_UART_SOURQ(scpi_t * context){
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

const char * uart_type[]={
        "STARt",
        "STOP",
        "DATA",
        "0:DATA",
        "1:DATA",
        "X:DATA",
        "PARIty",
        NULL
};

scpi_result_t TRIG_UART_TYPE(scpi_t * context){
    int param1;
    if (!SCPI_ParamChoice(context, trig_serial_source, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    int param2;
    if (!SCPI_ParamChoice(context, uart_type, &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }

    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
//???UART?????????????
scpi_result_t TRIG_UART_TYPEQ(scpi_t * context){
    return querySerial(context);
}

const char * uart_Relation[]={
        "LESS",
        "GREAt",
        "EQUAl",
        "UNEQual",
        NULL
};
scpi_result_t TRIG_UART_REL(scpi_t * context){
    int param1;
    if (!SCPI_ParamChoice(context, trig_serial_source, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    int param2;
    if (!SCPI_ParamChoice(context, uart_Relation, &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
//??UART???????????????DATA??0:DATA??1:DATA??X:DATA??????UART??????????
scpi_result_t TRIG_UART_RELQ(scpi_t * context){
    return querySerial(context);
}
//??UART???????????????DATA??0:DATA??1:DATA??X:DATA???????UART????????????
scpi_result_t TRIG_UART_DATA(scpi_t * context){
    int param1;
    if (!SCPI_ParamChoice(context, trig_serial_source, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    const char* param = NULL;
    size_t len2=0;
    if  (!SCPI_ParamString(context,&param,&len2,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    std::string param2(param,len2);
    setParam_1Int1String(context->env,context->param,param1,param2.c_str());
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
//??UART???????????????DATA??0:DATA??1:DATA??X:DATA??????UART????????????
scpi_result_t TRIG_UART_DATAQ(scpi_t * context){
    return querySerial(context);
}
//????UART?????????????
scpi_result_t TRIG_UART_LEV(scpi_t * context){
    int param1;
    if (!SCPI_ParamChoice(context, trig_serial_source, &param1, true)) {
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
    return SCPI_RES_OK;
}
//???UART?????????????
scpi_result_t TRIG_UART_LEVQ(scpi_t * context){
    return querySerial(context);
}

//Trigger lin
//????LIN??????????
scpi_result_t TRIG_LIN_SOUR(scpi_t * context){
    return querySerial(context);
}
//???LIN??????????
scpi_result_t TRIG_LIN_SOURQ(scpi_t * context){
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

const char * lin_type[]={
        "SRISe",
        "FID",
        "IDATa",
        NULL
};
scpi_result_t TRIG_LIN_TYPE(scpi_t * context){
    int param1;
    if (!SCPI_ParamChoice(context, trig_serial_source, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    int param2;
    if (!SCPI_ParamChoice(context, lin_type, &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
//???LIN?????????????
scpi_result_t TRIG_LIN_TYPEQ(scpi_t * context){
    return querySerial(context);
}
//??LIN????????????FID??IDATa???????LIN?????????ID?
scpi_result_t TRIG_LIN_ID(scpi_t * context){
    int param1;
    if (!SCPI_ParamChoice(context, trig_serial_source, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    const char* param = NULL;
    size_t len2=0;
    if  (!SCPI_ParamString(context,&param,&len2,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    std::string param2(param,len2);
    setParam_1Int1String(context->env,context->param,param1,param2.c_str());
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
//??LIN????????????FID??IDATa??????LIN?????????ID?
scpi_result_t TRIG_LIN_IDQ(scpi_t * context){
    return querySerial(context);
}
//??LIN????????????IDATa???????LIN?????????????
scpi_result_t TRIG_LIN_DATA(scpi_t * context){
    int param1;
    if (!SCPI_ParamChoice(context, trig_serial_source, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    const char* param = NULL;
    size_t len2=0;
    if  (!SCPI_ParamString(context,&param,&len2,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    std::string param2(param,len2);
    setParam_1Int1String(context->env,context->param,param1,param2.c_str());
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;}
//??LIN????????????IDATa??????LIN?????????????
scpi_result_t TRIG_LIN_DATAQ(scpi_t * context){
    return querySerial(context);
}
//????LIN?????????????
scpi_result_t TRIG_LIN_LEV(scpi_t * context){
    int param1;
    if (!SCPI_ParamChoice(context, trig_serial_source, &param1, true)) {
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
    return SCPI_RES_OK;
}
//???LIN?????????????
scpi_result_t TRIG_LIN_LEVQ(scpi_t * context){
    return querySerial(context);
}

//Trigger can
//????CAN??????????
scpi_result_t TRIG_CAN_SOUR(scpi_t * context){
    return querySerial(context);
}
//???CAN??????????
scpi_result_t TRIG_CAN_SOURQ(scpi_t * context){
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
const char * can_type[]={
        "FSTArt",
        "RFID",
        "DFID",
        "RDID",
        "IDATa",
        "WRFR",
        "AERRor",
        "ACKError",
        "OVERload",
        NULL
};
scpi_result_t TRIG_CAN_TYPE(scpi_t * context){
    int param1;
    if (!SCPI_ParamChoice(context, trig_serial_source, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    int param2;
    if (!SCPI_ParamChoice(context, can_type, &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;}
//???CAN?????????????
scpi_result_t TRIG_CAN_TYPEQ(scpi_t * context){
    return querySerial(context);
}
//??CAN??????????????RFID??DFID??IDATa??RDID???????CAN?????????ID?
scpi_result_t TRIG_CAN_ID(scpi_t * context){
    int param1;
    if (!SCPI_ParamChoice(context, trig_serial_source, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    const char* param = NULL;
    size_t len2=0;
    if  (!SCPI_ParamString(context,&param,&len2,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    std::string param2(param,len2);
    setParam_1Int1String(context->env,context->param,param1,param2.c_str());
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
//??CAN??????????????RFID??DFID??IDATa??RDID??????CAN?????????ID?
scpi_result_t TRIG_CAN_IDQ(scpi_t * context){
    return querySerial(context);
}
//??CAN ??????????????IDATa???????CAN??????DLC?
const char* can_dlc[]={
        "0","1","2","3","4","5","6","7","8","12","16","20","24","32","48","64",NULL
};
scpi_result_t TRIG_CAN_DLC(scpi_t * context){
    int param1;
    if (!SCPI_ParamChoice(context, trig_serial_source, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
//    const char* param2=(char*)malloc(256);
//    memset((void*)param2,0,256);
//    size_t len2=0;
//    if  (!SCPI_ParamString(context,&param2,&len2,true)){
//        dealCallBack_ParamError(context);
//        return SCPI_RES_ERR;
//    }
    int param2;
    if (!SCPI_ParamChoice(context,can_dlc,&param2,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;}
//??CAN ??????????????IDATa??????CAN??????DLC?
scpi_result_t TRIG_CAN_DLCQ(scpi_t * context){
    return querySerial(context);
}
//??CAN ??????????????IDATa???????CAN??????????????
scpi_result_t TRIG_CAN_DATA(scpi_t * context){
    int param1;
    if (!SCPI_ParamChoice(context, trig_serial_source, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    const char* param =NULL;
    size_t len2=0;
    if  (!SCPI_ParamString(context,&param,&len2,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    std::string param2(param,len2);
    setParam_1Int1String(context->env,context->param,param1,param2.c_str());
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;}
//??CAN ??????????????IDATa??????CAN??????????????
scpi_result_t TRIG_CAN_DATAQ(scpi_t * context){
    return querySerial(context);
}
//????CAN?????????????
scpi_result_t TRIG_CAN_LEV(scpi_t * context){
    int param1;
    if (!SCPI_ParamChoice(context, trig_serial_source, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    double param2;
    if (!SCPI_ParamDouble(context,  &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_1Int1Double(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
//???CAN?????????????
scpi_result_t TRIG_CAN_LEVQ(scpi_t * context){
    return querySerial(context);
}

//Trigger spi
const char * spi_type[]={
        "CS",
        "DATA",
        "X:DATa",
        NULL
};
scpi_result_t TRIG_SPI_TYPE(scpi_t * context){
    int param1;
    if (!SCPI_ParamChoice(context, trig_serial_source, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    int param2;
    if (!SCPI_ParamChoice(context, spi_type, &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t TRIG_SPI_TYPEQ(scpi_t * context){
    return querySerial(context);
}
//????SPI????????????
scpi_result_t TRIG_SPI_DATA(scpi_t * context){
    int param1;
    if (!SCPI_ParamChoice(context, trig_serial_source, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    const char* param=NULL;
    size_t len2=0;
    if  (!SCPI_ParamString(context,&param,&len2,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    std::string param2(param,len2);
    setParam_1Int1String(context->env,context->param,param1,param2.c_str());
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
//???SPI????????????
scpi_result_t TRIG_SPI_DATAQ(scpi_t * context){
    return querySerial(context);
}
//????SPI??????????
scpi_result_t TRIG_SPI_SOUR(scpi_t * context){
    return querySerial(context);
}
//???SPI??????????
scpi_result_t TRIG_SPI_SOURQ(scpi_t * context){
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
//????SPI?????????????
scpi_result_t TRIG_SPI_CLKLEV(scpi_t * context){
    int param1;
    if (!SCPI_ParamChoice(context, trig_serial_source, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    double param2;
    if (!SCPI_ParamDouble(context,  &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_1Int1Double(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;}
//???SPI?????????????
scpi_result_t TRIG_SPI_CLKLEVQ(scpi_t * context){
    return querySerial(context);
}
scpi_result_t TRIG_SPI_DATLEV(scpi_t * context){
    int param1;
    if (!SCPI_ParamChoice(context, trig_serial_source, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    double param2;
    if (!SCPI_ParamDouble(context,  &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_1Int1Double(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;}
//???SPI?????????????
scpi_result_t TRIG_SPI_DATLEVQ(scpi_t * context){
    return querySerial(context);
}
scpi_result_t TRIG_SPI_CSLEV(scpi_t * context){
    int param1;
    if (!SCPI_ParamChoice(context, trig_serial_source, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    double param2;
    if (!SCPI_ParamDouble(context,  &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_1Int1Double(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;}
//???SPI?????????????
scpi_result_t TRIG_SPI_CSLEVQ(scpi_t * context){
    return querySerial(context);
}


//Trigger iic
//????IIC??????????
scpi_result_t TRIG_IIC_SOUR(scpi_t * context){
    return querySerial(context);}
//???IIC??????????
scpi_result_t TRIG_IIC_SOURQ(scpi_t * context){
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

const char * iic_type[]={
        "STARt",
        "STOP",
        "ACKLost",
        "RESTart",
        "NACKaddress",
        "FRAM1",
        "FRAM2",
        "RDATa",
        "WRITe10",
        NULL

};
scpi_result_t TRIG_IIC_TYPE(scpi_t * context){
    int param1;
    if (!SCPI_ParamChoice(context, trig_serial_source, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    int param2;
    if (!SCPI_ParamChoice(context, iic_type, &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
//???IIC?????????????
scpi_result_t TRIG_IIC_TYPEQ(scpi_t * context){
    return querySerial(context);
}
//??IIC?????????NACKaddress??FRAM1??FRAM2???????IIC???????????????
scpi_result_t TRIG_IIC_ADDR(scpi_t * context){
    int param1;
    if (!SCPI_ParamChoice(context, trig_serial_source, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    const char* param = NULL;
    size_t len2=0;
    if  (!SCPI_ParamString(context,&param,&len2,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    std::string param2(param,len2);
    setParam_1Int1String(context->env,context->param,param1,param2.c_str());
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
//??IIC?????????NACKaddress??FRAM1??FRAM2??????IIC???????????????
scpi_result_t TRIG_IIC_ADDRQ(scpi_t * context){
    return querySerial(context);
}

const char * iic_Relation[]={
        "LESS",
        "GREAt",
        "EQUAl",
        "UNEQual",
        NULL
};
scpi_result_t TRIG_IIC_REL(scpi_t * context){
    int param1;
    if (!SCPI_ParamChoice(context, trig_serial_source, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    int param2;
    if (!SCPI_ParamChoice(context, iic_Relation, &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
//??IIC?????????RDATa??????IIC???????????????
scpi_result_t TRIG_IIC_RELQ(scpi_t * context){
    return querySerial(context);
}
//??IIC?????????RDATa??FRAM1??FRAM2???????IIC????????????????
scpi_result_t TRIG_IIC_DATA1(scpi_t * context){
    int param1;
    if (!SCPI_ParamChoice(context, trig_serial_source, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    const char* param;
    size_t len2=0;
    if  (!SCPI_ParamString(context,&param,&len2,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    std::string param2(param,len2);
    setParam_1Int1String(context->env,context->param,param1,param2.c_str());
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
//??IIC?????????RDATa??FRAM1??FRAM2??????IIC????????????????
scpi_result_t TRIG_IIC_DATA1Q(scpi_t * context){
    return querySerial(context);
}
//??IIC?????????RDATa??FRAM1??FRAM2???????IIC????????????????
scpi_result_t TRIG_IIC_DATA2(scpi_t * context){
    int param1;
    if (!SCPI_ParamChoice(context, trig_serial_source, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    const char* param;
    size_t len2=0;
    if  (!SCPI_ParamString(context,&param,&len2,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    std::string param2(param,len2);
    setParam_1Int1String(context->env,context->param,param1,param2.c_str());
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
//??IIC?????????RDATa??FRAM1??FRAM2??????IIC????????????????
scpi_result_t TRIG_IIC_DATA2Q(scpi_t * context){
    return querySerial(context);
}
//????IIC?????????????
scpi_result_t TRIG_IIC_LEVCLK(scpi_t * context){
    int param1;
    if (!SCPI_ParamChoice(context, trig_serial_source, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    double param2;
    if (!SCPI_ParamDouble(context,  &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_1Int1Double(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;}
//???IIC?????????????
scpi_result_t TRIG_IIC_LEVCLKQ(scpi_t * context){
    return querySerial(context);
}
scpi_result_t TRIG_IIC_LEVDAT(scpi_t * context){
    int param1;
    if (!SCPI_ParamChoice(context, trig_serial_source, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    double param2;
    if (!SCPI_ParamDouble(context,  &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_1Int1Double(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;}
//???IIC?????????????
scpi_result_t TRIG_IIC_LEVDATQ(scpi_t * context){
    return querySerial(context);
}

//1553B
scpi_result_t  TRIG_1553B_SOUR(scpi_t * context){
    return querySerial(context);
}
scpi_result_t  TRIG_1553B_SOURQ(scpi_t * context){
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

const char * b1553_type[]={
        "CSSYnc",
        "DWSYnc",
        "CSWOrd",
        "RTADdress",
        "MERRor",
        "DWORd",
        "OPERror",
        "AERRor",
        NULL
};
scpi_result_t  TRIG_1553B_TYPE(scpi_t * context){
    int param1;
    if (!SCPI_ParamChoice(context, trig_serial_source, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    int param2;
    if (!SCPI_ParamChoice(context, b1553_type, &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t  TRIG_1553B_TYPEQ(scpi_t * context){
    return querySerial(context);
}
scpi_result_t  TRIG_1553B_CSWO(scpi_t * context){
    int param1;
    if (!SCPI_ParamChoice(context, trig_serial_source, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    const char* param;
    size_t len2=0;
    if  (!SCPI_ParamString(context,&param,&len2,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    std::string param2(param,len2);
    setParam_1Int1String(context->env,context->param,param1,param2.c_str());
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t  TRIG_1553B_CSWOQ(scpi_t * context){
    return querySerial(context);
}
scpi_result_t  TRIG_1553B_DWOR(scpi_t * context){
    int param1;
    if (!SCPI_ParamChoice(context, trig_serial_source, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    const char* param;
    size_t len2=0;
    if  (!SCPI_ParamString(context,&param,&len2,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    std::string param2(param,len2);
    setParam_1Int1String(context->env,context->param,param1,param2.c_str());
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t  TRIG_1553B_DWORQ(scpi_t * context){
    return querySerial(context);
}
scpi_result_t  TRIG_1553B_RTAD(scpi_t * context){
    int param1;
    if (!SCPI_ParamChoice(context, trig_serial_source, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    const char* param;
    size_t len2=0;
    if  (!SCPI_ParamString(context,&param,&len2,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    std::string param2(param,len2);
    setParam_1Int1String(context->env,context->param,param1,param2.c_str());
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t  TRIG_1553B_RTADQ(scpi_t * context){
    return querySerial(context);
}
scpi_result_t  TRIG_1553B_LEV(scpi_t * context){
    int param1;
    if (!SCPI_ParamChoice(context, trig_serial_source, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    double param2;
    if (!SCPI_ParamDouble(context,  &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_1Int1Double(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t  TRIG_1553B_LEVQ(scpi_t * context){
    return querySerial(context);
}
//429
scpi_result_t  TRIG_429_SOUR(scpi_t * context){
    return querySerial(context);
}
scpi_result_t  TRIG_429_SOURQ(scpi_t * context){
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

const char * a429_type[]={
        "WBEGin",
        "WEND",
        "LABEl",
        "SDI",
        "DATA",
        "SSM",
        "LSDI",
        "LDATa",
        "LSSM",
        "WERROr",
        "WINTerval",
        "VERRor",
        "AERRor",
        "ALL0",
        "ALL1",
        NULL
};
scpi_result_t  TRIG_429_TYPE(scpi_t * context){
    int param1;
    if (!SCPI_ParamChoice(context, trig_serial_source, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    int param2;
    if (!SCPI_ParamChoice(context, a429_type, &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t  TRIG_429_TYPEQ(scpi_t * context){
    return querySerial(context);
}
scpi_result_t  TRIG_429_WORD(scpi_t * context){
    int param1;
    if (!SCPI_ParamChoice(context, trig_serial_source, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    const char* param;
    size_t len2=0;
    if  (!SCPI_ParamString(context,&param,&len2,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    std::string param2(param,len2);
    setParam_1Int1String(context->env,context->param,param1,param2.c_str());
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

scpi_result_t  TRIG_429_WORDQ(scpi_t * context){
    return querySerial(context);
}

scpi_result_t  TRIG_429_LABEL(scpi_t * context){
    int param1;
    if (!SCPI_ParamChoice(context, trig_serial_source, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    const char* param;
    size_t len2=0;
    if  (!SCPI_ParamString(context,&param,&len2,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    std::string param2(param,len2);
    setParam_1Int1String(context->env,context->param,param1,param2.c_str());
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t  TRIG_429_LABELQ(scpi_t * context){
    return querySerial(context);
}
scpi_result_t  TRIG_429_SDI(scpi_t * context){
    int param1;
    if (!SCPI_ParamChoice(context, trig_serial_source, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    const char* param;
    size_t len2=0;
    if  (!SCPI_ParamString(context,&param,&len2,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    std::string param2(param,len2);
    setParam_1Int1String(context->env,context->param,param1,param2.c_str());
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t  TRIG_429_SDIQ(scpi_t * context){
    return querySerial(context);
}
scpi_result_t  TRIG_429_DATA(scpi_t * context){
    int param1;
    if (!SCPI_ParamChoice(context, trig_serial_source, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    const char* param;
    size_t len2=0;
    if  (!SCPI_ParamString(context,&param,&len2,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    std::string param2(param,len2);
    setParam_1Int1String(context->env,context->param,param1,param2.c_str());
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t  TRIG_429_DATAQ(scpi_t * context){
    return querySerial(context);
}
scpi_result_t  TRIG_429_SSM(scpi_t * context){
    int param1;
    if (!SCPI_ParamChoice(context, trig_serial_source, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    const char* param;
    size_t len2=0;
    if  (!SCPI_ParamString(context,&param,&len2,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    std::string param2(param,len2);
    setParam_1Int1String(context->env,context->param,param1,param2.c_str());
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t  TRIG_429_SSMQ(scpi_t * context){
    return querySerial(context);
}
scpi_result_t  TRIG_429_HLEV(scpi_t * context){
    int param1;
    if (!SCPI_ParamChoice(context, trig_serial_source, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    double param2;
    if (!SCPI_ParamDouble(context,  &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_1Int1Double(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t  TRIG_429_HLEVQ(scpi_t * context){
    return querySerial(context);
}


scpi_result_t  TRIG_429_LLEV(scpi_t * context){
    int param1;
    if (!SCPI_ParamChoice(context, trig_serial_source, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    double param2;
    if (!SCPI_ParamDouble(context,  &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_1Int1Double(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t  TRIG_429_LLEVQ(scpi_t * context){
    return querySerial(context);
}

