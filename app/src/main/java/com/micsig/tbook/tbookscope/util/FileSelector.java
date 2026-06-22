package com.micsig.tbook.tbookscope.util; // // 工具类包，存放应用级工具组件

/*
 * =====================================================================
 * |  _____ _      ___   __  __  ___  ___  ___                           |
 * | |_   _| |    | __| |  \/  |/ _ \| _ \| __|                          |
 * |   | | | |__  | _|  | |\/| | (_) |   /| _|                           |
 * |   |_| |____| |___| |_|  |_|\___/|_|_\|___|                          |
 * |                                                                     |
 * |  模块名称: FileSelector (文件选择器)                                   |
 * |  所属层级: com.micsig.tbook.tbookscope.util                         |
 * |  核心职责: 封装PathSelector第三方库，提供文件/文件夹选择功能             |
 * |                                                                     |
 * |  架构设计:                                                           |
 * |    FileSelector封装了PathSelector库的Dialog模式，提供三种选择器：       |
 * |    1. buildSaveFileSelector    - 保存文件选择器（选择文件夹）           |
 * |    2. buildSaveFileRefSelector - 参考波形文件选择器                    |
 * |    3. buildInvokeFileSelector  - 调用文件选择器（选择文件）             |
 * |    通过PathSelectHandler回调接口将选择结果传递给调用方。                |
 * |    同时管理虚拟键盘（TopDialogTextKeyBoard）用于新建文件夹输入。         |
 * |                                                                     |
 * |  数据流向:                                                           |
 * |    buildXxxSelector()                                                |
 * |      └─> PathSelector.build() 构建选择器                              |
 * |      └─> 设置各种Listener（新建文件夹/确认/取消/删除/确定）             |
 * |      └─> 用户操作 → Listener回调                                     |
 * |      └─> pathSelectHandler.addPathToSet() 通知调用方                  |
 * |                                                                     |
 * |    RxBus事件监听:                                                    |
 * |      MSG_HIDE_KEYBOARD    → 隐藏虚拟键盘                             |
 * |      MSG_HIDE_FILESELECTOR → 关闭文件选择器对话框                     |
 * |                                                                     |
 * |  依赖关系:                                                           |
 * |    - PathSelector     : 第三方文件选择库                               |
 * |    - TopDialogTextKeyBoard : 虚拟键盘（新建文件夹输入）                |
 * |    - RxBus            : 事件总线，监听隐藏事件                         |
 * |    - DToast           : 底部Toast提示                                 |
 * |    - DToastDialog     : 选择器内嵌Toast                               |
 * |                                                                     |
 * |  使用示例:                                                           |
 * |    FileSelector selector = new FileSelector(context, handler);       |
 * |    selector.buildSaveFileSelector(root, display, frag, ctx);         |
 * |    selector.buildInvokeFileSelector(root, display, frag, ctx,        |
 * |                                      isFilesOnly, fileTypes);        |
 * |    selector.isFAT32(rootPath);  // 检测文件系统类型                    |
 * |                                                                     |
 * =====================================================================
 */

import android.app.Dialog; // // 对话框基类
import android.content.Context; // // Android上下文接口
import android.graphics.Color; // // 颜色常量
import android.graphics.PixelFormat; // // 像素格式常量
import android.os.storage.StorageVolume; // // 存储卷信息，用于FAT32检测
import android.util.Log; // // Android日志工具
import android.view.View; // // 视图基类
import android.view.WindowManager; // // 窗口管理器，用于添加悬浮键盘

import androidx.fragment.app.Fragment; // // AndroidX Fragment基类

