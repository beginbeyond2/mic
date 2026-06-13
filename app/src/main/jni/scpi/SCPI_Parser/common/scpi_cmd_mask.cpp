
#include "scpi_cmd_mask.h"
#include <stdio.h>
#include <stdlib.h>
#include "scpi_help.h"
#include "../../SCPICommandCallBackJava.h"
#include "../../Log.h"


//设置pass/fial测试的通道源
scpi_result_t MASK_SOUR(scpi_t * context){
    //Q_UNUSED(context);

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;}
//查询pass/fial测试的通道源
scpi_result_t MASK_SOURQ(scpi_t * context){
    //SEND_RECEIVE_CMD(context);
    const char* param1="OKAY";
    setParam_Resutl_1String(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;}
//设置模板测试的测试区域
scpi_result_t MASK_RANG(scpi_t * context){
    //Q_UNUSED(context);

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;}
//查询模板测试的测试区域
scpi_result_t MASK_RANGQ(scpi_t * context){
    //SEND_RECEIVE_CMD(context);
    const char* param1="OKAY";
    setParam_Resutl_1String(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;}
//打开或关闭pass/fail测试时的统计功能状态，统计信息包括通过、失败、和总的测试帧数
scpi_result_t MASK_STAT(scpi_t * context){
    //Q_UNUSED(context);

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;}
//查询pass/fail测试时的统计功能状态打开或关闭
scpi_result_t MASK_STATQ(scpi_t * context){
    //SEND_RECEIVE_CMD(context);
    const char* param1="OKAY";
    setParam_Resutl_1String(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;}
//复位模板测试统计信息
scpi_result_t MASK_RES(scpi_t * context){
    //Q_UNUSED(context);

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;}
//打开或关闭“输出即停
scpi_result_t MASK_SOO(scpi_t * context){

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;}
//查询“输出即停 打开或关闭
scpi_result_t MASK_SOOQ(scpi_t * context){
   // SEND_RECEIVE_CMD(context);
    const char* param1="OKAY";
    setParam_Resutl_1String(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;}
//打开模板测试的完成响应
scpi_result_t MASK_AUX(scpi_t * context){

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;}
//查询模板测试的完成响应
scpi_result_t MASK_AUXQ(scpi_t * context){
    //SEND_RECEIVE_CMD(context);
    const char* param1="OKAY";
    setParam_Resutl_1String(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;}
//打开或关闭模板测试
scpi_result_t MASK_ENAB(scpi_t * context){
    //Q_UNUSED(context);

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;}
//查询模板测试打开或关闭
scpi_result_t MASK_ENABQ(scpi_t * context){
    //SEND_RECEIVE_CMD(context);
    const char* param1="OKAY";
    setParam_Resutl_1String(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;}
//控制pass/fail测试的运行和停止
scpi_result_t MASK_OPER(scpi_t * context){

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;}
//查询pass/fail测试的运行和停止
scpi_result_t MASK_OPERQ(scpi_t * context){
    const char* param1="OKAY";
    setParam_Resutl_1String(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;}
//设置pass/fail测试的规则中的“水平调整”参数
scpi_result_t MASK_X(scpi_t * context){

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;}
//查询pass/fail测试的规则中的“水平调整”参数
scpi_result_t MASK_XQ(scpi_t * context){
    const char* param1="OKAY";
    setParam_Resutl_1String(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;}
//设置pass/fail测试的规则中的“垂直调整”参数
scpi_result_t MASK_Y(scpi_t * context){
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;}
//查询pass/fail测试的规则中的“垂直调整”参数
scpi_result_t MASK_YQ(scpi_t * context){
    const char* param1="OKAY";
    setParam_Resutl_1String(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;}
