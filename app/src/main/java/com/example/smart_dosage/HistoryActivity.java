package com.example.smart_dosage;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smart_dosage.data.AppDatabase;
import com.example.smart_dosage.data.DoseEvent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoryActivity extends AppCompatActivity {
    private List<Map<String, String>> data = new ArrayList<>();
    private SimpleAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        TextView tv = findViewById(R.id.tv_metrics);
        ListView lv = findViewById(R.id.list);
        adapter = new SimpleAdapter(this, data, android.R.layout.simple_list_item_2, new String[]{"title", "subtitle"}, new int[]{android.R.id.text1, android.R.id.text2});
        lv.setAdapter(adapter);

        loadPeriod(7, tv);
        findViewById(R.id.btn_day).setOnClickListener(v -> loadPeriod(1, tv));
        findViewById(R.id.btn_week).setOnClickListener(v -> loadPeriod(7, tv));
        findViewById(R.id.btn_month).setOnClickListener(v -> loadPeriod(30, tv));
    }

    private void loadPeriod(int days, TextView tv) {
        new Thread(() -> {
            Calendar cal = Calendar.getInstance();
            java.util.Date end = cal.getTime();
            cal.add(Calendar.DAY_OF_YEAR, -days);
            java.util.Date start = cal.getTime();
            int taken = AppDatabase.get(this).doseEventDao().countTaken(start, end);
            int scheduled = AppDatabase.get(this).doseEventDao().countScheduled(start, end);
            int pct = scheduled == 0 ? 0 : (int) Math.round((taken * 100.0) / scheduled);
            runOnUiThread(() -> tv.setText(days + "-day adherence: " + pct + "%"));

            List<DoseEvent> events = AppDatabase.get(this).doseEventDao().historyAllList(start, end);
            List<Map<String, String>> maps = new ArrayList<>();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            if (events != null) {
                for (DoseEvent e : events) {
                    Map<String, String> map = new HashMap<>();
                    map.put("title", e.action + " • " + sdf.format(e.actionTime));
                    map.put("subtitle", "Medicine " + e.medicineId);
                    maps.add(map);
                }
            }
            runOnUiThread(() -> {
                data.clear();
                data.addAll(maps);
                adapter.notifyDataSetChanged();
            });
        }).start();
    }
}
