//
// Created by liwb on 2018/1/15.
//

#include <jni.h>
#include "SCPICommandCallBackJava.h"
#include <android/log.h>


#define TAG "liwb"

#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, TAG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

void dealCallBack(JNIEnv * env,jobject obj,jobject param,int commandIndex){
    jclass clazz = (env)->FindClass( "com/micsig/tbook/tbookscope/scpi/SCPICommandDeal");
    if(clazz == 0){
        return;
    }
    jmethodID method3 = (env)->GetMethodID(clazz,"deal","(Lcom/micsig/tbook/tbookscope/scpi/SCPIParam;)V");
    if(method3 == 0){
        return;
    }
    setCommandIndex(env,param,commandIndex);
    (env)->CallVoidMethod( obj, method3,param);
}

void setCommandIndex(JNIEnv *env,jobject param,int commandIndex){
    jclass objectClass = (env)->FindClass("com/micsig/tbook/tbookscope/scpi/SCPIParam");
    jfieldID iCommandIndex = (env)->GetFieldID(objectClass, "commandIndex", "I");
    (env)->SetIntField(param, iCommandIndex,commandIndex);
}

void setParam_1String(JNIEnv * env,jobject param,const char* param1){
    //获取Java中的实例类
    jclass objectClass = (env)->FindClass("com/micsig/tbook/tbookscope/scpi/SCPIParam");
    //获取类中每一个变量的定义
    jfieldID sParam1 = (env)->GetFieldID(objectClass, "sParam1", "Ljava/lang/String;");
    //给每一个实例的变量付值
    (env)->SetObjectField(param, sParam1, env->NewStringUTF(param1));
}

void setParam_5String(JNIEnv * env,jobject param,const char* param1,const char* param2,const char* param3,const char *param4,const char *param5){
    //获取Java中的实例类
    jclass objectClass = (env)->FindClass("com/micsig/tbook/tbookscope/scpi/SCPIParam");

    //获取类中每一个变量的定义
    jfieldID sParam1 = (env)->GetFieldID(objectClass, "sParam1", "Ljava/lang/String;");
    jfieldID sParam2 = (env)->GetFieldID(objectClass, "sParam2", "Ljava/lang/String;");
    jfieldID sParam3 =  (env)->GetFieldID(objectClass, "sParam3", "Ljava/lang/String;");
    jfieldID sParam4 =  (env)->GetFieldID(objectClass, "sParam4", "Ljava/lang/String;");
    jfieldID sParam5 =  (env)->GetFieldID(objectClass, "sParam5", "Ljava/lang/String;");


    //给每一个实例的变量付值
    (env)->SetObjectField(param, sParam1, env->NewStringUTF(param1));
    (env)->SetObjectField(param, sParam2, env->NewStringUTF(param2));
    (env)->SetObjectField(param, sParam3, env->NewStringUTF(param3));
    (env)->SetObjectField(param, sParam4, env->NewStringUTF(param4));
    (env)->SetObjectField(param, sParam5, env->NewStringUTF(param5));
    //(env)->SetObjectField(param, strStream, (e)->NewStringUTF("my stream"));
}

void setParam_5Int(JNIEnv * env,jobject param,int param1,int param2,int param3,int param4,int param5){
//获取Java中的实例类
    jclass objectClass = (env)->FindClass("com/micsig/tbook/tbookscope/scpi/SCPIParam");

    //获取类中每一个变量的定义
    jfieldID iParam1 = (env)->GetFieldID(objectClass, "iParam1", "I");
    jfieldID iParam2 = (env)->GetFieldID(objectClass, "iParam2", "I");
    jfieldID iParam3 =  (env)->GetFieldID(objectClass, "iParam3", "I");
    jfieldID iParam4 =  (env)->GetFieldID(objectClass, "iParam4", "I");
    jfieldID iParam5 =  (env)->GetFieldID(objectClass, "iParam5", "I");


    //给每一个实例的变量付值
    (env)->SetIntField(param, iParam1, param1);
    (env)->SetIntField(param, iParam2, param2);
    (env)->SetIntField(param, iParam3, param3);
    (env)->SetIntField(param, iParam4, param4);
    (env)->SetIntField(param, iParam5, param5);
}

