package com.example.mhikeapplication.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.mhikeapplication.R;
import com.example.mhikeapplication.models.HikeImage;
import java.util.List;

public class HikeImageAdapter extends RecyclerView.Adapter<HikeImageAdapter.HikeImageViewHolder> {

    private List<HikeImage> imageList;

    public HikeImageAdapter(List<HikeImage> imageList) {
        this.imageList = imageList;
    }

    @NonNull
    @Override
    public HikeImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hike_image, parent, false);
        return new HikeImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HikeImageViewHolder holder, int position) {
        HikeImage hikeImage = imageList.get(position);
        String imagePath = hikeImage.getImagePath();

        if (imagePath != null && !imagePath.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(imagePath)
                    .into(holder.imageView);
        }
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    static class HikeImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public HikeImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageViewHikeImage);
        }
    }
}
