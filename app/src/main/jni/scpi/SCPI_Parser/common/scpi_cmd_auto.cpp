
#include "scpi_cmd_curs.h"
#include <stdio.h>
#include <stdlib.h>
#include "scpi_help.h"
#include "scpi_cmd_auto.h"
#include "../../SCPICommandCallBackJava.h"
#include "../../Log.h"

//�����Զ���ͨ��
scpi_result_t AUTO_SET_CHAN(scpi_t * context)
{
    //Q_UNUSED(context);
    bool param1;
    if (!SCPI_ParamBool(context,&param1,true))
    {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
//    MenuNormalAutoFrame *temp=(MenuNormalAutoFrame*)gMainWindow->GetMenuFrame_Help(OscilloUi::MI_AUTO_SETTING);
//    temp->setAutoOpenEnable(param1);
    setParam_1Boolean(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//��ѯ�Զ���ͨ��
scpi_result_t AUTO_SET_CHANQ(scpi_t * context)
{
    //Q_UNUSED(context);
//    MenuNormalAutoFrame *temp=(MenuNormalAutoFrame*)gMainWindow->GetMenuFrame_Help(OscilloUi::MI_AUTO_SETTING);
//    bool param1= temp->getAutoOpenEnable();
//    SCPI_ResultBool(context,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
//�������޵�ƽ
scpi_result_t AUTO_SET_LEVE(scpi_t * context)
{
    //Q_UNUSED(context);
    double param1;
    if (!SCPI_ParamDouble(context,&param1,true))
    {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
//    MenuNormalAutoFrame *temp=(MenuNormalAutoFrame*)gMainWindow->GetMenuFrame_Help(OscilloUi::MI_AUTO_SETTING);
//    temp->setAutoLimitV(param1);
    setParam_1Double(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
//��ѯ���޵�ƽ
scpi_result_t AUTO_SET_LEVEQ(scpi_t * context)
{
    //Q_UNUSED(context);
//    MenuNormalAutoFrame *temp=(MenuNormalAutoFrame*)gMainWindow->GetMenuFrame_Help(OscilloUi::MI_AUTO_SETTING);
//    float param1= temp->getAutoLimitV();
//    SCPI_ResultDouble(context,param1);

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

const char *charAuto_set_sour[]={
    "CURrent",
    "MAX",
    NULL
};
//���ô���Դ
scpi_result_t AUTO_SET_SOUR(scpi_t * context)
{
    //Q_UNUSED(context);
    int param1;
    if (!SCPI_ParamChoice(context,charAuto_set_sour,&param1,true))
    {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
//    MenuNormalAutoFrame *temp=(MenuNormalAutoFrame*)gMainWindow->GetMenuFrame_Help(OscilloUi::MI_AUTO_SETTING);
//    temp->setAutoTriggerSrc(param1);
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
//��ѯ����Դ
scpi_result_t AUTO_SET_SOURQ(scpi_t * context)
{
    //Q_UNUSED(context);
//    MenuNormalAutoFrame *temp=(MenuNormalAutoFrame*)gMainWindow->GetMenuFrame_Help(OscilloUi::MI_AUTO_SETTING);
//    int param1= temp->getAutoTriggerSrc();
//    SCPI_ResultString(context,charAuto_set_sour[param1]);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
//�����Զ�����
scpi_result_t AUTO_RAN(scpi_t * context)
{
    //Q_UNUSED(context);
    bool param1;
    if (!SCPI_ParamBool(context,&param1,true))
    {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
//    MenuAutoRangeFrame *temp=(MenuAutoRangeFrame *)gMainWindow->GetMenuFrame_Help(OscilloUi::MI_AUTO_RANGE);
//    temp->setAutoRangeEnable(param1);
    setParam_1Boolean(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
//��ѯ�Զ�����
scpi_result_t AUTO_RANQ(scpi_t * context)
{
    //Q_UNUSED(context);
//    MenuAutoRangeFrame *temp=(MenuAutoRangeFrame *)gMainWindow->GetMenuFrame_Help(OscilloUi::MI_AUTO_RANGE);
//    bool param1=temp->getAutoRangeEnable();
//    SCPI_ResultBool(context,param1);

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
//�����Զ���ֱ
scpi_result_t AUTO_RAN_VER(scpi_t * context)
{
    //Q_UNUSED(context);
    bool param1;
    if (!SCPI_ParamBool(context,&param1,true))
    {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
//    MenuAutoRangeFrame *temp=(MenuAutoRangeFrame *)gMainWindow->GetMenuFrame_Help(OscilloUi::MI_AUTO_RANGE);
//    temp->setAutoRangeVer(param1);
    setParam_1Boolean(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
//��ѯ�Զ���ֱ
scpi_result_t AUTO_RAN_VERQ(scpi_t * context)
{
    //Q_UNUSED(context);
//    MenuAutoRangeFrame *temp=(MenuAutoRangeFrame *)gMainWindow->GetMenuFrame_Help(OscilloUi::MI_AUTO_RANGE);
//    bool param1=temp->getAutoRangeVer();
//    SCPI_ResultBool(context,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
//�����Զ�ˮƽ
scpi_result_t AUTO_RAN_HOR(scpi_t * context)
{
    //Q_UNUSED(context);
    bool param1;
    if (!SCPI_ParamBool(context,&param1,true))
    {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
//    MenuAutoRangeFrame *temp=(MenuAutoRangeFrame *)gMainWindow->GetMenuFrame_Help(OscilloUi::MI_AUTO_RANGE);
//    temp->setAutoRangeHor(param1);
    setParam_1Boolean(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
//��ѯ�Զ�ˮƽ
scpi_result_t AUTO_RAN_HORQ(scpi_t * context)
{
    //Q_UNUSED(context);
//    MenuAutoRangeFrame *temp=(MenuAutoRangeFrame *)gMainWindow->GetMenuFrame_Help(OscilloUi::MI_AUTO_RANGE);
//    bool param1=temp->getAutoRangeHor();
//    SCPI_ResultBool(context,param1);

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
//�����Զ�����
scpi_result_t AUTO_RAN_LEVE(scpi_t * context)
{
    //Q_UNUSED(context);
    bool param1;
    if (!SCPI_ParamBool(context,&param1,true))
    {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
//    MenuAutoRangeFrame *temp=(MenuAutoRangeFrame *)gMainWindow->GetMenuFrame_Help(OscilloUi::MI_AUTO_RANGE);
//    temp->setAutoRangeLevel(param1);
    setParam_1Boolean(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
//��ѯ�Զ�����
scpi_result_t AUTO_RAN_LEVEQ(scpi_t * context)
{
    //Q_UNUSED(context);
//    MenuAutoRangeFrame *temp=(MenuAutoRangeFrame *)gMainWindow->GetMenuFrame_Help(OscilloUi::MI_AUTO_RANGE);
//    bool param1=temp->getAutoRangeLevel();
//    SCPI_ResultBool(context,param1);

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
