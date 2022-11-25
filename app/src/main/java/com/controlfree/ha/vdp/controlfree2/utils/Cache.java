package com.controlfree.ha.vdp.controlfree2.utils;

import android.app.backup.BackupManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;

import com.controlfree.ha.vdp.controlfree2.MyBackupAgent;
import com.controlfree.ha.vdp.controlfree2.component.ExPageNavView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Cache {
    private final static String TAG = "Cache";
    private static JSONObject dataObj = new JSONObject();
    private static JSONObject dataJsonObj = new JSONObject();
    private static JSONObject dataJsonArrObj = new JSONObject();
    private static JSONObject imgObj = new JSONObject();
    public static boolean is_updateDevice = false;
    public static ArrayList<View> viewList = new ArrayList();
    public static ExPageNavView navView = null;

    public static void init(Context c){
        DB db = new DB(c);
        db.init();
        try{
            String x = db.selectSetting("ssid");
            if(x.contentEquals("")){
                String app_id = SharedPref.read(c, "app_id");
                //Log.e(TAG, "SharedPref.read: app_id: "+app_id);
                if(app_id.contentEquals("")) {
                    x = db.getUniquePsuedoIDv2(c);
                    SharedPref.write(c, "app_id", x);
                    //Log.e(TAG, "SharedPref.write: app_id: "+(SharedPref.write(c, "app_id", x)?"T":"F"));
                    //Log.e(TAG, "SharedPref.read2: app_id: "+SharedPref.read(c, "app_id"));
                }else{
                    x = app_id;
                }
                db.insert("Setting", new String[]{"name", "value"}, new String[]{"ssid", x});
            }
            set("ssid", x);
        }catch(Exception e){ e.printStackTrace(); }
        db.close();
    }
    public static String get(String id){
        try{
            if(dataObj.has(id)) return dataObj.get(id)+"";
        }catch(Exception e){ e.printStackTrace(); }
        return "";
    }
    public static void set(String id, String val){
        try{
            dataObj.put(id, val);
        }catch(Exception e){ e.printStackTrace(); }
    }
    public static JSONObject getObj(String id){
        try{
            if(dataJsonObj.has(id)) return dataJsonObj.getJSONObject(id);
        }catch(Exception e){ e.printStackTrace(); }
        return null;
    }
    public static void setObj(String id, JSONObject val){
        try{
            dataJsonObj.put(id, val);
        }catch(Exception e){ e.printStackTrace(); }
    }
    public static JSONArray getArr(Context c, String id){
        try{
            JSONArray arr = null;
            if(id.contentEquals("server_list")){
                DB db = new DB(c);
                arr = db.getServerList();
                db.close();
            }else if(id.contentEquals("room_list")){
                DB db = new DB(c);
                arr = db.getRoomList(DB.getSetting(c, "gid"));
                db.close();
            }else if(id.contentEquals("scene_list")){
                DB db = new DB(c);
                arr = db.getSceneList(DB.getSetting(c, "gid"));
                db.close();
            }
            if(arr!=null) return arr;
            if(dataJsonArrObj.has(id)) return dataJsonArrObj.getJSONArray(id);
        }catch(Exception e){ e.printStackTrace(); }
        return null;
    }
    public static void setArr(String id, JSONArray val){
        try{
            dataJsonArrObj.put(id, val);
            //Log.e(TAG, "setArr: "+id+" : "+val);
        }catch(Exception e){ e.printStackTrace(); }
    }


    public static interface SyncListener{
        public void onSuccess();
        public void onFail(String msg);
    }
    private static boolean[] syncState = {};
    public static void syncData(Context c, SyncListener listener){
        syncState = new boolean[]{false, false, false, false, false};
        Api.getServerList(c, new Api.Listener(){
            @Override
            public void onSuccess(JSONObject reqObj, JSONObject obj, JSONArray arr, JSONObject resObj) {
                try{
                    if(arr==null){
                        listener.onFail("system_error");
                        return;
                    }
                    DB db = new DB(c);
                    db.updateList("Server", arr);
                    arr = db.getServerList();
                    db.close();
                    //Cache.setArr("server_list", arr);

                    if (arr.length() > 0) {
                        String gw_id = DB.getSetting(c, "gw_id");

                        if (!gw_id.contentEquals("")) {
                            boolean has = false;
                            for (int i = 0; i < arr.length(); i++) {
                                if (arr.getJSONObject(i).has("gw_id") && arr.getJSONObject(i).getString("gw_id").contentEquals(gw_id)) {
                                    has = true;
                                    break;
                                }
                            }
                            if(!has) gw_id = "";
                        }
                        if (gw_id.contentEquals("")) {
                            if (arr.length() > 0 && arr.getJSONObject(0).has("gw_id")) {
                                gw_id = arr.getJSONObject(0).getString("gw_id");
                            }
                        }

                        String gid = "";
                        for (int i = 0; i < arr.length(); i++) {
                            if (arr.getJSONObject(i).has("gw_id") && arr.getJSONObject(i).getString("gw_id").contentEquals(gw_id)) {
                                gid = arr.getJSONObject(i).getString("id");
                                Cache.set("server_name", arr.getJSONObject(i).getString("name"));
                                DB.setSetting(c, "gid", gid);
                                DB.setSetting(c, "gw_id", gw_id);
                                break;
                            }
                        }
                        if (!gw_id.contentEquals("")) {
                            final String final_gid = gid;
                            Api.getRoomList(c, new Api.Listener() {
                                @Override
                                public void onSuccess(JSONObject reqObj, JSONObject obj, JSONArray arr, JSONObject resObj) {
                                    try {
                                        if(arr==null){
                                            listener.onFail("system_error");
                                            return;
                                        }
                                        for(int i=0;i<arr.length();i++){
                                            arr.getJSONObject(i).put("server_id", final_gid);
                                        }
                                        if(resObj.has("area_list")){
                                            JSONArray final_arr = new JSONArray();
                                            String[] aidArr = resObj.getString("area_list").split(",");
                                            for(int i=0;i<aidArr.length;i++){
                                                for(int j=0;j<arr.length();j++){
                                                    if(arr.getJSONObject(j).getString("id").contentEquals(aidArr[i])){
                                                        final_arr.put(arr.getJSONObject(j));
                                                        break;
                                                    }
                                                }
                                            }
                                            List<String> aidList = Arrays.asList(aidArr);
                                            for(int i=0;i<arr.length();i++){
                                                if(!aidList.contains(arr.getJSONObject(i).getString("id"))){
                                                    final_arr.put(arr.getJSONObject(i));
                                                }
                                            }
                                            for(int i=0;i<final_arr.length();i++){
                                                final_arr.getJSONObject(i).put("ordering", i);
                                            }
                                            arr = final_arr;
                                        }

                                        DB db = new DB(c);
                                        db.updateList("Room", arr);
                                        db.close();
                                    } catch (Exception e) { e.printStackTrace(); }
                                    syncNext(c, 0, listener);
                                }

                                @Override
                                public void onFail(String msg, JSONObject resObj) {
                                    listener.onFail(msg);
                                }
                            });
                        }
                    }
                }catch(Exception e){e.printStackTrace();}

            }
            @Override
            public void onFail(String msg, JSONObject resObj) {
                listener.onFail(msg);
            }
        });
    }
    private static void syncNext(Context c, int index, SyncListener listener){
        if(index==0){
            final String final_gid = DB.getSetting(c, "gid");
            Api.getSceneList(c, new Api.Listener() {
                @Override
                public void onSuccess(JSONObject reqObj, JSONObject obj, JSONArray arr, JSONObject resObj) {
                    try {
                        if(arr==null){
                            //listener.onFail("system_error");
                            return;
                        }
                        for(int i=0;i<arr.length();i++){
                            arr.getJSONObject(i).put("server_id", final_gid);
                            if(arr.getJSONObject(i).has("member_id")){
                                arr.getJSONObject(i).remove("member_id");
                            }
                        }
                        DB db = new DB(c);
                        db.updateList("Scene", arr);
                        db.close();
                    } catch (Exception e) { e.printStackTrace(); }
                    syncNext(c, 1, listener);
                }
                @Override
                public void onFail(String msg, JSONObject resObj) {
                    listener.onFail(msg);
                }
            });
        }else if(index==1){
            Api.getClientConfig(c, new Api.Listener(){
                @Override
                public void onSuccess(JSONObject reqObj, JSONObject obj, JSONArray arr, JSONObject resObj) {
                    try{
                        //Log.e(TAG, "getClientConfig: "+resObj);
                        if(obj!=null) setObj("control_bookmark", obj);
                    } catch (Exception e) {e.printStackTrace();}
                    syncNext(c, 2, listener);
                }
                @Override
                public void onFail(String msg, JSONObject resObj) {
                    listener.onFail(msg);
                }
            });
        }else if(index==2){
            Api.getClientConfigDeviceIcon(c, new Api.Listener(){
                @Override
                public void onSuccess(JSONObject reqObj, JSONObject obj, JSONArray arr, JSONObject resObj) {
                    try{
                        //Log.e(TAG, "getClientConfig: "+obj);
                        if(obj!=null) setObj("device_icon", obj);
                    } catch (Exception e) {e.printStackTrace();}
                    syncNext(c, 3, listener);
                }
                @Override
                public void onFail(String msg, JSONObject resObj) {
                    listener.onFail(msg);
                }
            });
        }else if(index==3){
            Api.getAutomationList(c, new Api.Listener(){
                @Override
                public void onSuccess(JSONObject reqObj, JSONObject obj, JSONArray arr, JSONObject resObj) {
                    try{
                        JSONArray final_arr = new JSONArray();
                        boolean has = false;
                        for(int i=0;i<arr.length();i++){
                            has = false;
                            for(int j=0;j<final_arr.length();j++){
                                if(final_arr.getJSONObject(j).getInt("scene_id")==arr.getJSONObject(i).getInt("scene_id")){
                                    has = true;
                                    final_arr.getJSONObject(j).getJSONArray("mtrigger_arr").put(arr.getJSONObject(i));
                                    break;
                                }
                            }
                            if(!has){
                                arr.getJSONObject(i).put("mtrigger_arr", new JSONArray());
                                final_arr.put(arr.getJSONObject(i));
                            }
                        }
                        JSONArray tmpArr = new JSONArray();
                        for(int i=final_arr.length()-1;i>=0;i--){
                            tmpArr.put(final_arr.getJSONObject(i));
                        }
                        final_arr = tmpArr;
                        //Log.e(TAG, "final_arr: "+final_arr);
                        Cache.setArr("automation_list", final_arr);
                    } catch (Exception e) {e.printStackTrace();}
                    syncNext(c, 4, listener);
                }
                @Override
                public void onFail(String msg, JSONObject resObj) {
                    listener.onFail(msg);
                }
            });
        }else if(index==4){
            listener.onSuccess();
        }
    }
    private static void ckSyncState(int index, SyncListener listener){
        syncState[index] = true;
        for(int i=0;i<syncState.length;i++){
            //Log.e(TAG, "syncState[i]: "+i+" : "+(syncState[i]?"T":"F"));
            if(!syncState[i]) return ;
        }
        listener.onSuccess();
    }



    //--------------------------
    public static JSONObject getServer(Context c){
        try{
            JSONArray arr = Cache.getArr(c, "server_list");
            String gw_id = DB.getSetting(c, "gw_id");
            for(int i=0;i<arr.length();i++) {
                //Log.e(TAG, "getServer: "+arr.getJSONObject(i).getString("gw_id")+" / "+gw_id);
                if(arr.getJSONObject(i).getString("gw_id").contentEquals(gw_id)) {
                    return arr.getJSONObject(i);
                }
            }
        }catch(Exception e){e.printStackTrace();}
        return null;
    }
    public static JSONObject getSceneById(Context c, int id){
        JSONArray arr = Cache.getArr(c, "scene_list");
        if(arr==null) return null;
        try{
            for(int i=0;i<arr.length();i++){
                if(arr.getJSONObject(i).getString("id").contentEquals(""+id)){
                    return arr.getJSONObject(i);
                }
            }
        }catch(Exception e){e.printStackTrace();}
        return null;
    }
    public static JSONArray getRoomAutomationList(Context c, int room_id){
        try{
            DB db = new DB(c);
            JSONArray sArr = db.getRoomSceneList(DB.getSetting(c, "gid"), ""+room_id);
            db.close();

            JSONArray final_arr = new JSONArray();
            JSONArray arr = Cache.getArr(c, "automation_list");
            for(int i=0;i<arr.length();i++) {
                for(int j=0;j<sArr.length();j++){
                    if(arr.getJSONObject(i).getInt("scene_id")==sArr.getJSONObject(j).getInt("id")) {
                        ArrayList<String> tpList = new ArrayList<String>();
                        JSONArray pArr = arr.getJSONObject(i).getJSONArray("parameter");
                        for(int k=0;k<pArr.length();k++){
                            if(!tpList.contains(pArr.getJSONObject(k).getString("tp"))){
                                tpList.add(pArr.getJSONObject(k).getString("tp"));
                            }
                        }
                        int count = 0;
                        for(int k=0;k<tpList.size();k++){
                            if(tpList.get(k).contentEquals("fb")){
                                count++;
                            }
                        }
                        if(count>0) final_arr.put(arr.getJSONObject(i));
                        break;
                    }
                }
            }
            return final_arr;

        }catch(Exception e){e.printStackTrace();}
        return new JSONArray();
    }
    public static JSONArray getBookmarkAutomationList(Context c){
        try{
            DB db = new DB(c);
            JSONArray sArr = db.getBookmarkSceneList(DB.getSetting(c, "gid"));
            db.close();

            JSONArray final_arr = new JSONArray();
            JSONArray arr = Cache.getArr(c, "automation_list");
            for(int i=0;i<arr.length();i++) {
                for(int j=0;j<sArr.length();j++){
                    if(arr.getJSONObject(i).getInt("scene_id")==sArr.getJSONObject(j).getInt("id")) {
                        ArrayList<String> tpList = new ArrayList<String>();
                        JSONArray pArr = arr.getJSONObject(i).getJSONArray("parameter");
                        for(int k=0;k<pArr.length();k++){
                            if(!tpList.contains(pArr.getJSONObject(k).getString("tp"))){
                                tpList.add(pArr.getJSONObject(k).getString("tp"));
                            }
                        }
                        int count = 0;
                        for(int k=0;k<tpList.size();k++){
                            if(tpList.get(k).contentEquals("fb")){
                                count++;
                            }
                        }
                        if(count>0) final_arr.put(arr.getJSONObject(i));
                        break;
                    }
                }
            }
            return final_arr;

        }catch(Exception e){e.printStackTrace();}
        return new JSONArray();
    }






    //--------------------------
    public static Bitmap getBitmap(Context c, String img, String img_type, int w){
        Bitmap bmp = null;
        try{
            if(!imgObj.has(img+"_"+img_type)){
                Bitmap b = FileHandler.getDownloadedImgBitmap(c, img, img_type, w);
                imgObj.put(img+"_"+img_type, b);
                bmp = b;
            }else{
                bmp = (Bitmap)imgObj.get(img+"_"+img_type);
            }
        }catch(Exception e){e.printStackTrace();}
        return bmp;
    }




    public static void updateDevice(Context c, JSONObject obj){
        DB db = new DB(c);
        db.internalUpdateItem("Device", obj);
        db.close();
    }



}
