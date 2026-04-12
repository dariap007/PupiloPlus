package com.example.pupiloplus.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pupiloplus.R;
import com.example.pupiloplus.data.Reminder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder> {
    private final Context context;
    private final List<Reminder> items = new ArrayList<>();
    private final ReminderClickListener listener;

    public interface ReminderClickListener {
        void onReminderClick(Reminder reminder);
    }

    public ReminderAdapter(Context context, List<Reminder> reminders, ReminderClickListener listener) {
        this.context = context;
        this.listener = listener;
        items.addAll(reminders);
    }

    public ReminderAdapter(Context context, List<Reminder> reminders) {
        this.context = context;
        this.listener = null;
        items.addAll(reminders);
    }

    public void updateItems(List<Reminder> reminders) {
        items.clear();
        items.addAll(reminders);
        notifyDataSetChanged();
    }

    public void sortByTime() {
        Collections.sort(items, (a, b) -> a.getDateTime().compareTo(b.getDateTime()));
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_reminder_card, parent, false);
        return new ReminderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
        Reminder reminder = items.get(position);
        holder.title.setText(reminder.getTitle());
        holder.details.setText(reminder.getType() + " • " + reminder.getDateTime());
        holder.period.setText(reminder.getPeriod());
        holder.dose.setText(reminder.getNotes() != null && !reminder.getNotes().isEmpty() ? reminder.getNotes() : reminder.getDose());
        holder.icon.setImageResource(reminder.getImageRes());
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onReminderClick(reminder);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ReminderViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView title;
        TextView details;
        TextView period;
        TextView dose;

        public ReminderViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.image_reminder);
            title = itemView.findViewById(R.id.text_reminder_title);
            details = itemView.findViewById(R.id.text_reminder_details);
            period = itemView.findViewById(R.id.text_reminder_period);
            dose = itemView.findViewById(R.id.text_reminder_dose);
        }
    }
}
