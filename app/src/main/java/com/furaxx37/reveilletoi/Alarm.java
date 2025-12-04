package com.furaxx37.reveilletoi;

import java.io.Serializable;

public class Alarm implements Serializable {
    private long id;
    private int hour;
    private int minute;
    private String label;
    private boolean enabled;
    private String ringtoneUri;
    private long createdAt;

    public Alarm() {
        this.createdAt = System.currentTimeMillis();
        this.enabled = true;
        this.label = "";
        this.ringtoneUri = "";
    }

    public Alarm(int hour, int minute, String label) {
        this();
        this.hour = hour;
        this.minute = minute;
        this.label = label;
    }

    public Alarm(int hour, int minute, String label, boolean enabled, String ringtoneUri) {
        this();
        this.hour = hour;
        this.minute = minute;
        this.label = label;
        this.enabled = enabled;
        this.ringtoneUri = ringtoneUri;
    }

    // Getters
    public long getId() {
        return id;
    }

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }

    public String getLabel() {
        return label;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getRingtoneUri() {
        return ringtoneUri;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    // Setters
    public void setId(long id) {
        this.id = id;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setRingtoneUri(String ringtoneUri) {
        this.ringtoneUri = ringtoneUri;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    // Utility methods
    public String getFormattedTime() {
        return String.format("%02d:%02d", hour, minute);
    }

    public String getTimeInMillis() {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.set(java.util.Calendar.HOUR_OF_DAY, hour);
        calendar.set(java.util.Calendar.MINUTE, minute);
        calendar.set(java.util.Calendar.SECOND, 0);
        calendar.set(java.util.Calendar.MILLISECOND, 0);
        
        // If the time has already passed today, schedule for tomorrow
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(java.util.Calendar.DAY_OF_MONTH, 1);
        }
        
        return String.valueOf(calendar.getTimeInMillis());
    }

    @Override
    public String toString() {
        return "Alarm{" +
                "id=" + id +
                ", time=" + getFormattedTime() +
                ", label='" + label + '\'' +
                ", enabled=" + enabled +
                '}';
    }
}
