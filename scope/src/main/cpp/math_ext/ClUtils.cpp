//
// Created by liwb on 2025-3-14.
//
#include "ClUtils.h"
#include <stdlib.h>
#include <iostream>
#include <fstream>
#include <sstream>
#include <unistd.h>
#include <sys/time.h>
#include<time.h>
#include<stdio.h>
#include<stdlib.h>
#include "CommonCL.h"
#include "OpenCLWrapper.h"

OpenCLWrapper gOpenCLWrapper;

ClUtils::ClUtils(AAssetManager * mgr){
    this->mgr=mgr;
}
ClUtils::~ClUtils() {

}
cl_context ClUtils::CreateContext()
{
    cl_int errNum;
    cl_uint numPlatforms;
    cl_platform_id firstPlatformId;
    cl_context context = NULL;

    //选择可用的平台中的第一个
    errNum = gOpenCLWrapper.clGetPlatformIDs(1, &firstPlatformId, &numPlatforms);
    if (errNum != CL_SUCCESS || numPlatforms <= 0)
    {
        LOGD("Failed to find any OpenCL platforms.");
        return NULL;
    }

    //创建一个OpenCL上下文环境
    cl_context_properties contextProperties[] =
            {
                    CL_CONTEXT_PLATFORM,
                    (cl_context_properties)firstPlatformId,
                    0
            };
    context = gOpenCLWrapper.clCreateContextFromType(contextProperties, CL_DEVICE_TYPE_GPU,
                                      NULL, NULL, &errNum);
    if (errNum!=CL_SUCCESS ){
        LOGD("create context error.");
        return NULL;
    }

    return context;
}

cl_command_queue ClUtils::CreateCommandQueue(cl_context context, cl_device_id *device){
    cl_int errNum;
    cl_device_id *devices;
    cl_command_queue commandQueue = NULL;
    size_t deviceBufferSize = -1;


    // 获取设备缓冲区大小
    errNum = gOpenCLWrapper.clGetContextInfo(context, CL_CONTEXT_DEVICES, 0, NULL, &deviceBufferSize);

    if (deviceBufferSize <= 0)
    {
        LOGD("No devices available.");
        return NULL;
    }



    size_t len;
    len=deviceBufferSize/sizeof(cl_device_id);
//    LOGD("device count:%d \n",len);

    // 为设备分配缓存空间
    devices = new cl_device_id[deviceBufferSize / sizeof(cl_device_id)];
    errNum = gOpenCLWrapper.clGetContextInfo(context, CL_CONTEXT_DEVICES, deviceBufferSize, devices, NULL);
//    //打印设备信息
    for(int i=0;i<len;i++){
        LOGDDeviceInfo(devices[i]);
    }


    //选取可用设备中的第一个   CL_QUEUE_PROFILING_ENABLE：启动事件计时
    //                     CL_QUEUE_OUT_OF_ORDER_EXEC_MODE_ENABLE:启用乱序队列，用于异步加载
    commandQueue = gOpenCLWrapper.clCreateCommandQueue(context, devices[0], CL_QUEUE_PROFILING_ENABLE | CL_QUEUE_OUT_OF_ORDER_EXEC_MODE_ENABLE, &errNum);
    if (errNum != CL_SUCCESS){
        LOGD("create command queue error:%d",errNum);
    }
//    for (int i=0;i<sizeof(devices)/sizeof (cl_device_id);i++){
//        LOGDDeviceInfo(device[i]);
//    }

    *device = devices[0];
    delete[] devices;
    return commandQueue;
}
cl_program ClUtils::CreateProgram(cl_context context, cl_device_id device, const char* fileName){
    cl_int errNum;
    cl_program program;

//    android 中读文件找不到
//    std::ifstream kernelFile(fileName, std::ios::in);
//    if (!kernelFile.is_open())
//    {
////        LOGD("Failed to open file for reading: %s\n" , fileName );
//        return NULL;
//    }
//
//    std::ostringstream oss;
//    oss << kernelFile.rdbuf();
//
//    std::string srcStdStr = oss.str();
//    const char *srcStr = srcStdStr.c_str();

    AAsset* asset = AAssetManager_open(this->mgr, fileName, AASSET_MODE_BUFFER);
    if(asset) {
        size_t dataFileSize = AAsset_getLength(asset)+1; //因为最后一个字符不是0，所以手工加一个结束符
        char *buffer = (char *) malloc(dataFileSize);
        memset(buffer, 0x00, dataFileSize);
        int numBytesRead = AAsset_read(asset, buffer, dataFileSize);
        AAsset_close(asset);

//        LOGD("fileName:%s",buffer);
//        const char *srcStr = buffer;
        const char* srcStr=Content;
        program = gOpenCLWrapper.clCreateProgramWithSource(context, 1,
                                            (const char **) &srcStr,
                                            NULL, NULL);

        errNum = gOpenCLWrapper.clBuildProgram(program, 0, NULL, NULL, NULL, NULL);
        if (errNum!=CL_SUCCESS){
            LOGD("build program error:%d",errNum);
            size_t log_size;
            gOpenCLWrapper.clGetProgramBuildInfo(program,device,CL_PROGRAM_BUILD_LOG,0,NULL,&log_size);
            char * program_log=(char*)malloc(log_size+1);
            gOpenCLWrapper.clGetProgramBuildInfo(program,device,CL_PROGRAM_BUILD_LOG,log_size+1,program_log,NULL);
            LOGD("cl kernel error:%s",program_log);
            free(program_log);

        }
        free(buffer);
        return program;
    }else {
        LOGD("not find file name.");
        return NULL;
    }
}

