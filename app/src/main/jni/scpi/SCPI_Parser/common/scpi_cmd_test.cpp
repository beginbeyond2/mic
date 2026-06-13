
#include "scpi_cmd_test.h"
#include <stdio.h>
#include <stdlib.h>
#include "../../SCPICommandCallBackJava.h"
#include "../../Log.h"

scpi_result_t USBC(scpi_t * context) //修改usb链接方式为usb_abd
{
//    system("/usr/bin/usb_adb");

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

scpi_result_t USBV(scpi_t * context) //修改usb链接方式为usbnet
{
//    system("/usr/bin/usbnet");

    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}

scpi_result_t REB(scpi_t * context) //重启机器
{
//    system("/sbin/reboot");
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
    return SCPI_RES_OK;
}
