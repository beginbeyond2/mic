//
// Created by liwb on 2018/1/15.
//
#include "com_micsig_tbook_tbookscope_scpi_SCPICommandDeal.h"
#include <stdio.h>
#include <string.h>

#include "SCPI_Parser/inc/parser.h"
#include "SCPI_Parser/inc/types.h"
#include "SCPI_Parser/common/scpi-def.h"
#include "Log.h"
#include "SCPICommandCallBackJava.h"

JNIEXPORT void JNICALL Java_com_micsig_tbook_tbookscope_scpi_SCPICommandDeal_scpiCommand
(JNIEnv *env,jobject obj, jstring command,jobject param){
   const char *cscpi = env->GetStringUTFChars(command, NULL);
   if (cscpi==NULL){
    //jniThrowException(env,"java/lang/RuntimeException","Out of Memory");
    return;
   }

//LOGD("setparam");
    if (scpi_context.error_queue==NULL)   SCPI_Init(&scpi_context);
   scpi_context.env=env;
   scpi_context.obj=obj;
   scpi_context.param=param;
   int len = strlen(cscpi);
// LOGD("scpi_input: %s,%d",cscpi,len);
   int i= SCPI_Input(&scpi_context,cscpi,len);
    if (i<=0){
        LOGD("scpi_input result:%d\n",i);
        dealCallBack(env,obj,param,-1);
    }
//LOGD("SCPI_Input over i:%d",i);
   env->ReleaseStringUTFChars(command, cscpi);
}