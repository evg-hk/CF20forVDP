package com.controlfree.ha.vdp.controlfree2.view;

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

import com.controlfree.ha.vdp.controlfree2.R;
import com.controlfree.ha.vdp.controlfree2.component.ExImageView;
import com.controlfree.ha.vdp.controlfree2.component.ExShortLine;
import com.controlfree.ha.vdp.controlfree2.component.ExTextView;
import com.controlfree.ha.vdp.controlfree2.utils.Api;
import com.controlfree.ha.vdp.controlfree2.utils.Cache;
import com.controlfree.ha.vdp.controlfree2.utils.DB;
import com.controlfree.ha.vdp.controlfree2.utils.Fun;
import com.controlfree.ha.vdp.controlfree2.utils.ResolutionHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

public class SceneView extends FrameLayout {
    private final static String TAG = "SceneView";
    private Context c;
    private int W,H;
    private FrameLayout f_container;
    private boolean is_run = true;
    private int mode = 0, direction = 0;
    private PointF containerPt = new PointF(), pt = new PointF(), newPt = new PointF();
    private long touchTime = 0;
    private SceneGridView sceneGridView;

    private Listener listener;
    public static interface Listener{
        public void sendCmd(String msg);
    }

    public SceneView(Context context, Listener l){
        super(context);
        this.c = context;
        this.listener = l;
        this.W = ResolutionHandler.getViewPortW();
        this.H = ResolutionHandler.getViewPortH();
        FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(W, H);
        setLayoutParams(p);

        FrameLayout.LayoutParams p_f_container = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        f_container = new FrameLayout(c);
        addView(f_container, p_f_container);

        FrameLayout.LayoutParams p_f_touch = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        FrameLayout f_touch = new FrameLayout(c);
        //f_touch.setBackgroundColor(0x5500ff00);
        f_touch.setLongClickable(true);
        f_touch.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent me) {
                if(me.getPointerCount()==1) {
                    if (me.getAction() == MotionEvent.ACTION_DOWN) {
                        //Log.e(TAG, "ACTION_DOWN");
                        mode = 1;
                        containerPt = new PointF(f_container.getX(), f_container.getY());
                        pt = new PointF(me.getX(), me.getY());
                        newPt = new PointF(me.getX(), me.getY());
                        touchTime = System.currentTimeMillis();
                    } else if (me.getAction() == MotionEvent.ACTION_UP) {
                        //Log.e(TAG, "ACTION_UP");
                        mode = 0;
                        //tap
                        if(System.currentTimeMillis()-touchTime<1000){
                            float d = ResolutionHandler.getW(0.05f);
                            if(ResolutionHandler.getH(0.05f)>d) d = ResolutionHandler.getH(0.05f);
                            if(Math.abs(me.getX()-pt.x)<d && Math.abs(me.getY()-pt.y)<d) {
                                onTap(me.getX(), me.getY());
                            }
                        }
                    } else if (me.getAction() == MotionEvent.ACTION_MOVE) {
                        newPt = new PointF(me.getX(), me.getY());
                        if (direction == 0) {
                            if (pt.x == newPt.x && pt.y == newPt.y) return false;
                            direction = 2;
                        }
                    }else{
                        //Log.e(TAG, "MotionEvent: "+me.getAction());
                    }
                }
                return false;
            }
        });
        addView(f_touch, p_f_touch);

        genLayout();
        loop();
    }

    private void genLayout(){
        FrameLayout.LayoutParams p_ll = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        p_ll.setMargins(ResolutionHandler.getPaddingW(), 20, ResolutionHandler.getPaddingW(), 20);
        LinearLayout ll = new LinearLayout(c);
        ll.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams p_f_ll_top = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        FrameLayout f_ll_top = new FrameLayout(c);

        FrameLayout.LayoutParams p_ll_top = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        p_ll_top.gravity = Gravity.BOTTOM;
        LinearLayout ll_top = new LinearLayout(c);
        ll_top.setOrientation(LinearLayout.VERTICAL);

        ExTextView title = new ExTextView(c, "Scene");
        title.setTextColor(0xffffffff);
        title.setTextSize(ResolutionHandler.fontsize_large);
        title.setTypeface(null, Typeface.BOLD);
        //((LinearLayout.LayoutParams)title.getLayoutParams()).width = LinearLayout.LayoutParams.WRAP_CONTENT;
        ll_top.addView(title);


        f_ll_top.addView(ll_top, p_ll_top);
        ll.addView(f_ll_top, p_f_ll_top);
        //---------

        ExShortLine line = new ExShortLine(c);
        ll.addView(line);

        SceneGridView sgv = new SceneGridView(c, ResolutionHandler.getViewPortW()-ResolutionHandler.getPaddingW()*2, new SceneGridView.Listener(){
            @Override
            public void onHeightChange() {
                ((FrameLayout.LayoutParams)f_container.getLayoutParams()).height = getContainerH();
            }
        });
        ll.addView(sgv);
        sceneGridView = sgv;

        f_container.addView(ll, p_ll);

        FrameLayout.LayoutParams p_btn = new FrameLayout.LayoutParams(ResolutionHandler.getBtnW(), ResolutionHandler.getBtnW());
        p_btn.gravity = Gravity.RIGHT;
        p_btn.rightMargin = ResolutionHandler.getPaddingW();
        p_btn.topMargin = (ResolutionHandler.getH(0.1f)-ResolutionHandler.getBtnW())/2;
        ExImageView btn = new ExImageView(c,  R.drawable.btn_add, ResolutionHandler.getBtnW(), ResolutionHandler.getBtnW());
        btn.setVisibility(View.GONE);
        f_container.addView(btn, p_btn);

        f_container.measure(0, 0);
        if(f_container.getMeasuredHeight()<ResolutionHandler.getViewPortH()-ResolutionHandler.getBottomTabH()){
            ((FrameLayout.LayoutParams) f_container.getLayoutParams()).height = ResolutionHandler.getViewPortH()-ResolutionHandler.getBottomTabH();
        }else {
            ((FrameLayout.LayoutParams) f_container.getLayoutParams()).height = f_container.getMeasuredHeight();
        }

        JSONArray arr = Cache.getArr(c, "automation_list");
        sceneGridView.setData(arr);
    }
    private int getContainerH(){
        f_container.measure(0, 0);
        return f_container.getMeasuredHeight();
    }

    private void onTap(float x, float y){
        try{
            //Log.e(TAG, "onTap: "+x+" : "+y);
            y = 0-f_container.getY()+y;
            Rect r = new Rect();
            sceneGridView.getDrawingRect(r);
            this.offsetDescendantRectToMyCoords(sceneGridView, r);
            //Log.e(TAG, "onTap: rect: "+r.left+" : "+r.top+" : "+r.right+" : "+r.bottom);
            if(y>r.top){
                JSONObject obj = sceneGridView.getGridDataByLocation(x-sceneGridView.getX()-ResolutionHandler.getPaddingW(), y-r.top);
                if(obj!=null){
                    //Log.e(TAG, "onTap: "+x+" : "+y+" -> "+obj.getString("name")+" - > "+obj.getString("id"));
                    listener.sendCmd("SC"+obj.getString("id"));
                }
            }
        }catch(Exception e){e.printStackTrace();}
    }

    private void loop(){
        if(is_run){
            try{
                if(mode==1){
                    if(direction!=0) {
                        float newY = containerPt.y + (newPt.y - pt.y);
                        if (direction == 2)
                            f_container.setY(f_container.getY() + (newY - f_container.getY()) * 0.5f);
                    }
                }else{
                    if(direction==2){
                        float newY = f_container.getY();
                        //Log.e(TAG, "newY: "+newY+" / "+(0-f_container.getHeight()-ResolutionHandler.getBottomTabH()));
                        if(newY>0) {
                            newY = 0;
                        }else if(newY<(ResolutionHandler.getViewPortH()-ResolutionHandler.getBottomTabH())-f_container.getHeight()){
                            newY = (ResolutionHandler.getViewPortH()-ResolutionHandler.getBottomTabH())-f_container.getHeight();
                            //Log.e(TAG, "f_container.getHeight(): "+f_container.getHeight()+" / "+newY);
                        }
                        f_container.setY(f_container.getY() + (newY - f_container.getY()) * 0.3f);
                        if(Math.abs(f_container.getY()-newY)<1){
                            direction = 0;
                        }
                    }
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

    public void onSCend(String sid){
        if(sceneGridView.onSCend(sid)){
            return;
        }
    }
    public void onSC(String sid){
        if(sceneGridView.onSC(sid)){
            return;
        }
    }


    public void end(){
        is_run = false;
    }




}
