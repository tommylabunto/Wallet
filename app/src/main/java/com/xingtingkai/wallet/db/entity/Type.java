package com.xingtingkai.wallet.db.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Size;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Type {
    @PrimaryKey(autoGenerate = true)
    @NonNull
    private long typeId;

    @NonNull
    @Size(min = 1, max = 100)
    private String name;

    @NonNull
    private boolean isExpenseType;

    public Type() {
    }

    public Type(String name, boolean isExpenseType) {
        this();
        this.name = name;
        this.isExpenseType = isExpenseType;
    }

    public long getTypeId() {
        return typeId;
    }

    public void setTypeId(long typeId) {
        this.typeId = typeId;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    public boolean isExpenseType() {
        return isExpenseType;
    }

    public void setExpenseType(boolean expenseType) {
        isExpenseType = expenseType;
    }
}
