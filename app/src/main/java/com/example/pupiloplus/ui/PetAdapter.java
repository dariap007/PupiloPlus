package com.example.pupiloplus.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pupiloplus.R;
import com.example.pupiloplus.data.Pet;
import com.example.pupiloplus.utils.AgeCalculator;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class PetAdapter extends RecyclerView.Adapter<PetAdapter.PetViewHolder> {
    private final Context context;
    private final List<Pet> items = new ArrayList<>();
    private final PetClickListener listener;

    public interface PetClickListener {
        void onPetClick(Pet pet);
    }

    public PetAdapter(Context context, List<Pet> pets, PetClickListener listener) {
        this.context = context;
        this.items.addAll(pets);
        this.listener = listener;
    }

    public void updateItems(List<Pet> pets) {
        items.clear();
        items.addAll(pets);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pet_card, parent, false);
        return new PetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PetViewHolder holder, int position) {
        Pet pet = items.get(position);
        holder.name.setText(pet.getName());
        String age = AgeCalculator.calculateAge(pet.getBirthDate());
        String displayType = applyGenderInflection(pet.getType(), pet.getGender());
        holder.description.setText(displayType + ", " + age);

        // Load pet photo
        if (pet.getPhotoPath() != null && !pet.getPhotoPath().isEmpty()) {
            try {
                Bitmap bitmap = loadBitmapFromInternalStorage(pet.getPhotoPath());
                if (bitmap != null) {
                    holder.image.setImageBitmap(bitmap);
                } else {
                    holder.image.setImageResource(pet.getPhotoRes());
                }
            } catch (Exception e) {
                holder.image.setImageResource(pet.getPhotoRes());
            }
        } else {
            holder.image.setImageResource(pet.getPhotoRes());
        }

        holder.card.setOnClickListener(v -> listener.onPetClick(pet));
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
            FileInputStream fis = context.openFileInput(filename);
            Bitmap bitmap = BitmapFactory.decodeStream(fis);
            fis.close();
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class PetViewHolder extends RecyclerView.ViewHolder {
        CardView card;
        TextView name;
        TextView description;
        ImageView image;

        public PetViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.card_pet);
            name = itemView.findViewById(R.id.text_pet_name);
            description = itemView.findViewById(R.id.text_pet_description);
            image = itemView.findViewById(R.id.image_pet);
        }
    }
}
