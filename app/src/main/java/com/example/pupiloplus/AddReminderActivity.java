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
import java.util.ArrayList;
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
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(this, R.array.reminder_types, R.layout.spinner_item);
        typeAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        typeSpinner.setAdapter(typeAdapter);
        typeSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                EditText customTypeInput = findViewById(R.id.edit_custom_reminder_type);
                String selectedType = parent.getItemAtPosition(position).toString();
                if ("Другое".equals(selectedType)) {
                    customTypeInput.setVisibility(android.view.View.VISIBLE);
                } else {
                    customTypeInput.setVisibility(android.view.View.GONE);
                    customTypeInput.setText("");
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        // Загрузка питомцев как CheckBox
        loadPetsAsCheckBoxes();

        // Spinner для периодичности
        frequencySpinner = findViewById(R.id.spinner_frequency);
        ArrayAdapter<CharSequence> frequencyAdapter = ArrayAdapter.createFromResource(this, R.array.frequency_types, R.layout.spinner_item);
        frequencyAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
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
        dateText.setTextColor(getResources().getColor(R.color.secondary));

        // TimePicker для времени напоминания
        timeText = findViewById(R.id.text_reminder_time);
        timeText.setOnClickListener(v -> showTimePicker());
        timeText.setTextColor(getResources().getColor(R.color.secondary));

        Button saveButton = findViewById(R.id.button_save_reminder);
        saveButton.setOnClickListener(v -> saveReminder());

        if (isEdit && reminderId != -1L) {
            loadReminder(reminderId, typeSpinner);
        }
    }

    private void loadReminder(long id, Spinner typeSpinner) {
        Reminder reminder = databaseHelper.getReminderById(id);
        if (reminder == null) return;

        EditText titleInput = findViewById(R.id.edit_reminder_title);
        titleInput.setText(reminder.getTitle());

        typeSpinner.setSelection(((ArrayAdapter) typeSpinner.getAdapter()).getPosition(reminder.getType()));

        String[] dateTime = reminder.getDateTime().split(" ");
        selectedDate = dateTime[0];
        String displayDate = String.format(Locale.US, "%s", selectedDate.replace("-", "."));
        dateText.setText(displayDate);
        dateText.setTextColor(getResources().getColor(R.color.accent));

        if (dateTime.length > 1) {
            String[] timeParts = dateTime[1].split(":");
            selectedHour = Integer.parseInt(timeParts[0]);
            selectedMinute = Integer.parseInt(timeParts[1]);
            String displayTime = String.format(Locale.US, "%02d:%02d", selectedHour, selectedMinute);
            timeText.setText(displayTime);
            timeText.setTextColor(getResources().getColor(R.color.accent));
        }

        String period = reminder.getPeriod();
        if (period.startsWith("По дням недели")) {
            frequencySpinner.setSelection(((ArrayAdapter) frequencySpinner.getAdapter()).getPosition("По дням недели"));
        } else {
            frequencySpinner.setSelection(((ArrayAdapter) frequencySpinner.getAdapter()).getPosition(period));
        }

        if (reminder.getPeriod().contains("По дням недели")) {
            showDaysOfWeekSelector();
            // Parse and check days
            if (reminder.getPeriod().contains(": ")) {
                String daysStr = reminder.getPeriod().substring(reminder.getPeriod().indexOf(": ") + 2);
                String[] selectedDays = daysStr.split(", ");
                LinearLayout daysLayout = findViewById(R.id.layout_days_of_week);
                for (int i = 0; i < daysLayout.getChildCount(); i++) {
                    CheckBox cb = (CheckBox) daysLayout.getChildAt(i);
                    for (String day : selectedDays) {
                        if (cb.getText().toString().equals(day.trim())) {
                            cb.setChecked(true);
                            break;
                        }
                    }
                }
            }
        }

        EditText notesInput = findViewById(R.id.edit_reminder_notes);
        notesInput.setText(reminder.getNotes());

        // Загрузка выбранных питомцев
        List<Reminder> group = databaseHelper.getRemindersByGroup(reminder.getTitle(), reminder.getType(), reminder.getDateTime(), reminder.getPeriod(), reminder.getNotes());
        for (Reminder r : group) {
            loadSelectedPets(r.getPetId());
        }
    }

    private void loadPetsAsCheckBoxes() {
        LinearLayout layout = findViewById(R.id.layout_pet_selection);
        layout.removeAllViews();
        List<Pet> pets = databaseHelper.getAllPets();
        for (Pet pet : pets) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(pet.getName());
            checkBox.setTextColor(getResources().getColor(R.color.black));
            checkBox.setTag(pet.getId());
            layout.addView(checkBox);
        }
    }

    private void loadSelectedPets(long petId) {
        LinearLayout layout = findViewById(R.id.layout_pet_selection);
        for (int i = 0; i < layout.getChildCount(); i++) {
            CheckBox checkBox = (CheckBox) layout.getChildAt(i);
            long id = (long) checkBox.getTag();
            if (id == petId) {
                checkBox.setChecked(true);
                break;
            }
        }
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            Calendar selected = Calendar.getInstance();
            selected.set(year, month, dayOfMonth, 0, 0, 0);
            selected.set(Calendar.MILLISECOND, 0);
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);
            if (selected.before(today)) {
                Toast.makeText(this, "Нельзя выбрать прошедшую дату", Toast.LENGTH_SHORT).show();
                return;
            }
            selectedDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            String displayDate = String.format(Locale.US, "%02d.%02d.%04d", dayOfMonth, month + 1, year);
            dateText.setText(displayDate);
            dateText.setTextColor(getResources().getColor(R.color.accent));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            if (!selectedDate.isEmpty()) {
                Calendar selected = Calendar.getInstance();
                String[] parts = selectedDate.split("-");
                selected.set(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]) - 1, Integer.parseInt(parts[2]), hourOfDay, minute, 0);
                selected.set(Calendar.MILLISECOND, 0);
                Calendar nowPlusOne = Calendar.getInstance();
                nowPlusOne.add(Calendar.MINUTE, 1);
                if (selected.before(nowPlusOne)) {
                    Toast.makeText(this, "Нельзя выбрать прошедшее время", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            selectedHour = hourOfDay;
            selectedMinute = minute;
            String displayTime = String.format(Locale.US, "%02d:%02d", hourOfDay, minute);
            timeText.setText(displayTime);
            timeText.setTextColor(getResources().getColor(R.color.accent));
        }, selectedHour, selectedMinute, true);
        timePickerDialog.show();
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
        if (selectedDate.isEmpty() || timeText.getText().toString().isEmpty()) {
            Toast.makeText(this, "Выберите дату и время", Toast.LENGTH_SHORT).show();
            return;
        }

        EditText titleInput = findViewById(R.id.edit_reminder_title);
        Spinner typeSpinner = findViewById(R.id.spinner_reminder_type);
        Spinner frequencySpinner = findViewById(R.id.spinner_frequency);
        EditText notesInput = findViewById(R.id.edit_reminder_notes);
        EditText customTypeInput = findViewById(R.id.edit_custom_reminder_type);

        String title = titleInput.getText().toString().trim();
        String type = typeSpinner.getSelectedItem().toString();
        if ("Другое".equals(type)) {
            String customType = customTypeInput.getText().toString().trim();
            if (!customType.isEmpty()) {
                type = customType;
            }
        }
        String date = selectedDate;
        String frequency = frequencySpinner.getSelectedItem().toString();
        if (frequency.equals("По дням недели")) {
            StringBuilder days = new StringBuilder();
            LinearLayout daysLayout = findViewById(R.id.layout_days_of_week);
            for (int i = 0; i < daysLayout.getChildCount(); i++) {
                CheckBox cb = (CheckBox) daysLayout.getChildAt(i);
                if (cb.isChecked()) {
                    if (days.length() > 0) days.append(", ");
                    days.append(cb.getText().toString());
                }
            }
            frequency = "По дням недели: " + days.toString();
        }
        String notes = notesInput.getText().toString().trim();

        if (title.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "Введите название и выберите дату", Toast.LENGTH_SHORT).show();
            return;
        }

        // Получить выбранных питомцев
        LinearLayout layout = findViewById(R.id.layout_pet_selection);
        List<Long> selectedPetIds = new ArrayList<>();
        for (int i = 0; i < layout.getChildCount(); i++) {
            CheckBox checkBox = (CheckBox) layout.getChildAt(i);
            if (checkBox.isChecked()) {
                selectedPetIds.add((Long) checkBox.getTag());
            }
        }

        if (selectedPetIds.isEmpty()) {
            Toast.makeText(this, "Выберите хотя бы одного питомца", Toast.LENGTH_SHORT).show();
            return;
        }

        String dateTimeString = date + " " + String.format(Locale.US, "%02d:%02d", selectedHour, selectedMinute);
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
            java.util.Date reminderDate = sdf.parse(dateTimeString);
            java.util.Date now = new java.util.Date();
            now.setTime(now.getTime() + 60000); // Allow 1 minute later
            if (reminderDate.before(now)) {
                Toast.makeText(this, "Нельзя сохранить напоминание на прошедшее время", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (Exception e) {
            // ignore
        }

        // Создать напоминания для каждого питомца
        if (isEdit) {
            Reminder oldReminder = databaseHelper.getReminderById(reminderId);
            if (oldReminder != null) {
                List<Reminder> oldGroup = databaseHelper.getRemindersByGroup(oldReminder.getTitle(), oldReminder.getType(), oldReminder.getDateTime(), oldReminder.getPeriod(), oldReminder.getNotes());
                for (Reminder r : oldGroup) {
                    NotificationScheduler.cancel(this, r.getId());
                    databaseHelper.deleteReminder(r.getId());
                }
            }
        }
        for (Long petId : selectedPetIds) {
            Reminder reminder = new Reminder();
            reminder.setTitle(title);
            reminder.setType(type);
            reminder.setDateTime(dateTimeString);
            reminder.setPeriod(frequency);
            reminder.setNotes(notes.isEmpty() ? "" : notes);
            reminder.setImageRes(getImageResForType(type));
            reminder.setPetId(petId);

            long savedReminderId = databaseHelper.insertReminder(reminder);
            NotificationScheduler.schedule(this, savedReminderId, title, "Напоминание для питомца", dateTimeString, frequency);
        }

        Toast.makeText(this, isEdit ? "Напоминание обновлено" : "Напоминание добавлено", Toast.LENGTH_SHORT).show();
        finish();
    }

    private int getImageResForType(String type) {
        if (type.contains("Кормление")) {
            return R.drawable.ic_reminder; // Use different if available
        } else if (type.contains("Врач")) {
            return R.drawable.ic_reminder;
        } else if (type.contains("Лотка")) {
            return R.drawable.ic_reminder;
        } else if (type.contains("Лекарства")) {
            return R.drawable.ic_reminder;
        } else if (type.contains("Воды")) {
            return R.drawable.ic_reminder;
        } else {
            return R.drawable.ic_reminder;
        }
    }
}
