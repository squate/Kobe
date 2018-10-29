package com.moonsplain.kobe;
import static java.lang.Math.sqrt;

public class AccelInputActivity {


    public boolean thrown(float x, float y, float z){
        //if magnitude of accelerometer vector is close enough to zero (phone is probably in free-fall
        if(sqrt(x*x+y*y+z*z) < 0.2)
            return True;
        else
            return False;
    }
}
