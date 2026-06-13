#ifndef SCPI_CMD_WAV_H
#define SCPI_CMD_WAV_H

#include "../inc/scpi.h"

#ifdef  __cplusplus
extern "C" {
#endif


scpi_result_t  WAV_SOUR(scpi_t * context);//���ò��ζ�ȡ��ͨ��Դ
scpi_result_t  WAV_SOURQ(scpi_t * context);//��ѯ���ζ�ȡ��ͨ��Դ
scpi_result_t  WAV_MODE(scpi_t * context);//���ò��εĶ�ȡģʽ
scpi_result_t  WAV_MODEQ(scpi_t * context);//��ѯ���εĶ�ȡģʽ
scpi_result_t  WAV_FORMAT(scpi_t * context);
scpi_result_t  WAV_FORMATQ(scpi_t * context);
scpi_result_t  WAV_STAR(scpi_t * context);//�����ڴ��в��α���ȡ����ʼλ��
scpi_result_t  WAV_STARQ(scpi_t * context);//��ѯ�ڴ��в��α���ȡ����ʼλ��
scpi_result_t  WAV_STOP(scpi_t * context);//�����ڴ��в��α���ȡ��ֹͣλ��
scpi_result_t  WAV_STOPQ(scpi_t * context);//��ѯ�ڴ��в��α���ȡ��ֹͣλ��
scpi_result_t  WAV_DATA(scpi_t * context);//��ȡ��������
scpi_result_t  WAV_DATA_BINQ(scpi_t* context);
scpi_result_t  WAV_DATA_HEXQ(scpi_t* context);
scpi_result_t  WAV_DATA_ASCIIQ(scpi_t* context);
scpi_result_t  WAV_PREQ(scpi_t * context);//��ѯȫ���Ĳ��β���
scpi_result_t  WAV_XINCQ(scpi_t * context);//��ѯָ��Դx���������������ʱ���
scpi_result_t  WAV_XORQ(scpi_t * context);//��ѯָ��Դx����Ӵ����㵽�ο�ʱ���׼��ʱ��
scpi_result_t  WAV_XREFQ(scpi_t * context);//��ѯָ��Դx���������ݵ�Ĳο�ʱ���׼
scpi_result_t  WAV_YINCQ(scpi_t * context);//��ѯָ��Դy���������������ʱ���
scpi_result_t  WAV_YORQ(scpi_t * context);//��ѯָ��Դy����Ӵ����㵽�ο�ʱ���׼��ʱ��
scpi_result_t  WAV_YREFQ(scpi_t * context);//��ѯָ��Դy���������ݵ�Ĳο�ʱ���׼
#ifdef  __cplusplus
}
#endif

#endif // SCPI_CMD_WAV_H
