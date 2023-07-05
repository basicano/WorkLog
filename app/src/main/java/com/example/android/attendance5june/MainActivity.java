// declares the package name for the Java file.
package com.example.android.attendance5june;

// import necessary classes from the AndroidX library for activity and fragment handling
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

// import various classes and utilities from the Android framework
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

// import classes related to JSON parsing and handling.
import org.json.JSONException;
import org.json.JSONObject;

// import classes for using HashMap and Map data structures.
import java.util.HashMap;
import java.util.Map;

// declares a class named MainActivity that extends AppCompatActivity and implements the VolleyJsonResponseListener interface.
public class MainActivity extends AppCompatActivity implements VolleyJsonResponseListener{
    // declare private variables to store shared preferences, QR code data, and a progress dialog object.
    private SharedPreferences pref;
    private static String qr_code_data = "";
    ProgressDialog loadingDialog;

    // method that shows the progress dialog if it's not already showing.
    private void showProgressDialog() {
        if (!loadingDialog.isShowing())
            loadingDialog.show();
    }

    // method that hides/dismisses the progress dialog if it's showing.
    private void hideProgressDialog() {
        if (loadingDialog.isShowing())
            loadingDialog.dismiss();
    }

    // This is the onCreate method of the activity. It is called when the activity is being created. 
    // Here, the layout for the activity is set, shared preferences and progress dialog are initialized, and the initFragment() method is called.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pref = getSharedPreferences(PrefsUserInfo.PREF_FILE_NAME, 0);
        loadingDialog = new ProgressDialog(MainActivity.this);
        initFragment();
    }

    // This is a method that initializes the fragment. It checks if the user is logged in based on the value stored in shared preferences. 
    // If logged in, it sets up a network request using PostVolleyJsonRequest to perform a login operation and shows the progress dialog. 
    // Otherwise, it sets the fragment to LoginFragment. Finally, it replaces the current fragment in the activity's layout container with the selected fragment.
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

    // This is the onResume method of the activity. It is called when the activity is resumed. Here, it logs the value of qr_code_data using Log.e().
    @Override
    protected void onResume() {
        super.onResume();
        Log.e("qr_code_data ", qr_code_data);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
    }

    // This is a method called onSuccessJson, which is implemented from the VolleyJsonResponseListener interface. 
    // It is called when a successful JSON response is received from a network request. Here, the JSON response is parsed and processed. 
    // If the response indicates success, it checks if a "qrcode" key is present in the JSON object and stores its value in shared preferences. 
    // Otherwise, if the response indicates an incorrect password, it updates the shared preferences and replaces the fragment with LoginFragment. 
    // If none of the above conditions are met, an error message is logged.
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

    // This is a method called onFailureJson, which is implemented from the VolleyJsonResponseListener interface. 
    // It is called when a network request fails. Here, it hides the progress dialog.
    @Override
    public void onFailureJson(int responseCode, String responseMessage) {
        hideProgressDialog();
    }
}
