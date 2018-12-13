/*
PointerDrawable.java

A class that generates a cross-hair pointer in the center of the screen.

Author: Kobe
 */

package com.moonsplain.kobe;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class PointerDrawable extends Drawable {
    private final Paint paint = new Paint();
    private boolean enabled;

    @Override
    public void draw(@NonNull Canvas canvas) {
        float cx = canvas.getWidth()/2;     //Get width and height of screen.
        float cy = canvas.getHeight()/2;
        if (enabled) {      //If a plane is detected, change the pointer to a green circle.
            paint.setColor(Color.GREEN);
            canvas.drawCircle(cx, cy, 10, paint);
        } else {        //Else, change the pointer to a gray x.
            paint.setColor(Color.GRAY);
            canvas.drawText("X", cx, cy, paint);
        }
    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return 0;
    }       //Opacity of pointer is always 0.

    public boolean isEnabled() {
        return enabled;
    }       //Method to check if enabled boolean is true.

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }       //Method to change state of enable boolean.
}
