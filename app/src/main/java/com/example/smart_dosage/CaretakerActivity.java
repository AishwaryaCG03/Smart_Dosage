package com.example.smart_dosage;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smart_dosage.data.AppDatabase;
import com.example.smart_dosage.data.Caretaker;

import java.util.ArrayList;
import java.util.List;

public class CaretakerActivity extends AppCompatActivity {
    private List<Caretaker> caretakers = new ArrayList<>();
    private CaretakerAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caretaker);

        RecyclerView rv = findViewById(R.id.rv_caretakers);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CaretakerAdapter(caretakers);
        rv.setAdapter(adapter);

        findViewById(R.id.btn_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = ((EditText)findViewById(R.id.et_name)).getText().toString();
                String phone = ((EditText)findViewById(R.id.et_phone)).getText().toString();
                if (name==null||name.trim().isEmpty()){ Toast.makeText(CaretakerActivity.this, "Enter name", Toast.LENGTH_SHORT).show(); return; }
                if (phone==null||phone.trim().isEmpty()){ Toast.makeText(CaretakerActivity.this, "Enter phone", Toast.LENGTH_SHORT).show(); return; }
                if (!android.util.Patterns.PHONE.matcher(phone).matches()){ Toast.makeText(CaretakerActivity.this, "Invalid phone", Toast.LENGTH_SHORT).show(); return; }
                new Thread(() -> {
                    try {
                        Caretaker existing = AppDatabase.get(CaretakerActivity.this).caretakerDao().getByPhone(phone);
                        if (existing != null) { runOnUiThread(() -> Toast.makeText(CaretakerActivity.this, "Caretaker already exists", Toast.LENGTH_SHORT).show()); }
                        else {
                            Caretaker c = new Caretaker(); c.name = name; c.phone = phone;
                            AppDatabase.get(CaretakerActivity.this).caretakerDao().insert(c);
                            runOnUiThread(() -> { ((EditText)findViewById(R.id.et_name)).setText(""); ((EditText)findViewById(R.id.et_phone)).setText(""); Toast.makeText(CaretakerActivity.this, "Caretaker saved", Toast.LENGTH_SHORT).show(); });
                        }
                    } catch (Exception e) { runOnUiThread(() -> Toast.makeText(CaretakerActivity.this, "Save failed", Toast.LENGTH_LONG).show()); }
                    load();
                }).start();
            }
        });

        load();
    }

    private void load() {
        new Thread(() -> {
            caretakers = AppDatabase.get(CaretakerActivity.this).caretakerDao().getAll();
            runOnUiThread(() -> { adapter.setItems(caretakers); adapter.notifyDataSetChanged(); });
        }).start();
    }

    static class CaretakerAdapter extends RecyclerView.Adapter<CaretakerAdapter.VH> {
        private final List<Caretaker> items;
        CaretakerAdapter(List<Caretaker> items){ this.items = new ArrayList<>(items); }
        void setItems(List<Caretaker> newItems){ items.clear(); items.addAll(newItems); }
        static class VH extends RecyclerView.ViewHolder {
            android.widget.LinearLayout root; android.widget.TextView name; android.widget.TextView phone; android.widget.ImageButton btnCall, btnSms, btnWa, btnShare, btnDelete;
            VH(android.view.View v){ super(v); root=(android.widget.LinearLayout)v; name=(android.widget.TextView)root.getChildAt(0); phone=(android.widget.TextView)root.getChildAt(1);
                android.widget.LinearLayout actions=(android.widget.LinearLayout)root.getChildAt(2);
                btnCall=(android.widget.ImageButton)actions.getChildAt(0); btnSms=(android.widget.ImageButton)actions.getChildAt(1); btnWa=(android.widget.ImageButton)actions.getChildAt(2); btnShare=(android.widget.ImageButton)actions.getChildAt(3); btnDelete=(android.widget.ImageButton)actions.getChildAt(6);
            }
        }
        @Override public VH onCreateViewHolder(android.view.ViewGroup parent, int viewType){
            android.content.Context ctx = parent.getContext();
            android.widget.LinearLayout card = new android.widget.LinearLayout(ctx);
            card.setOrientation(android.widget.LinearLayout.VERTICAL);
            RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(dp(ctx,8), dp(ctx,6), dp(ctx,8), dp(ctx,6)); card.setLayoutParams(lp); card.setPadding(dp(ctx,14), dp(ctx,12), dp(ctx,14), dp(ctx,12)); card.setElevation(4f);
            android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable(); bg.setCornerRadius(dp(ctx,12)); bg.setColor(0xFFFFFFFF); bg.setStroke(2, 0x1A000000); card.setBackground(bg);
            android.widget.TextView tvName = new android.widget.TextView(ctx); tvName.setTextSize(18f); tvName.setTextColor(0xFF2E2A35);
            android.widget.TextView tvPhone = new android.widget.TextView(ctx); tvPhone.setTextSize(15f); tvPhone.setTextColor(0xFF5F6A76);
            android.widget.LinearLayout actions = new android.widget.LinearLayout(ctx); actions.setOrientation(android.widget.LinearLayout.HORIZONTAL);
            actions.setPadding(0, dp(ctx,8), 0, 0);
            actions.setGravity(android.view.Gravity.END);
            android.widget.ImageButton bCall = makeIconButton(ctx, android.R.drawable.ic_menu_call);
            android.widget.ImageButton bSms = makeIconButton(ctx, android.R.drawable.ic_dialog_email);
            android.widget.ImageButton bWa = makeIconButton(ctx, android.R.drawable.ic_menu_share);
            android.widget.ImageButton bShare = makeIconButton(ctx, android.R.drawable.ic_menu_send);
            android.widget.ImageButton bTest = makeIconButton(ctx, android.R.drawable.ic_media_play);
            android.widget.ImageButton bEdit = makeIconButton(ctx, android.R.drawable.ic_menu_edit);
            android.widget.ImageButton bDelete = makeIconButton(ctx, android.R.drawable.ic_menu_delete);
            actions.addView(bCall); actions.addView(bSms); actions.addView(bWa); actions.addView(bShare); actions.addView(bTest); actions.addView(bEdit); actions.addView(bDelete);
            card.addView(tvName); card.addView(tvPhone); card.addView(actions);
            return new VH(card);
        }
        @Override public void onBindViewHolder(VH h, int pos){ Caretaker c = items.get(pos); h.name.setText(c.name); h.phone.setText(c.phone);
            h.btnCall.setOnClickListener(v -> {
                android.content.Intent i = new android.content.Intent(android.content.Intent.ACTION_DIAL, android.net.Uri.parse("tel:" + c.phone)); v.getContext().startActivity(i);
            });
            h.btnSms.setOnClickListener(v -> {
                android.content.Intent i = new android.content.Intent(android.content.Intent.ACTION_SENDTO); i.setData(android.net.Uri.parse("smsto:" + c.phone)); i.putExtra("sms_body", "Smart Dosage alerts enabled"); v.getContext().startActivity(i);
            });
            h.btnWa.setOnClickListener(v -> {
                try { android.content.Intent i = new android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://wa.me/" + c.phone)); v.getContext().startActivity(i); } catch (Exception ignore) {}
            });
            h.btnShare.setOnClickListener(v -> {
                android.content.Intent i = new android.content.Intent(android.content.Intent.ACTION_SEND); i.setType("text/plain"); i.putExtra(android.content.Intent.EXTRA_TEXT, c.name + " (" + c.phone + ")"); v.getContext().startActivity(android.content.Intent.createChooser(i, "Share contact"));
            });
            ((android.widget.ImageButton)((android.widget.LinearLayout)h.root.getChildAt(2)).getChildAt(4)).setOnClickListener(v -> {
                android.content.Intent i = new android.content.Intent(android.content.Intent.ACTION_SENDTO); i.setData(android.net.Uri.parse("smsto:" + c.phone)); i.putExtra("sms_body", "Test alert from Smart Dosage"); v.getContext().startActivity(i);
            });
            ((android.widget.ImageButton)((android.widget.LinearLayout)h.root.getChildAt(2)).getChildAt(5)).setOnClickListener(v -> {
                android.widget.EditText etN = new android.widget.EditText(v.getContext()); etN.setHint("Name"); etN.setText(c.name);
                android.widget.EditText etP = new android.widget.EditText(v.getContext()); etP.setHint("Phone"); etP.setInputType(android.text.InputType.TYPE_CLASS_PHONE); etP.setText(c.phone);
                android.widget.LinearLayout cont = new android.widget.LinearLayout(v.getContext()); cont.setOrientation(android.widget.LinearLayout.VERTICAL); cont.setPadding(dp(v.getContext(),16), dp(v.getContext(),8), dp(v.getContext(),16), 0); cont.addView(etN); cont.addView(etP);
                new android.app.AlertDialog.Builder(v.getContext()).setTitle("Edit caretaker").setView(cont).setPositiveButton("Save", (d,w) -> {
                    String nn = etN.getText().toString(); String np = etP.getText().toString();
                    if (nn==null||nn.trim().isEmpty()||np==null||np.trim().isEmpty()||!android.util.Patterns.PHONE.matcher(np).matches()){ android.widget.Toast.makeText(v.getContext(), "Invalid details", android.widget.Toast.LENGTH_SHORT).show(); return; }
                    new Thread(() -> { c.name = nn; c.phone = np; AppDatabase.get(v.getContext()).caretakerDao().update(c); ((CaretakerActivity)v.getContext()).load(); }).start();
                }).setNegativeButton("Cancel", null).show();
            });
            h.btnDelete.setOnClickListener(v -> {
                new android.app.AlertDialog.Builder(v.getContext()).setTitle("Remove caretaker?").setMessage(c.name).setPositiveButton("Remove", (d,w) -> {
                    new Thread(() -> { AppDatabase.get(v.getContext()).caretakerDao().delete(c); ((CaretakerActivity)v.getContext()).load(); }).start();
                }).setNegativeButton("Cancel", null).show();
            });
        }
        @Override public int getItemCount(){ return items.size(); }
        private static int dp(android.content.Context ctx, int d){ return (int)(d * ctx.getResources().getDisplayMetrics().density); }
        private android.widget.ImageButton makeIconButton(android.content.Context ctx, int res){ android.widget.ImageButton b = new android.widget.ImageButton(ctx); b.setImageResource(res); b.setBackgroundColor(0x00000000); android.widget.LinearLayout.LayoutParams p = new android.widget.LinearLayout.LayoutParams(dp(ctx,40), dp(ctx,40)); p.setMargins(dp(ctx,4),0,dp(ctx,4),0); b.setLayoutParams(p); return b; }
    }
}
