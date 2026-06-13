//
// Created by zhuzh on 2018-4-11.
//
#include "mem.h"
#include <memory.h>
#include "Logger.h"
 #include <stdint.h>
#include <unistd.h>
#include <arm_neon.h>

#define TAG "MEM"
extern "C" void *__memcpy_aarch64_simd (void *__restrict, const void *__restrict, size_t);
JNIEXPORT void JNICALL Java_com_micsig_tbook_scope_mem_Memory_memcpy
        (JNIEnv *env, jobject obj, jobject dst,jint dstOffset,jobject src,jint srcOffset,jint length){

    uint8_t * d = (uint8_t*)env->GetDirectBufferAddress(dst);
    uint8_t * s = (uint8_t*)env->GetDirectBufferAddress(src);

    if(d && s && length>0){
        __memcpy_aarch64_simd(d+dstOffset,s+srcOffset,length);
    }
}
JNIEXPORT void JNICALL Java_com_micsig_tbook_scope_mem_Memory_memset
        (JNIEnv *env, jobject obj, jobject dst,jint dstOffset,jint length,jint val){

    uint8_t * d = (uint8_t*)env->GetDirectBufferAddress(dst);
    if(d  && length>0){
        memset(d + dstOffset,val,length);
    }
}

JNIEXPORT void JNICALL Java_com_micsig_tbook_scope_mem_Memory_convert16to32
        (JNIEnv *env, jobject obj, jobject dst,jint dstOffset,jobject src,jint srcOffset,jint val,jint length){
    uint8_t * baseDst = (uint8_t*)env->GetDirectBufferAddress(dst);
    uint8_t * baseSrc = (uint8_t*)env->GetDirectBufferAddress(src);
    int32_t * d = (int32_t*)(baseDst + dstOffset);
    int16_t * s = (int16_t*)(baseSrc + srcOffset);


    if(d && s && length>0){
        int n = length/4;
        int32x4_t v = vmovq_n_s32(val);
        for(int i=0;i<n;i++,d+=4,s+=4){
            vst1q_s32(d,vaddq_s32(v,vmovl_s16(vld1_s16(s))));
        }
        for(int i=n*4;i<length;i++){
            *d++ = val + *s++;
        }
    }

}

JNIEXPORT void JNICALL Java_com_micsig_tbook_scope_mem_Memory_sync
        (JNIEnv *env, jobject obj){
    ::sync();
}