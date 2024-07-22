package com.example.guessr;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        final Animation animAnticipate = AnimationUtils.loadAnimation(this, R.anim.anticipate);
        final ImageView image = (ImageView)findViewById(R.id.logo);

        new Handler().postDelayed(new Runnable() {

            @Override

            public void run() {

                Intent i = new Intent(SplashScreen.this, StartScreen.class);
                startActivity(i);
                finish();

            }

        }, 2*1000); // wait for 5 seconds
        image.startAnimation(animAnticipate);


    }
}
