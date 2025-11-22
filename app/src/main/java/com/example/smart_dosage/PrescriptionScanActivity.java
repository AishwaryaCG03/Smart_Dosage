package com.example.smart_dosage;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smart_dosage.data.AppDatabase;
import com.example.smart_dosage.data.Medicine;
import com.example.smart_dosage.ocr.PrescriptionParser;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrescriptionScanActivity extends AppCompatActivity {
    private static final int REQ_IMAGE = 201;
    private final List<Medicine> suggestions = new ArrayList<>();
    private SimpleAdapter adapter;
    private final List<Map<String, String>> data = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prescription_scan);

        ListView lv = findViewById(R.id.list);
        adapter = new SimpleAdapter(this, data, android.R.layout.simple_list_item_2, new String[]{"name", "details"}, new int[]{android.R.id.text1, android.R.id.text2});
        lv.setAdapter(adapter);

        findViewById(R.id.btn_pick).setOnClickListener(v -> {
            Intent pick = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(pick, REQ_IMAGE);
        });
        findViewById(R.id.btn_add_all).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(() -> {
                    for (Medicine m : suggestions) AppDatabase.get(PrescriptionScanActivity.this).medicineDao().insert(m);
                    finish();
                }).start();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            try {
                Bitmap bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                InputImage image = InputImage.fromBitmap(bmp, 0);
                TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS).process(image).addOnSuccessListener(this::onRecognized);
            } catch (IOException e) {
            }
        }
    }

    private void onRecognized(Text text) {
        List<Medicine> meds = PrescriptionParser.parse(text.getText());
        suggestions.clear();
        suggestions.addAll(meds);
        List<Map<String, String>> maps = new ArrayList<>();
        for (Medicine m : meds) {
            Map<String, String> map = new HashMap<>();
            map.put("name", m.name);
            map.put("details", m.strength + " • times: " + String.join(", ", m.times));
            maps.add(map);
        }
        data.clear();
        data.addAll(maps);
        adapter.notifyDataSetChanged();
    }
}
