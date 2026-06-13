//
// Created by zhuzh on 2021/12/2.
//

#ifndef MY_APPLICATION_CV4L2CAMERA_H
#define MY_APPLICATION_CV4L2CAMERA_H

#include <stdint.h>
#include <android/native_window.h>
#include <android/native_window_jni.h>
#include <linux/videodev2.h>
#include "ANativeWindowEx.h"


struct VideoBuffer {
    void    *addr;
    unsigned length;
    unsigned offset;
    unsigned index;
};
class CV4L2Camera {
public:

    CV4L2Camera(int fd,ANativeWindow *window);
    bool init(int width,int height,int memType);
    void v4l2Close();
    int v4l2StreamOn();
    int v4l2StreamOff();

    int v4L2QBuf(int idx);
    int v4L2Dqbuf();
    int v4L2DqbufMap();
    int v4L2QBufMap(int idx);

    int v4L2Reqbufs(uint32_t mem, uint32_t count);
    int v4L2SFormat(int width, int height);
    void v4l2Query();
    int getFd(){return fd;}
    VideoBuffer * getVideoBuffer(int idx);
    int index;

    void Recv();
    bool isRun();
private:
    void v4L2MemMap();
    void v4L2DmaBuf();
    int fd;
    VideoBuffer vbs[MAX_WINDOW_BUFFER];
    struct v4l2_buffer  buf;
    uint32_t bufType;
    uint32_t memType;
    ANativeWindowEx windowEx;

    uint32_t  iState;
    pthread_mutex_t lock;
    pthread_t pid;
    ANativeWindow *window;
};


#endif //MY_APPLICATION_CV4L2CAMERA_H
