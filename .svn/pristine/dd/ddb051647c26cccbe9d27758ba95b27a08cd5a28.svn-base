//
// Created by zhuzh on 2018-4-16.
//
#include <math.h>
#include <memory.h>
#include <stdlib.h>
#include <cmath>
#include <vector>
#include "MeasureCalc.h"
#include "MeasureItem.h"
#include "../Logger.h"
#define TAG "MeasureCalc"
MeasureCalc::MeasureCalc(XWaveData *pWave,MEASURE_T * pMeasure){
    this->pWave = pWave;
    this->pMeasure = pMeasure;
    maxVal = 0;
    minVal = 0;
    highVal = 0;
    lowVal = 0;
    FrRs = -1;
    ScRs = -1;
    FrFl = -1;
    ScFl = -1;
    LastRs = -1;
    LastFl = -1;
    zeroNum1 = -1;
    zeroNum2 = -1;
    period = -1;
    freq = -1;
    timePot = 0;
    bPeriodVaild = false;
    waveFactor = pWave->getWaveFactor();

    begin = pMeasure->measureheader.header.begin;
    end = pMeasure->measureheader.header.end;
    pWaveData = pWave->getWaveData();
    wavelen = pWave->getWaveLength();
    int64_t len = wavelen;
    cols = pWave->getEndX() - pWave->getStartX() + 1;

    pMeasure->measureheader.header.hrate = (float )len/cols;

    if(begin <= pWave->getStartX()){
        begin = pWave->getStartX();
    }else{
        int n = (int)((begin - pWave->getStartX()) * len / cols);
        if(n > 0
            && n <= wavelen){
            pWaveData += n;
            wavelen -= n;
        }
    }
    if(end >= pWave->getEndX()){
        end = pWave->getEndX();
    }else{
        int n = (int)((pWave->getEndX() - end) * len / cols);
        if(n > 0
           && n <= wavelen) {
            wavelen -= n;
        }
    }
}
void MeasureCalc::MeasureGetMaxMin(const int *buf,const int buf_len,
                                   int &Max,int &Min,int &maxIdx,int &minIdx,
                                   double &sum,double &square_sum)
{
    int i = 0;
    sum = 0;
    square_sum = 0;
    if (buf_len > 0){
        Min = Max = buf[0];
        minIdx = maxIdx = 0;
    }else{
        Min = Max = 0;
        maxIdx = minIdx = -1;
    }
    for(i=0;i<buf_len;i++){
        if(buf[i]>Max){
            Max = buf[i];
            maxIdx = i;
        }else if(buf[i] < Min){
            Min = buf[i];
            minIdx = i;
        }
        sum += buf[i];
        square_sum += (double)buf[i] * buf[i];
    }

}

void MeasureCalc::IMG_histogram(const int *buf,const int buf_len,int Max,int Min,int *Cache)
{
    int i = 0;
    int N = Max-Min+1;
    memset(Cache, 0, N*sizeof(int));
    for(i=0;i<buf_len;i++)
    {
        int k = (int)buf[i]-Min;
        if(k >= 0 && k<N){
            Cache[k]++;
        }
    }
}

void MeasureCalc::GetHigLowByHistogram(const int *buf,const int buf_len,int Max,int Min,int &high,int &low)
{
    int N = Max-Min+1;
    if(N < 1 || N > 0xFFFF){
        high = Max;
        low = Min;
        return;
    }

    int *Cache = new int[N];
	if(Cache == NULL)
		return;

	IMG_histogram(buf,buf_len,Max,Min,Cache);


    int midle_h = (int)round((Max-Min)*0.6);
    int midle_l = (int)round((Max-Min)*0.4);
    int top = Max-Min;
    //计算中部最大概率
    int max_gai_midle = 0;
    for(int i=midle_l+1; i<midle_h; i++){
        if(max_gai_midle < Cache[i])
            max_gai_midle = Cache[i];
    }
    //计算上部最大概率
    double gai_pj1 = 0; //平均概率
    int gai_max1=0; //最大概率
    int place=0;
    for(int i=midle_h; i<top+1; i++){
        if(gai_max1 <= Cache[i]){
            gai_max1 = Cache[i];
            place = i;
        }
        gai_pj1 += Cache[i];
    }
    gai_pj1 /= top+1-midle_h;
    double temp1=gai_max1;
    if(gai_pj1 == 0) gai_pj1 = 0.0001;
    if(max_gai_midle == 0) max_gai_midle = 1;
    //计算高
    if((temp1/gai_pj1 > 5.0 &&  temp1/max_gai_midle > 2.0)){
        high = place+Min;
    }
    else {
        high = Max;
    }
    //计算下部最大概率
    double gai_pj2 = 0; //平均概率
    int gai_max2=0; //最大概率
    place=0;
    for(int i=0; i<midle_l+1; i++){
        if(gai_max2 < Cache[i]){
            gai_max2 = Cache[i];
            place = i;
        }
        gai_pj2 += Cache[i];
    }
    gai_pj2 /= midle_l+1;
    temp1=gai_max2;
    if(gai_pj2 == 0) gai_pj2 = 0.0001;
    if(max_gai_midle == 0) max_gai_midle = 1;
    //计算低
    if((temp1/gai_pj2 > 5.0 &&  temp1/max_gai_midle > 2.0)){
        low = place+Min;
    }
    else {
        low = Min;
    }
    delete [] Cache;

}

//返回值：高16位为有效上升沿（第一沿、第二沿、最后沿）个数
//        低16位为有效下降沿（第一沿、第二沿、最后沿）个数
//zeroNum1上升零点个数 zeroNum2下降零点个数
int MeasureCalc::GetSignalEdge_c(const int *buf, int len
        , int high, int low
        , int *rise_stem, int *fall_stem
        , int *zeroNum1, int *zeroNum2)
{
    int val50pct = this->middle;
    int val10pct = this->lower;
    int val90pct = this->upper;
    //有效边沿为：     第一边沿      第二边沿       最后边沿
    //上升存储位置：  rise_stem[0] rise_stem[1] rise_stem[2]
    //下降存储位置：  fall_stem[0] fall_stem[1] fall_stem[2]
    int pRise;//有效上升边沿存储位置
    int pFall;//有效下降边沿存储位置
    int i, /*m=1,*/ choice,choice2;
    int lastdata;//前一个数据
    //零点(幅值50%的点)判断标记：false 零点找到 true零点未找到
    int chance1=false;//上升沿零点命中标记
    int chance2=false;//下降沿零点命中标记
    //边沿判断标记：false查找零点阶段 true零点已找到，判断是否为有效边沿
    int inCheckRise = false;//上升沿判断标记
    int inCheckFall = false;//下降沿判断标记
    int old1 = 0, old2 = 0, upzero = 0, downzero = 0;
    bool have10pct_rise=0;
    bool have90pct_fall=0;

    pRise=0, pFall=0;
    lastdata = buf[0];
    for(i = 1; i<len; ++i)                                                                                           //查找前两个沿
    {
        //上升沿判断
        chance1= inCheckRise;
        //查找上升零点(幅值50%的点)
        choice = !inCheckRise  && lastdata<val50pct && buf[i]>= val50pct;
        if(choice)
        {//命中上升零点（找到幅度过50%的点位置），置标记，并记录位置
            chance1 = choice;
            old1 = i;
        }
        //过滤突变的数据
        //如果大于50%后(inCheckRise=1)，来一个大于90%的数据，将取值
        //choice = inCheckRise&&buf[i]>=val90pct;
        int riseCheckSucces = inCheckRise&&buf[i]>=val90pct;
        rise_stem[pRise+(1-riseCheckSucces)*3/*4*/] = old1;
        pRise += riseCheckSucces;//riseCheckSucces=1时有效边沿存储位置递增
        upzero += riseCheckSucces;//riseCheckSucces=1时上升零点递增
        //刷新上升零点判断标记，若上升零点命中后找到90%点则复位该标记，重找零点
        chance1 = chance1*(1-riseCheckSucces);
        //刷新上升零点判断标记，若上升零点命中后找到10%的点则复位标记，重找零点
        //choice = inCheckRise&&buf[i]<=val10pct;
        int riseCheckFail = inCheckRise&&buf[i]<=val10pct;
        chance1 = chance1*(1-riseCheckFail);
        if(pRise>2)//有效边沿只有3个，限定最后一个有效边沿的存储位置
            pRise = 2;
        inCheckRise = chance1;//刷新上升沿判断标记，上升零点找到后启动该上升沿判断
        if(!riseCheckSucces)
            have10pct_rise |= buf[i]<=val10pct;
        else
            have10pct_rise = 0;

        //下降沿判断
        chance2=inCheckFall;
        choice2 = !inCheckFall  && lastdata>val50pct && buf[i]<= val50pct;
        if(choice2)
        {
            chance2 =  choice2;
            old2 = i;
        }
        int fallCheckSucces = inCheckFall&&buf[i]<=val10pct;
        fall_stem[pFall+(1-fallCheckSucces)*3/*4*/] = old2;
        pFall += fallCheckSucces;
        downzero += fallCheckSucces;
        chance2 = chance2*(1-fallCheckSucces);
        choice2 = inCheckFall && buf[i]>=val90pct;
        chance2 = chance2*(1-choice2);
        if(pFall>2)
            pFall = 2;
        inCheckFall = chance2;
        if(!fallCheckSucces)
            have90pct_fall |= buf[i]>=val90pct;
        else
            have90pct_fall = 0;


        //更新前一个数据
        lastdata = buf[i];

        //最多只统计100个周期
        if(upzero > 100)
            break;
    }

    //最后一个边沿,严格检查，将原来的50%改成90%和10%，以去除末尾点是0的影响
    if(inCheckRise)
    {
        //if(buf[len -1]>val50pct)
        //if(buf[len-1]>val90pct)
        if(have10pct_rise)
        {
            rise_stem[pRise] = old1;
            upzero ++;
            if(pRise<2)
                pRise++;
            //printf("-----------upzero=%d\n",upzero);
        }
    }
    if(inCheckFall)
    {
        //if(buf[len -1]<val50pct)
        //if(buf[len-1]<val10pct)
        if(have90pct_fall)
        {
            fall_stem[pFall] = old2;
            downzero ++;
            if(pFall<2)
                pFall++;;
            //printf("-----------downzero=%d\n",downzero);
        }
    }
    *zeroNum1 = upzero;
    *zeroNum2 = downzero;
    return pFall +(pRise<<16);
}

