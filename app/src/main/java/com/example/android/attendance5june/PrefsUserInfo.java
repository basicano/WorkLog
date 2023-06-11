package com.example.android.attendance5june;

import android.content.Context;
import android.content.SharedPreferences;


public class PrefsUserInfo {
    public static final String TAG = PrefsUserInfo.class.getSimpleName();

    public final static String PREF_FILE_NAME = "AttendancePreferences";

    public final static String PREF_UID = "uid";
    final static String PREF_EMAIL= "email";
    final static String PREF_PASS= "password";
    final static String PREF_NAME = "name";
    final static String PREF_IS_LOGGED_IN= "is_logged_in";
    public final static String PREF_IN_TIME = "in_time";
    public final static String PREF_ATT_DATE = "attendance_date";
    final static String PREF_DYN_ATT_QRKEY="dyn_att_qrkey";
    final static String PREF_IS_ADMIN="is_admin";

    public String uid = "";
    public String email = "";
    public String password = "";
    public String name = "";
    public String in_time = "";
    public Boolean is_logged_in = false;
    public String att_date= "";
    public String dyn_att_qrkey ="";
    public Boolean is_admin = false;

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    static PrefsUserInfo prefsUserInfo = new PrefsUserInfo();

    PrefsUserInfo(){

    }

    public static PrefsUserInfo getInstance() {
        return prefsUserInfo;
    }


    public void loadPreferences(Context context){
        pref = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        uid = pref.getString(PREF_UID, "default email accessed");
        email = pref.getString(PREF_EMAIL, "default email accessed");
        password = pref.getString(PREF_PASS, "def");
        name = pref.getString(PREF_NAME, "default name accessed");
        in_time = pref.getString(PREF_IN_TIME, "default in_time accessed");
        is_logged_in = pref.getBoolean(PREF_IS_LOGGED_IN, false);
        att_date = pref.getString(PREF_ATT_DATE, "default att_date accessed");
        dyn_att_qrkey= pref.getString(PREF_DYN_ATT_QRKEY, "default qr_code accessed");
        is_admin= pref.getBoolean(PREF_IS_ADMIN, false);
    }
}
