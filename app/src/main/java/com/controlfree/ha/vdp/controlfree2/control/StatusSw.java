package com.controlfree.ha.vdp.controlfree2.control;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.controlfree.ha.vdp.controlfree2.R;
import com.controlfree.ha.vdp.controlfree2.component.ExDialog;
import com.controlfree.ha.vdp.controlfree2.component.ExImageView;
import com.controlfree.ha.vdp.controlfree2.component.ExTextView;
import com.controlfree.ha.vdp.controlfree2.utils.ResolutionHandler;

import org.json.JSONArray;
import org.json.JSONObject;

public class StatusSw extends FrameLayout {
    private final static String TAG = "StatusText";
    private Context c;
    private int W = 0, H = 0;
    private JSONArray dataArr = new JSONArray();
    private ExTextView et_menu_value;
    private String gp = "";

    private Listener listener;
    public static interface Listener{
        public void onSelect(String gp, JSONObject obj);
    }
    public StatusSw(Context context, int w, int h, Listener l){
        super(context);
        this.c = context;
        W = w-60;
        H = h;
        this.listener = l;
        setBackgroundResource(R.drawable.white_bg);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(w, h);
        setLayoutParams(p);

        FrameLayout.LayoutParams p_mv = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, h);
        ExTextView mv = new ExTextView(c, "");
        mv.setPadding(ResolutionHandler.getPaddingW(), 0, ResolutionHandler.getPaddingW(), 0);
        mv.setTextAlignment(TEXT_ALIGNMENT_CENTER);
        mv.setOnClickListener(onClick_mValue);
        addView(mv, p_mv);
        et_menu_value = mv;

        int ivW = h/2;
        FrameLayout.LayoutParams p_iv = new FrameLayout.LayoutParams(ivW, ivW);
        p_iv.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
        p_iv.rightMargin = ResolutionHandler.getPaddingW();
        ExImageView iv = new ExImageView(c, ivW, ivW);
        iv.setImageResource(R.drawable.btn_arrow_right);
        addView(iv, p_iv);
    }

    private OnClickListener onClick_mValue = new OnClickListener(){
        @Override
        public void onClick(View view) {
            try{
                CharSequence[] arr = new CharSequence[dataArr.length()];
                for(int i=0;i<dataArr.length();i++){
                    arr[i] = dataArr.getJSONObject(i).getString("name");
                }
                ExDialog.showMenu(c, arr, new ExDialog.Listener(){
                    @Override
                    public void onClick(int index) {
                        try{
                            et_menu_value.setText(dataArr.getJSONObject(index).getString("name"));
                            listener.onSelect(gp, dataArr.getJSONObject(index));
                        }catch(Exception e){e.printStackTrace();}
                    }
                });
            }catch(Exception e){e.printStackTrace();}
        }
    };

    public void setData(String group, JSONArray arr){
        this.gp = group;
        dataArr = arr;
    }

    public void updateStatus(String g, String cid, String v, long t){
        if(!gp.contentEquals(g)) return;
        try{
            for(int i=0;i<dataArr.length();i++) {
                if (dataArr.getJSONObject(i).getString("id").contentEquals(cid)) {
                    et_menu_value.setText(dataArr.getJSONObject(i).getString("name"));
                }
            }
        }catch(Exception e){e.printStackTrace();}
    }
}