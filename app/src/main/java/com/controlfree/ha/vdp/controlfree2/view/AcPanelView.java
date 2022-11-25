package com.controlfree.ha.vdp.controlfree2.view;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.controlfree.ha.vdp.controlfree2.R;
import com.controlfree.ha.vdp.controlfree2.component.ExImageView;
import com.controlfree.ha.vdp.controlfree2.component.ExTextView;
import com.controlfree.ha.vdp.controlfree2.utils.ResolutionHandler;

import org.json.JSONObject;

import java.util.LinkedList;

public class AcPanelView extends FrameLayout {
    private Context c;
    private JSONObject dataObj;
    private LinkedList<View> screenItemList = new LinkedList();
    public AcPanelView(Context context, int w){
        super(context);
        this.c = context;
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(w, LinearLayout.LayoutParams.WRAP_CONTENT);
        setLayoutParams(p);

        int screenH = ResolutionHandler.getW(3f/8f);
        FrameLayout f_screen = new FrameLayout(c);

        FrameLayout.LayoutParams p_tv = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, screenH);
        p_tv.leftMargin = ResolutionHandler.getPaddingW();
        ExTextView tv = new ExTextView(c, "30C");
        //tv.setAlpha(0f);
        f_screen.addView(tv, p_tv);

        FrameLayout.LayoutParams p_ll = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, screenH);
        p_ll.gravity = Gravity.RIGHT;
        p_ll.rightMargin = ResolutionHandler.getPaddingW();
        LinearLayout ll = new LinearLayout(c);
        ll.setOrientation(LinearLayout.VERTICAL);
        f_screen.addView(ll, p_ll);

        ExTextView tv_sp = new ExTextView(c, "HIGH");
        //tv_sp.setAlpha(0f);
        tv_sp.setTextSize(ResolutionHandler.fontsize_xsmall);
        ll.addView(tv_sp);
        screenItemList.add(tv_sp);
        ExImageView iv_h = new ExImageView(c, screenH/3, screenH/3);
        iv_h.setImageResource(R.drawable.btn_ac_swing_hor_black);
        //iv_h.setAlpha(0f);
        ll.addView(iv_h);
        screenItemList.add(iv_h);
        ExImageView iv_v = new ExImageView(c, screenH/3, screenH/3);
        iv_v.setImageResource(R.drawable.btn_ac_swing_ver_black);
        //iv_v.setAlpha(0f);
        ll.addView(iv_v);
        screenItemList.add(iv_v);

        addView(f_screen);
    }

    public void setData(JSONObject acGpObj){
        //removeAllViews();
        dataObj = acGpObj;

        if(dataObj.has("temperature")){
        }
    }
}
