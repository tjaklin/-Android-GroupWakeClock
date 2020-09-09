package com.tjaklin.groupwakeclock.Models;

public class AlarmTemplate {

    private static final String TAG = AlarmTemplate.class.getSimpleName();

    private int complexity;
    private String type;

    public AlarmTemplate() {
        complexity = 0;
        type = "math";
    }
    public AlarmTemplate(String t, int c) {
        complexity = c;
        type = t;
    }

    /**
     * getters
     */
    public int getComplexity() {
        return complexity;
    }
    public String getType() {
        return type;
    }

    /**
     * setters
     */
    public void setComplexity(int complexity) {
        this.complexity = complexity;
    }
    public void setType(String type) {
        this.type = type;
    }
}
