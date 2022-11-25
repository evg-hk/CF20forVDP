package com.controlfree.ha.vdp.controlfree2.component;

import android.content.Context;
import android.graphics.Outline;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.controlfree.ha.vdp.controlfree2.R;
import com.controlfree.ha.vdp.controlfree2.utils.DB;
import com.controlfree.ha.vdp.controlfree2.utils.ResolutionHandler;

public class ExPageNavView extends LinearLayout {
    private final static String TAG = "ExPageNavView";
    private Context c;
    private int[] resArr = {R.drawable.tab_1, R.drawable.tab_2, R.drawable.tab_3, R.drawable.tab_4};
    private int[] resOnArr = {R.drawable.tab_1_on, R.drawable.tab_2_on, R.drawable.tab_3_on, R.drawable.tab_4_on};
    private long initTime = 0;
    private ExImageView[] btnArr = new ExImageView[4];

    private Listener listener;
    public static interface Listener{
        public void onClick(int index);
    }
    public ExPageNavView(Context context, Listener l){
        super(context);
        this.c = context;
        this.listener = l;
        this.setElevation(10);
        initTime = System.currentTimeMillis();
        FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        p.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        p.bottomMargin = ResolutionHandler.getH(0.04f);
        //p.rightMargin = ResolutionHandler.getH(0.04f);
        setLayoutParams(p);
        setOrientation(LinearLayout.HORIZONTAL);
        setPadding(ResolutionHandler.getRefW(0.06f), ResolutionHandler.getRefW(0.04f), ResolutionHandler.getRefW(0.06f), ResolutionHandler.getRefW(0.04f));

        setBackgroundResource(R.drawable.nav_bg);

        int btnW = ResolutionHandler.getBtnW();
        String tab = DB.getSetting(c, "tab");
        //Log.e(TAG, "tab: "+tab);
        for(int i=0;i<resArr.length;i++) {
            int resId = 0;
            if(!tab.contentEquals(""+i)){
                resId = resArr[i];
            }else{
                resId = resOnArr[i];
            }
            ExImageView btn = new ExImageView(c, resId, btnW, btnW);
            btn.setTag(i);
            btn.setOnClickListener(onClick);
            addView(btn);
            if(i>0){
                ((LayoutParams)btn.getLayoutParams()).leftMargin = ResolutionHandler.getRefW(0.075f);
            }
            btnArr[i] = btn;
        }
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
    }
    public void setSelectedTab(int index){
        try {
            for(int i=0;i<btnArr.length;i++){
                int resId = 0;
                if (i!=index) {
                    resId = resArr[i];
                } else {
                    resId = resOnArr[i];
                }
                btnArr[i].setImg(resId);
            }
        }catch(Exception e){e.printStackTrace();}
    }
    private OnClickListener onClick = new OnClickListener(){
        @Override
        public void onClick(View view) {
            if(System.currentTimeMillis()-initTime<500) return;
            setSelectedTab((int)(view.getTag()));
            if(listener!=null) listener.onClick((int)(view.getTag()));
        }
    };
}
