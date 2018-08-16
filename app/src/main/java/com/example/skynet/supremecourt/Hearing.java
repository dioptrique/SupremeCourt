package com.example.skynet.supremecourt;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Hearing {
    String hearingId;
    String caseNo;
    String caseName;
    String venue;
    Date date;
    String justDate;
    String justTime;
    ArrayList<Party> parties = new ArrayList<Party>();

    public Hearing(JSONObject hearingJSON) {
        SimpleDateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");

        try {
            this.hearingId = hearingJSON.getString("HearingID");
            this.caseNo = hearingJSON.getString("CaseNo");
            this.caseName = hearingJSON.getString("CaseName");
            try {
                this.date = dateParser.parse(hearingJSON.getString("Date"));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            this.justDate = dateParser.format(date).split(" ")[0];
            this.justTime = dateParser.format(date).split(" ")[1].substring(0,5);
            this.venue = hearingJSON.getString("Venue");

            JSONArray parties = hearingJSON.getJSONArray("Parties");
            for(int i = 0; i < parties.length(); i++) {
                JSONObject party = (JSONObject) parties.getJSONObject(i);
                this.parties.add(new Party(party.getString("PartyName"), party.getString("PartyType")));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

