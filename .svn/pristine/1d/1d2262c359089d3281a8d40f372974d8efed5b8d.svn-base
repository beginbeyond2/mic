//
// Created by zhuzh on 2025/8/4.
//
#include <unistd.h>
#include <cstdlib>
#include <cstdint>
#include <sys/mman.h>
#include <sys/time.h>
#include <errno.h>
#include <string>
#include "XDmaDevUser.h"
#include "Logger.h"
static inline uint32_t swap_uint32(uint32_t value){
    return ((value &0xFF000000) >> 24) |
            ((value &0x00FF0000) >> 8) |
            ((value &0x0000FF00) << 8) |
            ((value &0x000000FF) << 24) ;
}
#define _ALIGNMENT (4096ULL)
#define MAP_BLOCK_SIZE (1024 * 1024UL)
#define MEM_SIZE (4096 * 2048UL)
#define TAG ("XDmaDevUser")
typedef enum _mss_t{
    MSS_AA55 = 0xAA550000,
    MSS_MEMORY = 100,
    MSS_REG = 101,
    MSS_WAVE = 102,
    MSS_FRAME = 103,
    MSS_SERDEC = 104,
    MSS_SERSORT = 105,
    MSS_MASK = 0xFF,
}MSS_TYPE;
#define MSS_VALID(mss,t) (((mss) & MSS_MASK) == (t) && ((mss & 0xFFFF0000) == MSS_AA55))
static XDmaDevUser * sXDma[] = {NULL,NULL};
XDmaDevUser * XDmaDevUser::getInstance(int idx){
    if(sXDma[idx] == NULL){
        sXDma[idx] = new XDmaDevUser(idx);
    }
    return sXDma[idx];
}


XDmaDevUser::XDmaDevUser(int idx){
    this->idx = idx;
    vir_addr = MAP_FAILED;
    byteBuffer = NULL;
    progressVal = 0;
    bar_buffer = (uint8_t*)malloc(4096);
}

static void dump(int idx, STORE_STATUS_T *status, STORE_MEM_T *mem,STORE_CMD_T * cmd){
    LOGD("idx:%d,fpga_id:%x", idx,status->fpga_id);
    LOGD("idx:%d,fpga_ver:%u", idx,status->fpga_ver);
    LOGD("idx:%d,fpga_ddr_init_done:%u", idx,status->fpga_ddr_init_done);
    LOGD("idx:%d,fpga_pll_lock:%d", idx,status->fpga_pll_lock);
    LOGD("idx:%d,fpga_pll_init_done:%d", idx,status->fpga_pll_init_done);
    LOGD("idx:%d,fpga_adc_init_done:%d", idx,status->fpga_adc_init_done);
    LOGD("idx:%d,fpga_204B_done0:%d", idx,status->fpga_204B_done0);
    LOGD("idx:%d,fpga_204B_done1:%d", idx,status->fpga_204B_done1);
    LOGD("idx:%d,fpga_init_done:%d", idx,status->fpga_init_done);
    LOGD("idx:%d,xmda_c2h0_is_busy:%d", idx,status->xmda_c2h0_is_busy);
    LOGD("idx:%d,xmda_c2h1_is_busy:%d", idx,status->xmda_c2h1_is_busy);
    LOGD("idx:%d,xmda_c2h2_is_busy:%d", idx,status->xmda_c2h2_is_busy);
    LOGD("idx:%d,xmda_c2h3_is_busy:%d", idx,status->xmda_c2h3_is_busy);
    LOGD("idx:%d,xmda_h2c0_is_busy:%d", idx,status->xmda_h2c0_is_busy);
    LOGD("idx:%d,fpga_store_rdy:%d", idx,status->fpga_store_rdy);
    LOGD("idx:%d,fpga_resotre_done:%d", idx,status->fpga_resotre_done);
    LOGD("idx:%d,cmd:%u",idx,cmd->cmd);
    LOGD("idx:%d,waveAddr:%lu",idx,mem->waveAddr);
    LOGD("idx:%d,frameAddr:%lu",idx,mem->frameAddr);
    LOGD("idx:%d,serDecAddr:%lu",idx,mem->serDecAddr);
    LOGD("idx:%d,serSortAddr:%lu",idx,mem->serSortAddr);
    LOGD("idx:%d,waveLength:%lu",idx,mem->waveLength);
    LOGD("idx:%d,frameLength:%lu",idx,mem->frameLength);
    LOGD("idx:%d,serDecLength:%lu",idx,mem->serDecLength);
    LOGD("idx:%d,serSortLength:%lu",idx,mem->serSortLength);
    LOGD("idx:%d,regLength:%lu",idx,mem->regLength);
}

