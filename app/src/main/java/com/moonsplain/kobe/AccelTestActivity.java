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

public class AccelTestActivity extends Activity implements SensorEventListener {
    View view;
    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    boolean up = false;
    long t0 = 0;
    long t1 = 0;
    long a = 0;

    private static final String TAG = "AccelTestActivity";

    TextView xValue, yValue, zValue, airtime;

    private long lastUpdate = 0;
    private float last_x, last_y, last_z;
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
        airtime = (TextView) findViewById(R.id.airtime);
        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];

            xValue.setText("xValue: " + sensorEvent.values[0]);
            yValue.setText("yValue: " + sensorEvent.values[1]);
            zValue.setText("zValue: " + sensorEvent.values[2]);
            airtime.setText("most recent airtime: " + a +" ms");


            long curTime = System.currentTimeMillis();

            if ((curTime - lastUpdate) > 100) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;

                float speed = Math.abs(x + y + z - last_x - last_y - last_z)/ diffTime * 10000;

               // if (speed > SHAKE_THRESHOLD) {
                 //   view.setBackgroundResource(R.color.colorPrimary);
                //}
                if (thrown(x, y, z)){
                    up = true;
                    t0 = System.currentTimeMillis();
                    view.setBackgroundResource(R.color.colorPrimary);
                }

                if (up){
                    if (landed(x,y,z)) {
                        t1 = System.currentTimeMillis();
                        a = t1-t0;
                        airtime.setText("most recent airtime: " + a + " ms");
                        view.setBackgroundResource(R.color.colorAccent);
                        up = false;
                    }
                }

                last_x = x;
                last_y = y;
                last_z = z;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    protected void onPause() {
        super.onPause();
        senSensorManager.unregisterListener(this);
    }

    protected void onResume() {
        super.onResume();
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

}
