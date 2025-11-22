package com.example.smart_dosage.ocr;

import com.example.smart_dosage.data.Medicine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PrescriptionParser {
    public static List<Medicine> parse(String text) {
        List<Medicine> list = new ArrayList<>();
        String[] lines = text.split("\n");
        for (String line : lines) {
            String l = line.trim();
            if (l.isEmpty()) continue;
            String[] parts = l.split(" ");
            if (parts.length < 2) continue;
            Medicine m = new Medicine();
            m.name = parts[0];
            m.strength = parts[1];
            m.dosageAmount = 1;
            m.times = Arrays.asList("08:00");
            m.dosesPerDay = m.times.size();
            list.add(m);
        }
        return list;
    }
}