bool XDmaDevUser::Open(JNIEnv *env,int barfd,int c2hfd,int h2cfd){

    vir_addr = ::mmap(0,MAP_BLOCK_SIZE,PROT_READ | PROT_WRITE,MAP_SHARED,barfd,0);
    if(vir_addr == MAP_FAILED){
        return false;
    }
    LOGD("idx:%d,addr:%p",idx,vir_addr);
    byteBuffer = env->NewDirectByteBuffer(vir_addr, MAP_BLOCK_SIZE);
    uint8_t  * ptr = (uint8_t*) vir_addr;
    status = (STORE_STATUS_T *) (ptr + 0);
    cmd = (STORE_CMD_T *) (ptr + 0x80);
    mem = (STORE_MEM_T *) (ptr + 0x100);
    reg = (ptr + 0x300);
    xdma = (STORE_XDMA_T *)(ptr + 0x500);


    dump(idx,status,mem,cmd);


    ptr = NULL;
    posix_memalign((void **)&ptr,4096,MEM_SIZE + 4096);
    if(ptr != NULL){
        data = ptr;
    }
    this->bar_fd = barfd;
    this->c2h_fd = c2hfd;
    this->h2c_fd = h2cfd;
    return true;
}
jobject XDmaDevUser::getByteBuffer(){
    return byteBuffer;
}
void XDmaDevUser::Close(){
    if(vir_addr != MAP_FAILED){
        ::munmap(vir_addr,MAP_BLOCK_SIZE);
    }
    vir_addr = MAP_FAILED;
    byteBuffer = NULL;
}
static double GetTickCount(){
    timeval tv;
    gettimeofday(&tv, NULL);
    return tv.tv_sec * 1000000.0 + tv.tv_usec;
}
static ssize_t writeV(int fd,uint8_t * ptr, ssize_t count){
    ssize_t s,r = 0;
    while(r < count){
        s = write(fd,ptr + r ,count - r);
        if(s <= 0){
            LOGE("err  %d,%zd,%s",__LINE__,s,strerror(errno));
            if(s < 0 && errno == EINTR){
                continue;
            }
            break;
        }
        r += s;
    }
    return r;
}
static ssize_t readV(int fd,uint8_t * ptr,ssize_t len){
    ssize_t s,r = 0;
    while(r < len){
        s = read(fd,ptr + r,len -r);
        if(s <= 0){
            LOGE("err  %d,%zd,%s",__LINE__,s,strerror(errno));
            if(s < 0 && errno == EINTR){
                continue;
            }
            break;
        }
        r += s;
    }
    return r;
}

