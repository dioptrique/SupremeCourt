package com.example.skynet.supremecourt;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class ScheduleLoadingActivity extends AppCompatActivity {
    Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_loading);
        context = this;

        final String hearingId = getIntent().getStringExtra("hearingId");

        final ProgressBar progress = findViewById(R.id.progressBar);

        RequestParams params = new RequestParams();
        params.add("hearingId",hearingId);
        SupremeCourtRESTClient.post("checkBookingStatus",params,new JsonHttpResponseHandler(){
            @Override
            public void onStart() {
                super.onStart();
                progress.setProgress(50);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                progress.setProgress(100);
                String status = null;
                String bookerNo = null;
                String timeslot = null;
                ArrayList<String> acceptedParties = new ArrayList<>();
                try {
                    status = ((JSONObject) response.getJSONObject("response")).getString("status");
                    Log.d("Ananda", "booking status: " + status);
                    if (!status.equals("idle")) {
                        Log.d("Ananda", "Status is not idle");
                        bookerNo = ((JSONObject) response.getJSONObject("response")).getString("bookerNo");
                        Log.d("Ananda", "bookerNo " + bookerNo);
                        timeslot = ((JSONObject) response.getJSONObject("response")).getString("timeslot");
                        Log.d("Ananda", "timeslot " + timeslot);
                        JSONArray acceptedPartiesJSON = ((JSONObject) response.getJSONObject("response")).getJSONArray("acceptedParties");
                        for (int i = 0; i < acceptedPartiesJSON.length(); i++) {
                            acceptedParties.add(acceptedPartiesJSON.getString(i));
                        }

                        Log.d("Ananda", "parsed response objects" + bookerNo + " " + timeslot);

                        if (status.equals("booked")) {
                            Intent intent = new Intent(context,BookedActivity.class);
                            intent.putExtra("hearingId",hearingId);
                            intent.putExtra("timeslot",timeslot);
                            startActivity(intent);
                        } else if (status.equals("rejected")) {
                            Intent intent = new Intent(context,SchedulingActivity.class);
                            intent.putExtra("hearingId",hearingId);
                            intent.putExtra("status",BookingStatus.REJECTED);
                            startActivity(intent);
                        } else if (status.equals("expired")) {
                            Intent intent = new Intent(context,SchedulingActivity.class);
                            intent.putExtra("hearingId",hearingId);
                            intent.putExtra("status",BookingStatus.EXPIRED);
                            startActivity(intent);
                        // If current party is the booker and the booking is ongoing
                        } else if (bookerNo.equals(getSharedPreferences("DATA", MODE_PRIVATE).getString("phoneNo", null)) && status.equals("ongoing")) {
                            Intent intent = new Intent(context,WaitingActivity.class);
                            intent.putExtra("hearingId",hearingId);
                            intent.putExtra("timeslot",timeslot);
                            startActivity(intent);
                        // If current party not the booker and the booking is ongoing
                        } else if (status.equals("ongoing")) {
                            boolean alreadyAccepted = false;
                            // Check if current user has already accepted booking
                            for (String partyNo : acceptedParties) {
                                if (partyNo.equals(getSharedPreferences("DATA", MODE_PRIVATE).getString("phoneNo", null))) {
                                    alreadyAccepted = true;
                                    Intent intent = new Intent(context,WaitingActivity.class);
                                    intent.putExtra("hearingId",hearingId);
                                    intent.putExtra("timeslot",timeslot);
                                    startActivity(intent);
                                    break;
                                }
                            }
                            // If booking is ongoing and current party has yet to accept booking
                            if (!alreadyAccepted) {
                                // redirect user to acceptBookingActivity page
                                Intent intent = new Intent(context, AcceptBookingActivity.class);
                                intent.putExtra("hearingId", hearingId);
                                intent.putExtra("timeslot", timeslot);
                                startActivity(intent);
                            }

                        }
                    } else {
                        Intent intent = new Intent(context,SchedulingActivity.class);
                        intent.putExtra("hearingId",hearingId);
                        intent.putExtra("status",BookingStatus.IDLE);
                        startActivity(intent);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }
}
