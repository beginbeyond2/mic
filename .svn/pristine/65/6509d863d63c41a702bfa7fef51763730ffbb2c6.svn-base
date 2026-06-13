//
// Created by liwb on 2025-6-24.
//

#ifndef ETO_COMMONCL_H
#define ETO_COMMONCL_H
extern const char Content[];
const char Content[]="__kernel void warm(){}"
                     "__kernel void intToHex(__global const int* input,__global char* output,const int count,const int placeVal)\n"
                     "{\n"
                     "    int idx=get_global_id(0);\n"
                     "    if (idx>=count) return;\n"
                     "\n"
                     "    const char hexTable[]=\"0123456789ABCDEF\";\n"
                     "    int num=input[idx]-placeVal;\n"
                     "\n"
                     "    for(int i=0;i<4;i++)\n"
                     "    {\n"
                     "        int id = (num>>(12 - i * 4))&0xF;\n"
                     "        output[idx*4+i]=hexTable[id];\n"
                     "\n"
                     "    }\n"
                     "}\n"
                     "\n"
                     "void paddingZero(__global char* output,int begin)\n"
                     "{\n"
                     "    int gid=get_global_id(0);\n"
                     "    for(int i=begin;i<16;i++){\n"
                     "        output[gid*16+i]=' ';\n"
                     "    }\n"
                     "    output[gid*16+15]=',';\n"
                     "\n"
                     "}\n"
                     "\n"
                     "__kernel void floatToScientific(__global const int* input,__global char* output,const int count,const float vv, const int decimalPlaces)\n"
                     "{\n"
                     "    int gid=get_global_id(0);\n"
                     "    if (gid>=count) return;\n"
                     "    float val=input[gid]*vv;\n"
                     "    float exponent=0;\n"
                     "    char sign=val<0?'-':'+';\n"
                     "    val=fabs(val);\n"
                     "\n"
                     "    if (isnan(val)){\n"
                     "        output[gid*16]='N';output[gid*16+1]='a';output[gid*16+2]='N';\n"
                     "        paddingZero(output,3);\n"
                     "        return;\n"
                     "    }\n"
                     "    if (isinf(val)){\n"
                     "        output[gid*16]='I';output[gid*16+1]='n';output[gid*16+2]='f';\n"
                     "        paddingZero(output,3);\n"
                     "        return;\n"
                     "    }\n"
                     "    if (val==0.0f){\n"
                     "        output[gid*16]='0';output[gid*16+1]='.';\n"
                     "        for(int i=0;i<decimalPlaces;i++) output[gid*16+2+i]='0';\n"
                     "        output[gid*16+2+decimalPlaces]='e';output[gid*16+3+decimalPlaces]='+';\n"
                     "        output[gid*16+4+decimalPlaces]='0';output[gid*16+5+decimalPlaces]='0';\n"
                     "        paddingZero(output,6);\n"
                     "        return;\n"
                     "    }\n"
                     "\n"
                     "    //标准化\n"
                     "//    val=frexp(val,&exponent);\n"
                     "    exponent=log10(val);\n"
                     "    val *=pow(10,(trunc(-exponent)));\n"
                     "//    exponent -= 1;\n"
                     "\n"
                     "    //写入符号\n"
                     "    output[gid*16]=sign;\n"
                     "    //计算写入尾数\n"
                     "    int pos=1;\n"
                     "    int digit=(int)val;\n"
                     "    output[gid*16+pos++]=(char)(digit+'0');\n"
                     "    output[gid*16+pos++]='.';\n"
                     "\n"
                     "    float remainder=val-digit;\n"
                     "    for(int i=0;i<decimalPlaces;i++){\n"
                     "        remainder *=10.0f;\n"
                     "        digit=(int)remainder;\n"
                     "        output[gid*16+pos++]=(char)(digit+'0');\n"
                     "        remainder -=digit;\n"
                     "    }\n"
                     "\n"
                     "    //写入指数部分\n"
                     "    output[gid*16+pos++]='e';\n"
                     "    output[gid*16+pos++]=exponent>=0?'+':'-';\n"
                     "    int absExp =fabs(exponent);\n"
                     "    output[gid*16+pos++]=(char)(absExp/10+'0');\n"
                     "    output[gid*16+pos++]=(char)(absExp%10+'0');\n"
                     "    paddingZero(output,pos);\n"
                     "}"
                     ;

//源码
//__kernel void warm(){
//}
//
//__kernel void intToHex(__global const int* input,__global char* output,const int count,const int placeVal)
//{
//    int idx=get_global_id(0);
//    if (idx>=count) return;
//
//    const char hexTable[]="0123456789ABCDEF";
//    int num=input[idx]-placeVal;
//
//    for(int i=0;i<4;i++)
//    {
//        int id = (num>>(12 - i * 4))&0xF;
//        output[idx*4+i]=hexTable[id];
//
//    }
//}
//
//void paddingZero(__global char* output,int begin)
//{
//    int gid=get_global_id(0);
//    for(int i=begin;i<16;i++){
//        output[gid*16+i]=' ';
//    }
//    output[gid*16+15]=',';
//
//}
//
//__kernel void floatToScientific(__global const int* input,__global char* output,const int count,const float vv, const int decimalPlaces)
//{
//    int gid=get_global_id(0);
//    if (gid>=count) return;
//    float val=input[gid]*vv;
//    float exponent=0;
//    char sign=val<0?'-':'+';
//    val=fabs(val);
//
//    if (isnan(val)){
//        output[gid*16]='N';output[gid*16+1]='a';output[gid*16+2]='N';
//        paddingZero(output,3);
//        return;
//    }
//    if (isinf(val)){
//        output[gid*16]='I';output[gid*16+1]='n';output[gid*16+2]='f';
//        paddingZero(output,3);
//        return;
//    }
//    if (val==0.0f){
//        output[gid*16]='0';output[gid*16+1]='.';
//        for(int i=0;i<decimalPlaces;i++) output[gid*16+2+i]='0';
//        output[gid*16+2+decimalPlaces]='e';output[gid*16+3+decimalPlaces]='+';
//        output[gid*16+4+decimalPlaces]='0';output[gid*16+5+decimalPlaces]='0';
//        paddingZero(output,6);
//        return;
//    }
//
//    //标准化
////    val=frexp(val,&exponent);
//    exponent=log10(val);
//    val *=pow(10,(trunc(-exponent)));
////    exponent -= 1;
//
//    //写入符号
//    output[gid*16]=sign;
//    //计算写入尾数
//    int pos=1;
//    int digit=(int)val;
//    output[gid*16+pos++]=(char)(digit+'0');
//    output[gid*16+pos++]='.';
//
//    float remainder=val-digit;
//    for(int i=0;i<decimalPlaces;i++){
//        remainder *=10.0f;
//        digit=(int)remainder;
//        output[gid*16+pos++]=(char)(digit+'0');
//        remainder -=digit;
//    }
//
//    //写入指数部分
//    output[gid*16+pos++]='e';
//    output[gid*16+pos++]=exponent>=0?'+':'-';
//    int absExp =fabs(exponent);
//    output[gid*16+pos++]=(char)(absExp/10+'0');
//    output[gid*16+pos++]=(char)(absExp%10+'0');
//    paddingZero(output,pos);
//}
#endif //ETO_COMMONCL_H
