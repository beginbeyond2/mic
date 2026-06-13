//
// Created by zhuzh on 2018-7-6.
//
#include <memory.h>
#include <sys/types.h>
#include <cstdio>
#include "jni.h"
#include "convert_2_35.h"
#include "Property.h"
#include "micsigcrypto.h"
#include "../../../../scope/src/main/cpp/Logger.h"
#define TAG "PROPERTY"
static short GetCheckByCRC(void* Data,int Long)
{
    unsigned char CRC16Lo, CRC16Hi;
    unsigned char CL, CH;                    //check digit = A001
    unsigned char SaveHi, SaveLo;
    int i=0;
    int Flag= 0;
    unsigned char *vol=(unsigned char *)Data;

    CRC16Lo = 255;
    CRC16Hi = 255;
    CL = 1;
    CH = 160;
    for(i = 0; i<Long; i++)
    {
        unsigned char temp=*vol;
        CRC16Lo ^=temp;

        for(Flag = 0; Flag <= 7;Flag ++)
        {
            SaveHi = CRC16Hi;
            SaveLo = CRC16Lo;
            CRC16Hi=CRC16Hi>>1;
            CRC16Lo=CRC16Lo>>1;
            if((SaveHi & 1) == 1)
                CRC16Lo |= 128;
            if((SaveLo & 1) == 1)
            {
                CRC16Hi ^= CH;
                CRC16Lo ^= CL;
            }
        }
        vol++;
    }

    return (short)((short)CRC16Hi*256+CRC16Lo);
}
Property::Property(){
    pModel = new CONFIG_MODEL;

    pMF = (CONFIG_MODEL_FUN *)pModel->fun;
    init();
}
void Property::init(){
    memset(pModel,0,sizeof(*pModel));
    szUUID[0] = 0;
    szPrivateUUID[0] = 0;
}

Property::~Property(){
    if(pModel){
        delete pModel;
        pModel = NULL;
    }
}
uint8_t  * Property::getData()
{
    return (uint8_t*)pModel;
}
int Property::getLength()
{
    return sizeof(*pModel);
}
bool Property::isVaild(){

    int len = sizeof(*pModel);
    short crc = GetCheckByCRC(getData() + sizeof(short),len - sizeof(short));
    int n = strlen(szUUID);
    int m = strlen(pModel->type.logname);
    if(n < m){
        n = 0;
        m = m - n;
    }else{
        n = n - m;
        m = 0;
    }

    if((pModel->type.Len == len) && (pModel->type.crc16 == crc)
            && ((strcmp(szUUID+n,pModel->type.logname+m) == 0))){
        return true;
    }
    return false;
}
void Property::setUUID(char * szUUID)
{
    stpcpy(this->szUUID,szUUID);
}

char * Property::getUUID()
{
    return szUUID;
}

char * Property::getPrivateUUID() {

    int len = strlen(szUUID);
    unsigned char pOutbuf[4096];
    memset(pOutbuf, 0, 4096);
    IMicsigCrypto::Instance()->DefaultKey();
    IMicsigCrypto::Instance()->Encrypt((unsigned char *) szUUID, len,
                                       pOutbuf, 4096);
    memset(szPrivateUUID,0,128);
    for (int i = 0; i < len; i++) {
        sprintf(szPrivateUUID + i * 2, "%02x", pOutbuf[i]);
    }
    return szPrivateUUID;
}

void Property::calcCrc(){

    int len = getLength();
    pModel->type.Len = len;
    pModel->SNLen = strlen(pModel->SN);
    pModel->DisplaySNLen = strlen(pModel->DisplaySN);
    pModel->mlen = strlen(pModel->model);
    pModel->flen = sizeof(CONFIG_MODEL_FUN);
    pModel->dlen = strlen(pModel->datestr);
    pModel->OEMLen = strlen(pModel->OEMName);
    pModel->iHWVerLen = strlen(pModel->HWVersion);
    strcpy(pModel->type.logname,szUUID);
    pModel->type.crc16 = GetCheckByCRC(getData() + sizeof(short),len - sizeof(short));

}
char* Property::getSN()
{
    return pModel->SN;
}
char* Property::getDisplaySN()
{
    return pModel->DisplaySN;
}
char* Property::getType()
{
    return pModel->model;
}
char* Property::getDeliveryDate()
{
    return pModel->datestr;
}
char* Property::getOemName()
{
    return pModel->OEMName;
}
char * Property::getHwVersion()
{
    return pModel->HWVersion;
}
int Property::getBandWidth()
{
    return pModel->iBandWidth;
}
void Property::setBandWidth(int bandWidth)
{
    pModel->iBandWidth = bandWidth;
}
static int BandWidthTable[16] =
{
        0,
        100,
        200,
        300,
        350,
        500,
        750,
        1000,
        70,
        150,
        250,
        600,
        650,
        700,
        1500,
        2000
};
static int ZunDepthTable[16] =
{
        0,
        3600000,//3.6M
        18000000,//18M
        90000000,//90M
        14000000,//14M
        28000000,//28M
        2800000,//2.8M
        28000,//28K
        70000000,   //70M
        140000000,  //140M
        0,
        0,
        0,
        0,
        0,
        0
};
int Property::getMemDepth()
{
    int idx = (pMF->fun[0].data[0] >> 8) & 0xF;
    return ZunDepthTable[idx];
}
static int HighRefreshTable[16] =
{
        0,
        0,
        0,
        80000,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0
};

