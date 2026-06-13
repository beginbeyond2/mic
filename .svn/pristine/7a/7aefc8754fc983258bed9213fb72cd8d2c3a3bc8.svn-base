#ifndef CMICSIGCRYPTO_H
#define CMICSIGCRYPTO_H
#include "micsigcrypto.h"
#include "aes.h"
class CMicsigCrypto : public IMicsigCrypto
{
public:
    CMicsigCrypto();
    virtual void DefaultKey();
    virtual void SetKey(char * Key,int klen);
    virtual void Encrypt(const unsigned char *in,int inlen,unsigned char *out,int outlen);
    virtual void Decrypt(const unsigned char *in,int inlen,unsigned char *out,int outlen);
private:
    crypto_aes_ctx * ctx;
    static CMicsigCrypto * _Instance;
    friend class IMicsigCrypto;
};

#endif // CMICSIGCRYPTO_H
