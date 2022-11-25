package com.controlfree.ha.vdp.controlfree2.view;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

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

public class TempAcDeviceControlView extends DeviceControlView {
    private final static String TAG = "TempAcDeviceControlView";
    private Context c;

    public TempAcDeviceControlView(Context context, int w, int h, DeviceControlView.Listener l) {
        super(context, w, h, l);
        this.c = context;
    }
    @Override
    public void setData(JSONObject obj){
        dataObj = obj;
        try{
            ll_control.removeAllViews();
            scrollView.scrollTo(0, 0);
            JSONArray cArr = obj.getJSONArray("control");
            if(cArr.length()>=2){
                LinearLayout.LayoutParams p_f = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                FrameLayout f = new FrameLayout(c);
                f.setBackgroundResource(R.drawable.dev_bg);
                ll_control.addView(f, p_f);

                FrameLayout.LayoutParams p_tv_tm = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                p_tv_tm.topMargin = ResolutionHandler.getPaddingW();
                p_tv_tm.bottomMargin = ResolutionHandler.getPaddingW();
                p_tv_tm.leftMargin = ResolutionHandler.getPaddingW()/2;
                ExTextView tv_tm = new ExTextView(c, "18℃");
                tv_tm.setTextSize(ResolutionHandler.fontsize_xxxlarge);
                f.addView(tv_tm, p_tv_tm);

                FrameLayout.LayoutParams p_tv_sp = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                p_tv_sp.rightMargin = ResolutionHandler.getPaddingW()/2;
                p_tv_sp.topMargin = ResolutionHandler.getPaddingW()/2;
                p_tv_sp.gravity = Gravity.RIGHT;
                ExTextView tv_sp = new ExTextView(c, "HIGH");
                tv_sp.setTextSize(ResolutionHandler.fontsize_default);
                f.addView(tv_sp, p_tv_sp);

                LinearLayout.LayoutParams p_temp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                p_temp.topMargin = ResolutionHandler.getPaddingW();
                LinearLayout temp = new LinearLayout(c);
                temp.setOrientation(LinearLayout.HORIZONTAL);
                ll_control.addView(temp, p_temp);

                ExImageView tv_min = new ExImageView(c, R.drawable.ctrl_minus, ResolutionHandler.getPaddingW()*5, ResolutionHandler.getPaddingW()*3/2);
                tv_min.setPadding(0, ResolutionHandler.getPaddingW()/5, 0, ResolutionHandler.getPaddingW()/5);
                tv_min.setBackgroundResource(R.drawable.ac_btn_bg);
                ((LinearLayout.LayoutParams)tv_min.getLayoutParams()).rightMargin = ResolutionHandler.getPaddingW()/2;
                temp.addView(tv_min);

                ExImageView tv_add = new ExImageView(c, R.drawable.ctrl_plus, ResolutionHandler.getPaddingW()*5, ResolutionHandler.getPaddingW()*3/2);
                tv_add.setPadding(0, ResolutionHandler.getPaddingW()/5, 0, ResolutionHandler.getPaddingW()/5);
                tv_add.setBackgroundResource(R.drawable.ac_btn_bg_alpha);
                ((LinearLayout.LayoutParams)tv_add.getLayoutParams()).rightMargin = ResolutionHandler.getPaddingW()/2;
                temp.addView(tv_add);

                LinearLayout.LayoutParams p_control = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                p_control.topMargin = ResolutionHandler.getPaddingW()*2;
                LinearLayout control = new LinearLayout(c);
                control.setOrientation(LinearLayout.HORIZONTAL);
                ll_control.addView(control, p_control);

                FrameLayout.LayoutParams p_iv_speed = new FrameLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                p_iv_speed.rightMargin = ResolutionHandler.getPaddingW()/2;
                ExImageView iv_speed = new ExImageView(c, R.drawable.ac_speed, ResolutionHandler.getBtnW()*3/2, ResolutionHandler.getBtnW()*3/2);
                iv_speed.setGray2CircleIcon();
                control.addView(iv_speed, p_iv_speed);

                FrameLayout.LayoutParams p_iv_mode = new FrameLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                p_iv_mode.rightMargin = ResolutionHandler.getPaddingW()/2;
                ExImageView iv_mode = new ExImageView(c, R.drawable.ac_mode, ResolutionHandler.getBtnW()*3/2, ResolutionHandler.getBtnW()*3/2);
                iv_mode.setGray2CircleIcon();
                control.addView(iv_mode, p_iv_mode);
            }
        }catch(Exception e){e.printStackTrace();}
    }
}
