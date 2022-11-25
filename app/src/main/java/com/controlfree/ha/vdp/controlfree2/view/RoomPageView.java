package com.controlfree.ha.vdp.controlfree2.view;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.controlfree.ha.vdp.controlfree2.R;
import com.controlfree.ha.vdp.controlfree2.component.ExGradientView;
import com.controlfree.ha.vdp.controlfree2.component.ExImageView;
import com.controlfree.ha.vdp.controlfree2.component.ExShortLine;
import com.controlfree.ha.vdp.controlfree2.component.ExTextView;
import com.controlfree.ha.vdp.controlfree2.utils.Api;
import com.controlfree.ha.vdp.controlfree2.utils.Cache;
import com.controlfree.ha.vdp.controlfree2.utils.DB;
import com.controlfree.ha.vdp.controlfree2.utils.FileHandler;
import com.controlfree.ha.vdp.controlfree2.utils.Fun;
import com.controlfree.ha.vdp.controlfree2.utils.ResolutionHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;

public class RoomPageView extends FrameLayout {
    private final static String TAG = "RoomPageView";
    private Context c;
    private ExTextView v_title, sceneNameView, sensorNameView, deviceNameView;
    private DeviceGridView deviceGridView;
    private SensorGridView sensorGridView;
    private RoomSceneGridView sceneGridView;
    private JSONObject dataObj = new JSONObject();
    private ExImageView iv_bg;
    private ExGradientView gv_bg;
    private LinearLayout ll_main;
    private boolean is_run = true;
    private float W = 0;
    private int bgH = ResolutionHandler.getViewPortH()*3/4;
    private int mode = 0;
    private PointF containerPt = new PointF(), pt = new PointF(), newPt = new PointF();
    private long touchTime = 0;
    public boolean isLoading = false;

