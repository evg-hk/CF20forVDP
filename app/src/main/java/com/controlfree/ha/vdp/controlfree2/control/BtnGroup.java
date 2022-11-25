package com.controlfree.ha.vdp.controlfree2.control;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.controlfree.ha.vdp.controlfree2.R;
import com.controlfree.ha.vdp.controlfree2.component.ExButton;
import com.controlfree.ha.vdp.controlfree2.component.ExImageButton;
import com.controlfree.ha.vdp.controlfree2.component.ExImageView;
import com.controlfree.ha.vdp.controlfree2.utils.ResolutionHandler;

import org.json.JSONArray;
import org.json.JSONObject;

public class BtnGroup extends FrameLayout {
    private final static String TAG = "BtnGroup";
    private Context c;
    private int W;
    private JSONArray dataArr;
    private int padding = 0;
    private String gp = "";

    private Listener listener;
    public static interface Listener{
        public void onClick(String gp, JSONObject obj);
        public void onSlide(String gp, JSONObject obj, double val);
    }
    public BtnGroup(Context context, int w, Listener l) {
        super(context);
        this.c = context;
        this.W = w;
        this.listener = l;
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(w, LinearLayout.LayoutParams.WRAP_CONTENT);
        setLayoutParams(p);
        padding = 10;
        //setBackgroundColor(0x3300ff00);
    }
    public void setData(String group, JSONArray arr){
        gp = group;
        dataArr = arr;
        this.removeAllViews();

        if(arr.length()==0) return;

        int widgetH = (int)(ResolutionHandler.getBtnW()*2.4);
        int btnH = (int)(ResolutionHandler.getBtnW()*2);
        int row = 0;
        int h = 0;
        try{
            if(gp.startsWith("power") && arr.length()==2) {
                String name = "Power";
                String n = arr.getJSONObject(0).getString("name");
                if(n.contains(":")){
                    String[] nArr = n.split("\\:");
                    name = nArr[0].trim();
                }else{
                    n = arr.getJSONObject(1).getString("name");
                    if(n.contains(":")){
                        String[] nArr = n.split("\\:");
                        name = nArr[0].trim();
                    }
                }
                PowerBtn btn = new PowerBtn(c, W, name, listener_powerBtn);
                JSONObject dObj = new JSONObject();
                if(arr.getJSONObject(0).getString("code").contentEquals("")) arr.getJSONObject(0).put("code", arr.getJSONObject(1).getString("code"));
                if(arr.getJSONObject(1).getString("code").contentEquals("")) arr.getJSONObject(1).put("code", arr.getJSONObject(0).getString("code"));
                dObj.put("on", arr.getJSONObject(0));
                dObj.put("off", arr.getJSONObject(1));
                btn.setData(gp, dObj);
                addView(btn);

            }else if(getType(arr.getJSONObject(0)).contentEquals("st")){
                StatusText st = new StatusText(c, W, btnH);
                st.setTag(0);
                st.setData(gp, arr);
                addView(st);
                row++;
                h += btnH;

            }else if(getType(arr.getJSONObject(0)).contentEquals("stb")){
                StatusBar st = new StatusBar(c, W, widgetH);
                st.setTag(0);
                st.setData(gp, arr.getJSONObject(0));
                addView(st);
                row++;
                h += widgetH;

            }else if(getType(arr.getJSONObject(0)).contentEquals("ssw")){
                StatusSw st = new StatusSw(c, W, btnH, onSelect_ssw);
                st.setTag(0);
                st.setData(gp, arr);
                addView(st);
                row++;
                h += btnH;

            }else if(getType(arr.getJSONObject(0)).startsWith("sld")){
                Slider st = new Slider(c, W, widgetH, onChange_slider);
                st.setTag(0);
                st.setData(gp, arr.getJSONObject(0));
                addView(st);
                row++;
                h += widgetH;

            }else{
                for(int i=0;i<arr.length();i++){
                    //Log.e(TAG, "ck type: "+getType(arr.getJSONObject(i)));
                    if(getType(arr.getJSONObject(i)).contentEquals("btn")){
                        if(i+1<arr.length() && getType(arr.getJSONObject(i+1)).contentEquals("btn")){
                            if(i+2<arr.length() && getType(arr.getJSONObject(i+2)).contentEquals("btn")){
                                int btnW = (W - 2 * padding)/3;
                                addButton(c, i, btnW, btnH, 0, h, arr.getJSONObject(i).getString("name"));
                                addButton(c, i+1, btnW, btnH, btnW+padding, h, arr.getJSONObject(i+1).getString("name"));
                                addButton(c, i+2, btnW, btnH, (btnW+padding)*2, h, arr.getJSONObject(i+2).getString("name"));
                                i+=2;
                            }else{
                                int btnW = (W - padding)/2;
                                addButton(c, i, btnW, btnH, 0, h, arr.getJSONObject(i).getString("name"));
                                addButton(c, i+1, btnW, btnH, btnW+padding, h, arr.getJSONObject(i+1).getString("name"));
                                i+=1;
                            }
                        }else{
                            int btnW = W;
                            addButton(c, i, btnW, btnH, 0, h, arr.getJSONObject(i).getString("name"));
                        }
                    }
                    row++;
                    h += btnH+padding;
                }
            }
        }catch(Exception e){e.printStackTrace();}

        //Log.e(TAG, "setData: row: "+row+" / "+arr.length());
        //((LinearLayout.LayoutParams)getLayoutParams()).height = h;
    }
    private String getType(JSONObject obj){
        try {
            if (obj.getString("type").contentEquals("")) return "btn";
            return obj.getString("type");
        }catch(Exception e){e.printStackTrace();}
        return "btn";
    }
    private void addButton(Context c, int index, int w, int h, int x, int y, String name){
        FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(w, h);
        p.leftMargin = x;
        p.topMargin = y;
        int rid = 0;
        String n = name.toLowerCase();
        if(n.contentEquals("up")){
            rid = R.drawable.ctrl_arrow_up;
        }else if(n.contentEquals("down")){
            rid = R.drawable.ctrl_arrow_down;
        }else if(n.contentEquals("left")){
            rid = R.drawable.ctrl_arrow_left;
        }else if(n.contentEquals("right")){
            rid = R.drawable.ctrl_arrow_right;
        }else if(n.contentEquals("play")){
            rid = R.drawable.ctrl_play;
        }else if(n.contentEquals("pause")){
            rid = R.drawable.ctrl_pause;
        }else if(n.contentEquals("stop")){
            rid = R.drawable.ctrl_stop;
        }else if(n.contentEquals("forward")){
            rid = R.drawable.ctrl_forward;
        }else if(n.contentEquals("backward")){
            rid = R.drawable.ctrl_backward;
        }else if(n.startsWith("home") || n.endsWith("home")){
            rid = R.drawable.ctrl_home;
        }

        View btn;
        if(rid!=0){
            ExImageButton v = new ExImageButton(c, rid);
            v.setPaddingH((int)(h/4.5));
            btn = v;
        }else {
            ExButton v = new ExButton(c, name);
            if(name.length()>8){
                v.setTextSize(ResolutionHandler.fontsize_small);
            }else if(name.length()>18){
                v.setTextSize(ResolutionHandler.fontsize_xsmall);
            }
            v.setTwoLine();
            v.setPadding(5, 0, 5, 0);
            v.setGravity(Gravity.CENTER_VERTICAL);
            btn = v;
        }
        btn.setTag(index);
        addView(btn, p);
        btn.setOnClickListener(onClick_btn);
    }

