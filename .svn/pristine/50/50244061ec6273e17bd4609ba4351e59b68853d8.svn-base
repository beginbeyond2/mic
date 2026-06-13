//
// Created by zhuzh on 2018-4-12.
//

#include <cmath>
#include <ctime>
#include "stdlib.h"
#include "MathDualCalc.h"
#include "../Logger.h"
#define  TAG "MathDualCalc"



bool MathDualCalc::Add(XWaveData *dstWave, XWaveData *src1, XWaveData *src2) {
//    long start=getCurrentTime();
    double ratio1 = src1->getVerticalPerPix() / dstWave->getVerticalPerPix();
    double ratio2 = src2->getVerticalPerPix() / dstWave->getVerticalPerPix();


    int bufLen = 0, bufLen1 = src1->getWaveLength(), bufLen2 = src2->getWaveLength();
    int iStep1 = 1, iStep2 = 1;

    if(bufLen1 > bufLen2) {
        bufLen = bufLen2;
        if(bufLen > 0) {
            iStep1 = bufLen1 / bufLen;
            iStep2 = 1;
        } else {
            bufLen = bufLen1;
        }
    } else {
        bufLen = bufLen1;
        if(bufLen > 0) {
            iStep1 = 1;
            iStep2 = bufLen2 / bufLen;
        } else {
            bufLen = bufLen2;
        }
    }

    int32_t *p1 = src1->getWaveData();
    int32_t *p2 = src2->getWaveData();
    int32_t *pd = dstWave->getWaveData();
    int64_t val = 0;
    if(bufLen1 > 0 && bufLen2 > 0){
        for(int i=0; i<bufLen; i++) {
            val = lround((*p1) * ratio1 + (*p2) * ratio2);
            if(val > INT32_MAX) val = INT32_MAX;
            else if(val < INT32_MIN) val = INT32_MIN;
            *pd++ = (int32_t)val;
            p1 += iStep1;
            p2 += iStep2;
        }
    }else if(bufLen1 > 0){
        for(int i=0; i<bufLen; i++) {
            val = lround((*p1) * ratio1);
            if(val > INT32_MAX) val = INT32_MAX;
            else if(val < INT32_MIN) val = INT32_MIN;
            *pd++ = (int32_t)val;
            p1 += iStep1;
        }
    }else if(bufLen2 > 0){
        for(int i=0; i<bufLen; i++) {
            val = lround((*p2) * ratio2);
            if(val > INT32_MAX) val = INT32_MAX;
            else if(val < INT32_MIN) val = INT32_MIN;
            *pd++ = (int32_t)val;
            p2 += iStep2;
        }
    }


    dstWave->setWaveLength(bufLen);
//    long end=getCurrentTime();
//    long interval=end-start;

//    LOGD("math add time interval:%ld -%ld = %ld, len:%d",end,start,interval,bufLen);
    return true;
}

bool MathDualCalc::Sub(XWaveData *dstWave, XWaveData *src1, XWaveData *src2) {
    double ratio1 = src1->getVerticalPerPix() / dstWave->getVerticalPerPix();
    double ratio2 = src2->getVerticalPerPix() / dstWave->getVerticalPerPix();

    int bufLen = 0, bufLen1 = src1->getWaveLength(), bufLen2 = src2->getWaveLength();
    int iStep1 = 1, iStep2 = 1;

    if(bufLen1 > bufLen2) {
        bufLen = bufLen2;
        if(bufLen > 0) {
            iStep1 = bufLen1 / bufLen;
            iStep2 = 1;
        } else {
            bufLen = bufLen1;
        }
    } else {
        bufLen = bufLen1;
        if(bufLen > 0) {
            iStep1 = 1;
            iStep2 = bufLen2 / bufLen;
        } else {
            bufLen = bufLen2;
        }
    }

    int32_t ch1, ch2;
    int32_t *p1 = src1->getWaveData();
    int32_t *p2 = src2->getWaveData();
    int32_t *pd = dstWave->getWaveData();
    int64_t val = 0;
    if(bufLen1 >0 && bufLen2 > 0){
        for(int i=0; i<bufLen; i++) {
            val = lround((*p1) * ratio1 - (*p2) * ratio2);
            if(val > INT32_MAX) val = INT32_MAX;
            else if(val < INT32_MIN) val = INT32_MIN;
            *pd++ = (int32_t)val;
            p1 += iStep1;
            p2 += iStep2;
        }
    }else if(bufLen1 > 0){
        for(int i=0; i<bufLen; i++) {
            val = lround((*p1) * ratio1);
            if(val > INT32_MAX) val = INT32_MAX;
            else if(val < INT32_MIN) val = INT32_MIN;
            *pd++ = (int32_t)val;
            p1 += iStep1;
        }
    }else if(bufLen2 > 0){
        for(int i=0; i<bufLen; i++) {
            val = lround(0 - (*p2) * ratio2);
            if(val > INT32_MAX) val = INT32_MAX;
            else if(val < INT32_MIN) val = INT32_MIN;
            *pd++ = (int32_t)val;
            p2 += iStep2;
        }
    }

    dstWave->setWaveLength(bufLen);
    return true;
}

