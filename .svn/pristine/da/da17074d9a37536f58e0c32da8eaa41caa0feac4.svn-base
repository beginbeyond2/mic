//
// Created by zhuzh on 2018-4-12.
//

#include "MathFFT.h"
#include <math.h>
#include <memory.h>
#include "fftw3/include/fftw3.h"
#include "../Logger.h"

#define TAG "MathFFT"
std::mutex MathFFT::mtx;
static MathFFT * pMathFFT[] = {NULL,NULL,NULL,NULL,
                               NULL,NULL,NULL,NULL,};
MathFFT *MathFFT::Instane(int chIdx){
    if(chIdx >= 0 && chIdx < 8){
        if(pMathFFT[chIdx] == NULL){
            pMathFFT[chIdx] = new MathFFT;
            pMathFFT[chIdx]->setChIdx(chIdx);
        }
        return pMathFFT[chIdx];
    }
    return NULL;
}
#define TABLELEN    65536
#define pi          3.14159265
#define QX          10
#define FLOATTOFIX(x,Q)					((long long)((x)*(1<<(Q)))) //浮点数转定点数
#define FIXTOFLOAT(x,Q)					((double)((double)(x)/(1<<Q))) //定点数转浮点数
#define FFTLEN      400000
MathFFT::MathFFT()
{
    fftWindow = FW_RECTANGULAR;
    fftType = FFT_RMS;
    mNum = 0;
    pfftwfComplexIn = NULL;
    pfftwfComplex = NULL;
    bufLen = 0;
    fftLen = 0;
    fftPlan = NULL;
    vDCValue = 0.0;
    vMaxValue = 0.0;
    mMaxValueIdx = 0;
    vRatio = 1.0;
    vADValue = 1.0;
    TabCos = new int[TABLELEN];
    TabSin = new int[TABLELEN];
    memset(TabCos,0,sizeof(int)*TABLELEN);
    memset(TabSin,0,sizeof(int)*TABLELEN);
    CreatTabCos();
    CreatTabSin();
    InitBuf(FFTLEN);

}
void MathFFT::setChIdx(int idx){
    this->chIdx = idx;
}
void MathFFT::InitBuf(int len){
    if(len > bufLen){
        if(pfftwfComplexIn)
            fftwf_free(pfftwfComplexIn);
        if(pfftwfComplex)
            fftwf_free(pfftwfComplex);
        pfftwfComplexIn = (fftwf_complex *)fftwf_malloc(sizeof(fftwf_complex) * len);
        pfftwfComplex = (fftwf_complex *)fftwf_malloc(sizeof(fftwf_complex) * len);
        if(pfftwfComplex != NULL && pfftwfComplexIn != NULL){
            bufLen = len;
        }
    }
}
void MathFFT::InitFFt(int len){

    if(len > 0 && len != fftLen){
        mtx.lock();
        if(fftPlan != NULL){
            fftwf_destroy_plan(fftPlan);
            fftPlan = NULL;
        }
        InitBuf(len);
        fftPlan =  fftwf_plan_dft_1d(len, pfftwfComplexIn, pfftwfComplex,FFTW_FORWARD,FFTW_ESTIMATE);
        if(fftPlan != NULL){
            fftLen = len;
        }else{
            LOGE("%s,%d,chIdx:%d",__FUNCTION__ ,__LINE__,chIdx);
        }
        mtx.unlock();
    }

}
void MathFFT::CreatTabCos()
{
    int i;
    for(i = 0;i<TABLELEN;i++)
    {
        TabCos[i] = (int)(FLOATTOFIX(cos(2*pi*i/TABLELEN),QX));
    }
}


void MathFFT::CreatTabSin()
{
    int i;
    for(i = 0;i<TABLELEN;i++)
    {
        TabSin[i] = (int)(FLOATTOFIX(sin(2*pi*i/TABLELEN),QX));

    }
}
inline double MathFFT::COS(int u,int PointNum)
{
    return FIXTOFLOAT(TabCos[((TABLELEN*(long long)(u))/(PointNum))%TABLELEN],QX);
}
inline double MathFFT::SIN(int u,int PointNum)

