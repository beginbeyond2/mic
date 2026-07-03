package com.micsig.tbook.tbookscope.main.dialog;

import android.content.Context;

import com.micsig.tbook.tbookscope.MainActivity;
import com.micsig.tbook.tbookscope.R;

/**
 * @auother Liwb
 * @description:
 * @data:2025-8-5 16:44
 */

/*
 * +===========================================================================+
 * |                            DialogManage                                   |
 * |                         对话框管理器（单例模式）                           |
 * +===========================================================================+
 * | 模块定位: 主界面对话框统一管理模块                                          |
 * | 核心职责: 管理应用程序中各类对话框的创建、初始化和获取                       |
 * | 架构设计: 采用单例模式，全局唯一实例，集中管理对话框引用                     |
 * | 数据流向: MainActivity -> DialogManage -> DialogOk/DialogOkCancel          |
 * | 依赖关系: Context, MainActivity, DialogOk, DialogOkCancel                 |
 * | 使用场景: 应用启动时初始化，运行时通过getIns()获取实例访问对话框             |
 * +===========================================================================+
 */
public class DialogManage {
    // 单例实例，在类加载时创建 // 私有静态成员变量
    private static DialogManage instance=new DialogManage();  // 创建唯一的单例实例

    /**
     * 获取DialogManage单例实例
     * @return DialogManage实例
     */
    public static DialogManage getIns(){return instance;}  // 返回单例实例

    private Context context;  // 应用上下文引用
    private DialogOk dialogOk;  // 确认对话框引用
    private DialogOkCancel dialogOkCancel;  // 确认取消对话框引用

    /**
     * 初始化对话框管理器
     * 从MainActivity中获取对话框视图引用
     * @param context 应用上下文（需为MainActivity实例）
     */
    public void init(Context context)
    {
        this.context = context;  // 保存上下文引用
        dialogOk= (DialogOk) ((MainActivity) context).findViewById(R.id.dialogOk);  // 从MainActivity获取确认对话框
        dialogOkCancel= (DialogOkCancel) ((MainActivity) context).findViewById(R.id.dialogOkCancel);  // 从MainActivity获取确认取消对话框
    }

    /**
     * 获取确认对话框实例
     * @return DialogOk实例
     */
    public DialogOk getDialogOk(){
        return dialogOk;  // 返回确认对话框引用
    }

    /**
     * 获取确认取消对话框实例
     * @return DialogOkCancel实例
     */
    public DialogOkCancel getDialogOkCancel(){
        return dialogOkCancel;  // 返回确认取消对话框引用
    }
}