#define CAL_ZERO(x) (x-(comp-buf[x])/(buf[x-1]-buf[x]))
//返回值：高16位为rise_stem的有效数据个数
//       低16位为fall_stem的有效数据个数
//zeroNum1上升零点个数 zeroNum2下降零点个数
int MeasureCalc::GetSignalEdge_c_1(const int *buf, int len
                                         , int high, int low
                                         , double *rise_stem, double *fall_stem
                                         , int *zeroNum1, int *zeroNum2)
{
    double comp = this->middle;
    int high1 = this->upper;
    int low1 = this->lower;
    
    for(int i=0; i<3; i++){
        rise_stem[i] = -1;
        fall_stem[i] = -1;
    }
    *zeroNum1 = -1;
    *zeroNum2 = -1;
    if(len < 10 || high1-comp < 2 || comp-low1 < 2)
        return 0;
    
    int posedge = 0;
    int negedge = 0;
    int up_place = -1;
    int down_place = -1;
    int lastEdge = -1;
    //找2个上升沿和2个下降沿
    for(int i=1; i<len; i++){
        int last = buf[i-1];
        int now = buf[i];
        if(last < comp && now >= comp)
            //刷新上升位置,以最后一个为准
            up_place = i;
        else if(last >= comp && now < comp)
            //刷新下降位置,以最后一个为准
            down_place = i;

        if(now > high1){
            //遇到high1位置就需要重新查找下降沿
            down_place = -1; //用于此for后的下一个步骤，确认下降沿虽然不用下降到low1，但也不能上升到high1；
            if(lastEdge == -1 && up_place < 0) //如果信号从大于high1开始，则先检测下降沿;
                lastEdge = 0x01; //第一个是下降沿
        }
        else if(now < low1){
            //遇到low1位置就需要重新查找上升沿
            up_place = -1; //用于此for后的下一个步骤，确认上升沿虽然不用上升到high1，但也不能下降爱你个下降到low1；
            if(lastEdge == -1 && down_place < 0) //如果信号从大于low1开始，则先检测上升沿;
                lastEdge = 0x10; //第一个是上升沿
        }

        if(lastEdge == -1){
            //确认第一个沿
            if(up_place > 0 && now > high1){
                //确认上升零点
                //printf("1. get rise zero\n");
                lastEdge = 0x01;
                rise_stem[posedge++] = CAL_ZERO(up_place);
            }
            else if(down_place > 0 && now < low1){
                //确认下降零点
                //printf("1. get down zero\n");
                lastEdge = 0x10;
                fall_stem[negedge++] = CAL_ZERO(down_place);
            }
        }
        else if(lastEdge == 0x01){
            //上一次是上升沿，这次需要找下降沿
            if(now < low1){
                //确认下降零点
                //printf("2. get down zero\n");
                lastEdge = 0x10;
                fall_stem[negedge++] = CAL_ZERO(down_place);
            }
        }
        else {
            //上一次是下降沿，这次需要找上升沿
            if(now > high1){
                //确认上升零点
                //printf("2. get rise zero\n");
                lastEdge = 0x01;
                rise_stem[posedge++] = CAL_ZERO(up_place);
            }
        }

        if(posedge == 2 && negedge == 2)
            break;
    }

    if(lastEdge == -1){
        //一个沿都没有找到
        return 0;
    }
    else if(lastEdge == 0x01 && negedge < 2){
        //最后一个下降沿不用降到low1
        if(down_place > 0) { //有下降沿
            fall_stem[negedge++] = CAL_ZERO(down_place);
            //printf("3. get down zero\n");
        }
        *zeroNum1 = posedge;
        *zeroNum2 = negedge;
        return negedge +(posedge<<16);
    }
    else if(lastEdge == 0x10 && posedge < 2){
        //最后一个上升沿不用升到high1
        if(up_place > 0) { //有上升沿
            rise_stem[posedge++] = CAL_ZERO(up_place);
            //printf("3. get rise zero\n");
        }
        *zeroNum1 = posedge;
        *zeroNum2 = negedge;
        return negedge +(posedge<<16);
    }

    //找最后一个上升沿和下降沿
    up_place = -1;
    down_place = -1;
    int up_place1 = -1; //用于第2记忆，防止方波时，沿上没有采集到数据
    int down_place1 = -1;
    lastEdge = -1;
    int step=0;
    for(int i=len-1; i>=1; i--){
        int last = buf[i-1];
        int now = buf[i];
        if(up_place < 0 && last < comp && now >= comp){
            //刷新上升位置,以第一个为准
            up_place1 = i;
            up_place = i;
        }
        else if(down_place < 0 && last >= comp && now < comp){
            //刷新下降位置,以第一个为准
            down_place1 = i;
            down_place = i;
        }

        if(now > high1){
            //遇到high1位置就需要重新查找上升沿
            up_place = -1;
            if(lastEdge == -1 && down_place < 0)
                lastEdge = 0x10; //最后一个是上升沿
        }
        else if(now < low1){
            //遇到low1位置就需要重新查找下降沿
            down_place = -1;
            if(lastEdge == -1 && up_place < 0)
                lastEdge = 0x01; //最后一个是下降沿
        }

        if(lastEdge == -1){
            //确认第一个沿
            if(up_place > 0 && now < low1){
                //确认上升零点
                lastEdge = 0x01;
                step++;
                rise_stem[posedge++] = CAL_ZERO(up_place);
                //printf("4. get rise zero\n");
            }
            else if(down_place > 0 && now > high1){
                //确认下降零点
                lastEdge = 0x10;
                step++;
                fall_stem[negedge++] = CAL_ZERO(down_place);
                //printf("4. get down zero\n");
            }
        }
        else if(lastEdge == 0x01){
            //上一次是上升沿，这次需要找下降沿
            if(now > high1){
                //确认下降零点
                //printf("5. get down zero\n");
                lastEdge = 0x10;
                if(down_place < 0) //方波，只在小于low1出现零点，然后就到high1了
                    down_place = down_place1;
                fall_stem[negedge++] = CAL_ZERO(down_place);
                if(++step > 1) break;
            }
        }
        else {
            //上一次是下降沿，这次需要找上升沿
            if(now < low1){
                //确认上升零点
                //printf("5. get rise zero\n");
                lastEdge = 0x01;
                if(up_place < 0) //方波，只在大于high1出现零点，然后就到low1了
                    up_place = up_place1;
                rise_stem[posedge++] = CAL_ZERO(up_place);
                if(++step > 1) break;
            }
        }
    }

    *zeroNum1 = posedge;
    *zeroNum2 = negedge;
    return negedge +(posedge<<16);
}

