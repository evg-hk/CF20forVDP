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
import com.controlfree.ha.vdp.controlfree2.utils.Api;
import com.controlfree.ha.vdp.controlfree2.utils.Cache;
import com.controlfree.ha.vdp.controlfree2.utils.ResolutionHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedList;

public class DeviceControlView extends FrameLayout {
    private final static String TAG = "DeviceControlView";
    protected Context c;
    protected int W,H,contentW;
    protected JSONObject dataObj = new JSONObject();
    protected ExTextView tv_title;
    protected LinearLayout ll_control;
    protected ScrollView scrollView;
    protected ExImageView bookmarkBtn;
    protected boolean is_bookmark = false;

    protected Listener listener;
    public static interface Listener{
        public void onClick_closeBtn();
        public void initStatus(JSONObject obj);
        public void sendCommand(String msg);
    }

    public DeviceControlView(Context context, int w, int h, Listener l) {
        super(context);
        this.c = context;
        this.W = w;
        this.contentW = W-ResolutionHandler.getPaddingW()*2;
        this.H = h;
        this.listener = l;
        FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(W, H);
        setLayoutParams(p);
        setBackgroundResource(R.drawable.dev_control_bg);

        int topH = ResolutionHandler.getBtnW()+ResolutionHandler.getPaddingW();
        FrameLayout.LayoutParams p_ll_top = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        p_ll_top.leftMargin = ResolutionHandler.getPaddingW();
        p_ll_top.topMargin = ResolutionHandler.getPaddingW()/2;
        LinearLayout ll_top = new LinearLayout(c);
        ll_top.setOrientation(LinearLayout.HORIZONTAL);
        ll_top.setGravity(Gravity.CENTER_VERTICAL);

        ExTextView title = new ExTextView(c, "");
        title.setTextColor(0xffffffff);
        title.setTextSize(ResolutionHandler.fontsize_large);
        title.setTypeface(null, Typeface.BOLD);
        //((LinearLayout.LayoutParams)title.getLayoutParams()).bottomMargin = 20;
        ((LinearLayout.LayoutParams)title.getLayoutParams()).width = (int)(contentW-2*ResolutionHandler.getBtnW()-ResolutionHandler.getBtnW()*0.4f);
        ll_top.addView(title);
        tv_title = title;

        //ExTextView tv = new ExTextView(c, "Edit Button");
//        ExTextView tv = new ExTextView(c, "");
//        tv.setSingleLine();
//        ((LinearLayout.LayoutParams)tv.getLayoutParams()).width = (int)(contentW-ResolutionHandler.getPaddingW()-ResolutionHandler.getBtnW());
//        ll_top.addView(tv);

        ExImageView btn0 = new ExImageView(c, R.drawable.btn_bookmark_uncheck, ResolutionHandler.getBtnW(), topH);
        ((LinearLayout.LayoutParams)btn0.getLayoutParams()).rightMargin = (int)(ResolutionHandler.getBtnW()*0.4f);
        ll_top.addView(btn0);
        btn0.setOnClickListener(onClick_bookmark);
        //btn0.setVisibility(View.GONE);
        bookmarkBtn = btn0;

        ExImageView btn1 = new ExImageView(c, R.drawable.btn_circle_cross, ResolutionHandler.getBtnW(), topH);
        ll_top.addView(btn1);
        btn1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onClick_closeBtn();
            }
        });

        addView(ll_top, p_ll_top);

        //---------------

        FrameLayout.LayoutParams p_sv = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        p_sv.topMargin = topH+ResolutionHandler.getPaddingW();
        ScrollView sv = new ScrollView(c);
        addView(sv, p_sv);
        scrollView = sv;

        ScrollView.LayoutParams p_ll = new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT);
        LinearLayout ll = new LinearLayout(c);
        ll.setPadding(ResolutionHandler.getPaddingW(), 0, ResolutionHandler.getPaddingW(), 20);
        ll.setOrientation(LinearLayout.VERTICAL);

        ExThinLineView l0 = new ExThinLineView(c);
        ll.addView(l0);

        LinearLayout.LayoutParams p_ll_c = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        p_ll_c.topMargin = 5;
        LinearLayout ll_c = new LinearLayout(c);
        ll_c.setOrientation(LinearLayout.VERTICAL);
        ll_c.setPadding(0, 0, 0, ResolutionHandler.getBottomTabH()+ResolutionHandler.getW(0.06f));
        ll.addView(ll_c, p_ll_c);
        ll_control = ll_c;

        sv.addView(ll, p_ll);
    }
    private OnClickListener onClick_bookmark = new OnClickListener(){
        @Override
        public void onClick(View view) {
            try{
                is_bookmark = !is_bookmark;
                bookmarkBtn.setImageResource(is_bookmark?R.drawable.btn_bookmark_check:R.drawable.btn_bookmark_uncheck);
                dataObj.put("is_bookmark", is_bookmark);
                Api.bookmarkDevice(c, dataObj.getString("area_id"), dataObj.getString("id"), is_bookmark, new Api.Listener(){
                    @Override
                    public void onSuccess(JSONObject reqObj, JSONObject obj, JSONArray arr, JSONObject resObj) {
                        Cache.updateDevice(c, dataObj);
                        for(int i=Cache.viewList.size()-1;i>=0;i--){
                            if(Cache.viewList.get(i) instanceof Server2View){
                                Cache.viewList.remove(i);
                                break;
                            }
                        }
                    }
                    @Override
                    public void onFail(String msg, JSONObject resObj) {

                    }
                });
            }catch(Exception e){e.printStackTrace();}
        }
    };
    public void setData(JSONObject obj){
        dataObj = obj;
        try{
            if(dataObj.getBoolean("is_bookmark")){
                is_bookmark = dataObj.getBoolean("is_bookmark");
                bookmarkBtn.setImageResource(is_bookmark?R.drawable.btn_bookmark_check:R.drawable.btn_bookmark_uncheck);
            }
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

            for(int i=0;i<gpArr.size();i++){
                JSONArray bArr = gpObj.getJSONArray(gpArr.get(i));
                if(gpArr.get(i).contentEquals("input") || gpArr.get(i).contentEquals("aspect")){
                    for(int j=0;j<bArr.length();j++){
                        bArr.getJSONObject(j).put("type", "ssw");
                    }
                }

                //btn
                JSONArray btnArr = new JSONArray();
                for(int j=0;j<bArr.length();j++){
                    if(bArr.getJSONObject(j).getString("type").contentEquals("btn")
                        || bArr.getJSONObject(j).getString("type").contentEquals("")){
                        btnArr.put(bArr.getJSONObject(j));
                    }
                }
                for(int j=bArr.length()-1;j>=0;j--){
                    if(bArr.getJSONObject(j).getString("type").contentEquals("btn")
                            || bArr.getJSONObject(j).getString("type").contentEquals("")){
                        bArr.remove(j);
                    }
                }
                if(btnArr.length()>0){
                    BtnGroup g = new BtnGroup(c, contentW, listener_btnGroup);
                    g.setData(gpArr.get(i), btnArr);
                    ll_control.addView(g);
                }
                if(bArr.length()>0) {
                    if(btnArr.length()>0){
                        ExThinLineView l0 = new ExThinLineView(c);
                        l0.setBackgroundColor(0xff666666);
                        ll_control.addView(l0);
                    }
                    BtnGroup g = new BtnGroup(c, contentW, listener_btnGroup);
                    g.setData(gpArr.get(i), bArr);
                    ll_control.addView(g);
                }

                ExThinLineView l0 = new ExThinLineView(c);
                l0.setBackgroundColor(0xff666666);
                ll_control.addView(l0);
            }
            listener.initStatus(dataObj);
        }catch(Exception e){e.printStackTrace();}
    }
    public JSONObject getData(){ return dataObj;}
    protected PowerBtn.Listener listener_powerBtn = new PowerBtn.Listener(){
        @Override
        public void onClick(String gp, JSONObject obj) {
            try{
                listener.sendCommand("CTdevice|"+dataObj.getString("id")+"|"+gp+"|"+obj.getString("id")+"|-1|"+obj.getString("delay"));
                for(int i=0;i<ll_control.getChildCount();i++){
                    if(ll_control.getChildAt(i) instanceof BtnGroup){
                        ((BtnGroup)ll_control.getChildAt(i)).changeSliderByPower(gp, obj.getString("id"));
                    }
                }
            }catch(Exception e){e.printStackTrace();}
        }
    };
    protected BtnGroup.Listener listener_btnGroup = new BtnGroup.Listener(){
        @Override
        public void onClick(String gp, JSONObject obj) {
            try{
                listener.sendCommand("CTdevice|"+dataObj.getString("id")+"|"+gp+"|"+obj.getString("id")+"|-1|"+obj.getString("delay"));
            }catch(Exception e){e.printStackTrace();}
        }
        @Override
        public void onSlide(String gp, JSONObject obj, double val) {
            try{
                listener.sendCommand("CTdevice|"+dataObj.getString("id")+"|"+gp+"|"+obj.getString("id")+"|"+val+"|"+obj.getString("delay"));
            }catch(Exception e){e.printStackTrace();}
        }
    };

    public void updateStatus(String did, String gp, String cid, String v, long t){
        try{
            if(dataObj.getString("id").contentEquals(did)){
                for(int i=0;i<ll_control.getChildCount();i++){
                    if(ll_control.getChildAt(i) instanceof PowerBtn){
                        ((PowerBtn)ll_control.getChildAt(i)).updateStatus(gp, cid, v, t);
                    }else if(ll_control.getChildAt(i) instanceof BtnGroup){
                        ((BtnGroup)ll_control.getChildAt(i)).updateStatus(gp, cid, v, t);
                    }
                }
                if(gp.contentEquals("brightness")){
                    if(ll_control.getChildAt(0) instanceof PowerBtn){
                        double ve = Double.parseDouble(v);
                        ((PowerBtn)ll_control.getChildAt(0)).setOnOff((ve<0.02?false:true));
                    }
//                    for(int i=0;i<dataObj.getJSONArray("control").length();i++){
//                        if(dataObj.getJSONArray("control").getJSONObject(i).getString("id").contentEquals(cid)){
//                            JSONObject p = dataObj.getJSONArray("control").getJSONObject(i).getJSONObject("param_pair");
//
//                        }
//                    }
                }
            }
        }catch(Exception e){e.printStackTrace();}
    }
}
