package com.example.RemoteSMSHandling;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.RingtoneManager;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import java.util.Calendar;

/**
 * Created by ivan on 7/24/14.
 */
public class AlarmService extends Service {

    //used for register alarm manager
    PendingIntent pendingIntent;
    //used to store running alarmmanager instance
    AlarmManager alarmManager;
    //Callback function for Alarmmanager event
    BroadcastReceiver mReceiver;


    @Override
    public void onCreate() {
        super.onCreate();

    }

    public int onStartCommand(Intent intent, int flags, int startId) {

        registerAlarmBroadcast();
        setAlarmTime(pendingIntent, intent.getLongExtra("time", -1));
        return super.onStartCommand(intent, flags, startId);
    }

    public IBinder onBind(Intent arg0) {
        return null;
    }

    private void registerAlarmBroadcast() {

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Toast.makeText(getApplicationContext(), "time comming", Toast.LENGTH_LONG);
                sendNotif(intent);
                alarmManager.cancel(pendingIntent);
            }
        };

        // register the alarm broadcast here
        registerReceiver(mReceiver, new IntentFilter("com.example.RemoteSMSHandling"));
        pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent("com.example.RemoteSMSHandling"), 0);
        alarmManager = (AlarmManager) (getApplicationContext().getSystemService(Context.ALARM_SERVICE));
    }

    public void setAlarmTime(PendingIntent pi, long time) {
        if (time != -1) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(time);
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
        } else {
            //TODO dialog
        }
    }

    private void sendNotif(Intent intent) {

        Context context = getApplicationContext();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context)
                        .setAutoCancel(true)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("Alarm")
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setContentText("Time is come");

        builder.setContentIntent(contentIntent);
        notificationManager.notify(123456789, builder.build());
    }
}
