package com.example.mhikeapplication.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.mhikeapplication.R;
import com.example.mhikeapplication.models.Hike;
import java.util.List;

public class UpcomingHikesAdapter extends RecyclerView.Adapter<UpcomingHikesAdapter.UpcomingHikeViewHolder> {

    private List<Hike> upcomingHikes;

    public UpcomingHikesAdapter(List<Hike> upcomingHikes) {
        this.upcomingHikes = upcomingHikes;
    }

    @NonNull
    @Override
    public UpcomingHikeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_upcoming_hike, parent, false);
        return new UpcomingHikeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UpcomingHikeViewHolder holder, int position) {
        Hike hike = upcomingHikes.get(position);
        holder.hikeName.setText(hike.getName());
        holder.hikeDate.setText(hike.getDate());

        if (hike.getCoverPhotoPath() != null && !hike.getCoverPhotoPath().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                .load(hike.getCoverPhotoPath())
                .into(holder.coverImage);
        } else {
            holder.coverImage.setImageResource(R.drawable.default_hike_image);
        }
    }

    @Override
    public int getItemCount() {
        return upcomingHikes.size();
    }

    static class UpcomingHikeViewHolder extends RecyclerView.ViewHolder {
        ImageView coverImage;
        TextView hikeName;
        TextView hikeDate;

        public UpcomingHikeViewHolder(@NonNull View itemView) {
            super(itemView);
            coverImage = itemView.findViewById(R.id.imageViewCover);
            hikeName = itemView.findViewById(R.id.textViewHikeName);
            hikeDate = itemView.findViewById(R.id.textViewHikeDate);
        }
    }
}
