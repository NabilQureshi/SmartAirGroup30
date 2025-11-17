package com.example.smartair;

import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

public class TechniqueActivity extends AppCompatActivity {

    private VideoView videoView;
    private TextView tvSteps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_technique);

        videoView = findViewById(R.id.videoView);
        tvSteps = findViewById(R.id.tvSteps);

        // 示例视频 URL（你可以换成 Firebase Storage 的下载URL 或本地 raw 资源）
        String sampleVideo = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4";

        videoView.setVideoURI(Uri.parse(sampleVideo));
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);
        videoView.requestFocus();

        // 技巧步骤
        String steps = "1. Seal lips around mouthpiece.\n"
                + "2. Take a slow, deep breath in.\n"
                + "3. Hold your breath for ~10 seconds.\n"
                + "4. If multiple puffs: wait 30–60s between puffs.\n"
                + "5. If using spacer/mask: attach properly, breathe slowly.\n\n"
                + "（点击视频播放按钮查看演示）";
        tvSteps.setText(steps);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (videoView.isPlaying()) videoView.pause();
    }
}
