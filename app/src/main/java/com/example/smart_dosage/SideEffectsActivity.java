package com.example.smart_dosage;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;

public class SideEffectsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_side_effects);

        android.widget.Spinner severity = findViewById(R.id.spinner_severity);
        severity.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, new String[]{"1","2","3","4","5"}));
        android.widget.EditText note = findViewById(R.id.et_note);

        findViewById(R.id.btn_save).setOnClickListener(v -> {
            try {
                int sev = Integer.parseInt(severity.getSelectedItem().toString());
                String n = note.getText().toString();
                android.content.SharedPreferences sp = getSharedPreferences("side_effects", MODE_PRIVATE);
                String raw = sp.getString("logs", "[]");
                JSONArray arr = new JSONArray(raw);
                JSONObject obj = new JSONObject();
                obj.put("severity", sev);
                obj.put("note", n);
                obj.put("time", new Date().getTime());
                arr.put(obj);
                sp.edit().putString("logs", arr.toString()).apply();
                Toast.makeText(this, "Logged", Toast.LENGTH_SHORT).show();
                note.setText("");
            } catch (Exception e) {
                Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btn_trend).setOnClickListener(v -> {
            android.content.SharedPreferences sp = getSharedPreferences("side_effects", MODE_PRIVATE);
            String raw = sp.getString("logs", "[]");
            try {
                JSONArray arr = new JSONArray(raw);
                int recentHigh = 0;
                long twoWeeks = 14L * 24L * 60L * 60L * 1000L;
                long cutoff = System.currentTimeMillis() - twoWeeks;
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject o = arr.getJSONObject(i);
                    if (o.getLong("time") >= cutoff && o.getInt("severity") >= 4) recentHigh++;
                }
                Toast.makeText(this, recentHigh >= 3 ? "Trend: frequent high severity" : "Trend: stable", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Toast.makeText(this, "No data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
