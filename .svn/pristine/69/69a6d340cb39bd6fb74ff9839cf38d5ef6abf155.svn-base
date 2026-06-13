//
// Created by zhuzh on 2021/12/2.
//

#include <android/native_window_jni.h>
#include <android/native_window.h>
#include <unistd.h>
#include <sys/mman.h>
#include <sys/ioctl.h>
#include <stdlib.h>
#include <stdio.h>
#include <fcntl.h>
#include <errno.h>
#include <string.h>
#include <pthread.h>
#include "Logger.h"
#include "CV4L2Camera.h"
#define TAG ("CV4L2Camera")

#define CLEAR(x) memset(&x,0,sizeof(x))
#define WIN_WIDTH (1808)
#define WIN_HEIGHT (2000)
#define DATA_HEIGHT (1000)
static double GetTickCount(){
    timeval tv;
    gettimeofday(&tv, NULL);
    return tv.tv_sec * 1000000.0 + tv.tv_usec;
}
static void *pthread_mipi_recv(void* args) {
    CV4L2Camera * dev = (CV4L2Camera * )args;
    if(dev != NULL){
        dev->Recv();
    }
    return 0;
}

void CV4L2Camera::Recv() {

    if(memType == V4L2_MEMORY_DMABUF){
        int idx = 0;
        double t = GetTickCount();
        //int frameNums = 0;
        while (isRun()) {
            idx = v4L2Dqbuf();
            if (idx < 0) {
                LOGE(" Recv v4L2Dqbuf :  %d", idx);
                break;
            }
            v4L2QBuf(idx);
//            frameNums++;
//            if(frameNums >= 400){
//                LOGD("bmp FrameNums: %f/s",frameNums * 1e6/(GetTickCount() - t));
//                t = GetTickCount();
//                frameNums = 0;
//            }
        }
    }
    iState = 2;
}


CV4L2Camera::CV4L2Camera(int fd,ANativeWindow *window):
fd(fd),
windowEx(window),
window(window)
{
    pthread_mutex_init(&lock, NULL);
    init(WIN_WIDTH,window ? WIN_HEIGHT : DATA_HEIGHT,window ? V4L2_MEMORY_DMABUF : V4L2_MEMORY_MMAP);
    iState = 1;
    pthread_create(&pid, 0, pthread_mipi_recv, this);
}


int CV4L2Camera::v4L2SFormat(int width, int height)
{
    v4l2_format format;

    CLEAR(format);
    format.type = bufType;
    format.fmt.pix_mp.width = width;
    format.fmt.pix_mp.height = height;		// 逐行指描不用乘2， 隔行扫描，高度乘2
    format.fmt.pix_mp.pixelformat = V4L2_PIX_FMT_YUYV; // V4L2_PIX_FMT_RGB565; // V4L2_PIX_FMT_YUYV; //V4L2_PIX_FMT_NV12;
    format.fmt.pix_mp.field = V4L2_FIELD_NONE; // 逐行扫描
    if(ioctl(fd,VIDIOC_S_FMT,&format) < 0){
        LOGE("[native_camera][%s]  VIDIOC_S_FMT failed! %s.", __FUNCTION__, strerror(errno));
        return -1;
    }
    memset(&format, 0, sizeof(format));
    format.type = bufType;
    if (ioctl(fd, VIDIOC_G_FMT, &format) == 0){
        LOGI("[native_camera][%s] Current output format:  fmt=0x%X, %dx%d", __FUNCTION__,
              format.fmt.pix_mp.pixelformat,
              format.fmt.pix_mp.width,
              format.fmt.pix_mp.height
        );
    } else {
        LOGE("[native_camera] VIDIOC_G_FMT: %s", strerror(errno));

        return -1;
    }

    return 0;
}
int CV4L2Camera::v4L2Reqbufs(uint32_t mem, uint32_t count)
{
    v4l2_requestbuffers bufrequest;

    CLEAR(bufrequest);
    bufrequest.type = bufType;
    bufrequest.memory =  mem;
    bufrequest.count = count;

    if (ioctl(fd, VIDIOC_REQBUFS, &bufrequest) < 0) {
        LOGE("[native_camera] VIDIOC_REQBUFS: %s", strerror(errno));
        return -1;
    }
    return 0;
}

