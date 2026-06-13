package com.keanbin.pinyinime;

import java.io.FileDescriptor;
import java.util.List;
import java.util.Vector;

/**
 * Created by yangj on 2017/12/5.
 */

public class PinyinDecoderService {
    // 导入本地函数库
    static {
        System.loadLibrary("jni_pinyinime");
    }

    public static boolean imOpenDecoderFd(FileDescriptor fd, long startOffset, long length, byte fn_usr_dict[]) {
        return nativeImOpenDecoderFd(fd, startOffset, length, fn_usr_dict);
    }

    public static int imSearch(byte[] pyBuf, int length) {
        return nativeImSearch(pyBuf, length);
    }

    public static int[] imGetSplStart() {
        return nativeImGetSplStart();
    }

    public static String imGetPyStr(boolean decode) {
        return nativeImGetPyStr(decode);
    }

    public static int imGetPyStrLen(boolean decode) {
        return nativeImGetPyStrLen(decode);
    }

    public static String imGetChoice(int choiceId) {
        return nativeImGetChoice(choiceId);
    }

    public static int imGetFixedLen() {
        return nativeImGetFixedLen();
    }

    public static List<String> imGetChoiceList(int choicesStart, int choicesNum, int sentFixedLen) {
        Vector<String> choiceList = new Vector<String>();
        for (int i = choicesStart; i < choicesStart + choicesNum; i++) {
            String retStr = nativeImGetChoice(i);
            if (0 == i)
                retStr = retStr.substring(sentFixedLen);
            choiceList.add(retStr);
        }
        return choiceList;
    }

    public static List<String> imGetPredictList(int predictsStart, int predictsNum) {
        Vector<String> predictList = new Vector<String>();
        for (int i = predictsStart; i < predictsStart + predictsNum; i++) {
            predictList.add(nativeImGetPredictItem(i));
        }
        return predictList;
    }

    public static int imGetPredictsNum(String fixedStr) {
        return nativeImGetPredictsNum(fixedStr);
    }

    public static void imResetSearch() {
        nativeImResetSearch();
    }

    public static void imCloseDecoder() {
        nativeImCloseDecoder();
    }

    native static boolean nativeImOpenDecoder(byte fn_sys_dict[], byte fn_usr_dict[]);

    /**
     * JNI函数：打开解码器
     */
    native static boolean nativeImOpenDecoderFd(FileDescriptor fd, long startOffset, long length, byte fn_usr_dict[]);

    /**
     * JNI函数：设置最大的长度
     */
    native static void nativeImSetMaxLens(int maxSpsLen, int maxHzsLen);

    /**
     * JNI函数：关闭解码器
     */
    native static boolean nativeImCloseDecoder();

    /**
     * JNI函数：根据拼音查询候选词
     */
    native static int nativeImSearch(byte pyBuf[], int pyLen);

    /**
     * JNI函数：删除指定位置的拼音后进行查询
     */
    native static int nativeImDelSearch(int pos, boolean is_pos_in_splid, boolean clear_fixed_this_step);

    /**
     * JNI函数：重置拼音查询，应该是清除之前查询的数据
     */
    native static void nativeImResetSearch();

    /**
     * JNI函数：增加字母。
     */
    native static int nativeImAddLetter(byte ch);

    /**
     * JNI函数：获取拼音字符串
     */
    native static String nativeImGetPyStr(boolean decoded);

    /**
     * JNI函数：获取拼音字符串的长度
     */
    native static int nativeImGetPyStrLen(boolean decoded);

    /**
     * JNI函数：获取每个拼写的开始位置，猜测：第一个元素是拼写的总数量？
     */
    native static int[] nativeImGetSplStart();

    /**
     * JNI函数：获取指定位置的候选词
     */
    native static String nativeImGetChoice(int choiceId);

    /**
     * JNI函数：获取候选词的数量
     */
    native static int nativeImChoose(int choiceId);

    /**
     * JNI函数：取消最后的选择
     */
    native static int nativeImCancelLastChoice();

    /**
     * JNI函数：获取固定字符的长度
     */
    native static int nativeImGetFixedLen();

    /**
     * JNI函数：取消输入
     */
    native static boolean nativeImCancelInput();

    /**
     * JNI函数：刷新缓存
     */
    native static boolean nativeImFlushCache();

    /**
     * JNI函数：根据字符串 fixedStr 获取预报的候选词
     */
    native static int nativeImGetPredictsNum(String fixedStr);

    /**
     * JNI函数：获取指定位置的预报候选词
     */
    native static String nativeImGetPredictItem(int predictNo);

    // Sync related

    /**
     * JNI函数：同步到用户词典，猜测：是不是记住用户的常用词。
     */
    native static String nativeSyncUserDict(byte[] user_dict, String tomerge);

    /**
     * JNI函数：开始用户词典同步
     */
    native static boolean nativeSyncBegin(byte[] user_dict);

    /**
     * JNI函数：同步结束
     */
    native static boolean nativeSyncFinish();

    /**
     * JNI函数：同步获取Lemmas
     */
    native static String nativeSyncGetLemmas();

    /**
     * JNI函数：同步存入Lemmas
     */
    native static int nativeSyncPutLemmas(String tomerge);

    /**
     * JNI函数：同步获取最后的数量
     */
    native static int nativeSyncGetLastCount();

    /**
     * JNI函数：同步获取总数量
     */
    native static int nativeSyncGetTotalCount();

    /**
     * JNI函数：同步清空最后获取
     */
    native static boolean nativeSyncClearLastGot();

    /**
     * JNI函数：同步获取容量
     */
    native static int nativeSyncGetCapacity();
}
