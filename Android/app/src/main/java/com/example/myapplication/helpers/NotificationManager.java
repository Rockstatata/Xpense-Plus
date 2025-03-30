// NotificationManager.java
package com.example.myapplication.helpers;

import android.content.Context;
import android.widget.Toast;

import com.example.myapplication.models.UserTransaction;

public class NotificationManager {

    private Context context;

    public NotificationManager(Context context) {
        this.context = context;
    }

    public void sendNotification(String recipient, UserTransaction transaction) {
        // For simplicity, use a Toast to simulate a notification
        Toast.makeText(context, "New transaction request from " + transaction.getSender(), Toast.LENGTH_LONG).show();
    }

    public void sendUpdateNotification(String recipient, UserTransaction transaction) {
        // For simplicity, use a Toast to simulate a notification
        Toast.makeText(context, "Transaction update from " + transaction.getSender(), Toast.LENGTH_LONG).show();
    }
}