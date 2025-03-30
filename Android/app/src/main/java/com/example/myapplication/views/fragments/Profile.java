// Profile.java
package com.example.myapplication.views.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.myapplication.R;
import com.example.myapplication.models.Transaction;
import com.example.myapplication.ui.login.LoginActivity;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

public class Profile extends Fragment {

    TextInputLayout fullname, username, phoneNo, email;
    TextView fullnameLabel, usernameLabel, cashLabel, bankLabel;
    Button signOutButton, deleteProfileButton, editProfileButton;
    ImageView notificationIcon, settingsIcon;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        fullname = view.findViewById(R.id.full_name_profile);
        username = view.findViewById(R.id.username_profile);
        phoneNo = view.findViewById(R.id.number_profile);
        email = view.findViewById(R.id.email_profile);
        fullnameLabel = view.findViewById(R.id.fullname_field);
        usernameLabel = view.findViewById(R.id.username_field);
        cashLabel = view.findViewById(R.id.cash_label);
        bankLabel = view.findViewById(R.id.bank_label);
        signOutButton = view.findViewById(R.id.logout_btn);
        editProfileButton = view.findViewById(R.id.edit_profile_btn);
        deleteProfileButton = view.findViewById(R.id.delete_profile_btn);
        notificationIcon = view.findViewById(R.id.notification_icon);
        settingsIcon = view.findViewById(R.id.settings_icon);

        mAuth = FirebaseAuth.getInstance();

        showAllUserData();
        fetchAndUpdateAccountBalances();

        signOutButton.setOnClickListener(v -> signOut());
        editProfileButton.setOnClickListener(v -> updateUserProfile());

        deleteProfileButton.setOnClickListener(v -> showDeleteConfirmationDialog());

        notificationIcon.setOnClickListener(v -> {
            // Handle notification icon click
        });

        settingsIcon.setOnClickListener(v -> openSettings());

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

    private void fetchAndUpdateAccountBalances() {
        DatabaseReference transactionsRef = FirebaseDatabase.getInstance("https://xpense-dab26-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference().child("Users").child(mAuth.getCurrentUser().getDisplayName()).child("transactions").child("normal");

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

                double totalBankBalance = bankBalance + bkashBalance + nagadBalance + receivableBalance + payableBalance;

                cashLabel.setText(String.format(Locale.getDefault(), "$%.2f", cashBalance));
                bankLabel.setText(String.format(Locale.getDefault(), "$%.2f", totalBankBalance));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.err.println("Database error: " + databaseError.getMessage());
            }
        });
    }

    private void updateUserProfile() {
        String newFullname = fullname.getEditText().getText().toString();
        String newUsername = username.getEditText().getText().toString();
        String newPhoneNo = phoneNo.getEditText().getText().toString();
        String newEmail = email.getEditText().getText().toString();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance("https://xpense-dab26-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Users").child(user.getDisplayName());

            userRef.child("fullname").setValue(newFullname);
            userRef.child("phoneNo").setValue(newPhoneNo);
            userRef.child("email").setValue(newEmail);

            user.updateEmail(newEmail).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(getActivity(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Failed to update email", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void showAllUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance("https://xpense-dab26-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Users").child(user.getDisplayName());
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String user_fullname = dataSnapshot.child("fullname").getValue(String.class);
                        String user_username = dataSnapshot.child("username").getValue(String.class);
                        String user_phoneNo = dataSnapshot.child("phoneNo").getValue(String.class);
                        String user_email = dataSnapshot.child("email").getValue(String.class);

                        fullnameLabel.setText(user_fullname);
                        usernameLabel.setText(user_username);
                        fullname.getEditText().setText(user_fullname);
                        username.getEditText().setText(user_username);
                        phoneNo.getEditText().setText(user_phoneNo);
                        email.getEditText().setText(user_email);
                    } else {
                        Log.d("ProfileFragment", "User data not found in database.");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("ProfileFragment", "Database error: " + databaseError.getMessage());
                }
            });
        }
    }

    private void signOut() {
        Log.d("ProfileFragment", "Signing out...");
        mAuth.signOut();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();
    }

    private void openSettings() {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, new Settings());
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Delete Profile");
        builder.setMessage("Are you sure you want to delete your profile? This action cannot be undone.");

        builder.setPositiveButton("Yes", (dialog, which) -> showPasswordConfirmationDialog());
        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showPasswordConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Confirm Password");

        final EditText passwordInput = new EditText(getActivity());
        passwordInput.setHint("Enter your password");
        builder.setView(passwordInput);

        builder.setPositiveButton("Confirm", (dialog, which) -> {
            String password = passwordInput.getText().toString();
            if (!password.isEmpty()) {
                deleteUserProfile(password);
            } else {
                Toast.makeText(getActivity(), "Password cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteUserProfile(String password) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);
            user.reauthenticate(credential).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DatabaseReference userRef = FirebaseDatabase.getInstance("https://xpense-dab26-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Users").child(user.getDisplayName());
                    userRef.removeValue().addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            user.delete().addOnCompleteListener(task2 -> {
                                if (task2.isSuccessful()) {
                                    Log.d("ProfileFragment", "User profile deleted.");
                                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    getActivity().finish();
                                } else {
                                    Log.e("ProfileFragment", "Error deleting user profile: " + task2.getException().getMessage());
                                    Toast.makeText(getActivity(), "Error deleting user profile", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Log.e("ProfileFragment", "Error deleting user data: " + task1.getException().getMessage());
                            Toast.makeText(getActivity(), "Error deleting user data", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Log.e("ProfileFragment", "Error reauthenticating user: " + task.getException().getMessage());
                    Toast.makeText(getActivity(), "Error reauthenticating user", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}