//
// Created by liwb on 2025-3-14.
//

#ifndef OPENCLIMAGE3568_CLUTILS_H
#define OPENCLIMAGE3568_CLUTILS_H



#define CL_TARGET_OPENCL_VERSION 200

#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>

#include "OpenCLWrapper.h"
#include <android/log.h>
#define TAG OpenCL
#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,"OPENCL",__VA_ARGS__)



class ClUtils {

private:
    AAssetManager *mgr;
public:
    ClUtils(AAssetManager *mgr);
    ~ClUtils();
    cl_context CreateContext();
    cl_command_queue CreateCommandQueue(cl_context context, cl_device_id *device);
    cl_program CreateProgram(cl_context context, cl_device_id device, const char* fileName);
    int LOGDDeviceInfo(cl_device_id device);
    void Cleanup(cl_context context, cl_command_queue commandQueue,
                 cl_program program, cl_kernel kernel);
};

extern OpenCLWrapper gOpenCLWrapper;
#endif //OPENCLIMAGE3568_CLUTILS_H
