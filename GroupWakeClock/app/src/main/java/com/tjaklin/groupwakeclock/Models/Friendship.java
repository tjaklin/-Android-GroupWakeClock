package com.tjaklin.groupwakeclock.Models;

public class Friendship {

    private static final String TAG = Friendship.class.getSimpleName();

    private String userID1, userID2;

    public Friendship(String uid1, String uid2) {
        userID1 = uid1;
        userID2 = uid2;
    }

    /**
     * getters
     */
    public String getUserID1() {
        return userID1;
    }
    public String getUserID2() {
        return userID2;
    }
}
