package com.example.smart_dosage.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {Medicine.class, DoseEvent.class, Supply.class, Caretaker.class}, version = 2)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract MedicineDao medicineDao();
    public abstract DoseEventDao doseEventDao();
    public abstract SupplyDao supplyDao();
    public abstract CaretakerDao caretakerDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase get(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "smart_dosage.db").fallbackToDestructiveMigration().build();
                }
            }
        }
        return INSTANCE;
    }
}
