#include "Int128.h"



S128::S128(U128 in) : L(in.L) , H(in.H) {}

int U128::operator>= (U128 b)
{
	if(this->H > b.H) return 1;
	if(this->H < b.H) return 0;
	if(this->L >= b.L) return 1;
	return 0;
}

U128& U128::operator<<= (unsigned char b)
{
	for(int i=0; i<b; i++)
	{
		H <<= 1;
		if(L & 0x8000000000000000uL)
			H |= 0x01;
		L <<= 1;
	}
	return *this;
}

S128 S128::operator+ (S128 b)
{
	U128 c(H,L);
	c = c+b;
	return c;
}

S128 S128::operator- (S128 b)
{
    U128 c(H,L);
    c = c-b;
    return c;
}

U128 U128::operator- (U128 b)
{	
	b.H = ~b.H;
	b.L = ~b.L;
	b = b+1;
	return *this+b;
}

U128 U128::operator+ (U128 b)
{
	//低位加
	U64 aH = (L >> 32) & 0xFFFFFFFF;
	U64 aL = L & 0xFFFFFFFF;
	U64 bH = (b.L >> 32) & 0xFFFFFFFF;
	U64 bL = b.L & 0xFFFFFFFF;

	aL = aL + bL;
	aH = aH + bH;
	
	U128 c(H,L);
	c.L = aL & 0xFFFFFFFF;
	aL = (aL >> 32) & 0xFFFFFFFF;
	aH = aH + aL;
	c.H += b.H+((aH >> 32) & 0xFFFFFFFF);
	c.L += (aH << 32);

	return c;
}

U128 U128::operator*(U128 b)
{	
	//低位相乘
	U128 a(H,L);
	U128 c = a * b.L;
	a = a * b.H;
	a.H = a.L+c.H;
	a.L = c.L;
	return a;
}

U128 U128::operator*(U64 b)
{
	U32 bH = (b >> 32) & 0xFFFFFFFF;
	U32 bL = b & 0xFFFFFFFF;
	U128 a(H,L);
	U128 c = a * bL;
	a = a * bH;
	a.H <<= 32;
	a.H += (a.L >> 32) & 0xFFFFFFFF;
	a.L <<= 32;
	return a + c;
}


U128 U128::operator*(U32 b)
{	
	//低位*b
	U64 aH = (L >> 32) & 0xFFFFFFFF;
	U64 aL = L & 0xFFFFFFFF;

	aL = aL*b;
	aH = aH*b;

	U128 c1 = (U128)aL + (U128)(aH << 32);
	c1.H += (aH >> 32) & 0xFFFFFFFF;
	
	//高位*b
	U128 a(H,L);
	a.H *= b;
	a.L = c1.L;
	a.H += c1.H;

	return a;
}


S128 S128::operator*(S128 b)
{
	int s_a = !!(H & 0x8000000000000000uL);
	int s_b = !!(b.H & 0x8000000000000000uL);
	
	//转化为无符号数
	U128 a(H,L);
	if(s_a)
	{
		a.H = ~a.H;
		a.L = ~a.L;
		a = a+1;
	}
	if(s_b)
	{
		b.H = ~b.H;
		b.L = ~b.L;
		b = b+1;
	}
	a = a * b;
	if(s_a ^ s_b)
	{
		//负数
		a.H = ~a.H;
		a.L = ~a.L;
		a = a+1;
	}	
	return a;
}

U128 U128::operator/ (U128 b)
{
	U128 a(H,L);//被除数
	U128 c;//商	
	U128 z;

	for(int i=0; i<128; i++)
	{
		z <<= 1;
		if(a.H & 0x8000000000000000uL)
			z.L |= 0x01;
		a <<= 1;
		c <<= 1;
		if(z >= b)
		{
			z = z-b;			
			c.L |= 0x01;
		}
	}
	return c;
}

U128 U128::operator% (U128 b)
{
	U128 a(H,L);//被除数
	U128 c;//商	
	U128 z;
	for(int i=0; i<128; i++)
	{
		z <<= 1;
		if(a.H & 0x8000000000000000uL)
			z.L |= 0x01;
		a <<= 1;
		c <<= 1;
		if(z >= b)
		{
			z = z-b;			
			c.L |= 0x01;
		}
	}	
	return z;
}

S128 S128::operator/ (S128 b)
{
	int s_a = !!(H & 0x8000000000000000uL);
	int s_b = !!(b.H & 0x8000000000000000uL);
	
	//转化为无符号数
	U128 a(H,L);
	if(s_a)
	{
		a.H = ~a.H;
		a.L = ~a.L;
		a = a+1;
	}
	if(s_b)
	{
		b.H = ~b.H;
		b.L = ~b.L;
		b = b+1;
	}

	U128 z = a % b;

	a = a / b;	
	if(s_a ^ s_b)
	{
		//负数
		a.H = ~a.H;
		a.L = ~a.L;
		if((z.H == 0) && (z.L == 0)) a = a+1;

//		a = a+1; //负值需要商减1

	}
	return a;
}

S128 S128::operator% (S128 b)
{
	int s_a = !!(H & 0x8000000000000000uL);
	int s_b = !!(b.H & 0x8000000000000000uL);
	
	//转化为无符号数
	U128 a(H,L);
	if(s_a)
	{
		a.H = ~a.H;
		a.L = ~a.L;
		a = a+1;
	}
	if(s_b)
	{
		b.H = ~b.H;
		b.L = ~b.L;
		b = b+1;
	}
	a = a % b;

	if(s_a ^ s_b)
	{	
		if(!((a.H == 0) && (a.L == 0)))
		{
			if(s_b)
				a = a-b;
			else
				a = (U128)b-a;
		}
	}
	else if(s_b)
	{
		a.H = ~a.H;
		a.L = ~a.L;
		a = a+1;
	}	


	return a;
}

int S128::operator== (S128 b)
{
    if((this->H == b.H) && (this->L == b.L))
        return 1;
    else
        return 0;
}

int S128::operator> (S128 b)
{
    int s_a = !!(H & 0x8000000000000000uL);
	int s_b = !!(b.H & 0x8000000000000000uL);
	
	if(s_a ^ s_b)
	{//一正一负
	    if(s_a) return 0;
	    else return 1;
	}
	if(this->H > b.H) return 1;
	if(this->H < b.H) return 0;
	if(this->L > b.L) return 1;	
    return 0;
}

int S128::operator< (S128 b)
{
    return b > *this;    
}

int S128::operator>= (S128 b)
{
    return (*this > b) && (*this == b);
}

int S128::operator<= (S128 b)
{
    return (b > *this) && (*this == b);
}

