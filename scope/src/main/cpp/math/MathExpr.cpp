//
// Created by zhuzh on 2019-8-5.
//

#include "MathExpr.h"
#include "exprtk.hpp"
#include "../Logger.h"
#include <string>
#include <stdio.h>
#define  TAG "MathExpr"
//static const double _E =  2.71828182845904523536028747135266249775724709369996;
static const double _PI =  3.14159265358979323846264338327950288419716939937510;
static const double _T = 1e12;
static const double _G = 1e9;
static const double _M = 1e6;
static const double _k = 1e3;
static const double _m = 1e-3;
static const double _u = 1e-6;
static const double _n = 1e-9;
static const double _p = 1e-12;
static const double _f = 1e-15;


static void intg_func(vector_double_t & vec1,vector_double_t  & vec2, double d)
{
    double sum = 0;
    vec2.clear();
    int len = vec1.size();
    for (int i = 0; i < len; i++) {
        sum += vec1[i];
        vec2.push_back(d * sum);
    }
}
static inline double diffVal(int idx,vector_double_t & vec1,double d){
    double y_2,y_4,y2,y4;
    y_2 = vec1[idx - 2];
    y_4 = vec1[idx - 4];
    y2 = vec1[idx + 2];
    y4 = vec1[idx + 4];
    return (y4 + 2 * y2 - 2 * y_2 - y_4) / (8 * d);
}
static void diff_func(vector_double_t & vec1, vector_double_t  & vec2, double d)
{

    double val;
    vec2.clear();
    int len = vec1.size();
    int i = 4;
    val = diffVal(i,vec1,d);
    for(i=0;i<4;i++){
        vec2.push_back(val);
    }
    for ( i = 4; i < (len-4); i++){
        val = diffVal(i,vec1,d);
        vec2.push_back(val);
    }
    for( i=0;i<4;i++){
        vec2.push_back(val);
    }

}



jobject allocMathExprErrorObj(JNIEnv *env,jboolean error, exprtk::parser_error::type &errType)
{
    jstring jstrval = env->NewStringUTF(errType.token.value.c_str());
    jstring jstrdiagnostic = env->NewStringUTF(errType.diagnostic.c_str());
    jclass MathExprError = env->FindClass("com/micsig/tbook/scope/math/MathExprError");
    jmethodID MathExprError_costruct = env->GetMethodID(MathExprError , "<init>","(ZIILjava/lang/String;Ljava/lang/String;)V");

    jobject exprErrObj = env->NewObject(MathExprError , MathExprError_costruct,
            error,errType.mode,errType.token.position,jstrval,jstrdiagnostic);
    return exprErrObj;
}
jobject MathExpr::isMathExprValid(JNIEnv *env,std::string expr_string){
    double val,var1,var2;
    val = var1 = var2 = 0;

    parser_t parser;
    jobject obj;
    jboolean error = JNI_TRUE;
    exprtk::parser_error::type errType;
    symbol_table_t symbol_table;
    expression_t expression;
    Deg<double> deg;
    Rad<double> rad;
    Lg<double> lg;
    Ln<double> ln;
    exprtk::ifunction<double> intg(1);
    exprtk::ifunction<double> diff(1);

    symbol_table.add_variable("ch1",val);
    symbol_table.add_variable("ch2",val);
    symbol_table.add_variable("ch3",val);
    symbol_table.add_variable("ch4",val);
    symbol_table.add_variable("ch5",val);
    symbol_table.add_variable("ch6",val);
    symbol_table.add_variable("ch7",val);
    symbol_table.add_variable("ch8",val);
    symbol_table.add_variable("var1",var1);
    symbol_table.add_variable("var2",var2);
    symbol_table.add_variable("time", val);
    symbol_table.add_function("deg", deg);
    symbol_table.add_function("rad", rad);
    symbol_table.add_function("intg", intg);
    symbol_table.add_function("diff", diff);
    symbol_table.add_function("lg", lg);
    symbol_table.add_function("ln", ln);
    //symbol_table.add_constant("E",_E);
    symbol_table.add_constant("P",_PI);
    symbol_table.add_constant("T",_T);
    symbol_table.add_constant("G",_G);
    symbol_table.add_constant("M",_M);
    symbol_table.add_constant("k",_k);
    symbol_table.add_constant("m",_m);
    symbol_table.add_constant("u",_u);
    symbol_table.add_constant("n",_n);
    symbol_table.add_constant("p",_p);
    symbol_table.add_constant("f",_f);

    symbol_table.add_constants();
    expression.register_symbol_table(symbol_table);
    if(!parser.compile(expr_string,expression)){
        errType = parser.get_error(0);
        error = JNI_FALSE;
    }
    obj = allocMathExprErrorObj(env,error,errType);
    return obj;
}

MathExpr::MathExpr(std::string expr_string):
intg(exprtk::ifunction<double>(1)),
 diff(exprtk::ifunction<double>(1)),
