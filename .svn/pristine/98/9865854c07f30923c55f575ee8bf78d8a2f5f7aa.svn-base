
#include "scpi_cmd_menu.h"
#include <stdio.h>
#include <stdlib.h>
#include "scpi_cmd_calibrate.h"
#include "scpi_help.h"
#include "../../SCPICommandCallBackJava.h"
#include "../../Log.h"
//#include "main_window.h"
//#include "device/include/imodule_device.h"
//#include "channel/include/imodule_channel.h"
//#include "platform/PlatSystem/include/idevconfig.h"
//#include "platform/light/include/ilight.h"
//#include "platform/Audio/include/iaudioplay.h"
//#include "platform/PlatSystem/include/isystemevent.h"
//#include "module/cursor/include/imodule_cursor_define.h"
//#include "lib/ui_scope/src/control/control_lock_screen.h"
//#include "lib/ui_scope/src/menu/menufactoryresetframe.h"
//#include "freaccountgroup.h"
//#include "waveform_scene.h"
//#include "xy_scene_item.h"
//#include "xy_scene.h"
//#include "menu_bottom.h"
//#include "menufrequencymeterframe.h"
//
//using namespace Channel;
//using namespace OscilloUi;
//using namespace Device;
//using namespace Cursor;

//�Զ�
scpi_result_t MENU_AUTO(scpi_t * context)
{
//    Q_UNUSED(context);
//    gMainWindow->OnAuto();
    bool param1;
    if (!SCPI_ParamBool(context, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_1Boolean(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t MENU_AUTOQ(scpi_t * context)
{
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//ʹʾ������ʼ���У����ϴ�����������ʼ�ɼ�����
scpi_result_t MENU_RUN(scpi_t * context)
{
//    Q_UNUSED(context);
//    if(gMainWindow->IsWork())
//    {
//        if(!IModuleDevice::instance()->isInRunState(false))
//        {
//            gMainWindow->onRun(true);
//        }
//    }

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//ʹʾ����ֹͣ���У����ݲɼ�ֹͣ
scpi_result_t MENU_STOP(scpi_t * context)
{
//    Q_UNUSED(context);
//    if(gMainWindow->IsWork())
//    {
//        if(IModuleDevice::instance()->isInRunState(false))
//        {
//            gMainWindow->onRun(false);
//        }
//    }

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//��ͨ��λ������Ϊ��ֱ���λ�ã�������ʾ����ֱ���ģ�
scpi_result_t MENU_HALF_CHAN(scpi_t * context)
{
    int param1;
    if (!SCPI_ParamChoice(context, allCh, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
//    CH_IDX idx = (CH_IDX)getCh(param1);
//    if(!Channel::isDynamicCh(idx)
//    && !Channel::isRefCh(idx)
//    && idx!=CI_MATH) {
//        return SCPI_RES_ERR;
//    }
//
//    if(gMainWindow->isInXYMode())
//    {
//        CXYSceneItem * pXYSceneItem  = gMainWindow->scene3->getCurSceneItem();
//        switch(idx)
//        {
//            case CI_CH1: pXYSceneItem->setVChannelIndicator0Position(200); break;
//            case CI_CH2: pXYSceneItem->setHChannelIndicator0Position(200); break;
//            default: return SCPI_RES_ERR;
//        }
//    }
//    else gMainWindow->setCH_Half(idx);
    return SCPI_RES_OK;
}

//���ô���λ�õ���Ļ�м�
scpi_result_t MENU_HALF_TRIG(scpi_t * context)
{
   // ERROR_XY_MODE;
    int param1;
    if (!SCPI_ParamChoice(context, allCh, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
//    CH_IDX idx = (CH_IDX)getCh(param1);
//    if(!Channel::isDynamicCh(idx)
//            && !Channel::isRefCh(idx)
//            && idx!=CI_MATH) {
//        return SCPI_RES_ERR;
//    }
//    gMainWindow->setTrig_Half(idx);
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//����ͨ���Ĵ�ֱ�����50%��
scpi_result_t MENU_HALF_XCUR(scpi_t * context)
{
//    Q_UNUSED(context);
//    if(gMainWindow->isInXYMode())
//    {
//        CXYSceneItem * pXYSceneItem  = gMainWindow->scene3->getCurSceneItem();
//        IModuleCursor::instanceEx(pXYSceneItem->getIndex());
//        pXYSceneItem->setCursorPos(XYZone::CID_V1,100);
//        pXYSceneItem->setCursorPos(XYZone::CID_V2,300);
//    }
//    else gMainWindow->setCour_Half(CT_V);

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//����ͨ����ˮƽ�����50%��
scpi_result_t MENU_HALF_YCUR(scpi_t * context)
{
//    Q_UNUSED(context);
//    if(gMainWindow->isInXYMode())
//    {
//        CXYSceneItem * pXYSceneItem  = gMainWindow->scene3->getCurSceneItem();
//        IModuleCursor::instanceEx(pXYSceneItem->getIndex());
//        pXYSceneItem->setCursorPos(XYZone::CID_H1,100);
//        pXYSceneItem->setCursorPos(XYZone::CID_H2,300);
//    }
//    else gMainWindow->setCour_Half(CT_H);

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//��������ƽ����Ϊ�����źŷ�ֵ���м�λ��
scpi_result_t MENU_HALF_LEV(scpi_t * context)
{
   // ERROR_XY_MODE;
  //  CH_IDX idx = IModuleChannel::createInstance()->active();
    int param1;
    if (SCPI_ParamChoice(context, allCh, &param1, true)) {
        //idx = (CH_IDX)getCh(param1);
    }

//    if(!Channel::isDynamicCh(idx)
//    && !Channel::isRefCh(idx)
//    && idx!=CI_MATH) {
//        return SCPI_RES_ERR;
//    }
//    if(IModuleDevice::instance()->isChannelSampOn(idx))
//        gMainWindow->setLevel_Half(idx);
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//����ʾ�����ص�������
scpi_result_t MENU_HOM(scpi_t * context)
{
//    Q_UNUSED(context);
//    gMainWindow->onHome();

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
//#include <QTimer>
//�����˳�ʾ������������������
scpi_result_t MENU_RET(scpi_t * context)
{
//    Q_UNUSED(context);
//    gMainWindow->onHome();
//    QTimer::singleShot(100, qApp, SLOT(quit()));

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//��ʾ��������Ϊ�����У�ʾ����������ʾ���βɼ�
scpi_result_t MENU_SING(scpi_t * context)
{
//    Q_UNUSED(context);
//    gMainWindow->onSingle();

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//��ʾ��������Ϊ����������ʽ
scpi_result_t MENU_MULT(scpi_t * context)
{
//    Q_UNUSED(context);
//    if(gMainWindow->IsWork())
//    {
//        gMainWindow->onRun(true);
//        if(IModuleDevice::instance()->isInSingleState(0))
//        {
//            IModuleDevice::instance()->isInSingleState(1) = 0;
//            ILight::Instance()->ControlLed(ILight::LED_SINGLE, false);
//            ISystemEvent::Instance()->SetLedsVal(ILight::Instance()->GetLedsVal());
//        }
//    }

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

scpi_result_t MENU_BEEP(scpi_t *context)
{
//    Q_UNUSED(context);
//    IAudioPlay::Instance()->Play(IAudioPlay::AUDIO_STARTUP_IDX);

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t MENU_VERSIONQ(scpi_t* context)
{
    return MENU_BEEP(context);
}

//����ʾ������Ļ
scpi_result_t MENU_LOCK(scpi_t * context)
{
//    Q_UNUSED(context);
//    gMainWindow->RunScreenOff();
    bool param;
    if (!SCPI_ParamBool(context,&param,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_1Boolean(context->env,context->param,param);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t MENU_LOCKQ(scpi_t *context){
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//����ʾ������Ļ
scpi_result_t MENU_UNL(scpi_t * context)
{
//    Q_UNUSED(context);
//    gMainWindow->RunScreenOn();

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

const char * menu_counter_ch[]={
        "CLOSe",
        "CH1",
        "CH2",
        "CH3",
        "CH4",
        NULL
};
//Ƶ�ʼƵĴ���ر�
scpi_result_t MENU_COUN(scpi_t * context)
{

    int param1;
    if (SCPI_ParamChoice(context, menu_counter_ch, &param1, true)) {

    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
//    MenuFrequencyMeterFrame *menu = (MenuFrequencyMeterFrame *)gMainWindow->GetMenuFrame_Help(MI_Frequency);
//    menu->setUIFrequencyMeter(param1);
    return SCPI_RES_OK;
}

//Ƶ�ʼƵĴ���رղ�ѯ
scpi_result_t MENU_COUNQ(scpi_t * context)
{

//    int param1= IModuleDevice::instance()->flagFreSource(true);
//    if (param1==-1) param1=9;
//    SCPI_ResultString(context,allCh[param1]);

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

scpi_result_t MENU_RES(scpi_t * context)
{
//    Q_UNUSED(context);
//    MenuFactoryresetFrame *menu = (MenuFactoryresetFrame *)gMainWindow->GetMenuFrame_Help(MI_FACTORY);
//    menu->RestoreScopeFactoryCfg();

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

scpi_result_t MENU_MEAS(scpi_t * context)//��������˵�
{
//    Q_UNUSED(context);
//    gMainWindow->on_btnMeasure_clicked();

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;

}

scpi_result_t MENU_TRIG(scpi_t * context)//��������˵�
{
//    Q_UNUSED(context);
//    gMainWindow->on_btnTrigger_clicked();

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

scpi_result_t MENU_CHAN(scpi_t *context){
    int param1;
    if (!SCPI_ParamChoice(context,allCh,&param1,true)){
        dealCallBack_ParamError(context);
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
scpi_result_t MENU_CHANQ(scpi_t *context){
    int param1;
    if (!SCPI_ParamChoice(context,allCh,&param1,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t MENU_QUICK(scpi_t *context){
    bool param1;
    if (!SCPI_ParamBool(context,&param1,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_1Boolean(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t MENU_QUICKQ(scpi_t *context){
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t MENU_MAIN(scpi_t *context){
    bool param1;
    if (!SCPI_ParamBool(context,&param1,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_1Boolean(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t MENU_MAINQ(scpi_t *context){
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

const char* aux[]={
        "OUT",
        "IN",
        NULL
};
scpi_result_t MENU_AUX_Trigger(scpi_t *context){
    int param1;
    if (!SCPI_ParamChoice(context,aux,&param1,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t MENU_AUX_TriggerQ(scpi_t *context){
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t MENU_AUX_Clock(scpi_t *context){
    int param1;
    if (!SCPI_ParamChoice(context,aux,&param1,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t MENU_AUX_ClockQ(scpi_t *context){
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

const char * inp[] = {
        "MEGA",
        "FIFTy",
        NULL
};
scpi_result_t MENU_AUX_Inputres(scpi_t *context){
    int param1;
    if (!SCPI_ParamChoice(context, inp, &param1, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t MENU_AUX_InputresQ(scpi_t *context){
    return MENU_AUX_ClockQ(context);
}
