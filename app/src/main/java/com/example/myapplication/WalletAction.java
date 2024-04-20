package com.example.myapplication;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WalletAction {
    private String action;
    private int pointsGained;
    private Timestamp timestamp;

    public WalletAction() {
        // Default constructor required for Firestore's deserialization
    }

    public WalletAction(String action, int pointsGained, Timestamp timestamp) {
        this.action = action;
        this.pointsGained = pointsGained;
        this.timestamp = timestamp;
    }

    public String getAction() {
        return action;
    }

    public int getPointsGained() {
        return pointsGained;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public String getFormattedTimestamp() {
        Date date = timestamp.toDate();
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.getDefault());
        return sdf.format(date);
    }

}

