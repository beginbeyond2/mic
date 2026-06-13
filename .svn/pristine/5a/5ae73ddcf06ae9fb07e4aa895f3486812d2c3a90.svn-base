package com.micsig.base;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileValidateUtil {
    private static final String TAG = "FileValidateUtil";

    public enum TypeEnum {
        MD5, SHA1, SHA256
    }

    private static boolean isEmpty(String str){
        return str == null || str.isEmpty();
    }
    /**
     * @param typeEnum
     * @param standardStr
     * @param fileToCheck
     * @return
     */
    public static boolean validateFile(TypeEnum typeEnum, String standardStr, File fileToCheck) {
        if (isEmpty(standardStr) || fileToCheck == null) {
            Log.e(TAG, "MD5 string empty or updateFile null");
            return false;
        }

        String calculatedDigest = getFileSignature(fileToCheck, typeEnum);

        if (isEmpty(calculatedDigest)) {
            Log.d(TAG, "calculatedDigest null");
            return false;
        }
        return calculatedDigest.equalsIgnoreCase(standardStr);
    }

    private static String getFileSignature(File file, TypeEnum typeEnum) {
        MessageDigest digest;
        String type = "";
        switch (typeEnum) {
            case MD5:
                type = "MD5";
                break;
            case SHA1:
                type = "SHA-1";
                break;
            case SHA256:
                type = "SHA-256";
                break;
        }
        if (isEmpty(type)) {
            Log.e(TAG, "type undefined");
            return null;
        }
        try {
            digest = MessageDigest.getInstance(type);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }

        InputStream is;
        try {
            is = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e(TAG, "Exception while getting FileInputStream");
            return null;
        }

        byte[] buffer = new byte[8192];
        int read;
        try {
            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            byte[] md5sum = digest.digest();
            BigInteger bigInt = new BigInteger(1, md5sum);
            String output = bigInt.toString(16);
            output = String.format("%32s", output).replace(' ', '0');
            return output;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG,"Unable to process file for ");
            return null;
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Exception on closing inputstream:" );
            }
        }
    }

}
