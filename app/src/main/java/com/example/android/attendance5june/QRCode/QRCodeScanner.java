package com.example.android.attendance5june.QRCode;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.android.attendance5june.MainActivity;
import com.example.android.attendance5june.PostVolleyJsonRequest;
import com.example.android.attendance5june.PrefsUserInfo;
import com.example.android.attendance5june.R;
import com.example.android.attendance5june.VolleyJsonResponseListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class QRCodeScanner extends Activity implements VolleyJsonResponseListener {
    public static  final String TAG = QRCodeScanner.class.getSimpleName();

    TextView text;
    private SurfaceView mySurfaceView;
    private QREader qrEader;

    private SharedPreferences pref;

    final static int REQUEST_CODE = 1;
    private String file_name = "mark_attendance.php";
    boolean read = false;
    ProgressDialog progressDialog;
    boolean granted_once = false;

    private void showProgressDialog() {
        if (!progressDialog.isShowing())
            progressDialog.show();
    }

    private void hideProgressDialog() {
        if (progressDialog.isShowing())
            progressDialog.dismiss();
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrscanner);
        progressDialog = new ProgressDialog(getApplicationContext());
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Please Wait");
        pref = getSharedPreferences(PrefsUserInfo.PREF_FILE_NAME, 0);
        text = (TextView) findViewById(R.id.code_info);
        Button restartbtn = (Button) findViewById(R.id.btn_restart_activity);
        restartbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(QRCodeScanner.this, QRCodeScanner.class));
                finish();
            }
        });

        mySurfaceView = (SurfaceView) findViewById(R.id.camera_view);
        boolean result = checkPermission(QRCodeScanner.this);
        if(result && !granted_once){
            restartbtn.setVisibility(View.GONE);
            readQRCode();
        }
    }

    public boolean checkPermission(Context context){
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= Build.VERSION_CODES.LOLLIPOP) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.CAMERA)) {
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                    alertBuilder.setCancelable(true);
                    alertBuilder.setTitle("Permission necessary.");
                    alertBuilder.setMessage("Camera permission is necessary.");
                    alertBuilder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.CAMERA}, REQUEST_CODE);
                        }
                    });
                    AlertDialog alert = alertBuilder.create();
                    alert.show();
                } else {
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                    alertBuilder.setCancelable(true);
                    alertBuilder.setTitle("Permission necessary.");
                    alertBuilder.setMessage("Camera permission is necessary.");
                    alertBuilder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                        public void onClick(DialogInterface dialog, int which) {
                            context.startActivity(new Intent(Settings.ACTION_APPLICATION_SETTINGS));
                        }
                    });
                    AlertDialog alert = alertBuilder.create();
                    alert.show();
                }
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {

            case REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    readQRCode();
                } else {
                    //code for deny
//                    Toast.makeText(QRCodeScanner.this, "Permission Granted ...", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    // The readQRCode method initializes the qrEader instance and configures it with the provided QRDataListener. 
    // The QRDataListener listens for detected QR codes. When a QR code is detected, the listener's onDetected method is called. 
    // Inside this method, the scanned QR code data is decoded, processed, and appropriate actions are performed.
    private void readQRCode() {
        qrEader = new QREader.Builder(this, mySurfaceView, new QRDataListener() {

            @Override
            public void onDetected(final String data, final String rawData) {
                text.post(new Runnable() {
                    @Override
                    public void run() {
                        if(read  == false) {
                            read = true;
                            try {
                                byte[] datas = Base64.decode(data, Base64.DEFAULT);
                                String texts = new String(datas, "UTF-8");
                                StringBuffer sbr = new StringBuffer(texts);
                                String decoded_qr = sbr.reverse().toString();
                                Log.e("Scanned Data - ", decoded_qr);
                                if (decoded_qr.equalsIgnoreCase(PrefsUserInfo.getInstance().dyn_att_qrkey) && !granted_once) {
                                    granted_once = true;
//                                    read = true;
                                    Map<String, String> params = new HashMap<>();
                                    params.put("email", PrefsUserInfo.getInstance().email);
                                    Log.e(TAG, params.toString());
                                    new PostVolleyJsonRequest(QRCodeScanner.this,
                                            QRCodeScanner.this, "mark_att", file_name, params);
                                } else {
                                    Toast.makeText(QRCodeScanner.this, "Wrong QR Code, Try Again ...", Toast.LENGTH_LONG).show();
                                    read = false;
                                    finish();
                                }
                            } catch (UnsupportedEncodingException e) {
                                Log.i("UnsupportedEncoding ", "UnsupportedEncodingException");
                                e.printStackTrace();
                            } catch (IllegalArgumentException e) {
                                Log.i("IllegalArgumentExce ", data);
                                Toast.makeText(QRCodeScanner.this, "QR Data : " + data, Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        }).facing(QREader.BACK_CAM)
                .enableAutofocus(true)
                .height(mySurfaceView.getHeight())
                .width(mySurfaceView.getWidth())
                .build();

        //qrEader.start();
        qrEader.initAndStart(mySurfaceView);
    }

    // The onSuccessJson method is implemented from the VolleyJsonResponseListener interface. It is called when a successful JSON response is received from a network request made using Volley library. 
    // It processes the response, extracts relevant data, and performs actions accordingly. It also updates the shared preferences and starts a new activity.
    @Override
    public void onSuccessJson(String response, String type) {
        Log.v("MarkAttendance 11 ", "onSuccessJson 11 = " + response);
        //finish();
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(response);
            Log.v(TAG, "onSuccessJson = " + jsonObject);
            if(jsonObject.getString("success").equalsIgnoreCase("successful")) {
                SharedPreferences.Editor editor = pref.edit();
                Log.e(TAG, "Login time = " + jsonObject.getString("time"));
                editor.putString(PrefsUserInfo.PREF_IN_TIME, jsonObject.getString("time"));
                String str_date1 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                editor.putString(PrefsUserInfo.PREF_ATT_DATE, str_date1);
                editor.apply();
                Intent intent = new Intent(QRCodeScanner.this, MainActivity.class);
                startActivity(intent);
                granted_once = false;
                finish();
            }
            else{
                Toast.makeText(QRCodeScanner.this, jsonObject.getString("error_msg"),Toast.LENGTH_LONG);
                Intent intent = new Intent(QRCodeScanner.this, MainActivity.class);
                startActivity(intent);
                granted_once = false;
                finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFailureJson(int responseCode, String responseMessage) {
        read = false;
        Log.e(TAG, responseMessage);
    }
}