import com.micsig.tbook.tbookscope.MainViewGroup; // // 主视图组
import com.micsig.tbook.tbookscope.R; // // 资源ID常量
import com.micsig.tbook.tbookscope.rxjava.RxBus; // // RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // // RxJava事件枚举
import com.micsig.tbook.tbookscope.top.popwindow.keyboardtext.TopDialogTextKeyBoard; // // 虚拟键盘对话框
import com.molihuan.pathselector.PathSelector; // // 第三方文件选择器
import com.molihuan.pathselector.dao.SelectConfigData; // // 选择器配置数据
import com.molihuan.pathselector.dialog.impl.DeleteConfirmDialog; // // 删除确认对话框
import com.molihuan.pathselector.entity.FileBean; // // 文件实体Bean
import com.molihuan.pathselector.entity.FontBean; // // 字体配置Bean
import com.molihuan.pathselector.fragment.BasePathSelectFragment; // // 选择器Fragment基类
import com.molihuan.pathselector.fragment.impl.FileShowFragment; // // 文件显示Fragment
import com.molihuan.pathselector.fragment.impl.PathSelectFragment; // // 路径选择Fragment
import com.molihuan.pathselector.listener.CommonItemListener; // // 通用点击监听器
import com.molihuan.pathselector.service.IConfigDataBuilder; // // 配置构建器接口
import com.molihuan.pathselector.service.impl.ConfigDataBuilderImpl; // // 配置构建器实现
import com.molihuan.pathselector.utils.DToastDialog; // // 选择器内嵌Toast
import com.molihuan.pathselector.utils.DisplayNameTools; // // 显示名称工具
import com.molihuan.pathselector.utils.MConstants; // // 选择器常量
import com.molihuan.pathselector.utils.ReflectTools; // // 反射工具，用于获取存储卷信息

import java.io.File; // // 文件操作类
import java.util.ArrayList; // // 动态数组
import java.util.List; // // 列表接口

import io.reactivex.rxjava3.functions.Consumer; // // RxJava消费者接口

/**
 * 文件选择器。
 * 封装PathSelector第三方库，提供保存文件、参考波形、调用文件三种选择器。
 * 支持新建文件夹、FAT32文件系统检测、RxBus事件监听。
 */
public class FileSelector {

    /** 窗口管理器，用于添加悬浮虚拟键盘 */
    public WindowManager windowManager; // // 窗口管理器

    /** 虚拟键盘对话框，用于新建文件夹时输入名称 */
    private TopDialogTextKeyBoard fileSelectorTextKeyBoard; // // 虚拟键盘

    /** 文件选择结果回调处理器 */
    public PathSelectHandler pathSelectHandler; // // 选择结果回调

    /** 选择器内嵌Toast，用于在选择器对话框内显示提示 */
    public DToastDialog dToastDialog = new DToastDialog(); // // 选择器内嵌Toast

    /** 当前路径选择Fragment引用 */
    private PathSelectFragment pathSelectFragment; // // 路径选择Fragment

    /** 上下文引用 */
    public  Context context; // // 上下文

    /**
     * 带虚拟键盘的构造函数。
     * 创建文件选择器并初始化悬浮虚拟键盘窗口，
     * 同时订阅RxBus的隐藏键盘和隐藏选择器事件。
     *
     * @param context           上下文
     * @param pathSelectHandler 选择结果回调处理器
     */
    public  FileSelector (Context context,PathSelectHandler pathSelectHandler){ // // 带虚拟键盘的构造函数
        this.pathSelectHandler = pathSelectHandler; // // 保存回调处理器
        this.context = context; // // 保存上下文

        windowManager = (WindowManager) context // // 获取窗口管理器
                .getSystemService(Context.WINDOW_SERVICE); // // 系统窗口服务
        WindowManager.LayoutParams params; // // 窗口布局参数
        int layout_paras_type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY; // // 悬浮窗口类型

        params = new WindowManager.LayoutParams( // // 创建布局参数
                WindowManager.LayoutParams.MATCH_PARENT, // // 宽度匹配父容器
                WindowManager.LayoutParams.WRAP_CONTENT, // // 高度包裹内容
                layout_paras_type, // // 悬浮窗口类型
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, // // 允许布局在屏幕边界内
                PixelFormat.TRANSLUCENT // // 半透明像素格式
        );
        fileSelectorTextKeyBoard = new TopDialogTextKeyBoard(context); // // 创建虚拟键盘
        fileSelectorTextKeyBoard.showTitle(); // // 显示键盘标题
        windowManager.addView(fileSelectorTextKeyBoard, params); // // 将键盘添加为悬浮窗口

        RxBus.getInstance().getObservable(RxEnum.MSG_HIDE_KEYBOARD).subscribe(consumerHideKeyboard);; // // 订阅隐藏键盘事件
        RxBus.getInstance().getObservable(RxEnum.MSG_HIDE_FILESELECTOR).subscribe(consumerHideFileSelector);; // // 订阅隐藏选择器事件
    }

