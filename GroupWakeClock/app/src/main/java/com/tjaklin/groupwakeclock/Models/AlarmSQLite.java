package com.tjaklin.groupwakeclock.Models;

import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.Locale;

public class AlarmSQLite implements Parcelable {

    public static final String TABLE = "alarms";

    public static final String COLUMN_DATETIME = "datetime";
    public static final String COLUMN_GROUP_NAME = "group_name";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_COMPLEXITY = "complexity";
    public static final String COLUMN_QUESTION = "question";
    public static final String COLUMN_ANSWERS = "answers";
    public static final String COLUMN_GROUP_CHAT_ID = "group_chat_id";
    public static final String COLUMN_MEMBERSHIP_ID = "membership_id";

    public static final String CREATE_TABLE_QUERY =
            "CREATE TABLE " + TABLE + "("
                    + COLUMN_DATETIME + " INTEGER NOT NULL,"
                    + COLUMN_GROUP_NAME + " TEXT,"
                    + COLUMN_EMAIL + " TEXT NOT NULL,"
                    + COLUMN_TYPE + " TEXT,"
                    + COLUMN_COMPLEXITY + " INTEGER,"
                    + COLUMN_QUESTION + " BLOB,"
                    + COLUMN_ANSWERS + " TEXT,"
                    + COLUMN_GROUP_CHAT_ID + " TEXT,"
                    + COLUMN_MEMBERSHIP_ID + " TEXT,"
                    + "PRIMARY KEY ("
                    + COLUMN_DATETIME + ", "
                    + COLUMN_EMAIL + ")"
                    + ");";

    private String type, groupname, answer, email;
    private String membershipID, groupChatID;
    private int complexity;
    private long datetime;
    private byte[] question;

    public AlarmSQLite(long dt) {
        datetime = dt;
    }
    public AlarmSQLite(long dt, String gn, String e, String t, int c, byte[] q, String a, String gcID, String mID) {
        datetime = dt;
        groupname = gn;
        email = e;
        type = t;
        complexity = c;
        question = q;
        answer = a;
        groupChatID = gcID;
        membershipID = mID;
    }

    /**
     * getters
     */
    public long getDatetime() {
        return datetime;
    }
    public String getGroupname() {
        return groupname;
    }
    public String getEmail() {
        return email;
    }
    public String getDatetimeFormatted() {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(datetime);

        SimpleDateFormat sf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault());

        return sf.format(c.getTime());
    }
    public String getType() {
        return type;
    }
    public int getComplexity() {
        return complexity;
    }
    public byte[] getQuestion() {
        return question;
    }
    public String getAnswer() {
        return answer;
    }
    public String[] getAnswerAsStringArray() {

        // Answers can contain 1 or multiple possible answers. They are separated by a comma and a
        // space (cs = ", "). To get each of the possible answers, we have to split our 'answer'
        // string.

        if (answer == null || answer.isEmpty()) {
            Log.d("AlarmSQLite", "[getAnswerAsStringArray] answer is NULL or Empty!");
            return null;
        }

        String cs = ", ";

        String[] answerArray = answer.split(cs);

        if (answerArray.length < 1) {
            Log.e("AlarmSQLite", "[getAnswerAsStringArray] answerArray is of illegal size!");
            return null;
        }

        return answerArray;
    }
    public String getGroupChatID() {
        return groupChatID;
    }
    public String getMembershipID() {
        return membershipID;
    }

    /**
     * setters
     */
    public void setDatetime(long datetime) {
        this.datetime = datetime;
    }
    public void setGroupname(String groupname) {
        this.groupname = groupname;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public void setType(String type) {
        this.type = type;
    }
    public void setComplexity(int complexity) {
        this.complexity = complexity;
    }
    public void setQuestion(byte[] question) {
        this.question = question;
    }
    public void setAnswer(String answer) {
        this.answer = answer;
    }
    public void setGroupChatID(String groupChatID) {
        this.groupChatID = groupChatID;
    }
    public void setMembershipID(String membershipID) {
        this.membershipID = membershipID;
    }

    /**
     * Implementation of Parcelable
     */
    private AlarmSQLite(Parcel in) {
        type = in.readString();
        complexity = in.readInt();

        // I'm not using these.
//        answer = in.readString();
//        datetime = in.readLong();
//        in.readByteArray(question);
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(type);
        out.writeInt(complexity);

        // Not using these.
//        out.writeString(answer);
//        out.writeLong(datetime);
//        out.writeByteArray(question);
    }

    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<AlarmSQLite> CREATOR
            = new Parcelable.Creator<AlarmSQLite>() {
        public AlarmSQLite createFromParcel(Parcel in) {
            return new AlarmSQLite(in);
        }

        public AlarmSQLite[] newArray(int size) {
            return new AlarmSQLite[size];
        }
    };


}
