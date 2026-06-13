//
// Created by zhuzh on 2018-5-16.
//
#include <jni.h>
#ifndef TBOOKSCOPE_SURFACE_H
#define TBOOKSCOPE_SURFACE_H

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT void JNICALL Java_com_micsig_tbook_scope_surface_SurfaceNative_acquire
        (JNIEnv *, jobject, jobject,jint,jint);
JNIEXPORT void JNICALL Java_com_micsig_tbook_scope_surface_SurfaceNative_release
        (JNIEnv *, jobject);
JNIEXPORT void JNICALL Java_com_micsig_tbook_scope_surface_SurfaceNative_clear
        (JNIEnv *, jobject);

JNIEXPORT void JNICALL Java_com_micsig_tbook_scope_surface_SurfaceNative_draw
        (JNIEnv *, jobject, jobject, jobject);

JNIEXPORT void JNICALL Java_com_micsig_tbook_scope_surface_SurfaceNative_drawXY
        (JNIEnv *, jobject, jobject,jobject,jobject);
JNIEXPORT void JNICALL Java_com_micsig_tbook_scope_surface_SurfaceNative_drawBitmap
        (JNIEnv *env, jobject obj,jobject ch,jint offset,jint length);

#ifdef __cplusplus
}
#endif
#endif //TBOOKSCOPE_SURFACE_H