int ClUtils::LOGDDeviceInfo(cl_device_id device)
{
    LOGD("----------------------");
    size_t valueSize;
    gOpenCLWrapper.clGetDeviceInfo(device, CL_DEVICE_NAME, 0, NULL, &valueSize);
    char* value = (char*) malloc(valueSize);
    gOpenCLWrapper.clGetDeviceInfo(device, CL_DEVICE_NAME, valueSize, value, NULL);
    LOGD("Device Name: %s\n", value);
    free(value);

    char deviceVersion[1024];
    gOpenCLWrapper.clGetDeviceInfo(device,CL_DEVICE_VERSION,sizeof(deviceVersion),deviceVersion,NULL);
    LOGD("Device Version:%s\n",deviceVersion);

    cl_device_type type;
    gOpenCLWrapper.clGetDeviceInfo(device,CL_DEVICE_TYPE,sizeof(type),&type,NULL);
    LOGD("device type(2:cpu   4:GPU   8:加速设置):%u   ",type);

    cl_uint compute_size; //计算单元个数
    gOpenCLWrapper.clGetDeviceInfo(device, CL_DEVICE_MAX_COMPUTE_UNITS,
                    sizeof(cl_uint), &compute_size, NULL);
    LOGD("compute units size(CU):%u",compute_size);
    cl_uint local_size;
    gOpenCLWrapper.clGetDeviceInfo(device, CL_DEVICE_MAX_WORK_GROUP_SIZE,
                    sizeof(cl_uint), &local_size, NULL);
    LOGD("work group size:%u",local_size);
    cl_uint local_size_dim[3];
    gOpenCLWrapper.clGetDeviceInfo(device, CL_DEVICE_MAX_WORK_ITEM_SIZES,
                    sizeof(int)*3, &local_size_dim, NULL);
    LOGD("dim1:%u dim2:%u dim3:%u",local_size_dim[0],local_size_dim[1],local_size_dim[2]);
    cl_uint fre;
    gOpenCLWrapper.clGetDeviceInfo(device,CL_DEVICE_MAX_CLOCK_FREQUENCY,
                    sizeof(cl_uint),&fre,NULL);
    LOGD("main frequery MHz:%u",fre);

    cl_uint min_ptr_size;
    gOpenCLWrapper.clGetDeviceInfo(device,CL_DEVICE_MEM_BASE_ADDR_ALIGN,
                    sizeof(cl_uint),&min_ptr_size,NULL);
    LOGD("最小指针大小（byte）:%u",min_ptr_size);

    cl_uint min_size;
    gOpenCLWrapper.clGetDeviceInfo(device,CL_DEVICE_MIN_DATA_TYPE_ALIGN_SIZE,
                    sizeof(cl_uint),&min_size,NULL);
    LOGD("最小对齐小大（byte）:%u",min_size);

    cl_ulong local_mem_size;
    gOpenCLWrapper.clGetDeviceInfo(device,CL_DEVICE_LOCAL_MEM_SIZE,
                    sizeof(cl_ulong),&local_mem_size,NULL );
    LOGD("本地组大小：%d kb",local_mem_size/1024);

    size_t time_unit;
    gOpenCLWrapper.clGetDeviceInfo(device,CL_DEVICE_PROFILING_TIMER_RESOLUTION,
                    sizeof(time_unit),&time_unit,NULL);
    LOGD("time unit:%d",time_unit);

    size_t sub_devices;
    gOpenCLWrapper.clGetDeviceInfo(device, CL_DEVICE_PARTITION_MAX_SUB_DEVICES, sizeof(sub_devices), &sub_devices, NULL);
    LOGD("子设备:%d ",sub_devices);

    cl_uint max_queue;
    gOpenCLWrapper.clGetDeviceInfo(device, CL_DEVICE_MAX_ON_DEVICE_QUEUES, sizeof(max_queue), &max_queue, NULL);
    LOGD("最大设备队列数:%u",max_queue);

    //SVM不是所有的显卡都支持，只有部分支持
    cl_device_svm_capabilities caps;
    cl_int err=gOpenCLWrapper.clGetDeviceInfo(device,CL_DEVICE_SVM_CAPABILITIES,sizeof(cl_device_svm_capabilities),&caps,0);
    if (err==CL_INVALID_VALUE){
        LOGD("不支持");
    }
    LOGD("支持的SVM:%d",caps);
    if (err==CL_SUCCESS && (caps & CL_DEVICE_SVM_COARSE_GRAIN_BUFFER)){
        LOGD("粗粒度缓冲SVM");
    }
    if (err==CL_SUCCESS && (caps & CL_DEVICE_SVM_FINE_GRAIN_BUFFER)){
        LOGD("细粒度缓冲SVM");
    }
    if (err==CL_SUCCESS && (caps & CL_DEVICE_SVM_FINE_GRAIN_BUFFER) && (caps & CL_DEVICE_SVM_ATOMICS)){
        LOGD("带原子操作的细粒度缓冲SVM");
    }
    if (err==CL_SUCCESS && (caps & CL_DEVICE_SVM_FINE_GRAIN_SYSTEM)){
        LOGD("细料度系统SVM");
    }
    if (err==CL_SUCCESS  && (caps & CL_DEVICE_SVM_FINE_GRAIN_SYSTEM) && (caps & CL_DEVICE_SVM_ATOMICS)){
        LOGD("带原子操作的细粒度系统SVM");
    }


    //一个工作组内最多256个线程，但是没有找到获得的方法。

    return local_size;
}

void ClUtils::Cleanup(cl_context context, cl_command_queue commandQueue,
                      cl_program program, cl_kernel kernel){
//    for (int i = 0; i < 3; i++)
//    {
//        if (memObjects[i] != 0)
//            clReleaseMemObject(memObjects[i]);
//    }
    if (commandQueue != 0)
        gOpenCLWrapper.clReleaseCommandQueue(commandQueue);

    if (kernel != 0)
        gOpenCLWrapper.clReleaseKernel(kernel);

    if (program != 0)
        gOpenCLWrapper.clReleaseProgram(program);

    if (context != 0)
        gOpenCLWrapper.clReleaseContext(context);
}