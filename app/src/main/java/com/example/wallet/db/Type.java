package com.example.wallet.db;

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

    public Type() {
    }

    public Type(String name) {
        this();
        this.name = name;
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
}
