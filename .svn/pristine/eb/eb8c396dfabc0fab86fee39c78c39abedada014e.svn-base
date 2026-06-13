//
// Created by zhuzh on 2018-4-13.
//
#include "measure.h"
#include "../wavedata.h"
#include "MeasureHeader.h"
#include "MeasureCalc.h"
#include "MeasureItem.h"

#include "../Logger.h"
#define TAG "MEASURE"
static bool MeasureCalcFun(WAVE_T * pWave,MEASURE_T *pMeasure) {
    if (pWave && pMeasure) {
        bool bVaild = false;
        double val;
        MEASURE_ITEM_TYPE itemType;
        MeasureItem measureItem(pMeasure);
        XWaveData xWaveData(pWave);
        int len = xWaveData.getWaveLength();
        if (xWaveData.getWaveType() != 2 && len > 0 && len <= WAVE_POINTS) {
            MeasureCalc measureCalc(&xWaveData,pMeasure);
            measureCalc.Calc();
            bool bPkPk = false;
            for (int i = MEASURE_PERIOD; i < MEASURE_ITEM_MAX; i++) {
                itemType = (MEASURE_ITEM_TYPE) i;
                if (measureItem.isEnableItem(itemType)
                    || i == MEASURE_PERIOD
                    || i == MEASURE_FREQ
                    || (bPkPk && (i == MEASURE_MAX || i == MEASURE_MIN))
                    ) {
                    val = 0;
                    bVaild = measureCalc.getMeasureItemVal(itemType,val);
                    if (bVaild) {
                        measureItem.setItemVal(itemType, val);
                    }
                    measureItem.setItemVaild(itemType, bVaild);
                    if(itemType == MEASURE_PK_PK){
                        bPkPk = true;
                    }
                }
            }
            //begin特殊
            itemType = MEASURE_TVALUE;
            if(measureItem.isEnableItem(itemType)){
                val = 0;
                measureCalc.setMeasureIndication(itemType,-1);
                bVaild = measureCalc.CalcTValue(measureItem.getlevel(),
                                                measureItem.getNums(),
                                                val);
                if(bVaild){
                    measureCalc.setMeasureIndication(itemType,
                                                     INDICATION_TOP,
                                                     measureCalc.Vertical2Pix(measureItem.getlevel()));
                    measureCalc.setMeasureIndication(itemType,
                                                     INDICATION_LEFT,
                                                     val);
                    measureItem.setItemVal(itemType,val);
                }

                measureItem.setItemVaild(itemType, bVaild);
            }

            for(int i=MEASURE_CURSOR_X1;i<=MEASURE_CURSOR_X2;i++){
                itemType = (MEASURE_ITEM_TYPE) i;
                if(measureItem.isEnableItem(itemType)){
                    val = 0;
                    bVaild = measureCalc.CalcCursor((itemType == MEASURE_CURSOR_X1) ? measureItem.getX1() : measureItem.getX2(),val);
                    if(bVaild){
                        measureItem.setItemVal(itemType,val);
                    }
                    measureItem.setItemVaild(itemType, bVaild);
                }
            }


            itemType = MEASURE_COLV;
            if(measureItem.isEnableItem(itemType)){
                val = 0;
                bVaild = measureCalc.CalcColV(measureItem.getCol(),val);
                if(bVaild){
                    measureItem.setItemVal(itemType,val);
                }
                measureItem.setItemVaild(itemType, bVaild);
            }
            //end
            for(int i = MEASURE_CLIPPING;i<MEASURE_ITEM_NUMS;i++){
                itemType = (MEASURE_ITEM_TYPE) i;
                bVaild = measureCalc.getMeasureItemVal(itemType,val);
                measureItem.setItemVal(itemType, val);
                measureItem.setItemVaild(itemType, bVaild);
            }

        } else {
            measureItem.setAllItemNoVaild();
        }
        return true;
    }
    return false;
}

JNIEXPORT jboolean JNICALL Java_com_micsig_tbook_scope_measure_Measure_measure
        (JNIEnv * env, jobject thisz, jobject ch,jobject measureItem){
    if(MeasureCalcFun((WAVE_T*)(env->GetDirectBufferAddress(ch))
            ,(MEASURE_T*)(env->GetDirectBufferAddress(measureItem)))){
        return JNI_TRUE;
    }
    return JNI_FALSE;

}