package com.micsig.tbook.scope.Data;

/**
 * Created by zhuzh on 2018-9-13.
 */

public class ImageParam extends BaseDirectBuffer{


        public ImageParam() {
            super(128);
        }
        public void setPerPixelByte(int val){
            setVal(0,val);
        }
        public void setXOffset(int val){
            setVal(4,val);
        }
        public void setYOffset(int val){
            setVal(8,val);
        }
        public void setWidth(int w){
            setVal(12,w);
        }
        public void setHeight(int h){
            setVal(16,h);
        }
        public void setForegroundColor(int val){
            setVal(20,val);
        }
        public void setBackgroundColor(int val){
            setVal(24,val);
        }
        public void setVerticalPerPix(double val){
            setVal(28,val);
        }
        public void setTimeScaleVal(double val){
            setVal(36,val);
        }

        public void setCursor(int x1,int x2){
            setVal(44,x1);
            setVal(48,x2);
        }

        public double getCursorX1Value(){
            return getDoubleVal(52);
        }
        public double getCursorX2Value(){
            return getDoubleVal(60);
        }

        public boolean isCursorX1Valid(){
            return getByteVal(68) != 0;
        }
        public boolean isCursorX2Valid(){
            return getByteVal(69) != 0;
        }

        public void setStartX(int x){
            setVal(72,x);
        }
        public void setEndX(int x){
            setVal(76,x);
        }



}
