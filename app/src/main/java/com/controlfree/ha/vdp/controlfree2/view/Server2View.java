package com.controlfree.ha.vdp.controlfree2.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PointF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.controlfree.ha.vdp.controlfree2.component.ExTextView;
import com.controlfree.ha.vdp.controlfree2.utils.Cache;
import com.controlfree.ha.vdp.controlfree2.utils.DB;
import com.controlfree.ha.vdp.controlfree2.utils.ResolutionHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

public class Server2View extends FrameLayout {
    private final static String TAG = "ServerView";
    private Context c;
    private ExTextView v_title;
    private ServerPageView page;
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

    public Server2View(Context context, Listener l){
        super(context);
        this.c = context;
        this.listener = l;
        this.W = ResolutionHandler.getViewPortW();
        this.H = ResolutionHandler.getViewPortH();
        FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(W, H);
        setLayoutParams(p);
        //setClipBounds(new Rect(0, 0, W, H));
        //setBackgroundColor(0xff646668);

        FrameLayout.LayoutParams p_pbg_container = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        pbg_container = new FrameLayout(c);
        //pbg_container.setBackgroundColor(0x220000ff);
        //addView(pbg_container, p_pbg_container);

        FrameLayout.LayoutParams p_f_container = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        f_container = new FrameLayout(c);
        addView(f_container, p_f_container);

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
                        mode = 1;
                        direction = 0;
                        containerPt = new PointF(f_container.getX(), f_container.getY());
                        pt = new PointF(me.getX(), me.getY());
                        newPt = new PointF(me.getX(), me.getY());
                        touchTime = System.currentTimeMillis();

//                        float y = page.getSceneGridViewY();
//                        if (newPt.y >= y + f_container.getY() && newPt.y < y + f_container.getY() + page.getSceneGridViewH()) {
//                            page.mouseDownSceneGridView(pt.x);
//                        }
//                        y = page.getSensorGridViewY();
//                        if (newPt.y >= y + f_container.getY() && newPt.y < y + f_container.getY() + page.getSensorGridViewH()) {
//                            page.mouseDownSensorGridView(pt.x);
//                        }
                    } else if (me.getAction() == MotionEvent.ACTION_UP) {
                        //Log.e(TAG, "ACTION_UP");
                        if (mode == 2) {
                            page.mouseUpSceneGridView(newPt.x);
                            direction = 0;
                        } else if (mode == 3) {
                            page.mouseUpSensorGridView(newPt.x);
                            direction = 0;
                        }
                        if (System.currentTimeMillis() - touchTime < 1000) {
                            float d = ResolutionHandler.getW(0.05f);
                            if(ResolutionHandler.getH(0.05f)>d) d = ResolutionHandler.getH(0.05f);
                            if (Math.abs(me.getX() - pt.x) < d && Math.abs(me.getY() - pt.y) < d) {
                                onTap(me.getX(), me.getY());
                            }
                        }
                        mode = 0;

                    } else if (me.getAction() == MotionEvent.ACTION_MOVE) {
                        newPt = new PointF(me.getX(), me.getY());
                        if (direction == 0) {
                            if (pt.x == newPt.x && pt.y == newPt.y) return false;
                            if (Math.abs(newPt.y - pt.y) < Math.abs(newPt.x - pt.x)) {
                                direction = 1; //hor

                                //scene
                                float y = page.getSceneGridViewY();
                                if (me.getY() >= y + f_container.getY() && me.getY() < y + f_container.getY() + page.getSceneGridViewH()) {
                                    mode = 2;
                                    page.mouseDownSceneGridView(newPt.x);
                                    return false;
                                }

                                //sensor
                                y = page.getSensorGridViewY();
                                if (me.getY() >= y + f_container.getY() && me.getY() < y + f_container.getY() + page.getSensorGridViewH()) {
                                    mode = 3;
                                    page.mouseDownSensorGridView(newPt.x);
                                    return false;
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
        JSONObject dataObj = Cache.getServer(c);;
        db.close();
        setPage(dataObj);

        loop();
    }
    public boolean isDataLoading(){
        if(isLoading) return true;
        try{
            if(page!=null){
                return page.isLoading;
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
            page.onTap(0-f_container.getX()+x, 0-f_container.getY()+y);
        }catch(Exception e){e.printStackTrace();}
    }

    private void loop(){
        if(is_run){
            try{
                if (mode == 1) {
                    if (direction != 0) {
                        float newX = containerPt.x + (newPt.x - pt.x);
                        float newY = containerPt.y + (newPt.y - pt.y);
                        if (direction == 1)
                            f_container.setX(f_container.getX() + (newX - f_container.getX()) * 0.5f);
                        if (direction == 2) {
                            f_container.setY(f_container.getY() + (newY - f_container.getY()) * 0.5f);
                            if(f_container.getY()>0) f_container.setY(0);
                            resizeBgImg(newY);
                        }
                    }
                } else if (mode == 2) {
                    page.mouseMoveSceneGridView(newPt.x);
                } else if (mode == 3) {
                    page.mouseMoveSensorGridView(newPt.x);
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
                            onPageAnimationEnd();
                        }
                    } else if (direction == 2) {
                        float newY = f_container.getY();
                        if(newY<0) {
                            if (newY > 0 - page.getY()) {
                                newY = 0 - page.getY();
                            } else{
//                                if ((newY+page.getHeight()) < (ResolutionHandler.getViewPortH() - ResolutionHandler.getBottomTabH())) {
//                                    newY = (ResolutionHandler.getViewPortH() - ResolutionHandler.getBottomTabH()) - page.getHeight();
//                                }
                                if ((newY+page.getHeight()) < (ResolutionHandler.getViewPortH())) {
                                    newY = (ResolutionHandler.getViewPortH()) - page.getHeight();
                                }
                            }
                            f_container.setY(f_container.getY() + (newY - f_container.getY()) * 0.3f);
                            if (Math.abs(f_container.getY() - newY) < 1) {
                                direction = 0;
                            }
                        }else if(newY<=0) {
                            f_container.setY(0);
                            newY = page.getMainLayoutY();
                            newY = newY + (0-newY)*0.3f;
                            resizeBgImg(newY);
                            if (Math.abs(page.getMainLayoutY()) < 1) {
                                direction = 0;
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
            ServerPageView p = page;
            if(p!=null){
                p.setBgEffect(y);
            }
        }catch(Exception e){e.printStackTrace();}
    }

    private void setPage(JSONObject obj){
        FrameLayout.LayoutParams p_pbg = new FrameLayout.LayoutParams(ResolutionHandler.getViewPortW(), ResolutionHandler.getViewPortH());
        View pbg = new View(c);
        p_pbg.leftMargin = 0*W;
        pbg_container.addView(pbg, p_pbg);
        pageBgList.add(pbg);
        ServerPageView v = new ServerPageView(c, new ServerPageView.Listener(){
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
            public void onBackgroundColor(int color) {
                try{
                    if(pageBgList.get(0)==null) return;
                    pageBgList.get(0).setBackgroundColor(color);
                }catch(Exception e){e.printStackTrace();}
            }
        });
        try {
            v.setData(obj);
        }catch(Exception e){e.printStackTrace();}
        page = v;
        f_container.addView(v);
        v.detectH();

        try{
            if(obj.has("img") && !obj.getString("img").contentEquals("")) {
                listener.downloadImg(obj.getString("img"));
            }
        }catch(Exception e){e.printStackTrace();}

        ((FrameLayout.LayoutParams)f_container.getLayoutParams()).width = W;
    }

    private void onPageAnimationEnd(){
        try{
        }catch(Exception e){e.printStackTrace();}
    }

    public void end(){
        is_run = false;
        page.end();
    }









    //-------------------
    public boolean updateStatus(String did, String gp, String cid, String v, long t){
        //Log.e(TAG, "updateStatus");
        if(dcv!=null){
            dcv.updateStatus(did, gp, cid, v, t);
        }

        if(page.updateStatus(did, gp, cid, v, t)){
            return true;
        }
        return false;
    }
    public void onSCend(String sid){
        if(page.onSCend(sid)){
            return;
        }
    }
    public void onSC(String sid){
        if(page.onSC(sid)){
            return;
        }
    }
    public void onImgDownloaded(String url){
        try{
            JSONObject obj = page.getData();
            //Log.e(TAG, "onImgDownloaded: "+obj.getString("img")+" / "+url);
            if(obj.has("img") && obj.getString("img").contentEquals(url)){
                page.loadBgImg();
            }
        }catch(Exception e){e.printStackTrace();}
    }


    DeviceControlView dcv;
    int dcv_dir = 0;
    boolean isDcvAnimating = false;
    private void genDeviceControl(JSONObject obj){
        try{
            if(dcv==null) {
                if(obj.getString("cat_code").contentEquals("ac")) {
                    TempAcDeviceControlView v = new TempAcDeviceControlView(c, ResolutionHandler.getViewPortW() - 30, ResolutionHandler.getViewPortH(), new DeviceControlView.Listener() {
                        @Override
                        public void onClick_closeBtn() {
                            dcv_dir = -1;
                            if (!isDcvAnimating) animateDeviceControlView();

                        }
                        @Override
                        public void initStatus(JSONObject obj) {
                        }
                        @Override
                        public void sendCommand(String msg) {

                        }
                    });
                    v.setAlpha(0f);
                    v.setX(15);
                    v.setY(ResolutionHandler.getViewPortH());
                    v.setElevation(20);
                    dcv = v;

                }else{
                    DeviceControlView v = new DeviceControlView(c, ResolutionHandler.getViewPortW() - 30, ResolutionHandler.getViewPortH(), new DeviceControlView.Listener() {
                        @Override
                        public void onClick_closeBtn() {
                            dcv_dir = -1;
                            if (!isDcvAnimating) animateDeviceControlView();

                        }

                        @Override
                        public void initStatus(JSONObject obj) {
                            try {
                                JSONObject sObj = listener.getDeviceStatus(obj.getString("id"));
                                if (sObj != null) {
                                    JSONArray arr = sObj.names();
                                    if (arr != null) {
                                        for (int i = 0; i < arr.length(); i++) {
                                            if (arr.getString(i).startsWith("_")) continue;
                                            JSONObject statusObj = sObj.getJSONObject(arr.getString(i));
                                            //Log.e("TAG", obj.getString("id")+" : "+arr.getString(i)+", status: "+statusObj);
                                            dcv.updateStatus(obj.getString("id"), arr.getString(i), statusObj.getString("cid"), statusObj.getString("v"), statusObj.getLong("t"));
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void sendCommand(String msg) {
                            listener.sendCmd(msg);
                        }
                    });
                    v.setAlpha(0f);
                    v.setX(15);
                    v.setY(ResolutionHandler.getViewPortH());
                    v.setElevation(20);
                    dcv = v;
                }
            }
            if(dcv.getParent()==null) addView(dcv);
            dcv.setData(obj);

            dcv_dir = 1;
            if(!isDcvAnimating) animateDeviceControlView();
        }catch(Exception e){e.printStackTrace();}
    }
    private void animateDeviceControlView(){
        if(dcv==null) return;
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
