package com.softaan.sweetsleep;

public class StoredFile {
    private String name;

    public StoredFile(String name) {
        this.name = name.replace(".csv", "");
    }

    public String getName() {
        return name;
    }
}
