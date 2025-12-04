package com.furaxx37.reveilletoi;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TimePicker;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;

public class AddAlarmActivity extends AppCompatActivity {
    
    public static final String EXTRA_ALARM = "extra_alarm";
    public static final String EXTRA_IS_EDIT = "extra_is_edit";
    
    private TimePicker timePicker;
    private TextInputEditText labelEditText;
    private Button saveButton;
    private Button cancelButton;
    private Button ringtoneButton;
    
    private Alarm currentAlarm;
    private boolean isEditMode = false;
    private String selectedRingtoneUri = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_alarm);
        
        initializeViews();
        setupListeners();
        handleIntent();
    }

    private void initializeViews() {
        timePicker = findViewById(R.id.time_picker);
        labelEditText = findViewById(R.id.alarm_label_input);
        saveButton = findViewById(R.id.save_button);
        cancelButton = findViewById(R.id.cancel_button);
        ringtoneButton = findViewById(R.id.ringtone_button);
        
        // Set 24-hour format
        timePicker.setIs24HourView(true);
    }

    private void setupListeners() {
        saveButton.setOnClickListener(v -> saveAlarm());
        cancelButton.setOnClickListener(v -> finish());
        ringtoneButton.setOnClickListener(v -> openRingtoneSelection());
    }

    private void handleIntent() {
        Intent intent = getIntent();
        isEditMode = intent.getBooleanExtra(EXTRA_IS_EDIT, false);
        
        if (isEditMode) {
            currentAlarm = (Alarm) intent.getSerializableExtra(EXTRA_ALARM);
            if (currentAlarm != null) {
                populateFields();
                setTitle("Modifier l'alarme");
            }
        } else {
            currentAlarm = new Alarm();
            setTitle("Nouvelle alarme");
        }
    }

    private void populateFields() {
        if (currentAlarm != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                timePicker.setHour(currentAlarm.getHour());
                timePicker.setMinute(currentAlarm.getMinute());
            } else {
                timePicker.setCurrentHour(currentAlarm.getHour());
                timePicker.setCurrentMinute(currentAlarm.getMinute());
            }
            labelEditText.setText(currentAlarm.getLabel());
            selectedRingtoneUri = currentAlarm.getRingtoneUri();
            updateRingtoneButtonText();
        }
    }

    private void saveAlarm() {
        // Get time from TimePicker
        int hour, minute;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            hour = timePicker.getHour();
            minute = timePicker.getMinute();
        } else {
            hour = timePicker.getCurrentHour();
            minute = timePicker.getCurrentMinute();
        }
        
        // Get label
        String label = labelEditText.getText() != null ? 
                      labelEditText.getText().toString().trim() : "";
        
        // Update alarm object
        currentAlarm.setHour(hour);
        currentAlarm.setMinute(minute);
        currentAlarm.setLabel(label);
        currentAlarm.setRingtoneUri(selectedRingtoneUri);
        
        // If it's a new alarm, set creation time and enable it
        if (!isEditMode) {
            currentAlarm.setCreatedAt(System.currentTimeMillis());
            currentAlarm.setEnabled(true);
        }
        
        // Return result
        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_ALARM, currentAlarm);
        resultIntent.putExtra(EXTRA_IS_EDIT, isEditMode);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void openRingtoneSelection() {
        Intent intent = new Intent(this, RingtoneSelectionActivity.class);
        intent.putExtra("current_ringtone", selectedRingtoneUri);
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            selectedRingtoneUri = data.getStringExtra("selected_ringtone");
            updateRingtoneButtonText();
        }
    }

    private void updateRingtoneButtonText() {
        if (selectedRingtoneUri != null && !selectedRingtoneUri.isEmpty()) {
            // Extract filename from URI for display
            String filename = selectedRingtoneUri.substring(selectedRingtoneUri.lastIndexOf("/") + 1);
            ringtoneButton.setText("Sonnerie: " + filename);
        } else {
            ringtoneButton.setText("Choisir une sonnerie");
        }
    }
}
