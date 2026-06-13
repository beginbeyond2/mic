//
// Created by liwb on 2025-6-11.
//

#include "OpenCLCommon.h"
#include <cstdlib>
#include <ctime>
#include <cstring>

#define mLOGD if(1)LOGD
extern "C" void *__memcpy_aarch64_simd (void *__restrict, const void *__restrict, size_t);
OpenCLCommon *OpenCLCommon::mIns=nullptr;
OpenCLCommon::OpenCLCommon() {
    cl= nullptr;
    device=0;
    event=0;
    context=0;
    program=0;

}

OpenCLCommon::~OpenCLCommon() {
    if (cl!= nullptr){
        cl->Cleanup(context,0,program,0);
        delete cl;
        cl= nullptr;
    }
}

OpenCLCommon *OpenCLCommon::GetIns(){
    if (mIns==nullptr){
        gOpenCLWrapper.load();
        mIns=new OpenCLCommon();

    }
    return mIns;
}
void OpenCLCommon::deleteIns() {

}
long OpenCLCommon::getCurrentTime(){
    struct timeval tv;
    gettimeofday(&tv,NULL);
    return (tv.tv_sec*1000000+tv.tv_usec)/1000;
}
void OpenCLCommon::init(JNIEnv *env,jobject asset_manager){
    if (cl== nullptr){
        mLOGD("cl is null,init param.");
        AAssetManager* mgr = AAssetManager_fromJava(env, asset_manager);
        cl=new ClUtils(mgr);
        char* fileName="Common.cl";
        cl_int err,herr=CL_SUCCESS;
        context=cl->CreateContext();
        queue= cl->CreateCommandQueue(context,&device);
        program=cl->CreateProgram(context,device,fileName);
        warm();
    }
}

void OpenCLCommon::warm(){
    cl_int err=0;
    cl_kernel kernel= gOpenCLWrapper.clCreateKernel(program,"warm",&err);
    size_t globalSize=8;
    size_t blockSize=8;
    globalSize=((globalSize+blockSize-1)/blockSize)*blockSize;
    err=gOpenCLWrapper.clEnqueueNDRangeKernel(queue,kernel,1,0,&globalSize,&blockSize,0, NULL,&event);
    if (err!= CL_SUCCESS){
        mLOGD("compute error: %d",err);
        return;
    }
    err= gOpenCLWrapper.clWaitForEvents(1,&event);
    if (err!= CL_SUCCESS){
        mLOGD("wait error: %d",err);
        return;
    }
    gOpenCLWrapper.clReleaseEvent(event);
    gOpenCLWrapper.clReleaseKernel(kernel);
    mLOGD("warm complete!");
}

void OpenCLCommon::intToHex(int* src,char* des,int len,int placeVal,int headLen)
{
    const int DOT_LENGTH=4;
    cl_int err=0;
    cl_mem clInput= gOpenCLWrapper.clCreateBuffer(context,CL_MEM_ALLOC_HOST_PTR | CL_MEM_READ_WRITE,len*sizeof(int), nullptr,&err);
    cl_int* input=(cl_int*) gOpenCLWrapper.clEnqueueMapBuffer(queue,clInput, CL_TRUE,CL_MAP_READ| CL_MAP_WRITE,0,len*sizeof(int),0,
                                               nullptr, nullptr,&err);
    int* tem=src/*+headLen/sizeof (int)*/;
    memcpy(input,tem,len*sizeof(int));
    gOpenCLWrapper.clEnqueueUnmapMemObject(queue,clInput,input,0, nullptr, nullptr);

    cl_mem clOutput= gOpenCLWrapper.clCreateBuffer(context,CL_MEM_ALLOC_HOST_PTR | CL_MEM_READ_WRITE,len*DOT_LENGTH, nullptr,&err);

    cl_kernel kernel= gOpenCLWrapper.clCreateKernel(program,"intToHex",&err);
    gOpenCLWrapper.clSetKernelArg(kernel,0,sizeof (cl_mem),&clInput);
    gOpenCLWrapper.clSetKernelArg(kernel,1,sizeof(cl_mem),&clOutput);
    gOpenCLWrapper.clSetKernelArg(kernel,2,sizeof(int),&len);
    gOpenCLWrapper.clSetKernelArg(kernel,3,sizeof(int),&placeVal);
    if (err!=CL_SUCCESS){
        mLOGD("CreateKernel:%d",err);
    }

    size_t globalSize=len;
    size_t blockSize=128;
    globalSize=((globalSize+blockSize-1)/blockSize)*blockSize;
    err= gOpenCLWrapper.clEnqueueNDRangeKernel(queue,kernel,1,0,&globalSize,&blockSize,0,NULL,&event);
    err|=gOpenCLWrapper.clWaitForEvents(1,&event);
    if (err!=CL_SUCCESS){
        mLOGD("NDRangeKernel:%d",err);
    }
    err=gOpenCLWrapper.clReleaseEvent(event);

    char* out=(char*) gOpenCLWrapper.clEnqueueMapBuffer(queue,clOutput,CL_TRUE,CL_MAP_READ| CL_MAP_WRITE,0,len*DOT_LENGTH,0,
                                         nullptr,nullptr,&err);

    memcpy(des,out,len*DOT_LENGTH);
    gOpenCLWrapper.clEnqueueUnmapMemObject(queue,clOutput,out,0, nullptr, nullptr);

    gOpenCLWrapper.clReleaseKernel(kernel);
    gOpenCLWrapper.clReleaseMemObject(clInput);
    gOpenCLWrapper.clReleaseMemObject(clOutput);

}

