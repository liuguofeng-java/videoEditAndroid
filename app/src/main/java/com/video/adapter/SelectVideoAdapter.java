package com.video.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.video.R;
import com.video.entity.TabButsItem;
import com.video.entity.VideoInfo;
import com.video.utils.VideoUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 选择图片
 *
 * @author liuguofeng
 * @date 2023/09/21 16:40
 **/
public class SelectVideoAdapter extends RecyclerView.Adapter<SelectVideoAdapter.ViewHolder> {
    private List<VideoInfo> list = new ArrayList<>();

    public void add(List<VideoInfo> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_select_video, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        VideoInfo item = list.get(position);
        holder.imageView.setImageBitmap(item.getBitmap());


        holder.textView.setText(VideoUtils.stringForTime(item.getDurationMs()));
        holder.checkBox.setOnCheckedChangeListener((compoundButton, b) -> {
            if (mClickListener != null)
                mClickListener.onItemClick(position, b);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView;
        CheckBox checkBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.imageView = itemView.findViewById(R.id.image);
            this.textView = itemView.findViewById(R.id.text);
            this.checkBox = itemView.findViewById(R.id.checkBox);
        }

    }

    private OnItemClickListener mClickListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mClickListener = listener;
    }

    public interface OnItemClickListener {
        public void onItemClick(int position, boolean isCheck);
    }

}

