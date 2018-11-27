package com.moonsplain.kobe;

import android.content.Context;
import android.content.res.AssetManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStream;
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
    int level = 0; int q = 0; int pgs = 0;
    private static final String TAG = "AccelTestActivity";
    public Quest[] page;

    TextView
            yeetView, maxYeetView,
            //xValue, yValue, zValue,
            //wX, wY, wZ,
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

    //here's where we have the conditions for successful quests
    public boolean attemptQuest(int page, Throw t){
        //ALL QUEST CONDITIONS HERE
        page+=1;
        switch (page) {
            //level 1: the contract is sealed
            case 1: return (t.a > 300 && t.a < 400 && t.twirl > 200);

            //level 2 quest: believe in yourself
            case 2: return (t.a >= 600 && !t.fD);

            //careful now!
            case 3: return (t.a >= 1500);

            //go to church, or outside
            case 4: return (t.a >= 2000);

            default:
                view.setBackgroundResource(R.color.colorAccent);
                break;

        }return false;
    }

    //TODO: make work for any button
    public void toggleButton(View view){
        if (q == 0){
            quest_button.setBackgroundResource(R.color.colorPrimary);
            quest_button.setTextColor(getColor(R.color.colorPrimaryDark));
            q = 1;
        }else{
            q = 0;
            quest_button.setBackgroundResource(R.color.colorPrimaryDark);
            quest_button.setTextColor(getColor(R.color.colorPrimary));

        }return;
    }

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
        Log.d("OOF", ""+ pages.length);
        Quest[] quests = new Quest[pages.length];

        String[] cD;
        for (int i = 0; i < pages.length; i++){
            cD = pages[i].split("%");
            quests[i] = new Quest(cD[0], Integer.parseInt(cD[1]), Integer.parseInt(cD[2]),
                    Integer.parseInt(cD[4]),
                    Float.parseFloat(cD[6]), Float.parseFloat(cD[8]),
                    Float.parseFloat(cD[14]),Float.parseFloat(cD[16]),
                    Float.parseFloat(cD[10]),Float.parseFloat(cD[12])
                    );
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
        /*xValue = findViewById(R.id.xValue);
        yValue = findViewById(R.id.yValue);
        zValue = findViewById(R.id.zValue);
        wX=  findViewById(R.id.wX);
        wY=  findViewById(R.id.wY);
        wZ=  findViewById(R.id.wZ); */
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
        //accelerometer sensor airtime
        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_DELAY_FASTEST);

        proxSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senProximity = senSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        proxSensorManager.registerListener(this, senProximity , SensorManager.SENSOR_DELAY_FASTEST);

        gyroSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senGyro = gyroSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        gyroSensorManager.registerListener(this, senGyro , SensorManager.SENSOR_DELAY_GAME);

        //add one for rotation when it's time

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

            //xValue.setText("aX: " + x);
            //yValue.setText("aY: " + y);
            //zValue.setText("aZ: " + z);
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
                        if (page[level].attempt(lastThrow)) {//check if throw meets criteri
                            level = page[level].succPage;
                            view.setBackgroundResource(R.color.green);
                            toggleButton(quest_button);
                        }else {
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

            //wX.setText("wX: " + sensorEvent.values[0]);
            //wY.setText("wY: " + sensorEvent.values[1]);
            //wZ.setText("wZ: " + sensorEvent.values[2]);
            //if (gN > 0.01){ wN.setText("twirl: " + gN);}
            //else{wN.setText("twirl: 0" );}
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


        Quest(String questStr, int succPage, int failPage, int fDReq,
              float aMin, float aMax,
              float tMin, float tMax,
              float yMin, float yMax){

            String s[] = questStr.split("#");
            this.story = s[0];
            this.reqString = s[1];
            this.succPage = succPage;
            this.failPage = failPage;
            this.aMax = aMax;
            this.aMin = aMin;
            this.tMin = tMin;
            this.tMax = tMax;
            this.yMin = yMin;
            this.yMax = yMax;
            this.fDReq = fDReq;
        }
        boolean attempt(Throw t){

            if ((t.maxYeet < this.yMin || t.maxYeet > this.yMax) ||
                (t.a < this.aMin || t.a > this.aMax) ||
                (t.twirl < this.tMin || t.twirl >this.tMax) ||
                (fDReq == 1 && t.fD) || (fDReq == 0 && !t.fD)
            )
            { return false; }

            else return true;
        }
    }

}

