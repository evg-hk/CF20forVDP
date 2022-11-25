package com.controlfree.ha.vdp.controlfree2.component;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.controlfree.ha.vdp.controlfree2.utils.ResolutionHandler;

public class ExTopbarView extends ExTextView {
    private Context c;
    public final static int MODE_NONE = 1;
    public final static int MODE_CONNECTING = 2;
    public final static int MODE_CONNECTED = 3;
    private int mode = MODE_NONE;
    private long changeModeTime = 0;

    public ExTopbarView(Context context){
        super(context, " ");
        this.c = context;
        setTextAlignment(TEXT_ALIGNMENT_CENTER);
        setTextSize(ResolutionHandler.fontsize_small);
        setPadding(0, ResolutionHandler.getPaddingW()/5, 0, ResolutionHandler.getPaddingW()/5);

        FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        setLayoutParams(p);
    }
    public void setMode(int m){
        mode = m;
        changeModeTime = System.currentTimeMillis();
        if(mode==MODE_NONE){
            setText("");
            setVisibility(View.GONE);
        }else if(mode==MODE_CONNECTING){
            setText("Syncing...");
            setBackgroundColor(0xffff7f50);
            setTextColor(0xffffffff);
            setVisibility(View.VISIBLE);
        }else if(mode==MODE_CONNECTED){
            setText("Connected");
            setBackgroundColor(0xff32cd32);
            setTextColor(0xffffffff);
            setVisibility(View.VISIBLE);
        }
    }
    public int getMode(){ return mode;}
    public long getChangeModeTime(){ return changeModeTime;}
}
