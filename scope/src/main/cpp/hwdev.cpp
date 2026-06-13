//
// Created by zhuzh on 2021/12/2.
//
#include "Logger.h"


#include <jni.h>
#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>
#include "CV4L2Camera.h"
#include "XDmaDevice.h"
#include "XDmaDevBitmap.h"
#include "XDmaDevUser.h"

#define DEVICE_NUMS (2)
static CV4L2Camera* gV4L2[DEVICE_NUMS]={NULL, NULL};
static XDmaDevice* gXDma[DEVICE_NUMS] = {NULL,NULL};



#define TAG ("HWDEV")
#define DEVICE_NATIVE_JNI_ID "mDevice"

static jfieldID  field_context = NULL;
static jfieldID  getFieldContext(JNIEnv* env, jobject thiz ){
    if(field_context == NULL){
        jclass  clazz = env->GetObjectClass(thiz);
        field_context = env->GetFieldID(clazz,DEVICE_NATIVE_JNI_ID, "J");
    }
    return field_context;
}

static void setDeviceFd(JNIEnv* env, jobject thiz ,jlong fd){

    jfieldID id = getFieldContext(env,thiz);
    env->SetLongField(thiz,id,fd);
}

static int getDeviceFd(JNIEnv* env, jobject thiz){
    jfieldID id = getFieldContext(env,thiz);
    return (int) env->GetLongField(thiz,id);
}
static int jniGetFDFromFileDescriptor(JNIEnv * env, jobject fileDescriptor) {
    jint fd = -1;
    jclass fdClass = env->FindClass("java/io/FileDescriptor");
    if (fdClass != NULL) {
        jfieldID fdClassDescriptorFieldID = env->GetFieldID(fdClass, "descriptor", "I");
        if (fdClassDescriptorFieldID != NULL && fileDescriptor != NULL) {
            fd = env->GetIntField(fileDescriptor, fdClassDescriptorFieldID);
        }
    }
    LOGD("jni Get FD From File Descriptor %d",fd);
    return fd;
}
ANativeWindow * getWindow(JNIEnv* env, jobject thiz);

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_micsig_tbook_scope_surface_MipiDevice_native_1open(JNIEnv *env, jobject thiz, jobject pfd, jobject pfd1,jobject jobj) {

    int fd = jniGetFDFromFileDescriptor(env, pfd);

    // duplicate the file descriptor, since ParcelFileDescriptor will eventually close its copy
    fd = fcntl(fd, F_DUPFD_CLOEXEC, 0);
    if (fd < 0) {

        return JNI_FALSE;
    }

    int fd1 = jniGetFDFromFileDescriptor(env, pfd1);

    // duplicate the file descriptor, since ParcelFileDescriptor will eventually close its copy
    fd1 = fcntl(fd1, F_DUPFD_CLOEXEC, 0);
    if (fd1 < 0) {

        return JNI_FALSE;
    }

    gV4L2[0] = new CV4L2Camera(fd, getWindow(env,jobj));
    gV4L2[1] = new CV4L2Camera(fd1, NULL);
    setDeviceFd(env,thiz,1);
    return JNI_TRUE;

}


