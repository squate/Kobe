package com.moonsplain.kobe;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
    }

    public void openARView(View view){
        Intent intent = new Intent(this, ThrowMode.class);
        startActivity(intent);
    }
    public void openLeaderboard(View view){
        Intent intent = new Intent(this, LeaderboardActivity.class);
        startActivity(intent);
    }

    public void openAccel(View view){
        Intent intent = new Intent(this, KobeQuest.class);
        startActivity(intent);
    }

    public void openHelp(View view){
        Intent intent = new Intent(this, HelpActivity.class);
        startActivity(intent);
    }


}

