package com.controlfree.ha.vdp.controlfree2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;

import com.controlfree.ha.vdp.controlfree2.component.ExImageView;
import com.controlfree.ha.vdp.controlfree2.component.ExPageNavView;
import com.controlfree.ha.vdp.controlfree2.component.ExTopbarView;
import com.controlfree.ha.vdp.controlfree2.utils.Cache;
import com.controlfree.ha.vdp.controlfree2.utils.DB;
import com.controlfree.ha.vdp.controlfree2.utils.FileHandler;
import com.controlfree.ha.vdp.controlfree2.utils.Fun;
import com.controlfree.ha.vdp.controlfree2.utils.ResolutionHandler;
import com.controlfree.ha.vdp.controlfree2.utils.TcpClient;
import com.controlfree.ha.vdp.controlfree2.view.HomeView;
import com.controlfree.ha.vdp.controlfree2.view.LoginView;
import com.controlfree.ha.vdp.controlfree2.view.ProfileView;
import com.controlfree.ha.vdp.controlfree2.view.RegisterView;
import com.controlfree.ha.vdp.controlfree2.view.SceneView;
import com.controlfree.ha.vdp.controlfree2.view.Server2View;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private FrameLayout f_main, f_content;
    private Context c;
    private TcpClient tcpClient;
    private FrameLayout current_layout;
    private boolean is_run = true;
    private static boolean is_connect_cloud = false;
    private ExTopbarView topbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        c = MainActivity.this;
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        ResolutionHandler.renew(c, getWindowManager());

        WindowInsetsControllerCompat windowInsetsController = ViewCompat.getWindowInsetsController(getWindow().getDecorView());
        if (windowInsetsController != null) {
            windowInsetsController.setSystemBarsBehavior(
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            );
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
        }

        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        registerReceiver(downloadReceiver, filter);

        f_main = (FrameLayout)findViewById(R.id.main);
        f_main.setBackgroundColor(0xff000000);
        //f_main.setBackgroundResource(R.drawable.gray_gradient_bg);

        ExImageView iv_bg = new ExImageView(getApplicationContext(), ResolutionHandler.getViewPortW(), ResolutionHandler.getViewPortH());
        iv_bg.setImageResource(R.drawable.bg);
        f_main.addView(iv_bg, 0);

        f_content = (FrameLayout)findViewById(R.id.content);

        ExTopbarView b = new ExTopbarView(c);
        b.setAlpha(0);
        b.setMode(ExTopbarView.MODE_NONE);
        f_main.addView(b);
        topbar = b;

        ExPageNavView v_nav = new ExPageNavView(c, listener_pageNav);
        Cache.navView = v_nav;

        ckPermission();

    }

    private String[] appPermissionArr = {android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE};
    private void ckPermission(){
        int count = 0;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (int i = 0; i < appPermissionArr.length; i++) {
                if (ActivityCompat.checkSelfPermission(this, appPermissionArr[i]) != PackageManager.PERMISSION_GRANTED) {
                    count++;
                }
            }
        }
        if(count>0){
            String[] pArr = new String[count];
            int index = 0;
            for(int i=0;i<appPermissionArr.length;i++) {
                if (ActivityCompat.checkSelfPermission(this, appPermissionArr[i]) != PackageManager.PERMISSION_GRANTED) {
                    pArr[index] = appPermissionArr[i];
                    index++;
                }
            }
            ActivityCompat.requestPermissions(this, pArr, 3456);
        }else{
            Cache.init(c);
            genLoginView(f_content);
            loop();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        ckPermission();
    }
    BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (id == -1) return;

            DownloadManager dlMgr = (DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);
            Cursor cur = dlMgr.query(new DownloadManager.Query().setFilterById(id));
            if (cur.moveToFirst()) {
                int status = cur.getInt(cur.getColumnIndex(DownloadManager.COLUMN_STATUS));
                if(status == DownloadManager.STATUS_SUCCESSFUL){
                    String url = cur.getString(cur.getColumnIndex(DownloadManager.COLUMN_URI)).replace(Fun.server_url, "");
                    String local_url = cur.getString(cur.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                    String dir = "";
                    if(local_url.contains("/room/thu/")){
                        dir = "room/thu";
                    }else if(local_url.contains("/server/thu/")){
                        dir = "server/thu";
                    }
                    //Log.e(TAG, "downloadReceiver: "+cur.getString(cur.getColumnIndex(DownloadManager.COLUMN_URI))+" > "+local_url+" / "+dir);
                    if(!dir.contentEquals("")){
                        FileHandler.moveDownloadedFileToInternal(c, url, dir);
                        if (current_layout instanceof Server2View) {
                            try {
                                Server2View v = (Server2View) current_layout;
                                v.onImgDownloaded(url);
                            } catch (Exception e) { e.printStackTrace(); }
                        }else if (current_layout instanceof HomeView) {
                            try {
                                HomeView v = (HomeView) current_layout;
                                v.onImgDownloaded(url);
                            } catch (Exception e) { e.printStackTrace(); }
                        }
                    }
                    dlMgr.remove(id);
                }
            }
        }
    };

    private long lastLayoutChangeTime = 0;
    private boolean isLayoutChanging = false;
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        if(!isLayoutChanging) {
            isLayoutChanging = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        Thread.sleep(3000);
                    } catch (Exception e) { e.printStackTrace(); }
                    isLayoutChanging = false;
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                }
            }).start();
        }
        resetLayout();
    }
    private void resetLayout(){
        ResolutionHandler.renew(c, getWindowManager());
        if(current_layout!=null) {
            if (current_layout instanceof LoginView) {
                genLoginView(f_content);
            } else if (current_layout instanceof Server2View) {
                genServerView(f_content);
            } else if (current_layout instanceof HomeView) {
                ((HomeView)current_layout).end();
                genHomeView(f_content);
            } else if (current_layout instanceof SceneView) {
                genSceneView(f_content);
            } else if (current_layout instanceof ProfileView) {
                genProfileView(f_content);
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if(current_layout!=null && !(current_layout instanceof LoginView)){
            try{
                connectCloud();
            } catch (Exception e) { e.printStackTrace(); }
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        disconnectCloud();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(downloadReceiver);
        is_run = false;
    }

    private void hideKeyboard(){
        try{
            View view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
    private int mainGetStatusBarH() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }









    //----------------------------


    private void loop(){
        if(is_run){
            if(current_layout!=null) {
                //connection ------------
                if(is_connect_cloud) {
                    runOnUiThread(new Runnable(){
                        @Override
                        public void run() {
                            long t = System.currentTimeMillis();
                            try {
                                if (!isTcpClientReady()) {
                                    if (topbar.getMode() != ExTopbarView.MODE_CONNECTING) {
                                        topbar.setMode(ExTopbarView.MODE_CONNECTING);
                                    }
                                } else {
                                    if (topbar.getMode() != ExTopbarView.MODE_CONNECTED) {
                                        topbar.setMode(ExTopbarView.MODE_CONNECTED);
                                    }
                                }
                                if (topbar.getMode() == ExTopbarView.MODE_CONNECTED) {
                                    if (t - topbar.getChangeModeTime() > 3000) {
                                        if (topbar.getAlpha() > 0)
                                            topbar.setAlpha(topbar.getAlpha() - 0.1f);
                                    }else{
                                        if (topbar.getAlpha() < 1) topbar.setAlpha(topbar.getAlpha() + 0.1f);
                                    }
                                } else if (topbar.getMode() == ExTopbarView.MODE_CONNECTING) {
                                    if (topbar.getAlpha() < 1) topbar.setAlpha(topbar.getAlpha() + 0.1f);
                                }
                            } catch (Exception e) { e.printStackTrace(); }
                        }
                    });
                }
            }
            new Thread(new Runnable(){
                @Override
                public void run() {
                    try{
                        Thread.sleep(30);
                    }catch(Exception e){}
                    loop();
                }
            }).start();
        }
    }

    private void genLoginView(FrameLayout f){
        is_connect_cloud = false;
        f.removeAllViews();

        LoginView v = new LoginView(c, new LoginView.Listener(){
            @Override
            public void onLogin() {
                Cache.syncData(c, new Cache.SyncListener(){
                    @Override
                    public void onSuccess() {
                        hideKeyboard();
                        connectCloud();
                    }
                    @Override
                    public void onFail(String msg) {
                        Fun.showAlert(c, Fun.getErrorMsg(msg));
                    }
                });
            }
            @Override
            public void onRegister() {
                genRegisterView(f_content);
            }
        });
        f.addView(v);
        current_layout = v;
    }
    private void genRegisterView(FrameLayout f){
        f.removeAllViews();
        RegisterView v = new RegisterView(c, new RegisterView.Listener(){
            @Override
            public void onBack() {
                genLoginView(f_content);
            }
        });
        f.addView(v);
        current_layout = v;
    }






    private void genServerView(FrameLayout f){
        f.removeAllViews();
        DB.setSetting(c, "tab", "0");

        Server2View xv = null;
        for(int i=0;i<Cache.viewList.size();i++){
            if(Cache.viewList.get(i) instanceof Server2View){
                xv = (Server2View)Cache.viewList.get(i);
                if(xv.getParent()!=null) ((FrameLayout)xv.getParent()).removeView(xv);
                f.addView(xv);
                current_layout = xv;
                break;
            }
        }
        if(xv==null) {
            Server2View v = new Server2View(c, new Server2View.Listener() {
                @Override
                public void onDeviceLoaded(JSONArray dArr) {
                    try {
                        for (int i = 0; i < dArr.length(); i++) {
                            //Log.e(TAG, "onDeviceLoaded: "+dArr.getJSONObject(i).getString("name"));
                            tcpClient.loadDeviceStatus(dArr.getJSONObject(i).getString("id"));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void downloadImg(String url) {
                    url = Fun.server_url + url;
                    //Log.e(TAG, "downloadImg: "+url);
//                if(!FileHandler.isFileDownloaded(c, url, "server/thu")){
//                    FileHandler.downloadFile(c, url, "server/thu");
//                }
                }

                @Override
                public void sendCmd(String msg) {
                    sendCommand(msg);
                }

                @Override
                public float getStatusBarH() {
                    return mainGetStatusBarH();
                }

                @Override
                public JSONObject getDeviceStatus(String id) {
                    if (tcpClient != null) {
                        try {
                            return tcpClient.getDeviceStatus(id);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    return null;
                }
            });
            f.addView(v);
            current_layout = v;
            Cache.viewList.add(v);
        }

        Cache.navView.setSelectedTab(0);
        f.addView(Cache.navView);
    }
    private void genHomeView(FrameLayout f){
        f.removeAllViews();
        DB.setSetting(c, "tab", "1");

        HomeView xv = null;
        for(int i=0;i<Cache.viewList.size();i++){
            if(Cache.viewList.get(i) instanceof HomeView){
                xv = (HomeView)Cache.viewList.get(i);
                if(xv.getParent()!=null) ((FrameLayout)xv.getParent()).removeView(xv);
                f.addView(xv);
                current_layout = xv;
                break;
            }
        }
        if(xv==null) {
            HomeView v = new HomeView(c, new HomeView.Listener() {
                @Override
                public void onDeviceLoaded(JSONArray dArr) {
                    try {
                        for (int i = 0; i < dArr.length(); i++) {
                            tcpClient.loadDeviceStatus(dArr.getJSONObject(i).getString("id"));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void downloadImg(String url) {
                    url = Fun.server_url + url;
                    //Log.e(TAG, "downloadImg: "+url);
//                if(!FileHandler.isFileDownloaded(c, url, "room/thu")){
//                    FileHandler.downloadFile(c, url, "room/thu");
//                }
                }

                @Override
                public void sendCmd(String msg) {
                    sendCommand(msg);
                }

                @Override
                public float getStatusBarH() {
                    return mainGetStatusBarH();
                }

                @Override
                public JSONObject getDeviceStatus(String id) {
                    if (tcpClient != null) {
                        try {
                            return tcpClient.getDeviceStatus(id);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    return null;
                }
            });
            f.addView(v);
            current_layout = v;
            Cache.viewList.add(v);
        }

        Cache.navView.setSelectedTab(1);
        f.addView(Cache.navView);
    }
    private void genSceneView(FrameLayout f) {
        f.removeAllViews();
        DB.setSetting(c, "tab", "2");

        SceneView v = new SceneView(c, new SceneView.Listener(){
            @Override
            public void sendCmd(String msg) {
                sendCommand(msg);
            }
        });
        f.addView(v);
        current_layout = v;

        Cache.navView.setSelectedTab(2);
        f.addView(Cache.navView);
    }
    private void genProfileView(FrameLayout f) {
        f.removeAllViews();
        DB.setSetting(c, "tab", "3");

        ProfileView v = new ProfileView(c, new ProfileView.Listener(){
            @Override
            public void onSelect_server(JSONObject obj) {
                try{
                    DB.setSetting(c, "gw_id", obj.getString("gw_id"));
                    Cache.viewList = new ArrayList();
                    Cache.syncData(c, new Cache.SyncListener(){
                        @Override
                        public void onSuccess() {
                            DB.setSetting(c, "tab", "0");
                            connectCloud();
                        }
                        @Override
                        public void onFail(final String msg) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Fun.showAlert(getApplicationContext(), msg);
                                }
                            });
                        }
                    });
                }catch(Exception e){e.printStackTrace();}
            }
            @Override
            public void onLogout() {
                DB.setSetting(c, "tab", "");
                DB.setSetting(c, "login_email", "");
                DB.setSetting(c, "login_pwd", "");
                DB.setSetting(c, "login_cid", "");
                Cache.viewList = new ArrayList<View>();
                genLoginView(f_content);
            }
        });
        f.addView(v);
        current_layout = v;

        Cache.navView.setSelectedTab(3);
        f.addView(Cache.navView);
    }
    private ExPageNavView.Listener listener_pageNav = new ExPageNavView.Listener(){
        @Override
        public void onClick(int index) {
            Log.e(TAG, "index: "+index);
            if(current_layout instanceof Server2View) {
                if(((Server2View)current_layout).isDataLoading()) return;
            }else if(current_layout instanceof HomeView) {
                if(((HomeView)current_layout).isDataLoading()) return;
            }
            if(index==0){
                genServerView(f_content);
            }else if(index==1){
                genHomeView(f_content);
            }else if(index==2){
                genSceneView(f_content);
            }else if(index==3){
                genProfileView(f_content);
            }
            System.gc();
        }
    };






    private void connectCloud(){
        new Thread(new Runnable(){
            @Override
            public void run() {
                is_connect_cloud = true;
            }
        }).start();

        disconnectCloud();
        runOnUiThread(new Runnable(){
            @Override
            public void run() {
                topbar.setMode(ExTopbarView.MODE_CONNECTING);
            }
        });
        tcpClient = new TcpClient(DB.getSetting(c, "gid"), DB.getSetting(c, "gw_id"), new TcpClient.Listener(){
            @Override
            public void onRead(String msg) {
                try{
                    if(msg.startsWith("CR")){

                    }else if(msg.startsWith("SCend")){
                        final String[] strArr = msg.split("\\|");
                        runOnUiThread(new Runnable(){
                            @Override
                            public void run() {
                                try{
                                    if(current_layout instanceof Server2View){
                                        Server2View view = (Server2View)current_layout;
                                        view.onSCend(strArr[1]);
                                    }else if(current_layout instanceof HomeView){
                                        HomeView view = (HomeView)current_layout;
                                        view.onSCend(strArr[1]);
                                    }else if(current_layout instanceof SceneView){
                                        SceneView view = (SceneView)current_layout;
                                        view.onSCend(strArr[1]);
                                    }
                                }catch(Exception e){e.printStackTrace();}
                            }
                        });
                    }else if(msg.startsWith("SC")){
                        final String[] strArr = msg.substring(2).split("\\|");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    if (current_layout instanceof Server2View) {
                                        Server2View view = (Server2View) current_layout;
                                        view.onSC(strArr[0]);
                                    }else if (current_layout instanceof HomeView) {
                                        HomeView view = (HomeView) current_layout;
                                        view.onSC(strArr[0]);
                                    }else if(current_layout instanceof SceneView){
                                        SceneView view = (SceneView)current_layout;
                                        view.onSC(strArr[0]);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }catch(Exception e){e.printStackTrace();}
            }
            @Override
            public void onFB(String did, String gp, String cid, String v, long t) {
                //Log.e(TAG, "onFB: "+did+" : "+gp+" : "+cid+" : "+v+" : " +t);
                runOnUiThread(new Runnable(){
                    @Override
                    public void run() {
                        try{
                            for(int i=0;i<Cache.viewList.size();i++){
                                if(Cache.viewList.get(i) instanceof Server2View){
                                    ((Server2View)Cache.viewList.get(i)).updateStatus(did, gp, cid, v, t);
                                }else if(Cache.viewList.get(i) instanceof HomeView){
                                    ((HomeView)Cache.viewList.get(i)).updateStatus(did, gp, cid, v, t);
                                }
                            }
//                            if(current_layout instanceof Server2View){
//                                Server2View view = (Server2View)current_layout;
//                                view.updateStatus(did, gp, cid, v, t);
//                            }else if(current_layout instanceof HomeView){
//                                HomeView view = (HomeView)current_layout;
//                                view.updateStatus(did, gp, cid, v, t);
//                            }
                        }catch(Exception e){e.printStackTrace();}
                    }
                });
            }
            @Override
            public void onReady() {
                Log.e(TAG, "tcpClient: onReady");
                runOnUiThread(new Runnable(){
                    @Override
                    public void run() {
                        String tab = DB.getSetting(c, "tab");
                        if(tab.contentEquals("0")) {
                            genServerView(f_content);
                            //sendCommand("IFHue|192.168.1.102:80|get_gp_list");
                        }else if(tab.contentEquals("1")) {
                            genHomeView(f_content);
                        }else if(tab.contentEquals("2")) {
                            genSceneView(f_content);
                        }else if(tab.contentEquals("3")) {
                            genProfileView(f_content);
                        }else{
                            genServerView(f_content);
                        }
                    }
                });
            }

            @Override
            public void onError(String msg) {
                Log.e(TAG, "tcpClient: onError(): "+msg);
                if(msg.contentEquals("ERNOGW")){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Fun.showAlert(c, "Server not connected");
                        }
                    });
                }
            }

            @Override
            public void onEnd() {
                Log.e(TAG, "tcpClient: onEnd()");
                connectCloud();
            }
        });
    }
    private void disconnectCloud(){
        if(tcpClient!=null){
            try {
                tcpClient.end(false);
            }catch(Exception e){e.printStackTrace();}
            tcpClient = null;
        }
    }
    private void sendCommand(String msg){
        if(tcpClient!=null){
            try {
                if(tcpClient.isReady()){
                    tcpClient.send(msg);
                }
            }catch(Exception e){e.printStackTrace();}
        }
    }
    private boolean hasTcpClient(){
        return (tcpClient!=null);
    }
    private boolean isTcpClientReady(){
        try{
            if(tcpClient!=null) {
                if (tcpClient.isReady()) return true;
            }
        }catch(Exception e){e.printStackTrace();}
        return false;
    }
}