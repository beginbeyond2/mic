#ifndef SCPI_CMD_CHAN_H
#define SCPI_CMD_CHAN_H

#include "../inc/scpi.h"

#ifdef  __cplusplus
extern "C" {
#endif


//1.1协议部分
scpi_result_t CHAN_NUM_DISP(scpi_t * context);//通道打开或关闭
scpi_result_t CHAN_NUM_INV(scpi_t * context); //打开或关闭通道的反相显示
scpi_result_t CHAN_NUM_PRTY(scpi_t * context); //设置通道的探针类型
scpi_result_t CHAN_NUM_PROB(scpi_t * context);  //设置探头的衰减比
scpi_result_t CHAN_NUM_COUP(scpi_t * context);  //设置通道输入耦合方式
scpi_result_t CHAN_NUM_SCAL(scpi_t * context);  //设置通道波形显示的垂直档位
scpi_result_t CHAN_NUM_POS(scpi_t * context);  //设置通道波形显示的垂直偏移
scpi_result_t CHAN_NUM_VERN(scpi_t * context); //打开或关闭指定通道的垂直档位微调功能
scpi_result_t CHAN_NUM_PC(scpi_t * context);   //获取通道波形到上位机
scpi_result_t CHAN_NUM_INP(scpi_t* context);   //设置阻抗

scpi_result_t CHAN_NUM_DISPQ(scpi_t * context); //查询通道打开或关闭
scpi_result_t CHAN_NUM_INVQ(scpi_t * context); //查询通道的反相显示
scpi_result_t CHAN_NUM_PRTYQ(scpi_t * context); //查询通道的探针类型
scpi_result_t CHAN_NUM_PROBQ(scpi_t * context);  //查询探头的衰减比
scpi_result_t CHAN_NUM_COUPQ(scpi_t * context);  //查询通道输入耦合方式
scpi_result_t CHAN_NUM_SCALQ(scpi_t * context);  //查询通道波形显示的垂直档位
scpi_result_t CHAN_NUM_POSQ(scpi_t * context);  //查询通道波形显示的垂直偏移
scpi_result_t CHAN_NUM_VERNQ(scpi_t * context); //查询指定通道的垂直档位微调功能
scpi_result_t CHAN_NUM_PCQ(scpi_t * context);   //查询通道波形到上位机状态
scpi_result_t CHAN_NUM_INPQ(scpi_t* context);   //设置阻抗

scpi_result_t CHAN_NUM_BAND(scpi_t* context);
scpi_result_t CHAN_NUM_BANDQ(scpi_t* context);
scpi_result_t CHAN_NUM_EXT(scpi_t* context);
scpi_result_t CHAN_NUM_PLUS_EXT(scpi_t* context);
scpi_result_t CHAN_NUM_EXTQ(scpi_t* context);
scpi_result_t CHAN_NUM_VREF(scpi_t* context);
scpi_result_t CHAN_NUM_VREFQ(scpi_t* context);
scpi_result_t CHAN_NUM_LAB(scpi_t* context);
scpi_result_t CHAN_NUM_LABQ(scpi_t* context);
scpi_result_t CHAN_NUM_LAB_CLEAR(scpi_t* context);
scpi_result_t CHAN_NUM_CURR(scpi_t* context);
scpi_result_t CHAN_NUM_DELAY(scpi_t* context);
scpi_result_t CHAN_NUM_DELAYQ(scpi_t* context);

//1.0 版SCPI协议
scpi_result_t CHAN_DISP(scpi_t * context);//通道的打开或关闭
scpi_result_t CHAN_DISPQ(scpi_t * context);//查询通道的打开或关闭
scpi_result_t CHAN_INV(scpi_t * context);//打开或关闭通道的反相显示
scpi_result_t CHAN_INVQ(scpi_t * context);//查询通道的反相显示
scpi_result_t CHAN_BAND(scpi_t * context);//设置通道的带宽限制
scpi_result_t CHAN_BANDQ(scpi_t * context);//查询通道的带宽限制
scpi_result_t CHAN_BAND_VALUEQ(scpi_t* context);
scpi_result_t CHAN_PRTY(scpi_t * context);//设置通道的探针类型
scpi_result_t CHAN_PRTYQ(scpi_t * context);//查询通道的探针类型
scpi_result_t CHAN_PROB(scpi_t * context);//设置探头的衰减比
scpi_result_t CHAN_PROBQ(scpi_t * context);//查询探头的衰减比
scpi_result_t CHAN_COUP(scpi_t * context);//设置通道输入耦合方式
scpi_result_t CHAN_COUPQ(scpi_t * context);//查询通道输入耦合方式
scpi_result_t CHAN_INP(scpi_t * context);//设置通道的输入阻抗
scpi_result_t CHAN_INPQ(scpi_t * context);//查询通道的输入阻抗
scpi_result_t CHAN_EXT(scpi_t * context);//设置指定通道波形显示的垂直档位
scpi_result_t CHAN_PLUS_EXT(scpi_t * context);//设置指定通道波形显示的垂直档位
scpi_result_t CHAN_EXTQ(scpi_t * context);//查询指定通道波形显示的垂直档位
scpi_result_t CHAN_POS(scpi_t * context);//设置指定通道波形显示的垂直偏移
scpi_result_t CHAN_PLUS_POS(scpi_t * context);//设置指定通道波形显示的垂直偏移
scpi_result_t CHAN_POSQ(scpi_t * context);//查询指定通道波形显示的垂直偏移
scpi_result_t CHAN_VERN(scpi_t * context);//打开或关闭指定通道的垂直档位微调功能
scpi_result_t CHAN_VERNQ(scpi_t * context);//查询指定通道的垂直档位微调功能的打开或关闭
scpi_result_t CHAN_VREF(scpi_t * context);//设置垂直展开基准
scpi_result_t CHAN_VREFQ(scpi_t * context);//查询垂直展开基准
scpi_result_t CHAN_LAB(scpi_t* context);
scpi_result_t CHAN_LABQ(scpi_t* context);
scpi_result_t CHAN_LAB_CLEAR(scpi_t* context);
scpi_result_t CHAN_DELAY(scpi_t* context);
scpi_result_t CHAN_DELAYQ(scpi_t* context);
scpi_result_t CHAN_OFFSET(scpi_t* context);
scpi_result_t CHAN_OFFSETQ(scpi_t* context);
scpi_result_t CHAN_COUNTQ(scpi_t* context);
scpi_result_t CHAN_CURR(scpi_t * context);//选择通道为活动通道
scpi_result_t CHAN_CURRQ(scpi_t * context);//返回当前活动通道
scpi_result_t CHAN_BAND_IS200MQ(scpi_t * context); //200M
scpi_result_t CHAN_BAND_MAXQ(scpi_t * context); //最大带宽
scpi_result_t CHAN_PROBE_INFOQ(scpi_t* constext);
//功能执行部分
scpi_result_t deal_CHAN_DISP(int chNo,bool param2);//通道打开或关闭
scpi_result_t deal_CHAN_INV(int chNo,bool param2); //打开或关闭通道的反相显示
scpi_result_t deal_CHAN_PRTY(int chNo,int param2); //设置通道的探针类型
scpi_result_t deal_CHAN_PROB(int chNo,double param2);  //设置探头的衰减比
scpi_result_t deal_CHAN_COUP(int chNo,int param2);  //设置通道输入耦合方式
scpi_result_t deal_CHAN_SCAL(int chNo,double param2);  //设置通道波形显示的垂直档位
scpi_result_t deal_CHAN_SCAL_PLUS(int chNo,int param2);  //设置通道波形显示的垂直档位
scpi_result_t deal_CHAN_POS(int chNo,double param2);  //设置通道波形显示的垂直偏移
scpi_result_t deal_CHAN_POS_PLUS(int chNo,int param2);  //设置通道波形显示的垂直偏移
scpi_result_t deal_CHAN_VERN(int chNo,bool param2); //打开或关闭指定通道的垂直档位微调功能
scpi_result_t deal_CHAN_PC(int chNo,int param2);   //获取通道波形到上位机
scpi_result_t deal_CHAN_BAND(int chNo,int param2,int param3); //设置带宽
scpi_result_t deal_ChAN_INP(int chNo,int param2);   //设置阻抗

scpi_result_t deal_CHAN_DISPQ(int chNo,scpi_t * context); //查询通道打开或关闭
scpi_result_t deal_CHAN_INVQ(int chNo,scpi_t * context); //查询通道的反相显示
scpi_result_t deal_CHAN_PRTYQ(int chNo,scpi_t * context); //查询通道的探针类型
scpi_result_t deal_CHAN_PROBQ(int chNo,scpi_t * context);  //查询探头的衰减比
scpi_result_t deal_CHAN_COUPQ(int chNo,scpi_t * context);  //查询通道输入耦合方式
scpi_result_t deal_CHAN_SCALQ(int chNo,scpi_t * context);  //查询通道波形显示的垂直档位
scpi_result_t deal_CHAN_POSQ(int chNo,scpi_t * context);  //查询通道波形显示的垂直偏移
scpi_result_t deal_CHAN_VERNQ(int chNo,scpi_t * context); //查询指定通道的垂直档位微调功能
scpi_result_t deal_CHAN_PCQ(int chNo,scpi_t * context);   //查询通道波形到上位机状态
scpi_result_t deal_CHAN_BANDQ(int chNo,scpi_t* context);//查询通道带宽
scpi_result_t deal_ChAN_INPQ(int chNo,int param2);   //查询阻抗


#ifdef  __cplusplus
}
#endif

#endif // SCPI_CMD_CHAN_H
