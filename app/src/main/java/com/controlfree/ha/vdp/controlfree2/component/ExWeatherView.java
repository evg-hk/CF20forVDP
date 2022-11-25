package com.controlfree.ha.vdp.controlfree2.component;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.controlfree.ha.vdp.controlfree2.R;
import com.controlfree.ha.vdp.controlfree2.utils.Api;
import com.controlfree.ha.vdp.controlfree2.utils.ResolutionHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;

public class ExWeatherView extends LinearLayout {
    public static String TAG = "ExWeatherView";
    private Context c;
    private boolean is_run = true;
    private ExTextView tv_temp, tv_humi;
    private Listener listener;
    public static interface Listener{
        public void onWeather(JSONObject obj);
    }
    public ExWeatherView(Context context, Listener l){
        super(context);
        this.c = context;
        this.listener = l;
        setOrientation(LinearLayout.HORIZONTAL);
        setPadding(ResolutionHandler.getRefW(0.05f), ResolutionHandler.getRefW(0.02f), ResolutionHandler.getRefW(0.05f), ResolutionHandler.getRefW(0.02f));

        setBackgroundResource(R.drawable.white_bg_alpha);

        ExTextView t = new ExTextView(c, "");
        t.setTextColor(0xff222222);
        t.setSingleLine();
        ((LinearLayout.LayoutParams)t.getLayoutParams()).width = LinearLayout.LayoutParams.WRAP_CONTENT;
        addView(t);
        tv_temp = t;

        LinearLayout.LayoutParams p_line = new LinearLayout.LayoutParams(3, LayoutParams.MATCH_PARENT);
        p_line.topMargin = 5;
        p_line.bottomMargin = 5;
        p_line.leftMargin = ResolutionHandler.getRefW(0.04f);
        p_line.rightMargin = ResolutionHandler.getRefW(0.04f);
        View line = new View(c);
        line.setBackgroundColor(0xff888888);
        addView(line, p_line);

        ExTextView h = new ExTextView(c, "");
        h.setTextColor(0xff222222);
        h.setSingleLine();
        ((LinearLayout.LayoutParams)h.getLayoutParams()).width = LinearLayout.LayoutParams.WRAP_CONTENT;
        addView(h);
        tv_humi = h;

        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        setLayoutParams(p);

        loop();
    }

    private long lastTime = 0;
    private void loop(){
        if(is_run){
            if(System.currentTimeMillis()-lastTime>20*60*1000){
                lastTime = System.currentTimeMillis();
                Api.getWeather(c, new Api.Listener(){
                    @Override
                    public void onSuccess(JSONObject reqObj, JSONObject obj, JSONArray arr, JSONObject resObj) {
                        //Log.e(TAG, ""+obj);
                        try{
                            double temp = Double.parseDouble(obj.getString("temp"));
                            double humi = Double.parseDouble(obj.getString("humi"));
                            tv_temp.setText(new DecimalFormat("#.#").format(temp)+" ℃");
                            tv_humi.setText((int)humi+" %");
                            ExWeatherView.this.setVisibility(View.VISIBLE);
                            listener.onWeather(obj);
                        }catch(Exception e){e.printStackTrace();}
                    }
                    @Override
                    public void onFail(String msg, JSONObject resObj) {

                    }
                });
            }
            new Thread(new Runnable(){
                @Override
                public void run() {
                    try{
                        Thread.sleep(60*1000);
                    }catch(Exception e){}
                    loop();
                }
            }).start();
        }
    }
    public void end(){
        is_run = false;
    }
}
