package com.example.pupiloplus;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.pupiloplus.data.DatabaseHelper;
import com.example.pupiloplus.data.Pet;
import com.example.pupiloplus.utils.AgeCalculator;

import java.io.IOException;

public class PetProfileActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private Pet pet;
    private long petId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(SettingsActivity.getCurrentTheme(this));
        setContentView(R.layout.activity_pet_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        databaseHelper = new DatabaseHelper(this);

        petId = getIntent().getLongExtra("petId", -1);
        pet = databaseHelper.getPetById(petId);
        if (pet == null) {
            finish();
            return;
        }

        ImageView image = findViewById(R.id.image_profile_pet);
        TextView name = findViewById(R.id.text_profile_name);
        TextView typeAge = findViewById(R.id.text_profile_type_age);
        TextView type = findViewById(R.id.text_profile_type);
        TextView gender = findViewById(R.id.text_profile_gender);
        TextView birthdate = findViewById(R.id.text_profile_birthdate);
        TextView breed = findViewById(R.id.text_profile_breed);
        TextView color = findViewById(R.id.text_profile_color);
        TextView weight = findViewById(R.id.text_profile_weight);
        TextView chip = findViewById(R.id.text_profile_chip);
        TextView food = findViewById(R.id.text_profile_food);
        TextView notes = findViewById(R.id.text_profile_notes);
        Button editButton = findViewById(R.id.button_edit_pet);
        Button deleteButton = findViewById(R.id.button_delete_pet);

        // Load pet photo
        if (pet.getPhotoPath() != null && !pet.getPhotoPath().isEmpty()) {
            try {
                Bitmap bitmap = loadBitmapFromInternalStorage(pet.getPhotoPath());
                if (bitmap != null) {
                    image.setImageBitmap(bitmap);
                } else {
                    image.setImageResource(pet.getPhotoRes());
                }
            } catch (Exception e) {
                image.setImageResource(pet.getPhotoRes());
            }
        } else {
            image.setImageResource(pet.getPhotoRes());
        }

        name.setText(pet.getName());
        String age = AgeCalculator.calculateAge(pet.getBirthDate());
        String displayType = applyGenderInflection(pet.getType(), pet.getGender());
        typeAge.setText(displayType + ", " + age);
        
        type.setText("Вид: " + displayType);
        gender.setText("Пол: " + (pet.getGender().isEmpty() ? "—" : pet.getGender()));
        birthdate.setText("Дата рождения: " + (pet.getBirthDate().isEmpty() ? "—" : pet.getBirthDate()));
        breed.setText("Порода: " + (pet.getBreed().isEmpty() ? "—" : pet.getBreed()));
        color.setText("Окрас: " + (pet.getColor().isEmpty() ? "—" : pet.getColor()));
        weight.setText("Вес: " + (pet.getWeight().isEmpty() ? "—" : pet.getWeight()));
        chip.setText("Чип: " + (pet.getChip().isEmpty() ? "—" : pet.getChip()));
        food.setText("Корм: " + (pet.getFood().isEmpty() ? "—" : pet.getFood()));
        notes.setText(pet.getNotes().isEmpty() ? "—" : pet.getNotes());

        editButton.setOnClickListener(v -> editPet());
        deleteButton.setOnClickListener(v -> showDeleteConfirmation());
    }

    private String applyGenderInflection(String type, String gender) {
        if (type == null || type.isEmpty()) {
            return "";
        }
        if (gender == null || gender.isEmpty()) {
            return type;
        }
        if ("Самец".equals(gender)) {
            switch (type) {
                case "Кошка":
                    return "Кот";
                case "Собака":
                    return "Пёс";
                default:
                    return type;
            }
        } else if ("Самка".equals(gender)) {
            switch (type) {
                case "Кот":
                    return "Кошка";
                case "Пёс":
                    return "Собака";
                default:
                    return type;
            }
        }
        return type;
    }

    private Bitmap loadBitmapFromInternalStorage(String filename) {
        try {
            java.io.FileInputStream fis = openFileInput(filename);
            Bitmap bitmap = BitmapFactory.decodeStream(fis);
            fis.close();
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void editPet() {
        Intent intent = new Intent(this, AddPetActivity.class);
        intent.putExtra("petId", petId);
        intent.putExtra("edit", true);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Удалить питомца?")
                .setMessage("Вы уверены, что хотите удалить " + pet.getName() + "?")
                .setPositiveButton("Удалить", (dialog, which) -> deletePet())
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void deletePet() {
        databaseHelper.deletePet(petId);
        Toast.makeText(this, "Питомец удален", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, MainActivity.class));
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
