package com.example.skynet.supremecourt;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Calendar;
import java.util.TreeMap;

public final class AppData {
    private Date today;
    private final int window = 2;
    Application appContext;
    private String realDataNested;
    private String realData;
    private JSONArray realData_JSON;
    private JSONArray realDataNested_JSON;
    private HashMap<String, Hearing> hearingIdToHearing;
    private HashMap<String,HashSet<String>> lawFirmToHearingIds;
    public Set<String> lawFirms;
    private HashMap<String,TreeMap<String,HashSet<String>>> venueToDatesToHearingIds;

    public AppData(Application appContext) {
        try {
            this.today = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS")).parse("2018-07-18 00:00:00:000");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        this.appContext = appContext;
        this.realData = this.readFromAssets("realData.json",appContext);
        this.realDataNested = this.readFromAssets("realDataNested.json",appContext);
        this.realData_JSON = this.getRealDataJSON(this.realData);
        this.realDataNested_JSON = this.getRealDataNestedJSON(this.realDataNested);
        this.hearingIdToHearing = mapHearingIdToHearing(this.realData_JSON);
        this.lawFirmToHearingIds = mapLawFirmToHearingIds(realData_JSON);
        this.lawFirms = lawFirmToHearingIds.keySet();
    }

    public Hearing getHearing(String id) {
        return hearingIdToHearing.get(id);
    }

    public HashMap<String, ArrayList<String>> getHearingsInWindow(String lawFirm) {
        Calendar c = null;
        c = Calendar.getInstance();
        c.setTime(today);
        c.add(Calendar.DATE,window);

        Set<String> hearingIds = lawFirmToHearingIds.get(lawFirm);
        HashMap<String,ArrayList<String>> dateToHearingIdsInWindow = new HashMap<>();
        for(String hearingId : hearingIds) {
            Date hearingDate = hearingIdToHearing.get(hearingId).date;
            // Get just the date portion in Date object
            String justDate = hearingIdToHearing.get(hearingId).justDate;
            // If within the specified window
            if(hearingDate.after(today) && hearingDate.before(c.getTime())) {
                // If date is already a key in the hashmap
                if(dateToHearingIdsInWindow.containsKey(justDate)) {
                    dateToHearingIdsInWindow.get(justDate).add(hearingId);
                }
                else {
                    ArrayList<String> list = new ArrayList<>();
                    list.add(hearingId);
                    dateToHearingIdsInWindow.put(justDate,list);
                }
            }
        }

        return dateToHearingIdsInWindow;
    }

    private HashMap<String,HashSet<String>> mapLawFirmToHearingIds(JSONArray data) {
        HashMap<String,HashSet<String>> map = new HashMap<>();
        for(int i = 0; i < data.length(); i++) {
            try {
                JSONObject hearing = (JSONObject) data.get(i);
                final String hearingId = hearing.getString("HearingID");
                JSONArray parties = (JSONArray)((JSONObject) hearing.getJSONObject("PartiesList"))
                                                                     .getJSONArray("Party");
                if(parties.length() != 0) {
                    for(int j = 0; j < parties.length(); j++) {
                        JSONObject party = (JSONObject) parties.getJSONObject(j);
                        if(party.has("Solicitors")) {
                            JSONArray solicitors = (JSONArray) ((JSONObject) party.getJSONObject("Solicitors"))
                                    .getJSONArray("Solicitor");
                            if(solicitors.length() != 0) {
                                for(int k = 0; k < solicitors.length(); k++) {
                                    JSONObject solicitor = (JSONObject) solicitors.getJSONObject(k);
                                    String lawFirmName = solicitor.getString("LFName");
                                    // Check for Solicitor element with invalid details
                                    if(!lawFirmName.equals("")) {
                                        if(map.containsKey(lawFirmName)){
                                            ((HashSet) map.get(lawFirmName)).add(hearingId);
                                        } else {
                                            HashSet<String> set = new HashSet<>();
                                            set.add(hearingId);
                                            map.put(lawFirmName, set);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }
        return map;
    }

    private HashMap<String,Hearing> mapHearingIdToHearing(JSONArray data) {
        HashMap<String,Hearing> map = new HashMap<>();
        for(int i = 0; i < data.length(); i++) {
            try {
                String hearingId = ((JSONObject) data.get(i))
                                    .getString("HearingID");
                JSONObject hearing = (JSONObject) data.get(i);
                map.put(hearingId, new Hearing(hearing));
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }
        return map;
    }

    private JSONArray getRealDataNestedJSON(String realDataNested) {
        JSONArray jsonArr = null;
        try {
            jsonArr = new JSONArray(realDataNested);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return jsonArr;
    }

    private JSONArray getRealDataJSON(String realData) {
        JSONArray jsonArr = null;
        try {
            jsonArr = new JSONArray(realData);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return jsonArr;
    }

    // Read json data form assets
    public String readFromAssets(String filename, Context context) {
        try {
            // Read realDataNested
            InputStream is = context.getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            return new String(buffer, "UTF-8");
        }
        catch(Exception e){
            e.printStackTrace();
        }

        return null;
    }
}
