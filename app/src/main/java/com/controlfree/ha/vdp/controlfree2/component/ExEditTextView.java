package com.controlfree.ha.vdp.controlfree2.component;

import android.content.Context;
import android.util.AttributeSet;

import com.controlfree.ha.vdp.controlfree2.utils.ResolutionHandler;

import androidx.appcompat.widget.AppCompatEditText;

public class ExEditTextView extends AppCompatEditText {
    public ExEditTextView(Context c, String hint){
        super(c, null, android.R.attr.editTextStyle);
        setHint(hint);
        setTextColor(0xff454545);
        setHintTextColor(0xffbbbbbb);
        setBackgroundColor(0x00000000);
        setTextSize(ResolutionHandler.fontsize_small);
    }
}