void setParam_6Int(JNIEnv * env,jobject param,int param1,int param2,int param3,int param4,int param5,int param6){
//获取Java中的实例类
    jclass objectClass = (env)->FindClass("com/micsig/tbook/tbookscope/scpi/SCPIParam");

    //获取类中每一个变量的定义
    jfieldID iParam1 = (env)->GetFieldID(objectClass, "iParam1", "I");
    jfieldID iParam2 = (env)->GetFieldID(objectClass, "iParam2", "I");
    jfieldID iParam3 =  (env)->GetFieldID(objectClass, "iParam3", "I");
    jfieldID iParam4 =  (env)->GetFieldID(objectClass, "iParam4", "I");
    jfieldID iParam5 =  (env)->GetFieldID(objectClass, "iParam5", "I");
    jfieldID iParam6 =  (env)->GetFieldID(objectClass, "iParam6", "I");


    //给每一个实例的变量付值
    (env)->SetIntField(param, iParam1, param1);
    (env)->SetIntField(param, iParam2, param2);
    (env)->SetIntField(param, iParam3, param3);
    (env)->SetIntField(param, iParam4, param4);
    (env)->SetIntField(param, iParam5, param5);
    (env)->SetIntField(param, iParam6, param6);
}
void setParam_5Double(JNIEnv * env,jobject param,double param1, double param2, double param3, double param4,
                      double param5){
    //获取Java中的实例类
    jclass objectClass = (env)->FindClass("com/micsig/tbook/tbookscope/scpi/SCPIParam");

    //获取类中每一个变量的定义
    jfieldID dParam1 = (env)->GetFieldID(objectClass, "dParam1", "D");
    jfieldID dParam2 = (env)->GetFieldID(objectClass, "dParam2", "D");
    jfieldID dParam3 =  (env)->GetFieldID(objectClass, "dParam3", "D");
    jfieldID dParam4 =  (env)->GetFieldID(objectClass, "dParam4", "D");
    jfieldID dParam5 =  (env)->GetFieldID(objectClass, "dParam5", "D");

    //给每一个实例的变量付值
    (env)->SetDoubleField(param, dParam1, param1);
    (env)->SetDoubleField(param, dParam2, param2);
    (env)->SetDoubleField(param, dParam3, param3);
    (env)->SetDoubleField(param, dParam4, param4);
    (env)->SetDoubleField(param, dParam5, param5);
}
void setParam_5Boolean(JNIEnv * env,jobject param, bool param1, bool param2, bool param3, bool param4,
                       bool param5){
    //获取Java中的实例类
    jclass objectClass = (env)->FindClass("com/micsig/tbook/tbookscope/scpi/SCPIParam");

    //获取类中每一个变量的定义
    jfieldID bParam1 =  (env)->GetFieldID(objectClass, "bParam1", "Z");
    jfieldID bParam2 =  (env)->GetFieldID(objectClass, "bParam2", "Z");
    jfieldID bParam3 =  (env)->GetFieldID(objectClass, "bParam3", "Z");
    jfieldID bParam4 =  (env)->GetFieldID(objectClass, "bParam4", "Z");
    jfieldID bParam5 =  (env)->GetFieldID(objectClass, "bParam5", "Z");

    //给每一个实例的变量付值
    (env)->SetBooleanField(param, bParam1, param1);
    (env)->SetBooleanField(param, bParam2, param2);
    (env)->SetBooleanField(param, bParam3, param3);
    (env)->SetBooleanField(param, bParam4, param4);
    (env)->SetBooleanField(param, bParam5, param5);
}

void setParam_1Int1Boolean(JNIEnv * env,jobject param,int i, bool b){
    //获取Java中的实例类
    jclass objectClass = (env)->FindClass("com/micsig/tbook/tbookscope/scpi/SCPIParam");

    //获取类中每一个变量的定义
    jfieldID iParam1 = (env)->GetFieldID(objectClass, "iParam1", "I");
    jfieldID bParam1 = (env)->GetFieldID(objectClass, "bParam1", "Z");

    (env)->SetIntField(param, iParam1, i);
    (env)->SetBooleanField(param, bParam1, b);
}

