package com.moonsplain.kobe;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.hardware.camera2.*;

import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ShapeFactory;

public class ARViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arview);
    }
    //Get intent that started this activity
    Intent intent = getIntent();

    /*MaterialFactory.makeOpaqueWithColor(this, new Color(android.graphics.Color.RED))
            .thenAccept(
                material -> {
                    redSphereRenderable =
                        ShapeFactory.makeSphere(0.1f, new Vector3(0.0f, 0.15f, 0.0f), material); });

    Node node = new Node();
    node.setParent(arFragment.getArSceneView().getScene());
    node.setRenderable(andyRenderable); */

}
