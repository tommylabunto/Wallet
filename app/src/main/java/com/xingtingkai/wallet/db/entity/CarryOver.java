package com.xingtingkai.wallet.db.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

// obsolete, not longer in implementation
@Entity
public class CarryOver {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    private long carryOverId;

    // default is false
    @NonNull
    private boolean isCarryOver;

    public CarryOver() {
        this.isCarryOver = false;
    }

    public boolean isCarryOver() {
        return isCarryOver;
    }

    public void setCarryOver(boolean carryOver) {
        isCarryOver = carryOver;
    }

    public long getCarryOverId() {
        return carryOverId;
    }

    public void setCarryOverId(long carryOverId) {
        this.carryOverId = carryOverId;
    }

    @NonNull
    @Override
    public String toString() {
        return "CarryOver Id: " + getCarryOverId()
                + ", " + "IsCarryOver: " + isCarryOver();
    }
}
