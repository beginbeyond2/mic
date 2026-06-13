//
// Created by zhuzh on 2018-4-12.
//

#include <string.h>
#include "math.h"
#include "../wavedata.h"
#include "MathDualCalc.h"
#include "MathFFT.h"
#include "MathExpr.h"
#include "../Logger.h"
#define MATH_MAX (8)
#define  TAG "MATH"
static bool MathAdd(WAVE_T* pDst,WAVE_T* pSrc1,WAVE_T* pSrc2){
    XWaveData dst(pDst),src1(pSrc1),src2(pSrc2);
    dst.setWaveType(1);
    return MathDualCalc::Add(&dst,&src1,&src2);
}
static bool MathSub(WAVE_T* pDst,WAVE_T* pSrc1,WAVE_T* pSrc2){
    XWaveData dst(pDst),src1(pSrc1),src2(pSrc2);
    dst.setWaveType(1);
    return MathDualCalc::Sub(&dst,&src1,&src2);
}
static bool MathMul(WAVE_T* pDst,WAVE_T* pSrc1,WAVE_T* pSrc2){
    XWaveData dst(pDst),src1(pSrc1),src2(pSrc2);
    dst.setWaveType(1);
    return MathDualCalc::Mul(&dst,&src1,&src2);
}
static bool MathDiv(WAVE_T* pDst,WAVE_T* pSrc1,WAVE_T* pSrc2){
    XWaveData dst(pDst),src1(pSrc1),src2(pSrc2);
    dst.setWaveType(1);
    return MathDualCalc::Div(&dst,&src1,&src2);
}
static bool MathFFTCalc(int chIdx,int fftType,int fftWindow,WAVE_T* pDst,WAVE_T* pSrc){
    XWaveData dst(pDst),src(pSrc);
    dst.setWaveType(2);
    return MathFFT::CalcFFT(chIdx,fftType,fftWindow,&dst,&src);
}
static MathExpr * gMathExpr[MATH_MAX] = {nullptr,nullptr,nullptr,nullptr,
                                  nullptr,nullptr,nullptr,nullptr};
static bool setMathExpr(int chIdx,std::string expr_string)
{
    MathExpr * mathExpr = gMathExpr[chIdx];
    if(mathExpr != nullptr){
        if(expr_string.compare(mathExpr->getExpr()) != 0){
            delete mathExpr;
            gMathExpr[chIdx] =  mathExpr = nullptr;
        }
    }
    if(mathExpr == nullptr){
        mathExpr = gMathExpr[chIdx] = new MathExpr(expr_string);

    }
    return mathExpr->isExprValid();
}
static void CalcMathExpr(int chIdx,WAVE_T * pDst,const std::vector<WAVE_T*> &chVec ,double time,double var1,double var2)
{

    MathExpr * mathExpr = gMathExpr[chIdx];
    if(mathExpr != nullptr){
        XWaveData dst(pDst);
        dst.setWaveType(1);
        mathExpr->setVar(var1,var2);
        mathExpr->setTime(time);
        std::vector<XWaveData*> waveVec;
        for(auto v:chVec){
            waveVec.push_back(v != nullptr ? new XWaveData(v) : nullptr);
        }
        mathExpr->calcExpr(&dst,waveVec);
        for(auto v:waveVec){
            if(v != nullptr){
                delete v;
            }
        }
    }
}
static double getMathExprMaxVal(int chIdx){
    MathExpr * mathExpr = gMathExpr[chIdx];
    if(mathExpr != nullptr){
        return mathExpr->getMaxVal();
    }
    return 0;
}