//first rise ：数据序列中的第一个上升沿50%点的位置
//first fall ：数据序列中的第一个下降沿50%点的位置
void MeasureCalc::MeasureGetBustWidthAndSignalEdge1(
               const int *buf, int buf_len,
               int high, int low,
               double &FirstRise, double &FirstFall,
               double &SecRise, double &SecFall,
               double &LstRise, double &LstFall,  double &BustWidth,
               int &zeroNum1, int &zeroNum2)
{
    double rise_stem[3];
    double fall_stem[3];

    GetSignalEdge_c_1(buf, buf_len, high, low, &rise_stem[0],
            &fall_stem[0], &zeroNum1, &zeroNum2);
    //补齐最后1个沿
    //printf("pos num=%d, neg num=%d\n", zeroNum1, zeroNum2);
    if(zeroNum1 < 3) //看有没有最后一个沿
        rise_stem[2] = rise_stem[1] < 0 ? rise_stem[0] : rise_stem[1];
    if(zeroNum2 < 3)
        fall_stem[2] = fall_stem[1] < 0 ? fall_stem[0] : fall_stem[1];

    FirstRise = rise_stem[0];
    SecRise = rise_stem[1];
    LstRise = rise_stem[2];
    //printf("posedge place=%f, %f, %f; ", FirstRise,SecRise,LstRise);

    FirstFall = fall_stem[0];
    SecFall = fall_stem[1];
    LstFall = fall_stem[2];
    //printf("\t negedge place=%f, %f, %f\n", FirstFall,SecFall,LstFall);

    //计算突发脉宽
    BustWidth = -1;
    if(FirstRise >= 0 && FirstRise < FirstFall){
        //先来上升沿
        if(LstFall > LstRise) //最后一个是下降沿
            BustWidth = LstFall-FirstRise;
        else if(LstRise > FirstFall) //最后一个是上升沿
            BustWidth = LstRise-FirstRise;
    }
    else if(FirstFall >= 0 && FirstFall < FirstRise){
        //先来下降沿
        if(LstRise > LstFall) //最后一个是上升沿
            BustWidth = LstRise-FirstFall;
        else if(LstFall > FirstRise) //最后一个是下降沿
            BustWidth = LstFall-FirstFall;
    }
}

void MeasureCalc::MeasureGetBustWidthAndSignalEdge(const int *buf, int buf_len,
                                                           int high, int low,
                                                           double &FirstRise, double &FirstFall,
                                                           double &SecRise, double &SecFall,
                                                           double &LstRise, double &LstFall,  double &BustWidth,
                                                           int &zeroNum1, int &zeroNum2)


{
    double rise[3], fall[3];
    int val50pct = this->middle;
    int pRise, pFall;
    int i, chance2= false,m;
    double temp2 = -1;
    int num_rise,num_fall;
    int rise_stem[15];
    int fall_stem[10];
    for(i = 0; i<3; ++i)
    {
        rise[i] = fall[i] = -1;
        rise_stem[i] = fall_stem[i] = -1;
        rise_stem[i+3] = fall_stem[i+3] = -1;                                                                       //本数据域-1表示该数据缺失
    }
    chance2 = GetSignalEdge_c(buf, buf_len,  high,low, &rise_stem[0], &fall_stem[0],&zeroNum1,&zeroNum2);

    pRise=chance2 >>16;
    pFall=chance2&0x0ffff;
    //检查有效沿位置是否有效，修正存储位置参数
    if(rise_stem[2]==-1)
        pRise=1;
    if(fall_stem[2]==-1)
        pFall=1;
    if(rise_stem[1]==-1)
        pRise=0;
    if(fall_stem[1]==-1)
        pFall=0;

    //上升：对首沿和次沿的位置添加舍入计算的尾值
    if(pRise>1)
        num_rise = pRise-1;
    else
        num_rise = pRise;
    if(num_rise==1)//独立出来做除法。
    {
        m = rise_stem[1] ;
        if(m > 0)
        {
            if(buf[m] - buf[m-1] != 0)
                rise[1] = 1.0f * (val50pct-buf[m-1])/(buf[m] - buf[m-1]) + m - 1;
            else
                rise[1] = m-1;
        }
        num_rise--;
    }
    if(num_rise==0)
    {
        m = rise_stem[0] ;
        if(m > 0)
        {
            if(buf[m] - buf[m-1] != 0)
                rise[0] =1.0f * (val50pct-buf[m-1])/(buf[m] - buf[m-1]) + m - 1;
            else
                rise[0] = m-1;
        }
    }
    //下降：对首沿和次沿的位置添加舍入计算的尾值
    if(pFall>1)
        num_fall = pFall-1 ;
    else
        num_fall = pFall ;
    if(num_fall==1)
    {
        m = fall_stem[1] ;
        if(m > 0)
        {
            if(buf[m] - buf[m-1] != 0)
                fall[1] = 1.0f * (val50pct-buf[m-1])/(buf[m] - buf[m-1]) + m - 1;
            else
                fall[1] = m-1;
        }
        num_fall--;
    }
    if(num_fall==0)
    {
        m = fall_stem[0] ;
        if(m > 0)
        {
            if(buf[m] - buf[m-1] != 0)
                fall[0] = 1.0f * (val50pct-buf[m-1])/(buf[m] - buf[m-1]) + m - 1;
            else
                fall[0] = m-1;
        }
    }

    //补足尾沿的位置
    if(pRise==1)
    {
        rise_stem[2] = rise_stem[1];
        pRise = 2;
    }
    if(pFall==1)
    {
        fall_stem[2] = fall_stem[1];
        pFall = 2;
    }
    if(pRise==0)
    {
        rise_stem[2] = rise_stem[0];
        pRise = 2;
    }
    if(pFall==0)
    {
        fall_stem[2] = fall_stem[0];
        pFall = 2;
    }
    //对尾沿的位置添加舍入计算的尾值
    num_rise = pRise;
    if(pRise==2)
    {
        m = rise_stem[2];
        if(m > 0)
        {
            if(buf[m] - buf[m-1] != 0)
                rise[2] = 1.0f * (val50pct-buf[m-1])/(buf[m] - buf[m-1]) + m - 1;
            else
                rise[2] = m-1;
        }
    }
    num_fall = pFall;
    if(pFall==2)
    {
        m = fall_stem[2] ;
        if(m > 0)
        {
            if(buf[m] - buf[m-1] != 0)
                fall[2] = 1.0f * (val50pct-buf[m-1])/(buf[m] - buf[m-1]) + m - 1;
            else
                fall[2] = m-1;
        }
    }
    //边沿位置值输出到外部
    FirstFall =fall[0];
    SecFall = fall[1];
    LstFall = fall[2];
    FirstRise =rise[0];
    SecRise = rise[1];
    LstRise = rise[2];
    //借用pRise和pFall计算整个屏幕的周期脉冲宽度
    //pRise为首沿位置，首上升沿在前取首上升沿，首下降沿在前取首下降沿
    //pFall为尾沿位置，尾上升沿在后区尾上升沿，尾下降沿在后取尾下降沿
    pRise = (rise[0]-fall[0]<0) ?                                                                     //计算突发脉冲宽度
            rise[0] : fall[0];
    pFall = (rise[2]>fall[2]) ?
            rise[2] : fall[2];
    //有首沿和尾沿，计算周期波形脉宽
    if(pRise - pFall!=0)
        temp2 = pFall - pRise;
    //无首沿或尾沿，非周期波，无脉宽
    if(rise[0] == -1 || fall[0] == -1)
        temp2 = -1;
    //脉宽值输出到外部
    BustWidth = temp2;
    return;
}

