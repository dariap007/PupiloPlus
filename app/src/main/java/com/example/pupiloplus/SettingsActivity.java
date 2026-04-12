package com.example.pupiloplus;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class SettingsActivity extends AppCompatActivity {
    private static final String PREFS = "pupiloplus_prefs";
    private static final String KEY_DARK = "dark_theme";
    private static final String KEY_NOTIFY = "notifications_enabled";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(getCurrentTheme(this));
        setContentView(R.layout.activity_settings);

        Switch themeSwitch = findViewById(R.id.switch_theme);
        Switch notifySwitch = findViewById(R.id.switch_notifications);
        Button backButton = findViewById(R.id.button_back_settings);

        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        boolean darkMode = prefs.getBoolean(KEY_DARK, false);
        boolean notifications = prefs.getBoolean(KEY_NOTIFY, true);

        themeSwitch.setChecked(darkMode);
        notifySwitch.setChecked(notifications);

        themeSwitch.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            prefs.edit().putBoolean(KEY_DARK, isChecked).apply();
            AppCompatDelegate.setDefaultNightMode(isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
            recreate();
        });

        notifySwitch.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            prefs.edit().putBoolean(KEY_NOTIFY, isChecked).apply();
        });

        backButton.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }

    public static int getCurrentTheme(AppCompatActivity activity) {
        SharedPreferences prefs = activity.getSharedPreferences(PREFS, MODE_PRIVATE);
        return prefs.getBoolean(KEY_DARK, false) ? R.style.Theme_PupiloPlus_Dark : R.style.Theme_PupiloPlus;
    }
}
