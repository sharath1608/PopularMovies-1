package app.sunshine.android.example.com.popmovies;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Asus1 on 10/2/2015.
 *
 * Thanks to user Matthieu of StackOverflow for coming up with this solution. Credits to him.
 */
public class TouchMask extends View {

    private boolean touch_switch = true;

    public TouchMask(Context context) {
        super(context);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return touch_switch;
    }

    public TouchMask(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void enable_touchmask(boolean action){
        touch_switch = action;
    }
}