    /**
     * 不带虚拟键盘的构造函数。
     * 仅订阅RxBus事件，不创建悬浮键盘窗口。
     *
     * @param pathSelectHandler 选择结果回调处理器
     */
    public  FileSelector (PathSelectHandler pathSelectHandler){ // // 不带虚拟键盘的构造函数
        RxBus.getInstance().getObservable(RxEnum.MSG_HIDE_KEYBOARD).subscribe(consumerHideKeyboard);; // // 订阅隐藏键盘事件
        RxBus.getInstance().getObservable(RxEnum.MSG_HIDE_FILESELECTOR).subscribe(consumerHideFileSelector);; // // 订阅隐藏选择器事件
        this.pathSelectHandler = pathSelectHandler; // // 保存回调处理器
    }

    /**
     * 构建保存文件选择器（选择文件夹）。
     * 用于保存波形/设置/图片等场景，用户选择目标文件夹。
     * 支持新建文件夹、删除、确认选择当前路径等操作。
     *
     * @param rootPath    根路径，选择器的起始浏览路径
     * @param disPlayPath 显示路径，标题栏显示的路径段
     * @param fragment    宿主Fragment，可为null
     * @param context     上下文
     * @return PathSelectFragment实例
     */
    public PathSelectFragment buildSaveFileSelector(String rootPath, String disPlayPath, Fragment fragment, Context context) { // // 构建保存文件选择器
        ConfigDataBuilderImpl builder; // // 配置构建器
        this.context = context; // // 保存上下文
        if (fragment == null) { // // 无宿主Fragment
            builder = (ConfigDataBuilderImpl) PathSelector.build(context, MConstants.BUILD_DIALOG); // // 使用Context构建Dialog模式
        } else { // // 有宿主Fragment
            builder = (ConfigDataBuilderImpl) PathSelector.build(fragment, MConstants.BUILD_DIALOG); // // 使用Fragment构建Dialog模式
        }
        String mainTitle = getDisplayPathSegment(disPlayPath); // // 获取显示路径的第一段作为标题
        builder.setTitlebarBG(Color.BLACK) // // 标题栏背景色：黑色
                .setTitlebarMainTitle(new FontBean((mainTitle), 16, context.getColor(R.color.folder_text_color))) // // 标题文字
                .setShowSelectStorageBtn(true) // // 显示存储设备切换按钮
                .setMaxCount(1) // // 最大选择数量：1
                .setShowTabbarFragment(true) // // 显示标签栏
                .setShowFileTypes("") // // 显示所有文件类型
                .setRootPath(rootPath) // // 设置根路径
                .setAddFolderListener(new CommonItemListener(new FontBean("", 14)) { // // 新建文件夹按钮监听
                    @Override
                    public boolean onClick(View v, View tv, List<FileBean> selectedFiles, String currentPath, BasePathSelectFragment pathSelectFragment) { // // 点击新建文件夹
                        handleAddClick(currentPath, pathSelectFragment,context); // // 处理新建文件夹逻辑
                        return false; // // 返回false表示不关闭选择器
                    }
                })
                .setCurrentPathConfirmListeners(new CommonItemListener(new FontBean("", 14, Color.WHITE)) { // // 确认当前路径监听（点击路径栏确认）
                    @Override
                    public boolean onClick(View v, View tv, List<FileBean> selectedFiles, String currentPath, BasePathSelectFragment pathSelectFragment) { // // 确认当前路径
                        if(selectedFiles.isEmpty()){ // // 未选择任何文件
                            FileBean fileBean = new FileBean(currentPath); // // 用当前路径创建FileBean
                            DisplayNameTools displayNameTools =new DisplayNameTools(context); // // 显示名称工具
                            displayNameTools.convertSinglePathToDisplayName(fileBean); // // 转换为显示名称
                            pathSelectHandler.addPathToSet(fileBean); // // 回调当前路径

                        }else { // // 已选择文件
                            pathSelectHandler.addPathToSet(selectedFiles.get(0)); // // 回调选中的第一个文件
                        }
                        SelectConfigData mConfigData = ConfigDataBuilderImpl.getInstance().getSelectConfigData(); // // 获取配置数据
                        mConfigData.buildController.getDialogFragment().dismissAllowingStateLoss(); // // 关闭对话框
                        return true; // // 返回true表示已处理
                    }
                })
                .setHandleItemListeners( // // 底部操作按钮监听
                        new CommonItemListener(new FontBean((context.getResources().getString(R.string.dialog_cancel)), 14, context.getColor(R.color.folder_text_color), R.drawable.x_close)) { // // 取消按钮
                            @Override
                            public boolean onClick(View v, View tv, List<FileBean> selectedFiles, String currentPath, BasePathSelectFragment pathSelectFragment) { // // 点击取消
                                pathSelectFragment.openCloseMultipleMode(false); // // 关闭多选模式
                                return false; // // 返回false
                            }
                        },
                        new CommonItemListener(new FontBean((context.getResources().getString(R.string.mathRefSerialsSlip_delete)), 14, context.getColor(R.color.folder_text_color), R.drawable.icon_delete)) { // // 删除按钮
                            @Override
                            public boolean onClick(View v, View tv, List<FileBean> selectedFiles, String currentPath, BasePathSelectFragment pathSelectFragment) { // // 点击删除
                                DeleteConfirmDialog deleteConfirmDialog = new DeleteConfirmDialog(context,selectedFiles.get(0), (DeleteConfirmDialog.OnDeleteListener) pathSelectFragment.getFileShowFragment()); // // 创建删除确认对话框
                                deleteConfirmDialog.show(); // // 显示删除确认
                                pathSelectFragment.openCloseMultipleMode(false); // // 关闭多选模式
                                return false; // // 返回false
                            }
                        },
                        new CommonItemListener(new FontBean((context.getResources().getString(R.string.dialog_ok)), 14, context.getColor(R.color.folder_text_color), R.drawable.confirm)) { // // 确定按钮
                            @Override
                            public boolean onClick(View v, View tv, List<FileBean> selectedFiles, String currentPath, BasePathSelectFragment pathSelectFragment) { // // 点击确定
                                StringBuilder builder = new StringBuilder(); // // 构建路径字符串
                                for (FileBean fileBean : selectedFiles) { // // 遍历选中的文件
                                    if (fileBean.isDir()) { // // 是文件夹
                                        builder.append(fileBean.getPath()); // // 拼接路径
                                    } else { // // 是文件（保存模式不允许选择文件）
                                        dToastDialog.show(pathSelectFragment.getDialog(), context.getResources().getString(R.string.file_selection_not_supported)); // // 提示不支持文件选择
                                        return false; // // 返回false，不关闭
                                    }
                                }
                                DToast.get().showBottom(context.getResources().getString(R.string.seleced_folder) + builder); // // 底部Toast显示选中的文件夹
                                pathSelectFragment.openCloseMultipleMode(false); // // 关闭多选模式
                                SelectConfigData mConfigData = ConfigDataBuilderImpl.getInstance().getSelectConfigData(); // // 获取配置数据
                                mConfigData.buildController.getDialogFragment().dismissAllowingStateLoss(); // // 关闭对话框

                                pathSelectHandler.addPathToSet(selectedFiles.get(0)); // // 回调选中的文件夹
                                return true; // // 返回true
                            }
                        }
                ).show(); // // 显示选择器


        return pathSelectFragment; // // 返回Fragment引用
    }


