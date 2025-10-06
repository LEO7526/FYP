package com.example.yummyrestaurant.models;

import java.util.Objects;

public class Customization {
    private String spiceLevel;
    private String extraNotes;

    public Customization(String spiceLevel, String extraNotes) {
        this.spiceLevel = spiceLevel;
        this.extraNotes = extraNotes;
    }

    public String getSpiceLevel() { return spiceLevel; }
    public String getExtraNotes() { return extraNotes; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Customization)) return false;
        Customization that = (Customization) o;
        return Objects.equals(spiceLevel, that.spiceLevel) &&
                Objects.equals(extraNotes, that.extraNotes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(spiceLevel, extraNotes);
    }

    @Override
    public String toString() {
        return "Customization{spiceLevel=" + spiceLevel + ", extraNotes=" + extraNotes + "}";
    }
}