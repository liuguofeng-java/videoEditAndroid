package com.video.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.video.R;
import com.video.entity.TabButsItem;

import java.util.ArrayList;
import java.util.List;

/**
 * 页面滑动
 *
 * @author liuguofeng
 * @date 2023/04/23 17:56
 **/
public class TabButsAdapter extends RecyclerView.Adapter<TabButsAdapter.ViewHolder> {
    private List<TabButsItem> list = new ArrayList<>();

    public void add(List<TabButsItem> list){
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_tab, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TabButsItem item = list.get(position);
        holder.imageView.setImageResource(item.getImageId());
        holder.textView.setText(item.getText());
        holder.root.setOnClickListener(view -> {
            if(mClickListener != null)
                mClickListener.onItemClick(view, position);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout root;
        ImageView imageView;
        TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.imageView = itemView.findViewById(R.id.image);
            this.textView = itemView.findViewById(R.id.text);
            this.root = itemView.findViewById(R.id.root);
        }

    }

    private OnItemClickListener mClickListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mClickListener = listener;
    }

    public interface OnItemClickListener {
        public void onItemClick(View view, int position);
    }

}