    /**
     * 构建参考波形文件选择器。
     * 与buildSaveFileSelector类似，用于选择参考波形文件的存储位置。
     * 区别：无新建文件夹按钮。
     *
     * @param rootPath    根路径
     * @param disPlayPath 显示路径
     * @param fragment    宿主Fragment，可为null
     * @param context     上下文
     * @return PathSelectFragment实例
     */
    public PathSelectFragment buildSaveFileRefSelector(String rootPath, String disPlayPath, Fragment fragment, Context context) { // // 构建参考波形文件选择器
        ConfigDataBuilderImpl builder; // // 配置构建器
        this.context = context; // // 保存上下文
        if (fragment == null) { // // 无宿主Fragment
            builder = (ConfigDataBuilderImpl) PathSelector.build(context, MConstants.BUILD_DIALOG); // // 使用Context构建
        } else { // // 有宿主Fragment
            builder = (ConfigDataBuilderImpl) PathSelector.build(fragment, MConstants.BUILD_DIALOG); // // 使用Fragment构建
        }
        String mainTitle = getDisplayPathSegment(disPlayPath); // // 获取显示路径段
        return builder.setTitlebarBG(Color.BLACK) // // 标题栏背景色
                .setTitlebarMainTitle(new FontBean((mainTitle), 16, context.getColor(R.color.folder_text_color))) // // 标题文字
                .setShowSelectStorageBtn(true) // // 显示存储切换按钮
                .setMaxCount(1) // // 最大选择1个
                .setShowTabbarFragment(true) // // 显示标签栏
                .setShowFileTypes("") // // 显示所有类型
                .setRootPath(rootPath) // // 根路径
                .setCurrentPathConfirmListeners(new CommonItemListener(new FontBean("", 14, Color.WHITE)) { // // 确认当前路径
                    @Override
                    public boolean onClick(View v, View tv, List<FileBean> selectedFiles, String currentPath, BasePathSelectFragment pathSelectFragment) { // // 点击确认
                        if(selectedFiles.isEmpty()){ // // 未选择文件
                            FileBean fileBean = new FileBean(currentPath); // // 用当前路径创建
                            DisplayNameTools displayNameTools =new DisplayNameTools(context); // // 显示名称工具
                            displayNameTools.convertSinglePathToDisplayName(fileBean); // // 转换显示名称
                            pathSelectHandler.addPathToSet(fileBean); // // 回调
                        }else { // // 已选择
                            pathSelectHandler.addPathToSet(selectedFiles.get(0)); // // 回调选中项
                        }
                        SelectConfigData mConfigData = ConfigDataBuilderImpl.getInstance().getSelectConfigData(); // // 获取配置
                        mConfigData.buildController.getDialogFragment().dismissAllowingStateLoss(); // // 关闭对话框
                        return true; // // 已处理
                    }
                })
                .setHandleItemListeners( // // 底部操作按钮
                        new CommonItemListener(new FontBean((context.getResources().getString(R.string.dialog_cancel)), 14, context.getColor(R.color.folder_text_color), R.drawable.x_close)) { // // 取消
                            @Override
                            public boolean onClick(View v, View tv, List<FileBean> selectedFiles, String currentPath, BasePathSelectFragment pathSelectFragment) { // // 点击取消
                                pathSelectFragment.openCloseMultipleMode(false); // // 关闭多选
                                return false; // // 返回false
                            }
                        },
                        new CommonItemListener(new FontBean((context.getResources().getString(R.string.mathRefSerialsSlip_delete)), 14, context.getColor(R.color.folder_text_color), R.drawable.icon_delete)) { // // 删除
                            @Override
                            public boolean onClick(View v, View tv, List<FileBean> selectedFiles, String currentPath, BasePathSelectFragment pathSelectFragment) { // // 点击删除
                                DeleteConfirmDialog deleteConfirmDialog = new DeleteConfirmDialog(context,selectedFiles.get(0), (DeleteConfirmDialog.OnDeleteListener) pathSelectFragment.getFileShowFragment()); // // 删除确认
                                deleteConfirmDialog.show(); // // 显示
                                pathSelectFragment.openCloseMultipleMode(false); // // 关闭多选
                                return false; // // 返回false
                            }
                        },
                        new CommonItemListener(new FontBean((context.getResources().getString(R.string.dialog_ok)), 14, context.getColor(R.color.folder_text_color), R.drawable.confirm)) { // // 确定
                            @Override
                            public boolean onClick(View v, View tv, List<FileBean> selectedFiles, String currentPath, BasePathSelectFragment pathSelectFragment) { // // 点击确定
                                StringBuilder builder = new StringBuilder(); // // 路径构建器
                                for (FileBean fileBean : selectedFiles) { // // 遍历选中项
                                    if (fileBean.isDir()) { // // 是文件夹
                                        builder.append(fileBean.getPath()); // // 拼接路径
                                    } else { // // 是文件
                                        dToastDialog.show(pathSelectFragment.getDialog(), context.getResources().getString(R.string.file_selection_not_supported)); // // 提示不支持
                                        return false; // // 返回false
                                    }
                                }
                                DToast.get().showBottom(context.getResources().getString(R.string.seleced_folder) + builder); // // Toast提示
                                pathSelectFragment.openCloseMultipleMode(false); // // 关闭多选
                                SelectConfigData mConfigData = ConfigDataBuilderImpl.getInstance().getSelectConfigData(); // // 获取配置
                                mConfigData.buildController.getDialogFragment().dismissAllowingStateLoss(); // // 关闭对话框
                                pathSelectHandler.addPathToSet(selectedFiles.get(0)); // // 回调选中项
                                return true; // // 已处理
                            }
                        }
                )
                .show(); // // 显示选择器

    }

