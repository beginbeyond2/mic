#ifndef SCPI_CMD_MATH_H
#define SCPI_CMD_MATH_H

#include "../inc/scpi.h"

#ifdef  __cplusplus
extern "C" {
#endif

scpi_result_t MATH_DISP(scpi_t * context);
scpi_result_t MATH_DISPQ(scpi_t * context);
scpi_result_t MATH_MODE(scpi_t * context);
scpi_result_t MATH_MODEQ(scpi_t * context);
scpi_result_t MATH_VREF(scpi_t *context);
scpi_result_t MATH_VREFQ(scpi_t *context);
//base double wave
scpi_result_t MATH_BASE_S1(scpi_t * context);
scpi_result_t MATH_BASE_S1Q(scpi_t * context);
scpi_result_t MATH_BASE_S2(scpi_t * context);
scpi_result_t MATH_BASE_S2Q(scpi_t * context);
scpi_result_t MATH_BASE_EXT(scpi_t * context);
scpi_result_t MATH_BASE_EXTQ(scpi_t * context);
scpi_result_t MATH_BASE_OFFS(scpi_t * context);
scpi_result_t MATH_BASE_OFFSQ(scpi_t * context);
scpi_result_t MATH_BASE_OPER(scpi_t * context);
scpi_result_t MATH_BASE_OPERQ(scpi_t * context);

//FFT
scpi_result_t MATH_FFT_SOUR(scpi_t * context);
scpi_result_t MATH_FFT_SOURQ(scpi_t * context);
scpi_result_t MATH_FFT_WIND(scpi_t * context);
scpi_result_t MATH_FFT_WINDQ(scpi_t * context);
scpi_result_t MATH_FFT_TYPE(scpi_t * context);
scpi_result_t MATH_FFT_TYPEQ(scpi_t * context);
scpi_result_t MATH_FFT_EXT(scpi_t * context);
scpi_result_t MATH_FFT_EXTQ(scpi_t * context);
scpi_result_t MATH_FFT_OFFS(scpi_t * context);
scpi_result_t MATH_FFT_OFFSQ(scpi_t * context);
scpi_result_t MATH_FFT_HSCA(scpi_t * context);
scpi_result_t MATH_FFT_HSCAQ(scpi_t * context);
scpi_result_t MATH_FFT_POS(scpi_t * context);
scpi_result_t MATH_FFT_POSQ(scpi_t * context);
//AX+B
scpi_result_t MATH_AXB_SOUR(scpi_t * context);
scpi_result_t MATH_AXB_SOURQ(scpi_t * context);
scpi_result_t MATH_AXB_A(scpi_t * context);
scpi_result_t MATH_AXB_AQ(scpi_t * context);
scpi_result_t MATH_AXB_B(scpi_t * context);
scpi_result_t MATH_AXB_BQ(scpi_t * context);
scpi_result_t MATH_AXB_UNIT(scpi_t * context);
scpi_result_t MATH_AXB_UNITQ(scpi_t * context);
scpi_result_t MATH_AXB_EXT(scpi_t * context);
scpi_result_t MATH_AXB_EXTQ(scpi_t * context);
scpi_result_t MATH_AXB_OFFS(scpi_t * context);
scpi_result_t MATH_AXB_OFFSQ(scpi_t * context);
//ADVanced
scpi_result_t MATH_ADV_EXPR(scpi_t * context);
scpi_result_t MATH_ADV_EXPRQ(scpi_t * context);
scpi_result_t MATH_ADV_VAR1(scpi_t * context);
scpi_result_t MATH_ADV_VAR1Q(scpi_t * context);
scpi_result_t MATH_ADV_VAR2(scpi_t * context);
scpi_result_t MATH_ADV_VAR2Q(scpi_t * context);
scpi_result_t MATH_ADV_EXT(scpi_t * context);
scpi_result_t MATH_ADV_EXTQ(scpi_t * context);
scpi_result_t MATH_ADV_OFFS(scpi_t * context);
scpi_result_t MATH_ADV_OFFSQ(scpi_t * context);
scpi_result_t MATH_ADV_UNIT(scpi_t * context);
scpi_result_t MATH_ADV_UNITQ(scpi_t * context);

//math sample query
scpi_result_t MATH_SAMPLE_SRateQ(scpi_t * context);
scpi_result_t MATH_SAMPLE_MDepthQ(scpi_t * context);


bool setDualS1(scpi_t * context);
bool setDualS2(scpi_t * context);
void setDualHScal(scpi_t * context);
void getDualHScal(scpi_t * context);
void setDualVScal(scpi_t * context, int op);
void getDualVScal(scpi_t * context, int op);
void setVPos(scpi_t * context, int mode);
void getVPos(scpi_t * context, int mode);
#ifdef  __cplusplus
}
#endif

#endif // SCPI_CMD_MATH_H
