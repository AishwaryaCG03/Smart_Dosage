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

import java.util.List;

public class EscalationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        long medicineId = intent.getLongExtra("medicineId", -1);
        boolean allowCall = context.getSharedPreferences("settings", Context.MODE_PRIVATE).getBoolean("escalate_call", true);
        boolean allowSOS = context.getSharedPreferences("settings", Context.MODE_PRIVATE).getBoolean("escalate_sos", true);

        NotificationHelper.ensureChannel(context);
        NotificationCompat.Builder b = new NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Escalation")
                .setContentText("Missed dose escalation options")
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        List<Caretaker> caretakers = AppDatabase.get(context).caretakerDao().getAll();
        if (allowCall && caretakers != null && caretakers.size() > 0) {
            Caretaker c = caretakers.get(0);
            Intent dial = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + c.phone));
            android.app.PendingIntent pDial = android.app.PendingIntent.getActivity(context, (int)(medicineId+6000), dial, android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE);
            b.addAction(0, "Call Caretaker", pDial);
        }
        if (allowSOS) {
            String sosNumber = context.getSharedPreferences("settings", Context.MODE_PRIVATE).getString("sos_number", "112");
            Intent sos = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + sosNumber));
            android.app.PendingIntent pSos = android.app.PendingIntent.getActivity(context, (int)(medicineId+7000), sos, android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE);
            b.addAction(0, "SOS", pSos);
        }

        NotificationManagerCompat.from(context).notify((int)(medicineId+50000), b.build());
    }
}
