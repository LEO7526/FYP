package com.example.yummyrestaurant.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.models.Booking;

import java.util.List;

/**
 * Adapter for displaying booking list
 */
public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {
    private Context context;
    private List<Booking> bookingList;
    private OnBookingClickListener listener;

    public interface OnBookingClickListener {
        void onBookingClick(Booking booking);
    }

    public BookingAdapter(Context context, List<Booking> bookingList, OnBookingClickListener listener) {
        this.context = context;
        this.bookingList = bookingList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookingList.get(position);
        
        holder.textTableNumber.setText(String.format("Table #%d", booking.getTid()));
        holder.textCustomerName.setText(booking.getBkcname());
        holder.textBookingTime.setText(booking.getBtime());
        holder.textPeople.setText(String.format("%d person(s)", booking.getPnum()));
        holder.textStatus.setText(booking.getStatusText());
        
        // Set status color
        int statusColor;
        switch (booking.getStatus()) {
            case 1: // Pending
                statusColor = context.getResources().getColor(R.color.status_pending);
                break;
            case 2: // Done/Checked-in
                statusColor = context.getResources().getColor(R.color.status_done);
                break;
            default:
                statusColor = context.getResources().getColor(android.R.color.darker_gray);
        }
        holder.textStatus.setTextColor(statusColor);
        
        holder.itemView.setOnClickListener(v -> listener.onBookingClick(booking));
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    public static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView textTableNumber;
        TextView textCustomerName;
        TextView textBookingTime;
        TextView textPeople;
        TextView textStatus;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            textTableNumber = itemView.findViewById(R.id.textTableNumber);
            textCustomerName = itemView.findViewById(R.id.textCustomerName);
            textBookingTime = itemView.findViewById(R.id.textBookingTime);
            textPeople = itemView.findViewById(R.id.textPeople);
            textStatus = itemView.findViewById(R.id.textStatus);
        }
    }
}
