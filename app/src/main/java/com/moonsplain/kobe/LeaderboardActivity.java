package com.moonsplain.kobe;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import static com.moonsplain.kobe.ViewPhotoActivity.leaderAirtime;
import static com.moonsplain.kobe.ViewPhotoActivity.leaderStreak;
import static com.moonsplain.kobe.ViewPhotoActivity.myPref;

public class LeaderboardActivity extends AppCompatActivity {
    TextView strk, air;
    public int max = -999999999;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        SharedPreferences sharedPreferences = getSharedPreferences(myPref, 0);
        //null? instead of not found
        int leaderStreak = sharedPreferences.getInt(ViewPhotoActivity.leaderStreak, 0);
        long leaderAirtime = sharedPreferences.getLong(ViewPhotoActivity.leaderAirtime, 0);

        strk = findViewById(R.id.streakView);
        air = findViewById(R.id.airtimeView);
        if (leaderStreak > max) {
            strk.setText("Streak: " + leaderStreak);
        }
        if (leaderAirtime > max){
            air.setText("Airtime: " + leaderAirtime);
        }

    }


}
