package com.example.smart_dosage;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.animation.ObjectAnimator;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smart_dosage.data.AppDatabase;
import com.example.smart_dosage.data.DoseEvent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoryActivity extends AppCompatActivity {
    private final List<Map<String, String>> timeline = new ArrayList<>();
    private TimelineAdapter timelineAdapter;
    private CalendarAdapter calendarAdapter;
    private java.util.Date selectedDay;
    private final Map<Long, String> medNameCache = new HashMap<>();
    private Calendar viewMonthCal = Calendar.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        TextView tvMetrics = findViewById(R.id.tv_metrics);
        TextView tvStreak = findViewById(R.id.tv_streak);
        RecyclerView rvTimeline = findViewById(R.id.rv_timeline);
        RecyclerView rvCalendar = findViewById(R.id.rv_calendar);
        rvTimeline.setLayoutManager(new LinearLayoutManager(this));
        timelineAdapter = new TimelineAdapter(timeline);
        rvTimeline.setAdapter(timelineAdapter);
        rvCalendar.setLayoutManager(new GridLayoutManager(this, 7));
        calendarAdapter = new CalendarAdapter(new ArrayList<>(), day -> {
            selectedDay = day;
            showDayActions(day);
        });
        rvCalendar.setAdapter(calendarAdapter);

        viewMonthCal.set(Calendar.DAY_OF_MONTH, 1);
        loadStats(tvMetrics, tvStreak);
        findViewById(R.id.btn_day).setOnClickListener(v -> loadStats(tvMetrics, tvStreak));
        findViewById(R.id.btn_week).setOnClickListener(v -> loadStats(tvMetrics, tvStreak));
        findViewById(R.id.btn_month).setOnClickListener(v -> loadStats(tvMetrics, tvStreak));

        View prev = findViewById(R.id.btn_prev_month);
        View next = findViewById(R.id.btn_next_month);
        TextView tvTitle = findViewById(R.id.tv_month_title);
        if (tvTitle != null) tvTitle.setText(new java.text.SimpleDateFormat("MMMM yyyy").format(viewMonthCal.getTime()));
        if (prev != null) prev.setOnClickListener(v -> { viewMonthCal.add(Calendar.MONTH, -1); if (tvTitle != null) tvTitle.setText(new java.text.SimpleDateFormat("MMMM yyyy").format(viewMonthCal.getTime())); loadStats(tvMetrics, tvStreak); });
        if (next != null) next.setOnClickListener(v -> { viewMonthCal.add(Calendar.MONTH, 1); if (tvTitle != null) tvTitle.setText(new java.text.SimpleDateFormat("MMMM yyyy").format(viewMonthCal.getTime())); loadStats(tvMetrics, tvStreak); });
    }

    private void showDayActions(java.util.Date day){
        String[] options = new String[]{"Taken","Snoozed","Missed"};
        new android.app.AlertDialog.Builder(this)
                .setTitle(new SimpleDateFormat("EEE, MMM d").format(day))
                .setItems(options, (d,w) -> {
                    String act = w==0?"TAKEN":(w==1?"SNOOZE":"MISSED");
                    markDayStatus(day, act);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void markDayStatus(java.util.Date day, String action){
        new Thread(() -> {
            Calendar c = Calendar.getInstance(); c.setTime(day); c.set(Calendar.HOUR_OF_DAY, 12); c.set(Calendar.MINUTE, 0); c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0);
            com.example.smart_dosage.data.DoseEvent e = new com.example.smart_dosage.data.DoseEvent();
            e.medicineId = -1; // manual mark
            e.scheduledTime = c.getTime();
            e.action = action;
            e.actionTime = new java.util.Date();
            AppDatabase.get(this).doseEventDao().insert(e);
            runOnUiThread(() -> {
                loadTimelineForDay(day);
                TextView tvMetrics = findViewById(R.id.tv_metrics);
                TextView tvStreak = findViewById(R.id.tv_streak);
                loadStats(tvMetrics, tvStreak);
            });
        }).start();
    }

    private void loadStats(TextView tvMetrics, TextView tvStreak) {
        new Thread(() -> {
            Calendar cal = Calendar.getInstance();
            java.util.Date today = cal.getTime();
            java.util.Date weekStart, monthStart, monthEnd;
            cal.add(Calendar.DAY_OF_YEAR, -6);
            weekStart = cal.getTime();
            Calendar calMonth = (Calendar) viewMonthCal.clone();
            calMonth.set(Calendar.HOUR_OF_DAY, 0); calMonth.set(Calendar.MINUTE, 0); calMonth.set(Calendar.SECOND, 0); calMonth.set(Calendar.MILLISECOND, 0);
            monthStart = calMonth.getTime();
            Calendar calMonthEnd = (Calendar) calMonth.clone();
            calMonthEnd.add(Calendar.MONTH, 1); calMonthEnd.add(Calendar.MILLISECOND, -1);
            monthEnd = calMonthEnd.getTime();

            int takenWeek = AppDatabase.get(this).doseEventDao().countTaken(weekStart, today);
            int scheduledWeek = AppDatabase.get(this).doseEventDao().countScheduled(weekStart, today);
            int weekPct = scheduledWeek == 0 ? 0 : (int) Math.round((takenWeek * 100.0) / scheduledWeek);

            int takenMonth = AppDatabase.get(this).doseEventDao().countTaken(monthStart, monthEnd);
            int scheduledMonth = AppDatabase.get(this).doseEventDao().countScheduled(monthStart, monthEnd);
            int monthPct = scheduledMonth == 0 ? 0 : (int) Math.round((takenMonth * 100.0) / scheduledMonth);

            List<DoseEvent> monthEvents = AppDatabase.get(this).doseEventDao().historyAllList(monthStart, monthEnd);
            Map<String, DayStats> dayStatsMonth = new HashMap<>(); // key=yyyy-MM-dd
            SimpleDateFormat dayFmt = new SimpleDateFormat("yyyy-MM-dd");
            if (monthEvents != null) {
                for (DoseEvent e : monthEvents) {
                    String key = dayFmt.format(e.scheduledTime != null ? e.scheduledTime : e.actionTime);
                    DayStats stats = dayStatsMonth.getOrDefault(key, new DayStats());
                    if ("SCHEDULED".equalsIgnoreCase(e.action)) stats.scheduled += 1;
                    else if ("TAKEN".equalsIgnoreCase(e.action)) stats.taken += 1;
                    else if ("SNOOZE".equalsIgnoreCase(e.action) || "SNOOZED".equalsIgnoreCase(e.action)) stats.snoozed = true;
                    else if ("MISSED".equalsIgnoreCase(e.action)) stats.missed = true;
                    dayStatsMonth.put(key, stats);
                }
            }

            Calendar rangeCal = Calendar.getInstance();
            rangeCal.add(Calendar.DAY_OF_YEAR, -365);
            java.util.Date rangeStart = rangeCal.getTime();
            List<DoseEvent> allEvents = AppDatabase.get(this).doseEventDao().historyAllList(rangeStart, today);
            Map<String, DayStats> dayStatsAll = new HashMap<>();
            if (allEvents != null) {
                for (DoseEvent e : allEvents) {
                    String key = dayFmt.format(e.scheduledTime != null ? e.scheduledTime : e.actionTime);
                    DayStats stats = dayStatsAll.getOrDefault(key, new DayStats());
                    if ("SCHEDULED".equalsIgnoreCase(e.action)) stats.scheduled += 1;
                    else if ("TAKEN".equalsIgnoreCase(e.action)) stats.taken += 1;
                    else if ("SNOOZE".equalsIgnoreCase(e.action) || "SNOOZED".equalsIgnoreCase(e.action)) stats.snoozed = true;
                    else if ("MISSED".equalsIgnoreCase(e.action)) stats.missed = true;
                    dayStatsAll.put(key, stats);
                }
            }

            int streak = 0; // consecutive green days across months
            Calendar scan = Calendar.getInstance();
            for (int i=0;i<365;i++){
                String k = dayFmt.format(scan.getTime());
                DayStats s = dayStatsAll.get(k);
                int status = computeStatus(s);
                if (status == 3) streak++; else break;
                scan.add(Calendar.DAY_OF_YEAR, -1);
            }

            List<DayCell> monthCells = buildMonthCells(dayStatsMonth, viewMonthCal);
            final int finalWeekPct = weekPct;
            final int finalMonthPct = monthPct;
            int totalGreen = 0;
            Calendar countCal = Calendar.getInstance();
            countCal.setTime(today);
            Calendar begin = Calendar.getInstance(); begin.add(Calendar.DAY_OF_YEAR, -365);
            while (!countCal.before(begin)) {
                String k = dayFmt.format(countCal.getTime());
                DayStats s = dayStatsAll.get(k);
                if (computeStatus(s) == 3) totalGreen++;
                countCal.add(Calendar.DAY_OF_YEAR, -1);
            }
            final int finalStreak = totalGreen;
            runOnUiThread(() -> {
                ((android.widget.ProgressBar)findViewById(R.id.pb_week)).setProgress(finalWeekPct);
                tvMetrics.setText("Weekly adherence: " + finalWeekPct + "% • Monthly: " + finalMonthPct + "%");
                tvStreak.setText("🏅 Streak: " + finalStreak + " days");
                ObjectAnimator anim = ObjectAnimator.ofFloat(tvStreak, "scaleX", 1f, 1.06f, 1f);
                anim.setDuration(800); anim.start();
                ObjectAnimator anim2 = ObjectAnimator.ofFloat(tvStreak, "scaleY", 1f, 1.06f, 1f); anim2.setDuration(800); anim2.start();

                calendarAdapter.setCells(monthCells);
                if (selectedDay == null) selectedDay = new java.util.Date();
                loadTimelineForDay(selectedDay);

                android.widget.FrameLayout pie = findViewById(R.id.pie_container);
                pie.removeAllViews();
                pie.addView(new PieChartView(HistoryActivity.this, monthPct));
            });
        }).start();
    }

    private List<DayCell> buildMonthCells(Map<String,DayStats> dayStats, Calendar base){
        List<DayCell> cells = new ArrayList<>();
        Calendar cal = (Calendar) base.clone();
        cal.set(Calendar.DAY_OF_MONTH,1); cal.set(Calendar.HOUR_OF_DAY,0); cal.set(Calendar.MINUTE,0); cal.set(Calendar.SECOND,0); cal.set(Calendar.MILLISECOND,0);
        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK); // 1=Sunday
        int offset = (firstDayOfWeek + 6) % 7; // make Monday=0
        for (int i=0;i<offset;i++) cells.add(new DayCell(null, 0));
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        for (int d=1; d<=daysInMonth; d++){
            cal.set(Calendar.DAY_OF_MONTH,d);
            String k = fmt.format(cal.getTime());
            DayStats s = dayStats.get(k);
            int status = computeStatus(s);
            cells.add(new DayCell(cal.getTime(), status));
        }
        return cells;
    }

    private int computeStatus(DayStats s){
        if (s == null) return 0;
        // 0=none,1=missed(red),2=partial/snoozed(yellow),3=full(green)
        if (s.taken > 0 && (s.scheduled == 0 || s.taken >= s.scheduled)) return 3;
        if (s.snoozed) return 2;
        if (s.missed || (s.scheduled > 0 && s.taken == 0)) return 1;
        if (s.taken > 0 && s.taken < s.scheduled) return 2;
        return 0;
    }

    private void loadTimelineForDay(java.util.Date day){
        new Thread(() -> {
            Calendar start = Calendar.getInstance(); start.setTime(day); start.set(Calendar.HOUR_OF_DAY,0); start.set(Calendar.MINUTE,0); start.set(Calendar.SECOND,0); start.set(Calendar.MILLISECOND,0);
            Calendar end = Calendar.getInstance(); end.setTime(day); end.set(Calendar.HOUR_OF_DAY,23); end.set(Calendar.MINUTE,59); end.set(Calendar.SECOND,59); end.set(Calendar.MILLISECOND,999);
            List<DoseEvent> events = AppDatabase.get(this).doseEventDao().historyAllList(start.getTime(), end.getTime());
            List<Map<String,String>> maps = new ArrayList<>();
            SimpleDateFormat sdf = new SimpleDateFormat("h:mm a");
            if (events != null){
                for (DoseEvent e : events){
                    Map<String,String> m = new HashMap<>();
                    String act = e.action;
                    String medName = getMedName(e.medicineId);
                    String label = (e.medicineId==-1)?"Manual":(medName!=null?medName:("Medicine " + e.medicineId));
                    String title = sdf.format(e.scheduledTime==null?e.actionTime:e.scheduledTime) + " — " + label + " — " + act;
                    String subtitle = ("TAKEN".equals(act) && e.actionTime!=null) ? ("Taken at " + sdf.format(e.actionTime)) : "";
                    m.put("title", title);
                    m.put("subtitle", subtitle);
                    maps.add(m);
                }
            }
            runOnUiThread(() -> { timeline.clear(); timeline.addAll(maps); timelineAdapter.notifyDataSetChanged(); });
        }).start();
    }

    private String getMedName(long id){
        String cached = medNameCache.get(id);
        if (cached != null) return cached;
        com.example.smart_dosage.data.Medicine m = AppDatabase.get(this).medicineDao().getById(id);
        if (m != null && m.name != null){ medNameCache.put(id, m.name); return m.name; }
        return null;
    }

    static class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.VH> {
        private final List<Map<String,String>> items;
        TimelineAdapter(List<Map<String,String>> items){ this.items = items; }
        static class VH extends RecyclerView.ViewHolder { TextView t1; TextView t2; VH(View v){ super(v); t1=v.findViewById(android.R.id.text1); t2=v.findViewById(android.R.id.text2);} }
        @Override public VH onCreateViewHolder(ViewGroup parent, int viewType){ View v = android.view.LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false); return new VH(v);}    
        @Override public void onBindViewHolder(VH h, int pos){ Map<String,String> m = items.get(pos); h.t1.setText(m.get("title")); h.t2.setText(m.get("subtitle")); }
        @Override public int getItemCount(){ return items.size(); }
    }

    static class DayCell { final java.util.Date day; final int status; DayCell(java.util.Date d,int s){ day=d; status=s; }}
    static class DayStats { int scheduled; int taken; boolean snoozed; boolean missed; }
    interface OnDayClick { void onClick(java.util.Date day); }
    static class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.VH> {
        private final List<DayCell> cells; private final OnDayClick onDayClick;
        CalendarAdapter(List<DayCell> cells, OnDayClick click){ this.cells=cells; this.onDayClick=click; }
        void setCells(List<DayCell> newCells){ cells.clear(); cells.addAll(newCells); notifyDataSetChanged(); }
        static class VH extends RecyclerView.ViewHolder { TextView tv; VH(TextView v){ super(v); tv=v; } }
        @Override public VH onCreateViewHolder(ViewGroup parent,int viewType){ TextView tv=new TextView(parent.getContext()); tv.setPadding(8,16,8,16); tv.setTextSize(16f); tv.setGravity(android.view.Gravity.CENTER); return new VH(tv);}    
        @Override public void onBindViewHolder(VH h, int pos){
            DayCell c=cells.get(pos);
            if (c.day==null){ h.tv.setText(""); h.tv.setBackgroundColor(0x00000000); h.tv.setOnClickListener(null); return; }
            java.util.Calendar cal=java.util.Calendar.getInstance(); cal.setTime(c.day);
            h.tv.setText(String.valueOf(cal.get(java.util.Calendar.DAY_OF_MONTH)));
            int fill = 0xFF3A3A3A; // default grey
            int text = 0xFFFFFFFF;
            if (c.status==3) fill=0xFF2E7D32; // green full
            else if (c.status==2) fill=0xFFFFC107; // amber partial
            else if (c.status==1) fill=0xFFB00020; // red missed
            android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
            bg.setCornerRadius(24f);
            bg.setColor(fill);
            bg.setStroke(2, 0xFFFFFFFF);
            h.tv.setTextColor(text);
            h.tv.setBackground(bg);
            h.tv.setOnClickListener(v -> onDayClick.onClick(c.day));
        }
        @Override public int getItemCount(){ return cells.size(); }
    }

    static class PieChartView extends View {
        private final int pct;
        private final Paint pFill = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint pBg = new Paint(Paint.ANTI_ALIAS_FLAG);
        PieChartView(android.content.Context ctx, int pct){ super(ctx); this.pct=pct; pFill.setStyle(Paint.Style.FILL); pFill.setColor(0xFFBB86FC); pBg.setStyle(Paint.Style.FILL); pBg.setColor(0xFF3E3E3E); }
        @Override protected void onDraw(Canvas c){ super.onDraw(c); float w=getWidth(), h=getHeight(); float r=Math.min(w,h)/2f-8f; float cx=w/2f, cy=h/2f; RectF oval=new RectF(cx-r, cy-r, cx+r, cy+r); c.drawArc(oval, 0, 360, true, pBg); c.drawArc(oval, -90, 360f*pct/100f, true, pFill); Paint pText=new Paint(Paint.ANTI_ALIAS_FLAG); pText.setColor(0xFFFFFFFF); pText.setTextSize(36f); pText.setTextAlign(Paint.Align.CENTER); c.drawText(pct+"%", cx, cy+12f, pText);}        
    }
}