static bool writeData(uint32_t mss,int fd,void * ptr,uint64_t len){
    mss = swap_uint32(mss | MSS_AA55);
    ssize_t s = writeV(fd,(uint8_t*)&mss,sizeof(mss));
    if(s != sizeof(mss)){
        LOGE("err  %d,%zd,%s",__LINE__,s,strerror(errno));
        return false;
    }
    s = writeV(fd,(uint8_t*)&len,sizeof(len));
    if(s != sizeof(len)){
        LOGE("err  %d,%zd,%s",__LINE__,s,strerror(errno));
        return false;
    }
    s = writeV(fd,(uint8_t*)ptr,len);
    if(s != len){
        LOGE("err  %d,%zd,%s",__LINE__,s,strerror(errno));
        return false;
    }
    return true;
}
bool XDmaDevUser::store(int fd){
    progressVal = 0;
    cmd->cmd = 1;
    forceStop = false;

    LOGD("idx:%d,cmd:%p",idx,cmd);

    double t = GetTickCount();
    while (status->fpga_store_rdy == 0){
        if(GetTickCount() - t > 3e6
            || forceStop){
            LOGE("err, %d",__LINE__);
            dump(idx,status,mem,cmd);
            return false;
        }
        ::usleep(1000);
    }
    dump(idx,status,mem,cmd);
    LOGD("%s.%d,%p\n",__FUNCTION__ ,__LINE__,bar_buffer);
    uint8_t  * ptr = (uint8_t*)mem;
    for(int i=0;i<256;i++){
        bar_buffer[i] = ptr[i];
    }
//    memcpy(bar_buffer,mem,256);
    if(!writeData(MSS_MEMORY,fd,bar_buffer,256)
        || forceStop){
        return false;
    }
//    memcpy(bar_buffer,reg,256);
    ptr = (uint8_t*)reg;
    for(int i=0;i<256;i++){
        bar_buffer[i] = ptr[i];
    }
    if(!writeData(MSS_REG,fd,bar_buffer,256)
        || forceStop){
        return false;
    }
    t = GetTickCount();
    int xtype = 0;
    totalLength = mem->waveLength + mem->frameLength + mem->serDecLength + mem->serSortLength;
    recvLength = 0;

    LOGD("xdma:type:%lu,xtype:%d,totalLength:%lu\n",xdma->type,xtype,totalLength);
    do{
        if(xtype == xdma->type){
            uint64_t  l = 0;
            bool bLoop = false;
            do{
                bLoop = false;
                switch (xtype) {
                    case 0:
                        l = mem->waveLength;
                        break;
                    case 1:
                        l = mem->frameLength;
                        bLoop = mem->serDecLength == 0;
                        break;
                    case 2:
                        l = mem->serDecLength;
                        bLoop = mem->serSortLength == 0;
                        break;
                    case 3:
                        l = mem->serSortLength;
                        break;
                }
                if(forceStop){
                    return false;
                }
                if(!storeData(xtype,l,fd)){
                    return false;
                }
                xtype++;
            } while (xtype < 4 && bLoop);
            t = GetTickCount();
        }else{
            if(GetTickCount() - t > 2e6
                || forceStop){
                LOGE("err, %d,type:%lu,%d",__LINE__,xdma->type,xtype);
                return false;
            }
            ::usleep(1000);
        }
    } while (xtype < 4);
    progressVal = 100;
    return true;
}
bool XDmaDevUser::storeData(int type,uint64_t length,int fd){
    uint64_t r,m,l = length;
    ssize_t n;
    r = 0;
    LOGD("store %d len:%lu",type,l);
    l = (l + _ALIGNMENT - 1) & ~(_ALIGNMENT - 1);

    int mss = swap_uint32((MSS_WAVE + type) | MSS_AA55);
    ssize_t s = writeV(fd,(uint8_t*)&mss,sizeof(mss));
    if(s != sizeof (mss)){
        LOGE("err  %d,%zd,%s",__LINE__,s,strerror(errno));
        return false;
    }
    s = writeV(fd,(uint8_t*)&l,sizeof(l));
    if(s != sizeof (l)){
        LOGE("err  %d,%zd,%s",__LINE__,s,strerror(errno));
        return false;
    }
    while (r < l){

        if(forceStop){
            return false;
        }

        m = l - r;
        if(m > MEM_SIZE){
            m = MEM_SIZE;
        }

        n = readV(c2h_fd,data,m);
        if(n == m ){
            s = writeV(fd,data,n);
            if(s != n){
                LOGE("err %d,%zd,%s",__LINE__,s,strerror(errno));
                return false;
            }
            r += n;
            recvLength += n;
            progressVal = recvLength * 98 / totalLength;
        }else{
            LOGE("err %zd,%zd,%s\n",n,m,strerror(errno));
            return false;
        }

    }
    return true;
}

