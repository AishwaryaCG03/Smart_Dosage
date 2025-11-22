package com.example.smart_dosage.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.smart_dosage.data.AppDatabase;
import com.example.smart_dosage.data.Medicine;

import java.util.List;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        new Thread(() -> {
            List<Medicine> meds = AppDatabase.get(context).medicineDao().getAllSync();
            for (Medicine m : meds) ReminderScheduler.scheduleForMedicine(context, m);
        }).start();
    }
}
