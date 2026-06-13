#ifndef SCPI_CMD_MEAS_H
#define SCPI_CMD_MEAS_H

#include "../inc/scpi.h"

#ifdef  __cplusplus
extern "C" {
#endif

scpi_result_t MEAS_PERQ(scpi_t * context);//��ѯָ��ͨ�����ε����ڲ���ֵ
scpi_result_t MEAS_FREQQ(scpi_t * context);//��ѯָ��ͨ�����ε�Ƶ�ʲ���ֵ
scpi_result_t MEAS_RISQ(scpi_t * context);//��ѯָ��ͨ�����ε�����ʱ�����ֵ
scpi_result_t MEAS_FALLQ(scpi_t * context);//��ѯָ��ͨ�����ε��½�ʱ�����ֵ
scpi_result_t MEAS_DELQ(scpi_t * context);//��ѯͨ�����ӳٲ����Ľ��
scpi_result_t MEAS_PDUTQ(scpi_t * context);//��ѯָ��ͨ�����ε���ռ�ձȲ���ֵ
scpi_result_t MEAS_NDUTQ(scpi_t * context);//��ѯָ��ͨ�����εĸ�ռ�ձȲ���ֵ
scpi_result_t MEAS_PWIDQ(scpi_t * context);//��ѯָ��ͨ�����ε����������ֵ
scpi_result_t MEAS_NWIDQ(scpi_t * context);//��ѯָ��ͨ�����εĸ��������ֵ
scpi_result_t MEAS_BURSQ(scpi_t * context);//��ѯָ��ͨ�����ε�ͻ�������Ȳ���ֵ
scpi_result_t MEAS_POVQ(scpi_t * context); //��ѯָ��ͨ�����ε����򳬵�����ֵ
scpi_result_t MEAS_NOVQ(scpi_t * context); //��ѯָ��ͨ�����εĸ��򳬵�����ֵ
scpi_result_t MEAS_PHASQ(scpi_t * context);//��ѯָ��ͨ������λ������Ľ��
scpi_result_t MEAS_PKPKQ(scpi_t * context);//��ѯָ��ͨ�����εķ��ֵ
scpi_result_t MEAS_AMPQ(scpi_t * context);//��ѯָ��ͨ�����εķ��Ȳ���ֵ
scpi_result_t MEAS_HIGHQ(scpi_t * context); //��ѯָ��ͨ�����εĸ�ֵ
scpi_result_t MEAS_LOWQ(scpi_t * context);//��ѯָ��ͨ�����εĵ�ֵ
scpi_result_t MEAS_MAXQ(scpi_t * context);//��ѯָ��ͨ�����ε����ֵ
scpi_result_t MEAS_MINQ(scpi_t * context);//��ѯָ��ͨ�����ε���Сֵ
scpi_result_t MEAS_RMSQ(scpi_t * context);//��ѯָ��ͨ�����εľ�����ֵ
scpi_result_t MEAS_CRMSQ(scpi_t * context);//��ѯָ��ͨ�����ε����ھ�����ֵ
scpi_result_t MEAS_MEANQ(scpi_t * context); //��ѯָ��ͨ�����ε�ƽ��ֵ
scpi_result_t MEAS_CMEQ(scpi_t * context);//��ѯָ��ͨ�����ε�����ƽ��ֵ
scpi_result_t MEAS_ACRMQ(scpi_t * context);
scpi_result_t MEAS_PRATQ(scpi_t * context);
scpi_result_t MEAS_NRATQ(scpi_t * context);
scpi_result_t MEAS_COLVQ(scpi_t * context);
scpi_result_t MEAS_AREQ(scpi_t * context);//��ѯָ��ͨ�����ε����
scpi_result_t MEAS_CARQ(scpi_t * context);//��ѯָ��ͨ�����ε��������
scpi_result_t MEAS_CLE(scpi_t * context);//����򿪵Ĳ������е���һ���������
scpi_result_t MEAS_CLOS(scpi_t * context);//�رղ�����
scpi_result_t MEAS_OPEN(scpi_t * context);//�򿪲�����
scpi_result_t MEAS_ADIS(scpi_t * context);//�򿪻�ر�ȫ������
scpi_result_t MEAS_ADISQ(scpi_t * context);//��ѯȫ�������򿪻�ر�
scpi_result_t MEAS_SCOP(scpi_t * context);//���ò�����Χ
scpi_result_t MEAS_SCOPQ(scpi_t * context); //��ѯ������Χ
//1.1��������
scpi_result_t MEAS_COUNTER_SOUR(scpi_t * context);  //����Դ
scpi_result_t MEAS_COUNTER_SOURQ(scpi_t * context); //��ѯԴ
scpi_result_t MEAS_COUNTER_MODE(scpi_t * context);
scpi_result_t MEAS_COUNTER_MODEQ(scpi_t * context);
scpi_result_t MEAS_COUNTER_VALQ(scpi_t* context); //��ѯƵ�ʼ�
scpi_result_t MEAS_ITEM(scpi_t* context);   //������Դ
scpi_result_t MEAS_ITEMQ(scpi_t* context);  //��ѯ��Դ

scpi_result_t MEAS_TVALUE(scpi_t* context);
scpi_result_t MEAS_TVALUEQ(scpi_t* context);


scpi_result_t MEAS_STAT_DISP(scpi_t * context); //�򿪻�ر�ͳ�ƹ���
scpi_result_t MEAS_STAT_DISPQ(scpi_t * context);//��ѯͳ�ƹ��ܴ򿪻�ر�
scpi_result_t MEAS_STAT_RES(scpi_t * context);//�����ʷͳ�����ݲ�����ͳ��
scpi_result_t  MEAS_STAT_MEAN(scpi_t * context);
scpi_result_t  MEAS_STAT_MEANQ(scpi_t * context);
scpi_result_t  MEAS_STAT_MAX(scpi_t * context);
scpi_result_t  MEAS_STAT_MAXQ(scpi_t * context);
scpi_result_t  MEAS_STAT_MIN(scpi_t * context);
scpi_result_t  MEAS_STAT_MINQ(scpi_t * context);
scpi_result_t  MEAS_STAT_DEV(scpi_t * context);
scpi_result_t  MEAS_STAT_DEVQ(scpi_t * context);
scpi_result_t  MEAS_STAT_COUNT(scpi_t * context);
scpi_result_t  MEAS_STAT_COUNTQ(scpi_t * context);
scpi_result_t  MEAS_STAT_VIEWQ(scpi_t * context);
scpi_result_t  MEAS_STAT_MEAN_VIEWQ(scpi_t * context);
scpi_result_t  MEAS_STAT_MAX_VIEWQ(scpi_t * context);
scpi_result_t  MEAS_STAT_MIN_VIEWQ(scpi_t * context);
scpi_result_t  MEAS_STAT_DEV_VIEWQ(scpi_t * context);
scpi_result_t  MEAS_STAT_COUNT_VIEWQ(scpi_t * context);
scpi_result_t  MEAS_STAT_CURRENT_VIEWQ(scpi_t * context);


scpi_result_t MEAS_SETTING_IND(scpi_t* context);
scpi_result_t MEAS_SETTING_INDQ(scpi_t* context);
scpi_result_t MEAS_SETTING_RANGE(scpi_t* context);
scpi_result_t MEAS_SETTING_RANGEQ(scpi_t* context);
scpi_result_t MEAS_SETTING_THRESHOLD(scpi_t* context);
scpi_result_t MEAS_SETTING_THRESHOLDQ(scpi_t* context);
scpi_result_t MEAS_SETTING_HIGH(scpi_t* context);
scpi_result_t MEAS_SETTING_HIGHQ(scpi_t* context);
scpi_result_t MEAS_SETTING_MID(scpi_t* context);
scpi_result_t MEAS_SETTING_MIDQ(scpi_t* context);
scpi_result_t MEAS_SETTING_LOW(scpi_t* context);
scpi_result_t MEAS_SETTING_LOWQ(scpi_t* context);

scpi_result_t MEAS_LISTQ(scpi_t* context);
scpi_result_t MEAS_ADDNEW(scpi_t* context);
scpi_result_t MEAS_DELETE(scpi_t* context);
scpi_result_t MEAS_MEASX_TYPE(scpi_t* context);
scpi_result_t MEAS_MEASX_TYPEQ(scpi_t* context);
scpi_result_t MEAS_MEASX_SOURCE1(scpi_t* context);
scpi_result_t MEAS_MEASX_SOURCE1Q(scpi_t* context);
scpi_result_t MEAS_MEASX_SOURCE2(scpi_t* context);
scpi_result_t MEAS_MEASX_SOURCE2Q(scpi_t* context);
scpi_result_t MEAS_MEASX_VALUEQ(scpi_t* context);
scpi_result_t MEAS_MEASX_UNITQ(scpi_t* context);
scpi_result_t MEAS_MEASX_VALIDQ(scpi_t* context);
scpi_result_t MEAS_MEASX_EDGE1(scpi_t* context);
scpi_result_t MEAS_MEASX_EDGE1Q(scpi_t* context);
scpi_result_t MEAS_MEASX_EDGE2(scpi_t* context);
scpi_result_t MEAS_MEASX_EDGE2Q(scpi_t* context);
scpi_result_t MEAS_MEASX_VVLUE(scpi_t* context);
scpi_result_t MEAS_MEASX_VVLUEQ(scpi_t* context);
scpi_result_t MEAS_MEASX_CURSOR(scpi_t* context);
scpi_result_t MEAS_MEASX_CURSORQ(scpi_t* context);

double getValue(int idx, int typ, int delay);//����λ���ӳ��⣬��������������
#ifdef  __cplusplus
}
#endif

#endif // SCPI_CMD_MEAS_H
