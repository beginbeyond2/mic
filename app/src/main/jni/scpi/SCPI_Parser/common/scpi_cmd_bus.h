#ifndef SCPI_CMD_BUS_H
#define SCPI_CMD_BUS_H

#include "../inc/scpi.h"

#ifdef  __cplusplus
extern "C" {
#endif
//extern scpi_t scpi_context;
scpi_result_t Query(scpi_t * context);

scpi_result_t BUS_DISP(scpi_t * context);
scpi_result_t BUS_DISPQ(scpi_t * context);
scpi_result_t BUS_TYPE(scpi_t * context);
scpi_result_t BUS_TYPEQ(scpi_t * context);
scpi_result_t BUS_MODE(scpi_t * context);
scpi_result_t BUS_MODEQ(scpi_t * context);
scpi_result_t BUS_LEVEL(scpi_t * context);
scpi_result_t BUS_LEVELQ(scpi_t * context);
scpi_result_t BUS_HLEVEL(scpi_t * context);
scpi_result_t BUS_HLEVELQ(scpi_t * context);
scpi_result_t BUS_LLEVEL(scpi_t * context);
scpi_result_t BUS_LLEVELQ(scpi_t * context);
scpi_result_t BUS_DATAQ(scpi_t * context);

scpi_result_t BUS_UART_RX(scpi_t * context);
scpi_result_t BUS_UART_RXQ(scpi_t * context);
scpi_result_t BUS_UART_IDLE(scpi_t * context);
scpi_result_t BUS_UART_IDLEQ(scpi_t * context);
scpi_result_t BUS_UART_BAUD(scpi_t * context);
scpi_result_t BUS_UART_BAUDQ(scpi_t * context);
scpi_result_t BUS_UART_CHECK(scpi_t * context);
scpi_result_t BUS_UART_CHECKQ(scpi_t * context);
scpi_result_t BUS_UART_USER(scpi_t * context);
scpi_result_t BUS_UART_USERQ(scpi_t * context);
scpi_result_t BUS_UART_WIDTH(scpi_t * context);
scpi_result_t BUS_UART_WIDTHQ(scpi_t * context);
scpi_result_t BUS_UART_DISP(scpi_t * context);
scpi_result_t BUS_UART_DISPQ(scpi_t * context);
scpi_result_t BUS_UART_LEV(scpi_t * context);
scpi_result_t BUS_UART_LEVQ(scpi_t * context);

scpi_result_t BUS_LIN_CHAN(scpi_t * context);
scpi_result_t BUS_LIN_CHANQ(scpi_t * context);
scpi_result_t BUS_LIN_IDLE(scpi_t * context);
scpi_result_t BUS_LIN_IDLEQ(scpi_t * context);
scpi_result_t BUS_LIN_BAUD(scpi_t * context);
scpi_result_t BUS_LIN_BAUDQ(scpi_t * context);
scpi_result_t BUS_LIN_USER(scpi_t * context);
scpi_result_t BUS_LIN_USERQ(scpi_t * context);
scpi_result_t BUS_LIN_LEV(scpi_t * context);
scpi_result_t BUS_LIN_LEVQ(scpi_t * context);

scpi_result_t BUS_SPI_CLK(scpi_t * context);
scpi_result_t BUS_SPI_CLKQ(scpi_t * context);
scpi_result_t BUS_SPI_DATA(scpi_t * context);
scpi_result_t BUS_SPI_DATAQ(scpi_t * context);
scpi_result_t BUS_SPI_WIDTH(scpi_t * context);
scpi_result_t BUS_SPI_WIDTHQ(scpi_t * context);
scpi_result_t BUS_SPI_IDLElvl(scpi_t * context);
scpi_result_t BUS_SPI_IDLElvlQ(scpi_t * context);
scpi_result_t BUS_SPI_SLOP(scpi_t * context);
scpi_result_t BUS_SPI_SLOPQ(scpi_t * context);
scpi_result_t BUS_SPI_CS(scpi_t * context);
scpi_result_t BUS_SPI_CSQ(scpi_t * context);
scpi_result_t BUS_SPI_CS_SOURCE(scpi_t * context);
scpi_result_t BUS_SPI_CS_SOURCEQ(scpi_t * context);
scpi_result_t BUS_SPI_CS_IDLE(scpi_t * context);
scpi_result_t BUS_SPI_CS_IDLEQ(scpi_t * context);
scpi_result_t BUS_SPI_CLKLEV(scpi_t * context);
scpi_result_t BUS_SPI_CLKLEVQ(scpi_t * context);
scpi_result_t BUS_SPI_DATLEV(scpi_t * context);
scpi_result_t BUS_SPI_DATLEVQ(scpi_t * context);
scpi_result_t BUS_SPI_CSLEV(scpi_t * context);
scpi_result_t BUS_SPI_CSLEVQ(scpi_t * context);

scpi_result_t BUS_CAN_CHAN(scpi_t * context);
scpi_result_t BUS_CAN_CHANQ(scpi_t * context);
scpi_result_t BUS_CAN_SIGNal(scpi_t * context);
scpi_result_t BUS_CAN_SIGNalQ(scpi_t * context);
scpi_result_t BUS_CAN_BAUD(scpi_t * context);
scpi_result_t BUS_CAN_BAUDQ(scpi_t * context);
scpi_result_t BUS_CAN_USER(scpi_t * context);
scpi_result_t BUS_CAN_USERQ(scpi_t * context);
scpi_result_t BUS_CAN_SamplePoint(scpi_t * context);
scpi_result_t BUS_CAN_SamplePointQ(scpi_t * context);
scpi_result_t BUS_CAN_FDBaudrate(scpi_t * context);
scpi_result_t BUS_CAN_FDBaudrateQ(scpi_t * context);
scpi_result_t BUS_CAN_FDUserBaud(scpi_t * context);
scpi_result_t BUS_CAN_FDUserBaudQ(scpi_t * context);
scpi_result_t BUS_CAN_FDSamplePoint(scpi_t * context);
scpi_result_t BUS_CAN_FDSamplePointQ(scpi_t * context);
scpi_result_t BUS_CAN_LEV(scpi_t * context);
scpi_result_t BUS_CAN_LEVQ(scpi_t * context);
scpi_result_t BUS_CAN_ISO(scpi_t * context);
scpi_result_t BUS_CAN_ISOQ(scpi_t * context);

scpi_result_t BUS_IIC_SDA(scpi_t * context);
scpi_result_t BUS_IIC_SDAQ(scpi_t * context);
scpi_result_t BUS_IIC_SCL(scpi_t * context);
scpi_result_t BUS_IIC_SCLQ(scpi_t * context);
scpi_result_t BUS_IIC_LEVCLK(scpi_t * context);
scpi_result_t BUS_IIC_LEVCLKQ(scpi_t * context);
scpi_result_t BUS_IIC_LEVDAT(scpi_t * context);
scpi_result_t BUS_IIC_LEVDATQ(scpi_t * context);


scpi_result_t BUS_1553B_CHAN(scpi_t * context);
scpi_result_t BUS_1553B_CHANQ(scpi_t * context);
scpi_result_t BUS_1553B_DISP(scpi_t * context);
scpi_result_t BUS_1553B_DISPQ(scpi_t * context);
scpi_result_t BUS_1553B_LEV(scpi_t * context);
scpi_result_t BUS_1553B_LEVQ(scpi_t * context);

scpi_result_t BUS_429_SOURce(scpi_t * context);
scpi_result_t BUS_429_SOURceQ(scpi_t * context);
scpi_result_t BUS_429_FORMat(scpi_t * context);
scpi_result_t BUS_429_FORMatQ(scpi_t * context);
scpi_result_t BUS_429_DISP(scpi_t * context);
scpi_result_t BUS_429_DISPQ(scpi_t * context);
scpi_result_t BUS_429_BAND(scpi_t * context);
scpi_result_t BUS_429_BANDQ(scpi_t * context);
scpi_result_t BUS_429_HLEV(scpi_t * context);
scpi_result_t BUS_429_HLEVQ(scpi_t * context);
scpi_result_t BUS_429_LLEV(scpi_t * context);
scpi_result_t BUS_429_LLEVQ(scpi_t * context);



#ifdef  __cplusplus
}
#endif

#endif // SCPI_CMD_BUS_H
