package com.example.pupiloplus.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AgeCalculator {

    public static String calculateAge(String birthDate) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date date = format.parse(birthDate);
            
            Calendar birth = Calendar.getInstance();
            birth.setTime(date);
            
            Calendar today = Calendar.getInstance();
            
            int years = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR);
            int months = today.get(Calendar.MONTH) - birth.get(Calendar.MONTH);
            
            if (months < 0) {
                years--;
                months += 12;
            }
            
            if (today.get(Calendar.DAY_OF_MONTH) < birth.get(Calendar.DAY_OF_MONTH)) {
                months--;
                if (months < 0) {
                    years--;
                    months += 12;
                }
            }
            
            StringBuilder result = new StringBuilder();
            if (years > 0) {
                result.append(years).append(" ").append(declineYears(years));
            }
            if (months > 0) {
                if (years > 0) result.append(" ");
                result.append(months).append(" ").append(declineMonths(months));
            }
            
            if (years == 0 && months == 0) {
                return "Меньше месяца";
            }
            
            return result.toString();
        } catch (ParseException e) {
            return "Неизвестно";
        }
    }
    
    private static String declineYears(int count) {
        if (count % 10 == 1 && count % 100 != 11) {
            return "год";
        } else if (count % 10 >= 2 && count % 10 <= 4 && (count % 100 < 10 || count % 100 >= 20)) {
            return "года";
        } else {
            return "лет";
        }
    }
    
    private static String declineMonths(int count) {
        if (count % 10 == 1 && count % 100 != 11) {
            return "месяц";
        } else if (count % 10 >= 2 && count % 10 <= 4 && (count % 100 < 10 || count % 100 >= 20)) {
            return "месяца";
        } else {
            return "месяцев";
        }
    }
}
