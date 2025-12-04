package com.furaxx37.reveilletoi;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import java.io.IOException;

public class AlarmService extends Service {
    
    private static final String TAG = "AlarmService";
    private static final String CHANNEL_ID = "alarm_channel";
    private static final int NOTIFICATION_ID = 1;
    
    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;
    private PowerManager.WakeLock wakeLock;
    
    private long alarmId;
    private String alarmLabel;
    private String ringtoneUri;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "AlarmService created");
        
        // Create notification channel for Android 8.0+
        createNotificationChannel();
        
        // Get vibrator service
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        
        // Acquire wake lock to keep device awake
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ReveilToi:AlarmWakeLock");
        wakeLock.acquire(10 * 60 * 1000L); // 10 minutes max
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "AlarmService started");
        
        if (intent != null) {
            alarmId = intent.getLongExtra("alarm_id", -1);
            alarmLabel = intent.getStringExtra("alarm_label");
            ringtoneUri = intent.getStringExtra("ringtone_uri");
            
            // Start foreground service with notification
            startForeground(NOTIFICATION_ID, createNotification());
            
            // Start playing alarm
            startAlarm();
        }
        
        return START_NOT_STICKY;
    }

    private void startAlarm() {
        try {
            // Start vibration
            if (vibrator != null && vibrator.hasVibrator()) {
                // Vibrate pattern: wait 0ms, vibrate 1000ms, wait 1000ms, repeat
                long[] pattern = {0, 1000, 1000};
                vibrator.vibrate(pattern, 0);
            }
            
            // Start playing ringtone
            playRingtone();
            
        } catch (Exception e) {
            Log.e(TAG, "Error starting alarm", e);
        }
    }

    private void playRingtone() {
        try {
            mediaPlayer = new MediaPlayer();
            
            // Set audio attributes for alarm
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build();
                mediaPlayer.setAudioAttributes(audioAttributes);
            } else {
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
            }
            
            // Set data source
            if (ringtoneUri != null && !ringtoneUri.isEmpty()) {
                mediaPlayer.setDataSource(this, Uri.parse(ringtoneUri));
            } else {
                // Use default alarm sound
                Uri defaultAlarmUri = android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI;
                if (defaultAlarmUri != null) {
                    mediaPlayer.setDataSource(this, defaultAlarmUri);
                } else {
                    // Fallback to notification sound
                    Uri defaultUri = android.provider.Settings.System.DEFAULT_NOTIFICATION_URI;
                    mediaPlayer.setDataSource(this, defaultUri);
                }
            }
            
            mediaPlayer.setLooping(true);
            mediaPlayer.prepare();
            mediaPlayer.start();
            
            Log.d(TAG, "Ringtone started playing");
            
        } catch (IOException e) {
            Log.e(TAG, "Error playing ringtone", e);
            // Try to play default system alarm
            playDefaultAlarm();
        }
    }

    private void playDefaultAlarm() {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }
            mediaPlayer = MediaPlayer.create(this, android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI);
            if (mediaPlayer != null) {
                mediaPlayer.setLooping(true);
                mediaPlayer.start();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error playing default alarm", e);
        }
    }

    public void stopAlarm() {
        Log.d(TAG, "Stopping alarm");
        
        // Stop media player
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
        
        // Stop vibration
        if (vibrator != null) {
            vibrator.cancel();
        }
        
        // Release wake lock
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
        
        // Stop foreground service
        stopForeground(true);
        stopSelf();
    }

    private Notification createNotification() {
        Intent stopIntent = new Intent(this, AlarmActivity.class);
        stopIntent.putExtra("alarm_id", alarmId);
        stopIntent.putExtra("alarm_label", alarmLabel);
        stopIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, stopIntent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        String title = alarmLabel != null && !alarmLabel.isEmpty() ? alarmLabel : "Alarme";
        
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText("Touchez pour arrêter l'alarme")
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentIntent(pendingIntent)
                .setAutoCancel(false)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Alarmes",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Canal pour les notifications d'alarme");
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "AlarmService destroyed");
        stopAlarm();
    }
}
