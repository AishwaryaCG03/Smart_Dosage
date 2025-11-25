package com.example.smart_dosage;

import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.widget.Toast;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smart_dosage.data.AppDatabase;
import com.example.smart_dosage.data.DoseEvent;
import com.example.smart_dosage.data.Medicine;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DoctorPackActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_pack);

        android.widget.TextView tvLast = findViewById(R.id.tv_last_path);
        android.view.View btnOpen = findViewById(R.id.btn_open_pdf);
        android.view.View btnShare = findViewById(R.id.btn_share_pdf);
        android.widget.TextView tvStats = findViewById(R.id.tv_stats);

        updateLast(tvLast);

        try {
            java.util.Calendar cal0 = java.util.Calendar.getInstance();
            java.util.Date end0 = cal0.getTime();
            cal0.add(java.util.Calendar.DAY_OF_YEAR, -30);
            java.util.Date start0 = cal0.getTime();
            java.util.List<com.example.smart_dosage.data.DoseEvent> ev0 = com.example.smart_dosage.data.AppDatabase.get(this).doseEventDao().historyAllList(start0, end0);
            int taken0 = 0, missed0 = 0;
            if (ev0 != null) {
                for (com.example.smart_dosage.data.DoseEvent e : ev0) {
                    if ("TAKEN".equals(e.action)) taken0++;
                    else if ("MISSED".equals(e.action)) missed0++;
                }
            }
            if (tvStats != null) tvStats.setText("Last 30 days • Taken: " + taken0 + "  Missed: " + missed0);
        } catch (Exception ignored) {}

        btnOpen.setOnClickListener(v -> {
            java.io.File f = getLastFile();
            if (f == null || !f.exists()) { Toast.makeText(this, "No PDF yet", Toast.LENGTH_SHORT).show(); return; }
            try {
                android.net.Uri uri = androidx.core.content.FileProvider.getUriForFile(this, getPackageName()+".fileprovider", f);
                Intent open = new Intent(Intent.ACTION_VIEW);
                open.setDataAndType(uri, "application/pdf");
                open.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(open);
            } catch (Exception e) {
                Toast.makeText(this, "No PDF viewer installed", Toast.LENGTH_SHORT).show();
            }
        });

        btnShare.setOnClickListener(v -> {
            java.io.File f = getLastFile();
            if (f == null || !f.exists()) { Toast.makeText(this, "No PDF yet", Toast.LENGTH_SHORT).show(); return; }
            android.net.Uri uri = androidx.core.content.FileProvider.getUriForFile(this, getPackageName()+".fileprovider", f);
            Intent send = new Intent(Intent.ACTION_SEND);
            send.setType("application/pdf");
            send.putExtra(Intent.EXTRA_SUBJECT, "Smart Dosage — Doctor Pack");
            send.putExtra(Intent.EXTRA_STREAM, uri);
            send.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(send, "Share PDF"));
        });

        findViewById(R.id.btn_generate_pdf).setOnClickListener(v -> {
            try {
                Calendar cal = Calendar.getInstance();
                Date end = cal.getTime();
                cal.add(Calendar.DAY_OF_YEAR, -30);
                Date start = cal.getTime();
                List<Medicine> meds = AppDatabase.get(this).medicineDao().getAllSync();
                List<DoseEvent> events = AppDatabase.get(this).doseEventDao().historyAllList(start, end);

                PdfDocument doc = new PdfDocument();
                PdfDocument.PageInfo info = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
                PdfDocument.Page page = doc.startPage(info);
                android.graphics.Canvas c = page.getCanvas();
                android.graphics.Paint p = new android.graphics.Paint();
                p.setTextSize(14f);
                int y = 40;
                c.drawText("Smart Dosage — Doctor Pack", 40, y, p); y += 30;
                c.drawText("Generated: " + new Date().toString(), 40, y, p); y += 30;
                c.drawText("Current Regimen:", 40, y, p); y += 24;
                for (Medicine m : meds) {
                    c.drawText("• " + m.name + " " + m.strength + " × " + m.dosageAmount, 60, y, p); y += 20;
                }
                y += 10;
                c.drawText("Adherence (last 30 days):", 40, y, p); y += 24;
                int taken = 0, missed = 0;
                if (events != null) {
                    for (DoseEvent e : events) {
                        if ("TAKEN".equals(e.action)) taken++;
                        else if ("MISSED".equals(e.action)) missed++;
                    }
                }
                c.drawText("Taken: " + taken + "  Missed: " + missed, 60, y, p); y += 24;
                y += 10;
                c.drawText("Side-effects (manual logs coming soon)", 40, y, p); y += 24;
                doc.finishPage(page);

                File outDir = getExternalFilesDir("docs");
                if (outDir == null) outDir = getFilesDir();
                if (!outDir.exists()) outDir.mkdirs();
                File out = new File(outDir, "doctor_pack.pdf");
                FileOutputStream fos = new FileOutputStream(out);
                doc.writeTo(fos);
                fos.close();
                doc.close();
                getSharedPreferences("doctor_pack", MODE_PRIVATE).edit().putString("last_path", out.getAbsolutePath()).apply();
                updateLast(tvLast);
                Toast.makeText(this, "Saved: " + out.getAbsolutePath(), Toast.LENGTH_LONG).show();
            } catch (Exception ex) {
                Toast.makeText(this, "Failed to generate", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateLast(android.widget.TextView tv) {
        if (tv == null) return;
        String p = getSharedPreferences("doctor_pack", MODE_PRIVATE).getString("last_path", null);
        tv.setText(p == null ? "No PDF generated yet" : ("Last: " + p));
    }

    private java.io.File getLastFile() {
        String p = getSharedPreferences("doctor_pack", MODE_PRIVATE).getString("last_path", null);
        return p == null ? null : new java.io.File(p);
    }
}
