package com.example.smart_dosage;

import android.app.Application;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;

public class SmartDosageApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            try {
                File f = new File(getFilesDir(), "crash.txt");
                try (FileOutputStream fos = new FileOutputStream(f); PrintWriter pw = new PrintWriter(fos)) {
                    e.printStackTrace(pw);
                }
            } catch (Exception ignored) {}
            Toast.makeText(this, "App encountered an error. Crash log saved.", Toast.LENGTH_LONG).show();
        });
    }
}
