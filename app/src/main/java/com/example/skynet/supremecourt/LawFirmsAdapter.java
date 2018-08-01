package com.example.skynet.supremecourt;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.JsonReader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.util.ArrayList;
import java.util.Comparator;

/*
 ViewHolders will be created sparingly: only enough to populate screen.
 Once scrolling happens ViewHolders which are out of bounds will be recycled and bound
 with new data. The use of ViewHolders ensures that the adapter does not have to
 inflate large numbers of views at a time when only a portion of it is actually visible.
*/
public class LawFirmsAdapter extends RecyclerView.Adapter<LawFirmsAdapter.MyViewHolder>{
    // To inflate the list item
    private final LayoutInflater inflater;
    ArrayList<String> data;
    public LawFirmsAdapter(Context context, ArrayList<String> data) {
        this.data = data;
        this.inflater = LayoutInflater.from(context);
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        Button companyName;
        int position;
        public MyViewHolder(View itemView) {
            super(itemView);
            companyName =(Button) itemView.findViewById(R.id.law_firm_name);
        }
    }

    @NonNull
    @Override
    public LawFirmsAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the required view to display a single row of data
        View view = inflater.inflate(R.layout.law_firm_list_item, parent, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        // Will be responsible for attaching a single data at a particular positiion
        // to an existing ViewHolder
            final String lawFirm = this.data.get(position);
            // Set text of the Button in the ViewHolder object to name of LawFirm at position
            holder.companyName.setText(lawFirm);
            holder.companyName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    goToCases(view.getContext(), lawFirm);
                }
            });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void updateData(ArrayList<String> newData) {
        this.data = newData;
        notifyDataSetChanged();
    }

    public void goToCases(Context context, String lawFirm) {
        Intent intent = new Intent(context,LawFirmCasesActivity.class);
        // The position in the data will be used by the next activity to render
        // the data for the law firm
        intent.putExtra("lawFirm", lawFirm);
        context.startActivity(intent);
    }
}
