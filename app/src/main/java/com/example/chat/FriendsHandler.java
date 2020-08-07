package com.example.chat;



import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

public class FriendsHandler {

   private static Context mContext;
   private static  FirebaseDatabase mFirebaseDatabase;
   private static  DatabaseReference mDatabaseRefrence;
   static {
       mFirebaseDatabase = FirebaseDatabase.getInstance();
       mDatabaseRefrence = mFirebaseDatabase.getReference();
   }

    public FriendsHandler(Context context) {
       mContext = context;
    }

    public void addFriendsHandler(String senderID){
        mDatabaseRefrence.child("Users").child(senderID).child("friends").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               Map<String,String> map = (Map<String, String>) dataSnapshot.getValue();

               for(Map.Entry<String,String > entry : map.entrySet()) {
                   if ("pending".equals(entry.getValue()))
                       Toast.makeText(mContext, entry.getKey() + "send you a friend request", Toast.LENGTH_SHORT).show();
               }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
