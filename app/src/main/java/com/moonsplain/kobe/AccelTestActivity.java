package com.moonsplain.kobe;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;// keep, just in case
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.System;


//TODO: add function to detect rotational throws
public class AccelTestActivity extends Activity implements SensorEventListener {
    View view;
    private SensorManager senSensorManager;
    private SensorManager proxSensorManager;
    private SensorManager gyroSensorManager;
    //private SensorManager rotSensorManager;
    private Sensor senAccelerometer;
    private Sensor senProximity;
    private Sensor senGyro;
    //private Sensor senRot;
    boolean up = false;
    boolean faceDown = false;
    long t0, t1, a, best = 0;
    float x, y, z, gX, gY, gZ, yeet, maxYeet, lastThrowMaxYeet;
    Throw lastThrow;
    float gN, gN0, twirl= 0;
    int level = 0; int q = 0;
    public Quest[] page;
    MediaPlayer loseSound;
    MediaPlayer winSound;
    TextView
            yeetView, maxYeetView,
            wN, airtime, best_airtime, prox, prox_last, levelView, story;

    Button quest_button;



    //FUNCTIONS
    //if magnitude of accelerometer vector is close enough to zero
    // (if phone is probably in free-fall)
    public boolean thrown(float yeet) {
        return (yeet < 2);
    }

    //true if the normal vector of the rotational forces is significant and unchanging
    public boolean spinThrown(float wN0, float wN1){
        return (  ((wN1-wN0)/wN0 < .1)  &&  (wN1 > 100));
    }
    //if magnitude of accelerometer vector is close enough to 9.8
    //(if phone is probably at rest)
    //TODO: update so this can override hand jitter to filter carries
    public boolean landed(float yeet){
        return(yeet > 94);
    }

    //TODO: if we have time, implement muliple option system
    public void toggleButton(View view){
        if (q == 0){
            quest_button.setBackgroundResource(R.color.colorPrimary);
            quest_button.setTextColor(getColor(R.color.colorPrimaryDark));
            q = 1;
        }else{
            q = 0;
            quest_button.setBackgroundResource(R.color.colorPrimaryDark);
            quest_button.setTextColor(getColor(R.color.colorPrimary));

        }
    }

