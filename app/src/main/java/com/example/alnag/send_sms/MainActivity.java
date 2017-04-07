package com.example.alnag.send_sms;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    int MY_PERMISSION_REQUEST_SMS = 1;
    int MY_PERMISSION_REQUEST_CALL = 1;

    String SENT_SMS = "SMS_SENT";
    String DELIVERED = "SMS_Delivered";
    String ALARM_SET = "ALARM_SET";
    String ALARM_OFF = "ALARM_OFF";
    PendingIntent sentPendingIntent, deliveredPendingIntent, alarmPendingIntent;
    BroadcastReceiver smsSentReciever, smsDeliveredReciever, alarmBroadcastReceiver;
    MediaPlayer mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // This pendingIntent will shout out the sent_sms, delivered SMS and ALARM is set message.
        sentPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(SENT_SMS), 0);
        deliveredPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED), 0);
        alarmPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(ALARM_SET), 0);


    }

    // Capture the PendingIntent which was broadcast in broadcast receiver and do something.
    // Ex. create a toast message

    @Override
    protected void onResume() {
        super.onResume();

        smsSentReciever = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                switch (getResultCode()){
                    case Activity.RESULT_OK:
                        Toast.makeText(MainActivity.this, "SMS Sent!", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(MainActivity.this, "Generic Error", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(MainActivity.this, "No Service", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(MainActivity.this, "Null PDU", Toast.LENGTH_SHORT).show();
                        break;
                }

            }
        };

        smsDeliveredReciever = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                switch (getResultCode()){
                    case Activity.RESULT_OK:
                        Toast.makeText(MainActivity.this, "SMS Delivered!", Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(MainActivity.this, "SMS not delivered", Toast.LENGTH_SHORT).show();
                        break;

                }

            }
        };

        alarmBroadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                Uri alarm_uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
                mp = MediaPlayer.create(context, alarm_uri);
                mp.start();
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
                builder.setDefaults(Notification.DEFAULT_ALL)
                        .setWhen(System.currentTimeMillis())
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentText("Alarm Activated")
                        .setContentText("You've reached your destination")
                        .setContentInfo("Info")
                        .setSound(alarm_uri);

                NotificationManager notificationManager = (NotificationManager)
                        context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(1099, builder.build());

            }
        };


        // Register the broadcast receivers.
        registerReceiver(smsSentReciever, new IntentFilter(SENT_SMS));
        registerReceiver(smsDeliveredReciever, new IntentFilter(DELIVERED));
        registerReceiver(alarmBroadcastReceiver, new IntentFilter(ALARM_SET));

    }

    // Unregister if we move away from app.
    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(smsSentReciever);
        unregisterReceiver(smsDeliveredReciever);
        unregisterReceiver(alarmBroadcastReceiver);
    }


    // Function to send SMS. Check's permission if exists else asks for permission and send SMS
    // Calls pendingIntent which sends a system-wide broadcast message Captured by broadcast receivers.

    public void send_sms(View v){

        String message = "I have reached my destination";
        String PhoneNo = "+919739987000";

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, MY_PERMISSION_REQUEST_SMS);
        }
        else {
            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(PhoneNo, null, message, sentPendingIntent, deliveredPendingIntent);
        }

    }

    public void set_alarm(View v) {
        // Sets alarm for system time + 3 s and generates a notification
        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarm.set(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime() + 30000, alarmPendingIntent);

    }

    public void make_call(View v) {
        Intent phoneIntent  = new Intent(Intent.ACTION_CALL);
        phoneIntent.setData(Uri.parse("tel:00919739987000"));

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CALL_PHONE}, MY_PERMISSION_REQUEST_CALL);
        }
        else {
            startActivity(phoneIntent);
        }

    }

}
