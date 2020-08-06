package com.example.chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFirebaseAuth = FirebaseAuth.getInstance();
        sendToSignin();
       testfriends();
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
        mDatabaseReference = mFirebaseDatabase.getReference();
        Map<String,String> map = new HashMap<>();
        map.put("ahmed","yes");
        map.put("khaled","No");
        map.put("yasser","pending");
        mDatabaseReference.child("Users").child(id).child("friends");
        mDatabaseReference.child("Users").child(id).child("friends").setValue(map);
        AddingFriends addingFriends = new AddingFriends();
        addingFriends.addFriends(id,"your new friend");
    }
}