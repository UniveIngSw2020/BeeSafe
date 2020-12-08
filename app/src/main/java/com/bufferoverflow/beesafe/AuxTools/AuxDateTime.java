package com.bufferoverflow.beesafe.AuxTools;

import com.google.firebase.database.DataSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/*
 * Auxiliary static methods for date and time.
 */

public class AuxDateTime {

    /* Get time difference (last seen) from a database snapshot and current time */
    public static int getLastSeen (DataSnapshot snapshot) {
        Date now = currentTime();
        Date before = stringToDate((String) snapshot.child("lastSeen").getValue());
        if (before == null)
            before = now;
        return timeDifference(now, Objects.requireNonNull(before));
    }

    /* Convert String to Date */
    public static Date stringToDate (String date) {
        SimpleDateFormat ISO_8601_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
        if (date != null) {
            try {
                return ISO_8601_FORMAT.parse(date);
            } catch (ParseException ignored) { return currentTime(); }
        }
        else
            return currentTime();
    }

    /* Convert Date to String */
    public static String dateToString (Date date) {
        SimpleDateFormat ISO_8601_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
        return ISO_8601_FORMAT.format(date);
    }

    /* Difference d1 - d2 in seconds */
    public static int timeDifference (Date d1, Date d2) {
        long diff = d1.getTime() - d2.getTime();
        return (int) TimeUnit.MILLISECONDS.toMinutes(diff);
    }

    /* Get current DateTime */
    public static Date currentTime() {
        return Calendar.getInstance().getTime();
    }

}
