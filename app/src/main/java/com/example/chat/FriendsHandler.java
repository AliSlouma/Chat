package com.example.chat;



import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
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
        mDatabaseRefrence.child("FriendRequests").child(senderID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<UserInstance> requests = (List<UserInstance>) dataSnapshot.getValue();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
       });
    }

}
