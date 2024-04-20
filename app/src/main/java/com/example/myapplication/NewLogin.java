package com.example.myapplication;


import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.example.myapplication.Authentication.AuthManager;
import com.example.myapplication.Authentication.AuthObserver;
import com.example.myapplication.Authentication.User;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;


public class NewLogin extends AppCompatActivity implements AuthObserver {

    private EditText emailEditText;
    private EditText passwordEditText;
    private ProgressBar progressBar;
    private AuthManager authManager;
    private CheckBox remember;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        authManager = new AuthManager();
        authManager.registerObserver(this);

        TextInputLayout emailTextInputLayout = findViewById(R.id.username);
        TextInputLayout passwordTextInputLayout = findViewById(R.id.textInputLayout);

        emailEditText = (TextInputEditText) emailTextInputLayout.getEditText();
        passwordEditText = (TextInputEditText) passwordTextInputLayout.getEditText();
        remember = findViewById(R.id.rememberMe);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        retrieveSavedCredentials();

        TextView forgotPasswordButton = findViewById(R.id.forgotpassword);
        forgotPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showForgotPasswordDialog();
            }
        });
    }

    public void loginClicked(View view) {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.button_pressed);
        view.startAnimation(animation);

        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        boolean rememberMe = remember.isChecked();

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email is required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password is required");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);


        authManager.signInUser(email, password, rememberMe, this);

        if (rememberMe) {
            saveCredentials(email, password);
        } else {
            clearSavedCredentials();
        }
    }

    @Override
    public void onAuthStatusChanged(boolean isSuccess, String message) {
        if (isSuccess) {
            Toast.makeText(NewLogin.this, "Login successful", Toast.LENGTH_SHORT).show();
            User currentUser = User.getInstance();
            currentUser.setListener(new User.UserListener() {
                @Override
                public void onUserDataLoaded() {
                    Intent intent = new Intent(NewLogin.this, BottomNavigator.class);
                    startActivity(intent);
                    finish();
                }
                @Override
                public void onUserDataUpdated() {
                }
            });
            currentUser.populateUserData();
        } else {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(NewLogin.this, message, Toast.LENGTH_SHORT).show();
        }
    }


    public void signUpClicked(View view) {
        Intent intent = new Intent(this, NewSign.class);
        startActivity(intent);
    }

    private void saveCredentials(String email, String password) {
        SharedPreferences sharedPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("email", email);
        editor.putString("password", password);
        editor.apply();
    }

    private void retrieveSavedCredentials() {
        SharedPreferences sharedPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        String savedEmail = sharedPreferences.getString("email", "");
        String savedPassword = sharedPreferences.getString("password", "");


        if (!TextUtils.isEmpty(savedEmail) && !TextUtils.isEmpty(savedPassword)) {
            emailEditText.setText(savedEmail);
            passwordEditText.setText(savedPassword);
            remember.setChecked(true);
        }
    }

    private void clearSavedCredentials() {
        SharedPreferences sharedPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("email");
        editor.remove("password");
        editor.apply();
    }

    private void showForgotPasswordDialog() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.forgot_password_dialog, null);

        EditText emailEditText = bottomSheetView.findViewById(R.id.emailLayout);
        TextView sendButton = bottomSheetView.findViewById(R.id.sendText);
        emailEditText.requestFocus();
        bottomSheetDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString().trim();
                if (!TextUtils.isEmpty(email)) {
                    AuthManager authManager = new AuthManager();
                    authManager.resetPasswordLinkSender(email, new AuthObserver() {
                        @Override
                        public void onAuthStatusChanged(boolean isSuccess, String message) {
                            if (isSuccess) {
                                Toast.makeText(NewLogin.this, message, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(NewLogin.this, message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    Toast.makeText(NewLogin.this, "Please enter your email", Toast.LENGTH_SHORT).show();
                }
                bottomSheetDialog.dismiss();
            }
        });

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }

}


