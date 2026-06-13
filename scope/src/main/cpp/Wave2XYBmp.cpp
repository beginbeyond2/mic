//
// Created by zhuzh on 2018-5-25.
//

#include <memory.h>
#include <math.h>
#include "Wave2XYBmp.h"
static uint32_t cct_rgb[256]={
        0,
        16733952,
        16743168,
        16750336,
        16756480,
        16761600,
        16766720,
        16770816,
        16774912,
        16252672,
        15204096,
        14417664,
        13631232,
        12844800,
        12058368,
        11271936,
        10485504,
        9961216,
        9174784,
        8650496,
        7864064,
        7339776,
        6815488,
        6029056,
        5504768,
        4980480,
        4456192,
        3931904,
        3407616,
        2883328,
        2359040,
        1834752,
        1572608,
        1048320,
        524032,
        65283,
        65287,
        65295,
        65303,
        65307,
        65315,
        65319,
        65327,
        65331,
        65339,
        65343,
        65351,
        65355,
        65363,
        65367,
        65371,
        65379,
        65383,
        65387,
        65395,
        65399,
        65403,
        65407,
        65415,
        65419,
        65423,
        65427,
        65431,
        65435,
        65443,
        65447,
        65451,
        65455,
        65459,
        65463,
        65467,
        65471,
        65475,
        65479,
        65483,
        65487,
        65491,
        65495,
        65499,
        65503,
        65507,
        65511,
        65515,
        65519,
        65523,
        65527,
        65531,
        65531,
        65535,
        64511,
        63487,
        62463,
        61439,
        61439,
        60415,
        59391,
        58367,
        57343,
        57343,
        56319,
        55295,
        54271,
        54271,
        53247,
        52223,
        51199,
        51199,
        50175,
        49151,
        48127,
        48127,
        47103,
        46079,
        46079,
        45055,
        44031,
        44031,
        43007,
        41983,
        41983,
        40959,
        40959,
        39935,
        38911,
        38911,
        37887,
        37887,
        36863,
        35839,
        35839,
        34815,
        34815,
        33791,
        33791,
        32767,
        32767,
        31743,
        31743,
        30719,
        30719,
        29695,
        29695,
        28671,
        28671,
        27647,
        27647,
        26623,
        26623,
        25599,
        25599,
        24575,
        24575,
        23551,
        23551,
        23551,
        22527,
        22527,
        21503,
        21503,
        20479,
        20479,
        20479,
        19455,
        19455,
        18431,
        18431,
        18431,
        17407,
        17407,
        17407,
        16383,
        16383,
        16383,
        15359,
        15359,
        15359,
        14335,
        14335,
        14335,
        13311,
        13311,
        13311,
        12287,
        12287,
        12287,
        12287,
        11263,
        11263,
        11263,
        10239,
        10239,
        10239,
        10239,
        9215,
        9215,
        9215,
        9215,
        8191,
        8191,
        8191,
        8191,
        8191,
        7167,
        7167,
        7167,
        7167,
        7167,
        6143,
        6143,
        6143,
        6143,
        6143,
        5119,
        5119,
        5119,
        5119,
        5119,
        5119,
        5119,
        4095,
        4095,
        4095,
        4095,
        4095,
        4095,
        4095,
        4095,
        3071,
        3071,
        3071,
        3071,
        3071,
        3071,
        3071,
        3071,
        3071,
        3071,
        3071,
        3071,
        2047,
        2047,
        2047,
        2047,
        2047,
        2047,
        2047,
        2047,
        2047,
        2047,
        2047,
        2047,
        2047,
        2047,
        2047,
        2047,
        1023,
};
Wave2XYBmp * Wave2XYBmp::_instance = NULL;
Wave2XYBmp * Wave2XYBmp::Instance() {
    if(Wave2XYBmp::_instance == NULL){
        Wave2XYBmp::_instance = new Wave2XYBmp;
    }
    return Wave2XYBmp::_instance;
}
Wave2XYBmp::Wave2XYBmp(){
    Cache1 = NULL;
    Cache2 = NULL;
    hWaveData = NULL;
    vWaveData = NULL;
    xTable = NULL;
    yTable = NULL;
    width = 0;
    height = 0;
    brightness = 0;
    foregroundColor = 0xFFFFFF00;
    backgroundColor = 0;
    xAdPix = 0;
    yAdPix = 0;
    vWaveData = new XWaveData;
    hWaveData = new XWaveData;

}
static __inline  bool equ_double(double a, double b)
{
    return fabs(a - b) < 1e-10;
}

static void CalcTable(int32_t * table,int32_t len, double adPix){
    int idx = 0;
    int32_t *addr = 0;

    for(int i=0;i<len;i++){
        idx = (int)((double)i/adPix);
        addr = (table + idx * (len + 1));
        addr[0] += 1;
        int num = addr[0];
        if(num > 0)
            addr[num] = i;
    }
}

