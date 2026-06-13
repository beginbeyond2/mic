//
// Created by zhuzh on 2018-5-25.
//

#include <unistd.h>
#include "Wave2Bmp.h"
#include "Logger.h"
#define TAG "waveConvertBmp::"
//#define mLOGD if(1)LOGD
#define mLOGD
//#define DEBUG_XX
#ifdef DEBUG_XX
#define WAVE_WIDTH (700)
#define WAVE_HEIGHT (500)

#define WAVE_ASSERT \
while (img > imgEnd){\
LOGE("img > imgEnd,img = 0x%x,imgEnd = 0x%x ,%d\n",img,imgEnd,__LINE__);\
::sleep(1);\
}
#else
#define WAVE_ASSERT
#endif

void Wave2Bmp::waveConvertBmp(XBmp *xBmp,XWaveData *xWaveData){
    if(xBmp == NULL || xWaveData == NULL) return;
    mLOGD(TAG"============== waveConvertBmp ==============\n");
    mLOGD(TAG"xWaveData\tsampRate:%g waveType:%d chIdx:%d"
    , xWaveData->getSampRate(), xWaveData->getWaveType(), xWaveData->getChIdx());
    //注意：xBmp的xBmpBuffer排布为先横向后纵向
    uint32_t *img = xBmp->getBuffer();

    uint32_t fgdCor = xBmp->getForegroundColor();
    uint32_t bgdCor = xBmp->getBackgroundColor();
    int hMax = xBmp->getWidth(), halfHMax = hMax/2;
    int vMax = xBmp->getHeight(), halfVMax = vMax/2;
    int vInit = 0;
    bool bZoom = false;
    int w = xBmp->getW();
    int x1 = xBmp->getX1();
    int x2 = xBmp->getX2();
#ifdef DEBUG_XX
    uint32_t *imgEnd = NULL;
    if(hMax > WAVE_WIDTH || vMax > WAVE_HEIGHT ) {
        LOGE("hMax = %d,vMax = %d\n",hMax,vMax);
        return;
    }
    imgEnd = img + (WAVE_WIDTH * WAVE_HEIGHT);
#endif

    if (vMax == 750) {
        bZoom = true;
        vInit = 250;
    }
    if(xWaveData->getWaveLength() < 2) {//单点和空点都无法绘制图形
        for(int i=0; i<w; i++) {
            for(int j=0; j<vMax+vInit; j++) {
                WAVE_ASSERT;
                *img++ = bgdCor;
            }
        }
        return;
    }

    int32_t *data = xWaveData->getWaveData();
    int len = xWaveData->getWaveLength();
    int start = xBmp->getStartX();
    int end = xBmp->getEndX();
    int cols = end - start + 1;
    double xRatio = xWaveData->getTimeScaleVal()/xBmp->getTimeScaleVal();
    double yRatio = xWaveData->getVerticalPerPix()/xBmp->getVerticalPerPix();//原始档位值除以当前档位值

    if(bZoom) yRatio *= 75.0/100;//比例是一样的，这里不做修改

    double xDelta = (double)len/cols/xRatio;

    double xOffset = xBmp->getXOffset();//移动多少像素
    double yOffset = xBmp->getYOffset();


    //绝对坐标转换为逻辑坐标，有效区域为[-halfHMax : 0 : halfHMax-1]，即长度为HMax
    double iStart = (start - halfHMax) * xRatio;
    double iEnd = (end - halfHMax) * xRatio;


    int xStart = 0, xEnd = hMax-1, xPoint = 0;
    double dPosS = iStart+xOffset+halfHMax;
    if(dPosS < 0) {
        xStart = 0;
        xPoint = (int) (-1.0 * dPosS * xDelta);
    } else {
        xPoint = 0;
        xStart = (int)(dPosS);
    }
    double dPosE = iEnd+xOffset+halfHMax;
    if(dPosE > hMax-1) {
        xEnd = hMax-1;
    } else {
        xEnd = (int)(dPosE);
    }
    if(xEnd < xStart) xPoint = 0;
    if(xStart > xEnd) xPoint = 0;
//    mLOGD(TAG"hMax:%d halfHMax:%d xOffset:%g iStart:%g iEnd:%g", hMax, halfHMax, xOffset, iStart, iEnd);
//    mLOGD(TAG" dPosS:%g dPosE:%g xStart:%d xEnd:%d xPoint:%d", dPosS, dPosE, xStart, xEnd, xPoint);
    if (xEnd - xStart >= xRatio * cols) {//0011184
        xEnd -= 1;
    }
    int32_t *pData = data + xPoint;
    if(xDelta < 1) {
        //插值
        int32_t vPre, vBak, yCur, yOld;
        double tDelta = 1/xDelta;           //计算插值率，即两点间插入几个点
        int tDelta1 = (int)(tDelta);        //插值率的整数部分
        double tDelta2 = tDelta - tDelta1;  //插值率的小数部分
        mLOGD(TAG"\tChaZhi==>tDelta[%g]:%d - %g", tDelta, tDelta1, tDelta2);
        double deltaSum = 0;//插值率小数部分的累积和，当累积和超过1，则需要多插入一个点
        int deltaLoop = tDelta1;
        int i = 0;
        bool bFirst = true;
        while(i<hMax) {
            if(i<xStart || i>xEnd || deltaLoop>hMax) {
                uint32_t * imgBak = img;
                for(int j=0; j<vMax+vInit; j++) {
                    WAVE_ASSERT;
                    *img = bgdCor;
                    img += w;
                }
                img = imgBak+1;

                i++;
            } else {
                if(i == 0) {
                    deltaSum = 0;
                    deltaLoop = tDelta1;
                } else {
                    deltaSum += tDelta2;//插值率小数部分进行累积
                    if(deltaSum > 1) {
                        deltaSum -= 1.0;
                        deltaLoop = tDelta1+1;
                    } else {
                        deltaLoop = tDelta1;
                    }
                }
                vPre = *pData++, vBak = *pData;         //原始数据递进
                vPre *= yRatio, vBak *= yRatio;         //垂直缩放
                double slope = (vBak-vPre)/(double)deltaLoop;   //计算直线插值的斜率值

                if(i <= x1  && i+deltaLoop > x1) {
                    double v = (vPre + (x1 - i) *  slope) * xBmp->getVerticalPerPix();
                    xBmp->setX1Val(v);
                    xBmp->setX1Valid(1);
                }
                if(i <= x2  && i + deltaLoop > x2) {
                    double v = (vPre + (x2 - i) *  slope) * xBmp->getVerticalPerPix();
                    xBmp->setX2Val(v);
                    xBmp->setX2Valid(1);
                }

#if 1
                //绘线模式
                yCur = (int32_t)(vPre+yOffset);
                if(bFirst) {
                    yOld = yCur;
                    bFirst = false;
                }
                i += deltaLoop;
                if(i>hMax) {
                    deltaLoop -= i - hMax;
                }
//                mLOGD(TAG"i:%d hMax:%d deltaLoop:%d", i, hMax, deltaLoop);
                for(int m=0; m<deltaLoop; m++) {
                    if (m == 0) {
                        yCur = (int32_t) (vPre + yOffset);
                    } else {//注意：因slope可能为负值，所以需要进行计算获取
                        yCur = (int32_t) (vPre + yOffset + (int) (slope * m));//步进斜率值
                    }
                    //坐标变换：逻辑坐标(250:0:-250)变为绝对坐标(0:500) [上 -> 下]
                    int32_t yMax = halfVMax - yCur;
                    int32_t yMin = halfVMax - yOld;
                    yOld = yCur;//刷新前点
                    if(yMax < yMin) {
                        int32_t yTmp = yMax;
                        yMax = yMin; yMin = yTmp;
                    }
                    //边界处理
                    //边界处理
                    int32_t tMin = vInit, tMax = vInit+vMax-1;
                    yMin += vInit;
                    yMax += vInit;
                    if(yMin<tMin && yMax < tMin){
                        yMin = yMax = tMin - 1;
                    }else if(yMin > tMax && yMax > tMax){
                        yMin = yMax = tMax + 1;
                    }else{
                        if(yMin < tMin) yMin = tMin;
                        if(yMin > tMax) yMin = tMax;

                        if(yMax < tMin) yMax = tMin;
                        if(yMax > tMax) yMax = tMax;
                    }
                    //绘制处理
                    uint32_t * imgBak = img;
                    for (int j = 0; j < vMax+vInit; j++) {
                        WAVE_ASSERT;
                        if(j<yMin || j>yMax) {
                            *img = bgdCor;
                        } else {
                            *img = fgdCor;
                        }
                        img += w;
                    }

                    img = imgBak+1;
                }
#else
                //绘点模式
                yCur = (int32_t)(vPre+yOffset);
                i += deltaLoop;
                if(i>hMax) {
                    deltaLoop -= i - hMax;
                }
                for(int m=0; m<deltaLoop; m++) {
                    if(m == 0) {
                        yCur = (int32_t)(vPre+yOffset);
                    } else {//注意：因slope可能为负值，所以需要进行计算获取
                        yCur = (int32_t)(vPre+yOffset+(int)(slope*m));//步进斜率值
                    }
                    //坐标变换：逻辑坐标(250:0:-250)变为绝对坐标(0:500) [上 -> 下]
                    yCur = halfVMax - yCur;
                    //边界处理
                    int32_t tMin = vInit, tMax = vInit+vMax-1;
                    yCur += vInit;
                    if(yCur < tMin) yCur = tMin;
                    if(yCur > tMax) yCur = tMax;
                    //绘制处理
                    uint32_t * imgBak = img;
                    for (int j = 0; j < vMax+vInit; j++) {
                        if (j != yCur) {
                            *img = bgdCor;
                        } else {
                            *img = fgdCor;
                        }
                        img += hMax;
                    }
                    img = imgBak+1;
                }
#endif
            }
        }
    } else {
        //抽样
        int32_t yMin = *pData, yMax = *pData;
        int32_t yMinBak = yMin, yMaxBak = yMax;
        int tDelta1 = (int)(xDelta);        //抽值率的整数部分，注意抽值率即为多少点抽成一个点
        double tDelta2 = xDelta - tDelta1;  //抽值率的小数部分
        double deltaSum = 0;//抽值率的小数部分的累积和，当累积和超过1，则需要多一个点进入样本数组进行抽值比对
        int deltaLoop = tDelta1;
        int deltaLoopSum = xPoint;
//        mLOGD(TAG"\tChouYang==>tDelta[%g]:%d - %g", xDelta, tDelta1, tDelta2);
        for(int i=0; i<hMax; i++) {
            if(i<xStart || i>xEnd) {
                uint32_t * imgBak = img;
                for(int j=0; j<vMax+vInit; j++) {
                    WAVE_ASSERT;
                    *img = bgdCor;
                    img += w;
                }
//                mLOGD("\thide [%d] img:0x%X - 0x%X", i, (uint32_t)imgBak, (uint32_t)img);

                img = imgBak+1;
            } else {
                if(i == 0) {
                    deltaSum = 0;
                    deltaLoop = tDelta1;
                } else {
                    deltaSum += tDelta2;//抽值率小数部分进行累积
                    if(deltaSum > 1) {
                        deltaSum -= 1.0;
                        deltaLoop = tDelta1+1;
                    } else {
                        deltaLoop = tDelta1;
                    }
                }
                if(deltaLoop > len) deltaLoop = len;
                deltaLoopSum += deltaLoop;
                if(deltaLoopSum > len) {
                    deltaLoop -= deltaLoopSum - len;
                }
                yMin = *pData, yMax = *pData;
                for(int m=0; m<deltaLoop; m++) {
                    if(*pData < yMin) yMin = *pData;
                    else if(*pData > yMax) yMax = *pData;
                    pData++;
                }
                if(yMax < yMinBak) yMax = yMinBak;
                if(yMin > yMaxBak) yMin = yMaxBak;
                yMinBak = yMin, yMaxBak = yMax;
                yMin *= yRatio, yMax *= yRatio;//垂直缩放
                if(i == x1) {
                    mLOGD("x1 yMin:%d,yMax:%d\n",yMin,yMax);
                    xBmp->setX1Val((yMin + yMax) / 2 * xBmp->getVerticalPerPix());
                    xBmp->setX1Valid(1);
                }
                if(i == x2) {
                    mLOGD("x2 yMin:%d,yMax:%d\n",yMin,yMax);
                    xBmp->setX2Val((yMin + yMax) / 2 *xBmp->getVerticalPerPix());
                    xBmp->setX2Valid(1);
                }
                yMin += yOffset, yMax += yOffset;
                //坐标变换：逻辑坐标(250:0:-250)变为绝对坐标(0:500) [上 -> 下]
                int32_t temp = halfVMax - yMin;
                yMin = halfVMax - yMax;
                yMax = temp;
                //边界处理
                int32_t tMin = vInit, tMax = vInit+vMax-1;
                yMin += vInit;
                yMax += vInit;
                if(yMin<tMin && yMax < tMin){
                    yMin = yMax = tMin - 1;
                }else if(yMin > tMax && yMax > tMax){
                    yMin = yMax = tMax + 1;
                }else{
                    if(yMin < tMin) yMin = tMin;
                    if(yMin > tMax) yMin = tMax;

                    if(yMax < tMin) yMax = tMin;
                    if(yMax > tMax) yMax = tMax;
                }

                //绘制处理
                uint32_t * imgBak = img;
                for(int j=0; j<vMax+vInit; j++) {
                    WAVE_ASSERT;
                    if(j<yMin || j>yMax) {
                        *img = bgdCor;
                    } else {
                        *img = fgdCor;
                    }

                    img += w;
                }
//                if(i == xStart || i == xEnd) {
//                    mLOGD (TAG"\tdraw [%d] y:%d - %d img:0x%X - 0x%X", i, yMin, yMax, (uint32_t) imgBak, (uint32_t) img);
//                    mLOGD(TAG"\t deltaLoopSum:%d deltaLoop:%d", deltaLoopSum, deltaLoop);
//                }

                img = imgBak+1;
            }
        }
    }
}
