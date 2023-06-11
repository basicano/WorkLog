package com.example.android.attendance5june;


import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.applandeo.materialcalendarview.listeners.OnCalendarPageChangeListener;
import com.applandeo.materialcalendarview.listeners.OnDayClickListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class  EmpViewAttFragment extends Fragment implements VolleyJsonResponseListener {
    private static final String TAG = EmpViewAttFragment.class.getSimpleName();


    TextView selected_date, status_et, in_time_et, outime_et;
    ProgressDialog progressDialog;

    private Calendar calendar = Calendar.getInstance();

    private CalendarView calendarView;
    private ArrayList<EventDay> calenderMarkDays = new ArrayList<>();
    SharedPreferences pref;

    private void showProgressDialog() {
        if (!progressDialog.isShowing())
            progressDialog.show();
    }

    private void hideProgressDialog() {
        if (progressDialog.isShowing())
            progressDialog.dismiss();
    }

    public EmpViewAttFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mainTabView = inflater.inflate(R.layout.fragment_emp_view_att,container,false);
        initView(mainTabView);
        return mainTabView;
    }

    private void initView(View view) {
        selected_date = (TextView)  view.findViewById(R.id.selected_date);
        status_et = (TextView) view.findViewById(R.id.status_et);
        in_time_et = (TextView) view.findViewById(R.id.inTime_et);
        outime_et = (TextView) view.findViewById(R.id.outTime_et);
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Please Wait");

        selected_date.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));

        pref = getActivity().getSharedPreferences(getString(R.string.pref_name), 0);
        PrefsUserInfo.getInstance().loadPreferences(getActivity());


        calendarView = (CalendarView) view.findViewById(R.id.calendarView);

        calendarView.setOnForwardPageChangeListener(new OnCalendarPageChangeListener(){

            @Override
            public void onChange() {
                Log.d(TAG, "Reached in onChange() in CalenderView setOnForwardPageChangeListener Listeners ");
                calendar.set(calendar.MONTH,  calendar.get( calendar.MONTH ) + 1);
                getAttListByDate(getFirstDate(calendar.DAY_OF_MONTH, calendar), getLastDate(calendar.DAY_OF_MONTH, calendar));
            }
        });

        calendarView.setOnPreviousPageChangeListener(new OnCalendarPageChangeListener() {
            @Override
            public void onChange() {
                Log.d(TAG, "Reached in onChange() in CalenderView setOnPreviousPageChangeListener Listeners ");
                calendar.set(calendar.MONTH,  calendar.get( calendar.MONTH ) - 1);
                getAttListByDate(getFirstDate(calendar.DAY_OF_MONTH, calendar), getLastDate(calendar.DAY_OF_MONTH, calendar));
            }
        });

        calendarView.setOnDayClickListener(new OnDayClickListener() {
            @Override
            public void onDayClick(EventDay eventDay) {
                Log.d(TAG, "Reached in onDayClick() in CalenderView Listeners ");

                Calendar cal = eventDay.getCalendar();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String sel_date = dateFormat.format(cal.getTime());
                selected_date.setText(sel_date);
                String curr_date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                Date date_s, date_c;
                try {
                    date_s = dateFormat.parse(sel_date);                                     // when this date is parsed the time is 00:00
                    date_c = dateFormat.parse(curr_date);
                    long difference = date_s.getTime() - date_c.getTime();
                    long differenceDates = difference / (24 * 60 * 60 * 1000);          // this indicates the number of days between these two dates
                    String dayDifference = Long.toString(differenceDates);
                    Log.e(TAG, "Diff - "+dayDifference);

                    if(differenceDates>0){
                        status_et.setText("---");
                        in_time_et.setText("---");
                        outime_et.setText("---");
                    }
                    else{
                        showProgressDialog();
                        Map<String, String> params = new HashMap<>();
                        params.put("email", pref.getString(PrefsUserInfo.PREF_EMAIL, ""));
                        params.put("date", sel_date);
                        params.put("type", "getRecord");

                        Log.e("params -->> ", params.toString());
                        new PostVolleyJsonRequest(getActivity(), EmpViewAttFragment.this,
                                "getRecord", "getAttRecord.php", params);
                    }
                }catch (Exception exception){
                    Toast.makeText(getActivity(), exception.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        getAttListByDate(getFirstDate(calendar.DAY_OF_MONTH, calendar), getLastDate(calendar.DAY_OF_MONTH, calendar));
        getRecordByDate(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
    }

    private void getRecordByDate(String date){
        Log.d(TAG, "Reached in getRecordByDate()");
        showProgressDialog();
        String file_name = "getAttRecord.php";
        Map<String, String> params = new HashMap<>();
        params.put("email", pref.getString(PrefsUserInfo.PREF_EMAIL, ""));
        params.put("date", date);
        params.put("type", "getRecord");
        new PostVolleyJsonRequest(getActivity(), EmpViewAttFragment.this,"getRecord", file_name, params);
    }

    private void getAttListByDate(String firstDate, String lastDate) {
        Log.d(TAG, "Reached in getAttListByDate()");
        String file_name = "getCalenderAtt.php";
        Map<String,String> params = new HashMap<>();
        params.put("email", pref.getString(PrefsUserInfo.PREF_EMAIL, "")); // PrefsUserInfo().getUserEmail
        params.put("firstDate", firstDate);
        params.put("lastDate", lastDate);
        params.put("type", "getCalAtt");
        new PostVolleyJsonRequest(getActivity(),EmpViewAttFragment.this, "getCalAtt",file_name, params);
    }

    private String getFirstDate(int dayOfMonth, Calendar cal) {
        cal.set(dayOfMonth, cal.getActualMinimum(Calendar.DAY_OF_MONTH)); //(Month, 1)
        return getFormatFromStringDate("yyyy-MM-dd", cal.getTime());
    }

    private String getLastDate(int dayOfMonth, Calendar cal) {
        cal.set(dayOfMonth,  cal.getActualMaximum(Calendar.DAY_OF_MONTH)); //(Month, 31)
        return getFormatFromStringDate("yyyy-MM-dd", cal.getTime());
    }

    public String getFormatFromStringDate(String pattern, Date date) {
        return new java.text.SimpleDateFormat(pattern).format(date); //java.text.DateFormat.getDateTimeInstance().format(ts.getTime())
    }

    @Override
    public void onSuccessJson(String response, String type) {
        Log.d(TAG, "Reached in onSuccessJson() "+response);
        hideProgressDialog();

        if(type.equalsIgnoreCase("getCalAtt")){
            try {
                JSONObject jsonObject = new JSONObject( response );
                Log.e(TAG, jsonObject.toString());
                if (jsonObject.getString( "success" ).equalsIgnoreCase( "successful" )) {
                    ArrayList<String> present_days = new Gson().fromJson(jsonObject.getJSONArray("present_days").toString(), new TypeToken<ArrayList<String>>(){}.getType());
                    Log.d(TAG, "present_days"+present_days.toString());
                    ArrayList<String> absent_days = new Gson().fromJson(jsonObject.getJSONArray("absent_days").toString(), new TypeToken<ArrayList<String>>(){}.getType());
                    Log.d(TAG, "absent_days"+ absent_days.toString());
                    for (String str_date : present_days) {
                        Calendar cal = toCalendar(str_date);
                        Log.d(TAG, cal.toString());
                        calenderMarkDays.add(new EventDay(cal, R.drawable.ic_circle_pr));
                    }

                    for (String str_date : absent_days) {
                        Calendar cal = toCalendar(str_date);
                        calenderMarkDays.add(new EventDay(cal, R.drawable.ic_circle_ab));
                    }
                    if (calenderMarkDays != null) {
                        calendarView.setEvents(calenderMarkDays);
                    }
                }
                else{
                    Toast.makeText(getActivity(), jsonObject.getString("error_msg"),Toast.LENGTH_LONG).show();
                }
            } catch (JSONException | ParseException e) {
                e.printStackTrace();
            }
        }
        else if(type.equalsIgnoreCase("getRecord")){
            try {
                JSONObject jsonObject = new JSONObject( response );
                Log.v( TAG, "onSuccessJson 11 = " + jsonObject );
                if (jsonObject.getString( "status" ).equalsIgnoreCase( "success" )) {
                    status_et.setText(jsonObject.getString("pr_ab"));
                    in_time_et.setText(jsonObject.getString("in_time"));
                    outime_et.setText(jsonObject.getString("out_time"));
                }
                else{
                    String error_msg = jsonObject.getString("error_msg");
                    Toast.makeText(getContext(),error_msg,Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else{
            Log.d(TAG, "error");
        }

    }

    public static Calendar toCalendar(String str_date) throws ParseException {
        Date date=  new SimpleDateFormat("yyyy-MM-dd").parse(str_date);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }

    @Override
    public void onFailureJson(int responseCode, String responseMessage) {
        hideProgressDialog();
        Log.d(TAG, responseMessage);
    }

}
