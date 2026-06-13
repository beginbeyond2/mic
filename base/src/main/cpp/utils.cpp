//
// Created by zhuzh on 2018-7-5.
//
#include <ctime>
#include "utils.h"

static short getCheckByCRC(void* Data,int Long)
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
JNIEXPORT jshort JNICALL Java_com_micsig_base_Utils_crc16
        (JNIEnv *env, jobject thisz, jbyteArray Data, jint idx, jint len){
    jbyte *buf = env->GetByteArrayElements(Data, 0);
    int Long = env->GetArrayLength(Data);
    if(idx < 0 || idx > Long-1)
        idx = 0;
    unsigned char *vol = (unsigned char *)&buf[idx];
    Long = Long-idx;
    if(Long > len && len != 0)
        Long = len;

    short crc=getCheckByCRC(vol, Long);
    env->ReleaseByteArrayElements(Data, buf, 0);

    return crc;
}


bool bInitSignal = false;
JNIEXPORT void JNICALL
Java_com_micsig_base_Utils_initSignal(JNIEnv *env, jclass clazz) {
    if(!bInitSignal){
        signal(SIGINT, SIG_IGN);
        bInitSignal = true;
    }
}
