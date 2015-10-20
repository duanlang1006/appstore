package com.applite.calendarview;

import android.text.format.Time;

/**
 * Created by LSY on 15-10-15.
 */
public interface ICallback {
    void OnClickDate(Time selectedTime);

    void ToNextMonth();

    void ToLastMonth();
}
