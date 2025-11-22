package com.example.smartair;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class YoutubePageFragment extends Fragment {

    private static final String ARG_VIDEO = "arg_video";
    private String videoId;

    public static YoutubePageFragment newInstance(String videoId) {
        YoutubePageFragment f = new YoutubePageFragment();
        Bundle b = new Bundle();
        b.putString(ARG_VIDEO, videoId);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) videoId = getArguments().getString(ARG_VIDEO);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_youtube, container, false);
        WebView webView = v.findViewById(R.id.youtubeWebView);

        WebSettings ws = webView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient());

        // Use YouTube embed (responsive)
        String html = "<html><body style='margin:0;padding:0;'><iframe width='100%' height='100%' " +
                "src='https://www.youtube.com/embed/" + videoId + "?rel=0&autoplay=0' " +
                "frameborder='0' allow='accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture' allowfullscreen></iframe></body></html>";

        webView.loadData(html, "text/html", "utf-8");
        return v;
    }
}

