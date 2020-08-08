package com.example.chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toolbar;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserProfileActivity extends AppCompatActivity {

    private String mUserID;
    private DatabaseReference mUserInfo;
    private TextView mNameEditText;
    private TextView mStatusEditText;
    private ImageView mPhotoImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        mNameEditText = (TextView)findViewById(R.id.user_name);
        mStatusEditText = (TextView)findViewById(R.id.user_status);
        mPhotoImageView = (ImageView)findViewById(R.id.user_photo);
        displayStateValue();
        displayData();
    }

    private void displayStateValue() {
        Intent intent = getIntent();
        mUserID = intent.getStringExtra(MainActivity.USER_ID);
    }


    private void displayData() {

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = (String)dataSnapshot.child("name").getValue();
                String status = (String)dataSnapshot.child("status").getValue();
                mNameEditText.setText(name);
                mStatusEditText.setText(status);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("ERROR" ,databaseError.getMessage());
            }
        };

        FirebaseUtil.sDatabaseReference.child("Users").child(mUserID).addListenerForSingleValueEvent(valueEventListener);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.profile_menu , menu);
        return super.onCreateOptionsMenu(menu);
    }
}