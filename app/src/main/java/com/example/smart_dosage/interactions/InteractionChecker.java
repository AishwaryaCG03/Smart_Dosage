package com.example.smart_dosage.interactions;

import com.example.smart_dosage.data.Medicine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class InteractionChecker {
    private static final Map<String, List<String>> interactions = new HashMap<>();
    private static final Map<String, String> sideEffects = new HashMap<>();

    static {
        addInteraction("ibuprofen", "aspirin", "Increased bleeding risk. Consider consulting a pharmacist.");
        addInteraction("warfarin", "aspirin", "High bleeding risk. Consult doctor.");
        addInteraction("paracetamol", "alcohol", "Liver strain risk. Avoid alcohol.");
        sideEffects.put("ibuprofen", "GI upset, bleeding risk");
        sideEffects.put("aspirin", "Bleeding, GI irritation");
        sideEffects.put("warfarin", "Bleeding, bruising");
        sideEffects.put("paracetamol", "Liver toxicity at high doses");
    }

    private static void addInteraction(String a, String b, String msg) {
        String la = a.toLowerCase(Locale.ROOT);
        String lb = b.toLowerCase(Locale.ROOT);
        interactions.computeIfAbsent(la, k -> new ArrayList<>()).add(lb + "::" + msg);
        interactions.computeIfAbsent(lb, k -> new ArrayList<>()).add(la + "::" + msg);
    }

    public static String check(List<Medicine> current, Medicine candidate) {
        List<String> warnings = new ArrayList<>();
        String cname = candidate.name == null ? "" : candidate.name.toLowerCase(Locale.ROOT);
        for (Medicine m : current) {
            String name = m.name == null ? "" : m.name.toLowerCase(Locale.ROOT);
            List<String> list = interactions.get(cname);
            if (list != null) {
                for (String s : list) {
                    String[] parts = s.split("::");
                    if (parts.length == 2 && name.equals(parts[0])) warnings.add(candidate.name + " + " + m.name + ": " + parts[1]);
                }
            }
        }
        List<String> effects = new ArrayList<>();
        String e1 = sideEffects.get(cname);
        if (e1 != null) effects.add(candidate.name + ": " + e1);
        for (Medicine m : current) {
            String e = sideEffects.get(m.name == null ? "" : m.name.toLowerCase(Locale.ROOT));
            if (e != null) effects.add(m.name + ": " + e);
        }
        StringBuilder sb = new StringBuilder();
        if (!warnings.isEmpty()) sb.append("Interactions:\n").append(String.join("\n", warnings)).append("\n\n");
        if (!effects.isEmpty()) sb.append("Possible side effects:\n").append(String.join("\n", effects)).append("\n");
        if (sb.length() > 0) sb.append("If unsure, consult a pharmacist or doctor.");
        return sb.toString();
    }
}
