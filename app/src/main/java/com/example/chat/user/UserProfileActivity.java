package com.example.chat.user;

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
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.example.chat.FrontActivity;
import com.example.chat.R;
import com.example.chat.chats.ChatActivity;
import com.example.chat.services.MySingleton;
import com.example.chat.services.UploadPhotoService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.chat.FirebaseUtil.*;

public class UserProfileActivity extends AppCompatActivity {
    final private String FCM_API = "https://fcm.googleapis.com/fcm/send";
    final private String serverKey = "key=" + "AAAAgnprc9w:APA91bE-f5o3ju0LZFvw_HrmPRgm6fBrwEs9jwQ4tBukzfh8sWv4ktm8EHwfNfl7GbJl2A7sYbl6R7tlZmCPJn0fEX8pGJqWyqK87Pe-bZYL0qIutr6LW57Z8lUhqDh9W8C7LAg8q1bc";
    final private String contentType = "application/json";
    final String TAG = "NOTIFICATION TAG";
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
    private int rejection_cnt;
    private int mMessageCnt;
    private int mAcceptCnt;
    private int mCancelCnt;
    private int mAddCnt;
    private Button mNameEditButton;
    private Button mStatusEditButton;

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
        mNameEditButton = (Button) findViewById(R.id.button_edit_name);
        mNameEditButton.setOnClickListener(new View.OnClickListener() {
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

        mStatusEditButton = (Button) findViewById(R.id.button_edit_status);
        mStatusEditButton.setOnClickListener(new View.OnClickListener() {
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
        //  addBlockListener();
        if(!mUserID.equals(mFirebaseAuth.getUid())){
            mUploadButton.setVisibility(View.INVISIBLE);
            mNameEditButton.setVisibility(View.INVISIBLE);
            mStatusEditButton.setVisibility(View.INVISIBLE);
        }
    }

    private void addBlockListener() {

        mReference.child(FrontActivity.BLOCKED_USERS).child(mUserID).child(mFirebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null){
                    finish();
                    Intent intent = new Intent(getBaseContext(),UserProfileActivity.class);
                    intent.putExtra(FrontActivity.STATE,FrontActivity.BLOCKED_ID);
                    intent.putExtra(FrontActivity.USER_ID,mUserID);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void friendsListener(){
        mReference.child(FrontActivity.PATH_REQUESTS).child(mUserID).child(sFirebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() == null)
                    checkFriends();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mReference.child(FrontActivity.PATH_REQUESTS).child(mFirebaseAuth.getUid()).child(mUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() == null)
                    checkFriends();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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
        final int id = item.getItemId();

        if (id == R.id.action_add_friend) {
            mAddCnt = 0;
            mReference.child(FrontActivity.PATH_REQUESTS).child(mUserID).child(sFirebaseAuth.getUid()).setValue(FrontActivity.mMyUSer.getName());
            mReference.child(FrontActivity.BLOCKED_USERS).child(mUserID).child(mFirebaseAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getValue() == null)
                        mAddCnt++;
                    else {
                        finish();
                        Intent intent = new Intent(getBaseContext(),UserProfileActivity.class);
                        intent.putExtra(FrontActivity.STATE,FrontActivity.BLOCKED_ID);
                        intent.putExtra(FrontActivity.USER_ID,mUserID);
                        startActivity(intent);
                    }
                    if(mAddCnt == 2) {
                        mReference.child(FrontActivity.PATH_REQUESTS).child(mUserID).child(sFirebaseAuth.getUid()).setValue(FrontActivity.mMyUSer.getName());
                        manageNotification();
                        changePendingState(mBaseMenu);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            mReference.child(FrontActivity.PATH_FRIENDS).child(mFirebaseAuth.getUid()).child(mUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getValue() == null)
                        mAddCnt++;
                    else {
                        changeFriendsState(mBaseMenu);
                        Toast.makeText(getBaseContext(),"User already sent you a request!! you are now Friends",Toast.LENGTH_SHORT).show();
                    }
                    if(mAddCnt == 2) {
                        mReference.child(FrontActivity.PATH_REQUESTS).child(mUserID).child(sFirebaseAuth.getUid()).setValue(FrontActivity.mMyUSer.getName());
                        manageNotification();
                        changePendingState(mBaseMenu);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });



            /*
            mReference.child(FrontActivity.PATH_REQUESTS).child(mUserID).child(sFirebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getValue() == null)
                        checkFriends();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            mReference.child(FrontActivity.PATH_REQUESTS).child(mFirebaseAuth.getUid()).child(mUserID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getValue() == null)
                        checkFriends();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


             */


        }
        if (id == R.id.action_send_message) {
            mMessageCnt = 0;
            mReference.child(FrontActivity.BLOCKED_USERS).child(mUserID).child(mFirebaseAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getValue() == null)
                        mMessageCnt++;
                    else {
                        finish();
                        Intent intent = new Intent(getBaseContext(),UserProfileActivity.class);
                        intent.putExtra(FrontActivity.STATE,FrontActivity.BLOCKED_ID);
                        intent.putExtra(FrontActivity.USER_ID,mUserID);
                        startActivity(intent);
                    }
                    if(mMessageCnt == 2) {
                        Intent intent = new Intent(getBaseContext(), ChatActivity.class);
                        intent.putExtra("receiverID", mUserID);
                        startActivity(intent);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            mReference.child(FrontActivity.PATH_FRIENDS).child(mUserID).child(mFirebaseAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getValue() != null)
                        mMessageCnt++;
                    else {
                        changeNotFriendsState(mBaseMenu);
                        Toast.makeText(getBaseContext(),"User unfriended you!!!",Toast.LENGTH_SHORT).show();
                    }
                    if(mMessageCnt == 2) {
                        Intent intent = new Intent(getBaseContext(), ChatActivity.class);
                        intent.putExtra("receiverID", mUserID);
                        startActivity(intent);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            /*
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("receiverID", mUserID);
            startActivity(intent);

             */
        }
        if (id == R.id.action_accept_request) {
            mAcceptCnt = 0;
            mReference.child(FrontActivity.BLOCKED_USERS).child(mUserID).child(mFirebaseAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getValue() == null)
                        mAcceptCnt++;
                    else {
                        finish();
                        Intent intent = new Intent(getBaseContext(),UserProfileActivity.class);
                        intent.putExtra(FrontActivity.STATE,FrontActivity.BLOCKED_ID);
                        intent.putExtra(FrontActivity.USER_ID,mUserID);
                        startActivity(intent);
                    }
                    if(mAcceptCnt == 2) {
                        mReference.child(FrontActivity.PATH_FRIENDS).child(mUserID).child(mFirebaseAuth.getUid()).setValue(FrontActivity.mMyUSer.getName());
                        mReference.child(FrontActivity.PATH_FRIENDS).child(mFirebaseAuth.getUid()).child(mUserID).setValue(mName);
                        mReference.child(FrontActivity.PATH_REQUESTS).child(mFirebaseAuth.getUid()).child(mUserID).removeValue();
                        changeFriendsState(mBaseMenu);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            mReference.child(FrontActivity.PATH_REQUESTS).child(mFirebaseAuth.getUid()).child(mUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getValue() != null)
                        mAcceptCnt++;
                    else {
                        changeNotFriendsState(mBaseMenu);
                        Toast.makeText(getBaseContext(),"User cancelled Request",Toast.LENGTH_SHORT).show();
                    }
                    if(mAcceptCnt == 2) {
                        mReference.child(FrontActivity.PATH_FRIENDS).child(mUserID).child(mFirebaseAuth.getUid()).setValue(FrontActivity.mMyUSer.getName());
                        mReference.child(FrontActivity.PATH_FRIENDS).child(mFirebaseAuth.getUid()).child(mUserID).setValue(mName);
                        mReference.child(FrontActivity.PATH_REQUESTS).child(mFirebaseAuth.getUid()).child(mUserID).removeValue();
                        changeFriendsState(mBaseMenu);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            /*
            mReference.child(FrontActivity.PATH_FRIENDS).child(mUserID).child(mFirebaseAuth.getUid()).setValue(FrontActivity.mMyUSer.getName());
            mReference.child(FrontActivity.PATH_FRIENDS).child(mFirebaseAuth.getUid()).child(mUserID).setValue(mName);
            mReference.child(FrontActivity.PATH_REQUESTS).child(mFirebaseAuth.getUid()).child(mUserID).removeValue();
            changeFriendsState(mBaseMenu);

             */
        }
        if(id == R.id.action_reject){
            rejection_cnt = 0;
            mReference.child(FrontActivity.PATH_REQUESTS).child(mFirebaseAuth.getUid()).child(mUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getValue()!= null)
                        rejection_cnt++;
                    else {
                        changeNotFriendsState(mBaseMenu);
                        Toast.makeText(getBaseContext(),"User cancelled Request",Toast.LENGTH_SHORT).show();
                    }
                    if(rejection_cnt == 2)
                        rejectRequest();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            mReference.child(FrontActivity.BLOCKED_USERS).child(mUserID).child(mFirebaseAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getValue() == null)
                        rejection_cnt++;
                    else {
                        finish();
                        Intent intent = new Intent(getBaseContext(),UserProfileActivity.class);
                        intent.putExtra(FrontActivity.STATE,FrontActivity.BLOCKED_ID);
                        intent.putExtra(FrontActivity.USER_ID,mUserID);
                        startActivity(intent);
                    }
                    if(rejection_cnt == 2)
                        rejectRequest();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        if (id == R.id.action_pending) {
            mCancelCnt = 0;
            mReference.child(FrontActivity.PATH_FRIENDS).child(mUserID).child(mFirebaseAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getValue() != null) {
                        changeFriendsState(mBaseMenu);
                        Toast.makeText(getBaseContext(), "User Accepted request!", Toast.LENGTH_SHORT).show();
                    }else {
                        mCancelCnt++;
                    }
                    if(mCancelCnt == 3) {
                        removeRequest();
                        changeNotFriendsState(mBaseMenu);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            mReference.child(FrontActivity.BLOCKED_USERS).child(mUserID).child(mFirebaseAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getValue() == null)
                        mCancelCnt++;
                    else {
                        disableAll(mBaseMenu);
                        finish();
                        Intent intent = new Intent(getBaseContext(),UserProfileActivity.class);
                        intent.putExtra(FrontActivity.STATE,FrontActivity.BLOCKED_ID);
                        intent.putExtra(FrontActivity.USER_ID,mUserID);
                        startActivity(intent);
                    }
                    if(mCancelCnt == 3){
                        removeRequest();
                        changeNotFriendsState(mBaseMenu);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            mReference.child(FrontActivity.PATH_REQUESTS).child(mUserID).child(mFirebaseAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getValue()!= null)
                        mCancelCnt++;
                    else {
                        changeNotFriendsState(mBaseMenu);
                        Toast.makeText(getBaseContext(),"User rejected Request!",Toast.LENGTH_SHORT).show();
                    }
                    if(mCancelCnt == 3) {
                        removeRequest();
                        changeNotFriendsState(mBaseMenu);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
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
            mReference.child(FrontActivity.BLOCKED_USERS).child(mUserID).child(mFirebaseAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null) {
                        disableAll(mBaseMenu);
                        finish();
                        Intent intent = new Intent(getBaseContext(), UserProfileActivity.class);
                        intent.putExtra(FrontActivity.STATE, FrontActivity.BLOCKED_ID);
                        intent.putExtra(FrontActivity.USER_ID, mUserID);
                        startActivity(intent);
                    }else{
                        unFriend();
                        changeNotFriendsState(mBaseMenu);
                    }
                }


                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }
        return true;
    }

    private void checkFriends() {
        mReference.child(FrontActivity.PATH_FRIENDS).child(mUserID).child(sFirebaseAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() == null)
                    changeNotFriendsState(mBaseMenu);
                else
                    changeFriendsState(mBaseMenu);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void manageNotification() {
        JSONObject notification = new JSONObject();
        JSONObject notifcationBody = new JSONObject();
        try {
            notifcationBody.put("title", "Request");
            notifcationBody.put("message", FrontActivity.mMyUSer.getName() + " send you a friend request!");
            notifcationBody.put(FrontActivity.USER_ID,mFirebaseAuth.getUid());
            notification.put("to","/topics/"+mUserID);
            notification.put("data", notifcationBody);
        } catch (JSONException e) {
        }
        sendNotification(notification);
    }
    private void sendNotification(JSONObject notification) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(FCM_API, notification,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(TAG, "onResponse: " + response.toString());
                      /*  edtTitle.setText("");
                        edtMessage.setText("");*/
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i(TAG, "onErrorResponse: Didn't work");
                    }
                }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", serverKey);
                params.put("Content-Type", contentType);
                return params;
            }
        };
        MySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectRequest);
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