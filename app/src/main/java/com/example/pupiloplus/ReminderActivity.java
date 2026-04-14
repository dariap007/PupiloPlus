package com.example.pupiloplus;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pupiloplus.data.DatabaseHelper;
import com.example.pupiloplus.data.Pet;
import com.example.pupiloplus.data.Reminder;
import com.example.pupiloplus.notifications.NotificationScheduler;
import com.example.pupiloplus.ui.ReminderAdapter;

import java.util.List;

public class ReminderActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private ReminderAdapter reminderAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(SettingsActivity.getCurrentTheme(this));
        setContentView(R.layout.activity_reminders);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        databaseHelper = new DatabaseHelper(this);

        Button addReminderButton = findViewById(R.id.button_add_reminder);
        addReminderButton.setOnClickListener(v -> {
            startActivity(new Intent(this, AddReminderActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        Button filterButton = findViewById(R.id.button_filter_reminders);
        filterButton.setOnClickListener(v -> showFilterDialog());

        Button homeTab = findViewById(R.id.button_tab_home);
        homeTab.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            finish();
        });

        RecyclerView recyclerReminders = findViewById(R.id.recycler_reminders);
        recyclerReminders.setLayoutManager(new LinearLayoutManager(this));
        reminderAdapter = new ReminderAdapter(this, databaseHelper.getAllReminders(), reminder -> showReminderOptions(reminder));
        recyclerReminders.setAdapter(reminderAdapter);

        loadReminders();
    }

    private void showReminderOptions(Reminder reminder) {
        String[] options = {"Редактировать", "Удалить", "Отмена"};
        new AlertDialog.Builder(this)
                .setTitle(reminder.getTitle())
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        editReminder(reminder);
                    } else if (which == 1) {
                        deleteReminder(reminder);
                    }
                })
                .show();
    }

    private void editReminder(Reminder reminder) {
        Intent intent = new Intent(this, AddReminderActivity.class);
        intent.putExtra("reminderId", reminder.getId());
        intent.putExtra("edit", true);
        startActivity(intent);
    }

    private void deleteReminder(Reminder reminder) {
        new AlertDialog.Builder(this)
                .setTitle("Удалить напоминание?")
                .setMessage(reminder.getTitle())
                .setPositiveButton("Удалить", (dialog, which) -> {
                    // Cancel the scheduled notification
                    NotificationScheduler.cancel(this, reminder.getId());
                    databaseHelper.deleteReminder(reminder.getId());
                    loadReminders();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadReminders();
        reminderAdapter.resetFilter();
    }

    private void loadReminders() {
        List<Reminder> reminders = databaseHelper.getAllReminders();
        reminderAdapter.updateItems(reminders);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void showFilterDialog() {
        String[] options = {"Сортировать по времени", "Сортировать по питомцу", "Фильтровать по питомцу", "Фильтровать по частоте", "Показать все"};
        new AlertDialog.Builder(this)
                .setTitle("Фильтр и сортировка")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            reminderAdapter.sortByTime();
                            break;
                        case 1:
                            reminderAdapter.sortByPet();
                            break;
                        case 2:
                            showPetFilterDialog();
                            break;
                        case 3:
                            showFrequencyFilterDialog();
                            break;
                        case 4:
                            reminderAdapter.resetFilter();
                            break;
                    }
                })
                .show();
    }

    private void showPetFilterDialog() {
        List<Pet> pets = databaseHelper.getAllPets();
        String[] petNames = new String[pets.size()];
        for (int i = 0; i < pets.size(); i++) {
            petNames[i] = pets.get(i).getName();
        }
        new AlertDialog.Builder(this)
                .setTitle("Выберите питомца")
                .setItems(petNames, (dialog, which) -> {
                    long petId = pets.get(which).getId();
                    reminderAdapter.filterByPet(petId);
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void showFrequencyFilterDialog() {
        String[] frequencies = {"Однократно", "Ежедневно", "По дням недели"};
        new AlertDialog.Builder(this)
                .setTitle("Выберите частоту")
                .setItems(frequencies, (dialog, which) -> {
                    reminderAdapter.filterByFrequency(frequencies[which]);
                })
                .setNegativeButton("Отмена", null)
                .show();
    }
}
