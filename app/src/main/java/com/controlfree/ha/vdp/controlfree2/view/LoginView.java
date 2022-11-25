package com.controlfree.ha.vdp.controlfree2.view;

import android.content.Context;
import android.graphics.Typeface;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.controlfree.ha.vdp.controlfree2.component.ExButton;
import com.controlfree.ha.vdp.controlfree2.component.ExInputView;
import com.controlfree.ha.vdp.controlfree2.component.ExPageNavView;
import com.controlfree.ha.vdp.controlfree2.component.ExTextView;
import com.controlfree.ha.vdp.controlfree2.component.ExThinLineView;
import com.controlfree.ha.vdp.controlfree2.utils.Api;
import com.controlfree.ha.vdp.controlfree2.utils.DB;
import com.controlfree.ha.vdp.controlfree2.utils.Fun;
import com.controlfree.ha.vdp.controlfree2.utils.ResolutionHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

public class LoginView extends FrameLayout {
    private final static String TAG = "LoginView";
    private Context c;
    private List<ExInputView> inputList = new LinkedList<ExInputView>();
    private ExButton loginBtn;

    private Listener listener;
    public static interface Listener{
        public void onLogin();
        public void onRegister();
    }
    public LoginView(Context context, Listener l){
        super(context);
        this.c = context;
        this.listener = l;
        FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        setLayoutParams(p);

        FrameLayout.LayoutParams p_f = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        ScrollView sv = new ScrollView(c);

        ScrollView.LayoutParams p_ll = new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT);
        p_ll.setMargins(ResolutionHandler.getPaddingW(), ResolutionHandler.getPaddingW()*2, ResolutionHandler.getPaddingW(), ResolutionHandler.getPaddingW()*2/3);
        LinearLayout ll = new LinearLayout(c);
        ll.setOrientation(LinearLayout.VERTICAL);

        ExTextView et = new ExTextView(c, "ControlFree 2.0");
        et.setTextSize(ResolutionHandler.fontsize_large);
        et.setTypeface(null, Typeface.BOLD);
        et.setTextColor(0xffffffff);
        ll.addView(et);
        ExTextView et2 = new ExTextView(c, "Please login to continue");
        et2.setTextColor(0xffcccccc);
        ((LinearLayout.LayoutParams)et2.getLayoutParams()).bottomMargin = ResolutionHandler.getPaddingW();
        ll.addView(et2);

        ExInputView ein = new ExInputView(c, "EMAIL", "e.g. abc@example.com");
        ein.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        ((LinearLayout.LayoutParams)ein.getLayoutParams()).bottomMargin = ResolutionHandler.getPaddingW();
        ll.addView(ein);
        inputList.add(ein);
        //ein.setText("vtfd96u6@gmail.com");

        ExInputView ein2 = new ExInputView(c, "PASSWORD", "");
        ein2.setAsPassword();
        ((LinearLayout.LayoutParams)ein2.getLayoutParams()).bottomMargin = ResolutionHandler.getPaddingW();
        ll.addView(ein2);
        inputList.add(ein2);
        //ein2.setText("qqqqqqqq");

        ExButton btn = new ExButton(c, "LOGIN");
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    String email = inputList.get(0).getText().trim().toLowerCase();
                    String password = inputList.get(1).getText().trim();
                    if(email.contentEquals("")){
                        Fun.showAlert(c, "Please enter email");
                        return;
                    }else if(password.contentEquals("")){
                        Fun.showAlert(c, "Please enter password");
                        return;
                    }else if(((ExButton)view).isLoading()){
                        return;
                    }

