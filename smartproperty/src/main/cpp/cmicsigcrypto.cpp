#include <stdio.h>
#include <memory.h>
#include "cmicsigcrypto.h"
static unsigned int iCryptoKey[AES_KEYSIZE_256/sizeof(int)] =
{
    0x20140519,
    0x11272711,
    0x20130506,
    0x19840320,
    0x19890209,
    0x20120327,
    0x20120516,
    0x20120931
};
CMicsigCrypto::CMicsigCrypto():
    ctx(NULL)
{
    ctx = new crypto_aes_ctx;
    crypto_aes_set_key(ctx,(u8*)iCryptoKey,AES_KEYSIZE_256);
}

void CMicsigCrypto::DefaultKey()
{
   crypto_aes_set_key(ctx,(u8*)iCryptoKey,AES_KEYSIZE_256);
}

void CMicsigCrypto::SetKey(char * Key,int klen)
{
    if(klen < AES_KEYSIZE_128)
    {
       crypto_aes_set_key(ctx,(u8*)iCryptoKey,AES_KEYSIZE_256);
    }
    else if(klen < AES_KEYSIZE_192)
    {
        crypto_aes_set_key(ctx,(u8*)Key,AES_KEYSIZE_128);
    }
    else if(klen < AES_KEYSIZE_256)
    {
        crypto_aes_set_key(ctx,(u8*)Key,AES_KEYSIZE_192);
    }
    else
    {
       crypto_aes_set_key(ctx,(u8*)Key,AES_KEYSIZE_256);
    }
}

void CMicsigCrypto::Encrypt(const unsigned char *in,int inlen,unsigned char *out,int outlen)
{
    if(ctx && outlen >= inlen)
    {
        int len = (inlen / AES_BLOCK_SIZE) * AES_BLOCK_SIZE;
        for(int i=0;i<len;i+=AES_BLOCK_SIZE)
        {
            aes_encrypt(ctx,out+i,in+i);
        }
        if(len < inlen)
        {
            memcpy(out+len,in+len,inlen-len);
        }
    }
}

void CMicsigCrypto::Decrypt(const unsigned char *in,int inlen,unsigned char *out,int outlen)
{
    if(ctx && outlen >= inlen)
    {
        int len = (inlen / AES_BLOCK_SIZE) * AES_BLOCK_SIZE;
        for(int i=0;i<len;i+=AES_BLOCK_SIZE)
        {
            aes_decrypt(ctx,out+i,in+i);
        }
        if(len < inlen)
        {
            memcpy(out+len,in+len,inlen-len);
        }
    }
}
CMicsigCrypto * CMicsigCrypto::_Instance = NULL;
IMicsigCrypto * IMicsigCrypto::Instance()
{
    if(CMicsigCrypto::_Instance == NULL)
    {
        CMicsigCrypto::_Instance = new CMicsigCrypto;
    }
    return dynamic_cast<IMicsigCrypto *>(CMicsigCrypto::_Instance);
}