    private Listener listener;
    public static interface Listener{
        public void onHeightChange();
        public void onDeviceLoaded(JSONArray arr);
        public void onTagDevice(JSONObject obj);
        public void onTagScene(JSONObject obj);
        public void onBackgroundColor(int tag, int color);
    }
    public RoomPageView(Context context, Listener l) {
        super(context);
        this.c = context;
        this.listener = l;
        FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(ResolutionHandler.getViewPortW(), LayoutParams.WRAP_CONTENT);
        setLayoutParams(p);
        setClipChildren(false);
        setClipToOutline(false);

        FrameLayout.LayoutParams p_bg = new FrameLayout.LayoutParams(ResolutionHandler.getViewPortW(), bgH);
        ExImageView bg = new ExImageView(c, ResolutionHandler.getViewPortW(), bgH);
        //bg.setBackgroundColor(0xff646668);
        //bg.setScaleType(ImageView.ScaleType.CENTER_CROP);
        //addView(bg, p_bg);
        iv_bg = bg;

        ExGradientView gv = new ExGradientView(c, ResolutionHandler.getViewPortW(), bgH);
        //addView(gv);
        gv_bg = gv;

        W = ResolutionHandler.getViewPortW()-2*ResolutionHandler.getPaddingW();
        FrameLayout.LayoutParams p_ll = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        p_ll.setMargins(ResolutionHandler.getPaddingW(), 0, ResolutionHandler.getPaddingW(), 0);
        LinearLayout ll = new LinearLayout(c);
        ll.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams p_f_ll_top = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        p_f_ll_top.topMargin = ResolutionHandler.getPaddingW()/2;
        FrameLayout f_ll_top = new FrameLayout(c);

        FrameLayout.LayoutParams p_ll_top = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        p_ll_top.gravity = Gravity.BOTTOM;
        LinearLayout ll_top = new LinearLayout(c);
        ll_top.setOrientation(LinearLayout.VERTICAL);

        ExImageView btn = new ExImageView(c,  R.drawable.btn_edit, ResolutionHandler.getBtnW(), ResolutionHandler.getBtnW());
        btn.setCircleIcon();
        btn.setVisibility(View.GONE);
        ((LinearLayout.LayoutParams)btn.getLayoutParams()).gravity = Gravity.RIGHT;
        ll_top.addView(btn);

        ExTextView title = new ExTextView(c, "");
        title.setTextColor(0xffffffff);
        title.setTextSize(ResolutionHandler.fontsize_large);
        title.setTypeface(null, Typeface.BOLD);
        ll_top.addView(title);
        v_title = title;

        ExTextView location = new ExTextView(c, Cache.get("server_name"));
        location.setTextColor(0xffffffff);
        ll_top.addView(location);

        f_ll_top.addView(ll_top, p_ll_top);
        ll.addView(f_ll_top, p_f_ll_top);
        //---------

        ExShortLine line = new ExShortLine(c);
        ll.addView(line);

        ExTextView t_scene = new ExTextView(c, "Scene");
        t_scene.setTextColor(0xffffffff);
        t_scene.setTypeface(null, Typeface.BOLD);
        ll.addView(t_scene);
        sceneNameView = t_scene;
        sceneNameView.setVisibility(View.GONE);

        RoomSceneGridView scgv = new RoomSceneGridView(c, (int)W);
        ll.addView(scgv);
        sceneGridView = scgv;
        sceneGridView.setVisibility(View.GONE);

        ExTextView t_device = new ExTextView(c, "Device");
        t_device.setTextColor(0xffffffff);
        t_device.setTypeface(null, Typeface.BOLD);
        ll.addView(t_device);
        deviceNameView = t_device;
        deviceNameView.setVisibility(View.GONE);

        DeviceGridView dgv = new DeviceGridView(c, (int)W);
        //dgv.setBackgroundColor(0xffff0000);
        ll.addView(dgv);
        deviceGridView = dgv;
        deviceGridView.setVisibility(View.GONE);

        ExTextView t_sensor = new ExTextView(c, "Sensor");
        t_sensor.setTextColor(0xffffffff);
        t_sensor.setTypeface(null, Typeface.BOLD);
        ll.addView(t_sensor);
        sensorNameView = t_sensor;
        sensorNameView.setVisibility(View.GONE);

        SensorGridView sgv = new SensorGridView(c, (int)W);
        ll.addView(sgv);
        sensorGridView = sgv;
        sensorGridView.setVisibility(View.GONE);

        addView(ll, p_ll);
        ll_main = ll;

        detectH();
        loop();
    }
    private void detectH(){
        measure(0, 0);
        int h = getMeasuredHeight();
        //h += ResolutionHandler.getBottomTabH()+ResolutionHandler.getPaddingW();
        if(h<ResolutionHandler.getViewPortH()) h = (ResolutionHandler.getViewPortH());
        ((FrameLayout.LayoutParams)getLayoutParams()).height = h;
        listener.onHeightChange();
    }
    private void loop() {
        if (is_run) {
            try {
                if(mode==1) {
                    float newX = containerPt.x + (newPt.x - pt.x);
                    sceneGridView.setX(sceneGridView.getX() + (newX - sceneGridView.getX()) * 0.5f);
                }else if(mode==2) {
                    float newX = containerPt.x + (newPt.x - pt.x);
                    sensorGridView.setX(sensorGridView.getX() + (newX - sensorGridView.getX()) * 0.5f);
                }else{
                    float newX = sceneGridView.getX();
                    if(newX<0-sceneGridView.getW()+W) newX = 0-sceneGridView.getW()+W;
                    if(newX>0) newX = 0;
                    sceneGridView.setX(sceneGridView.getX() + (newX - sceneGridView.getX()) * 0.5f);

                    newX = sensorGridView.getX();
                    if(newX<0-sensorGridView.getW()+W) newX = 0-sensorGridView.getW()+W;
                    if(newX>0) newX = 0;
                    sensorGridView.setX(sensorGridView.getX() + (newX - sensorGridView.getX()) * 0.5f);
                }
                sceneGridView.ckLoading();
            }catch(Exception e){e.printStackTrace();}
            new Thread(new Runnable(){
                @Override
                public void run() {
                    try{
                        Thread.sleep(30);
                    }catch(Exception e){}
                    loop();
                }
            }).start();
        }
    }

    //scene ---------------
    public float getSceneGridViewY(){
        Rect r = new Rect();
        sceneGridView.getDrawingRect(r);
        this.offsetDescendantRectToMyCoords(sceneGridView, r);
        return r.top;
    }
    public float getSceneGridViewH(){
        return sceneGridView.getH();
    }
    public void mouseDownSceneGridView(float x){
        containerPt = new PointF(sceneGridView.getX(), sceneGridView.getY());
        pt = new PointF(x, 0);
        newPt = new PointF(x, 0);
        touchTime = System.currentTimeMillis();
        mode = 1;
    }
    public void mouseMoveSceneGridView(float x){
        newPt.x = x;
    }
    public void mouseUpSceneGridView(float x){
        mode = 0;
    }

