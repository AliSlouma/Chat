package com.example.chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth mFirebaseAuth;
    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference mDatabaseReference;
    ListView listView;
    ArrayAdapter arrayAdapter;
    ArrayList<String> messages;

    public void logout(){
        mFirebaseAuth.signOut();

        startActivity(new Intent(this, LoginActivity.class));
        finish();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu , menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()){
            case R.id.logout:
                logout();
                return true;
            default:
                return false;
        }
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

        listView = findViewById(R.id.chat_list);
        messages = new ArrayList<>();
        arrayAdapterFunc();
    }


    private void arrayAdapterFunc() {
        messages.add("Test Chat");
        arrayAdapter = new ArrayAdapter<>(getApplicationContext(),android.R.layout.simple_list_item_1 , messages);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int postion, long id) {
                Intent intent = new Intent(getApplicationContext() , ChatActivity.class);
                startActivity(intent);
            }
        });
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