package com.micsig.tbook.ui.util.svg;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;

import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;
import com.micsig.base.Logger;

import java.util.List;

/**
 * SVG管理器类
 * 
 * <p>提供SVG动态生成和渲染功能，用于在运行时创建各种图标和指示器。
 * 该类通过拼接SVG字符串并使用AndroidSVG库解析渲染为Bitmap图像。</p>
 * 
 * <p>主要功能：</p>
 * <ul>
 *     <li>根据路径数据和颜色动态生成SVG图标</li>
 *     <li>支持缩放渲染SVG图像</li>
 *     <li>生成通道选择用的对号图标</li>
 *     <li>生成光标指示器图标</li>
 * </ul>
 * 
 * <p>使用示例：</p>
 * <pre>{@code
 * List<String> paths = Arrays.asList("M0,0 L10,10", "M10,0 L0,10");
 * List<String> colors = Arrays.asList("#FF0000", "#00FF00");
 * Bitmap bitmap = SvgManager.createSvg(paths, colors, 24, 24);
 * }</pre>
 * 
 * @author micsig
 * @version 1.0
 * @since 2023
 * @see SvgNodeInfo SVG节点信息类，提供路径数据和颜色常量
 * @see SVG AndroidSVG库的核心类
 */
public class SvgManager {

    // ==================== 常量定义 ====================

    /**
     * 日志标签
     * 用于标识SVG创建管理器的日志输出
     */
    private static final String TAG = "SvgCreateManager";

    // ==================== 公共方法 ====================

