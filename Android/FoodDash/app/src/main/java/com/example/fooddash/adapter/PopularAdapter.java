package com.example.fooddash.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fooddash.FoodDetails;
import com.example.fooddash.R;
import com.example.fooddash.model.Popular;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class PopularAdapter extends RecyclerView.Adapter<PopularAdapter.PopularViewHolder> {

    private Context context;
    private List<Popular> popularList;

    public PopularAdapter(Context context, List<Popular> popularList) {
        this.context = context;
        this.popularList = popularList;
    }

    @NonNull
    @Override
    public PopularViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.popular, parent, false);

        return new PopularViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PopularViewHolder holder, final int position) {

        // Load image asynchronously with Picasso
        Picasso.get()
                .load(popularList.get(position).getImageUrl())
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error_image)
                .into(holder.popularImage);

        // Set text data
        holder.popularName.setText(popularList.get(position).getName());
        holder.popularPrice.setText(popularList.get(position).getPrice());
        holder.popularRating.setText(popularList.get(position).getRating());

        // Handle item click
        holder.itemView.setOnClickListener(view -> {
            Intent i = new Intent(context, FoodDetails.class);
            i.putExtra("name", popularList.get(position).getName());
            i.putExtra("price", popularList.get(position).getPrice());
            i.putExtra("rating", popularList.get(position).getRating());
            i.putExtra("description", popularList.get(position).getNote());
            i.putExtra("image", popularList.get(position).getImageUrl());

            context.startActivity(i);
        });
    }

    @Override
    public int getItemCount() {
        return popularList.size();
    }

    public  static class PopularViewHolder extends RecyclerView.ViewHolder{

        ImageView popularImage;
        TextView popularName,popularRating,popularPrice;

        public PopularViewHolder(@NonNull View itemView) {
            super(itemView);

            popularName = itemView.findViewById(R.id.popular_name);
            popularImage = itemView.findViewById(R.id.popular_img);
            popularRating = itemView.findViewById(R.id.popular_rating);
            popularPrice = itemView.findViewById(R.id.popular_price);

        }
    }
}