void OpenCLCommon::doubleToAscii(int* src,char* des,int len,float vv,int headLen)
{
//    mLOGD("double to ascii  vv:%g,src[0]:%d",vv,src[256/4] );
    const int DOT_LENGTH=16;
    int decimalPlaces=5;
    cl_int err=0;
    cl_mem clInput= gOpenCLWrapper.clCreateBuffer(context,CL_MEM_ALLOC_HOST_PTR | CL_MEM_READ_WRITE,len*sizeof(int), nullptr,&err);
    cl_int* input=(cl_int*) gOpenCLWrapper.clEnqueueMapBuffer(queue,clInput, CL_TRUE,CL_MAP_READ| CL_MAP_WRITE,0,len*sizeof(int),0,
                                               nullptr, nullptr,&err);
    int* tem=src/*+headLen/sizeof (int)*/;
    memcpy(input,tem,len*sizeof(int));
//    mLOGD("input0:%d  input1:%d",input[0],input[1]);
    gOpenCLWrapper.clEnqueueUnmapMemObject(queue,clInput,input,0, nullptr, nullptr);


//    mLOGD("doubleToASCII cpy complete!");
    cl_mem clOutput= gOpenCLWrapper.clCreateBuffer(context,CL_MEM_ALLOC_HOST_PTR | CL_MEM_READ_WRITE,len*DOT_LENGTH, nullptr,&err);

    cl_kernel kernel= gOpenCLWrapper.clCreateKernel(program,"floatToScientific",&err);
    gOpenCLWrapper.clSetKernelArg(kernel,0,sizeof (cl_mem),&clInput);
    gOpenCLWrapper.clSetKernelArg(kernel,1,sizeof(cl_mem),&clOutput);
    gOpenCLWrapper.clSetKernelArg(kernel,2,sizeof(int),&len);
    gOpenCLWrapper.clSetKernelArg(kernel,3,sizeof(float),&vv);
    gOpenCLWrapper.clSetKernelArg(kernel,4,sizeof(int),&decimalPlaces );
    if (err!=CL_SUCCESS){
        mLOGD("CreateKernel:%d",err);
    }
//    mLOGD("double to ascii ,create kernel complete!");

    size_t globalSize=len;
    size_t blockSize=128;
    globalSize=((globalSize+blockSize-1)/blockSize)*blockSize;
    err= gOpenCLWrapper.clEnqueueNDRangeKernel(queue,kernel,1,0,&globalSize,&blockSize,0,NULL,&event);
    err|= gOpenCLWrapper.clWaitForEvents(1,&event);
    if (err!=CL_SUCCESS){
        mLOGD("NDRangeKernel:%d",err);
    }
    err= gOpenCLWrapper.clReleaseEvent(event);

    char* out=(char*) gOpenCLWrapper.clEnqueueMapBuffer(queue,clOutput,CL_TRUE,CL_MAP_READ| CL_MAP_WRITE,0,len*DOT_LENGTH,0,
                                         nullptr,nullptr,&err);

    memcpy(des,out,len*DOT_LENGTH);
    gOpenCLWrapper.clEnqueueUnmapMemObject(queue,clOutput,out,0, nullptr, nullptr);

    gOpenCLWrapper.clReleaseKernel(kernel);
    gOpenCLWrapper.clReleaseMemObject(clInput);
    gOpenCLWrapper.clReleaseMemObject(clOutput);
//    mLOGD("double to ASCII Complete!");
}

