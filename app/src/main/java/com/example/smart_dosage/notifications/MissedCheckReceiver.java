package com.example.smart_dosage.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.smart_dosage.R;
import com.example.smart_dosage.data.AppDatabase;
import com.example.smart_dosage.data.Caretaker;
import com.example.smart_dosage.data.DoseEvent;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MissedCheckReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        long medicineId = intent.getLongExtra("medicineId", -1);
        long scheduledMillis = intent.getLongExtra("scheduled", 0);
        Date scheduled = new Date(scheduledMillis);
        Date now = new Date();
        List<DoseEvent> events = AppDatabase.get(context).doseEventDao().eventsForMedicineBetween(medicineId, scheduled, now);
        boolean taken = false;
        if (events != null) {
            for (DoseEvent e : events) {
                if ("TAKEN".equals(e.action)) { taken = true; break; }
            }
        }
        if (!taken) {
            DoseEvent missed = new DoseEvent();
            missed.medicineId = medicineId;
            missed.scheduledTime = scheduled;
            missed.action = "MISSED";
            missed.actionTime = now;
            new Thread(() -> AppDatabase.get(context).doseEventDao().insert(missed)).start();

            NotificationHelper.ensureChannel(context);
            NotificationCompat.Builder b = new NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle("Missed dose")
                    .setContentText("A dose was missed. Consider contacting caretaker.")
                    .addAction(0, "Find Nearby Pharmacy", android.app.PendingIntent.getActivity(context, (int)(medicineId+3000), new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=pharmacy")), android.app.PendingIntent.FLAG_IMMUTABLE))
                    .setPriority(NotificationCompat.PRIORITY_HIGH);
            NotificationManagerCompat.from(context).notify((int)(medicineId+20000), b.build());

            new Thread(() -> {
                List<Caretaker> caretakers = AppDatabase.get(context).caretakerDao().getAll();
                for (Caretaker c : caretakers) {
                    Intent sms = new Intent(Intent.ACTION_SENDTO);
                    sms.setData(Uri.parse("smsto:" + c.phone));
                    sms.putExtra("sms_body", "Missed dose: medicine " + medicineId + ", scheduled " + scheduled);
                    sms.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(sms);
                }
            }).start();
        }
    }
}