void setParam_2Int1Boolean(JNIEnv * env,jobject param,int i1,int i2, bool b){
    //获取Java中的实例类
    jclass objectClass = (env)->FindClass("com/micsig/tbook/tbookscope/scpi/SCPIParam");

    //获取类中每一个变量的定义
    jfieldID iParam1 = (env)->GetFieldID(objectClass, "iParam1", "I");
    jfieldID iParam2 = (env)->GetFieldID(objectClass, "iParam2", "I");
    jfieldID bParam1 = (env)->GetFieldID(objectClass, "bParam1", "Z");

    (env)->SetIntField(param, iParam1, i1);
    (env)->SetIntField(param, iParam2, i2);
    (env)->SetBooleanField(param, bParam1, b);
}
void setParam_1Int1Double(JNIEnv * env,jobject param,int i, double d){
    //获取Java中的实例类
    jclass objectClass = (env)->FindClass("com/micsig/tbook/tbookscope/scpi/SCPIParam");

    //获取类中每一个变量的定义
    jfieldID iParam1 = (env)->GetFieldID(objectClass, "iParam1", "I");
    jfieldID dParam1 = (env)->GetFieldID(objectClass, "dParam1", "D");

    (env)->SetIntField(param, iParam1, i);
    (env)->SetDoubleField(param, dParam1, d);
}

void setParam_1Boolean(JNIEnv * env,jobject param,bool b){
    //获取Java中的实例类
    jclass objectClass = (env)->FindClass("com/micsig/tbook/tbookscope/scpi/SCPIParam");
    //获取类中每一个变量的定义
    jfieldID bParam1 = (env)->GetFieldID(objectClass, "bParam1", "Z");
    (env)->SetBooleanField(param, bParam1, b);
}

void setParam_1Double(JNIEnv * env,jobject param, double d){
    //获取Java中的实例类
    jclass objectClass = (env)->FindClass("com/micsig/tbook/tbookscope/scpi/SCPIParam");
    //获取类中每一个变量的定义
    jfieldID dParam1 = (env)->GetFieldID(objectClass, "dParam1", "D");
    (env)->SetDoubleField(param, dParam1, d);
}

void setParam_1Int(JNIEnv * env,jobject param, int i){
    //获取Java中的实例类
    jclass objectClass = (env)->FindClass("com/micsig/tbook/tbookscope/scpi/SCPIParam");
    //获取类中每一个变量的定义
    jfieldID iParam1 = (env)->GetFieldID(objectClass, "iParam1", "I");
    (env)->SetIntField(param, iParam1, i);
}



void setParam_2Double(JNIEnv * env,jobject param, double d1, double d2){
    //获取Java中的实例类
    jclass objectClass = (env)->FindClass("com/micsig/tbook/tbookscope/scpi/SCPIParam");
    //获取类中每一个变量的定义
    jfieldID dParam1 = (env)->GetFieldID(objectClass, "dParam1", "D");
    jfieldID dParam2 = (env)->GetFieldID(objectClass, "dParam2", "D");
    (env)->SetDoubleField(param, dParam1, d1);
    (env)->SetDoubleField(param, dParam2, d2);
}
void setParam_3Double(JNIEnv * env,jobject param, double d1, double d2,double d3){
    //获取Java中的实例类
    jclass objectClass = (env)->FindClass("com/micsig/tbook/tbookscope/scpi/SCPIParam");
    //获取类中每一个变量的定义
    jfieldID dParam1 = (env)->GetFieldID(objectClass, "dParam1", "D");
    jfieldID dParam2 = (env)->GetFieldID(objectClass, "dParam2", "D");
    jfieldID dParam3 = (env)->GetFieldID(objectClass, "dParam3", "D");
    (env)->SetDoubleField(param, dParam1, d1);
    (env)->SetDoubleField(param, dParam2, d2);
    (env)->SetDoubleField(param, dParam3, d3);
}
void setParam_4Double(JNIEnv * env,jobject param, double d1, double d2,double d3,double d4){
    //获取Java中的实例类
    jclass objectClass = (env)->FindClass("com/micsig/tbook/tbookscope/scpi/SCPIParam");
    //获取类中每一个变量的定义
    jfieldID dParam1 = (env)->GetFieldID(objectClass, "dParam1", "D");
    jfieldID dParam2 = (env)->GetFieldID(objectClass, "dParam2", "D");
    jfieldID dParam3 = (env)->GetFieldID(objectClass, "dParam3", "D");
    jfieldID dParam4 = (env)->GetFieldID(objectClass, "dParam4", "D");
    (env)->SetDoubleField(param, dParam1, d1);
    (env)->SetDoubleField(param, dParam2, d2);
    (env)->SetDoubleField(param, dParam3, d3);
    (env)->SetDoubleField(param, dParam4, d4);
}

