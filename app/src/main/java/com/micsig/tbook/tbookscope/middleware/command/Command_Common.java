package com.micsig.tbook.tbookscope.middleware.command;

import android.os.Build;

import com.micsig.smart.PropertyManage;
import com.micsig.tbook.scope.Scope;
import com.micsig.tbook.tbookscope.BuildConfig;

public class Command_Common {
    //            new SCPICommandStruct("*CLS","SCPI_Common","CLS"),//
//            new SCPICommandStruct("*ESE","SCPI_Common","ESE"),//
//            new SCPICommandStruct("*ESE?","SCPI_Common","ESEQ"),//
//            new SCPICommandStruct("*ESR","SCPI_Common","ESR"),//
//            new SCPICommandStruct("*IDN?","SCPI_Common","IDNQ"),//
//            new SCPICommandStruct("*OPC","SCPI_Common","OPC"),//
//            new SCPICommandStruct("*OPC?","SCPI_Common","OPCQ"),//
//            new SCPICommandStruct("*RST","SCPI_Common","RST"),//
//            new SCPICommandStruct("*SRE","SCPI_Common","SRE"),//
//            new SCPICommandStruct("*SRE?","SCPI_Common","SREQ"),//
//            new SCPICommandStruct("*STB?","SCPI_Common","STBQ"),//
//            new SCPICommandStruct("*TST?","SCPI_Common","TSTQ"),//
//            new SCPICommandStruct("*WAI","SCPI_Common","WAI"),//

    public  void CLS( ){

    }
    public  void ESE( ){

    }
    public  String ESEQ( ){
        return "";
    }
    public  void ESR( ){

    }
    //Micsig,<model>,<serial numbe>,X.X.XXX
    //<model>：仪器型号。
    //<serial numbe>：仪器序列号。
    //X.X.XXX：仪器软件版本
    //如： Micsig，TO202A，232000054,4.0.155
    //获得失败以“Failed”显示
    public  String IDNQ( ){
        String result="Micsig,";
        PropertyManage propertyManage = PropertyManage.getInstance();
        propertyManage.update();
        String model=propertyManage.getProperty().getType();
        if (model!=null && model.length()>0){
            result +=model+",";
        }else {
            result += Build.MODEL + ",";
        }
        String SN=propertyManage.getProperty().getSN();
        if (SN!=null && SN.length()>0){
            result+=SN+",";
        }else{
            result += "123456789ABCDEF" + ",";
        }
        String ver=propertyManage.getProperty().getHwVersion();
        if (ver!=null && ver.length()>0){
            result+= ver + "." + BuildConfig.VERSION_CODE + "." + Scope.fpgaVer;
        }
        else {
            result+= "1." + BuildConfig.VERSION_CODE + "." + Scope.fpgaVer;
        }
        return result;
    }
    public  void OPC( ){

    }
    public  String OPCQ( ){
        return "";
    }
    public  void RST( ){

    }
    public  void SRE( ){

    }
    public  String SREQ(){
        return "";
    }
    public  String STBQ(){
        return "";
    }
    public  String TSTQ(){
        return "";
    }
    public  void WAI(){

    }

}
