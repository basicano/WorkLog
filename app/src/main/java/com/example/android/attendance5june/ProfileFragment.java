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


// declares a Java class named ProfileFragment which extends the Fragment class. It also implements the VolleyJsonResponseListener interface.
public class ProfileFragment extends Fragment implements VolleyJsonResponseListener{
    public static final String TAG = ProfileFragment.class.getSimpleName();
    private static String in_out_status = "in";

    // declare several variables of different types (TextView, Button, LinearLayout, SharedPreferences, and String) that will be used in the class.
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

    // method is overridden from the Fragment class and is responsible for creating the view hierarchy associated with the fragment. 
    // It inflates a layout file named fragment_profile, initializes the view elements, and returns the inflated view.
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_profile,container, false);
        initView(view);
        return view;
    }


    // method initializes the view elements and sets up a progress dialog with a message. 
    private void initView(View view) {
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Please Wait");

        // find the TextView elements with the IDs tv_name and tv_email from the provided view. 
        tv_name = (TextView) view.findViewById(R.id.tv_name);
        tv_email = (TextView) view.findViewById(R.id.tv_email);

        // They also retrieve shared preferences using the pref_name string resource and load the preferences into the PrefsUserInfo singleton instance. 
        pref = getActivity().getSharedPreferences(getString(R.string.pref_name), 0);
        PrefsUserInfo.getInstance().loadPreferences(getActivity());
        // The name and email values from the preferences are then set as text for the respective TextView elements.
        tv_name.setText(PrefsUserInfo.getInstance().name);
        tv_email.setText(PrefsUserInfo.getInstance().email);
        tv_intime = (TextView) view.findViewById(R.id.tv_intime);

        // the tv_intime TextView and admin_controls LinearLayout are found using their respective IDs. 
        // If the user is an admin (based on the value of is_admin in the preferences), the visibility of the admin_controls layout is set to View.VISIBLE.
        admin_controls = (LinearLayout) view.findViewById(R.id.admin_controls);        
        if(PrefsUserInfo.getInstance().is_admin){
            admin_controls.setVisibility(View.VISIBLE);
        }

        
        try {
            // calculates the difference in days between the current date and the stored attendance date. 
            // It retrieves the current date and the stored attendance date from the PrefsUserInfo singleton instance. 
            // The dates are parsed using the SimpleDateFormat class. 
            String str_date1 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            String str_date2 = PrefsUserInfo.getInstance().att_date;
            Log.e(TAG,"date current -- "+ str_date1);
            Log.e(TAG,"date stored -- "+ str_date2);
            Date date1;
            Date date2;
            SimpleDateFormat dates = new SimpleDateFormat("yyyy-MM-dd");
            date1 = dates.parse(str_date1);                                     // when this date is parsed the time is 00:00
            date2 = dates.parse(str_date2);

            // The difference in milliseconds is calculated, and then converted to the difference in days. 
            long difference = Math.abs(date1.getTime() - date2.getTime());
            long differenceDates = difference / (24 * 60 * 60 * 1000);          // this indicates the number of days between these two dates
            String dayDifference = Long.toString(differenceDates);
            Log.e(TAG, "Diff - "+dayDifference);

            // Depending on the difference, the tv_intime TextView is updated with either "-- : --" or "In: [in_time]". The in_out_status variable is also set accordingly.
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

        // the button is found using its ID. A click listener is set for the button, and when clicked, it calls the listener method.
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

    
    public final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 112; // value of 112 is used to identify a specific permission request.
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public boolean checkPermission() {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        
        // Checks if the current API version is higher than Android Lollipop (5.0).
        if (currentAPIVersion > Build.VERSION_CODES.LOLLIPOP) {
            if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                // Checks if the app should show a rationale for requesting the permission.
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

    // handle the user's response to a permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // checking the requestCode parameter to determine which permission request it is handling. This is useful when an activity requests multiple permissions and needs to differentiate between them.
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // if the permission was granted  length of grantResults should be greater than 0, indicating that at least one permission result is available. 
                    // the specific permission result is obtained from the grantResults array.
                    gps = new GPSTracker(getActivity());
                    // If the permission is granted, the code executes the corresponding actions. In the provided code, it creates a GPSTracker object (presumably for accessing GPS location) and displays a toast message indicating that GPS is enabled.
                    Toast.makeText(getActivity(), "GPS Enabled, Go for Scanning ...", Toast.LENGTH_SHORT).show();
                } else {
                    //  code for deny
                }
                break;
        }
    }

    // for displaying the attendance record of an employee. 
    private void view_attendance_record() {
        // creates an instance of the EmpViewAttFragment fragment and replaces the current fragment in the fragment_frame container
        Fragment fragment = new EmpViewAttFragment();
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction().setReorderingAllowed(true);
        ft.replace(R.id.fragment_frame,fragment);
        ft.commit();
    }

    // handles the functionality of changing the user's password
    private void change_password() {

        // shows an alert dialog with input fields for the current password, new password, and password verification.
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
                //  validates the input and sends a request to the server to change the password.
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

    // method handles the logout functionality
    private void logout() {
        // the shared preferences to mark the user as logged out and replaces the current fragment with the LoginFragment, effectively redirecting the user to the login screen.
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(getString(R.string.is_logged_in), false);
        editor.apply();
        Fragment login_fragment = new LoginFragment();
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction().setReorderingAllowed(true);
        ft.replace(R.id.fragment_frame, login_fragment);
        ft.commit();
    }

    // method is responsible for marking attendance. It uses the GPSTracker class to obtain the user's current GPS location. If the location is available and the user has granted the necessary permissions, it checks the distance between the user's location and the office location. If the user is within a certain distance, it starts the QRCodeScanner activity to scan a QR code for attendance marking.
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

    //  invoked when a JSON request is successful
    @Override
    public void onSuccessJson(String response, String type) {
        Log.e(TAG, "reached onSuccessJson() ");
        // checks if the type parameter is equal to a specific value (type_cng_pass). If they are equal, it means that the JSON request was for changing the password.
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
