package com.controlfree.ha.vdp.controlfree2.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONObject;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View.MeasureSpec;
import android.widget.TextView;

import com.controlfree.ha.vdp.controlfree2.BuildConfig;
import com.controlfree.ha.vdp.controlfree2.R;


public class Fun {
	private final static String TAG = "Fun";
	public static String version = BuildConfig.VERSION_NAME;
	static boolean isFirstLaunch = true;
	public static String server_address = "cloud.control-free.com";
	//public static String server_address = "34.80.137.35";
	//public static String server_address = "192.168.2.11";
	
	public static int server_port = 50033;
	public static String server_url = "https://"+server_address+"/";
	public static String server_api = Fun.server_url+"home/api_client.php";



	
	public static void init(Context c, int w, int h, float d){
		if(isFirstLaunch){
			isFirstLaunch = false;
	        //float ratio = (float)(Math.sqrt((double)(w*w+h*h))/Math.sqrt((double)(1024*1024+600*600)))/d;
	        
		}
	}

	

	public static int getImgId(Context context, String imageName) {
		return context.getResources().getIdentifier("drawable/" + imageName, null, context.getPackageName());
	}

	public static int getTextViewWidth(TextView tv){
		int widthMeasureSpec = MeasureSpec.makeMeasureSpec(ResolutionHandler.getViewPortW(), MeasureSpec.AT_MOST);
		int heightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
		tv.measure(widthMeasureSpec, heightMeasureSpec);
		return tv.getMeasuredWidth();
	}
	public static int getTextViewHeight(TextView tv){
		int widthMeasureSpec = MeasureSpec.makeMeasureSpec(ResolutionHandler.getViewPortW(), MeasureSpec.AT_MOST);
		int heightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
		tv.measure(widthMeasureSpec, heightMeasureSpec);
		return tv.getMeasuredHeight();
	}

	

