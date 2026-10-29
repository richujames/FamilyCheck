package org.familycheck.senior;
import android.app.*;
import android.os.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.widget.*;
import java.util.Calendar;
public class ReminderActivity extends BaseActivity {
    private final Calendar chosen=Calendar.getInstance();
    private TextView selected,current;
    private boolean testing;
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        setup("Follow up later");
        long saved=ReminderReceiver.prefs(this).getLong("time",0);
        chosen.setTimeInMillis(state!=null?state.getLong("chosen"):saved>0?saved:System.currentTimeMillis()+3600000);
        if(state!=null)testing=state.getBoolean("testing");
        Ui.text(content,"Make time to check in",26,true);
        Ui.text(content,"Set one reminder to review your saved checks. A new reminder replaces the previous one.",17,false);
        selected=Ui.text(Ui.card(content),"",22,true);
        update();
        Ui.button(content,"Choose date",false,()-> {
            DatePickerDialog d=new DatePickerDialog(this,(v,y,m,day)-> {
                chosen.set(y,m,day);
                update();
            }
            ,chosen.get(Calendar.YEAR),chosen.get(Calendar.MONTH),chosen.get(Calendar.DAY_OF_MONTH));
            d.getDatePicker().setMinDate(System.currentTimeMillis());
            d.show();
        }
        );
        Ui.button(content,"Choose time",false,()->new TimePickerDialog(this,(v,h,m)-> {
            chosen.set(Calendar.HOUR_OF_DAY,h);
            chosen.set(Calendar.MINUTE,m);
            chosen.set(Calendar.SECOND,0);
            chosen.set(Calendar.MILLISECOND,0);
            update();
        }
        ,chosen.get(Calendar.HOUR_OF_DAY),chosen.get(Calendar.MINUTE),android.text.format.DateFormat.is24HourFormat(this)).show());
        Ui.button(content,"Save reminder",true,()->request(false));
        Ui.button(content,"Send a test notification",false,()->request(true));
        Ui.button(content,"Cancel scheduled reminder",false,()-> {
            ReminderReceiver.cancel(this);
            current.setText("No reminder scheduled.");
            Ui.toast(this,"Reminder cancelled");
        }
        );
        Ui.button(content,"Notification settings",false,()->Ui.launch(this,new Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(android.provider.Settings.EXTRA_APP_PACKAGE,getPackageName())));
        current=Ui.text(content,"",16,false);
        Ui.text(content,"Android may delay delivery to save battery. This is not an emergency alarm. Force-stopping the app can cancel delivery.",15,false);
    }
    private void request(boolean test) {
        testing=test;
        if(!test&&chosen.getTimeInMillis()<=System.currentTimeMillis()) {
            Ui.toast(this,"Choose a future date and time");
            return;
        }
        if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {
                android.Manifest.permission.POST_NOTIFICATIONS
            }
            ,90);
            return;
        }
        perform();
    }
    private void perform() {
        NotificationManager m=ReminderReceiver.manager(this);
        if(!m.areNotificationsEnabled()||m.getNotificationChannel(ReminderReceiver.CHANNEL).getImportance()==NotificationManager.IMPORTANCE_NONE) {
            Ui.toast(this,"Enable follow-up notifications in Notification settings first");
            return;
        }
        if(testing) {
            ReminderReceiver.show(this);
            Ui.toast(this,"Test notification sent");
        }
        else {
            if(chosen.getTimeInMillis()<=System.currentTimeMillis()) {
                Ui.toast(this,"Choose a future time");
                return;
            }
            ReminderReceiver.schedule(this,chosen.getTimeInMillis());
            Ui.toast(this,"Reminder saved. Delivery time may vary.");
            updateCurrent();
        }
    }
    private void update() {
        selected.setText(java.text.DateFormat.getDateTimeInstance().format(chosen.getTime()));
    }
    private void updateCurrent() {
        long time=ReminderReceiver.prefs(this).getLong("time",0);
        current.setText(time==0?"No reminder scheduled.":"Scheduled: "+java.text.DateFormat.getDateTimeInstance().format(new java.util.Date(time)));
    }
    @Override public void onResume() {
        super.onResume();
        updateCurrent();
    }
    @Override public void onRequestPermissionsResult(int r,String[] p,int[] grants) {
        super.onRequestPermissionsResult(r,p,grants);
        if(r==90) {
            if(grants.length>0&&grants[0]==PackageManager.PERMISSION_GRANTED)perform();
            else Ui.toast(this,"Permission denied. Reminders need notifications enabled.");
        }
    }
    @Override protected void onSaveInstanceState(Bundle b) {
        b.putLong("chosen",chosen.getTimeInMillis());
        b.putBoolean("testing",testing);
        super.onSaveInstanceState(b);
    }
}