int CV4L2Camera::v4L2DqbufMap()
{
    struct v4l2_plane planes={0};
    CLEAR(buf);
    CLEAR(planes);
    buf.type = bufType;
    buf.memory = memType;
    buf.m.planes = &planes;
    buf.length = 1;
//    LOGI("%s,%d",__FUNCTION__,__LINE__);
    if (ioctl(fd, VIDIOC_DQBUF, &buf) < 0) {
        LOGE("[native_camera] %s  VIDIOC_DQBUF failed! %s.", __FUNCTION__, strerror(errno));
        return -1;
    }
//    LOGI("%s,%d",__FUNCTION__,__LINE__);
    return buf.index;
}
int CV4L2Camera::v4L2QBufMap(int idx){

    struct v4l2_plane  planes;
    CLEAR(buf);
    CLEAR(planes);
    buf.type = bufType;
    buf.memory = memType;
    buf.m.planes = &planes;
    buf.index = idx;
    buf.m.planes->length = vbs[idx].length;
    buf.length = 1;
    buf.m.planes->m.mem_offset = vbs[idx].offset;
//    LOGI("%s,%d,%d",__FUNCTION__,__LINE__,idx);
    if (ioctl(fd, VIDIOC_QBUF, &buf) < 0) {
        LOGE("[native_camera] %s  VIDIOC_QBUF failed! error=%s", __FUNCTION__, strerror(errno));

        return -1;
    }
    //LOGI("%s,%d",__FUNCTION__,__LINE__);
    return 0;
}


int CV4L2Camera::v4L2Dqbuf()
{
    struct v4l2_plane planes;
    CLEAR(buf);
    CLEAR(planes);
    buf.type = bufType;
    buf.memory = memType;
    buf.m.planes = &planes;
    buf.length = 1;
    if (ioctl(fd, VIDIOC_DQBUF, &buf) < 0) {
        LOGE("[native_camera] %s  VIDIOC_DQBUF failed! %s.", __FUNCTION__, strerror(errno));
        return -1;
    }
    int ret = windowEx.queueBuffer(buf.index);
    if( ret < 0){
        LOGE("queueBuffer : %d",ret);
    }
    return buf.index;
}
int CV4L2Camera::v4L2QBuf(int idx){

    int ret;
    struct v4l2_plane  planes;
    CLEAR(buf);
    CLEAR(planes);
    buf.type = bufType;
    buf.memory = memType;
    buf.m.planes = &planes;
    buf.index = idx;
    buf.length = 1;
    ret = windowEx.dequeueBuffer(idx);
    if(ret < 0){
        LOGE("dequeueBuffer : %d",ret);
        return -1;
    }
    planes.m.fd = ret;
    if (ioctl(fd, VIDIOC_QBUF, &buf) < 0) {
        LOGE("[native_camera] %s  VIDIOC_QBUF failed! error=%s", __FUNCTION__, strerror(errno));

        return -1;
    }

    return 0;
}

int CV4L2Camera::v4l2StreamOn()
{
    int type = bufType;
    if (ioctl(fd, VIDIOC_STREAMON, &type) < 0) {

        LOGE("V4l2Capture::%s VIDIOC_STREAMON error", __func__);
        return(-1);
    } else{

        LOGI("V4l2Capture::%s VIDIOC_STREAMON success", __func__);
        return 0;
    }

}

int CV4L2Camera::v4l2StreamOff()
{
    int type = bufType;
    if (ioctl(fd, VIDIOC_STREAMOFF, &type) < 0) {
        LOGE("VIDIOC_STREAMOFF: %s", strerror(errno));
        return -1;
    }
    return 0;
}
void CV4L2Camera::v4l2Close()
{
    if(memType == V4L2_MEMORY_MMAP){
        for (int i=0; i<MAX_WINDOW_BUFFER; i++){
            munmap(vbs[i].addr,vbs[i].length);
        }
    }else{
        iState = 0;
    }

    close(fd);
    do{
        usleep(100);
    } while (iState != 2);
    pthread_mutex_lock(&lock);
    pthread_mutex_unlock(&lock);
}

