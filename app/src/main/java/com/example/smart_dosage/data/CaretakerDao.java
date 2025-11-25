package com.example.smart_dosage.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CaretakerDao {
    @Insert
    long insert(Caretaker c);

    @Delete
    void delete(Caretaker c);

    @Query("SELECT * FROM caretakers ORDER BY name")
    List<Caretaker> getAll();

    @Query("SELECT * FROM caretakers WHERE phone=:phone LIMIT 1")
    Caretaker getByPhone(String phone);

    @Update
    void update(Caretaker c);
}
