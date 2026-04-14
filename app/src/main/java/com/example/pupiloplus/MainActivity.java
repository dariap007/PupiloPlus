package com.example.pupiloplus;

import android.Manifest;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pupiloplus.data.DatabaseHelper;
import com.example.pupiloplus.data.Pet;
import com.example.pupiloplus.ui.CustomCalendarView;
import com.example.pupiloplus.ui.PetAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private PetAdapter petAdapter;
    private TextView calendarHint;
    private CustomCalendarView customCalendarView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(SettingsActivity.getCurrentTheme(this));
        setContentView(R.layout.activity_main);

        requestPermissionsIfNeeded();

        databaseHelper = new DatabaseHelper(this);

        ImageButton settingsButton = findViewById(R.id.button_settings);
        settingsButton.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));

        ImageButton addPetButton = findViewById(R.id.button_add_pet);
        addPetButton.setOnClickListener(v -> {
            startActivity(new Intent(this, AddPetActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        Button remindersTab = findViewById(R.id.button_tab_reminders);
        remindersTab.setOnClickListener(v -> {
            startActivity(new Intent(this, ReminderActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        });

        Button homeTab = findViewById(R.id.button_tab_home);
        homeTab.setOnClickListener(v -> {
            // Already on home, do nothing
        });

        // Setup custom calendar
        customCalendarView = findViewById(R.id.calendar_view);
        calendarHint = findViewById(R.id.text_calendar_hint);

        Button prevMonthButton = findViewById(R.id.button_prev_month);
        prevMonthButton.setOnClickListener(v -> customCalendarView.previousMonth());

        Button nextMonthButton = findViewById(R.id.button_next_month);
        nextMonthButton.setOnClickListener(v -> customCalendarView.nextMonth());

        Button todayButton = findViewById(R.id.button_today);
        todayButton.setOnClickListener(v -> customCalendarView.goToToday());

        customCalendarView.setOnDateSelectedListener((year, month, day) -> {
            String dateText = String.format("%02d.%02d.%d", day, month, year);
            int count = databaseHelper.getReminderCountOnDate(year, month, day);
            calendarHint.setText(count > 0 ? "Напоминаний: " + count + " на " + dateText : "Нет напоминаний на " + dateText);
        });

        RecyclerView petRecycler = findViewById(R.id.recycler_pets);
        petRecycler.setLayoutManager(new LinearLayoutManager(this));
        petAdapter = new PetAdapter(this, databaseHelper.getAllPets(), pet -> {
            Intent intent = new Intent(MainActivity.this, PetProfileActivity.class);
            intent.putExtra("petId", pet.getId());
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        petRecycler.setAdapter(petAdapter);

        loadPets();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPets();
        if (customCalendarView != null) {
            customCalendarView.invalidate();
        }
    }

    private void loadPets() {
        List<Pet> pets = databaseHelper.getAllPets();
        petAdapter.updateItems(pets);
    }

    private void requestPermissionsIfNeeded() {
        List<String> permissions = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
        if (!permissions.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissions.toArray(new String[0]), 100);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        }

        // Check battery optimization
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            if (pm != null && !pm.isIgnoringBatteryOptimizations(getPackageName())) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        }
    }
}
