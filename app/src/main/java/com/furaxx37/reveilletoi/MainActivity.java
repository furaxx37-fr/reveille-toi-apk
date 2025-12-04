package com.furaxx37.reveilletoi;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AlarmAdapter.OnAlarmInteractionListener {
    
    private RecyclerView recyclerViewAlarms;
    private TextView textViewNoAlarms;
    private FloatingActionButton fabAddAlarm;
    private AlarmAdapter alarmAdapter;
    private DatabaseHelper databaseHelper;
    private List<Alarm> alarmList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initViews();
        setupRecyclerView();
        setupClickListeners();
        
        databaseHelper = new DatabaseHelper(this);
        loadAlarms();
    }

    private void initViews() {
        recyclerViewAlarms = findViewById(R.id.recyclerViewAlarms);
        textViewNoAlarms = findViewById(R.id.textViewNoAlarms);
        fabAddAlarm = findViewById(R.id.fabAddAlarm);
    }

    private void setupRecyclerView() {
        alarmList = new ArrayList<>();
        alarmAdapter = new AlarmAdapter(alarmList, this);
        recyclerViewAlarms.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewAlarms.setAdapter(alarmAdapter);
    }

    private void setupClickListeners() {
        fabAddAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddAlarmActivity.class);
                startActivityForResult(intent, 1001); // REQUEST_CODE_ADD_ALARM
            }
        });
    }

    private void loadAlarms() {
        alarmList.clear();
        alarmList.addAll(databaseHelper.getAllAlarms());
        alarmAdapter.notifyDataSetChanged();
        
        if (alarmList.isEmpty()) {
            textViewNoAlarms.setVisibility(View.VISIBLE);
            recyclerViewAlarms.setVisibility(View.GONE);
        } else {
            textViewNoAlarms.setVisibility(View.GONE);
            recyclerViewAlarms.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAlarms();
    }

    @Override
    public void onAlarmToggle(Alarm alarm, boolean isEnabled) {
        alarm.setEnabled(isEnabled);
        databaseHelper.updateAlarm(alarm);
        
        if (isEnabled) {
            AlarmScheduler.scheduleAlarm(this, alarm);
        } else {
            AlarmScheduler.cancelAlarm(this, alarm);
        }
    }

    @Override
    public void onAlarmDelete(Alarm alarm) {
        AlarmScheduler.cancelAlarm(this, alarm);
        databaseHelper.deleteAlarm(alarm.getId());
        loadAlarms();
    }

    @Override
    public void onAlarmEdit(Alarm alarm) {
        Intent intent = new Intent(MainActivity.this, AddAlarmActivity.class);
        intent.putExtra("alarm_id", alarm.getId());
        startActivityForResult(intent, 1002); // REQUEST_CODE_EDIT_ALARM
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == 1001) { // REQUEST_CODE_ADD_ALARM
                // Handle new alarm creation
                Alarm newAlarm = (Alarm) data.getSerializableExtra(AddAlarmActivity.EXTRA_ALARM);
                if (newAlarm != null) {
                    long alarmId = databaseHelper.insertAlarm(newAlarm);
                    newAlarm.setId(alarmId);
                    if (newAlarm.isEnabled()) {
                        AlarmScheduler.scheduleAlarm(this, newAlarm);
                    }
                    loadAlarms();
                }
            } else if (requestCode == 1002) { // REQUEST_CODE_EDIT_ALARM
                // Handle alarm editing
                Alarm updatedAlarm = (Alarm) data.getSerializableExtra("alarm");
                if (updatedAlarm != null) {
                    databaseHelper.updateAlarm(updatedAlarm);
                    AlarmScheduler.cancelAlarm(this, updatedAlarm);
                    if (updatedAlarm.isEnabled()) {
                        AlarmScheduler.scheduleAlarm(this, updatedAlarm);
                    }
                    loadAlarms();
                }
            }
        }
    }
}
