package com.example.smart_dosage;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.graphics.Color;

import org.json.JSONArray;
import org.json.JSONObject;

public class BudgetActivity extends AppCompatActivity {
    private RecyclerView rv;
    private ItemAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget);

        android.widget.EditText name = findViewById(R.id.et_item_name);
        android.widget.EditText cost = findViewById(R.id.et_item_cost);
        android.widget.TextView total = findViewById(R.id.tv_total);
        rv = findViewById(R.id.rv_budget_items);
        if (rv != null) { rv.setLayoutManager(new LinearLayoutManager(this)); adapter = new ItemAdapter(); rv.setAdapter(adapter); }

        findViewById(R.id.btn_add_item).setOnClickListener(v -> {
            try {
                String n = name.getText().toString();
                double c = Double.parseDouble(cost.getText().toString());
                android.content.SharedPreferences sp = getSharedPreferences("budget", MODE_PRIVATE);
                String raw = sp.getString("items", "[]");
                JSONArray arr = new JSONArray(raw);
                JSONObject obj = new JSONObject();
                obj.put("name", n);
                obj.put("cost", c);
                obj.put("ts", System.currentTimeMillis());
                arr.put(obj);
                sp.edit().putString("items", arr.toString()).apply();
                name.setText(""); cost.setText("");
                Toast.makeText(this, "Added", Toast.LENGTH_SHORT).show();
                updateTotal(total);
                loadList();
            } catch (Exception e) {
                Toast.makeText(this, "Invalid", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btn_refresh).setOnClickListener(v -> { updateTotal(total); loadList(); });
        updateTotal(total);
        loadList();
    }

    private void updateTotal(android.widget.TextView total) {
        android.content.SharedPreferences sp = getSharedPreferences("budget", MODE_PRIVATE);
        String raw = sp.getString("items", "[]");
        try {
            JSONArray arr = new JSONArray(raw);
            double sum = 0;
            for (int i = 0; i < arr.length(); i++) sum += arr.getJSONObject(i).getDouble("cost");
            total.setText("Estimated monthly spend: " + sum);
        } catch (Exception e) {
            total.setText("No data");
        }
    }

    private void loadList() {
        try {
            android.content.SharedPreferences sp = getSharedPreferences("budget", MODE_PRIVATE);
            String raw = sp.getString("items", "[]");
            JSONArray arr = new JSONArray(raw);
            java.util.List<JSONObject> list = new java.util.ArrayList<>();
            for (int i = 0; i < arr.length(); i++) list.add(arr.getJSONObject(i));
            java.util.Collections.sort(list, (a,b) -> {
                long ta = a.optLong("ts", 0L); long tb = b.optLong("ts", 0L);
                return Long.compare(tb, ta); // recent first
            });
            if (adapter != null) adapter.setItems(list);
        } catch (Exception ignore) {}
    }

    private class ItemAdapter extends RecyclerView.Adapter<ItemVH> {
        private final java.util.List<JSONObject> items = new java.util.ArrayList<>();
        void setItems(java.util.List<JSONObject> it){ items.clear(); items.addAll(it); notifyDataSetChanged(); }
        @Override public ItemVH onCreateViewHolder(android.view.ViewGroup parent, int viewType){
            android.view.View v = android.view.LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
            return new ItemVH(v);
        }
        @Override public void onBindViewHolder(ItemVH h, int pos){
            JSONObject o = items.get(pos);
            String n = o.optString("name","(unknown)");
            double c = o.optDouble("cost",0);
            long ts = o.optLong("ts",0);
            h.t1.setText(n);
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
            String when = ts==0?"":sdf.format(new java.util.Date(ts));
            h.t2.setText("₹"+c+ (when.isEmpty()?"":"  •  " + when));
            h.t1.setTextColor(Color.BLACK);
            h.t2.setTextColor(Color.BLACK);
            h.itemView.setOnClickListener(v -> showDetails(o));
        }
        @Override public int getItemCount(){ return items.size(); }
    }

    private static class ItemVH extends RecyclerView.ViewHolder {
        android.widget.TextView t1,t2;
        ItemVH(android.view.View v){ super(v); t1=v.findViewById(android.R.id.text1); t2=v.findViewById(android.R.id.text2); }
    }

    private void showDetails(JSONObject o){
        String n = o.optString("name","(unknown)");
        double c = o.optDouble("cost",0);
        long ts = o.optLong("ts",0);
        double yearly = c*12.0;
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
        String when = ts==0?"Unknown":sdf.format(new java.util.Date(ts));
        android.widget.LinearLayout cont = new android.widget.LinearLayout(this);
        cont.setOrientation(android.widget.LinearLayout.VERTICAL);
        int pad = (int)(16 * getResources().getDisplayMetrics().density);
        cont.setPadding(pad, pad/2, pad, pad/2);
        android.widget.TextView tv1 = new android.widget.TextView(this);
        android.widget.TextView tv2 = new android.widget.TextView(this);
        android.widget.TextView tv3 = new android.widget.TextView(this);
        tv1.setText("Name: " + n);
        tv2.setText("Monthly: ₹" + c + "    Yearly: ₹" + yearly);
        tv3.setText("Added: " + when);
        tv1.setTextColor(Color.BLACK);
        tv2.setTextColor(Color.BLACK);
        tv3.setTextColor(Color.BLACK);
        cont.addView(tv1); cont.addView(tv2); cont.addView(tv3);
        new android.app.AlertDialog.Builder(this)
                .setTitle("Budget item")
                .setView(cont)
                .setPositiveButton("Close", null)
                .setNeutralButton("Edit", (d,w) -> showEditDialog(o))
                .setNegativeButton("Delete", (d,w) -> confirmDelete(o.optLong("ts",0)))
                .show();
    }

    private void showEditDialog(JSONObject o){
        String n = o.optString("name","(unknown)");
        double c = o.optDouble("cost",0);
        android.widget.EditText etName = new android.widget.EditText(this);
        android.widget.EditText etCost = new android.widget.EditText(this);
        etName.setHint("Name"); etName.setText(n);
        etCost.setHint("Monthly cost"); etCost.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL); etCost.setText(String.valueOf(c));
        etName.setTextColor(Color.BLACK); etCost.setTextColor(Color.BLACK);
        etName.setHintTextColor(Color.BLACK); etCost.setHintTextColor(Color.BLACK);
        android.widget.LinearLayout cont = new android.widget.LinearLayout(this);
        cont.setOrientation(android.widget.LinearLayout.VERTICAL);
        int pad = (int)(16 * getResources().getDisplayMetrics().density);
        cont.setPadding(pad, pad/2, pad, pad/2);
        cont.addView(etName); cont.addView(etCost);
        new android.app.AlertDialog.Builder(this)
                .setTitle("Edit item")
                .setView(cont)
                .setPositiveButton("Save", (d,w) -> {
                    String nn = etName.getText().toString();
                    double cc = 0; try { cc = Double.parseDouble(etCost.getText().toString()); } catch (Exception ignore) {}
                    updateItem(o.optLong("ts",0), nn, cc);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmDelete(long ts){
        new android.app.AlertDialog.Builder(this)
                .setTitle("Delete item")
                .setMessage("Remove this budget entry?")
                .setPositiveButton("Delete", (d,w) -> deleteItem(ts))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateItem(long ts, String name, double cost){
        try {
            android.content.SharedPreferences sp = getSharedPreferences("budget", MODE_PRIVATE);
            String raw = sp.getString("items", "[]");
            JSONArray arr = new JSONArray(raw);
            for (int i=0;i<arr.length();i++){
                JSONObject o = arr.getJSONObject(i);
                if (o.optLong("ts",0)==ts){ o.put("name", name); o.put("cost", cost); break; }
            }
            sp.edit().putString("items", arr.toString()).apply();
            android.widget.TextView total = findViewById(R.id.tv_total);
            updateTotal(total);
            loadList();
        } catch (Exception ignore) {}
    }

    private void deleteItem(long ts){
        try {
            android.content.SharedPreferences sp = getSharedPreferences("budget", MODE_PRIVATE);
            String raw = sp.getString("items", "[]");
            JSONArray arr = new JSONArray(raw);
            JSONArray out = new JSONArray();
            for (int i=0;i<arr.length();i++){
                JSONObject o = arr.getJSONObject(i);
                if (o.optLong("ts",0)!=ts) out.put(o);
            }
            sp.edit().putString("items", out.toString()).apply();
            android.widget.TextView total = findViewById(R.id.tv_total);
            updateTotal(total);
            loadList();
        } catch (Exception ignore) {}
    }
}
