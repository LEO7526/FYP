package com.example.yummyrestaurant.utils;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import com.example.yummyrestaurant.R;
import java.util.Random;

public class NotificationHelper {

    private static final String CHANNEL_ID = "channel_id";

    public static void sendNotification(Context context, String title, String message) {
        // 创建通知渠道（适用于 Android 8.0 及以上版本）
        createNotificationChannel(context);

        // 检查 POST_NOTIFICATIONS 权限（适用于 Android 9.0 及以上版本）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // 权限未授予，请求权限
                // 这里需要在 Activity 中处理权限请求
                Toast.makeText(context, "Notification permission required.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        manager.notify(new Random().nextInt(), builder.build());
    }

    private static void createNotificationChannel(Context context) {
        // 仅在 Android 8.0 及以上版本中创建通知渠道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Channel Name"; // 通道名称
            String description = "Channel Description"; // 通道描述
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            // 获取通知管理器并创建通道
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
