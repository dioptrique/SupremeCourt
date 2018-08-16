package com.example.skynet.supremecourt;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;

public class LawFirmCasesActivity extends AppCompatActivity {
    ExpandableListView expandableListView;
    HashMap<String,ArrayList<Hearing>> dateToHearings = new HashMap<>();
    String currLawFirm;
    Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_law_firm_cases);
        context = this;

        expandableListView = (ExpandableListView) findViewById(R.id.expandable_list_view);

        // Get the intent that started this activity and get the extras passed
        Intent intent = getIntent();
        this.currLawFirm = intent.getStringExtra("lawFirm");
        RequestParams params = new RequestParams();
        params.add("lawFirm",currLawFirm);
        SupremeCourtRESTClient.post("getLawFirmHearings",params,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("Ananda","getLawFirmHearings SUCCESS!");
                try {
                    // Create a hashmap of date mapped to the Hearings on that date by parsing
                    // the response
                    JSONArray hearingsInWindowJSONArray = response.getJSONArray("hearingsInWindow");
                    for(int i= 0; i < hearingsInWindowJSONArray.length(); i++) {
                        JSONObject hearingsAtDate = hearingsInWindowJSONArray.getJSONObject(i);
                        String date = hearingsAtDate.getString("key");
                        Log.d("Ananda","Date of hearing to be rendered: "+date);
                        dateToHearings.put(date,new ArrayList<Hearing>());
                        JSONArray hearingsJSON = hearingsAtDate.getJSONArray("values");
                        for(int j = 0; j < hearingsJSON.length(); j++) {
                            JSONObject hearingJSON = hearingsJSON.getJSONObject(j);
                            Hearing hearing = new Hearing(hearingJSON);
                            dateToHearings.get(date).add(hearing);
                        }
                        Log.d("Ananda","Created dateToHearing");
                    }
                    ExpandableListViewAdapter adapter = new ExpandableListViewAdapter(context, dateToHearings);
                    expandableListView.setAdapter(adapter);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(context,R.string.failedToGetLawFirmHearings, Toast.LENGTH_SHORT).show();

            }
        });




    }
}
