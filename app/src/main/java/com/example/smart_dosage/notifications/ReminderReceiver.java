package com.example.smart_dosage.notifications;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

import androidx.core.app.NotificationCompat;

import com.example.smart_dosage.MainActivity;
import com.example.smart_dosage.R;

public class ReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        long medicineId = intent.getLongExtra("medicineId", -1);
        String name = intent.getStringExtra("name");
        String dose = intent.getStringExtra("dose");
        NotificationHelper.ensureChannel(context);
        long scheduledAt = intent.getLongExtra("scheduledAt", System.currentTimeMillis());
        com.example.smart_dosage.data.DoseEvent scheduled = new com.example.smart_dosage.data.DoseEvent();
        scheduled.medicineId = medicineId;
        scheduled.scheduledTime = new java.util.Date(scheduledAt);
        scheduled.action = "SCHEDULED";
        scheduled.actionTime = new java.util.Date();
        new Thread(() -> com.example.smart_dosage.data.AppDatabase.get(context).doseEventDao().insert(scheduled)).start();
        Intent open = new Intent(context, MainActivity.class);
        PendingIntent content = PendingIntent.getActivity(context, (int) medicineId, open, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent taken = new Intent(context, ActionReceiver.class).setAction("TAKEN").putExtra("medicineId", medicineId).putExtra("scheduledAt", scheduledAt);
        PendingIntent pTaken = PendingIntent.getBroadcast(context, (int) (medicineId + 1), taken, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Intent snooze = new Intent(context, ActionReceiver.class).setAction("SNOOZE").putExtra("medicineId", medicineId).putExtra("scheduledAt", scheduledAt);
        PendingIntent pSnooze = PendingIntent.getBroadcast(context, (int) (medicineId + 2), snooze, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder b = new NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(name)
                .setContentText(dose)
                .setContentIntent(content)
                .setAutoCancel(true)
                .setColor(Color.BLUE)
                .addAction(R.drawable.ic_launcher_foreground, "Taken", pTaken)
                .addAction(R.drawable.ic_launcher_foreground, "Snooze", pSnooze)
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        android.content.Intent pharmacy = new android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("geo:0,0?q=pharmacy"));
        android.app.PendingIntent pPharm = android.app.PendingIntent.getActivity(context, (int) (medicineId + 3), pharmacy, android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE);
        b.addAction(R.drawable.ic_launcher_foreground, "Pharmacy", pPharm);
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify((int) medicineId, b.build());

        android.app.AlarmManager am = (android.app.AlarmManager) context.getSystemService(android.content.Context.ALARM_SERVICE);
        android.content.Intent check = new android.content.Intent(context, MissedCheckReceiver.class).putExtra("medicineId", medicineId).putExtra("scheduled", scheduledAt);
        android.app.PendingIntent pi = android.app.PendingIntent.getBroadcast(context, (int)(medicineId+1000), check, android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE);
        long trigger = System.currentTimeMillis() + 30 * 60_000L; // 30-min grace
        am.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, trigger, pi);
    }
}
