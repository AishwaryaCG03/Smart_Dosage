package com.example.smart_dosage;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smart_dosage.data.AppDatabase;
import com.example.smart_dosage.data.Medicine;
import com.example.smart_dosage.data.Supply;

import java.util.List;

public class SupplyActivity extends AppCompatActivity {
    private List<Medicine> medicines;
    private long selectedId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supply);

        Spinner sp = findViewById(R.id.sp_medicine);
        new Thread(() -> {
            medicines = AppDatabase.get(SupplyActivity.this).medicineDao().getAllSync();
            runOnUiThread(() -> {
                ArrayAdapter<String> ad = new ArrayAdapter<>(SupplyActivity.this, android.R.layout.simple_spinner_item);
                for (Medicine m : medicines) ad.add(m.name);
                ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                sp.setAdapter(ad);
            });
        }).start();

        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Medicine m = medicines.get(position);
                selectedId = m.id;
                new Thread(() -> {
                    Supply s = AppDatabase.get(SupplyActivity.this).supplyDao().getByMedicine(selectedId);
                    runOnUiThread(() -> {
                        ((EditText)findViewById(R.id.et_remaining)).setText(s != null ? String.valueOf(s.remaining) : "0");
                        ((EditText)findViewById(R.id.et_lead_days)).setText(s != null ? String.valueOf(s.refillLeadDays) : "3");
                    });
                }).start();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        findViewById(R.id.btn_save).setOnClickListener(v -> {
            int remaining = Integer.parseInt(((EditText)findViewById(R.id.et_remaining)).getText().toString());
            int lead = Integer.parseInt(((EditText)findViewById(R.id.et_lead_days)).getText().toString());
            new Thread(() -> {
                Supply s = AppDatabase.get(SupplyActivity.this).supplyDao().getByMedicine(selectedId);
                if (s == null) { s = new Supply(); s.medicineId = selectedId; }
                s.remaining = remaining;
                s.refillLeadDays = lead;
                s.lastUpdated = new java.util.Date();
                if (s.id == 0) AppDatabase.get(SupplyActivity.this).supplyDao().insert(s); else AppDatabase.get(SupplyActivity.this).supplyDao().update(s);
                finish();
            }).start();
        });
    }
}
