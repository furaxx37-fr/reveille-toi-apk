package com.furaxx37.reveilletoi;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import java.util.Calendar;

public class AlarmManager {
    
    private static final String TAG = "AlarmManager";
    private Context context;
    private android.app.AlarmManager systemAlarmManager;

    public AlarmManager(Context context) {
        this.context = context;
        this.systemAlarmManager = (android.app.AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    public void setAlarm(Alarm alarm) {
        if (alarm == null || !alarm.isEnabled()) {
            Log.w(TAG, "Alarm is null or disabled, not setting");
            return;
        }

        try {
            // Create intent for AlarmReceiver
            Intent intent = new Intent(context, AlarmReceiver.class);
            intent.putExtra("alarm_id", alarm.getId());
            intent.putExtra("alarm_label", alarm.getLabel());
            intent.putExtra("ringtone_uri", alarm.getRingtoneUri());

            // Create PendingIntent
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    (int) alarm.getId(), // Use alarm ID as request code
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Calculate alarm time
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, alarm.getHour());
            calendar.set(Calendar.MINUTE, alarm.getMinute());
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            // If the time has already passed today, schedule for tomorrow
            if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }

            long triggerTime = calendar.getTimeInMillis();

            // Set the alarm
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                // For Android 6.0+ use setExactAndAllowWhileIdle for better reliability
                systemAlarmManager.setExactAndAllowWhileIdle(
                        android.app.AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                );
            } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                // For Android 4.4+ use setExact
                systemAlarmManager.setExact(
                        android.app.AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                );
            } else {
                // For older versions use set
                systemAlarmManager.set(
                        android.app.AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                );
            }

            Log.d(TAG, "Alarm set for " + alarm.getFormattedTime() + 
                      " (ID: " + alarm.getId() + ")");
            Log.d(TAG, "Trigger time: " + new java.util.Date(triggerTime));

        } catch (Exception e) {
            Log.e(TAG, "Error setting alarm", e);
        }
    }

    public void cancelAlarm(Alarm alarm) {
        if (alarm == null) {
            Log.w(TAG, "Alarm is null, cannot cancel");
            return;
        }

        try {
            // Create the same intent used when setting the alarm
            Intent intent = new Intent(context, AlarmReceiver.class);
            intent.putExtra("alarm_id", alarm.getId());
            intent.putExtra("alarm_label", alarm.getLabel());
            intent.putExtra("ringtone_uri", alarm.getRingtoneUri());

            // Create PendingIntent with same parameters
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    (int) alarm.getId(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Cancel the alarm
            systemAlarmManager.cancel(pendingIntent);
            
            Log.d(TAG, "Alarm cancelled for " + alarm.getFormattedTime() + 
                      " (ID: " + alarm.getId() + ")");

        } catch (Exception e) {
            Log.e(TAG, "Error cancelling alarm", e);
        }
    }

    public void updateAlarm(Alarm alarm) {
        // Cancel existing alarm and set new one
        cancelAlarm(alarm);
        if (alarm.isEnabled()) {
            setAlarm(alarm);
        }
    }

    public void toggleAlarm(Alarm alarm, boolean enabled) {
        alarm.setEnabled(enabled);
        if (enabled) {
            setAlarm(alarm);
        } else {
            cancelAlarm(alarm);
        }
    }

    /**
     * Check if exact alarm permission is granted (Android 12+)
     */
    public boolean canScheduleExactAlarms() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            return systemAlarmManager.canScheduleExactAlarms();
        }
        return true; // Always true for older versions
    }

    /**
     * Request exact alarm permission (Android 12+)
     */
    public void requestExactAlarmPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (!systemAlarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        }
    }
}
