package com.example.smart_dosage.notifications;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import com.example.smart_dosage.R;

public class RefillReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        long medicineId = intent.getLongExtra("medicineId", -1);
        String name = intent.getStringExtra("name");
        NotificationHelper.ensureChannel(context);
        NotificationCompat.Builder b = new NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Refill soon: " + name)
                .setContentText("Supply is running low")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify((int) (medicineId + 10000), b.build());
    }
}
