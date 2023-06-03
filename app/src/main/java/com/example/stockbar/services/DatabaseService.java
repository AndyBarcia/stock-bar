package com.example.stockbar.services;

import com.google.firebase.database.FirebaseDatabase;

public class DatabaseService {

    private static FirebaseDatabase database;

    public static FirebaseDatabase getDatabase() {
        if (database == null) {
            database = FirebaseDatabase.getInstance();
            database.setPersistenceEnabled(true);
        }
        return database;
    }

}
