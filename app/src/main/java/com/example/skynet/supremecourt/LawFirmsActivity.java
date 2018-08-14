package com.example.skynet.supremecourt;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.SearchView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Set;

public class LawFirmsActivity extends AppCompatActivity implements SearchView.OnQueryTextListener{
    private AppData data;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private LawFirmsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // JSON data fetched from assets
        data = MyApplication.data;
        setContentView(R.layout.activity_law_firms);

        // Configure the recyclerView
        recyclerView = (RecyclerView) this.<View>findViewById(R.id.law_firm_list);

        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new LawFirmsAdapter(this, new ArrayList<String>(data.lawFirms));
        recyclerView.setAdapter(this.adapter);

        SearchView searchView =(SearchView) this.findViewById(R.id.search_bar);
        searchView.setOnQueryTextListener(this);
    }


    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }


    @Override
    public boolean onQueryTextChange(String s) {
        ArrayList<String> matchingLawFirms = new ArrayList<>();
        String userInput = s.toLowerCase();
        Set<String> lawFirms = data.lawFirms;
        for(String firm : lawFirms) {
            if(firm.toLowerCase().contains(userInput)) {
                    matchingLawFirms.add(firm);
            }
        }

        this.adapter.updateData(matchingLawFirms);
        return true;
    }
}
