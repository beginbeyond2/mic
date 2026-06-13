//
// Created by zhuzh on 2018-7-6.
//

#ifndef TBOOKSCOPE_PROPERTY_H
#define TBOOKSCOPE_PROPERTY_H
#include <stdint.h>
#pragma pack(push) //保存对齐状态
#pragma pack(1)



struct CONFIG_TYPE
{
    short crc16;
    int Len;
    char logname[128];
};
struct CONFIG_FUN
{
    int data[3];
    int tz:16;
    int crc:16;
};
struct CONFIG_MODEL_FUN
{
    int ver; //0x20151208
    CONFIG_FUN fun[16];
};

struct CONFIG_MODEL
{
    CONFIG_TYPE type;
    int SNLen;
    char SN[64];
    int DisplaySNLen;
    char DisplaySN[64];
    int  mlen;
    char model[128];//示波器型号
    int  flen;
    char fun[4096];//示波器扩展功能
    int dlen;
    char datestr[128];//日期
    int iBandWidth;   //带宽
    int OEMLen;             //oem字符串长度
    char OEMName[64];       //Oem长度
    int iHWVerLen;          //硬件版本长度
    char HWVersion[32];    //硬件版本号
    int iReserve[4096];     //扩展用
};
#pragma pack(pop)

enum _LANGUAGE_t
{
    LANGUAGE_en_US = 0,	//0 英语
    LANGUAGE_zh_CN,	//1 简体中文
    LANGUAGE_zh_TW,	//2 繁体中文
    LANGUAGE_de_DE,	//3 德语
    LANGUAGE_ru_RU,	//4 俄语
    LANGUAGE_es_ES,	//5 西班牙语
    LANGUAGE_ko_KR,	//6 朝鲜语
    LANGUAGE_cs_CZ,	//7 捷克语
    LANGUAGE_ar_AE,	//8 阿拉伯语
    LANGUAGE_it_CH,	//9 意大利语
    LANGUAGE_tr_TR,	//10 土耳其语
    LANGUAGE_fr_FR,	//11 法语
    LANGUAGE_MAX
};

enum _OPTION_PARTS {

   OPPA_SERIAL_UART = 0   //：串型总线
    , OPPA_SERIAL_LIN
    , OPPA_SERIAL_SPI
    , OPPA_SERIAL_CAN
    , OPPA_SERIAL_I2C
    , OPPA_SERIAL_1553B
    , OPPA_SERIAL_429
    , OPPA_SERIAL_CANFD
    , OPPA_SERIAL_MAX
};

class Property {
public:
    Property();
    virtual ~Property();

    void init();
    bool isVaild();
    uint8_t  * getData();
    char * getUUID();
    void setUUID(char * szUUID);
    char * getPrivateUUID();
    int getLength();
    void calcCrc();
    char* getSN();
    char* getDisplaySN();
    char* getType();
    char* getDeliveryDate();
    char* getOemName();
    char * getHwVersion();
    void setBandWidth(int bandWidth);
    int getBandWidth();
    void setMemDepth(int memDepth);
    int getMemDepth();
    void setHighRefresh(int highRefresh);
    int getHighRefresh();
    bool IsEnableFreqCounter(); //频率计
    bool IsDeliveryDate();
    bool IsEnableHdmi();
    bool IsEnableWlan();
    bool IsEnableAutoV();
    bool IsEnable500uV();

    bool IsHighLowPassFilter();
    bool IsEnableCar();
    bool IsKeyCursorEnable();
    bool IsEnableSerial(_OPTION_PARTS serial);
    int  getWarrantyDate(); //保修事件

    bool IsLanguage(_LANGUAGE_t lang); // 语言

    bool serialCodeUpgrade(char * serialCode,int serialCodeLength);

    bool Clear();

private:
    void ProcessFunction(CONFIG_FUN *fun);
    CONFIG_MODEL * pModel;
    CONFIG_MODEL_FUN *pMF;
    char szUUID[128];
    char szPrivateUUID[128];
};


#endif //TBOOKSCOPE_PROPERTY_H
