//
// Created by zhuzh on 2018-5-22.
//

#include <memory.h>
#include "wavedata.h"
XWaveData::XWaveData(){
    pWave = new WAVE_T;
    memset(pWave,0,sizeof(*pWave));
}
XWaveData::XWaveData(WAVE_T *pWave){
    this->pWave = pWave;
}
int XWaveData::getMagicNum(){
    return getHeader()->magicNum;
}

int XWaveData::getVersion(){
    return getHeader()->version;
}

void XWaveData::setWaveLength(int waveLength){
    getHeader()->waveLength = waveLength;
}

int XWaveData::getWaveLength(){
    return getHeader()->waveLength;
}

int XWaveData::getBytesPerPoint(){
    return getHeader()->bytesPerPoint;
}
void XWaveData::setWaveType(int waveType){
    getHeader()->waveType = waveType;
}
int XWaveData::getWaveType(){
    return getHeader()->waveType;
}

int XWaveData::getStartX(){
    return getHeader()->startX;
}

int XWaveData::getEndX(){
    return getHeader()->endX;
}

double XWaveData::getSampRate(){
    return getHeader()->sampRate;
}

int64_t XWaveData::getXPos(){
    return getHeader()->xPos;
}

int32_t XWaveData::getYPos(){
    return getHeader()->yPos;
}

int XWaveData::getProbeType(){
    return getHeader()->probeType;
}

double XWaveData::getProbeRate(){
    return getHeader()->probeRate;
}

void XWaveData::setVScaleVal(double val){
    getHeader()->vScaleVal = val;
}

double XWaveData::getVScaleVal(){
    return getHeader()->vScaleVal;
}

double XWaveData::getTimeScaleVal(){
    return getHeader()->timeScaleVal;
}

double XWaveData::getTotalTime(){
    return getHeader()->totalTime;
}

double XWaveData::getOneScreenTime(){
    return getHeader()->oneScreenTime;
}

int XWaveData::getChIdx(){
    return getHeader()->chIdx;
}

double XWaveData::getVerticalPerPix(){
    return getHeader()->vPerPixVal ;
}
double XWaveData::getAdPix(){
    return getHeader()->adPix;
}
double XWaveData::getWaveFactor(){
    return getVerticalPerPix() * getHeader()->vPerGridPixels / getVScaleVal();
}

int32_t * XWaveData::getWaveData(){
    return pWave->wavedata;
}

WAVE_T::WAVEHEADER::HEADER * XWaveData::getHeader(){
    return &(pWave->waveheader.header);
}

int XWaveData::getWaveData(int idx)
{
    if(idx >=0 && idx < getWaveLength())
    {
        return pWave->wavedata[idx];
    }
    return 0;
}

//--------------------------------------------------------------

XBmp::XBmp(BMP_T * pBmp){
    this->pBmp = pBmp;
    this->pBmp->x1Valid = 0;
    this->pBmp->x2Valid = 0;
}
int32_t XBmp::getPerPixelByte(){
    return pBmp->perPixelByte;
}
int32_t XBmp::getWidth(){
    return pBmp->width;
}
int32_t XBmp::getHeight(){
    return pBmp->height;
}
uint32_t XBmp::getForegroundColor(){
    return pBmp->foregroundColor;
}
uint32_t XBmp::getBackgroundColor(){
    return pBmp->backgroundColor;
}
int32_t XBmp::getXOffset(){
    return pBmp->xOffset;
}
int32_t XBmp::getYOffset(){
    return pBmp->yOffset;
}
double XBmp::getVerticalPerPix(){
    return pBmp->vPerPixVal;
}
double XBmp::getTimeScaleVal(){
    return pBmp->timeScaleVal;
}
int32_t XBmp::getX1(){
    return pBmp->x1;
}
int32_t XBmp::getX2(){
    return pBmp->x2;
}
void XBmp::setX1Val(double x1Val){
    pBmp->x1Val = x1Val;
}
void XBmp::setX2Val(double x2Val){
    pBmp->x2Val = x2Val;
}
void XBmp::setX1Valid(int8_t val){
    pBmp->x1Valid = val;
}
void XBmp::setX2Valid(int8_t val){
    pBmp->x2Valid = val;
}

int32_t XBmp::getStartX(){
    return pBmp->startX;
}

int32_t XBmp::getEndX(){
    return pBmp->endX;
}

uint32_t * XBmp::getBuffer(){
    return pBmp->buffer;
}
void XBmp::setW(int32_t w)
{
    pBmp->w = w;
}
int32_t XBmp::getW(){
    return pBmp->w;
}
//----------------------------------------------------
XYBmp::XYBmp(XYBMP_T * pXYBmp){
    this->pXYBmp = pXYBmp;
}
int32_t XYBmp::getPerPixelByte(){
    return pXYBmp->perPixelByte;
}
int32_t XYBmp::getWidth(){
    return pXYBmp->width;
}
int32_t XYBmp::getHeight(){
    return pXYBmp->height;
}
uint32_t XYBmp::getForegroundColor(){
    return pXYBmp->foregroundColor;
}
uint32_t XYBmp::getBackgroundColor(){
    return pXYBmp->backgroundColor;
}
uint32_t XYBmp::getBrightness(){
    return pXYBmp->brightness;
};
int32_t XYBmp::getX(){
    return pXYBmp->x;
}
int32_t XYBmp::getY(){
    return pXYBmp->y;
}
uint32_t * XYBmp::getBuffer(){
    return pXYBmp->buffer;
}


bool XYBmp::isRun(){
    return (pXYBmp->flags & 0x01) == 1;
}
double XYBmp::getXDstVScaleVal(){
    return pXYBmp->xDstVScaleVal;
}
double XYBmp::getYDstVScaleVal(){
    return pXYBmp->yDstVScaleVal;
}
uint32_t XYBmp::cctEnable()
{
    return pXYBmp->cctEnable;
}

uint32_t XYBmp::getAdMaxVal()
{
    return pXYBmp->adMaxVal;
}