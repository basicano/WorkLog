package com.example.android.attendance5june;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.SyncStateContract;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements VolleyJsonResponseListener{
    private SharedPreferences pref;
    private static String qr_code_data = "";
    ProgressDialog loadingDialog;

    private void showProgressDialog() {
        if (!loadingDialog.isShowing())
            loadingDialog.show();
    }

    private void hideProgressDialog() {
        if (loadingDialog.isShowing())
            loadingDialog.dismiss();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pref = getSharedPreferences(PrefsUserInfo.PREF_FILE_NAME, 0);
        loadingDialog = new ProgressDialog(MainActivity.this);
        initFragment();
    }

    private void initFragment() {
        Fragment fragment;
        if(pref.getBoolean(PrefsUserInfo.PREF_IS_LOGGED_IN,false)){
            loadingDialog.setMessage("Please Wait");
            showProgressDialog();
            Map<String, String> params = new HashMap<>();
            params.put("email", pref.getString(PrefsUserInfo.PREF_EMAIL, ""));
            params.put("password", pref.getString(PrefsUserInfo.PREF_PASS, ""));
            params.put("type", "Login");

            Log.e("params -->> ", params.toString());
            new PostVolleyJsonRequest(MainActivity.this, MainActivity.this,
                    "login_req", "login.php", params);
            fragment = new ProfileFragment();
        }else {
            fragment = new LoginFragment();
        }
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction().setReorderingAllowed(true);
        ft.replace(R.id.fragment_frame,fragment);
        ft.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("qr_code_data ", qr_code_data);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
    }

    @Override
    public void onSuccessJson(String response, String type) {
        try {
            SharedPreferences.Editor editor = pref.edit();
            JSONObject jsonObject = new JSONObject( response );
            Log.v( "LoginReg 11 ", "onSuccessJson 11 = " + jsonObject );
            hideProgressDialog();
            if (jsonObject.getString( "status" ).equalsIgnoreCase( "success" )) {
                if (jsonObject.has("qrcode")) {
                    Log.e("Yes Key ", jsonObject.getString("qrcode"));
                    editor.putString(PrefsUserInfo.PREF_DYN_ATT_QRKEY, jsonObject.getString("qrcode"));
                } else {
                    Log.e("No Key ", "NO");
                    editor.putString(PrefsUserInfo.PREF_DYN_ATT_QRKEY, jsonObject.getString("attendance app weblink.in pvt ltd"));
                }
                editor.apply();
            }
            else{
                String error_msg = jsonObject.getString("error_msg");
                if(error_msg.equalsIgnoreCase("incorrect password")){
                    editor.putBoolean(PrefsUserInfo.PREF_IS_LOGGED_IN, false);
                    editor.apply();
                    Fragment fragment = new LoginFragment();
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction().setReorderingAllowed(true);
                    ft.replace(R.id.fragment_frame,fragment);
                    ft.commit();
                }
                else {
                    Log.e("MainActivity", error_msg);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFailureJson(int responseCode, String responseMessage) {
        hideProgressDialog();
    }
}