package com.controlfree.ha.vdp.controlfree2.component;

import android.content.Context;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import com.controlfree.ha.vdp.controlfree2.R;
import com.controlfree.ha.vdp.controlfree2.utils.ResolutionHandler;

import androidx.appcompat.widget.AppCompatButton;

public class ExButton extends AppCompatButton {
    private String nameStr = "";
    private boolean isLoading = false;
    public ExButton(Context c, String name){
        super(c);
        nameStr = name;
        setBackgroundColor(0x00000000);
        setBackgroundResource(R.drawable.white_bg);
        setElevation(0);

        setAllCaps(false);
        setText(name);
        setTextSize(ResolutionHandler.fontsize_default);
        setTextSize(0xff454545);
        setPadding(ResolutionHandler.getW(0.045f), ResolutionHandler.getW(0.02f), ResolutionHandler.getW(0.045f), ResolutionHandler.getW(0.02f));
        setTextAlignment(TEXT_ALIGNMENT_CENTER);

        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        p.gravity = Gravity.RIGHT;
        setLayoutParams(p);

        setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        v.setAlpha(0.6f);
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        v.setAlpha(1f);
                        break;
                    }
                    default: {
                        v.setAlpha(1f);
                        break;
                    }
                }
                return false;
            }
        });
    }
    public void setLoading(boolean is){
        isLoading = is;
        if(is){
            setText("...");
        }else{
            setText(nameStr);
        }
    }
    public boolean isLoading(){ return isLoading; }
    public void setTwoLine(){
        setMaxLines(2);
    }
}

