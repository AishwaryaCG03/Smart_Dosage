package com.example.smart_dosage.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.example.smart_dosage.data.Medicine;

import java.util.Calendar;
import java.util.List;

public class ReminderScheduler {
    public static void scheduleForMedicine(Context context, Medicine m) {
        if (m.scheduleType == null || m.scheduleType.equals("TIMES")) {
            List<String> times = m.times;
            if (times == null) return;
            for (String t : times) {
                String[] parts = t.split(":");
                int h = Integer.parseInt(parts[0]);
                int min = Integer.parseInt(parts[1]);
                Calendar c = Calendar.getInstance();
                c.set(Calendar.HOUR_OF_DAY, h);
                c.set(Calendar.MINUTE, min);
                c.set(Calendar.SECOND, 0);
                if (c.getTimeInMillis() < System.currentTimeMillis()) c.add(Calendar.DAY_OF_YEAR, 1);
                setAlarm(context, m, c.getTimeInMillis());
            }
        } else if (m.scheduleType.equals("EVERY_X_HOURS")) {
            int interval = m.intervalHours != null ? m.intervalHours : 8;
            long trigger = System.currentTimeMillis() + interval * 60L * 60L * 1000L;
            setAlarm(context, m, trigger);
        } else if (m.scheduleType.equals("WEEKDAYS")) {
            List<String> times = m.times;
            List<String> days = m.weekdays;
            if (times == null || days == null) return;
            for (String d : days) {
                int dow = mapDay(d);
                for (String t : times) {
                    String[] parts = t.split(":");
                    int h = Integer.parseInt(parts[0]);
                    int min = Integer.parseInt(parts[1]);
                    Calendar c = Calendar.getInstance();
                    c.set(Calendar.DAY_OF_WEEK, dow);
                    c.set(Calendar.HOUR_OF_DAY, h);
                    c.set(Calendar.MINUTE, min);
                    c.set(Calendar.SECOND, 0);
                    if (c.getTimeInMillis() < System.currentTimeMillis()) c.add(Calendar.WEEK_OF_YEAR, 1);
                    setAlarm(context, m, c.getTimeInMillis());
                }
            }
        }
    }

    private static int mapDay(String d) {
        String s = d.toUpperCase();
        switch (s) {
            case "SUN": return Calendar.SUNDAY;
            case "MON": return Calendar.MONDAY;
            case "TUE": return Calendar.TUESDAY;
            case "WED": return Calendar.WEDNESDAY;
            case "THU": return Calendar.THURSDAY;
            case "FRI": return Calendar.FRIDAY;
            case "SAT": return Calendar.SATURDAY;
            default: return Calendar.MONDAY;
        }
    }

    private static void setAlarm(Context context, Medicine m, long triggerAtMillis) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, ReminderReceiver.class);
        i.putExtra("medicineId", m.id);
        i.putExtra("name", m.name);
        i.putExtra("dose", m.strength + " • " + m.dosageAmount);
        i.putExtra("scheduledAt", triggerAtMillis);
        PendingIntent pi = PendingIntent.getBroadcast(context, (int) m.id, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi);
    }

    public static void snooze(Context context, long medicineId, int minutes) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, ReminderReceiver.class);
        i.putExtra("medicineId", medicineId);
        PendingIntent pi = PendingIntent.getBroadcast(context, (int) medicineId, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        long trigger = System.currentTimeMillis() + minutes * 60_000L;
        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pi);
    }
}