void setParam_2Int(JNIEnv * env,jobject param, int i1, int i2){
    //获取Java中的实例类
    jclass objectClass = (env)->FindClass("com/micsig/tbook/tbookscope/scpi/SCPIParam");
    //获取类中每一个变量的定义
    jfieldID iParam1 = (env)->GetFieldID(objectClass, "iParam1", "I");
    jfieldID iParam2 = (env)->GetFieldID(objectClass, "iParam2", "I");
    (env)->SetIntField(param, iParam1, i1);
    (env)->SetIntField(param, iParam2, i2);
}

void setParam_3Int(JNIEnv * env,jobject param, int i1, int i2,int i3){
    //获取Java中的实例类
    jclass objectClass = (env)->FindClass("com/micsig/tbook/tbookscope/scpi/SCPIParam");
    //获取类中每一个变量的定义
    jfieldID iParam1 = (env)->GetFieldID(objectClass, "iParam1", "I");
    jfieldID iParam2 = (env)->GetFieldID(objectClass, "iParam2", "I");
    jfieldID iParam3 = (env)->GetFieldID(objectClass, "iParam3", "I");
    (env)->SetIntField(param, iParam1, i1);
    (env)->SetIntField(param, iParam2, i2);
    (env)->SetIntField(param, iParam3, i3);
}


void setParam_2Int1Double(JNIEnv * env,jobject param, int i1, int i2, double d1){
    //获取Java中的实例类
    jclass objectClass = (env)->FindClass("com/micsig/tbook/tbookscope/scpi/SCPIParam");
    //获取类中每一个变量的定义
    jfieldID iParam1 = (env)->GetFieldID(objectClass, "iParam1", "I");
    jfieldID iParam2 = (env)->GetFieldID(objectClass, "iParam2", "I");
    jfieldID dParam1=(env)->GetFieldID(objectClass, "dParam1", "D");
    (env)->SetIntField(param, iParam1, i1);
    (env)->SetIntField(param, iParam2, i2);
    (env)->SetDoubleField(param, dParam1, d1);
}

void setParam_4Int1Double(JNIEnv * env,jobject param, int i1, int i2, int i3,int i4,double d1){
    //获取Java中的实例类
    jclass objectClass = (env)->FindClass("com/micsig/tbook/tbookscope/scpi/SCPIParam");
    //获取类中每一个变量的定义
    jfieldID iParam1 = (env)->GetFieldID(objectClass, "iParam1", "I");
    jfieldID iParam2 = (env)->GetFieldID(objectClass, "iParam2", "I");
    jfieldID iParam3 = (env)->GetFieldID(objectClass, "iParam3", "I");
    jfieldID iParam4 = (env)->GetFieldID(objectClass, "iParam4", "I");
    jfieldID dParam1 = (env)->GetFieldID(objectClass, "dParam1", "D");
    (env)->SetIntField(param, iParam1, i1);
    (env)->SetIntField(param, iParam2, i2);
    (env)->SetIntField(param, iParam3, i3);
    (env)->SetIntField(param, iParam4, i4);
    (env)->SetDoubleField(param, dParam1, d1);
}

