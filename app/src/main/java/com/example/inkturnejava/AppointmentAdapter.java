package com.example.inkturnejava;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder> {

    private final List<com.example.inkturnejava.Appointment> appointmentList;

    public AppointmentAdapter(List<com.example.inkturnejava.Appointment> appointmentList) {
        this.appointmentList = appointmentList;
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.appointment_list_item, parent, false);
        return new AppointmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        com.example.inkturnejava.Appointment appt = appointmentList.get(position);
        holder.timeText.setText(appt.getTimestamp());
        holder.messageText.setText(appt.getMessage());
        holder.detailText.setText("Szalon ID: " + appt.getSalonId());
        holder.statusText.setText("Állapot: " + (appt.getStatus() != null ? appt.getStatus() : "függőben"));

        if ("accepted".equals(appt.getStatus())) {
            holder.cancelButton.setVisibility(View.VISIBLE);
            holder.cancelButton.setOnClickListener(v -> {
                String documentId = appt.getSalonId() + "_" + encodeTime(appt.getTimestamp());

                FirebaseFirestore.getInstance()
                        .collection("appointments")
                        .document(documentId)
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(holder.itemView.getContext(), "Foglalás lemondva", Toast.LENGTH_SHORT).show();
                            appointmentList.remove(holder.getAdapterPosition());
                            notifyItemRemoved(holder.getAdapterPosition());
                        });
            });
        } else {
            holder.cancelButton.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return appointmentList.size();
    }

    private String encodeTime(String time) {
        return time.replace(":", "-").replace(" ", "_");
    }

    public static class AppointmentViewHolder extends RecyclerView.ViewHolder {
        TextView timeText, messageText, detailText, statusText;
        Button cancelButton;

        public AppointmentViewHolder(@NonNull View itemView) {
            super(itemView);
            timeText = itemView.findViewById(R.id.appointmentTimeText);
            messageText = itemView.findViewById(R.id.appointmentMessageText);
            detailText = itemView.findViewById(R.id.appointmentDetailText);
            statusText = itemView.findViewById(R.id.appointmentStatusText);
            cancelButton = itemView.findViewById(R.id.cancelAppointmentButton);
        }
    }
}