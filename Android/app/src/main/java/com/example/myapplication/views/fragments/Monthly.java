// Monthly.java
package com.example.myapplication.views.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapters.TransactionsAdapter;
import com.example.myapplication.models.Transaction;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class Monthly extends Fragment implements TransactionsAdapter.OnTransactionLongClickListener {

    private TextView totalIncomeTextView;
    private TextView totalExpensesTextView;
    private TextView balanceTextView;
    private RecyclerView recyclerView;
    private TransactionsAdapter adapter;
    private List<Transaction> transactions;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private TextView currentDateTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_monthly, container, false);

        totalIncomeTextView = view.findViewById(R.id.totalIncome);
        totalExpensesTextView = view.findViewById(R.id.totalExpenses);
        balanceTextView = view.findViewById(R.id.balance);
        recyclerView = view.findViewById(R.id.transactionsList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        currentDateTextView = getActivity().findViewById(R.id.currentDate);

        transactions = new ArrayList<>();
        adapter = new TransactionsAdapter(transactions, this);
        recyclerView.setAdapter(adapter);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userDisplayName = currentUser.getDisplayName();
            databaseReference = FirebaseDatabase.getInstance("https://xpense-dab26-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference()
                    .child("Users").child(userDisplayName).child("transactions").child("normal");

            fetchTransactions();
        }

        return view;
    }

    void fetchTransactions() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                transactions.clear();

                double totalIncome = 0;
                double totalExpenses = 0;
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                String selectedMonth = currentDateTextView.getText().toString();
                Log.d("Selected Month", selectedMonth);

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Transaction transaction = snapshot.getValue(Transaction.class);
                    Log.d("Transaction", transaction.toString());
                    if (transaction != null) {
                        try {
                            Date transactionDate = sdf.parse(transaction.getDate());
                            String transactionMonth = new SimpleDateFormat("MM/yyyy").format(transactionDate);

                            if (transactionMonth.equals(selectedMonth)) {
                                transactions.add(transaction);
                                if (transaction.getType().equals("Income")) {
                                    totalIncome += transaction.getAmount();
                                } else {
                                    totalExpenses += transaction.getAmount();
                                }
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }

                double balance = totalIncome + totalExpenses;

                totalIncomeTextView.setText(String.format(String.valueOf(totalIncome)));
                totalExpensesTextView.setText(String.format(String.valueOf(totalExpenses)));
                balanceTextView.setText(String.format(String.valueOf(balance)));

                Collections.sort(transactions, new Comparator<Transaction>() {
                    @Override
                    public int compare(Transaction t1, Transaction t2) {
                        try {
                            Date dateTime1 = dateTimeFormat.parse(t1.getDate() + " " + t1.getTime());
                            Date dateTime2 = dateTimeFormat.parse(t2.getDate() + " " + t2.getTime());
                            return dateTime1.compareTo(dateTime2);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        return 0;
                    }
                });

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.err.println("Database error: " + databaseError.getMessage());
            }
        });
    }

    @Override
    public void onTransactionLongClick(Transaction transaction) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Transaction")
                .setMessage("Are you sure you want to delete this transaction?")
                .setPositiveButton("Yes", (dialog, which) -> deleteTransaction(transaction))
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteTransaction(Transaction transaction) {
        databaseReference.child(transaction.getId()).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Transaction deleted", Toast.LENGTH_SHORT).show();
                fetchTransactions();
            } else {
                Toast.makeText(getContext(), "Failed to delete transaction", Toast.LENGTH_SHORT).show();
            }
        });
    }
}