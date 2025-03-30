package com.example.myapplication.ui.login;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.views.activities.BaseActivity;
import com.example.myapplication.R;
import com.example.myapplication.ui.login.SignUp;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout username, password;
    Button loginBtn, signUpBtn, forgotPasswordBtn;
    private DatabaseReference reference;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        loginBtn = findViewById(R.id.login);
        signUpBtn = findViewById(R.id.signup);
        forgotPasswordBtn = findViewById(R.id.forgotPass);
        mAuth = FirebaseAuth.getInstance();

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignUp.class);
                startActivity(intent);
            }
        });

        forgotPasswordBtn.setOnClickListener(v -> showForgotPasswordDialog());
    }

    private void showForgotPasswordDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_forgot_password);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        EditText emailInput = dialog.findViewById(R.id.emailInput);
        MaterialButton resetPasswordButton = dialog.findViewById(R.id.resetPasswordButton);

        resetPasswordButton.setOnClickListener(v -> {
            String username = emailInput.getText().toString().trim();
            if (username.isEmpty()) {
                emailInput.setError("Username is required");
                emailInput.requestFocus();
                return;
            }
            DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://xpense-dab26-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Users");
            databaseReference.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Get the email from the snapshot
                        String email = dataSnapshot.child("email").getValue(String.class);
                        sendPasswordResetEmail(username,email);
                        dialog.dismiss();
                        // Use the retrieved email
                        Log.d("FirebaseDatabase", "Email: " + email);
                    } else {
                        Log.d("FirebaseDatabase", "No data found for the specified user.");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle possible errors
                    Log.e("FirebaseDatabase", "Database error: " + databaseError.getMessage());
                }
            });

        });

        dialog.show();
    }

    private void sendPasswordResetEmail(String username, String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Reset link sent to your email", Toast.LENGTH_SHORT).show();
                        showNewPasswordDialog(username);
                    } else {
                        Toast.makeText(LoginActivity.this, "Unable to send reset mail", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showNewPasswordDialog(String username) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_new_password);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCancelable(false); // Make the dialog non-cancelable

        EditText newPasswordInput = dialog.findViewById(R.id.newPasswordInput);
        MaterialButton updatePasswordButton = dialog.findViewById(R.id.updatePasswordButton);

        updatePasswordButton.setOnClickListener(v -> {
            String newPassword = newPasswordInput.getText().toString().trim();
            if (newPassword.isEmpty()) {
                newPasswordInput.setError("Password is required");
                newPasswordInput.requestFocus();
                return;
            }
            updatePasswordInDatabase(username,newPassword);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void updatePasswordInDatabase(String username, String newPassword) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance("https://xpense-dab26-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Users").child(username);
            userRef.child("password").setValue(newPassword)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Password updated in database", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(LoginActivity.this, "Failed to update password in database", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private Boolean validateUsername() {
        String name = username.getEditText().getText().toString();
        if (name.isEmpty()) {
            username.setError("Field cannot be empty");
            return false;
        } else {
            username.setError(null);
            username.setErrorEnabled(false);
            return true;
        }
    }

    private Boolean validatePassword() {
        String passwordInput = password.getEditText().getText().toString();
        if (passwordInput.isEmpty()) {
            password.setError("Field cannot be empty");
            return false;
        } else {
            password.setError(null);
            password.setErrorEnabled(false);
            return true;
        }
    }

    public void loginUser() {
        if (!validateUsername() | !validatePassword()) {
            return;
        } else {
            isUser();
        }
    }

    private void isUser() {
        String userEnteredUsername = username.getEditText().getText().toString().trim();
        String userEnteredPassword = password.getEditText().getText().toString().trim();

        reference = FirebaseDatabase.getInstance("https://xpense-dab26-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Users");
        reference.orderByChild("username").equalTo(userEnteredUsername).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        String emailFromDB = userSnapshot.child("email").getValue(String.class);
                        if (emailFromDB != null) {
                            mAuth.signInWithEmailAndPassword(emailFromDB, userEnteredPassword)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            String nameFromDB = userSnapshot.child("fullname").getValue(String.class);
                                            String phoneNoFromDB = userSnapshot.child("phoneNo").getValue(String.class);
                                            saveUserData(nameFromDB, userEnteredUsername, phoneNoFromDB, emailFromDB, userEnteredPassword);
                                            Intent intent = new Intent(LoginActivity.this, BaseActivity.class);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            password.setError("Wrong Password");
                                            password.requestFocus();
                                        }
                                    });
                        }
                    }
                } else {
                    username.setError("No such User exists");
                    username.requestFocus();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("LoginActivity", "Database error: " + error.getMessage());
            }
        });
    }

    private void saveUserData(String name, String username, String phoneNo, String email, String password) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("name", name);
        editor.putString("username", username);
        editor.putString("phoneNo", phoneNo);
        editor.putString("email", email);
        editor.putString("password", password);
        editor.putBoolean("isLoggedIn", true);
        editor.apply();
        Log.d("LoginActivity", "User data saved: " + name + ", " + username + ", " + phoneNo + ", " + email);
    }
}