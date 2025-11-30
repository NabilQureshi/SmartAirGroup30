package com.example.smartair.checkin;

public class HistoryEntry {

    public String date;
    public String summary;
    public String triggers;
    public String submittedBy;

    public HistoryEntry(String date, String summary, String triggers, String submittedBy) {
        this.date = date;
        this.summary = summary;
        this.triggers = triggers;
        this.submittedBy = submittedBy;
    }
}
