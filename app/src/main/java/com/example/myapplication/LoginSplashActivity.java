package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class LoginSplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_splash);


        Animation spinAnimation = AnimationUtils.loadAnimation(this, R.anim.spin_animation);


        ImageView logoImageView = findViewById(R.id.logoImageView);


        logoImageView.startAnimation(spinAnimation);

        // Move to the login screen
        Intent intent = new Intent(LoginSplashActivity.this, BottomNavigator.class);
        startActivity(intent);
        finish();
    }
}
