package com.controlfree.ha.vdp.controlfree2.utils;

import android.app.DownloadManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

public class FileHandler {
    private final static String TAG = "FileHandler";
    private final static String CONFIG_CF_DIR = "/ControlFree/";
    private final static String CONFIG_DIR = Environment.DIRECTORY_DOCUMENTS + CONFIG_CF_DIR;
    private final static String CONFIG_FILE = "app_config";

    private static void prepareConfigFile(Context c){
        try{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Uri contentUri = MediaStore.Files.getContentUri("external");
                String selection = MediaStore.MediaColumns.RELATIVE_PATH + "=?";
                String[] selectionArgs = new String[]{CONFIG_DIR};
                Cursor cursor = c.getContentResolver().query(contentUri, null, selection, selectionArgs, null);
                if (cursor.getCount()>0) {
                    while (cursor.moveToNext()) {
                        long id = cursor.getLong(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
                        Uri uri = ContentUris.withAppendedId(contentUri, id);
                        c.getContentResolver().delete(uri, null, null);
                    }
                }
            }else{
            }
        }catch(Exception e){e.printStackTrace();}
    }
    public static void saveAppId(Context c, String app_id){
        try{
            JSONObject obj = new JSONObject();
            obj.put("app_id", app_id);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                FileHandler.prepareConfigFile(c);
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, CONFIG_FILE);
                values.put(MediaStore.MediaColumns.MIME_TYPE, "application/json");
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, CONFIG_DIR);