void MeasureCalc::MeasureGetPeriodAndFreq(double rise1,double rise2,
                                          double fall1,double fall2,
                                          double zeroNum1,double zeroNum2,
                                          double &Period,double &Freq)
{
    double tmp(-1), tmp1(-1), tmp2(-1);

    if(rise1 > 0 && rise2 > 0 && fall1 > 0 && rise1<fall1)
    {
        tmp1 = (rise2 - rise1)/zeroNum1;
        if(fall2 > 0)
        {
            if(zeroNum2 > 0)
            {
                tmp2 = (fall2 - fall1)/zeroNum2;
                tmp = (tmp2<tmp1) ? tmp2 : tmp1;
                ///1、周期误差超过最小的周期值的10%既认为周期计算有误
                ///2、周期误差低于最小的周期值的10%则认为正确，取上下周期平均值
                if(fabs((tmp1-tmp2)*1.0/tmp)>0.1) tmp = -1;
                else tmp = (tmp2+tmp1) / 2;
            }
        }
    }
    else if(fall1 > 0 && fall2 > 0 && rise1 > 0 && fall1<rise1)
    {
        if(zeroNum2 > 0)
        {
            tmp1 = (fall2 - fall1)/zeroNum2;
            if(rise2 > 0)
            {
                tmp2 = (rise2 - rise1)/zeroNum1;
                tmp = (tmp2<tmp1) ? tmp2 : tmp1;
                ///1、周期误差超过最小的周期值的10%既认为周期计算有误
                ///2、周期误差低于最小的周期值的10%则认为正确，取上下周期平均值
                if(fabs((tmp1-tmp2)*1.0/tmp)>0.1) tmp = -1;
                else tmp = (tmp2+tmp1) / 2;
            }
        }
        else
        {
            tmp = -1;
        }
    }
    else
    {
        tmp = -1;
    }
    Period = tmp;
    Freq = 1/tmp;
}

void MeasureCalc::MeasureGetPeriodAndFreq(double FirstRise,double FirstFall,
                                                  double SecRise,double SecFall,
                                                  double &Period,double &Freq)
{

    double ret = -1;
    if(FirstRise >= 0 && SecRise > 0 && FirstRise < FirstFall)
    {
        ret = SecRise - FirstRise;
    }
    else if(FirstFall >= 0 && SecFall > 0 && FirstFall<FirstRise)
    {
        ret = SecFall - FirstFall;
    }

    Period = ret;
    Freq = 1.0/Period;
}

void MeasureCalc::MeasureGetRiseTime(const int *buf, const int len, \
                                             int high, int low, double firstEdge, \
                                             double secondEdge, double &RsTime,double &p1,double &p2)
{

//    int val10pct = (int)(low + (high - low)*0.1);
//    int val90pct = (int)(low + (high - low)*0.9);

    int val10pct = low;
    int val90pct = high;
    double edges[2];
    double pos1, pos2;
    int i, j,k;
    edges[0] = firstEdge;
    edges[1] = secondEdge;
    int v = 0;

    k = 0;
    for(j = 0; j<2; ++j)
    {
        k = 0;

        if((int)edges[j]<1)
        {
            continue;
        }
        pos1 = pos2 = -1;
        for(i = (int)ceil(edges[j]); i>0 && i<len; i--)                                                //寻找10%点
        {
            if(buf[i-1]<val10pct && buf[i]>=val10pct)
            {
                k = i;
                v = buf[i] - buf[i-1];
                if( v != 0)
                {
                    pos1 = 1.0*(val10pct - buf[i-1])/(v) + i - 1;
                }
                else
                    pos1 = i-1;
                break;
            }
        }
        if(-1 == pos1) continue;
        for(i = (int)(floor(edges[j])); i>0 && i<len; i++)                                              //寻找90%点
        {
            if(buf[i-1]<val90pct && buf[i]>= val90pct)
            {
                k = i-k;
                v = buf[i] - buf[i-1];
                if(v != 0)
                {
                    pos2 = 1.0*(val90pct - buf[i-1])/(v) + i - 1;
                }
                else
                    pos2 = i-1;
                break;
            }
        }
        if(-1 == pos2) continue;
        if(abs(k) < 2) continue;
        RsTime = (0 == pos2-pos1)? 1 : pos2-pos1;
        p1 = pos1;
        p2 = pos2;
        return;
    }
    RsTime = -1;
    p1 = p2 = -1;

}

void MeasureCalc::MeasureGetFallTime(const int *buf, const int len, \
                                             int high, int low, double firstEdge, \
                                             double secondEdge, double &FlTime,double &p1,double &p2)
{

//    int val10pct = (int)(low + (high - low)*0.1);
//    int val90pct = (int)(low + (high - low)*0.9);
    int val10pct = low;
    int val90pct = high;
    double edges[2];
    double pos1, pos2;
    int i, j,k;

    edges[0] = firstEdge;
    edges[1] = secondEdge;

    k = 0;
    for(j = 0; j<2; ++j)
    {
        k = 0;
        if(-1 == edges[j]) continue;
        if(0 == edges[j]) continue;
        pos1 = pos2 = -1;
        for(i = (int)(ceil(edges[j])); i>0 && i < len; i--)                                                //寻找10%点
        {
            if(buf[i-1]>val90pct && buf[i]<=val90pct)
            {
                k = i;
                if(buf[i] - buf[i-1] != 0)
                    pos1 = 1.0*(val90pct - buf[i-1])/(buf[i]-buf[i-1]) + i - 1;
                else
                    pos1 = i-1;
                break;
            }
        }
        if(-1 == pos1) continue;
        for(i = (int)(floor(edges[j])); i>0 && i < len; i++)                                              //寻找90%点
        {
            if(buf[i-1]>val10pct && buf[i]<= val10pct)
            {
                k = i-k;
                if(buf[i] - buf[i-1] != 0)
                    pos2 = 1.0*(val10pct - buf[i-1])/(buf[i]-buf[i-1]) + i - 1;
                else
                    pos2 = i-1;
                break;
            }
        }
        if(-1 == pos2) continue;
        if(abs(k) < 2) continue;
        FlTime = (0 == pos2-pos1)? 1 : pos2-pos1;
        p1 = pos1;
        p2 = pos2;
        return;
    }
    FlTime = -1;
    p1 = p2 = -1;
    return;

}
void MeasureCalc::MeasureGetFirstPsNgWidth(double FrRs, double ScRs,\
															double FrFl,double ScFl,\
															double &PsWidth,double &NgWidth)
{

    double ret, cy;
    if(FrRs == -1)
        ret = -1;
    else if(FrFl>=FrRs)
    {
        ret = FrFl - FrRs;
    }
    else
    {
        ret = ScFl - FrRs;
    }
   PsWidth = ret;
   cy = ScRs - FrRs;
   if(cy > 0){
       if(ret > cy)
           PsWidth = cy;
   }
   if(PsWidth < -1)
       PsWidth = -1;

    if(FrFl == -1)
        ret = -1;
    else if(FrRs>=FrFl)
    {
        ret = FrRs - FrFl;
    }
    else
    {
        ret = ScRs - FrFl;
    }
   NgWidth = ret;
   cy = ScFl - FrFl;
   if(cy > 0){
       if(ret > cy)
          NgWidth = cy;
   }
   if(NgWidth<-1)
       NgWidth = -1;

}

