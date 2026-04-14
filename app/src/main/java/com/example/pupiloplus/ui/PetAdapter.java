package com.example.pupiloplus.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
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
        int photoRes = resolvePhotoRes(pet.getPhotoRes());
        if (pet.getPhotoPath() != null && !pet.getPhotoPath().isEmpty()) {
            try {
                Bitmap bitmap = loadBitmapFromInternalStorage(pet.getPhotoPath());
                if (bitmap != null) {
                    holder.image.setImageBitmap(bitmap);
                } else {
                    setImageResourceSafe(holder.image, photoRes);
                }
            } catch (Exception e) {
                setImageResourceSafe(holder.image, photoRes);
            }
        } else {
            setImageResourceSafe(holder.image, photoRes);
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

    private Bitmap loadBitmapFromInternalStorage(String path) {
        try {
            if (path.startsWith("content://")) {
                // For URI paths, try MediaStore (though may fail after restart)
                return MediaStore.Images.Media.getBitmap(context.getContentResolver(), android.net.Uri.parse(path));
            } else {
                // For internal file paths
                FileInputStream fis = context.openFileInput(path);
                Bitmap bitmap = BitmapFactory.decodeStream(fis);
                fis.close();
                return bitmap;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private int resolvePhotoRes(int photoRes) {
        if (photoRes == 0) {
            return R.drawable.ic_pet;
        }
        try {
            context.getResources().getResourceName(photoRes);
            return photoRes;
        } catch (Resources.NotFoundException e) {
            return R.drawable.ic_pet;
        }
    }

    private void setImageResourceSafe(ImageView imageView, int photoRes) {
        try {
            imageView.setImageResource(photoRes);
        } catch (Resources.NotFoundException e) {
            imageView.setImageResource(R.drawable.ic_pet);
        }
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
