package com.example.skynet.supremecourt;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class ExpandableListViewAdapter extends BaseExpandableListAdapter {
    Context context;
    HashMap<String,ArrayList<Hearing>> dateToHearings;
    ArrayList<String> dates;
    LayoutInflater inflater;

    public ExpandableListViewAdapter(Context context, HashMap<String,ArrayList<Hearing>> dateToHearings) {
        this.context = context;
        this.dateToHearings = dateToHearings;
        this.dates = new ArrayList<>(this.dateToHearings.keySet());
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getGroupCount() {
        // Number of dates
        return dates.size();
    }

    @Override
    public int getChildrenCount(int i) {
        // Number of hearings in each date
        return dateToHearings.get(dates.get(i)).size();
    }

    @Override
    public Object getGroup(int i) {
        return dates.get(i);
    }

    @Override
    public Object getChild(int i, int i1) {
        return dateToHearings.get(dates.get(i)).get(i1);
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int i1) {
        return i1;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int i, boolean b, View view, ViewGroup parent) {
        View mview = inflater.inflate(R.layout.date_item, parent, false);
        TextView textView = (TextView) mview.findViewById(R.id.date);
        textView.setText(dates.get(i));
        return mview;
    }

    @Override
    public View getChildView(int i, int i1, boolean b, View view, ViewGroup parent) {
        View mview = inflater.inflate(R.layout.case_item, parent, false);
        TextView textView = (TextView) mview.findViewById(R.id.case_name);
        final Hearing currHearing = dateToHearings.get(dates.get(i)).get(i1);
        textView.setText(currHearing.justTime+" | " +
                         currHearing.venue+" | "+
                         currHearing.caseNo+" | "+
                         currHearing.caseName);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToScheduling(context,currHearing.hearingId);
            }
        });
        return mview;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return false;
    }

    private void goToScheduling(Context context, String hearingId) {
        Intent intent = new Intent(context,ScheduleLoadingActivity.class);
        intent.putExtra("hearingId",hearingId);
        context.startActivity(intent);
    }
}
