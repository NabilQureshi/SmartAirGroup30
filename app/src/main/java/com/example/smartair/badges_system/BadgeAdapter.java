package com.example.smartair.badges_system;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;


import com.example.smartair.R;

import java.util.List;

public class BadgeAdapter extends RecyclerView.Adapter<BadgeAdapter.ViewHolder> {

    private List<BadgeModel> list;
    private Context ctx;

    public BadgeAdapter(List<BadgeModel> list, Context ctx) {
        this.list = list;
        this.ctx = ctx;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(ctx).inflate(R.layout.badge_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        BadgeModel b = list.get(pos);

        // 设置图片
        int imgRes = ctx.getResources().getIdentifier(b.id, "drawable", ctx.getPackageName());
        h.ivBadge.setImageResource(imgRes);

        // 标题
        h.tvBadgeTitle.setText(b.title);

        // 是否获得
        h.tvAchieved.setText("Obtained：" + (b.achieved ? "Yes" : "No"));

        // 第一次获得时间
        h.tvFirstTime.setText("Obtained for the first time：" + b.firstTime);

        // 描述
        h.tvDescription.setText(b.description);

        // 是否高亮（新徽章）
        if (b.highlight) {
            h.card.setCardBackgroundColor(Color.parseColor("#FFF59D")); // 黄色高亮
        } else {
            h.card.setCardBackgroundColor(Color.WHITE);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivBadge;
        TextView tvBadgeTitle, tvAchieved, tvFirstTime, tvDescription;
        CardView card;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBadge = itemView.findViewById(R.id.ivBadge);
            tvBadgeTitle = itemView.findViewById(R.id.tvBadgeTitle);
            tvAchieved = itemView.findViewById(R.id.tvAchieved);
            tvFirstTime = itemView.findViewById(R.id.tvFirstTime);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            card = itemView.findViewById(R.id.cardBadge);
        }
    }
}
