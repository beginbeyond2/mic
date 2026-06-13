package com.micsig.tbook.tbookscope.wavezone.display;

import com.chillingvan.canvasgl.ICanvasGL;

/**
 * Created by liwb on 2017/11/3.
 */

public interface IGrid {
    public static final int GridAttr_CrossLine = 0x01;
    public static final int GridAttr_CrossPoint = 0x02;
    public static final int GridAttr_ALLPoint = 0x04;
    public static final int GridAttr_Frame = 0x08;

    public int getGridLine_Attr();
    public void setGridLine_Attr(int gridLine_Attr);
    public int getGridLine_Bright();
    public void setGridLine_Bright(int gridLine_Bright);

    public void draw(ICanvasGL canvas);
    public void refresh();
    public void setHeightDiv(int heightDiv);
}
