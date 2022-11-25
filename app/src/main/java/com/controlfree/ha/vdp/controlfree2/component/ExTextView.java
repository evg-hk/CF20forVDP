package com.controlfree.ha.vdp.controlfree2.component;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.controlfree.ha.vdp.controlfree2.utils.ResolutionHandler;

import androidx.appcompat.widget.AppCompatTextView;

public class ExTextView extends AppCompatTextView {
    public ExTextView(Context c, String str){
        super(c);
        setText(str);
        setTextSize(ResolutionHandler.fontsize_default);
        setTextColor(0xff020202);
        setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        setLayoutParams(p);
    }
    public int getViewW(){
        this.measure(0, 0);
        return getMeasuredWidth();
    }
    public int getViewH(){
        this.measure(0, 0);
        return getMeasuredHeight();
    }
    public void setBold(){
        setTypeface(null, Typeface.BOLD);
    }
    public void setOneLineCenter(){
        setSingleLine();
        setGravity(Gravity.CENTER);
    }
}
