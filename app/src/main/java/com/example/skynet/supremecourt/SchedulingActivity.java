package com.example.skynet.supremecourt;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

        // Get the view instances of the of the current activity
        final View list = findViewById(R.id.list_of_parties);
        final TextView date = (TextView) findViewById(R.id.date);
        final TextView caseInfo = (TextView) findViewById(R.id.case_info);
        slotSelector = (RadioGroup) findViewById(R.id.slot_selector);
        this.inflater = getLayoutInflater();
        final Button button = (Button) findViewById(R.id.book_slot);
        button.setClickable(false);
        button.setText(R.string.loading);

        String hearingId = getIntent().getStringExtra("hearingId");
        BookingStatus status = (BookingStatus) getIntent().getSerializableExtra("status");

        // Make api call to get the hearing details of the current hearing
        RequestParams params = new RequestParams();
        params.put("hearingId",hearingId);
        SupremeCourtRESTClient.post("/getHearing",params,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    hearing = new Hearing(response.getJSONObject("hearing"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                parties = hearing.parties;
                // Display hearing details
                date.setText(hearing.justTime + " | " + hearing.justDate + " | "+hearing.venue);
                caseInfo.setText(hearing.hearingId + " | " + hearing.caseNo + " | " + hearing.caseName);

                // Display each party
                for(Party party : parties) {
                    View partyItem = inflater.inflate(R.layout.party_item,null);

                    TextView partyType = partyItem.findViewById(R.id.party_label);
                    partyType.setText(party.partyType + ":");
                    TextView partyName = partyItem.findViewById(R.id.party_name);
                    partyName.setText(party.partyName);
                    ((ViewGroup) list).addView(partyItem);

                    // Map each party to mobile_no input view to access the mobile_no. later
                    partyToPhoneNo.put(party,partyItem.findViewById(R.id.mobile_no));
                }

                // Make api call to get available timeslots and render them in the radiogroup
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

                        // Form validation
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
                                } else if(statusCode == 200) {
                                    try {
                                        String bookingStatus = response.getString("bookingStatus");
                                        if(bookingStatus.equals("successful")) {
                                            // Redirecting to loading page to decide which activity to start next
                                            Intent intent = new Intent(context,ScheduleLoadingActivity.class);
                                            intent.putExtra("hearingId",hearing.hearingId);
                                            startActivity(intent);
                                            if(hearing.parties.size() == 1) {
                                                Toast.makeText(context,R.string.bookingSingleSuccess,Toast.LENGTH_SHORT).show();

                                            } else {
                                                Toast.makeText(context,R.string.bookingSuccess,Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                        else if(bookingStatus.equals("unsuccessful")){
                                            // Call set available time slots again to update the ongoing/booked/outdated slots
                                            // from backend
                                            setAvailableTimeSlots();
                                            // Get the reason for unsuccessful booking
                                            String reason = response.getString("reason");
                                            // If current time has already passed current time
                                            if(reason.equals("outdated")) {
                                                Toast.makeText(context,R.string.slotOutdated,Toast.LENGTH_SHORT).show();
                                            // If slot is already taken by another hearing on the same date and venue
                                            } else if(reason.equals("taken")) {
                                                Toast.makeText(context,R.string.slotAlreadyTaken,Toast.LENGTH_SHORT).show();

                                            }
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                                Toast.makeText(context,R.string.bookingFailed,Toast.LENGTH_SHORT).show();
                                button.setClickable(true);
                                button.setText("Book now");
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

                //Make bookNow button clickable
                Button button = findViewById(R.id.book_slot);
                button.setClickable(true);
                button.setText(R.string.bookNow);
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
        // If no phone number is entered and there are more than one party in this hearing...
        if(phoneNos.size() == 0 && hearing.parties.size() > 1) {
            Toast.makeText(context,R.string.noNumberError,Toast.LENGTH_SHORT).show();
            return false;
        }
        // If no radio button is checked
        else if(checkRadioButtonId == -1) {
            Toast.makeText(context,R.string.noTimeslotSelected,Toast.LENGTH_SHORT).show();
            return false;
        }
        else {
            return true;
        }
    }
}
