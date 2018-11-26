package com.moonsplain.kobe;

import android.content.Context;
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
    float x, y, z, t0;
    int streak;
    boolean up = false;
    TextView xvalue;

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

            ARViewActivity.targetAnchor.getPose();

        }
        xvalue = findViewById(R.id.textView13);

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
                streak++;
                xvalue.setText("Streak" + streak);

            }
        }

    }

    //if magnitude of accelerometer vector is close enough to zero
    // (if phone is probably in free-fall)
    public boolean thrown(float x, float y, float z) {
        return ((x * x + y * y + z * z) < 2);
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
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        proxSensorManager.registerListener(this, senProximity, SensorManager.SENSOR_DELAY_FASTEST);
        gyroSensorManager.registerListener(this, senGyro, SensorManager.SENSOR_DELAY_FASTEST);
    }


}
