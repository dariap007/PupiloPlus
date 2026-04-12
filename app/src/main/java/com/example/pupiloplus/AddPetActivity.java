package com.example.pupiloplus;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
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
    private Button birthdateButton;
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
                        selectedPhotoPath = saveBitmapToInternalStorage(bitmap);
                        updatePhotoButtons();
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
        birthdateButton = findViewById(R.id.button_birthdate);
        petPhotoImageView = findViewById(R.id.image_pet_photo);
        customTypeInput = findViewById(R.id.edit_custom_type);

        // Spinner для типа питомца
        Spinner typeSpinner = findViewById(R.id.spinner_pet_type);
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(this, R.array.pet_types, R.layout.spinner_item);
        typeAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
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
        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(this, R.array.gender_options, R.layout.spinner_item);
        genderAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        genderSpinner.setAdapter(genderAdapter);

        // Spinner для единиц измерения веса
        Spinner weightUnitSpinner = findViewById(R.id.spinner_weight_unit);
        ArrayAdapter<CharSequence> weightAdapter = ArrayAdapter.createFromResource(this, R.array.weight_units, R.layout.spinner_item);
        weightAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        weightUnitSpinner.setAdapter(weightAdapter);

        // Check if editing existing pet
        petId = getIntent().getLongExtra("petId", -1);
        if (petId != -1 && getIntent().getBooleanExtra("edit", false)) {
            existingPet = databaseHelper.getPetById(petId);
            if (existingPet != null) {
                loadPetData(existingPet);
                toolbar.setTitle("Редактирование питомца");
            }
        }

        // DatePicker для даты рождения
        birthdateButton.setOnClickListener(v -> showDatePicker());

        // Photo buttons
        Button addPhotoButton = findViewById(R.id.button_add_photo);
        addPhotoButton.setOnClickListener(v -> pickPhoto());

        Button changePhotoButton = findViewById(R.id.button_change_photo);
        changePhotoButton.setOnClickListener(v -> pickPhoto());

        Button deletePhotoButton = findViewById(R.id.button_delete_photo);
        deletePhotoButton.setOnClickListener(v -> deletePhoto());

        Button saveButton = findViewById(R.id.button_save_pet);
        saveButton.setOnClickListener(v -> savePet());

        updatePhotoButtons();
    }

    private void updatePhotoButtons() {
        Button addPhotoButton = findViewById(R.id.button_add_photo);
        Button changePhotoButton = findViewById(R.id.button_change_photo);
        Button deletePhotoButton = findViewById(R.id.button_delete_photo);

        if (selectedPhotoPath == null || selectedPhotoPath.isEmpty()) {
            addPhotoButton.setVisibility(View.VISIBLE);
            changePhotoButton.setVisibility(View.GONE);
            deletePhotoButton.setVisibility(View.GONE);
        } else {
            addPhotoButton.setVisibility(View.GONE);
            changePhotoButton.setVisibility(View.VISIBLE);
            deletePhotoButton.setVisibility(View.VISIBLE);
        }
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
        if (!pet.getBirthDate().isEmpty()) {
            birthdateButton.setText(pet.getBirthDate());
        }
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
            Bitmap bitmap = loadBitmapFromInternalStorage(pet.getPhotoPath());
            if (bitmap != null) {
                petPhotoImageView.setImageBitmap(bitmap);
                selectedPhotoPath = pet.getPhotoPath();
            } else {
                petPhotoImageView.setImageResource(R.drawable.pet_care_18769509);
            }
        } else {
            petPhotoImageView.setImageResource(R.drawable.pet_care_18769509);
        }
        updatePhotoButtons();

        // Set weight
        weightInput.setText(pet.getWeight());

        // Set weight unit spinner
        if (pet.getWeightUnit() != null && !pet.getWeightUnit().isEmpty()) {
            ArrayAdapter<CharSequence> weightUnitAdapter = (ArrayAdapter<CharSequence>) weightUnitSpinner.getAdapter();
            int unitPosition = weightUnitAdapter.getPosition(pet.getWeightUnit());
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
        petPhotoImageView.setImageResource(R.drawable.pet_care_18769509);
        updatePhotoButtons();
        Toast.makeText(this, "Фото удалено", Toast.LENGTH_SHORT).show();
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        Calendar today = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            Calendar selected = Calendar.getInstance();
            selected.set(year, month, dayOfMonth);
            if (selected.after(today)) {
                Toast.makeText(this, "Дата рождения не может быть в будущем", Toast.LENGTH_SHORT).show();
                return;
            }
            String date = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            birthdateButton.setText(date);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setMaxDate(today.getTimeInMillis());
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

        pet.setBirthDate(birthdateButton.getText().toString().trim());
        pet.setBreed(breedInput.getText().toString().trim());
        pet.setColor(colorInput.getText().toString().trim());
        pet.setWeight(weightInput.getText().toString().trim());
        pet.setWeightUnit(weightUnitSpinner.getSelectedItem().toString());
        pet.setChip(chipInput.getText().toString().trim());
        pet.setFood(foodInput.getText().toString().trim());
        pet.setNotes(notesInput.getText().toString().trim());
        pet.setPhotoRes(R.drawable.pet_care_18769509);

        // Set photo path
        if (selectedPhotoPath != null) {
            pet.setPhotoPath(selectedPhotoPath);
        } else if (existingPet != null && existingPet.getPhotoPath() != null) {
            pet.setPhotoPath(existingPet.getPhotoPath());
        }

        if (pet.getName().isEmpty() || birthdateButton.getText().toString().isEmpty()) {
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
