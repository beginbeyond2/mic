//
// Created by zhuzh on 2023/3/20.
//

#ifndef HDO_XDMADEVICE_H
#define HDO_XDMADEVICE_H

#define XDMA_BUFFER_MAX (3)
#include <atomic>
class XDmaDevice {
public:
    XDmaDevice(int idx);
    bool Open(int dfd,int efd);
    void Close();
    void Recv();
    bool isRun();

    unsigned char * getBuffer();

    unsigned int getLength();
    static XDmaDevice * getInstance(int idx);
private:
    volatile int dfd;
    volatile int efd;
    volatile int w_idx;
    volatile int r_idx;
    unsigned char *c2h_align_mem[XDMA_BUFFER_MAX];
    pthread_cond_t cond;
    pthread_mutex_t lock;
    pthread_t pid;
//    volatile int state;
    std::atomic_int state;
    volatile int idx;
};


#endif //HDO_XDMADEVICE_H
