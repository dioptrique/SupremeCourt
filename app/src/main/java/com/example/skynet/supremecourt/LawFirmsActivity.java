package com.example.skynet.supremecourt;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import cz.msebera.android.httpclient.Header;

public class LawFirmsActivity extends AppCompatActivity implements SearchView.OnQueryTextListener{
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private LawFirmsAdapter adapter;
    private Set<String> lawFirms = new HashSet<>();
    private LawFirmsActivity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_law_firms);
        activity = this;
        // Configure the recyclerView
        recyclerView = (RecyclerView) this.<View>findViewById(R.id.law_firm_list);

        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // Make asyc call to fetch all lawfirms
        SupremeCourtRESTClient.get("getLawFirms",new RequestParams(),new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                //super.onSuccess(statusCode, headers, response);
                try {
                    JSONArray lawFirmsJSON = response.getJSONArray("lawFirms");
                    for(int i = 0; i < lawFirmsJSON.length(); i++) {
                        lawFirms.add(lawFirmsJSON.getString(i));
                    }
                    adapter = new LawFirmsAdapter(activity, new ArrayList<>(lawFirms));
                    recyclerView.setAdapter(adapter);

                    SearchView searchView =(SearchView) activity.findViewById(R.id.search_bar);
                    searchView.setOnQueryTextListener(activity);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(activity,R.string.failedToGetLawFirms,Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }


    @Override
    public boolean onQueryTextChange(String s) {
        ArrayList<String> matchingLawFirms = new ArrayList<>();
        String userInput = s.toLowerCase();
        for(String firm : lawFirms) {
            if(firm.toLowerCase().contains(userInput)) {
                    matchingLawFirms.add(firm);
            }
        }

        this.adapter.updateData(matchingLawFirms);
        return true;
    }
}
