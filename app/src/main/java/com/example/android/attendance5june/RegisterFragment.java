package com.example.android.attendance5june;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

public class  RegisterFragment extends Fragment implements VolleyJsonResponseListener {

    private static final String TAG = RegisterFragment.class.getSimpleName();
    private EditText email_et, name_et, password_et;
    private Button register_btn;
    private TextView login_tv;

    ProgressDialog progressDialog;
    SharedPreferences pref;

    private static final String file_name= "register.php";


    public RegisterFragment(){

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        pref = getContext().getSharedPreferences(getString(R.string.pref_name),0);
        progressDialog  = new ProgressDialog(getActivity()); // ??      getContext()
        progressDialog.setMessage("Please Wait");
        progressDialog.setCancelable(false);
        register_btn = (Button) view.findViewById(R.id.register_btn);
        login_tv = (TextView) view.findViewById(R.id.login_tv);

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
                    if(email.isEmpty() ){
                        email_et.setError("Email cannot be empty");
                    }
                    else if(!pat.matcher(email).matches()){
                        email_et.setError("Invalid Email Address");
                    }
                }
            }
        });


        name_et = (EditText) view.findViewById(R.id.name_et);
        name_et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String password = name_et.getText().toString().trim();
                if(password.isEmpty()){
//                    name_et.requestFocus();
                    name_et.setError("Password cannot be empty");
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

        login_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToLogin();
            }
        });

        register_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = email_et.getText().toString().trim();
                String name = name_et.getText().toString().trim();
                String password = password_et.getText().toString().trim();
                if(checkParameters(email, name, password)){
                    Map<String, String> params = new HashMap<>();
                    params.put("email", email);
                    params.put("name", name);
                    params.put("password", password);
                    new PostVolleyJsonRequest(getActivity(), RegisterFragment.this,
                            "register_req", file_name, params);
                }
            }
        });
    }

    private boolean checkParameters(String email, String name, String password) {
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
        if(name.isEmpty()){
            name_et.requestFocus();
            Toast.makeText(getActivity(), "Password is empty", Toast.LENGTH_SHORT).show();
            return false ;
        }

        if(password.isEmpty()){
            password_et.requestFocus();
            Toast.makeText(getActivity(), "Password is empty", Toast.LENGTH_SHORT).show();
            return false ;
        }
        return true;
    }


    private void goToLogin() {
        Fragment login_fragment = new LoginFragment();
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction().setReorderingAllowed(true);
        ft.replace(R.id.fragment_frame, login_fragment);
        ft.commit();
    }

    @Override
    public void onSuccessJson(String response, String type) {
        try {
            JSONObject jsonObject = new JSONObject( response );
            Log.v( "LoginReg 11 ", "onSuccessJson 11 = " + jsonObject );
            hideProgressDialog();
            if (!jsonObject.getBoolean( "error" )) {
                SharedPreferences.Editor editor = pref.edit();
                editor.putBoolean(PrefsUserInfo.PREF_IS_LOGGED_IN, true);
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
                editor.apply();
                goToProfile();
            }
            else{
                String error_msg = jsonObject.getString("error_msg");
                if(error_msg.equalsIgnoreCase("User already exists")){
                    email_et.setError("This email is already registered with us! Try logging in");
                    email_et.requestFocus();
                    login_tv.setTextSize(20);
                }
                else if(error_msg.equalsIgnoreCase("Unknown error occurred in registration!")){
                    Toast.makeText(getActivity(),"Unknown error occurred in registration!", Toast.LENGTH_LONG ).show();
                }
                else{
                    Toast.makeText(getActivity(), "Required fields missing!",Toast.LENGTH_LONG).show();
                }
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onFailureJson(int responseCode, String responseMessage) {

    }

    private void goToProfile() {
        Fragment profile_fragment = new ProfileFragment();
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction().setReorderingAllowed(true);
        ft.replace(R.id.fragment_frame, profile_fragment);
        ft.commit();
    }

    private void showProgressDialog() {
        if (!progressDialog.isShowing())
            progressDialog.show();
    }

    private void hideProgressDialog() {
        if (progressDialog.isShowing())
            progressDialog.dismiss();
    }
}