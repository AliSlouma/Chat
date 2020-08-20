package com.example.chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.stream.BaseGlideUrlLoader;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.core.Context;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

import static com.example.chat.FirebaseUtil.*;

public class UserProfileActivity extends AppCompatActivity {

    private static final int GET_FROM_GALLERY = 3;
    private static final String PHOTO = "Photo";
    private String mUserID;
    private DatabaseReference mUserInfo;
    private TextView mNameEditText;
    private TextView mStatusEditText;
    private ImageView mPhotoImageView;
    private String mId;
    private Menu mBaseMenu;
    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mReference;
    private Button mUploadButton;
    private ImageView mUserPhoto;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mStorageRef;
    private StorageReference mProfileRef;
    private Uri mDownloadUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mReference = FirebaseDatabase.getInstance().getReference();
        mNameEditText = (TextView) findViewById(R.id.user_name);
        mStatusEditText = (TextView) findViewById(R.id.user_status);
        mPhotoImageView = (ImageView) findViewById(R.id.user_photo);
        mUploadButton = (Button) findViewById(R.id.action_upload_photo);
        mFirebaseStorage = FirebaseStorage.getInstance();
        mStorageRef = mFirebaseStorage.getReferenceFromUrl("gs://chat-d4365.appspot.com");
        displayStateValue();
        mProfileRef = mStorageRef.child("images/profiles/" + mUserID + ".jpg");
        mUploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);
            }
        });

        mUserPhoto = findViewById(R.id.user_photo);
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

                String name = (String) dataSnapshot.child("name").getValue();
                String status = (String) dataSnapshot.child("status").getValue();
                String uri = (String) dataSnapshot.child("imageUri").getValue();
                mNameEditText.setText(name);
                mStatusEditText.setText(status);
                RequestOptions requestOptions = new RequestOptions().override(1000,500);
                Glide.with(getBaseContext()).load(uri).apply(requestOptions).into(mUserPhoto);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("ERROR", databaseError.getMessage());
            }
        };

        sDatabaseReference.child("Users").child(mUserID).addListenerForSingleValueEvent(valueEventListener);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mBaseMenu = menu;
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.profile_menu, menu);
        Intent intent = getIntent();
        switch (intent.getStringExtra(FrontActivity.STATE)) {
            case FrontActivity.FRIENDS_ID:
                changeFriendsState(menu);
                break;
            case FrontActivity.REQUEST_ID:
                changeRequestState(menu);
                break;
            case FrontActivity.PENDING_ID:
                changePendingState(menu);
                break;
            case FrontActivity.NOT_FRIENDS_ID:
                changeNotFriendsState(menu);
                break;
            case FrontActivity.USER_PROFILE_ID:
                changeUserState(menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    private void changeUserState(Menu menu) {
        disableAll(menu, R.id.action_pending, R.id.action_send_message, R.id.action_add_friend, R.id.action_accept_request);
    }

    private void changeRequestState(Menu menu) {
        changeState(menu, R.id.action_pending, R.id.action_send_message, R.id.action_add_friend, R.id.action_accept_request);
    }

    private void changeNotFriendsState(Menu menu) {
        changeState(menu, R.id.action_pending, R.id.action_send_message, R.id.action_accept_request, R.id.action_add_friend);
    }

    private void changePendingState(Menu menu) {
        changeState(menu, R.id.action_add_friend, R.id.action_send_message, R.id.action_accept_request, R.id.action_pending);
    }

    private void changeFriendsState(Menu menu) {
        changeState(menu, R.id.action_add_friend, R.id.action_accept_request, R.id.action_pending, R.id.action_send_message);
    }

    private void changeState(Menu menu, int p, int p2, int p3, int p4) {
        menu.findItem(p).setVisible(false);
        menu.findItem(p2).setVisible(false);
        menu.findItem(p3).setVisible(false);
        menu.findItem(p4).setVisible(true);
        mUploadButton.setVisibility(Button.INVISIBLE);
    }

    private void disableAll(Menu menu, int p, int p2, int p3, int p4) {
        menu.findItem(p).setVisible(false);
        menu.findItem(p2).setVisible(false);
        menu.findItem(p3).setVisible(false);
        menu.findItem(p4).setVisible(false);
        mUploadButton.setVisibility(Button.VISIBLE);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        //Detects request codes
        if (requestCode == GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                Bitmap photoBit = (Bitmap.createScaledBitmap(bitmap, 800, 500, false));
                mUserPhoto.setImageBitmap(photoBit);
                Intent intent = new Intent(this, UploadPhotoService.class);
                intent.putExtra(UploadPhotoService.IMAGE_REG, selectedImage.toString());
                intent.putExtra(UploadPhotoService.USER_ID, mUserID);
                startService(intent);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public interface MyDataModel {
        public String buildUrl(int width, int height);
    }

    public class CustomImageSize implements MyDataModel {

        private String uri;

        public CustomImageSize(String uri) {
            this.uri = uri;
        }

        @Override
        public String buildUrl(int width, int height) {

            return uri + "?w=" + width + "&h=" + height;
        }
    }

}