	final private static char[] hexArray = "0123456789ABCDEF".toCharArray();
	public static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for ( int j = 0; j < bytes.length; j++ ) {
		    int v = bytes[j] & 0xFF;
		    hexChars[j * 2] = hexArray[v >>> 4];
		    hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}
	public static byte[] hexToByte(String s) {
	    int len = s.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0,j=0;i+1<len && j<data.length;i+=2,j++) {
	        data[j] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
	                             + Character.digit(s.charAt(i+1), 16));
	    }
	    return data;
	}
	
	
	
	
	
	
	

	final private static String hexString = new String(hexArray);
	public static String codeToHex(String str){
		String returnStr = "";
		str = str.replace("\\n", "\n").replace("\\r", "\r");
		for(int i=0;i<str.length();i++){
			if(str.charAt(i)=='\\' && i+3<str.length() && str.charAt(i+1)=='x'){
				if(hexString.contains(""+str.charAt(i+2)) && hexString.contains(""+str.charAt(i+3))){
					returnStr += (str.charAt(i+2)+""+str.charAt(i+3)).toUpperCase(Locale.ENGLISH);
					i+=3;
					continue;
				}
			}
		    int v = (int)str.charAt(i);
			returnStr += hexArray[(v>>>4) & 0x0F]+""+hexArray[v & 0x0F];
		}

		//Log.e("codeToHex", str+" -> "+returnStr);
		return returnStr;
	}
	
	
	
	
	
	

	public static JSONObject parseParameter(String parameter){
		JSONObject obj = new JSONObject();
		try{
			obj.put("gp", "");
			if(!parameter.contentEquals("")){
				String[] strArr = parameter.split(",");
				for(int i=0;i<strArr.length;i++){
					String[] valArr = strArr[i].split("=");
					if(valArr.length==2){
						obj.put(valArr[0], valArr[1]);
					}
				}
			}
		}catch(Exception e){e.printStackTrace();}
		return obj;
	}
	public static JSONObject prepareParamPairForAnalogVal(JSONObject obj){
		String max = "100";
		String min = "0";
		String unit = "%";
		try{
			if(obj.has("max")) max = obj.getString("max");
			if(obj.has("min")) min = obj.getString("min");
			if(obj.has("to_max")) max = obj.getString("to_max");
			if(obj.has("to_min")) min = obj.getString("to_min");
			if(obj.has("unit")) unit = obj.getString("unit");

			obj.put("max", max);
			obj.put("min", min);
			obj.put("unit", unit);
		}catch(Exception e){e.printStackTrace();}
		return obj;
	}



	public static AlertDialog showAlert(Context c, String title, String msg){
		AlertDialog.Builder builder1 = new AlertDialog.Builder(c);
		builder1.setTitle(title);
		builder1.setMessage(msg);
		builder1.setCancelable(true);
		builder1.setPositiveButton(
				"OK",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		AlertDialog a = builder1.create();
		a.show();
		return a;
	}
	public static AlertDialog showAlert(Context c, String msg){
		return showAlert(c, "", msg);
	}
	public static String getErrorMsg(String err){
		if(err.contentEquals("network_error")) return "Fail to connect server, please check your network";
		if(err.contentEquals("system_error")) return "Unknown error";
		if(err.contentEquals("not_latest_version")) return "Please update your app to the latest version";
		if(err.contentEquals("member_repeat")) return "Email already exists";
		if(err.contentEquals("not_reg")) return "Registration not yet complete, please check email";
		if(err.contentEquals("not_confirm")) return "Registration under review";
		if(err.contentEquals("blocked")) return "Registration rejected, please contact us";
		if(err.contentEquals("invalid")) return "Invalid email/password";
		if(err.contentEquals("invalid_password")) return "Invalid password";
		if(err.contentEquals("reach_limit")) return "You have reached the limit";
		if(err.contentEquals("repeat_scene_name")) return "Scene with the same name already exists (may be created by other user)";
		return "Error";
	}
	public static interface Listener{
		public void onYes();
	}
	public static AlertDialog showConfirm(Context c, String msg, Listener l){
		AlertDialog.Builder builder1 = new AlertDialog.Builder(c);
		builder1.setMessage(msg);
		builder1.setCancelable(true);
		builder1.setPositiveButton(
				"Yes",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						l.onYes();
						dialog.cancel();
					}
				});
		builder1.setNegativeButton(
				"No",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		AlertDialog a = builder1.create();
		a.show();
		return a;
	}




	public static int getDeviceIconRes(String cat_code){
		//Log.e(TAG, "getDeviceIconRes: "+cat_code);
		if (cat_code.contentEquals("dim")) {
			return R.drawable.dev_dimmer;
		} else if (cat_code.contentEquals("cu")) {
			return R.drawable.dev_curtains;
		} else if (cat_code.contentEquals("sw") || cat_code.contentEquals("lts")) {
			return R.drawable.dev_switch_single;
		} else if (cat_code.contentEquals("tv")) {
			return R.drawable.dev_smart_tv;
		} else if (cat_code.contentEquals("ac")) {
			return R.drawable.dev_ac;
		} else if (cat_code.contentEquals("pj")) {
			return R.drawable.dev_projector;
		} else if (cat_code.contentEquals("cam")) {
			return R.drawable.dev_webcam;
		} else if (cat_code.contentEquals("recv")) {
			return R.drawable.dev_av_receiver;
		} else if (cat_code.contentEquals("bply")) {
			return R.drawable.dev_player;
		} else if (cat_code.contentEquals("mply")) {
			return R.drawable.dev_player;
		} else if (cat_code.contentEquals("tun")) {
			return R.drawable.dev_tuner;
		} else if (cat_code.contentEquals("stb")) {
			return R.drawable.dev_tuner;
		} else if (cat_code.contentEquals("smlt")) {
			return R.drawable.dev_smart_lighting;
		} else if (cat_code.contentEquals("smpp")) {
			return R.drawable.dev_smart_plug;
		} else if (cat_code.contentEquals("fan")) {
			return R.drawable.dev_fan;
		} else if (cat_code.contentEquals("sen")) {
			return R.drawable.dev_sensor;
		} else if (cat_code.contentEquals("mtx")) {
			return R.drawable.dev_av_receiver;
		} else if (cat_code.contentEquals("mtr")) {
			return R.drawable.dev_energymeter;
		} else if (cat_code.contentEquals("apf")) {
			return R.drawable.dev_air_purifier;
		} else if (cat_code.contentEquals("wtht")) {
			return R.drawable.dev_water_heater;
		}
		//Log.e(TAG, "default");
		return R.drawable.dev_default1;
	}
}
