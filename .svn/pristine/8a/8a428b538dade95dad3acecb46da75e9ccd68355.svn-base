#ifndef SCPI_HELP_H
#define SCPI_HELP_H

#include "../inc/scpi.h"

#ifdef _TBOOK_DEBUG_//for debug
    #define DEBUG_RETURN(str)   SCPI_ResultString(context,str)
#else
    #define DEBUG_RETURN(str)
#endif

//#define SCPI_CLOSE_MENU     gMenuLogic[0]->closeMenu()//���ز˵�
//
//#define ERROR_XY_MODE       if(gMainWindow->isInXYMode()){\
//                                return SCPI_RES_ERR;}
//#define ERROR_XY_MODEQ      if(gMainWindow->isInXYMode()){\
//                                return SCPI_RES_ERR;}
//#define ERROR_ROLL_MODE     if(gMainWindow->IsScrollMode()){\
//                                return SCPI_RES_ERR;}
//
//#define SCPI_CHECK_TRIG_TYPE(t,bReturn) {\
//                                            IDescTriggerCommon &objTriggerCommon = IDescTrigger::triggerDesc(TRIG_TYPE_START_ID);\
//                                            TriggerType trigType = objTriggerCommon.triggerType(0);\
//                                            if(trigType!=t)\
//                                            {\
//                                                if(bReturn==true)\
//                                                {\
//                                                    DEBUG_RETURN("ERROR TRIG TYPE!");\
//                                                }\
//                                                return SCPI_RES_ERR;\
//                                            }\
//                                        }
//
//#define SCPI_GET_PARAM1_CHIDX       CH_IDX idx=CI_NONE;\
//                                    {   \
//                                        int param1;\
//                                        if (!SCPI_ParamChoice(context, allCh, &param1, true)) {\
//                                            return SCPI_RES_ERR;\
//                                        }\
//                                        idx = (CH_IDX)getCh(param1);\
//                                        if(!Channel::isDynamicCh(idx)) {\
//                                            return SCPI_RES_ERR;\
//                                        }\
//                                    }
//
//#define  SCPI_CHECK_CONDITION(trigTp,con,bReturn)  {\
//                                                CTriggerGetMsg *cgetm = CTriggerGetMsg::Instance();\
//                                                paramCondition conditionValue(con);\
//                                                cgetm->exec(TR_CONDITION, &conditionValue, trigTp);\
//                                                if(conditionValue.cnd!=con)\
//                                                {\
//                                                    if(bReturn==true)\
//                                                    {\
//                                                        DEBUG_RETURN("ERROR CONDITION!");\
//                                                    }\
//                                                    return SCPI_RES_ERR;\
//                                                }\
//                                            }
//
//#define SCPI_GET_MEASURE_IDX    {\
//                                    int  param1;\
//                                    if (SCPI_ParamChoice(context, allCh, &param1, true))\
//                                    {\
//                                        idx = (CH_IDX)getCh(param1);\
//                                        if(!Channel::isDynamicCh(idx) && !isRefCh(idx) && idx!=CI_MATH)\
//                                        {\
//                                            DEBUG_RETURN("ERROR CH!");\
//                                            return SCPI_RES_ERR;\
//                                        }\
//                                    }\
//                                }

void SEND_RECEIVE_CMD(scpi_t * context);//��֪�ͻ����յ�����ҽ�����ȷ����ʱֻ���У׼����


#define SCPI_SET_BETWEEN_VALUE(src,small,big) {\
                                                    if(src<small)src=small;\
                                                    if(src>big)src=big;\
                                                }

#define SCPI_MEASURE_PARAM    do{\
                                int param2;\
                                if (SCPI_ParamChoice(context, allCh, &param2, true))\
                                {\
                                }\
                                setParam_2Int(context->env,context->param,param1,param2);\
                                dealCallBack(context->env,context->obj,context->param,context->scpi_command_index);\
                           }while(0)

#define SCPI_OPEN_MEASURE(tp)       {\
                                    int param2;\
                                IModuleChannel *mc = dynamic_cast<IModuleChannel *>(\
                                    ProcessCenter::IModuleRegisterDesk::module(\
                                    ProcessCenter::MI_CHANNEL));\
                                    CH_IDX ch = mc->active();\
                                    if (SCPI_ParamChoice(context, allCh, &param2, true))\
                                    {\
                                        printf("get param2 %d\n",param2);\
                                        ch = (CH_IDX)getCh(param2);\
                                    }\
                                    gMainWindow->SCPI_OpenMeasure(tp,ch,CI_NONE);\
                                }


//#define SCPI_CLOSE_MEASURE(tp)       {\
//                                    int param2;\
//                                IModuleChannel *mc = dynamic_cast<IModuleChannel *>(\
//                                    ProcessCenter::IModuleRegisterDesk::module(\
//                                    ProcessCenter::MI_CHANNEL));\
//                                    CH_IDX ch = mc->active();\
//                                    if (SCPI_ParamChoice(context, allCh, &param2, true))\
//                                    {\
//                                        printf("get param2 %d\n",param2);\
//                                        ch = (CH_IDX)getCh(param2);\
//                                    }\
//                                    gMainWindow->SCPI_CloseMeasure(tp,ch);\
//                                }
int getCh(int index);
void dealCallBack_ParamError(scpi_t * context);
extern const char* allCh[];
extern const char* WaveMode[];
#endif // SCPI_HELP_H
