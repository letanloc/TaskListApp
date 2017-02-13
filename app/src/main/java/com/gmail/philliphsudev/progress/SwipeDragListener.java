package com.gmail.philliphsudev.progress;

/**
 * Created by Phillip Hsu on 9/11/2015.
 */
public interface SwipeDragListener {

    void onItemMove(int from, int to);
    void onItemSwipe(int position, int direction);

}