VideoBuffer * CV4L2Camera::getVideoBuffer(int idx)
{
    VideoBuffer * vb = NULL;
    if(idx >= 0 && idx < MAX_WINDOW_BUFFER)
    {
        vb = &vbs[idx];
    }
    return vb;
}
void CV4L2Camera::v4L2MemMap()
{
    v4L2Reqbufs(memType, MAX_WINDOW_BUFFER);
    struct v4l2_plane  planes;
    for (int i=0; i<MAX_WINDOW_BUFFER; i++)
    {
        CLEAR(buf);
        CLEAR(planes);
        buf.type   = bufType;
        buf.memory = memType;
        buf.index  = i;
        buf.length = 1;
        buf.m.planes = &planes;
        if(ioctl(fd, VIDIOC_QUERYBUF, &buf) < 0)
        {
            LOGE("[native_camera] %s  VIDIOC_QUERYBUF failed! error=%s", __FUNCTION__, strerror(errno));
        }
        vbs[i].length = buf.m.planes->length;
        vbs[i].offset = buf.m.planes->m.mem_offset;
        vbs[i].addr= mmap(NULL, vbs[i].length, PROT_READ | PROT_WRITE, MAP_SHARED,
                          fd, vbs[i].offset );
        vbs[i].index = i;
        LOGD("%d,length:%d",i,buf.m.planes->length);
        v4L2QBufMap(i);
    }
}
void CV4L2Camera::v4L2DmaBuf()
{
    v4L2Reqbufs(memType, MAX_WINDOW_BUFFER);

    int ret;
    struct v4l2_plane planes;

    for (int i=0; i<MAX_WINDOW_BUFFER; i++)
    {
        CLEAR(buf);
        buf.type   = bufType;
        buf.memory = memType;
        buf.index  = i;
        buf.m.planes = &planes;
        buf.length = 1;
        memset(&planes, 0, sizeof(planes));
        if(ioctl(fd, VIDIOC_QUERYBUF, &buf) < 0){
            LOGE("[native_camera] VIDIOC_QUERYBUF: %s", strerror(errno));
            return;
        }
        ret = windowEx.dequeueBuffer(i);
        if(ret < 0){
            LOGE("dequeueBuffer returned: %d ", ret);
            return ;
        }

        buf.m.planes->m.fd = ret;
        //LOGD("fd:%d,len:%u",ret, buf.m.planes->length);
        ret = ioctl(fd, VIDIOC_QBUF, &buf);
        if ( ret< 0) {
            LOGE("VIDIOC_QBUF returned: %d (%s),%d", ret, strerror(errno),errno);
            return ;
        }
    }

}
void CV4L2Camera::v4l2Query()
{
    int ret(0);
    struct v4l2_capability cap;

    ret = ioctl(fd, VIDIOC_QUERYCAP, &cap);

    if (ret < 0) {
        LOGE("VIDIOC_QUERYCAP returned: %d (%s)", ret, strerror(errno));
        return ;
    }

    LOGI("driver:       '%s'", cap.driver);
    LOGI("card:         '%s'", cap.card);
    LOGI("bus_info:     '%s'", cap.bus_info);
    LOGI("version:      %x", cap.version);
    LOGI("capabilities: %x", cap.capabilities);
    LOGI("device caps:  %x", cap.device_caps);
    int mBufType;
    if (cap.capabilities & V4L2_CAP_VIDEO_CAPTURE){
        mBufType = V4L2_BUF_TYPE_VIDEO_CAPTURE;
    }
    else if (cap.capabilities & V4L2_CAP_VIDEO_CAPTURE_MPLANE){
        mBufType = V4L2_BUF_TYPE_VIDEO_CAPTURE_MPLANE;
    }else if (cap.capabilities & V4L2_CAP_VIDEO_OUTPUT){
        mBufType = V4L2_BUF_TYPE_VIDEO_OUTPUT;
    }else if (cap.capabilities & V4L2_CAP_VIDEO_OUTPUT_MPLANE){
        mBufType = V4L2_BUF_TYPE_VIDEO_OUTPUT_MPLANE;
    }else if (cap.capabilities & V4L2_CAP_META_CAPTURE){
        mBufType = V4L2_BUF_TYPE_META_CAPTURE;
    }else{
    }

    LOGI("mBufType: %x",mBufType);

    v4l2_fmtdesc formatDescriptions;
    formatDescriptions.type = mBufType;
    {
        for (int i=0; true; i++) {
            formatDescriptions.index = i;
            // query supported format
            if (ioctl(fd, VIDIOC_ENUM_FMT, &formatDescriptions) == 0) {
                LOGI("V4l2Capture::%s, %2d: %s 0x%08X 0x%X",__func__,i,formatDescriptions.description,formatDescriptions.pixelformat,formatDescriptions.flags);
            }else {
                // No more formats available
                break;
            }
        }

        // Verify we can use this device for video capture
        if (!(cap.capabilities & V4L2_CAP_VIDEO_CAPTURE_MPLANE) ||
            !(cap.capabilities & V4L2_CAP_STREAMING)) {
            // Can't do streaming capture.
            LOGE("V4l2Capture::%s,Streaming capture not supported by",__func__);

        }
    }

    return ;
}



bool CV4L2Camera::init(int width,int height,int memType) {

    bufType = V4L2_BUF_TYPE_VIDEO_CAPTURE_MPLANE;
    this->memType = memType;
    v4l2Query();
    v4L2SFormat(width, height);
    if(memType == V4L2_MEMORY_DMABUF){
        v4L2DmaBuf();
    }else if(memType == V4L2_MEMORY_MMAP){
        v4L2MemMap();
    }
    v4l2StreamOn();
    return true;
}

bool CV4L2Camera::isRun() {

    return memType ==V4L2_MEMORY_DMABUF ? iState == 1 : iState > 0;
}
