package com.example.myapplication.views.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Settings extends Fragment {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance("https://xpense-dab26-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Users");

        LinearLayout changePassword = view.findViewById(R.id.change_password);
        LinearLayout updateEmail = view.findViewById(R.id.update_email);
        LinearLayout pushNotifications = view.findViewById(R.id.push_notifications);
        LinearLayout emailNotifications = view.findViewById(R.id.email_notifications);
        LinearLayout languagePreferences = view.findViewById(R.id.language_preferences);
        LinearLayout theme = view.findViewById(R.id.theme);
        LinearLayout faq = view.findViewById(R.id.faq);
        LinearLayout contactSupport = view.findViewById(R.id.contact_support);
        LinearLayout termsOfService = view.findViewById(R.id.terms_of_service);
        LinearLayout privacyPolicy = view.findViewById(R.id.privacy_policy);
        LinearLayout appVersion = view.findViewById(R.id.app_version);

        changePassword.setOnClickListener(v -> showChangePasswordDialog());
        updateEmail.setOnClickListener(v -> showUpdateEmailDialog());
        pushNotifications.setOnClickListener(v -> togglePushNotifications());
        emailNotifications.setOnClickListener(v -> toggleEmailNotifications());
        languagePreferences.setOnClickListener(v -> showLanguagePreferencesDialog());
        theme.setOnClickListener(v -> toggleTheme());
        faq.setOnClickListener(v -> openFAQ());
        contactSupport.setOnClickListener(v -> showContactSupportDialog());
        termsOfService.setOnClickListener(v -> openTermsOfService());
        privacyPolicy.setOnClickListener(v -> openPrivacyPolicy());
        appVersion.setOnClickListener(v -> showAppVersion());

        return view;
    }

    private void showChangePasswordDialog() {

        Dialog dialogView = new Dialog(getContext());
        dialogView.setContentView(R.layout.dialog_change_password);
        dialogView.getWindow().setBackgroundDrawableResource(android.R.color.transparent);


        EditText currentPassword = dialogView.findViewById(R.id.current_password);
        EditText newPassword = dialogView.findViewById(R.id.new_password);
        EditText confirmNewPassword = dialogView.findViewById(R.id.confirm_new_password);
        Button changePasswordButton = dialogView.findViewById(R.id.change_password_button);


        changePasswordButton.setOnClickListener(v -> {
            String currentPwd = currentPassword.getText().toString();
            String newPwd = newPassword.getText().toString();
            String confirmNewPwd = confirmNewPassword.getText().toString();

            if (newPwd.equals(confirmNewPwd)) {
                changePassword(currentPwd, newPwd);
                dialogView.dismiss();
            } else {
                Toast.makeText(getContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
            }
        });

        dialogView.show();
    }

    private void changePassword(String currentPwd, String newPwd) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String email = user.getEmail();
            mAuth.signInWithEmailAndPassword(email, currentPwd).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    user.updatePassword(newPwd).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            updatePasswordInDatabase(user.getDisplayName(), newPwd);
                            Toast.makeText(getContext(), "Password changed successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Failed to change password", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(getContext(), "Current password is incorrect", Toast.LENGTH_SHORT).show();
                }
            });
        }
        else{
            Toast.makeText(getContext(), "User is null", Toast.LENGTH_SHORT).show();
        }
    }
    private void updatePasswordInDatabase(String userId, String newPwd) {
        mDatabase.child(userId).child("password").setValue(newPwd);
    }

    private void showUpdateEmailDialog() {
        // Implement the dialog to update the email
        Toast.makeText(getContext(), "Update Email clicked", Toast.LENGTH_SHORT).show();
    }

    private void togglePushNotifications() {
        // Implement the toggle for push notifications
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        boolean isEnabled = sharedPreferences.getBoolean("pushNotifications", true);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("pushNotifications", !isEnabled);
        editor.apply();
        Toast.makeText(getContext(), "Push Notifications " + (!isEnabled ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
    }

    private void toggleEmailNotifications() {
        // Implement the toggle for email notifications
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        boolean isEnabled = sharedPreferences.getBoolean("emailNotifications", true);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("emailNotifications", !isEnabled);
        editor.apply();
        Toast.makeText(getContext(), "Email Notifications " + (!isEnabled ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
    }

    private void showLanguagePreferencesDialog() {
        // Implement the dialog to select the language
        Toast.makeText(getContext(), "Language Preferences clicked", Toast.LENGTH_SHORT).show();
    }

    private void toggleTheme() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        boolean isDarkMode = sharedPreferences.getBoolean("darkMode", false);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("darkMode", !isDarkMode);
        editor.apply();

        // Apply the theme instantly
        getActivity().recreate();
    }

    private void openFAQ() {
        // Implement the action to open FAQ
        Toast.makeText(getContext(), "FAQ clicked", Toast.LENGTH_SHORT).show();
    }

    private void showContactSupportDialog() {
        // Implement the dialog to contact support
        Toast.makeText(getContext(), "Contact Support clicked", Toast.LENGTH_SHORT).show();
    }

    private void openTermsOfService() {
        // Implement the action to open Terms of Service
        Toast.makeText(getContext(), "Terms of Service clicked", Toast.LENGTH_SHORT).show();
    }

    private void openPrivacyPolicy() {
        // Implement the action to open Privacy Policy
        Toast.makeText(getContext(), "Privacy Policy clicked", Toast.LENGTH_SHORT).show();
    }

    private void showAppVersion() {
        // Implement the action to show the app version
        Toast.makeText(getContext(), "App Version clicked", Toast.LENGTH_SHORT).show();
    }
}