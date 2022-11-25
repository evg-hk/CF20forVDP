package com.controlfree.ha.vdp.controlfree2.view;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.controlfree.ha.vdp.controlfree2.R;
import com.controlfree.ha.vdp.controlfree2.component.ExTextView;
import com.controlfree.ha.vdp.controlfree2.utils.Cache;
import com.controlfree.ha.vdp.controlfree2.utils.ResolutionHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;

public class SensorGridCellView extends LinearLayout {
    private final static String TAG = "SensorGridCellView";
    private Context c;
    private int W, H;
    private ExTextView t_name, t_offline;
    private ExTextView[] statusNameTxtArr = {}, statusTxtArr = {};
    private LinearLayout ll_status;
    private JSONObject dataObj = new JSONObject();
    private float sensorTxtW = 0;
    private String[] statusIdArr = {};
    private boolean isOnline = false;
    private long lastFbTime = 0;

    public SensorGridCellView(Context context, int w, int h) {
        super(context);
        this.c = context;
        this.W = w;
        this.H = h;
        this.setOrientation(LinearLayout.VERTICAL);
        FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(W, H);
        setLayoutParams(p);
        setBackgroundResource(R.drawable.dev_bg_alpha);
        setPadding(ResolutionHandler.getPaddingW()*2/3, ResolutionHandler.getPaddingW()/2, ResolutionHandler.getPaddingW()*2/3, ResolutionHandler.getPaddingW()/3);

        int contentH = (H-ResolutionHandler.getPaddingW()/2-ResolutionHandler.getPaddingW()/3);

        LinearLayout.LayoutParams p_ll_top = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        //p_ll_top.bottomMargin = ResolutionHandler.getPaddingW()/3;
        LinearLayout ll_top = new LinearLayout(c);
        ll_top.setOrientation(LinearLayout.HORIZONTAL);
        ll_top.setLayoutParams(p_ll_top);
        //addView(ll_top, p_ll_top);
        ll_status = ll_top;

        ExTextView offline = new ExTextView(c, "Offline");
        offline.setTypeface(null, Typeface.BOLD);
        offline.setSingleLine();
        offline.setEllipsize(TextUtils.TruncateAt.END);
        offline.setTextSize(ResolutionHandler.fontsize_small);
        offline.setTextColor(0xff888888);
        offline.setVisibility(View.VISIBLE);
        //((LinearLayout.LayoutParams)offline.getLayoutParams()).height = halfH;
        //((LinearLayout.LayoutParams)offline.getLayoutParams()).bottomMargin = ResolutionHandler.getPaddingW()/3;
        addView(offline);
        t_offline = offline;

        //--------

        ExTextView name = new ExTextView(c, "");
        name.setTypeface(null, Typeface.BOLD);
        name.setSingleLine();
        name.setEllipsize(TextUtils.TruncateAt.END);
        name.setTextSize(ResolutionHandler.fontsize_small);
        name.setGravity(Gravity.BOTTOM);
        //((LinearLayout.LayoutParams)name.getLayoutParams()).height = halfH;
        //((LinearLayout.LayoutParams)name.getLayoutParams()).gravity = Gravity.BOTTOM;
        addView(name);
        t_name = name;

        int nameH = name.getViewH();
        ((LinearLayout.LayoutParams)name.getLayoutParams()).height = nameH;
        ((LinearLayout.LayoutParams)offline.getLayoutParams()).height = contentH-nameH;
        ((LinearLayout.LayoutParams)ll_top.getLayoutParams()).height = contentH-nameH;
    }
    public void setData(JSONObject obj){
        try{
            dataObj = obj;
            t_name.setText(obj.getString("name"));

            JSONArray cArr = dataObj.getJSONArray("control");

            JSONObject bObj = Cache.getObj("control_bookmark");
            if (bObj==null || !bObj.has(dataObj.getString("id"))) {
                JSONObject firstObj = null;
                for (int i = 0; i < cArr.length(); i++) {
                    if (cArr.getJSONObject(i).getString("type").contentEquals("st")
                            || cArr.getJSONObject(i).getString("type").contentEquals("stb")) {
                        firstObj = cArr.getJSONObject(i);
                        break;
                    }
                }
                if (firstObj != null) {
                    bObj = new JSONObject();
                    bObj.put(dataObj.getString("id"), new JSONArray());
                    bObj.getJSONArray(dataObj.getString("id")).put(firstObj.getString("id"));
                }
            }
            if (bObj!=null && bObj.has(dataObj.getString("id"))) {
                ll_status.removeAllViews();
                JSONArray cidArr = bObj.getJSONArray(dataObj.getString("id"));
                statusNameTxtArr = new ExTextView[cidArr.length()];
                statusTxtArr = new ExTextView[cidArr.length()];
                statusIdArr = new String[cidArr.length()];
                double sW = (cidArr.length() > 1 ? W / 2.5 : LinearLayout.LayoutParams.WRAP_CONTENT);
                for (int i = 0; i < cidArr.length(); i++) {
                    JSONObject cObj = null;
                    for (int j = 0; j < cArr.length(); j++) {
                        if (cArr.getJSONObject(j).getString("id").contentEquals(cidArr.getString(i))) {
                            cObj = cArr.getJSONObject(j);
                            break;
                        }
                    }
                    if (cObj == null) continue;

                    LinearLayout.LayoutParams p_ll_sub = new LinearLayout.LayoutParams((int) sW, H / 2);
                    if (sensorTxtW > 0) {
                        p_ll_sub.leftMargin = ResolutionHandler.getPaddingW() * 1 / 3;
                        sensorTxtW += p_ll_sub.leftMargin;
                    }
                    LinearLayout ll_sub = new LinearLayout(c);
                    ll_sub.setOrientation(LinearLayout.VERTICAL);
                    ll_status.addView(ll_sub, p_ll_sub);

                    ExTextView status = new ExTextView(c, "");
                    status.setTypeface(null, Typeface.BOLD);
                    status.setSingleLine();
                    status.setEllipsize(TextUtils.TruncateAt.END);
                    status.setTextSize(ResolutionHandler.fontsize_small);
                    ll_sub.addView(status);
                    statusTxtArr[i] = status;

                    ExTextView status_name = new ExTextView(c, cObj.getString("name"));
                    status_name.setTextColor(0xff666666);
                    status_name.setSingleLine();
                    status_name.setEllipsize(TextUtils.TruncateAt.END);
                    status_name.setTextSize(ResolutionHandler.fontsize_xxsmall);
                    ll_sub.addView(status_name);
                    statusNameTxtArr[i] = status_name;

                    statusIdArr[i] = cObj.getString("id");

                    sensorTxtW += W / 2.5;
                }
            }
        }catch(Exception e){e.printStackTrace();}
    }
    private void setOnline(boolean is){
        isOnline = is;
        if(is) {
            t_offline.setVisibility(View.GONE);
            ll_status.setVisibility(View.VISIBLE);
            if(ll_status.getParent()==null) addView(ll_status, 0);
            setBackgroundResource(R.drawable.dev_bg);
        }else{
            t_offline.setVisibility(View.VISIBLE);
            ll_status.setVisibility(View.GONE);
            if(ll_status.getParent()!=null) removeView(ll_status);
            setBackgroundResource(R.drawable.dev_bg_alpha);
        }
    }
    public boolean updateStatus(String did, String gp, String cid, String v, long t){
        try {
            //Log.e(TAG, "updateStatus: "+dataObj.getString("id")+" / "+did);
            if (dataObj.getString("id").contentEquals(did)){
                //Log.e(TAG, "updateStatus: "+did+" : "+gp+" : "+cid+" : "+v);
                if(t>lastFbTime) lastFbTime = t;
                setOnline(System.currentTimeMillis()/1000-lastFbTime<60*60*1000);

                //return true; //return true for device found
                JSONArray cArr = dataObj.getJSONArray("control");
                for(int i=0;i<statusIdArr.length;i++){
                    JSONObject cObj = null;
                    for(int j=0;j<cArr.length();j++) {
                        if (statusIdArr[i].contentEquals(cArr.getJSONObject(j).getString("id"))){
                            cObj = cArr.getJSONObject(j);
                        }
                    }
                    if(cObj==null) continue;

                    if(cObj.getJSONObject("param_pair").getString("gp").contentEquals(gp)){
                        if(cObj.getString("type").contentEquals("stb") || cObj.getString("type").startsWith("sld")) {
                            //Log.e(TAG, "param_pair: "+cObj.getJSONObject("param_pair"));
                            double max = Double.parseDouble(cObj.getJSONObject("param_pair").getString("max"));
                            double min = Double.parseDouble(cObj.getJSONObject("param_pair").getString("min"));
                            String unit = cObj.getJSONObject("param_pair").getString("unit");
                            double d = Double.parseDouble(v);
                            if (d >= 0 && d <= 1) {
                                //Log.e(TAG, "d: "+d+" : "+max+" : "+min);
                                //Log.e(TAG, "t: "+t);
                                statusTxtArr[i].setText(new DecimalFormat("#.###").format((min + d * (max - min))) + " " + unit);
                            }
                        }else if(cObj.getString("type").contentEquals("st") || cObj.getString("type").contentEquals("ssw")) {
                            statusTxtArr[i].setText(cObj.getString("name"));
                        }
                    }
                }
            }
        }catch(Exception e){e.printStackTrace();}
        return false;
    }
    public float getMeasuredW(){
        float w = 0;
        float w2 = sensorTxtW;
        if(w2>w) w = w2;
        w2 = t_name.getViewW();
        if(w2>w) w = w2;
        return w+2*ResolutionHandler.getPaddingW()*2/3;
    }
}
