package com.micsig.tbook.tbookscope.wavezone.display;

import com.chillingvan.canvasgl.ICanvasGL;
import com.micsig.tbook.tbookscope.wavezone.IWorkMode;
import com.micsig.tbook.tbookscope.wavezone.WorkModeManage;


/**
 * Created by liwb on 2017/11/8.
 * 网格管理
 */

public class WaveGridManage implements IWorkMode, IGrid {
    //region 创建单例
    private static class WaveGridManageHolder {
        private static final WaveGridManage instance = new WaveGridManage();
    }

    public static final WaveGridManage getInstance() {
        return WaveGridManageHolder.instance;
    }
    //endregion

    private WaveGrid_XY waveGrid_xy;
    private WaveGrid_YT waveGrid_yt;
    private WaveGrid_YTZoom waveGrid_ytZoom;

    public WaveGridManage(){
        waveGrid_xy=new WaveGrid_XY();
        waveGrid_yt=new WaveGrid_YT();
        waveGrid_ytZoom=new WaveGrid_YTZoom();
    }
    public void init(){}

    //region interface IWorkMode
    @Override
    public void switchWorkMode(@WorkMode int workMode) {
          switch (workMode){
              case IWorkMode.WorkMode_XY:
                  waveGrid_xy.switchWorkMode(workMode);
                  break;
              case IWorkMode.WorkMode_YT:
              case IWorkMode.WorkMode_YTZOOM:
                  waveGrid_yt.switchWorkMode(workMode);
                  break;
          }
    }
    //endregion

    //region interface IGrid


    @Override
    public void refresh() {
        waveGrid_xy.refresh();
        waveGrid_yt.refresh();
        waveGrid_ytZoom.refresh();
    }

    @Override
    public void setHeightDiv(int heightDiv) {
        waveGrid_yt.setHeightDiv(heightDiv);
        waveGrid_ytZoom.setHeightDiv(heightDiv);
        waveGrid_xy.setHeightDiv(heightDiv);
        refresh();
    }

    @Override
    public int getGridLine_Attr() {
        switch (WorkModeManage.getInstance().getmWorkMode()){
            case IWorkMode.WorkMode_XY:
                return waveGrid_xy.getGridLine_Attr();
            case IWorkMode.WorkMode_YT:
            case IWorkMode.WorkMode_YTZOOM:
                return waveGrid_yt.getGridLine_Attr();
        }
        return 0;
    }

    @Override
    public void setGridLine_Attr(int gridLine_Attr) {
        waveGrid_xy.setGridLine_Attr(gridLine_Attr);
        waveGrid_yt.setGridLine_Attr(gridLine_Attr);
        waveGrid_ytZoom.setGridLine_Attr(gridLine_Attr);
    }

    @Override
    public int getGridLine_Bright() {
        switch(WorkModeManage.getInstance().getmWorkMode()){
            case IWorkMode.WorkMode_XY:
                return waveGrid_xy.getGridLine_Bright();

            case IWorkMode.WorkMode_YT:
            case IWorkMode.WorkMode_YTZOOM:
                return waveGrid_yt.getGridLine_Bright();
        }
        return 0;
    }

    @Override
    public void setGridLine_Bright(int gridLine_Bright) {
        waveGrid_xy.setGridLine_Bright(gridLine_Bright);
        waveGrid_yt.setGridLine_Bright(gridLine_Bright);
        waveGrid_ytZoom.setGridLine_Bright(gridLine_Bright);
    }

    @Override
    public void draw(ICanvasGL canvas) {
//        switch (WorkModeManage.get().getmWorkMode()){
//            case IWorkMode.WorkMode_XY:
//                waveGrid_xy.draw(canvas);
//                break;
//            case IWorkMode.WorkMode_YT:waveGrid_yt.draw(canvas);break;
//            case IWorkMode.WorkMode_YTZOOM:waveGrid_ytZoom.draw(canvas); break;
//        }
    }

    public void draw(int currCanvas,ICanvasGL canvas){
        switch (currCanvas){
            case IWorkMode.WorkMode_XY:
                waveGrid_xy.draw(canvas);
                break;
            case IWorkMode.WorkMode_YT:waveGrid_yt.draw(canvas);break;
            case IWorkMode.WorkMode_YTZOOM:waveGrid_ytZoom.draw(canvas); break;
        }
    }
    //endregion
}
