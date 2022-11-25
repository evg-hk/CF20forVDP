package com.controlfree.ha.vdp.controlfree2.component;

import android.content.Context;
import android.print.PrintAttributes;
import android.view.View;
import android.widget.LinearLayout;

import com.controlfree.ha.vdp.controlfree2.R;
import com.controlfree.ha.vdp.controlfree2.utils.ResolutionHandler;

public class ExShortLine extends View {
    public ExShortLine(Context context){
        super(context);
        setBackgroundResource(R.drawable.white_bg);

        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 3);
        p.topMargin = ResolutionHandler.getPaddingW()/2;
        p.bottomMargin = ResolutionHandler.getPaddingW()/2;
        setLayoutParams(p);
    }
    public int getViewHeight(){
        return ((LinearLayout.LayoutParams)getLayoutParams()).topMargin+
                ((LinearLayout.LayoutParams)getLayoutParams()).bottomMargin+
                ((LinearLayout.LayoutParams)getLayoutParams()).height;
    }
}
