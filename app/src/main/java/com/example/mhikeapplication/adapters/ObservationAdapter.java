package com.example.mhikeapplication.adapters;

import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mhikeapplication.R;
import com.example.mhikeapplication.models.HikeObservation;
import java.util.List;

public class ObservationAdapter extends RecyclerView.Adapter<ObservationAdapter.ObservationViewHolder> {

    private List<HikeObservation> observationList;
    private final OnObservationListener listener;

    public interface OnObservationListener {
        void onEditClick(HikeObservation observation);
        void onDeleteClick(HikeObservation observation);
    }

    public ObservationAdapter(List<HikeObservation> observationList, OnObservationListener listener) {
        this.observationList = observationList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ObservationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_observation, parent, false);
        return new ObservationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ObservationViewHolder holder, int position) {
        HikeObservation observation = observationList.get(position);
        holder.bind(observation, listener);
    }

    @Override
    public int getItemCount() {
        return observationList.size();
    }

    static class ObservationViewHolder extends RecyclerView.ViewHolder {
        TextView content, time, additionalNotes;
        ImageButton menuButton;
        Context context;

        public ObservationViewHolder(@NonNull View itemView) {
            super(itemView);
            context = itemView.getContext();
            content = itemView.findViewById(R.id.textViewObservationContent);
            time = itemView.findViewById(R.id.textViewObservationTime);
            additionalNotes = itemView.findViewById(R.id.textViewObservationNotes);
            menuButton = itemView.findViewById(R.id.button_menu);
        }

        public void bind(final HikeObservation observation, final OnObservationListener listener) {
            content.setText(observation.getObservationContent());
            time.setText(observation.getTimeOfObservation());

            String notes = observation.getObservationNotes();
            if (notes != null && !notes.isEmpty()) {
                additionalNotes.setText(notes);
                additionalNotes.setVisibility(View.VISIBLE);
            } else {
                additionalNotes.setVisibility(View.GONE);
            }

            menuButton.setOnClickListener(view -> {
                Context wrapper = new ContextThemeWrapper(context, R.style.App_PopupMenu);
                PopupMenu popup = new PopupMenu(wrapper, view);
                popup.inflate(R.menu.observation_item_menu);
                popup.setOnMenuItemClickListener(item -> {
                    int itemId = item.getItemId();
                    if (itemId == R.id.action_edit_observation) {
                        listener.onEditClick(observation);
                        return true;
                    } else if (itemId == R.id.action_delete_observation) {
                        listener.onDeleteClick(observation);
                        return true;
                    } else {
                        return false;
                    }
                });
                popup.show();
            });
        }
    }
}
