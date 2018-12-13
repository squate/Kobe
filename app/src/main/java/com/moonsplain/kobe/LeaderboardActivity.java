/*
    Author: Kobe
    LeaderboardActivity creates a leaderboard that displays best streak and airtime using
    Shared Preferences
 */

package com.moonsplain.kobe;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import static com.moonsplain.kobe.ViewPhotoActivity.myPref;

public class LeaderboardActivity extends AppCompatActivity {
    //Create textview
    TextView strk, air;
    public int max = -999999999;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        //Access shared preferences
        SharedPreferences sharedPreferences = getSharedPreferences(myPref, 0);
        //Create variables from shared preference data
        int leaderStreak = sharedPreferences.getInt(ThrowMode.leaderStreak, 0);
        long leaderAirtime = sharedPreferences.getLong(ThrowMode.leaderAirtime, 0);

        //Find textviews from XML file
        strk = findViewById(R.id.streakView);
        air = findViewById(R.id.airtimeView);
        //if the most recent streak/airtime is better than current
        //update the textview on the leaderboard
        if (leaderStreak > max) {
            strk.setText("Streak: " + leaderStreak);
        }
        if (leaderAirtime > max){
            air.setText("Airtime: " + leaderAirtime);
        }

    }


}
