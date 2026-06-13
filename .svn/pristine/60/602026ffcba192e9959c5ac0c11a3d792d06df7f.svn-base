//
// Created by zhuzh on 2018-4-12.
//

#ifndef TBOOKSCOPE_MATH_H
#define TBOOKSCOPE_MATH_H
#include <jni.h>
#ifdef __cplusplus
extern "C" {
#endif
JNIEXPORT jobject JNICALL Java_com_micsig_tbook_scope_math_MathNative_isExprValid
        (JNIEnv *, jobject, jstring);
JNIEXPORT jboolean JNICALL Java_com_micsig_tbook_scope_math_MathNative_setExpr
        (JNIEnv *, jobject, jint ,jstring);
JNIEXPORT jdouble JNICALL Java_com_micsig_tbook_scope_math_MathNative_calcExpr
        (JNIEnv *, jobject, jint ,jobject,jobjectArray ,jdouble,jdouble,jdouble);
JNIEXPORT jboolean JNICALL Java_com_micsig_tbook_scope_math_MathNative_add
        (JNIEnv *, jobject, jobject,jobject,jobject);
JNIEXPORT jboolean JNICALL Java_com_micsig_tbook_scope_math_MathNative_sub
        (JNIEnv *, jobject, jobject,jobject,jobject);
JNIEXPORT jboolean JNICALL Java_com_micsig_tbook_scope_math_MathNative_mul
        (JNIEnv *, jobject, jobject,jobject,jobject);
JNIEXPORT jboolean JNICALL Java_com_micsig_tbook_scope_math_MathNative_div
        (JNIEnv *, jobject, jobject,jobject,jobject);

JNIEXPORT jboolean JNICALL Java_com_micsig_tbook_scope_math_MathNative_fft
        (JNIEnv *, jobject,jint,jint,jint, jobject,jobject);
JNIEXPORT jint JNICALL Java_com_micsig_tbook_scope_math_MathNative_CalFFTPointNum
        (JNIEnv *, jobject, jint);

JNIEXPORT jdouble JNICALL Java_com_micsig_tbook_scope_math_MathNative_fftDcVal
        (JNIEnv *, jobject,jint);
JNIEXPORT jdouble JNICALL Java_com_micsig_tbook_scope_math_MathNative_fftMaxVal
        (JNIEnv *, jobject,jint);
JNIEXPORT jint JNICALL Java_com_micsig_tbook_scope_math_MathNative_fftMaxValIdx
        (JNIEnv *, jobject,jint);
JNIEXPORT void JNICALL Java_com_micsig_tbook_scope_math_MathNative_csetVAd
        (JNIEnv *, jobject ,jint, jdouble);

JNIEXPORT jlong JNICALL Java_com_micsig_tbook_scope_math_MathNative_sum
        (JNIEnv *, jobject, jobject);
JNIEXPORT jlong JNICALL Java_com_micsig_tbook_scope_math_MathNative_sum1
        (JNIEnv *, jobject, jobject, jint, jint);
JNIEXPORT jlong JNICALL Java_com_micsig_tbook_scope_math_MathNative_sum2
        (JNIEnv *, jobject, jobject, jint, jint, jint);
JNIEXPORT jint JNICALL Java_com_micsig_tbook_scope_math_MathNative_average
        (JNIEnv *, jobject, jobject);
JNIEXPORT jint JNICALL Java_com_micsig_tbook_scope_math_MathNative_max
        (JNIEnv *, jobject, jobject);
JNIEXPORT jint JNICALL Java_com_micsig_tbook_scope_math_MathNative_max1
        (JNIEnv *, jobject, jobject, jint, jint);
JNIEXPORT jint JNICALL Java_com_micsig_tbook_scope_math_MathNative_min
        (JNIEnv *, jobject, jobject);
JNIEXPORT jint JNICALL Java_com_micsig_tbook_scope_math_MathNative_min1
        (JNIEnv *, jobject, jobject, jint, jint);

JNIEXPORT jintArray JNICALL Java_com_micsig_tbook_scope_math_MathNative_ByteBufferToIntArray(JNIEnv *, jobject, jobject);



#ifdef __cplusplus
}
#endif
#endif //TBOOKSCOPE_MATH_H
