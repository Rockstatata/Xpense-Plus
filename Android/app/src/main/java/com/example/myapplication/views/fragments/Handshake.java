// Handshake.java
package com.example.myapplication.views.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapters.UserTransactionsAdapter;
import com.example.myapplication.helpers.DatabaseHelper;
import com.example.myapplication.models.UserTransaction;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Handshake extends Fragment {

    private TextView totalBalanceTextView;
    private TextView payableTextView;
    private TextView receivableTextView;

    private RecyclerView newRequestRecyclerView;
    private RecyclerView pendingTransactionsRecyclerView;
    private RecyclerView completedTransactionsRecyclerView;
    private RecyclerView pendingRequestsRecyclerView;
    private RecyclerView sentRequestsRecyclerView;
    private RecyclerView pendingSettlementsRecyclerView;

    private UserTransactionsAdapter pendingTransactionsAdapter;
    private UserTransactionsAdapter completedTransactionsAdapter;
    private UserTransactionsAdapter pendingRequestsAdapter;
    private UserTransactionsAdapter sentRequestsAdapter;
    private UserTransactionsAdapter pendingSettlementsAdapter;
    private List<UserTransaction> pendingTransactions = new ArrayList<>();
    private List<UserTransaction> completedTransactions = new ArrayList<>();
    private List<UserTransaction> pendingRequests = new ArrayList<>();
    private List<UserTransaction> sentRequests = new ArrayList<>();
    private List<UserTransaction> pendingSettlements = new ArrayList<>();
    private List<UserTransaction> transactions = new ArrayList<>();
    private DatabaseHelper databaseHelper;
    private DatabaseReference requestsRef;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_handshake, container, false);

        totalBalanceTextView = view.findViewById(R.id.availableBalanceValue);
        payableTextView = view.findViewById(R.id.payablesValue);
        receivableTextView = view.findViewById(R.id.receivablesValue);


        newRequestRecyclerView = view.findViewById(R.id.newRequestRecyclerView);
        pendingTransactionsRecyclerView = view.findViewById(R.id.pendingTransactionsRecyclerView);
        completedTransactionsRecyclerView = view.findViewById(R.id.completedTransactionsRecyclerView);
        pendingRequestsRecyclerView = view.findViewById(R.id.pendingRequestsRecyclerView);
        sentRequestsRecyclerView = view.findViewById(R.id.sentRequestsRecyclerView);
        pendingSettlementsRecyclerView = view.findViewById(R.id.pendingSettlementsRecyclerView);
        Button addRequestButton = view.findViewById(R.id.addRequestButton);

        setupRecyclerView(newRequestRecyclerView);
        setupRecyclerView(pendingTransactionsRecyclerView);
        setupRecyclerView(completedTransactionsRecyclerView);
        setupRecyclerView(pendingRequestsRecyclerView);
        setupRecyclerView(sentRequestsRecyclerView);
        setupRecyclerView(pendingSettlementsRecyclerView);

        setupCardToggle(view, R.id.pendingTransactionsCard, R.id.pendingTransactionsRecyclerView);
        setupCardToggle(view, R.id.completedTransactionsCard, R.id.completedTransactionsRecyclerView);
        setupCardToggle(view, R.id.pendingRequestsCard, R.id.pendingRequestsRecyclerView);
        setupCardToggle(view, R.id.sentRequestsCard, R.id.sentRequestsRecyclerView);
        setupCardToggle(view, R.id.pendingSettlementsCard, R.id.pendingSettlementsRecyclerView);

        pendingRequests = new ArrayList<>();
        sentRequests = new ArrayList<>();
        pendingTransactions = new ArrayList<>();
        completedTransactions = new ArrayList<>();
        databaseHelper = new DatabaseHelper();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userDisplayName = currentUser.getDisplayName();
            requestsRef = FirebaseDatabase.getInstance("https://xpense-dab26-default-rtdb.asia-southeast1.firebasedatabase.app/")
                    .getReference().child("Users").child(userDisplayName).child("transactions").child("handshake");

            fetchPendingRequests();
            fetchSentRequests();
            fetchPendingTransactions();
            fetchCompletedTransactions();
            fetchPendingSettlements();
        }

        // Handshake.java
        pendingTransactionsAdapter = new UserTransactionsAdapter(pendingTransactions, false, this);
        pendingTransactionsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        pendingTransactionsRecyclerView.setAdapter(pendingTransactionsAdapter);

        completedTransactionsAdapter = new UserTransactionsAdapter(completedTransactions, true, this);
        completedTransactionsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        completedTransactionsRecyclerView.setAdapter(completedTransactionsAdapter);

        pendingRequestsAdapter = new UserTransactionsAdapter(pendingRequests, false, this);
        pendingRequestsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        pendingRequestsRecyclerView.setAdapter(pendingRequestsAdapter);

        sentRequestsAdapter = new UserTransactionsAdapter(sentRequests, false, this);
        sentRequestsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        sentRequestsRecyclerView.setAdapter(sentRequestsAdapter);

        pendingSettlementsAdapter = new UserTransactionsAdapter(pendingSettlements, false, this);
        pendingSettlementsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        pendingSettlementsRecyclerView.setAdapter(pendingSettlementsAdapter);

        addRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddRequestDialog();
            }
        });

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Fragment childFragment = getChildFragmentManager().findFragmentById(R.id.fragment_container);
                if (childFragment != null && getChildFragmentManager().getBackStackEntryCount() > 0) {
                    getChildFragmentManager().popBackStack();
                } else {
                    requireActivity().getSupportFragmentManager().popBackStack();
                }
            }
        });

        return view;
    }


    private void fetchTransactions() {
        requestsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                transactions = new ArrayList<>();
                double totalBalance = 0;
                double payable = 0;
                double receivable = 0;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    UserTransaction transaction = snapshot.getValue(UserTransaction.class);
                    if (transaction != null) {
                        transactions.add(transaction);
                        if (transaction.getStatus().equals("Pending")) {
                            if (transaction.getSender().equals(mAuth.getCurrentUser().getDisplayName())) {
                                payable += transaction.getAmount();
                            } else {
                                receivable += transaction.getAmount();
                            }
                        }
                    }
                }

                totalBalance = receivable - payable;

                totalBalanceTextView.setText(String.format("Total Balance: %.2f", totalBalance));
                payableTextView.setText(String.format("Payable: %.2f", payable));
                receivableTextView.setText(String.format("Receivable: %.2f", receivable));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.err.println("Database error: " + databaseError.getMessage());
            }
        });
    }

    private void setupRecyclerView(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void setupCardToggle(View view, int cardId, int recyclerViewId) {
        View card = view.findViewById(cardId);
        RecyclerView recyclerView = view.findViewById(recyclerViewId);

        card.setOnClickListener(v -> {
            if (recyclerView.getVisibility() == View.GONE) {
                recyclerView.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.GONE);
            }
        });
    }

    private void showAddRequestDialog() {
        Dialog dialogView = new Dialog(getContext());
        dialogView.setContentView(R.layout.dialog_new_request);
        dialogView.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        EditText recipientUsername = dialogView.findViewById(R.id.recipientUsername);
        EditText amount = dialogView.findViewById(R.id.amount);
        EditText selectedDate = dialogView.findViewById(R.id.selectedDate);
        EditText note = dialogView.findViewById(R.id.note);
        RadioGroup transactionTypeGroup = dialogView.findViewById(R.id.transactionTypeGroup);
        EditText account = dialogView.findViewById(R.id.account);
        Button sendRequestButton = dialogView.findViewById(R.id.sendRequestButton);

        final Calendar calendar = Calendar.getInstance();

        selectedDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), R.style.CustomDatePickerDialog, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        String selectedDateString = DateFormat.getDateInstance(DateFormat.MEDIUM).format(calendar.getTime());
                        selectedDate.setText(selectedDateString);
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });

        // Get the transaction types from the array resource
        String[] transactionTypes = getResources().getStringArray(R.array.transaction_types);
        for (String type : transactionTypes) {
            RadioButton radioButton = new RadioButton(getContext());
            radioButton.setText(type);
            transactionTypeGroup.addView(radioButton);
        }

        sendRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String recipient = recipientUsername.getText().toString();
                double amountValue = Double.parseDouble(amount.getText().toString());
                int selectedRadioButtonId = transactionTypeGroup.getCheckedRadioButtonId();
                RadioButton selectedRadioButton = dialogView.findViewById(selectedRadioButtonId);
                String type = selectedRadioButton.getText().toString();
                String accountValue = account.getText().toString();
                String dateValue = selectedDate.getText().toString();
                String noteValue = note.getText().toString();

                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser != null) {
                    String sender = currentUser.getDisplayName();

                    // Generate a unique ID based on the total number of transactions
                    String transactionId = requestsRef.push().getKey();

                    // Create a new transaction request for the sender
                    UserTransaction newTransaction = new UserTransaction(
                            transactionId, sender, recipient, amountValue, type, dateValue, accountValue, "Sent", noteValue);

                    // Save the new transaction to the Firebase database for the sender
                    databaseHelper.addHandshakeTransaction(newTransaction, sender);

                    // Create a new transaction request for the recipient
                    UserTransaction recipientTransaction = new UserTransaction(
                            transactionId, sender, recipient, amountValue, type, dateValue, accountValue, "Requested", noteValue);

                    // Save the new transaction to the Firebase database for the recipient
                    databaseHelper.addHandshakeTransaction(recipientTransaction, recipient);

                    // Send notification to the recipient
                    sendNotification(recipient, newTransaction);
                }

                dialogView.dismiss();
            }
        });

        dialogView.show();
    }

    private void fetchPendingRequests() {
        requestsRef.orderByChild("status").equalTo("Requested").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                pendingRequests.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    UserTransaction transaction = snapshot.getValue(UserTransaction.class);
                    if (transaction != null) {
                        pendingRequests.add(transaction);
                    }
                }
                pendingRequestsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.err.println("Database error: " + databaseError.getMessage());
            }
        });
    }

    private void fetchSentRequests() {
        requestsRef.orderByChild("status").equalTo("Sent").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                sentRequests.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    UserTransaction transaction = snapshot.getValue(UserTransaction.class);
                    if (transaction != null) {
                        sentRequests.add(transaction);
                    }
                }
                sentRequestsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.err.println("Database error: " + databaseError.getMessage());
            }
        });
    }

    private void fetchPendingTransactions() {
        requestsRef.orderByChild("status").equalTo("Pending").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                pendingTransactions.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    UserTransaction transaction = snapshot.getValue(UserTransaction.class);
                    if (transaction != null) {
                        pendingTransactions.add(transaction);
                    }
                }
                pendingTransactionsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.err.println("Database error: " + databaseError.getMessage());
            }
        });
    }

    private void fetchCompletedTransactions() {
        requestsRef.orderByChild("status").equalTo("Completed").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                completedTransactions.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    UserTransaction transaction = snapshot.getValue(UserTransaction.class);
                    if (transaction != null) {
                        completedTransactions.add(transaction);
                    }
                }
                completedTransactionsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.err.println("Database error: " + databaseError.getMessage());
            }
        });
    }

    private void fetchPendingSettlements() {
        requestsRef.orderByChild("status").equalTo("SettlementRequested").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                pendingSettlements.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    UserTransaction transaction = snapshot.getValue(UserTransaction.class);
                    if (transaction != null) {
                        pendingSettlements.add(transaction);
                    }
                }
                pendingSettlementsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.err.println("Database error: " + databaseError.getMessage());
            }
        });
    }

    // Handshake.java
    // Handshake.java
    public void showSettlementDialog(UserTransaction transaction) {
        Dialog dialogView = new Dialog(getContext());
        dialogView.setContentView(R.layout.dialog_settlement);
        dialogView.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView amountTextView = dialogView.findViewById(R.id.amountTextView);
        SeekBar amountSeekBar = dialogView.findViewById(R.id.amountSeekBar);
        Button submitButton = dialogView.findViewById(R.id.submitButton);

        double fullAmount = transaction.getAmount();
        amountSeekBar.setMax((int) fullAmount);
        amountSeekBar.setProgress((int) fullAmount);
        amountTextView.setText(String.valueOf(fullAmount));

        amountSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress < 1) {
                    seekBar.setProgress(1);
                    amountTextView.setText("1");
                } else {
                    amountTextView.setText(String.valueOf(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        submitButton.setOnClickListener(v -> {
            double settledAmount = amountSeekBar.getProgress();
            handleSettlement(transaction, settledAmount);
            dialogView.dismiss();
        });

        dialogView.show();
    }

    private void handleSettlement(UserTransaction transaction, double settledAmount) {
        String userId = mAuth.getCurrentUser().getDisplayName();
        String recipientId = transaction.getReceiver().equals(userId) ? transaction.getSender() : transaction.getReceiver();

        if (settledAmount == transaction.getAmount()) {
            transaction.setStatus("Completed");
            transaction.setSettledAmount(settledAmount);
            databaseHelper.updateSettledAmount(transaction.getId(), userId, settledAmount);
            databaseHelper.updateSettledAmount(transaction.getId(), recipientId, settledAmount);
            databaseHelper.updateTransactionStatus(transaction.getId(), userId, "Completed");
            databaseHelper.updateTransactionStatus(transaction.getId(), recipientId, "Completed");
        } else {
            transaction.setStatus("SettlementRequested");
            transaction.setSettledAmount(settledAmount);
            databaseHelper.updateSettledAmount(transaction.getId(), userId, settledAmount);
            databaseHelper.updateSettledAmount(transaction.getId(), recipientId, settledAmount);
            databaseHelper.updateTransactionStatus(transaction.getId(), userId, "SettlementRequested");
            databaseHelper.updateTransactionStatus(transaction.getId(), recipientId, "SettlementRequested");
        }

        if (transaction.getStatus().equals("Completed")) {
            convertToNormalTransaction(transaction);
        }

        sendSettlementNotification(transaction, settledAmount);
    }

    private void convertToNormalTransaction(UserTransaction transaction) {
        DatabaseReference normalTransactionsRefSender = FirebaseDatabase.getInstance("https://xpense-dab26-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference().child("Users").child(transaction.getSender()).child("transactions").child("normal");

        DatabaseReference normalTransactionsRefReceiver = FirebaseDatabase.getInstance("https://xpense-dab26-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference().child("Users").child(transaction.getReceiver()).child("transactions").child("normal");

        normalTransactionsRefSender.child(transaction.getId()).setValue(transaction).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                normalTransactionsRefReceiver.child(transaction.getId()).setValue(transaction).addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        requestsRef.child(transaction.getId()).removeValue().addOnCompleteListener(task2 -> {
                            if (task2.isSuccessful()) {
                                Log.d("Transaction", "Handshake transaction converted to normal transaction for both users successfully.");
                            } else {
                                Log.e("Transaction", "Failed to remove handshake transaction.");
                            }
                        });
                    } else {
                        Log.e("Transaction", "Failed to convert handshake transaction to normal transaction for receiver.");
                    }
                });
            } else {
                Log.e("Transaction", "Failed to convert handshake transaction to normal transaction for sender.");
            }
        });
    }

    private void sendSettlementNotification(UserTransaction transaction, double settledAmount) {
        // Implement notification logic here
    }

    private void sendNotification(String recipient, UserTransaction transaction) {
        // Implement notification logic here
    }
}