    //load quests into page[] array
    public Quest[] loadQuests(String fileName){
        String questsRaw = "";
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(getAssets().open(fileName), "UTF-8"));
            // do reading, usually loop until end of file reading
            String mLine;
            while ((mLine = reader.readLine()) != null) {
                questsRaw += mLine;
            }
        } catch (IOException e) {
            //log the exception
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    //log the exception
                }
            }
        }

        String[] pages = questsRaw.split("~");
        Quest[] quests = new Quest[pages.length];

        String[] cD;
        for (int i = 0; i < pages.length; i++){
            cD = pages[i].split("%");
            quests[i] = new Quest(cD);
        }
        return quests;
    }

    //pretty much main below here
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accel_test);
        view = this.getWindow().getDecorView();
        view.setBackgroundResource(R.color.colorAccent);

        yeetView = findViewById(R.id.yeet);
        maxYeetView = findViewById(R.id.maxYeet);
        wN=  findViewById(R.id.wN);
        prox = findViewById(R.id.prox);
        story = findViewById(R.id.story);

        prox_last = findViewById(R.id.prox_last);
        prox_last.setText("most recent landing: none");

        airtime = findViewById(R.id.airtime);
        best_airtime = findViewById(R.id.best_airtime);
        levelView = findViewById(R.id.level);
        levelView.setText("level: " + level);

        quest_button = findViewById(R.id.quest1);
        page = loadQuests("demo.txt");
        story.setText(page[level].story);
        quest_button.setText(page[level].reqString);
        loseSound = MediaPlayer.create(this, R.raw.bad);
        winSound = MediaPlayer.create(this, R.raw.good);

        //set up sensors
        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_DELAY_FASTEST);

        proxSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senProximity = senSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        proxSensorManager.registerListener(this, senProximity , SensorManager.SENSOR_DELAY_FASTEST);

        gyroSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senGyro = gyroSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        gyroSensorManager.registerListener(this, senGyro , SensorManager.SENSOR_DELAY_GAME);

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            x = sensorEvent.values[0];
            y = sensorEvent.values[1];
            z = sensorEvent.values[2];
            yeet = x*x+y*y+z*z;

            if (yeet > maxYeet){
                maxYeet = yeet;
                maxYeetView.setText("max yeet: " + maxYeet);}

            yeetView.setText("yeet: " + (int) yeet);


            if (thrown(yeet) && !up){
                t0 = System.currentTimeMillis();
                up = true;
                if (!spinThrown(gN, gN0)){twirl = 0;}
                wN.setText("twirl: " + twirl);
                view.setBackgroundResource(R.color.colorPrimary);
            }

            if (up){ //phone is in the air
                if (landed(yeet) && !spinThrown(gN0,gN)) {
                    t1 = System.currentTimeMillis();
                    a = t1-t0;
                    lastThrowMaxYeet = maxYeet;
                    maxYeet = 0;

                    lastThrow = new Throw(a, faceDown, lastThrowMaxYeet, twirl);
                    if (a > best){
                        best = a;
                        best_airtime.setText("best airtime in session: " + best +" ms");
                    }
                    if (faceDown)
                        prox_last.setText("last landing: face-down");
                    else
                        prox_last.setText("last landing: face-up");
                    if (a >= 70) {
                        airtime.setText("most recent airtime: " + a + " ms");
                    }
                    if (q > 0 && a > 55) {
                        if (page[level].attempt(lastThrow)) {//check if throw meets criteria
                            winSound.start();
                            level = page[level].succPage;
                            view.setBackgroundResource(R.color.green);
                            toggleButton(quest_button);
                        }else {
                            loseSound.start();
                            level = page[level].failPage;
                            view.setBackgroundResource(R.color.red);
                            toggleButton(quest_button);
                        }
                        levelView.setText("you are on page: " + level);
                        story.setText(page[level].story);
                        quest_button.setText(page[level].reqString);
                    }else{
                        view.setBackgroundResource(R.color.colorAccent);
                    }
                    up = false;
                }

            }
        }if (mySensor.getType() == Sensor.TYPE_PROXIMITY && !up){
            if (sensorEvent.values[0] < senProximity.getMaximumRange()) {
                // Detected something nearby
                prox.setText("face-down");
                faceDown = true;
            } else {
                // Nothing is nearby
                prox.setText("face-up");
                faceDown = false;
            }
        }if (mySensor.getType() == Sensor.TYPE_GYROSCOPE){
            gX = sensorEvent.values[0];
            gY = sensorEvent.values[1];
            gZ = sensorEvent.values[2];
            gN0 = gN;
            gN = gX*gX + gY*gY + gZ*gZ;

            //spinning throw detection
            if (spinThrown(gN0, gN) && !up){
                t0 = System.currentTimeMillis();
                twirl = gN;
                wN.setText("last twirl: " + twirl);
                view.setBackgroundResource(R.color.colorPrimary);
                up = true;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    protected void onPause() {
        super.onPause();
        senSensorManager.unregisterListener(this);
        proxSensorManager.unregisterListener(this);
        gyroSensorManager.unregisterListener(this);
    }

    protected void onResume() {
        super.onResume();
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        proxSensorManager.registerListener(this, senProximity, SensorManager.SENSOR_DELAY_FASTEST);
        gyroSensorManager.registerListener(this, senGyro, SensorManager.SENSOR_DELAY_FASTEST);
    }


    public class Throw {
        long a;
        boolean fD;
        float maxYeet; //normal of the accelerometer vector
        float twirl; //normal of the gyroscope vector

        Throw(long a, boolean fD, float maxYeet, float twirl) {
            this.a = a;
            this.fD = fD;
            this.maxYeet = maxYeet;
            this.twirl = twirl;
        }
    }

    public class Quest{
        String story, reqString;
        int succPage, failPage, fDReq;
        float aMax, aMin, tMax, tMin, yMin, yMax;

        Quest(String[] s){
            this.story = s[0];
            this.reqString = s[1];
            this.succPage = Integer.parseInt(s[2]);
            this.failPage = Integer.parseInt(s[3]);
            this.fDReq = Integer.parseInt(s[5]);
            this.aMin = Float.parseFloat(s[7]);
            this.aMax = Float.parseFloat(s[9]);
            this.tMin = Float.parseFloat(s[15]);
            this.tMax = Float.parseFloat(s[17]);
            this.yMin = Float.parseFloat(s[11]);
            this.yMax = Float.parseFloat(s[13]);
        }
        boolean attempt(Throw t){
            return (!(t.maxYeet < this.yMin) && !(t.maxYeet > this.yMax)) && //yeet in range
                    (!(t.a < this.aMin) && !(t.a > this.aMax)) && //airtime in range
                    (!(t.twirl < this.tMin) && !(t.twirl > this.tMax)) && //twirl in range
                    (fDReq != 1 || !t.fD) && (fDReq != 0 || t.fD); //faceup correct or a non-issue
        }
    }

}

