package com.example.chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.FileNotFoundException;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.chat.FirebaseUtil.*;

public class UserProfileActivity extends AppCompatActivity {

    private static final int GET_FROM_GALLERY = 3;
    private static final String PHOTO = "Photo";
    private static final String NAME = "name";
    private static final String STATUS = "status";
    private static final String IMAGE_URI = "imageUri";
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
    private CircleImageView mUserPhoto;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mStorageRef;
    private StorageReference mProfileRef;
    private Uri mDownloadUri;
    private AlertDialog.Builder mDialogName;
    private AlertDialog.Builder mDialogStatus;
    private String mState;
    private String mName;
    private String mStatus;
    private String mUri;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mReference = FirebaseDatabase.getInstance().getReference();
        mNameEditText = (TextView) findViewById(R.id.user_name);
        mStatusEditText = (TextView) findViewById(R.id.user_status);
        mPhotoImageView = (ImageView) findViewById(R.id.receiver_chat_photo);
        mUploadButton = (Button) findViewById(R.id.action_upload_photo);
        mFirebaseStorage = FirebaseStorage.getInstance();
        mStorageRef = mFirebaseStorage.getReferenceFromUrl("gs://chat-d4365.appspot.com");

        displayStateValue();
        createDialogs();
        Button button = (Button) findViewById(R.id.button_edit_name);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText input = new EditText(getBaseContext());
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                mDialogName.setView(input);
                mDialogName.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mNameEditText.setText(input.getText().toString());
                        sDatabaseReference.child("Users").child(mUserID).child(NAME).setValue(input.getText().toString());
                    }
                });
                mDialogName.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                mDialogName.show();
            }
        });

        Button btn = (Button) findViewById(R.id.button_edit_status);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText input = new EditText(getBaseContext());
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                mDialogStatus.setView(input);
                mDialogStatus.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mNameEditText.setText(input.getText().toString());
                        sDatabaseReference.child("Users").child(mUserID).child(STATUS).setValue(input.getText().toString());
                    }
                });
                mDialogStatus.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                mDialogStatus.show();
            }
        });

        mProfileRef = mStorageRef.child("images/profiles/" + mUserID + ".jpg");
        mUploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);
            }
        });

        mUserPhoto = (CircleImageView)findViewById(R.id.receiver_chat_photo);
        displayData();
    }

    public void createDialogs(){
        mDialogName = new AlertDialog.Builder(this);
        mDialogName.setTitle("Enter your name");

        mDialogStatus = new AlertDialog.Builder(this);
        mDialogStatus.setTitle("Enter your status");

    }

    private void displayStateValue() {
        Intent intent = getIntent();
        mUserID = intent.getStringExtra(FrontActivity.USER_ID);
    }


    private void displayData() {

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                mName = (String) dataSnapshot.child(NAME).getValue();
                mStatus = (String) dataSnapshot.child(STATUS).getValue();
                mUri = (String) dataSnapshot.child(IMAGE_URI).getValue();
                mNameEditText.setText(mName);
                mStatusEditText.setText(mStatus);
                Glide.with(getBaseContext()).load(mUri).into(mUserPhoto);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("ERROR", databaseError.getMessage());
            }
        };

        sDatabaseReference.child("Users").child(mUserID).addValueEventListener(valueEventListener);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mBaseMenu = menu;
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.profile_menu, menu);
        Intent intent = getIntent();
        mState = intent.getStringExtra(FrontActivity.STATE);
        switch (mState) {
            case FrontActivity.FRIENDS_ID:
                changeFriendsState(menu);
                displayData();
                break;
            case FrontActivity.REQUEST_ID:
                changeRequestState(menu);
                displayData();
                break;
            case FrontActivity.PENDING_ID:
                changePendingState(menu);
                displayData();
                break;
            case FrontActivity.NOT_FRIENDS_ID:
                changeNotFriendsState(menu);
                displayData();
                break;
            case FrontActivity.USER_PROFILE_ID:
                changeUserState(menu);
                displayData();
                break;
            case FrontActivity.BLOCKED_ID:
                disableAll(menu);
                mNameEditText.setText("User is blocking you ;(");
                break;
            case FrontActivity.BLOCKING_ID:
                changeBlockingState(menu);
                break;
        }
        return super.onCreateOptionsMenu(menu);
    }

    private void changeBlockingState(Menu menu) {
        disableAll(menu);
        displayData();
        menu.findItem(R.id.action_unblock).setVisible(true);
    }

    private void changeUserState(Menu menu) {
        disableAll(menu);
    }

    private void disableAll(Menu menu) {
        disable(menu,R.id.action_send_message);
        disable(menu,R.id.action_unfriend);
        disable(menu,R.id.action_pending);
        disable(menu,R.id.action_add_friend);
        disable(menu,R.id.action_block);
        disable(menu,R.id.action_accept_request);
        disable(menu,R.id.action_reject);
        disable(menu,R.id.action_unblock);
    }

    private void changeRequestState(Menu menu) {
        disable(menu,R.id.action_send_message);
        disable(menu,R.id.action_unfriend);
        disable(menu,R.id.action_pending);
        disable(menu,R.id.action_add_friend);
        disable(menu,R.id.action_unblock);
        enable(menu,R.id.action_block);
        enable(menu,R.id.action_accept_request);
        enable(menu,R.id.action_reject);
    }

    private void changeNotFriendsState(Menu menu) {
        disable(menu,R.id.action_unblock);
        disable(menu,R.id.action_accept_request);
        disable(menu,R.id.action_reject);
        disable(menu,R.id.action_send_message);
        disable(menu,R.id.action_unfriend);
        disable(menu,R.id.action_pending);
        enable(menu,R.id.action_add_friend);
        enable(menu,R.id.action_block);
    }

    private void changePendingState(Menu menu) {
        disable(menu,R.id.action_unblock);
        disable(menu,R.id.action_add_friend);
        disable(menu,R.id.action_accept_request);
        disable(menu,R.id.action_reject);
        disable(menu,R.id.action_send_message);
        disable(menu,R.id.action_unfriend);
        enable(menu,R.id.action_pending);
        enable(menu,R.id.action_block);
    }

    private void changeFriendsState(Menu menu) {
        disable(menu,R.id.action_unblock);
       disable(menu,R.id.action_add_friend);
       disable(menu,R.id.action_accept_request);
       disable(menu,R.id.action_pending);
       disable(menu,R.id.action_reject);
       enable(menu,R.id.action_send_message);
       enable(menu,R.id.action_unfriend);
       enable(menu,R.id.action_block);
    }

    private void disable(Menu menu, int p ){
        menu.findItem(p).setVisible(false);
    }

    private void enable(Menu menu , int p){
        menu.findItem(p).setVisible(true);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_add_friend) {
            mReference.child(FrontActivity.PATH_REQUESTS).child(mUserID).child(sFirebaseAuth.getUid()).setValue(FrontActivity.mMyUSer.getName());
            changePendingState(mBaseMenu);
        }
        if (id == R.id.action_send_message) {
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("receiverID", mUserID);
            startActivity(intent);
        }
        if (id == R.id.action_accept_request) {
            mReference.child(FrontActivity.PATH_FRIENDS).child(mUserID).child(mFirebaseAuth.getUid()).setValue(FrontActivity.mMyUSer.getName());
            mReference.child(FrontActivity.PATH_FRIENDS).child(mFirebaseAuth.getUid()).child(mUserID).setValue(mName);
            mReference.child(FrontActivity.PATH_REQUESTS).child(mFirebaseAuth.getUid()).child(mUserID).removeValue();
            changeFriendsState(mBaseMenu);
        }
        if(id == R.id.action_reject){
            rejectRequest();
        }
        if (id == R.id.action_pending) {
            removeRequest();
            changeNotFriendsState(mBaseMenu);
        }
        if(id == R.id.action_block){
            sDatabaseReference.child(FrontActivity.BLOCKED_USERS).child(sFirebaseAuth.getUid()).child(mUserID).setValue(mName);
            if(mState.equals(FrontActivity.FRIENDS_ID)){
                unFriend();
            } else if (mState.equals(FrontActivity.PENDING_ID)) {
                removeRequest();
            }else if(mState.equals(FrontActivity.REQUEST_ID)){
                rejectRequest();
            }
            changeBlockingState(mBaseMenu);
        }
        if(id == R.id.action_unblock){
            sDatabaseReference.child(FrontActivity.BLOCKED_USERS).child(sFirebaseAuth.getUid()).child(mUserID).removeValue();
            changeNotFriendsState(mBaseMenu);
        }
        if(id == R.id.action_unfriend){
            unFriend();
            changeNotFriendsState(mBaseMenu);
        }
        return true;
    }

    private void rejectRequest() {
        mReference.child(FrontActivity.PATH_REQUESTS).child(mFirebaseAuth.getUid()).child(mUserID).removeValue();
    }

    private void removeRequest() {
        sDatabaseReference.child(FrontActivity.PATH_REQUESTS).child(mUserID).child(sFirebaseAuth.getUid()).removeValue();
    }

    private void unFriend() {
        mReference.child(FrontActivity.PATH_FRIENDS).child(mUserID).child(mFirebaseAuth.getUid()).removeValue();
        mReference.child(FrontActivity.PATH_FRIENDS).child(mFirebaseAuth.getUid()).child(mUserID).removeValue();
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