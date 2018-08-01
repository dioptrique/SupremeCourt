package com.example.skynet.supremecourt;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ExpandableListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class LawFirmCasesActivity extends AppCompatActivity {
    ExpandableListView expandableListView;
    HashMap<String,ArrayList<String>> lawFirmHearings = null;
    String currLawFirm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_law_firm_cases);

        // Get the intent that started this activity and get the extras passed
        Intent intent = getIntent();
        this.currLawFirm = intent.getStringExtra("lawFirm");

        lawFirmHearings = MyApplication.data.getHearingsInWindow(currLawFirm);

        expandableListView = (ExpandableListView) findViewById(R.id.expandable_list_view);

        ExpandableListViewAdapter adapter = new ExpandableListViewAdapter(this, lawFirmHearings);
        expandableListView.setAdapter(adapter);
    }
}
