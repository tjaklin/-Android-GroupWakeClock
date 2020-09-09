package com.tjaklin.groupwakeclock.Models;

public class AlarmFB {

    private static final String TAG = AlarmFB.class.getSimpleName();

    private String alarmID, questionID, answer;

    public AlarmFB(String qID, String a) {
        questionID = qID;
        answer = a;
    }
    public AlarmFB(String aID, String qID, String a) {
        alarmID = aID;
        questionID = qID;
        answer = a;
    }

    /**
     * getters
     */
    public String getAlarmID() {
        return alarmID;
    }
    public String getQuestionID() {
        return questionID;
    }
    public String getAnswer() {
        return answer;
    }

    /**
     * setters
     */
    public void setQuestionID(String questionID) {
        this.questionID = questionID;
    }
    public void setAnswer(String answer) {
        this.answer = answer;
    }
}
