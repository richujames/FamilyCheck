package org.familycheck.senior;
import android.app.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
/** Small view helpers; all screens use ordinary Android widgets. */
public class Ui {
    public static final int INK=Color.rgb(23,46,60), TEAL=Color.rgb(20,125,112), MUTED=Color.rgb(82,103,115);
    public static int dp(Context c,int n) {
        return (int)(n*c.getResources().getDisplayMetrics().density);
    }
    public static boolean large(Context c) {
        return c.getSharedPreferences("settings",0).getBoolean("large",false);
    }
    public static float size(Context c,int n) {
        return n+(large(c)?3:0);
    }
    public static LinearLayout column(Context c) {
        LinearLayout l=new LinearLayout(c);
        l.setOrientation(1);
        return l;
    }
    public static TextView text(LinearLayout parent,String value,int size,boolean bold) {
        Context c=parent.getContext();
        TextView t=new TextView(c);
        t.setText(value);
        t.setTextSize(size(c,size));
        t.setTextColor(INK);
        if(bold)t.setTypeface(null,Typeface.BOLD);
        t.setPadding(0,dp(c,6),0,dp(c,10));
        parent.addView(t);
        return t;
    }
    public static GradientDrawable background(Context c,int color) {
        GradientDrawable d=new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(c,16));
        return d;
    }
    public static Button button(LinearLayout parent,String title,boolean primary,Runnable action) {
        Context c=parent.getContext();
        Button b=new Button(c);
        b.setText(title);
        b.setTextSize(size(c,16));
        b.setTextColor(primary?Color.WHITE:TEAL);
        b.setBackground(background(c,primary?TEAL:Color.rgb(225,240,236)));
        b.setPadding(dp(c,12),dp(c,10),dp(c,12),dp(c,10));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);
        p.setMargins(0,dp(c,6),0,dp(c,6));
        parent.addView(b,p);
        b.setOnClickListener(v->action.run());
        return b;
    }
    public static LinearLayout card(LinearLayout parent) {
        Context c=parent.getContext();
        LinearLayout l=column(c);
        l.setBackgroundResource(R.drawable.card);
        int p=dp(c,18);
        l.setPadding(p,p,p,p);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
        lp.setMargins(0,dp(c,6),0,dp(c,10));
        parent.addView(l,lp);
        return l;
    }
    public static void toast(Context c,String text) {
        Toast.makeText(c,text,Toast.LENGTH_LONG).show();
    }
    public static void launch(Context c,Intent intent) {
        try {
            c.startActivity(intent);
        }
        catch(ActivityNotFoundException e) {
            toast(c,"No compatible app is installed.");
        }
    }
    public static void share(Context c,String text) {
        launch(c,Intent.createChooser(new Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT,text),"Choose who to ask"));
    }
    public static void insets(View root) {
        root.setOnApplyWindowInsetsListener((v,i)-> {
            v.setPadding(i.getSystemWindowInsetLeft(),i.getSystemWindowInsetTop(),i.getSystemWindowInsetRight(),i.getSystemWindowInsetBottom());
            return i;
        }
        );
    }
}
