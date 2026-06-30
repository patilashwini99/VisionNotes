package com.example.visionnotes;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.ViewHolder> {

    public interface OnItemCheckedListener {
        void onItemChecked(SettingItem item, boolean isChecked);
    }

    private final List<SettingItem> items;
    private final Context context;
    private final OnItemCheckedListener listener;

    public SettingsAdapter(Context context, List<SettingItem> items, OnItemCheckedListener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_settings, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SettingItem item = items.get(position);

        holder.tvTitle.setText(item.getTitle());

        if (item.isSwitchable()) {
            holder.switchView.setVisibility(View.VISIBLE);
            holder.switchView.setOnCheckedChangeListener(null);
            holder.switchView.setChecked(item.isChecked());

            holder.switchView.setOnCheckedChangeListener((buttonView, isChecked) -> {
                item.setChecked(isChecked);
                if (listener != null) listener.onItemChecked(item, isChecked);
            });
        } else {
            holder.switchView.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (!item.isSwitchable() && "About".equals(item.getTitle())) {
                Toast.makeText(context, "Notes App v1.0", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        Switch switchView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvOption);
            switchView = itemView.findViewById(R.id.switchOption);
        }
    }
}