    /**
     * 创建SVG图标
     * 
     * <p>根据提供的路径数据数组和颜色数组动态生成SVG图像。
     * 每个路径数据对应一个颜色，路径和颜色数量必须一致。</p>
     * 
     * <p>处理流程：</p>
     * <ol>
     *     <li>验证路径数据和颜色数组长度是否一致</li>
     *     <li>创建SVG文件头</li>
     *     <li>遍历添加所有路径元素</li>
     *     <li>闭合SVG标签并解析渲染</li>
     * </ol>
     * 
     * @param pathDataArray SVG路径数据列表，每个元素是一个有效的SVG路径字符串
     *                      如："M0,0 L10,10 L10,0 Z"
     * @param colors 填充颜色列表，每个元素是有效的颜色字符串
     *               如："#FF0000" 或 "#FF0000FF"（含透明度）
     * @param width 目标图像宽度（像素）
     * @param height 目标图像高度（像素）
     * @return 渲染后的Bitmap图像，解析失败时返回null
     * @throws IllegalArgumentException 当路径数据和颜色数组长度不一致时抛出
     */
    public static Bitmap createSvg(List<String> pathDataArray, List<String> colors, int width, int height) {
        // 验证路径数据和颜色数组长度是否一致
        try {
            if (pathDataArray.size() != colors.size()) {
                throw new IllegalArgumentException("Path data and colors arrays must have same length");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 构建SVG字符串
        StringBuilder svgInfo = new StringBuilder();
        // 添加SVG文件头
        svgInfo.append(createHeader(width, height));

        // 遍历添加所有路径元素
        if (pathDataArray.size() > 0 && colors.size() > 0) {
            for (int i = 0; i < pathDataArray.size(); i++) {
                svgInfo.append(createBasePath(pathDataArray.get(i), colors.get(i)));
            }
        }

        // 闭合SVG标签
        svgInfo.append("</svg>");
//        Logger.i(TAG, "svgInfo= \n" + svgInfo);

        // 解析并渲染SVG
        return parseSvgInfo(svgInfo.toString(), width, height);
    }

    /**
     * 创建缩放后的SVG图标
     * 
     * <p>先生成原始尺寸的SVG图像，然后缩放到目标尺寸。
     * 使用双线性插值进行缩放，保证图像质量。</p>
     * 
     * @param pathDataArray SVG路径数据列表
     * @param colors 填充颜色列表
     * @param width 原始图像宽度（像素）
     * @param height 原始图像高度（像素）
     * @param desWidth 目标缩放宽度（像素）
     * @param desHeight 目标缩放高度（像素）
     * @return 缩放后的Bitmap图像
     */
    public static Bitmap createScaleSvg(List<String> pathDataArray, List<String> colors, int width, int height, int desWidth, int desHeight) {
        // 先创建原始尺寸的SVG图像
        Bitmap bitmap = createSvg(pathDataArray, colors, width, height);
        // 缩放到目标尺寸，使用双线性插值
        return Bitmap.createScaledBitmap(bitmap, desWidth, desHeight, true);
    }

    /**
     * 创建对号选择图标
     * 
     * <p>生成用于通道单选/多选的对号图标。
     * 图标由外框和对号两部分组成，未选中状态只显示外框。</p>
     * 
     * <p>图标结构：</p>
     * <ul>
     *     <li>外框：圆角矩形边框，使用通道选择框颜色</li>
     *     <li>对号：选中时显示，使用通道对应的颜色</li>
     * </ul>
     * 
     * @param chIndex 通道索引，用于获取对应颜色（如TChan.Ch1）
     * @param width 图标宽度（像素）
     * @param height 图标高度（像素）
     * @param isUncheck 是否未选中状态，true=未选中（只显示外框），false=选中（显示外框+对号）
     * @return 渲染后的Bitmap图像
     */
    public static Bitmap createDuiHaoSvg(int chIndex, int width, int height, Boolean isUncheck) {
        // 构建SVG字符串
        StringBuilder svgInfo = new StringBuilder();
        // 添加SVG文件头和外框路径
        svgInfo.append(createHeader(width, height))
                .append(createDuiHaoBorderPath());

        // 选中状态时添加对号路径
        if (!isUncheck) {
            svgInfo.append(createDuiHaoPath(chIndex));
        }

        // 闭合SVG标签
        svgInfo.append("</svg>");
//        Logger.i(TAG, "svgInfo= \n" + svgInfo);

        // 解析并渲染SVG
        return parseSvgInfo(svgInfo.toString(), width, height);
    }

    /**
     * 创建光标指示器SVG图标
     * 
     * <p>生成带有菱形多边形背景的光标指示器图标。
     * 用于X/Y光标的可视化显示。</p>
     * 
     * <p>图标结构：</p>
     * <ul>
     *     <li>多边形背景：菱形形状，使用指定的填充色和描边色</li>
     *     <li>路径元素：叠加在多边形上的文字或符号</li>
     * </ul>
     * 
     * @param pathDataArray SVG路径数据列表，用于绘制文字或符号
     * @param colors 路径填充颜色列表
     * @param width 原始图像宽度（像素）
     * @param height 原始图像高度（像素）
     * @param polygonPoints 多边形顶点坐标字符串，如："1,13 13,1 25,13 25,37 13,49 1,37"
     * @param polygonColors 多边形颜色列表，索引0=填充色，索引1=描边色
     * @return 渲染后的Bitmap图像，输出尺寸为原始尺寸的80%
     */
    public static Bitmap createCursorSvg(List<String> pathDataArray, List<String> colors, int width, int height, String polygonPoints, List<String> polygonColors) {
        // 验证路径数据和颜色数组长度是否一致
        try {
            if (pathDataArray.size() != colors.size()) {
                throw new IllegalArgumentException("Path data and colors arrays must have same length");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 构建SVG字符串
        StringBuilder svgInfo = new StringBuilder();
        // 添加SVG文件头和多边形背景
        svgInfo.append(createHeader(width, height))
                .append(createPolygon(polygonPoints, polygonColors.get(0), 2, polygonColors.get(1)));

        // 遍历添加所有路径元素
        if (pathDataArray.size() > 0 && colors.size() > 0) {
            for (int i = 0; i < pathDataArray.size(); i++) {
                svgInfo.append(createBasePath(pathDataArray.get(i), colors.get(i)));
            }
        }

        // 闭合SVG标签
        svgInfo.append("</svg>");
//        Logger.i(TAG, "svgInfo= \n" + svgInfo);

        // 解析并渲染SVG，输出尺寸为原始尺寸的80%
        Bitmap bitmap = parseSvgInfo(svgInfo.toString(), (int) (width * 0.8), (int) (height * 0.8));
        return bitmap;
    }

    // ==================== 私有方法 ====================

    /**
     * 解析SVG字符串并渲染为Bitmap
     * 
     * <p>使用AndroidSVG库解析SVG字符串，并根据目标尺寸进行缩放渲染。
     * 自动计算缩放比例，保持SVG的宽高比。</p>
     * 
     * <p>处理流程：</p>
     * <ol>
     *     <li>从字符串解析SVG对象</li>
     *     <li>获取SVG的原始尺寸（优先使用viewBox）</li>
     *     <li>计算缩放比例（保持宽高比）</li>
     *     <li>创建Bitmap并渲染SVG</li>
     * </ol>
     * 
     * @param svgString SVG格式的字符串
     * @param targetWidth 目标宽度（像素）
     * @param targetHeight 目标高度（像素）
     * @return 渲染后的Bitmap图像，解析失败时返回null
     */
    private static Bitmap parseSvgInfo(String svgString, int targetWidth, int targetHeight) {
        try {
            // 从字符串解析SVG对象
            SVG svg = SVG.getFromString(svgString);

            // 获取SVG的原始尺寸
            float tempWidth, tempHeight;
            if (svg.getDocumentViewBox() == null) {
                // 没有viewBox时使用文档尺寸
                tempWidth = svg.getDocumentWidth();
                tempHeight = svg.getDocumentHeight();
                // 如果文档尺寸无效，使用目标尺寸
                if (tempWidth <= 0 || tempHeight <= 0) {
                    tempWidth = targetWidth;
                    tempHeight = targetHeight;
                }
            } else {
                // 有viewBox时使用viewBox尺寸
                tempWidth = svg.getDocumentViewBox().width();
                tempHeight = svg.getDocumentViewBox().height();
            }

            // 计算缩放比例，保持宽高比
            float scaleX = targetWidth / tempWidth;
            float scaleY = targetHeight / tempHeight;
            // 使用较小的缩放比例，确保图像完全适应目标区域
            float scale = Math.min(scaleY, scaleX);

            // 计算实际输出尺寸
            int width = (int) (tempWidth * scale);
            int height = (int) (tempHeight * scale);

            // 创建Bitmap和Canvas
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);

            // 应用缩放变换
            canvas.scale(scale, scale);

            // 将SVG渲染到Canvas
            svg.renderToCanvas(canvas);

            return bitmap;
        } catch (SVGParseException e) {
            // SVG解析失败
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 创建SVG文件头
     * 
     * <p>生成SVG XML文件的头部声明和根元素。
     * 包含XML版本、编码、尺寸和命名空间等基本信息。</p>
     * 
     * @param width SVG画布宽度
     * @param height SVG画布高度
     * @return SVG头部字符串
     */
    private static String createHeader(int width, int height) {
        StringBuilder header = new StringBuilder();
        header
                // XML声明
                .append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n")
                // SVG根元素开始，包含唯一ID、尺寸和命名空间
                .append("<svg id=\"").append("Test").append("\"") //TODO 唯一ID，后续可用于区分是哪个图标
                .append(" width=\"").append(width).append("\" height=\"").append(height).append("\" ")
                .append("xmlns=\"http://www.w3.org/2000/svg\">\n");
        return header.toString();
    }

    /**
     * 创建多边形元素
     * 
     * <p>生成SVG多边形（polygon）元素的字符串表示。
     * 多边形由一组顶点坐标定义，支持填充色和描边。</p>
     * 
     * @param points 顶点坐标字符串，多个坐标用空格分隔
     *               如："1,13 13,1 25,13 25,37 13,49 1,37"
     * @param fillColor 填充颜色，如"#FF0000"
     * @param strokeWidth 描边宽度（像素）
     * @param strokeColor 描边颜色，如"#000000"
     * @return SVG多边形元素字符串
     */
    private static String createPolygon(String points, String fillColor, int strokeWidth, String strokeColor) {
        StringBuilder polygon = new StringBuilder();
        polygon.append("    <polygon\n")
                // 顶点坐标
                .append("        points=\"").append(points).append("\"\n")
                // 填充颜色
                .append("        fill=\"").append(fillColor).append("\"\n")
                // 描边宽度
                .append("        stroke-width=\"").append(strokeWidth).append("\"\n")
                // 描边颜色
                .append("        stroke=\"").append(strokeColor).append("\"\n")
                .append("    />\n");
        return polygon.toString();
    }

    /**
     * 创建标准路径元素
     * 
     * <p>生成SVG路径（path）元素的字符串表示。
     * 路径由d属性定义的路径数据组成，支持填充色。</p>
     * 
     * @param path 路径数据字符串，如"M0,0 L10,10 L10,0 Z"
     * @param fillColor 填充颜色，如"#FF0000"
     * @return SVG路径元素字符串
     */
    //生成标准路径
    private static String createBasePath(String path, String fillColor) {
        StringBuilder basePath = new StringBuilder();
        basePath.append("    <path\n")
                // 路径数据
                .append("        d=\"").append(path).append("\"\n")
                // 填充颜色
                .append("        fill=\"").append(fillColor).append("\"")
                .append("  />\n");
        return basePath.toString();
    }

    /**
     * 创建对号路径元素
     * 
     * <p>生成选中状态下显示的对号（勾选标记）SVG路径元素。
     * 对号使用通道对应的颜色进行描边，背景使用默认背景色。</p>
     * 
     * <p>样式特点：</p>
     * <ul>
     *     <li>填充色：背景色（黑色）</li>
     *     <li>描边色：通道对应的颜色</li>
     *     <li>描边宽度：2像素</li>
     *     <li>线帽和连接：圆角</li>
     * </ul>
     * 
     * @param chIndex 通道索引，用于获取对应颜色
     * @return SVG路径元素字符串
     */
    //生成对号路径
    private static String createDuiHaoPath(int chIndex) {
        StringBuilder duiHao = new StringBuilder();
        duiHao.append("    <path\n")
                // 对号路径数据
                .append("        d=\"").append(SvgNodeInfo.PATH_DUI_HAO).append("\"\n")
                // 填充色为背景色
                .append("        fill=\"").append(SvgNodeInfo.getColorBackground()).append("\"\n")
                // 描边宽度
                .append("        stroke-width=\"").append("2").append("\"\n")
                // 描边色为通道颜色
                .append("        stroke=\"").append(SvgNodeInfo.getAllBaseColor(chIndex)).append("\"\n")
                // 圆角连接
                .append("        stroke-linejoin=\"").append("round").append("\"\n")
                // 圆角线帽
                .append("        stroke-linecap=\"").append("round").append("\"\n")
                .append("  />\n");
        return duiHao.toString();
    }

    /**
     * 创建对号外框路径元素
     * 
     * <p>生成通道选择框的外框SVG路径元素。
     * 外框是一个圆角矩形，使用通道选择框颜色进行描边。</p>
     * 
     * <p>样式特点：</p>
     * <ul>
     *     <li>填充色：背景色（黑色）</li>
     *     <li>描边色：通道选择框颜色</li>
     *     <li>描边宽度：2像素</li>
     * </ul>
     * 
     * @return SVG路径元素字符串
     */
    //生成对号外框路径
    private static String createDuiHaoBorderPath() {
        StringBuilder duiHao = new StringBuilder();
        duiHao.append("    <path\n")
                // 外框路径数据
                .append("        d=\"").append(SvgNodeInfo.PATH_MULCHOICE_BORDER).append("\"\n")
                // 填充色为背景色
                .append("        fill=\"").append(SvgNodeInfo.getColorBackground()).append("\"\n")
                // 描边宽度
                .append("        stroke-width=\"").append("2").append("\"\n")
                // 描边色为通道选择框颜色
                .append("        stroke=\"").append(SvgNodeInfo.getColorChChoice()).append("\"\n")
                .append("  />\n");
        return duiHao.toString();
    }

}
