package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class GetStarted extends AppCompatActivity {

    Animation topAnim, bottomAnim;
    ImageView image;
    TextView logo, slogan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_landing);

        topAnim = AnimationUtils.loadAnimation(this, R.anim.top_animation);
        bottomAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_animation);

        image = findViewById(R.id.art);
        logo = findViewById(R.id.logo);
        slogan = findViewById(R.id.slogan);

        image.setAnimation(topAnim);
        logo.setAnimation(bottomAnim);
        slogan.setAnimation(bottomAnim);

        // Set animation listener to topAnim
        topAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // Animation started
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // Animation ended, move to the next page
                startNextActivity();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // Animation repeated
            }
        });

        // Set animation listener to bottomAnim
        bottomAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // Animation started
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // Animation ended, move to the next page
                startNextActivity();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // Animation repeated
            }
        });
    }

    private void startNextActivity() {
        // Delay before starting the next activity
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Set custom animations
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

                // Move to the next page
                Intent intent = new Intent(GetStarted.this, NavigationActivity.class);
                startActivity(intent);
                finish(); // Finish current activity to prevent going back
            }
        }, 1000); // Adjust the delay time (in milliseconds) as needed
    }
}
