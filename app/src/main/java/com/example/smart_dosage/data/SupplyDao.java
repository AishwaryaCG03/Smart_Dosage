package com.example.smart_dosage.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface SupplyDao {
    @Insert
    long insert(Supply supply);

    @Update
    void update(Supply supply);

    @Query("SELECT * FROM supplies WHERE medicineId=:medicineId LIMIT 1")
    Supply getByMedicine(long medicineId);
}
