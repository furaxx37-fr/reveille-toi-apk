package com.furaxx37.reveilletoi;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AlarmActivity extends AppCompatActivity {
    
    private TextView timeText;
    private TextView labelText;
    private TextView dateText;
    private Button dismissButton;
    private Button snoozeButton;
    
    private long alarmId;
    private String alarmLabel;
    private String ringtoneUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Show activity over lock screen
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                               WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                               WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                               WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        }
        
        setContentView(R.layout.activity_alarm);
        
        initializeViews();
        handleIntent();
        setupListeners();
        updateTimeDisplay();
    }

    private void initializeViews() {
        timeText = findViewById(R.id.alarm_time_display);
        labelText = findViewById(R.id.alarm_label_display);
        dateText = findViewById(R.id.alarm_date_display);
        dismissButton = findViewById(R.id.dismiss_button);
        snoozeButton = findViewById(R.id.snooze_button);
    }

    private void handleIntent() {
        Intent intent = getIntent();
        alarmId = intent.getLongExtra("alarm_id", -1);
        alarmLabel = intent.getStringExtra("alarm_label");
        ringtoneUri = intent.getStringExtra("ringtone_uri");
        
        // Set label text
        if (alarmLabel != null && !alarmLabel.isEmpty()) {
            labelText.setText(alarmLabel);
            labelText.setVisibility(View.VISIBLE);
        } else {
            labelText.setText("Alarme");
            labelText.setVisibility(View.VISIBLE);
        }
    }

    private void setupListeners() {
        dismissButton.setOnClickListener(v -> dismissAlarm());
        snoozeButton.setOnClickListener(v -> snoozeAlarm());
    }

    private void updateTimeDisplay() {
        // Display current time
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, d MMMM yyyy", Locale.FRENCH);
        
        Date now = new Date();
        timeText.setText(timeFormat.format(now));
        dateText.setText(dateFormat.format(now));
    }

    private void dismissAlarm() {
        // Stop the alarm service
        Intent serviceIntent = new Intent(this, AlarmService.class);
        stopService(serviceIntent);
        
        // Close the activity
        finish();
    }

    private void snoozeAlarm() {
        // Stop current alarm
        Intent serviceIntent = new Intent(this, AlarmService.class);
        stopService(serviceIntent);
        
        // Schedule snooze (5 minutes)
        AlarmManager alarmManager = new AlarmManager(this);
        long snoozeTime = System.currentTimeMillis() + (5 * 60 * 1000); // 5 minutes
        
        Alarm snoozeAlarm = new Alarm();
        snoozeAlarm.setId(alarmId);
        snoozeAlarm.setLabel(alarmLabel + " (Répétition)");
        snoozeAlarm.setRingtoneUri(ringtoneUri);
        snoozeAlarm.setEnabled(true);
        
        // Calculate hour and minute for snooze time
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTimeInMillis(snoozeTime);
        snoozeAlarm.setHour(calendar.get(java.util.Calendar.HOUR_OF_DAY));
        snoozeAlarm.setMinute(calendar.get(java.util.Calendar.MINUTE));
        
        alarmManager.setAlarm(snoozeAlarm);
        
        // Show snooze message
        android.widget.Toast.makeText(this, "Alarme reportée de 5 minutes", 
                                    android.widget.Toast.LENGTH_SHORT).show();
        
        // Close the activity
        finish();
    }

    @Override
    public void onBackPressed() {
        // Prevent back button from closing alarm
        // User must dismiss or snooze
        super.onBackPressed();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent();
        updateTimeDisplay();
    }
}
