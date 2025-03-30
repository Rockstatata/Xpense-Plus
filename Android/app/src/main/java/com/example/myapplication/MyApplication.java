// MyApplication.java
package com.example.myapplication;

import android.app.Application;
import com.google.firebase.database.FirebaseDatabase;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Enable disk persistence
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}