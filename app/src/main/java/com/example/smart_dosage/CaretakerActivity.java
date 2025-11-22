package com.example.smart_dosage;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smart_dosage.data.AppDatabase;
import com.example.smart_dosage.data.Caretaker;

import java.util.ArrayList;
import java.util.List;

public class CaretakerActivity extends AppCompatActivity {
    private List<Caretaker> caretakers = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caretaker);

        ListView lv = findViewById(R.id.list);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        lv.setAdapter(adapter);

        findViewById(R.id.btn_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = ((EditText)findViewById(R.id.et_name)).getText().toString();
                String phone = ((EditText)findViewById(R.id.et_phone)).getText().toString();
                new Thread(() -> {
                    Caretaker c = new Caretaker(); c.name = name; c.phone = phone;
                    AppDatabase.get(CaretakerActivity.this).caretakerDao().insert(c);
                    load();
                }).start();
            }
        });

        load();
    }

    private void load() {
        new Thread(() -> {
            caretakers = AppDatabase.get(CaretakerActivity.this).caretakerDao().getAll();
            List<String> names = new ArrayList<>();
            for (Caretaker c : caretakers) names.add(c.name + " (" + c.phone + ")");
            runOnUiThread(() -> { adapter.clear(); adapter.addAll(names); adapter.notifyDataSetChanged(); });
        }).start();
    }
}
