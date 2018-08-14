package com.example.skynet.supremecourt;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import cz.msebera.android.httpclient.Header;

public class SchedulingActivity extends Activity {
    ArrayList<Party> parties = new ArrayList<>();
    Hearing hearing;
    LayoutInflater inflater;
    TreeMap<String,Boolean> timeSlots;
    HashMap<Party,View> partyToPhoneNo = new HashMap<Party,View>();
    List<String> times;
    Context context = this;
    RadioGroup slotSelector = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scheduling);

        Log.d("Ananda","Intent with action: "+getIntent().getAction()+", extra: "+getIntent().getStringExtra("hearingId"));
        hearing = MyApplication.data.getHearing(getIntent().getStringExtra("hearingId"));

        final Button button = (Button) findViewById(R.id.book_slot);
        button.setClickable(false);
        button.setText(R.string.loading);

        this.parties = hearing.parties;
        this.inflater = getLayoutInflater();

        RequestParams params = new RequestParams();
        params.add("hearingId",hearing.hearingId);
        // Make api call to check booking status of current booking
        SupremeCourtRESTClient.post("checkBookingStatus",params,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                String status = null;
                String bookerNo = null;
                String timeslot = null;
                ArrayList<String> acceptedParties = new ArrayList<>();
                try {
                    status = ((JSONObject) response.getJSONObject("response")).getString("status");
                    Log.d("Ananda","booking status: "+status);
                    if(!status.equals("idle")) {
                        Log.d("Ananda","Status is not idle");
                        bookerNo = ((JSONObject) response.getJSONObject("response")).getString("bookerNo");
                        Log.d("Ananda","bookerNo "+bookerNo);
                        timeslot = ((JSONObject) response.getJSONObject("response")).getString("timeslot");
                        Log.d("Ananda","timeslot "+timeslot);
                        JSONArray acceptedPartiesJSON =  ((JSONObject) response.getJSONObject("response")).getJSONArray("acceptedParties");
                        for(int i = 0; i < acceptedPartiesJSON.length(); i++) {
                            acceptedParties.add(acceptedPartiesJSON.getString(i));
                        }

                        Log.d("Ananda","parsed response objects"+ bookerNo +" "+timeslot);
                        for(String s : acceptedParties) {
                            Log.d("Ananda",s);
                        }
                        if (status.equals("booked")) {
                            Log.d("Ananda","Hearing is already booked. Redirecting to booked activity.");
                            // Start booked activity
                            Intent intent = new Intent(context,BookedActivity.class);
                            intent.putExtra("hearingId",hearing.hearingId);
                            intent.putExtra("timeslot",timeslot);
                            Log.d("Ananda","Passing "+hearing.hearingId+" and "+timeslot+" to BookedActivity.");
                            startActivity(intent);
                            Log.d("Ananda","Finishing schedule activity");
                            //Toast.makeText(context,"Finishing schedule activity",Toast.LENGTH_SHORT).show();
                            //finish();
                        } else if(status.equals("rejected")){
                            Log.d("Ananda","Status rejected. Enable booking");
                            button.setClickable(true);
                            button.setText(R.string.bookNow);
                        } else if(status.equals("expired")){
                            Log.d("Ananda","Status expired. Enable booking");
                            button.setClickable(true);
                            button.setText(R.string.bookNow);
                        } else if (bookerNo.equals(getSharedPreferences("DATA", MODE_PRIVATE).getString("phoneNo", null)) && status.equals("ongoing")) {
                                Log.d("Ananda","status is ongoing and current user sent booking");
                                // If current user is the booker disable booking button
                                button.setClickable(false);
                                button.setText(R.string.waitingForOthers);
                                Log.d("Ananda","button set to unclickable");
                        } else if (status.equals("ongoing")) {
                                Log.d("Ananda","status is ongoing and current user did not send booking");

                                boolean alreadyAccepted = false;
                                // Check if current user has already accepted booking
                                for (String partyNo : acceptedParties) {
                                    if (partyNo.equals(getSharedPreferences("DATA", MODE_PRIVATE).getString("phoneNo", null))) {
                                        // If user has already accepted booking just disable the
                                        // booking button
                                        button.setClickable(false);
                                        button.setText(R.string.waitingForOthers);
                                        alreadyAccepted = true;
                                        break;
                                    }
                                }
                                if(!alreadyAccepted) {
                                    Log.d("Ananda","Hearing is ongoing and user has yet to book. Redirecting to acceptance activity!.");
                                    // redirect user to acceptBookingActivity page
                                    Intent intent = new Intent(context,AcceptBookingActivity.class);
                                    intent.putExtra("hearingId",hearing.hearingId);
                                    intent.putExtra("timeslot",timeslot);
                                    startActivity(intent);
                                    finish();
                                }

                        }
                    } else {
                        button.setText(R.string.bookNow);
                        button.setClickable(true);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });


        View list = findViewById(R.id.list_of_parties);

        // Display hearing details
        TextView date = (TextView) findViewById(R.id.date);
        date.setText(hearing.justTime + " | " + hearing.justDate + " | "+hearing.venue);
        TextView caseInfo = (TextView) findViewById(R.id.case_info);
        caseInfo.setText(hearing.hearingId + " | " + hearing.caseNo + " | " + hearing.caseName);

        for(Party party : parties) {
            // Display each party
            View partyItem = inflater.inflate(R.layout.party_item,null);

            TextView partyType = partyItem.findViewById(R.id.party_label);
            partyType.setText(party.partyType + ":");
            TextView partyName = partyItem.findViewById(R.id.party_name);
            partyName.setText(party.partyName);
            ((ViewGroup) list).addView(partyItem);

            // Map each party to mobile_no input view to access the mobile_no. later
            partyToPhoneNo.put(party,partyItem.findViewById(R.id.mobile_no));
        }

        slotSelector = (RadioGroup) findViewById(R.id.slot_selector);
        setAvailableTimeSlots();
        // Handle clicking of book slot for first time
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                button.setClickable(false);
                button.setText(R.string.loading);

                List<String> validPhoneNos = new ArrayList<>();
                // Get only the phoneNos which are entered
                for(View phoneNoView : partyToPhoneNo.values()) {
                    String currPhoneNo = ((EditText) phoneNoView).getText().toString();
                    if(!currPhoneNo.equals("")) {
                        // Add the phoneNos to an array to be sent to notification sending api
                        validPhoneNos.add(currPhoneNo);
                    }
                }

                int checkedRadioButtonId = slotSelector.getCheckedRadioButtonId();
                boolean valid = validate(validPhoneNos,checkedRadioButtonId);
                if(!valid) {
                    button.setText(R.string.bookNow);
                    button.setClickable(true);
                    return;
                }
                String selectedTimeSlot = ((RadioButton) findViewById(checkedRadioButtonId)).getText().toString();

                // Send validPhoneNos through api call for notifications to be sent
                RequestParams params = new RequestParams();
                params.put("phoneNos",validPhoneNos);
                params.put("bookerNo",getSharedPreferences("DATA",MODE_PRIVATE).getString("phoneNo",null));
                params.put("timeslot",selectedTimeSlot);
                params.put("hearingId",hearing.hearingId);
                // Exclude booker from pendingPartiesCount
                params.put("partyCount",hearing.parties.size()-1);
                SupremeCourtRESTClient.post("bookNow", params, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        if(statusCode == 400) {
                            Toast.makeText(context,R.string.bookingFailed,Toast.LENGTH_SHORT).show();
                            button.setClickable(true);
                            button.setText("Book now");
                            return;
                        }
                        try {
                            if(response.getBoolean("timeslotAvailable")) {
                                if(hearing.parties.size() == 1) {
                                    recreate();
                                    Log.d("Ananda","Successful booking");
                                    Toast.makeText(context,R.string.bookingSingleSuccess,Toast.LENGTH_SHORT).show();
                                } else {
                                    Log.d("Ananda","Successful booking");
                                    button.setText(R.string.waitingForOthers);
                                    Toast.makeText(context,R.string.bookingSuccess,Toast.LENGTH_SHORT).show();
                                }
                            }
                            else {
                                // Call set available time slots again to update the ongoing/booked slots
                                // from backend
                                setAvailableTimeSlots();
                                Toast.makeText(context,R.string.slotAlreadyTaken,Toast.LENGTH_SHORT).show();
                                button.setClickable(true);
                                button.setText(R.string.bookNow);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        Log.d("Ananda","Booking failure: "+errorResponse.toString());
                        Toast.makeText(context,R.string.bookingFailed,Toast.LENGTH_SHORT).show();
                        button.setClickable(true);
                        button.setText("Book now");
                    }
                });
            }
        });
    }

    void setAvailableTimeSlots() {
        final ArrayList<String> availableTimeslots = new ArrayList<>();
        final ArrayList<String> allTimeslots = new ArrayList<>();
        Log.d("Ananda","In getAvailableTimeSlots");
        RequestParams params = new RequestParams();
        params.put("hearingId",hearing.hearingId);
        SupremeCourtRESTClient.post("getAvailableTimeslots",params,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("Ananda","Successfully received timeslots!");
                try {
                    JSONArray availableTimeslotsJSON = response.getJSONArray("availableTimeslots");
                    JSONArray allTimeslotsJSON = response.getJSONArray("allTimeslots");
                    for(int i = 0; i < availableTimeslotsJSON.length(); i++) {
                        availableTimeslots.add(availableTimeslotsJSON.getString(i));
                    }
                    for(int i = 0; i < allTimeslotsJSON.length(); i++) {
                        allTimeslots.add(allTimeslotsJSON.getString(i));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                TreeMap<String,Boolean> map = new TreeMap<>();

                for(String slot : allTimeslots) {
                    if(availableTimeslots.contains(slot)) {
                        map.put(slot,true);
                    } else {
                        map.put(slot,false);
                    }
                }

                // Get available time slots for booking for curr hearing
                ArrayList<String> times = new ArrayList<>(map.keySet());

                if(slotSelector.getChildCount() > 0) {
                    // If the radio group is already populated with radio buttons clear them
                    slotSelector.removeAllViews();
                }
                // Populate radio group with radio buttons
                for(String time : times) {
                    RadioButton rb = new RadioButton(context);
                    rb.setId(View.generateViewId());
                    rb.setLayoutParams(new LinearLayout.LayoutParams(
                            new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT)
                    ));
                    // Check if time slot is available
                    rb.setEnabled(map.get(time));
                    rb.setText(time);
                    slotSelector.addView(rb,-1);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(context,R.string.errorGettingTimeslots,Toast.LENGTH_SHORT).show();
                Log.d("Ananda","Error getting timeslots");
            }
        });
    }

    boolean validate(List<String> phoneNos, int checkRadioButtonId) {
        //TODO a more robust validation of phoneNos
        if(phoneNos.size() == 0 && hearing.parties.size() > 1) {
            Toast.makeText(context,R.string.noNumberError,Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(checkRadioButtonId == -1) {
            Toast.makeText(context,R.string.noTimeslotSelected,Toast.LENGTH_SHORT).show();
            return false;
        }
        else {
            return true;
        }
    }
}
