package com.example.smart_dosage.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "dose_events")
public class DoseEvent {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long medicineId;
    public Date scheduledTime;
    public String action;
    public Date actionTime;
}
