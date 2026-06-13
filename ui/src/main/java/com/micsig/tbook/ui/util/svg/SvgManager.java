package com.micsig.tbook.ui.util.svg;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;

import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;
import com.micsig.base.Logger;

import java.util.List;

public class SvgManager {

    private static final String TAG = "SvgCreateManager";

    public static Bitmap createSvg(List<String> pathDataArray, List<String> colors, int width, int height) {
        try {
            if (pathDataArray.size() != colors.size()) {
                throw new IllegalArgumentException("Path data and colors arrays must have same length");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        StringBuilder svgInfo = new StringBuilder();
        svgInfo.append(createHeader(width, height));
        if (pathDataArray.size() > 0 && colors.size() > 0) {
            for (int i = 0; i < pathDataArray.size(); i++) {
                svgInfo.append(createBasePath(pathDataArray.get(i), colors.get(i)));
            }
        }
        svgInfo.append("</svg>");
//        Logger.i(TAG, "svgInfo= \n" + svgInfo);
        return parseSvgInfo(svgInfo.toString(), width, height);
    }

    public static Bitmap createScaleSvg(List<String> pathDataArray, List<String> colors, int width, int height, int desWidth, int desHeight) {
        Bitmap bitmap = createSvg(pathDataArray, colors, width, height);
        return Bitmap.createScaledBitmap(bitmap, desWidth, desHeight, true);
    }

    public static Bitmap createDuiHaoSvg(int chIndex, int width, int height, Boolean isUncheck) {
        StringBuilder svgInfo = new StringBuilder();
        svgInfo.append(createHeader(width, height))
                .append(createDuiHaoBorderPath());
        if (!isUncheck) {
            svgInfo.append(createDuiHaoPath(chIndex));
        }
        svgInfo.append("</svg>");
//        Logger.i(TAG, "svgInfo= \n" + svgInfo);
        return parseSvgInfo(svgInfo.toString(), width, height);
    }

    private static Bitmap parseSvgInfo(String svgString, int targetWidth, int targetHeight) {
        try {
            SVG svg = SVG.getFromString(svgString);
            float tempWidth, tempHeight;
            if (svg.getDocumentViewBox() == null) {
                tempWidth = svg.getDocumentWidth();
                tempHeight = svg.getDocumentHeight();
                if (tempWidth <= 0 || tempHeight <= 0) {
                    tempWidth = targetWidth;
                    tempHeight = targetHeight;
                }
            } else {
                tempWidth = svg.getDocumentViewBox().width();
                tempHeight = svg.getDocumentViewBox().height();
            }
            float scaleX = targetWidth / tempWidth;
            float scaleY = targetHeight / tempHeight;
            float scale = Math.min(scaleY, scaleX);
            int width = (int) (tempWidth * scale);
            int height = (int) (tempHeight * scale);
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            canvas.scale(scale, scale);
            svg.renderToCanvas(canvas);
            return bitmap;
        } catch (SVGParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String createHeader(int width, int height) {
        StringBuilder header = new StringBuilder();
        header
                .append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n")
                .append("<svg id=\"").append("Test").append("\"") //TODO 唯一ID，后续可用于区分是哪个图标
                .append(" width=\"").append(width).append("\" height=\"").append(height).append("\" ")
                .append("xmlns=\"http://www.w3.org/2000/svg\">\n");
        return header.toString();
    }

    private static String createPolygon(String points, String fillColor, int strokeWidth, String strokeColor) {
        StringBuilder polygon = new StringBuilder();
        polygon.append("    <polygon\n")
                .append("        points=\"").append(points).append("\"\n")
                .append("        fill=\"").append(fillColor).append("\"\n")
                .append("        stroke-width=\"").append(strokeWidth).append("\"\n")
                .append("        stroke=\"").append(strokeColor).append("\"\n")
                .append("    />\n");
        return polygon.toString();
    }

    //生成标准路径
    private static String createBasePath(String path, String fillColor) {
        StringBuilder basePath = new StringBuilder();
        basePath.append("    <path\n")
                .append("        d=\"").append(path).append("\"\n")
                .append("        fill=\"").append(fillColor).append("\"")
                .append("  />\n");
        return basePath.toString();
    }

    //生成对号路径
    private static String createDuiHaoPath(int chIndex) {
        StringBuilder duiHao = new StringBuilder();
        duiHao.append("    <path\n")
                .append("        d=\"").append(SvgNodeInfo.PATH_DUI_HAO).append("\"\n")
                .append("        fill=\"").append(SvgNodeInfo.getColorBackground()).append("\"\n")
                .append("        stroke-width=\"").append("2").append("\"\n")
                .append("        stroke=\"").append(SvgNodeInfo.getAllBaseColor(chIndex)).append("\"\n")
                .append("        stroke-linejoin=\"").append("round").append("\"\n")
                .append("        stroke-linecap=\"").append("round").append("\"\n")
                .append("  />\n");
        return duiHao.toString();
    }

    //生成对号外框路径
    private static String createDuiHaoBorderPath() {
        StringBuilder duiHao = new StringBuilder();
        duiHao.append("    <path\n")
                .append("        d=\"").append(SvgNodeInfo.PATH_MULCHOICE_BORDER).append("\"\n")
                .append("        fill=\"").append(SvgNodeInfo.getColorBackground()).append("\"\n")
                .append("        stroke-width=\"").append("2").append("\"\n")
                .append("        stroke=\"").append(SvgNodeInfo.getColorChChoice()).append("\"\n")
                .append("  />\n");
        return duiHao.toString();
    }


    public static Bitmap createCursorSvg(List<String> pathDataArray, List<String> colors, int width, int height, String polygonPoints, List<String> polygonColors) {
        try {
            if (pathDataArray.size() != colors.size()) {
                throw new IllegalArgumentException("Path data and colors arrays must have same length");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        StringBuilder svgInfo = new StringBuilder();
        svgInfo.append(createHeader(width, height))
                .append(createPolygon(polygonPoints, polygonColors.get(0), 2, polygonColors.get(1)));
        if (pathDataArray.size() > 0 && colors.size() > 0) {
            for (int i = 0; i < pathDataArray.size(); i++) {
                svgInfo.append(createBasePath(pathDataArray.get(i), colors.get(i)));
            }
        }
        svgInfo.append("</svg>");
//        Logger.i(TAG, "svgInfo= \n" + svgInfo);
        Bitmap bitmap = parseSvgInfo(svgInfo.toString(), (int) (width * 0.8), (int) (height * 0.8));
        return bitmap;
    }


}
