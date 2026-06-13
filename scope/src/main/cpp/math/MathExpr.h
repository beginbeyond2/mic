//
// Created by zhuzh on 2019-8-5.
//

#ifndef TBOOKSCOPE_MATHEXPR_H
#define TBOOKSCOPE_MATHEXPR_H

#include "../wavedata.h"
#include <string>
#include <jni.h>
#include "exprtk.hpp"
template <typename T>
struct Deg : public exprtk::ifunction<T>
{
    Deg() : exprtk::ifunction<T>(1){}

    T operator()(const T& v1)
    {
        return exprtk::details::numeric::r2d(v1);
    }
};

template <typename T>
struct Rad : public exprtk::ifunction<T>
{
    Rad() : exprtk::ifunction<T>(1){}

    T operator()(const T& v1)
    {
        return exprtk::details::numeric::d2r(v1);
    }
};

template <typename T>
struct Lg : public exprtk::ifunction<T>
{
    Lg() : exprtk::ifunction<T>(1){}

    T operator()(const T& v1)
    {
        if(v1 < 0){
            return std::numeric_limits<T>::infinity();
        }else{
            return exprtk::details::numeric::log10(v1);
        }

    }
};

template <typename T>
struct Ln : public exprtk::ifunction<T>
{
    Ln() : exprtk::ifunction<T>(1){}

    T operator()(const T& v1)
    {
        if(v1 < 0){
            return std::numeric_limits<T>::infinity();
        }else {
            return exprtk::details::numeric::log(v1);
        }
    }
};

typedef exprtk::symbol_table<double> symbol_table_t;
typedef exprtk::expression<double> expression_t;
typedef exprtk::parser<double>  parser_t;
typedef std::vector<double> vector_double_t;
class MathExpr {
public:
    MathExpr(std::string expr_string);

    std::string getExpr();
    bool isExprValid();

    void calcExpr(XWaveData *dstWave,const std::vector<XWaveData*> & waveVec);
    void calcExpr(const std::vector<vector_double_t *> & chValVec,
                    vector_double_t & vecOut,
                    std::string expr_string);
    void calcExpr(const std::vector<vector_double_t *> &chValVec,
                  vector_double_t & vecOut);
    void calcExpr(const std::vector<vector_double_t *> &chValVec,
                  vector_double_t & vecOut,
                  std::vector<vector_double_t> &ll,
                  std::string expr_string);
    void setVar(double var1,double var2);
    void setDT(double dt);
    void setTime(double time);
   static jobject isMathExprValid(JNIEnv *env,std::string expr_string);
    double getMaxVal();
private:

    symbol_table_t symbol_table;
    expression_t expression;
    parser_t parser;
    double chVal[8];

    double var1;
    double var2;
    double dt;
    double time;

    double maxVal;

    std::string expr_string;
    bool bExprValid;

    Deg<double> deg;
    Rad<double> rad;
    Lg<double> lg;
    Ln<double> ln;
    exprtk::ifunction<double> intg;
    exprtk::ifunction<double> diff;



};


#endif //TBOOKSCOPE_MATHEXPR_H
