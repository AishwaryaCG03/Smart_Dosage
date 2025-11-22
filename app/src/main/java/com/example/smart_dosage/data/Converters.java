package com.example.smart_dosage.data;

import androidx.room.TypeConverter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Converters {
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    @TypeConverter
    public static String fromDate(Date date) {
        if (date == null) return null;
        return new SimpleDateFormat(DATE_FORMAT).format(date);
    }

    @TypeConverter
    public static Date toDate(String value) {
        if (value == null) return null;
        try {
            return new SimpleDateFormat(DATE_FORMAT).parse(value);
        } catch (ParseException e) {
            return null;
        }
    }

    @TypeConverter
    public static String fromStringList(List<String> list) {
        if (list == null) return null;
        return String.join("|", list);
    }

    @TypeConverter
    public static List<String> toStringList(String value) {
        if (value == null || value.isEmpty()) return new ArrayList<>();
        String[] parts = value.split("\\|");
        List<String> list = new ArrayList<>();
        for (String p : parts) list.add(p);
        return list;
    }
}
