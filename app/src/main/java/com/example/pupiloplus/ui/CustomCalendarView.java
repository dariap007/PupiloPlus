package com.example.pupiloplus.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.pupiloplus.R;
import com.example.pupiloplus.data.DatabaseHelper;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomCalendarView extends View {

    private Paint paintDay;
    private Paint paintSelected;
    private Paint paintHasReminder;
    private Paint paintText;
    private Paint paintReminderDot;
    private float cellWidth;
    private float cellHeight;
    private int selectedDay = -1;
    private Calendar calendar;
    private DatabaseHelper databaseHelper;
    private OnDateSelectedListener onDateSelectedListener;
    private Map<String, Integer> typeColors;

    private static final int DAYS_IN_WEEK = 7;
    private static final String[] monthNames = {"Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
            "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"};

    public interface OnDateSelectedListener {
        void onDateSelected(int year, int month, int day);
    }

    public CustomCalendarView(Context context) {
        super(context);
        init(context);
    }

    public CustomCalendarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CustomCalendarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        databaseHelper = new DatabaseHelper(context);
        calendar = Calendar.getInstance();

        typeColors = new HashMap<>();
        typeColors.put("Кормление", Color.parseColor("#B48952"));
        typeColors.put("Визит к врачу", Color.parseColor("#C5D7F2"));
        typeColors.put("Смена лотка", Color.parseColor("#EDDA8C"));
        typeColors.put("Лекарства", Color.parseColor("#A92F50"));
        typeColors.put("Смена воды", Color.parseColor("#4F204D"));
        typeColors.put("Другое", Color.parseColor("#AFC29C"));

        paintDay = new Paint();
        paintDay.setColor(Color.WHITE);
        paintDay.setStyle(Paint.Style.FILL);

        paintSelected = new Paint();
        paintSelected.setColor(context.getResources().getColor(R.color.primaryGreen));
        paintSelected.setStyle(Paint.Style.FILL);

        paintHasReminder = new Paint();
        paintHasReminder.setColor(context.getResources().getColor(R.color.primaryGreen));
        paintHasReminder.setStyle(Paint.Style.STROKE);
        paintHasReminder.setStrokeWidth(2);

        paintText = new Paint();
        paintText.setColor(context.getResources().getColor(R.color.textColor));
        paintText.setTextSize(26);
        paintText.setTextAlign(Paint.Align.CENTER);

        paintReminderDot = new Paint();
        paintReminderDot.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        cellWidth = getWidth() / (float) DAYS_IN_WEEK;
        cellHeight = getHeight() / 7.5f; // 6 weeks + header

        // Draw month/year header
        String header = monthNames[calendar.get(Calendar.MONTH)] + " " + calendar.get(Calendar.YEAR);
        Paint headerPaint = new Paint(paintText);
        headerPaint.setTextSize(40);
        headerPaint.setFakeBoldText(true);
        canvas.drawText(header, getWidth() / 2f, cellHeight * 0.7f, headerPaint);

        // Draw day names (Mon, Tue, etc.)
        String[] dayNames = {"Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"};
        Paint dayNamesPaint = new Paint(paintText);
        dayNamesPaint.setFakeBoldText(true);
        for (int i = 0; i < 7; i++) {
            float x = i * cellWidth + cellWidth / 2;
            float y = cellHeight * 1.5f;
            canvas.drawText(dayNames[i], x, y, dayNamesPaint);
        }

        // Calculate first day of month and total days
        Calendar firstDay = Calendar.getInstance();
        firstDay.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), 1);
        int startDayOfWeek = (firstDay.get(Calendar.DAY_OF_WEEK) + 5) % 7; // Adjust for Monday start
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Draw date cells
        int day = 1;
        for (int week = 0; week < 6; week++) {
            for (int dayOfWeek = 0; dayOfWeek < 7; dayOfWeek++) {
                if (week == 0 && dayOfWeek < startDayOfWeek) {
                    continue;
                }
                if (day > daysInMonth) {
                    break;
                }

                float x = dayOfWeek * cellWidth;
                float y = (week + 2) * cellHeight;

                // Check if this day is selected
                boolean isSelected = day == selectedDay;
                if (isSelected) {
                    float centerX = x + cellWidth / 2;
                    float centerY = y + cellHeight / 2;
                    float radius = Math.min(cellWidth, cellHeight) / 2 - 5;
                    canvas.drawCircle(centerX, centerY, radius, paintSelected);
                }

                // Check if this day has reminders
                List<String> reminderTypes = databaseHelper.getReminderTypesOnDate(
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH) + 1,
                        day
                );

                // Draw border if has reminders
                if (!reminderTypes.isEmpty()) {
                    canvas.drawRect(x + 5, y + 5, x + cellWidth - 5, y + cellHeight - 5, paintHasReminder);
                }

                // Draw day number
                Paint dayTextPaint = new Paint(paintText);
                if (isSelected) {
                    dayTextPaint.setColor(Color.WHITE);
                }
                canvas.drawText(String.valueOf(day), x + cellWidth / 2, y + cellHeight * 0.6f, dayTextPaint);

                // Draw reminder dots if has reminders
                if (!reminderTypes.isEmpty()) {
                    float dotRadius = 6;
                    float dotSpacing = 2;
                    float startX = x + cellWidth - dotRadius - 5;
                    float dotY = y + dotRadius + 5;
                    int maxDots = 6; // Limit to 6 dots
                    for (int i = 0; i < Math.min(reminderTypes.size(), maxDots); i++) {
                        String type = reminderTypes.get(i);
                        Integer color = typeColors.get(type);
                        if (color == null) color = Color.GRAY;
                        paintReminderDot.setColor(color);
                        float dotX = startX - i * (dotRadius * 2 + dotSpacing);
                        canvas.drawCircle(dotX, dotY, dotRadius, paintReminderDot);
                    }
                }

                day++;
            }
            if (day > daysInMonth) {
                break;
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            float y = event.getY();

            // Find which cell was touched
            cellWidth = getWidth() / (float) DAYS_IN_WEEK;
            cellHeight = getHeight() / 7.5f;

            int dayOfWeek = (int) (x / cellWidth);
            int week = (int) ((y - cellHeight * 2) / cellHeight);

            if (week >= 0) {
                // Calculate first day of month
                Calendar firstDay = Calendar.getInstance();
                firstDay.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), 1);
                int startDayOfWeek = (firstDay.get(Calendar.DAY_OF_WEEK) + 5) % 7;

                int day = week * DAYS_IN_WEEK + dayOfWeek - startDayOfWeek + 1;
                int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

                if (day > 0 && day <= daysInMonth) {
                    selectedDay = day;
                    if (onDateSelectedListener != null) {
                        onDateSelectedListener.onDateSelected(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, day);
                    }
                    invalidate();
                }
            }
            return true;
        }
        return super.onTouchEvent(event);
    }

    public void setOnDateSelectedListener(OnDateSelectedListener listener) {
        this.onDateSelectedListener = listener;
    }

    public void nextMonth() {
        calendar.add(Calendar.MONTH, 1);
        selectedDay = -1;
        invalidate();
    }

    public void previousMonth() {
        calendar.add(Calendar.MONTH, -1);
        selectedDay = -1;
        invalidate();
    }

    public void goToToday() {
        calendar = Calendar.getInstance();
        selectedDay = -1;
        invalidate();
    }
}
