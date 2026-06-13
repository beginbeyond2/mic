#ifndef MICSIGCRYPTO_H
#define MICSIGCRYPTO_H


class IMicsigCrypto
{
    
public:
    IMicsigCrypto();
    virtual void DefaultKey() = 0;
    virtual void SetKey(char * Key,int klen) = 0;
    virtual void Encrypt(const unsigned char *in,int inlen,unsigned char *out,int outlen) = 0;
    virtual void Decrypt(const unsigned char *in,int inlen,unsigned char *out,int outlen) = 0;
    static IMicsigCrypto * Instance();
};

#endif // MICSIGCRYPTO_H
