//
// Created by liwb on 2018/1/15.
//

#ifndef TBOOKSCOPE_SCPICOMMANDCALLBACKJAVA_H
#define TBOOKSCOPE_SCPICOMMANDCALLBACKJAVA_H



#ifdef  __cplusplus
extern "C" {
#endif

void dealCallBack(JNIEnv * env,jobject obj,jobject param,int commandIndex);

void setCommandIndex(JNIEnv *env,jobject param,int commandIndex);

void setParam_1String(JNIEnv * env,jobject param,const char* param1);
void setParam_5String(JNIEnv * env,jobject param,const char* param1,const char* param2,const char* param3,const char *param4,const char *param5);
void setParam_5Int(JNIEnv * env,jobject param,int param1,int param2,int param3,int param4,int param5);
void setParam_6Int(JNIEnv * env,jobject param,int param1,int param2,int param3,int param4,int param5,int param6);
void setParam_5Double(JNIEnv * env,jobject param,double param1, double param2, double param3, double param4,
                      double param5);
void setParam_5Boolean(JNIEnv * env,jobject param, bool param1, double param2, double param3, double param4,
                       double param5);
void setParam_1Int1Boolean(JNIEnv * env,jobject param,int i, bool b);
void setParam_1Int1Double(JNIEnv * env,jobject param,int i, double d);


void setParam_1Boolean(JNIEnv * env,jobject param,bool b);
void setParam_1Double(JNIEnv * env,jobject param, double d);
void setParam_1Int(JNIEnv * env,jobject param, int i);

void setParam_2Double(JNIEnv * env,jobject param, double d1, double d2);
void setParam_3Double(JNIEnv * env,jobject param, double d1, double d2,double d3);
void setParam_4Double(JNIEnv * env,jobject param, double d1, double d2,double d3,double d4);
void setParam_2Int(JNIEnv * env,jobject param, int i1, int i2);
void setParam_3Int(JNIEnv * env,jobject param, int i1, int i2, int i3);
void setParam_2Int1Boolean(JNIEnv * env,jobject param,int i1,int i2, bool b);
void setParam_2Int1Double(JNIEnv * env,jobject param, int i1, int i2, double d1);
void setParam_3Int1Double(JNIEnv * env,jobject param, int i1, int i2, int i3,double d1);
void setParam_4Int1Double(JNIEnv * env,jobject param, int i1, int i2, int i3, int i4,double d1);
void setParam_5Int1Double(JNIEnv * env,jobject param, int i1, int i2, int i3, int i4,int i5,double d1);
void setParam_1Int1String(JNIEnv * env,jobject param, int i,const char* param2);
void setParam_1Int1String1Boolean(JNIEnv * env,jobject param, int i,const char* param2,bool param3);

void setParam_Resutl_1String(JNIEnv * env,jobject param,const char* param1);
#ifdef  __cplusplus
}
#endif

#endif //TBOOKSCOPE_SCPICOMMANDCALLBACKJAVA_H
