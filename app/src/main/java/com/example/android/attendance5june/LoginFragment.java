package com.example.android.attendance5june;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class LoginFragment extends Fragment implements VolleyJsonResponseListener {
    public static final String TAG = LoginFragment.class.getSimpleName();

    // These lines declare variables used within the LoginFragment class. 
    // They represent UI components like Button, EditText, and TextView. 
    private Button login_btn;
    private EditText email_et, password_et;
    private TextView register_tv;
    
    // SharedPreferences is used for storing data, and ProgressDialog is used for displaying progress. 
    private SharedPreferences pref;


    ProgressDialog progressDialog;
    // file_name represents the name of the PHP file used for login.
    private String file_name = "login.php";

    public LoginFragment() {
        // Required empty public constructor
    }

    // This method is overridden from the Fragment class and is called when the fragment is being created. 
    // It is responsible for any initialization tasks specific to the fragment.
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG,"Login Fragment started");
        View view =  inflater.inflate(R.layout.fragment_login, container, false);
        initView(view);
        return view;
    }

    // This method is overridden from the Fragment class and is responsible for creating and returning the fragment's view hierarchy. 
    // In this case, it inflates the layout file fragment_login.xml and initializes the view components by calling the initView() method.
    public void initView(View view){
        pref = getContext().getSharedPreferences(PrefsUserInfo.PREF_FILE_NAME,0);

        if(pref.getBoolean(PrefsUserInfo.PREF_IS_LOGGED_IN, false)){
            goToProfile();
        }
        email_et = (EditText) view.findViewById(R.id.email_et);
        email_et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String email = email_et.getText().toString().trim();
                if(email.isEmpty() ){
//                    email_et.requestFocus();
                    email_et.setError("Email cannot be empty");
                }

            }
        });

        email_et.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    String email = email_et.getText().toString().trim();
                    String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+
                            "[a-zA-Z0-9_+&*-]+)*@" +
                            "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                            "A-Z]{2,7}$";
                    Pattern pat = Pattern.compile(emailRegex);
                    if(email.isEmpty() ){;
                        email_et.setError("Email cannot be empty");
                    }
                    else if(!pat.matcher(email).matches()){
                        email_et.setError("Invalid Email Address");
                    }
                }
            }

        });

        password_et = (EditText) view.findViewById(R.id.password_et);
        password_et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String password = password_et.getText().toString().trim();
                if(password.isEmpty()){
//                    password_et.requestFocus();
                    password_et.setError("Password cannot be empty");
                }
            }
        });

        login_btn = (Button) view.findViewById(R.id.login_btn);
        register_tv = (TextView) view.findViewById(R.id.register_tv);
