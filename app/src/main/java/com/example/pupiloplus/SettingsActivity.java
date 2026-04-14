package com.example.pupiloplus;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.pupiloplus.notifications.NotificationHelper;

public class SettingsActivity extends AppCompatActivity {
    private static final String PREFS = "pupiloplus_prefs";
    private static final String KEY_DARK = "dark_theme";
    private static final String KEY_NOTIFY = "notifications_enabled";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(getCurrentTheme(this));
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Switch notifySwitch = findViewById(R.id.switch_notifications);

        SharedPreferences prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        boolean darkMode = prefs.getBoolean(KEY_DARK, false);
        boolean notifications = prefs.getBoolean(KEY_NOTIFY, true);

        notifySwitch.setChecked(notifications);

        notifySwitch.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            prefs.edit().putBoolean(KEY_NOTIFY, isChecked).apply();
        });
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static int getCurrentTheme(AppCompatActivity activity) {
        SharedPreferences prefs = activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_DARK, false) ? R.style.Theme_PupiloPlus_Dark : R.style.Theme_PupiloPlus;
    }
}
