//
// Created by zhuzh on 2018-7-6.
//
#include <memory.h>
#include "smart.h"
#include "micsigcrypto.h"
#include "Property.h"

enum ENUM_FIELD_ITEM{
    FIELD_PTR = 0,
    FIELD_SN,
    FIELD_DISPLAY_SN,
    FIELD_TYPE,
    FIELD_DELIVERYDATE,
    FIELD_OEMNAME,
    FIELD_HW_VERSION,
    FIELD_BANDWIDTH,
    FIELD_MEMDEPTH,
    FIELD_FREQ_COUNTER,
    FIELD_HDMI,
    FIELD_500uV,
    FIELD_AUTORANGE,
    FIELD_WLAN,
    FIELD_AUTOMOTIVE,
    FIELD_FILTER,
    FIELD_BUS,
    FIELD_LANGUAGE,
    FIELD_UUID,
    FIELD_WARRANTYDATE,
    FIELD_HIGHREFRESH,
    FIELD_PRIVATEUUID,
    FIELD_KEY_CURSOR,
    FIELD_MAX
};

struct FIELD_ITEM
{
    ENUM_FIELD_ITEM item;
    const char * name;
    const char * signature;
    jfieldID id;
};


static struct CLASSINFO
{
    bool bInit;
    jclass clazz;
    FIELD_ITEM fields[FIELD_MAX];
    void setLongField(JNIEnv* env, jobject thiz ,int idx,jlong val){
        if(isVaild(idx))
            env->SetLongField(thiz,fields[idx].id,val);
    }
    jlong getLongField(JNIEnv* env, jobject thiz ,int idx){
        jlong val = 0;
        if(isVaild(idx)){
            val = env->GetLongField(thiz,fields[idx].id);
        }
        return val;
    }
    void setIntField(JNIEnv* env, jobject thiz ,int idx,jint val){
        if(isVaild(idx))
            env->SetIntField(thiz,fields[idx].id,val);
    }
    jint getIntField(JNIEnv* env, jobject thiz ,int idx){
        jint val = 0;
        if(isVaild(idx)){
            val = env->GetIntField(thiz,fields[idx].id);
        }
        return val;
    }
    void setBooleanField(JNIEnv* env, jobject thiz ,int idx,jboolean val){
        if(isVaild(idx))
            env->SetBooleanField(thiz,fields[idx].id,val);
    }
    jboolean getBooleanField(JNIEnv* env, jobject thiz ,int idx){
        jboolean val = JNI_FALSE;
        if(isVaild(idx)){
            val = env->GetBooleanField(thiz,fields[idx].id);
        }
        return val;
    }
    void setObjectField(JNIEnv* env, jobject thiz ,int idx,jobject val){
        if(isVaild(idx))
            env->SetObjectField(thiz,fields[idx].id,val);
    }
    jobject getObjectField(JNIEnv* env, jobject thiz ,int idx){
        jobject val = NULL;
        if(isVaild(idx)){
            val = env->GetObjectField(thiz,fields[idx].id);
        }
        return val;
    }
    bool isVaild(int idx){
        return idx >= 0 && idx < FIELD_MAX;
    }
    void setString(JNIEnv * env, jobject thiz,int idx,char * str){
        jstring jstr = env->NewStringUTF(str);
        setObjectField(env,thiz,idx,jstr);
    }
    int getString(JNIEnv * env, jobject thiz,int idx,char * str,int len){
        jstring jstr = (jstring)getObjectField(env,thiz,idx);
        char * p = (char*)env->GetStringUTFChars(jstr,0);
        int utf8len = env->GetStringUTFLength(jstr);
        if(utf8len < len){
            strcpy(str,p);
        }
        env->ReleaseStringUTFChars(jstr,p);
        return utf8len;
    }
    void setBoolArray(JNIEnv * env, jobject thiz,int idx,jboolean * boolArray,int arraySize){
        jbooleanArray  arr = (jbooleanArray)getObjectField(env,thiz,idx);
        jboolean * elems = env->GetBooleanArrayElements(arr,0);
        for(int i=0;i<arraySize;i++){
            elems[i] = boolArray[i];
        }
        env->ReleaseBooleanArrayElements(arr,elems,0);
    }
    int getBoolArray(JNIEnv * env, jobject thiz,int idx,jboolean * boolArray,int arraySize){
        jbooleanArray  arr = (jbooleanArray)getObjectField(env,thiz,idx);
        jboolean * elems = env->GetBooleanArrayElements(arr,0);
        for(int i=0;i<arraySize;i++){
            boolArray[i] = elems[i];
        }
        env->ReleaseBooleanArrayElements(arr,elems,0);
        return arraySize;
    }


}sClassInfo={
        false,
        0,
        {
            {FIELD_PTR,"ptr", "J", 0},
            {FIELD_SN,"sn", "Ljava/lang/String;", 0},
            {FIELD_DISPLAY_SN,"displaySN", "Ljava/lang/String;", 0},
            {FIELD_TYPE,"type", "Ljava/lang/String;", 0},
            {FIELD_DELIVERYDATE,"deliveryDate", "Ljava/lang/String;", 0},
            {FIELD_OEMNAME,"oemName", "Ljava/lang/String;", 0},
            {FIELD_HW_VERSION,"hwVersion", "Ljava/lang/String;", 0},
            {FIELD_BANDWIDTH,"bandWidth", "I", 0},
            {FIELD_MEMDEPTH,"memDepth", "I", 0},
            {FIELD_FREQ_COUNTER,"bEnableFreqCounter", "Z", 0},
            {FIELD_HDMI,"bEnableHdmi", "Z", 0},
            {FIELD_500uV,"bEnable500uV", "Z", 0},
            {FIELD_AUTORANGE,"bEnableAutoRange", "Z", 0},
            {FIELD_WLAN,"bEnableWlan", "Z", 0},
            {FIELD_AUTOMOTIVE,"bEnableAutomotive", "Z", 0},
            {FIELD_FILTER,"bHighLowPassFilter", "Z", 0},
            {FIELD_BUS,"busEnableArray", "[Z", 0},
            {FIELD_LANGUAGE,"languageEnableArray", "[Z", 0},
            {FIELD_UUID,"uuid","Ljava/lang/String;",0},
            {FIELD_WARRANTYDATE,"warrantyDate","I",0},
            {FIELD_HIGHREFRESH,"highRefresh","I",0},
            {FIELD_PRIVATEUUID,"privateUUID","Ljava/lang/String;",0},
            {FIELD_KEY_CURSOR,"bKeyCursorEnable","Z",0}
        },

};


