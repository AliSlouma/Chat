package com.example.chat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseUtil {
    public static  FirebaseAuth sFirebaseAuth;
    public static FirebaseDatabase sFirebaseDatabase;
    public static DatabaseReference sDatabaseReference;
    static {
        sFirebaseDatabase = FirebaseDatabase.getInstance();
        sDatabaseReference = sFirebaseDatabase.getReference();
        sFirebaseAuth = FirebaseAuth.getInstance();
    }
}
