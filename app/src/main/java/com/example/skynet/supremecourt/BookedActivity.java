package com.example.skynet.supremecourt;

import android.app.Application;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class BookedActivity extends AppCompatActivity {
    Hearing hearing;
    String timeslot;
    Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booked);
        context = this;
        Log.d("Ananda","From booked activity: "+getIntent().getStringExtra("hearingId"));

        String hearingId = getIntent().getStringExtra("hearingId");
        timeslot = getIntent().getStringExtra("timeslot");
        final TextView textView = (TextView) findViewById(R.id.confirmed_booking_details);
        final TextView textView2 = (TextView) findViewById(R.id.confirmed_booking_time);

        RequestParams params = new RequestParams();
        params.add("hearingId",hearingId);
        SupremeCourtRESTClient.post("getHearing",params,new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    hearing = new Hearing(response.getJSONObject("hearing"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                textView.setText(""+hearing.caseNo+" | "+hearing.caseName);
                textView2.setText("Hearing is booked at "+timeslot+" on "+hearing.justDate);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(context,R.string.failedToGetHearingDetails, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
