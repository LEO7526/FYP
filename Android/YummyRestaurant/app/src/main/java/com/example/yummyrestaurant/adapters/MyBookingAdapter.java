package com.example.yummyrestaurant.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.models.MyBooking;

import java.util.List;

public class MyBookingAdapter extends RecyclerView.Adapter<MyBookingAdapter.BookingViewHolder> {

    public interface OnCancelClickListener {
        void onCancelClick(MyBooking booking);
    }

    private final List<MyBooking> bookings;
    private final OnCancelClickListener onCancelClickListener;

    public MyBookingAdapter(List<MyBooking> bookings, OnCancelClickListener onCancelClickListener) {
        this.bookings = bookings;
        this.onCancelClickListener = onCancelClickListener;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        MyBooking booking = bookings.get(position);

        holder.textBookingId.setText("Booking #" + booking.getBid());
        holder.textDateTime.setText(booking.getBookingDate() + " " + booking.getBookingTime());
        holder.textTableAndPeople.setText("Table " + booking.getTableId() + " • " + booking.getPeopleCount() + " people");

        if (booking.getPurpose() != null && !booking.getPurpose().trim().isEmpty()) {
            holder.textPurpose.setVisibility(View.VISIBLE);
            holder.textPurpose.setText("Purpose: " + booking.getPurpose());
        } else {
            holder.textPurpose.setVisibility(View.GONE);
        }

        if (booking.getRemark() != null && !booking.getRemark().trim().isEmpty()) {
            holder.textRemark.setVisibility(View.VISIBLE);
            holder.textRemark.setText("Remark: " + booking.getRemark());
        } else {
            holder.textRemark.setVisibility(View.GONE);
        }

        if (booking.isCancelled()) {
            holder.textStatus.setText("Status: Cancelled");
            holder.buttonCancelBooking.setEnabled(false);
            holder.buttonCancelBooking.setText("Cancelled");
        } else {
            holder.textStatus.setText("Status: Active");
            holder.buttonCancelBooking.setEnabled(true);
            holder.buttonCancelBooking.setText("Cancel Booking");
            holder.buttonCancelBooking.setOnClickListener(v -> onCancelClickListener.onCancelClick(booking));
        }
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView textBookingId;
        TextView textDateTime;
        TextView textTableAndPeople;
        TextView textPurpose;
        TextView textRemark;
        TextView textStatus;
        Button buttonCancelBooking;

        BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            textBookingId = itemView.findViewById(R.id.textBookingId);
            textDateTime = itemView.findViewById(R.id.textDateTime);
            textTableAndPeople = itemView.findViewById(R.id.textTableAndPeople);
            textPurpose = itemView.findViewById(R.id.textPurpose);
            textRemark = itemView.findViewById(R.id.textRemark);
            textStatus = itemView.findViewById(R.id.textStatus);
            buttonCancelBooking = itemView.findViewById(R.id.buttonCancelBooking);
        }
    }
}
