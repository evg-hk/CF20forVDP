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
import com.controlfree.ha.vdp.controlfree2.component.ExRadioItemView;
import com.controlfree.ha.vdp.controlfree2.component.ExShortLine;
import com.controlfree.ha.vdp.controlfree2.component.ExTextItemView;
import com.controlfree.ha.vdp.controlfree2.component.ExTextView;
import com.controlfree.ha.vdp.controlfree2.component.ExThinLineView;
import com.controlfree.ha.vdp.controlfree2.component.ExWeatherView;
import com.controlfree.ha.vdp.controlfree2.utils.Api;
import com.controlfree.ha.vdp.controlfree2.utils.Cache;
import com.controlfree.ha.vdp.controlfree2.utils.DB;
import com.controlfree.ha.vdp.controlfree2.utils.Fun;
import com.controlfree.ha.vdp.controlfree2.utils.ResolutionHandler;

import org.json.JSONArray;
import org.json.JSONObject;

public class ProfileView extends FrameLayout {
    private final static String TAG = "ProfileView";
    private Context c;

    private Listener listener;
    public static interface Listener{
        public void onSelect_server(JSONObject obj);
        public void onLogout();
    }
    public ProfileView(Context context, Listener l){
        super(context);
        this.c = context;
        this.listener = l;
        setBackgroundColor(0xffeeeeee);
        FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        setLayoutParams(p);

        FrameLayout.LayoutParams p_f = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        ScrollView sv = new ScrollView(c);

        ScrollView.LayoutParams p_ll = new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT);
        LinearLayout ll = new LinearLayout(c);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setPadding(ResolutionHandler.getPaddingW(), ResolutionHandler.getPaddingW(), ResolutionHandler.getPaddingW(), ResolutionHandler.getBottomTabH());

        ExTextView t = new ExTextView(c, "My Account");
        t.setTextColor(0xff888888);
        t.setTypeface(null, Typeface.BOLD);
        ll.addView(t);

        ExThinLineView l0 = new ExThinLineView(c);
        ll.addView(l0);

        ExTextView email = new ExTextView(c, DB.getSetting(c, "login_email"));
        email.setBold();
        ll.addView(email);

        //-----

        ExTextView t2 = new ExTextView(c, "Sites");
        t2.setTextColor(0xff888888);
        t2.setTypeface(null, Typeface.BOLD);
        ((LinearLayout.LayoutParams)t2.getLayoutParams()).topMargin = ResolutionHandler.getPaddingW();
        ll.addView(t2);

        ExThinLineView l3 = new ExThinLineView(c);
        ll.addView(l3);

        JSONArray arr = Cache.getArr(c, "server_list");
        if(arr!=null){
            try {
                String gw_id = DB.getSetting(c, "gw_id");
                for (int i = 0; i < arr.length(); i++) {
                    if(i>0){
                        ExThinLineView l2 = new ExThinLineView(c);
                        ll.addView(l2);
                    }
                    ExRadioItemView item = new ExRadioItemView(c, ResolutionHandler.getViewPortW()-ResolutionHandler.getPaddingW()*2, arr.getJSONObject(i).getString("name"),
                            arr.getJSONObject(i).getString("gw_id").contentEquals(gw_id));
                    item.setTag(i);
                    ll.addView(item);
                    item.setOnClickListener(onClick_item);
                }
            }catch(Exception e){e.printStackTrace();}
        }

        //-----

        ExTextView t3 = new ExTextView(c, "Setting");
        t3.setTextColor(0xff888888);
        t3.setTypeface(null, Typeface.BOLD);
        ((LinearLayout.LayoutParams)t3.getLayoutParams()).topMargin = ResolutionHandler.getContentW(0.1f);
        ll.addView(t3);

        ExThinLineView l4 = new ExThinLineView(c);
        ll.addView(l4);

        ExTextItemView logout = new ExTextItemView(c, ResolutionHandler.getViewPortW()-ResolutionHandler.getPaddingW()*2, "Logout");
        logout.setTextColor(0xffcc0000);
        ll.addView(logout);
        logout.setOnClickListener(onClick_logout);

        ExThinLineView l5 = new ExThinLineView(c);
        ll.addView(l5);

        ExTextView t_b = new ExTextView(c, "v"+ Fun.version+" @2021 by Control Free Ltd.\n"+Cache.get("ssid"));
        t_b.setTextColor(0xff888888);
        t_b.setTextSize(ResolutionHandler.fontsize_small);
        t_b.setLineSpacing(6, 1);
        ll.addView(t_b);

        sv.addView(ll, p_ll);
        addView(sv, p_f);
    }
    private OnClickListener onClick_item = new OnClickListener(){
        @Override
        public void onClick(View view) {
            try{
                int index = (int)view.getTag();

                JSONArray arr = Cache.getArr(c, "server_list");
                if(arr!=null){
                    if(index<arr.length()){
                        listener.onSelect_server(arr.getJSONObject(index));
                    }
                }
            }catch(Exception e){e.printStackTrace();}
        }
    };
    private OnClickListener onClick_logout = new OnClickListener(){
        @Override
        public void onClick(View view) {
            Fun.showConfirm(c, "Are you sure to logout?", new Fun.Listener(){
                @Override
                public void onYes() {
                    listener.onLogout();
                }
            });
        }
    };
}
