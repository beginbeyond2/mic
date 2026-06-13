#ifndef __SCPI_CMD_CALIBRATE_H_
#define __SCPI_CMD_CALIBRATE_H_

#include "../inc/scpi.h"

#ifdef  __cplusplus
extern "C" {
#endif
//extern scpi_t scpi_context;

scpi_result_t CAL_DATEQ(scpi_t * context);//��ѯ�ϴ�У׼ʱ��
scpi_result_t CAL_STAR(scpi_t * context);//��ʼУ׼
scpi_result_t CAL_QUIT(scpi_t * context);//�˳�У׼
scpi_result_t CAL_STOP(scpi_t * context);//ֹͣУ׼��ǿ��ֹͣ
scpi_result_t CAL_RESQ(scpi_t * context);//��ѯУ׼���
scpi_result_t CAL_ZER(scpi_t * context);//���У׼
scpi_result_t CAL_ZERQ(scpi_t * context);//��ѯ���У׼״̬
scpi_result_t CAL_CHDF(scpi_t * context);//ͨ������У׼
scpi_result_t CAL_CHDFQ(scpi_t * context);//��ѯͨ������У׼״̬
scpi_result_t CAL_ADPH(scpi_t * context);//AD��λУ׼
scpi_result_t CAL_ADPHQ(scpi_t * context);//��ѯAD��λУ׼״̬
scpi_result_t CAL_ADG(scpi_t * context);//AD����У׼
scpi_result_t CAL_ADGQ(scpi_t * context);//��ѯAD����У׼״̬
scpi_result_t CAL_OFFS(scpi_t * context);//ƫ����У׼
scpi_result_t CAL_OFFSQ(scpi_t * context);//��ѯƫ����У׼״̬
scpi_result_t CAL_CHG(scpi_t * context);//ͨ������У׼
scpi_result_t CAL_CHGQ(scpi_t * context);//��ѯͨ������У׼״̬
scpi_result_t CAL_ExCHG(scpi_t * context);//ͨ������У׼
scpi_result_t CAL_ExCHGQ(scpi_t * context);//��ѯͨ������У׼״̬
scpi_result_t CAL_CHSetV(scpi_t * context);
scpi_result_t CAL_CHVQ(scpi_t * context);//��ѯͨ������У׼״̬
scpi_result_t CAL_CHCofit(scpi_t * context);//ͨ������У׼
scpi_result_t CAL_CHCofitQ(scpi_t * context);//��ѯͨ������У׼״̬
scpi_result_t CAL_CHCap(scpi_t * context);//ͨ������У׼
scpi_result_t CAL_CHCapQ(scpi_t * context);//��ѯͨ������У׼״̬
scpi_result_t CAL_CapVal(scpi_t * context);//ͨ������У׼
scpi_result_t CAL_CapValQ(scpi_t * context);//��ѯͨ������У׼״̬
scpi_result_t CAL_UPCal(scpi_t * context);//ͨ������У׼
scpi_result_t CAL_UPCalQ(scpi_t * context);//��ѯͨ������У׼״̬
scpi_result_t CAL_DOWNCal(scpi_t * context);//ͨ������У׼
scpi_result_t CAL_DOWNCalQ(scpi_t * context);//��ѯͨ������У׼״̬
scpi_result_t CAL_TRIG_ZER(scpi_t * context);//�����������У׼
scpi_result_t CAL_TRIG_ZERQ(scpi_t * context);//��ѯ�������У׼״̬
scpi_result_t CAL_TRIG_ZERAC(scpi_t * context);//�����������У׼
scpi_result_t CAL_TRIG_ZERACQ(scpi_t * context);//��ѯ�������У׼״̬
scpi_result_t CAL_TRIG_COEF(scpi_t * context);//����ϵ��У׼
scpi_result_t CAL_TRIG_COEFQ(scpi_t * context);//��ѯ����ϵ��У׼״̬
scpi_result_t CAL_TRIG_PREC(scpi_t * context);//��׼����У׼
scpi_result_t CAL_TRIG_PRECQ(scpi_t * context);//��ѯ��׼����У׼״̬
scpi_result_t CAL_DATA_LENGQ(scpi_t * context);//��ѯУ׼���ݳ���
scpi_result_t CAL_DATE_GET(scpi_t * context);//��ȡУ׼����
scpi_result_t CAL_FILE_RESQ(scpi_t * context);//��λУ׼��Ϣ(ɾ��У׼�ļ�)


#ifdef  __cplusplus
}
#endif

#endif // __SCPI_CMD_CALIBRATE_H_
