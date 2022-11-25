package com.controlfree.ha.vdp.controlfree2.component;

import android.content.Context;
import android.view.Gravity;
import android.widget.LinearLayout;

import com.controlfree.ha.vdp.controlfree2.R;
import com.controlfree.ha.vdp.controlfree2.utils.ResolutionHandler;

public class ExTextItemView extends LinearLayout {
    private Context c;
    private ExTextView t_txt;
    public ExTextItemView(Context context, int w, String name){
        super(context);
        this.c = context;
        setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(w, LinearLayout.LayoutParams.WRAP_CONTENT);
        setLayoutParams(p);

        ExTextView item = new ExTextView(c, name);
        item.setBold();
        ((LinearLayout.LayoutParams) item.getLayoutParams()).topMargin = ResolutionHandler.getContentW(0.025f);
        ((LinearLayout.LayoutParams) item.getLayoutParams()).bottomMargin = ResolutionHandler.getContentW(0.025f);
        ((LinearLayout.LayoutParams) item.getLayoutParams()).width = w-ResolutionHandler.getBtnW();
        addView(item);
        t_txt = item;
    }

    public void setTextColor(int c){
        t_txt.setTextColor(c);
    }
}