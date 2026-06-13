//
// Created by zhuzh on 2018-5-25.
//

#ifndef TBOOKSCOPE_WAVE2XYBMP_H
#define TBOOKSCOPE_WAVE2XYBMP_H


#include "wavedata.h"

class Wave2XYBmp {
public:
    Wave2XYBmp();
    void InitXY(XYBmp *xyBmp,XWaveData *xWaveData,XWaveData *yWaveData);
    static void waveConvertXYBmp(XYBmp *xyBmp,XWaveData *aWaveData,XWaveData *bWaveData);
    void XYWave2Bitmap(int32_t *x,
                       int32_t *y,
                       int32_t len,
                       int32_t xz,
                       int32_t yz,
                       uint32_t *pBitmap);
    int32_t * CalcWaveX(XYBmp *xyBmp,XWaveData * waveData);
    int32_t * CalcWaveY(XYBmp *xyBmp,XWaveData * waveData);


private:
    int32_t * CalcWave(XWaveData * dstWaveData,XWaveData * waveData);
    int32_t *Cache1;
    int32_t *Cache2;
    int32_t width;
    int32_t height;
    int32_t *xTable;
    int32_t *yTable;
    uint32_t brightness;
    uint32_t foregroundColor;
    uint32_t backgroundColor;
    uint32_t cctEnable;
    double xAdPix;
    double yAdPix;
    static Wave2XYBmp * _instance;
    static Wave2XYBmp* Instance();


    XWaveData * hWaveData;
    XWaveData * vWaveData;

    double xWaveFactor;
    double yWaveFactor;
    uint32_t adMaxVal;
};


#endif //TBOOKSCOPE_WAVE2XYBMP_H
