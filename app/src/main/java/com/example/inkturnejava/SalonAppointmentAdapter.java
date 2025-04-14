package com.example.inkturnejava;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inkturnejava.Appointment;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.function.BiConsumer;

public class SalonAppointmentAdapter extends RecyclerView.Adapter<SalonAppointmentAdapter.AppointmentViewHolder> {

    private List<Appointment> appointments;
    private BiConsumer<Appointment, String> onStatusChange;

    public SalonAppointmentAdapter(List<Appointment> appointments,
                                   BiConsumer<Appointment, String> onStatusChange) {
        this.appointments = appointments;
        this.onStatusChange = onStatusChange;
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.appointment_list_item_salon, parent, false);
        return new AppointmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        Appointment appt = appointments.get(position);
        holder.timeText.setText(appt.getTimestamp());
        holder.messageText.setText(appt.getMessage());
        holder.userEmailText.setText(appt.getUserEmail());
        holder.statusText.setText("Státusz: " + (appt.getStatus() != null ? appt.getStatus() : "pending"));

        if ("accepted".equals(appt.getStatus())) {
            holder.acceptButton.setVisibility(View.GONE);
            holder.rejectButton.setVisibility(View.GONE);
            holder.cancelButton.setVisibility(View.VISIBLE);

            holder.cancelButton.setOnClickListener(v -> {
                String documentId = appt.getSalonId() + "_" + encodeTime(appt.getTimestamp());

                FirebaseFirestore.getInstance()
                        .collection("appointments")
                        .document(documentId)
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(holder.itemView.getContext(), "Foglalás lemondva", Toast.LENGTH_SHORT).show();
                            appointments.remove(holder.getAdapterPosition());
                            notifyItemRemoved(holder.getAdapterPosition());
                        });
            });
        } else {
            holder.acceptButton.setVisibility(View.VISIBLE);
            holder.rejectButton.setVisibility(View.VISIBLE);
            holder.cancelButton.setVisibility(View.GONE);

            holder.acceptButton.setOnClickListener(v -> onStatusChange.accept(appt, "accepted"));
            holder.rejectButton.setOnClickListener(v -> onStatusChange.accept(appt, "rejected"));
        }
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    private String encodeTime(String time) {
        return time.replace(":", "-").replace(" ", "_");
    }

    public static class AppointmentViewHolder extends RecyclerView.ViewHolder {
        TextView timeText, messageText, userEmailText, statusText;
        Button acceptButton, rejectButton, cancelButton;

        public AppointmentViewHolder(@NonNull View itemView) {
            super(itemView);
            timeText = itemView.findViewById(R.id.appointmentTimeText);
            messageText = itemView.findViewById(R.id.appointmentMessageText);
            userEmailText = itemView.findViewById(R.id.appointmentUserEmailText);
            statusText = itemView.findViewById(R.id.appointmentStatusText);
            acceptButton = itemView.findViewById(R.id.acceptAppointmentButton);
            rejectButton = itemView.findViewById(R.id.rejectAppointmentButton);
            cancelButton = itemView.findViewById(R.id.cancelAppointmentButton);
        }
    }
}