//        forgotpass_tv = (TextView) view.findViewById(R.id.forgotpass_tv);

        progressDialog  = new ProgressDialog(getActivity());
        progressDialog.setMessage("Please Wait");
        progressDialog.setCancelable(false);

        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = email_et.getText().toString().trim();
                String password = password_et.getText().toString().trim();
                if(checkParameters(email, password)){
                    showProgressDialog();
                    Map<String, String> params = new HashMap<>();
                    params.put("email", email);
                    params.put("password", password);

                    new PostVolleyJsonRequest(getActivity(), LoginFragment.this,
                            "login_request", file_name, params);
                }

            }
        });

        register_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToRegister();
            }
        });
    }
    
    // This method performs validation checks on the email and password entered by the user. 
    // It ensures that the fields are not empty and that the email follows a specific pattern.
    private boolean checkParameters(String email, String password) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";

        Pattern pat = Pattern.compile(emailRegex);

        if(email.isEmpty() ){
            email_et.requestFocus();
            Toast.makeText(getActivity(), "Email is empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(!pat.matcher(email).matches()){
            email_et.requestFocus();
            Toast.makeText(getActivity(), "Invalid Email address", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(password.isEmpty()){
            password_et.requestFocus();
            Toast.makeText(getActivity(), "Password is empty", Toast.LENGTH_SHORT).show();
            return false ;
        }
        return true;
    }

    // This method is responsible for navigating to the register fragment when the user clicks on the register text view. 
    // It replaces the current fragment with a new instance of the RegisterFragment.
    private void goToRegister() {
        Fragment register = new RegisterFragment();
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction().setReorderingAllowed(true);
        ft.replace(R.id.fragment_frame, register);
        ft.commit();
    }

    // This method is overridden from the VolleyJsonResponseListener interface and is called when a JSON response is received successfully. 
    // It handles the response and takes appropriate actions based on the data.
    @Override
    public void onSuccessJson(String response, String type) {
        try {
            JSONObject jsonObject = new JSONObject( response );
            Log.v( "LoginReg 11 ", "onSuccessJson 11 = " + jsonObject );
            hideProgressDialog();
            if (jsonObject.getString( "status" ).equalsIgnoreCase( "success" )) {
                SharedPreferences.Editor editor = pref.edit();
                editor.putBoolean(PrefsUserInfo.PREF_IS_LOGGED_IN, true);
                editor.putString(PrefsUserInfo.PREF_UID,jsonObject.getString( "uid" ));
                JSONObject user =  jsonObject.getJSONObject("user") ;
                editor.putString(PrefsUserInfo.PREF_EMAIL, user.getString("email"));
                editor.putString(PrefsUserInfo.PREF_NAME, user.getString("name"));
                editor.putString(PrefsUserInfo.PREF_PASS, password_et.getText().toString().trim());
                if (jsonObject.has("qrcode")) {
                    Log.e("Yes Key ", "YES");
                    editor.putString(PrefsUserInfo.PREF_DYN_ATT_QRKEY, jsonObject.getString("qrcode"));
                } else {
                    Log.e("No Key ", "NO");
                    editor.putString(PrefsUserInfo.PREF_DYN_ATT_QRKEY, jsonObject.getString("attendance app weblink.in pvt ltd"));
                }
                if (jsonObject.has("is_admin")) {
                    Log.e("User type: ", "Admin");
                    editor.putBoolean(PrefsUserInfo.PREF_IS_ADMIN,true);

                    editor.apply();
                    goToAdminProfile();
                }
//                Log.d(TAG,user.getString("name"));
                editor.apply();
                goToProfile();
            }
            else{
                String error_msg = jsonObject.getString("error_msg");
                if(error_msg.equalsIgnoreCase("unregistered email")){
                    email_et.setError("Email not registered!");
                    email_et.requestFocus();
                    register_tv.setTextSize(20);
                }
                else if(error_msg.equalsIgnoreCase("incorrect password")){
                    password_et.setError("Incorrect password!");
                    password_et.requestFocus();
                    register_tv.setVisibility((View.GONE));
//                    forgotpass_tv.setVisibility(View.VISIBLE);
                }
                else{
                    Toast.makeText(getActivity(), "Required fields missing!",Toast.LENGTH_LONG).show();
                }
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void goToAdminProfile() {
        Fragment profile_fragment = new ProfileFragment();
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction().setReorderingAllowed(true);
        ft.replace(R.id.fragment_frame, profile_fragment);
        ft.commit();
    }

    // This method is responsible for navigating to the admin profile fragment after successful login. 
    // It replaces the current fragment with a new instance of the ProfileFragment.
    private void goToProfile() {
        Fragment profile_fragment = new ProfileFragment();
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction().setReorderingAllowed(true);
        ft.replace(R.id.fragment_frame, profile_fragment);
        ft.commit();
    }
    
    // This method is overridden from the VolleyJsonResponseListener interface and is called when a JSON request fails. 
    // It handles the failure and takes appropriate actions based on the response code and message.
    @Override
    public void onFailureJson(int responseCode, String responseMessage) {
        hideProgressDialog();
        Log.v( TAG, responseMessage );
        Toast.makeText(getActivity(), "error occurred ",Toast.LENGTH_SHORT).show();
    }

    // These methods are used to show and hide the progress dialog, respectively, when needed.
    private void showProgressDialog() {
        if (!progressDialog.isShowing())
            progressDialog.show();
    }

    private void hideProgressDialog() {
        if (progressDialog.isShowing())
            progressDialog.dismiss();
    }
}