                    ((ExButton)view).setLoading(true);
                    Api.login(c, email, password, new Api.Listener(){
                        @Override
                        public void onSuccess(JSONObject reqObj, JSONObject obj, JSONArray arr, JSONObject resObj) {
                            loginBtn.setLoading(false);
                            //Log.e(TAG, "login: onSuccess: "+resObj);
                            try{
                                if(!obj.getBoolean("associated")){
                                    if(Integer.parseInt(obj.getString("count"))>=Integer.parseInt(obj.getString("limit"))){
                                        //Fun.showAlert(c, Fun.getErrorMsg(msg));
                                        return;
                                    }
                                }
                                loginWithCid(""+obj.getInt("cid"));
                            }catch(Exception e){e.printStackTrace();}
                        }
                        @Override
                        public void onFail(String msg, JSONObject resObj) {
                            loginBtn.setLoading(false);
                            if(msg.contentEquals("invalid")){
                                Fun.showAlert(c, "Invalid email or password");
                            }else if(msg.contentEquals("client_full")){
                                int limit = 1;
                                try{
                                    if(resObj.has("limit")){
                                        limit = Integer.parseInt(""+resObj.get("limit"));
                                    }
                                }catch(Exception e){e.printStackTrace();}
                                Fun.showAlert(c, "Max. "+limit+" device per account. You have reached the limit");
                            }else {
                                Fun.showAlert(c, Fun.getErrorMsg(msg));
                            }
                        }
                    });
                }catch(Exception e){e.printStackTrace();loginBtn.setLoading(false);}
            }
        });
        ll.addView(btn);
        loginBtn = btn;



        //bottom ------------------

        ExThinLineView v = new ExThinLineView(c);
        ((LinearLayout.LayoutParams)v.getLayoutParams()).topMargin = ResolutionHandler.getH(0.4f);
        ll.addView(v);

        LinearLayout.LayoutParams p_ll_create = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout ll_create = new LinearLayout(c);
        ll_create.setOrientation(LinearLayout.HORIZONTAL);

        ExTextView et3 = new ExTextView(c, "Don't have account?");
        et3.setTextColor(0xffffffff);
        et3.setEllipsize(TextUtils.TruncateAt.END);
        //((LinearLayout.LayoutParams)et2.getLayoutParams()).bottomMargin = ResolutionHandler.getW(0.05f);
        ((LinearLayout.LayoutParams)et3.getLayoutParams()).width = LinearLayout.LayoutParams.WRAP_CONTENT;
        ll_create.addView(et3);

        ExTextView et4 = new ExTextView(c, "Create");
        et4.setTextColor(0xffcccccc);
        et4.setBold();
        et4.setEllipsize(TextUtils.TruncateAt.END);
        et4.setOnClickListener(onClick_reg);
        ((LinearLayout.LayoutParams)et4.getLayoutParams()).bottomMargin = ResolutionHandler.getH(0.2f);
        ((LinearLayout.LayoutParams)et4.getLayoutParams()).leftMargin = ResolutionHandler.getPaddingW();
        ((LinearLayout.LayoutParams)et4.getLayoutParams()).width = LinearLayout.LayoutParams.WRAP_CONTENT;
        ll_create.addView(et4);

        ll.addView(ll_create, p_ll_create);

        sv.addView(ll, p_ll);
        addView(sv, p_f);

        String email = DB.getSetting(c, "login_email");
        String password = DB.getSetting(c, "login_pwd");
        String cid = DB.getSetting(c, "login_cid");
        if(!email.contentEquals("") && !password.contentEquals("") && !cid.contentEquals("")){
            //Log.e(TAG, "loginWithCid: "+email+" : "+password+" : "+cid);
            inputList.get(0).setText(email);
            inputList.get(1).setText(password);
            loginWithCid(cid);
        }
    }

    private void loginWithCid(String cid){
        loginBtn.setLoading(true);
        try{
            String email = inputList.get(0).getText().trim().toLowerCase();
            String password = inputList.get(1).getText().trim();

            Api.loginWithCid(c, email, password, cid, new Api.Listener(){
                @Override
                public void onSuccess(JSONObject reqObj, JSONObject obj, JSONArray arr, JSONObject resObj) {
                    loginBtn.setLoading(false);
                    //Log.e(TAG, "onSuccess: "+obj);
                    try{
                        if(obj.has("token")){
                            DB.token = obj.getString("token");
                            DB.setSetting(c, "login_email", email);
                            DB.setSetting(c, "login_pwd", password);
                            DB.setSetting(c, "login_cid", cid);
                            listener.onLogin();
                        }
                    }catch(Exception e){e.printStackTrace();}
                }
                @Override
                public void onFail(String msg, JSONObject resObj) {
                    loginBtn.setLoading(false);
                    Fun.showAlert(c, Fun.getErrorMsg(msg));
                    //Log.e(TAG, "onFail: "+resObj);
                }
            });
        }catch(Exception e){e.printStackTrace();loginBtn.setLoading(false);}
    }


    private OnClickListener onClick_reg = new OnClickListener(){
        @Override
        public void onClick(View view) {
            listener.onRegister();
        }
    };
}
