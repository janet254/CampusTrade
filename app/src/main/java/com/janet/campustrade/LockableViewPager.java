package com.janet.campustrade;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by Janet on 20/11/2017.
 */

public class LockableViewPager extends ViewPager {

    private boolean swipeable;

    public LockableViewPager(Context context){
        super(context);
    }

    public LockableViewPager(Context context, AttributeSet attributeSet){
        super(context, attributeSet);
        this.swipeable = false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        if (this.swipeable){
            return super.onTouchEvent(event);
        }

        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event){
        if (this.swipeable){
            return super.onInterceptTouchEvent(event);
        }

        return false;
    }
}