    private OnClickListener onClick_btn = new OnClickListener(){
        @Override
        public void onClick(View view) {
            try{
                int index = (int)view.getTag();
                listener.onClick(gp, dataArr.getJSONObject(index));
            }catch(Exception e){e.printStackTrace();}
        }
    };
    private PowerBtn.Listener listener_powerBtn = new PowerBtn.Listener(){
        @Override
        public void onClick(String gp, JSONObject obj) {
            try{
                listener.onClick(gp, obj);
            }catch(Exception e){e.printStackTrace();}
        }
    };
    private Slider.Listener onChange_slider = new Slider.Listener(){
        @Override
        public void onChange(JSONObject obj, double progress) {
            try{
                listener.onSlide(gp, obj, progress);
            }catch(Exception e){e.printStackTrace();}
        }
    };
    private StatusSw.Listener onSelect_ssw = new StatusSw.Listener(){
        @Override
        public void onSelect(String gp, JSONObject obj) {
            listener.onClick(gp, obj);
        }
    };

    public void updateStatus(String g, String cid, String v, long t){
        //Log.e(TAG, "updateStatus: "+gp+" : "+cid+" : "+v);
        if(!gp.contentEquals(g)){
            for(int i=0;i<getChildCount();i++){
                if(getChildAt(i) instanceof Slider){
                    ((Slider)getChildAt(i)).updateStatusByPower(gp, cid, v, t);
                }
            }
            return;
        }
        try{
            for(int i=0;i<getChildCount();i++){
                if(getChildAt(i) instanceof StatusText){
                    ((StatusText)getChildAt(i)).updateStatus(gp, cid, v, t);
                }else if(getChildAt(i) instanceof StatusBar){
                    ((StatusBar)getChildAt(i)).updateStatus(gp, cid, v, t);
                }else if(getChildAt(i) instanceof StatusSw){
                    ((StatusSw)getChildAt(i)).updateStatus(gp, cid, v, t);
                }else if(getChildAt(i) instanceof Slider){
                    ((Slider)getChildAt(i)).updateStatus(gp, cid, v, t);
                }
            }
        }catch(Exception e){e.printStackTrace();}
    }
    public void changeSliderByPower(String g, String cid){
        for(int i=0;i<getChildCount();i++){
            if(getChildAt(i) instanceof Slider){
                ((Slider)getChildAt(i)).updateStatusByPower(gp, cid, "-1", 0);
            }
        }
    }
}