int Property::getHighRefresh()
{
    int idx = (pMF->fun[0].data[0] >> 16) & 0xF;
    return HighRefreshTable[idx];
}
bool Property::IsDeliveryDate()//出货
{
    return (pModel->dlen > 0);
}

bool Property::IsEnableFreqCounter() //频率计
{
    return (pMF->fun[0].data[0] & (1<<20)) != 0;
}

bool Property::IsEnableHdmi()    //hdmi
{
    return (pMF->fun[0].data[0] & (1<<21)) != 0;
}

bool Property::IsEnable500uV()  //500V
{
    return (pMF->fun[0].data[0] & (1<<22)) != 0;
}

bool Property::IsEnableAutoV() //自动量程
{
    return (pMF->fun[0].data[0] & (1<<23)) != 0;
}

bool Property::IsEnableWlan()
{
    return (pMF->fun[0].data[0] & (1<<28)) != 0;

}



bool Property::IsHighLowPassFilter()
{
    return (pMF->fun[0].data[0] & (1<<29)) != 0;
}


bool Property::IsEnableCar()
{
    return (pMF->fun[0].data[0] & (1<<30)) != 0;
}

bool Property::IsKeyCursorEnable()
{
    return (pMF->fun[0].data[0] & (1<<31)) != 0;
}

bool Property::IsEnableSerial(_OPTION_PARTS serial)
{
    switch(serial){
        case OPPA_SERIAL_UART:{

            return (pMF->fun[0].data[1] & 0x01) != 0;

        }

        case OPPA_SERIAL_LIN:{

            return (pMF->fun[0].data[1] & 0x02) != 0;

        }

        case OPPA_SERIAL_SPI:{

            return (pMF->fun[0].data[1] & 0x04) != 0;

        }

        case OPPA_SERIAL_CAN:{

            return (pMF->fun[0].data[1] & 0x08) != 0;

        }

        case OPPA_SERIAL_I2C:{

            return (pMF->fun[0].data[1] & 0x10) != 0;

        }

        case OPPA_SERIAL_1553B:{

            return (pMF->fun[0].data[1] & 0x20) != 0;

        }

        case OPPA_SERIAL_429:{

            return (pMF->fun[0].data[1] & 0x40) != 0;

        }
        case OPPA_SERIAL_CANFD:{
            return (pMF->fun[0].data[1] & 0x80) != 0;
        }

        default:
            return false;


    }
}



static int WarrantyTable[16] =
{
        0,
        0,
        0,
        0,
        4,
        5,
        6,
        7,
        8,
        9,
        10,
        0,
        0,
        0,
        0,
        0
};

int  Property::getWarrantyDate() //保修事件
{
    int idx = (pMF->fun[0].data[0] >> 24) & 0xF;
    return WarrantyTable[idx];
}

