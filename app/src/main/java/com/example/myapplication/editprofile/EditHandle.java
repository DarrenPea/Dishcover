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

public class EditHandle extends AppCompatActivity {

    private TextView saveButton;
    private TextInputEditText editTextHandle;
    private ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_handle);

        backButton = findViewById(R.id.backButton);
        saveButton = findViewById(R.id.save);
        editTextHandle = findViewById(R.id.editTextHandle);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateHandle();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditHandle.this, EditAccountActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
        });
    }

    private void updateHandle() {
        String Handle = Objects.requireNonNull(editTextHandle.getText()).toString().trim();
        if (Handle.isEmpty()) {
            Toast.makeText(EditHandle.this, "Please enter your new Dishcover handle.", Toast.LENGTH_SHORT).show();
            return;
        }
        User user = User.getInstance();
        user.setDisplayName(Handle);
        Toast.makeText(EditHandle.this, "Your Dishcover Handle has been updated successfully. Has a ring to it!", Toast.LENGTH_SHORT).show();
        finish();
    }
}
