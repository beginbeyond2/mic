//
// Created by zhuzh on 2018-4-12.
//

#ifndef TBOOKSCOPE_WAVEDATA_H
#define TBOOKSCOPE_WAVEDATA_H

#include <stdint.h>
#define WAVE_FILE_SIZE (400*1000*4)
#define WAVE_HEADER_SIZE (256)
#define WAVE_MAX_LEN (WAVE_FILE_SIZE - WAVE_HEADER_SIZE)
#define WAVE_POINTS (WAVE_MAX_LEN/4)
#pragma pack(push) //保存对齐状态
#pragma pack(4)// 设定为4字节对齐
struct WAVE_T{
    union WAVEHEADER{
        uint8_t data[WAVE_HEADER_SIZE];
        struct HEADER{
            int32_t magicNum;           //标记
            int32_t version;            //版本
            int32_t waveLength;         //波形点数
            int32_t bytesPerPoint;      //每个波形点数占用几个字节
            int32_t waveType;           //波形类型
            int32_t startX;             //屏幕开始位置
            int32_t endX;               //屏幕结束为止
            double  sampRate;           //采样率
            int64_t xPos;               //x 触发位置
            int32_t yPos;               //y 零点位置
            int32_t probeType;          //探针类型
            double   probeRate;          //探针倍数
            double vScaleVal;           //垂直档位
            double timeScaleVal;        //水平档位
            double totalTime;
            double oneScreenTime;
            int32_t chIdx;              //通道
            double vPerPixVal;           //垂直方向每个像素代表值 幅值
            double adPix;
            double sampRateDisplay;
            int memDepth;
            int memDepthIdx;
            int probestr[3];
            int binType;
            int placeVal;
            int segmentNums;
            int segmentlen;
            int chNums;
            int idx;
            int vPerGridPixels;
        }header;
    }waveheader;
    int32_t wavedata[WAVE_MAX_LEN/4];
};
struct BMP_T{
    int32_t perPixelByte;
    int32_t xOffset;
    int32_t yOffset;
    int32_t width;
    int32_t height;
    uint32_t foregroundColor;
    uint32_t backgroundColor;
    double vPerPixVal;           //垂直档位
    double timeScaleVal;        //水平档位
    int32_t x1;
    int32_t x2;
    double x1Val;
    double x2Val;
    int8_t x1Valid;
    int8_t x2Valid;
    int16_t r;      //保留
    int32_t startX;
    int32_t endX;

    int32_t w;
    uint32_t * buffer;
};
struct XYBMP_T{
    int32_t perPixelByte;
    int32_t x;
    int32_t y;
    int32_t width;
    int32_t height;
    uint32_t foregroundColor;
    uint32_t backgroundColor;
    uint32_t brightness;
    int32_t flags;
    double  xDstVScaleVal;
    double  yDstVScaleVal;
    uint32_t cctEnable;
    uint32_t adMaxVal;
    uint32_t * buffer;

};
#pragma pack(pop)// 恢复对齐状态

class XWaveData{
public:
    XWaveData();
    XWaveData(WAVE_T *pWave);

    int getMagicNum();

    int getVersion();

    void setWaveLength(int waveLength);

    int getWaveLength();

    int getBytesPerPoint();

    void setWaveType(int waveType);
    int getWaveType();

    int getStartX();

    int getEndX();

    double getSampRate();

    int64_t getXPos();

    int getYPos();

    int getProbeType();

    double getProbeRate();

    void setVScaleVal(double val);

    double getVScaleVal();

    double getTimeScaleVal();

    double getTotalTime();

    double getOneScreenTime();

    int getChIdx();

    double getVerticalPerPix();

    double getAdPix();

    double getWaveFactor();

    int32_t * getWaveData();
    int getWaveData(int idx);
private:

    WAVE_T::WAVEHEADER::HEADER * getHeader();
    WAVE_T * pWave;
};
//-------------------------------------------------
class XBmp{
public:
    XBmp(BMP_T * pBmp);
    int32_t getPerPixelByte();
    int32_t getWidth();
    int32_t getHeight();
    uint32_t getForegroundColor();
    uint32_t getBackgroundColor();
    int32_t getXOffset();
    int32_t getYOffset();
    double getVerticalPerPix();
    double getTimeScaleVal();

    int32_t getX1();
    int32_t getX2();
    void setX1Val(double x1Val);
    void setX2Val(double x2Val);
    void setX1Valid(int8_t val);
    void setX2Valid(int8_t val);

    int32_t getStartX();
    int32_t getEndX();

    uint32_t * getBuffer();
    void setW(int32_t w);
    int32_t getW();
private:
    BMP_T * pBmp;
};
//----------------------------------------------------
class XYBmp{
public:
    XYBmp(XYBMP_T * pXYBmp);
    int32_t getPerPixelByte();
    int32_t getWidth();
    int32_t getHeight();
    uint32_t getForegroundColor();
    uint32_t getBackgroundColor();
    uint32_t getBrightness();
    int32_t getX();
    int32_t getY();

    bool isRun();
    double getXDstVScaleVal();
    double getYDstVScaleVal();

    uint32_t cctEnable();
    uint32_t getAdMaxVal();
    uint32_t * getBuffer();
private:
    XYBMP_T * pXYBmp;
};
#endif //TBOOKSCOPE_WAVEDATA_H