void MeasureCalc::MeasureGetPostiveDutyCycle(double PsWidth,double Period,double &PsDtCycle)
{
    if(PsWidth == -1 || Period == -1)
        PsDtCycle = -1;
    else
        PsDtCycle = PsWidth/Period;
    return;
}

void MeasureCalc::GetPosNavOverShoot(int max,int min,\
                                                    int hig,int low,\
										 			double &PosOverShoot,double &NavOverShoot)
{
    if(hig!=low)
    {
        PosOverShoot = (double)(max-hig)/(hig-low);
        NavOverShoot = (double)(low-min)/(hig-low);
    }
    else
    {
        PosOverShoot = -1;
        NavOverShoot = -1;
    }
    return;
}

double MeasureCalc::GetMean(const int *buf, int buf_len)
{
    return sum/buf_len;
}

double MeasureCalc::GetRms(const int *buf, int buf_len)
{   double rms;
    if(buf && buf_len>0) {
        rms = sqrt(square_sum/buf_len);
    } else {
        rms = -1;
    }
    return rms;
}


bool MeasureCalc::GetCycleMean(const int *buf,int buf_len,double FrRs,double ScRs,double FrFl,double ScFl,double &CycleMean)
{
    int len;
    int i= 0;
    int64_t Sum = 0;
    bool bx=true;
    if(FrRs<FrFl)
    {
        len = (int)(ScRs-FrRs);
        if(len>0 && len < buf_len)
        {
            for(i =(int)FrRs;i<ScRs;i++)
                Sum += buf[i];
            CycleMean = (double)Sum/len;
        }
        else
            bx = false;
    }
    else
    {
        len = (int)(ScFl-FrFl);
        if(len>0 && len < buf_len)
        {
            for(i =(int)FrFl;i<ScFl;i++)
                Sum += buf[i];
            CycleMean = (double)Sum/len;
        }
        else
            bx = false;
    }
    return bx;
}

void MeasureCalc::GetCycleRms(const int *buf,int buf_len,double FrRs,double ScRs,double FrFl,double ScFl,double &Cyclerms)
{
    int len;
    int i= 0;
    int64_t Sum = 0;
    if(FrRs<FrFl)
    {
        len = (int)(ScRs-FrRs);
        if(len>0&&len < buf_len)
        {
            for(i = (int)FrRs;i<ScRs;i++)
                Sum += (int64_t)buf[i]*buf[i];
            Cyclerms = (double)Sum/len;
            Cyclerms = (double)sqrt(Cyclerms);
        }
        else
            Cyclerms = -1;
    }
    else
    {
        len = (int)(ScFl-FrFl);
        if(len>0&&len < buf_len)
        {
            for(i =(int)FrFl;i<ScFl;i++)
                Sum += buf[i]*buf[i];
            Cyclerms = (double)Sum/len;
            Cyclerms = (double)sqrt(Cyclerms);
        }
        else
            Cyclerms = -1;
    }
    return;
}
double MeasureCalc::GetPkPk()
{
    return (double)maxVal - minVal;
}

double MeasureCalc::GetAmplitude()
{
    return (double)highVal - lowVal;
}
void MeasureCalc::setMeasureIndication(MEASURE_ITEM_TYPE itemType,float val){
    if(isMeasureItemValid(itemType)){
        pMeasure->measreIndication[itemType][INDICATION_LEFT] = val;
        pMeasure->measreIndication[itemType][INDICATION_TOP] = val;
        pMeasure->measreIndication[itemType][INDICATION_RIGHT] = val;
        pMeasure->measreIndication[itemType][INDICATION_BOTTOM] = val;
    }

}
void MeasureCalc::setMeasureIndication(MEASURE_ITEM_TYPE itemType,int dir,float val){
    if(isMeasureItemValid(itemType) && isIndicationValid(dir)){
        pMeasure->measreIndication[itemType][dir] = val;
    }
}
double MeasureCalc::WaveIdx2PixEx(double idx){
    return begin + idx * cols / pWave->getWaveLength();
}

int32_t MeasureCalc::WaveIdx2Pix(double idx){
    return WaveIdx2Pix((int32_t)lround(idx));
}
int32_t MeasureCalc::WaveIdx2Pix(int32_t idx){
    return begin + idx * cols / pWave->getWaveLength();
}
int32_t MeasureCalc::Vertical2Pix(double v){
    return Vertical2Pix((int32_t) lround(v));
}
int32_t MeasureCalc::Vertical2Pix(int32_t v){

    double val = (double)v * waveFactor * pMeasure->measureheader.header.vscale;

    return (int32_t)lround((pMeasure->measureheader.header.h/2 - (pMeasure->measureheader.header.pos + val)) * pMeasure->measureheader.header.vrate);
}



