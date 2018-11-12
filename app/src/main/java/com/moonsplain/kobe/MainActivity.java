package com.moonsplain.kobe;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;


public class MainActivity extends AppCompatActivity {
    private static int SPLASH_TIME_OUT = 3;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new Handler().postDelayed(new Runnable(){
            @Override
                public void run() {
                Intent homeIntent = new Intent(MainActivity.this, SplashActivity.class);
                startActivity(homeIntent);
                finish();
                }

        },SPLASH_TIME_OUT);

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