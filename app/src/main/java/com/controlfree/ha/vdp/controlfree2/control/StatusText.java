package com.controlfree.ha.vdp.controlfree2.control;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.controlfree.ha.vdp.controlfree2.R;
import com.controlfree.ha.vdp.controlfree2.component.ExTextView;
import com.controlfree.ha.vdp.controlfree2.utils.ResolutionHandler;

import org.json.JSONArray;
import org.json.JSONObject;


public class StatusText extends FrameLayout {
    private final static String TAG = "StatusText";
    private Context c;
    private int W = 0, H = 0;
    private JSONArray dataArr = new JSONArray();
    private ExTextView et_name;
    private String gp = "";
    public StatusText(Context context, int w, int h){
        super(context);
        this.c = context;
        W = w-60;
        H = h;
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(w, h);
        setLayoutParams(p);

        FrameLayout.LayoutParams p_t = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        ExTextView t = new ExTextView(c, "");
        t.setSingleLine();
        t.setGravity(Gravity.CENTER_VERTICAL);
        t.setBackgroundResource(R.drawable.dev_bg_alpha);
        t.setTextAlignment(TEXT_ALIGNMENT_CENTER);
        addView(t, p_t);
        et_name = t;
    }

    public void setData(String group, JSONArray arr){
        this.gp = group;
        dataArr = arr;
        try{
            if(arr.length()>0){
                et_name.setText(dataArr.getJSONObject(0).getString("name"));
            }
        }catch(Exception e){e.printStackTrace();}
    }

    public void updateStatus(String g, String cid, String v, long t){
        if(!gp.contentEquals(g)) return;
        try{
            for(int i=0;i<dataArr.length();i++) {
                if (dataArr.getJSONObject(i).getString("id").contentEquals(cid)) {
                    et_name.setText(dataArr.getJSONObject(i).getString("name"));
                }
            }
        }catch(Exception e){e.printStackTrace();}
    }
}