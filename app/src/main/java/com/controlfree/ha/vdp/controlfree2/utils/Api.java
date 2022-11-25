package com.controlfree.ha.vdp.controlfree2.utils;

import android.content.Context;


import com.controlfree.ha.vdp.controlfree2.BuildConfig;

import org.json.JSONArray;
import org.json.JSONObject;

public class Api {
    private final static String TAG = "Api";

    public static interface Listener{
        public void onSuccess(JSONObject reqObj, JSONObject obj, JSONArray arr, JSONObject resObj);
        public void onFail(String msg, JSONObject resObj);
    }
    private static void call(Context c, JSONObject requestObj, Listener l){
        try {
            requestObj.put("token", DB.token);
        }catch(Exception e){}
        //Log.e(TAG, "call: " + requestObj);
        final JSONObject rObj = requestObj;
        HttpHandler.post(c, Fun.server_api, requestObj, new HttpHandler.Listener() {
            @Override
            public void onResponse(String result) {
                //Log.e(TAG, "response: " + result);
                try {
                    JSONObject obj = new JSONObject(result);
                    if(obj.getInt("result")==1){
                        if(obj.has("data")) {
                            if(obj.get("data") instanceof JSONObject) {
                                if(obj.getJSONObject("data").has("token")){
                                    DB.token = obj.getJSONObject("data").getString("token");
                                }
                                l.onSuccess(rObj, obj.getJSONObject("data"), null, obj);
                            }else if(obj.get("data") instanceof JSONArray) {
                                l.onSuccess(rObj, null, obj.getJSONArray("data"), obj);
                            }else{
                                l.onSuccess(rObj, null, null, obj);
                            }
                        }else{
                            l.onSuccess(rObj, null, null, obj);
                        }
                        return;
                    }
                    l.onFail("network_error", obj);
                } catch (Exception e) {
                    e.printStackTrace();
                    l.onFail("system_error", null);
                }
            }
            @Override
            public void onError(String msg) {
                l.onFail(msg, null);
            }
        });
    }

    public static void login(Context c, String email, String password, Listener l){
        try {
            JSONObject obj = new JSONObject();
            obj.put("action", "login_member");
            obj.put("email", email);
            obj.put("password", password);
            obj.put("app_version", BuildConfig.VERSION_NAME);
            obj.put("client_id", Cache.get("ssid"));
            call(c, obj, l);
        }catch(Exception e){e.printStackTrace();}
    }
    public static void loginWithCid(Context c, String email, String password, String cid, Listener l){
        try {
            JSONObject obj = new JSONObject();
            obj.put("action", "login_member");
            obj.put("email", email);
            obj.put("password", password);
            obj.put("app_version", BuildConfig.VERSION_NAME);
            obj.put("client_id", Cache.get("ssid"));
            obj.put("cid", cid);
            call(c, obj, l);
        }catch(Exception e){e.printStackTrace();}
    }
    public static void register(Context c, String first_name, String last_name, String email, String password, Listener l){
        try {
            JSONObject obj = new JSONObject();
            obj.put("action", "register_member");
            obj.put("first_name", first_name);
            obj.put("last_name", last_name);
            obj.put("email", email);
            obj.put("password", password);
            call(c, obj, l);
        }catch(Exception e){e.printStackTrace();}
    }

    public static void getServerList(Context c, Listener l){
        try {
            JSONObject obj = new JSONObject();
            obj.put("action", "ck_gateway_list");
            DB db = new DB(c);
            db.getIdForUpdate_server(obj);
            db.close();
            call(c, obj, l);
        }catch(Exception e){e.printStackTrace();}
    }
    public static void getRoomList(Context c, Listener l){
        try {
            JSONObject obj = new JSONObject();
            obj.put("action", "ck_area_list");
            obj.put("gateway_id", DB.getSetting(c, "gw_id"));
            DB db = new DB(c);
            db.getIdForUpdate_room(DB.getSetting(c, "gid"), obj);
            db.close();
            call(c, obj, l);
        }catch(Exception e){e.printStackTrace();}
    }
    public static void getDeviceInRoom(Context c, String area_id, Listener l){
        try {
            JSONObject obj = new JSONObject();
            obj.put("action", "ck_device_list");
            obj.put("gateway_id", DB.getSetting(c, "gw_id"));
            obj.put("area_id", area_id);
            DB db = new DB(c);
            db.getIdForUpdate_device(area_id, obj);
            db.close();
            call(c, obj, l);
        }catch(Exception e){e.printStackTrace();}
    }
    public static void getSceneList(Context c, Listener l){
        try {
            JSONObject obj = new JSONObject();
            obj.put("action", "ck_scene_list");
            obj.put("gateway_id", DB.getSetting(c, "gw_id"));
            DB db = new DB(c);
            db.getIdForUpdate_scene(DB.getSetting(c, "gid"), obj);
            db.close();
            call(c, obj, l);
        }catch(Exception e){e.printStackTrace();}
    }
    public static void bookmarkDevice(Context c, String area_id, String id, boolean is_bookmark, Listener l){
        try {
            JSONObject obj = new JSONObject();
            obj.put("action", "bookmark_device");
            obj.put("gateway_id", DB.getSetting(c, "gw_id"));
            obj.put("area_id", area_id);
            obj.put("id", id);
            obj.put("is_bookmark", is_bookmark);
            call(c, obj, l);
        }catch(Exception e){e.printStackTrace();}
    }
    public static void getDevice(Context c, Listener l){
        try {
            JSONObject obj = new JSONObject();
            obj.put("action", "ck_device_list");
            obj.put("gateway_id", DB.getSetting(c, "gw_id"));
            DB db = new DB(c);
            db.getIdForUpdate_allDevice(DB.getSetting(c, "gid"), obj);
            db.close();
            call(c, obj, l);
        }catch(Exception e){e.printStackTrace();}
    }
    public static void getWeather(Context c, Listener l){
        try {
            JSONObject obj = new JSONObject();
            obj.put("action", "get_weather");
            obj.put("gateway_id", DB.getSetting(c, "gw_id"));
            call(c, obj, l);
        }catch(Exception e){e.printStackTrace();}
    }
    public static void getClientConfig(Context c, Listener l){
        try {
            JSONObject obj = new JSONObject();
            obj.put("action", "get_client_config");
            obj.put("gateway_id", DB.getSetting(c, "gw_id"));
            obj.put("meta", "starredSensorEntries.all");
            call(c, obj, l);
        }catch(Exception e){e.printStackTrace();}
    }
    public static void getClientConfigDeviceIcon(Context c, Listener l){
        try {
            JSONObject obj = new JSONObject();
            obj.put("action", "get_client_config");
            obj.put("gateway_id", DB.getSetting(c, "gw_id"));
            obj.put("meta", "deviceIcons.all");
            call(c, obj, l);
        }catch(Exception e){e.printStackTrace();}
    }




    public static void getAutomationList(Context c, Listener l){
        try {
            JSONObject obj = new JSONObject();
            obj.put("action", "get_mtrigger");
            obj.put("gateway_id", DB.getSetting(c, "gw_id"));
            call(c, obj, l);
        }catch(Exception e){e.printStackTrace();}
    }
}