bool MathDualCalc::Mul(XWaveData *dstWave, XWaveData *src1, XWaveData *src2) {
    double ratio = src1->getVerticalPerPix() * src2->getVerticalPerPix() / dstWave->getVerticalPerPix();

    int bufLen = 0, bufLen1 = src1->getWaveLength(), bufLen2 = src2->getWaveLength();
    int iStep1 = 1, iStep2 = 1;

    if(bufLen1 > bufLen2) {
        bufLen = bufLen2;
        if(bufLen > 0) {
            iStep1 = bufLen1 / bufLen;
            iStep2 = 1;
        } else {
            bufLen = bufLen1;
        }
    } else {
        bufLen = bufLen1;
        if(bufLen > 0) {
            iStep1 = 1;
            iStep2 = bufLen2 / bufLen;
        } else {
            bufLen = bufLen2;
        }
    }

    int32_t *p1 = src1->getWaveData();
    int32_t *p2 = src2->getWaveData();
    int32_t *pd = dstWave->getWaveData();
    int64_t val = 0;
    if(bufLen1 > 0 && bufLen2 > 0){
        for(int i=0; i<bufLen; i++) {
            val = lround((*p1) * (*p2) * ratio);
            if(val > INT32_MAX) val = INT32_MAX;
            else if(val < INT32_MIN) val = INT32_MIN;
            *pd++ = (int32_t)val;
            p1 += iStep1;
            p2 += iStep2;
        }
    }else {
        for(int i=0; i<bufLen; i++) {
            *pd++ = 0;
        }
    }

    dstWave->setWaveLength(bufLen);
    return true;
}

bool MathDualCalc::Div(XWaveData *dstWave, XWaveData *src1, XWaveData *src2) {
    double ratio = src1->getVerticalPerPix() / src2->getVerticalPerPix() / dstWave->getVerticalPerPix();

    int bufLen = 0, bufLen1 = src1->getWaveLength(), bufLen2 = src2->getWaveLength();
    int iStep1 = 1, iStep2 = 1;

    if(bufLen1 > bufLen2) {
        bufLen = bufLen2;
        if(bufLen > 0) {
            iStep1 = bufLen1 / bufLen;
            iStep2 = 1;
        } else {
            bufLen = bufLen1;
        }
    } else {
        bufLen = bufLen1;
        if(bufLen > 0) {
            iStep1 = 1;
            iStep2 = bufLen2 / bufLen;
        } else {
            bufLen = bufLen2;
        }
    }

#define  NI_HE_0 10 //正常计算的拟合值定义，设置为0则拟合效果取消
#define  NI_HE_1 5  //无穷计算的拟合值定义，设置为0则你和效果取消
    int32_t ch1, ch2;
    int32_t *p1 = src1->getWaveData();
    int32_t *p2 = src2->getWaveData();
    int32_t *pd = dstWave->getWaveData();
    int64_t val = 0;
    if(bufLen1 > 0 &&  bufLen2 > 0){
        for(int i=0; i<bufLen; i++) {
            ch1 = *p1;
            ch2 = *p2;
            if(ch2 != 0) {
                double res = 1.0*ch1/ch2;
                if(abs(ch1 - ch2) < NI_HE_0) res = 1;
                val = lround(res * ratio);

            } else {
                int iRes = 1;
                if(ch1 > NI_HE_1) {
                    val = 0x7FFFFFFF;
                } else if(ch1 < -NI_HE_1) {
                    val = 0x80000000;
                } else {
                    val = lround(iRes * ratio);
                }
            }
            if(val > INT32_MAX) val = INT32_MAX;
            else if(val < INT32_MIN) val = INT32_MIN;
            *pd = (int32_t)val;
            p1 += iStep1; p2 += iStep2;pd++;
        }
    }else if(bufLen1 > 0){
        for(int i=0; i<bufLen; i++) {
            ch1 = *p1;
            int iRes = 1;
            if(ch1 > NI_HE_1) {
                val = 0x7FFFFFFF;
            } else if(ch1 < -NI_HE_1) {
                val = 0x80000000;
            } else {
                val = lround(iRes * ratio);
            }
            if(val > INT32_MAX) val = INT32_MAX;
            else if(val < INT32_MIN) val = INT32_MIN;
            *pd = (int32_t)val;
            p1 += iStep1;
            pd++;
        }
    }else if(bufLen2 > 0){
        for(int i=0; i<bufLen; i++) {
            *pd++ = 0;
        }
    }

    dstWave->setWaveLength(bufLen);
    return true;
}

long MathDualCalc::getCurrentTime(){
    struct timeval tv;
    gettimeofday(&tv,NULL);
    return (tv.tv_sec*1000000+tv.tv_usec)/1000;
}