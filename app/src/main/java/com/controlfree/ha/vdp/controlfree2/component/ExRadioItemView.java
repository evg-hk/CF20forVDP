package com.controlfree.ha.vdp.controlfree2.component;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

import com.controlfree.ha.vdp.controlfree2.R;
import com.controlfree.ha.vdp.controlfree2.utils.ResolutionHandler;

public class ExRadioItemView extends LinearLayout {
    private Context c;
    private ExImageView iv_radio;
    public ExRadioItemView(Context context, int w, String name, boolean isSelected){
        super(context);
        this.c = context;
        setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(w, LinearLayout.LayoutParams.WRAP_CONTENT);
        setLayoutParams(p);

        ExTextView item = new ExTextView(c, name);
        item.setBold();
        ((LinearLayout.LayoutParams) item.getLayoutParams()).topMargin = ResolutionHandler.getPaddingW()/2;
        ((LinearLayout.LayoutParams) item.getLayoutParams()).bottomMargin = ResolutionHandler.getPaddingW()/2;
        ((LinearLayout.LayoutParams) item.getLayoutParams()).width = w-ResolutionHandler.getBtnW();
        addView(item);

        ExImageView iv = new ExImageView(c, R.drawable.btn_radio_check, ResolutionHandler.getBtnW(), ResolutionHandler.getBtnW());
        ((LinearLayout.LayoutParams) iv.getLayoutParams()).gravity = Gravity.CENTER_VERTICAL;
        addView(iv);
        iv_radio = iv;
        setSelected(isSelected);
    }

    public void setSelected(boolean is){
        if(is){
            iv_radio.setImageResource(R.drawable.btn_radio_check);
            iv_radio.clearColorFilter();
        }else{
            iv_radio.setImageResource(R.drawable.btn_radio_uncheck);
            iv_radio.setColorFilter(0xffffffff);
        }
    }
}
