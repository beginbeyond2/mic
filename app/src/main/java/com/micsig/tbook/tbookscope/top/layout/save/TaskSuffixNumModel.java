package com.micsig.tbook.tbookscope.top.layout.save; // 保存功能模块的包声明

import androidx.lifecycle.LiveData; // 导入LiveData可观察数据类
import androidx.lifecycle.MutableLiveData; // 导入可变LiveData类
import androidx.lifecycle.ViewModel; // 导入ViewModel基类

/*
 * ╔══════════════════════════════════════════════════════════════════════╗
 * ║  模块定位: 保存功能模块 - 自动保存任务序号模型                         ║
 * ║  核心职责: 管理自动保存任务的文件名后缀序号，支持UI实时更新              ║
 * ║  架构设计: 基于ViewModel+LiveData的MVVM架构，生命周期感知的数据模型     ║
 * ║  数据流向: AutoSaveTaskManager → updateText() → textLiveData → UI    ║
 * ║  依赖关系: 继承ViewModel，使用MutableLiveData/LiveData               ║
 * ║  使用场景: 自动保存任务中文件名序号递增时，通知UI更新序号显示            ║
 * ╚══════════════════════════════════════════════════════════════════════╝
 */

/**
 * 自动保存任务后缀序号ViewModel
 * <p>管理文件名后缀序号的状态，通过LiveData实现UI自动更新</p>
 */
public class TaskSuffixNumModel extends ViewModel { // 继承ViewModel，生命周期感知的序号模型

    /** 文件名后缀序号的LiveData，用于观察序号变化并更新UI */
    private final MutableLiveData<String> textLiveData = new MutableLiveData<>(); // 创建可变LiveData实例

    /**
     * 获取文本LiveData的不可变引用，供UI层观察
     * @return 文本LiveData，UI层可订阅此观察序号变化
     */
    public LiveData<String> getTextLiveData(){ // 获取不可变LiveData引用
        return textLiveData; // 返回textLiveData
    }

    /**
     * 更新后缀序号文本，通知UI层刷新
     * @param newSuffixNum 新的后缀序号字符串
     */
    public void updateText(String newSuffixNum){ // 更新序号文本
        textLiveData.postValue(newSuffixNum); // 使用postValue确保线程安全地更新值
    }
}
