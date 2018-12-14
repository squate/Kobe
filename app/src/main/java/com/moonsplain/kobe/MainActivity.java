/*
    Author: Kobe
    MainActivity displays the disclaimer and gives you the option to
    accept or reject it
 */
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
    //Opens the Menu when the "Accept" button is clicked
    public void acceptDisclaimer(View view) {
        Intent intent = new Intent(this, MenuActivity.class);
        startActivity(intent);
    }
    //Opens the disagree message when the "Disagree" button is clicked
    public void openDisagree(View view){
        Intent intent = new Intent(this, DisagreeActivity.class);
        startActivity(intent);
    }
}