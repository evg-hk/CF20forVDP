package com.controlfree.ha.vdp.controlfree2.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

public class DB extends SQLiteOpenHelper {
    private final static String TAG = "DB";
    private final static String DATABASE_NAME = "sql7.db";
    private final static int DATABASE_VERSION = 1;
    private static String SSID = "";

    public static String token = "";
    private static String[] serverFieldArr = {};
    private static String[] roomFieldArr = {};
    private static String[] deviceFieldArr = {};
    private static String[] sceneFieldArr = {};

    private SQLiteDatabase db;
    public DB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        db = this.getWritableDatabase();
    }

    public void init(){
        serverFieldArr = getColumnName("Server");
        roomFieldArr = getColumnName("Room");
        deviceFieldArr = getColumnName("Device");
        sceneFieldArr = getColumnName("Scene");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //this.db = db;
        Log.e(TAG, "oncreate");
        try{
            db.execSQL("create table Setting (name VARCHAR(100), value VARCHAR(100));");
            db.execSQL("create table Server (id INTEGER PRIMARY KEY, gw_id VARCHAR(64), local_ip VARCHAR(48), ext_ip VARCHAR(48), name VARCHAR(100), address VARCHAR(64), last_update_date INTEGER, img VARCHAR(128), file_last_update_date INTEGER, ordering INTEGER);");
            db.execSQL("create table Room (id INTEGER PRIMARY KEY, server_id INTEGER, name VARCHAR(50), ordering INTEGER, last_update_date INTEGER, img VARCHAR(128), device_list VARCHAR(256), file_last_update_date INTEGER);");
            db.execSQL("create table Device (id INTEGER PRIMARY KEY, img VARCHAR(128), area_id INTEGER, cat_code VARCHAR(20), category VARCHAR(30), brand VARCHAR(50), model VARCHAR(50), version float, interface VARCHAR(30), address VARCHAR(48), sub_address VARCHAR(128), layout_code VARCHAR(50), name VARCHAR(100), x float, y float, driver_id INTEGER, control TEXT, protocol VARCHAR(10), format VARCHAR(24), last_update_date INTEGER, last_fb_time INTEGER, is_bookmark INTEGER, is_online INTEGER, ordering INTEGER);");
            db.execSQL("create table Scene (id INTEGER PRIMARY KEY, server_id INTEGER, name VARCHAR(50), mode INTEGER, control TEXT, ordering INTEGER, img VARCHAR(128), jump_device INTEGER, last_update_date INTEGER, is_bookmark INTEGER);");
        }catch(Exception e){ e.printStackTrace(); }
    }
    private String[] getColumnName(String tbl){
        Cursor cur = db.query(tbl, null, null, null, null, null, null);
        String[] arr = cur.getColumnNames();
        cur.moveToLast();
        cur.close();
        return arr;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }


    //---------------------------------------------

    public void ck(String tbl){
        Cursor c = db.rawQuery("select * from "+tbl+" ", null);
        JSONArray arr =  cursor2JSONArray(c);
        Log.e(TAG, "ck: "+tbl+" : "+arr);
    }

    public int insert(String tbl, JSONObject obj) throws Exception {
        JSONArray arr = obj.names();
        if(arr!=null){
            String[] fieldArr = new String[arr.length()];
            String[] valArr = new String[arr.length()];
            for(int i=0;i<arr.length();i++){
                fieldArr[i] = arr.getString(i);
                valArr[i] = obj.getString(arr.getString(i));
            }
            return insert(tbl, fieldArr, valArr);
        }
        return 0;
    }
    public int insert(String tbl, String field, String value) throws Exception {
        return insert(tbl, new String[]{field}, new String[]{value});
    }
    public int insert(String tbl, String[] fieldArr, String[] valueArr) throws Exception {
        String QUERY = "insert into "+tbl+" ('"+ TextUtils.join("','", fieldArr)+"') values ('"+TextUtils.join("','", valueArr)+"')";
        db.execSQL(QUERY);
        Cursor c = db.rawQuery("select last_insert_rowid()", null);
        c.moveToFirst();
        int i = 0;
        if(!c.isAfterLast()){
            i = c.getInt(0);
        }
        c.close();
        return i;
    }
    public void update(String tbl, String field, String value, String where) throws Exception {
        update(tbl, new String[]{field}, new String[]{value}, where);
    }
    public void update(String tbl, String[] fieldArr, String[] valueArr, String where) throws Exception {
        String field_value = "";
        for(int i=0;i<fieldArr.length;i++){
            if(i>0) field_value += ",";
            field_value += fieldArr[i]+"='"+valueArr[i]+"'";
        }
        String WHERE = (where.contentEquals("")?"":("where "+where));
        db.execSQL("update "+tbl+" set "+field_value+" "+WHERE);
    }
    public void delete(String tbl, String where) throws Exception {
        String WHERE = (where.contentEquals("")?"":("where "+where));
        db.execSQL("delete from "+tbl+" "+WHERE);
    }
    public JSONArray select(String tbl, String field, String where) throws Exception {
        String WHERE = (where.contentEquals("")?"":("where "+where));
        Cursor c = db.rawQuery("select "+field+" from "+tbl+" "+WHERE, null);
        return cursor2JSONArray(c);
    }

    public static void setSetting(Context c, String name, String value){
        try {
            DB db = new DB(c);
            db.updateSetting(name, value);
            db.close();
            if(name.startsWith("login_")) return;
            Log.e(TAG, "setSetting: "+name+" : "+value);
        }catch(Exception e){e.printStackTrace();}
    }
    public static String getSetting(Context c, String name){
        String value = "";
        try {
            DB db = new DB(c);
            value = db.selectSetting(name);
            db.close();
            //Log.e(TAG, "getSetting: "+name+" : "+value);
        }catch(Exception e){e.printStackTrace();}
        return value;
    }
    public void updateSetting(String name, String value) throws Exception {
        Cursor cur = db.rawQuery("select value from Setting where name='"+name+"'", null);
        int count = cur.getCount();
        cur.close();
        if(count==0){
            insert("Setting", new String[]{"name", "value"}, new String[]{name, value});
        }else {
            update("Setting", "value", value, "name='" + name + "'");
        }
    }
    public String selectSetting(String name) throws Exception {
        Cursor cur = db.rawQuery("select value from Setting where name='"+name+"'", null);

        String value = "";
        cur.moveToFirst();
        while(!cur.isAfterLast()){
            try{
                value = cur.getString(0);
            }catch(Exception e){
                e.printStackTrace();
            }
            cur.moveToNext();
        }
        cur.close();
        return value;
    }
    public JSONObject selectSetting(String[] idStrArr) throws Exception {
        String WHERE = "";
        for(int i=0;i<idStrArr.length;i++){
            if(!WHERE.contentEquals("")) WHERE += " or ";
            WHERE += "name='"+idStrArr[i]+"'";
        }
        if(!WHERE.contentEquals("")) WHERE = "where "+WHERE;
        Cursor cur = db.rawQuery("select name,value from Setting "+WHERE, null);

        JSONObject obj = new JSONObject();
        cur.moveToFirst();
        while(!cur.isAfterLast()){
            try{
                obj.put(cur.getString(0), cur.getString(1));
            }catch(Exception e){
                e.printStackTrace();
            }
            cur.moveToNext();
        }
        cur.close();
        return obj;
    }
    private JSONArray cursor2JSONArray(Cursor cur){
        JSONArray arr = new JSONArray();
        cur.moveToFirst();
        while(!cur.isAfterLast()){
            JSONObject obj = new JSONObject();
            for(int i=0;i<cur.getColumnCount();i++){
                try{
                    if(cur.getColumnName(i).startsWith("is_")){
                        if(cur.getString(i)!=null && cur.getString(i).contentEquals("true")){
                            obj.put(cur.getColumnName(i), true);
                        }else if(cur.getInt(i)==1){
                            obj.put(cur.getColumnName(i), true);
                        }else{
                            obj.put(cur.getColumnName(i), false);
                        }
                    }else {
                        obj.put(cur.getColumnName(i), cur.getString(i));
                    }
                }catch(Exception e){e.printStackTrace();}
            }
            arr.put(obj);
            cur.moveToNext();
        }
        cur.close();
        return arr;
    }

    public String getUniquePsuedoIDv2(Context c) throws Exception{
        String ssid = "";
        if(Build.BOARD.length()>=2) ssid += Build.BOARD.substring(Build.BOARD.length() - 2);
        if(Build.BRAND.length()>=2) ssid += Build.BRAND.substring(Build.BRAND.length()-2);
        if(Build.DEVICE.length()>=2) ssid += Build.DEVICE.substring(Build.DEVICE.length()-2);
        if(Build.MANUFACTURER.length()>=2) ssid += Build.MANUFACTURER.substring(Build.MANUFACTURER.length()-2);
        if(Build.MODEL.length()>=2) ssid += Build.MODEL.substring(Build.MODEL.length()-2);
        if(Build.PRODUCT.length()>=2) ssid += Build.PRODUCT.substring(Build.PRODUCT.length()-2);
        if(Build.SERIAL.length()>=2) ssid += Build.SERIAL.substring(Build.SERIAL.length()-2);
        if(ssid.length()>16) ssid = ssid.substring(0, 16);
        //Log.e("getUniquePsuedoIDv2", "ID 1: "+ssid);

        String an_id = Settings.Secure.getString(c.getContentResolver(), Settings.Secure.ANDROID_ID);
        for(int i=an_id.length();i<16;i++){
            an_id += "x";
        }
        //Log.e("getUniquePsuedoIDv2", "ID 2: "+an_id);
        if(an_id.length()>16) an_id = an_id.substring(0, 16);
        ssid = AES.encrypt(ssid+an_id, an_id);

        String mac = "";
        try {
            WifiManager wifiMan = (WifiManager) c.getSystemService(Context.WIFI_SERVICE);
            if(wifiMan!=null){
                WifiInfo wifiInf = wifiMan.getConnectionInfo();
                if(wifiInf!=null && wifiInf.getMacAddress()!=null){
                    mac = wifiInf.getMacAddress().replace(":","");
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        if(mac.contentEquals("")) mac = ssid.substring(0, 16);
        for(int i=mac.length();i<16;i++){
            mac += "x";
        }
        if(mac.length()>16) mac = mac.substring(0, 16);
        //Log.e("getUniquePsuedoIDv2", "ID 3: "+mac);
        ssid = AES.encrypt(ssid, mac);
        if(ssid.length()>32) ssid = ssid.substring(0, 32);
        //Log.e("getUniquePsuedoIDv2", "ID 4: "+ssid);
        return ssid;
    }



    //----------------------------

    public void updateList(String tbl, JSONArray arr){
        try {
            for (int i = 0; i < arr.length(); i++) {
                internalUpdateItem(tbl, arr.getJSONObject(i));
            }
            delete(tbl, "last_update_date=-1");
        }catch(Exception e){e.printStackTrace();}
    }
    public void internalUpdateItem(String tbl, JSONObject obj){
        try {
                delete(tbl, "id='" + obj.getString("id") + "'");
                if(obj.getString("last_update_date").contentEquals("-1")) return;
                JSONObject dObj = new JSONObject();
                String[] fieldArr = {};
                if(tbl.contentEquals("Server")){
                    fieldArr = serverFieldArr;
                }else if(tbl.contentEquals("Room")){
                    fieldArr = roomFieldArr;
                }else if(tbl.contentEquals("Device")){
                    fieldArr = deviceFieldArr;
                }else if(tbl.contentEquals("Scene")){
                    fieldArr = sceneFieldArr;
                }
                for(int j=0;j<fieldArr.length;j++){
                    if(obj.has(fieldArr[j])){
                        dObj.put(fieldArr[j], obj.get(fieldArr[j]));
                    }
                }
                insert(tbl, dObj);
        }catch(Exception e){e.printStackTrace();}
    }




    public JSONObject getIdForUpdate(String tbl, String where, JSONObject obj){
        try {
            JSONArray arr = select(tbl, "id,last_update_date", where);
            String idList = "";
            String dateList = "";
            for(int i=0;i<arr.length();i++){
                if(i>0){
                    idList += ",";
                    dateList += ",";
                }
                idList += arr.getJSONObject(i).getString("id");
                dateList += arr.getJSONObject(i).getString("last_update_date");
            }
            obj.put("id_list", idList);
            obj.put("last_update_date_list", dateList);
        }catch(Exception e){e.printStackTrace();}
        return obj;
    }
    public JSONObject getIdForUpdate_server(JSONObject obj){
        return getIdForUpdate("Server", "", obj);
    }
    public JSONObject getIdForUpdate_room(String gid, JSONObject obj){
        return getIdForUpdate("Room", "server_id='"+gid+"'", obj);
    }
    public JSONObject getIdForUpdate_device(String room_id, JSONObject obj){
        return getIdForUpdate("Device", "area_id='"+room_id+"'", obj);
    }
    public JSONObject getIdForUpdate_allDevice(String gid, JSONObject obj){
        JSONArray arr = getRoomList(gid);
        String id_list = "";
        try{
            for(int i=0;i<arr.length();i++){
                if(!id_list.contentEquals("")) id_list += ",";
                id_list += arr.getJSONObject(i).getString("id");
            }
        }catch(Exception e){e.printStackTrace();}
        return getIdForUpdate("Device", "area_id in ("+id_list+")", obj);
    }
    public JSONObject getIdForUpdate_scene(String gid, JSONObject obj){
        return getIdForUpdate("Scene", "server_id='"+gid+"'", obj);
    }
    public JSONArray getServerList(){
        try{
            return select("Server", "*", "1=1 order by ordering asc");
        }catch(Exception e){e.printStackTrace();}
        return new JSONArray();
    }
    public JSONArray getRoomList(String gw_id){
        try{
            return select("Room", "*", "server_id='"+gw_id+"' order by ordering asc");
        }catch(Exception e){e.printStackTrace();}
        return new JSONArray();
    }
    public JSONArray getDeviceList(String room_id){
        try{
            JSONArray rmArr = select("Room", "device_list", "id='"+room_id+"' limit 0,1");
            if(rmArr.length()>0) {
                JSONArray arr = select("Device", "*", "area_id='" + room_id + "' order by ordering asc");
                for (int i = 0; i < arr.length(); i++) {
                    if (arr.getJSONObject(i).getString("control").contentEquals("")) continue;
                    JSONArray cArr = new JSONArray(arr.getJSONObject(i).getString("control"));
                    for (int j = 0; j < cArr.length(); j++) {
                        JSONObject pObj = Fun.parseParameter(cArr.getJSONObject(j).getString("parameter"));
                        if(cArr.getJSONObject(j).getString("type").startsWith("sld")
                            || cArr.getJSONObject(j).getString("type").contentEquals("stb")){
                            pObj = Fun.prepareParamPairForAnalogVal(pObj);
                        }
                        cArr.getJSONObject(j).put("param_pair", pObj);
                    }
                    arr.getJSONObject(i).put("control", cArr);
                }

                String[] dOrderArr = rmArr.getJSONObject(0).getString("device_list").split(",");
                JSONArray final_arr = new JSONArray();
                for(int i=0;i<dOrderArr.length;i++){
                    for(int j=0;j<arr.length();j++){
                        if(arr.getJSONObject(j).getString("id").contentEquals(dOrderArr[i])){
                            final_arr.put(arr.getJSONObject(j));
                            break;
                        }
                    }
                }
                //not in order list
                boolean is = false;
                for(int j=0;j<arr.length();j++){
                    is = false;
                    for(int i=0;i<dOrderArr.length;i++){
                        if(arr.getJSONObject(j).getString("id").contentEquals(dOrderArr[i])){
                            is = true;
                            break;
                        }
                    }
                    if(!is){
                        final_arr.put(arr.getJSONObject(j));
                    }
                }
                return final_arr;
            }
        }catch(Exception e){e.printStackTrace();}
        return new JSONArray();
    }
    public JSONArray getSceneList(String gw_id){
        try{
            JSONArray arr = select("Scene", "*", "server_id='"+gw_id+"' order by ordering asc");
            for(int i=0;i<arr.length();i++){
                if(arr.getJSONObject(i).has("control") && !arr.getJSONObject(i).getString("control").contentEquals("")){
                    arr.getJSONObject(i).put("control", arr.getJSONObject(i).getString("control").replace("\\",""));
                }
            }
            return orderedSceneList(arr);
        }catch(Exception e){e.printStackTrace();}
        return new JSONArray();
    }

    public static JSONArray orderedSceneList(JSONArray sArr){
        try{
            JSONArray final_arr = new JSONArray();
            while(sArr.length()>0) {
                int index = 0;
                JSONObject obj = sArr.getJSONObject(index);
                for (int i = 0; i < sArr.length(); i++) {
                    if(sArr.getJSONObject(i).getInt("ordering")>obj.getInt("ordering")){
                        //Log.e(TAG, sArr.getJSONObject(i).getInt("ordering")+" > "+obj.getInt("ordering"));
                        obj = sArr.getJSONObject(i);
                        index = i;
                    }
                }
                final_arr.put(obj);
                sArr.remove(index);
            }
            return final_arr;
        }catch(Exception e){e.printStackTrace();}
        return new JSONArray();
    }
    public JSONArray getRoomSceneList(String gid, String room_id){
        //Log.e(TAG, "getRoomSceneList: "+gw_id+" : "+room_id);
        JSONArray final_arr = new JSONArray();
        try{
            JSONArray rmArr = select("Room", "device_list", "id='"+room_id+"' limit 0,1");
            if(rmArr.length()>0) {
                //Log.e(TAG, "rmArr: "+rmArr);
                String[] didArr = rmArr.getJSONObject(0).getString("device_list").split(",");
                if(didArr.length>0){
                    List<String> didList = Arrays.asList(didArr);
                    JSONArray arr = getSceneList(gid);
                    boolean has = false;
                    for(int i=0;i<arr.length();i++){
                        has = true;
                        JSONArray dArr = new JSONArray(arr.getJSONObject(i).getString("control"));
                        //Log.e(TAG, "getRoomSceneList: dArr: "+dArr.length());
                        if(dArr.length()==0) continue;
                        for(int j=0;j<dArr.length();j++){
                            if(!didList.contains(dArr.getJSONObject(j).getString("device_id"))){
                                has = false;
                                break;
                            }
                        }
                        if(has){
//                            Log.e(TAG, "Scene: "+arr.getJSONObject(i).getString("name"));
//                            String str = "";
//                            for(int j=0;j<dArr.length();j++){
//                                if(!str.contentEquals("")) str += ", ";
//                                str += dArr.getJSONObject(j).getString("device_id");
//                            }
//                            Log.e(TAG, "device_id: "+str);
                            final_arr.put(arr.getJSONObject(i));
                        }
                    }
                }
            }
        }catch(Exception e){e.printStackTrace();}
        return final_arr;
    }
    public JSONArray getBookmarkSceneList(String gid){
        try{
            JSONArray returnArr = new JSONArray();
            JSONArray arr = getSceneList(gid);
            for(int i=0;i<arr.length();i++){
                if(arr.getJSONObject(i).getBoolean("is_bookmark")){
                    returnArr.put(arr.getJSONObject(i));
                }
            }
            return returnArr;
        }catch(Exception e){e.printStackTrace();}
        return new JSONArray();
    }
    public JSONArray getBookmarkDeviceList(String gid){
        try{
            JSONArray rmArr = getRoomList(gid);
            JSONArray returnArr = new JSONArray();
            for(int i=0;i<rmArr.length();i++){
                JSONArray dArr = getDeviceList(rmArr.getJSONObject(i).getString("id"));
                for(int j=0;j<dArr.length();j++){
                    //Log.e(TAG, "getBookmarkDeviceList: "+j+" : "+dArr.getJSONObject(j).getString("is_bookmark"));
                    if(dArr.getJSONObject(j).getBoolean("is_bookmark")) {
                        returnArr.put(dArr.getJSONObject(j));
                    }
                }
            }
            return returnArr;
        }catch(Exception e){e.printStackTrace();}
        return new JSONArray();
    }

    public JSONArray getSimpleDeviceListById(JSONArray idArr){
        if(idArr.length()==0) return new JSONArray();
        try{
            String str = "";
            for(int i=0;i<idArr.length();i++){
                if(i>0) str += ",";
                str += idArr.getString(i);
            }
            JSONArray returnArr = select("Device", "id,name,cat_code", "id in ("+str+")");
            if(returnArr.length()>0) {
                return returnArr;
            }
        }catch(Exception e){e.printStackTrace();}
        return new JSONArray();
    }
}
