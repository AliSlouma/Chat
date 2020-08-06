package com.example.chat;



import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

public class AddingFriends {

   private static  FirebaseDatabase mFirebaseDatabase;
   private static  DatabaseReference mDatabaseRefrence;
   static {
       mFirebaseDatabase = FirebaseDatabase.getInstance();
       mDatabaseRefrence = mFirebaseDatabase.getReference();
   }
    public void addFriends(final String senderID , final String receiverID ){
        mDatabaseRefrence.child("Users").child(senderID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                DataSnapshot friendsShot = dataSnapshot.child("friends");
               Map<String,String> map = (Map<String, String>) friendsShot.getValue();
               for(Map.Entry<String,? > entry : map.entrySet()){
                   if(receiverID.equals(entry.getKey()))
                      return;
               }

                map.put(receiverID,"yes");
                mDatabaseRefrence.child("Users").child(senderID).child("friends").setValue(map);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
