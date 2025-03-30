package com.example.myapplication.views.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapters.GoalsAdapter;
import com.example.myapplication.models.Goal;
import com.example.myapplication.models.Transaction;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DashboardPage extends Fragment implements GoalsAdapter.OnGoalLongClickListener{

    private FloatingActionButton fabAddTransaction;
    private FloatingActionButton fabAddGoal;
    private RecyclerView goalsRecyclerView;
    private GoalsAdapter goalsAdapter;
    private DatabaseReference goalsRef;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference userRef;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard_page, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userDisplayName = currentUser.getDisplayName();
            userRef = FirebaseDatabase.getInstance("https://xpense-dab26-default-rtdb.asia-southeast1.firebasedatabase.app/")
                    .getReference().child("Users").child(userDisplayName).child("accounts");
            goalsRef = FirebaseDatabase.getInstance("https://xpense-dab26-default-rtdb.asia-southeast1.firebasedatabase.app/")
                    .getReference().child("Users").child(userDisplayName).child("goals");
            fetchAccountBalances();
            fetchTransactions();
            fetchAndUpdateAccountBalances();
            fetchGoals();
        }

        fabAddGoal = view.findViewById(R.id.fab_add_goal);
        fabAddGoal.setOnClickListener(v -> showAddGoalDialog());

        goalsRecyclerView = view.findViewById(R.id.goals_recycler_view);
        goalsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        goalsAdapter = new GoalsAdapter(new ArrayList<>(), this);
        goalsRecyclerView.setAdapter(goalsAdapter);



        fabAddTransaction = requireActivity().findViewById(R.id.fab_add_transaction);
        if (fabAddTransaction != null) {
            fabAddTransaction.setVisibility(View.VISIBLE);
            fabAddTransaction.setOnClickListener(v -> {
                // Handle adding a new transaction
            });
        }

        setupAccountCard(view, R.id.card_cash, R.id.cash_value, "Cash");
        setupAccountCard(view, R.id.card_bank, R.id.bank_value, "Bank");
        setupAccountCard(view, R.id.card_bkash, R.id.bkash_value, "Bkash");
        setupAccountCard(view, R.id.card_nagad, R.id.nagad_value, "Nagad");
        setupAccountCard(view, R.id.card_receive, R.id.receive_value, "Receivable");
        setupAccountCard(view, R.id.card_pay, R.id.pay_value, "Payable");

        goalsRecyclerView = view.findViewById(R.id.goals_recycler_view);
        goalsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        goalsAdapter = new GoalsAdapter(new ArrayList<>(), this);
        goalsRecyclerView.setAdapter(goalsAdapter);

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (getChildFragmentManager().getBackStackEntryCount() > 0) {
                    getChildFragmentManager().popBackStack();
                } else {
                    requireActivity().getSupportFragmentManager().popBackStack();
                }
            }
        });

        return view;
    }

    private void showAddGoalDialog() {
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_add_goal);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        EditText titleInput = dialog.findViewById(R.id.goal_title_input);
        EditText targetAmountInput = dialog.findViewById(R.id.goal_target_amount_input);
        EditText currentAmountInput = dialog.findViewById(R.id.goal_current_amount_input);
        TextView endDateTextView = dialog.findViewById(R.id.goal_end_date);
        Button addButton = dialog.findViewById(R.id.add_goal_button);

        endDateTextView.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), R.style.CustomDatePickerDialog, (view, year, month, dayOfMonth) -> {
                calendar.set(year, month, dayOfMonth);
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                String dateString = dateFormat.format(calendar.getTime());
                endDateTextView.setText(dateString);
                endDateTextView.setTag(dateString);
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        addButton.setOnClickListener(v -> {
            String title = titleInput.getText().toString();
            double targetAmount = Double.parseDouble(targetAmountInput.getText().toString());
            double currentAmount = Double.parseDouble(currentAmountInput.getText().toString());
            String endDate = (String) endDateTextView.getTag();

            Goal goal = new Goal(title, targetAmount, currentAmount, endDate);
            goalsRef.push().setValue(goal).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(getContext(), "Goal added", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } else {
                    Toast.makeText(getContext(), "Failed to add goal", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    // DashboardPage.java
    private void fetchGoals() {
        goalsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Goal> goals = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Goal goal = snapshot.getValue(Goal.class);
                    if (goal != null) {
                        goals.add(goal);
                    }
                }
                goalsAdapter = new GoalsAdapter(goals, DashboardPage.this);
                goalsRecyclerView.setAdapter(goalsAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.err.println("Database error: " + databaseError.getMessage());
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (fabAddTransaction != null) {
            fabAddTransaction.setVisibility(View.GONE);
        }
    }

    private void setupAccountCard(View view, int cardId, int valueId, String accountType) {
        View card = view.findViewById(cardId);
        TextView valueTextView = view.findViewById(valueId);
        card.setOnClickListener(v -> showUpdateBalanceDialog(accountType, valueTextView));
    }

    private void showUpdateBalanceDialog(String accountType, TextView valueTextView) {
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_update_balance);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        EditText balanceInput = dialog.findViewById(R.id.balance_input);
        Button updateButton = dialog.findViewById(R.id.update_button);

        updateButton.setOnClickListener(v -> {
            double newBalance = Double.parseDouble(balanceInput.getText().toString());
            updateAccountBalance(accountType, newBalance);
            valueTextView.setText(String.format("$%.2f", newBalance));
            dialog.dismiss();
        });

        dialog.show();
    }

    public void onGoalLongClick(Goal goal) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Goal")
                .setMessage("Are you sure you want to delete this goal?")
                .setPositiveButton("Yes", (dialog, which) -> deleteGoal(goal))
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteGoal(Goal goal) {
        goalsRef.orderByChild("title").equalTo(goal.getTitle()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    snapshot.getRef().removeValue();
                }
                Toast.makeText(getContext(), "Goal deleted", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.err.println("Database error: " + databaseError.getMessage());
            }
        });
    }


    private void fetchTransactions() {
        DatabaseReference transactionsRef = FirebaseDatabase.getInstance("https://xpense-dab26-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference().child("Users").child(currentUser.getDisplayName()).child("transactions").child("normal");
        transactionsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                double totalIncome = 0;
                double totalExpense = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Transaction transaction = snapshot.getValue(Transaction.class);
                    if (transaction != null) {
                        double amount = transaction.getAmount();
                        String type = transaction.getType();
                        if ("Income".equals(type)) {
                            totalIncome += amount;
                        } else if ("Expense".equals(type)) {
                            totalExpense += amount;
                        }
                    }
                }
                updateIncomeExpenseUI(totalIncome, totalExpense);
                updateBalance(totalIncome, totalExpense);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.err.println("Database error: " + databaseError.getMessage());
            }
        });
    }

    private void updateIncomeExpenseUI(double income, double expense) {
        View rootView = getView();
        if (rootView != null) {
            ((TextView) rootView.findViewById(R.id.income_value)).setText(String.format("৳%.2f", income));
            ((TextView) rootView.findViewById(R.id.expense_value)).setText(String.format("৳%.2f", expense));
        }
    }

    private void updateBalance(double income, double expense) {
        double balance = income + expense;
        View rootView = getView();
        if (rootView != null) {
            ((TextView) rootView.findViewById(R.id.balance_value)).setText(String.format("৳%.2f", balance));
        }
    }

    private void fetchAndUpdateAccountBalances() {
        DatabaseReference transactionsRef = FirebaseDatabase.getInstance("https://xpense-dab26-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference().child("Users").child(currentUser.getDisplayName()).child("transactions").child("normal");

        transactionsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                double cashBalance = 0;
                double bankBalance = 0;
                double bkashBalance = 0;
                double nagadBalance = 0;
                double receivableBalance = 0;
                double payableBalance = 0;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Transaction transaction = snapshot.getValue(Transaction.class);
                    if (transaction != null) {
                        double amount = transaction.getAmount();
                        String account = transaction.getAccount();
                        String type = transaction.getType();
                        switch (account) {
                            case "Cash":
                                cashBalance += amount;
                                break;
                            case "Bank":
                                bankBalance += amount;
                                break;
                            case "Bkash":
                                bkashBalance += amount;
                                break;
                            case "Nagad":
                                nagadBalance += amount;
                                break;
                            case "Receivable":
                                receivableBalance += amount;
                                break;
                            case "Payable":
                                payableBalance += amount;
                                break;
                        }
                    }
                }

                updateAccountBalance("Cash", cashBalance);
                updateAccountBalance("Bank", bankBalance);
                updateAccountBalance("Bkash", bkashBalance);
                updateAccountBalance("Nagad", nagadBalance);
                updateAccountBalance("Receivable", receivableBalance);
                updateAccountBalance("Payable", payableBalance);

                View rootView = getView();
                if (rootView != null) {
                    updateBalanceUI(rootView, "Cash", cashBalance);
                    updateBalanceUI(rootView, "Bank", bankBalance);
                    updateBalanceUI(rootView, "Bkash", bkashBalance);
                    updateBalanceUI(rootView, "Nagad", nagadBalance);
                    updateBalanceUI(rootView, "Receivable", receivableBalance);
                    updateBalanceUI(rootView, "Payable", payableBalance);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.err.println("Database error: " + databaseError.getMessage());
            }
        });
    }

    private void fetchAccountBalances() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                View rootView = getView();
                if (rootView != null) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String accountType = snapshot.getKey();
                        double balance = snapshot.getValue(Double.class);
                        updateBalanceUI(rootView, accountType, balance);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.err.println("Database error: " + databaseError.getMessage());
            }
        });
    }

    private void updateBalanceUI(View rootView, String accountType, double balance) {
        String formattedBalance = String.format(Locale.getDefault(), "৳%.2f", balance);
        switch (accountType) {
            case "Cash":
                ((TextView) rootView.findViewById(R.id.cash_value)).setText(formattedBalance);
                break;
            case "Bank":
                ((TextView) rootView.findViewById(R.id.bank_value)).setText(formattedBalance);
                break;
            case "Bkash":
                ((TextView) rootView.findViewById(R.id.bkash_value)).setText(formattedBalance);
                break;
            case "Nagad":
                ((TextView) rootView.findViewById(R.id.nagad_value)).setText(formattedBalance);
                break;
            case "Receivable":
                ((TextView) rootView.findViewById(R.id.receive_value)).setText(formattedBalance);
                break;
            case "Payable":
                ((TextView) rootView.findViewById(R.id.pay_value)).setText(formattedBalance);
                break;
        }
    }

    private void updateAccountBalance(String accountType, double newBalance) {
        userRef.child(accountType).setValue(newBalance);
    }

}