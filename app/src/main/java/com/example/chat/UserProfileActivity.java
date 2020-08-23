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

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mReference = FirebaseDatabase.getInstance().getReference();
        mNameEditText = (TextView) findViewById(R.id.user_name);
        mStatusEditText = (TextView) findViewById(R.id.user_status);
        mPhotoImageView = (ImageView) findViewById(R.id.profile_photo);
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

        mUserPhoto = (CircleImageView)findViewById(R.id.profile_photo);
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

                String name = (String) dataSnapshot.child(NAME).getValue();
                String status = (String) dataSnapshot.child(STATUS).getValue();
                String uri = (String) dataSnapshot.child(IMAGE_URI).getValue();
                mNameEditText.setText(name);
                mStatusEditText.setText(status);
                Glide.with(getBaseContext()).load(uri).into(mUserPhoto);
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