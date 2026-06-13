/*****************************************************************************
 * Copyright (C) 2013 郑州麦科信电子技术有限公司
 * All right reserved.
 *
 * 文件名称：micsig_types.h
 * 摘要：全局性类型定义
 *
 * 作者：冯旭辉
 * 建立日期：Mar,2013
 *
 *****************************************************************************
 * Aug23,2013 消除因其他文件内重定义类型可能导致的编译错误
 * Aug19,2013 消除编译警告。
 */
#pragma once
//****************************************************************************
//**                                                                        **
//**                              类型定义                                  **
//**                                                                        **
//****************************************************************************
#ifndef U8
typedef unsigned char                   U8; //8Bits unsigned integer
#endif
#ifndef S8
typedef signed char                     S8; //8Bits signed integer
#endif
#ifndef S16
typedef signed short                    S16; //16Bits signed integer
#endif
#ifndef U16
typedef unsigned short                  U16; //16Bits unsigned integer
#endif
#ifndef U32
typedef unsigned int                    U32; //32Bits unsigned integer
#endif
#ifndef S32
typedef signed int                      S32;
#endif
#ifndef S64
typedef signed long long                S64; //64Bits signed integer
#endif
#ifndef U64
typedef unsigned long long              U64;
#endif



struct U128;
struct S128
{
    U64 L;
    S64 H;

    S128(S64 in) : L(in), H(in < 0 ? -1 : 0){}
    S128(S64 inH, S64 inL) : L(inL), H(inH) {}
    S128() : L(0) , H(0){}
    S128(U128 in);

    S128 operator* (S128 b);
	S128 operator+ (S128 b);
    S128 operator- (S128 b);
	S128 operator/ (S128 b);
	S128 operator% (S128 b);
	int operator> (S128 b);
	int operator>= (S128 b);
	int operator<= (S128 b);
	int operator< (S128 b);
    int operator== (S128 b);
    S128& operator= (const int& a)
    {
        H = (a < 0) ? -1 : 0;
        L = a;
        return *this;
    }
    S128& operator= (const S64& a)
    {
        H = (a < 0) ? -1 : 0;
        L = a;
        return *this;
    }

};


struct U128
{
    U64 L;
	U64 H;

    U128(U64 in) : L(in), H(0){}
    U128(U64 inH,U64 inL) :  L(inL), H(inH){}
	U128() : L(0) , H(0){}
    U128(S128 in) : L(in.L), H(in.H){}

	U128 operator* (U32 b);
	U128 operator* (U64 b);
	U128 operator* (U128 b);
	U128 operator+ (U128 b);
	U128 operator/ (U128 b);
	U128 operator% (U128 b);
	U128 operator- (U128 b);
	U128& operator<<= (unsigned char b);
	int operator>= (U128 b);
};
