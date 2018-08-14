package com.example.skynet.supremecourt;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.firebase.iid.FirebaseInstanceId;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("Ananda", ""+FirebaseInstanceId.getInstance().getToken());
        super.onCreate(savedInstanceState);
        final Boolean isFirstRun = getSharedPreferences("PREFERENCE",MODE_PRIVATE)
                .getBoolean("isFirstRun", true);
        setContentView(R.layout.activity_main);

        Button userButton = (Button) findViewById(R.id.loginUser);
        userButton.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v) {
                if(isFirstRun) {
                    enterPhoneNo();
                }
                else {
                    loginUser();
                }
            }
        });
    }

    protected void loginUser() {
        Intent intent = new Intent(this, LawFirmsActivity.class);
        startActivity(intent);
    }

    protected void enterPhoneNo() {
        Intent intent = new Intent(this,PhoneNumberActivity.class);
        startActivity(intent);
    }
}

