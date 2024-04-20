package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class SignUp extends AppCompatActivity {

    private boolean isPasswordVisible = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        final ImageButton showPasswordButton = findViewById(R.id.imageButtonShowPassword);
        final EditText passwordEditText = findViewById(R.id.editTextPassword);
        final EditText confirmPasswordEditText = findViewById(R.id.editTextConfirmPassword);
        TextView loginTextView = findViewById(R.id.textViewLogin);


        showPasswordButton.setImageResource(R.drawable.eye_hidden);

        showPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                isPasswordVisible = !isPasswordVisible;
                if (isPasswordVisible) {
                    passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    confirmPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    showPasswordButton.setImageResource(R.drawable.eye);
                } else {
                    passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    confirmPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    showPasswordButton.setImageResource(R.drawable.eye_hidden);
                }


                passwordEditText.setSelection(passwordEditText.getText().length());
                confirmPasswordEditText.setSelection(confirmPasswordEditText.getText().length());
            }
        });

        // Set OnClickListener for "Login" text
        loginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to the login activity
                Intent intent = new Intent(SignUp.this, Login.class);
                startActivity(intent);
            }
        });
    }
}
