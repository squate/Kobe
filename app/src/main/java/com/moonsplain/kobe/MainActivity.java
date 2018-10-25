package com.moonsplain.kobe;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }
   /*Called when the user taps the "Accept" button"*/
    public void acceptDisclaimer(View view) {
        Intent intent = new Intent(this, MenuActivity.class);
        startActivity(intent);
    }

    public void openDisagree(View view){
        Intent intent = new Intent(this, DisagreeActivity.class);
        startActivity(intent);
    }
}