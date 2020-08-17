package com.example.chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Iterator;

import static com.example.chat.FirebaseUtil.*;

public class UserProfileActivity extends AppCompatActivity {

    private String mUserID;
    private DatabaseReference mUserInfo;
    private TextView mNameEditText;
    private TextView mStatusEditText;
    private ImageView mPhotoImageView;
    private String mId;
    private Menu mBaseMenu;
    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mReference = FirebaseDatabase.getInstance().getReference();
        mNameEditText = (TextView)findViewById(R.id.user_name);
        mStatusEditText = (TextView)findViewById(R.id.user_status);
        mPhotoImageView = (ImageView)findViewById(R.id.user_photo);
        displayStateValue();
        displayData();
    }

    private void displayStateValue() {
        Intent intent = getIntent();
        mUserID = intent.getStringExtra(FrontActivity.USER_ID);
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

        sDatabaseReference.child("Users").child(mUserID).addListenerForSingleValueEvent(valueEventListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mBaseMenu = menu;
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.profile_menu , menu);
        Intent intent = getIntent();
        switch (intent.getStringExtra(FrontActivity.STATE)){
            case FrontActivity.FRIENDS_ID:
                 changeFriendsState(menu);
                break;
            case  FrontActivity.REQUEST_ID :
                 changeRequestState(menu);
                 break;
            case FrontActivity.PENDING_ID:
                 changePendingState(menu);
                 break;
            case FrontActivity.NOT_FRIENDS_ID :
                changeNotFriendsState(menu);
                break;
        }
        return super.onCreateOptionsMenu(menu);
    }

    private void changeRequestState(Menu menu){
        changeState(menu, R.id.action_pending, R.id.action_send_message, R.id.action_add_friend,R.id.action_accept_request);
    }

    private void changeNotFriendsState(Menu menu){
        changeState(menu, R.id.action_pending, R.id.action_send_message, R.id.action_accept_request,R.id.action_add_friend);
    }

    private void changePendingState(Menu menu){
        changeState(menu, R.id.action_add_friend, R.id.action_send_message, R.id.action_accept_request,R.id.action_pending);
    }

    private void changeFriendsState(Menu menu){
        changeState(menu, R.id.action_add_friend, R.id.action_accept_request, R.id.action_pending,R.id.action_send_message);
    }

    private void changeState(Menu menu, int p, int p2, int p3 , int p4) {
        menu.findItem(p).setVisible(false);
        menu.findItem(p2).setVisible(false);
        menu.findItem(p3).setVisible(false);
        menu.findItem(p4).setVisible(true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_add_friend) {
            mReference.child(FrontActivity.PATH_REQUESTS).child(mUserID).child(sFirebaseAuth.getUid()).setValue("");
            changePendingState(mBaseMenu);
        }
        if (id == R.id.action_send_message) {
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("receiverID", mUserID);
            startActivity(intent);
        }
        if (id == R.id.action_accept_request) {
            mReference.child(FrontActivity.PATH_FRIENDS).child(mUserID).child(mFirebaseAuth.getUid()).setValue("");
            mReference.child(FrontActivity.PATH_FRIENDS).child(mFirebaseAuth.getUid()).child(mUserID).setValue("");
            mReference.child(FrontActivity.PATH_REQUESTS).child(mFirebaseAuth.getUid()).child(mUserID).setValue("done");
            changeFriendsState(mBaseMenu);
        }
        if (id == R.id.action_pending) {
            sDatabaseReference.child(FrontActivity.PATH_REQUESTS).child(mUserID).child(sFirebaseAuth.getUid()).setValue("done");
            changeNotFriendsState(mBaseMenu);
        }
        return true;
    }
}