package com.example.pupiloplus;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.pupiloplus.R;
import com.example.pupiloplus.data.DatabaseHelper;
import com.example.pupiloplus.data.Pet;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddPetActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private EditText birthdateInput;
    private Uri selectedPhotoUri = null;
    private String selectedPhotoPath = null;
    private Pet existingPet = null;
    private long petId = -1;
    private ImageView petPhotoImageView;
    private EditText customTypeInput;

    private ActivityResultLauncher<Intent> photoPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedPhotoUri = result.getData().getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedPhotoUri);
                        petPhotoImageView.setImageBitmap(bitmap);
                        Toast.makeText(this, "Фото выбрано", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Toast.makeText(this, "Ошибка загрузки фото", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(SettingsActivity.getCurrentTheme(this));
        setContentView(R.layout.activity_add_pet);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        databaseHelper = new DatabaseHelper(this);
        birthdateInput = findViewById(R.id.edit_pet_birthdate);
        petPhotoImageView = findViewById(R.id.image_pet_photo);
        customTypeInput = findViewById(R.id.edit_custom_type);

        // Check if editing existing pet
        petId = getIntent().getLongExtra("petId", -1);
        if (petId != -1 && getIntent().getBooleanExtra("edit", false)) {
            existingPet = databaseHelper.getPetById(petId);
            if (existingPet != null) {
                loadPetData(existingPet);
            }
        }

        // Spinner для типа питомца
        Spinner typeSpinner = findViewById(R.id.spinner_pet_type);
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(this, R.array.pet_types, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(typeAdapter);
        typeSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                String selectedType = parent.getItemAtPosition(position).toString();
                if ("Другое".equals(selectedType)) {
                    customTypeInput.setVisibility(android.view.View.VISIBLE);
                } else {
                    customTypeInput.setVisibility(android.view.View.GONE);
                    customTypeInput.setText("");
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });

        // Spinner для пола
        Spinner genderSpinner = findViewById(R.id.spinner_pet_gender);
        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(this, R.array.gender_options, android.R.layout.simple_spinner_item);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(genderAdapter);

        // Spinner для единиц измерения веса
        Spinner weightUnitSpinner = findViewById(R.id.spinner_weight_unit);
        ArrayAdapter<CharSequence> weightAdapter = ArrayAdapter.createFromResource(this, R.array.weight_units, android.R.layout.simple_spinner_item);
        weightAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        weightUnitSpinner.setAdapter(weightAdapter);

        // DatePicker для даты рождения
        birthdateInput.setOnClickListener(v -> showDatePicker());

        // Photo buttons
        Button changePhotoButton = findViewById(R.id.button_change_photo);
        changePhotoButton.setOnClickListener(v -> pickPhoto());

        Button deletePhotoButton = findViewById(R.id.button_delete_photo);
        deletePhotoButton.setOnClickListener(v -> deletePhoto());

        Button saveButton = findViewById(R.id.button_save_pet);
        saveButton.setOnClickListener(v -> savePet());
    }

    private void loadPetData(Pet pet) {
        if (pet == null) return;
        if (pet == null) return;

        EditText nameInput = findViewById(R.id.edit_pet_name);
        EditText breedInput = findViewById(R.id.edit_pet_breed);
        EditText colorInput = findViewById(R.id.edit_pet_color);
        EditText weightInput = findViewById(R.id.edit_pet_weight);
        EditText chipInput = findViewById(R.id.edit_pet_chip);
        EditText foodInput = findViewById(R.id.edit_pet_food);
        EditText notesInput = findViewById(R.id.edit_pet_notes);
        Spinner typeSpinner = findViewById(R.id.spinner_pet_type);
        Spinner genderSpinner = findViewById(R.id.spinner_pet_gender);
        Spinner weightUnitSpinner = findViewById(R.id.spinner_weight_unit);

        nameInput.setText(pet.getName());
        birthdateInput.setText(pet.getBirthDate());
        breedInput.setText(pet.getBreed());
        colorInput.setText(pet.getColor());
        chipInput.setText(pet.getChip());
        foodInput.setText(pet.getFood());
        notesInput.setText(pet.getNotes());

        // Set spinner selections
        ArrayAdapter<CharSequence> typeAdapter = (ArrayAdapter<CharSequence>) typeSpinner.getAdapter();
        int typePosition = typeAdapter.getPosition(pet.getType());
        typeSpinner.setSelection(typePosition);

        // Set gender spinner
        if (pet.getGender() != null && !pet.getGender().isEmpty()) {
            ArrayAdapter<CharSequence> genderAdapter = (ArrayAdapter<CharSequence>) genderSpinner.getAdapter();
            int genderPosition = genderAdapter.getPosition(pet.getGender());
            genderSpinner.setSelection(genderPosition);
        }

        // Load photo
        if (pet.getPhotoPath() != null && !pet.getPhotoPath().isEmpty()) {
            try {
                Uri photoUri = Uri.parse(pet.getPhotoPath());
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
                petPhotoImageView.setImageBitmap(bitmap);
                selectedPhotoUri = photoUri;
            } catch (IOException e) {
                petPhotoImageView.setImageResource(pet.getPhotoRes());
            }
        } else {
            petPhotoImageView.setImageResource(pet.getPhotoRes());
        }

        // Parse weight and unit
        String[] weightParts = pet.getWeight().split(" ");
        if (weightParts.length >= 1) {
            weightInput.setText(weightParts[0]);
        }
        if (weightParts.length >= 2) {
            ArrayAdapter<CharSequence> weightAdapter = (ArrayAdapter<CharSequence>) weightUnitSpinner.getAdapter();
            int unitPosition = weightAdapter.getPosition(weightParts[1]);
            weightUnitSpinner.setSelection(unitPosition);
        }
    }

    private void pickPhoto() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        photoPickerLauncher.launch(intent);
    }

    private void deletePhoto() {
        selectedPhotoUri = null;
        selectedPhotoPath = null;
        petPhotoImageView.setImageResource(R.drawable.ic_pet);
        Toast.makeText(this, "Фото удалено", Toast.LENGTH_SHORT).show();
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String date = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            birthdateInput.setText(date);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void savePet() {
        EditText nameInput = findViewById(R.id.edit_pet_name);
        EditText breedInput = findViewById(R.id.edit_pet_breed);
        EditText colorInput = findViewById(R.id.edit_pet_color);
        EditText weightInput = findViewById(R.id.edit_pet_weight);
        EditText chipInput = findViewById(R.id.edit_pet_chip);
        EditText foodInput = findViewById(R.id.edit_pet_food);
        EditText notesInput = findViewById(R.id.edit_pet_notes);
        Spinner typeSpinner = findViewById(R.id.spinner_pet_type);
        Spinner genderSpinner = findViewById(R.id.spinner_pet_gender);
        Spinner weightUnitSpinner = findViewById(R.id.spinner_weight_unit);

        Pet pet = new Pet();
        pet.setName(nameInput.getText().toString().trim());

        // Set type with gender inflection
        String selectedType = typeSpinner.getSelectedItem().toString();
        String gender = genderSpinner.getSelectedItem().toString();
        pet.setGender(gender);

        if ("Другое".equals(selectedType)) {
            String customType = customTypeInput.getText().toString().trim();
            if (customType.isEmpty()) {
                Toast.makeText(this, "Введите вид животного", Toast.LENGTH_SHORT).show();
                return;
            }
            pet.setType(customType);
        } else {
            pet.setType(selectedType);
        }

        pet.setBirthDate(birthdateInput.getText().toString().trim());
        pet.setBreed(breedInput.getText().toString().trim());
        pet.setColor(colorInput.getText().toString().trim());
        String weight = weightInput.getText().toString().trim() + " " + weightUnitSpinner.getSelectedItem().toString();
        pet.setWeight(weight);
        pet.setChip(chipInput.getText().toString().trim());
        pet.setFood(foodInput.getText().toString().trim());
        pet.setNotes(notesInput.getText().toString().trim());
        pet.setPhotoRes(R.drawable.ic_pet);

        // Set photo path
        if (selectedPhotoPath != null) {
            pet.setPhotoPath(selectedPhotoPath);
        } else if (existingPet != null && existingPet.getPhotoPath() != null) {
            pet.setPhotoPath(existingPet.getPhotoPath());
        }

        if (pet.getName().isEmpty() || birthdateInput.getText().toString().isEmpty()) {
            Toast.makeText(this, "Введите имя и дату рождения", Toast.LENGTH_SHORT).show();
            return;
        }

        if (existingPet != null && petId != -1) {
            pet.setId(petId);
            databaseHelper.updatePet(pet);
            Toast.makeText(this, "Питомец обновлен", Toast.LENGTH_SHORT).show();
        } else {
            databaseHelper.insertPet(pet);
            Toast.makeText(this, "Питомец сохранён", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private String applyGenderInflection(String type, String gender) {
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

    private String saveBitmapToInternalStorage(Bitmap bitmap) {
        try {
            String filename = "pet_photo_" + System.currentTimeMillis() + ".jpg";
            java.io.FileOutputStream fos = openFileOutput(filename, MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();
            return filename;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
