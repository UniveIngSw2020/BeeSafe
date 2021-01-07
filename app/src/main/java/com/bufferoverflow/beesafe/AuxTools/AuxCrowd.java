package com.bufferoverflow.beesafe.AuxTools;

import com.bufferoverflow.beesafe.MapsActivity;
import com.bufferoverflow.beesafe.R;
import com.google.firebase.database.DataSnapshot;
import com.google.maps.android.heatmaps.Gradient;

/*
 * Auxiliary static methods for crowd utilities
 */

public class AuxCrowd {

    private static final int SAFE_BOUND = 2;
    private static final int LOW_BOUND = 6;
    //private static final int HIGH_BOUND = 20;

    public enum Crowded {
        //NO_DATA,
        SAFE,
        LOW,
        HIGH
    }

    /* Checks if the passed snapshot, is Crowd or not and returns the corresponding string */
    public static int crowdTypeToString (DataSnapshot snapshot) {
        Object snap = snapshot.child("nrDevices").getValue();
        if (snap == null)
            return R.string.no_data;
        else {
            int nrDevices = ((Long) snap).intValue();
            if (nrDevices < SAFE_BOUND)
                return R.string.safe;
            else if (nrDevices < LOW_BOUND)
                return R.string.low;
            else
                return R.string.high;
        }
    }

    /* Returns a Gradient based on number of devices passed. This method is used by MapsActivity to generate the HeatMap type */
    public static Gradient crowdTypeToGradient(int nrDevices) {
        return (nrDevices >= SAFE_BOUND && nrDevices < LOW_BOUND) ? MapsActivity.HEATMAP_ORANGE : MapsActivity.HEATMAP_RED;
    }

    /* Crowd type based on devices number */
    public static Crowded crowdType(int nrDevices) {
        if (nrDevices<SAFE_BOUND)
            return Crowded.SAFE;
        else if (nrDevices < LOW_BOUND)
            return Crowded.LOW;
        else
            return Crowded.HIGH;
    }

    /* True if number is higher than the safe limit bound, otherwise false */
    public static boolean isCrowd(int nrDevices) {
        return nrDevices > SAFE_BOUND;
    }

}