void Wave2XYBmp::InitXY(XYBmp *xyBmp,XWaveData *xWaveData,XWaveData *yWaveData){

    width = xyBmp->getWidth();
    height = xyBmp->getHeight();
    int size = width * height;
    if(Cache1 == NULL){
        Cache1 = new int32_t[size];
    }
    if(Cache2 == NULL){
        Cache2 = new int32_t[size];
    }
    memset(Cache1,0,size*sizeof(int32_t));
    memset(Cache2,0,size*sizeof(int32_t));

    adMaxVal = xyBmp->getAdMaxVal() + 1;
    if(xTable == NULL) {
        xTable = new int32_t[adMaxVal * (width + 1)];
    }
    if(yTable == NULL) {
        yTable = new int32_t[adMaxVal * (height + 1)];
    }

    xWaveFactor = xWaveData->getWaveFactor();
    yWaveFactor = yWaveData->getWaveFactor();

    double adPix = xWaveData->getAdPix();

    if(!equ_double(xAdPix,adPix)){
        xAdPix = adPix;
        memset(yTable,0,adMaxVal * (width + 1)*sizeof(int32_t));
        CalcTable(xTable,width,xAdPix);
    }
    adPix = yWaveData->getAdPix();


    if(!equ_double(yAdPix,adPix)){
        yAdPix = adPix;
        memset(yTable,0,adMaxVal * (height + 1)*sizeof(int32_t));
        CalcTable(yTable,height,yAdPix);
    }


    brightness = xyBmp->getBrightness();
    foregroundColor = xyBmp->getForegroundColor();
    backgroundColor = xyBmp->getBackgroundColor();
    cctEnable = xyBmp->cctEnable();
    if(cctEnable){
        brightness = 0;
    }
    uint32_t * pBitmap = xyBmp->getBuffer();
    for(int i=0;i<size;i++){
        *pBitmap++ = backgroundColor;
    }
}

void Wave2XYBmp::waveConvertXYBmp(XYBmp *xyBmp,XWaveData *xWaveData,XWaveData *yWaveData){

    Wave2XYBmp * pWave2XYBmp = Instance();
    pWave2XYBmp->InitXY(xyBmp,xWaveData,yWaveData);
    if(xyBmp->isRun()){
        pWave2XYBmp->XYWave2Bitmap(xWaveData->getWaveData(),
                                   yWaveData->getWaveData(),
                                   xWaveData->getWaveLength(),
                                   xyBmp->getX(),
                                   xyBmp->getY(),
                                   xyBmp->getBuffer()
        );
    }else{
        pWave2XYBmp->XYWave2Bitmap(pWave2XYBmp->CalcWaveX(xyBmp,xWaveData),
                                   pWave2XYBmp->CalcWaveY(xyBmp,yWaveData),
                                   xWaveData->getWaveLength(),
                                   xyBmp->getX(),
                                   xyBmp->getY(),
                                   xyBmp->getBuffer()
        );

    }

}

int32_t * Wave2XYBmp::CalcWave(XWaveData * dstWaveData,XWaveData * waveData){
    int32_t waveNums = waveData->getWaveLength();
    int32_t * dstData = dstWaveData->getWaveData();
    int32_t * srcData = waveData->getWaveData();
    double srcVScale = waveData->getVScaleVal();
    double dstVScale = dstWaveData->getVScaleVal();
    for(int i=0;i<waveNums;i++){
        *(dstData + i) =(int32_t) ((*(srcData + i)) * srcVScale/dstVScale);
    }
    return dstData;
}
int32_t * Wave2XYBmp::CalcWaveX(XYBmp *xyBmp,XWaveData * waveData){

    hWaveData->setVScaleVal(xyBmp->getXDstVScaleVal());
    return CalcWave(hWaveData,waveData);
}
int32_t * Wave2XYBmp::CalcWaveY(XYBmp *xyBmp,XWaveData * waveData){

    vWaveData->setVScaleVal(xyBmp->getYDstVScaleVal());
    return CalcWave(vWaveData,waveData);
}

void Wave2XYBmp::XYWave2Bitmap(int32_t *x,
                               int32_t *y,
                               int32_t len,
                               int32_t xz,
                               int32_t yz,
                               uint32_t *pBitmap)
{


#define MAX_VALUE 0xFF

    int iCount2len = 0;
    int max = 0;
    int index = 0;
    int wx = 0;
    int wy = 0;

    int32_t Val = 0;
    int32_t * xAddr = 0;
    int32_t * yAddr = 0;
    xz = (int32_t)((width/2-xz)/xAdPix);
    yz = (int32_t)((height/2-yz)/yAdPix);
    //xz/=xAdPix;
    //yz/=yAdPix;
    for(int i=0;i<len;i++)
    {
        wx = x[i] * xWaveFactor  + xz;
        wy =  yz - y[i] * yWaveFactor;

        if(wx>=0
           && wx<adMaxVal
           && wy>=0
           && wy<adMaxVal)
        {

            xAddr = (xTable + wx *(width+1));
            for(int m = 1;m <= *xAddr;m++)
            {
                yAddr = (yTable + wy * (height+1));
                for(int n = 1;n <= *(yAddr);n++)
                {
                    index = (yAddr[n]) * width + (xAddr[m]);
                    if(Cache1[index] == 0)
                    {
                        Cache2[iCount2len]=index;
                        iCount2len++;
                        Cache1[index] = MAX_VALUE * brightness / 100;
                    }
                    Cache1[index]++;
                    if(Cache1[index] > MAX_VALUE) Cache1[index] = MAX_VALUE;
                    index = Cache1[index];
                    if(index > max) max = index;
                }
            }

        }
    }
    if(max > 0)
    {
        for(int i=0;i<iCount2len;i++)
        {
            index = Cache2[i];
            Val = Cache1[index] * MAX_VALUE / max;
            if(cctEnable){
                Val = cct_rgb[Val];
            }else{
                Val = ((Val << 16) | (Val << 8) | (Val<<0)) & foregroundColor;
            }
            pBitmap[index] = 0xFF000000 | Val;
        }
    }

}
