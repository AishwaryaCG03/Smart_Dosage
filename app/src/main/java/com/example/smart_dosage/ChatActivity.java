package com.example.smart_dosage;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smart_dosage.data.AppDatabase;
import com.example.smart_dosage.data.Medicine;

import java.util.List;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity {
    private List<Medicine> meds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        new Thread(() -> {
            meds = AppDatabase.get(ChatActivity.this).medicineDao().getAllSync();
        }).start();

        findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String q = ((EditText)findViewById(R.id.et_input)).getText().toString();
                ((TextView)findViewById(R.id.tv_output)).setText(answer(q));
            }
        });
    }

    private String answer(String q) {
        String lq = q.toLowerCase(Locale.ROOT);
        if (meds == null || meds.isEmpty()) return "No medicines found.";
        for (Medicine m : meds) {
            if (lq.contains(m.name.toLowerCase(Locale.ROOT))) {
                StringBuilder sb = new StringBuilder();
                sb.append(m.name).append(" ").append(m.strength).append("\n");
                sb.append("Typical schedule: ").append(String.join(", ", m.times)).append("\n");
                if (m.instructions != null && !m.instructions.isEmpty()) sb.append("Instructions: ").append(m.instructions).append("\n");
                sb.append("If in doubt, consult your doctor.");
                return sb.toString();
            }
        }
        return "Please mention a medicine name. Available: " + meds.get(0).name + (meds.size()>1?", etc.":"");
    }
}