    /**
     * 构建调用文件选择器（选择文件）。
     * 用于加载/调用波形文件、设置文件等场景，用户选择具体文件。
     * 支持文件类型过滤和仅显示文件模式。
     *
     * @param rootPath       根路径
     * @param disPlayPath    显示路径
     * @param fragment       宿主Fragment
     * @param context        上下文
     * @param isFilesShowOnly 是否仅显示文件（不显示文件夹）
     * @param waveFileType   文件类型过滤数组，如 new String[]{"wav","csv"}
     */
    public void buildInvokeFileSelector(String rootPath, String disPlayPath, Fragment fragment, Context context, boolean isFilesShowOnly, String[] waveFileType) { // // 构建调用文件选择器
        this.context = context; // // 保存上下文
        String mainTitle = getDisplayPathSegment(disPlayPath); // // 获取显示路径段
        PathSelector.build(fragment, MConstants.BUILD_DIALOG) // // 构建Dialog模式选择器
                .setTitlebarBG(Color.BLACK) // // 标题栏背景色
                .setTitlebarMainTitle(new FontBean(mainTitle, 16, context.getColor(R.color.folder_text_color))) // // 标题文字
                .setShowSelectStorageBtn(true) // // 显示存储切换按钮
                .setShowTabbarFragment(true) // // 显示标签栏
                .setRootPath(rootPath) // // 根路径
                .setMaxCount(1) // // 最大选择1个
                //设置是否只显示文件
                .setIsFilesOnlyMode(isFilesShowOnly) // // 是否仅显示文件模式
                //设置过滤类型
                .setShowFileTypes(waveFileType) // // 文件类型过滤
                .setCurrentPathConfirmListeners(new CommonItemListener(new FontBean("", 16, Color.WHITE)) { // // 确认当前路径
                    @Override
                    public boolean onClick(View v, View tv, List<FileBean> selectedFiles, String currentPath, BasePathSelectFragment pathSelectFragment) { // // 点击确认
                        StringBuilder builder = new StringBuilder(); // // 路径构建器
                        if (selectedFiles.size() == 0) { // // 未选择文件
                            dToastDialog.show(pathSelectFragment.getDialog(), context.getResources().getString(R.string.folder_selection_not_supported)); // // 提示不支持文件夹选择
                            return false; // // 返回false
                        }
                        for (FileBean fileBean : selectedFiles) { // // 遍历选中项
                            if (fileBean.isDir()) { // // 是文件夹
                                dToastDialog.show(pathSelectFragment.getDialog(), context.getResources().getString(R.string.folder_selection_not_supported)); // // 提示不支持
                                return false; // // 返回false
                            } else { // // 是文件
                                builder.append(fileBean.getPath()); // // 拼接路径
                            }
                        }
                        pathSelectHandler.addPathToSet(selectedFiles.get(0)); // // 回调选中的文件
                        SelectConfigData mConfigData = ConfigDataBuilderImpl.getInstance().getSelectConfigData(); // // 获取配置
                        mConfigData.buildController.getDialogFragment().dismissAllowingStateLoss(); // // 关闭对话框
                        return true; // // 已处理
                    }
                })
                .setHandleItemListeners( // // 底部操作按钮
                        new CommonItemListener(new FontBean((context.getResources().getString(R.string.dialog_cancel)), 14, context.getColor(R.color.folder_text_color), R.drawable.cancle)) { // // 取消
                            @Override
                            public boolean onClick(View v, View tv, List<FileBean> selectedFiles, String currentPath, BasePathSelectFragment pathSelectFragment) { // // 点击取消
                                pathSelectFragment.openCloseMultipleMode(false); // // 关闭多选
                                return false; // // 返回false
                            }
                        },
                        new CommonItemListener(new FontBean((context.getResources().getString(R.string.mathRefSerialsSlip_delete)), 14, context.getColor(R.color.folder_text_color), R.drawable.icon_delete)) { // // 删除
                            @Override
                            public boolean onClick(View v, View tv, List<FileBean> selectedFiles, String currentPath, BasePathSelectFragment pathSelectFragment) { // // 点击删除
                                DeleteConfirmDialog deleteConfirmDialog = new DeleteConfirmDialog(context,selectedFiles.get(0), (DeleteConfirmDialog.OnDeleteListener) pathSelectFragment.getFileShowFragment()); // // 删除确认
                                deleteConfirmDialog.show(); // // 显示
                                pathSelectFragment.openCloseMultipleMode(false); // // 关闭多选
                                return false; // // 返回false
                            }
                        },
                        new CommonItemListener(new FontBean((context.getResources().getString(R.string.dialog_ok)), 14, context.getColor(R.color.folder_text_color), R.drawable.confirm)) { // // 确定
                            @Override
                            public boolean onClick(View v, View tv, List<FileBean> selectedFiles, String currentPath, BasePathSelectFragment pathSelectFragment) { // // 点击确定
                                StringBuilder builder = new StringBuilder(); // // 路径构建器
                                for (FileBean fileBean : selectedFiles) { // // 遍历选中项
                                    if (fileBean.isDir()) { // // 是文件夹
                                        dToastDialog.show(pathSelectFragment.getDialog(), context.getResources().getString(R.string.folder_selection_not_supported)); // // 提示不支持
                                        return false; // // 返回false
                                    } else { // // 是文件
                                        builder.append(fileBean.getPath()); // // 拼接路径
                                    }
                                }
                                pathSelectFragment.openCloseMultipleMode(false); // // 关闭多选
                                SelectConfigData mConfigData = ConfigDataBuilderImpl.getInstance().getSelectConfigData(); // // 获取配置
                                mConfigData.buildController.getDialogFragment().dismissAllowingStateLoss(); // // 关闭对话框
                                pathSelectHandler.addPathToSet(selectedFiles.get(0)); // // 回调选中文件
                                return true; // // 已处理
                            }
                        }

                )
                .show(); // // 显示选择器
    }



