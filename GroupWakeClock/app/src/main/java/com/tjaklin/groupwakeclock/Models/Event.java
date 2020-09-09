package com.tjaklin.groupwakeclock.Models;

import android.icu.util.Calendar;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.Nullable;

public class Event implements Parcelable {

    private static final String TAG = Event.class.getSimpleName();

    private String eventID, eventname, description, adminUserID, eventChatID;
    private String currentUserMembershipID;
    private Calendar datetime;
    private AlarmTemplate defaultAlarmTemplate;

    public Event(String nm, long dt) {
        eventname = nm;
        setDatetimeFromMillis(dt);
    }
    public Event(String nm, String desc, long dt, AlarmTemplate at) {
        eventname = nm;
        description = desc;
        defaultAlarmTemplate = at;
        setDatetimeFromMillis(dt);
    }
    public Event(String gID, String nm, String desc, long dt, String uID, String cID, AlarmTemplate at) {
        eventID = gID;
        eventname = nm;
        description = desc;
        setDatetimeFromMillis(dt);
        adminUserID = uID;
        eventChatID = cID;
        defaultAlarmTemplate = at;
    }

    /**
     * getters
     */
    public String getEventID() {
        return eventID;
    }
    public String getEventname() {
        return eventname;
    }
    public String getDescription() {
        return description;
    }
    public String getEventChatID() {
        return eventChatID;
    }
    public String getAdminUserID() {
        return adminUserID;
    }
    public String getCurrentUserMembershipID() {
        return currentUserMembershipID;
    }
    public AlarmTemplate getDefaultAlarmTemplate() {
        return defaultAlarmTemplate;
    }

    public Calendar getDatetime() {return datetime;}
    public long getDatetimeInMillis() {
        // Returns -1 if something went wrong!
        if (datetime == null) {
            Log.d(TAG, "getDate(): groupDatetime object is NULL!");
            return -1;
        }

        return datetime.getTimeInMillis();
    }

    /**
     * setters
     */
    public void set(Event newEvent) {
        setEventname(newEvent.getEventname());
        setDescription(newEvent.getDescription());
        setAdminUserID(newEvent.getAdminUserID());
        setEventChatID(newEvent.getEventChatID());
        setDefaultAlarmTemplate(newEvent.getDefaultAlarmTemplate());
        setDatetimeFromMillis(newEvent.getDatetimeInMillis());
    }
    public void setEventID(String eventID) {
        this.eventID = eventID;
    }
    public void setEventname(String eventname) {
        this.eventname = eventname;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public void setEventChatID(String eventChatID) {
        this.eventChatID = eventChatID;
    }
    public void setAdminUserID(String adminUserID) {
        this.adminUserID = adminUserID;
    }
    public void setCurrentUserMembershipID(String currentUserMembershipID) {
        this.currentUserMembershipID = currentUserMembershipID;
    }
    public void setDefaultAlarmTemplate(AlarmTemplate defaultAlarmTemplate) {
        this.defaultAlarmTemplate = defaultAlarmTemplate;
    }

    public void setDatetimeFromMillis(long millis) {
        if (datetime == null)
            datetime = Calendar.getInstance();
        datetime.setTimeInMillis(millis);
    }


    /**
     * overridal of equals and hash
     */
    @Override
    public boolean equals(@Nullable Object obj) {
        boolean retVal = false;

        if (obj instanceof Event){
            Event ptr = (Event) obj;
            retVal = ptr.getEventID().equals(eventID);
        }

        return retVal;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + (eventID != null ? eventID.hashCode() : 0);
        return hash;
    }


    /**
     * Parcelable implementation
     */
    private Event(Parcel in) {
        eventID = in.readString();
        eventname = in.readString();
        description = in.readString();
        adminUserID = in.readString();
        eventChatID = in.readString();
        String aType = in.readString();

        setDatetimeFromMillis(in.readLong());
        int aComp = in.readInt();

        defaultAlarmTemplate = new AlarmTemplate(aType, aComp);
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(eventID);
        out.writeString(eventname);
        out.writeString(description);
        out.writeString(adminUserID);
        out.writeString(eventChatID);
        out.writeString(defaultAlarmTemplate.getType());

        out.writeLong(datetime.getTimeInMillis());
        out.writeInt(defaultAlarmTemplate.getComplexity());
    }

    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Event> CREATOR
            = new Parcelable.Creator<Event>() {
        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }

        public Event[] newArray(int size) {
            return new Event[size];
        }
    };


}
