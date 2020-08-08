package com.example.chat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth mFirebaseAuth;
    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference mDatabaseReference;
    public void logout(View view){
        mFirebaseAuth.signOut();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
    public void add (View view){

    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();
        sendToSignin();
        //testfriends();
        // Chats child is for main activity showing the last message and time.
        // will store objects of ChatInstance class.
        mDatabaseReference.child("Chats").child(mFirebaseAuth.getCurrentUser().getUid()).setValue("");

        // Messages child of for chat activity showing all the messages.
        // will store objects of MessageInstance class.
        mDatabaseReference.child("Messages").child(mFirebaseAuth.getCurrentUser().getUid() + "DiWxZsUAqjMrCRU4QJp8lwLzjXJ2").setValue("");
    }



    private void sendToSignin() {
        if (mFirebaseAuth.getCurrentUser() == null){
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    private void testfriends(){
        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        String id = user.getUid();
        mFirebaseDatabase = FirebaseDatabase.getInstance();

        Map<String,Boolean> map = new HashMap<>();

        map.put("ahmed",true);
        map.put("khaled",false);
        map.put("yasser",true);
        mDatabaseReference.child("Users").child(id).child("friends");
        mDatabaseReference.child("Users").child(id).child("friends").setValue(map);

        FriendsHandler friendsHandler = new FriendsHandler();
        friendsHandler.addFriends(id,"your new friend");
    }
}