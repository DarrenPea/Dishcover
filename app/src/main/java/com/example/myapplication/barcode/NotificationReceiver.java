package com.example.myapplication.barcode;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.myapplication.R;

public class NotificationReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "product_expiry_notification";
    private static final CharSequence CHANNEL_NAME = "Product Expiry Notification";

    @Override
    public void onReceive(Context context, Intent intent) {
        String productName = intent.getStringExtra("productName");

        // Show the notification with the product name
        showNotification(context, productName);
    }

    private void showNotification(Context context, String productName) {
        Intent detailsIntent = new Intent(context, ProductDetailsActivity.class);
        // Pass the product name as an extra
        detailsIntent.putExtra("productName", productName);
        // Add any other necessary extras to identify the product (e.g., product ID or barcode)

        // Create a pending intent to open the ProductDetailsActivity when the notification is clicked
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, detailsIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        // Create a notification manager
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create a notification channel for Android Oreo and higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        // Build the notification with the product name included
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle("Product Expiry Reminder")
                .setContentText(productName + " is expiring soon!")  // Include product name here
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        // Show the notification
        notificationManager.notify(0, builder.build());
    }
    private void showExpiryDateNotification(Context context) {
        // Create a notification manager
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create a notification channel for Android Oreo and higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle("Product Expiry Date Notification")
                .setContentText("Your product is expiring on the set date!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        // Show the notification
        notificationManager.notify(1, builder.build());
    }
}