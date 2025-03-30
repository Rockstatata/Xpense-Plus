package com.example.myapplication.helpers;

import com.example.myapplication.models.Transaction;
import com.example.myapplication.models.User;
import com.example.myapplication.models.UserTransaction;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DatabaseHelper {
    private DatabaseReference databaseReference;

    public DatabaseHelper() {
        databaseReference = FirebaseDatabase.getInstance("https://xpense-dab26-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();
    }

    public void addUser(User user, String userId) {
        if (user == null || userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("Invalid user or userId");
        }
        databaseReference.child("Users").child(userId).setValue(user, (databaseError, databaseReference) -> {
            if (databaseError != null) {
                System.err.println("Data could not be saved " + databaseError.getMessage());
            } else {
                System.out.println("Data saved successfully.");
            }
        });
    }

    public void addNormalTransaction(Transaction transaction, String userId) {
        if (transaction == null || userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("Invalid transaction or userId");
        }
        String transactionId = transaction.getId();
        if (transactionId != null && !transactionId.isEmpty()) {
            databaseReference.child("Users").child(userId).child("transactions").child("normal").child(transactionId).setValue(transaction);
        } else {
            throw new IllegalStateException("Transaction ID cannot be null or empty");
        }
    }

    public void addHandshakeTransaction(UserTransaction transaction, String userId) {
        if (transaction == null || userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("Invalid transaction or userId");
        }
        String transactionId = transaction.getId();
        if (transactionId != null && !transactionId.isEmpty()) {
            databaseReference.child("Users").child(userId).child("transactions").child("handshake").child(transactionId).setValue(transaction);
        } else {
            throw new IllegalStateException("Transaction ID cannot be null or empty");
        }
    }

    public void updateTransactionAmount(String transactionId, String userId, double amount) {
        if (transactionId == null || transactionId.isEmpty() || userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("Invalid transactionId or userId");
        }
        databaseReference.child("Users").child(userId).child("transactions").child("handshake").child(transactionId).child("amount").setValue(amount, (databaseError, databaseReference) -> {
            if (databaseError != null) {
                System.err.println("Data could not be updated " + databaseError.getMessage());
            } else {
                System.out.println("Data updated successfully.");
            }
        });
    }

    public void removeHandshakeTransaction(String transactionId, String userId) {
        if (transactionId == null || transactionId.isEmpty() || userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("Invalid transactionId or userId");
        }
        databaseReference.child("Users").child(userId).child("transactions").child("handshake").child(transactionId).removeValue((databaseError, databaseReference) -> {
            if (databaseError != null) {
                System.err.println("Data could not be deleted " + databaseError.getMessage());
            } else {
                System.out.println("Data deleted successfully.");
            }
        });
    }

    public void updateTransactionStatus(String transactionId, String userId, String status) {
        if (transactionId == null || transactionId.isEmpty() || userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("Invalid transactionId or userId");
        }
        databaseReference.child("Users").child(userId).child("transactions").child("handshake").child(transactionId).child("status").setValue(status, (databaseError, databaseReference) -> {
            if (databaseError != null) {
                System.err.println("Data could not be updated " + databaseError.getMessage());
            } else {
                System.out.println("Data updated successfully.");
            }
        });
    }

    public void updateSettledAmount(String transactionId, String userId, double settledAmount) {
        if (transactionId == null || transactionId.isEmpty() || userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("Invalid transactionId or userId");
        }
        databaseReference.child("Users").child(userId).child("transactions").child("handshake").child(transactionId).child("settledAmount").setValue(settledAmount, (databaseError, databaseReference) -> {
            if (databaseError != null) {
                System.err.println("Data could not be updated " + databaseError.getMessage());
            } else {
                System.out.println("Data updated successfully.");
            }
        });
    }

    // DatabaseHelper.java
    // DatabaseHelper.java
    public void updateUserPayableAmount(String userId, double amount) {
        databaseReference.child("Users").child(userId).child("accounts").child("Payable").setValue(amount, (databaseError, databaseReference) -> {
            if (databaseError != null) {
                System.err.println("Data could not be updated " + databaseError.getMessage());
            } else {
                System.out.println("Data updated successfully.");
            }
        });
    }

    public void updateUserReceivableAmount(String userId, double amount) {
        databaseReference.child("Users").child(userId).child("accounts").child("Receivable").setValue(amount, (databaseError, databaseReference) -> {
            if (databaseError != null) {
                System.err.println("Data could not be updated " + databaseError.getMessage());
            } else {
                System.out.println("Data updated successfully.");
            }
        });
    }

    public void transformHandshakeToNormal(String userId, String transactionId, Transaction transaction) {
        if (userId == null || userId.isEmpty() || transactionId == null || transactionId.isEmpty() || transaction == null) {
            throw new IllegalArgumentException("Invalid userId, transactionId, or transaction");
        }
        // Remove handshake transaction
        databaseReference.child("Users").child(userId).child("transactions").child("handshake").child(transactionId).removeValue();
        // Add as normal transaction
        addNormalTransaction(transaction, userId);
    }
}