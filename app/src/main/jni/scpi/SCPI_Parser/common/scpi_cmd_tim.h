#ifndef SCPI_CMD_TIM_H
#define SCPI_CMD_TIM_H

#include "../inc/scpi.h"

#ifdef  __cplusplus
extern "C" {
#endif

scpi_result_t TIM_EXT(scpi_t * context);//����ˮƽʱ����λ
scpi_result_t TIM_PLUS_EXT(scpi_t * context);//����ˮƽʱ����λ
scpi_result_t TIM_EXTQ(scpi_t * context);//��ѯˮƽʱ����λ
scpi_result_t TIM_MODE(scpi_t * context);//������Ļʱ����ʾ��ʽ
scpi_result_t TIM_MODEQ(scpi_t * context);//��ѯ��Ļʱ����ʾ��ʽ
scpi_result_t TIM_ROLL_DISP(scpi_t * context);
scpi_result_t TIM_ROLL_DISPQ(scpi_t * context);
scpi_result_t TIM_XY1_DISP(scpi_t * context);//�򿪻�ر�ͨ��1��ͨ��2��XYģʽ��ʾ
scpi_result_t TIM_XY1_DISPQ(scpi_t * context);//��ѯͨ��1��ͨ��2��XYģʽ��ʾ
scpi_result_t TIM_OFFS(scpi_t * context);//���ò�����ʾ��ˮƽƫ��
scpi_result_t TIM_PLUS_OFFS(scpi_t * context);//���ò�����ʾ��ˮƽƫ��
scpi_result_t TIM_OFFSQ(scpi_t * context);//��ѯ������ʾ��ˮƽƫ��
scpi_result_t TIM_ZOO_SCA(scpi_t * context);
scpi_result_t TIM_ZOO_SCAQ(scpi_t * context);
scpi_result_t TIM_LISTQ(scpi_t* context);

#ifdef  __cplusplus
}
#endif

#endif // SCPI_CMD_TIM_H
