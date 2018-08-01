package com.example.skynet.supremecourt;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button userButton = (Button) findViewById(R.id.loginUser);
        userButton.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v) {
                loginUser();
            }
        });
    }

    protected void loginUser() {
        Intent intent = new Intent(this, LawFirmsActivity.class);
        startActivity(intent);
    }
}

