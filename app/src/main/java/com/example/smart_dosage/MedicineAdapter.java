package com.example.smart_dosage;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.smart_dosage.data.Medicine;

import java.util.ArrayList;
import java.util.List;

public class MedicineAdapter extends RecyclerView.Adapter<MedicineAdapter.VH> {
    public interface OnScheduleClick {
        void onClick(Medicine medicine);
    }

    private List<Medicine> items = new ArrayList<>();
    private final OnScheduleClick onScheduleClick;

    public MedicineAdapter(OnScheduleClick listener) {
        this.onScheduleClick = listener;
    }

    public void submit(List<Medicine> list) {
        items = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medicine, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Medicine m = items.get(position);
        holder.name.setText("💊 " + m.name);
        holder.details.setText("🧪 " + (m.strength!=null?m.strength:"") + " • " + m.dosageAmount);
        holder.times.setText("⏱️ " + (m.times!=null?String.join(", ", m.times):""));
        if (m.photoUri != null && !m.photoUri.isEmpty()) {
            Glide.with(holder.photo.getContext()).load(m.photoUri).into(holder.photo);
        }
        holder.schedule.setOnClickListener(v -> onScheduleClick.onClick(m));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView photo;
        TextView name;
        TextView details;
        TextView times;
        View schedule;

        VH(@NonNull View itemView) {
            super(itemView);
            photo = itemView.findViewById(R.id.photo);
            name = itemView.findViewById(R.id.name);
            details = itemView.findViewById(R.id.details);
            times = itemView.findViewById(R.id.times);
            schedule = itemView.findViewById(R.id.btn_schedule);
        }
    }
}