maxVal(0)
{
    this->expr_string = "";
    bExprValid = false;
    symbol_table.add_variable("ch1",chVal[0]);
    symbol_table.add_variable("ch2",chVal[1]);
    symbol_table.add_variable("ch3",chVal[2]);
    symbol_table.add_variable("ch4",chVal[3]);
    symbol_table.add_variable("ch5",chVal[4]);
    symbol_table.add_variable("ch6",chVal[5]);
    symbol_table.add_variable("ch7",chVal[6]);
    symbol_table.add_variable("ch8",chVal[7]);
    symbol_table.add_variable("var1",var1);
    symbol_table.add_variable("var2",var2);
    symbol_table.add_variable("time", time);
    symbol_table.add_function("deg", deg);
    symbol_table.add_function("rad", rad);
    symbol_table.add_function("intg", intg);
    symbol_table.add_function("diff", diff);
    symbol_table.add_function("lg", lg);
    symbol_table.add_function("ln", ln);

    //symbol_table.add_constant("E",_E);
    symbol_table.add_constant("P",_PI);
    symbol_table.add_constant("T",_T);
    symbol_table.add_constant("G",_G);
    symbol_table.add_constant("M",_M);
    symbol_table.add_constant("k",_k);
    symbol_table.add_constant("m",_m);
    symbol_table.add_constant("u",_u);
    symbol_table.add_constant("n",_n);
    symbol_table.add_constant("p",_p);
    symbol_table.add_constant("f",_f);
    symbol_table.add_constants();
    expression.register_symbol_table(symbol_table);
    if(!parser.compile(expr_string,expression))
    {
        LOGE("[load_expression] - Parser Error: %s\tExpression: %s\n",
               parser.error().c_str(),
               expr_string.c_str());
    }else{
        this->expr_string = expr_string;
        bExprValid = true;
    }
}


std::string MathExpr::getExpr()
{
    return expr_string;
}
bool MathExpr::isExprValid()
{
    return bExprValid;
}