    /**
     * 处理新建文件夹点击事件。
     * 弹出虚拟键盘让用户输入文件夹名称，
     * 输入完成后在当前路径下创建新文件夹。
     *
     * @param currentPath        当前浏览路径
     * @param pathSelectFragment 路径选择Fragment，用于刷新文件列表
     * @param context            上下文
     */
    public void handleAddClick(String currentPath, BasePathSelectFragment pathSelectFragment, Context context) { // // 处理新建文件夹
        fileSelectorTextKeyBoard.setData(context.getString(R.string.new_folder), TopDialogTextKeyBoard.INPUT_TYPE_ALL_BUT_SYMBOL, 64, new TopDialogTextKeyBoard.OnDialogDismissListener() { // // 设置键盘数据（标题、输入类型、最大长度、回调）
            @Override
            public void onDismiss(String result) { // // 键盘关闭回调
                File newFolder = new File(currentPath, result); // // 创建新文件夹File对象
                if (!newFolder.exists()) { // // 文件夹不存在
                    boolean isSuccess = newFolder.mkdirs(); // // 创建文件夹
                    if (isSuccess) { // // 创建成功
                        dToastDialog.show(pathSelectFragment.getDialog(),context.getResources().getString(R.string.label_folder) + newFolder.getName() + " " + context.getResources().getString(R.string.toast_create_success)); // // 提示创建成功
                        pathSelectFragment.updateFileList(); // // 刷新文件列表
                    } else { // // 创建失败
                        dToastDialog.show(pathSelectFragment.getDialog(),context.getResources().getString(R.string.label_folder) + newFolder.getAbsolutePath() + " " + context.getResources().getString(R.string.toast_create_failure)); // // 提示创建失败
                    }
                } else { // // 文件夹已存在
                    dToastDialog.show(pathSelectFragment.getDialog(),context.getResources().getString(R.string.label_folder) + newFolder.getName() + " " + context.getResources().getString(R.string.error_already_exists)); // // 提示已存在
                }
            }
        });
    }