{
    return FIXTOFLOAT(TabSin[((long long)(TABLELEN*(long long)(u))/(PointNum))%TABLELEN],QX);
}
void MathFFT::Rectangle(int *datain, int count)
{
    for(int i=0; i<count; i++)
    {
        pfftwfComplexIn[i][0] = (float)(datain[i]);
        pfftwfComplexIn[i][1] = 0;
    }
}
//海明窗
void MathFFT::Hamming(int* datain, int count)
{
    for(int i=0; i<count; i++)
    {
        pfftwfComplexIn[i][0] = (float)((0.54-0.46*COS(i,(count-1))) * datain[i]);
        pfftwfComplexIn[i][1] = 0;
    }
}
void MathFFT::Blackman(int *datain, int count)
{
    for(int i=0; i<count; i++)
    {
        pfftwfComplexIn[i][0] = (float)((0.42-0.5*COS(i,(count-1))+0.08*COS(i,(int)(0.5*(count-1)))) * datain[i]);
        pfftwfComplexIn[i][1] = 0;
    }
}
//汉宁窗
void MathFFT::Hann(int*datain,int count)//datain为输入数据，也是处理后的输出数据，count为FFT点数
{
    for(int i=0; i<count; i++)
    {
        pfftwfComplexIn[i][0] = (float)(0.5*(1-COS(i,(count-1)))*datain[i]);//cos(2*pi*i/(count-1)));
        pfftwfComplexIn[i][1] = 0;
    }
}
void MathFFT::AddFFTWindow(int *buf,int len,FFT_WINDOW fftWindow){
    switch(fftWindow){
        case FW_RECTANGULAR: //矩形窗
            Rectangle(buf,len);
            break;
        case FW_HAMMING: //汉明窗
            Hamming(buf,len);
            break;
        case FW_BLACKMAN_HARRIS: //布莱克曼窗
            Blackman(buf,len);
            break;
        case FW_HANNING: //汉宁窗
            Hann(buf,len);
            break;
        default:
            break;
    }
}
double  MathFFT::GetCoef(FFT_WINDOW fftWindow,FFT_TYPE fftType){
    double coef = 1.0;
    switch(fftWindow){
        case FW_RECTANGULAR: //矩形窗

            break;
        case FW_HAMMING: //汉明窗
            if(fftType == FFT_RMS) {
                coef = 1.852;//幅值恢复系数
            } else if(fftType == FFT_VDB) {
                coef = 1.586;//功率恢复系数
            }
            break;
        case FW_BLACKMAN_HARRIS: //布莱克曼窗
            if(fftType == FFT_RMS) {
                coef = 2.381;//幅值恢复系数
            } else if(fftType == FFT_VDB) {
                coef = 1.812;//功率恢复系数
            }
            break;
        case FW_HANNING: //汉宁窗
            if(fftType == FFT_RMS) {
                coef = 2.0;//幅值恢复系数
            } else if(fftType == FFT_VDB) {
                coef = 1.633;//功率恢复系数
            }
            break;
        default:
            break;
    }
    return coef;
}
int MathFFT::CalFFTPointNum(int length)
{
    int LenAfterAddZero = 0;
    LenAfterAddZero = length;
    if(length < 32)
        LenAfterAddZero = 32;
    else if(length & 0x01)
        LenAfterAddZero -= 1;//必须为偶数
    return LenAfterAddZero;
}

