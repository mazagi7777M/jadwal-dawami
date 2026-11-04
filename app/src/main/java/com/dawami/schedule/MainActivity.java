package com.dawami.schedule;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final LocalDate START = LocalDate.of(2026,11,4);
    private static final LocalDate END = LocalDate.of(2030,1,1);
    private static final String[] SHIFTS = {"نهار", "نهار", "صباح", "صباح", "إجازة", "إجازة"};
    private static final int[] COLORS = {0xffd7eafe,0xffd7eafe,0xffffefc2,0xffffefc2,0xffd6f4e2,0xffd6f4e2};
    private final Locale AR = new Locale("ar");
    private YearMonth current = YearMonth.from(START);
    private LinearLayout root, grid;
    private TextView monthLabel, info;
    private int dp(int v) { return (int)(v*getResources().getDisplayMetrics().density+0.5f); }
    private GradientDrawable bg(int color, int radius) { GradientDrawable d=new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(radius)); return d; }
    private TextView text(String s,int size,boolean bold) { TextView t=new TextView(this); t.setText(s);t.setTextSize(size);t.setTextColor(0xff182333);t.setGravity(Gravity.CENTER);t.setPadding(dp(3),dp(8),dp(3),dp(8));if(bold)t.setTypeface(null,Typeface.BOLD);return t; }
    private int phase(LocalDate d) { return (int)(Math.floorMod(java.time.temporal.ChronoUnit.DAYS.between(START,d),6)); }
    private boolean valid(LocalDate d) { return !d.isBefore(START)&&!d.isAfter(END); }
    @Override public void onCreate(Bundle b) {super.onCreate(b);getWindow().setStatusBarColor(0xff152a45);getWindow().setNavigationBarColor(Color.BLACK);build();}
    private void build() {
        ScrollView scroll=new ScrollView(this);root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(14),dp(18),dp(14),dp(22));root.setBackgroundColor(0xfff5f7fb);root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);scroll.addView(root);setContentView(scroll);
        TextView title=text("جدول دوامي",28,true);root.addView(title);
        TextView sub=text("يومين نهار • يومين صباح • يومين إجازة",15,false);root.addView(sub);
        TextView period=text("4 نوفمبر 2026 — 1 يناير 2030",13,false);root.addView(period);
        LinearLayout nav=new LinearLayout(this);nav.setGravity(Gravity.CENTER);nav.setOrientation(LinearLayout.HORIZONTAL);root.addView(nav);
        Button prev=new Button(this);prev.setText("▶");nav.addView(prev);
        monthLabel=text("",19,true);nav.addView(monthLabel,new LinearLayout.LayoutParams(0,dp(55),1));
        Button next=new Button(this);next.setText("◀");nav.addView(next);
        prev.setOnClickListener(v->{YearMonth p=current.minusMonths(1);if(!p.isBefore(YearMonth.from(START))){current=p;render();}});
        next.setOnClickListener(v->{YearMonth n=current.plusMonths(1);if(!n.isAfter(YearMonth.from(END))){current=n;render();}});
        LinearLayout legend=new LinearLayout(this);legend.setGravity(Gravity.CENTER);root.addView(legend);
        for(int i:new int[]{0,2,4}){TextView l=text(SHIFTS[i],13,true);l.setBackground(bg(COLORS[i],8));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(40),1);lp.setMargins(dp(3),dp(4),dp(3),dp(9));legend.addView(l,lp);}
        grid=new LinearLayout(this);grid.setOrientation(LinearLayout.VERTICAL);root.addView(grid);
        info=text("اضغط على أي تاريخ لمعرفة نوع الدوام",16,true);root.addView(info);
        Button today=new Button(this);today.setText("الانتقال إلى اليوم");root.addView(today);today.setOnClickListener(v->{LocalDate d=LocalDate.now();if(valid(d)){current=YearMonth.from(d);render();showDay(d);}});
        Button export=new Button(this);export.setText("مشاركة الجدول CSV");root.addView(export);export.setOnClickListener(v->shareCsv());
        render();
    }
    private void render(){monthLabel.setText(current.atDay(1).format(DateTimeFormatter.ofPattern("MMMM yyyy",AR)));grid.removeAllViews();String[] days={"س","ح","ن","ث","ر","خ","ج"}; // السبت إلى الجمعة
        LinearLayout header=new LinearLayout(this);grid.addView(header);for(String s:days){TextView t=text(s,14,true);header.addView(t,new LinearLayout.LayoutParams(0,dp(38),1));}
        int offset=(current.atDay(1).getDayOfWeek().getValue()+1)%7;
        int length=current.lengthOfMonth();int rows=(offset+length+6)/7;
        for(int r=0;r<rows;r++){LinearLayout row=new LinearLayout(this);grid.addView(row);for(int c=0;c<7;c++){
            int n=r*7+c-offset+1;TextView cell=text("",13,false);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(76),1);lp.setMargins(dp(2),dp(2),dp(2),dp(2));row.addView(cell,lp);
            if(n>=1&&n<=length){LocalDate d=current.atDay(n);if(valid(d)){int p=phase(d);cell.setText(n+"\n"+SHIFTS[p]);cell.setBackground(bg(COLORS[p],8));cell.setOnClickListener(v->showDay(d));}else{cell.setText(""+n);cell.setBackground(bg(0xffe9e9e9,8));}}}
        }
    }
    private void showDay(LocalDate d){info.setText(d.format(DateTimeFormatter.ofPattern("EEEE، d MMMM yyyy",AR))+"\n"+SHIFTS[phase(d)]);}
    private void shareCsv(){StringBuilder csv=new StringBuilder("Date,Shift\n");for(LocalDate d=START;!d.isAfter(END);d=d.plusDays(1))csv.append(d).append(',').append(SHIFTS[phase(d)]).append('\n');Intent send=new Intent(Intent.ACTION_SEND);send.setType("text/plain");send.putExtra(Intent.EXTRA_SUBJECT,"جدول دوامي 2026-2030.csv");send.putExtra(Intent.EXTRA_TEXT,csv.toString());startActivity(Intent.createChooser(send,"مشاركة جدول الدوام"));}
}
