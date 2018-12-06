package com.moonsplain.kobe;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.util.Log;

import com.google.ar.core.Anchor;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import com.google.ar.core.Camera;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;


public class ARViewActivity extends AppCompatActivity implements SensorEventListener {

    private ArFragment fragment;

    private PointerDrawable pointer = new PointerDrawable();
    private boolean isTracking;
    private boolean isHitting;
    private boolean hitTarget;
    private boolean throwing;
    private boolean successChanged = false;
    private int streak = 0;
    public static Anchor targetAnchor;

    private boolean targetActive;
    TextView streakView;

    private SensorManager senSensorManager;
    //private SensorManager gyroSensorManager;
    private Sensor senAccelerometer;
    //private Sensor senGyro;
    float x, y, z, gX, gY, gZ, gN, gN0, twirl = 0;
    long t0, t1, best, a = 0;
    boolean up = false;
    //boolean faceDown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arview);

        fragment = (ArFragment)
                getSupportFragmentManager().findFragmentById(R.id.sceneform_fragment);

        fragment.getArSceneView().getScene().addOnUpdateListener(frameTime -> {
            fragment.onUpdate(frameTime);
            onUpdate();
        });

        targetActive = false;
        initializeButton();

        findViewById(R.id.floatingActionButton).setOnClickListener(view -> enterThrow());
        streakView = findViewById(R.id.textView14);
        //streakView.setText("Streak: "+streak);

        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_DELAY_FASTEST);

    }

    private void onUpdate() {
        boolean trackingChanged = updateTracking();

        View contentView = findViewById(android.R.id.content);
        if (trackingChanged) {
            if (isTracking) {
                contentView.getOverlay().add(pointer);
            } else {
                contentView.getOverlay().remove(pointer);
            }
            contentView.invalidate();
        }
        if (up && !hitTarget) {
            if (updateSuccess()){
                hitTarget = true;
            }
        }
        //streakView.setText("Streak: "+streak);
        if (isTracking) {
            boolean hitTestChanged = updateHitTest();
            if (hitTestChanged) {
                pointer.setEnabled(isHitting);
                contentView.invalidate();
            }
        }
    }
    private boolean updateSuccess(){
        Frame frame = fragment.getArSceneView().getArFrame();
        Camera c = frame.getCamera();
        return closeEnough(c.getPose(), targetAnchor.getPose());
    }
    private boolean updateTracking() {
        Frame frame = fragment.getArSceneView().getArFrame();
        boolean wasTracking = isTracking;
        isTracking = frame != null &&
                frame.getCamera().getTrackingState() == TrackingState.TRACKING;
        return isTracking != wasTracking;
    }

    private boolean updateHitTest() {
        Frame frame = fragment.getArSceneView().getArFrame();
        android.graphics.Point pt = getScreenCenter();
        List<HitResult> hits;
        boolean wasHitting = isHitting;
        isHitting = false;
        if (frame != null) {
            hits = frame.hitTest(pt.x, pt.y);
            for (HitResult hit : hits) {
                Trackable trackable = hit.getTrackable();
                if (trackable instanceof Plane &&
                        ((Plane) trackable).isPoseInPolygon(hit.getHitPose())) {
                    isHitting = true;
                    break;
                }
            }
        }
        return wasHitting != isHitting;
    }

    private android.graphics.Point getScreenCenter() {
        View vw = findViewById(android.R.id.content);
        return new android.graphics.Point(vw.getWidth()/2, vw.getHeight()/2);
    }

    private void addObject(Uri model) {
        Frame frame = fragment.getArSceneView().getArFrame();
        android.graphics.Point pt = getScreenCenter();
        List<HitResult> hits;
        if (frame != null) {
            hits = frame.hitTest(pt.x, pt.y);
            for (HitResult hit : hits) {
                Trackable trackable = hit.getTrackable();
                if (trackable instanceof Plane &&
                        ((Plane) trackable).isPoseInPolygon(hit.getHitPose())) {
                    if (targetActive == false) {
                        targetAnchor = hit.createAnchor();
                        placeObject(fragment, targetAnchor, model);
                        targetActive = true;
                        break;
                    }
                }
            }
        }
    }

    private void placeObject(ArFragment fragment, Anchor anchor, Uri model) {
        CompletableFuture<Void> renderableFuture =
                ModelRenderable.builder()
                        .setSource(fragment.getContext(), model)
                        .build()
                        .thenAccept(renderable -> addNodeToScene(fragment, anchor, renderable))
                        .exceptionally((throwable -> {
                            AlertDialog.Builder builder = new AlertDialog.Builder(this);
                            builder.setMessage(throwable.getMessage())
                                    .setTitle("Kobe error!");
                            AlertDialog dialog = builder.create();
                            dialog.show();
                            return null;
                        }));
    }

    private void addNodeToScene(ArFragment fragment, Anchor anchor, Renderable renderable) {
        AnchorNode anchorNode = new AnchorNode(anchor);
        TransformableNode node = new TransformableNode(fragment.getTransformationSystem());
        node.setRenderable(renderable);
        node.setParent(anchorNode);
        fragment.getArSceneView().getScene().addChild(anchorNode);
        node.select();
    }

    //Button to place target
    private void initializeButton() {
        FloatingActionButton button = findViewById(R.id.floatingActionButton2);

        button.setOnClickListener(view -> {addObject(Uri.parse("model.sfb"));});

    }

    /*private String generateFilename() {
        String date =
                new SimpleDateFormat("yyyyMMddHHmmss", java.util.Locale.getDefault()).format(new Date());
        return Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES) + File.separator + "Sceneform/" + date + "_screenshot.jpg";
    }

    private void saveBitmapToDisk(Bitmap bitmap, String filename) throws IOException {

        File out = new File(filename);
        if (!out.getParentFile().exists()) {
            out.getParentFile().mkdirs();
        }
        try (FileOutputStream outputStream = new FileOutputStream(filename);
             ByteArrayOutputStream outputData = new ByteArrayOutputStream()) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputData);
            outputData.writeTo(outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (IOException ex) {
            throw new IOException("Failed to save bitmap to disk", ex);
        }
    }*/

    private void enterThrow() {
        throwing = true;
    }

    private boolean closeEnough(Pose cam, Pose targ){
            float dx = cam.tx() - targ.tx();
            float dy = cam.ty() - targ.ty();
            float dz = cam.tz() - targ.tz();
            double dist = Math.sqrt(dx * dx + dz * dz + dy * dy);
            double cmDist = ( (( (dist) * 1000)));
            Log.d("DIST", "d"+cmDist);
            if (cmDist < 300){
                return true;
            }else{
                return false;
            }
            //streakView.setText(  (int) cmDist  );
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;
        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            x = sensorEvent.values[0];
            y = sensorEvent.values[1];
            z = sensorEvent.values[2];
            if (thrown(x, y, z) && !up) { //phone enters free fall
                t0 = System.currentTimeMillis();
                up = true;
            }
            if (up) {
                if (hitTarget);
                 {
                    streak++;
                    streakView.setText("streak" + streak);
                    hitTarget = false;
                    up = false;
                }
                hitTarget = false;
                if (up && landed(x, y, z)){
                    Log.d("LAND", "made it into the if statement");
                    t1 = System.currentTimeMillis();
                    a = t1 - t0;
                    //streakView.setText(".");
                    if (a > 100 && !hitTarget) {
                        Log.d("LAND", "made it into the if statement");
                        streak = 0;
                        streakView.setText("STREAK: "+ streak);
                    }
                    if (a > best){
                        best = a;
                    }
                    up = false;
                }
            }
        }
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

    //true if the normal vector of the rotational forces is significant and unchanging
    //public boolean spinThrown(float wN0, float wN1){
      //  return (  ((wN1-wN0)/wN0 < .1)  &&  (wN1 > 100));
    //}

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy){

    }

    protected void onPause() {
        super.onPause();
        senSensorManager.unregisterListener(this);
        //gyroSensorManager.unregisterListener(this);
    }

    protected void onResume() {
        super.onResume();
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        //gyroSensorManager.registerListener(this, senGyro, SensorManager.SENSOR_DELAY_FASTEST);
    }
}