JNIEXPORT jboolean JNICALL Java_com_micsig_tbook_scope_math_MathNative_add
        (JNIEnv *env, jobject thisz, jobject dst,jobject src1,jobject src2){

    if(MathAdd((WAVE_T*)env->GetDirectBufferAddress(dst),
            (WAVE_T*)env->GetDirectBufferAddress(src1),
            (WAVE_T*)env->GetDirectBufferAddress(src2)
    )){
        return JNI_TRUE;
    }
    return JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_micsig_tbook_scope_math_MathNative_sub
        (JNIEnv *env, jobject thisz, jobject dst,jobject src1,jobject src2){
    if(MathSub((WAVE_T*)env->GetDirectBufferAddress(dst),
            (WAVE_T*)env->GetDirectBufferAddress(src1),
            (WAVE_T*)env->GetDirectBufferAddress(src2)
    )){
        return JNI_TRUE;
    }
    return JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_micsig_tbook_scope_math_MathNative_mul
        (JNIEnv *env, jobject thisz, jobject dst,jobject src1,jobject src2){
    if(MathMul((WAVE_T*)env->GetDirectBufferAddress(dst),
            (WAVE_T*)env->GetDirectBufferAddress(src1),
            (WAVE_T*)env->GetDirectBufferAddress(src2)
    )){
        return JNI_TRUE;
    }
    return JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_micsig_tbook_scope_math_MathNative_div
        (JNIEnv *env, jobject thisz, jobject dst,jobject src1,jobject src2){
    if(MathDiv((WAVE_T*)env->GetDirectBufferAddress(dst),
            (WAVE_T*)env->GetDirectBufferAddress(src1),
            (WAVE_T*)env->GetDirectBufferAddress(src2)
    )){
        return JNI_TRUE;
    }
    return JNI_FALSE;

}

JNIEXPORT jintArray JNICALL Java_com_micsig_tbook_scope_math_MathNative_ByteBufferToIntArray
        (JNIEnv *env, jobject thisz, jobject src){
    XWaveData xWaveData((WAVE_T*)env->GetDirectBufferAddress(src));
    int len=xWaveData.getWaveLength();
    int* wave=xWaveData.getWaveData();
    jintArray jintArray1=env->NewIntArray(len);
    env->SetIntArrayRegion(jintArray1, 0, len, wave);
    return jintArray1;
}





JNIEXPORT jboolean JNICALL Java_com_micsig_tbook_scope_math_MathNative_fft
        (JNIEnv *env, jobject thisz,jint chIdx, jint fftType,jint fftWindow,jobject dst,jobject src) {

    if (chIdx >= 0 && chIdx < MATH_MAX) {
        if (MathFFTCalc(chIdx,fftType, fftWindow, (WAVE_T *) env->GetDirectBufferAddress(dst),
                        (WAVE_T *) env->GetDirectBufferAddress(src)
        )) {
            return JNI_TRUE;
        }
    }
    return JNI_FALSE;
}

JNIEXPORT jint JNICALL Java_com_micsig_tbook_scope_math_MathNative_CalFFTPointNum
        (JNIEnv *env, jobject thisz, jint length){
    return MathFFT::CalFFTPointNum(length);
}

JNIEXPORT jdouble JNICALL Java_com_micsig_tbook_scope_math_MathNative_fftDcVal
        (JNIEnv *, jobject,jint chIdx){
    if(chIdx >= 0 && chIdx < MATH_MAX){
        return MathFFT::Instane(chIdx)->getDCValue();
    }
    return 0;
}
JNIEXPORT void JNICALL Java_com_micsig_tbook_scope_math_MathNative_csetVAd
        (JNIEnv *, jobject ,jint chIdx, jdouble vol){
    if(chIdx >= 0 && chIdx < MATH_MAX) {
        MathFFT::Instane(chIdx)->setVAd(vol);
    }

}

JNIEXPORT jdouble JNICALL Java_com_micsig_tbook_scope_math_MathNative_fftMaxVal
        (JNIEnv *, jobject,jint chIdx){
    if(chIdx >= 0 && chIdx < MATH_MAX) {
        return MathFFT::Instane(chIdx)->getMaxValue();
    }
    return 0;
}
JNIEXPORT jint JNICALL Java_com_micsig_tbook_scope_math_MathNative_fftMaxValIdx
        (JNIEnv *, jobject,jint chIdx){
    if(chIdx >= 0 && chIdx < MATH_MAX) {
        return MathFFT::Instane(chIdx)->getMaxValIdx();
    }
    return 0;
}
static int64_t WaveSum(int32_t * pWaveData,int WaveLength){
    int64_t sum = 0;
    for(int i=0;i<WaveLength;i++,pWaveData++){
        sum += *pWaveData;
    }
    return sum;
}

static int32_t WaveMax(int32_t * pWaveData,int WaveLength){
    int32_t max = INT32_MIN;
    for(int i=0;i<WaveLength;i++,pWaveData++){
        if(max < *pWaveData)
            max = *pWaveData;
    }
    return max;
}

static int32_t WaveMin(int32_t * pWaveData,int WaveLength){
    int32_t min = INT32_MAX;
    for(int i=0;i<WaveLength;i++,pWaveData++){
        if(min > *pWaveData)
            min = *pWaveData;
    }
    return min;
}

static int64_t WaveSum(int32_t * pWaveData,int step, int WaveLength){
    int64_t sum = 0;
    for(int i=0;i<WaveLength;i++,pWaveData+=step){
        sum += *pWaveData;
    }
    return sum;
}

static int32_t MathAverage(WAVE_T* pSrc){
    XWaveData src(pSrc);
    return(int32_t)(WaveSum(src.getWaveData(),src.getWaveLength())/src.getWaveLength()) ;
}

static int64_t MathSum(WAVE_T* pSrc){
    XWaveData src(pSrc);
    return WaveSum(src.getWaveData(),src.getWaveLength());
}
static int64_t MathSum(WAVE_T* pSrc, jint idx, jint len){
    XWaveData src(pSrc);
    return WaveSum(src.getWaveData()+idx, len);
}

static int64_t MathSum(WAVE_T* pSrc, jint idx, jint step, jint len){
    XWaveData src(pSrc);
    return WaveSum(src.getWaveData()+idx, step, len);
}

static int32_t MathMax(WAVE_T* pSrc){
    XWaveData src(pSrc);
    return WaveMax(src.getWaveData(),src.getWaveLength());
}
static int32_t MathMax(WAVE_T* pSrc, jint idx, jint len){
    XWaveData src(pSrc);
    return WaveMax(src.getWaveData()+idx, len);
}
static int32_t MathMin(WAVE_T* pSrc){
    XWaveData src(pSrc);
    return WaveMin(src.getWaveData(),src.getWaveLength());
}
static int32_t MathMin(WAVE_T* pSrc, jint idx, jint len){
    XWaveData src(pSrc);
    return WaveMin(src.getWaveData()+idx, len);
}

JNIEXPORT jlong JNICALL Java_com_micsig_tbook_scope_math_MathNative_sum
        (JNIEnv *env, jobject thiz, jobject src){
    return MathSum((WAVE_T*)env->GetDirectBufferAddress(src));
}

JNIEXPORT jlong JNICALL Java_com_micsig_tbook_scope_math_MathNative_sum1
        (JNIEnv *env, jobject thiz, jobject src, jint idx, jint len){
    return MathSum((WAVE_T*)env->GetDirectBufferAddress(src), idx, len);
}

JNIEXPORT jlong JNICALL Java_com_micsig_tbook_scope_math_MathNative_sum2
        (JNIEnv *env, jobject thiz, jobject src, jint idx, jint step, jint len){
    return MathSum((WAVE_T*)env->GetDirectBufferAddress(src), idx, step, len);
}

JNIEXPORT jint JNICALL Java_com_micsig_tbook_scope_math_MathNative_average
        (JNIEnv *env, jobject thiz, jobject src){
    return MathAverage((WAVE_T*)env->GetDirectBufferAddress(src));
}

JNIEXPORT jint JNICALL Java_com_micsig_tbook_scope_math_MathNative_max
        (JNIEnv *env, jobject thiz, jobject src){
    return MathMax((WAVE_T*)env->GetDirectBufferAddress(src));
}

JNIEXPORT jint JNICALL Java_com_micsig_tbook_scope_math_MathNative_max1
        (JNIEnv *env, jobject thiz, jobject src, jint idx, jint len){
    return MathMax((WAVE_T*)env->GetDirectBufferAddress(src), idx, len);
}

JNIEXPORT jint JNICALL Java_com_micsig_tbook_scope_math_MathNative_min
        (JNIEnv *env, jobject thiz, jobject src){
    return MathMin((WAVE_T*)env->GetDirectBufferAddress(src));
}

JNIEXPORT jint JNICALL Java_com_micsig_tbook_scope_math_MathNative_min1
        (JNIEnv *env, jobject thiz, jobject src, jint idx, jint len){
    return MathMin((WAVE_T*)env->GetDirectBufferAddress(src), idx, len);
}

std::string jstring2str(JNIEnv* env, jstring jstr)
{
    char*   rtn   =   NULL;
    jclass   clsstring   =   env->FindClass("java/lang/String");
    jstring   strencode   =   env->NewStringUTF("GB2312");
    jmethodID   mid   =   env->GetMethodID(clsstring,   "getBytes",   "(Ljava/lang/String;)[B");
    jbyteArray   barr=   (jbyteArray)env->CallObjectMethod(jstr,mid,strencode);
    jsize   alen   =   env->GetArrayLength(barr);
    jbyte*   ba   =   env->GetByteArrayElements(barr,JNI_FALSE);
    if(alen   >=0)
    {
        rtn   =   (char*)malloc(alen+1);
        memcpy(rtn,ba,alen);
        rtn[alen]=0;
    }
    env->ReleaseByteArrayElements(barr,ba,0);
    std::string stemp(rtn);
    free(rtn);
    return   stemp;
}
JNIEXPORT jobject JNICALL Java_com_micsig_tbook_scope_math_MathNative_isExprValid
        (JNIEnv *env, jobject thiz, jstring expr_string)
{
    return MathExpr::isMathExprValid(env,jstring2str(env,expr_string));
}
JNIEXPORT jboolean JNICALL Java_com_micsig_tbook_scope_math_MathNative_setExpr
        (JNIEnv *env, jobject thiz,jint chIdx,jstring expr_string)
{
    if(chIdx >= 0 && chIdx < MATH_MAX){
        return setMathExpr(chIdx,jstring2str(env,expr_string)) ? JNI_TRUE : JNI_FALSE;
    }
    return JNI_FALSE;
}
JNIEXPORT jdouble JNICALL Java_com_micsig_tbook_scope_math_MathNative_calcExpr
        (JNIEnv *env, jobject thiz,jint chIdx, jobject dst,jobjectArray  chArray,jdouble time,
                jdouble var1,jdouble var2)
{
    if(chIdx >= 0 && chIdx < MATH_MAX){
        jsize arrayLength = env->GetArrayLength(chArray);
        std::vector<WAVE_T*> chVec;

        for(int i=0;i<arrayLength;i++){
            jobject ch = env->GetObjectArrayElement(chArray,i);
            chVec.push_back(ch==0 ? nullptr : (WAVE_T*)env->GetDirectBufferAddress(ch));
        }
        CalcMathExpr(chIdx,(WAVE_T*)env->GetDirectBufferAddress(dst),chVec,time,var1,var2);
        return getMathExprMaxVal(chIdx);
    }
    return 0;
}