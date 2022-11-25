package com.controlfree.ha.vdp.controlfree2.component;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import com.controlfree.ha.vdp.controlfree2.R;

import androidx.appcompat.widget.AppCompatImageView;

public class ExImageButton extends AppCompatImageView {
    public ExImageButton(Context c, int id){
        super(c);
        setScaleType(ScaleType.CENTER_INSIDE);
        setBackgroundResource(R.drawable.white_bg);
        setImageResource(id);

        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        //p.gravity = Gravity.RIGHT;
        setLayoutParams(p);

        setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        v.setAlpha(0.6f);
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        v.setAlpha(1f);
                        break;
                    }
                    default: {
                        v.setAlpha(1f);
                        break;
                    }
                }
                return false;
            }
        });
    }
    public void setPaddingH(int h){
        setPadding(0, h, 0, h);
    }
}
