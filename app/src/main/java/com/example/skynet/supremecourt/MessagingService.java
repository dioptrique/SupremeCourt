package com.example.skynet.supremecourt;

import android.content.Intent;
import android.util.Log;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.loopj.android.http.*;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class MessagingService extends FirebaseMessagingService {
    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.d("Ananda","OnNewToken: "+s);

        getSharedPreferences("DATA",MODE_PRIVATE).edit().putString("registrationId",s).commit();
        String registrationId = s;
        String phoneNo = getSharedPreferences("DATA",MODE_PRIVATE).getString("phoneNo","null");
        // Check if user has already given his phoneNo
        if(!phoneNo.equals("null")) {
            RequestParams params = new RequestParams();
            params.add("phoneNo",phoneNo);
            params.add("registrationId",registrationId);
            SupremeCourtRESTClient.post("addNewRegistrationId",params,new JsonHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    super.onSuccess(statusCode, headers, response);
                    Log.d("Ananda","addNewRegistrationId success");
                }
            });
        }
        // Wait for user to add phone number and then make api call if phone number is not currently stored
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Get the click_action passed via notification and find an activity whose intent filter
        // specifies the same action (In our case it is the scheduling_activity)
        String click_action = remoteMessage.getNotification().getClickAction();
        String hearingId = remoteMessage.getData().get("hearingId");
        Log.d("Ananda","remoteMessage received! Click action: "+click_action+", hearingId: "+hearingId);
        Intent intent = new Intent(click_action);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("hearingId",hearingId);
        Log.d("Ananda","Intent created");
        startActivity(intent);
    }
}