static void getFieldId(JNIEnv* env, jobject thiz ){
    if(!sClassInfo.bInit)
    {
        sClassInfo.clazz = env->GetObjectClass(thiz);
        for(int i=0;i<FIELD_MAX;i++){
            sClassInfo.fields[i].id = env->GetFieldID(
                    sClassInfo.clazz, sClassInfo.fields[i].name,sClassInfo.fields[i].signature);
        }
        sClassInfo.bInit = true;
    }
}


static void sync_update_to_java(JNIEnv *env ,jobject thiz,Property * aProperty){

    sClassInfo.setString(env,thiz,FIELD_SN,aProperty->getSN());
    sClassInfo.setString(env,thiz,FIELD_DISPLAY_SN,aProperty->getDisplaySN());
    sClassInfo.setString(env,thiz,FIELD_TYPE,aProperty->getType());
    sClassInfo.setString(env,thiz,FIELD_DELIVERYDATE,aProperty->getDeliveryDate());
    sClassInfo.setString(env,thiz,FIELD_OEMNAME,aProperty->getOemName());
    sClassInfo.setString(env,thiz,FIELD_HW_VERSION,aProperty->getHwVersion());
    sClassInfo.setIntField(env,thiz,FIELD_BANDWIDTH,aProperty->getBandWidth());
    sClassInfo.setIntField(env,thiz,FIELD_MEMDEPTH,aProperty->getMemDepth());
    sClassInfo.setBooleanField(env,thiz,FIELD_FREQ_COUNTER,aProperty->IsEnableFreqCounter());
    sClassInfo.setBooleanField(env,thiz,FIELD_HDMI,aProperty->IsEnableHdmi());

    sClassInfo.setBooleanField(env,thiz,FIELD_500uV,aProperty->IsEnable500uV());
    sClassInfo.setBooleanField(env,thiz,FIELD_AUTORANGE,aProperty->IsEnableAutoV());
    sClassInfo.setBooleanField(env,thiz,FIELD_WLAN,aProperty->IsEnableWlan());
    sClassInfo.setBooleanField(env,thiz,FIELD_AUTOMOTIVE,aProperty->IsEnableCar());
    sClassInfo.setBooleanField(env,thiz,FIELD_FILTER,aProperty->IsHighLowPassFilter());
    sClassInfo.setBooleanField(env,thiz,FIELD_KEY_CURSOR,aProperty->IsKeyCursorEnable());
    sClassInfo.setString(env,thiz,FIELD_PRIVATEUUID,aProperty->getPrivateUUID());
    jboolean busArray[OPPA_SERIAL_MAX]={false};

    for(int i=0;i<OPPA_SERIAL_MAX;i++){
        busArray[i] = aProperty->IsEnableSerial((_OPTION_PARTS)i);
    }
    sClassInfo.setBoolArray(env,thiz,FIELD_BUS,busArray,OPPA_SERIAL_MAX);
    jboolean langArray[LANGUAGE_MAX]={false};

    for(int i=0;i<LANGUAGE_MAX;i++){
        langArray[i] = aProperty->IsLanguage((_LANGUAGE_t)i);
    }
    sClassInfo.setBoolArray(env,thiz,FIELD_LANGUAGE,langArray,LANGUAGE_MAX);
    sClassInfo.setIntField(env,thiz,FIELD_WARRANTYDATE,aProperty->getWarrantyDate());
    sClassInfo.setIntField(env,thiz,FIELD_HIGHREFRESH,aProperty->getHighRefresh());
}
static void sync_update_to_c(JNIEnv *env ,jobject thiz,Property * xProperty){

    sClassInfo.getString(env,thiz,FIELD_SN,xProperty->getSN(),64);
    sClassInfo.getString(env,thiz,FIELD_DISPLAY_SN,xProperty->getDisplaySN(),64);
    sClassInfo.getString(env,thiz,FIELD_TYPE,xProperty->getType(),128);
    sClassInfo.getString(env,thiz,FIELD_DELIVERYDATE,xProperty->getDeliveryDate(),64);
    sClassInfo.getString(env,thiz,FIELD_OEMNAME,xProperty->getOemName(),64);
    sClassInfo.getString(env,thiz,FIELD_HW_VERSION,xProperty->getHwVersion(),64);
    xProperty->setBandWidth(sClassInfo.getIntField(env,thiz,FIELD_BANDWIDTH));
    sClassInfo.getString(env,thiz,FIELD_UUID,xProperty->getUUID(),128);
}
JNIEXPORT jboolean JNICALL Java_com_micsig_smart_Property_nativeInit
        (JNIEnv * env, jobject thiz, jbyteArray byteArray)
{
    getFieldId(env,thiz);
    jboolean bRet = JNI_FALSE;
    uint8_t *buf = (uint8_t*)env->GetByteArrayElements(byteArray, 0);
    int len = env->GetArrayLength(byteArray);
    Property  *aProperty = new Property;
    if(len == aProperty->getLength()){

        IMicsigCrypto * pMicsigCrypto = IMicsigCrypto::Instance();
        if(pMicsigCrypto){
            pMicsigCrypto->DefaultKey();
            pMicsigCrypto->Decrypt(buf,len,aProperty->getData(),len);
            sClassInfo.getString(env,thiz,FIELD_UUID,aProperty->getUUID(),128);
            //sClassInfo.setString(env,thiz,FIELD_PRIVATEUUID,aProperty->getPrivateUUID());
            if(aProperty->isVaild()){
                Property *xProperty = (Property  *)sClassInfo.getLongField(env,thiz,FIELD_PTR);
                if(xProperty != NULL){
                    delete xProperty;
                }
            } else{
                aProperty->init();
                sClassInfo.getString(env,thiz,FIELD_UUID,aProperty->getUUID(),128);
            }
            sync_update_to_java(env,thiz,aProperty);
            sClassInfo.setLongField(env,thiz,FIELD_PTR,(jlong)aProperty);
            bRet = JNI_TRUE;
        }
    }
    env->ReleaseByteArrayElements(byteArray,(jbyte*) buf, 0);
    if(bRet == JNI_FALSE){
        delete aProperty;
    }
    return bRet;
}
JNIEXPORT jbyteArray JNICALL Java_com_micsig_smart_Property_nativeGetBytes
        (JNIEnv * env, jobject thiz)
{
    getFieldId(env,thiz);
    jbyteArray byteArray = NULL;
    Property *xProperty = (Property  *)sClassInfo.getLongField(env,thiz,FIELD_PTR);
    if(xProperty){
        sync_update_to_c(env,thiz,xProperty);
        xProperty->calcCrc();
        IMicsigCrypto * pMicsigCrypto = IMicsigCrypto::Instance();
        if(pMicsigCrypto){
            CONFIG_MODEL model;
            int len = xProperty->getLength();
            pMicsigCrypto->DefaultKey();
            pMicsigCrypto->Encrypt(xProperty->getData(),len,(uint8_t *)&model, len);
            byteArray = env->NewByteArray(len);
            env->SetByteArrayRegion(byteArray,0,len,(jbyte*)&model);
        }
    }else{
        //返回一块空BUF
        byteArray = env->NewByteArray(sizeof(CONFIG_MODEL));
    }
    return byteArray;
}

