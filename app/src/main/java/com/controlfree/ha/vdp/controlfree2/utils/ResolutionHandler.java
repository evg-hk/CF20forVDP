package com.controlfree.ha.vdp.controlfree2.utils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import com.controlfree.ha.vdp.controlfree2.R;

import androidx.appcompat.app.AppCompatActivity;

public class ResolutionHandler {
	public static int AUTO_FROM_VIEWPORT = -1;
	private static int w = 0, h = 0;
	private static float d = 0, s = 0;
	private static int paddingW = 0, paddingH = 0;
	public static float fontsize_xxsmall, fontsize_xsmall,fontsize_small,fontsize_default,fontsize_mid,fontsize_midSmall,
			fontsize_midLarge,fontsize_large,fontsize_xlarge,fontsize_xxlarge,fontsize_xxxlarge;
	public static int contentW = 0;
	private static boolean isTablet = false;

	private static int getNavigationBarHeight(WindowManager wMgr) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			DisplayMetrics metrics = new DisplayMetrics();
			wMgr.getDefaultDisplay().getMetrics(metrics);
			int usableHeight = metrics.heightPixels;
			wMgr.getDefaultDisplay().getRealMetrics(metrics);
			int realHeight = metrics.heightPixels;
			if (realHeight > usableHeight)
				return realHeight - usableHeight;
			else
				return 0;
		}
		return 0;
	}
	//call at Activity onCreate
	public static void renew(Context c, WindowManager wMgr){
		isTablet = c.getResources().getBoolean(R.bool.isTablet);
		Log.e("ScreenResolutionHandler", "isTablet: "+(isTablet?"T":"F"));

		DisplayMetrics displaymetrics = new DisplayMetrics();
		wMgr.getDefaultDisplay().getMetrics(displaymetrics);
		h = displaymetrics.heightPixels+getNavigationBarHeight(wMgr);
		w = displaymetrics.widthPixels;
		d = displaymetrics.density;

//		fontsize_xxsmall = 10f;
//		fontsize_xsmall = 11f;
//		fontsize_small = 14f;
//		fontsize_default = 18f;
//		fontsize_mid = 27f;
//		fontsize_midSmall = 33f;
//		fontsize_midLarge = 36f;
//		fontsize_large = 40f;
//		fontsize_xlarge = 48f;
//		fontsize_xxlarge = 67f;
//		fontsize_xxxlarge = 90f;

//		if(isTablet){
//			fontsize_xxsmall *= 0.8f;
//			fontsize_xsmall *= 0.8f;
//			fontsize_small *= 0.8f;
//			fontsize_default *= 0.8f;
//			fontsize_mid *= 0.8f;
//			fontsize_midSmall *= 0.8f;
//			fontsize_midLarge *= 0.8f;
//			fontsize_large *= 0.8f;
//			fontsize_xlarge *= 0.8f;
//			fontsize_xxlarge *= 0.8f;
//			fontsize_xxxlarge *= 0.8f;
//		}

		s = (float)(0.95*Math.sqrt(Math.sqrt((float)w/1600f)));
		
		if(islandscape()){
//			paddingW = ResolutionHandler.getH(0.045f);
//			paddingH = ResolutionHandler.getH(0.025f);
			paddingW = ResolutionHandler.getW(0.025f);
			paddingH = ResolutionHandler.getW(0.015f);
			contentW = ResolutionHandler.getW(0.9f);
		}else{
			paddingW = ResolutionHandler.getW(0.05f);
			paddingH = ResolutionHandler.getW(0.035f);
			contentW = ResolutionHandler.getW(0.9f);
//			fontsize_xxsmall += 4;
//			fontsize_xsmall += 4;
//			fontsize_small += 4;
//			fontsize_default += 4;
//			fontsize_mid += 4;
//			fontsize_midSmall += 4;
//			fontsize_midLarge += 4;
//			fontsize_large += 4;
//			fontsize_xlarge += 4;
//			fontsize_xxlarge += 4;
//			fontsize_xxxlarge += 4;
		}

//		fontsize_xxsmall *= s;
//		fontsize_xsmall *= s;
//		fontsize_small *= s;
//		fontsize_default *= s;
//		fontsize_mid *= s;
//		fontsize_midSmall *= s;
//		fontsize_midLarge *= s;
//		fontsize_large *= s;
//		fontsize_xlarge *= s;
//		fontsize_xxlarge *= s;
//		fontsize_xxxlarge *= s;
//
//		Log.e("ScreenResolutionHandler", "fontsize_xxsmall: "+fontsize_xxsmall);
//		Log.e("ScreenResolutionHandler", "fontsize_xsmall: "+fontsize_xsmall);
//		Log.e("ScreenResolutionHandler", "fontsize_small: "+fontsize_small);
//		Log.e("ScreenResolutionHandler", "fontsize_default: "+fontsize_default);
//		Log.e("ScreenResolutionHandler", "fontsize_mid: "+fontsize_mid);
//		Log.e("ScreenResolutionHandler", "fontsize_midSmall: "+fontsize_midSmall);
//		Log.e("ScreenResolutionHandler", "fontsize_midLarge: "+fontsize_midLarge);
//		Log.e("ScreenResolutionHandler", "fontsize_large: "+fontsize_large);
//		Log.e("ScreenResolutionHandler", "fontsize_xlarge: "+fontsize_xlarge);
//		Log.e("ScreenResolutionHandler", "fontsize_xxlarge: "+fontsize_xxlarge);
//		Log.e("ScreenResolutionHandler", "fontsize_xxxlarge: "+fontsize_xxxlarge);
		fontsize_xxsmall = getContentW(0.0123f);
		fontsize_xsmall = getContentW(0.0133f);
		fontsize_small = getContentW(0.0159f);
		fontsize_default = getContentW(0.0195f);
		fontsize_mid = getContentW(0.0275f);
		fontsize_midSmall = getContentW(0.0328f);
		fontsize_midLarge = getContentW(0.0354f);
		fontsize_large = getContentW(0.039f);
		fontsize_xlarge = getContentW(0.0461f);
		fontsize_xxlarge = getContentW(0.0629f);
		fontsize_xxxlarge = getContentW(0.0833f);

		float f_r = 1f*2.75f/d;
		if(islandscape()){
			f_r *= 0.6f;
		}
		if(isTablet()){
			f_r *= 0.8f;
		}
		fontsize_xxsmall *= f_r;
		fontsize_xsmall *= f_r;
		fontsize_small *= f_r;
		fontsize_default *= f_r;
		fontsize_mid *= f_r;
		fontsize_midSmall *= f_r;
		fontsize_midLarge *= f_r;
		fontsize_large *= f_r;
		fontsize_xlarge *= f_r;
		fontsize_xxlarge *= f_r;
		fontsize_xxxlarge *= f_r;

		Log.e("ScreenResolutionHandler", "Screen Size: "+w+" x "+h+" : "+contentW+" / "+d);
	}
	public static float getPxByDp(float dp){
		return dp*d+0.5f;
	}
	public static int getW(float f){
		return (int) (w*f);
	}
	public static int getRefW(float f){
		if(islandscape()) return (int) (h*0.75f*f);
		return (int) (w*f);
	}
	public static int getH(float f){
		return (int) (h*f);
	}
	public static int getViewPortW(){ return w; }
	public static int getViewPortH(){ return h; }
	public static float getDensity(){ return d; }
	public static int getPaddingW(){ return paddingW; }
	public static int getPaddingH(){ return paddingH; }
	public static boolean islandscape(){ return (w>=h); }
	public static int getBtnW(){
		return (int) (getRefW(0.08f));
	}
	public static int getBottomTabH(){
		return getBtnW()+2*ResolutionHandler.getRefW(0.03f)+2*ResolutionHandler.getH(0.04f);
	}
	public static int getDevIconW(){
		return (int) (w*0.06f);
	}
	public static boolean isTablet(){ return isTablet; }
	public static int getContentW(float f){ return (int) (contentW*f);
	}
}
