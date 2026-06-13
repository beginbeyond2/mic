#include "scpi_help.h"
#include "../../SCPICommandCallBackJava.h"
#include "../../Log.h"


const char * allCh[] = {
    "CH1",
    "CH2",
    "CH3",
    "CH4",
    "CH5",
    "CH6",
    "CH7",
    "CH8",
    "MATH1",
    "MATH2",
    "MATH3",
    "MATH4",
    "MATH5",
    "MATH6",
    "MATH7",
    "MATH8",
    "R1",
    "R2",
    "R3",
    "R4",
    "R5",
    "R6",
    "R7",
    "R8",
    "S1",
    "S2",
    "S3",
    "S4",
    "OFF",
    "EXT",
    NULL
};
int getCh(int index)
{
//    CH_IDX ch=CI_CH1;
//    switch(index)
//    {
//        case 0:ch=CI_CH1;break;
//        case 1:ch=CI_CH2;break;
//        case 2:ch=CI_CH3;break;
//        case 3:ch=CI_CH4;break;
//        case 4:ch=CI_MATH;break;
//        case 5:ch=CI_REF1;break;
//        case 6:ch=CI_REF2;break;
//        case 7:ch=CI_REF3;break;
//        case 8:ch=CI_REF4;break;
//        case 9:ch=CI_NONE;break;
//        default:ch=CI_NONE;break;
//    }
    return index;
}

void dealCallBack_ParamError(scpi_t * context){
    context->scpi_command_index=-2;
    dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);
}

const char* WaveMode[]={
    "NORMal",
    "MAXimum",
    "RAW"
};