bool MeasureCalc::getMeasureItemVal(MEASURE_ITEM_TYPE itemType, double & val){

    double l,r;
    bool bValid = true;
    setMeasureIndication(itemType,-1);
    switch (itemType){
        case MEASURE_PERIOD:
            val = period;
            bValid = bPeriodVaild;
            if(bValid){
                if(FrRs >= 0 && ScRs > 0 && FrRs < FrFl){
                    setMeasureIndication(itemType,INDICATION_LEFT,WaveIdx2Pix(FrRs));
                    setMeasureIndication(itemType,INDICATION_RIGHT,WaveIdx2Pix(ScRs));
                    setMeasureIndication(itemType,INDICATION_TOP,Vertical2Pix(middle));
                }
                else if(FrFl >= 0 && ScFl > 0 && FrFl<FrRs){
                    setMeasureIndication(itemType,INDICATION_LEFT,WaveIdx2Pix(FrFl));
                    setMeasureIndication(itemType,INDICATION_RIGHT,WaveIdx2Pix(ScFl));
                    setMeasureIndication(itemType,INDICATION_TOP,Vertical2Pix(middle));
                }
            }
            val *= timePot;
            break;
        case MEASURE_FREQ:
            val = period;
            bValid = bPeriodVaild;
            if(bValid){
                if(FrRs >= 0 && ScRs > 0 && FrRs < FrFl)
                {
                    setMeasureIndication(itemType,INDICATION_LEFT,WaveIdx2Pix(FrRs));
                    setMeasureIndication(itemType,INDICATION_RIGHT,WaveIdx2Pix(ScRs));
                    setMeasureIndication(itemType,INDICATION_TOP,Vertical2Pix(middle));
                }
                else if(FrFl >= 0 && ScFl > 0 && FrFl<FrRs)
                {
                    setMeasureIndication(itemType,INDICATION_LEFT,WaveIdx2Pix(FrFl));
                    setMeasureIndication(itemType,INDICATION_RIGHT,WaveIdx2Pix(ScFl));
                    setMeasureIndication(itemType,INDICATION_TOP,Vertical2Pix(middle));
                }
            }
            val *= timePot;
            val = 1.0f/val;
            break;
        case MEASURE_RISETIME:{

            MeasureGetRiseTime(pWaveData, wavelen,
                               (int)lround(upper), (int)lround(lower),
                               FrRs, ScRs, val, l, r);
            val *= timePot;
            if(val < 0) val = 0;
            bValid = val >= 0;
            if(bValid){
                if(val > 0){
                    setMeasureIndication(itemType,INDICATION_TOP,Vertical2Pix(upper));
                    setMeasureIndication(itemType,INDICATION_BOTTOM,Vertical2Pix(lower));
                    setMeasureIndication(itemType,INDICATION_LEFT,WaveIdx2Pix(l));
                    setMeasureIndication(itemType,INDICATION_RIGHT,WaveIdx2Pix(r));
                }
            }

            break;
        }


        case MEASURE_FALLTIME:{
            MeasureGetFallTime(pWaveData, wavelen, (int)lround(upper), (int)lround(lower), FrFl, ScFl, val, l, r);

            val *= timePot;
            if(val < 0) val = 0;
            bValid = val >= 0;
            if(bValid){
                if(val > 0){
                    setMeasureIndication(itemType,INDICATION_TOP,Vertical2Pix(upper));
                    setMeasureIndication(itemType,INDICATION_BOTTOM,Vertical2Pix(lower));
                    setMeasureIndication(itemType,INDICATION_LEFT,WaveIdx2Pix(l));
                    setMeasureIndication(itemType,INDICATION_RIGHT,WaveIdx2Pix(r));
                }
            }

            break;
        }

        case MEASURE_POSITIVE_DUTY:
            MeasureGetPostiveDutyCycle(positivePulseWidth,period,val);
            bValid = bPeriodVaild;
            if(bValid && val >= 0){
                if(FrRs < FrFl){
                    setMeasureIndication(itemType,INDICATION_LEFT,WaveIdx2Pix(FrRs));
                    setMeasureIndication(itemType,INDICATION_RIGHT,WaveIdx2Pix(FrFl));
                }else if(FrRs < ScFl){
                    setMeasureIndication(itemType,INDICATION_LEFT,WaveIdx2Pix(FrRs));
                    setMeasureIndication(itemType,INDICATION_RIGHT,WaveIdx2Pix(ScFl));
                }
            }
            break;
        case MEASURE_NEGATIVE_DUTY:
            MeasureGetPostiveDutyCycle(positivePulseWidth,period,val);
            if(val < 0) bValid = false;
            else val = 1 - val;

            bValid = bPeriodVaild;
            if(bValid && val >= 0){
                if(FrFl < FrRs){
                    setMeasureIndication(itemType,INDICATION_LEFT,WaveIdx2Pix(FrFl));
                    setMeasureIndication(itemType,INDICATION_RIGHT,WaveIdx2Pix(FrRs));
                }else if(FrFl < ScRs){
                    setMeasureIndication(itemType,INDICATION_LEFT,WaveIdx2Pix(FrFl));
                    setMeasureIndication(itemType,INDICATION_RIGHT,WaveIdx2Pix(ScRs));
                }
            }

            break;
        case MEASURE_POSITIVE_PULSE_WIDTH:
            val = positivePulseWidth;

            val *= timePot;
            if(val < 0) bValid = false;
            if(bValid){
                if(FrRs < FrFl){
                    setMeasureIndication(itemType,INDICATION_LEFT,WaveIdx2Pix(FrRs));
                    setMeasureIndication(itemType,INDICATION_RIGHT,WaveIdx2Pix(FrFl));
                }else if(FrRs < ScFl){
                    setMeasureIndication(itemType,INDICATION_LEFT,WaveIdx2Pix(FrRs));
                    setMeasureIndication(itemType,INDICATION_RIGHT,WaveIdx2Pix(ScFl));
                }
            }
            break;
        case MEASURE_NEGATIVE_PULSE_WIDTH:
            val = negativePulseWidth;

            val *= timePot;
            if(val < 0) bValid = false;
            if(bValid){
                if(FrFl < FrRs){
                    setMeasureIndication(itemType,INDICATION_LEFT,WaveIdx2Pix(FrFl));
                    setMeasureIndication(itemType,INDICATION_RIGHT,WaveIdx2Pix(FrRs));
                }else if(FrFl < ScRs){
                    setMeasureIndication(itemType,INDICATION_LEFT,WaveIdx2Pix(FrFl));
                    setMeasureIndication(itemType,INDICATION_RIGHT,WaveIdx2Pix(ScRs));
                }
            }
            break;
        case MEASURE_BURST_WIDTH:
            val = burstWidth;
            val *= timePot;
            if(val < 0) bValid = false;
            if(bValid){

                if(FrRs >= 0 && FrRs < FrFl){
                    setMeasureIndication(itemType,INDICATION_LEFT,WaveIdx2Pix(FrRs));
                    if(LastFl > LastRs) {
                        setMeasureIndication(itemType,INDICATION_RIGHT,WaveIdx2Pix(LastFl));
                    }
                    else if(LastRs > FrFl) {
                        setMeasureIndication(itemType,INDICATION_RIGHT,WaveIdx2Pix(LastRs));
                    }
                }
                else if(FrFl >= 0 && FrFl < FrRs){
                    setMeasureIndication(itemType,INDICATION_LEFT,WaveIdx2Pix(FrFl));
                    if(LastRs > LastFl) {
                        setMeasureIndication(itemType,INDICATION_RIGHT,WaveIdx2Pix(LastRs));
                    }
                    else if(LastFl > FrRs) {
                        setMeasureIndication(itemType,INDICATION_RIGHT,WaveIdx2Pix(LastFl));
                    }
                }

            }

            break;
        case MEASURE_NEGATIVE_OVERSHOOT:
        case MEASURE_POSITIVE_OVERSHOOT:
        {
            double positiveOvershoot;
            double negativeOvershoot;
            GetPosNavOverShoot(maxVal,minVal,highVal,lowVal,positiveOvershoot,negativeOvershoot);
            if(itemType == MEASURE_POSITIVE_OVERSHOOT){
//                LOGD("maxVal:%d,minVal:%d,highVal:%d,lowVal:%d",maxVal,minVal,highVal,lowVal);

                if(positiveOvershoot < 0) bValid = false;
                else val = positiveOvershoot;
                if(bValid){
                    setMeasureIndication(itemType,INDICATION_TOP,Vertical2Pix(maxVal));
                    setMeasureIndication(itemType,INDICATION_BOTTOM,Vertical2Pix(highVal));
                }
            }
            else {
                if(negativeOvershoot < 0) bValid = false;
                else val = negativeOvershoot;
                if(bValid){
                    setMeasureIndication(itemType,INDICATION_TOP,Vertical2Pix(lowVal));
                    setMeasureIndication(itemType,INDICATION_BOTTOM,Vertical2Pix(minVal));
                }
            }
            break;
        }
        case MEASURE_POSITIVE_UNDERSHOOT:
        {

            if(highVal != lowVal){
                val = (double)(highVal - ConcaveVal)/(highVal-lowVal);
            }else{
                bValid = false;
            }
        }
            break;
        case MEASURE_NEGATIVE_UNDERSHOOT:
        {
            if(highVal != lowVal){
                val = (double)(ConvexVal-lowVal)/(highVal-lowVal);
            }else{
                bValid = false;
            }
        }
        break;

        case MEASURE_AC_RMS:
        {
            int len = wavelen;
            if(len > 0){
                double v = sum / len;
                val = sqrt((square_sum - 2 * v * sum  + v * v * len)/len);
                setMeasureIndication(itemType,INDICATION_TOP,Vertical2Pix(val));
                val = (double)(pWave->getVerticalPerPix() * val);
            }else{
                bValid = false;
            }

        }
            break;
        case MEASURE_POSITIVE_RATE:
            MeasureGetRiseTime(pWaveData, wavelen, (int)round(upper), (int)round(lower), FrRs, ScRs, val,l, r);
            val *= timePot;
            if(val > 0){
                val = (upper - lower) * pWave->getVerticalPerPix()/val;
            }else{
                val = 0;
            }
            bValid = val >= 0;
            if(bValid && val>0){
                setMeasureIndication(itemType,INDICATION_TOP,Vertical2Pix(upper));
                setMeasureIndication(itemType,INDICATION_BOTTOM,Vertical2Pix(lower));
                setMeasureIndication(itemType,INDICATION_LEFT,WaveIdx2Pix(l));
                setMeasureIndication(itemType,INDICATION_RIGHT,WaveIdx2Pix(r));
            }

            break;
        case MEASURE_NEGATIVE_RATE:
            MeasureGetFallTime(pWaveData, wavelen, (int)round(upper), (int)round(lower), FrFl, ScFl, val,l, r);

            val *= timePot;
            if(val > 0){
                val = (upper - lower)* pWave->getVerticalPerPix()/val;
            }else{
                val = 0;
            }
            bValid = val >= 0;
            if(bValid && val > 0){
                setMeasureIndication(itemType,INDICATION_TOP,Vertical2Pix(upper));
                setMeasureIndication(itemType,INDICATION_BOTTOM,Vertical2Pix(lower));
                setMeasureIndication(itemType,INDICATION_LEFT,WaveIdx2Pix(l));
                setMeasureIndication(itemType,INDICATION_RIGHT,WaveIdx2Pix(r));
            }

            break;
        case MEASURE_DELAY:
            if(abs(maxVal - minVal)< 15){
                bValid = false;
            }
            val = timePot;

            break;
        case MEASURE_PHASE:
            val = timePot;
            break;
        case MEASURE_PK_PK:
            val = (double)(pWave->getVerticalPerPix() * GetPkPk());
            setMeasureIndication(itemType,INDICATION_TOP,Vertical2Pix(maxVal));
            setMeasureIndication(itemType,INDICATION_BOTTOM,Vertical2Pix(minVal));
            break;
        case MEASURE_AMPLITUDE:
            val = (double)(pWave->getVerticalPerPix() * GetAmplitude());
            setMeasureIndication(itemType,INDICATION_TOP,Vertical2Pix(highVal));
            setMeasureIndication(itemType,INDICATION_BOTTOM,Vertical2Pix(lowVal));
            break;
        case MEASURE_HIGH:
            val =(double) (pWave->getVerticalPerPix() * highVal);
            setMeasureIndication(itemType,INDICATION_TOP,Vertical2Pix(highVal));
            break;
        case MEASURE_LOW:
            val = (double) (pWave->getVerticalPerPix() * lowVal);
            setMeasureIndication(itemType,INDICATION_BOTTOM,Vertical2Pix(lowVal));
            break;
        case MEASURE_MAX:
            val =(double) (pWave->getVerticalPerPix() * maxVal);
            setMeasureIndication(itemType,INDICATION_TOP,Vertical2Pix(maxVal));
            break;
        case MEASURE_MIN:
            val =(double) (pWave->getVerticalPerPix() * minVal);
            setMeasureIndication(itemType,INDICATION_BOTTOM,Vertical2Pix(minVal));
            break;
        case MEASURE_RMS:
            val = GetRms(pWaveData, wavelen);
            if(val < 0) bValid = false;
            else {
                setMeasureIndication(itemType,INDICATION_TOP,Vertical2Pix(val));
                val = (double)(pWave->getVerticalPerPix() * val);
            }
            break;
        case MEASURE_CRMS:
            GetCycleRms(pWaveData, wavelen, FrRs, ScRs, FrFl, ScFl, val);
            if(val < 0) bValid = false;
            else{
                if(FrRs<FrFl)
                {
                    setMeasureIndication(itemType,INDICATION_LEFT,WaveIdx2Pix(FrRs));
                    setMeasureIndication(itemType,INDICATION_RIGHT,WaveIdx2Pix(ScRs));
                }
                else
                {
                    setMeasureIndication(itemType,INDICATION_LEFT,WaveIdx2Pix(FrFl));
                    setMeasureIndication(itemType,INDICATION_RIGHT,WaveIdx2Pix(ScFl));
                }
                setMeasureIndication(itemType,INDICATION_TOP,Vertical2Pix(val));
                val = (double)(pWave->getVerticalPerPix() * val);
            }
            break;
        case MEASURE_MEAN:
            if(wavelen > 0 ){
                val = GetMean(pWaveData, wavelen);

                setMeasureIndication(itemType,INDICATION_TOP,Vertical2Pix(val));
                val = (double)(pWave->getVerticalPerPix() * val);
            }else{
                bValid = false;
            }
            break;
        case MEASURE_CMEAN:
            bValid = GetCycleMean(pWaveData, wavelen, FrRs, ScRs, FrFl, ScFl, val);

            if(bValid){
                if(FrRs<FrFl)
                {
                    setMeasureIndication(itemType,INDICATION_LEFT,WaveIdx2Pix(FrRs));
                    setMeasureIndication(itemType,INDICATION_RIGHT,WaveIdx2Pix(ScRs));
                }
                else
                {
                    setMeasureIndication(itemType,INDICATION_LEFT,WaveIdx2Pix(FrFl));
                    setMeasureIndication(itemType,INDICATION_RIGHT,WaveIdx2Pix(ScFl));
                }
                setMeasureIndication(itemType,INDICATION_TOP,Vertical2Pix(val));
            }


            val = (double)(pWave->getVerticalPerPix() * val);
            break;
        case MEASURE_TIME_POT:
            val = timePot;
            break;
        case MEASURE_FIRST_RISE_EDGE:
            val = FrRs;
            if(val < 0)bValid = false;
            break;
        case MEASURE_FIRST_FALL_EDGE:
            val = FrFl;
            if(val < 0)bValid = false;
            break;
        case MEASURE_SECON_RISE_EDGE:
            val = ScRs;
            if(val < 0)bValid = false;
            break;
        case MEASURE_SECON_FALL_EDGE:
            val = ScFl;
            if(val < 0)bValid = false;
            break;
        case MEASURE_LAST_RISE_EDGE:
            val = LastRs;
            if(val < 0)bValid = false;
            break;
        case MEASURE_LAST_FALL_EDGE:
            val = LastFl;
            if(val < 0)bValid = false;
            break;
        case MEASURE_CLIPPING:
            val = clipping;
            break;
        default:
            bValid = false;
            break;
    }
    int limit = pMeasure->measureheader.header.h / 100;
    if(abs(maxVal) < limit && abs(minVal) < limit){
        setMeasureIndication(itemType,-1);
    }
    return bValid;
}
float MeasureCalc::GetValbyHistogram(const int *buf,const int buf_len,int Max,int Min)
{
    int temp =0;
    int N = (int)Max-Min+1;
    int i = 0;
    if(N < 1 || N >= 0xFFFF){
        return 0;
    }

    int *Cache = new int[N];
    if(Cache == NULL)
        return 0;
    for(i=0;i<N;i++){
        Cache[i] = 0;
    }
    for(i=0;i<buf_len;i++){
        int k = buf[i]-Min;
        if(k>= 0 && k < N){
            Cache[k]++;
        }
    }

    int y[3];
    int x[3];


    y[0] = y[1] = y[2] = 0;
    x[0] = x[1] = x[2] = Min+0;

    for(i = 0;i<N;i++)
    {

        if(y[2]<Cache[i])
        {
            y[2]=Cache[i];
            x[2]=Min+i;
            if(y[1]<y[2])
            {
                temp = y[1];
                y[1] = y[2];
                y[2] = temp;
                temp = x[1];
                x[1] = x[2];
                x[2] = temp;
                if(y[0]<y[1])
                {
                    temp = y[0];
                    y[0] = y[1];
                    y[1] = temp;
                    temp = x[0];
                    x[0] = x[1];
                    x[1] = temp;
                }

            }
        }
    }
    delete [] Cache;

    if(x[0] == x[1]&&x[0] == x[2])
    {
        return x[0];
    }


    double val;
    double y1 = y[0];
    double y2 = y[1];
    double y3 = y[2];
    double x1 = x[0];
    double x2 = x[1];
    double x3 = x[2];

    y1 /= buf_len;
    y2 /= buf_len;
    y3 /= buf_len;
    if(y[0] == y[1]&&y[0]==y[2]){
        return (x1+x2+x3)/3;
    }
    if(0 == y[2]){
        val = x1*y1+x2*y2+x3*y3;
    }else{
        val = (((x1*x1-x3*x3) * log(y2/y1) - (x1*x1-x2*x2) * log(y3/y1))/(2*((x1-x3)*log(y2/y1)-(x1-x2)*log(y3/y1))));
    }
    return val;
}

