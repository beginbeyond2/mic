#ifndef SCPI_CMD_STOR_H
#define SCPI_CMD_STOR_H

#include "../inc/scpi.h"

#ifdef  __cplusplus
extern "C" {
#endif

scpi_result_t STOR_SAVE(scpi_t * context); //�洢ָ��ͨ���Ĳ��ε�ָ��λ��
scpi_result_t STOR_LOAD(scpi_t * context); //����ref
scpi_result_t STOR_CAPT(scpi_t * context); //��Ļ��ͼ
scpi_result_t STOR_CAPT_TIME(scpi_t * context);
scpi_result_t STOR_CAPT_TIMEQ(scpi_t * context);
scpi_result_t STOR_CAPT_INCOLOR(scpi_t * context);
scpi_result_t STOR_CAPT_INCOLORQ(scpi_t * context);
scpi_result_t STOR_CAPT_THUM(scpi_t * context);
scpi_result_t STOR_CAPT_THUMQ(scpi_t * context);
scpi_result_t STOR_CAPT_START(scpi_t * context);
scpi_result_t STOR_DEPT(scpi_t * context); //����ʾ�����洢���
scpi_result_t STOR_DEPTQ(scpi_t * context); //��ѯʾ�����洢���
scpi_result_t STOR_CONS(scpi_t * context);//�洢ʾ��������
scpi_result_t STOR_CONS_START(scpi_t * context);
scpi_result_t STOR_CONL(scpi_t * context);//����ʾ��������
scpi_result_t STOR_REC(scpi_t * context);//����ʾ����¼�ƹ��ܵĴ���ر�
scpi_result_t STOR_RECQ(scpi_t * context);//��ѯʾ����¼�ƹ��ܵĴ���ر�
scpi_result_t STOR_PLAY(scpi_t * context); //����ʾ�����طŹ��ܵĴ򿪺͹ر�
scpi_result_t STOR_PLAYQ(scpi_t * context); //��ѯʾ�����طŹ��ܵĴ򿪺͹ر�
scpi_result_t STOR_PLAY_SPE(scpi_t * context);//����ʾ�����طſ��ѡ��
scpi_result_t STOR_PLAY_SPEQ(scpi_t * context);//��ѯʾ�����طſ��ѡ��
scpi_result_t STOR_PLAY_BACK(scpi_t * context); //����ʾ�����طź���ѡ��
scpi_result_t STOR_PLAY_BACKQ(scpi_t * context);//��ѯʾ�����طź���ѡ��
scpi_result_t STOR_SAVE_SOUR(scpi_t * context);
scpi_result_t STOR_SAVE_SOURQ(scpi_t * context);
scpi_result_t STOR_SAVE_LOCA(scpi_t * context);
scpi_result_t STOR_SAVE_LOCAQ(scpi_t * context);
scpi_result_t STOR_SAVE_TYPE(scpi_t * context);
scpi_result_t STOR_SAVE_TYPEQ(scpi_t * context);
scpi_result_t STOR_SAVE_FIL(scpi_t * context);
scpi_result_t STOR_SAVE_FILQ(scpi_t * context);
scpi_result_t STOR_SAVE_START(scpi_t * context);
scpi_result_t STOR_SAVE_ALLS(scpi_t * context);
scpi_result_t STOR_SAVE_ALLSQ(scpi_t * context);

scpi_result_t STOR_DATA_TYPE(scpi_t * context);
scpi_result_t STOR_DATA_STATUSQ(scpi_t * context);
scpi_result_t STOR_DATA_CSVQ(scpi_t * context);
scpi_result_t STOR_DATA_PNGQ(scpi_t * context);
scpi_result_t STOR_DATA_MSSQ(scpi_t * context);



#ifdef  __cplusplus
}
#endif

#endif // SCPI_CMD_STOR_H
