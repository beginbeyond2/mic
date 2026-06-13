#include <dlfcn.h>
#include <android/dlext.h>
#include <android/log.h>

#ifndef OPENCLWRAPPER_H
#define OPENCLWRAPPER_H

#include <CL/cl.h>
#define LOG_TAG "OpenCL"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

class OpenCLWrapper{
private:
    void* handle = nullptr;
    
    template<typename FuncPtr>
    bool loadFunction(FuncPtr& func, const char* name) {
        func = reinterpret_cast<FuncPtr>(dlsym(handle, name));
        if (!func) {
            LOGE("Failed to load %s: %s", name, dlerror());
            return false;
        }
        return true;
    }

public:
    // 函数指针声明
    decltype(::clGetPlatformIDs)* clGetPlatformIDs = nullptr;
    decltype(::clGetDeviceIDs)* clGetDeviceIDs = nullptr;
    decltype(::clGetDeviceInfo)* clGetDeviceInfo = nullptr;
    decltype(::clCreateContext)* clCreateContext = nullptr;
    decltype(::clCreateCommandQueue)* clCreateCommandQueue = nullptr;
    decltype(::clCreateProgramWithSource)* clCreateProgramWithSource = nullptr;
    decltype(::clBuildProgram)* clBuildProgram = nullptr;
    decltype(::clCreateKernel)* clCreateKernel = nullptr;
    decltype(::clSetKernelArg)* clSetKernelArg = nullptr;
    decltype(::clEnqueueNDRangeKernel)* clEnqueueNDRangeKernel = nullptr;
    decltype(::clReleaseKernel)* clReleaseKernel = nullptr;
    decltype(::clReleaseProgram)* clReleaseProgram = nullptr;
    decltype(::clReleaseCommandQueue)* clReleaseCommandQueue = nullptr;
    decltype(::clReleaseContext)* clReleaseContext = nullptr;
    decltype(::clReleaseDevice)* clReleaseDevice = nullptr;
    decltype(::clCreateContextFromType)* clCreateContextFromType = nullptr;
    decltype(::clGetContextInfo)* clGetContextInfo = nullptr;
    decltype(::clGetProgramBuildInfo)* clGetProgramBuildInfo = nullptr;
    decltype(::clWaitForEvents)* clWaitForEvents = nullptr;
    decltype(::clReleaseEvent)* clReleaseEvent = nullptr;
    decltype(::clCreateBuffer)* clCreateBuffer = nullptr;
    decltype(::clEnqueueMapBuffer) * clEnqueueMapBuffer = nullptr;
    decltype(::clEnqueueUnmapMemObject) * clEnqueueUnmapMemObject = nullptr;
    decltype(::clReleaseMemObject) * clReleaseMemObject = nullptr;

    bool load() {
        const char* libs[] = {
                "libOpenCL.so",
            "/system/vendor/lib64/libOpenCL.so",
            "/system/vendor/lib/libOpenCL.so",
            "/system/lib64/libOpenCL.so",
            "/system/lib/libOpenCL.so",
            nullptr
        };
        
        for (int i = 0; libs[i]; i++) {
            handle = dlopen(libs[i], RTLD_LAZY | RTLD_LOCAL);
            if (handle) {
                LOGE("load OpenCL library:%p,%s",handle,libs[i]);
                break;
            }
        }
        
        if (!handle) {
            LOGE("Failed to load OpenCL library");
            return false;
        }
        
        // 加载所有函数
        if (!loadFunction(clGetPlatformIDs, "clGetPlatformIDs")) return false;
        if (!loadFunction(clGetDeviceIDs, "clGetDeviceIDs")) return false;
        if (!loadFunction(clGetDeviceInfo, "clGetDeviceInfo")) return false;
        if (!loadFunction(clCreateContext, "clCreateContext")) return false;
        if (!loadFunction(clCreateCommandQueue, "clCreateCommandQueue")) return false;
        if (!loadFunction(clCreateProgramWithSource, "clCreateProgramWithSource")) return false;
        if (!loadFunction(clBuildProgram, "clBuildProgram")) return false;
        if (!loadFunction(clCreateKernel, "clCreateKernel")) return false;
        if (!loadFunction(clSetKernelArg, "clSetKernelArg")) return false;
        if (!loadFunction(clEnqueueNDRangeKernel, "clEnqueueNDRangeKernel")) return false;
        if (!loadFunction(clReleaseKernel, "clReleaseKernel")) return false;
        if (!loadFunction(clReleaseProgram, "clReleaseProgram")) return false;
        if (!loadFunction(clReleaseCommandQueue, "clReleaseCommandQueue")) return false;
        if (!loadFunction(clReleaseContext, "clReleaseContext")) return false;
        if (!loadFunction(clReleaseDevice, "clReleaseDevice")) return false;
        if (!loadFunction(clCreateContextFromType, "clCreateContextFromType")) return false;
        if (!loadFunction(clGetContextInfo, "clGetContextInfo")) return false;
        if (!loadFunction(clGetProgramBuildInfo, "clGetProgramBuildInfo")) return false;
        if (!loadFunction(clWaitForEvents, "clWaitForEvents")) return false;
        if (!loadFunction(clReleaseEvent, "clReleaseEvent")) return false;
        if (!loadFunction(clCreateBuffer, "clCreateBuffer")) return false;
        if (!loadFunction(clEnqueueMapBuffer, "clEnqueueMapBuffer")) return false;
        if (!loadFunction(clEnqueueUnmapMemObject, "clEnqueueUnmapMemObject")) return false;
        if (!loadFunction(clReleaseMemObject, "clReleaseMemObject")) return false;

        LOGE("load OpenCL library OK");
        return true;
    }
    
    ~OpenCLWrapper() {
        if (handle) {
            dlclose(handle);
        }
    }
};

#endif