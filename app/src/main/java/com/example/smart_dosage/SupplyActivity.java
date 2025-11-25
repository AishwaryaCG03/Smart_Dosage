package com.example.smart_dosage;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ProgressBar;

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
        TextView tvName = findViewById(R.id.tv_med_name);
        TextView tvDaysLeft = findViewById(R.id.tv_days_left);
        TextView tvRunout = findViewById(R.id.tv_runout_date);
        TextView tvProgress = findViewById(R.id.tv_progress);
        ProgressBar pb = findViewById(R.id.pb_supply);
        Switch swEnable = findViewById(R.id.sw_enable_refill);
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
                        tvName.setText(m.name);
                        int remaining = s != null ? s.remaining : m.initialSupply;
                        int total = m.initialSupply;
                        int dosesPerDay = Math.max(1, m.dosesPerDay);
                        int daysLeft = total==0?0: (remaining / dosesPerDay);
                        tvDaysLeft.setText("Estimated days left: " + daysLeft);
                        java.util.Calendar c = java.util.Calendar.getInstance(); c.add(java.util.Calendar.DAY_OF_YEAR, daysLeft);
                        tvRunout.setText("Run-out date: " + new java.text.SimpleDateFormat("MMM d").format(c.getTime()));
                        int pct = total==0?0: (int)Math.round(remaining * 100.0 / total);
                        pb.setProgress(pct);
                        tvProgress.setText(remaining + " left out of " + total);
                        ((EditText)findViewById(R.id.et_remaining)).setText(String.valueOf(remaining));
                        ((EditText)findViewById(R.id.et_lead_days)).setText(s != null ? String.valueOf(s.refillLeadDays) : "5");
                        swEnable.setChecked(s == null || s.refillLeadDays > 0);
                        ((EditText)findViewById(R.id.et_lead_days)).setEnabled(swEnable.isChecked());
                    });
                }).start();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        swEnable.setOnCheckedChangeListener((buttonView, isChecked) -> ((EditText)findViewById(R.id.et_lead_days)).setEnabled(isChecked));

        findViewById(R.id.btn_save).setOnClickListener(v -> {
            final int remainingVal = Integer.parseInt(((EditText)findViewById(R.id.et_remaining)).getText().toString());
            int parsedLead = Integer.parseInt(((EditText)findViewById(R.id.et_lead_days)).getText().toString());
            final int leadVal = swEnable.isChecked() ? parsedLead : 0; // disable reminders when off
            new Thread(() -> {
                Supply s = AppDatabase.get(SupplyActivity.this).supplyDao().getByMedicine(selectedId);
                if (s == null) { s = new Supply(); s.medicineId = selectedId; }
                s.remaining = remainingVal;
                s.refillLeadDays = leadVal;
                s.lastUpdated = new java.util.Date();
                if (s.id == 0) AppDatabase.get(SupplyActivity.this).supplyDao().insert(s); else AppDatabase.get(SupplyActivity.this).supplyDao().update(s);
                Medicine m = AppDatabase.get(SupplyActivity.this).medicineDao().getById(selectedId);
                if (m != null) com.example.smart_dosage.supply.SupplyManager.ensureInitial(SupplyActivity.this, m);
                finish();
            }).start();
        });

        findViewById(R.id.btn_take_one).setOnClickListener(v -> {
            if (selectedId <= 0) return;
            new Thread(() -> {
                com.example.smart_dosage.supply.SupplyManager.decrement(SupplyActivity.this, selectedId);
                Supply s = AppDatabase.get(SupplyActivity.this).supplyDao().getByMedicine(selectedId);
                Medicine m = AppDatabase.get(SupplyActivity.this).medicineDao().getById(selectedId);
                runOnUiThread(() -> {
                    int remaining = s != null ? s.remaining : 0;
                    int total = m != null ? m.initialSupply : 0;
                    int dosesPerDay = m != null ? Math.max(1, m.dosesPerDay) : 1;
                    int daysLeft = total==0?0: (remaining / dosesPerDay);
                    tvDaysLeft.setText("Estimated days left: " + daysLeft);
                    java.util.Calendar c = java.util.Calendar.getInstance(); c.add(java.util.Calendar.DAY_OF_YEAR, daysLeft);
                    tvRunout.setText("Run-out date: " + new java.text.SimpleDateFormat("MMM d").format(c.getTime()));
                    int pct = total==0?0: (int)Math.round(remaining * 100.0 / total);
                    pb.setProgress(pct);
                    tvProgress.setText(remaining + " left out of " + total);
                    ((EditText)findViewById(R.id.et_remaining)).setText(String.valueOf(remaining));
                });
            }).start();
        });

        findViewById(R.id.btn_add_refill).setOnClickListener(v -> {
            android.widget.EditText input = new android.widget.EditText(SupplyActivity.this);
            input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
            new com.google.android.material.dialog.MaterialAlertDialogBuilder(SupplyActivity.this)
                    .setTitle("Add refill count")
                    .setView(input)
                    .setPositiveButton("Add", (d,w) -> {
                        String t = input.getText().toString();
                        int add = 0; try { add = Integer.parseInt(t); } catch (Exception ignore) {}
                        final int fAdd = add;
                        new Thread(() -> {
                            Supply s0 = AppDatabase.get(SupplyActivity.this).supplyDao().getByMedicine(selectedId);
                            if (s0 == null) { s0 = new Supply(); s0.medicineId = selectedId; }
                            s0.remaining = Math.max(0, (s0.remaining) + fAdd);
                            s0.lastUpdated = new java.util.Date();
                            if (s0.id == 0) AppDatabase.get(SupplyActivity.this).supplyDao().insert(s0); else AppDatabase.get(SupplyActivity.this).supplyDao().update(s0);
                            Medicine m0 = AppDatabase.get(SupplyActivity.this).medicineDao().getById(selectedId);
                            final int remaining = s0.remaining;
                            final int total = m0 != null ? m0.initialSupply : 0;
                            final int dosesPerDay = m0 != null ? Math.max(1, m0.dosesPerDay) : 1;
                            final int daysLeft = total==0?0: (remaining / dosesPerDay);
                            final int pct = total==0?0: (int)Math.round(remaining * 100.0 / total);
                            runOnUiThread(() -> {
                                tvDaysLeft.setText("Estimated days left: " + daysLeft);
                                java.util.Calendar c = java.util.Calendar.getInstance(); c.add(java.util.Calendar.DAY_OF_YEAR, daysLeft);
                                tvRunout.setText("Run-out date: " + new java.text.SimpleDateFormat("MMM d").format(c.getTime()));
                                pb.setProgress(pct);
                                tvProgress.setText(remaining + " left out of " + total);
                                ((EditText)findViewById(R.id.et_remaining)).setText(String.valueOf(remaining));
                            });
                        }).start();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        findViewById(R.id.btn_pharmacy).setOnClickListener(v -> {
            android.content.Intent i = new android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("geo:0,0?q=pharmacy"));
            startActivity(i);
        });
    }
}
