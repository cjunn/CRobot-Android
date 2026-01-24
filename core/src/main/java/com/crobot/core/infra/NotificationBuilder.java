package com.crobot.core.infra;

import static android.content.Context.NOTIFICATION_SERVICE;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

import com.crobot.core.resource.My;

import java.util.function.Supplier;

public class NotificationBuilder {

    private Context context;

    public NotificationBuilder(Context context) {
        this.context = context;
    }


    public Notification get(String channelName, Supplier<Class<? extends Activity>> openActivity) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    channelName,
                    channelName,
                    NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setShowBadge(true);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager manager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(notificationChannel);
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, openActivity.get()), PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder builder = new Notification.Builder(context);
        builder.setContentTitle(channelName)
                .setContentText(channelName + " Is Running")
                .setContentIntent(pendingIntent)
                .setSmallIcon(My.drawable.theme_logo)
                .setWhen(System.currentTimeMillis());
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
            builder.setChannelId(channelName);
        Notification notification = builder.build();
        notification.defaults = Notification.DEFAULT_SOUND;
        return notification;
    }

}
