package com.xingtingkai.wallet.helper;

import java.time.Month;
import java.util.Calendar;
import java.util.Date;

public final class DateFormatter {

    /*
    java Calendar and Date store month starting from 0 (January)
    - so when format from string to date, minus 1 (so it fits java convention)
    - so when format from date to string, add 1 (so it looks normal for the user
      that 1 represents january)
     */
    public static Date formatStringToDate(String dateString) {

        Calendar calendar = Calendar.getInstance();

        if (!dateString.isEmpty()) {
            // dateString is in the format of "02/12/2020"
            String dayString = dateString.substring(0,2);
            String monthString = dateString.substring(3,5);
            String yearString = dateString.substring(6,10);

            int day = Integer.parseInt(dayString);
            int month = Integer.parseInt(monthString) - 1;
            int year = Integer.parseInt(yearString);

            calendar.set(Calendar.DAY_OF_MONTH, day);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.YEAR, year);
        }

        return calendar.getTime();
    }

    public static String formatDateToString(Date date) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        // return in the format of "02/12/2020"
        String format = "%02d";

        String dayString = String.format(format, calendar.get(Calendar.DAY_OF_MONTH));
        String monthString = String.format(format, calendar.get(Calendar.MONTH) + 1);
        String yearString = String.valueOf(calendar.get(Calendar.YEAR));

        return appendDateString(yearString, monthString, dayString, "/");
    }

    public static String beautifyDateString(String dateString) {

        String dayString = dateString.substring(0,2);
        String tempMonthString = dateString.substring(3,5);
        String yearString = dateString.substring(6,10);

        int month = Integer.parseInt(tempMonthString);

        // month uses 1 (jan) to 12 (dec)
        Month tempMonth = Month.of(month);
        // originally is all caps
        String monthString = tempMonth.name().substring(0,1) + tempMonth.name().substring(1,3).toLowerCase();

        return appendDateString(yearString, monthString, dayString, " ");
    }

    public static String appendDateString(String year, String month, String day, String delimiter) {

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(day);
        stringBuilder.append(delimiter);
        stringBuilder.append(month);
        stringBuilder.append(delimiter);
        stringBuilder.append(year);

        return stringBuilder.toString();
    }
}
