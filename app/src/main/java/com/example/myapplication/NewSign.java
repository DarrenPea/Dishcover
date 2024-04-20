package com.example.myapplication;


import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.example.myapplication.Authentication.AuthManager;
import com.example.myapplication.Authentication.AuthObserver;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.util.Objects;
import java.util.Random;

public class NewSign extends AppCompatActivity implements AuthObserver {

    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private TextInputEditText confirmPasswordEditText;
    private TextView signUpButton;
    private ProgressBar progressBar;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);

        authManager = new AuthManager();
        authManager.registerObserver(this);

        TextInputLayout emailTextInputLayout = findViewById(R.id.username);
        TextInputLayout passwordTextInputLayout = findViewById(R.id.password);
        TextInputLayout confirmPasswordTextInputLayout = findViewById(R.id.editTextConfirmPassword);

        emailEditText = (TextInputEditText) emailTextInputLayout.getEditText();
        passwordEditText = (TextInputEditText) passwordTextInputLayout.getEditText();
        confirmPasswordEditText = (TextInputEditText) confirmPasswordTextInputLayout.getEditText();

        signUpButton = findViewById(R.id.sign_up);
        progressBar = findViewById(R.id.progressBar);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUpButton.startAnimation(AnimationUtils.loadAnimation(NewSign.this, R.anim.button_pressed));
                registerNewUser();
            }
        });
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        authManager.removeObserver(this);
    }

    private void registerNewUser() {
        progressBar.setVisibility(View.VISIBLE);
        String email = Objects.requireNonNull(emailEditText.getText()).toString().trim();
        String password = Objects.requireNonNull(passwordEditText.getText()).toString().trim();
        String confirmPassword = Objects.requireNonNull(confirmPasswordEditText.getText()).toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email is required");
            progressBar.setVisibility(View.GONE);
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password is required");
            progressBar.setVisibility(View.GONE);
            return;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Passwords do not match");
            progressBar.setVisibility(View.GONE);
            return;
        }

        authManager.registerNewUser(email, password);
    }

    public void loginClicked(View view) {
        Intent intent = new Intent(this, NewLogin.class);
        startActivity(intent);
    }

    @Override
    public void onAuthStatusChanged(boolean isSuccess, String message) {
        if (isSuccess) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            startActivity(new Intent(NewSign.this, OnboardFullName.class));
            finish();
        } else {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
        progressBar.setVisibility(View.GONE);
    }

    private String generateUniqueDisplayName() {
        Random random = new Random();
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        return "dishcover-user" + sb.toString();
    }
}

