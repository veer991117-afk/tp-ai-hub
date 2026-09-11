package com.tpaihub.app;

import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(32, 48, 32, 32);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setBackgroundColor(Color.rgb(16,16,20));

        TextView logo = new TextView(this);
        logo.setText("TP-AI-HUB");
        logo.setTextColor(Color.WHITE);
        logo.setTextSize(34);
        logo.setGravity(Gravity.CENTER);
        logo.setPadding(0, 20, 0, 16);

        TextView sub = new TextView(this);
        sub.setText("Your AI workspace\nImage • Video • Writing • Coding • Audio");
        sub.setTextColor(Color.LTGRAY);
        sub.setTextSize(16);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 0, 0, 32);

        Button health = new Button(this);
        health.setText("Check Backend");
        health.setOnClickListener(v ->
            Toast.makeText(this, "Backend URL: " + BuildConfig.API_BASE_URL, Toast.LENGTH_LONG).show()
        );

        root.addView(logo, new LinearLayout.LayoutParams(-1, -2));
        root.addView(sub, new LinearLayout.LayoutParams(-1, -2));
        root.addView(health, new LinearLayout.LayoutParams(-1, -2));

        setContentView(root);
    }
}