static bool readData(uint32_t *mss,int fd,void * ptr,uint64_t len){
    uint32_t r = 0;
    uint64_t n = 0;
    ssize_t s = readV(fd, (uint8_t*)(&r), sizeof(r));
    if(s != sizeof(r)){
        LOGE("err  %d,%zd,%s",__LINE__,s,strerror(errno));
        return false;
    }
    s = readV(fd, (uint8_t*)(&n), sizeof(n));
    if(s != sizeof(n)){
        LOGE("err  %d,%zd,%s",__LINE__,s,strerror(errno));
        return false;
    }
    if(n == len){
        s = readV(fd,(uint8_t*)ptr,len);
        if(s != len){
            LOGE("err  %d,%zd,len:%lu,%s",__LINE__,s,len,strerror(errno));
            return false;
        }
        *mss = swap_uint32(r);
        return true;
    }
    return false;
}

bool XDmaDevUser::restore(int fd){
    progressVal = 0;
    forceStop = false;

    uint32_t mss = 0;

    if(!readData(&mss,fd,mem,256)
        || !MSS_VALID(mss,MSS_MEMORY)
        || forceStop){

        LOGE("err  %d,%d,%s",__LINE__,mss,strerror(errno));
        return false;
    }

    if(!readData(&mss,fd,reg,256)
        || !MSS_VALID(mss,MSS_REG)
        || forceStop){

        LOGE("err  %d,%d,%s",__LINE__,mss,strerror(errno));
        return false;
    }

    dump(idx,status,mem,cmd);

    totalLength = mem->waveLength + mem->frameLength + mem->serDecLength + mem->serSortLength;
    recvLength = 0;
    cmd->cmd = 2;
    int xtype = 0;
    do{
        uint64_t l = 0;
        switch (xtype) {
            case 0: l = mem->waveLength;break;
            case 1: l = mem->frameLength;break;
            case 2: l = mem->serDecLength;break;
            case 3: l = mem->serSortLength;break;
        }
        if(!restoreData(xtype,l,fd)
            || forceStop){
            return false;
        }
        xtype++;
    } while (xtype < 4);

    double t = GetTickCount();
    while (status->fpga_resotre_done == 0){
        if(GetTickCount() - t > 2e6
            || forceStop){
            LOGE("err %d",__LINE__);
            return false;
        }
        ::usleep(1000);
    }
    progressVal = 100;
    return true;
}
bool XDmaDevUser::restoreData(int type,uint64_t length,int fd){
    uint32_t mss = 0;
    uint64_t r,m,l = length;
    ssize_t n;
    r = 0;
    l = (l + _ALIGNMENT - 1) & ~(_ALIGNMENT - 1);

    ssize_t s = readV(fd,(uint8_t*)&mss,sizeof (mss));
    mss = swap_uint32(mss);
    if(s != sizeof (mss) || !MSS_VALID(mss,(MSS_WAVE + type))){
        LOGE("err  %d,%zd,%s",__LINE__,s,strerror(errno));
        return false;
    }
    s = readV(fd,(uint8_t*)&r,sizeof (r));
    if(s != sizeof (r)){
        LOGE("err  %d,%zd,%s",__LINE__,s,strerror(errno));
        return false;
    }
    if(r >= l){
        LOGD("restore wave len:%lu",l);
        r = 0;
        while(r < l){
            m = l - r;
            if(m > MEM_SIZE){
                m = MEM_SIZE;
            }

            if(forceStop){
                return false;
            }

            n = readV(fd,data,m);
            if(n == m){
                s = writeV(h2c_fd,data,n);
                if(s != n){
                    LOGE("err %d,%zd,%zd,%s",__LINE__,s,n,strerror(errno));
                    return false;
                }
                r += n;
                recvLength += n;
                progressVal = recvLength * 98 / totalLength;
            }else{
                LOGE("err %zd,%s\n",n,strerror(errno));
                return false;
            }
        }
    }
    return true;
}
int XDmaDevUser::getProgress(){
    return progressVal;
}
void XDmaDevUser::stop(){
    forceStop = true;
}