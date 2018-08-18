package com.example.skynet.supremecourt;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class AcceptBookingActivity extends AppCompatActivity {
    Hearing hearing;
    String chosenTimeslot;
    Context context = this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accept_booking);
        Log.d("Ananda","In acceptBookingActivity");

        String hearingId = getIntent().getStringExtra("hearingId");
        chosenTimeslot = getIntent().getStringExtra("timeslot");

        final TextView bookingDetails = (TextView) findViewById(R.id.booking_details);
        final TextView timeslot = (TextView) findViewById(R.id.timeslot);
        final Button rejectButton = (Button) findViewById(R.id.reject_button);
        final Button acceptButton = (Button) findViewById(R.id.accept_button);

        // Make api call to get hearing object from hearingId
        RequestParams params = new RequestParams();
        params.put("hearingId",hearingId);
        SupremeCourtRESTClient.post("getHearing",params,new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    hearing = new Hearing(response.getJSONObject("hearing"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                bookingDetails.setText(hearing.hearingId+" | "+hearing.caseName);
                timeslot.setText("Confirm booking at "+chosenTimeslot+" on  "+hearing.justDate);

                acceptButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        acceptButton.setClickable(false);
                        rejectButton.setClickable(false);
                        RequestParams params = new RequestParams();
                        params.put("hearingId",hearing.hearingId);
                        params.put("acceptorNo",getSharedPreferences("DATA",MODE_PRIVATE).getString("phoneNo",null));
                        SupremeCourtRESTClient.post("acceptBooking",params,new JsonHttpResponseHandler(){
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                //super.onSuccess(statusCode, headers, response);
                                Log.d("Ananda","Accept Booking call successful!");
                                Intent intent = new Intent(context,ScheduleLoadingActivity.class);
                                intent.putExtra("hearingId",hearing.hearingId);
                                startActivity(intent);
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                                Log.d("Ananda","Booking call failed: "+responseString+ " status code: "+statusCode);
                                if(statusCode == 200) {
                                    Log.d("Ananda","Accept Booking call successful!");
                                    Intent intent = new Intent(context,ScheduleLoadingActivity.class);
                                    intent.putExtra("hearingId",hearing.hearingId);
                                    startActivity(intent);
                                }
                                Toast.makeText(context,R.string.acceptBookingFailed,Toast.LENGTH_SHORT);
                                acceptButton.setClickable(true);
                            }
                        });
                    }
                });

                rejectButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d("Ananda","reject button clicked");
                        acceptButton.setClickable(false);
                        rejectButton.setClickable(false);
                        RequestParams params = new RequestParams();
                        params.put("hearingId",hearing.hearingId);
                        params.put("rejectorNo",getSharedPreferences("DATA",MODE_PRIVATE).getString("phoneNo",null));
                        SupremeCourtRESTClient.post("rejectBooking",params,new JsonHttpResponseHandler(){
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                //super.onSuccess(statusCode, headers, response);
                                Log.d("Ananda","Reject booking successful!");
                                Intent intent = new Intent(context,SchedulingActivity.class);
                                intent.putExtra("hearingId",hearing.hearingId);
                                startActivity(intent);
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                                //super.onFailure(statusCode, headers, responseString, throwable);
                                if(statusCode == 200) {
                                    Log.d("Ananda","Reject booking successful!");
                                    Intent intent = new Intent(context,SchedulingActivity.class);
                                    intent.putExtra("hearingId",hearing.hearingId);
                                    startActivity(intent);
                                }
                                Toast.makeText(context,R.string.rejectBookingFailed,Toast.LENGTH_SHORT);
                                acceptButton.setClickable(true);
                            }
                        });
                    }
                });
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(context,R.string.failedToGetHearingDetails,Toast.LENGTH_SHORT).show();
            }
        });
    }
}
