//
// Created by zhuzh on 2025/8/4.
//

#ifndef MHO68_V2_BAR_XDMADEVUSER_H
#define MHO68_V2_BAR_XDMADEVUSER_H
#include <jni.h>
#pragma pack(push)      //保存对齐状态
#pragma pack(1)         // 设定为1字节对齐
typedef struct _store_status_t{
    volatile uint32_t fpga_id;
    volatile uint32_t fpga_ver;
    volatile uint8_t fpga_ddr_init_done:1;
    volatile uint8_t fpga_pll_lock:1;
    volatile uint8_t fpga_pll_init_done:1;
    volatile uint8_t fpga_adc_init_done:1;
    volatile uint8_t fpga_204B_done0:1;
    volatile uint8_t fpga_204B_done1:1;
    volatile uint8_t fpga_reserved:1;
    volatile uint8_t fpga_init_done:1;
    volatile uint8_t xmda_c2h0_is_busy:1;
    volatile uint8_t xmda_c2h1_is_busy:1;
    volatile uint8_t xmda_c2h2_is_busy:1;
    volatile uint8_t xmda_c2h3_is_busy:1;
    volatile uint8_t xmda_h2c0_is_busy:1;
    volatile uint8_t xmda_reserved:3;
    volatile uint8_t fpga_store_rdy:1;
    volatile uint8_t fpga_resotre_done:1;
    volatile uint8_t fgpa_reserved:6;
}STORE_STATUS_T;

typedef struct _store_cmd_t{
    volatile uint32_t cmd;
}STORE_CMD_T;

typedef struct _store_mem_t{
    volatile uint64_t waveAddr;
    volatile uint64_t frameAddr;
    volatile uint64_t serDecAddr;
    volatile uint64_t serSortAddr;
    volatile uint8_t reserved[0x80-0x20];
    volatile uint64_t waveLength;
    volatile uint64_t frameLength;
    volatile uint64_t serDecLength;
    volatile uint64_t serSortLength;
    volatile uint64_t regLength;
}STORE_MEM_T;

typedef struct _store_xdma_t{
    volatile uint64_t type;
    volatile uint64_t length;
}STORE_XDMA_T;


#pragma pack(pop)       // 恢复对齐状态
class XDmaDevUser {
public:
    XDmaDevUser(int idx);
    bool Open(JNIEnv *env,int barfd,int c2hfd,int h2cfd);
    void Close();
    jobject getByteBuffer();

    bool store(int fd);
    bool restore(int fd);
    int getProgress();
    void stop();

    static XDmaDevUser * getInstance(int idx);
private:
    std::atomic_bool forceStop;

    int bar_fd;
    int c2h_fd;
    int h2c_fd;
    int idx;
    jobject byteBuffer;
    void *vir_addr;
    uint8_t *data;

    STORE_STATUS_T * status;
    STORE_CMD_T * cmd;
    STORE_MEM_T * mem;
    STORE_XDMA_T * xdma;
    uint8_t * reg;
    volatile int progressVal;
    uint64_t totalLength;
    uint64_t recvLength;
    uint8_t  * bar_buffer;


private:
    bool storeData(int type,uint64_t length,int fd);
    bool restoreData(int type,uint64_t length,int fd);
};


#endif //MHO68_V2_BAR_XDMADEVUSER_H
