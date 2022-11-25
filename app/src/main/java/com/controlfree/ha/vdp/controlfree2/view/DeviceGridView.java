package com.controlfree.ha.vdp.controlfree2.view;

import android.content.Context;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.controlfree.ha.vdp.controlfree2.component.ExTextView;
import com.controlfree.ha.vdp.controlfree2.utils.ResolutionHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedList;

public class DeviceGridView extends FrameLayout {
    private final static String TAG = "DeviceGridView";
    private Context c;
    private int W;
    float padding = ResolutionHandler.getPaddingW()/3f;
    private float colCount = 3;
    private LinkedList<DeviceGridCellView> cellList = new LinkedList<DeviceGridCellView>();
    private JSONArray dataArr = new JSONArray();
    public DeviceGridView(Context context, int w) {
        super(context);
        this.c = context;
        this.W = w;
        setBackgroundColor(0x00000000);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(W, LinearLayout.LayoutParams.WRAP_CONTENT);
        setLayoutParams(p);
    }
    public void setData(JSONArray arr){
        removeAllViews();
        dataArr = arr;
        if(ResolutionHandler.isTablet()){
            colCount = 4;
            if(ResolutionHandler.islandscape()) colCount = 7;
        }else{
            if(ResolutionHandler.islandscape()) colCount = 6;
        }
        float gridW = getGridW(colCount);
        //Log.e(TAG, "arr.length(): "+arr.length());
        float maxRow = 0;
        float predictRowCount = (float)Math.ceil(arr.length()/colCount);
        for(int i=0;i<arr.length();i++){
//            float row = (float)(i-i%colCount)/(float)colCount;
//            float col = i%colCount;
            float row = (float)(i%predictRowCount);
            float col = (float)(i-i%predictRowCount)/(float)predictRowCount;
            //Log.e(TAG, "grid: "+col+" : "+row+" / "+gridW);
            DeviceGridCellView cell = new DeviceGridCellView(c, (int)gridW);
            cell.setX(col*(gridW+padding));
            cell.setY(row*(gridW+padding));
            try {
                //Log.e(TAG, "setData: "+i+" : "+arr.getJSONObject(i).getString("id"));
                cell.setData(arr.getJSONObject(i));
            }catch(Exception e){e.printStackTrace();}
            addView(cell);
            cellList.add(cell);
            if(row>maxRow) maxRow = row;
        }
        maxRow++;
        //Log.e(TAG, "maxRow: "+maxRow+" / "+(int)(maxRow*(gridW+15f)));
        ((LinearLayout.LayoutParams)getLayoutParams()).height = (int)(maxRow*(gridW+padding));
        //Log.e(TAG, "height: "+((LinearLayout.LayoutParams)getLayoutParams()).height);
    }
    public float getGridW(float col){
        return ((float)W-padding*(col-1))/col;
    }
    public float getGridH(float w){
        return w;
    }
    public JSONObject getGridDataByLocation(float x, float y){
        try{
            float gridW = getGridW(colCount);
            float gridH = getGridH(gridW);
            for(int i=0;i<cellList.size();i++){
                if(x>=cellList.get(i).getX() && x<cellList.get(i).getX()+gridW){
                    if(y>=cellList.get(i).getY() && y<cellList.get(i).getY()+gridH) {
                        return dataArr.getJSONObject(i);
                    }
                }
            }
//            int index = (int)(colCount*Math.floor(y/(getGridW(colCount)+padding)));
//            if(index>=0){
//                index += (int)Math.floor(x/((float)W/colCount));
//                if(index<dataArr.length()){
//                    return dataArr.getJSONObject(index);
//                }
//            }
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
