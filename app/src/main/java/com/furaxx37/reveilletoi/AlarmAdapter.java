package com.furaxx37.reveilletoi;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.material.switchmaterial.SwitchMaterial;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder> {
    
    private List<Alarm> alarms;
    private OnAlarmInteractionListener listener;

    public interface OnAlarmInteractionListener {
        void onAlarmToggle(Alarm alarm, boolean enabled);
        void onAlarmDelete(Alarm alarm);
        void onAlarmEdit(Alarm alarm);
    }

    public AlarmAdapter(List<Alarm> alarms, OnAlarmInteractionListener listener) {
        this.alarms = alarms;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AlarmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.alarm_list_item, parent, false);
        return new AlarmViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlarmViewHolder holder, int position) {
        Alarm alarm = alarms.get(position);
        holder.bind(alarm);
    }

    @Override
    public int getItemCount() {
        return alarms.size();
    }

    public void updateAlarms(List<Alarm> newAlarms) {
        this.alarms = newAlarms;
        notifyDataSetChanged();
    }

    public void addAlarm(Alarm alarm) {
        alarms.add(alarm);
        notifyItemInserted(alarms.size() - 1);
    }

    public void removeAlarm(int position) {
        if (position >= 0 && position < alarms.size()) {
            alarms.remove(position);
            notifyItemRemoved(position);
        }
    }

    class AlarmViewHolder extends RecyclerView.ViewHolder {
        private TextView timeText;
        private TextView labelText;
        private SwitchMaterial alarmSwitch;
        private View deleteButton;

        public AlarmViewHolder(@NonNull View itemView) {
            super(itemView);
            timeText = itemView.findViewById(R.id.alarm_time);
            labelText = itemView.findViewById(R.id.alarm_label);
            alarmSwitch = itemView.findViewById(R.id.switchAlarmEnabled);
            deleteButton = itemView.findViewById(R.id.buttonDeleteAlarm);
        }

        public void bind(Alarm alarm) {
            timeText.setText(alarm.getFormattedTime());
            labelText.setText(alarm.getLabel().isEmpty() ? "Alarme" : alarm.getLabel());
            alarmSwitch.setChecked(alarm.isEnabled());

            // Set alpha based on enabled state
            float alpha = alarm.isEnabled() ? 1.0f : 0.5f;
            timeText.setAlpha(alpha);
            labelText.setAlpha(alpha);

            // Switch listener
            alarmSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onAlarmToggle(alarm, isChecked);
                }
                // Update alpha immediately
                float newAlpha = isChecked ? 1.0f : 0.5f;
                timeText.setAlpha(newAlpha);
                labelText.setAlpha(newAlpha);
            });

            // Delete button listener
            deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAlarmDelete(alarm);
                }
            });

            // Edit alarm on item click
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAlarmEdit(alarm);
                }
            });
        }
    }
}
