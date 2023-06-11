package com.example.android.attendance5june;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PostVolleyJsonRequest {
    public static final String TAG = PostVolleyJsonRequest.class.getSimpleName();

    private String type;
    private Activity activity;
    private VolleyJsonResponseListener volleyJsonResponseListener;
//    private String networkURL = "http://192.168.0.102/login/";
    private String networkURL = "http://muser.weblink4you.com/attendance_android";
    private JSONObject jsonObject = null;
    Map<String, String> params = new HashMap<>();

    public PostVolleyJsonRequest(Activity activity, VolleyJsonResponseListener volleyJsonResponseListener,
                                 String type, String file_name, Map<String, String> params) {         // type specifies the process or activity that is making the network request
        this.activity = activity;
        this.volleyJsonResponseListener = volleyJsonResponseListener;
        this.type = type;
        this.networkURL = this.networkURL+ file_name;
        Log.v(TAG,"networkURL = " + networkURL);
        this.params = params;
        Log.v(TAG,"params = " + params);
        requestCall();
    }

    void requestCall(){

        RequestQueue requestQueue ;
        requestQueue = Volley.newRequestQueue(activity);
//        Cache cache = new DiskBasedCache(activity.getCacheDir(), 1024 * 1024);
//        Network network = new BasicNetwork(new HurlStack());
//        requestQueue = new RequestQueue(cache, network);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, networkURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        volleyJsonResponseListener.onSuccessJson(response, type);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                volleyJsonResponseListener.onFailureJson(0,error.toString() );
            }
        }) {
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Log.d(TAG, "sent params");
                return params;
            }
        };
        requestQueue.start();
        requestQueue.add(stringRequest);

    }
}
