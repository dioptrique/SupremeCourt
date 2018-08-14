package com.example.skynet.supremecourt;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.*;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class PhoneNumberActivity extends AppCompatActivity {
    Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context = this;

        boolean isFirstRun = getSharedPreferences("PREFERENCE",MODE_PRIVATE).getBoolean("isFirstRun",true);
        if(!isFirstRun) {
            goToLawFirms();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_numer);
        final Button button = (Button) findViewById(R.id.submit_number);
        final TextView textView = (TextView) findViewById(R.id.number_field);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                button.setText(R.string.loading);
                button.setClickable(false);

                String phoneNo = textView.getText().toString();
                getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit()
                        .putBoolean("isFirstRun",false).commit();
                getSharedPreferences("DATA",MODE_PRIVATE).edit().putString("phoneNo",phoneNo).commit();

                // Check if registrationId is currently stored
                String registrationId = getSharedPreferences("DATA",MODE_PRIVATE).getString("registrationId","null");
                if(!registrationId.equals("null")) {
                    //TODO Make api call to add phoneNo to regId mapping to db
                    RequestParams params = new RequestParams();
                    params.add("phoneNo",phoneNo);
                    params.add("registrationId",registrationId);
                    SupremeCourtRESTClient.post("addNewRegistrationId",params,new JsonHttpResponseHandler(){
                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            button.setText(R.string.submit);
                            button.setClickable(true);

                            Toast.makeText(context,R.string.phoneNoFailure,Toast.LENGTH_SHORT).show();
                            super.onFailure(statusCode, headers, throwable, errorResponse);
                        }

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            super.onSuccess(statusCode, headers, response);
                            Toast.makeText(context,R.string.phoneNoSuccess,Toast.LENGTH_SHORT).show();
                            Log.d("Ananda","Api request success");
                            goToLawFirms();
                            finish();
                        }
                    });
                }

            }
        });
    }

    protected void goToLawFirms() {
        Intent intent = new Intent(this, LawFirmsActivity.class);
        startActivity(intent);
        finish();
    }
}
