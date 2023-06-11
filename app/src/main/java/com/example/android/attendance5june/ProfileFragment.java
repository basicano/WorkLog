package com.example.android.attendance5june;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.attendance5june.QRCode.QRCodeScanner;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ProfileFragment extends Fragment implements VolleyJsonResponseListener{
    public static final String TAG = ProfileFragment.class.getSimpleName();
    private static String in_out_status = "in";

    TextView tv_name, tv_email, tv_intime;
    Button mark_att, change_password_btn, logout_btn, view_attendance_record_btn, manage_emp_btn, change_qrcode_btn;
    LinearLayout admin_controls;
    SharedPreferences pref;
    String type_cng_pass = "cng_pass", type_gen_qr = "grn_qr";

    GPSTracker gps;
    private ProgressDialog progressDialog;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void showProgressDialog() {
        if (!progressDialog.isShowing())
            progressDialog.show();
    }

    private void hideProgressDialog() {
        if (progressDialog.isShowing())
            progressDialog.dismiss();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_profile,container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Please Wait");
        tv_name = (TextView) view.findViewById(R.id.tv_name);
        tv_email = (TextView) view.findViewById(R.id.tv_email);
        pref = getActivity().getSharedPreferences(getString(R.string.pref_name), 0);
        PrefsUserInfo.getInstance().loadPreferences(getActivity());
        tv_name.setText(PrefsUserInfo.getInstance().name);
        tv_email.setText(PrefsUserInfo.getInstance().email);
        tv_intime = (TextView) view.findViewById(R.id.tv_intime);
        admin_controls = (LinearLayout) view.findViewById(R.id.admin_controls);
                
        if(PrefsUserInfo.getInstance().is_admin){
            admin_controls.setVisibility(View.VISIBLE);
        }

        try {
            String str_date1 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            String str_date2 = PrefsUserInfo.getInstance().att_date;
            Log.e(TAG,"date current -- "+ str_date1);
            Log.e(TAG,"date stored -- "+ str_date2);
            Date date1;
            Date date2;
            SimpleDateFormat dates = new SimpleDateFormat("yyyy-MM-dd");
            date1 = dates.parse(str_date1);                                     // when this date is parsed the time is 00:00
            date2 = dates.parse(str_date2);
            long difference = Math.abs(date1.getTime() - date2.getTime());
            long differenceDates = difference / (24 * 60 * 60 * 1000);          // this indicates the number of days between these two dates
            String dayDifference = Long.toString(differenceDates);
            Log.e(TAG, "Diff - "+dayDifference);

            if(differenceDates > 0) {
                tv_intime.setText("-- : --");
                in_out_status = "in";
            } else {
                tv_intime.setText("In : " + PrefsUserInfo.getInstance().in_time);
                in_out_status = "out";
            }
        } catch (Exception exception) {
            //  Toast.makeText(getActivity(), "Unable to find difference", Toast.LENGTH_SHORT).show();
        }

        mark_att = (Button) view.findViewById(R.id.mark_attendance);
        mark_att.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mark_attendance();
            }
        });

        logout_btn = (Button) view.findViewById(R.id.btn_logout);
        logout_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        change_password_btn = (Button) view.findViewById(R.id.btn_change_pass);
        change_password_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                change_password();
            }
        });

        view_attendance_record_btn = (Button) view.findViewById(R.id.btn_view_my_attendance);
        view_attendance_record_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view_attendance_record();
            }
        });

        change_qrcode_btn = (Button) view.findViewById(R.id.change_qrcode);
        change_qrcode_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                admin_change_qrcode();
            }
        });

        manage_emp_btn = (Button) view.findViewById(R.id.manage_emp);
        manage_emp_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                admin_manage_emp();
            }
        });
    }

    private void admin_manage_emp() {
    }

    private void admin_change_qrcode() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.dailog_change_qrcode, null);
        builder.setView(view);
        EditText qrcode_str = view.findViewById(R.id.et_qrcode_str);


        final AlertDialog dialog = builder.create();
        dialog.show();

        view.findViewById(R.id.buttonCreateQrCode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String qr_code = qrcode_str.getText().toString().trim();

                if (qr_code.isEmpty()) {
                    qrcode_str.setError("Please enter the current password");
                    qrcode_str.requestFocus();
                    return;
                }
                String file_name = "generate_qr_code.php";
                Map<String,String> params = new HashMap<>();
                params.put("admin_id", pref.getString(PrefsUserInfo.PREF_EMAIL, " "));
                params.put("qrcode",qr_code);
                params.put("type",type_gen_qr);
                showProgressDialog();
                new PostVolleyJsonRequest(getActivity(),ProfileFragment.this, type_gen_qr,file_name, params);
                dialog.dismiss();
            }
        });
    }

    public final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 112;
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public boolean checkPermission() {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion > Build.VERSION_CODES.LOLLIPOP) {
            if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity());
                    alertBuilder.setCancelable(true);
                    alertBuilder.setTitle("Security Permission");
                    alertBuilder.setMessage("ACCESS_FINE_LOCATION permission is necessary.");
                    alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions((Activity) getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                        }
                    });
                    AlertDialog alert = alertBuilder.create();
                    alert.show();
                } else {
                    ActivityCompat.requestPermissions((Activity) getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                }
                return false;
            } else {
                /*Intent intent = new Intent(getActivity(), ScanQRCode.class);
                startActivity(intent);*/
                return true;
            }
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    gps = new GPSTracker(getActivity());
                    Toast.makeText(getActivity(), "GPS Enabled, Go for Scanning ...", Toast.LENGTH_SHORT).show();
                } else {
                    //  code for deny
                }
                break;
        }
    }



    private void view_attendance_record() {
        Fragment fragment = new EmpViewAttFragment();
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction().setReorderingAllowed(true);
        ft.replace(R.id.fragment_frame,fragment);
        ft.commit();
    }

    private void change_password() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.dailog_change_password, null);
        builder.setView(view);


        EditText currentPassword = view.findViewById(R.id.et_current_pass);
        EditText newPassword = view.findViewById(R.id.et_new_pass);
        EditText verifyPassword = view.findViewById(R.id.et_verify_pass);


        final AlertDialog dialog = builder.create();
        dialog.show();

        view.findViewById(R.id.btnChangePassword).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String current_password = currentPassword.getText().toString().trim();
                String new_password = newPassword.getText().toString().trim();
                String verify_password = verifyPassword.getText().toString().trim();

                if (current_password.isEmpty()) {
                    currentPassword.setError("Please enter the current password");
                    currentPassword.requestFocus();
                    return;
                }
                if(new_password.isEmpty()){
                    newPassword.setError("Please enter the new password");
                    newPassword.requestFocus();
                    return;
                }
                if (verify_password.isEmpty()) {
                    verifyPassword.setError("Please re-enter the new password");
                    verifyPassword.requestFocus();
                    return;
                }
                else if(!new_password.equals(verify_password)){
                    verifyPassword.setError("New passwords donot match");
                    verifyPassword.setText("");
                    verifyPassword.requestFocus();
                    return;
                }

                if(current_password.equals(PrefsUserInfo.getInstance().password)){
                    String file_name = "change_password.php";
                    Map<String,String> params = new HashMap<>();
                    params.put("email",PrefsUserInfo.getInstance().email);
                    params.put("current_password", current_password);
                    params.put("new_password", new_password);
                    params.put("type", type_cng_pass);
                    showProgressDialog();
                    new PostVolleyJsonRequest(getActivity(),ProfileFragment.this, type_cng_pass,file_name, params);
                }
                else{
                    Log.d(TAG,"Pass: "+PrefsUserInfo.getInstance().password+"  user entered:"+current_password);
                    currentPassword.setError("This password is incorrect");
                    currentPassword.requestFocus();
                    return;
                }
                dialog.dismiss();
            }
        });
    }

    private void logout() {
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(getString(R.string.is_logged_in), false);
        editor.apply();
        Fragment login_fragment = new LoginFragment();
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction().setReorderingAllowed(true);
        ft.replace(R.id.fragment_frame, login_fragment);
        ft.commit();
    }

    private void mark_attendance() {
        gps = new GPSTracker(getActivity());
        gps.getLocation();

        double longitude = gps.getLongitude();
        double latitude = gps.getLatitude();

        if(gps.canGetLocation) {
            Log.i("GPS  ", "Enabled");
            boolean result = checkPermission();
            if (result) {
                latitude = gps.getLatitude();
                longitude = gps.getLongitude();
                Log.i("Offline ", "Lat + Long  " + latitude + " : " + longitude);
                //  Toast.makeText(getActivity(), "Offline   Lat + Long  " + latitude + " : " + longitude, Toast.LENGTH_LONG).show();

                if (latitude < 0 && longitude < 0) {
                    Toast.makeText(getActivity(), "Please wait\nSearching for location ... ", Toast.LENGTH_LONG).show();
                }
                else {
                    Log.d(TAG, "user current location, lat - " + latitude + ", long - " + longitude);
//                double office_latitude = 37.42342342342342;
//                double office_longitude = -122.08395287867832;
                    double office_latitude = 28.6770937;
                    double office_longitude = 77.0958365;
                    float[] results = new float[5];
                    Location.distanceBetween(latitude, longitude, office_latitude, office_longitude, results);
                    Log.e("distance - ", String.valueOf(results[0]) + String.valueOf(results[1]) +
                            String.valueOf(results[2]) + String.valueOf(results[3]) + String.valueOf(results[4]));
                    if (results[0] > 150) {
                        Toast.makeText(getActivity(), "Your detected location is too far from the office", Toast.LENGTH_LONG).show();
                    } else {
                        Intent intent = new Intent(getActivity(), QRCodeScanner.class);
                        startActivity(intent);
                    }
                }
            }
            else {
            Log.i("GPS ", "Disabled");
                gps.showSettingsAlert();
            }
        }
    }

    @Override
    public void onSuccessJson(String response, String type) {
        Log.e(TAG, "reached onSuccessJson() ");
        if(type.equalsIgnoreCase(type_cng_pass)){
            try {
                JSONObject jsonObject = new JSONObject( response );
                Log.e(TAG, jsonObject.toString());
                if (jsonObject.getString( "status" ).equalsIgnoreCase( "success" )) {
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString(PrefsUserInfo.PREF_PASS,jsonObject.getString("new_password"));
                    editor.apply();
                    Toast.makeText(getActivity(),"Password Changed successfully!",Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(getActivity(), jsonObject.getString("error_msg"),Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else if(type.equalsIgnoreCase(type_gen_qr)){
            try {
                JSONObject jsonObject = new JSONObject( response );
                Log.e(TAG, jsonObject.toString());
                if (jsonObject.getString( "status" ).equalsIgnoreCase( "success" )) {
                    Toast.makeText(getActivity(),"Password Changed successfully!",Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(getActivity(), jsonObject.getString("error_msg"),Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else{
            Log.d(TAG, "error");
        }

    }

    @Override
    public void onFailureJson(int responseCode, String responseMessage) {
        Log.d(TAG, responseMessage); 
    }
}