//
// Created by zhuzh on 2018-4-12.
//

#ifndef TBOOKSCOPE_MATHFFT_H
#define TBOOKSCOPE_MATHFFT_H


#include "../wavedata.h"
#include "fftw3/include/fftw3.h"
#include <mutex>
class MathFFT {
public:
    enum FFT_WINDOW{
        FW_RECTANGULAR = 0, //矩形窗
        FW_HAMMING, //汉明窗
        FW_BLACKMAN_HARRIS, //布莱克曼窗
        FW_HANNING, //汉宁窗
    };
    enum FFT_TYPE
    {
        FFT_RMS = 0, //有效值
        FFT_VDB, //伏分贝
    };

    static bool CalcFFT(int chIdx,int fftType,int fftWindow,XWaveData *dstWave,XWaveData *src);

    static MathFFT *Instane(int chIdx);
    void setWindow(FFT_WINDOW fftWindow);
    void setType(FFT_TYPE fftType);
    void setVRatio(double val);
    void setVAd(double val);
    double getDCValue();
    int getMaxValIdx();
    double getMaxValue();
    int calcFFT(int *buf,int len);
    static int CalFFTPointNum(int length);
    int getFFT(int *buf,int len);
    void setChIdx(int idx);
private:
    MathFFT();
    void CreatTabCos();
    void CreatTabSin();
    void InitFFt(int len);
    void InitBuf(int len);
    inline double COS(int u,int PointNum);
    inline double SIN(int u,int PointNum);
    void Rectangle(int *datain, int count);
    void Hamming(int*datain, int count);
    void Blackman(int *datain, int count);
    void Hann(int*datain,int count);
    int CalcRMS(int *resbuf,int reslen);
    int CalcVBD(int *resbuf,int reslen);
    void AddFFTWindow(int *buf,int len,FFT_WINDOW fftWindow);
    double GetCoef(FFT_WINDOW fftWindow,FFT_TYPE fftType);

    int *TabSin;
    int *TabCos;

    fftwf_complex *pfftwfComplex;
    fftwf_complex *pfftwfComplexIn;
    //double *pIn;
    fftwf_plan fftPlan;
    int fftLen;
    int bufLen;
    double vDCValue;
    double vMaxValue;
    int   mMaxValueIdx;
    double vRatio;
    double vADValue;
    int mNum;
    double mCoef;
    FFT_WINDOW fftWindow;
    FFT_TYPE fftType;
    int chIdx;
    static std::mutex mtx;
};


#endif //TBOOKSCOPE_MATHFFT_H
