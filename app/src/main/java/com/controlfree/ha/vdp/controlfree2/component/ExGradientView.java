package com.controlfree.ha.vdp.controlfree2.component;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

public class ExGradientView extends View {
    public ExGradientView(Context c, int w, int h){
        super(c);
        FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(w, h);
        p.gravity = Gravity.TOP;
        setLayoutParams(p);

        GradientDrawable gd = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[] {0x000000,0xff323334});
        gd.setCornerRadius(0f);
        setBackground(gd);

    }
    public void setColor(int color){
        GradientDrawable gd = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[] {color & 0x00ffffff,color});
        gd.setCornerRadius(0f);
        setBackground(gd);
    }
}
