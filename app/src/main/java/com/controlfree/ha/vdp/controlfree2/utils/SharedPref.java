package com.controlfree.ha.vdp.controlfree2.utils;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPref {
    public static final String PREF_NAME = "config";
    public static boolean write(Context c, String key, String value){
        return c.getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit().putString(key, value).commit();
    }
    public static String read(Context c, String key){
        return c.getSharedPreferences(PREF_NAME, MODE_PRIVATE).getString(key, "");
    }
}
