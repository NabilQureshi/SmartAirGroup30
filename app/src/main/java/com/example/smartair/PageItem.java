package com.example.smartair;



public class PageItem {
    public int imageRes; // 0 => special (youtube)
    public String text;  // for youtube page we store "youtube:VIDEO_ID"

    public PageItem(int imageRes, String text) {
        this.imageRes = imageRes;
        this.text = text;
    }
}

