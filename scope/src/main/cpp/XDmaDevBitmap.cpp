//
// Created by zhuzh on 2024/1/3.
//


#include <unistd.h>
#include <cstdlib>
#include "Logger.h"
#include <poll.h>
#include <pthread.h>
#include <sys/ioctl.h>
#include "XDmaDevBitmap.h"
#include <errno.h>
#include <string.h>

struct xdma_fd_ioctl {
    int fd;
    uint32_t len;
    int error;
    uint32_t done;
};
#define IOCTL_XDMA_FD_R   		_IOW('q', 9, struct xdma_fd_ioctl *)

#define MEM_SIZE (1766*4096UL)
#define TAG ("XDmaDevBitmap")
static XDmaDevBitmap * sXDma[] = {NULL,NULL};
XDmaDevBitmap * XDmaDevBitmap::getInstance(int idx){
    if(sXDma[idx] == NULL){
        sXDma[idx] = new XDmaDevBitmap();
        sXDma[idx]->setIdx(idx);
    }
    return sXDma[idx];
}

static void *pthread_xdma_recv(void* args) {
    XDmaDevBitmap* dev = static_cast<XDmaDevBitmap*>(args);
    if(dev != NULL){
        dev->Recv();
    }
    return 0;
}
void XDmaDevBitmap::setIdx(int idx){
    this->idx = idx;
}
XDmaDevBitmap::XDmaDevBitmap()
{
    int ret = 0;
    unsigned char * ptr = NULL;
    this->state = 0;
    this->efd = -1;
    this->dfd = -1;
    this->bclear = true;
    pthread_mutex_init(&lock, NULL);
    pthread_cond_init(&cond,NULL);
}
bool XDmaDevBitmap::Open(int dfd,int efd,ANativeWindow* window)
{
    LOGD("%s.%d,dfd:%d,efd:%d",__FUNCTION__ ,__LINE__,dfd,efd);
    this->w_idx = 0;
    this->r_idx = 0;
    this->dfd = dfd;
    this->efd = efd;
    this->state = 1;
    this->window = window;
    pthread_create(&pid, 0, pthread_xdma_recv, this);
    return true;

}

bool XDmaDevBitmap::isRun()
{
    return efd > 0 && dfd > 0 && state == 1;
}
static double GetTickCount(){
    timeval tv;
    gettimeofday(&tv, NULL);
    return tv.tv_sec * 1000000.0 + tv.tv_usec;
}
void XDmaDevBitmap::Recv()
{
    int val = 0;
    int ret = 0;
    struct pollfd poll_fd;
    ANativeWindow_Buffer buffer;
    double t = GetTickCount();
    int frameNums = 0;
    LOGD("%s.%d",__FUNCTION__ ,__LINE__);
    int vv = 50;
    while (isRun()){
        bool expected = true;
        if(bclear.compare_exchange_strong(expected,false)){
            buffer.reserved[0] = 0x20240110;
            if(ANativeWindow_lock(window,&buffer,0)==0) {
                memset(buffer.bits, 0, (size_t)(buffer.stride * buffer.height * sizeof (int32_t)));
                ANativeWindow_unlockAndPost(window);
            }
        }

        poll_fd.fd = efd;
        poll_fd.events = POLLIN;
        ret = poll(&poll_fd, 1, 100);
        if(ret < 0 && errno != EINTR){
            LOGE("%s.%d,%d %s\n",__FUNCTION__ ,__LINE__,errno,strerror(errno));
            break;
        }
        //LOGD("MM-----------------------------------------ret=%d",ret);
        if(ret == 0){
            continue;
        }
        if(poll_fd.revents==POLLIN){
         //   LOGD("MM-----------------------------------------");
            ret = read(efd,&val,4);
            if(ret > 0){
                if(isRun()){
                    buffer.reserved[0] = 0x20240110;
                    if(ANativeWindow_lock(window,&buffer,0)==0) {
                        if (buffer.reserved[0] != 0x20240110 && buffer.bits != NULL) {
                            int32_t *p = (int32_t *) (buffer.reserved);
                            int fd = p[1];
                            struct xdma_fd_ioctl io;
                            io.fd = fd;
                            io.error = 0;
                            io.done = 0;


                            int32_t rc = ioctl(dfd, IOCTL_XDMA_FD_R, &io);
                            if (rc < 0 || io.error) {
                                LOGE("idx:%d.%s.%d,rc:%d,io.error:%d\n", idx, __FUNCTION__, __LINE__,
                                     rc, io.error);
                            } else {
                                //LOGE("%s.%d,rc:%d,io.done:%d,0x%x",__FUNCTION__ ,__LINE__,rc,io.done,*((uint32_t *)buffer.bits));

                                frameNums++;
                                if (frameNums >= 400) {
                                    LOGD("bmp FrameNums: %f/s",
                                         frameNums * 1e6 / (GetTickCount() - t));
                                    t = GetTickCount();
                                    frameNums = 0;
                                }
                            }
                        }else{
                            LOGE("%s,%d\n",__FUNCTION__ ,__LINE__);
                        }
                        ANativeWindow_unlockAndPost(window);
                    }else{
                        LOGE("%s,%d\n",__FUNCTION__ ,__LINE__);
                    }
                }else{
                    LOGE("%s,%d\n",__FUNCTION__ ,__LINE__);
                }
            }else{
                LOGE("%s,%d,ret = %d\n",__FUNCTION__ ,__LINE__,ret);
            }
        }
    }
    LOGD("%s.%d Exit",__FUNCTION__ ,__LINE__);
    pthread_cond_signal(&cond);
    state = 3;

}

void XDmaDevBitmap::Close() {
    LOGD("%s.%d,state:%d",__FUNCTION__ ,__LINE__,state.load());
    if(isRun()){
        state = 2;
        while (state == 2){
            usleep(100);
        }
    }

    usleep(100);
    pthread_mutex_lock(&lock);
//    if (efd > 0) {
//        close(efd);
//        efd = -1;
//    }
//    if (dfd > 0) {
//        close(dfd);
//        dfd = -1;
//    }
    pthread_mutex_unlock(&lock);
    LOGD("%s.%d",__FUNCTION__ ,__LINE__);
}

void XDmaDevBitmap::Clear(){
   bclear.exchange(true);
}

