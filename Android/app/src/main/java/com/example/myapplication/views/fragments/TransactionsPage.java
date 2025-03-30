// TransactionsPage.java
package com.example.myapplication.views.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.activity.OnBackPressedCallback;

import com.example.myapplication.R;
import com.example.myapplication.models.Transaction;
import com.example.myapplication.helpers.DatabaseHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TransactionsPage extends Fragment {

    private TabLayout tabLayout;
    private DatabaseHelper databaseHelper;
    private FirebaseAuth mAuth;
    private DatabaseReference Requests;
    private TextView currentDateTextView;
    private ImageView navigationBack;
    private ImageView navigationNext;
    private Calendar calendar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transactions_page, container, false);

        tabLayout = view.findViewById(R.id.tabLayout);
        FloatingActionButton fabAddTransaction = view.findViewById(R.id.fab_add_transaction);
        mAuth = FirebaseAuth.getInstance();
        databaseHelper = new DatabaseHelper();
        currentDateTextView = view.findViewById(R.id.currentDate);
        navigationBack = view.findViewById(R.id.navigation_back);
        navigationNext = view.findViewById(R.id.navigation_next);
        calendar = Calendar.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userDisplayName = "";
        if (currentUser != null) {
            userDisplayName = currentUser.getDisplayName();
        }
        Requests = FirebaseDatabase.getInstance("https://xpense-dab26-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference().child("Users").child(userDisplayName).child("transactions").child("handshake");

        updateCurrentDate();
        replaceFragment(new Daily());

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Fragment selectedFragment = null;
                switch (tab.getPosition()) {
                    case 0:
                        selectedFragment = new Daily();
                        updateCurrentDate();
                        break;
                    case 1:
                        selectedFragment = new Monthly();
                        updateCurrentDate();
                        break;
                    case 2:
                        selectedFragment = new Stats();
                        break;
                }
                if (selectedFragment != null) {
                    replaceFragment(selectedFragment);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Do nothing
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Do nothing
            }
        });

        fabAddTransaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddTransactionDialog();
            }
        });

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (getChildFragmentManager().getBackStackEntryCount() > 0) {
                    getChildFragmentManager().popBackStack();
                    updateTabLayout();
                } else {
                    requireActivity().getSupportFragmentManager().popBackStack();
                }
            }
        });

        navigationBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleNavigation(false);
            }
        });

        navigationNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleNavigation(true);
            }
        });

        return view;
    }

    private void updateCurrentDate() {
        SimpleDateFormat sdf;
        int tabPosition = tabLayout.getSelectedTabPosition();
        if (tabPosition == 1) { // Monthly
            sdf = new SimpleDateFormat("MM/yyyy");
        } else if (tabPosition == 3) { // Summary
            calendar = Calendar.getInstance(); // Reset to current date
            sdf = new SimpleDateFormat("dd/MM/yyyy");
        } else { // Daily and other tabs
            sdf = new SimpleDateFormat("dd/MM/yyyy");
        }
        currentDateTextView.setText(sdf.format(calendar.getTime()));
    }

    private void handleNavigation(boolean isNext) {
        int tabPosition = tabLayout.getSelectedTabPosition();
        Calendar today = Calendar.getInstance();

        if (tabPosition == 0) { // Daily
            if (isNext) {
                if (calendar.before(today) || calendar.equals(today)) {
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                    if (calendar.after(today)) {
                        calendar = today;
                    }
                }
            } else {
                calendar.add(Calendar.DAY_OF_MONTH, -1);
            }
        } else if (tabPosition == 1) { // Monthly
            if (isNext) {
                if (calendar.before(today) || calendar.equals(today)) {
                    calendar.add(Calendar.MONTH, 1);
                    if (calendar.after(today)) {
                        calendar = today;
                    }
                }
            } else {
                calendar.add(Calendar.MONTH, -1);
            }
        }

        updateCurrentDate();
        fetchTransactions();
    }

    private void fetchTransactions() {
        Fragment currentFragment = getChildFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof Daily) {
            ((Daily) currentFragment).fetchTransactions();
        }
        else if (currentFragment instanceof Monthly) {
            ((Monthly) currentFragment).fetchTransactions();
        }
    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void showAddTransactionDialog() {
        Dialog dialogView = new Dialog(getContext());
        dialogView.setContentView(R.layout.dialog_add_transaction);
        dialogView.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        EditText transactionCategory = dialogView.findViewById(R.id.transactionCategory);
        EditText transactionAmount = dialogView.findViewById(R.id.transactionAmount);
        EditText transactionDate = dialogView.findViewById(R.id.transactionDate);
        EditText transactionAccount = dialogView.findViewById(R.id.transactionAccount);
        EditText transactionNote = dialogView.findViewById(R.id.transactionNote);
        RadioGroup transactionTypeGroup = dialogView.findViewById(R.id.transactionTypeGroup);
        Button saveTransactionButton = dialogView.findViewById(R.id.saveTransactionButton);

        transactionDate.setInputType(InputType.TYPE_NULL);
        transactionDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), R.style.CustomDatePickerDialog, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                        Calendar selectedDate = Calendar.getInstance();
                        selectedDate.set(year, monthOfYear, dayOfMonth);
                        transactionDate.setText(sdf.format(selectedDate.getTime()));
                    }
                }, year, month, day);
                datePickerDialog.show();
            }
        });

        saveTransactionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String category = transactionCategory.getText().toString();
                double amount = Double.parseDouble(transactionAmount.getText().toString());
                String date = transactionDate.getText().toString();
                String account = transactionAccount.getText().toString();
                String note = transactionNote.getText().toString();
                String id = Requests.push().getKey();

                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
                String time = timeFormat.format(Calendar.getInstance().getTime());

                int selectedTypeId = transactionTypeGroup.getCheckedRadioButtonId();
                String type;
                if (selectedTypeId == R.id.incomeRadioButton) {
                    type = "Income";
                } else {
                    type = "Expense";
                    amount = -amount;
                }

                Transaction transaction = new Transaction();
                transaction.setCategory(category);
                transaction.setAmount(amount);
                transaction.setDate(date);
                transaction.setTime(time);
                transaction.setAccount(account);
                transaction.setNote(note);
                transaction.setType(type);
                transaction.setId(id);

                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser != null) {
                    String userDisplayName = currentUser.getDisplayName();
                    databaseHelper.addNormalTransaction(transaction, userDisplayName);
                }

                dialogView.dismiss();
            }
        });
        dialogView.show();
    }

    private void updateTabLayout() {
        Fragment currentFragment = getChildFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof Daily) {
            tabLayout.selectTab(tabLayout.getTabAt(0));
        } else if (currentFragment instanceof Monthly) {
            tabLayout.selectTab(tabLayout.getTabAt(1));
        } else if (currentFragment instanceof Stats) {
            tabLayout.selectTab(tabLayout.getTabAt(2));
        }
    }
}