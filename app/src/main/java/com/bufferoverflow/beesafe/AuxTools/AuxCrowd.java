package com.bufferoverflow.beesafe.AuxTools;

import com.bufferoverflow.beesafe.R;
import com.google.firebase.database.DataSnapshot;

enum Crowded {
    NO_DATA,
    SAFE,
    LOW,
    HIGH
}

public class AuxCrowd {

    /* Checks if the passed snapshot, is Crowd or not */
    public static int crowdType (DataSnapshot snapshot) {
        Object snap = snapshot.child("nrDevices").getValue();
        if (snap == null)
            return R.string.no_data;
        else {
            int nrDevices = ((Long) snap).intValue();
            if (nrDevices < 20)
                return R.string.safe;
            else if (nrDevices < 30)
                return R.string.low;
            else
                return R.string.high;
        }
    }

}
