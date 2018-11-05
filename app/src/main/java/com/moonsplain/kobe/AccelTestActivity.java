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
    private Sensor senAccelerometer;
    private Sensor senProximity;
    boolean up = false;
    boolean faceDown = false;
    long t0, t1, a, best = 0;

    private static final String TAG = "AccelTestActivity";

    TextView xValue, yValue, zValue, airtime, best_airtime, prox, prox_last;

    private long lastUpdate = 0;
    private static final int SHAKE_THRESHOLD = 600;


    //if magnitude of accelerometer vector is close enough to zero
    // (if phone is probably in free-fall)
    public boolean thrown(float x, float y, float z) {
        return ((x * x + y * y + z * z) < 2);
    }
    //if magnitude of accelerometer vector is close enough to 9.8
    //(if phone is probably at rest)
    //TODO: update so this can override hand jitter to filter carries
    public boolean landed(float x, float y, float z){
        return((x*x+y*y+z*z) >(9.7*9.7));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accel_test);

        view = this.getWindow().getDecorView();
        view.setBackgroundResource(R.color.colorAccent);

        xValue = (TextView) findViewById(R.id.xValue);
        yValue = (TextView) findViewById(R.id.yValue);
        zValue = (TextView) findViewById(R.id.zValue);
        prox = (TextView) findViewById(R.id.prox);
        prox_last = (TextView) findViewById(R.id.prox_last);
        prox_last.setText("most recent landing: none");
        airtime = (TextView) findViewById(R.id.airtime);
        best_airtime = (TextView) findViewById(R.id.best_airtime);

        //accelerometer sensor airtime
        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_DELAY_FASTEST);


        proxSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senProximity = senSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        proxSensorManager.registerListener(this, senProximity , SensorManager.SENSOR_DELAY_FASTEST);

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;
        boolean ground = false;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];

            xValue.setText("xValue: " + sensorEvent.values[0]);
            yValue.setText("yValue: " + sensorEvent.values[1]);
            zValue.setText("zValue: " + sensorEvent.values[2]);
            airtime.setText("most recent airtime: " + a +" ms");
            best_airtime.setText("best airtime in session: " + best +" ms");

            long curTime = System.currentTimeMillis();

            if (thrown(x, y, z)){
                up = true;
                ground = false;
                t0 = System.currentTimeMillis();
                view.setBackgroundResource(R.color.colorPrimary);
            }

            if (up){
                if (landed(x,y,z)) {
                    t1 = System.currentTimeMillis();
                    a = t1-t0;
                    if (a > best){
                        best = a;
                        best_airtime = (TextView) findViewById(R.id.best_airtime);
                    }
                    if (faceDown)
                        prox_last.setText("last landing: face-down");
                    else
                        prox_last.setText("last landing: face-up");
                    airtime.setText("most recent airtime: " + a + " ms");
                    view.setBackgroundResource(R.color.colorAccent);
                    ground = true;
                    up = false;
                }

            }
        }if (mySensor.getType() == Sensor.TYPE_PROXIMITY){
            if (sensorEvent.values[0] < senProximity.getMaximumRange()) {
                // Detected something nearby
                prox.setText("face-down");
                faceDown = true;
            } else {
                // Nothing is nearby
                // prox.setText("face-up");
                faceDown = false;
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
    }

    protected void onResume() {
        super.onResume();
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        proxSensorManager.registerListener(this, senProximity, SensorManager.SENSOR_DELAY_FASTEST);
    }

}
