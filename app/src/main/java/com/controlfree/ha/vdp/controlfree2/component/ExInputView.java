package com.controlfree.ha.vdp.controlfree2.component;

import android.content.Context;
import android.graphics.Typeface;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.controlfree.ha.vdp.controlfree2.R;
import com.controlfree.ha.vdp.controlfree2.utils.ResolutionHandler;

public class ExInputView extends LinearLayout {
    private ExEditTextView valueView;
    public ExInputView(Context c, String title, String hint){
        super(c);
        setOrientation(LinearLayout.VERTICAL);
        //setBackgroundColor(0xffeeeeee);
        setBackgroundResource(R.drawable.white_bg);

        setPadding(ResolutionHandler.getPaddingW(), ResolutionHandler.getPaddingW()*2/3, ResolutionHandler.getPaddingW(), ResolutionHandler.getPaddingW()*2/3);
        LinearLayout.LayoutParams p_t = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        ExTextView t = new ExTextView(c, title);
        t.setTextSize(ResolutionHandler.fontsize_small);
        t.setTextColor(0xff808080);
        t.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View view) {
                try{
                    valueView.requestFocus();
                }catch(Exception e){e.printStackTrace();}
            }
        });
        addView(t, p_t);
        LinearLayout.LayoutParams p_et = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        ExEditTextView et = new ExEditTextView(c, hint);
        et.setPadding(0, ResolutionHandler.getPaddingW()/6, 0, ResolutionHandler.getPaddingW()/6);
        et.setTypeface(null, Typeface.BOLD);
        addView(et, p_et);
        valueView = et;

        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        setLayoutParams(p);
    }

    public void setAsPassword(){
        setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        valueView.setTransformationMethod(PasswordTransformationMethod.getInstance());
        //valueView.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
    }
    public void setInputType(int it){
        valueView.setInputType(it);
    }
    public String getText(){
        return valueView.getText().toString();
    }
    public void setText(String str){
        valueView.setText(str);
    }
}
