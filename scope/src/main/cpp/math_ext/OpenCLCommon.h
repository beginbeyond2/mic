//
// Created by liwb on 2025-6-11.
//

#ifndef ETO_OPENCLCOMMON_H
#define ETO_OPENCLCOMMON_H

#include "ClUtils.h"
#include <jni.h>

class OpenCLCommon {
public:
    static OpenCLCommon* GetIns();
    static void deleteIns();

    void init(JNIEnv *env,jobject asset_manager);
    static long getCurrentTime();

public:
    void intToHex(int* src,char* des,int len,int placeVal,int headLen);
    void doubleToAscii(int* src,char* des,int len,float vv,int headLen);
private:
    OpenCLCommon();
    ~OpenCLCommon();

    void warm();
private:
    static OpenCLCommon* mIns;

    ClUtils* cl;
    cl_device_id device;
    cl_event event;
    cl_context  context;
    cl_program  program;
    cl_command_queue queue;


};


#endif //ETO_OPENCLCOMMON_H
