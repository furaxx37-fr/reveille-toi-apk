package com.furaxx37.reveilletoi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import java.util.List;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            // Reschedule all active alarms after device reboot
            DatabaseHelper dbHelper = new DatabaseHelper(context);
            List<Alarm> alarms = dbHelper.getAllAlarms();
            
            for (Alarm alarm : alarms) {
                if (alarm.isEnabled()) {
                    AlarmScheduler.scheduleAlarm(context, alarm);
                }
            }
        }
    }
}
