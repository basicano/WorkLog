package com.example.android.attendance5june;

public interface VolleyJsonResponseListener {

    public void onSuccessJson(String response, String type);
    public void onFailureJson(int responseCode, String responseMessage);
}
