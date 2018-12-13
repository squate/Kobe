package com.moonsplain.kobe;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

public class ViewPhotoActivity extends AppCompatActivity implements SensorEventListener{
    private SensorManager senSensorManager;
    private SensorManager proxSensorManager;
    private SensorManager gyroSensorManager;
    //private SensorManager gameSensorManager;
    private Sensor senAccelerometer;
    private Sensor senProximity;
    private Sensor senGyro;
    //private Sensor senGame;
    float x, y, z;
    long t0, t1, best, a = 0;
    public static int streak;
    boolean up = false;
    boolean faceDown = false;
    TextView xvalue, bestAirtime, recentAirtime, proxLast;
    public static final String myPref = "Leaderboard preferences";
    public static final String leaderStreak = "Streak";
    public static final String leaderProx = "Proximity";
    public static final String leaderAirtime = "Best Airtime";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_photo);
        String photoPath = getIntent().getData().toString();
        ImageView myImage;
        File imgFile = new  File(photoPath);

        if(imgFile.exists()){

            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

            myImage = findViewById(R.id.imageView);

            myImage.setImageBitmap(myBitmap);

            ThrowMode.targetAnchor.getPose();

        }
        xvalue = findViewById(R.id.textView13);
        bestAirtime = findViewById(R.id.bestAirtimeText);
        recentAirtime = findViewById(R.id.recentAirtimeText);
        proxLast = findViewById(R.id.proxLastText);

        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_DELAY_FASTEST);

        proxSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senProximity = senSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        proxSensorManager.registerListener(this, senProximity , SensorManager.SENSOR_DELAY_FASTEST);

        gyroSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senGyro = gyroSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        gyroSensorManager.registerListener(this, senGyro , SensorManager.SENSOR_DELAY_GAME);

        //gameSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        //senGame= gameSensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
        //gameSensorManager.registerListener(this, senGame, SensorManager.SENSOR_DELAY_FASTEST);

        //Context context = ViewPhotoActivity.this;
        //SharedPreferences sharedPref = getSharedPreferences(myPref, Context.MODE_PRIVATE);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent){
        Sensor mySensor = sensorEvent.sensor;
        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER){
            x = sensorEvent.values[0];
            y = sensorEvent.values[1];
            z = sensorEvent.values[2];
            //xvalue.setText("xValue: " + sensorEvent.values[0]);
            if (thrown(x, y, z) && !up){
                t0 = System.currentTimeMillis();
                up = true;
                //TODO: this isn't incrementing past 1
                //streak++;
                //xvalue.setText("Streak" + streak);

            }
            if (up){
                if (landed(x,y,z) /*&& !spinThrown(gN0,gN)*/) {
                    t1 = System.currentTimeMillis();
                    a = t1-t0;
                    if (a > 100){
                        streak++;
                        xvalue.setText("Streak" + streak);
                    }
                    if (a > best){
                        best = a;
                        bestAirtime.setText("best airtime in session: " + best +" ms");
                    }
                    if (faceDown)
                        proxLast.setText("last landing: face-down");
                    else
                        proxLast.setText("last landing: face-up");
                    if (a >= 30) {
                        recentAirtime.setText("most recent airtime: " + a + " ms");
                    }
                    up = false;
                }

            }


        }
        if (mySensor.getType() == Sensor.TYPE_PROXIMITY && !up){
            if (sensorEvent.values[0] < senProximity.getMaximumRange()) {
                // Detected something nearby
                faceDown = true;
            } else {
                // Nothing is nearby
                faceDown = false;
            }
        }
        //Context context = ViewPhotoActivity.this;
        SharedPreferences pref = getSharedPreferences(myPref, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(leaderStreak, streak);
        editor.putLong(leaderAirtime, best);
        editor.commit();

    }

    //if magnitude of accelerometer vector is close enough to zero
    // (if phone is probably in free-fall)
    public boolean thrown(float x, float y, float z) {
        return ((x * x + y * y + z * z) < 2);
    }
    //if magnitude of accelerometer vector is close enough to 9.8
    //(if phone is probably at rest)
    public boolean landed(float x, float y, float z){
        return((x*x+y*y+z*z) >(94));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy){

    }

    protected void onPause() {
        super.onPause();
        senSensorManager.unregisterListener(this);
        proxSensorManager.unregisterListener(this);
        gyroSensorManager.unregisterListener(this);
    }

    protected void onResume() {
        super.onResume();
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        proxSensorManager.registerListener(this, senProximity, SensorManager.SENSOR_DELAY_FASTEST);
        gyroSensorManager.registerListener(this, senGyro, SensorManager.SENSOR_DELAY_FASTEST);
    }


}
