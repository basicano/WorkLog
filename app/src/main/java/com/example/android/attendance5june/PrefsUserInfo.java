package com.example.android.attendance5june;

import android.content.Context;
import android.content.SharedPreferences;


public class PrefsUserInfo {
    public static final String TAG = PrefsUserInfo.class.getSimpleName();

    // Declare variables and constants
    public final static String PREF_FILE_NAME = "AttendancePreferences";

    // These constants represent keys for accessing preferences
    public final static String PREF_UID = "uid";
    final static String PREF_EMAIL= "email";
    final static String PREF_PASS= "password";
    final static String PREF_NAME = "name";
    final static String PREF_IS_LOGGED_IN= "is_logged_in";
    public final static String PREF_IN_TIME = "in_time";
    public final static String PREF_ATT_DATE = "attendance_date";
    final static String PREF_DYN_ATT_QRKEY="dyn_att_qrkey";
    final static String PREF_IS_ADMIN="is_admin";

    // Declare variables to hold user information
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

    // Create a single instance of PrefsUserInfo
    static PrefsUserInfo prefsUserInfo = new PrefsUserInfo();
    // Creating a single static instance of PrefsUserInfo in the above code follows the Singleton design pattern. 
    // The Singleton pattern ensures that only one instance of a class is created throughout the execution of a program, providing global access to that instance.
    // In this case, by creating a single static instance of PrefsUserInfo, you ensure that there is only one object of the class PrefsUserInfo in your application. 
    // This is useful when you want to access the same instance of PrefsUserInfo from different parts of your codebase.
    // Benefits of using a Singleton pattern in this context include:
    // Centralized access: All components of your application can access the same instance of PrefsUserInfo without needing to create new instances or pass references between objects.
    // Data consistency: Since there is only one instance, any changes made to the PrefsUserInfo object will be reflected throughout the application, ensuring data consistency.
    // Efficient resource utilization: Creating multiple instances of PrefsUserInfo can lead to unnecessary memory consumption. With a Singleton, you ensure that only one instance is created, optimizing resource utilization.
    // In summary, the static instance of PrefsUserInfo ensures that you have a single, globally accessible object to manage user preferences, providing consistency and efficiency in accessing and modifying user information throughout your application.
 
    PrefsUserInfo(){

    }
    
    // Retrieve the instance of PrefsUserInfo
    public static PrefsUserInfo getInstance() {
        return prefsUserInfo;
    }


    // Load preferences from SharedPreferences
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

// The PrefsUserInfo class stores user information in SharedPreferences, which allows data to persist across app launches.
// The PREF_FILE_NAME constant represents the name of the preferences file.
// The constants starting with PREF_ represent keys used to access specific preferences in the SharedPreferences.
// The variables declared inside the class will hold the user information retrieved from SharedPreferences.
// The SharedPreferences object (pref) and the SharedPreferences.Editor object (editor) are used to read from and write to the SharedPreferences, respectively.
// The getInstance() method returns a single instance of the PrefsUserInfo class using the Singleton design pattern.
// The loadPreferences() method retrieves the user information from SharedPreferences based on the keys defined earlier and assigns them to the respective variables in the class.
// Overall, this code allows you to store and retrieve user information using SharedPreferences, providing a convenient way to manage user preferences in your Android app.
