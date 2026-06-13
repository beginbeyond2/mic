//
// Created by zhuzh on 2018-4-16.
//

#ifndef TBOOKSCOPE_MEASURECALC_H
#define TBOOKSCOPE_MEASURECALC_H


#include "../wavedata.h"
#include "MeasureHeader.h"
#include "MeasureItem.h"

class MeasureCalc {
public:
    MeasureCalc(XWaveData *pWave,MEASURE_T * pMeasure);
    void Calc();
	bool CalcColV(int col, double & val);
    bool CalcCursor(int x,double &val);

    bool CalcTValue(float level,int num,double & pix);

	void CalcConcaveConvexVal();
	void setMeasureIndication(MEASURE_ITEM_TYPE itemType,float val);
	void setMeasureIndication(MEASURE_ITEM_TYPE itemType,int dir,float val);
	bool getMeasureItemVal(MEASURE_ITEM_TYPE itemType, double & val);
	int32_t WaveIdx2Pix(double idx);
	int32_t WaveIdx2Pix(int32_t idx);
	int32_t Vertical2Pix(double v);
	int32_t Vertical2Pix(int32_t v);

    double WaveIdx2PixEx(double idx);

    static void MeasureGetMaxMin(const int *buf,const int buf_len,int &Max,int &Min,int &maxIdx,int &minIdx,double &sum,double &square_sum);
    static void GetHigLowByHistogram(const int *buf,const int buf_len,int Max,int Min,int &high,int &low);
    void MeasureGetBustWidthAndSignalEdge(const int *buf, int buf_len,
                                                       int high, int low,
                                                       double &FirstRise, double &FirstFall,
                                                       double &SecRise, double &SecFall,
                                                       double &LstRise, double &LstFall,  double &BustWidth,
                                                       int &zeroNum1, int &zeroNum2);
    void MeasureGetBustWidthAndSignalEdge1(
                                                       const int *buf, int buf_len,
                                                       int high, int low,
                                                       double &FirstRise, double &FirstFall,
                                                       double &SecRise, double &SecFall,
                                                       double &LstRise, double &LstFall,  double &BustWidth,
                                                       int &zeroNum1, int &zeroNum2);
    int GetSignalEdge_c(const int *buf, int len
                                        , int high, int low
                                        , int *rise_stem, int *fall_stem
                                        , int *zeroNum1, int *zeroNum2);
    int GetSignalEdge_c_1(const int *buf, int len
                                         , int high, int low
                                         , double *rise_stem, double *fall_stem
                                         , int *zeroNum1, int *zeroNum2);
    static void MeasureGetPeriodAndFreq(double FirstRise,double FirstFall,
                                              double SecRise,double SecFall,
										double &Period,double &Freq);
    static void MeasureGetPeriodAndFreq(double rise1,double rise2,
										double fall1,double fall2,
										double zeroNum1,double zeroNum2,
										double &Period,double &Freq);

    static void MeasureGetRiseTime(const int *buf, const int len, \
                                             int high, int low, double firstEdge, \
                                             double secondEdge, double &RsTime,double &p1,double &p2);
    static void MeasureGetFallTime(const int *buf, const int len, \
                                             int high, int low, double firstEdge, \
                                             double secondEdge, double &FlTime,double &p1,double &p2);
    void MeasureGetFirstPsNgWidth(double FrRs, double ScRs,\
															double FrFl,double ScFl,\
															double &PsWidth,double &NgWidth);
    static void MeasureGetPostiveDutyCycle(double PsWidth,double Period,double &PsDtCycle);
    static void GetPosNavOverShoot(int max,int min,\
                                                    int hig,int low,\
										 			double &PosOverShoot,double &NavOverShoot);

    double GetMean(const int *buf, int buf_len);
    double GetRms(const int *buf, int buf_len);
    static bool GetCycleMean(const int *buf,int buf_len,double FrRs,double ScRs,double FrFl,double ScFl,double &CycleMean);
    static void GetCycleRms(const int *buf,int buf_len,double FrRs,double ScRs,double FrFl,double ScFl,double &Cyclerms);

    static void IMG_histogram(const int *buf,const int buf_len,int Max,int Min,int *Cache);
	static float GetValbyHistogram(const int *buf,const int buf_len,int Max,int Min);
    double GetPkPk();
    double GetAmplitude();

private:
	XWaveData * pWave;
	MEASURE_T * pMeasure;
	int maxVal;
	int minVal;
	int highVal;
	int lowVal;
	double upper;
	double lower;
	double middle;
	double sum;
	double square_sum;
	double FrRs;
	double ScRs;
	double FrFl;
	double ScFl;
	double LastRs;
	double LastFl;
	int zeroNum1;
	int zeroNum2;
	double burstWidth;
	double period;
	double freq;
	double positivePulseWidth;
	double negativePulseWidth;
	double timePot;
	double waveFactor;
	bool bPeriodVaild;
	int clipping;
	int maxIdx;
	int minIdx;

	int ConcaveVal;
	int ConvexVal;

	int32_t * pWaveData;
	int32_t wavelen;
	int32_t begin;
	int32_t end;
	int32_t cols;
};


#endif //TBOOKSCOPE_MEASURECALC_H
