package com.bufferoverflow.beesafe;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@IgnoreExtraProperties
public class User {

    private String username;
    private String email;
    private Map<String, Location> friends;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String username, String email) {
        this.username = username;
        this.email = email;
        this.friends = new HashMap<>();
        friends.put("alb", new Location(new LatLng(12.0, 30.0)));
        friends.put("ita", new Location(new LatLng(50.0, 31.0)));
        friends.put("ukk", new Location(new LatLng(22.0, 38.0)));
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public Map<String, Location> getFriends() {
        return friends;
    }
}