                Uri uri = c.getContentResolver().insert(MediaStore.Files.getContentUri("external"), values);      //important!
                Log.e(TAG, "saveAppId: "+uri.toString());
                OutputStream outputStream = c.getContentResolver().openOutputStream(uri);
                try{
                    outputStream.write(obj.toString().getBytes("UTF-8"));
                }catch(Exception e2){e2.printStackTrace();
                }finally {
                    outputStream.close();
                }
            }else{
                String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString()+CONFIG_CF_DIR;
                File f_dir = new File(dir);
                if(!f_dir.exists()) f_dir.mkdirs();
                if(f_dir.exists()){
                    File f = new File(dir+CONFIG_FILE);
                    if(!f.exists()) f.createNewFile();
                    if(f.exists()){
                        //Log.e(TAG, "saveAppId: config exists");
                        FileOutputStream stream = new FileOutputStream(f);
                        try {
                            stream.write(obj.toString().getBytes("UTF-8"));
                        }catch(Exception e2){e2.printStackTrace();
                        }finally {
                            stream.close();
                        }
                    }
                }
            }
        }catch(Exception e){e.printStackTrace();}
    }
    public static String getSavedAppId(Context c){
        try{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Uri contentUri = MediaStore.Files.getContentUri("external");
                String selection = MediaStore.MediaColumns.RELATIVE_PATH + "=?";
                String[] selectionArgs = new String[]{CONFIG_DIR};
                Cursor cursor = c.getContentResolver().query(contentUri, null, selection, selectionArgs, null);
                Log.e(TAG, "cursor.getCount(): "+cursor.getCount());
                if (cursor.getCount()>0){
                    Uri uri = null;
                    while (cursor.moveToNext()) {
                        String fileName = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME));
                        Log.e(TAG, "fileName: "+fileName);
                        if (fileName.startsWith(CONFIG_FILE)) {
                            long id = cursor.getLong(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
                            uri = ContentUris.withAppendedId(contentUri, id);
                            break;
                        }
                    }
                    if(uri!=null){
                        String content = "";
                        InputStream in = c.getContentResolver().openInputStream(uri);
                        try{
                            byte[] bytes = new byte[1024];
                            int count = 0;
                            while ((count = in.read(bytes)) >= 0) {
                                content += new String(bytes, 0, count, "UTF-8");
                            }
                        }catch(Exception e2){e2.printStackTrace();
                        }finally {
                            in.close();
                        }
                        if(!content.contentEquals("")) {
                            JSONObject obj = new JSONObject(content);
                            return obj.getString("app_id");
                        }
                    }
                }
            }else{
                String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString()+CONFIG_CF_DIR;
                File f = new File(dir+CONFIG_FILE);
                if(f.exists()){
                    //Log.e(TAG, "getSavedAppId: config exists");
                    FileInputStream stream = new FileInputStream(f);
                    StringBuilder builder = new StringBuilder();
                    try {
                        byte[] b = new byte[1024];
                        int count = 0;
                        while((count = stream.read(b))>0){
                            builder.append(new String(b, 0, count, "UTF-8"));
                        }
                    }catch(Exception e2){e2.printStackTrace();
                    }finally {
                        stream.close();
                    }
                    String content = builder.toString();
                    if(!content.contentEquals("")) {
                        JSONObject obj = new JSONObject(content);
                        return obj.getString("app_id");
                    }
                }
            }
        }catch(Exception e){e.printStackTrace();}
        return "";
    }

    private static File prepareInternalDir(Context c, String dir){
        try{
            ContextWrapper cw = new ContextWrapper(c);
            String toPath = cw.getFilesDir().getAbsolutePath()+"/"+dir;
            File f = new File(toPath);
            if(!f.isDirectory()){
                f.mkdirs();
            }
            return f;
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }
    private static File prepareExternalDir(Context c, String dir){
        try{
            ContextWrapper cw = new ContextWrapper(c);
            String toPath = cw.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath()+"/"+dir;
            File f = new File(toPath);
            if(!f.isDirectory()){
                f.mkdirs();
            }
            return f;
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }
    public static void setConfigToPublicFile(Context c, JSONObject obj) {
        File exDir = prepareExternalDir(c, "CF");
        if (exDir == null) return;
        try{
            File f = new File(exDir.getAbsolutePath(), "config.json");
            if(!f.exists()) f.createNewFile();
            if(f.exists()){
                FileOutputStream stream = new FileOutputStream(f);
                try {
                    stream.write(obj.toString().getBytes("UTF-8"));
                }catch(Exception e2){e2.printStackTrace();
                }finally {
                    stream.close();
                }
            }
        } catch (Exception e) { e.printStackTrace();}
    }
    public static JSONObject getConfigFromPublicFile(Context c) {
        File exDir = prepareExternalDir(c, "CF");
        if (exDir == null) return null;
        try{
            File f = new File(exDir.getAbsolutePath(), "config.json");
            if(!f.exists()) f.createNewFile();
            if(f.exists()){
                FileInputStream stream = new FileInputStream(f);
                StringBuilder builder = new StringBuilder();
                try {
                    byte[] b = new byte[1024];
                    int count = 0;
                    while((count = stream.read(b))>0){
                        builder.append(new String(b, 0, count, "UTF-8"));
                    }
                }catch(Exception e2){e2.printStackTrace();
                }finally {
                    stream.close();
                }
                String content = builder.toString();
                if(!content.contentEquals("")) {
                    return new JSONObject(content);
                }
            }
        } catch (Exception e) { e.printStackTrace();}
        return null;
    }

    public static boolean isFileDownloaded(Context c, String url, String toDir){
        if(url.contentEquals("")) return true;
        File dir = prepareInternalDir(c, toDir);
        if(dir==null) return true;

        String[] strArr = url.split("/");
        if(strArr.length<=1) return true;
        String fileName = strArr[strArr.length-1];
        File f = new File(dir.getAbsolutePath()+"/"+fileName);
        if(f.exists()) return true;
        return FileHandler.isInDownloadQueue(c, url);
    }

    private static boolean isInDownloadQueue(Context c, String url){
        DownloadManager dlMgr = (DownloadManager)c.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Query q = new DownloadManager.Query();
        if(q==null) return true;
        q.setFilterByStatus(DownloadManager.STATUS_FAILED|DownloadManager.STATUS_PAUSED|DownloadManager.STATUS_SUCCESSFUL|
                DownloadManager.STATUS_RUNNING|DownloadManager.STATUS_PENDING);
        Cursor cur = dlMgr.query(q);
        cur.moveToFirst();
        boolean is = false;
        while(!cur.isAfterLast()){
            if(cur.getString(cur.getColumnIndex(DownloadManager.COLUMN_URI)).contentEquals(url)){
                int status = cur.getInt(cur.getColumnIndex(DownloadManager.COLUMN_STATUS));
                if(status==DownloadManager.STATUS_PENDING || status==DownloadManager.STATUS_RUNNING || status==DownloadManager.STATUS_SUCCESSFUL){
                    is = true;
                    break;
                }
            }
            cur.moveToNext();
        }
        cur.close();
        return is;
    }

    public static void downloadFile(Context c, String url, String toDir){
        if(url.contentEquals("")) return;
        File dir = prepareInternalDir(c, toDir);
        if(dir==null) return;
        File exDir = prepareExternalDir(c, toDir);
        if(exDir==null) return;

        String[] strArr = url.split("/");
        if(strArr.length<=1) return;
        String fileName = strArr[strArr.length-1];
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        Uri path = Uri.withAppendedPath(Uri.fromFile(exDir), fileName);
        request.setDestinationUri(path);
        DownloadManager dlMgr = (DownloadManager)c.getSystemService(Context.DOWNLOAD_SERVICE);
        dlMgr.enqueue(request);
    }
    public static void moveDownloadedFileToInternal(Context c, String url, String toDir){
        if(url.contentEquals("")) return;
        File dir = prepareInternalDir(c, toDir);
        if(dir==null) return;
        File exDir = prepareExternalDir(c, toDir);
        if(exDir==null) return;
        String[] strArr = url.split("/");
        if(strArr.length<=1) return;
        String fileName = strArr[strArr.length-1];

        copyFile(c, exDir.getAbsolutePath()+"/"+fileName, dir.getAbsolutePath()+"/"+fileName);
        //Log.e(TAG, "moveDownloadedFileToInternal: "+dir.getAbsolutePath()+"/"+fileName);
        //Log.e(TAG, "moveDownloadedFileToInternal: exists: "+(dir.exists()?"T":"F"));
    }
    public static Boolean copyFile(Context c, String fromFile, String toFile){
        try{
            FileInputStream inStream = new FileInputStream(fromFile);
            File outFile = new File(toFile);
            if(outFile.exists()) outFile.delete();
            FileOutputStream outStream = new FileOutputStream(toFile);
            FileChannel inChannel = inStream.getChannel();
            FileChannel outChannel = outStream.getChannel();
            inChannel.transferTo(0, inChannel.size(), outChannel);
            inStream.close();
            outStream.close();
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        //Log.e("copyFile", "to: "+toFile+", has new file? "+((new File(toFile)).exists()?"T":"F"));
        return (new File(toFile)).exists();
    }









    // image -------------------------------------
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        int reqHeight = (int) ((float)reqWidth*(float)height/(float)width);

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height;
            final int halfWidth = width;
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        if(inSampleSize>1) inSampleSize = inSampleSize/4;
        return inSampleSize;
    }
    public static Bitmap getDownloadedImgBitmap(Context c, String url, String dir, int toW){
        String[] strArr = url.split("/");
        if(strArr.length<=1) return null;
        String fileName = strArr[strArr.length-1];
        Bitmap bmpSrc = null;
        try{
            ContextWrapper cw = new ContextWrapper(c);
            String toPath = cw.getFilesDir().getAbsolutePath()+"/"+dir+"/"+fileName;
            File f = new File(toPath);
            //Log.e(TAG, "getDownloadedImgBitmap: "+toPath+" : "+(f.isFile()?"T":"F"));
            if(!f.isFile()) return null;
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(f.getAbsolutePath(), options);

            int scaleH = (int) ((float)toW*(float)options.outHeight/(float)options.outWidth);
            options.inSampleSize = calculateInSampleSize(options, toW);
            options.inJustDecodeBounds = false;
            options.inMutable = true;
            bmpSrc = BitmapFactory.decodeFile(f.getAbsolutePath(), options);

            if(bmpSrc!=null) {
                if(toW<options.outWidth){
                    bmpSrc = Bitmap.createScaledBitmap(bmpSrc, toW, scaleH, true);
                }
                //Log.e(TAG, "getThuBitmap: "+bmpSrc.getWidth()+":"+bmpSrc.getHeight()+" / "+toW+":"+scaleH);
            }
        }catch(Exception e){e.printStackTrace();
        }catch(Error err){ err.printStackTrace();}
        return bmpSrc;
    }
}
