package com.example.inkturnejava;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.function.Consumer;

public class AvailabilityAdapter extends RecyclerView.Adapter<AvailabilityAdapter.AvailabilityViewHolder> {

    private final List<String> times;
    private final Consumer<String> onDeleteClick;

    public AvailabilityAdapter(List<String> times, Consumer<String> onDeleteClick) {
        this.times = times;
        this.onDeleteClick = onDeleteClick;
    }

    @NonNull
    @Override
    public AvailabilityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.availability_list_item, parent, false);
        return new AvailabilityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AvailabilityViewHolder holder, int position) {
        String time = times.get(position);
        holder.timeText.setText(time);
        holder.deleteButton.setOnClickListener(v -> onDeleteClick.accept(time));
    }

    @Override
    public int getItemCount() {
        return times.size();
    }

    public static class AvailabilityViewHolder extends RecyclerView.ViewHolder {
        TextView timeText;
        Button deleteButton;

        public AvailabilityViewHolder(@NonNull View itemView) {
            super(itemView);
            timeText = itemView.findViewById(R.id.availabilityTimeText);
            deleteButton = itemView.findViewById(R.id.deleteAvailabilityButton);
        }
    }
}