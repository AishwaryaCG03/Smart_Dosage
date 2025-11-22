package com.example.smart_dosage.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "supplies")
public class Supply {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long medicineId;
    public int remaining;
    public int refillLeadDays;
    public Date lastUpdated;
}
