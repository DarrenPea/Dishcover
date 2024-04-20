package com.example.myapplication;

import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class WalletManager {
    private static final String TAG = "WalletManager";
    private FirebaseFirestore firestore;
    private String userId;

    public WalletManager(String userId) {
        this.userId = userId;
        firestore = FirebaseFirestore.getInstance();
    }

    public void appendToWalletHistory(String action, int pointsGained) {
        CollectionReference historyRef = firestore.collection("users").document(userId).collection("wallet_history");

        Map<String, Object> actionData = new HashMap<>();
        actionData.put("action", action);
        actionData.put("points_gained", pointsGained);
        actionData.put("timestamp", FieldValue.serverTimestamp());

        historyRef.add(actionData)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Action added to wallet history: " + action);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding action to wallet history: ", e);
                });
    }

    public void fetchAllActionHistory(FirebaseFirestore firestore, OnSuccessListener<ArrayList<WalletAction>> successListener, OnFailureListener failureListener) {
        CollectionReference historyRef = firestore.collection("users").document(userId).collection("wallet_history");
        historyRef.orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<WalletAction> walletActions = new ArrayList<>();
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String action = documentSnapshot.getString("action");
                        Timestamp timestamp = documentSnapshot.getTimestamp("timestamp");
                        int pointsGained = Objects.requireNonNull(documentSnapshot.getLong("points_gained")).intValue();
                        // Assuming WalletAction class has appropriate constructor
                        WalletAction walletAction = new WalletAction(action, pointsGained, timestamp);
                        walletActions.add(walletAction);
                    }
                    successListener.onSuccess(walletActions);
                })
                .addOnFailureListener(failureListener);
    }


    public void updatePoints(int points) {
        DocumentReference userRef = firestore.collection("users").document(userId);
        firestore.runTransaction((Transaction.Function<Void>) transaction -> {
            DocumentSnapshot snapshot = transaction.get(userRef);
            if (snapshot.exists()) {
                int currentPoints = Objects.requireNonNull(snapshot.getLong("points")).intValue();
                int newPoints = currentPoints + points;
                transaction.update(userRef, "points", newPoints);
                Log.d(TAG, "User points updated successfully");
            }
            return null;
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error updating user points", e);
        });
    }


    public void checkPointsExceedLimit(OnSuccessListener<Boolean> successListener, OnFailureListener failureListener) {
        DocumentReference userRef = firestore.collection("users").document(userId);

        userRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        int points = Objects.requireNonNull(documentSnapshot.getLong("points")).intValue();
                        boolean exceedsLimit = points >= 90;
                        successListener.onSuccess(exceedsLimit);
                    } else {
                        Log.d(TAG, "User document does not exist");
                        failureListener.onFailure(new Exception("User document does not exist"));
                    }
                })
                .addOnFailureListener(failureListener);
    }


}
