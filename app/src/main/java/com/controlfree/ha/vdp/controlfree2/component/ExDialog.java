package com.controlfree.ha.vdp.controlfree2.component;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class ExDialog {
    public static interface Listener{
        public void onClick(int index);
    }
    public static void showMenu(Context c, CharSequence[] dataArr, Listener l){
        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setItems(dataArr , new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which) {
                try{
                    l.onClick(which);
                }catch(Exception e){e.printStackTrace();}
            }
        });
        builder.show();
    }
}
