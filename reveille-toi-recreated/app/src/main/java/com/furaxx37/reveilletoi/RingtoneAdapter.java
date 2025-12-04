package com.furaxx37.reveilletoi;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RingtoneAdapter extends RecyclerView.Adapter<RingtoneAdapter.RingtoneViewHolder> {
    
    private List<RingtoneSelectionActivity.RingtoneItem> ringtones;
    private OnRingtoneClickListener listener;
    private int selectedPosition = -1;

    public interface OnRingtoneClickListener {
        void onRingtoneClick(RingtoneSelectionActivity.RingtoneItem ringtone, int position);
    }

    public RingtoneAdapter(List<RingtoneSelectionActivity.RingtoneItem> ringtones, OnRingtoneClickListener listener) {
        this.ringtones = ringtones;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RingtoneViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ringtone_list_item, parent, false);
        return new RingtoneViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RingtoneViewHolder holder, int position) {
        RingtoneSelectionActivity.RingtoneItem ringtone = ringtones.get(position);
        
        holder.textTitle.setText(ringtone.getDisplayTitle());
        holder.radioButton.setChecked(position == selectedPosition);
        
        // Set click listener for the entire item
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION && listener != null) {
                    setSelectedPosition(adapterPosition);
                    listener.onRingtoneClick(ringtones.get(adapterPosition), adapterPosition);
                }
            }
        });
        
        // Set click listener for radio button
        holder.radioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION && listener != null) {
                    setSelectedPosition(adapterPosition);
                    listener.onRingtoneClick(ringtones.get(adapterPosition), adapterPosition);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return ringtones != null ? ringtones.size() : 0;
    }

    public void setSelectedPosition(int position) {
        int previousPosition = selectedPosition;
        selectedPosition = position;
        
        // Notify changes for both old and new positions
        if (previousPosition != -1) {
            notifyItemChanged(previousPosition);
        }
        if (selectedPosition != -1) {
            notifyItemChanged(selectedPosition);
        }
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public RingtoneSelectionActivity.RingtoneItem getSelectedRingtone() {
        if (selectedPosition >= 0 && selectedPosition < ringtones.size()) {
            return ringtones.get(selectedPosition);
        }
        return null;
    }

    static class RingtoneViewHolder extends RecyclerView.ViewHolder {
        TextView textTitle;
        RadioButton radioButton;

        public RingtoneViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.text_ringtone_title);
            radioButton = itemView.findViewById(R.id.radio_ringtone);
        }
    }
}