bool Property::IsLanguage(_LANGUAGE_t lang) // 语言
{
    if(lang < LANGUAGE_MAX)
    {
        int idx = lang - LANGUAGE_en_US + 4;
        int idx1 = idx / 32;
        idx = idx % 32;
        return (pMF->fun[4].data[idx1] & (1<<idx)) != 0;
    }
    else
    {
        return false;
    }
}
static void ParserFunction_0(CONFIG_FUN *dfun,CONFIG_FUN*sfun)
{
    int a0 = sfun->data[0] & 0xF;//类型
    int a1 = (sfun->data[0] >> 4) & 0xF;//带宽
    int a2 = (sfun->data[0] >> 8) & 0xF;//存储深度
    int a3 = (sfun->data[0] >> 12) & 0xF;//采样率
    int a4 = (sfun->data[0] >> 16) & 0xF;//刷新率
    int a5 = (sfun->data[0] >> 24) & 0xF;//保修
    int b1 = (dfun->data[0] >> 4) & 0xF;//带宽
    int b2 = (dfun->data[0] >> 8) & 0xF;//存储深度
    int b3 = (dfun->data[0] >> 12) & 0xF;//采样率
    int b4 = (dfun->data[0] >> 16) & 0xF;//刷新率
    int b5 = (dfun->data[0] >> 24) & 0xF;//保修
    if(a1==0) a1 = b1;
    if(a2==0) a2 = b2;
    if(a3==0) a3 = b3;
    if(a4==0) a4 = b4;
    if(a5==0) a5 = b5;
    dfun->data[0] |= sfun->data[0];
    dfun->data[1] |= sfun->data[1];
    dfun->data[2] |= sfun->data[2];
    dfun->data[0] &= 0xF0F00000;
    dfun->data[0] |= a0 | (a1<<4) | (a2<<8) | (a3<<12) | (a4<<16) | (a5<<24);
    dfun->tz = 0;
    dfun->crc = 0;
}
static void ParserFunction_4(CONFIG_FUN *dfun,CONFIG_FUN*sfun)
{
    int a0 = sfun->data[0] & 0xF;//类型
    dfun->data[0] |= sfun->data[0];
    dfun->data[1] |= sfun->data[1];
    dfun->data[2] |= sfun->data[2];
    dfun->data[0] &= 0xFFFFFFF0;
    dfun->data[0] |= a0 | 1<<4 ;
    dfun->tz = 0;
    dfun->crc = 0;
}

bool Property::serialCodeUpgrade(char * serialCode,int serialCodeLength){

    bool bRes = false;
    char szBuffer[32] = {0};

    char szOutBuffer[32] = {0};
    int iLen = 16;
    StrTonum(serialCode,szBuffer);

    IMicsigCrypto::Instance()->DefaultKey();
    IMicsigCrypto::Instance()->Decrypt((unsigned char *)szBuffer,iLen,(unsigned char *)szOutBuffer,iLen);

    IMicsigCrypto::Instance()->SetKey(szUUID,strlen(szUUID));
    IMicsigCrypto::Instance()->Decrypt((unsigned char *)szOutBuffer,iLen,(unsigned char *)szBuffer,iLen);
    IMicsigCrypto::Instance()->DefaultKey();
    CONFIG_FUN *fun = (CONFIG_FUN *)szBuffer;
    u_int16_t crc = GetCheckByCRC(szBuffer,14);
    u_int16_t tz = fun->tz & 0xFFFF;
    u_int16_t fcrc = fun->crc & 0xFFFF;
    if(tz == 0xAA55
       &&  crc == fcrc)
    {
        ProcessFunction(fun);
        bRes = true;
    }
    return bRes;
}

void Property::ProcessFunction(CONFIG_FUN *fun)
{
    int type = fun->data[0] & 0xF;

    LOGD("type:%d\n",type);
    switch(type)
    {
        case 0://示波器
        {
            ParserFunction_0(&pMF->fun[0],fun);
            int idx = (fun->data[0] >> 4) & 0xF;
            if(idx != 0)
                setBandWidth(BandWidthTable[idx]);
        }
            break;
        case 4://语言
            ParserFunction_4(&pMF->fun[4],fun);
            break;
        case 5://修改设备型号
        {
            pMF->fun[5] = *fun;
            int len = (fun->data[0] >> 4) & 0xF;
            if(len > 0 && len < 12)
            {
                pModel->model[0] = (fun->data[0]>>8) & 0xFF;
                pModel->model[1] = (fun->data[0]>>16) & 0xFF;
                pModel->model[2] = (fun->data[0]>>24) & 0xFF;
                pModel->model[3] = (fun->data[1]>>0) & 0xFF;
                pModel->model[4] = (fun->data[1]>>8) & 0xFF;
                pModel->model[5] = (fun->data[1]>>16) & 0xFF;
                pModel->model[6] = (fun->data[1]>>24) & 0xFF;
                pModel->model[7] = (fun->data[2]>>0) & 0xFF;
                pModel->model[8] = (fun->data[2]>>8) & 0xFF;
                pModel->model[9] = (fun->data[2]>>16) & 0xFF;
                pModel->model[10] = (fun->data[2]>>24) & 0xFF;
                pModel->model[len] = '\0';
            }
            break;
        }
        default:
            return;
    }

}
bool Property::Clear()
{
    memset(pMF,0,sizeof(*pMF));
    setBandWidth(0);
    pModel->dlen = 0;
    pModel->datestr[0] = 0;
    return true;
}