    /**
     * RxBus事件消费者：隐藏文件选择器。
     * 收到MSG_HIDE_FILESELECTOR事件时关闭文件选择器对话框。
     */
    public Consumer<Boolean> consumerHideFileSelector = new Consumer<Boolean>() { // // 隐藏文件选择器消费者
        @Override
        public void accept(Boolean aBoolean) throws Exception { // // 接收事件
            SelectConfigData mConfigData = ConfigDataBuilderImpl.getInstance().getSelectConfigData(); // // 获取配置数据
            if(mConfigData.buildController!=null){ // // 控制器存在
                mConfigData.buildController.getDialogFragment().dismissAllowingStateLoss(); // // 关闭对话框

            }
        }
    };

    /**
     * RxBus事件消费者：隐藏虚拟键盘。
     * 收到MSG_HIDE_KEYBOARD事件时隐藏虚拟键盘。
     */
    private Consumer<Boolean> consumerHideKeyboard = new Consumer<Boolean>() { // // 隐藏键盘消费者
        @Override
        public void accept(Boolean aBoolean) throws Exception { // // 接收事件
            fileSelectorTextKeyBoard.hide(); // // 隐藏虚拟键盘
        }

    };

    /**
     * 从完整路径中提取第一段路径作为显示标题。
     * 例如："/Internal shared storage/Waveforms" → "Internal shared storage"
     * 特殊处理"Internal shared storage"，替换为本地化字符串。
     *
     * @param path 完整路径
     * @return 路径的第一段，用于标题显示
     */
    public  String getDisplayPathSegment(String path){ // // 获取显示路径段
        if(path==null) // // 路径为空
            return ""; // // 返回空字符串
        String trimmed = path.replaceAll("^/+","").replaceAll("/+$",""); // // 去除首尾斜杠
        int slashIndex = trimmed.indexOf('/'); // // 查找第一个斜杠位置
        trimmed = slashIndex == -1 ? trimmed: trimmed.substring(0,slashIndex); // // 取第一段路径
        if(trimmed.equals("Internal shared storage")){ // // 是内部共享存储
            if(context!=null){ // // 上下文不为空
                trimmed = context.getString(R.string.internal_storage); // // 替换为本地化字符串
            }
        }
        return trimmed; // // 返回路径段
    }