void setParam_5Int1Double(JNIEnv * env,jobject param, int i1, int i2, int i3, int i4,int i5,double d1){
    //获取Java中的实例类
    jclass objectClass = (env)->FindClass("com/micsig/tbook/tbookscope/scpi/SCPIParam");
    //获取类中每一个变量的定义
    jfieldID iParam1 = (env)->GetFieldID(objectClass, "iParam1", "I");
    jfieldID iParam2 = (env)->GetFieldID(objectClass, "iParam2", "I");
    jfieldID iParam3 = (env)->GetFieldID(objectClass, "iParam3", "I");
    jfieldID iParam4 = (env)->GetFieldID(objectClass, "iParam4", "I");
    jfieldID iParam5 = (env)->GetFieldID(objectClass, "iParam5", "I");
    jfieldID dParam1 = (env)->GetFieldID(objectClass, "dParam1", "D");
    (env)->SetIntField(param, iParam1, i1);
    (env)->SetIntField(param, iParam2, i2);
    (env)->SetIntField(param, iParam3, i3);
    (env)->SetIntField(param, iParam4, i4);
    (env)->SetIntField(param, iParam5, i5);
    (env)->SetDoubleField(param, dParam1, d1);
}

void setParam_3Int1Double(JNIEnv * env,jobject param, int i1, int i2, int i3,double d1){
    //获取Java中的实例类
    jclass objectClass = (env)->FindClass("com/micsig/tbook/tbookscope/scpi/SCPIParam");
    //获取类中每一个变量的定义
    jfieldID iParam1 = (env)->GetFieldID(objectClass, "iParam1", "I");
    jfieldID iParam2 = (env)->GetFieldID(objectClass, "iParam2", "I");
    jfieldID iParam3 = (env)->GetFieldID(objectClass, "iParam3", "I");
    jfieldID dParam1 = (env)->GetFieldID(objectClass, "dParam1", "D");
    (env)->SetIntField(param, iParam1, i1);
    (env)->SetIntField(param, iParam2, i2);
    (env)->SetIntField(param, iParam3, i3);
    (env)->SetDoubleField(param, dParam1, d1);
}

void setParam_1Int1String(JNIEnv * env,jobject param, int i,const char* param2){
    //获取Java中的实例类
    jclass objectClass = (env)->FindClass("com/micsig/tbook/tbookscope/scpi/SCPIParam");
    //获取类中每一个变量的定义
    jfieldID iParam1 = (env)->GetFieldID(objectClass, "iParam1", "I");
    jfieldID sResultParam1 = (env)->GetFieldID(objectClass, "sParam1", "Ljava/lang/String;");
    (env)->SetIntField(param, iParam1, i);
    (env)->SetObjectField(param, sResultParam1,env->NewStringUTF( param2));
}

void setParam_1Int1String1Boolean(JNIEnv * env,jobject param, int i,const char* param2,bool param3){
    //获取Java中的实例类
    jclass objectClass = (env)->FindClass("com/micsig/tbook/tbookscope/scpi/SCPIParam");
    //获取类中每一个变量的定义
    jfieldID iParam1 = (env)->GetFieldID(objectClass, "iParam1", "I");
    jfieldID sResultParam1 = (env)->GetFieldID(objectClass, "sParam1", "Ljava/lang/String;");
    jfieldID bParam1 = (env)->GetFieldID(objectClass, "bParam1", "Z");
    (env)->SetIntField(param, iParam1, i);
    (env)->SetObjectField(param, sResultParam1,env->NewStringUTF( param2));
    (env)->SetBooleanField(param, bParam1, param3);
}

void setParam_Resutl_1String(JNIEnv * env,jobject param,const char* param1){
    //获取Java中的实例类
    jclass objectClass = (env)->FindClass("com/micsig/tbook/tbookscope/scpi/SCPIParam");
    //获取类中每一个变量的定义
    jfieldID sResultParam1 = (env)->GetFieldID(objectClass, "sResultParam1", "Ljava/lang/String;");
    (env)->SetObjectField(param, sResultParam1,env->NewStringUTF( param1));
}