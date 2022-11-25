package com.controlfree.ha.vdp.controlfree2.control;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

import com.controlfree.ha.vdp.controlfree2.R;
import com.controlfree.ha.vdp.controlfree2.component.ExImageView;
import com.controlfree.ha.vdp.controlfree2.component.ExTextView;
import com.controlfree.ha.vdp.controlfree2.utils.ResolutionHandler;
import com.controlfree.ha.vdp.controlfree2.view.DeviceControlView;

import org.json.JSONArray;
import org.json.JSONObject;

public class PowerBtn extends LinearLayout {
    private final static String TAG = "PowerBtn";
    private Context c;
    private int W;
    private ExTextView tv_name;
    private ExImageView iv_btn;
    private JSONObject dataObj;
    private String gp = "";
    private boolean isOn = false;

    private Listener listener;
    public static interface Listener{
        public void onClick(String gp, JSONObject obj);
    }
    public PowerBtn(Context context, int w, String name, Listener l){
        super(context);
        this.c = context;
        this.W = w;
        this.listener = l;
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        setLayoutParams(p);
        this.setOrientation(LinearLayout.HORIZONTAL);
        this.setGravity(Gravity.CENTER_VERTICAL);

        ExTextView tv = new ExTextView(c, name);
        tv.setSingleLine();
        ((LinearLayout.LayoutParams)tv.getLayoutParams()).width = W-(int)(ResolutionHandler.getBtnW()*1.4);
        addView(tv);
        tv_name = tv;

        ExImageView iv = new ExImageView(c, R.drawable.ctrl_power_btn_off, (int)(ResolutionHandler.getBtnW()*1.4), (int)(ResolutionHandler.getBtnW()*1.4));
        iv.setGrayCircleIcon();
        addView(iv);
        iv_btn = iv;

        setOnClickListener(onClick);
    }
    public void setData(String group, JSONObject obj){
        this.gp = group;
        dataObj = obj;
    }
    public void setOnOff(boolean is){
        isOn = is;
        iv_btn.setImageResource((is?R.drawable.ctrl_power_btn_on:R.drawable.ctrl_power_btn_off));
        try{
            if(is){
                if(dataObj.has("on")){
                    //tv_name.setText(dataObj.getJSONObject("on").getString("name"));
                }
            }else{
                if(dataObj.has("off")){
                    //tv_name.setText(dataObj.getJSONObject("off").getString("name"));
                }
            }
        }catch(Exception e){e.printStackTrace();}
    }

    private OnClickListener onClick = new OnClickListener(){
        @Override
        public void onClick(View view) {
            try{
                setOnOff(!isOn);
                listener.onClick(gp, (isOn?dataObj.getJSONObject("on"):dataObj.getJSONObject("off")));
            }catch(Exception e){e.printStackTrace();}
        }
    };


    public void updateStatus(String g, String cid, String v, long t){
        //Log.e(TAG, "updateStatus: "+gp+" : "+cid+" : "+v);
        if(!gp.contentEquals(g)) return;
        try{
            //Log.e(TAG, "updateStatus: "+dataObj);
            if(dataObj.has("on") && dataObj.getJSONObject("on").getString("id").contentEquals(cid)){
                setOnOff(true);
            }else if(dataObj.has("off") && dataObj.getJSONObject("off").getString("id").contentEquals(cid)){
                setOnOff(false);
            }
        }catch(Exception e){e.printStackTrace();}
    }
}
