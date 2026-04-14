package com.example.pupiloplus.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.pupiloplus.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "pupiloplus.db";
    private static final int DATABASE_VERSION = 4;

    private static final String TABLE_PETS = "pets";
    private static final String TABLE_REMINDERS = "reminders";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_PETS + " (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, type TEXT, gender TEXT, birthDate TEXT, breed TEXT, color TEXT, weight TEXT, weightUnit TEXT, chip TEXT, food TEXT, notes TEXT, photoRes INTEGER, photoPath TEXT)");
        db.execSQL("CREATE TABLE " + TABLE_REMINDERS + " (id INTEGER PRIMARY KEY AUTOINCREMENT, petIds TEXT, title TEXT, type TEXT, dateTime TEXT, period TEXT, dose TEXT, extra TEXT, notes TEXT, imageRes INTEGER, notifyBefore TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 4) {
            // Add weightUnit column to existing pets table if not exists
            db.execSQL("ALTER TABLE " + TABLE_PETS + " ADD COLUMN weightUnit TEXT DEFAULT 'кг'");
        }
        // Handle other upgrades if needed
    }

    public long insertPet(Pet pet) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", pet.getName());
        values.put("type", pet.getType());
        values.put("gender", pet.getGender());
        values.put("birthDate", pet.getBirthDate());
        values.put("breed", pet.getBreed());
        values.put("color", pet.getColor());
        values.put("weight", pet.getWeight());
        values.put("weightUnit", pet.getWeightUnit());
        values.put("chip", pet.getChip());
        values.put("food", pet.getFood());
        values.put("notes", pet.getNotes());
        values.put("photoRes", pet.getPhotoRes());
        values.put("photoPath", pet.getPhotoPath());
        return db.insert(TABLE_PETS, null, values);
    }

    public void updatePet(Pet pet) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", pet.getName());
        values.put("type", pet.getType());
        values.put("gender", pet.getGender());
        values.put("birthDate", pet.getBirthDate());
        values.put("breed", pet.getBreed());
        values.put("color", pet.getColor());
        values.put("weight", pet.getWeight());
        values.put("weightUnit", pet.getWeightUnit());
        values.put("chip", pet.getChip());
        values.put("food", pet.getFood());
        values.put("notes", pet.getNotes());
        values.put("photoRes", pet.getPhotoRes());
        values.put("photoPath", pet.getPhotoPath());
        db.update(TABLE_PETS, values, "id = ?", new String[]{String.valueOf(pet.getId())});
    }

    public void deletePet(long id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_PETS, "id = ?", new String[]{String.valueOf(id)});
        db.delete(TABLE_REMINDERS, "petId = ?", new String[]{String.valueOf(id)});
    }

    public List<Pet> getAllPets() {
        List<Pet> pets = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PETS + " ORDER BY id DESC", null)) {
            while (cursor.moveToNext()) {
                pets.add(createPet(cursor));
            }
        }
        return pets;
    }

    public Pet getPetById(long id) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PETS + " WHERE id = ?", new String[]{String.valueOf(id)})) {
            if (cursor.moveToFirst()) {
                return createPet(cursor);
            }
        }
        return null;
    }

    public long insertReminder(Reminder reminder) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("petId", reminder.getPetId());
        values.put("title", reminder.getTitle());
        values.put("type", reminder.getType());
        values.put("dateTime", reminder.getDateTime());
        values.put("period", reminder.getPeriod());
        values.put("dose", reminder.getDose());
        values.put("extra", reminder.getExtra());
        values.put("notes", reminder.getNotes());
        values.put("imageRes", reminder.getImageRes());
        values.put("notifyBefore", reminder.getNotifyBefore());
        return db.insert(TABLE_REMINDERS, null, values);
    }

    public void updateReminder(Reminder reminder) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("petId", reminder.getPetId());
        values.put("title", reminder.getTitle());
        values.put("type", reminder.getType());
        values.put("dateTime", reminder.getDateTime());
        values.put("period", reminder.getPeriod());
        values.put("dose", reminder.getDose());
        values.put("extra", reminder.getExtra());
        values.put("notes", reminder.getNotes());
        values.put("imageRes", reminder.getImageRes());
        values.put("notifyBefore", reminder.getNotifyBefore());
        db.update(TABLE_REMINDERS, values, "id = ?", new String[]{String.valueOf(reminder.getId())});
    }

    public Reminder getReminderById(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_REMINDERS, null, "id = ?", new String[]{String.valueOf(id)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            Reminder reminder = createReminder(cursor);
            cursor.close();
            return reminder;
        }
        return null;
    }

    public List<Reminder> getAllReminders() {
        List<Reminder> reminders = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_REMINDERS, null, null, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                reminders.add(createReminder(cursor));
            }
            cursor.close();
        }
        return reminders;
    }

    public int getReminderCountOnDate(int year, int month, int day) {
        SQLiteDatabase db = getReadableDatabase();
        String dateStr = String.format("%04d-%02d-%02d", year, month, day);
        String dayOfWeek = getDayOfWeek(year, month, day);
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_REMINDERS + " WHERE (dateTime LIKE ? AND period = 'Однократно') OR period = 'Ежедневно' OR (period LIKE 'По дням недели%' AND period LIKE ?)", new String[]{dateStr + "%", "%" + dayOfWeek + "%"});
        if (cursor != null && cursor.moveToFirst()) {
            int count = cursor.getInt(0);
            cursor.close();
            return count;
        }
        return 0;
    }
    public List<String> getReminderTypesOnDate(int year, int month, int day) {
        Set<String> typesSet = new LinkedHashSet<>();
        SQLiteDatabase db = getReadableDatabase();
        String dateStr = String.format("%04d-%02d-%02d", year, month, day);
        String dayOfWeek = getDayOfWeek(year, month, day);
        Cursor cursor = db.rawQuery("SELECT type FROM " + TABLE_REMINDERS + " WHERE (dateTime LIKE ? AND period = 'Однократно') OR period = 'Ежедневно' OR (period LIKE 'По дням недели%' AND period LIKE ?)", new String[]{dateStr + "%", "%" + dayOfWeek + "%"});
        if (cursor != null) {
            while (cursor.moveToNext()) {
                typesSet.add(cursor.getString(0));
            }
            cursor.close();
        }
        return new ArrayList<>(typesSet);
    }
    private String getDayOfWeek(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, day);
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        String[] days = {"Воскресенье", "Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота"};
        return days[dayOfWeek - 1];
    }

    public void deleteReminder(long id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_REMINDERS, "id = ?", new String[]{String.valueOf(id)});
    }

    public List<Reminder> getRemindersByGroup(String title, String type, String dateTime, String period, String notes) {
        List<Reminder> reminders = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_REMINDERS, null, "title = ? AND type = ? AND dateTime = ? AND period = ? AND notes = ?", new String[]{title, type, dateTime, period, notes}, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                reminders.add(createReminder(cursor));
            }
            cursor.close();
        }
        return reminders;
    }

    private Pet createPet(Cursor cursor) {
        Pet pet = new Pet();
        pet.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id")));
        pet.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
        pet.setType(cursor.getString(cursor.getColumnIndexOrThrow("type")));
        pet.setGender(cursor.getString(cursor.getColumnIndexOrThrow("gender")));
        pet.setBirthDate(cursor.getString(cursor.getColumnIndexOrThrow("birthDate")));
        pet.setBreed(cursor.getString(cursor.getColumnIndexOrThrow("breed")));
        pet.setColor(cursor.getString(cursor.getColumnIndexOrThrow("color")));
        pet.setWeight(cursor.getString(cursor.getColumnIndexOrThrow("weight")));
        pet.setWeightUnit(cursor.getString(cursor.getColumnIndexOrThrow("weightUnit")));
        pet.setChip(cursor.getString(cursor.getColumnIndexOrThrow("chip")));
        pet.setFood(cursor.getString(cursor.getColumnIndexOrThrow("food")));
        pet.setNotes(cursor.getString(cursor.getColumnIndexOrThrow("notes")));
        pet.setPhotoRes(cursor.getInt(cursor.getColumnIndexOrThrow("photoRes")));
        if (pet.getPhotoRes() == 0) {
            pet.setPhotoRes(R.drawable.ic_pet);
        }
        pet.setPhotoPath(cursor.getString(cursor.getColumnIndexOrThrow("photoPath")));
        return pet;
    }

    private Reminder createReminder(Cursor cursor) {
        Reminder reminder = new Reminder();
        reminder.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id")));
        reminder.setPetId(cursor.getLong(cursor.getColumnIndexOrThrow("petId")));
        reminder.setTitle(cursor.getString(cursor.getColumnIndexOrThrow("title")));
        reminder.setType(cursor.getString(cursor.getColumnIndexOrThrow("type")));
        reminder.setDateTime(cursor.getString(cursor.getColumnIndexOrThrow("dateTime")));
        reminder.setPeriod(cursor.getString(cursor.getColumnIndexOrThrow("period")));
        reminder.setDose(cursor.getString(cursor.getColumnIndexOrThrow("dose")));
        reminder.setExtra(cursor.getString(cursor.getColumnIndexOrThrow("extra")));
        reminder.setNotes(cursor.getString(cursor.getColumnIndexOrThrow("notes")));
        reminder.setImageRes(cursor.getInt(cursor.getColumnIndexOrThrow("imageRes")));
        reminder.setNotifyBefore(cursor.getString(cursor.getColumnIndexOrThrow("notifyBefore")));
        return reminder;
    }
}
