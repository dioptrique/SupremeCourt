package com.example.skynet.supremecourt;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

public class SchedulingActivity extends Activity {
    ArrayList<Party> parties = new ArrayList<>();
    Hearing hearing;
    LayoutInflater inflater;
    TreeMap<String,Boolean> timeSlots;
    List<String> times;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scheduling);

        hearing = MyApplication.data.getHearing(getIntent().getStringExtra("hearingId"));
        this.parties = hearing.parties;
        this.inflater = getLayoutInflater();

        View list = findViewById(R.id.list_of_parties);

        TextView date = (TextView) findViewById(R.id.date);
        date.setText(hearing.justTime + " | " + hearing.justDate + " | "+hearing.venue);
        TextView caseInfo = (TextView) findViewById(R.id.case_info);
        caseInfo.setText(hearing.hearingId + " | " + hearing.caseNo + " | " + hearing.caseName);

        for(Party party : parties) {
            View partyItem = inflater.inflate(R.layout.party_item,null);

            TextView partyType = partyItem.findViewById(R.id.party_label);
            partyType.setText(party.partyType + ":");
            TextView partyName = partyItem.findViewById(R.id.party_name);
            partyName.setText(party.partyName);
            ((ViewGroup) list).addView(partyItem);
        }
        this.timeSlots = this.getAvailableTimeSlots();
        this.times = new ArrayList<>(timeSlots.keySet());

        RadioGroup slotSelector = (RadioGroup) findViewById(R.id.slot_selector);
        // Populate radio group with radio buttons
        for(String time : times) {
            RadioButton rb = new RadioButton(this);
            rb.setLayoutParams(new LinearLayout.LayoutParams(
                    new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT)
            ));
            rb.setEnabled(timeSlots.get(time));
            rb.setText(time);
            slotSelector.addView(rb,-1);
        }


    }

    private TreeMap<String,Boolean> getAvailableTimeSlots() {
        // TODO Get available timeslots from db

        // Currently hardcoded
        String[] timeSlots = {"09:00",
                              "09:30",
                              "10:00",
                              "10:30",
                              "11:00",
                              "11:30",
                              "14:00",
                              "14:30",
                              "15:00",
                              "15:30",
                              "16:00",
                              "16:30",
                              "17:00",
                              "17:30"};
        TreeMap<String,Boolean> map = new TreeMap<>();

        for(String slot : timeSlots) {
            if(slot.equals("09:00") || slot.equals("14:00") || slot.equals("16:30")) {
                map.put(slot,false);
            }
            else map.put(slot,true);
        }
        return map;
    }
}
