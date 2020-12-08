package com.bufferoverflow.beesafe.BackgroundService;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.bufferoverflow.beesafe.FavoritePlace;
import com.bufferoverflow.beesafe.MainActivity;
import com.bufferoverflow.beesafe.R;

public class AppPersistentNotificationManager {

    private final Context context;

    @SuppressLint("StaticFieldLeak")
    private static AppPersistentNotificationManager instance;

    private AppPersistentNotificationManager (Context context){
        this.context = context;
        context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public static AppPersistentNotificationManager getInstance(Context context){
        if(instance==null)
            instance = new AppPersistentNotificationManager(context);
        return instance;
    }

    public void registerNotificationChannelChannel(String channelId, String channelName, String channelDescription) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, android.app.NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription(channelDescription);
            android.app.NotificationManager notificationManager = context.getSystemService(android.app.NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    public Notification getNotification(){

        Intent in = new Intent(context.getApplicationContext(), MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, 0, in, 0);
        in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,App.SERVICE_CHANNEL_ID)
                .setSmallIcon(R.drawable.safe)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.bee))
                .setContentTitle("")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pi)
                .setChannelId(App.SERVICE_CHANNEL_ID)
                .setOnlyAlertOnce(true)
                .setNotificationSilent()
                .setAutoCancel(true);
        return builder.build();
    }

    public void updateNotification(String title, String content) {
        Intent in = new Intent(context.getApplicationContext(), MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, 0, in, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,App.SERVICE_CHANNEL_ID)
                .setSmallIcon(R.drawable.safe)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.bee))
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pi)
                .setChannelId(App.SERVICE_CHANNEL_ID)
                .setNotificationSilent()
                .setAutoCancel(true);
        final Notification notification = builder.build();
        final NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, notification);
    }

    public void sendFavPlaceNotification(String title, String content, FavoritePlace favoritePlace) {
        NotificationChannel notificationChannel = new NotificationChannel("Favorite Place Notification Channel","Favorite Place Notification Channel", NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        manager.createNotificationChannel(notificationChannel);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, notificationChannel.getId())
                .setSmallIcon(R.drawable.people)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(favoritePlace.hashCode(), builder.build());
    }

}