package com.xingtingkai.wallet.db;

import androidx.room.TypeConverter;

import java.time.Instant;
import java.time.ZoneId;

public class Converters {

    @TypeConverter
    public static Instant fromEpoch(Long value) {
        return value == null ? null : Instant.ofEpochSecond(value);
    }

    @TypeConverter
    public static Long instantToEpoch(Instant instant) {
        return instant == null ? null : instant.getEpochSecond();
    }

    @TypeConverter
    public static ZoneId fromEpoch(String value) {
        return value == null ? null : ZoneId.of(value);
    }

    @TypeConverter
    public static String zoneIdToString(ZoneId zoneId) {
        return zoneId == null ? null : zoneId.getId();
    }
}
