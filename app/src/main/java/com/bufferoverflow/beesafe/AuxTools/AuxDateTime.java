package com.bufferoverflow.beesafe.AuxTools;

import com.google.firebase.database.DataSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class AuxDateTime {

    public static int getLastSeen (DataSnapshot snapshot) {
        Date now = currentTime();
        Date before = stringToDate((String) snapshot.child("lastSeen").getValue());
        return timeDifference(now, before);
    }

    public static Date stringToDate (String date) {
        SimpleDateFormat ISO_8601_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
        try {
            return ISO_8601_FORMAT.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String dateToString (Date date) {
        SimpleDateFormat ISO_8601_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
        return ISO_8601_FORMAT.format(date);
    }

    public static int timeDifference (Date d1, Date d2) {
        long diffInMillisec = d1.getTime() - d2.getTime();
        return (int) TimeUnit.MILLISECONDS.toMinutes(diffInMillisec);
    }

    public static Date currentTime() {
        return Calendar.getInstance().getTime();
    }

}
