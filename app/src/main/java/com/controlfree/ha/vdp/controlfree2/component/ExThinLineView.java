package com.controlfree.ha.vdp.controlfree2.component;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import com.controlfree.ha.vdp.controlfree2.R;
import com.controlfree.ha.vdp.controlfree2.utils.ResolutionHandler;

public class ExThinLineView extends View {
    public ExThinLineView(Context context){
        super(context);
        setBackgroundColor(0xffb0aea2);

        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1);
        p.topMargin = ResolutionHandler.getPaddingW()/3;
        p.bottomMargin = ResolutionHandler.getPaddingW()/3;
        setLayoutParams(p);
    }
    public int getViewH(){
        return 1+((LinearLayout.LayoutParams)getLayoutParams()).topMargin
                +((LinearLayout.LayoutParams)getLayoutParams()).bottomMargin;

    }
}
