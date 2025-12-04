package com.furaxx37.reveilletoi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {
    
    private static final String TAG = "AlarmReceiver";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Alarm received!");
        
        // Get alarm data from intent
        long alarmId = intent.getLongExtra("alarm_id", -1);
        String alarmLabel = intent.getStringExtra("alarm_label");
        String ringtoneUri = intent.getStringExtra("ringtone_uri");
        
        Log.d(TAG, "Alarm ID: " + alarmId + ", Label: " + alarmLabel);
        
        // Start the alarm service to handle the alarm
        Intent serviceIntent = new Intent(context, AlarmService.class);
        serviceIntent.putExtra("alarm_id", alarmId);
        serviceIntent.putExtra("alarm_label", alarmLabel);
        serviceIntent.putExtra("ringtone_uri", ringtoneUri);
        
        // Start as foreground service for Android 8.0+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
        
        // Also start the alarm activity to show the alarm screen
        Intent alarmActivityIntent = new Intent(context, AlarmActivity.class);
        alarmActivityIntent.putExtra("alarm_id", alarmId);
        alarmActivityIntent.putExtra("alarm_label", alarmLabel);
        alarmActivityIntent.putExtra("ringtone_uri", ringtoneUri);
        alarmActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | 
                                   Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                   Intent.FLAG_ACTIVITY_SINGLE_TOP);
        
        context.startActivity(alarmActivityIntent);
    }
}
