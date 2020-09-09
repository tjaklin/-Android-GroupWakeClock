package com.tjaklin.groupwakeclock.Models;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {

    private static final String TAG = User.class.getSimpleName();

    private String userID, email;
    private boolean isFriendsWithClient;

    public User(String id, String e) {
        userID = id;
        email = e;
        isFriendsWithClient = false;
    }

    /**
     * getters
     */
    public String getUserID() {
        return userID;
    }
    public String getEmail() {
        return email;
    }
    public boolean isFriendsWithClient() {
        return isFriendsWithClient;
    }

    /**
     * setters
     */
    public void set(User newUser) {
        setEmail(newUser.getEmail());
        isFriendsWithClient(newUser.isFriendsWithClient());
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public void isFriendsWithClient(boolean friendsWithClient) {
        isFriendsWithClient = friendsWithClient;
    }

    /**
     * overridal of equals and hash
     */
    @Override
    public boolean equals(Object v) {
        boolean retVal = false;

        if (v instanceof User){
            User ptr = (User) v;
            retVal = ptr.email.equals(email);
        }

        return retVal;
    }
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + (email != null ? email.hashCode() : 0);
        return hash;
    }

    /**
     * implementation of Parcelable methods
     */

    private User(Parcel in) {
        userID = in.readString();
        email = in.readString();
        isFriendsWithClient = in.readBoolean();
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(userID);
        out.writeString(email);
        out.writeBoolean(isFriendsWithClient);
    }

    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<User> CREATOR
            = new Parcelable.Creator<User>() {
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        public User[] newArray(int size) {
            return new User[size];
        }
    };
}
