//
// Created by zhuzh on 2018-7-5.
//

#ifndef TBOOKSCOPE_UTILS_H
#define TBOOKSCOPE_UTILS_H
#include <jni.h>
#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jshort JNICALL Java_com_micsig_base_Utils_crc16
(JNIEnv *, jobject, jbyteArray,jint,jint);
JNIEXPORT void JNICALL
Java_com_micsig_base_Utils_initSignal(JNIEnv *env, jclass clazz);

#ifdef __cplusplus
}
#endif
#endif //TBOOKSCOPE_UTILS_H
