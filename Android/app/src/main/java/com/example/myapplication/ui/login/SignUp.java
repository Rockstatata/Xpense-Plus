package com.example.myapplication.ui.login;
import android.util.Log;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplication.R;
import com.example.myapplication.models.User;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUp extends AppCompatActivity {

    Button alreadySignedUp, regBtn;
    TextInputLayout regName, regUsername, regEmail, regPhoneNo, regPassword;
    DatabaseReference reference1;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();

        alreadySignedUp = findViewById(R.id.sign_in);

        alreadySignedUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUp.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(SignUp.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        regBtn = findViewById(R.id.reg_btn);
        regName = findViewById(R.id.fullname);
        regUsername = findViewById(R.id.username);
        regEmail = findViewById(R.id.email);
        regPhoneNo = findViewById(R.id.phoneno);
        regPassword = findViewById(R.id.password);


        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("SignUp", "Submit button clicked");
                registerUser(v);
            }
        });

    }



    private Boolean validateName(){
        String name = regName.getEditText().getText().toString();
        if(name.isEmpty()){
            regName.setError("Field cannot be empty");
            return false;
        } else {
            regName.setError(null);
            regName.setErrorEnabled(false);
            return true;
        }
    }

    private Boolean validateUsername(){
        String name = regUsername.getEditText().getText().toString();
        String noWhiteSpace = "\\A\\w{4,20}\\z";
        if(name.isEmpty()){
            regName.setError("Field cannot be empty");
            return false;
        } else if(name.length() > 15){
            regName.setError("Username too long");
            return false;
        } else if(!name.matches(noWhiteSpace)){
            regName.setError("White spaces are not allowed");
            return false;
        } else {
            regName.setError(null);
            regName.setErrorEnabled(false);
            return true;
        }
    }

    private Boolean validateEmail(){
        String email = regEmail.getEditText().getText().toString();
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        if(email.isEmpty()){
            regEmail.setError("Field cannot be empty");
            return false;
        } else if(!email.matches(emailPattern)){
            regEmail.setError("Invalid email address");
            return false;
        } else {
            regEmail.setError(null);
            regEmail.setErrorEnabled(false);
            return true;
        }
    }

    private Boolean validatePhoneNo(){
        String phoneNo = regPhoneNo.getEditText().getText().toString();
        if(phoneNo.isEmpty()){
            regPhoneNo.setError("Field cannot be empty");
            return false;
        } else {
            regPhoneNo.setError(null);
            regPhoneNo.setErrorEnabled(false);
            return true;
        }
    }

    private Boolean validatePassword(){
        String password = regPassword.getEditText().getText().toString();
        String passwordVal = "^" +
                "(?=.*[a-zA-Z])" +      // any letter
                "(?=.*[@#$%^&+=])" +    // at least one special character
                "(?=\\S+$)" +           // no white spaces
                ".{4,}" +               // at least 4 characters
                "$";
        if(password.isEmpty()){
            regPassword.setError("Field cannot be empty");
            return false;
        } else if(!password.matches(passwordVal)){
            regPassword.setError("Password is too weak");
            return false;
        } else {
            regPassword.setError(null);
            regPassword.setErrorEnabled(false);
            return true;
        }
    }

    public void registerUser(View view) {
        if (!validateName() | !validateUsername() | !validateEmail() | !validatePhoneNo() | !validatePassword()) {
            return;
        }

        String name = regName.getEditText().getText().toString();
        String username = regUsername.getEditText().getText().toString();
        String email = regEmail.getEditText().getText().toString();
        String phoneNo = regPhoneNo.getEditText().getText().toString();
        String password = regPassword.getEditText().getText().toString();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(username)
                                    .build();

                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(profileUpdateTask -> {
                                        if (profileUpdateTask.isSuccessful()) {
                                            DatabaseReference reference1 = FirebaseDatabase.getInstance("https://xpense-dab26-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Users");
                                            User helperClass = new User(name, username, email, phoneNo, password);
                                            user.sendEmailVerification()
                                                    .addOnCompleteListener(emailVerificationTask -> {
                                                        if (emailVerificationTask.isSuccessful()) {
                                                            reference1.child(username).setValue(helperClass);
                                                            Toast.makeText(SignUp.this, "Verification email sent to " + email, Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            Toast.makeText(SignUp.this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });

                                            Log.d("SignUp", helperClass.toString());
                                            Intent intent = new Intent(getApplicationContext(), VerifyPhone.class);
                                            intent.putExtra("name", name);
                                            intent.putExtra("username", username);
                                            intent.putExtra("email", email);
                                            intent.putExtra("phoneNo", phoneNo);
                                            startActivity(intent);
                                        } else {
                                            Toast.makeText(SignUp.this, "Failed to update user profile.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(SignUp.this, "User is Already Registered!", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}