
#include "scpi_cmd_bus.h"
#include "scpi_cmd_calibrate.h"
#include "scpi_help.h"
#include "../../SCPICommandCallBackJava.h"
#include "../../Log.h"


const char * type[]={
        "UART",
        "LIN",
        "CAN",
        "SPI",
        "IIC",
        "429",
        "1553B",
};

const char * idLevel[]={
        "HIGH",
        "LOW",
};

scpi_result_t Query(scpi_t * context){
    int param1;
    if (SCPI_CmdParamInt(context,&param1)==false){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

//显示 并入到command.get.channel.display中
scpi_result_t BUS_DISP(scpi_t * context){
    int param1;
    if (SCPI_CmdParamInt(context,&param1)==false){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    bool param2;
    if (!SCPI_ParamBool(context,&param2,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    param1+=9;
    setParam_1Int1Boolean(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t BUS_DISPQ(scpi_t * context){
    int param1;
    if (SCPI_CmdParamInt(context,&param1)==false){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    param1+=9;
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

scpi_result_t BUS_TYPE(scpi_t * context)
{
    int param1;
    if (SCPI_CmdParamInt(context,&param1)==false){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    int  param2;
    if (!SCPI_ParamChoice(context,type, &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t BUS_TYPEQ(scpi_t * context)
{
    return Query(context);
}

const char* bus_mode[]={
        "GRAP",
        "TXT",
        NULL
};
scpi_result_t BUS_MODE(scpi_t * context){
    int param1;
    if (!SCPI_CmdParamInt(context,&param1)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    int  param2;
    if (!SCPI_ParamChoice(context,bus_mode, &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t BUS_MODEQ(scpi_t * context){
    return Query(context);
}
scpi_result_t BUS_LEVEL(scpi_t * context){
    int param1;
    if (!SCPI_CmdParamInt(context,&param1)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    int  param2;
    if (!SCPI_ParamChoice(context,allCh, &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    double param3;
    if (!SCPI_ParamDouble(context,&param3,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_2Int1Double(context->env,context->param,param1,param2,param3);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t BUS_LEVELQ(scpi_t * context){
    int param1;
    if (!SCPI_CmdParamInt(context,&param1)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    int  param2;
    if (!SCPI_ParamChoice(context,allCh, &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

scpi_result_t BUS_HLEVEL(scpi_t * context){
    int param1;
    if (!SCPI_CmdParamInt(context,&param1)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    int  param2;
    if (!SCPI_ParamChoice(context,allCh, &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    double param3;
    if (!SCPI_ParamDouble(context,&param3,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_2Int1Double(context->env,context->param,param1,param2,param3);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t BUS_HLEVELQ(scpi_t * context){
    int param1;
    if (!SCPI_CmdParamInt(context,&param1)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    int  param2;
    if (!SCPI_ParamChoice(context,allCh, &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t BUS_LLEVEL(scpi_t * context){
    int param1;
    if (!SCPI_CmdParamInt(context,&param1)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    int  param2;
    if (!SCPI_ParamChoice(context,allCh, &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    double param3;
    if (!SCPI_ParamDouble(context,&param3,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_2Int1Double(context->env,context->param,param1,param2,param3);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t BUS_LLEVELQ(scpi_t * context){
    int param1;
    if (!SCPI_CmdParamInt(context,&param1)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    int  param2;
    if (!SCPI_ParamChoice(context,allCh, &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

scpi_result_t BUS_DATAQ(scpi_t * context){
    int param1;
    if (!SCPI_CmdParamInt(context,&param1)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    param1+=9;
    setParam_1Int(context->env,context->param,param1);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;

}

scpi_result_t BUS_UART_RX(scpi_t * context)
{
    int param1;
    if (SCPI_CmdParamInt(context,&param1)==false){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    int param2;
    if (!SCPI_ParamChoice(context,allCh,&param2,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t BUS_UART_RXQ(scpi_t * context)
{
    return Query(context);
}

scpi_result_t BUS_UART_IDLE(scpi_t * context)
{
    int param1;
    if (SCPI_CmdParamInt(context,&param1)==false){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    int param2;
    if (!SCPI_ParamChoice(context,idLevel,&param2,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t BUS_UART_IDLEQ(scpi_t * context)
{
    return Query(context);
}

scpi_result_t BUS_UART_BAUD(scpi_t * context)
{
    int param1;
    if (SCPI_CmdParamInt(context,&param1)==false){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    int param2;
    if (SCPI_ParamInt(context,&param2,true)==false){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t BUS_UART_BAUDQ(scpi_t * context)
{
    return Query(context);
}
const char * uart_check[]={
        "NONE",
        "ODD",
        "EVEN"
};
scpi_result_t BUS_UART_CHECK(scpi_t * context)
{
    int param1;
    if (SCPI_CmdParamInt(context,&param1)==false){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    int param2;
    if (!SCPI_ParamChoice(context,uart_check,&param2,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t BUS_UART_CHECKQ(scpi_t * context)
{
    return Query(context);
}
scpi_result_t BUS_UART_USER(scpi_t * context)
{
    int param1;
    if (SCPI_CmdParamInt(context,&param1)==false){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    int param2;
    if (SCPI_ParamInt(context,&param2,true)== false){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t BUS_UART_USERQ(scpi_t * context)
{
    return Query(context);
}
const char * uart_width[]={
        "5",
        "6",
        "7",
        "8",
        "9",
};
scpi_result_t BUS_UART_WIDTH(scpi_t * context)
{
    int param1;
    if (SCPI_CmdParamInt(context,&param1)==false){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    int param2;
    if (!SCPI_ParamChoice(context,uart_width,&param2,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t BUS_UART_WIDTHQ(scpi_t * context)
{
    return Query(context);
}

const char * uart_display[]={
        "Hex",
        "Bin",
        "ASCII",
};

scpi_result_t BUS_UART_DISP(scpi_t * context){
    int param1;
    if (SCPI_CmdParamInt(context,&param1)==false){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    int param2;
    if (!SCPI_ParamChoice(context,uart_display,&param2,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t BUS_UART_DISPQ(scpi_t * context){
    return Query(context);
}
scpi_result_t BUS_UART_LEV(scpi_t * context){
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
    return SCPI_RES_OK;
}
scpi_result_t BUS_UART_LEVQ(scpi_t * context){
    return Query(context);
}


scpi_result_t BUS_LIN_CHAN(scpi_t * context)
{
    int param1;
    if (SCPI_CmdParamInt(context,&param1)==false){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    int param2;
    if (!SCPI_ParamChoice(context,allCh,&param2,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t BUS_LIN_CHANQ(scpi_t * context)
{
    return Query(context);
}
scpi_result_t BUS_LIN_IDLE(scpi_t * context)
{
    int param1;
    if (SCPI_CmdParamInt(context,&param1)==false){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    int param2;
    if (!SCPI_ParamChoice(context,idLevel,&param2,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t BUS_LIN_IDLEQ(scpi_t * context)
{
    return Query(context);
}
const char* lin_baud[]={
        "2400",
        "4800",
        "9600",
        "19200",
};
scpi_result_t BUS_LIN_BAUD(scpi_t * context)
{
    int param1;
    if (SCPI_CmdParamInt(context,&param1)==false){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    int param2;
    if (!SCPI_ParamChoice(context,lin_baud,&param2,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t BUS_LIN_BAUDQ(scpi_t * context)
{
    return Query(context);
}
scpi_result_t BUS_LIN_USER(scpi_t * context)
{
    int param1;
    if (SCPI_CmdParamInt(context,&param1)==false){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    int param2;
    if (SCPI_ParamInt(context,&param2,true)== false){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t BUS_LIN_USERQ(scpi_t * context)
{
    return Query(context);
}

scpi_result_t BUS_LIN_LEV(scpi_t * context){
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
    return SCPI_RES_OK;
}
scpi_result_t BUS_LIN_LEVQ(scpi_t * context){
    return Query(context);
}

scpi_result_t BUS_SPI_CLK(scpi_t * context)
{
    int param1;
    if (SCPI_CmdParamInt(context,&param1)==false){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    int param2;
    if (!SCPI_ParamChoice(context,allCh,&param2,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t BUS_SPI_CLKQ(scpi_t * context)
{
    return Query(context);
}
scpi_result_t BUS_SPI_DATA(scpi_t * context)
{
    int param1;
    if (SCPI_CmdParamInt(context,&param1)==false){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    int param2;
    if (!SCPI_ParamChoice(context,allCh,&param2,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t BUS_SPI_DATAQ(scpi_t * context)
{
    return Query(context);
}
const char * spi_width[]={
        "4",
        "8",
        "16",
        "24",
        "32",
};
scpi_result_t BUS_SPI_WIDTH(scpi_t * context)
{
    int param1;
    if (SCPI_CmdParamInt(context,&param1)==false){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    int param2;
    if (!SCPI_ParamChoice(context,spi_width,&param2,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t BUS_SPI_WIDTHQ(scpi_t * context)
{
    return Query(context);
}
scpi_result_t BUS_SPI_IDLElvl(scpi_t * context)
{
    int param1;
    if (SCPI_CmdParamInt(context,&param1)==false){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    int param2;
    if (!SCPI_ParamChoice(context,idLevel,&param2,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t BUS_SPI_IDLElvlQ(scpi_t * context)
{
    return Query(context);
}

const char* spi_slope[]={
        "RISE",
        "FALL",
        NULL
};
scpi_result_t BUS_SPI_SLOP(scpi_t * context){
    int param1;
    if (SCPI_CmdParamInt(context,&param1)==false){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    int param2;
    if (!SCPI_ParamChoice(context,spi_slope,&param2,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t BUS_SPI_SLOPQ(scpi_t * context){
    return Query(context);
}
scpi_result_t BUS_SPI_CS(scpi_t * context){
    int param1;
    if (!SCPI_CmdParamInt(context,&param1)){
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
scpi_result_t BUS_SPI_CSQ(scpi_t * context){
    return Query(context);
}
scpi_result_t BUS_SPI_CS_SOURCE(scpi_t * context){
    int param1;
    if (SCPI_CmdParamInt(context,&param1)==false){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    int param2;
    if (!SCPI_ParamChoice(context,allCh,&param2,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t BUS_SPI_CS_SOURCEQ(scpi_t * context){
    return Query(context);
}
scpi_result_t BUS_SPI_CS_IDLE(scpi_t * context){
    int param1;
    if (SCPI_CmdParamInt(context,&param1)==false){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    int param2;
    if (!SCPI_ParamChoice(context,idLevel,&param2,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

scpi_result_t BUS_SPI_CS_IDLEQ(scpi_t * context){
    return Query(context);
}

scpi_result_t BUS_SPI_CLKLEV(scpi_t * context){
    int param1;
    if (!SCPI_CmdParamInt(context, &param1)) {
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
scpi_result_t BUS_SPI_CLKLEVQ(scpi_t * context){
    Query(context);
}
scpi_result_t BUS_SPI_DATLEV(scpi_t * context){
    int param1;
    if (!SCPI_CmdParamInt(context, &param1)) {
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
scpi_result_t BUS_SPI_DATLEVQ(scpi_t * context){
    Query(context);
}
scpi_result_t BUS_SPI_CSLEV(scpi_t * context){
    int param1;
    if (!SCPI_CmdParamInt(context, &param1)) {
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
scpi_result_t BUS_SPI_CSLEVQ(scpi_t * context){
    Query(context);
}

scpi_result_t BUS_CAN_CHAN(scpi_t * context)
{
    int param1;
    if (SCPI_CmdParamInt(context,&param1)==false){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    int param2;
    if (!SCPI_ParamChoice(context,allCh,&param2,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t BUS_CAN_CHANQ(scpi_t * context)
{
    return Query(context);
}
const char * can_signal[]={
        "CAN_H",
        "CAN_L",
        "H_L",
        "L_H",
        "Rx",
        "Tx",
};
scpi_result_t BUS_CAN_SIGNal(scpi_t * context)
{
    int param1;
    if (SCPI_CmdParamInt(context,&param1)==false){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    int param2;
    if (!SCPI_ParamChoice(context,can_signal,&param2,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t BUS_CAN_SIGNalQ(scpi_t * context)
{
    return Query(context);
}
const char* can_baud[]={
        "100000",
        "500000",
        "1000000",
};
scpi_result_t BUS_CAN_BAUD(scpi_t * context)
{
    int param1;
    if (SCPI_CmdParamInt(context,&param1)==false){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    int param2;
    if (!SCPI_ParamChoice(context,can_baud,&param2,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t BUS_CAN_BAUDQ(scpi_t * context)
{
    return Query(context);
}
scpi_result_t BUS_CAN_USER(scpi_t * context)
{
    int param1;
    if (SCPI_CmdParamInt(context,&param1)==false){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    int param2;
    if (SCPI_ParamInt(context,&param2,true)== false){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t BUS_CAN_USERQ(scpi_t * context)
{
    return Query(context);
}
scpi_result_t BUS_CAN_SamplePoint(scpi_t * context){
    int param1;
    if (SCPI_CmdParamInt(context,&param1)==false){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    double param2;
    if (SCPI_ParamDouble(context,&param2,true)== false){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_1Int1Double(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t BUS_CAN_SamplePointQ(scpi_t * context){
    return Query(context);
}
const char* can_fdBaudrate[]={
        "NONE",
        "2M",
        "5M",
        NULL
};
scpi_result_t BUS_CAN_FDBaudrate(scpi_t * context){
    int param1;
    if (SCPI_CmdParamInt(context,&param1)==false){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    int param2;
    if (SCPI_ParamChoice(context, can_fdBaudrate,&param2,true)== false){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t BUS_CAN_FDBaudrateQ(scpi_t * context){
    return Query(context);
}
scpi_result_t BUS_CAN_FDUserBaud(scpi_t * context){
    int param1;
    if (SCPI_CmdParamInt(context,&param1)==false){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    int param2;
    if (SCPI_ParamInt(context,&param2,true)== false){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t BUS_CAN_FDUserBaudQ(scpi_t * context){
    return Query(context);
}
scpi_result_t BUS_CAN_FDSamplePoint(scpi_t * context){
    int param1;
    if (SCPI_CmdParamInt(context,&param1)==false){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    double param2;
    if (SCPI_ParamDouble(context,&param2,true)== false){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_1Int1Double(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t BUS_CAN_FDSamplePointQ(scpi_t * context){
    return Query(context);
}
scpi_result_t BUS_CAN_LEV(scpi_t * context){
    int param1;
    if (!SCPI_CmdParamInt(context,  &param1)) {
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
scpi_result_t BUS_CAN_LEVQ(scpi_t * context){
    return Query(context);
}

const char* can_iso[]={
        "ISO",
        "NON",
        NULL
};
scpi_result_t BUS_CAN_ISO(scpi_t * context){
    int param1;
    if (!SCPI_CmdParamInt(context,  &param1)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    int param2;
    if (!SCPI_ParamChoice(context,can_iso,  &param2, true)) {
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t BUS_CAN_ISOQ(scpi_t * context){
    return Query(context);
}


scpi_result_t BUS_IIC_SDA(scpi_t * context)
{
    int param1;
    if (SCPI_CmdParamInt(context,&param1)==false){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    int param2;
    if (!SCPI_ParamChoice(context,allCh,&param2,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t BUS_IIC_SDAQ(scpi_t * context)
{
    return Query(context);
}
scpi_result_t BUS_IIC_SCL(scpi_t * context)
{
    int param1;
    if (SCPI_CmdParamInt(context,&param1)==false){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    int param2;
    if (!SCPI_ParamChoice(context,allCh,&param2,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t BUS_IIC_SCLQ(scpi_t * context)
{
    return Query(context);
}
scpi_result_t BUS_IIC_LEVCLK(scpi_t * context){
    int param1;
    if (!SCPI_CmdParamInt(context,  &param1)) {
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
scpi_result_t BUS_IIC_LEVCLKQ(scpi_t * context){
    return Query(context);
}
scpi_result_t BUS_IIC_LEVDAT(scpi_t * context){
    int param1;
    if (!SCPI_CmdParamInt(context,  &param1)) {
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
scpi_result_t BUS_IIC_LEVDATQ(scpi_t * context){
    return Query(context);
}


scpi_result_t BUS_1553B_CHAN(scpi_t * context)
{
    int param1;
    if (SCPI_CmdParamInt(context,&param1)==false){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    int param2;
    if (!SCPI_ParamChoice(context,allCh,&param2,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t BUS_1553B_CHANQ(scpi_t * context)
{
    return Query(context);
}

const char * c1553B_display[]={
        "BINAry",
        "HEX",
};
scpi_result_t BUS_1553B_DISP(scpi_t * context)
{
    int param1;
    if (SCPI_CmdParamInt(context,&param1)==false){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    int param2;
    if (!SCPI_ParamChoice(context,c1553B_display,&param2,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t BUS_1553B_DISPQ(scpi_t * context)
{
    return Query(context);
}
scpi_result_t BUS_1553B_LEV(scpi_t * context){
    int param1;
    if (!SCPI_CmdParamInt(context,  &param1)) {
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
scpi_result_t BUS_1553B_LEVQ(scpi_t * context){
    return Query(context);
}

scpi_result_t BUS_429_SOURce(scpi_t * context)
{
    int param1;
    if (SCPI_CmdParamInt(context,&param1)==false){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    int param2;
    if (!SCPI_ParamChoice(context,allCh,&param2,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t BUS_429_SOURceQ(scpi_t * context)
{
    return Query(context);
}
const char* art429_format[]={
        "LDAT",
        "LDSS",
        "LSDS",
        NULL
};
scpi_result_t BUS_429_FORMat(scpi_t * context)
{
    int param1;
    if (SCPI_CmdParamInt(context,&param1)==false){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    int param2;
    if (!SCPI_ParamChoice(context,art429_format,&param2,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t BUS_429_FORMatQ(scpi_t * context)
{
    return Query(context);
}
const char* art429_display[]={
        "BINAry",
        "HEX",
};
scpi_result_t BUS_429_DISP(scpi_t * context)
{
    int param1;
    if (SCPI_CmdParamInt(context,&param1)==false){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    int param2;
    if (!SCPI_ParamChoice(context,art429_display,&param2,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t BUS_429_DISPQ(scpi_t * context)
{
    return Query(context);
}
const char * art429_band[]={
        "12500",
        "100000",
};
scpi_result_t BUS_429_BAND(scpi_t * context)
{
    int param1;
    if (SCPI_CmdParamInt(context,&param1)==false){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    int param2;
    if (!SCPI_ParamChoice(context,art429_band,&param2,true)){
        dealCallBack_ParamError(context);
        return SCPI_RES_ERR;
    }
    setParam_2Int(context->env,context->param,param1,param2);
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
scpi_result_t BUS_429_BANDQ(scpi_t * context)
{
    return Query(context);
}

scpi_result_t BUS_429_HLEV(scpi_t * context){
    int param1;
    if (!SCPI_CmdParamInt(context, &param1)) {
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
scpi_result_t BUS_429_HLEVQ(scpi_t * context){
    return Query(context);
}
scpi_result_t BUS_429_LLEV(scpi_t * context){
    int param1;
    if (!SCPI_CmdParamInt(context, &param1)) {
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
scpi_result_t BUS_429_LLEVQ(scpi_t * context){
    return Query(context);
}