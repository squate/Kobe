/*
ThrowMode.java

The class for the core game of the app. Displays an AR fragment that allows the user to place a
target on a plane, then throw their device at said target to make their streak increase. Detects
throw legitimacy and accuracy.

Author: Kobe
 */

package com.moonsplain.kobe;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
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
import java.util.List;
import java.util.concurrent.CompletableFuture;


public class ThrowMode extends AppCompatActivity implements SensorEventListener {
    //Instantiate variables
    private ArFragment fragment;

    private PointerDrawable pointer = new PointerDrawable();
    private boolean isTracking;
    private boolean isHitting;
    private boolean hitTarget = false;
    private boolean throwing;
    private boolean successChanged = false;
    private int streak, streakLast = 0;
    public static Anchor targetAnchor;

    private boolean targetActive;
    TextView streakView;

    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    float x, y, z = 0;
    long t0, t1, best, a = 0;
    boolean up = false;

    public static final String myPref = "Leaderboard preferences";
    public static final String leaderStreak = "Streak";
    public static final String leaderProx = "Proximity";
    public static final String leaderAirtime = "Best Airtime";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arview);

        fragment = (ArFragment)     //Display ArFragment
                getSupportFragmentManager().findFragmentById(R.id.sceneform_fragment);

        fragment.getArSceneView().getScene().addOnUpdateListener(frameTime -> {
            fragment.onUpdate(frameTime);       //Call onUpdate();
            onUpdate();
        });

        targetActive = false;       //No target is present at startup.
        initializeButton();     //Place target generation button.

        findViewById(R.id.floatingActionButton).setOnClickListener(view -> enterThrow());
        streakView = findViewById(R.id.textView14);     //Display user's streak of successful throws.
        //streakView.setText("Streak: "+streak);

        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_DELAY_FASTEST);
        //Set up accelerometer sensor for throw detection.

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
        if (up ) {      //If device is in free fall
            if (!hitTarget && updateSuccess()) {
                hitTarget = true;       //User has hit target.
                streak++;       //Increment streak.
                //streakView.setText("Streak: " + streak);
                targetAnchor.detach();      //Delete target so that the user may place a new one.
                targetActive = false;
                return;
            }
        }
        if (streak > streakLast+1){        //Stops a glitch where the streak would increment by 2
            streak = streakLast+1;         //instead of 1.
        }
        streakView.setText("Streak: "+streak);      //Update streak display.
        if (isTracking) {
            boolean hitTestChanged = updateHitTest();
            if (hitTestChanged) {
                pointer.setEnabled(isHitting);
                contentView.invalidate();
            }
        }
        SharedPreferences pref = getSharedPreferences(myPref, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(leaderStreak, streak);        //Send streak to leaderboard.
        editor.putLong(leaderAirtime, best);        //Send best airtime to leaderboard.
        editor.commit();
    }
    private boolean updateSuccess(){
        Frame frame = fragment.getArSceneView().getArFrame();
        Camera c = frame.getCamera();
        return closeEnough(c.getPose(), targetAnchor.getPose());        //Call closeEnough to detect
                                                                        //that the device is within
                                                                        //range of the target.
    }
    private boolean updateTracking() {      //Method to detect if camera is tracking the location of
        Frame frame = fragment.getArSceneView().getArFrame();       //the pointer.
        boolean wasTracking = isTracking;
        isTracking = frame != null &&
                frame.getCamera().getTrackingState() == TrackingState.TRACKING;
        return isTracking != wasTracking;
    }

    private boolean updateHitTest() {
        Frame frame = fragment.getArSceneView().getArFrame();
        android.graphics.Point pt = getScreenCenter();      //Get center of screen.
        List<HitResult> hits;
        boolean wasHitting = isHitting;
        isHitting = false;
        if (frame != null) {
            hits = frame.hitTest(pt.x, pt.y);
            for (HitResult hit : hits) {        //If pointer has hit a plane
                Trackable trackable = hit.getTrackable();
                if (trackable instanceof Plane &&
                        ((Plane) trackable).isPoseInPolygon(hit.getHitPose())) {
                    isHitting = true;       //Update isHitting.
                    break;
                }
            }
        }
        return wasHitting != isHitting;     //Update wasHitting.
    }

    private android.graphics.Point getScreenCenter() {      //Get center point of screen to draw pointer.
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
                if (trackable instanceof Plane &&       //If a plane has been detected
                        ((Plane) trackable).isPoseInPolygon(hit.getHitPose())) {
                    if (!targetActive) {        //If there are no other targets
                        targetAnchor = hit.createAnchor();      //Store target anchor in global variable.
                        placeObject(fragment, targetAnchor, model);     //Place target model.
                        targetActive = true;        //Stop the user from placing more than one target.
                        break;
                    }
                }
            }
        }
    }

    private void placeObject(ArFragment fragment, Anchor anchor, Uri model) {
        CompletableFuture<Void> renderableFuture =
                ModelRenderable.builder()       //Build the model.
                        .setSource(fragment.getContext(), model)
                        .build()
                        .thenAccept(renderable -> addNodeToScene(fragment, anchor, renderable))
                        .exceptionally((throwable -> {      //Catch builder errors.
                            AlertDialog.Builder builder = new AlertDialog.Builder(this);
                            builder.setMessage(throwable.getMessage())
                                    .setTitle("Kobe error!");       //Display error message.
                            AlertDialog dialog = builder.create();
                            dialog.show();
                            return null;
                        }));
    }

    private void addNodeToScene(ArFragment fragment, Anchor anchor, Renderable renderable) {
        AnchorNode anchorNode = new AnchorNode(anchor);     //Set up anchor node.
        TransformableNode node = new TransformableNode(fragment.getTransformationSystem());
        node.setRenderable(renderable);     //Attach model to node.
        node.setParent(anchorNode);
        fragment.getArSceneView().getScene().addChild(anchorNode);      //Add anchor node to scene.
        node.select();
    }

    //Button to place target
    private void initializeButton() {
        FloatingActionButton button = findViewById(R.id.floatingActionButton2);
        button.setOnClickListener(view -> {addObject(Uri.parse("model.sfb"));});
        //Set up floating action button to place target in scene.

    }

    private void enterThrow() {
        throwing = true;        //User is throwing the device.
    }

    private boolean closeEnough(Pose cam, Pose targ){
            float dx = cam.tx() - targ.tx();        //Get distance between camera and target on
            float dy = cam.ty() - targ.ty();        //every axis.
            float dz = cam.tz() - targ.tz();
            double dist = Math.sqrt(dx * dx + dz * dz + dy * dy);       //Get general distance.
            double cmDist = ( (( (dist) * 1000)));      //Convert distance to centimeters.
            Log.d("DISTANCE", "d"+cmDist);
            if (cmDist < 500){      //If the device is within 500 centimeters of the target
                return true;        //Device is close enough.
            }else{
                return false;       //Else, device is not close enough.
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
                t0 = System.currentTimeMillis();    //get the current time
                streakLast = streak;                //get the last streak
                up = true;
            }
            if (up) {
                if (up && landed(x, y, z)){
                    t1 = System.currentTimeMillis();    //get current time
                    a = t1 - t0;                        //get airtime
                    Log.d("STREAK", "streak:"+ streak +" last: "+ streakLast);
                    if (streakLast == streak && a > 100){   //if the phone has been thrown but the target
                        streak = 0;                         //not hit, set streak back to 0
                        streakView.setText("streak: "+streak);
                    }
                    if (a > best){              //if you beat your airtime reset it
                        best = a;
                    }
                    hitTarget = false;          //detach the anchor and remove target
                    up = false;
                    targetAnchor.detach();
                    targetActive = false;
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

    //Empty method required for sensor use
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy){

    }
    //Required method for sensor use
    protected void onPause() {
        super.onPause();
        senSensorManager.unregisterListener(this);
    }
    //Required method for sensor use
    protected void onResume() {
        super.onResume();
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }
}
