package com.example.pupiloplus;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pupiloplus.data.DatabaseHelper;
import com.example.pupiloplus.data.Pet;
import com.example.pupiloplus.ui.CustomCalendarView;
import com.example.pupiloplus.ui.PetAdapter;

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
}