int MathFFT::CalcRMS(int *resbuf,int reslen){
    double PnDivSqrt2 = (double)(sqrt(2) * mNum);
    double res;

    int i = 0;
    int xcount = (mNum & 0x01) ? mNum/2+1 : mNum/2;
    i = 0;
    res = pfftwfComplex[i][0]*pfftwfComplex[i][0] + pfftwfComplex[i][1]*pfftwfComplex[i][1];
    res = (double)(sqrt(res)*mCoef/mNum*vRatio);
    vDCValue = res;
    resbuf[i] = (int)(res + 0.5);

    //
    i = 1;
    res = pfftwfComplex[i][0]*pfftwfComplex[i][0] + pfftwfComplex[i][1]*pfftwfComplex[i][1];
    res = (double)(sqrt(res) * mCoef * 2 / PnDivSqrt2 * vRatio);
    vMaxValue = res;
    mMaxValueIdx = i;
    resbuf[i] = (int)(res + 0.5);

    //
    for(i=2;i<xcount;i++){
        res = pfftwfComplex[i][0]*pfftwfComplex[i][0] + pfftwfComplex[i][1]*pfftwfComplex[i][1];
        res = (double)(sqrt(res) * mCoef * 2 / PnDivSqrt2 * vRatio);
        resbuf[i] = (int)(res + 0.5);
        if(vMaxValue < res){
            mMaxValueIdx = i;
            vMaxValue = res;
        }
    }
    return xcount;
}
#define FANG_DA_VAL (1e4)
int MathFFT::CalcVBD(int *resbuf,int reslen){
    double PnDivSqrt2 = (double)(sqrt(2)*mNum);
    double res;
    int i = 0;
    int xcount = (mNum & 0x01) ? mNum/2+1 : mNum/2;
    i = 0;
    res = pfftwfComplex[i][0]*pfftwfComplex[i][0] + pfftwfComplex[i][1]*pfftwfComplex[i][1];
    res = (double)(20.0*log10(vADValue*sqrt(res)*mCoef/mNum)*vRatio);
    vDCValue = res;
    resbuf[i] = (int)(res * FANG_DA_VAL);

    //
    i = 1;
    res = pfftwfComplex[i][0]*pfftwfComplex[i][0] + pfftwfComplex[i][1]*pfftwfComplex[i][1];
    res = (double)(20.0*log10(vADValue*sqrt(res) * mCoef*2/PnDivSqrt2)*vRatio);

    vMaxValue = res;
    mMaxValueIdx = i;
    resbuf[i] = (int)(res * FANG_DA_VAL);
    //
    for(i=2;i<xcount;i++){
        res = pfftwfComplex[i][0]*pfftwfComplex[i][0] + pfftwfComplex[i][1]*pfftwfComplex[i][1];
        res = (double)(20.0*log10(vADValue*sqrt(res) * mCoef*2/PnDivSqrt2)*vRatio);
        resbuf[i] = (int)(res * FANG_DA_VAL);
        if(vMaxValue < res){
            mMaxValueIdx = i;
            vMaxValue = res;
        }
    }
    return xcount;
}
void MathFFT::setVRatio(double val)
{
    this->vRatio = val;
}
void MathFFT::setVAd(double val)
{
    this->vADValue = val;
}
double MathFFT::getDCValue()
{
    return this->vDCValue;
}
int MathFFT::getMaxValIdx()
{
    return this->mMaxValueIdx;
}
double MathFFT::getMaxValue()
{
    return this->vMaxValue;
}
void MathFFT::setWindow(FFT_WINDOW fftWindow){
    this->fftWindow = fftWindow;
}
void MathFFT::setType(FFT_TYPE fftType){
    this->fftType = fftType;
}
int MathFFT::calcFFT(int *buf,int len)
{
    int i = 0;
    bool needFillZeros = false;
    mCoef = 1.0;
    mNum = CalFFTPointNum(len);
    if(mNum > len){
        needFillZeros = true;
        mCoef *=(double)mNum/len;
    }
    InitFFt(mNum);
    AddFFTWindow(buf,mNum,fftWindow);
    mCoef *= GetCoef(fftWindow,fftType);
    if(needFillZeros) {//补零算法
        //取样点数据
        //补足样点数据个数到2的n次幂，以0补入
        for(i= len; i<mNum; i++) {
            pfftwfComplexIn[i][0] = 0;
            pfftwfComplexIn[i][1] = 0;
        }
    } else {
//        for(i=0;i<mNum;i++) {
//            pfftwfComplexIn[i][0] = buf[i];
//            pfftwfComplexIn[i][1] = 0;
//        }
    }
    if(fftPlan != NULL){
        fftwf_execute(fftPlan);
    }
    return mNum/2+1;
}
int MathFFT::getFFT(int *buf, int len) {
    if(len >= mNum/2+1) {
        if(fftType == FFT_RMS){
            return CalcRMS(buf,len);
        }else if(fftType == FFT_VDB){
            return CalcVBD(buf,len);
        }
    }
    return 0;
}
bool MathFFT::CalcFFT(int chIdx,int fftType,int fftWindow,XWaveData *dst, XWaveData *src) {
    MathFFT * p = MathFFT::Instane(chIdx);
    if(p != NULL){
        p->setWindow((FFT_WINDOW)fftWindow);
        p->setType((FFT_TYPE)fftType);
        p->calcFFT(src->getWaveData(),src->getWaveLength());
        dst->setWaveLength( p->getFFT(dst->getWaveData(),WAVE_MAX_LEN/sizeof(int)) );
        return true;
    }
    return false;
}
