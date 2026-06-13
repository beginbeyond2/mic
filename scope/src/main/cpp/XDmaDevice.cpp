//
// Created by zhuzh on 2023/3/20.
//

#include <unistd.h>
#include <cstdlib>
#include "XDmaDevice.h"
#include "Logger.h"
#include <poll.h>
#include <pthread.h>
#include <errno.h>
#include <string.h>
#define MEM_SIZE (844 * 4096UL)
#define TAG ("XDmaDevice")
static XDmaDevice * sXDma[] = {NULL,NULL};
XDmaDevice * XDmaDevice::getInstance(int idx){
    if(sXDma[idx] == NULL){
        sXDma[idx] = new XDmaDevice(idx);
        if(idx == 0){
            cpu_set_t  mask;
            CPU_ZERO(&mask);
            for(int i=0;i<4;i++){
                CPU_SET(i,&mask);
            }
            sched_setaffinity(0,sizeof(mask),&mask);
        }
    }
    return sXDma[idx];
}


static void *pthread_xdma_recv(void* args) {
    XDmaDevice* dev = static_cast<XDmaDevice*>(args);
    if(dev != NULL){
        dev->Recv();
    }
    return 0;
}

XDmaDevice::XDmaDevice(int idx)
{
    this->idx = idx;
    int ret = 0;
    unsigned char * ptr = NULL;
    for(int i=0;i<XDMA_BUFFER_MAX;i++){
        posix_memalign((void **)&ptr,4096,MEM_SIZE + 4096);
        if(ptr != NULL){
            c2h_align_mem[i] = ptr;
        }
    }
    this->state = 0;
    this->efd = -1;
    this->dfd = -1;
    pthread_mutex_init(&lock, NULL);
    pthread_cond_init(&cond,NULL);
}
bool XDmaDevice::Open(int dfd,int efd)
{
    this->w_idx = 0;
    this->r_idx = 0;
    this->dfd = dfd;
    this->efd = efd;
    this->state = 1;
    pthread_create(&pid, 0, pthread_xdma_recv, this);
   return true;
//OPEN_ERROR:
//
//    for(int i=0;i<XDMA_BUFFER_MAX;i++){
//        if(c2h_align_mem[i] != NULL){
//            free(c2h_align_mem[i]);
//            c2h_align_mem[i] = NULL;
//        }
//    }
//    return false;
}

bool XDmaDevice::isRun()
{
    return efd > 0 && dfd > 0 && state == 1;
}
static double GetTickCount(){
    timeval tv;
    gettimeofday(&tv, NULL);
    return tv.tv_sec * 1000000.0 + tv.tv_usec;
}
void XDmaDevice::Recv()
{
    int val = 0;
    int ret = 0;
    struct pollfd poll_fd;
    double  t,max = 0,sum = 0;
    int e1,e2,nn = 0;
    LOGD("%s.%d",__FUNCTION__ ,__LINE__);


    while (isRun()){
        poll_fd.fd = efd;
        poll_fd.events = POLLIN;
        ret = poll(&poll_fd, 1, 100);
        if(ret < 0 && errno != EINTR){
            LOGE("%s.%d,%d %s",__FUNCTION__ ,__LINE__,errno,strerror(errno));
            break;
        }
        if(ret == 0){
            continue;
        }
        if(poll_fd.revents==POLLIN){
            ret = read(efd,&val,4);
            if(ret > 0){
                e1 = ret;
//                LOGD("efd:%d,ret:%d",efd,ret);

                int c = 0;
                //do
                {
                    if(isRun()){
                        t = GetTickCount();
                        ret = read(dfd,c2h_align_mem[w_idx],MEM_SIZE);
                        if(ret == MEM_SIZE){
                            pthread_mutex_lock(&lock);
                            r_idx = w_idx;
                            w_idx++;
                            if(w_idx >= XDMA_BUFFER_MAX){
                                w_idx = 0;
                            }
                            pthread_cond_signal(&cond);
                            pthread_mutex_unlock(&lock);
                            c = 3;
                            e2 = e1;
                            t = GetTickCount() - t;
                            if(max < t){
                                max = t;
                            }
                            sum += t;
                            nn++;
                            if(nn >= 100){
//                                LOGE("idx:%d,max:%g,mean:%g\n",idx,max/1000,sum/nn/1000);
                                nn = 0;
                                sum = 0;
                                max = 0;
                            }
                        }else{
//                            LOGE("dfd : %d,err: %d,%s,%g,(%d,%d),%g\n",ret,errno,strerror(errno),(GetTickCount() - t)/1000,e1,e2,max/1000);
                            c++;
                        }
                        lseek(dfd,0,SEEK_SET);
                    }else{
                        break;
                    }
                }
                //while (c < 3);
            }else{
                LOGE("efd : %d\n",ret);
            }
        }
    }
    LOGD("%s.%d",__FUNCTION__ ,__LINE__);
    pthread_cond_signal(&cond);
    state = 3;
}


unsigned char * XDmaDevice::getBuffer()
{
    uint8_t  * ptr = NULL;
    if(isRun()){
        uint32_t idx;
        pthread_mutex_lock(&lock);
        if(r_idx == w_idx){
            pthread_cond_wait(&cond,&lock);
        }
        if(r_idx != w_idx){
            idx = r_idx;
            r_idx++;
            if(r_idx >= XDMA_BUFFER_MAX){
                r_idx = 0;
            }
            ptr = c2h_align_mem[idx];
        }
        pthread_mutex_unlock(&lock);
    }
    return ptr;
}
unsigned int XDmaDevice::getLength()
{
    return MEM_SIZE;
}
void XDmaDevice::Close() {
    LOGD("%s.%d,state:%d",__FUNCTION__ ,__LINE__,state.load());
    if(isRun()){
        state = 2;
        while (state == 2 ){
            usleep(100);
        }
    }

    usleep(100);
    pthread_mutex_lock(&lock);
//    for (int i = 0; i < XDMA_BUFFER_MAX; i++)
//    {
//        if (c2h_align_mem[i] != NULL) {
//            free(c2h_align_mem[i]);
//            c2h_align_mem[i] = NULL;
//        }
//    }
//    if (efd > 0) {
//        close(efd);
//        efd = -1;
//    }
//    if (dfd > 0) {
//        close(dfd);
//        dfd = -1;
//    }
    pthread_mutex_unlock(&lock);
    //pthread_mutex_destroy(&lock);
    //pthread_cond_destroy(&cond);
    LOGD("%s.%d",__FUNCTION__ ,__LINE__);
}