bool MeasureCalc::CalcColV(int col, double & val)
{
    int *pWaveData = pWave->getWaveData();
    int wavelen = pWave->getWaveLength();
    int cols = pWave->getEndX() - pWave->getStartX() + 1;
    int idx,MaxVal,MinVal,lNum = 0;
    lNum = (int)((float)wavelen/cols);
    int N = lNum * 25;
    if(cols > 25 && wavelen > 0 && lNum > 0
        && col > 12
        && col < pWave->getEndX()){
        bool bRet = true;
        int *plNum = new int[N];
        int startIdx = (col-12) * lNum;
        MaxVal = MinVal =  pWaveData[startIdx];
        for(int j=0;j < N ;j++){
            idx =  startIdx + j;
            if(idx>=0 && idx<wavelen ){
                plNum[j] = pWaveData[idx];
            }else{
                plNum[j] = 0;
                bRet = false;
            }
            if(plNum[j] > MaxVal) MaxVal = plNum[j];
            if(plNum[j] < MinVal) MinVal = plNum[j];
        }
        val = GetValbyHistogram(plNum,N ,MaxVal,MinVal);
        val = (double)(pWave->getVerticalPerPix() * val);
        delete []plNum;
        return bRet;
    }
    return false;
}
bool MeasureCalc::CalcCursor(int x,double &val){

    int *pWaveDatax = pWave->getWaveData();
    int wavelenx = pWave->getWaveLength();
    int colsx = pWave->getEndX() - pWave->getStartX() + 1;
    if(cols > 0 && wavelenx > 0){
        double lNum = (double)wavelenx/colsx;
        if(lNum >= 1){
            int startIdx = (x - pWave->getStartX())  * lNum;
            if(startIdx >= 0 && startIdx < wavelenx){
                double s = 0;
                int n = 0;
                for(int i=0;i<lNum;i++){
                    int idx = startIdx + i;
                    if(idx < wavelenx){
                        s += pWaveDatax[idx];
                        n++;
                    }else{
                        break;
                    }
                }
                val = s / n;
                val *= pWave->getVerticalPerPix();
                //LOGD("col:%d,val:%f,%d,%d,%f\n",x,val,pWave->getStartX(),pWave->getEndX(),lNum);
                return true;
            }

        }else{
            double startIdx = (x - pWave->getStartX())  * lNum;
            int x1 = (int) (std::floor(startIdx) + 0.1);
            int x2 = (int) (std::ceil(startIdx) + 0.1);
            if(x1 >= 0 && x2 < wavelenx) {
                val = pWaveDatax[x1] + (pWaveDatax[x2] - pWaveDatax[x1]) * (startIdx - x1);
                val *= pWave->getVerticalPerPix();
                //LOGD("col:%d,val:%f,%d,%d,%f\n",x,val,pWave->getStartX(),pWave->getEndX(),lNum);
                return true;
            }
        }

    }

    return false;
}
void MeasureCalc::CalcConcaveConvexVal()
{
    ConcaveVal = highVal;
    ConvexVal = lowVal;
    if(maxIdx >= 0 && minIdx >= 0)
    {
        int val;
        int thresholdVal = (highVal - 2 * (maxVal-highVal));
        int measureVal = maxVal;
        if(thresholdVal < lowVal) thresholdVal = lowVal;
        for(int i=maxIdx;i<wavelen;i++)
        {
            val = pWaveData[i];
            if(val < highVal && val > thresholdVal)
            {
                if(measureVal > val){
                    measureVal = val;
                }
            }
            else if( val < thresholdVal){
                break;
            }else if(val >= highVal && measureVal < highVal ){
                ConcaveVal = measureVal;
                break;
            }
        }

        thresholdVal = (lowVal + 2 * (lowVal-minVal));
        measureVal = minVal;
        if(thresholdVal > highVal) thresholdVal = highVal;
        for(int i=minIdx;i<wavelen;i++)
        {
            val = pWaveData[i];
            if(val > lowVal && val < thresholdVal)
            {
                if(measureVal < val){
                    measureVal = val;
                }

            }
            else if( val > thresholdVal){
                break;
            }else if(val <= lowVal && measureVal > lowVal ){
                ConvexVal = measureVal;
                break;
            }
        }

    }

}
void MeasureCalc::Calc(){

    MeasureGetMaxMin(pWaveData, wavelen, maxVal, minVal, maxIdx, minIdx, sum, square_sum);
    GetHigLowByHistogram(pWaveData,wavelen,maxVal,minVal,highVal,lowVal);
    double v = highVal - lowVal;
    if(pMeasure->measureheader.header.abs){
        this->upper = pMeasure->measureheader.header.absHigh;
        this->lower =  pMeasure->measureheader.header.absLow;
        this->middle =  pMeasure->measureheader.header.absMiddle;
    }else{
        this->upper = lowVal + v * pMeasure->measureheader.header.high / 100;
        this->lower = lowVal + v * pMeasure->measureheader.header.low / 100;
        this->middle = lowVal + v * pMeasure->measureheader.header.middle / 100;
    }

    MeasureGetBustWidthAndSignalEdge1(pWaveData,wavelen,highVal,lowVal,
                                     FrRs, FrFl,ScRs,ScFl,
                                     LastRs, LastFl,burstWidth,
                                     zeroNum1,zeroNum2);
    timePot =(double) (1.0/pWave->getSampRate());
    //计算周期、频率
    MeasureGetPeriodAndFreq(FrRs,FrFl,ScRs,ScFl,period,freq);
        
    MeasureGetFirstPsNgWidth(FrRs,ScRs,FrFl,ScFl,positivePulseWidth,negativePulseWidth);
    bPeriodVaild = period > 0;
    CalcConcaveConvexVal();
    int clipValue[2] = {0, 0};

    clipValue[0] = (int) (maxVal * waveFactor  + pWave->getYPos());
    clipValue[1] = (int) (minVal * waveFactor + pWave->getYPos());

    int bh = pMeasure->measureheader.header.h / 2;
    if(clipValue[0] > bh && clipValue[1] < -bh)
        clipping = 3;
    else if((clipValue[0] > bh && clipValue[1] > bh)
            || (clipValue[0] < -bh && clipValue[1] < -bh))
        clipping = 4;
    else if(clipValue[0] > bh)
        clipping = 1;
    else if(clipValue[1] < -bh)
        clipping = 2;
    else
        clipping = 0;
}

