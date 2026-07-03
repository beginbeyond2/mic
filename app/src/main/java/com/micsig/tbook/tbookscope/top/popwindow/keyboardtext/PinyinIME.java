package com.micsig.tbook.tbookscope.top.popwindow.keyboardtext; // 包声明：文本键盘子包

import android.content.Context; // 导入上下文类
import android.content.res.AssetFileDescriptor; // 导入资产文件描述符类

import com.keanbin.pinyinime.PinyinDecoderService; // 导入拼音解码服务类
import com.micsig.base.Logger; // 导入日志工具类
import com.micsig.tbook.tbookscope.R; // 导入资源类

import java.io.IOException; // 导入IO异常类
import java.util.Arrays; // 导入数组工具类
import java.util.List; // 导入列表类

/**
 * ┌──────────────────────────────────────────────────────────────────┐
 * │ 模块定位：文本键盘 - 拼音输入法引擎封装                            │
 * │ 核心职责：封装JNI拼音解码服务，提供拼音→候选词和联想词查询功能       │
 * │ 架构设计：单例式封装，初始化时加载字典文件到JNI解码器               │
 * │ 数据流向：拼音字符串 → PinyinDecoderService(JNI) → 候选词列表      │
 * │ 依赖关系：PinyinDecoderService(JNI), R.raw.dict_pinyin             │
 * │ 使用场景：TopDialogCandidatesWord中调用，实现中文拼音输入           │
 * └──────────────────────────────────────────────────────────────────┘
 *
 * Created by yangj on 2017/12/6.
 */

public class PinyinIME { // 拼音输入法引擎
    private Context context; // 上下文对象
    private String mUsr_dict_file; // 用户字典文件路径

    /**
     * 构造函数
     * @param context 上下文对象
     */
    public PinyinIME(Context context) { // 构造函数
        this.context = context; // 保存上下文
        init(); // 初始化
    }

    /**
     * 初始化用户字典文件和拼音引擎
     */
    private void init() { // 初始化方法
        mUsr_dict_file = context.getFileStreamPath("usr_dict.dat").getPath(); // 获取用户字典文件路径
        try { // 尝试创建空文件
            context.openFileOutput("dummy", 0).close(); // 创建空文件确保文件目录存在
        } catch (IOException e) { // 忽略异常
        }
        initPinyinEngine(); // 初始化拼音引擎
    }

    /**
     * 初始化拼音引擎
     */
    private void initPinyinEngine() { // 初始化拼音引擎方法
        byte usr_dict[]; // 用户字典字节数组
        usr_dict = new byte[mUsr_dict_file.length() + 256]; // 分配字节数组（路径长度+256余量）

        AssetFileDescriptor afd = context.getResources().openRawResourceFd(R.raw.dict_pinyin); // 打开原始拼音字典文件
        if (getUsrDictFileName(usr_dict)) { // 如果获取用户字典文件名成功
            // JNI函数：打开解码器
            PinyinDecoderService.imOpenDecoderFd(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength(), usr_dict); // 调用JNI打开解码器
        }
        try { // 尝试关闭文件描述符
            afd.close(); // 关闭资产文件描述符
        } catch (IOException e) { // 忽略异常
        }
    }

    /**
     * 将用户字典文件路径转为字节数组
     * @param usr_dict 输出的字节数组
     * @return true表示成功
     */
    private boolean getUsrDictFileName(byte usr_dict[]) { // 获取用户字典文件名方法
        if (null == usr_dict) { // 如果数组为空
            return false; // 返回失败
        }
        for (int i = 0; i < mUsr_dict_file.length(); i++) // 遍历路径字符
            usr_dict[i] = (byte) mUsr_dict_file.charAt(i); // 将字符转为字节存入数组
        usr_dict[mUsr_dict_file.length()] = 0; // 添加结束符0
        return true; // 返回成功
    }

    /**
     * 获得输入拼音之后的生成词组
     *
     * @param pinyin 输入的拼音
     * @return 候选词列表
     */
    public List<String> getChoiceList(String pinyin) { // 获取候选词列表方法
        if(pinyin.length() > 16){ // 如果拼音长度超过16
            pinyin = pinyin.substring(0,16); // 截取前16个字符
        }
        byte[] pyBuf = new byte[pinyin.length() + 1]; // 创建拼音字节数组


        for (int i = 0; i < pinyin.length(); i++) // 遍历拼音字符
            pyBuf[i] = (byte) pinyin.charAt(i); // 将字符转为字节
        pyBuf[pinyin.length()] = 0; // 添加结束符0


        PinyinDecoderService.imResetSearch(); // 重置搜索状态

        int totalChoicesNum = PinyinDecoderService.imSearch(pyBuf, pinyin.length()); // 执行拼音搜索，获取候选词总数
        int mFixedLen = PinyinDecoderService.imGetFixedLen(); // 获取固定匹配长度
        List<String> choiceList = PinyinDecoderService.imGetChoiceList(0, totalChoicesNum, mFixedLen); // 获取候选词列表
        Logger.i("\nPinyinEngine:totalChoicesNum:" + totalChoicesNum + "\tmFixedLen:" + mFixedLen); // 打印日志
//        Logger.i("\nchoiceList:" + Arrays.toString(choiceList.toArray()));
        return choiceList; // 返回候选词列表
    }

    /**
     * 获得点击汉字之后的联想词组
     *
     * @param predict 点击的汉字
     * @return 联想词列表
     */
    public List<String> getPredictList(String predict) { // 获取联想词列表方法
        int totalPredictsNum = PinyinDecoderService.imGetPredictsNum(predict); // 获取联想词总数
        List<String> predictList = PinyinDecoderService.imGetPredictList(0, totalPredictsNum); // 获取联想词列表
        Logger.i("\ntotalPredictsNum:" + totalPredictsNum + // 打印联想词数量日志
                "\npredictList:" + Arrays.toString(predictList.toArray())); // 打印联想词列表日志
        return predictList; // 返回联想词列表
    }

}