static uint32_t inside_expr(std::string expr_string){
    int nums = 0;
    uint32_t i = 0;
    for(i = 0;i<expr_string.length();i++){
        if(expr_string[i] == '('){
            nums++;
        }else if(expr_string[i] == ')'){
            nums--;
        }
        if(nums == 0){
            break;
        }
    }
    return i > 0 ? (i + 1) : i;
}
struct {
char * name;
void (*_func)(vector_double_t & vec1, vector_double_t  & vec2, double d);
}gFunc[] = {
        {"intg",intg_func},
        {"diff",diff_func},
};
void MathExpr::calcExpr(const std::vector<vector_double_t *> &chValVec,
                            vector_double_t & vecOut,
                            std::string expr_string)
{
    size_t r,n,len;
    int nums = sizeof(gFunc)/sizeof(gFunc[0]);
    vecOut.clear();
    bool bFunc = false;
    std::vector<vector_double_t> ll;
    std::string old_string = expr_string;
    std::string new_expr_string = "";
    do{
        bFunc = false;
        for(int i = 0;i < nums;i++){
            len = strlen(gFunc[i].name);
            if((r = old_string.find(gFunc[i].name)) != std::string::npos){
                n = inside_expr(old_string.substr( r + len));
                if( n > 2){
                    new_expr_string = old_string.substr( r + len,n);
                    MathExpr mathExpr(new_expr_string);
                    mathExpr.setVar(var1,var2);
                    mathExpr.setDT(dt);
                    mathExpr.setTime(time);
                    mathExpr.calcExpr(chValVec,vecOut,new_expr_string);
                    vector_double_t insideVec;
                    gFunc[i]._func(vecOut,insideVec,this->dt);
                    ll.push_back(insideVec);
                    char str[32];
                    sprintf(str,"VAL%d",(int)ll.size());
                    old_string = old_string.replace(r,len + n,str);
                }else{
                    old_string = old_string.substr(0,r) + old_string.substr(r + len + n);
                }
                bFunc = true;
            }
        }
    }while(bFunc);
    if(ll.size() > 0){
        calcExpr(chValVec,vecOut,ll,old_string);
    } else {
        calcExpr(chValVec,vecOut);
    }
}
void MathExpr::setVar(double var1,double var2)
{
    this->var1 = var1;
    this->var2 = var2;
}
void MathExpr::setDT(double dt)
{
    this->dt = dt;
}
void MathExpr::setTime(double time)
{
    this->time = time;
}
void MathExpr::calcExpr(const std::vector<vector_double_t *> &chValVec,
              vector_double_t & vecOut,
              std::vector<vector_double_t> & ll,
              std::string expr_string)
{

    symbol_table_t xsymbol_table;
    expression_t xexpression;
    parser_t xparser;
    char str[32];
    double * xx = new double[ll.size()];
    xsymbol_table.add_variable("ch1",chVal[0]);
    xsymbol_table.add_variable("ch2",chVal[1]);
    xsymbol_table.add_variable("ch3",chVal[2]);
    xsymbol_table.add_variable("ch4",chVal[3]);
    xsymbol_table.add_variable("ch5",chVal[4]);
    xsymbol_table.add_variable("ch6",chVal[5]);
    xsymbol_table.add_variable("ch7",chVal[6]);
    xsymbol_table.add_variable("ch8",chVal[7]);
    xsymbol_table.add_variable("time",time);
    xsymbol_table.add_variable("var1",var1);
    xsymbol_table.add_variable("var2",var2);
    xsymbol_table.add_function("deg", deg);
    xsymbol_table.add_function("rad", rad);
    xsymbol_table.add_function("intg", intg);
    xsymbol_table.add_function("diff", diff);
    xsymbol_table.add_function("lg", lg);
    xsymbol_table.add_function("ln", ln);

    for(int i = 0;i < ll.size();i++){
        sprintf(str,"VAL%d",(int)(i+1));
        xsymbol_table.add_variable(str,xx[i]);
    }
    //xsymbol_table.add_constant("E",_E);
    xsymbol_table.add_constant("P",_PI);
    xsymbol_table.add_constant("T",_T);
    xsymbol_table.add_constant("G",_G);
    xsymbol_table.add_constant("M",_M);
    xsymbol_table.add_constant("k",_k);
    xsymbol_table.add_constant("m",_m);
    xsymbol_table.add_constant("u",_u);
    xsymbol_table.add_constant("n",_n);
    xsymbol_table.add_constant("p",_p);
    xsymbol_table.add_constant("f",_f);
    xsymbol_table.add_constants();
    xexpression.register_symbol_table(xsymbol_table);
    vecOut.clear();
    if(xparser.compile(expr_string,xexpression))
    {
        double  val;
        int len = chValVec[0]->size();
        int m = chValVec.size();
        for(int i=0;i<len;i++){
            for(int j=0;j<m;j++){
                chVal[j] = chValVec[j]->at(i);
            }
            for(int j=0;j<ll.size();j++){
                if(i < ll[j].size()){
                    xx[j] = ll[j][i];
                } else{
                    xx[j] = 0;
                }
            }
            val = xexpression.value();
            vecOut.push_back(val);
        }
    }else{
        LOGE("[load_expression] - Parser Error: %s\tExpression: %s\n",
             xparser.error().c_str(),
             expr_string.c_str());
    }
    delete [] xx;
}
void MathExpr::calcExpr(const std::vector<vector_double_t *> &chValVec,
                        vector_double_t & vecOut
                        )
{
    double  val;
    int len = chValVec[0]->size();
    int m = chValVec.size();
    for(int i=0;i<len;i++){
        for(int j=0;j<m;j++){
            chVal[j] = chValVec[j]->at(i);
        }
        val = expression.value();
        vecOut.push_back(val);
    }
}
double MathExpr::getMaxVal(){
    return maxVal;
}
void MathExpr::calcExpr(XWaveData *dstWave,const std::vector<XWaveData*>& waveVec)
{
    if(bExprValid){
        int maxlen =0;
        int len = 0;

        dt = 0;
        maxVal = std::numeric_limits<double>::min();
        std::vector<vector_double_t *> chValVec;
        for(auto v:waveVec){
            if(v != nullptr){
                len = v->getWaveLength();
                if(maxlen < len){
                    maxlen = len;
                    dt = 1.0/v->getSampRate();
                }
            }

        }

        for(auto v:waveVec){
            vector_double_t * pv = new vector_double_t ;
            for(int i=0;i<maxlen;i++){
                pv->push_back(v == nullptr? 0 : v->getVerticalPerPix() * v->getWaveData(i));
            }
            chValVec.push_back(pv);
        }
        vector_double_t outVec;
        calcExpr(chValVec,outVec,expr_string);
        for(auto v:chValVec){
            if(v!= nullptr){
                delete v;
            }
        }
        int * pWave = dstWave->getWaveData();
        double val,absVal;
        maxlen = outVec.size();
        for(int i=0;i<maxlen;i++){
            val = outVec[i];
            if(std::numeric_limits<double>::infinity() == val){
                pWave[i] = std::numeric_limits<int>::max();
                maxVal = std::numeric_limits<double>::max();
            }else if(-std::numeric_limits<double>::infinity() == val){
                pWave[i] = std::numeric_limits<int>::min();
                maxVal = std::numeric_limits<double>::max();
            }else if(std::numeric_limits<double>::quiet_NaN() == val) {
                pWave[i] = std::numeric_limits<int>::max();
                maxVal = std::numeric_limits<double>::max();
            }else{
                absVal = std::abs(val);
                if(maxVal < absVal){
                    maxVal = absVal;
                }
                val = val / dstWave->getVerticalPerPix();
                pWave[i] = (int) (val > 0 ? val + 0.01: val - 0.01);
            }
        }
        dstWave->setWaveLength(maxlen);
    }

}
