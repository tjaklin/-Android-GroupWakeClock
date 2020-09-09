package com.tjaklin.groupwakeclock.Models;

public class Membership {

    private static final String TAG = Membership.class.getSimpleName();

    private String membershipID, eventID, userID;
    private long datetime;
    private AlarmTemplate alarmTemplate;
    private boolean isAwake;

    public Membership(String mID, AlarmTemplate at, boolean awake) {
        membershipID = mID;
        alarmTemplate = at;
        isAwake = awake;
    }
    public Membership(String gID, String uID, long dt, AlarmTemplate at) {
        eventID = gID;
        userID = uID;
        datetime = dt;
        alarmTemplate = at;
        isAwake = false;
    }
    public Membership(String mID, String gID, String uID, long dt, AlarmTemplate at) {
        membershipID = mID;
        eventID = gID;
        userID = uID;
        datetime = dt;
        alarmTemplate = at;
        isAwake = false;
    }

    /**
     * getters
     */
    public String getMembershipID() {
        return membershipID;
    }
    public String getEventID() {
        return eventID;
    }
    public String getUserID() {
        return userID;
    }
    public long getDatetimeInMillis() {
        return datetime;
    }
    public AlarmTemplate getAlarmTemplate() {
        return alarmTemplate;
    }
    public boolean isAwake() {
        return isAwake;
    }

    /**
     * setters
     */
    public void setDatetimeInMillis(long datetime) {
        this.datetime = datetime;
    }
    public void setMembershipID(String membershipID) {
        this.membershipID = membershipID;
    }
    public void setAlarmTemplate(AlarmTemplate alarmTemplate) {
        this.alarmTemplate = alarmTemplate;
    }
    public void isAwake(boolean awake) {
        isAwake = awake;
    }
}