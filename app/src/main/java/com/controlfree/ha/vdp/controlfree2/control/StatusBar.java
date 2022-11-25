package com.controlfree.ha.vdp.controlfree2.control;

import android.content.Context;
import android.os.Build;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.controlfree.ha.vdp.controlfree2.R;
import com.controlfree.ha.vdp.controlfree2.component.ExTextView;
import com.controlfree.ha.vdp.controlfree2.utils.Fun;
import com.controlfree.ha.vdp.controlfree2.utils.ResolutionHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;

public class StatusBar extends FrameLayout {
    private final static String TAG = "StatusText";
    private Context c;
    private int W = 0, H = 0;
    private JSONObject dataObj = new JSONObject();
    private ExTextView et_name, et_status;
    private View bar;
    private String gp = "", unit = "";
    private double max, min;
    public StatusBar(Context context, int w, int h){
        super(context);
        this.c = context;
        W = w;
        H = h;
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(W, h);
        setLayoutParams(p);

        FrameLayout.LayoutParams p_t = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        p_t.topMargin = h/6;
        ExTextView t = new ExTextView(c, "");
        addView(t, p_t);
        et_name = t;

        FrameLayout.LayoutParams p_s = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        p_s.topMargin = h/6;
        ExTextView s = new ExTextView(c, "");
        s.setTextAlignment(ExTextView.TEXT_ALIGNMENT_VIEW_END);
        addView(s, p_s);
        et_status = s;

        FrameLayout.LayoutParams p_b_bg = new FrameLayout.LayoutParams(W, 16);
        p_b_bg.gravity = Gravity.BOTTOM;
        p_b_bg.bottomMargin = h/5;
        View b_bg = new View(c);
        b_bg.setBackgroundResource(R.drawable.ctrl_bar_bg);
        //b_bg.setBackgroundColor(0xffbbbbbb);
        addView(b_bg, p_b_bg);

        FrameLayout.LayoutParams p_b = new FrameLayout.LayoutParams(0, 16);
        p_b.gravity = Gravity.BOTTOM;
        p_b.bottomMargin = h/5;
        View b = new View(c);
        b.setBackgroundResource(R.drawable.ctrl_bar);
        //b.setBackgroundColor(0xffffffff);
        addView(b, p_b);
        bar = b;
    }

    public void setData(String group, JSONObject obj){
        this.gp = group;
        dataObj = obj;
        try{
            et_name.setText(obj.getString("name"));
            JSONObject p = dataObj.getJSONObject("param_pair");
            p = Fun.prepareParamPairForAnalogVal(p);
            max = Double.parseDouble(p.getString("max"));
            min = Double.parseDouble(p.getString("min"));
            unit = p.getString("unit");
            et_status.setText(min+" "+unit);
        }catch(Exception e){e.printStackTrace();}
    }

    public void updateStatus(String g, String cid, String v, long t){
        //Log.e(TAG, "updateStatus: "+gp+" : "+cid+" : "+v);
        if(!gp.contentEquals(g)) return;
        try{
            if (dataObj.getString("id").contentEquals(cid)) {
                double d = Double.parseDouble(v);
                FrameLayout.LayoutParams p_f = ((FrameLayout.LayoutParams)bar.getLayoutParams());
                p_f.width = (int)((double)W*d);
                bar.setLayoutParams(p_f);
                String txt = new DecimalFormat("#.###").format((min+d*(max-min)))+" "+unit;
                if (Build.VERSION.SDK_INT >= 24) {
                    et_status.setText(Html.fromHtml(txt, Html.FROM_HTML_MODE_LEGACY));
                }else{
                    et_status.setText(Html.fromHtml(txt));
                }
            }
        }catch(Exception e){e.printStackTrace();}
    }
}