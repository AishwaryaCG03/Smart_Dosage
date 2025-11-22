package com.example.smart_dosage.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface MedicineDao {
    @Query("SELECT * FROM medicines ORDER BY name")
    LiveData<List<Medicine>> getAll();

    @Query("SELECT * FROM medicines ORDER BY name")
    List<Medicine> getAllSync();

    @Query("SELECT * FROM medicines WHERE id=:id")
    Medicine getById(long id);

    @Insert
    long insert(Medicine medicine);

    @Update
    void update(Medicine medicine);

    @Delete
    void delete(Medicine medicine);
}
