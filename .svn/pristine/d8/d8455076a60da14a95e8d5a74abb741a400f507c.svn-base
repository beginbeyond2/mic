package com.micsig.tbook.tbookscope.rightslipmenu.util;

import android.os.Environment;
import android.util.Base64;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class UtilFile {
    public static final String TAG = "UtilFile";

    /**
     * String fileName = getSDPath() +"/" + name;//以name存在目录中
     *
     * @return
     */
    public static String getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(android.os.Environment.MEDIA_MOUNTED);//判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();//获取跟目录
        }
        return sdDir != null ? sdDir.toString() : null;
    }

    public static ArrayList<DataBean> readTranFile(String desFile) {
        ArrayList<DataBean> list = new ArrayList<>();
        File file = new File(desFile);
        if (file.exists() == false) {
            return null;
        }
        try {
            InputStream instream = new FileInputStream(file);
            InputStreamReader inputreader = new InputStreamReader(instream);
            BufferedReader buffreader = new BufferedReader(inputreader);
            String line = "";
            while ((line = buffreader.readLine()) != null) {
                line = decryptPassword(line);
                String[] s = line.split(",");
                try {
                    DataBean bean = new DataBean(Float.valueOf(s[0]), Float.valueOf(s[1]), Float.valueOf(s[2]), s[3], Float.valueOf(s[4]), Float.valueOf(s[5]));
                    list.add(bean);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            instream.close();//关闭输入流


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }


        return list;
    }

    //region 字符串的加密与解密

    /**
     * 加密
     **/
    private static String encryptPassword(String clearText) {
        try {
            DESKeySpec keySpec = new DESKeySpec(
                    MyConstant.PASSWORD_ENC_SECRET.getBytes("UTF-8"));
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey key = keyFactory.generateSecret(keySpec);

            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            String encrypedPwd = Base64.encodeToString(cipher.doFinal(clearText
                    .getBytes("UTF-8")), Base64.DEFAULT);
            return encrypedPwd;
        } catch (Exception e) {
        }
        return clearText;
    }


    public class MyConstant {
        public static final String PASSWORD_ENC_SECRET = "mythmayor";
    }

    /**
     * 解密
     **/
    private static String decryptPassword(String encryptedPwd) {
        try {
            DESKeySpec keySpec = new DESKeySpec(MyConstant.PASSWORD_ENC_SECRET.getBytes("UTF-8"));
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey key = keyFactory.generateSecret(keySpec);

            byte[] encryptedWithoutB64 = Base64.decode(encryptedPwd, Base64.DEFAULT);
            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] plainTextPwdBytes = cipher.doFinal(encryptedWithoutB64);
            return new String(plainTextPwdBytes);
        } catch (Exception e) {
        }
        return encryptedPwd;
    }

    //endregion


}
