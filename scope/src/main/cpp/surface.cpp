//
// Created by zhuzh on 2018-5-16.
//
#include "surface.h"
#include "android/native_window_jni.h"
#include "android/native_window.h"
#include "wavedata.h"
#include "Wave2Bmp.h"
#include "Wave2XYBmp.h"
#include <memory.h>
#include <dlfcn.h>
#include <linux/time.h>
#include <sys/time.h>
#include "Logger.h"
#define TAG ("Surface")


#define SURFACENATIVE_JNI_ID "mWindow"

static jfieldID  aNativeWindow = NULL;
static jfieldID  getWindowFieldId(JNIEnv* env, jobject thiz ){
   if(aNativeWindow == NULL){
        jclass  clazz = env->GetObjectClass(thiz);
        aNativeWindow = env->GetFieldID(clazz,SURFACENATIVE_JNI_ID, "J");
    }
    return aNativeWindow;
}

static void setWindow(JNIEnv* env, jobject thiz ,jlong window){

    jfieldID id = getWindowFieldId(env,thiz);
    ANativeWindow * win =(ANativeWindow *) env->GetLongField(thiz,id);
    if(win){
        ANativeWindow_release(win);
    }
    env->SetLongField(thiz,id,window);
}

ANativeWindow * getWindow(JNIEnv* env, jobject thiz){
    jfieldID id = getWindowFieldId(env,thiz);
    return (ANativeWindow *) env->GetLongField(thiz,id);
}

JNIEXPORT void JNICALL Java_com_micsig_tbook_scope_surface_SurfaceNative_acquire
        (JNIEnv *env, jobject obj, jobject surface,jint width,jint height){
    ANativeWindow* window = ANativeWindow_fromSurface(env, surface);
    if(window){
        setWindow(env,obj,(jlong)window);
        ANativeWindow_setBuffersGeometry(window,width,height,WINDOW_FORMAT_RGBA_8888);
    }
}
JNIEXPORT void JNICALL Java_com_micsig_tbook_scope_surface_SurfaceNative_release
        (JNIEnv *env, jobject obj){
    ANativeWindow* window = getWindow(env,obj);
    if(window){
        setWindow(env,obj,0);
        ANativeWindow_release(window);
    }
}
JNIEXPORT void JNICALL Java_com_micsig_tbook_scope_surface_SurfaceNative_clear
    (JNIEnv *env, jobject obj){
    ANativeWindow* window = getWindow(env,obj);
    if(window){
        ANativeWindow_Buffer buffer;
        if(ANativeWindow_lock(window,&buffer,0)==0) {
            memset(buffer.bits, 0, (size_t)(buffer.stride * buffer.height * sizeof (int32_t)));
            ANativeWindow_unlockAndPost(window);
        }
    }
}

JNIEXPORT void JNICALL Java_com_micsig_tbook_scope_surface_SurfaceNative_draw
        (JNIEnv *env, jobject obj,jobject ch,jobject param){
    ANativeWindow* window = getWindow(env,obj);
    if(window){
        WAVE_T * pWave = (WAVE_T*)(env->GetDirectBufferAddress(ch));
        BMP_T * pBmp = (BMP_T*)(env->GetDirectBufferAddress(param));
        if(pWave && pBmp){
            XWaveData xWaveData(pWave);
            XBmp xBmp(pBmp);
            ANativeWindow_Buffer buffer;
            if(ANativeWindow_lock(window,&buffer,0)==0) {
                pBmp->buffer = (uint32_t *)buffer.bits;
                xBmp.setW(buffer.stride);
                Wave2Bmp::waveConvertBmp(&xBmp, &xWaveData);
                ANativeWindow_unlockAndPost(window);
            }
        }
    }
}
JNIEXPORT void JNICALL Java_com_micsig_tbook_scope_surface_SurfaceNative_drawXY
        (JNIEnv *env, jobject obj,jobject param,jobject chx,jobject chy){
    ANativeWindow* window = getWindow(env,obj);
    if(window){
        WAVE_T * xWave = (WAVE_T*)(env->GetDirectBufferAddress(chx));
        WAVE_T * yWave = (WAVE_T*)(env->GetDirectBufferAddress(chy));
        XYBMP_T * xyBmp = (XYBMP_T*)(env->GetDirectBufferAddress(param));
        if(xWave && xyBmp &&yWave ){
            XWaveData xWaveData(xWave);
            XWaveData yWaveData(yWave);
            XYBmp Bmp(xyBmp);

            ANativeWindow_Buffer buffer;
            if(ANativeWindow_lock(window,&buffer,0)==0) {
                xyBmp->buffer = (uint32_t *)buffer.bits;
                Wave2XYBmp::waveConvertXYBmp(&Bmp,&xWaveData,&yWaveData);
                ANativeWindow_unlockAndPost(window);
            }
        }
    }
}
extern "C" void *__memcpy_aarch64_simd (void *__restrict, const void *__restrict, size_t);



//static int gTestY = 0;
JNIEXPORT void JNICALL Java_com_micsig_tbook_scope_surface_SurfaceNative_drawBitmap
        (JNIEnv *env, jobject obj,jobject ch,jint offset,jint length){

    ANativeWindow* window = getWindow(env,obj);
    if(window){

        u_int8_t * pWave = (u_int8_t*)(env->GetDirectBufferAddress(ch));
        if(pWave){
            ANativeWindow_Buffer buffer;
            buffer.reserved[0] = 0x20240110;

            if(ANativeWindow_lock(window,&buffer,0)==0) {

                int32_t * data = (int32_t *) buffer.bits;
                u_int32_t len = buffer.width * sizeof(int32_t);
                pWave += offset;
                for(int y=0;y<buffer.height;y++){
                    __memcpy_aarch64_simd(data,pWave,len);
                    data += buffer.stride;
                    pWave += len;
                }

                ANativeWindow_unlockAndPost(window);
            }

        }
    }
}