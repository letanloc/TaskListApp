package com.gmail.philliphsudev.progress;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Phillip Hsu on 6/29/2015.
 */
public class RecyclerItemClickListener implements RecyclerView.OnItemTouchListener {
    public static final String TAG = RecyclerItemClickListener.class.getSimpleName();

    private OnItemClickListener mItemClickListener;
    private GestureDetector mGestureDetector;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public RecyclerItemClickListener(Context context, OnItemClickListener listener) {
        mItemClickListener = listener;
        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                Log.i(TAG, "onSingleTapUp()");
                return true;
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        //Log.i(TAG, "onInterceptTouchEvent()");
        View childView = rv.findChildViewUnder(e.getX(), e.getY());
        // Check for nulls and then trigger the gesture listener callbacks
        boolean b1 = childView != null;
        boolean b2 = mItemClickListener != null;
        boolean b3 = mGestureDetector.onTouchEvent(e);
        if (childView != null && mItemClickListener != null && b3) {
            //Log.i(TAG, "Calling onItemClick()");
            mItemClickListener.onItemClick(rv.getChildAdapterPosition(childView));
            return true;
        }
        //Log.i(TAG, "Failed to call onItemClick()!");
        //Log.e(TAG, "Bool values: " + b1 + ", " + b2 + ", " + b3);
        return false;
    }

    // This implementation is from the RecyclerView.OnItemTouchListener interface.
    // It is not the same as the one from the GestureDetector class.
    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        // Leave blank
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        // Leave blank
    }
}

