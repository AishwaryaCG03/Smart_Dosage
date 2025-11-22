package com.example.smart_dosage;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smart_dosage.data.AppDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AchievementsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievements);

        TextView tv = findViewById(R.id.tv_streak);
        ListView lv = findViewById(R.id.list);
        List<Map<String, String>> data = new ArrayList<>();
        SimpleAdapter ad = new SimpleAdapter(this, data, android.R.layout.simple_list_item_2, new String[]{"title","subtitle"}, new int[]{android.R.id.text1, android.R.id.text2});
        lv.setAdapter(ad);

        new Thread(() -> {
            int streak = computeStreak();
            runOnUiThread(() -> tv.setText("Current streak: " + streak + " days"));
            List<Map<String, String>> badges = new ArrayList<>();
            if (streak >= 3) badges.add(make("Bronze Streak", "3 days"));
            if (streak >= 7) badges.add(make("Silver Streak", "7 days"));
            if (streak >= 30) badges.add(make("Gold Streak", "30 days"));
            runOnUiThread(() -> { data.clear(); data.addAll(badges); ad.notifyDataSetChanged(); });
        }).start();
    }

    private Map<String,String> make(String t, String s){ Map<String,String> m=new HashMap<>(); m.put("title",t); m.put("subtitle",s); return m; }

    private int computeStreak() {
        int days = 0;
        Calendar cal = Calendar.getInstance();
        for (int i = 0; i < 60; i++) {
            java.util.Date end = cal.getTime();
            cal.add(Calendar.DAY_OF_YEAR, -1);
            java.util.Date start = cal.getTime();
            int taken = AppDatabase.get(this).doseEventDao().countTaken(start, end);
            int scheduled = AppDatabase.get(this).doseEventDao().countScheduled(start, end);
            if (scheduled == 0) break;
            if (taken >= scheduled) days++;
            else break;
        }
        return days;
    }
}
