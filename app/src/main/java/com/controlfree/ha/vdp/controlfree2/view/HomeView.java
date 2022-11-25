package com.controlfree.ha.vdp.controlfree2.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.controlfree.ha.vdp.controlfree2.R;
import com.controlfree.ha.vdp.controlfree2.component.ExImageView;
import com.controlfree.ha.vdp.controlfree2.component.ExShortLine;
import com.controlfree.ha.vdp.controlfree2.component.ExTextView;
import com.controlfree.ha.vdp.controlfree2.component.ExWeatherView;
import com.controlfree.ha.vdp.controlfree2.utils.Api;
import com.controlfree.ha.vdp.controlfree2.utils.Cache;
import com.controlfree.ha.vdp.controlfree2.utils.DB;
import com.controlfree.ha.vdp.controlfree2.utils.FileHandler;
import com.controlfree.ha.vdp.controlfree2.utils.ResolutionHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

public class HomeView extends FrameLayout {
    private final static String TAG = "HomeView";
    private Context c;
    private ExTextView v_title;
    private List<RoomPageView> pageList = new LinkedList<RoomPageView>();
    private List<View> pageBgList = new LinkedList<View>();
    private int W,H;
    private FrameLayout f_container, pbg_container;
    private boolean is_run = true, isAnimating = false;
    private int mode = 0, direction = 0;
    private PointF containerPt = new PointF(), pt = new PointF(), newPt = new PointF();
    private long touchTime = 0;
    public boolean isLoading = false;

    private Listener listener;
    public static interface Listener{
        public void onDeviceLoaded(JSONArray dArr);
        public void downloadImg(String url);
        public void sendCmd(String msg);
        public float getStatusBarH();
        public JSONObject getDeviceStatus(String id);
    }

