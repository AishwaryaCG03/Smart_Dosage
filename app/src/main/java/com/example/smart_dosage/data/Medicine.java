package com.example.smart_dosage.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;
import java.util.List;

@Entity(tableName = "medicines")
public class Medicine {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String name;
    public String strength;
    public double dosageAmount;
    public int dosesPerDay;
    public List<String> times;
    public Date startDate;
    public Date endDate;
    public String instructions;
    public int refills;
    public String photoUri;
    public int initialSupply;
    public String scheduleType; // "TIMES", "EVERY_X_HOURS", "WEEKDAYS"
    public Integer intervalHours;
    public List<String> weekdays; // e.g., MON,TUE
}
