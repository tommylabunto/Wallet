package com.xingtingkai.wallet.helper;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public final class DateFormatter {

    // u represents year, y represents year-of-era
    private static final String dateStringPattern = "dd MMM uuuu";

    public static String formatToDateString(Instant instant, ZoneId zoneId) {

        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, zoneId);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(dateStringPattern);
        return dateTimeFormatter.format(zonedDateTime);
    }
}
