package com.controlfree.ha.vdp.controlfree2.utils;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class TcpClient extends Thread{
    public static final String TAG = "TcpClient";
    private boolean is_run = true, isReady = false;

    private Socket s = null;
    private String key = "", gw_id = "";
    private List<String> writeQueue = new LinkedList<String>();
    private List<String> fbQueue = new LinkedList<String>();
    private Random rand = new Random();
    private JSONObject statusObj = new JSONObject();
    public String gid = "";

    private Listener listener;
    public static interface Listener{
        public void onRead(String msg);
        public void onFB(String did, String gp, String cid, String v, long t);
        public void onReady();
        public void onError(String msg);
        public void onEnd();
    }
    public TcpClient(String gid, String gw_id, Listener l){
        this.gid = gid;
        this.gw_id = gw_id;
        this.listener = l;
        start();
    }
    private Socket createSocket(){
        try {
            Socket socket = new Socket();
            socket.setKeepAlive(true);
            socket.setSoTimeout(5000);
            socket.connect(new InetSocketAddress(InetAddress.getByName(Fun.server_address), Fun.server_port), 7*1000);
            return socket;
        } catch(Exception e) {
            Log.e(TAG, "createSocket: fail");
        }
        return null;
    }
    public void run(){
        byte[] buffer = new byte[4096*2];
        String plainTxt = "", bufferTxt = "";
        String sendTxt = "";
        int count = 0, loopCount = 0;
        long lastReceiveCount = 0;
        while(is_run){
            count = 0;
            try{
                if(s==null){
                    s = createSocket();
                }
            }catch(Exception e){e.printStackTrace();}

            if(s!=null){
                try {
                    if (s.getInputStream().available() > 0) {
                        count = s.getInputStream().read(buffer);
                        if (count > 0) {
                            lastReceiveCount = System.currentTimeMillis();
                            byte[] b = new byte[count];
                            for (int i = 0; i < count; i++) {
                                b[i] = buffer[i];
                            }
                            String raw = bufferTxt+new String(b);
                            bufferTxt = "";
                            String[] strArr = raw.split("\n");
                            for (int i = 0; i < strArr.length; i++) {
                                if(i==strArr.length-1){
                                    if(!raw.endsWith("\n")){
                                        bufferTxt = strArr[i];
                                        break;
                                    }
                                }
                                strArr[i] = strArr[i].trim();
                                //Log.i(TAG, "read raw -> " + strArr[i]+" / "+Fun.bytesToHex(strArr[i].getBytes()));
                                if (strArr[i].contentEquals("")) continue;
                                if (key.contentEquals("")) {
                                    plainTxt = strArr[i];
                                } else {
                                    try {
                                        plainTxt = AES.decrypt(strArr[i], key);
                                    }catch(javax.crypto.IllegalBlockSizeException bse){
                                        bse.printStackTrace();
                                        Log.e(TAG, "error raw: " + raw);
                                        Log.e(TAG, "error: " + strArr[i]+" / "+key);
                                    }
                                }
                                if (plainTxt.length() < 2) continue;
                                if(!plainTxt.startsWith("HB") && !plainTxt.endsWith("OK") && !plainTxt.startsWith("LT")) {
                                    Log.i(TAG, "read -> " + plainTxt);
                                }
                                processRead(plainTxt);
                            }
                        }
                    }
                }catch(SocketTimeoutException se){
                }catch(Exception e){e.printStackTrace();closeSocket();}

                if(s!=null) {
                    try{
                        while(fbQueue.size()>0){
                            final String msg = fbQueue.get(0);
                            fbQueue.remove(0);
                            //Log.e(TAG, "fbQueue: "+msg);

                            if(msg.startsWith("LTcid")) {
                                //1:gid, 2:did, 3:d_last_fb, 4~:[gp,cid,val,last_fb]
                                final String[] strArr = msg.split("\\|");
                                if (strArr.length > 4) {
                                    if (!statusObj.has(strArr[2])) statusObj.put(strArr[2], new JSONObject());
                                    JSONObject obj = statusObj.getJSONObject(strArr[2]);
                                    obj.put("_is_loaded", 1);
                                    for (int i = 4; i < strArr.length; i++) {
                                        String[] pArr = strArr[i].split(",", -1);
                                        if (pArr.length == 4) {
                                            final long t = Long.parseLong(pArr[3]);
                                            if(!obj.has(pArr[0])) obj.put(pArr[0], new JSONObject());
                                            synchronized (obj.getJSONObject(pArr[0])) {
                                                if (obj.getJSONObject(pArr[0]).has("t") && obj.getJSONObject(pArr[0]).getLong("t") > t) continue;
                                                JSONObject vObj = new JSONObject();
                                                vObj.put("cid", pArr[1]);
                                                vObj.put("v", pArr[2]);
                                                vObj.put("t", t);
                                                obj.put(pArr[0], vObj);
                                            }
                                            new Thread(new Runnable(){
                                                @Override
                                                public void run() {
                                                    listener.onFB(strArr[2], pArr[0], pArr[1], pArr[2], t);
                                                }
                                            }).start();
                                        }
                                    }
                                    //statusObj.put(strArr[2], obj);
                                }
                            }else if(msg.startsWith("FBdevice") || msg.startsWith("CTdevice")){
                                //1:did, 2:gp, 3:cid, 4:v
                                final String[] strArr = msg.split("\\|");
                                if (strArr.length >= 5) {
                                    if (!statusObj.has(strArr[1])) statusObj.put(strArr[1], new JSONObject());
                                    JSONObject obj = statusObj.getJSONObject(strArr[1]);
                                    final long t = System.currentTimeMillis();
                                    if(!obj.has(strArr[2])) obj.put(strArr[2], new JSONObject());
                                    synchronized (obj.getJSONObject(strArr[2])) {
                                        if (obj.getJSONObject(strArr[2]).has("t") && obj.getJSONObject(strArr[2]).getLong("t") > t) return;
                                        JSONObject vObj = new JSONObject();
                                        vObj.put("cid", strArr[3]);
                                        vObj.put("v", strArr[4]);
                                        vObj.put("t", t);
                                        obj.put(strArr[2], vObj);
                                    }
                                    new Thread(new Runnable(){
                                        @Override
                                        public void run() {
                                            listener.onFB(strArr[1], strArr[2], strArr[3], strArr[4], t);
                                        }
                                    }).start();
                                    //statusObj.put(strArr[2], obj);
                                }
                            }
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                    try {
                        synchronized (writeQueue) {
                            if (writeQueue.size() > 0) {
                                sendTxt = writeQueue.get(0);
                                writeQueue.remove(0);
                                write(sendTxt);
                            }
                        }
                    } catch (Exception e) { e.printStackTrace(); closeSocket(); }
                }

                if(count==0){
                    if(lastReceiveCount==0){
                        loopCount = 0;
                        lastReceiveCount = System.currentTimeMillis();
                    }else if(System.currentTimeMillis()-lastReceiveCount>20*1000){
                        end();
                    }else{
                        if(loopCount%(50*15)==0){
                            send("HB"+rand.nextInt(999999));
                        }
                    }
                    loopCount++;
                }
            }else{
                try{
                    Thread.sleep(3000);
                }catch(Exception e){}
            }

            try{
                Thread.sleep(20);
            }catch(Exception e){}
        }
    }

    public boolean isReady(){
        return isReady;
    }

    private void processRead(final String msg){
        if(msg.startsWith("HB")) {
            if(!msg.contentEquals("HBOK")){
                send("HBOK");
            }
        }else if(msg.startsWith("KE")) {
            if (msg.contentEquals("KEOK")) {
            } else if (msg.contentEquals("KEER")) {
                //invalid_gw
            } else {
                key = msg.substring(2);
                send("CR" + Fun.bytesToHex(Cache.get("ssid").getBytes()) + "|" + Fun.bytesToHex(gw_id.getBytes()) + "|" + Fun.version);
            }
            return;
        }else if(msg.startsWith("CR")){
            isReady = true;
            listener.onReady();
        }else if(msg.startsWith("ER")) {
            if(!isReady) {
                isReady = true;
                listener.onReady();
            }
            listener.onError(msg);
        }else if(msg.startsWith("LTcid") || msg.startsWith("FBdevice") || msg.startsWith("CTdevice")) {
            fbQueue.add(msg);
        }
        new Thread(new Runnable(){
            @Override
            public void run() {
                try{
                    listener.onRead(msg);
                }catch(Exception e){}
            }
        }).start();
    }
    public void send(String msg){
        synchronized (writeQueue) {
            writeQueue.add(msg);
        }
    }
    private void write(final String msg) throws Exception{
        String m = (!key.contentEquals("")?AES.encrypt(msg, key):msg);
        if(!msg.startsWith("HB") && !msg.endsWith("OK") && !msg.startsWith("LT")) {
            Log.i(TAG, "write -> " + msg/*+" / "+m*/);
        }
        s.getOutputStream().write((m+"\n").getBytes());
        s.getOutputStream().flush();
    }
    private void closeSocket(){
        if(s!=null){
            try{
                s.getOutputStream().close();
                s.close();
            }catch(Exception e){}
            s = null;
        }
    }
    public void end(){
        end(true);
    }
    public void end(boolean isCallListener){
        isReady = false;
        is_run = false;
        closeSocket();
        if(isCallListener){
            listener.onEnd();
        }
    }





    //---------------------
    public boolean isDeviceStatusLoaded(String did){
        try{
            if(did.contentEquals("")) return true;
            if(statusObj.has(did)){
                if(statusObj.getJSONObject(did).has("_is_loaded")){
                    return true;
                }
            }
        }catch(Exception e){e.printStackTrace();}
        return false;
    }
    public void loadDeviceStatus(final String did){
        //Log.e(TAG, "loadDeviceStatus: "+did);
        try{
            if(did.contentEquals("")) return;

            if(!statusObj.has(did)) statusObj.put(did, new JSONObject());
            if(!statusObj.getJSONObject(did).has("_is_loaded")){
                statusObj.getJSONObject(did).put("_is_loaded", 1);
                //Log.e(TAG, "loadDeviceStatus: send: LTcid|"+gid+"|"+did);
                send("LTcid|"+gid+"|"+did);
            }else{
                JSONArray nArr = statusObj.getJSONObject(did).names();
                if(nArr!=null) {
                    //Log.e(TAG, "nArr: "+nArr);
                    for(int i=0;i<nArr.length();i++){
                        if(nArr.getString(i).contentEquals("_is_loaded")) continue;
                        JSONObject obj = statusObj.getJSONObject(did).getJSONObject(nArr.getString(i));
                        listener.onFB(did, nArr.getString(i), obj.getString("cid"), obj.getString("v"), obj.getLong("t"));
                    }
                }
            }
        }catch(Exception e){e.printStackTrace();}
    }
    public JSONObject getDeviceStatus(String did){
        try{
            if(statusObj.has(did)){
                return statusObj.getJSONObject(did);
            }
        }catch(Exception e){e.printStackTrace();}
        return null;
    }
    public JSONObject getDeviceStatus(String did, String gp){
        try{
            if(statusObj.has(did) && statusObj.getJSONObject(did).has(gp)){
                return statusObj.getJSONObject(did).getJSONObject(gp);
            }
        }catch(Exception e){e.printStackTrace();}
        return null;
    }
}
