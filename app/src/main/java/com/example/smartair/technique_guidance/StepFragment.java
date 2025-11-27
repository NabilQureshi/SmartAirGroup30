package com.example.smartair.technique_guidance;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.smartair.R;

public class StepFragment extends Fragment {

    private static final String ARG_IMG = "arg_img";
    private static final String ARG_TEXT = "arg_text";

    public static StepFragment newInstance(int imgRes, String text) {
        StepFragment f = new StepFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_IMG, imgRes);
        b.putString(ARG_TEXT, text);
        f.setArguments(b);
        return f;
    }

    private int imgRes;
    private String text;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            imgRes = getArguments().getInt(ARG_IMG);
            text = getArguments().getString(ARG_TEXT);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_step, container, false);
        ImageView img = v.findViewById(R.id.stepImage);
        TextView txt = v.findViewById(R.id.stepText);

        if (imgRes != 0) {
            img.setImageResource(imgRes);
        }
        txt.setText(text);

        return v;
    }
}

