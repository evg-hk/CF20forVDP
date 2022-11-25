package com.controlfree.ha.vdp.controlfree2.control;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import com.controlfree.ha.vdp.controlfree2.component.ExTextView;
import com.controlfree.ha.vdp.controlfree2.utils.Fun;
import com.controlfree.ha.vdp.controlfree2.utils.ResolutionHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;

public class Slider extends FrameLayout {
    private final static String TAG = "StatusText";
    private Context c;
    private int W = 0, H = 0;
    private JSONObject dataObj = new JSONObject();
    private ExTextView et_name, et_status;
    private String gp = "", unit = "";
    private SeekBar bar;
    private double max, min;

    private double progress = 0, maxProgress = 1000;
    private long time = 0;
    private boolean isPressed = false, is_run = true;
    private int afterPressDelay = 40;

    private Listener listener;
    public static interface Listener{
        public void onChange(JSONObject obj, double progress);
    }
    public Slider(Context context, int w, int h, Listener l){
        super(context);
        this.c = context;
        W = w;
        H = h;
        this.listener = l;
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

        FrameLayout.LayoutParams p_b = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        p_b.gravity = Gravity.BOTTOM;
        p_b.bottomMargin = h/6;
        SeekBar b = new SeekBar(c);
        b.setMax((int)maxProgress);
        b.setProgress(0);
        b.setOnSeekBarChangeListener(onChange);
        addView(b, p_b);
        bar = b;

        loop();
    }

    private SeekBar.OnSeekBarChangeListener onChange = new SeekBar.OnSeekBarChangeListener(){
        @Override
        public void onProgressChanged(SeekBar seekBar, int p, boolean fromUser) {
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            time = System.currentTimeMillis();
            isPressed = true;
            afterPressDelay = 0;
        }
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            isPressed = false;
            progress = ((float)bar.getProgress())/maxProgress;
            listener.onChange(dataObj, progress);
        }
    };
    private void loop(){
        if(is_run){
            double sbProgress = ((double)bar.getProgress())/maxProgress;
            if(isPressed){
                if(System.currentTimeMillis()-time>500 && progress!=sbProgress){
                    time = System.currentTimeMillis();
                    progress = sbProgress;
                    listener.onChange(dataObj, progress);
                }
                updateInfo(sbProgress);
            }else if(afterPressDelay<40){
                afterPressDelay++;
                updateInfo(sbProgress);
            }else{
                if(Math.abs(progress-sbProgress)>0.005){
                    double p = (sbProgress+(progress-sbProgress)*0.5);
                    bar.setProgress((int)(p*maxProgress));
                    updateInfo(p);
                }else{
                    bar.setProgress((int)(progress*maxProgress));
                    updateInfo(progress);
                }
            }
            this.postDelayed(new Runnable(){
                @Override
                public void run() {
                    loop();
                }
            }, 50);
        }
    }
    private void updateInfo(double p){
        et_status.setText(""+(int)(min+p*(max-min))+" "+unit);
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
        }catch(Exception e){e.printStackTrace();}
    }

    public void updateStatus(String g, String cid, String v, long t){
        //Log.e(TAG, "updateStatus: "+gp+" : "+cid+" : "+v);
        if(!gp.contentEquals(g)) return;
        try{
            if (dataObj.getString("id").contentEquals(cid)) {
                progress = Double.parseDouble(v);
                if(progress>1) progress = 1;
                if(progress<0) progress = 0;
            }
        }catch(Exception e){e.printStackTrace();}
    }
    public void updateStatusByPower(String g, String cid, String v, long t){
        try{
            if (dataObj.has("off") && dataObj.getJSONObject("off").getString("id").contentEquals(cid)) {
                progress = 0;
            }else if (dataObj.has("on") && dataObj.getJSONObject("on").getString("id").contentEquals(cid)) {
                //progress = 1;
            }

        }catch(Exception e){e.printStackTrace();}
    }
}