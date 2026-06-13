#include <jni.h>
#include <cstdlib>
#include "../wavedata.h"
// 以下三个头文件是实现零复制使用的
#include "GL/nativebase.h"
#include "GL/window.h"
#include "GL/cutils/native_handle.h"

//#include <pthread.h>
#include "OpenCLCommon.h"
#include <sys/mman.h>
#include <errno.h>

#define mLOGD if(1)LOGD


extern "C"
JNIEXPORT void JNICALL
Java_com_micsig_tbook_scope_math_mathEx_MathExt_initMathExprOpenCL(JNIEnv *env, jclass clazz,
                                                                   jobject asset_manager) {
    // TODO: implement initMathExprOpenCL()
    OpenCLCommon::GetIns()->init(env,asset_manager);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_micsig_tbook_scope_math_mathEx_MathExt_IntToHex(JNIEnv *env, jclass clazz, jobject src,
                                                         jobject desFD,
                                                         jint place_val,jint headLen,jint waveLen) {
    // TODO: implement IntToHex()
    const int FORMAT_HEAD=11;
    int* inSrc=(int*)(*env).GetDirectBufferAddress(src);
    char* outDes=(char*)(*env).GetDirectBufferAddress(desFD);

    long begin= OpenCLCommon::getCurrentTime();
     OpenCLCommon::GetIns()->intToHex(inSrc,outDes,waveLen,place_val,headLen);
    mLOGD("compute time:%d",(OpenCLCommon::getCurrentTime()-begin));

}
extern "C"
JNIEXPORT void JNICALL
Java_com_micsig_tbook_scope_math_mathEx_MathExt_DoubleToASCII(JNIEnv *env, jclass clazz,
                                                              jobject src,
                                                              jobject  des,
                                                              jdouble vv,jint headLen,jint waveLen) {
    // TODO: implement DoubleToASCII()
    int* inSrc=(int*)(*env).GetDirectBufferAddress(src);
    char* outDes=(char*)(*env).GetDirectBufferAddress(des);
    OpenCLCommon::GetIns()->doubleToAscii(inSrc,outDes,waveLen,vv,headLen);

}
