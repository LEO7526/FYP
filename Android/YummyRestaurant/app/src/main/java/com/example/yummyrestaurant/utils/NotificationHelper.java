package com.example.yummyrestaurant.utils;

import android.content.Context;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.yummyrestaurant.R;

import java.util.Random;

public class NotificationHelper {
    public static void sendNotification(Context context, String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "channel_id")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        manager.notify(new Random().nextInt(), builder.build());
    }
}


