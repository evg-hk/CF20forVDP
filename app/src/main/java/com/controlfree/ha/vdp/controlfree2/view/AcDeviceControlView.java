package com.controlfree.ha.vdp.controlfree2.view;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.controlfree.ha.vdp.controlfree2.R;
import com.controlfree.ha.vdp.controlfree2.component.ExImageView;
import com.controlfree.ha.vdp.controlfree2.component.ExTextView;
import com.controlfree.ha.vdp.controlfree2.component.ExThinLineView;
import com.controlfree.ha.vdp.controlfree2.control.BtnGroup;
import com.controlfree.ha.vdp.controlfree2.control.PowerBtn;
import com.controlfree.ha.vdp.controlfree2.utils.ResolutionHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedList;

public class AcDeviceControlView extends DeviceControlView {
    private final static String TAG = "AcDeviceControlView";
    private Context c;
    private AcPanelView asPanelView;

    public AcDeviceControlView(Context context, int w, int h, DeviceControlView.Listener l) {
        super(context, w, h, l);
        this.c = context;
    }
    @Override
    public void setData(JSONObject obj){
        dataObj = obj;
        try{
            tv_title.setText(obj.getString("name"));
            ll_control.removeAllViews();
            scrollView.scrollTo(0, 0);
            JSONArray cArr = obj.getJSONArray("control");

            if(cArr.length()>=2){
                if((cArr.getJSONObject(0).getJSONObject("param_pair").getString("gp").startsWith("power")
                        || cArr.getJSONObject(1).getJSONObject("param_pair").getString("gp").startsWith("power"))){
                    PowerBtn btn = new PowerBtn(c, contentW, "Power", listener_powerBtn);
                    JSONObject dObj = new JSONObject();
                    if(cArr.getJSONObject(0).getString("code").contentEquals("")) cArr.getJSONObject(0).put("code", cArr.getJSONObject(1).getString("code"));
                    if(cArr.getJSONObject(1).getString("code").contentEquals("")) cArr.getJSONObject(1).put("code", cArr.getJSONObject(0).getString("code"));
                    dObj.put("on", cArr.getJSONObject(0));
                    dObj.put("off", cArr.getJSONObject(1));
                    btn.setData(cArr.getJSONObject(0).getJSONObject("param_pair").getString("gp"), dObj);
                    ll_control.addView(btn);
                    ExThinLineView l0 = new ExThinLineView(c);
                    l0.setBackgroundColor(0xff666666);
                    ll_control.addView(l0);
                }else{
                    JSONArray bArr = new JSONArray();
                    String gp = "";
                    if(!cArr.getJSONObject(0).getString("code").contentEquals("")){
                        gp = cArr.getJSONObject(0).getJSONObject("param_pair").getString("gp");
                        bArr.put(cArr.getJSONObject(0));
                    }
                    if(!cArr.getJSONObject(1).getString("code").contentEquals("")){
                        gp = cArr.getJSONObject(1).getJSONObject("param_pair").getString("gp");
                        bArr.put(cArr.getJSONObject(1));
                    }
                    BtnGroup g = new BtnGroup(c, contentW, listener_btnGroup);
                    g.setData(gp, bArr);
                    ll_control.addView(g);
                    ExThinLineView l0 = new ExThinLineView(c);
                    l0.setBackgroundColor(0xff666666);
                    //((LinearLayout.LayoutParams)l0.getLayoutParams()).topMargin = 10;
                    //((LinearLayout.LayoutParams)l0.getLayoutParams()).bottomMargin = 10;
                    ll_control.addView(l0);
                }
            }

            LinkedList<String> gpArr = new LinkedList<String>();
            JSONObject gpObj = new JSONObject();
            for(int i=2;i<cArr.length();i++){
                //filter
                if(cArr.getJSONObject(i).getString("type").contentEquals("polling")) continue;
                if(cArr.getJSONObject(i).getString("type").contentEquals("alert")) continue;
                if(cArr.getJSONObject(i).getString("code").contentEquals("")){
                    if(!cArr.getJSONObject(i).getString("type").contentEquals("hzip")
                            && !cArr.getJSONObject(i).getString("type").contentEquals("st")
                            && !cArr.getJSONObject(i).getString("type").contentEquals("stb")) {
                        continue;
                    }
                }
                String gp = cArr.getJSONObject(i).getJSONObject("param_pair").getString("gp");
                if(!gpObj.has(gp)){
                    gpArr.add(gp);
                    gpObj.put(gp, new JSONArray());
                }
                //slider
                if(cArr.getJSONObject(i).getString("type").startsWith("sld")) {
                    if (cArr.getJSONObject(i).getJSONObject("param_pair").has("off")) {
                        cArr.getJSONObject(i).put("off", cArr.getJSONObject(1));
                    }
                    cArr.getJSONObject(i).put("on", cArr.getJSONObject(0));
                }
                gpObj.getJSONArray(gp).put(cArr.getJSONObject(i));
            }
            //Log.e(TAG, "gpObj: "+gpObj);

            AcPanelView v_ac = new AcPanelView(c, contentW);
            //v_ac.setVisibility(View.GONE);
            ll_control.addView(v_ac);
            asPanelView = v_ac;

            JSONObject acGpObj = new JSONObject();
            for(int i=0;i<gpArr.size();i++){
                JSONArray bArr = gpObj.getJSONArray(gpArr.get(i));
                if(gpArr.get(i).contentEquals("temperature") || gpArr.get(i).contentEquals("speed")
                    || gpArr.get(i).contentEquals("mode") || gpArr.get(i).contentEquals("verti") || gpArr.get(i).contentEquals("horiz")){
                    acGpObj.put(gpArr.get(i), bArr);
                    continue;
                }
                BtnGroup g = new BtnGroup(c, contentW, listener_btnGroup);
                g.setData(gpArr.get(i), bArr);
                ll_control.addView(g);
                ExThinLineView l0 = new ExThinLineView(c);
                l0.setBackgroundColor(0xff666666);
                ll_control.addView(l0);
            }

            genAcPanel(acGpObj);
        }catch(Exception e){e.printStackTrace();}
    }
    private void genAcPanel(JSONObject acGpObj){
        Log.e(TAG, "genAcPanel: "+acGpObj);
        asPanelView.setData(acGpObj);
        asPanelView.setVisibility(View.VISIBLE);
    }
}

