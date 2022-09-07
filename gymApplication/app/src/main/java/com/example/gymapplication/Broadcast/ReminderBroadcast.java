package com.example.gymapplication.Broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.gymapplication.R;

public class ReminderBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Resources res = context.getResources();
        String workoutName = intent.getStringExtra("woName");
        if(workoutName == null){
            workoutName = "";
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "notifyCustomer")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(res.getString(R.string.notification_tittle))
                .setContentText(res.getString(R.string.notification_body,workoutName))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(200,builder.build());
    }
}
