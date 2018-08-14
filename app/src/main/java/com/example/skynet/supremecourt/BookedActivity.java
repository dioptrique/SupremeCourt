package com.example.skynet.supremecourt;

import android.app.Application;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class BookedActivity extends AppCompatActivity {
    Hearing hearing;
    String timeslot;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booked);
        Log.d("Ananda","From booked activity: "+getIntent().getStringExtra("hearingId"));
        hearing = MyApplication.data.getHearing(getIntent().getStringExtra("hearingId"));
        timeslot = getIntent().getStringExtra("timeslot");

        TextView textView = (TextView) findViewById(R.id.confirmed_booking_details);
        TextView textView2 = (TextView) findViewById(R.id.confirmed_booking_time);

        textView.setText(""+hearing.hearingId+" | "+hearing.caseName);
        textView2.setText("Hearing is booked at "+timeslot+" on "+hearing.justDate);
    }
}