    /**
     * 文件选择结果回调接口。
     * 调用方实现此接口以接收用户选择的文件/文件夹。
     */
    public interface PathSelectHandler{ // // 文件选择回调接口
        /**
         * 将选中的文件/文件夹添加到调用方的集合中。
         * @param selectedBean 选中的文件Bean
         */
        void addPathToSet(FileBean selectedBean); // // 回调选中项
    }

    /**
     * 检测指定路径所在的存储卷是否为FAT32文件系统。
     * 通过反射获取存储卷列表，匹配路径前缀，
     * 再通过反射获取文件系统类型。
     *
     * @param rootPath 要检测的路径
     * @return true表示该路径位于FAT32文件系统上
     */
    public boolean isFAT32(String rootPath){ // // 检测是否FAT32文件系统
        List<StorageVolume> storageVolumes = ReflectTools.getStorageVolumes(context); // // 反射获取存储卷列表
        for(StorageVolume volueme:storageVolumes){ // // 遍历存储卷
            if(volueme.getState().equals("mounted")){ // // 存储卷已挂载
                String preFix = volueme.getDirectory().toString(); // // 获取挂载路径
                if(rootPath.startsWith(preFix)){ // // 目标路径属于此存储卷
                    String type = ReflectTools.getFileSystemType(context,volueme); // // 反射获取文件系统类型
                    if(type.equals("FAT32")){ // // 是FAT32
                        Log.d("FAT#2", volueme.getDescription(context)+"isFAT32:") ; // // 日志：是FAT32
                        return true; // // 返回true
                    }else{ // // 不是FAT32
                        Log.d("FAT#2", volueme.getDescription(context)+"not FAT32:" + type) ; // // 日志：不是FAT32

                        return false; // // 返回false
                    }
                }
            }

        }

        return false; // // 未匹配到存储卷，返回false
    }
}
