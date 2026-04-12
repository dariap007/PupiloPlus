package com.example.pupiloplus;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.pupiloplus.data.DatabaseHelper;
import com.example.pupiloplus.data.Pet;
import com.example.pupiloplus.data.Reminder;
import com.example.pupiloplus.notifications.NotificationScheduler;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddReminderActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private int selectedHour = 9;
    private int selectedMinute = 0;
    private String selectedDate = "";
    private TextView dateText;
    private TextView timeText;
    private Spinner frequencySpinner;
    private LinearLayout daysLayout;
    private Long reminderId = -1L;
    private boolean isEdit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(SettingsActivity.getCurrentTheme(this));
        setContentView(R.layout.activity_add_reminder);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        databaseHelper = new DatabaseHelper(this);

        reminderId = getIntent().getLongExtra("reminderId", -1L);
        isEdit = getIntent().getBooleanExtra("edit", false);

        // Spinner для типа напоминания
        Spinner typeSpinner = findViewById(R.id.spinner_reminder_type);
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(this, R.array.reminder_types, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(typeAdapter);

        // Spinner для выбора животного
        Spinner petSpinner = findViewById(R.id.spinner_pet_reminder);
        loadPetsIntoSpinner(petSpinner);

        // Spinner для периодичности
        frequencySpinner = findViewById(R.id.spinner_frequency);
        ArrayAdapter<CharSequence> frequencyAdapter = ArrayAdapter.createFromResource(this, R.array.frequency_types, android.R.layout.simple_spinner_item);
        frequencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        frequencySpinner.setAdapter(frequencyAdapter);
        frequencySpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                daysLayout = findViewById(R.id.layout_days_of_week);
                if (position == 2) { // "По дням недели"
                    showDaysOfWeekSelector();
                } else {
                    daysLayout.setVisibility(android.view.View.GONE);
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        // DatePicker для даты напоминания
        dateText = findViewById(R.id.text_reminder_date);
        dateText.setOnClickListener(v -> showDatePicker());

        // TimePicker для времени
        timeText = findViewById(R.id.text_reminder_time);
        timeText.setOnClickListener(v -> showTimePicker());

        Button saveButton = findViewById(R.id.button_save_reminder);
        saveButton.setOnClickListener(v -> saveReminder());

        if (isEdit && reminderId != -1L) {
            loadReminder(reminderId, typeSpinner, petSpinner);
        }
    }

    private void loadReminder(long id, Spinner typeSpinner, Spinner petSpinner) {
        Reminder reminder = databaseHelper.getReminderById(id);
        if (reminder == null) return;

        EditText titleInput = findViewById(R.id.edit_reminder_title);
        titleInput.setText(reminder.getTitle());

        typeSpinner.setSelection(((ArrayAdapter) typeSpinner.getAdapter()).getPosition(reminder.getType()));

        String[] dateTime = reminder.getDateTime().split(" ");
        selectedDate = dateTime[0];
        String[] timeParts = dateTime[1].split(":");
        selectedHour = Integer.parseInt(timeParts[0]);
        selectedMinute = Integer.parseInt(timeParts[1]);
        
        String displayDate = String.format(Locale.US, "%s", selectedDate.replace("-", "."));
        dateText.setText(displayDate);
        timeText.setText(String.format("%02d:%02d", selectedHour, selectedMinute));

        frequencySpinner.setSelection(((ArrayAdapter) frequencySpinner.getAdapter()).getPosition(reminder.getPeriod()));

        EditText notesInput = findViewById(R.id.edit_reminder_notes);
        notesInput.setText(reminder.getNotes());

        if (reminder.getPetId() > 0) {
            List<Pet> pets = databaseHelper.getAllPets();
            for (int i = 0; i < pets.size(); i++) {
                if (pets.get(i).getId() == reminder.getPetId()) {
                    petSpinner.setSelection(i + 1);
                    break;
                }
            }
        }
    }

    private void loadPetsIntoSpinner(Spinner spinner) {
        List<Pet> pets = databaseHelper.getAllPets();
        String[] petNames = new String[pets.size() + 1];
        petNames[0] = "-- Выберите животное --";
        for (int i = 0; i < pets.size(); i++) {
            petNames[i + 1] = pets.get(i).getName();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, petNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            selectedDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            String displayDate = String.format(Locale.US, "%02d.%02d.%04d", dayOfMonth, month + 1, year);
            dateText.setText(displayDate);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void showTimePicker() {
        TimePickerDialog dialog = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            selectedHour = hourOfDay;
            selectedMinute = minute;
            timeText.setText(String.format("%02d:%02d", hourOfDay, minute));
        }, selectedHour, selectedMinute, true);
        dialog.show();
    }

    private void showDaysOfWeekSelector() {
        daysLayout = findViewById(R.id.layout_days_of_week);
        daysLayout.removeAllViews();
        daysLayout.setVisibility(android.view.View.VISIBLE);

        String[] days = getResources().getStringArray(R.array.days_of_week);
        for (String day : days) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(day);
            checkBox.setTextColor(getResources().getColor(R.color.textColor));
            daysLayout.addView(checkBox);
        }
    }

    private void saveReminder() {
        EditText titleInput = findViewById(R.id.edit_reminder_title);
        Spinner typeSpinner = findViewById(R.id.spinner_reminder_type);
        Spinner petSpinner = findViewById(R.id.spinner_pet_reminder);
        Spinner frequencySpinner = findViewById(R.id.spinner_frequency);
        EditText notesInput = findViewById(R.id.edit_reminder_notes);

        String title = titleInput.getText().toString().trim();
        String type = typeSpinner.getSelectedItem().toString();
        String date = selectedDate.isEmpty() ? new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Calendar.getInstance().getTime()) : selectedDate;
        String frequency = frequencySpinner.getSelectedItem().toString();
        String notes = notesInput.getText().toString().trim();

        if (title.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "Введите название и выберите дату", Toast.LENGTH_SHORT).show();
            return;
        }

        Reminder reminder = new Reminder();
        if (isEdit) {
            reminder.setId(reminderId);
            // Cancel old notification before creating new one
            NotificationScheduler.cancel(this, reminderId);
        }
        reminder.setTitle(title);
        reminder.setType(type);
        String dateTimeString = date + " " + String.format("%02d:%02d", selectedHour, selectedMinute);
        reminder.setDateTime(dateTimeString);
        reminder.setPeriod(frequency);
        reminder.setNotes(notes.isEmpty() ? "" : notes);
        reminder.setImageRes(R.drawable.ic_reminder);

        long petId = -1;
        if (petSpinner.getSelectedItemPosition() > 0) {
            List<Pet> pets = databaseHelper.getAllPets();
            petId = pets.get(petSpinner.getSelectedItemPosition() - 1).getId();
        }
        reminder.setPetId(petId);

        long savedReminderId;
        if (isEdit) {
            databaseHelper.updateReminder(reminder);
            savedReminderId = reminderId;
            Toast.makeText(this, "Напоминание обновлено", Toast.LENGTH_SHORT).show();
        } else {
            savedReminderId = databaseHelper.insertReminder(reminder);
            Toast.makeText(this, "Напоминание добавлено", Toast.LENGTH_SHORT).show();
        }

        // Schedule the notification
        NotificationScheduler.schedule(this, savedReminderId, title, "Напоминание для питомца", dateTimeString, frequency);

        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