    //sensor ---------------
    public float getSensorGridViewY(){
        Rect r = new Rect();
        sensorGridView.getDrawingRect(r);
        this.offsetDescendantRectToMyCoords(sensorGridView, r);
        return r.top;
    }
    public float getSensorGridViewH(){
        return sensorGridView.getH();
    }
    public void mouseDownSensorGridView(float x){
        containerPt = new PointF(sensorGridView.getX(), sensorGridView.getY());
        pt = new PointF(x, 0);
        newPt = new PointF(x, 0);
        touchTime = System.currentTimeMillis();
        mode = 2;
    }
    public void mouseMoveSensorGridView(float x){
        newPt.x = x;
    }
    public void mouseUpSensorGridView(float x){
        mode = 0;
    }



    public void setData(final JSONObject obj){
        try {
            dataObj = new JSONObject(obj.toString());
            v_title.setText(dataObj.getString("name"));

            //scene
            JSONArray sArr = Cache.getRoomAutomationList(c, dataObj.getInt("id"));
//            DB db = new DB(c);
//            JSONArray sArr = db.getRoomSceneList(DB.getSetting(c, "gid"), obj.getString("id"));
//            db.close();
            sceneGridView.setData(sArr);
            if(sArr.length()>0){
                sceneNameView.setVisibility(View.VISIBLE);
                sceneGridView.setVisibility(View.VISIBLE);
                ((LinearLayout.LayoutParams)sceneNameView.getLayoutParams()).bottomMargin = ResolutionHandler.getPaddingH()/2;
                ((LinearLayout.LayoutParams)sceneGridView.getLayoutParams()).bottomMargin = ResolutionHandler.getPaddingH();
            }
            //Log.e(TAG, "sceneGridView: "+ sArr.length());

            isLoading = true;
            Api.getDeviceInRoom(c, dataObj.getString("id"), new Api.Listener() {
                @Override
                public void onSuccess(JSONObject reqObj, JSONObject obj, JSONArray arr, JSONObject resObj) {
                    isLoading = false;
                    try {
                        DB db = new DB(c);
                        db.updateList("Device", arr);
                        db.close();

                        loadDataToUi();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFail(String msg, JSONObject resObj) {
                    isLoading = false;
                    Fun.showAlert(c, Fun.getErrorMsg(msg));
                }
            });
            loadBgImg();
        }catch(Exception e){e.printStackTrace();}
    }
    public JSONObject getData(){
        return dataObj;
    }

    private void loadDataToUi(){
        ((Activity)c).runOnUiThread(new Runnable(){
            @Override
            public void run() {
                try{
                    DB db = new DB(c);
                    JSONArray arr = db.getDeviceList(dataObj.getString("id"));
                    db.close();

                    JSONObject dIconObj = Cache.getObj("device_icon");
                    //Log.e(TAG, "dIconObj: "+dIconObj);

                    JSONArray dArr = new JSONArray();
                    JSONArray sArr = new JSONArray();
                    for(int i=0;i<arr.length();i++){
                        try {
                            if(dIconObj!=null) {
                                if (dIconObj.has(arr.getJSONObject(i).getString("id"))) {
                                    arr.getJSONObject(i).put("symbolType", dIconObj.getJSONObject(arr.getJSONObject(i).getString("id")).get("symbolType"));
                                    arr.getJSONObject(i).put("symbolName", dIconObj.getJSONObject(arr.getJSONObject(i).getString("id")).get("name"));
                                }
                            }
                        }catch(Exception es){es.printStackTrace();}
                        String cat_code = arr.getJSONObject(i).getString("cat_code");
                        //Log.e(TAG, "device: "+cat_code+" : "+arr.getJSONObject(i).getString("name"));
                        if(cat_code.contentEquals("sen") || cat_code.contentEquals("mtr")){
                            sArr.put(arr.getJSONObject(i));
                        }else {
                            dArr.put(arr.getJSONObject(i));
                        }
                    }
                    if(dArr.length()>0) {
                        deviceNameView.setVisibility(View.VISIBLE);
                        deviceGridView.setVisibility(View.VISIBLE);
                        deviceGridView.setData(dArr);
                        ((LinearLayout.LayoutParams)deviceNameView.getLayoutParams()).bottomMargin = ResolutionHandler.getPaddingH()/2;
                        ((LinearLayout.LayoutParams)deviceGridView.getLayoutParams()).bottomMargin = ResolutionHandler.getPaddingH()/2;
                    }
                    if(sArr.length()>0){
                        sensorNameView.setVisibility(View.VISIBLE);
                        sensorGridView.setVisibility(View.VISIBLE);
                        sensorGridView.setData(sArr);
                        ((LinearLayout.LayoutParams)sensorNameView.getLayoutParams()).bottomMargin = ResolutionHandler.getPaddingH()/2;
                        ((LinearLayout.LayoutParams)sensorGridView.getLayoutParams()).bottomMargin = ResolutionHandler.getPaddingH()/2;
                    }
                    detectH();
                    listener.onDeviceLoaded(arr);
                }catch(Exception e){e.printStackTrace();}
            }
        });
    }

    public boolean updateStatus(String did, String gp, String cid, String v, long t){
        //Log.e(TAG, "updateStatus: "+did+" : "+gp+" : "+cid+" : "+v);
        if(!deviceGridView.updateStatus(did, gp, cid, v, t)) {
            return sensorGridView.updateStatus(did, gp, cid, v, t);
        }
        return true;
    }
    public void loadBgImg(){
        if(true) return;
        try{
            if(dataObj.has("img") && !dataObj.getString("img").contentEquals("")) {
                Bitmap b = Cache.getBitmap(c, dataObj.getString("img"), "room/thu", ResolutionHandler.getViewPortW());
                iv_bg.setImageBitmap(b);
                //Log.e(TAG, "color: "+Integer.toHexString(iv_bg.getBottomAvgColor()));
                int color = iv_bg.getBottomAvgColor();
                gv_bg.setColor(color);
                listener.onBackgroundColor((Integer)this.getTag(), color);
            }
        }catch(Exception e){e.printStackTrace();}
    }
    public void setBgEffect(float y){
        if(iv_bg==null) return;
        float newH = bgH;
        float newY = 0;
        if(y>0){
            newH = bgH+y;
            newY = y;
        }
        //iv_bg.setScaleX(newH/bgH);
        //iv_bg.setScaleY(newH/bgH);
        //gv_bg.setScaleY(newH/bgH);
        ll_main.setY(newY);
    }
    public float getMainLayoutY(){
        return ll_main.getY();
    }

    public void onTap(float x, float y){
        if(x<ResolutionHandler.getPaddingW()) return;
        if(x>W-ResolutionHandler.getPaddingW()) return;

        //Log.e(TAG, "onTap: "+x+" : "+y);
        Rect r = new Rect();
        sensorGridView.getDrawingRect(r);
        ll_main.offsetDescendantRectToMyCoords(sensorGridView, r);
        if(y>r.top && y<r.top+getSensorGridViewH()){
            JSONObject obj = sensorGridView.getGridDataByLocation(x-sensorGridView.getX()-ResolutionHandler.getPaddingW(), y-r.top);
            if(obj!=null){
                listener.onTagDevice(obj);
            }
            return;
        }

        r = new Rect();
        deviceGridView.getDrawingRect(r);
        ll_main.offsetDescendantRectToMyCoords(deviceGridView, r);
        if(y>r.top){
            JSONObject obj = deviceGridView.getGridDataByLocation(x-deviceGridView.getX()-ResolutionHandler.getPaddingW(), y-r.top);
            if(obj!=null){
                listener.onTagDevice(obj);
            }
            return;
        }

        r = new Rect();
        sceneGridView.getDrawingRect(r);
        ll_main.offsetDescendantRectToMyCoords(sceneGridView, r);
        if(y>r.top && y<r.top+getSceneGridViewH()){
            JSONObject obj = sceneGridView.getGridDataByLocation(x-sceneGridView.getX()-ResolutionHandler.getPaddingW(), y-r.top);
            if(obj!=null){
                listener.onTagScene(obj);
            }
            return;
        }
    }

    public boolean onSCend(String sid){
        return sceneGridView.onSCend(sid);
    }
    public boolean onSC(String sid){
        return sceneGridView.onSC(sid);
    }

    public void end(){
        is_run = false;
    }
}
