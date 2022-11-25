package com.controlfree.ha.vdp.controlfree2.utils;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class HttpHandler {
    public static String TAG = "HttpHandler";
    public static interface Listener{
        public void onResponse(String result);
        public void onError(String msg);
    }
    public static interface JsonListener{
        public void onResponseJson(JSONObject resultObj);
        public void onError(String msg);
    }
    public static void get(Context c, String url, Listener l) {
        request(c, url, Request.Method.GET, l);
    }
    public static void delete(Context c, String url, Listener l) {
        request(c, url, Request.Method.DELETE, l);
    }
    public static void request(Context c, String url, int type, Listener l) {
        RequestQueue queue = Volley.newRequestQueue(c);
        StringRequest stringRequest = new StringRequest(type, url,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    l.onResponse(response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    l.onError("network_error");
                }
            });
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                7000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(stringRequest);
    }

    public static void post(Context c, String url, JSONObject obj, Listener l) {
        requestWithBody(c, url, obj, Request.Method.POST, l);
    }
    public static void put(Context c, String url, JSONObject obj, Listener l) {
        requestWithBody(c, url, obj, Request.Method.PUT, l);
    }
    public static void requestWithBody(Context c, String url, JSONObject obj, int type, Listener l) {
        RequestQueue queue = Volley.newRequestQueue(c);
        StringRequest stringRequest = new StringRequest(type, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        l.onResponse(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                l.onError("network_error");
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                try {
                    JSONArray nArr = obj.names();
                    if (nArr != null) {
                        for (int i = 0; i < nArr.length(); i++) {
                            map.put(nArr.getString(i), obj.getString(nArr.getString(i)));
                        }
                    }
                }catch(Exception e){e.printStackTrace();}
                return map;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                7000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(stringRequest);
    }


    public static void postJson(Context c, String url, String json, JsonListener l) {
        requestWithJson(c, url, json, Request.Method.POST, l);
    }
    public static void putJson(Context c, String url, String json, JsonListener l) {
        requestWithJson(c, url, json, Request.Method.PUT, l);
    }
    public static void requestWithJson(Context c, String url, String json, int type, JsonListener l){
        RequestQueue queue = Volley.newRequestQueue(c);
        JsonObjectRequest mJsonObjectRequest = new JsonObjectRequest(type, url, json,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        l.onResponseJson(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        l.onError("network_error");
                    }
                }
        );
        mJsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                7000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(mJsonObjectRequest);
    }
}
