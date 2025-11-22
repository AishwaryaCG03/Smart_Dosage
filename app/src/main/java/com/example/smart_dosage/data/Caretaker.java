package com.example.smart_dosage.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "caretakers")
public class Caretaker {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String name;
    public String phone;
}
