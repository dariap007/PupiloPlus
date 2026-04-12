package com.example.pupiloplus.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "pupiloplus.db";
    private static final int DATABASE_VERSION = 3;

    private static final String TABLE_PETS = "pets";
    private static final String TABLE_REMINDERS = "reminders";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_PETS + " (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, type TEXT, gender TEXT, birthDate TEXT, breed TEXT, color TEXT, weight TEXT, chip TEXT, food TEXT, notes TEXT, photoRes INTEGER, photoPath TEXT)");
        db.execSQL("CREATE TABLE " + TABLE_REMINDERS + " (id INTEGER PRIMARY KEY AUTOINCREMENT, petId INTEGER, title TEXT, type TEXT, dateTime TEXT, period TEXT, dose TEXT, extra TEXT, notes TEXT, imageRes INTEGER, notifyBefore TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // For testing, drop and recreate tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PETS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REMINDERS);
        onCreate(db);
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
        values.put("title", reminder.getTitle());
        values.put("type", reminder.getType());
        values.put("dateTime", reminder.getDateTime());
        values.put("period", reminder.getPeriod());
        values.put("dose", reminder.getDose());
        values.put("extra", reminder.getExtra());
        values.put("notes", reminder.getNotes());
        values.put("notifyBefore", reminder.getNotifyBefore());
        db.update(TABLE_REMINDERS, values, "id = ?", new String[]{String.valueOf(reminder.getId())});
    }

    public void deleteReminder(long id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_REMINDERS, "id = ?", new String[]{String.valueOf(id)});
    }

    public List<Reminder> getAllReminders() {
        List<Reminder> reminders = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_REMINDERS + " ORDER BY dateTime ASC", null)) {
            while (cursor.moveToNext()) {
                reminders.add(createReminder(cursor));
            }
        }
        return reminders;
    }

    public int getReminderCountOnDate(int year, int month, int day) {
        String search = String.format("%04d-%02d-%02d", year, month, day);
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_REMINDERS + " WHERE dateTime LIKE ?", new String[]{search + "%"})) {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        }
        return 0;
    }

    public Reminder getReminderById(long id) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_REMINDERS + " WHERE id = ?", new String[]{String.valueOf(id)})) {
            if (cursor.moveToFirst()) {
                return createReminder(cursor);
            }
        }
        return null;
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
        pet.setChip(cursor.getString(cursor.getColumnIndexOrThrow("chip")));
        pet.setFood(cursor.getString(cursor.getColumnIndexOrThrow("food")));
        pet.setNotes(cursor.getString(cursor.getColumnIndexOrThrow("notes")));
        pet.setPhotoRes(cursor.getInt(cursor.getColumnIndexOrThrow("photoRes")));
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
