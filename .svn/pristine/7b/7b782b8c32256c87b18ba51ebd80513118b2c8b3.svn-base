//
// Created by zhuzh on 2018-4-16.
//

#include "MeasureItem.h"

MeasureItem::MeasureItem(MEASURE_T *pMeasure) {
    this->pMeasure = pMeasure;
}

int32_t MeasureItem::getChIdx(){
    return pMeasure->measureheader.header.chIdx & 0xFF;
}
int32_t MeasureItem::getCol(){
    return pMeasure->measureheader.header.col;
}
int32_t MeasureItem::getX1(){
    return (pMeasure->measureheader.header.chIdx >> 20) & 0xFFF;
}
int32_t MeasureItem::getX2(){
    return (pMeasure->measureheader.header.chIdx >> 8) & 0xFFF;
}
bool MeasureItem::isEnableItem(MEASURE_ITEM_TYPE item) {
    uint64_t val = 1;
    int idx = item + MEASURE_HEADER_SIZE;
    return (pMeasure->measureheader.header.itemEnable & (val<<(idx))) != 0;
}

void MeasureItem::setItemVal(MEASURE_ITEM_TYPE item, float val){
    pMeasure->meaureItemVal[item] = val;
}

float MeasureItem::getlevel(){
    return pMeasure->level;
}

int MeasureItem::getNums(){
    return pMeasure->num;
}

void MeasureItem::setItemVaild(MEASURE_ITEM_TYPE item, bool bValid) {
    uint64_t mask = 1;
    uint64_t val = pMeasure->measureheader.header.itemValid;
    int idx = item + MEASURE_HEADER_SIZE;
    mask = mask << idx;
    val &= ~(mask);
    if(bValid)
        val |= mask;
    pMeasure->measureheader.header.itemValid = val;
}

void MeasureItem::setAllItemNoVaild(){
    pMeasure->measureheader.header.itemValid = 0;
    for(int i=0;i<MEASURE_ITEM_NUMS;i++){
        for(int j=0;j<4;j++){
            pMeasure->measreIndication[i][j] = -1;
        }
    }
}