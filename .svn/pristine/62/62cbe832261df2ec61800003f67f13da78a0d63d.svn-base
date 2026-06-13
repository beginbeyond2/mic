package com.micsig.smart;

/**
 * Created by zhuzh on 2018-7-6.
 */

public class Property {

    public static final int BUS_UART  = 0;  //：串型总线
    public static final int BUS_LIN  = 1;
    public static final int BUS_SPI  = 2;
    public static final int BUS_CAN  = 3;
    public static final int BUS_I2C  = 4;
    public static final int BUS_1553B = 5;
    public static final int BUS_429  = 6;
    public static final int BUS_CAN_FD  = 7;
    public static final int BUS_CNT = 8;

    public static final int LANGUAGE_en_US = 0;	//0 英语
    public static final int LANGUAGE_zh_CN = 1;	//1 简体中文
    public static final int LANGUAGE_zh_TW = 2;	//2 繁体中文
    public static final int LANGUAGE_de_DE = 3;	//3 德语
    public static final int LANGUAGE_ru_RU = 4;	//4 俄语
    public static final int LANGUAGE_es_ES = 5;	//5 西班牙语
    public static final int LANGUAGE_ko_KR = 6;	//6 朝鲜语
    public static final int LANGUAGE_cs_CZ = 7;	//7 捷克语
    public static final int LANGUAGE_ar_AE = 8;	//8 阿拉伯语
    public static final int LANGUAGE_it_CH = 9;	//9 意大利语
    public static final int LANGUAGE_tr_TR = 10;	//10 土耳其语
    public static final int LANGUAGE_fr_FR = 11;	//11 法语
    public static final int LANGUAGE_CNT = 12;
    private long ptr;
    private String uuid="";
    private String sn="";
    private String displaySN="";
    private String type="";
    private String deliveryDate="";
    private String oemName="";
    private String hwVersion="1";
    private int bandWidth=0;
    private int memDepth=0;
    private int warrantyDate=0;
    private int highRefresh=0;
    private boolean bEnableFreqCounter = false;
    private boolean bEnableHdmi = false;
    private boolean bEnable500uV = false;
    private boolean bEnableAutoRange = false;
    private boolean bDeliveryDate = false;
    private boolean bEnableWlan = false;
    private boolean bEnableAutomotive = false;
    private boolean bHighLowPassFilter = false;
    private boolean bKeyCursorEnable = false;

    private boolean bVaild = false;
    private boolean [] busEnableArray = new boolean[BUS_CNT];
    private boolean [] languageEnableArray = new boolean[LANGUAGE_CNT];
    private String privateUUID="";
    private static boolean isBusValid(int busIdx){
        return busIdx >= BUS_UART && busIdx < BUS_CNT;
    }
    private static boolean isLanguageValid(int langIdx){
        return langIdx >= LANGUAGE_en_US && langIdx < LANGUAGE_CNT;
    }
    public Property(){
        for(int i=0;i<busEnableArray.length;i++){
            busEnableArray[i] = false;
        }
        for(int i=0;i<languageEnableArray.length;i++){
            languageEnableArray[i] = false;
        }
    }
    public boolean initProperty(byte[] bytes){
        bVaild = nativeInit(bytes);
        return bVaild;
    }
    public boolean isValid(){
        return bVaild;
    }
    public void setUUID(String uuid){
        this.uuid = uuid;
    }
    public String getUUID(){
        return uuid;
    }
    public String getSN() {

        return sn;
    }
    public void setSN(String sn){
        this.sn = sn;
    }

    public String getDisplaySN() {
        return displaySN;
    }
    public void setDisplaySN(String displaySN){
        this.displaySN = displaySN;
    }
    public void setType(String type){
        this.type = type;
    }
    public String getType() {
        return type;
    }
    public void setDeliveryDate(String deliveryDate){
        this.deliveryDate = deliveryDate;
    }
    public String getDeliveryDate() {
        return deliveryDate;
    }
    public void setOemName(String oemName){
        this.oemName = oemName;
    }
    public String getOemName() {
        return oemName;
    }
    public void setHwVersion(String hwVersion){
        this.hwVersion = hwVersion;
    }
    public String getHwVersion() {

        return hwVersion;
    }
    public String getPrivateUUID(){return privateUUID;}

    public void setBandWidth(int bandWidth){
        this.bandWidth=bandWidth;
    }
    public int getBandWidth() {
        return bandWidth;
    }

    public int getMemDepth() {
        return memDepth;
    }

    public int getHighRefresh(){return highRefresh;}

    public boolean isEnableFreqCounter() {
        return bEnableFreqCounter;
    }

    public boolean isEnableHdmi() {
        return bEnableHdmi;
    }

    public boolean isEnable500uV() {
        return bEnable500uV;
    }

    public boolean isEnableAutoRange() {
        return bEnableAutoRange;
    }

    public boolean isDeliveryDate() {
        if(deliveryDate != null && deliveryDate.length() > 0)
        {
            return  true;
        }
        return false;
    }

    public boolean isEnableWlan() {
        return bEnableWlan;
    }

    public boolean isEnableAutomotive() {
        return bEnableAutomotive;
    }

    public boolean isHighLowPassFilter() {
        return bHighLowPassFilter;
    }

    public boolean isKeyCursorEnable(){return bKeyCursorEnable;}

    public boolean isEnableBus(int busIdx){
        if(isBusValid(busIdx))
            return busEnableArray[busIdx];
        return false;
    }

    public boolean isEnableLanguage(int langIdx){
        if(isLanguageValid(langIdx))
            return languageEnableArray[langIdx];
        return false;
    }

    public int getWarrantyDate(){
        return warrantyDate;
    }
    public byte[] getBytes(){
        return nativeGetBytes();
    }
    public boolean serialCodeUpgrade(String serialCode){
        if(serialCode != null) {
            return nativeSerialCodeUpgrade(serialCode);
        }
        return false;
    }
    public boolean clear(){
        return nativeClear();
    }
    private native boolean nativeInit(byte [] bytes);
    private native byte[] nativeGetBytes();
    private native boolean nativeSerialCodeUpgrade(String serialCode);
    private native boolean nativeClear();

}