    public HomeView(Context context, Listener l){
        super(context);
        this.c = context;
        this.listener = l;
        this.W = ResolutionHandler.getViewPortW();
        this.H = ResolutionHandler.getViewPortH();
        FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(W, H);
        setLayoutParams(p);
        //setClipBounds(new Rect(0, 0, W, H));
        //setBackgroundColor(0xff323334);

        FrameLayout.LayoutParams p_pbg_container = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        pbg_container = new FrameLayout(c);
        //pbg_container.setBackgroundColor(0x220000ff);
        //addView(pbg_container, p_pbg_container);

        FrameLayout.LayoutParams p_f_container = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        f_container = new FrameLayout(c);
        addView(f_container, p_f_container);
        f_container.setY(0);

        FrameLayout.LayoutParams p_f_touch = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        FrameLayout f_touch = new FrameLayout(c);
        //f_touch.setBackgroundColor(0x5500ff00);
        f_touch.setLongClickable(true);
        f_touch.setOnTouchListener(new OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent me) {
                if(me.getPointerCount()==1) {
                    if (me.getAction() == MotionEvent.ACTION_DOWN) {
                        //Log.e(TAG, "ACTION_DOWN");
                        int index = getPageIndex();
                        if(index>=0 && index<pageList.size()) {
                            mode = 1;
                            direction = 0;
                            containerPt = new PointF(f_container.getX(), f_container.getY());
                            pt = new PointF(me.getX(), me.getY());
                            newPt = new PointF(me.getX(), me.getY());
                            touchTime = System.currentTimeMillis();
                            //Log.e(TAG, "onTouch: init: containerPt: "+containerPt.y+", pt: "+pt.y);
                        }
                    } else if (me.getAction() == MotionEvent.ACTION_UP) {
                        //Log.e(TAG, "ACTION_UP");
                        int index = getPageIndex();
                        if(index>=0 && index<pageList.size()) {
                            if (mode == 2) {
                                pageList.get(index).mouseUpSceneGridView(newPt.x);
                                direction = 0;
                            } else if (mode == 3) {
                                pageList.get(index).mouseUpSensorGridView(newPt.x);
                                direction = 0;
                            }
                            if (System.currentTimeMillis() - touchTime < 1000) {
                                float d = ResolutionHandler.getW(0.05f);
                                if(ResolutionHandler.getH(0.05f)>d) d = ResolutionHandler.getH(0.05f);
                                if (Math.abs(me.getX() - pt.x) < d && Math.abs(me.getY() - pt.y) < d) {
                                    onTap(me.getX(), me.getY());
                                }
                            }
                        }
                        mode = 0;

                    } else if (me.getAction() == MotionEvent.ACTION_MOVE) {
                        newPt = new PointF(me.getX(), me.getY());
                        //Log.e(TAG, "onTouch: move: newPt: "+newPt.y);
                        if (direction == 0) {
                            if (pt.x == newPt.x && pt.y == newPt.y) return false;
                            //Log.e(TAG, "onTouch: x: "+pt.x+" -> "+newPt.x);
                            //Log.e(TAG, "onTouch: y: "+pt.y+" -> "+newPt.y);
                            if (Math.abs(newPt.y - pt.y) < Math.abs(newPt.x - pt.x)) {
                                int index = getPageIndex();
                                if(index>=0 && index<pageList.size()) {
                                    direction = 1; //hor

                                    //scene
                                    float y = pageList.get(index).getSceneGridViewY();
                                    //Log.e(TAG, "setOnTouchListener: "+y+" / "+me.getY());
                                    if (me.getY() >= y + f_container.getY() && me.getY() < y + f_container.getY() + pageList.get(index).getSceneGridViewH()) {
                                        mode = 2;
                                        pageList.get(index).mouseDownSceneGridView(newPt.x);
                                        return false;
                                    }

                                    //sensor
                                    y = pageList.get(index).getSensorGridViewY();
                                    //Log.e(TAG, "setOnTouchListener: "+y+" / "+me.getY());
                                    if (me.getY() >= y + f_container.getY() && me.getY() < y + f_container.getY() + pageList.get(index).getSensorGridViewH()) {
                                        mode = 3;
                                        pageList.get(index).mouseDownSensorGridView(newPt.x);
                                        return false;
                                    }

                                    //align page to top
                                    pageList.get(index).setY(f_container.getY());
                                    f_container.setY(0);
                                    if (index - 1 >= 0){
                                        pageList.get(index - 1).setY(0);
                                    }
                                    if (index + 1 <pageList.size()){
                                        pageList.get(index + 1).setY(0);
                                    }
                                }
                            } else {
                                direction = 2; //ver
                            }
                        }
                    }else{
                        //Log.e(TAG, "MotionEvent: "+me.getAction());
                    }
                }
                return false;
            }
        });
        addView(f_touch, p_f_touch);

        DB db = new DB(c);
        //JSONArray arr = db.getRoomList(DB.getSetting(c, "gid"));
        JSONArray arr = Cache.getArr(c, "room_list");
        db.close();
        setPage(arr);

        loop();
    }

    public boolean isDataLoading(){
        if(isLoading) return true;
        try{
            for(int i=0;i<pageList.size();i++){
                if(pageList.get(i).isLoading) return true;
            }
        }catch(Exception e){}
        return false;
    }
    private int getContainerH(){
        f_container.measure(0, 0);
        return f_container.getMeasuredHeight();
    }
    private void onTap(float x, float y){
        try{
            int index = getPageIndex();
            //Log.e(TAG, "onTap: "+x+" : "+y+" / "+index);
            if(index>=0 && index<pageList.size()){
                pageList.get(index).onTap(0-f_container.getX()+x-index*W, 0-f_container.getY()+y);
            }
        }catch(Exception e){e.printStackTrace();}
    }

    private void loop(){
        if(is_run){
            //touch ------------
            try{
                if (mode == 1) {
                    if (direction != 0) {
                        float newX = containerPt.x + (newPt.x - pt.x);
                        float newY = containerPt.y + (newPt.y - pt.y);
                        if (direction == 1) {
                            f_container.setX(f_container.getX() + (newX - f_container.getX()) * 0.5f);
                            //pbg_container.setX(f_container.getX());
                        }
                        if (direction == 2) {
                            f_container.setY(newY);
                            //f_container.setY(f_container.getY() + (newY - f_container.getY()) * 0.5f);
                            if(f_container.getY()>0) f_container.setY(0);
                            //Log.e(TAG, "onTouch: loop: f_container: "+f_container.getY()+", newY: "+newY);
                            resizeBgImg(newY<0?0:newY);
                        }
                    }
                } else if (mode == 2) {
                    int index = getPageIndex();
                    if(index>=0 && index<pageList.size()) {
                        pageList.get(index).mouseMoveSceneGridView(newPt.x);
                    }
                } else if (mode == 3) {
                    int index = getPageIndex();
                    if(index>=0 && index<pageList.size()) {
                        pageList.get(index).mouseMoveSensorGridView(newPt.x);
                    }
                } else { //MOUSE_UP
                    if (direction == 1) {
                        float newX = f_container.getX() / (float) W;
                        if (newPt.x < pt.x) {
                            if (newX - Math.floor(newX) < 0.9) {
                                newX = (float) Math.floor(newX);
                            } else {
                                newX = (float) Math.ceil(newX);
                            }
                        } else {
                            if (newX - Math.floor(newX) < 0.1) {
                                newX = (float) Math.floor(newX);
                            } else {
                                newX = (float) Math.ceil(newX);
                            }
                        }
                        newX = (float) W * newX;
                        if (newX < 0 - f_container.getWidth() + ResolutionHandler.getViewPortW()) {
                            newX = 0 - f_container.getWidth() + ResolutionHandler.getViewPortW();
                        } else if (newX > 0) {
                            newX = 0;
                        }
                        f_container.setX(f_container.getX() + (newX - f_container.getX()) * 0.5f);
                        if (Math.abs(f_container.getX() - newX) < 1) {
                            direction = 0;
                            int index = getPageIndex();
                            if(index>=0 && index<pageList.size()) {
                                f_container.setY(pageList.get(index).getY());
                                pageList.get(index).setY(0);
                            }
                            onPageAnimationEnd();
                        }
                        //pbg_container.setX(f_container.getX());
                    } else if (direction == 2) {
                        int index = getPageIndex();
                        if(index>=0 && index<pageList.size()) {
                            float newY = f_container.getY();
                            if(newY<0) {
                                if (newY > 0 - pageList.get(index).getY()) {
                                    newY = 0 - pageList.get(index).getY();
                                } else {
                                    Log.e(TAG, "loop: test: "+newY +" + "+pageList.get(index).getHeight()+" : "+pageList.get(index).getLayoutParams().height);
                                    if ((newY + pageList.get(index).getHeight()) < (ResolutionHandler.getViewPortH())) {
                                        newY = (ResolutionHandler.getViewPortH()) - pageList.get(index).getHeight();
                                    }
                                    Log.e(TAG, "loop: test2: "+newY);
                                }
                                f_container.setY(f_container.getY() + (newY - f_container.getY()) * 0.3f);
                                if (Math.abs(f_container.getY() - newY) < 1) {
                                    direction = 0;
                                }
                            }else if(newY==0) {
                                f_container.setY(0);
                                newY = pageList.get(index).getMainLayoutY();
                                newY = newY + (0-newY)*0.3f;
                                resizeBgImg(newY);
                                if (Math.abs(pageList.get(index).getMainLayoutY()) < 1) {
                                    direction = 0;
                                }
                            }
                        }
                    }
                }
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

    private void resizeBgImg(float y){
        try{
            RoomPageView p = pageList.get(getPageIndex());
            if(p!=null){
                p.setBgEffect(y);
            }
        }catch(Exception e){e.printStackTrace();}
    }

    private void setPage(JSONArray arr){
        String room_id = "";
        try{
            String rm_id = DB.getSetting(c, "room_id");
            for(int i=0;i<arr.length();i++){
                if(arr.getJSONObject(i).getString("id").contentEquals(rm_id)){
                    room_id = rm_id;
                }
            }
        }catch(Exception e){e.printStackTrace();}

        int index = -1, count = 0;
        for(int i=0;i<arr.length();i++){
//            FrameLayout.LayoutParams p_pbg = new FrameLayout.LayoutParams(ResolutionHandler.getViewPortW(), ResolutionHandler.getViewPortH());
//            View pbg = new View(c);
//            p_pbg.leftMargin = count*W;
//            pbg_container.addView(pbg, p_pbg);
//            pageBgList.add(pbg);

            try{
                if(arr.getJSONObject(i).getString("name").startsWith("_")){
                    continue;
                }
                if(room_id.contentEquals("")){
                    room_id = arr.getJSONObject(i).getString("id");
                    DB.setSetting(c, "room_id", room_id);
                }
                if(arr.getJSONObject(i).getString("id").contentEquals(room_id)){
                    index = count;
                }
            }catch(Exception e){e.printStackTrace();}
            RoomPageView v = new RoomPageView(c, new RoomPageView.Listener(){
                @Override
                public void onHeightChange() {
                    ((FrameLayout.LayoutParams)f_container.getLayoutParams()).height = getContainerH();
                }
                @Override
                public void onDeviceLoaded(JSONArray arr) {
                    listener.onDeviceLoaded(arr);
                }
                @Override
                public void onTagDevice(JSONObject obj) {
                    genDeviceControl(obj);
                }
                @Override
                public void onTagScene(JSONObject obj) {
                    try{
                        listener.sendCmd("SC"+obj.getString("id"));
                    }catch(Exception e){e.printStackTrace();}
                }
                @Override
                public void onBackgroundColor(int tag, int color) {
                    try{
                        //if(pageBgList.get(tag)==null) return;
                        //pageBgList.get(tag).setBackgroundColor(color);
                    }catch(Exception e){e.printStackTrace();}
                }
            });
            v.setTag(i);
            try {
                v.setData(arr.getJSONObject(i));
            }catch(Exception e){e.printStackTrace();}
            ((LayoutParams)v.getLayoutParams()).leftMargin = count*W;
            pageList.add(v);
            f_container.addView(v);

            try{
                if(arr.getJSONObject(i).has("img") && !arr.getJSONObject(i).getString("img").contentEquals("")) {
                    listener.downloadImg(arr.getJSONObject(i).getString("img"));
                }
            }catch(Exception e){e.printStackTrace();}

            count++;
        }
        ((FrameLayout.LayoutParams)f_container.getLayoutParams()).width = count*W;
        //((FrameLayout.LayoutParams)pbg_container.getLayoutParams()).width = count*W;
        showPageByIndex(index);
    }

    public void showPageByIndex(int index){
        f_container.setX(0-W*index);
        //pbg_container.setX(f_container.getX());
        onPageAnimationEnd();
    }
    private int getPageIndex(){
        return (int)Math.round(Math.abs(f_container.getX())/(double)W);
    }

    private void onPageAnimationEnd(){
        try{
            int index = getPageIndex();
            //Log.e(TAG, "onPageAnimationEnd: "+index+" / "+pageList.size());
            if(index>=0 && index<pageList.size()){
                //Log.e(TAG, "onPageAnimationEnd: "+pageList.get(index).getData().getString("name"));
                DB.setSetting(c, "room_id", pageList.get(index).getData().getString("id"));
            }
        }catch(Exception e){e.printStackTrace();}
    }

    public void end(){
        is_run = false;
        for(int i=0;i<pageList.size();i++){
            pageList.get(i).end();
        }
    }









    //-------------------
    public boolean updateStatus(String did, String gp, String cid, String v, long t){
        //Log.e(TAG, "updateStatus");
        //String dcv_did = "";
        if(dcv!=null){
            dcv.updateStatus(did, gp, cid, v, t);
//            try{
//                dcv_did = dcv.getData().getString("id");
//            }catch(Exception e){e.printStackTrace();}
        }

        for(int i=0;i<pageList.size();i++){
            //if(dcv_did.contentEquals(did)) continue;
            if(pageList.get(i).updateStatus(did, gp, cid, v, t)){
                return true;
            }
        }
        return false;
    }
    public void onSCend(String sid){
        for(int i=0;i<pageList.size();i++){
            if(pageList.get(i).onSCend(sid)){
                return;
            }
        }
    }
    public void onSC(String sid){
        for(int i=0;i<pageList.size();i++){
            if(pageList.get(i).onSC(sid)){
                return;
            }
        }
    }
    public void onImgDownloaded(String url){
        for(int i=0;i<pageList.size();i++){
            try{
                JSONObject obj = pageList.get(i).getData();
                //Log.e(TAG, "onImgDownloaded: "+obj.getString("img")+" / "+url);
                if(obj.has("img") && obj.getString("img").contentEquals(url)){
                    pageList.get(i).loadBgImg();
                    break;
                }
            }catch(Exception e){e.printStackTrace();}
        }
    }


    DeviceControlView dcv;
    int dcv_dir = 0;
    boolean isDcvAnimating = false;
    private DeviceControlView.Listener listener_dcv = new DeviceControlView.Listener() {
        @Override
        public void onClick_closeBtn() {
            dcv_dir = -1;
            if (!isDcvAnimating) animateDeviceControlView();

        }
        @Override
        public void initStatus(JSONObject obj) {
            try{
                JSONObject sObj = listener.getDeviceStatus(obj.getString("id"));
                if(sObj!=null){
                    JSONArray arr = sObj.names();
                    if(arr!=null) {
                        for (int i = 0; i < arr.length(); i++) {
                            if(arr.getString(i).startsWith("_")) continue;
                            JSONObject statusObj = sObj.getJSONObject(arr.getString(i));
                            //Log.e("TAG", obj.getString("id")+" : "+arr.getString(i)+", status: "+statusObj);
                            dcv.updateStatus(obj.getString("id"), arr.getString(i), statusObj.getString("cid"), statusObj.getString("v"), statusObj.getLong("t"));
                        }
                    }
                }
            }catch(Exception e){e.printStackTrace();}
        }
        @Override
        public void sendCommand(String msg) {
            listener.sendCmd(msg);
        }
    };
    private void genDeviceControl(JSONObject obj){
        try{
            if(dcv==null) {
                boolean is_ac = (obj.getString("cat_code").contentEquals("ac"));
                DeviceControlView v = (is_ac?new TempAcDeviceControlView(c, ResolutionHandler.getViewPortW()-30, ResolutionHandler.getViewPortH(), listener_dcv)
                        :new DeviceControlView(c, ResolutionHandler.getViewPortW()-30, ResolutionHandler.getViewPortH(), listener_dcv));
                v.setAlpha(0f);
                v.setX(15);
                v.setY(ResolutionHandler.getViewPortH());
                v.setElevation(20);
                dcv = v;
            }
            if(dcv.getParent()==null) addView(dcv);
            dcv.setData(obj);

            dcv_dir = 1;
            if(!isDcvAnimating) animateDeviceControlView();
        }catch(Exception e){e.printStackTrace();}
    }
    private void animateDeviceControlView(){
        //Log.e(TAG, "animateDeviceControlView: "+dcv_dir+" / "+dcv.getAlpha());
        if(dcv_dir==1 && dcv.getAlpha()<1) {
            isDcvAnimating = true;
            dcv.setY(dcv.getY()+(15f-dcv.getY())*0.5f);
            dcv.setAlpha(dcv.getAlpha()+0.1f);
            postDelayed(new Runnable(){
                @Override
                public void run() {
                    animateDeviceControlView();
                }
            }, 30);
        }else if(dcv_dir==-1 && dcv.getAlpha()>0){
            isDcvAnimating = true;
            dcv.setY(dcv.getY()+dcv.getY()*0.5f+10f);
            dcv.setAlpha(dcv.getAlpha()-0.1f);
            postDelayed(new Runnable(){
                @Override
                public void run() {
                    animateDeviceControlView();
                }
            }, 30);
        }else{
            //Log.e(TAG, "animateDeviceControlView: end");
            isDcvAnimating = false;
            if(dcv_dir==1){
                dcv.setY(15);
                dcv.setAlpha(1);
            }else if(dcv_dir==-1){
                dcv.setAlpha(0);
                removeView(dcv);
                dcv = null;
            }
            dcv_dir = 0;
        }
    }

}
