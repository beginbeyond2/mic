
#include "scpi_cmd_math.h"
#include <stdio.h>
#include <stdlib.h>
#include <cstring>
#include <string>
#include "scpi_cmd_calibrate.h"
#include "scpi_help.h"
#include "../../SCPICommandCallBackJava.h"
#include "../../Log.h"

bool setDualS1(scpi_t * context)
{
    int param1;
    if (!SCPI_ParamChoice(context, allCh, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }

    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
//    CH_IDX idx = (CH_IDX)getCh(param1);
//    if(!Channel::isDynamicCh(idx)) {
//        return false;
//    }
//    IModuleChannel *imc = IModuleChannel::createInstance();
//    if(false == imc->isChOpened(CI_MATH))
//    {
//        return false;
//    }
//    SCPI_CLOSE_MENU;
//    if(CMathWave::Instance()->GetMathType() == 1)
//    {
//        switch(idx)
//        {
//        case CI_CH1:
//            gMainWindow->GetMenuMath()->on_btnS1CH1_clicked();
//            break;
//        case CI_CH2:
//            gMainWindow->GetMenuMath()->on_btnS1CH2_clicked();
//            break;
//        case CI_CH3:
//            gMainWindow->GetMenuMath()->on_btnS1CH3_clicked();
//            break;
//        case CI_CH4:
//            gMainWindow->GetMenuMath()->on_btnS1CH4_clicked();
//            break;
//        default:
//            return false;
//            break;
//        }
//    }
    return true;
}

bool setDualS2(scpi_t * context)
{
    int param1;
    if (!SCPI_ParamChoice(context, allCh, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
//    CH_IDX idx = (CH_IDX)getCh(param1);
//    if(!Channel::isDynamicCh(idx)) {
//        return false;
//    }
//    IModuleChannel *imc = IModuleChannel::createInstance();
//    if(false == imc->isChOpened(CI_MATH))
//    {
//        return false;
//    }
//    SCPI_CLOSE_MENU;
//    if(CMathWave::Instance()->GetMathType() == 1)
//    {
//        switch(idx)
//        {
//        case CI_CH1:
//            gMainWindow->GetMenuMath()->on_btnS2CH1_clicked();
//            break;
//        case CI_CH2:
//            gMainWindow->GetMenuMath()->on_btnS2CH2_clicked();
//            break;
//        case CI_CH3:
//            gMainWindow->GetMenuMath()->on_btnS2CH3_clicked();
//            break;
//        case CI_CH4:
//            gMainWindow->GetMenuMath()->on_btnS2CH4_clicked();
//            break;
//        default:
//            return false;
//            break;
//        }
//    }
    return true;
}

void setDualHScal(scpi_t * context)
{
//
// //Q_UNUSED(context);

//    if(gMainWindow->isInXYMode()
//            || CMathWave::Instance()->GetMathType() != 1)
//    {
//        return;
//    }
//    double  param1;
//    if (!SCPI_ParamDouble(context, &param1, true)) {
//        return;
//    }
//    int after = IModuleDevice::instance()->timeToId(param1);
//    int now = IModuleDevice::instance()->timeScaleIdOfView(false,Wave::WPI_STANDARD);

//    CGearToSnSMsg *gtsnsm = CGearToSnSMsg::Instance();
//    gtsnsm->exec(now-after);
}


void getDualHScal(scpi_t * context)
{
//
// //Q_UNUSED(context);

//    if(gMainWindow->isInXYMode()
//            || CMathWave::Instance()->GetMathType() != 1)
//    {
//        SCPI_ResultString(context,"ERROR MODE!");
//        return;
//    }
//    int now = IModuleDevice::instance()->timeScaleIdOfView(false,Wave::WPI_STANDARD);
}

void setDualVScalPLUS(scpi_t * context)
{
//    if(gMainWindow->isInXYMode()
//            || CMathWave::Instance()->GetMathType() != 1)
//    {
//        return;
//    }
    int  param1;
    if (!SCPI_ParamInt(context, &param1, true)) {
        return;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
//
//    CGearToVmVMsg *gtvmvm = CGearToVmVMsg::Instance();
//    gtvmvm->exec(param1, CI_MATH);
}

void setDualVScal(scpi_t * context, int op)
{
//    if(gMainWindow->isInXYMode()
//    || CMathWave::Instance()->GetMathType() != 1
//    || CMathWave::Instance()->GetDualWave()->getOp() != op)
//    {
//        return;
//    }
    double  param1;
    if (!SCPI_ParamDouble(context, &param1, true)) {
        return;
    }
    setParam_1Double(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
//    _YDANG_CL_EX after = IDescVertical::vScaleToFloatEx(param1);
//    float gear = IModuleDevice::instance()->vScale(after);
//    if((param1 - gear) > (gear/2))
//    {
//        return;//�����Ǳ�׼��λ�趨�������в���
//    }
//    _YDANG_CL_EX now = IDescVertical::vScaleToFloatEx(
//                           CMathWave::Instance()->GetDualWave()->GetCVScale());
//    CGearToVmVMsg *gtvmvm = CGearToVmVMsg::Instance();
//    gtvmvm->exec(after-now, CI_MATH);
}


void getDualVScal(scpi_t * context, int op)
{
//    if(gMainWindow->isInXYMode()
//    || CMathWave::Instance()->GetMathType() != 1
//    || CMathWave::Instance()->GetDualWave()->getOp() != op)
//    {
//        DEBUG_RETURN("ERROR MODE!");
//        return;
//    }
//
//    float cur =  CMathWave::Instance()->GetDualWave()->GetCVScale();
//    SCPI_ResultDouble(context,cur);
}

void setVPos(scpi_t * context, int mode)
{
//    if(gMainWindow->isInXYMode()
//    || mode != CMathWave::Instance()->GetMathType())
//    {
//        DEBUG_RETURN("ERROR MODE!");
//        return;
//    }
    double  param1;
    if (!SCPI_ParamDouble(context, &param1, true)) {
        return;
    }
    setParam_1Double(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
//    CH_IDX ch = CI_MATH;
//
//    if(IModuleChannel::createInstance()->isChOpened(ch)) {
//        IModuleDevice *md = IModuleDevice::instance();
//        float vValue = CMathWave::Instance()->GetMathCVScale()/50;
//        int afterPos = param1/vValue;
//        int nowPos = md->vPosOfZero(false, ch);
//        if(md->DispZoom(0)) {
//            afterPos = IDescVertical::vPosOfZeroConvesionToZoomFanda(afterPos);
//            nowPos = IDescVertical::vPosOfZeroConvesionToZoomFanda(nowPos);
//        }
//        CChannelDragMsg::Instance()->exec(afterPos-nowPos, ch);
//    }
}
void setVPosPLUS(scpi_t * context, int mode)
{
//    if(gMainWindow->isInXYMode()
//    || mode != CMathWave::Instance()->GetMathType())
//    {
//        DEBUG_RETURN("ERROR MODE!");
//        return;
//    }
    int  param1;
    if (!SCPI_ParamInt(context, &param1, true)) {
        return;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
//    CH_IDX ch = CI_MATH;
//
//    if(IModuleChannel::createInstance()->isChOpened(ch)) {
//
//        CChannelDragMsg::Instance()->exec(param1, ch);
//    }
}

void getVPos(scpi_t * context, int mode)
{
//    if(gMainWindow->isInXYMode()
//    || mode != CMathWave::Instance()->GetMathType())
//    {
//        DEBUG_RETURN("ERROR MODE!");
//        return;
//    }
//    CH_IDX ch = CI_MATH;
//    IModuleChannel *mc = IModuleChannel::createInstance();
//    if(mc->isChOpened(ch)) {
//        IModuleDevice *md = IModuleDevice::instance();
//        int pos = mc->getGeographInfo(ch);
//        if(md->DispZoom(0)) {
//            pos = IDescVertical::ZoomFandaConvesionTovPosOfZero(pos);
//        }
//        float vValue = CMathWave::Instance()->GetMathCVScale()/50*pos;
//        SCPI_ResultDouble(context, vValue);
//    }
//    else
//    {
//        DEBUG_RETURN("ERROR MATH NOT OPEN!");
//        return;
//    }

    return;
}

//����ر���ѧ����
scpi_result_t MATH_DISP(scpi_t * context)
{
   // ERROR_XY_MODE;
    bool  param1;
    if (!SCPI_ParamBool(context, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_1Boolean(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
//    SCPI_CLOSE_MENU;
//    IModuleChannel *imc = IModuleChannel::createInstance();
//    bool bMathOpen = imc->isChOpened(CI_MATH);
//    if(bMathOpen != param1)
//    {
//        gMainWindow->getChannelPannelEx()->dealChBtnClicked(CI_MATH);
//    }
    return SCPI_RES_OK;
}

//��ѯ��ѧ�������ر�
scpi_result_t MATH_DISPQ(scpi_t * context)
{
//    IModuleChannel *imc = IModuleChannel::createInstance();
//    SCPI_ResultBool(context,imc->isChOpened(CI_MATH));

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}


const char * math_mode[] = {
    "BASE",
    "FFT",
    "AX+B",
    "ADVAnced",
    NULL
};
const char * math_base_operator[]={
        "ADD",
        "SUB",
        "MUL",
        "DIV",
        NULL
};
//ѡ����ѧ��������
scpi_result_t MATH_MODE(scpi_t * context)
{
    int param1;
    if (!SCPI_ParamChoice(context, math_mode, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
//    IModuleChannel *imc = IModuleChannel::createInstance();
//    if(false == imc->isChOpened(CI_MATH))
//    {
//        return SCPI_RES_ERR;
//    }
//    SCPI_CLOSE_MENU;
////    MenuMath* menu = (MenuMath*)gMenuLogic[0]->getSpecifyMenu(OscilloUi::MI_MATH);
////    menu->SCPI_MenuStageInOk();
////    if(menu)
////    {
//        if(param1>=0&&param1<4)
//        {//�л�Ϊ˫����
//            gMainWindow->GetMenuMath()->when_math_mask_pressed(MenuMathFrame::CALC_DUALWAVE);
//        }
//        switch(param1)
//        {
//        case 0:
//            gMainWindow->GetMenuMath()->on_btnAdd_clicked();
//            break;
//        case 1:
//            gMainWindow->GetMenuMath()->on_btnDel_clicked();
//            break;
//        case 2:
//            gMainWindow->GetMenuMath()->on_btnMul_clicked();
//            break;
//        case 3:
//            gMainWindow->GetMenuMath()->on_btnDiv_clicked();
//            break;
//        case 4:
//            gMainWindow->GetMenuMath()->when_math_mask_pressed(MenuMathFrame::CALC_FFT);
//            break;
//        default:
//            break;
//        }
//    //}
    return SCPI_RES_OK;
}

//��ѯ��ѧ��������
scpi_result_t MATH_MODEQ(scpi_t * context)
{
//    IModuleChannel *imc = IModuleChannel::createInstance();
//    if(false == imc->isChOpened(CI_MATH))
//    {
//        DEBUG_RETURN("ERROR MATH NOT OPEN!");
//        return SCPI_RES_ERR;
//    }
//    if(CMathWave::Instance()->GetMathType() == 1)
//    {
//        DualWave_Op op = CMathWave::Instance()->GetDualWave()->getOp();
//        switch(op)
//        {
//        case DWO_ADD:
//            SCPI_ResultString(context,math_mode[0]);
//            break;
//        case DWO_SUB:
//            SCPI_ResultString(context,math_mode[1]);
//            break;
//        case DWO_MUL:
//            SCPI_ResultString(context,math_mode[2]);
//            break;
//        case DWO_DIV:
//            SCPI_ResultString(context,math_mode[3]);
//            break;
//        default:
//            break;
//        }
//    }
//    else if(CMathWave::Instance()->GetMathType() == 2)
//    {
//        SCPI_ResultString(context,math_mode[4]);
//    }
//    else
//    {
//        DEBUG_RETURN("ERROR UNKNOW!");
//    }

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
const char* math_vref[]={
    "CENTer",
    "ZERO",
    NULL
};
scpi_result_t MATH_VREF(scpi_t *context){
    int param1;
    if (!SCPI_ParamChoice(context, math_vref, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t MATH_VREFQ(scpi_t *context){
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return  SCPI_RES_OK;
}

//Base double wave
scpi_result_t MATH_BASE_S1(scpi_t * context)
{
    setDualS1(context);
    return SCPI_RES_OK;
}
scpi_result_t MATH_BASE_S1Q(scpi_t * context)
{
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t MATH_BASE_S2(scpi_t * context)
{
    setDualS2(context);
    return SCPI_RES_OK;
}
scpi_result_t MATH_BASE_S2Q(scpi_t * context)
{
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t MATH_BASE_EXT(scpi_t * context)
{
    setDualVScal(context, 0);
    return SCPI_RES_OK;
}

scpi_result_t MATH_BASE_EXTQ(scpi_t * context)
{
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

scpi_result_t MATH_BASE_OFFS(scpi_t * context)
{
    double  param1;
    if (!SCPI_ParamDouble(context, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_1Double(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
//    setVPos(context, 1);
    return SCPI_RES_OK;
}
scpi_result_t MATH_BASE_OFFSQ(scpi_t * context)
{
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t MATH_BASE_OPER(scpi_t * context)
{
    int param1;
    if (!SCPI_ParamChoice(context, math_base_operator, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t MATH_BASE_OPERQ(scpi_t * context)
{
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}


//FFT
//ѡ��FFT�������Դ
scpi_result_t MATH_FFT_SOUR(scpi_t * context)
{
    int param1;
    if (!SCPI_ParamChoice(context, allCh, &param1, true)) {
        dealCallBack_ParamError(context);
        DEBUG_RETURN("ERROR PARAM!");
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);

//    CH_IDX idx = (CH_IDX)getCh(param1);
//
//    if(!Channel::isDynamicCh(idx)) {
//        return SCPI_RES_ERR;
//    }
//
//    IModuleChannel *imc = IModuleChannel::createInstance();
//    if(false == imc->isChOpened(CI_MATH))
//    {
//        return SCPI_RES_ERR;
//    }
//    SCPI_CLOSE_MENU;
////    MenuMath* temp = (MenuMath*)gMenuLogic[0]->getSpecifyMenu(OscilloUi::MI_MATH);
//    if(CMathWave::Instance()->GetMathType() == 2)
//    {
//        switch(idx)
//        {
//        case CI_CH1:
//            gMainWindow->GetMenuMath()->on_btnFFTCH1_clicked();
//            break;
//        case CI_CH2:
//            gMainWindow->GetMenuMath()->on_btnFFTCH2_clicked();
//            break;
//        case CI_CH3:
//            gMainWindow->GetMenuMath()->on_btnFFTCH3_clicked();
//            break;
//        case CI_CH4:
//            gMainWindow->GetMenuMath()->on_btnFFTCH4_clicked();
//            break;
//        default:
//            break;
//        }
//    }
    return SCPI_RES_OK;
}

//��ѯFFT�������Դ
scpi_result_t MATH_FFT_SOURQ(scpi_t * context)
{
//    IModuleChannel *imc = IModuleChannel::createInstance();
//    if(false == imc->isChOpened(CI_MATH))
//    {
//        DEBUG_RETURN("ERROR FFT NOT OPEN!");
//        return SCPI_RES_ERR;
//    }
//    if(CMathWave::Instance()->GetMathType() == 2)
//    {
//        CH_IDX idx = CMathWave::Instance()->GetFFtWave()->GetSource();
//        SCPI_ResultString(context,gMainWindow->chToStr(idx));
//    }
//    else
//    {
//        DEBUG_RETURN("ERROR MODE!");
//    }

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}


const char * math_fft_wind[] = {
    "RECTangle",
    "HAMMing",
    "BLACkman",
    "HANNing",
    NULL
};
//ѡ��FFT����Ĵ�����
scpi_result_t MATH_FFT_WIND(scpi_t * context)
{
    int param1;
    if (!SCPI_ParamChoice(context, math_fft_wind, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
//    IModuleChannel *imc = IModuleChannel::createInstance();
//    if(false == imc->isChOpened(CI_MATH))
//    {
//        return SCPI_RES_ERR;
//    }
//    SCPI_CLOSE_MENU;
//    //MenuMath* temp = (MenuMath*)gMenuLogic[0]->getSpecifyMenu(OscilloUi::MI_MATH);
////    if(temp)
////    {
//        if(CMathWave::Instance()->GetMathType() != 2)
//        {
//            return SCPI_RES_ERR;
//        }
//        switch(param1)
//        {
//        case 0:
//            gMainWindow->GetMenuMath()->on_btnRectangle_clicked();
//            break;
//        case 1:
//            gMainWindow->GetMenuMath()->on_btnHamming_clicked();
//            break;
//        case 2:
//            gMainWindow->GetMenuMath()->on_btnBlackman_clicked();
//            break;
//        case 3:
//            gMainWindow->GetMenuMath()->on_btnHanning_clicked();
//            break;
//        default:
//            break;
//        }
//    //}
    return SCPI_RES_OK;
}

//��ѯFFT����Ĵ�����
scpi_result_t MATH_FFT_WINDQ(scpi_t * context)
{
//    IModuleChannel *imc = IModuleChannel::createInstance();
//    if(false == imc->isChOpened(CI_MATH))
//    {
//        DEBUG_RETURN("ERROR FFT NOT OPEN!");
//        return SCPI_RES_ERR;
//    }
//    if(CMathWave::Instance()->GetMathType() != 2)
//    {
//        DEBUG_RETURN("ERROR MODE!");
//        return SCPI_RES_ERR;
//    }
//
//    Fft_Window win = CMathWave::Instance()->GetFFtWave()->getWindow();
//    switch(win)
//    {
//    case FW_RECTANGULAR:
//        SCPI_ResultString(context,math_fft_wind[0]);
//        break;
//    case FW_HAMMING:
//        SCPI_ResultString(context,math_fft_wind[1]);
//        break;
//    case FW_BLACKMAN_HARRIS:
//        SCPI_ResultString(context,math_fft_wind[2]);
//        break;
//    case FW_HANNING:
//        SCPI_ResultString(context,math_fft_wind[3]);
//        break;
//    default:
//        break;
//    }

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}


const char * math_fft_type[] = {
    "LINE",
    "DB",
    NULL
};
//ѡ��FFT���ε���ʾ��ʽ
scpi_result_t MATH_FFT_TYPE(scpi_t * context)
{
    int param1;
    if (!SCPI_ParamChoice(context, math_fft_type, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
//    IModuleChannel *imc = IModuleChannel::createInstance();
//    if(false == imc->isChOpened(CI_MATH))
//    {
//        return SCPI_RES_ERR;
//    }
//    SCPI_CLOSE_MENU;
////    MenuMath* temp = (MenuMath*)gMenuLogic[0]->getSpecifyMenu(OscilloUi::MI_MATH);
////    if(temp)
//    {
//        if(CMathWave::Instance()->GetMathType() != 2)
//        {
//            return SCPI_RES_ERR;
//        }
//        switch(param1)
//        {
//        case 0:
//            gMainWindow->GetMenuMath()->when_fft_type_line_pressed();
//            break;
//        case 1:
//            gMainWindow->GetMenuMath()->when_fft_type_db_pressed();
//            break;
//        default:
//            break;
//        }
//    }

    return SCPI_RES_OK;
}

//��ѯFFT���ε���ʾ��ʽ
scpi_result_t MATH_FFT_TYPEQ(scpi_t * context)
{
//    IModuleChannel *imc = IModuleChannel::createInstance();
//    if(false == imc->isChOpened(CI_MATH))
//    {
//        DEBUG_RETURN("ERROR FFT NOT OPEN!");
//        return SCPI_RES_ERR;
//    }
//    if(CMathWave::Instance()->GetMathType() != 2)
//    {
//        DEBUG_RETURN("ERROR MODE!");
//        return SCPI_RES_ERR;
//    }
//
//    Fft_ResultType tp = CMathWave::Instance()->GetFFtWave()->getResultType();
//    switch(tp)
//    {
//    case FFT_RMS:
//        SCPI_ResultString(context,math_fft_type[0]);
//        break;
//    case FFT_VDB:
//        SCPI_ResultString(context,math_fft_type[1]);
//        break;
//    default:
//        break;
//    }

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}



//����FFT�������Ĵ�ֱƫ��
scpi_result_t MATH_FFT_OFFS(scpi_t * context)
{
    setVPos(context, 2);
    return SCPI_RES_OK;
}

//��ѯFFT�������Ĵ�ֱƫ��
scpi_result_t MATH_FFT_OFFSQ(scpi_t * context)
{
//    getVPos(context, 2);

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//����FFT�������Ĵ�ֱ��λ
scpi_result_t MATH_FFT_EXT(scpi_t * context)
{
//    if(gMainWindow->isInXYMode()
//    || CMathWave::Instance()->GetMathType() != 2)
//    {
//        return SCPI_RES_ERR;
//    }
    double  param1;
    if (!SCPI_ParamDouble(context, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
//    _YDANG_CL_EX after = (_YDANG_CL_EX)(IDescVertical::vScaleToFloatEx(param1));
//    _YDANG_CL_EX now = (_YDANG_CL_EX)(IDescVertical::vScaleToFloatEx(
//                                    CMathWave::Instance()->GetFFtWave()->GetCVScale()));
//    CGearToVmVMsg *gtvmvm = CGearToVmVMsg::Instance();
//    gtvmvm->exec(after-now, CI_MATH);
    setParam_1Double(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//��ѯFFT�������Ĵ�ֱ��λ
scpi_result_t MATH_FFT_EXTQ(scpi_t * context)
{
//    IModuleChannel *imc = IModuleChannel::createInstance();
//    if(false == imc->isChOpened(CI_MATH))
//    {
//        DEBUG_RETURN("ERROR FFT NOT OPEN!");
//        return SCPI_RES_ERR;
//    }
//    if(gMainWindow->isInXYMode()
//    || CMathWave::Instance()->GetMathType() != 2)
//    {
//        DEBUG_RETURN("ERROR MODE!");
//        return SCPI_RES_ERR;
//    }
//    float cur =  CMathWave::Instance()->GetFFtWave()->GetCVScale();
//    SCPI_ResultDouble(context,cur);

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t MATH_FFT_HSCA(scpi_t * context){
    double  param1;
    if (!SCPI_ParamDouble(context, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_1Double(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t MATH_FFT_HSCAQ(scpi_t * context){
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t MATH_FFT_POS(scpi_t * context){
    double  param1;
    if (!SCPI_ParamDouble(context, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_1Double(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t MATH_FFT_POSQ(scpi_t * context){
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//AX+B
scpi_result_t MATH_AXB_SOUR(scpi_t * context){
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
scpi_result_t MATH_AXB_SOURQ(scpi_t * context)
{
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t MATH_AXB_A(scpi_t * context){
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
scpi_result_t MATH_AXB_AQ(scpi_t * context){
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t MATH_AXB_B(scpi_t * context){
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
scpi_result_t MATH_AXB_BQ(scpi_t * context){
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t MATH_AXB_UNIT(scpi_t * context){
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
scpi_result_t MATH_AXB_UNITQ(scpi_t * context){
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

scpi_result_t MATH_AXB_EXT(scpi_t * context)
{
    double  param1;
    if (!SCPI_ParamDouble(context, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_1Double(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t MATH_AXB_EXTQ(scpi_t * context)
{
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t MATH_AXB_OFFS(scpi_t * context)
{
    setVPos(context, 2);
    return SCPI_RES_OK;
}
scpi_result_t MATH_AXB_OFFSQ(scpi_t * context)
{
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
//ADVanced
scpi_result_t MATH_ADV_EXPR(scpi_t * context)
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
scpi_result_t MATH_ADV_EXPRQ(scpi_t * context)
{
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t MATH_ADV_VAR1(scpi_t * context)
{
    double  param1;
    if (!SCPI_ParamDouble(context, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_1Double(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t MATH_ADV_VAR1Q(scpi_t * context)
{
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t MATH_ADV_VAR2(scpi_t * context)
{
    double  param1;
    if (!SCPI_ParamDouble(context, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_1Double(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t MATH_ADV_VAR2Q(scpi_t * context)
{
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t MATH_ADV_EXT(scpi_t * context)
{
    double  param1;
    if (!SCPI_ParamDouble(context, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_1Double(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t MATH_ADV_EXTQ(scpi_t * context)
{
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t MATH_ADV_OFFS(scpi_t * context)
{
    setVPos(context, 2);
    return SCPI_RES_OK;
}
scpi_result_t MATH_ADV_OFFSQ(scpi_t * context)
{
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t MATH_ADV_UNIT(scpi_t * context)
{
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
scpi_result_t MATH_ADV_UNITQ(scpi_t * context)
{
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//math sample query
scpi_result_t MATH_SAMPLE_SRateQ(scpi_t * context)
{
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t MATH_SAMPLE_MDepthQ(scpi_t * context)
{
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}





