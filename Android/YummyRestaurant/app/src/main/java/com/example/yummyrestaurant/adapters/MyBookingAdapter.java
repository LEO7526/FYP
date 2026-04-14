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
import com.example.yummyrestaurant.utils.AnimationUtils;

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
        AnimationUtils.animateItemEntry(holder.itemView, position);

        android.content.Context context = holder.itemView.getContext();

        holder.textBookingId.setText(context.getString(R.string.booking_id_format, booking.getBid()));
        holder.textDateTime.setText(booking.getBookingDate() + " " + booking.getBookingTime());
        holder.textTableAndPeople.setText(context.getString(R.string.table_people_format, booking.getTableId(), booking.getPeopleCount()));

        if (booking.getPurpose() != null && !booking.getPurpose().trim().isEmpty()) {
            holder.textPurpose.setVisibility(View.VISIBLE);
            String localizedPurpose = getLocalizedPurpose(context, booking.getPurpose());
            holder.textPurpose.setText(context.getString(R.string.purpose_format, localizedPurpose));
        } else {
            holder.textPurpose.setVisibility(View.GONE);
        }

        if (booking.getRemark() != null && !booking.getRemark().trim().isEmpty()) {
            holder.textRemark.setVisibility(View.VISIBLE);
            holder.textRemark.setText(context.getString(R.string.remark_format, booking.getRemark()));
        } else {
            holder.textRemark.setVisibility(View.GONE);
        }

        if (booking.isCancelled()) {
            holder.textStatus.setText(context.getString(R.string.status_cancelled));
            holder.buttonCancelBooking.setEnabled(false);
            holder.buttonCancelBooking.setText(context.getString(R.string.cancelled));
        } else {
            holder.textStatus.setText(context.getString(R.string.status_active));
            holder.buttonCancelBooking.setEnabled(true);
            holder.buttonCancelBooking.setText(context.getString(R.string.cancel_booking));
            holder.buttonCancelBooking.setOnClickListener(v -> onCancelClickListener.onCancelClick(booking));
        }
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    private String getLocalizedPurpose(android.content.Context context, String rawPurpose) {
        String normalized = rawPurpose == null ? "" : rawPurpose.trim();
        if (normalized.isEmpty()) {
            return rawPurpose;
        }

        switch (normalized.toLowerCase()) {
            case "family dinner":
                return context.getString(R.string.purpose_value_family_dinner);
            case "date night":
                return context.getString(R.string.purpose_value_date_night);
            case "business meeting":
                return context.getString(R.string.purpose_value_business_meeting);
            case "lunch meeting":
                return context.getString(R.string.purpose_value_lunch_meeting);
            case "birthday celebration":
                return context.getString(R.string.purpose_value_birthday_celebration);
            default:
                return rawPurpose;
        }
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
