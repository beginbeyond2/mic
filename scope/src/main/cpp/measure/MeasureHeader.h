//
// Created by zhuzh on 2018-4-16.
//

#ifndef TBOOKSCOPE_MEASUREHEADER_H
#define TBOOKSCOPE_MEASUREHEADER_H
#include <stdint.h>
#define MEASURE_HEADER_SIZE (16)

enum MEASURE_ITEM_TYPE{
    MEASURE_PERIOD = 0,
    MEASURE_FREQ = 1,
    MEASURE_RISETIME = 2,
    MEASURE_FALLTIME = 3,
    MEASURE_DELAY = 4,
    MEASURE_POSITIVE_DUTY = 5,
    MEASURE_NEGATIVE_DUTY = 6,
    MEASURE_POSITIVE_PULSE_WIDTH = 7,
    MEASURE_NEGATIVE_PULSE_WIDTH = 8,
    MEASURE_BURST_WIDTH = 9,
    MEASURE_POSITIVE_OVERSHOOT = 10,
    MEASURE_NEGATIVE_OVERSHOOT = 11,
    MEASURE_PHASE = 12,
    MEASURE_PK_PK = 13,
    MEASURE_AMPLITUDE = 14,
    MEASURE_HIGH = 15,
    MEASURE_LOW = 16,
    MEASURE_MAX = 17,
    MEASURE_MIN = 18,
    MEASURE_RMS = 19,
    MEASURE_CRMS = 20,
    MEASURE_MEAN = 21,
    MEASURE_CMEAN = 22,
    MEASURE_AC_RMS = 23,
    MEASURE_POSITIVE_RATE= 24,
    MEASURE_NEGATIVE_RATE = 25,
    MEASURE_TVALUE = 26,

    //中间空隙留扩展使用
    MEASURE_ITEM_MAX = 27,


    MEASURE_CURSOR_X1 = 35,
    MEASURE_CURSOR_X2 = 36,
    MEASURE_POSITIVE_UNDERSHOOT = 37,
    MEASURE_NEGATIVE_UNDERSHOOT = 38,
    MEASURE_COLV = 39,
    MEASURE_CLIPPING = 40,
    //第一个上升沿位置：选通区域最左侧=0，单位S[内部使用]，用于计算周期、
    // 频率、上升时间、延迟、相位等
    MEASURE_TIME_POT = 41,
    MEASURE_FIRST_RISE_EDGE = 42,
    //第一个下降沿位置，参见“第一个上升沿位置”
    MEASURE_FIRST_FALL_EDGE = 43,
    //第二个上升沿位置，
    MEASURE_SECON_RISE_EDGE = 44,
    //第二个下降沿位置
    MEASURE_SECON_FALL_EDGE = 45,
    //最后一个上升沿位置
    MEASURE_LAST_RISE_EDGE = 46,
    //最后一个下降沿位置
    MEASURE_LAST_FALL_EDGE = 47,
    MEASURE_ITEM_NUMS = 48
};


#define INDICATION_LEFT     (0)
#define INDICATION_TOP      (1)
#define INDICATION_RIGHT    (2)
#define INDICATION_BOTTOM   (3)

inline bool isMeasureItemValid(int itemIdx){
    return itemIdx >= MEASURE_PERIOD && itemIdx < MEASURE_ITEM_NUMS;
}
inline bool isIndicationValid(int idx){
    return idx >= INDICATION_LEFT && idx <= INDICATION_BOTTOM;
}

#pragma pack(push) //保存对齐状态
#pragma pack(1)
struct MEASURE_T{
    union MEASUREHEADER{
        int32_t data[MEASURE_HEADER_SIZE];
        //header 16 * 4  = 64 byte
        //0,1,2,3 chIdx 32bit 4 byte
        //4,5,6,7,8,9,10,11 enable 64bit 8 byte
        //12,13,14,15,16,17,18,19 valid 64bit 8 byte
        //20 phase relation to ch 1 byte
        //21 delay edge 1byte
        //22 delay relation to ch 1 byte
        //23 delay relation to ch edge 1 byte
        //24 col 2 byte
        //26 high 1 byte
        //27 middle 1 byte
        //28 low 1 byte
        //29 abs 1 byte
        //30 begin 2 byte
        //32 end 2 byte
        //34 h 2 byte
        //36 vrate 4 byte;
        //40 hrate 4 byte
        //44 pos 4 byte
        //48 vscale 4 byte
        //52 absHigh 4 byte
        //56 absMiddle 4 byte
        //60 absLow 4 byte
        struct HEADER{
            int32_t chIdx;
            uint64_t itemEnable;
            uint64_t itemValid;
            int8_t phaseRefCh;
            int8_t delayEdge;
            int8_t delayRefCh;
            int8_t delayRefChEdge;
            int16_t col;
            int8_t high;
            int8_t middle;
            int8_t low;
            int8_t abs;
            int16_t begin;
            int16_t end;
            int16_t h;
            float vrate;    //zoom
            float hrate;
            int32_t pos;
            float vscale;
            int32_t absHigh;
            int32_t absMiddle;
            int32_t absLow;
        }header;
    }measureheader;
    float meaureItemVal[MEASURE_ITEM_NUMS];
    float measreIndication[MEASURE_ITEM_NUMS][4]; //l,t,r,b

    float level;
    int num;

};
#pragma pack(pop)// 恢复对齐状态
#endif //TBOOKSCOPE_MEASUREHEADER_H
