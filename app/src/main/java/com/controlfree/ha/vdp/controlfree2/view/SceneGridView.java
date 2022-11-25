package com.controlfree.ha.vdp.controlfree2.view;

import android.content.Context;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.controlfree.ha.vdp.controlfree2.utils.ResolutionHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;

public class SceneGridView extends FrameLayout {
    private final static String TAG = "SceneGridView";
    private Context c;
    private int W;
    float padding = ResolutionHandler.getPaddingW()/3f;
    private float colCount = 2;
    private LinkedList<SceneGridCellView> cellList = new LinkedList<SceneGridCellView>();
    private JSONArray dataArr = new JSONArray();
    private ArrayList<Boolean> isLoadingList = new ArrayList<Boolean>();
    private ArrayList<Long> loadingTimeList = new ArrayList<Long>();

    private Listener listener;
    public static interface Listener{
        public void onHeightChange();
    }
    public SceneGridView(Context context, int w, Listener l) {
        super(context);
        this.c = context;
        this.W = w;
        this.listener = l;
        setBackgroundColor(0x00000000);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(W, LinearLayout.LayoutParams.WRAP_CONTENT);
        setLayoutParams(p);
        detectH();
    }
    public void setData(JSONArray arr){
        removeAllViews();
        dataArr = arr;
        if(ResolutionHandler.isTablet()){
            colCount = 3;
            if(ResolutionHandler.islandscape()) colCount = 4;
        }else{
            if(ResolutionHandler.islandscape()) colCount = 4;
        }
        float gridW = getGridW(colCount);
        float gridH = getGridH(gridW);
        float maxRow = 0;
        for(int i=0;i<arr.length();i++){
            float row = (float)(i-i%colCount)/(float)colCount;
            float col = i%colCount;
            SceneGridCellView cell = new SceneGridCellView(c, (int)gridW, (int)gridH);
            cell.setX(col*(gridW+padding));
            cell.setY(row*(gridH+padding));
            try {
                cell.setData(arr.getJSONObject(i));
            }catch(Exception e){e.printStackTrace();}
            addView(cell);
            cellList.add(cell);
            if(row>maxRow) maxRow = row;
            isLoadingList.add(false);
            loadingTimeList.add(0L);
        }
        maxRow++;
        ((LinearLayout.LayoutParams)getLayoutParams()).height = (int)(maxRow*(gridH+padding));
        detectH();
    }
    public void detectH(){
        measure(0, 0);
        int h = getMeasuredHeight();
        h += ResolutionHandler.getBottomTabH();
        if(h<ResolutionHandler.getViewPortH()) h = (ResolutionHandler.getViewPortH());
        ((LinearLayout.LayoutParams)getLayoutParams()).height = h;
        listener.onHeightChange();
    }
    public float getGridW(float col){
        return ((float)W-padding*(col-1))/col;
    }
    public float getGridH(float w){
        return w/1.85f;
    }
    public JSONObject getGridDataByLocation(float x, float y){
        try{
            float gridW = getGridW(colCount);
            float gridH = getGridH(gridW);
            for(int i=0;i<cellList.size();i++){
                if(x>=cellList.get(i).getX() && x<cellList.get(i).getX()+gridW){
                    if(y>=cellList.get(i).getY() && y<cellList.get(i).getY()+gridH) {
                        setLoading(i, true);
                        return dataArr.getJSONObject(i);
                    }
                }
            }
        }catch(Exception e){e.printStackTrace();}
        return null;
    }



    private void setLoading(int i, boolean is){
        if(is){
            loadingTimeList.set(i, System.currentTimeMillis());
        }
        isLoadingList.set(i, is);
        cellList.get(i).isLoading(is);
    }
    public void ckLoading(){
        for(int i=0;i<cellList.size();i++){
            if(isLoadingList.get(i)){
                if(System.currentTimeMillis()-loadingTimeList.get(i)>10*1000){
                    setLoading(i, false);
                }
            }
        }
    }
    public boolean onSCend(String sid){
        try{
            for(int i=0;i<dataArr.length();i++){
                if(dataArr.getJSONObject(i).getString("id").contentEquals(sid)){
                    setLoading(i, false);
                    return true;
                }
            }
        }catch(Exception e){e.printStackTrace();}
        return false;
    }
    public boolean onSC(String sid){
        try{
            for(int i=0;i<dataArr.length();i++){
                if(dataArr.getJSONObject(i).getString("id").contentEquals(sid)){
                    setLoading(i, true);
                    return true;
                }
            }
        }catch(Exception e){e.printStackTrace();}
        return false;
    }
}
