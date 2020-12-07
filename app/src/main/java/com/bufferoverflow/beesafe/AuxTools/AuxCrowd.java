package com.bufferoverflow.beesafe.AuxTools;

import com.bufferoverflow.beesafe.R;
import com.google.firebase.database.DataSnapshot;



public class AuxCrowd {

    public enum Crowded {
        NO_DATA,
        SAFE,
        LOW,
        HIGH
    }

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

    public static Crowded crowd (DataSnapshot snapshot) {
        Object snap = snapshot.child("nrDevices").getValue();
        if (snap == null)
            return Crowded.NO_DATA;
        else {
            int nrDevices = ((Long) snap).intValue();
            if (nrDevices < 20)
                return Crowded.SAFE;
            else if (nrDevices < 30)
                return Crowded.LOW;
            else
                return Crowded.HIGH;
        }
    }

    public static boolean isCrowd(DataSnapshot snapshot) {
        Object snap = snapshot.child("nrDevices").getValue();
        return snap != null && ((Long) snap).intValue() >= 20;
    }

    public static boolean isCrowd(int nrDevices) {
        return nrDevices>20;
    }

    public static Crowded crowdType(int nrDevices) {
        if (nrDevices<20)
            return Crowded.SAFE;
        else if (nrDevices < 30)
            return Crowded.LOW;
        else
            return Crowded.HIGH;
    }

}
