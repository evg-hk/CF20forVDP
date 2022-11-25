package com.controlfree.ha.vdp.controlfree2.view;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.controlfree.ha.vdp.controlfree2.R;
import com.controlfree.ha.vdp.controlfree2.component.ExImageView;
import com.controlfree.ha.vdp.controlfree2.component.ExTextView;
import com.controlfree.ha.vdp.controlfree2.utils.Fun;
import com.controlfree.ha.vdp.controlfree2.utils.ResolutionHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;

public class DeviceGridCellView extends FrameLayout {
    private final static String TAG = "DeviceGridCellView";
    private Context c;
    private int W;
    private ExTextView t_status, t_offline, t_name;
    private ExImageView iv_icon;
    private JSONObject dataObj = new JSONObject(), paramObj = new JSONObject();
    private long lastFbTime = 0;
    private boolean isOnline = false;

    public DeviceGridCellView(Context context, int w) {
        super(context);
        this.c = context;
        this.W = w;

        setBackgroundResource(R.drawable.dev_bg_alpha);
        FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(W, W);
        setLayoutParams(p);
        setPadding(ResolutionHandler.getPaddingW()/2, 0, ResolutionHandler.getPaddingW()/2, 0);

        FrameLayout.LayoutParams p_ll = new FrameLayout.LayoutParams(W, LayoutParams.WRAP_CONTENT);
        p_ll.gravity = Gravity.CENTER;
        //p_ll.topMargin = -w/18;
        LinearLayout ll = new LinearLayout(c);
        ll.setOrientation(LinearLayout.VERTICAL);

        int ivW = (int)(w/2.1)-(w/9);
        ExImageView iv = new ExImageView(c, R.drawable.dev_default1, ivW, ivW);
        iv.setPadding(0, 0, 0, w/15);
        iv.setColorFilter(0xff666666);
        ((LinearLayout.LayoutParams)iv.getLayoutParams()).gravity = Gravity.CENTER_HORIZONTAL;
        ll.addView(iv);
        iv_icon = iv;

        ExTextView status = new ExTextView(c, "-");
        status.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        status.setTypeface(null, Typeface.BOLD);
        status.setSingleLine();
        status.setEllipsize(TextUtils.TruncateAt.END);
        status.setTextSize(ResolutionHandler.fontsize_small);
        status.setVisibility(View.GONE);
        ll.addView(status);
        t_status = status;

        ExTextView offline = new ExTextView(c, "Offline");
        offline.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        offline.setTypeface(null, Typeface.BOLD);
        offline.setSingleLine();
        offline.setEllipsize(TextUtils.TruncateAt.END);
        offline.setTextSize(ResolutionHandler.fontsize_small);
        offline.setVisibility(View.VISIBLE);
        ll.addView(offline);
        t_offline = offline;

        ExTextView name = new ExTextView(c, "");
        name.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        name.setSingleLine();
        name.setEllipsize(TextUtils.TruncateAt.END);
        name.setTextSize(ResolutionHandler.fontsize_xxsmall);
        ((LinearLayout.LayoutParams)name.getLayoutParams()).leftMargin = ResolutionHandler.getPaddingW()/2;
        ((LinearLayout.LayoutParams)name.getLayoutParams()).rightMargin = ResolutionHandler.getPaddingW()/2;
        //((LinearLayout.LayoutParams)name.getLayoutParams()).topMargin = -10;
        //((LinearLayout.LayoutParams)name.getLayoutParams()).gravity = Gravity.CENTER_HORIZONTAL;
        ll.addView(name);
        t_name = name;

        addView(ll, p_ll);
    }
    public void setData(JSONObject obj){
        try{
            dataObj = obj;
            t_name.setText(obj.getString("name"));
            //Log.e(TAG, "setData: "+obj);
            if(obj.has("symbolType") && obj.has("symbolName")){
                String[] a = obj.getString("symbolName").toLowerCase().split("-");
                String n = "dev";
                for(int i=0;i<a.length;i++){
                    n += "_"+a[i];
                }
                Log.e(TAG, "symbolName: "+n+" / "+Fun.getImgId(c, n));
                iv_icon.setImageResource(Fun.getImgId(c, n));
            }else {
                int rid = Fun.getDeviceIconRes(obj.getString("cat_code"));
                if(rid>0){
                    iv_icon.setImageResource(rid);
                }
            }
        }catch(Exception e){e.printStackTrace();}
    }
    private void setOnline(boolean is){
        isOnline = is;
        if(is) {
            t_offline.setVisibility(View.GONE);
            t_status.setVisibility(View.VISIBLE);
        }else{
            t_offline.setVisibility(View.VISIBLE);
            t_status.setVisibility(View.GONE);
        }
    }
    public boolean updateStatus(String did, String gp, String cid, String v, long t){
        try {
            //Log.e(TAG, "updateStatus: "+dataObj.getString("id")+" / "+did);
            if (dataObj.getString("id").contentEquals(did)){
                if(t>lastFbTime) lastFbTime = t;
                //Log.e(TAG, "updateStatus: t: "+t+" -> "+(System.currentTimeMillis()/1000-lastFbTime)+" / "+60*60*1000);
                setOnline(System.currentTimeMillis()/1000-lastFbTime<60*60*1000);

                JSONArray cArr = dataObj.getJSONArray("control");
                //Log.e(TAG, "updateStatus: "+cArr);
                for (int i = 0; i < cArr.length(); i++) {
                    if (cArr.getJSONObject(i).getString("id").contentEquals(cid)) {
                        if (i == 0 || i == 1) {
                            t_status.setText(cArr.getJSONObject(i).getString("name"));
                            if (i == 0 && isOnline) {
                                setBackgroundResource(R.drawable.dev_bg);
                                if(dataObj.has("symbolType")) {
                                    iv_icon.clearColorFilter();
                                }else{
                                    iv_icon.setColorFilter(0xff666666);
                                }
                            } else {
                                setBackgroundResource(R.drawable.dev_bg_alpha);
                                iv_icon.setColorFilter(0xff666666);
                            }
                            return true;
                        } else if(/*cArr.getJSONObject(i).getString("type").contentEquals("st") || */cArr.getJSONObject(i).getString("type").contentEquals("ssw")
                                || cArr.getJSONObject(i).getString("type").contentEquals("gp") || gp.startsWith("power")){
                            t_status.setText(cArr.getJSONObject(i).getString("name"));
                        } else if(/*cArr.getJSONObject(i).getString("type").contentEquals("stb") || */cArr.getJSONObject(i).getString("type").startsWith("sld")){
                            if(!paramObj.has(cid)) {
                                JSONObject p = cArr.getJSONObject(i).getJSONObject("param_pair");
                                p = Fun.prepareParamPairForAnalogVal(p);
                                p.put("max", Double.parseDouble(p.getString("max")));
                                p.put("min", Double.parseDouble(p.getString("min")));
                                paramObj.put(cid, p);
                            }
                            JSONObject pObj = paramObj.getJSONObject(cid);
                            if(pObj==null) return true;

                            //only the first sld
                            if(dataObj.getString("cat_code").contentEquals("dim") || dataObj.getString("cat_code").contentEquals("ltsys")
                                 || dataObj.getString("cat_code").contentEquals("smlt")){
                                for(int j=2;j<cArr.length();j++){
                                    if(cArr.getJSONObject(j).getString("type").contentEquals("sld")){
                                        if(!cArr.getJSONObject(j).getString("id").contentEquals(cid)){
                                            return true;
                                        }
                                        break;
                                    }
                                }
                            }

                            double ve = Double.parseDouble(v);
                            if(ve<0) ve = 0;
                            if(ve>=1) ve = 1;
                            if(ve<0.02 || !isOnline){
                                setBackgroundResource(R.drawable.dev_bg_alpha);
                            }else{
                                setBackgroundResource(R.drawable.dev_bg);
                            }
                            ve = pObj.getDouble("min")+ve*(pObj.getDouble("max")-pObj.getDouble("min"));
                            if(ve>=100 || pObj.getString("unit").contentEquals("%")){
                                t_status.setText((int)ve+" "+pObj.getString("unit"));
                            }else if(ve>=10){
                                t_status.setText((Math.floor(ve*10.0)/10.0)+" "+pObj.getString("unit"));
                            }else {
                                DecimalFormat df = new DecimalFormat("0.00");
                                t_status.setText(df.format(ve) + " " + pObj.getString("unit"));
                            }
                        }
                        return true;
                    }
                }
            }
        }catch(Exception e){e.printStackTrace();}
        return false;
    }
}
