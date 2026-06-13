//
// Created by zhuzh on 2024/1/30.
//
#include "Logger.h"
#include <dlfcn.h>
#include "ANativeWindowEx.h"
#define TAG ("ANativeWindowEx")
void * ANativeWindowEx::handle = NULL;
ANativeWindow_dequeueBufferEx ANativeWindowEx::dequeueBufferFun = NULL;
ANativeWindow_queueBuffer ANativeWindowEx::queueBufferFun = NULL;
ANativeWindow_cancelBuffer ANativeWindowEx::cancelBufferFun = NULL;


ANativeWindowEx::ANativeWindowEx(ANativeWindow *window) :
window(window)
{
    if(ANativeWindowEx::handle == NULL){
        ANativeWindowEx::handle = dlopen("libnativewindow.so", RTLD_LAZY);
        if(ANativeWindowEx::handle != NULL){
            ANativeWindowEx::dequeueBufferFun = (ANativeWindow_dequeueBufferEx) dlsym(handle, "ANativeWindow_dequeueBufferEx");
            ANativeWindowEx::queueBufferFun = (ANativeWindow_queueBuffer) dlsym(handle, "ANativeWindow_queueBuffer");
            ANativeWindowEx::cancelBufferFun = (ANativeWindow_cancelBuffer) dlsym(handle, "ANativeWindow_cancelBuffer");
        }
    }

}

int ANativeWindowEx::dequeueBuffer(int index) {
    if(index >= 0 && index < MAX_WINDOW_BUFFER){
        ANativeWindowBuffer * buffer;
        int fenceFd, fd;
        if(dequeueBufferFun(window,&buffer,&fenceFd,&fd) == 0)
        {
            bufferEx[index].buffer = buffer;
            bufferEx[index].fencefd = fenceFd;
            bufferEx[index].fd = fd;
            return fd;
        }
    }
    return -1;
}

int ANativeWindowEx::queueBuffer(int index) {
    if(index >= 0 && index < MAX_WINDOW_BUFFER) {
        return queueBufferFun(window,  bufferEx[index].buffer, bufferEx[index].fencefd);
    }
    return -1;
}

int ANativeWindowEx::cancelBuffer(int index) {
    if(index >= 0 && index < MAX_WINDOW_BUFFER) {
        return cancelBufferFun(window,  bufferEx[index].buffer, bufferEx[index].fencefd);
    }
    return -1;
}


