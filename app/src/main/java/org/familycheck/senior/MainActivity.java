package org.familycheck.senior;
import android.app.*;
import android.os.Bundle;
import android.content.Intent;
import android.graphics.Color;
import android.view.*;
import android.widget.*;
public class MainActivity extends Activity {
    private int tab;
    private LinearLayout nav;
    private View drawer;
    private final String[] names= {
        "Home","Checks","People","Learn"
    }
    ;
    private final int[] icons= {
        android.R.drawable.ic_menu_view,android.R.drawable.ic_menu_recent_history,android.R.drawable.ic_menu_call,android.R.drawable.ic_menu_info_details
    }
    ;
    @Override public void onCreate(Bundle saved) {
        super.onCreate(saved);
        setContentView(R.layout.activity_main);
        Ui.insets(findViewById(R.id.root));
        Toolbar toolbar=findViewById(R.id.toolbar);
        toolbar.setTitle("FamilyCheck");
        toolbar.setTitleTextColor(Ui.INK);
        setActionBar(toolbar);
        toolbar.setNavigationIcon(android.R.drawable.ic_menu_sort_by_size);
        toolbar.setNavigationContentDescription("Open navigation");
        toolbar.setNavigationOnClickListener(v->toggleDrawer());
        nav=findViewById(R.id.nav);
        for(int i=0;i<4;i++) {
            final int index=i;
            Button b=new Button(this);
            b.setText(names[i]);
            b.setTextSize(12);
            b.setAllCaps(false);
            b.setMinWidth(0);
            b.setMinimumWidth(0);
            b.setPadding(0,Ui.dp(this,6),0,Ui.dp(this,6));
            android.graphics.drawable.Drawable icon=getDrawable(icons[i]);
            icon.setTint(Ui.TEAL);
            b.setCompoundDrawablesWithIntrinsicBounds(null,icon,null,null);
            nav.addView(b,new LinearLayout.LayoutParams(0,-2,1));
            b.setOnClickListener(v->select(index));
        }
        buildDrawer();
        tab=saved==null?getIntent().getIntExtra("tab",0):saved.getInt("tab");
        if(saved==null)select(tab);
        else highlight();
    }
    private void buildDrawer() {
        FrameLayout overlay=new FrameLayout(this);
        overlay.setBackgroundColor(0x66000000);
        overlay.setVisibility(View.GONE);
        overlay.setOnClickListener(v->toggleDrawer());
        drawer=overlay;
        ScrollView scroll=new ScrollView(this);
        LinearLayout panel=Ui.column(this);
        panel.setBackgroundColor(Color.WHITE);
        panel.setPadding(20,30,20,20);
        panel.setClickable(true);
        scroll.addView(panel);
        Ui.text(panel,"A calmer next step",24,true);
        for(int i=0;i<4;i++) {
            final int index=i;
            Ui.button(panel,names[i],false,()-> {
                select(index);
                toggleDrawer();
            }
            );
        }
        Ui.button(panel,"Follow-up reminder",false,()->open(ReminderActivity.class));
        Ui.button(panel,"Reading settings",false,this::settings);
        overlay.addView(scroll,new FrameLayout.LayoutParams(Ui.dp(this,280),-1,Gravity.START));
        ((FrameLayout)findViewById(R.id.middle)).addView(overlay,new FrameLayout.LayoutParams(-1,-1));
    }
    private void toggleDrawer() {
        boolean visible=drawer.getVisibility()!=View.VISIBLE;
        drawer.setVisibility(visible?View.VISIBLE:View.GONE);
        findViewById(R.id.host).setImportantForAccessibility(visible?View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS:View.IMPORTANT_FOR_ACCESSIBILITY_AUTO);
    }
    public void select(int t) {
        tab=Math.max(0,Math.min(3,t));
        getFragmentManager().beginTransaction().replace(R.id.host,DashboardFragment.create(tab)).commit();
        highlight();
    }
    public void refresh() {
        Fragment f=getFragmentManager().findFragmentById(R.id.host);
        if(f instanceof DashboardFragment)((DashboardFragment)f).refresh();
    }
    private void highlight() {
        for(int i=0;i<4;i++) {
            Button b=(Button)nav.getChildAt(i);
            b.setBackground(Ui.background(this,i==tab?0xFFE1F0EC:Color.WHITE));
            b.setTextColor(i==tab?Ui.TEAL:Ui.MUTED);
            b.setSelected(i==tab);
            b.setContentDescription(names[i]+(i==tab?", selected":""));
        }
    }
    private void open(Class<?> c) {
        startActivity(new Intent(this,c));
    }
    private void settings() {
        boolean[] value= {
            Ui.large(this)
        }
        ;
        new AlertDialog.Builder(this).setTitle("Reading settings").setMultiChoiceItems(new String[] {
            "Larger text"
        }
        ,new boolean[] {
            value[0]
        }
        ,(d,w,checked)->value[0]=checked).setNegativeButton("Cancel",null).setPositiveButton("Apply",(d,w)-> {
            getSharedPreferences("settings",0).edit().putBoolean("large",value[0]).apply();
            recreate();
        }
        ).show();
    }
    @Override public boolean onCreateOptionsMenu(Menu m) {
        m.add(0,1,0,"New check");
        m.add(0,2,1,"Follow-up reminder");
        SubMenu help=m.addSubMenu("Help & settings");
        help.add(0,3,0,"Larger text");
        help.add(0,4,1,"About & privacy");
        return true;
    }
    @Override public boolean onOptionsItemSelected(MenuItem i) {
        switch(i.getItemId()) {
            case 1:open(CheckActivity.class);
            return true;
            case 2:open(ReminderActivity.class);
            return true;
            case 3:settings();
            return true;
            case 4:new AlertDialog.Builder(this).setTitle("About FamilyCheck").setMessage("A simple way to pause, record a concern and ask someone you trust. Checks stay on this device. Sharing opens another app and you choose whether to send. FamilyCheck does not read your SMS or calls, and cannot decide whether a message is genuine.").setPositiveButton("Close",null).show();
            return true;
            default:return super.onOptionsItemSelected(i);
        }
    }
    @Override protected void onSaveInstanceState(Bundle out) {
        out.putInt("tab",tab);
        super.onSaveInstanceState(out);
    }
    @Override public void onBackPressed() {
        if(drawer.getVisibility()==View.VISIBLE)toggleDrawer();
        else if(tab!=0)select(0);
        else super.onBackPressed();
    }
}
