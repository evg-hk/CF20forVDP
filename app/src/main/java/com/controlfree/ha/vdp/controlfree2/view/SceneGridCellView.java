package com.controlfree.ha.vdp.controlfree2.view;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.controlfree.ha.vdp.controlfree2.R;
import com.controlfree.ha.vdp.controlfree2.component.ExImageView;
import com.controlfree.ha.vdp.controlfree2.component.ExTextView;
import com.controlfree.ha.vdp.controlfree2.component.ExThinLineView;
import com.controlfree.ha.vdp.controlfree2.utils.Cache;
import com.controlfree.ha.vdp.controlfree2.utils.DB;
import com.controlfree.ha.vdp.controlfree2.utils.Fun;
import com.controlfree.ha.vdp.controlfree2.utils.ResolutionHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class SceneGridCellView extends LinearLayout {
    private final static String TAG = "SceneGridCellView";
    private Context c;
    private int W, H;
    private ExTextView t_name, t_type;
    private JSONObject dataObj = new JSONObject();
    private ProgressBar progressBar;
    private LinearLayout ll_icon;
    private int bottomH = 0;

    public SceneGridCellView(Context context, int w, int h) {
        super(context);
        this.c = context;
        this.W = w;
        this.H = h;
        this.setOrientation(LinearLayout.VERTICAL);
        FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(W, H);
        setLayoutParams(p);
        setBackgroundResource(R.drawable.dev_bg_alpha);
        setPadding(ResolutionHandler.getPaddingW()*2/3, ResolutionHandler.getPaddingW()/2, ResolutionHandler.getPaddingW()*2/3, 0);

        LinearLayout.LayoutParams p_f_status = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, h);
        //p_f_status.bottomMargin = ResolutionHandler.getPaddingW()/3;
        LinearLayout f_status = new LinearLayout(c);
        //f_status.setBackgroundColor(0x9900ff00);
        f_status.setOrientation(LinearLayout.VERTICAL);
        addView(f_status, p_f_status);

        int contentW = W-2*ResolutionHandler.getPaddingW()*2/3;
        LinearLayout.LayoutParams p_ll_top = new LinearLayout.LayoutParams(contentW, LayoutParams.WRAP_CONTENT);
        //p_ll_top.bottomMargin = ResolutionHandler.getPaddingW()/2;
        LinearLayout ll_top = new LinearLayout(c);
        ll_top.setOrientation(LinearLayout.HORIZONTAL);
        f_status.addView(ll_top, p_ll_top);

        LinearLayout.LayoutParams p_v = new LinearLayout.LayoutParams(contentW-ResolutionHandler.getPaddingW(), LayoutParams.WRAP_CONTENT);
        //p_v.rightMargin = ResolutionHandler.getPaddingW();
        ExTextView v = new ExTextView(c, " ");
        v.setPadding(ResolutionHandler.getPaddingW()/3, 0, 0, 0);
        v.setBackgroundResource(R.drawable.dev_bg);
        v.setTextSize(ResolutionHandler.fontsize_xsmall);
        v.setEllipsize(TextUtils.TruncateAt.END);
        v.setTextColor(0xff86868a);
        v.setGravity(Gravity.CENTER_VERTICAL);
        v.setSingleLine();
        ll_top.addView(v, p_v);
        t_type = v;
        int vH = v.getViewH();
        ((LinearLayout.LayoutParams)v.getLayoutParams()).width = contentW-vH-vH/2;

        int pbH = vH;
        LinearLayout.LayoutParams p_pb = new LinearLayout.LayoutParams(pbH, pbH);
        p_pb.gravity = Gravity.CENTER_VERTICAL;
        p_pb.leftMargin = vH/2;
        ProgressBar pb = new ProgressBar(c);
        pb.setIndeterminate(true);
        pb.setVisibility(View.INVISIBLE);
        ll_top.addView(pb, p_pb);
        progressBar = pb;

        ExTextView name = new ExTextView(c, "");
        name.setTypeface(null, Typeface.BOLD);
        name.setSingleLine();
        name.setEllipsize(TextUtils.TruncateAt.END);
        name.setTextSize(ResolutionHandler.fontsize_small);
        //name.setBackgroundColor(0x99ff0000);
        ((LinearLayout.LayoutParams)name.getLayoutParams()).gravity = Gravity.CENTER_VERTICAL;
        f_status.addView(name);
        t_name = name;

        //--------
        ExThinLineView line = new ExThinLineView(c);
        line.setBackgroundColor(0xff111113);
        ((LayoutParams)line.getLayoutParams()).topMargin = 0;
        ((LayoutParams)line.getLayoutParams()).bottomMargin = 0;
        ((LayoutParams)line.getLayoutParams()).height = 1;
        addView(line);

        LinearLayout.LayoutParams p_ll_d = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, (int)(H/4));
        p_ll_d.gravity = Gravity.BOTTOM;
        LinearLayout ll_d = new LinearLayout(c);
        ll_d.setOrientation(LinearLayout.HORIZONTAL);
        //ll_d.setBackgroundColor(0x990000ff);
        addView(ll_d, p_ll_d);
        ll_icon = ll_d;
        bottomH += p_ll_d.height;

        ((LinearLayout.LayoutParams)f_status.getLayoutParams()).height = h-ResolutionHandler.getPaddingW()/2-bottomH-2;
        ((LinearLayout.LayoutParams)name.getLayoutParams()).height = LayoutParams.MATCH_PARENT;
    }
    public void isLoading(boolean is){
        progressBar.setVisibility((is? View.VISIBLE:View.INVISIBLE));
    }
    public void setData(JSONObject obj){
        try{
            dataObj = obj;
            t_name.setText(obj.getString("scene_name"));
            if(obj.has("parameter")){
                ArrayList<String> tpList = new ArrayList<String>();
                JSONArray arr = obj.getJSONArray("parameter");
                if(arr.length()>0){
                    if(!tpList.contains(arr.getJSONObject(0).getString("tp"))){
                        tpList.add(arr.getJSONObject(0).getString("tp"));
                    }
                }
                JSONArray mtriggerArr = obj.getJSONArray("mtrigger_arr");
                for(int i=0;i<mtriggerArr.length();i++){
                    JSONArray arr2 = mtriggerArr.getJSONObject(i).getJSONArray("parameter");
                    if(arr2.length()>0){
                        if(!tpList.contains(arr2.getJSONObject(0).getString("tp"))){
                            tpList.add(arr2.getJSONObject(0).getString("tp"));
                        }
                    }
                }
                String t_str = "";
                if(tpList.size()>1){
                    t_str = tpList.size()+" TRIGGERS";
                }else{
                    if(tpList.get(0).contentEquals("fb")){
                        t_str = "FEEDBACK";
                    }else if(tpList.get(0).contentEquals("bt")){
                        t_str = "BUTTON TAP";
                    }else if(tpList.get(0).contentEquals("t")){
                        t_str = "SCHEDULE";
                    }else if(tpList.get(0).contentEquals("wk")){
                        t_str = "SCHEDULE";
                    }else if(tpList.get(0).contentEquals("mn")){
                        t_str = "SCHEDULE";
                    }
                }
                t_type.setText(t_str);
            }
            if(obj.has("scene_id")) {
                JSONObject sObj = Cache.getSceneById(c, obj.getInt("scene_id"));
                if(sObj!=null){
                    if (sObj.has("control")) {
                        JSONArray idArr = new JSONArray();
                        JSONArray arr = new JSONArray(sObj.getString("control"));
                        for(int i=0;i<arr.length();i++){
                            idArr.put(arr.getJSONObject(i).getInt("device_id"));
                        }
                        DB db = new DB(c);
                        JSONArray dList = db.getSimpleDeviceListById(idArr);
                        //Log.e(TAG, ""+dList);
                        db.close();

                        for(int i=0;i<dList.length();i++){
                            int rid = Fun.getDeviceIconRes(dList.getJSONObject(i).getString("cat_code"));
                            if(rid>0){
                                ExImageView iv = new ExImageView(c, rid, bottomH*2/3, bottomH*2/3);
                                ((LayoutParams)iv.getLayoutParams()).rightMargin = ResolutionHandler.getPaddingW()/3;
                                ((LayoutParams)iv.getLayoutParams()).gravity = Gravity.CENTER_VERTICAL;
                                iv.setTintDeepGray();
                                ll_icon.addView(iv, ll_icon.getChildCount()-1);
                            }
                        }
                    }
                }
            }
        }catch(Exception e){e.printStackTrace();}
    }
}
