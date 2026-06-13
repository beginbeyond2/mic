//
// Created by zhuzh on 2024/1/3.
//

#ifndef ETO_XDMADEVBITMAP_H
#define ETO_XDMADEVBITMAP_H

#include <atomic>
#include <android/native_window_jni.h>

class XDmaDevBitmap {
public:
    XDmaDevBitmap();
    bool Open(int dfd,int efd,ANativeWindow * window);
    void Close();
    void Recv();
    bool isRun();
    void Clear();

    static XDmaDevBitmap * getInstance(int idx);
    void setIdx(int idx);
private:
    volatile int dfd;
    volatile int efd;
    volatile int w_idx;
    volatile int r_idx;

    pthread_cond_t cond;
    pthread_mutex_t lock;
    pthread_t pid;
//    volatile int state;
    std::atomic_int state;
    std::atomic_bool bclear;
    ANativeWindow* window;
    int idx;
};


#endif //ETO_XDMADEVBITMAP_H
