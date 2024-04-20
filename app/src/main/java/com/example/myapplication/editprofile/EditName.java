package com.example.myapplication.editprofile;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.EditAccountActivity;
import com.example.myapplication.Authentication.User;
import com.example.myapplication.R;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

public class EditName extends AppCompatActivity {

    private TextView saveButton;
    private TextInputEditText editTextFullName;
    private ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_name);

        backButton = findViewById(R.id.backButton);
        saveButton = findViewById(R.id.save);
        editTextFullName = findViewById(R.id.editTextName);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateFullName();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditName.this, EditAccountActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
        });
    }

    private void updateFullName() {
        String fullName = Objects.requireNonNull(editTextFullName.getText()).toString().trim();
        if (fullName.isEmpty()) {
            Toast.makeText(EditName.this, "Please enter your full name.", Toast.LENGTH_SHORT).show();
            return;
        }
        User user = User.getInstance();
        user.setFullName(fullName);
        Toast.makeText(EditName.this, "Full name updated successfully", Toast.LENGTH_SHORT).show();
        finish();
    }
}
