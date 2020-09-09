package com.tjaklin.groupwakeclock.Models;

public class Message {

    private static final String TAG = Message.class.getSimpleName();

    private String senderID, content;

    public Message(String sID, String c) {
        senderID = sID;
        content = c;
    }

    /**
     * setters
     */
    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * getters
     */
    public String getSenderID() {
        return senderID;
    }
    public String getContent() {
        return content;
    }
}