extern "C"
JNIEXPORT void JNICALL
Java_com_micsig_tbook_scope_surface_MipiDevice_native_1close(JNIEnv *env, jobject thiz) {

    for(int i=0;i<2;i++){
        if(gV4L2[i] != NULL){
            gV4L2[i]->v4l2StreamOff();
            gV4L2[i]->v4l2Close();
            usleep(300);
            delete gV4L2[i];
            gV4L2[i] = NULL;
        }
    }
    setDeviceFd(env,thiz,-1);
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_micsig_tbook_scope_surface_MipiDevice_native_1lock(JNIEnv *env, jobject thiz) {
    jobject byteBuffer = NULL;
    int idx = getDeviceFd(env,thiz);
    if(idx == 1){
        CV4L2Camera * v4l2 = gV4L2[idx];
        if(v4l2 != NULL &&  v4l2->isRun()){
            idx = v4l2->v4L2DqbufMap();
            if(idx >= 0){
                VideoBuffer * vb = v4l2->getVideoBuffer(idx);
                if(vb){
                    byteBuffer = env->NewDirectByteBuffer(vb->addr, vb->length);
                    v4l2->index = idx;
                    return byteBuffer;
                }
            }
        }
    }
    return byteBuffer;
}
extern "C"
JNIEXPORT void JNICALL
Java_com_micsig_tbook_scope_surface_MipiDevice_native_1unlock(JNIEnv *env, jobject thiz) {
    int idx = getDeviceFd(env,thiz);
    if(idx == 1){
        CV4L2Camera * v4l2 = gV4L2[idx];
        if(v4l2 != NULL && v4l2->isRun()){
            v4l2->v4L2QBufMap(v4l2->index);
        }
    }

}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_micsig_tbook_scope_surface_XDmaDevice_native_1open(JNIEnv *env, jobject thiz,
                                                            jint idx,
                                                            jobject jdfd,
                                                            jobject jefd) {

    int dfd = jniGetFDFromFileDescriptor(env, jdfd);
    int efd = jniGetFDFromFileDescriptor(env, jefd);
    // duplicate the file descriptor, since ParcelFileDescriptor will eventually close its copy
//        dfd = fcntl(dfd, F_DUPFD_CLOEXEC, 0);
//        efd = fcntl(efd,F_DUPFD_CLOEXEC, 0);
    if (dfd < 0 || efd < 0) {
        if(dfd > 0){
            close(dfd);
        }
        if(efd > 0){
            close(efd);
        }
        return JNI_FALSE;
    }

    XDmaDevice * dev = XDmaDevice::getInstance(idx);
    if(dev != NULL){
        if(dev->Open(dfd, efd)){
            return JNI_TRUE;
        }else{
            dev->Close();
        }
    }
    return JNI_FALSE;
}
extern "C"
JNIEXPORT void JNICALL
Java_com_micsig_tbook_scope_surface_XDmaDevice_native_1close(JNIEnv *env, jobject thiz,jint idx) {

    XDmaDevice * dev = XDmaDevice::getInstance(idx);
    if(dev != NULL){
        dev->Close();
    }

    XDmaDevBitmap * dev1 = XDmaDevBitmap::getInstance(idx);
    if(dev1 != NULL){
        dev1->Close();
    }

    XDmaDevUser * dev2 = XDmaDevUser::getInstance(idx);
    if(dev2 != NULL){
        dev2->Close();
    }

}

extern "C"
JNIEXPORT void JNICALL
Java_com_micsig_tbook_scope_surface_XDmaDevice_native_1clear(JNIEnv *env, jobject thiz,jint idx) {

    XDmaDevBitmap * dev1 = XDmaDevBitmap::getInstance(idx);
    if(dev1 != NULL){
        dev1->Clear();
    }

}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_micsig_tbook_scope_surface_XDmaDevice_native_1lock(JNIEnv *env, jobject thiz,
                                                            jint idx) {

    jobject byteBuffer = NULL;
    XDmaDevice * dev = XDmaDevice::getInstance(idx);
    if(dev != NULL){
        unsigned char * buf = dev->getBuffer();
        if(buf != NULL){
            byteBuffer = env->NewDirectByteBuffer(buf, dev->getLength());
        }
    }
    return byteBuffer;
}
extern "C"
JNIEXPORT jobject JNICALL
Java_com_micsig_tbook_scope_surface_XDmaDevice_native_1userbuffer(JNIEnv *env, jobject thiz,
                                                            jint idx) {

    XDmaDevUser * dev = XDmaDevUser::getInstance(idx);
    if(dev != NULL){
        return dev->getByteBuffer();
    }
    return NULL;
}
extern "C"
JNIEXPORT void JNICALL
Java_com_micsig_tbook_scope_surface_XDmaDevice_native_1unlock(JNIEnv *env, jobject thiz,jint idx) {


}
ANativeWindow * getWindow(JNIEnv* env, jobject thiz);
extern "C"
JNIEXPORT jboolean JNICALL
Java_com_micsig_tbook_scope_surface_XDmaDevice_native_1openex(JNIEnv *env, jobject thiz,
                                                            jint idx,
                                                            jobject jdfd,
                                                            jobject jefd,
                                                            jobject jobj) {

    int dfd = jniGetFDFromFileDescriptor(env, jdfd);
    int efd = jniGetFDFromFileDescriptor(env, jefd);
    ANativeWindow* window = getWindow(env,jobj);

    if (dfd < 0 || efd < 0 || window == NULL) {
        if(dfd > 0){
            close(dfd);
        }
        if(efd > 0){
            close(efd);
        }
        return JNI_FALSE;
    }
    LOGD("dfd:%d,efd:%d,idx:%d,%p",dfd,efd,idx,window);
    XDmaDevBitmap * dev = XDmaDevBitmap::getInstance(idx);
    if(dev != NULL){
        if(dev->Open(dfd, efd,window)){
            return JNI_TRUE;
        }else{
            dev->Close();
        }
    }
    return JNI_FALSE;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_micsig_tbook_scope_surface_XDmaDevice_native_1openuser(JNIEnv *env, jobject thiz,
                                                              jint idx,
                                                              jobject jbarfd,
                                                                jobject jc2hfd,
                                                                jobject jh2cfd) {

    int barfd = jniGetFDFromFileDescriptor(env, jbarfd);
    int c2hfd = jniGetFDFromFileDescriptor(env, jc2hfd);
    int h2cfd = jniGetFDFromFileDescriptor(env, jh2cfd);
    if (barfd < 0 || c2hfd < 0 || h2cfd < 0 ) {
        return JNI_FALSE;
    }
    XDmaDevUser * dev = XDmaDevUser::getInstance(idx);
    if(dev != NULL){
        if(dev->Open( env,barfd,c2hfd,h2cfd)){
            return JNI_TRUE;
        }else{
            dev->Close();
        }
    }
    return JNI_FALSE;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_micsig_tbook_scope_surface_XDmaDevice_native_1store(JNIEnv *env, jobject thiz,
                                                                jint idx,
                                                                jobject jfd) {

    int fd = jniGetFDFromFileDescriptor(env, jfd);
    if (fd < 0  ) {
        return JNI_FALSE;
    }
    XDmaDevUser * dev = XDmaDevUser::getInstance(idx);
    if(dev != NULL){
        if(dev->store(fd)){
            return JNI_TRUE;
        }
    }
    return JNI_FALSE;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_micsig_tbook_scope_surface_XDmaDevice_native_1restore(JNIEnv *env, jobject thiz,
                                                             jint idx,
                                                             jobject jfd) {

    int fd = jniGetFDFromFileDescriptor(env, jfd);
    if (fd < 0  ) {
        return JNI_FALSE;
    }
    XDmaDevUser * dev = XDmaDevUser::getInstance(idx);
    if(dev != NULL){
        if(dev->restore(fd)){
            return JNI_TRUE;
        }
    }
    return JNI_FALSE;
}




extern "C"
JNIEXPORT jint JNICALL
Java_com_micsig_tbook_scope_surface_XDmaDevice_native_1progress(JNIEnv *env, jobject thiz,
                                                               jint idx) {
    XDmaDevUser * dev = XDmaDevUser::getInstance(idx);
    if(dev != NULL){
        return dev->getProgress();
    }
    return -1;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_micsig_tbook_scope_surface_XDmaDevice_native_1stop(JNIEnv *env, jobject thiz,
                                                                jint idx) {
    XDmaDevUser * dev = XDmaDevUser::getInstance(idx);
    if(dev != NULL){
        dev->stop();
    }
}