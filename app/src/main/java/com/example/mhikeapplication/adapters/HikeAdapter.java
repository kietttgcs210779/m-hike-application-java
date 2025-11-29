package com.example.mhikeapplication.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mhikeapplication.R;
import com.example.mhikeapplication.models.Hike;
import java.util.List;

public class HikeAdapter extends RecyclerView.Adapter<HikeAdapter.HikeViewHolder> {

    private List<Hike> hikeList;
    private final OnHikeClickListener onHikeClickListener;

    public HikeAdapter(List<Hike> hikeList, OnHikeClickListener onHikeClickListener) {
        this.hikeList = hikeList;
        this.onHikeClickListener = onHikeClickListener;
    }

    @NonNull
    @Override
    public HikeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hike, parent, false);
        return new HikeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HikeViewHolder holder, int position) {
        Hike hike = hikeList.get(position);
        holder.bind(hike, onHikeClickListener);
    }

    @Override
    public int getItemCount() {
        return hikeList.size();
    }

    public void updateData(List<Hike> newHikeList) {
        this.hikeList.clear();
        this.hikeList.addAll(newHikeList);
        notifyDataSetChanged();
    }

    static class HikeViewHolder extends RecyclerView.ViewHolder {
        TextView hikeName, hikeLocation, hikeDate;

        public HikeViewHolder(@NonNull View itemView) {
            super(itemView);
            hikeName = itemView.findViewById(R.id.textViewItemHikeName);
            hikeLocation = itemView.findViewById(R.id.textViewItemHikeLocation);
            hikeDate = itemView.findViewById(R.id.textViewItemHikeDate);
        }

        public void bind(final Hike hike, final OnHikeClickListener listener) {
            hikeName.setText(hike.getName());
            hikeLocation.setText(hike.getLocation());
            hikeDate.setText(hike.getDate());
            itemView.setOnClickListener(v -> listener.onHikeClick(hike));
        }
    }
}
