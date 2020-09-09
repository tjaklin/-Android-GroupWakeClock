package com.tjaklin.groupwakeclock.Models;

public class Member {

    private static final String TAG = Member.class.getSimpleName();

    private String membershipID, userID, email;
    private AlarmTemplate alarmTemplate;
    private boolean isAwake, isSelected;

    public Member(String id, String e) {
        userID = id;
        email = e;
        isAwake = false;
        isSelected = false;
    }
    public Member(String id, String e, String mID, AlarmTemplate at, boolean iA) {
        userID = id;
        email = e;
        membershipID = mID;
        alarmTemplate = at;
        isAwake = iA;
        isSelected = false;
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
    public String getMembershipID() {
        return membershipID;
    }
    public AlarmTemplate getAlarmTemplate() {
        return alarmTemplate;
    }
    public boolean isAwake() {
        return isAwake;
    }
    public boolean isSelected() {
        return isSelected;
    }

    /**
     * setters
     */
    public void setMembershipID(String membershipID) {
        this.membershipID = membershipID;
    }
    public void setAlarmTemplate(AlarmTemplate alarmTemplate) {
        this.alarmTemplate = alarmTemplate;
    }
    public void isAwake(boolean awake) {
        isAwake = awake;
    }
    public void isSelected(boolean selected) {
        isSelected = selected;
    }

    /**
     * overridal of equals and hash
     */
    @Override
    public boolean equals(Object v) {
        boolean retVal = false;

        if (v instanceof Member){
            Member ptr = (Member) v;
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
}