package com.srinivasan.photovault;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class SplashActivity extends AppCompatActivity {

    private Animation topAnim,bottomAnim;
    private TextView logo,slogan;
    private ImageView img1,img2,img3,img4,logo_img; //logo_img,

    private static int DELAY = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        topAnim = AnimationUtils.loadAnimation(this, R.anim.top_animation);
        bottomAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_animation);

        logo = findViewById(R.id.logo_txt);
        slogan = findViewById(R.id.slogan);
        logo_img = findViewById(R.id.logo_img);
        img1 = findViewById(R.id.img1);
        img2 = findViewById(R.id.img2);
        img3 = findViewById(R.id.img3);
        img4 = findViewById(R.id.img4);

        logo.setAnimation(bottomAnim);
        slogan.setAnimation(bottomAnim);
        logo_img.setAnimation(topAnim);
        img1.setAnimation(bottomAnim);
        img2.setAnimation(bottomAnim);
        img3.setAnimation(topAnim);
        img4.setAnimation(topAnim);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this,LogInActivity.class);
                startActivity(intent);
                finish();
            }
        },DELAY);

    }
}