bool MeasureCalc::CalcTValue(float level,int num,double & pix){
    std::vector<int> r;
    std::vector<int> l;
    if(wavelen <= 0
        || pWaveData == NULL){
        return false;
    }

//    int hysteresis = std::abs(maxVal - minVal) * 0.02;
//    if(hysteresis < 25)
    int hysteresis = 20;

    int a = pWaveData[0] < level ? 0 : 1;

//    LOGD("ch:%d,hysteresis:%d,wavelen:%d,xpos:%lld,num:%d,level:%f,cols:%d\n",
//         pWave->getChIdx(),hysteresis,wavelen,pWave->getXPos(),num,level,cols);
    int nn = wavelen / cols;
    if(nn < 1) nn = 1;
    int idx = -1;
    for(int i=0;i<wavelen;i++){
        if(i % nn == 0){
            a = a & 1;
        }
        if(a == 0){
            if(pWaveData[i] >= level){
                if(idx == -1){
                    idx = i;
                }
                if( pWaveData[i] > (level + hysteresis)){
                    r.push_back(idx);
                    idx = -1;
                    a = 3;
                }
            }else{
                idx = -1;
            }
        }else if(a == 1){
            if(pWaveData[i] <= level){
                if(idx == -1){
                    idx = i;
                }
                if( pWaveData[i] < (level - hysteresis)){
                    l.push_back(idx);
                    idx = -1;
                    a = 2;
                }
            }else{
                idx = -1;
            }
        }

    }

//    for(auto v:r){
//        LOGD("ch:%d,r: %d",pWave->getChIdx(),v);
//    }
//
//    for(auto v:l){
//        LOGD("ch:%d,l: %d",pWave->getChIdx(),v);
//    }

    if(num > 0
        && num <= r.size() ){
        pix = WaveIdx2PixEx(r[num - 1]);
//        LOGD("ch:%d,num: %d,",pWave->getChIdx(),num);
        return true;
    }

    if(num < 0 ){
        num = std::abs(num);
        if(num > 0 && num <= l.size()){
            pix = WaveIdx2PixEx(l[num - 1]);
            return true;
        }
    }

    return false;
}
