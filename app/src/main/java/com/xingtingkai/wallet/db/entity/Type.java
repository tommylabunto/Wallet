package com.xingtingkai.wallet.db.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.auto.value.AutoValue;

@AutoValue
@Entity
public abstract class Type {

    @AutoValue.CopyAnnotations
    @PrimaryKey(autoGenerate = true)
    public abstract long getTypeId();

    @AutoValue.CopyAnnotations
    @NonNull
    public abstract String getName();

    @AutoValue.CopyAnnotations
    public abstract boolean isExpenseType();

    // Room uses this factory method to create Type objects.
    public static Type create(long typeId, String name, boolean expenseType) {
        return new AutoValue_Type(typeId, name, expenseType);
    }

    /*
    cannot use the builder method because it sets AutoValue_Type constructor to private
    Room requires the constructor to be package private or public
     */
//    public static Builder builder() {
//        return new AutoValue_Type.Builder();
//    }
//
//    @AutoValue.CopyAnnotations
//    @AutoValue.Builder
//    public abstract static class Builder {
//        public abstract Builder setTypeId(long value);
//        public abstract Builder setName(String value);
//        public abstract Builder setExpenseType(boolean value);
//        public abstract Type build();
//    }

//    @PrimaryKey(autoGenerate = true)
//    @NonNull
//    private long typeId;
//
//    @NonNull
//    @Size(min = 1, max = 100)
//    private String name;
//
//    @NonNull
//    private boolean isExpenseType;
//
//    public Type() {
//    }
//
//    public Type(String name, boolean isExpenseType) {
//        this();
//        this.name = name;
//        this.isExpenseType = isExpenseType;
//    }
//
//    public long getTypeId() {
//        return typeId;
//    }
//
//    public void setTypeId(long typeId) {
//        this.typeId = typeId;
//    }
//
//    @NonNull
//    public String getName() {
//        return name;
//    }
//
//    public void setName(@NonNull String name) {
//        this.name = name;
//    }
//
//    public boolean isExpenseType() {
//        return isExpenseType;
//    }
//
//    public void setExpenseType(boolean expenseType) {
//        isExpenseType = expenseType;
//    }
//
//    @NonNull
//    @Override
//    public String toString() {
//        return "Type Id: " + getTypeId()
//                + ", " + "Type Name: " + getName();
//    }
}
