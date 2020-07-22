package com.xingtingkai.wallet.db.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.auto.value.AutoValue;

// obsolete, not longer in implementation
@AutoValue
@Entity
public abstract class CarryOver {

    @AutoValue.CopyAnnotations
    @PrimaryKey(autoGenerate = true)
    public abstract long getCarryOverId();

    // default is false
    @AutoValue.CopyAnnotations
    public abstract boolean isCarryOver();

    // Room uses this factory method to create CarryOver objects.
    public static CarryOver create(long carryOverId, boolean carryOver) {
        return new AutoValue_CarryOver(carryOverId, carryOver);
    }
}
