package com.micsig.tbook.ui.util;

import com.molihuan.pathselector.entity.FileBean;

import java.util.ArrayList;

/**
 * 文件Bean转字符串工具类
 * 
 * <p>提供FileBean对象列表与字符串列表之间的转换功能。</p>
 * 
 * <p>主要功能：</p>
 * <ul>
 *   <li>提取显示名称：从FileBean列表中提取文件显示名称列表</li>
 *   <li>提取绝对路径：从FileBean列表中提取文件绝对路径列表</li>
 * </ul>
 * 
 * <p>该类主要用于文件选择器组件，将FileBean对象转换为可用于UI显示的字符串列表。</p>
 * 
 * @author Micsig Technology
 * @version 1.0
 * @since 2024
 * @see FileBean 文件实体类（来自pathselector库）
 */
public class FileBeanToStr {

    /**
     * 获取显示名称字符串列表
     * 
     * <p>从FileBean列表中提取每个文件的显示名称，生成字符串列表。</p>
     * <p>显示名称通常是文件名（不包含路径），适合在UI中展示。</p>
     *
     * @param fileBeans FileBean对象列表，如果为null或空列表则返回空列表
     * @return 包含所有文件显示名称的字符串列表
     * 
     * @see FileBean#getDisplayName() 获取文件显示名称
     */
    public static ArrayList<String> getDisPlayStrList(ArrayList<FileBean> fileBeans) {
        // 创建结果列表
        ArrayList<String> list = new ArrayList<>();
        
        // 遍历FileBean列表，提取显示名称
        for (FileBean fileBean : fileBeans) {
            // 将每个文件的显示名称添加到结果列表
            list.add(fileBean.getDisplayName());
        }
        
        return list;
    }

    /**
     * 获取绝对路径字符串列表
     * 
     * <p>从FileBean列表中提取每个文件的绝对路径，生成字符串列表。</p>
     * <p>绝对路径包含完整的目录路径和文件名，可用于文件操作。</p>
     *
     * @param fileBeans FileBean对象列表，如果为null或空列表则返回空列表
     * @return 包含所有文件绝对路径的字符串列表
     * 
     * @see FileBean#getPath() 获取文件绝对路径
     */
    public static ArrayList<String> getAbsoluteStrList(ArrayList<FileBean> fileBeans) {
        // 创建结果列表
        ArrayList<String> list = new ArrayList<>();
        
        // 遍历FileBean列表，提取绝对路径
        for (FileBean fileBean : fileBeans) {
            // 将每个文件的绝对路径添加到结果列表
            list.add(fileBean.getPath());
        }
        
        return list;
    }

}
