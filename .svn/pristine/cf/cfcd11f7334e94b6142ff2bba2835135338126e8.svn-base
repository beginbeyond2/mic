//
// Created by zhuzh on 2018-4-16.
//

#ifndef TBOOKSCOPE_MEASUREITEM_H
#define TBOOKSCOPE_MEASUREITEM_H


#include "MeasureHeader.h"

class MeasureItem {
public:

    MeasureItem(MEASURE_T *pMeasure);
    int32_t getChIdx();
    int32_t getCol();
    int32_t getX1();
    int32_t getX2();
    bool isEnableItem(MEASURE_ITEM_TYPE item);
    void setItemVal(MEASURE_ITEM_TYPE item, float val);
    void setItemVaild(MEASURE_ITEM_TYPE item,bool bValid);
    void setAllItemNoVaild();
    float getlevel();
    int getNums();
private:
    MEASURE_T * pMeasure;

};


#endif //TBOOKSCOPE_MEASUREITEM_H
