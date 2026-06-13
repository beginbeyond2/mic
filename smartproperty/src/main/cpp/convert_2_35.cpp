#include "Int128.h"
#include "convert_2_35.h"

#define MICSIG_LEN 35
#define MICSIG_MAX 25

static char *strTable = (char *)"R0U1E3Y4B8T9AQSVCD5NPWXZ6KLMG7FHI2J";
static U128 BigIntPow(U128 x, int y)
{	
	if (y == 0) return 1;
	U128 tmp = x;
	for (int i = 1; i < y ; i++)
	{
		x = x * tmp;
	}
	return x;
}


void numToStr(char* szInBuffer, char *szOutBuffer)//十进制转35进制
{	
    U64 * p = (U64 *)szInBuffer;
    U128 n = 0;
    U128 num = 0;
    num.H = *p++;
    num.L = *p;
    szOutBuffer[MICSIG_MAX] = '\0';
	for (int i = 0; i < MICSIG_MAX; i++)
	{
		n = num % MICSIG_LEN;
        szOutBuffer[MICSIG_MAX - i - 1] = strTable[n.L];
		num = num / MICSIG_LEN;
	}	
}


void StrTonum(char*szInBuffer, char *szOutBuffer)//35转10进制
{
    U64 * p = (U64 *)szOutBuffer;
	U128 num = 0;
	for (int i = 0; i < MICSIG_MAX; i++)
	{
		int idx = 0;
		for (int j = 0; j < MICSIG_LEN; j++)
		{
            if (szInBuffer[MICSIG_MAX - i - 1] == strTable[j])
			{
				break;
			}
			idx++;
		}		
		num = num + (U128(idx) * BigIntPow(MICSIG_LEN, i));
	}
    *p++ = num.H;
    *p = num.L;
}