JNIEXPORT jboolean JNICALL Java_com_micsig_smart_Property_nativeSerialCodeUpgrade
        (JNIEnv *env, jobject thiz, jstring serialCode)
{
    getFieldId(env,thiz);
    jboolean bRet = JNI_FALSE;
    Property * xProperty = (Property  *)sClassInfo.getLongField(env,thiz,FIELD_PTR);
    if(xProperty){
        sync_update_to_c(env,thiz,xProperty);
        char * s = (char*)env->GetStringUTFChars(serialCode,0);
        int len = env->GetStringUTFLength(serialCode);
        if(xProperty->serialCodeUpgrade(s,len)){

            sync_update_to_java(env,thiz,xProperty);
            bRet = JNI_TRUE;
        }
        env->ReleaseStringUTFChars(serialCode,s);
    }
    return bRet;
}

JNIEXPORT jboolean JNICALL Java_com_micsig_smart_Property_nativeClear
        (JNIEnv *env, jobject thiz )
{
    getFieldId(env,thiz);
    jboolean bRet = JNI_FALSE;
    Property * xProperty = (Property  *)sClassInfo.getLongField(env,thiz,FIELD_PTR);
    if(xProperty){
        xProperty->Clear();
        sync_update_to_java(env,thiz,xProperty);
    }
    return bRet;
}


