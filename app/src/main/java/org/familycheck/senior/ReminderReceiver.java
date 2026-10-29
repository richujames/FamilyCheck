package org.familycheck.senior;
import android.app.*;
import android.content.*;
public class ReminderReceiver extends BroadcastReceiver {
    static final String CHANNEL="follow_up";
    static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences("reminder",0);
    }
    static PendingIntent pending(Context c) {
        return PendingIntent.getBroadcast(c,1,new Intent(c,ReminderReceiver.class).setAction("org.familycheck.senior.FOLLOW_UP"),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
    }
    static void schedule(Context c,long time) {
        ((AlarmManager)c.getSystemService(Context.ALARM_SERVICE)).setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,time,pending(c));
        prefs(c).edit().putLong("time",time).apply();
    }
    static void cancel(Context c) {
        ((AlarmManager)c.getSystemService(Context.ALARM_SERVICE)).cancel(pending(c));
        prefs(c).edit().remove("time").apply();
        manager(c).cancel(1);
    }
    static NotificationManager manager(Context c) {
        NotificationManager m=(NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE);
        m.createNotificationChannel(new NotificationChannel(CHANNEL,"Follow-up reminders",NotificationManager.IMPORTANCE_DEFAULT));
        return m;
    }
    @Override public void onReceive(Context c,Intent intent) {
        if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            long time=prefs(c).getLong("time",0);
            if(time>0)schedule(c,Math.max(time,System.currentTimeMillis()+60000));
            return;
        }
        if(!"org.familycheck.senior.FOLLOW_UP".equals(intent.getAction()))return;
        prefs(c).edit().remove("time").apply();
        show(c);
    }
    static void show(Context c) {
        if (android.os.Build.VERSION.SDK_INT >= 33 && c.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) return;
        NotificationManager m=manager(c);
        if(!m.areNotificationsEnabled())return;
        PendingIntent open=PendingIntent.getActivity(c,2,new Intent(c,MainActivity.class).putExtra("tab",1).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        try {
            m.notify(1,new Notification.Builder(c,CHANNEL).setSmallIcon(R.drawable.ic_shield).setContentTitle("A moment to follow up").setContentText("Review your checks with someone you trust.").setContentIntent(open).setAutoCancel(true).setVisibility(Notification.VISIBILITY_PRIVATE).build());
        }
        catch(SecurityException ignored) {
            /* Permission can be revoked at any time. */
        }
    }
}

