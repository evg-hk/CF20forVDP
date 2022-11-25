package com.controlfree.ha.vdp.controlfree2.view;

import android.content.Context;
import android.graphics.Typeface;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.controlfree.ha.vdp.controlfree2.R;
import com.controlfree.ha.vdp.controlfree2.component.ExButton;
import com.controlfree.ha.vdp.controlfree2.component.ExImageView;
import com.controlfree.ha.vdp.controlfree2.component.ExInputView;
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

public class RegisterView extends FrameLayout {
    private final static String TAG = "RegisterView";
    private Context c;
    private List<ExInputView> inputList = new LinkedList<ExInputView>();
    private ExButton loginBtn;

    private Listener listener;
    public static interface Listener{
        public void onBack();
    }
    public RegisterView(Context context, Listener l){
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

        ScrollView.LayoutParams p_ll_head = new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT);
        LinearLayout ll_head = new LinearLayout(c);
        ll_head.setOrientation(LinearLayout.HORIZONTAL);

        ExTextView et = new ExTextView(c, "Register");
        et.setTextSize(ResolutionHandler.fontsize_large);
        et.setTypeface(null, Typeface.BOLD);
        int ivH = et.getViewH()/2;
        ExImageView iv = new ExImageView(c, ivH, ivH);
        iv.setImageResource(R.drawable.btn_arrow_left);
        iv.setDeepGrayCircleIcon();
        iv.setOnClickListener(onClick_back);
        ((LinearLayout.LayoutParams)iv.getLayoutParams()).gravity = Gravity.CENTER;
        ll_head.addView(iv);
        ((LinearLayout.LayoutParams)et.getLayoutParams()).leftMargin = ResolutionHandler.getPaddingW()/2;
        ll_head.addView(et);

        ll.addView(ll_head, p_ll_head);

        ExTextView et2 = new ExTextView(c, "A confirmation email will be sent");
        et2.setTextColor(0xff666666);
        ((LinearLayout.LayoutParams)et2.getLayoutParams()).bottomMargin = ResolutionHandler.getW(0.05f);
        ll.addView(et2);

        ExInputView ein3 = new ExInputView(c, "FIRST NAME", "");
        ((LinearLayout.LayoutParams)ein3.getLayoutParams()).bottomMargin = ResolutionHandler.getW(0.05f);
        ll.addView(ein3);
        inputList.add(ein3);

        ExInputView ein4 = new ExInputView(c, "LAST NAME", "");
        ((LinearLayout.LayoutParams)ein4.getLayoutParams()).bottomMargin = ResolutionHandler.getW(0.05f);
        ll.addView(ein4);
        inputList.add(ein4);

        ExInputView ein = new ExInputView(c, "EMAIL", "e.g. abc@example.com");
        ein.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        ((LinearLayout.LayoutParams)ein.getLayoutParams()).bottomMargin = ResolutionHandler.getW(0.05f);
        ll.addView(ein);
        inputList.add(ein);
        //ein.setText("vtfd96u7@gmail.com");

        ExInputView ein2 = new ExInputView(c, "PASSWORD", "");
        ein2.setAsPassword();
        ((LinearLayout.LayoutParams)ein2.getLayoutParams()).bottomMargin = ResolutionHandler.getW(0.05f);
        ll.addView(ein2);
        inputList.add(ein2);
        //ein2.setText("qqqqqqqq");

        ExInputView ein5 = new ExInputView(c, "CONFIRM PASSWORD", "");
        ein5.setAsPassword();
        ((LinearLayout.LayoutParams)ein5.getLayoutParams()).bottomMargin = ResolutionHandler.getW(0.05f);
        ll.addView(ein5);
        inputList.add(ein5);
        //ein5.setText("qqqqqqqq");

        ExButton btn = new ExButton(c, "REGISTER");
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    String first_name = inputList.get(0).getText().trim();
                    String last_name = inputList.get(1).getText().trim();
                    String email = inputList.get(2).getText().trim().toLowerCase();
                    String password = inputList.get(3).getText().trim();
                    String password2 = inputList.get(4).getText().trim();
                    if(first_name.contentEquals("")){
                        Fun.showAlert(c, "Please enter first name");
                        return;
                    }else if(last_name.contentEquals("")){
                        Fun.showAlert(c, "Please enter last name");
                        return;
                    }else if(email.contentEquals("")){
                        Fun.showAlert(c, "Please enter email");
                        return;
                    }else if(password.contentEquals("")){
                        Fun.showAlert(c, "Please enter password");
                        return;
                    }else if(password.length()<8){
                        Fun.showAlert(c, "Password length must at least 8");
                        return;
                    }else if(!password.contentEquals(password2)){
                        Fun.showAlert(c, "Confirm password is not same as password");
                        return;
                    }else if(((ExButton)view).isLoading()){
                        return;
                    }

                    ((ExButton)view).setLoading(true);
                    Api.register(c, first_name, last_name, email, password, new Api.Listener(){
                        @Override
                        public void onSuccess(JSONObject reqObj, JSONObject obj, JSONArray arr, JSONObject resObj) {
                            loginBtn.setLoading(false);
                            Fun.showAlert(c, "Please check your mailbox within 30 minutes to verify your account",
                                    "Check your junk mailbox as well if you did not see the mail");
                        }
                        @Override
                        public void onFail(String msg, JSONObject resObj) {
                            loginBtn.setLoading(false);
                            Fun.showAlert(c, Fun.getErrorMsg(msg));
                        }
                    });
                }catch(Exception e){e.printStackTrace();loginBtn.setLoading(false);}
            }
        });
        ll.addView(btn);
        loginBtn = btn;

        sv.addView(ll, p_ll);
        addView(sv, p_f);
    }


    private OnClickListener onClick_back = new OnClickListener(){
        @Override
        public void onClick(View view) {
            listener.onBack();
        }
    };
}
