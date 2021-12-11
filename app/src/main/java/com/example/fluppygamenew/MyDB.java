package com.example.fluppygamenew;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class MyDB {
    private ArrayList<Record> records;

    public MyDB() {
        records = new ArrayList<>();
    }

    public ArrayList<Record> getRecords() {
        return records;
    }

    public MyDB setRecords(ArrayList<Record> records) {
        this.records = records;
        return this;
    }

    public void sortRecords() {
        Collections.sort(records);

    }
}

