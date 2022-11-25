package com.controlfree.ha.vdp.controlfree2.component;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.widget.LinearLayout;

import com.controlfree.ha.vdp.controlfree2.R;
import com.controlfree.ha.vdp.controlfree2.utils.ResolutionHandler;

import androidx.appcompat.widget.AppCompatImageView;

public class ExImageView extends AppCompatImageView {
    private int W, H;
    public ExImageView(Context c, int w, int h){
        super(c);
        this.W = w;
        this.H = h;
        setScaleType(ScaleType.CENTER_CROP);
        setClipToOutline(true);

        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(w, h);
        setLayoutParams(p);
    }
    public ExImageView(Context c, int id, int w, int h){
        super(c);
        this.W = w;
        this.H = h;
        setImageResource(id);
        setScaleType(ScaleType.CENTER_INSIDE);

        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(w, h);
        setLayoutParams(p);
    }

    public void setCircleIcon(){
        this.setBackgroundResource(R.drawable.white_circle_bg);
        this.setPadding((int)(W/5), (int)(W/5), (int)(W/5), (int)(W/5));
    }
    public void setGrayCircleIcon(){
        setBgGray();
        this.setPadding((int)(W/4), (int)(W/4), (int)(W/4), (int)(W/4));
    }
    public void setGray2CircleIcon(){
        setBgGray2();
        this.setPadding((int)(W/3), (int)(W/3), (int)(W/3), (int)(W/3));
    }
    public void setDeepGrayCircleIcon(){
        setBgDeepGray();
        this.setPadding((int)(W/4), (int)(W/4), (int)(W/4), (int)(W/4));
    }
    public void setBgGray(){
        this.setBackgroundResource(R.drawable.gray_circle_bg);
    }
    public void setBgGray2(){
        this.setBackgroundResource(R.drawable.gray2_circle_bg);
    }
    public void setBgDeepGray(){
        this.setBackgroundResource(R.drawable.deep_gray_circle_bg);
    }
    public void fitStart(){
        setScaleType(ScaleType.FIT_START);
    }
    public int getBottomAvgColor(){
        if(this.getDrawable()==null) return 0;
        Bitmap bmp = ((BitmapDrawable)this.getDrawable()).getBitmap();
        if(bmp==null) return 0;
        int r = 0, g = 0, b = 0, tot = 0;
        for(int j=0;j<bmp.getHeight();j+=4) {
            for (int i = 0; i < bmp.getWidth(); i+=4) {
                int c = bmp.getPixel(i, j);
                r += ((c & 0xff0000) >> 16);
                g += ((c & 0xff00) >> 8);
                b += ((c & 0xff));
                tot++;
            }
        }
        return 0xff000000 | (((r/tot) & 0xff) << 16)
                | (((g/tot) & 0xff) << 8)
                | (((b/tot) & 0xff));
    }

    public void setTintGray(){
        setColorFilter(0xff808080);
    }
    public void setTintDeepGray(){
        setColorFilter(0xff111113);
    }
    public void setImg(int id){
        setImageResource(id);
    }
}
