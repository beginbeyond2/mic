//
// Created by zhuzh on 2024/1/30.
//

#ifndef ETO_ANATIVEWINDOWEX_H
#define ETO_ANATIVEWINDOWEX_H

#include <android/native_window.h>

struct ANativeWindowBuffer;
typedef struct ANativeWindowBuffer ANativeWindowBuffer;
typedef int (*ANativeWindow_dequeueBufferEx)(ANativeWindow* window, ANativeWindowBuffer ** buffer, int* fenceFd,int *fd) ;


typedef int (*ANativeWindow_queueBuffer)(ANativeWindow* window, ANativeWindowBuffer* buffer, int fenceFd);

typedef int (*ANativeWindow_cancelBuffer)(ANativeWindow* window, ANativeWindowBuffer* buffer, int fenceFd);
typedef struct {
    ANativeWindowBuffer * buffer;
    int fencefd;
    int fd;
}ANativeWindowBufferEx;
#define MAX_WINDOW_BUFFER (6)
class ANativeWindowEx {
public:
    ANativeWindowEx(ANativeWindow * window);
    int dequeueBuffer(int index);
    int queueBuffer(int index);
    int cancelBuffer(int index);
private:
    ANativeWindowBufferEx bufferEx[MAX_WINDOW_BUFFER];
    ANativeWindow * window;
    static void * handle;
    static ANativeWindow_dequeueBufferEx dequeueBufferFun;
    static ANativeWindow_queueBuffer queueBufferFun;
    static ANativeWindow_cancelBuffer cancelBufferFun;
};


#endif //ETO_ANATIVEWINDOWEX_H
