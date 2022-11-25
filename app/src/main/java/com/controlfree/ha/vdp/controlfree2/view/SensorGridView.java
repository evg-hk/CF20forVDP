package com.controlfree.ha.vdp.controlfree2.view;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.controlfree.ha.vdp.controlfree2.R;
import com.controlfree.ha.vdp.controlfree2.component.ExImageView;
import com.controlfree.ha.vdp.controlfree2.component.ExTextView;
import com.controlfree.ha.vdp.controlfree2.utils.Api;
import com.controlfree.ha.vdp.controlfree2.utils.ResolutionHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedList;

public class SensorGridView extends FrameLayout {
    private final static String TAG = "SensorGridView";
    private Context c;
    private int W;
    float padding = ResolutionHandler.getPaddingW()/3f;
    private float colCount = 2;
    private LinkedList<SensorGridCellView> cellList = new LinkedList<SensorGridCellView>();
    private JSONArray dataArr = new JSONArray();
    public SensorGridView(Context context, int w) {
        super(context);
        this.c = context;
        this.W = w;
        setBackgroundColor(0x00000000);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(W, LinearLayout.LayoutParams.WRAP_CONTENT);
        setLayoutParams(p);
    }
    public void setData(JSONArray arr) {
        removeAllViews();
        dataArr = arr;
        if (ResolutionHandler.isTablet()) {
            colCount = 3;
            if (ResolutionHandler.islandscape()) colCount = 4;
        } else {
            if (ResolutionHandler.islandscape()) colCount = 4;
        }
        float gridW = getGridW(colCount);
        float gridH = getGridH(gridW);
        float maxRow = 0;
        float totalW = 0;
        for (int i = 0; i < arr.length(); i++) {
            SensorGridCellView cell = new SensorGridCellView(c, (int) gridW, (int) gridH);
            cell.setX(totalW);
            try {
                cell.setData(arr.getJSONObject(i));
            } catch (Exception e) {
                e.printStackTrace();
            }
            addView(cell);
            cellList.add(cell);
            float w = cell.getMeasuredW();
            ((FrameLayout.LayoutParams) cell.getLayoutParams()).width = (int) w;
            totalW += w + padding;
        }
        if (arr.length() > 0) maxRow++;
        ((LinearLayout.LayoutParams) getLayoutParams()).width = (int) (totalW - padding);
        ((LinearLayout.LayoutParams) getLayoutParams()).height = (int) (maxRow * (gridH + padding) - (maxRow > 0 ? padding : 0));

    }
    public float getGridW(float col){
        return ((float)W-padding*(col-1))/col;
    }
    public float getGridH(float w){
        return w/1.8f;
    }
    public int getW(){
        return ((LinearLayout.LayoutParams)getLayoutParams()).width;
    }
    public int getH(){
        return ((LinearLayout.LayoutParams)getLayoutParams()).height;
    }
    public JSONObject getGridDataByLocation(float x, float y){
        try{
            for(int i=0;i<cellList.size();i++){
                if(x>=cellList.get(i).getX() && x<cellList.get(i).getX()+cellList.get(i).getLayoutParams().width){
                    if(y>=cellList.get(i).getY() && y<cellList.get(i).getY()+cellList.get(i).getLayoutParams().height) {
                        return dataArr.getJSONObject(i);
                    }
                }
            }
        }catch(Exception e){e.printStackTrace();}
        return null;
    }


    public boolean updateStatus(String did, String gp, String cid, String v, long t){
        //Log.e(TAG, "updateStatus: "+cellList.size());
        for(int i=0;i<cellList.size();i++){
            if(cellList.get(i).updateStatus(did, gp, cid, v, t)){
                return true;
            }
        }
        return false;
    }
}
