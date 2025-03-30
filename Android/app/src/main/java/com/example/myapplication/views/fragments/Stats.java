// Stats.java
package com.example.myapplication.views.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.myapplication.R;
import com.example.myapplication.adapters.TransactionsAdapter;
import com.example.myapplication.models.Transaction;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
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

public class Stats extends Fragment {

    private PieChart pieChart;
    private BarChart barChart;
    private LineChart lineChart;
    private TextView incomeBtn;
    private TextView expenseBtn;
    private TransactionsAdapter adapter;
    private List<Transaction> transactions;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private TextView currentDateTextView;
    private TextView totalIncomeTextView;
    private TextView totalExpensesTextView;
    private TextView balanceTextView;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    private SimpleDateFormat monthFormat = new SimpleDateFormat("MM/yyyy");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stats, container, false);

        pieChart = view.findViewById(R.id.pieChart);
        barChart = view.findViewById(R.id.barChart);
        lineChart = view.findViewById(R.id.lineChart);
        incomeBtn = view.findViewById(R.id.incomeBtn);
        expenseBtn = view.findViewById(R.id.expenseBtn);
        currentDateTextView = getActivity().findViewById(R.id.currentDate);
        totalIncomeTextView = getActivity().findViewById(R.id.totalIncome);
        totalExpensesTextView = getActivity().findViewById(R.id.totalExpenses);
        balanceTextView = getActivity().findViewById(R.id.balance);

        transactions = new ArrayList<>();
        adapter = new TransactionsAdapter(transactions, null);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userDisplayName = currentUser.getDisplayName();
            databaseReference = FirebaseDatabase.getInstance("https://xpense-dab26-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference()
                    .child("Users").child(userDisplayName).child("transactions").child("normal");

            fetchTransactions();
        }

        incomeBtn.setOnClickListener(v -> {
            updateChartsForIncome();
            setSelectedButton(incomeBtn, expenseBtn);
        });
        expenseBtn.setOnClickListener(v -> {
            updateChartsForExpense();
            setSelectedButton(expenseBtn, incomeBtn);
        });

        // Set default data to Income
        updateChartsForIncome();
        setSelectedButton(incomeBtn, expenseBtn);

        return view;
    }

    private void fetchTransactions() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                transactions.clear();

                double totalIncome = 0;
                double totalExpenses = 0;
                String selectedDate = currentDateTextView != null ? currentDateTextView.getText().toString() : "";

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Transaction transaction = snapshot.getValue(Transaction.class);
                    if (transaction != null) {
                        try {
                            Date transactionDate = sdf.parse(transaction.getDate());
                            String transactionMonth = monthFormat.format(transactionDate);

                            if (transaction.getDate().equals(selectedDate) || transactionMonth.equals(selectedDate)) {
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

                double balance = totalIncome - totalExpenses;

                // Update UI elements with the calculated values
                updateIncomeExpenseUI(totalIncome, totalExpenses, balance);

                Collections.sort(transactions, new Comparator<Transaction>() {
                    @Override
                    public int compare(Transaction t1, Transaction t2) {
                        try {
                            Date dateTime1 = sdf.parse(t1.getDate() + " " + t1.getTime());
                            Date dateTime2 = sdf.parse(t2.getDate() + " " + t2.getTime());
                            return dateTime1.compareTo(dateTime2);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        return 0;
                    }
                });

                adapter.notifyDataSetChanged();
                updateChartsForIncome(); // Update charts with the fetched data
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.err.println("Database error: " + databaseError.getMessage());
            }
        });
    }

    private void updateIncomeExpenseUI(double income, double expense, double balance) {
        if (totalIncomeTextView != null) {
            totalIncomeTextView.setText(String.format("$%.2f", income));
        }
        if (totalExpensesTextView != null) {
            totalExpensesTextView.setText(String.format("$%.2f", expense));
        }
        if (balanceTextView != null) {
            balanceTextView.setText(String.format("$%.2f", balance));
        }
    }

    private void updateChartsForIncome() {
        List<PieEntry> pieEntries = new ArrayList<>();
        List<BarEntry> barEntries = new ArrayList<>();
        List<Entry> lineEntries = new ArrayList<>();

        for (Transaction transaction : transactions) {
            if (transaction.getType().equals("Income")) {
                pieEntries.add(new PieEntry((float) transaction.getAmount(), transaction.getCategory()));
                barEntries.add(new BarEntry(barEntries.size() + 1, (float) transaction.getAmount()));
                lineEntries.add(new Entry(lineEntries.size() + 1, (float) transaction.getAmount()));
            }
        }

        PieDataSet pieDataSet = new PieDataSet(pieEntries, "Income Sources");
        pieDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        PieData pieData = new PieData(pieDataSet);
        pieChart.setData(pieData);
        pieChart.invalidate();

        BarDataSet barDataSet = new BarDataSet(barEntries, "Monthly Income");
        barDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);
        barChart.invalidate();

        LineDataSet lineDataSet = new LineDataSet(lineEntries, "Income Trends");
        lineDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        LineData lineData = new LineData(lineDataSet);
        lineChart.setData(lineData);
        lineChart.invalidate();
    }

    private void updateChartsForExpense() {
        List<PieEntry> pieEntries = new ArrayList<>();
        List<BarEntry> barEntries = new ArrayList<>();
        List<Entry> lineEntries = new ArrayList<>();

        for (Transaction transaction : transactions) {
            if (transaction.getType().equals("Expense")) {
                float amount = (float) Math.abs(transaction.getAmount()); // Invert the value for display
                pieEntries.add(new PieEntry(amount, transaction.getCategory()));
                barEntries.add(new BarEntry(barEntries.size() + 1, amount));
                lineEntries.add(new Entry(lineEntries.size() + 1, amount));
            }
        }

        PieDataSet pieDataSet = new PieDataSet(pieEntries, "Expense Categories");
        pieDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        PieData pieData = new PieData(pieDataSet);
        pieChart.setData(pieData);
        pieChart.invalidate();

        BarDataSet barDataSet = new BarDataSet(barEntries, "Monthly Expenses");
        barDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);
        barChart.invalidate();

        LineDataSet lineDataSet = new LineDataSet(lineEntries, "Expense Trends");
        lineDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        LineData lineData = new LineData(lineDataSet);
        lineChart.setData(lineData);
        lineChart.invalidate();
    }

    private void setSelectedButton(TextView selectedBtn, TextView unselectedBtn) {
        selectedBtn.setBackgroundResource(R.drawable.income_selector);
        unselectedBtn.setBackgroundResource(R.drawable.default_selector);
    }
}