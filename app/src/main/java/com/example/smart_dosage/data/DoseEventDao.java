package com.example.smart_dosage.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.Date;
import java.util.List;

@Dao
public interface DoseEventDao {
    @Insert
    long insert(DoseEvent event);

    @Query("SELECT * FROM dose_events WHERE medicineId=:medicineId AND actionTime BETWEEN :start AND :end ORDER BY actionTime DESC")
    LiveData<List<DoseEvent>> history(long medicineId, Date start, Date end);

    @Query("SELECT COUNT(*) FROM dose_events WHERE action='TAKEN' AND scheduledTime BETWEEN :start AND :end")
    int countTaken(Date start, Date end);

    @Query("SELECT COUNT(*) FROM dose_events WHERE scheduledTime BETWEEN :start AND :end")
    int countScheduled(Date start, Date end);

    @Query("SELECT * FROM dose_events WHERE actionTime BETWEEN :start AND :end ORDER BY actionTime DESC")
    LiveData<List<DoseEvent>> historyAll(Date start, Date end);

    @Query("SELECT * FROM dose_events WHERE actionTime BETWEEN :start AND :end ORDER BY actionTime DESC")
    List<DoseEvent> historyAllList(Date start, Date end);

    @Query("SELECT * FROM dose_events WHERE medicineId=:medicineId AND actionTime BETWEEN :start AND :end ORDER BY actionTime DESC")
    List<DoseEvent> eventsForMedicineBetween(long medicineId, Date start, Date end);
}
