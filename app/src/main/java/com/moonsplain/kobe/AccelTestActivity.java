package com.moonsplain.kobe;

import android.content.Context;
import android.hardware.Sensor;

import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.TextView;
import java.lang.System;

//TODO: add function to detect rotational throws
public class AccelTestActivity extends Activity implements SensorEventListener {
    View view;
    private SensorManager senSensorManager;
    private SensorManager proxSensorManager;
    private SensorManager gyroSensorManager;
    private SensorManager gameSensorManager;
    private Sensor senAccelerometer;
    private Sensor senProximity;
    private Sensor senGyro;
    private Sensor senGame;
    boolean up = false;
    boolean faceDown = false;
    long t0, t1, a, best = 0;
    float x, y, z, gX, gY, gZ, rX, rY, rZ;
    float gN, gN0 = 0;
    private static final String TAG = "AccelTestActivity";

    TextView xValue, yValue, zValue, wX, wY, wZ, wN, airtime, best_airtime, prox, prox_last, game;

    //if magnitude of accelerometer vector is close enough to zero
    // (if phone is probably in free-fall)
    public boolean thrown(float x, float y, float z) {
        return ((x * x + y * y + z * z) < 2);
    }

    //true if the normal vector of the rotational forces is significant and unchanging
    public boolean spinThrown(float wN0, float wN1){
        return (  ((wN1-wN0)/wN0 < .1)  &&  (wN1 > 100));
    }
    //if magnitude of accelerometer vector is close enough to 9.8
    //(if phone is probably at rest)
    //TODO: update so this can override hand jitter to filter carries
    public boolean landed(float x, float y, float z){
        return((x*x+y*y+z*z) >(94));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accel_test);

        view = this.getWindow().getDecorView();
        view.setBackgroundResource(R.color.colorAccent);

        xValue = findViewById(R.id.xValue);
        yValue = findViewById(R.id.yValue);
        zValue = findViewById(R.id.zValue);
        wX=  findViewById(R.id.wX);
        wY=  findViewById(R.id.wY);
        wZ=  findViewById(R.id.wZ);
        wN=  findViewById(R.id.wN);
        prox = findViewById(R.id.prox);
        prox_last = findViewById(R.id.prox_last);
        prox_last.setText("most recent landing: none");
        airtime = findViewById(R.id.airtime);
        best_airtime = findViewById(R.id.best_airtime);
        game = findViewById(R.id.game);

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

        gameSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senGame= gameSensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
        gameSensorManager.registerListener(this, senGame, SensorManager.SENSOR_DELAY_FASTEST);

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            x = sensorEvent.values[0];
            y = sensorEvent.values[1];
            z = sensorEvent.values[2];

            xValue.setText("xValue: " + sensorEvent.values[0]);
            yValue.setText("yValue: " + sensorEvent.values[1]);
            zValue.setText("zValue: " + sensorEvent.values[2]);
            //airtime.setText("most recent airtime: " + a +" ms");
            //best_airtime.setText("best airtime in session: " + best +" ms");

            if (thrown(x, y, z) && !up){
                t0 = System.currentTimeMillis();
                up = true;
                view.setBackgroundResource(R.color.colorPrimary);
            }

            if (up){
                if (landed(x,y,z) && !spinThrown(gN0,gN)) {
                    t1 = System.currentTimeMillis();
                    a = t1-t0;
                    if (a > best){
                        best = a;
                        //best_airtime = findViewById(R.id.best_airtime);
                        best_airtime.setText("best airtime in session: " + best +" ms");
                    }
                    if (faceDown)
                        prox_last.setText("last landing: face-down");
                    else
                        prox_last.setText("last landing: face-up");
                    if (a >= 30) {
                        airtime.setText("most recent airtime: " + a + " ms");
                    }
                    view.setBackgroundResource(R.color.colorAccent);
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
                view.setBackgroundResource(R.color.colorPrimary);
                up = true;
            }

            wX.setText("wX: " + sensorEvent.values[0]);
            wY.setText("wY: " + sensorEvent.values[1]);
            wZ.setText("wZ: " + sensorEvent.values[2]);
            wN.setText("wN: " + gN);
        }if (mySensor.getType()== Sensor.TYPE_GAME_ROTATION_VECTOR){
            rX = sensorEvent.values[0];
            rY = sensorEvent.values[1];
            rZ = sensorEvent.values[2];
            game.setText("GAME ROTATION X: "+ rX + "Y:" + rY + "Z: " + rZ);
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

}
