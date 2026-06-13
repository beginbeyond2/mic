#ifndef SCPI_CMD_TRIG_H
#define SCPI_CMD_TRIG_H

#include "../inc/scpi.h"

#ifdef  __cplusplus
extern "C" {
#endif

void getLevelPos(scpi_t * context, int ch);//��ȡ������ƽλ�ã���λV
void setLevelPos(scpi_t * context, int ch, double param1);//���ô�����ƽλ�ã���λV
void setHLevelPos(scpi_t * context, int ch, double param1);//���øߴ�����ƽλ�ã���λV
void setTrigSource(int idx, int trigTp);//���ô���Դ,�޵�����Դʹ��
void setAnotherLev(int idx, int levTp);//��������һ��������ƽλ�ã���λV��dwart slope

scpi_result_t querySerial(scpi_t * context);


scpi_result_t TRIG_TYPE(scpi_t * context);  //ѡ�񴥷�����
scpi_result_t TRIG_TYPEQ(scpi_t * context);  //��ѯ���ص�ǰʹ�õĴ�������
scpi_result_t TRIG_HOLD(scpi_t * context);  //���ô�������ʱ��
scpi_result_t TRIG_HOLDQ(scpi_t * context);  //��ѯ�Կ�ѧ������ʽ���ش�������ʱ��
scpi_result_t TRIG_MODE(scpi_t * context);  //���ô�����ʽ���Զ�����ͨ
scpi_result_t TRIG_MODEQ(scpi_t * context);  //��ѯ������ʽ
scpi_result_t TRIG_STATQ(scpi_t * context);  //��ѯ��ǰ�Ĵ���״̬
scpi_result_t TRIG_IS_EXTERNAL_TRIGGERQ(scpi_t* context);
scpi_result_t TRIG_IS_EXTERNAL_CLOCKQ(scpi_t* context);
scpi_result_t TRIG_HAS_EXTERNAL_DIALOGQ(scpi_t* context);
scpi_result_t TRIG_EXTERNAL_DIALOG_SET(scpi_t* context);
//Trigger edge
scpi_result_t TRIG_EDGE_SOUR(scpi_t * context);  //ѡ����ش����Ĵ���Դ
scpi_result_t TRIG_EDGE_SOURQ(scpi_t * context);  //��ѯ���ش����Ĵ���Դ
scpi_result_t TRIG_EDGE_SLOP(scpi_t * context);  //ѡ����ش����ı�������
scpi_result_t TRIG_EDGE_SLOPQ(scpi_t * context);  //��ѯ���ش����ı�������
scpi_result_t TRIG_EDGE_LEV(scpi_t * context);  //���ñ��ش���ʱ�Ĵ�����ƽ
scpi_result_t TRIG_EDGE_PLUS_LEV(scpi_t * context);  //���ñ��ش���ʱ�Ĵ�����ƽ
scpi_result_t TRIG_EDGE_LEVQ(scpi_t * context);  //��ѯ���ش���ʱ�Ĵ�����ƽ
scpi_result_t TRIG_EDGE_COUP(scpi_t * context);  //���ñ��ش�����Ϸ�ʽ��
scpi_result_t TRIG_EDGE_COUPQ(scpi_t * context);  //��ѯ���ش�����Ϸ�ʽ��
//Trigger pulse
scpi_result_t TRIG_PULS_SOUR(scpi_t * context);  //ѡ���������Ĵ���Դ
scpi_result_t TRIG_PULS_SOURQ(scpi_t * context);  //��ѯ�������Ĵ���Դ
scpi_result_t TRIG_PULS_POL(scpi_t * context);  //�����������ļ���
scpi_result_t TRIG_PULS_POLQ(scpi_t * context);  //��ѯ�������ļ���
scpi_result_t TRIG_PULS_WIDT(scpi_t * context);  //����������ʱ��������ֵ
scpi_result_t TRIG_PULS_WIDTQ(scpi_t * context);  //��ѯ������ʱ��������ֵ
scpi_result_t TRIG_PULS_COND(scpi_t * context);  //��������������
scpi_result_t TRIG_PULS_CONDQ(scpi_t * context);  //��ѯ����������
scpi_result_t TRIG_PULS_LEV(scpi_t * context);  //����������ʱ�Ĵ�����ƽ
scpi_result_t TRIG_PULS_PLUS_LEV(scpi_t * context);  //����������ʱ�Ĵ�����ƽ
scpi_result_t TRIG_PULS_LEVQ(scpi_t * context);  //��ѯ������ʱ�Ĵ�����ƽ
//Trigger logic
scpi_result_t TRIG_LOG_STAT(scpi_t * context);  //�����߼�������ͨ�����߼�״̬
scpi_result_t TRIG_LOG_STATQ(scpi_t * context);  //��ѯ�߼�������ͨ�����߼�״̬
scpi_result_t TRIG_LOG_FUNC(scpi_t * context);  //�����߼������ıȽϺ���
scpi_result_t TRIG_LOG_FUNCQ(scpi_t * context);  //��ѯ�߼������ıȽϺ���
scpi_result_t TRIG_LOG_COND(scpi_t * context);  //�����߼���������
scpi_result_t TRIG_LOG_CONDQ(scpi_t * context);  //��ѯ�߼���������
scpi_result_t TRIG_LOG_TIME(scpi_t * context);  //���ô����߼�ʱ��
scpi_result_t TRIG_LOG_TIMEQ(scpi_t * context);  //��ѯ�����߼�ʱ��
scpi_result_t TRIG_LOG_LEV(scpi_t * context);  //�����߼�����ʱ�ĸ�ͨ��������ƽ
scpi_result_t TRIG_LOG_PLUS_LEV(scpi_t * context);  //�����߼�����ʱ�ĸ�ͨ��������ƽ
scpi_result_t TRIG_LOG_LEVQ(scpi_t * context);  //��ѯ�߼�����ʱ�ĸ�ͨ��������ƽ
//Trigger B
scpi_result_t TRIG_B_SOUR(scpi_t * context);   //ѡ��B�����Ĵ���Դ
scpi_result_t TRIG_B_SOURQ(scpi_t * context);   //��ѯB�����Ĵ���Դ
scpi_result_t TRIG_B_EDGE(scpi_t * context);   //����B�����Ĵ���б��
scpi_result_t TRIG_B_EDGEQ(scpi_t * context);   //��ѯB�����Ĵ���б��
scpi_result_t TRIG_B_COUP(scpi_t * context);   //ѡ��B������Ϸ�ʽ
scpi_result_t TRIG_B_COUPQ(scpi_t * context);   //��ѯB������Ϸ�ʽ
scpi_result_t TRIG_B_SEQ(scpi_t * context);   //����B�����Ĵ������ͣ�B��A�󴥷�ʱ��/�¼���
scpi_result_t TRIG_B_SEQQ(scpi_t * context);   //��ѯB�����Ĵ�������
scpi_result_t TRIG_B_LEV(scpi_t * context);   //����B����ʱ�Ĵ�����ƽ
scpi_result_t TRIG_B_LEVQ(scpi_t * context);   //��ѯB����ʱ�Ĵ�����ƽ
//Trigger dwart
scpi_result_t TRIG_DWAR_SOUR(scpi_t * context);   //���ð��������Ĵ���Դ
scpi_result_t TRIG_DWAR_SOURQ(scpi_t * context);   //��ѯ���������Ĵ���Դ
scpi_result_t TRIG_DWAR_POL(scpi_t * context);   //���ð������������弫��
scpi_result_t TRIG_DWAR_POLQ(scpi_t * context);   //��ѯ�������������弫��
scpi_result_t TRIG_DWAR_COND(scpi_t * context);   //����������������
scpi_result_t TRIG_DWAR_CONDQ(scpi_t * context);   //��ѯ������������
scpi_result_t TRIG_DWAR_HTIM(scpi_t * context);  //���ð�������ʱ��ʱ������
scpi_result_t TRIG_DWAR_HTIMQ(scpi_t * context);  //��ѯ��������ʱ��ʱ������
scpi_result_t TRIG_DWAR_LTIM(scpi_t * context);  //���ð�������ʱ��ʱ������
scpi_result_t TRIG_DWAR_LTIMQ(scpi_t * context);  //��ѯ��������ʱ��ʱ������
scpi_result_t TRIG_DWAR_BTIM(scpi_t * context);  //���ð�������ʱ��ʱ������
scpi_result_t TRIG_DWAR_BTIMQ(scpi_t * context);  //��ѯ��������ʱ��ʱ�����޻�����
scpi_result_t TRIG_DWAR_HLEV(scpi_t * context);   //���ð�������ʱ�ĸߵ�ƽ
scpi_result_t TRIG_DWAR_PLUS_HLEV(scpi_t * context);   //���ð�������ʱ�ĸߵ�ƽ
scpi_result_t TRIG_DWAR_HLEVQ(scpi_t * context);   //��ѯ��������ʱ�ĸߵ�ƽ
scpi_result_t TRIG_DWAR_LLEV(scpi_t * context);   //���ð�������ʱ�ĵ͵�ƽ
scpi_result_t TRIG_DWAR_PLUS_LLEV(scpi_t * context);   //���ð�������ʱ�ĵ͵�ƽ
scpi_result_t TRIG_DWAR_LLEVQ(scpi_t * context);   //��ѯ��������ʱ�ĵ͵�ƽ
//Trgger slope
scpi_result_t TRIG_SLOP_SOUR(scpi_t * context);   //����б�ʴ����Ĵ���Դ
scpi_result_t TRIG_SLOP_SOURQ(scpi_t * context);  //��ѯб�ʴ����Ĵ���Դ
scpi_result_t TRIG_SLOP_EDGE(scpi_t * context);  //����б�ʴ�����
scpi_result_t TRIG_SLOP_EDGEQ(scpi_t * context);   //��ѯб�ʴ�����
scpi_result_t TRIG_SLOP_COND(scpi_t * context);   //����б�ʴ�������������
scpi_result_t TRIG_SLOP_CONDQ(scpi_t * context);   //��ѯб�ʴ�������������
scpi_result_t TRIG_SLOP_HTIM(scpi_t * context);   //����б�ʴ���ʱ��ʱ������
scpi_result_t TRIG_SLOP_HTIMQ(scpi_t * context);   //��ѯб�ʴ���ʱ��ʱ������
scpi_result_t TRIG_SLOP_LTIM(scpi_t * context);   //����б�ʴ���ʱ��ʱ������
scpi_result_t TRIG_SLOP_LTIMQ(scpi_t * context);   //��ѯб�ʴ���ʱ��ʱ������
scpi_result_t TRIG_SLOP_BTIM(scpi_t * context);//����б�ʴ���ʱ��ʱ������
scpi_result_t TRIG_SLOP_BTIMQ(scpi_t * context);//��ѯб�ʴ���ʱ��ʱ�����޻�����
scpi_result_t TRIG_SLOP_HLEV(scpi_t * context);   //����б�ʴ���ʱ�ĸߵ�ƽ
scpi_result_t TRIG_SLOP_PLUS_HLEV(scpi_t * context);   //����б�ʴ���ʱ�ĸߵ�ƽ
scpi_result_t TRIG_SLOP_HLEVQ(scpi_t * context);   //��ѯб�ʴ���ʱ�ĸߵ�ƽ
scpi_result_t TRIG_SLOP_LLEV(scpi_t * context);   //����б�ʴ���ʱ�ĵ͵�ƽ
scpi_result_t TRIG_SLOP_PLUS_LLEV(scpi_t * context);   //����б�ʴ���ʱ�ĵ͵�ƽ
scpi_result_t TRIG_SLOP_LLEVQ(scpi_t * context);   //��ѯб�ʴ���ʱ�ĵ͵�ƽ
//Trigger timeout
scpi_result_t TRIG_TIM_SOUR(scpi_t * context);   //���ó�ʱ�����Ĵ���Դ
scpi_result_t TRIG_TIM_SOURQ(scpi_t * context);   //��ѯ��ʱ�����Ĵ���Դ
scpi_result_t TRIG_TIM_POL(scpi_t * context);   //���ó�ʱ��������
scpi_result_t TRIG_TIM_POLQ(scpi_t * context);   //��ѯ��ʱ��������
scpi_result_t TRIG_TIM_TIME(scpi_t * context);   //���ó�ʱ�����ĳ�ʱʱ��
scpi_result_t TRIG_TIM_TIMEQ(scpi_t * context);   //��ѯ��ʱ�����ĳ�ʱʱ��
scpi_result_t TRIG_TIM_LEV(scpi_t * context);
scpi_result_t TRIG_TIM_LEVQ(scpi_t * context);

//Trigger nedge
scpi_result_t TRIG_NEDG_SOUR(scpi_t * context);   //���õ�N���ش����Ĵ���Դ
scpi_result_t TRIG_NEDG_SOURQ(scpi_t * context);   //��ѯ��N���ش����Ĵ���Դ
scpi_result_t TRIG_NEDG_SLOP(scpi_t * context);   //���õ�N���ش����ı�������
scpi_result_t TRIG_NEDG_SLOPQ(scpi_t * context);   //��ѯ��N���ش����ı�������
scpi_result_t TRIG_NEDG_IDLE(scpi_t * context);   //���õ�N���ش����п�ʼ���ؼ���֮ǰ�Ŀ���ʱ��
scpi_result_t TRIG_NEDG_IDLEQ(scpi_t * context);   //��ѯ��N���ش����п�ʼ���ؼ���֮ǰ�Ŀ���ʱ��
scpi_result_t TRIG_NEDG_EDGE(scpi_t * context);   //���õ�N���ش�����N����ֵ
scpi_result_t TRIG_NEDG_EDGEQ(scpi_t * context);   //��ѯ��N���ش�����N����ֵ
scpi_result_t TRIG_NEDG_LEV(scpi_t * context);   //���õ�N���ش���ʱ�Ĵ�����ƽ
scpi_result_t TRIG_NEDG_PLUS_LEV(scpi_t * context);   //���õ�N���ش���ʱ�Ĵ�����ƽ
scpi_result_t TRIG_NEDG_LEVQ(scpi_t * context);   //��ѯ��N���ش���ʱ�Ĵ�����ƽ
//Trigger setup
scpi_result_t TRIG_SET_CLOC(scpi_t * context);   //���ý�������ʱ�䴥����ʱ���ź�Դ
scpi_result_t TRIG_SET_CLOCQ(scpi_t * context);   //��ѯ��������ʱ�䴥����ʱ���ź�Դ
scpi_result_t TRIG_SET_DATA(scpi_t * context);   //���ý�������ʱ�䴥���������ź�Դ
scpi_result_t TRIG_SET_DATAQ(scpi_t * context);   //��ѯ��������ʱ�䴥���������ź�Դ
scpi_result_t TRIG_SET_CEDG(scpi_t * context);   //���ý�������ʱ�䴥����ʱ�ӱ�������
scpi_result_t TRIG_SET_CEDGQ(scpi_t * context);   //��ѯ��������ʱ�䴥����ʱ�ӱ�������
scpi_result_t TRIG_SET_STIM(scpi_t * context);   //���ý�������ʱ�䴥���Ľ���ʱ��
scpi_result_t TRIG_SET_STIMQ(scpi_t * context);   //��ѯ��������ʱ�䴥���Ľ���ʱ��
scpi_result_t TRIG_SET_HTIM(scpi_t * context);   //���ý�������ʱ�䴥���ı���ʱ��
scpi_result_t TRIG_SET_HTIMQ(scpi_t * context);   //��ѯ��������ʱ�䴥���ı���ʱ��
scpi_result_t TRIG_SET_CLEV(scpi_t * context);   //���ý�������ʱ�䴥����ʱ��Դ������ƽ
scpi_result_t TRIG_SET_CLEVQ(scpi_t * context);   //��ѯ��������ʱ�䴥����ʱ��Դ������ƽ
scpi_result_t TRIG_SET_DLEV(scpi_t * context);   //���ý�������ʱ�䴥��������Դ������ƽ
scpi_result_t TRIG_SET_DLEVQ(scpi_t * context);   //��ѯ��������ʱ�䴥��������Դ������ƽ
//Trigger video
scpi_result_t TRIG_VID_SOUR(scpi_t * context);   //������Ƶ�����Ĵ���Դ
scpi_result_t TRIG_VID_SOURQ(scpi_t * context);   //��ѯ��Ƶ�����Ĵ���Դ
scpi_result_t TRIG_VID_POL(scpi_t * context);   //������Ƶ�����ļ���
scpi_result_t TRIG_VID_POLQ(scpi_t * context);   //��ѯ��Ƶ�����ļ���
scpi_result_t TRIG_VID_STAN(scpi_t * context);  //������Ƶ����ʱ����Ƶ��׼
scpi_result_t TRIG_VID_STANQ(scpi_t * context);   //��ѯ��Ƶ����ʱ����Ƶ��׼
scpi_result_t TRIG_VID_AMOD(scpi_t * context);   //���ô�����׼ΪPAL��SECAm��NESC��1080Iʱ��Ƶ������ͬ������
scpi_result_t TRIG_VID_AMODQ(scpi_t * context);   //��ѯ������׼ΪPAL��SECAm��NESC��1080Iʱ��Ƶ������ͬ������
scpi_result_t TRIG_VID_BMOD(scpi_t * context);   //���ô�����׼Ϊ720P��1080Pʱ��Ƶ������ͬ������
scpi_result_t TRIG_VID_BMODQ(scpi_t * context);   //��ѯ������׼Ϊ720P��1080Pʱ��Ƶ������ͬ������
scpi_result_t TRIG_VID_AFR(scpi_t * context);   //���ô�����׼Ϊ720P��1080Iʱ��Ƶ�������ź�Ƶ��
scpi_result_t TRIG_VID_AFRQ(scpi_t * context);   //��ѯ������׼Ϊ720P��1080Iʱ��Ƶ�������ź�Ƶ��
scpi_result_t TRIG_VID_BFR(scpi_t * context);   //���ô�����׼Ϊ1080Pʱ��Ƶ�������ź�Ƶ��
scpi_result_t TRIG_VID_BFRQ(scpi_t * context);   //��ѯ������׼Ϊ1080Pʱ��Ƶ�������ź�Ƶ��
scpi_result_t TRIG_VID_LINE(scpi_t * context);
scpi_result_t TRIG_VID_LINEQ(scpi_t * context);
//Trigger uart
scpi_result_t TRIG_UART_SOUR(scpi_t * context);    //����UART�����Ĵ���Դ
scpi_result_t TRIG_UART_SOURQ(scpi_t * context);    //��ѯUART�����Ĵ���Դ
scpi_result_t TRIG_UART_TYPE(scpi_t * context);    //����UART�����Ĵ�������
scpi_result_t TRIG_UART_TYPEQ(scpi_t * context);   //��ѯUART�����Ĵ�������
scpi_result_t TRIG_UART_REL(scpi_t * context);   //��UART���ߴ�������ѡ��ΪDATA��0:DATA��1:DATA��X:DATAʱ������UART���ߴ�����ϵ
scpi_result_t TRIG_UART_RELQ(scpi_t * context);   //��UART���ߴ�������ѡ��ΪDATA��0:DATA��1:DATA��X:DATAʱ����ѯUART���ߴ�����ϵ
scpi_result_t TRIG_UART_DATA(scpi_t * context);   //��UART���ߴ�������ѡ��ΪDATA��0:DATA��1:DATA��X:DATAʱ������UART���ߴ������ݡ�
scpi_result_t TRIG_UART_DATAQ(scpi_t * context);   //��UART���ߴ�������ѡ��ΪDATA��0:DATA��1:DATA��X:DATAʱ����ѯUART���ߴ������ݡ�
scpi_result_t TRIG_UART_LEV(scpi_t * context);   //����UART����ʱ�Ĵ�����ƽ
scpi_result_t TRIG_UART_LEVQ(scpi_t * context);   //��ѯUART����ʱ�Ĵ�����ƽ
//Trigger lin
scpi_result_t TRIG_LIN_SOUR(scpi_t * context);   //����LIN�����Ĵ���Դ
scpi_result_t TRIG_LIN_SOURQ(scpi_t * context);   //��ѯLIN�����Ĵ���Դ
scpi_result_t TRIG_LIN_TYPE(scpi_t * context);   //����LIN�����Ĵ�������
scpi_result_t TRIG_LIN_TYPEQ(scpi_t * context);   //��ѯLIN�����Ĵ�������
scpi_result_t TRIG_LIN_ID(scpi_t * context);   //��LIN���ߴ�������ΪFID��IDATaʱ������LIN�����Ĵ���IDֵ
scpi_result_t TRIG_LIN_IDQ(scpi_t * context);   //��LIN���ߴ�������ΪFID��IDATaʱ����ѯLIN�����Ĵ���IDֵ
scpi_result_t TRIG_LIN_DATA(scpi_t * context);   //��LIN���ߴ�������ΪIDATaʱ������LIN�����Ĵ�������
scpi_result_t TRIG_LIN_DATAQ(scpi_t * context);  //��LIN���ߴ�������ΪIDATaʱ����ѯLIN�����Ĵ�������
scpi_result_t TRIG_LIN_LEV(scpi_t * context);  //����LIN����ʱ�Ĵ�����ƽ
scpi_result_t TRIG_LIN_LEVQ(scpi_t * context);   //��ѯLIN����ʱ�Ĵ�����ƽ
//Trigger can
scpi_result_t TRIG_CAN_SOUR(scpi_t * context);   //����CAN�����Ĵ���Դ
scpi_result_t TRIG_CAN_SOURQ(scpi_t * context);   //��ѯCAN�����Ĵ���Դ
scpi_result_t TRIG_CAN_TYPE(scpi_t * context);   //����CAN�����Ĵ�������
scpi_result_t TRIG_CAN_TYPEQ(scpi_t * context);   //��ѯCAN�����Ĵ�������
scpi_result_t TRIG_CAN_ID(scpi_t * context);   //��CAN�����Ĵ�������ΪRFID��DFID��IDATa��RDIDʱ������CAN�����Ĵ���IDֵ
scpi_result_t TRIG_CAN_IDQ(scpi_t * context);   //��CAN�����Ĵ�������ΪRFID��DFID��IDATa��RDIDʱ����ѯCAN�����Ĵ���IDֵ
scpi_result_t TRIG_CAN_DLC(scpi_t * context);   //��CAN �����Ĵ�������ΪIDATaʱ������CAN������DLCֵ
scpi_result_t TRIG_CAN_DLCQ(scpi_t * context);   //��CAN �����Ĵ�������ΪIDATaʱ����ѯCAN������DLCֵ
scpi_result_t TRIG_CAN_DATA(scpi_t * context);   //��CAN �����Ĵ�������ΪIDATaʱ������CAN�����Ĵ�������ֵ
scpi_result_t TRIG_CAN_DATAQ(scpi_t * context);   //��CAN �����Ĵ�������ΪIDATaʱ����ѯCAN�����Ĵ�������ֵ
scpi_result_t TRIG_CAN_LEV(scpi_t * context);   //����CAN����ʱ�Ĵ�����ƽ
scpi_result_t TRIG_CAN_LEVQ(scpi_t * context);   //��ѯCAN����ʱ�Ĵ�����ƽ
//Trigger spi
scpi_result_t TRIG_SPI_TYPE(scpi_t * context);   //����SPI�����µ�����ֵ
scpi_result_t TRIG_SPI_TYPEQ(scpi_t * context);   //��ѯSPI�����µ�����ֵ
scpi_result_t TRIG_SPI_DATA(scpi_t * context);   //����SPI�����µ�����ֵ
scpi_result_t TRIG_SPI_DATAQ(scpi_t * context);   //��ѯSPI�����µ�����ֵ
scpi_result_t TRIG_SPI_SOUR(scpi_t * context);   //����SPI�����Ĵ���Դ
scpi_result_t TRIG_SPI_SOURQ(scpi_t * context);  //��ѯSPI�����Ĵ���Դ
scpi_result_t TRIG_SPI_CLKLEV(scpi_t * context);   //����SPI����ʱ�Ĵ�����ƽ
scpi_result_t TRIG_SPI_CLKLEVQ(scpi_t * context);   //��ѯSPI����ʱ�Ĵ�����ƽ
scpi_result_t TRIG_SPI_DATLEV(scpi_t * context);   //����SPI����ʱ�Ĵ�����ƽ
scpi_result_t TRIG_SPI_DATLEVQ(scpi_t * context);   //��ѯSPI����ʱ�Ĵ�����ƽ
scpi_result_t TRIG_SPI_CSLEV(scpi_t * context);   //����SPI����ʱ�Ĵ�����ƽ
scpi_result_t TRIG_SPI_CSLEVQ(scpi_t * context);   //��ѯSPI����ʱ�Ĵ�����ƽ
//Trigger iic
scpi_result_t TRIG_IIC_SOUR(scpi_t * context);   //����IIC�����Ĵ���Դ
scpi_result_t TRIG_IIC_SOURQ(scpi_t * context);   //��ѯIIC�����Ĵ���Դ
scpi_result_t TRIG_IIC_TYPE(scpi_t * context);   //����IIC�����Ĵ�������
scpi_result_t TRIG_IIC_TYPEQ(scpi_t * context);   //��ѯIIC�����Ĵ�������
scpi_result_t TRIG_IIC_ADDR(scpi_t * context);  //��IIC��������ΪNACKaddress��FRAM1��FRAM2ʱ������IIC���ߴ����Ĵ�����ַ
scpi_result_t TRIG_IIC_ADDRQ(scpi_t * context);  //��IIC��������ΪNACKaddress��FRAM1��FRAM2ʱ����ѯIIC���ߴ����Ĵ�����ַ
scpi_result_t TRIG_IIC_REL(scpi_t * context);   //��IIC��������ΪRDATaʱ������IIC���ߴ����Ĵ�����ϵ
scpi_result_t TRIG_IIC_RELQ(scpi_t * context);   //��IIC��������ΪRDATaʱ����ѯIIC���ߴ����Ĵ�����ϵ
scpi_result_t TRIG_IIC_DATA1(scpi_t * context);   //��IIC��������ΪRDATa��FRAM1��FRAM2ʱ������IIC���ߴ����Ĵ�������
scpi_result_t TRIG_IIC_DATA1Q(scpi_t * context);   //��IIC��������ΪRDATa��FRAM1��FRAM2ʱ����ѯIIC���ߴ����Ĵ�������
scpi_result_t TRIG_IIC_DATA2(scpi_t * context);   //��IIC��������ΪRDATa��FRAM1��FRAM2ʱ������IIC���ߴ����Ĵ�������
scpi_result_t TRIG_IIC_DATA2Q(scpi_t * context);   //��IIC��������ΪRDATa��FRAM1��FRAM2ʱ����ѯIIC���ߴ����Ĵ�������
scpi_result_t TRIG_IIC_LEVCLK(scpi_t * context);   //����IIC����ʱ�Ĵ�����ƽ
scpi_result_t TRIG_IIC_LEVCLKQ(scpi_t * context);   //��ѯIIC����ʱ�Ĵ�����ƽ
scpi_result_t TRIG_IIC_LEVDAT(scpi_t * context);   //����IIC����ʱ�Ĵ�����ƽ
scpi_result_t TRIG_IIC_LEVDATQ(scpi_t * context);   //��ѯIIC����ʱ�Ĵ�����ƽ

//1553B
scpi_result_t  TRIG_1553B_SOUR(scpi_t * context);
scpi_result_t  TRIG_1553B_SOURQ(scpi_t * context);
scpi_result_t  TRIG_1553B_TYPE(scpi_t * context);
scpi_result_t  TRIG_1553B_TYPEQ(scpi_t * context);
scpi_result_t  TRIG_1553B_CSWO(scpi_t * context);
scpi_result_t  TRIG_1553B_CSWOQ(scpi_t * context);
scpi_result_t  TRIG_1553B_DWOR(scpi_t * context);
scpi_result_t  TRIG_1553B_DWORQ(scpi_t * context);
scpi_result_t  TRIG_1553B_RTAD(scpi_t * context);
scpi_result_t  TRIG_1553B_RTADQ(scpi_t * context);
scpi_result_t  TRIG_1553B_LEV(scpi_t * context);
scpi_result_t  TRIG_1553B_LEVQ(scpi_t * context);
//429
scpi_result_t  TRIG_429_SOUR(scpi_t * context);
scpi_result_t  TRIG_429_SOURQ(scpi_t * context);
scpi_result_t  TRIG_429_TYPE(scpi_t * context);
scpi_result_t  TRIG_429_TYPEQ(scpi_t * context);
scpi_result_t  TRIG_429_WORD(scpi_t * context);
scpi_result_t  TRIG_429_WORDQ(scpi_t * context);
scpi_result_t  TRIG_429_LABEL(scpi_t * context);
scpi_result_t  TRIG_429_LABELQ(scpi_t * context);
scpi_result_t  TRIG_429_SDI(scpi_t * context);
scpi_result_t  TRIG_429_SDIQ(scpi_t * context);
scpi_result_t  TRIG_429_DATA(scpi_t * context);
scpi_result_t  TRIG_429_DATAQ(scpi_t * context);
scpi_result_t  TRIG_429_SSM(scpi_t * context);
scpi_result_t  TRIG_429_SSMQ(scpi_t * context);
scpi_result_t  TRIG_429_HLEV(scpi_t * context);
scpi_result_t  TRIG_429_HLEVQ(scpi_t * context);
scpi_result_t  TRIG_429_LLEV(scpi_t * context);
scpi_result_t  TRIG_429_LLEVQ(scpi_t * context);

#ifdef  __cplusplus
}
#endif

#endif // SCPI_CMD_TRIG_H
