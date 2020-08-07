package com.example.chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth mFirebaseAuth;
    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference mDatabaseReference;
    private Button mButton;
    private EditText mEditText;
    private String mId;

    public void logout(View view) {
        mFirebaseAuth.signOut();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();
        sendToSignin();
       // testfriends();
       FriendsHandler friendsHandler = new FriendsHandler(this);
       friendsHandler.addFriendsHandler(mFirebaseAuth.getUid());
        mButton = (Button) findViewById(R.id.button_send_request);
        mEditText = (EditText) findViewById(R.id.action_name);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                String name = mEditText.getText().toString();
                sendRequest(name);
            }
        });

        // Chats child is for main activity showing the last message and time.
        // will store objects of ChatInstance class.
        mDatabaseReference.child("Chats").child(mFirebaseAuth.getCurrentUser().getUid()).setValue("");

        // Messages child of for chat activity showing all the messages.
        // will store objects of MessageInstance class.
        mDatabaseReference.child("Messages").child(mFirebaseAuth.getCurrentUser().getUid() + "DiWxZsUAqjMrCRU4QJp8lwLzjXJ2").setValue("");
    }

    private void sendRequest(final String name) {

     ValueEventListener valueEventListener = new ValueEventListener() {
         @Override
         public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
             Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();

             while (iterator.hasNext()) {
                 DataSnapshot data = iterator.next();
                 if (data.child("name").getValue().equals(name))
                     mId = data.getKey();
             }
             if(mId != null)
                 mDatabaseReference.child("Users").child(mId).child("friends").child(mFirebaseAuth.getUid()).setValue("pending");
         }

         @Override
         public void onCancelled(@NonNull DatabaseError databaseError) {

         }
     };

     mDatabaseReference.child("Users").addListenerForSingleValueEvent(valueEventListener);
    }

    private void sendToSignin() {
        if (mFirebaseAuth.getCurrentUser() == null){
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    /*
    private void testfriends(){
        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        String id = user.getUid();
        mFirebaseDatabase = FirebaseDatabase.getInstance();

        Map<String,String> map = new HashMap<>();

        map.put("ahmed","yes");
        map.put("khaled","accepted");
        map.put("yasser","pending");
        mDatabaseReference.child("Users").child(id).child("friends");
        mDatabaseReference.child("Users").child(id).child("friends").setValue(map);

        FriendsHandler friendsHandler = new FriendsHandler(this);
        friendsHandler.addFriendsHandler(id);
    }

     */
}