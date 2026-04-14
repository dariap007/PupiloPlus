package com.example.pupiloplus.ui;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.pupiloplus.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ColorSpinnerAdapter extends ArrayAdapter<String> {

    private Map<String, Integer> typeColors;

    public ColorSpinnerAdapter(@NonNull Context context, int resource, @NonNull List<String> objects) {
        super(context, resource, objects);

        typeColors = new HashMap<>();
        typeColors.put("Кормление", Color.parseColor("#6b4c24"));
        typeColors.put("Визит к врачу", Color.parseColor("#6e8b4f"));
        typeColors.put("Смена лотка", Color.parseColor("#ebc017"));
        typeColors.put("Лекарства", Color.parseColor("#A92F50"));
        typeColors.put("Смена воды", Color.parseColor("#73a1e7"));
        typeColors.put("Другое", Color.parseColor("#4F204D"));
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    private View getCustomView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.spinner_item_with_color, parent, false);
        }

        String item = getItem(position);
        TextView textView = convertView.findViewById(R.id.text);
        View colorDot = convertView.findViewById(R.id.color_dot);

        textView.setText(item);
        Integer color = typeColors.get(item);
        if (color != null) {
            colorDot.setBackgroundColor(color);
        }

        return convertView;
    }
}