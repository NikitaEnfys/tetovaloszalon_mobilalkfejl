package com.example.inkturnejava;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.inkturnejava.R;
import com.example.inkturnejava.Salon;
import java.util.List;
import java.util.stream.Collectors;

public class SalonAdapter extends RecyclerView.Adapter<SalonAdapter.SalonViewHolder> {
    private List<Salon> fullList;
    private List<Salon> filteredList;
    private List<String> documentIds;

    public SalonAdapter(List<Salon> salons, List<String> docIds) {
        this.fullList = salons;
        this.filteredList = salons;
        this.documentIds = docIds;
    }

    public void filter(String query) {
        if (query == null || query.trim().isEmpty()) {
            filteredList = fullList;
        } else {
            String lower = query.toLowerCase();
            filteredList = fullList.stream()
                    .filter(s -> s.getName().toLowerCase().contains(lower) ||
                            s.getAddress().toLowerCase().contains(lower))
                    .collect(Collectors.toList());
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SalonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.salon_list_item, parent, false);
        return new SalonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SalonViewHolder holder, int position) {
        Salon salon = filteredList.get(position);
        String docId = documentIds.get(position);

        holder.nameText.setText(salon.getName());
        holder.addressText.setText(salon.getAddress());
        holder.descriptionText.setText(salon.getDescription());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), SalonDetailsActivity.class);
            intent.putExtra("salonId", docId);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public static class SalonViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, addressText, descriptionText;

        public SalonViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.salonNameText);
            addressText = itemView.findViewById(R.id.salonAddressText);
            descriptionText = itemView.findViewById(R.id.salonDescriptionText);
        }
    }
}
