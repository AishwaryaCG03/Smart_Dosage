package com.example.smart_dosage.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.smart_dosage.data.AppDatabase;
import com.example.smart_dosage.data.DoseEvent;

import java.util.Date;

public class ActionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        long medicineId = intent.getLongExtra("medicineId", -1);
        DoseEvent e = new DoseEvent();
        e.medicineId = medicineId;
        e.scheduledTime = new Date();
        e.action = action;
        e.actionTime = new Date();
        new Thread(() -> AppDatabase.get(context).doseEventDao().insert(e)).start();
        if ("TAKEN".equals(action)) {
            new Thread(() -> com.example.smart_dosage.supply.SupplyManager.decrement(context, medicineId)).start();
        }
        if ("SNOOZE".equals(action)) {
            ReminderScheduler.snooze(context, medicineId, 15);
        }
    }
}
