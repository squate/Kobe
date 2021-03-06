package com.moonsplain.kobe;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.System;

//TODO: add function to detect rotational throws
public class KobeQuest extends Activity implements SensorEventListener {
    View view;
    private SensorManager senSensorManager;
    private SensorManager proxSensorManager;
    private SensorManager gyroSensorManager;
    private Sensor senAccelerometer;
    private Sensor senProximity;
    private Sensor senGyro;
    boolean up = false;
    boolean faceDown = false;
    long t0, t1, a, best = 0;
    float x, y, z, gX, gY, gZ, yeet, maxYeet, lastThrowMaxYeet;
    Throw lastThrow;
    float gN, gN0, twirl= 0;
    int level = 0; int q = 0; int pack = 1;
    public Quest[] page;
    MediaPlayer loseSound;
    MediaPlayer winSound;
    ImageView img;
    TextView
            //yeetView, levelView,prox,
            maxYeetView,
            wN, airtime, best_airtime,  prox_last,story;

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

    //TODO: if we have time, implement multiple option system
    public void toggleButton(View view){
        if (q == 0){
            quest_button.setBackgroundColor(0x0ffffffff);
            quest_button.setTextColor(getColor(R.color.colorPrimaryDark));
            q = 1;
        }else{
            quest_button.setBackgroundResource(R.color.colorPrimaryDark);
            quest_button.setTextColor(0xffffffff);
            q = 0;
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
        //establish text
        setContentView(R.layout.activity_kobequest);
        view = this.getWindow().getDecorView();
        view.setBackgroundResource(R.color.colorPrimary);
        story = findViewById(R.id.story);
        prox_last = findViewById(R.id.prox_last);
        airtime = findViewById(R.id.airtime);
        maxYeetView = findViewById(R.id.maxYeet);
        wN=  findViewById(R.id.wN);
        best_airtime = findViewById(R.id.best_airtime);
        quest_button = findViewById(R.id.quest1);
        img = findViewById(R.id.img);

        //module-dependent stuff
        page = loadQuests("pack"+pack+"/quests.txt");
        //set initial image
        setImage(img, pack, level);
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

        //when the accelerometer feed is altered
        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            x = sensorEvent.values[0];
            y = sensorEvent.values[1];
            z = sensorEvent.values[2];
            yeet = x*x+y*y+z*z;

            //capture max yeet
            if (yeet > maxYeet){
                maxYeet = yeet;
                //maxYeetView.setText("max yeet: " + maxYeet);
            }

            //yeetView.setText("yeet: " + (int) yeet);

            //if the phone is in free-fall for the first time in a throw
            if (thrown(yeet) && !up){
                t0 = System.currentTimeMillis();
                up = true;
                if (!spinThrown(gN, gN0)){twirl = 0;}
                wN.setText("twirl:\n" + (int)twirl);
                view.setBackgroundResource(R.color.colorAccent);
            }

            //when the phone lands
            if (up && landed(yeet) && !spinThrown(gN0,gN)){ //phone is in the air
                t1 = System.currentTimeMillis();
                a = t1-t0; //capture airtime as a
                lastThrowMaxYeet = maxYeet;
                maxYeetView.setText("yeet:\n" + (int)lastThrowMaxYeet);
                maxYeet = 0;

                //save throw as Throw
                lastThrow = new Throw(a, faceDown, lastThrowMaxYeet, twirl, gZ);

                //check for best airtime
                if (a > best){
                    best = a;
                    best_airtime.setText("best airtime in session: " + best +" ms");
                }

                //set textViews to update to most recent throw
                if (faceDown){
                    prox_last.setText("tails");
                } else {
                    prox_last.setText("heads");
                } if (a >= 70) {
                    airtime.setText("airtime:\n" + a + " ms"); }

                //if you're attempting a quest and the phone was properly thrown
                if (q > 0 && a > 55) {

                    if (page[level].attempt(lastThrow)) { //if throw meets criteria of quest
                        winSound.start();
                        level = page[level].succPage;
                        view.setBackgroundResource(R.color.green);
                        toggleButton(quest_button);
                    }else { //if your throw doesn't measure up
                        loseSound.start();
                        level = page[level].failPage;
                        view.setBackgroundResource(R.color.red);
                        toggleButton(quest_button);
                    }setImage(img, pack, level);

                    story.setText(page[level].story);
                    quest_button.setText(page[level].reqString);

                //return to default backdrop
                }else{
                    view.setBackgroundResource(R.color.colorPrimary);
                }
                up = false;
            }
        }

        //proximity sensor capture
        if (mySensor.getType() == Sensor.TYPE_PROXIMITY && !up){
            if (sensorEvent.values[0] < senProximity.getMaximumRange()) {
                // Detected something /
                faceDown = true;
            } else {
                // Nothing is nearby
                faceDown = false;
            }
        }

        //gyroscope capture
        if (mySensor.getType() == Sensor.TYPE_GYROSCOPE){
            gX = sensorEvent.values[0];
            gY = sensorEvent.values[1];
            gZ = sensorEvent.values[2];

            //store previous gyrsocope reading
            gN0 = gN;
            //take the magnitude of the gyroscope's components
            gN = gX*gX + gY*gY + gZ*gZ;

            //spinning throw detection
            if (spinThrown(gN0, gN) && !up){
                t0 = System.currentTimeMillis();
                twirl = gN;
                wN.setText("twirl:\n" + (int)twirl);
                view.setBackgroundResource(R.color.colorAccent);
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
    private void setImage(ImageView img, int pack, int level){
        Context context = img.getContext();
        int id = context.getResources().getIdentifier("a_"+level, "drawable", context.getPackageName());
        img.setImageResource(id);
        return;
    }
    //A set of metrics gathered from the sensors while ap hone is thrown
    public class Throw {
        long a;
        boolean fD;
        float maxYeet; //normal of the accelerometer vector
        float twirl; //normal of the gyroscope vector

        Throw(long a, boolean fD, float maxYeet, float twirl, float frisbee) {
            this.a = a;
            this.fD = fD;
            this.maxYeet = maxYeet;
            this.twirl = twirl;
        }
    }

    //a set of text and parameteres to which throws are compared
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
        boolean attempt(Throw t){ //compare throw and quest data
            return (!(t.maxYeet < this.yMin) && !(t.maxYeet > this.yMax)) && //yeet in range
                    (!(t.a < this.aMin) && !(t.a > this.aMax)) && //airtime in range
                    (!(t.twirl < this.tMin) && !(t.twirl > this.tMax)) && //twirl in range
                    (fDReq != 1 || !t.fD) && (fDReq != 0 || t.fD); //faceup correct or a non-issue
        }
    }
}
