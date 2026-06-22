package com.micsig.tbook.ui.util.svg;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;

import androidx.core.content.res.ColorStateListInflaterCompat;

import com.micsig.tbook.ui.wavezone.TChan;

/**
 * 选择器工具类
 * 
 * <p>用于创建各种状态选择器Drawable，包括单选按钮、多选按钮和颜色状态列表。
 * 该类是示波器UI组件状态管理的核心工具类。</p>
 * 
 * <p>主要功能：</p>
 * <ul>
 *   <li>创建通道单选按钮Drawable</li>
 *   <li>创建通道多选按钮Drawable</li>
 *   <li>创建颜色状态列表</li>
 * </ul>
 * 
 * <p>支持的状态：</p>
 * <ul>
 *   <li>STATE_PRESSED - 按下状态</li>
 *   <li>STATE_FOCUSED - 获得焦点状态</li>
 *   <li>STATE_ENABLED - 启用状态</li>
 *   <li>STATE_CHECKED - 选中状态</li>
 * </ul>
 * 
 * @since 1.0
 * @see SvgManager
 * @see SvgNodeInfo
 */
public class SelectorUtil {

    /** 按下状态属性 */
    public static int STATE_PRESSED = android.R.attr.state_pressed;
    
    /** 获得焦点状态属性 */
    public static int STATE_FOCUSED = android.R.attr.state_focused;
    
    /** 启用状态属性 */
    public static int STATE_ENABLED = android.R.attr.state_enabled;
    
    /** 选中状态属性 */
    public static int STATE_CHECKED = android.R.attr.state_checked;
    
    /** 顶部通道按钮宽度 */
    private static int WIDTH_TOP_CHANNEL = 20;
    
    /** 顶部通道按钮高度 */
    private static int HEIGHT_TOP_CHANNEL = 20;

    /**
     * 创建通道单选按钮Drawable
     * 
     * <p>根据通道索引创建单选按钮的状态选择器Drawable。
     * 选中时显示带颜色的圆形填充，未选中时显示空心圆。</p>
     * 
     * <p>状态对应：</p>
     * <ul>
     *   <li>选中状态(STATE_CHECKED)：显示带颜色的圆形填充</li>
     *   <li>未选中状态(-STATE_CHECKED)：显示空心圆</li>
     * </ul>
     * 
     * @param chIndex 通道索引，用于确定颜色
     * @return 状态选择器Drawable
     */
    //通道单选 按钮
    public static Drawable createCheckedDrawable(int chIndex) {
        // 创建状态选择器
        StateListDrawable drawable = new StateListDrawable();
        
        // 创建选中状态的位图
        Bitmap checked = SvgManager.createSvg(SvgNodeInfo.getSCPaths(), SvgNodeInfo.getSCColors(chIndex), WIDTH_TOP_CHANNEL, HEIGHT_TOP_CHANNEL);
        
        // 创建未选中状态的位图
        Bitmap unchecked = SvgManager.createSvg(SvgNodeInfo.getSCUncheckPaths(), SvgNodeInfo.getSCUncheckColors(), WIDTH_TOP_CHANNEL, HEIGHT_TOP_CHANNEL);
        
        // 添加选中状态
        drawable.addState(new int[]{STATE_CHECKED}, new BitmapDrawable(Resources.getSystem(), checked));
        
        // 添加未选中状态（负号表示状态取反）
        drawable.addState(new int[]{-STATE_CHECKED}, new BitmapDrawable(Resources.getSystem(), unchecked));
        
        return drawable;
    }

    /**
     * 创建通道多选按钮Drawable
     * 
     * <p>根据通道索引创建多选按钮的状态选择器Drawable。
     * 选中时显示带颜色的对勾，未选中时显示空心方框。</p>
     * 
     * <p>状态对应：</p>
     * <ul>
     *   <li>选中状态(STATE_CHECKED)：显示带颜色的对勾</li>
     *   <li>未选中状态(-STATE_CHECKED)：显示空心方框</li>
     * </ul>
     * 
     * @param chIndex 通道索引，用于确定颜色
     * @return 状态选择器Drawable
     */
    //通道多选 按钮
    public static Drawable createMuChoiceDrawable(int chIndex) {
        // 创建状态选择器
        StateListDrawable drawable = new StateListDrawable();
        
        // 创建选中状态的位图（显示对勾）
        Bitmap checked = SvgManager.createDuiHaoSvg(chIndex, WIDTH_TOP_CHANNEL, HEIGHT_TOP_CHANNEL, false);
        
        // 创建未选中状态的位图（不显示对勾）
        Bitmap unchecked = SvgManager.createDuiHaoSvg(chIndex, WIDTH_TOP_CHANNEL, HEIGHT_TOP_CHANNEL, true);
        
        // 添加选中状态
        drawable.addState(new int[]{STATE_CHECKED}, new BitmapDrawable(Resources.getSystem(), checked));
        
        // 添加未选中状态
        drawable.addState(new int[]{-STATE_CHECKED}, new BitmapDrawable(Resources.getSystem(), unchecked));
        
        return drawable;
    }

    /**
     * RadioButton状态列表
     * 
     * <p>定义RadioButton的四种状态组合：</p>
     * <ol>
     *   <li>禁用状态</li>
     *   <li>按下状态</li>
     *   <li>选中状态</li>
     *   <li>默认状态（空数组）</li>
     * </ol>
     */
    private static final int[][] RadioButtonStateList = new int[][]{
            new int[]{-SelectorUtil.STATE_ENABLED},  // 禁用状态
            new int[]{SelectorUtil.STATE_PRESSED},   // 按下状态
            new int[]{SelectorUtil.STATE_CHECKED},   // 选中状态
            new int[]{}                              // 默认状态
    };

    /**
     * 创建颜色状态列表
     * 
     * <p>根据通道索引创建RadioButton的颜色状态列表。
     * 不同状态显示不同的颜色。</p>
     * 
     * <p>状态颜色对应：</p>
     * <ol>
     *   <li>禁用状态：灰色（禁用色）</li>
     *   <li>按下状态：通道颜色</li>
     *   <li>选中状态：通道颜色</li>
     *   <li>默认状态：通用颜色</li>
     * </ol>
     * 
     * @param chIndex 通道索引，用于确定颜色
     * @return 颜色状态列表
     */
    public static ColorStateList createColorStateList(int chIndex) {
        // 定义四种状态对应的颜色
        int[] colors = new int[]{
                Color.parseColor(SvgNodeInfo.getColorViewDisable()),  // 禁用状态颜色
                SvgNodeInfo.getAllBaseColorInt(chIndex),              // 按下状态颜色（通道颜色）
                SvgNodeInfo.getAllBaseColorInt(chIndex),              // 选中状态颜色（通道颜色）
                SvgNodeInfo.getAllBaseColorInt(TChan.NULL),           // 默认状态颜色（通用颜色）
        };
        
        return new ColorStateList(RadioButtonStateList, colors);
    }


}
