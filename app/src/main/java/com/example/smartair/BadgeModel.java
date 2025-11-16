package com.example.smartair;

public class BadgeModel {
    public String id;
    public String title;
    public boolean achieved;
    public String firstTime;
    public String description;
    public boolean highlight;

    public BadgeModel(String id, String title, boolean achieved, String firstTime,
                      String description, boolean highlight) {
        this.id = id;
        this.title = title;
        this.achieved = achieved;
        this.firstTime = firstTime;
        this.description = description;
        this.highlight = highlight;
    }
}
