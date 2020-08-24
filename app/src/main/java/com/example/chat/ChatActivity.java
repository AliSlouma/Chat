package com.example.chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Instrumentation;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static com.example.chat.FirebaseUtil.sDatabaseReference;

public class ChatActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    DatabaseReference messageKeyRef , messageRef , userRef ,receRef;
    EditText sendMessageEditText;
    MessageInstance messageInstance;
    String receiverID;
    String senderID;
    RecyclerView chatRecyclerView;
    LinearLayoutManager linearLayoutManager;
    private  MessageAdapter messageAdapter;
    private final List<MessageInstance> messageInstanceList = new ArrayList<>();
    public static final int galleryConstant = 5;
    StorageReference storageReference;
    ImageView imageView;



    public void sendButton(View view) {


        String getMessage = sendMessageEditText.getText().toString();
        if(!TextUtils.isEmpty(getMessage)){
            String key = messageKeyRef.push().getKey();
            messageRef = messageKeyRef.child(key);
            messageInstance.setMessage(getMessage);
            messageInstance.setmType("text");
            messageRef.setValue(messageInstance);
            sendMessageEditText.setText("");
        }



    }
    public void uploadPhoto(View view){


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == galleryConstant && resultCode == RESULT_OK && data!=null && data.getData()!= null){
            Uri uri = data.getData();
            String s = uri.getPath();

            final StorageReference file = storageReference.child(s);

             UploadTask uploadTask = file.putFile(uri);
             uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return file.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri mDownloadUri = task.getResult();
                        String url = mDownloadUri.toString();

                        messageInstance.setmChatPhoto(url);
                        messageInstance.setmType("photo");
                        String key = messageKeyRef.push().getKey();
                        messageRef = messageKeyRef.child(key);
                        messageRef.setValue(messageInstance);


                    }
                }
            });
        }
    }


    private void setTimeInformation() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MM , YYYY");
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
        String currentDate = dateFormat.format(calendar.getTime());
        String currentTime = timeFormat.format(calendar.getTime());
        messageInstance.setDate(currentDate);
        messageInstance.setTime(currentTime);

    }

    @Override
    protected void onStart() {
        super.onStart();
        messageKeyRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                messageInstanceList.add(dataSnapshot.getValue(MessageInstance.class));
                Log.i("hi" ,"11111111111111");
                messageAdapter.notifyDataSetChanged();
                chatRecyclerView.smoothScrollToPosition(chatRecyclerView.getAdapter().getItemCount());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        final Intent intent = getIntent();
        receiverID = intent.getStringExtra("receiverID");
        messageInstance = new MessageInstance();

        firebaseAuth = FirebaseAuth.getInstance();
        senderID = firebaseAuth.getUid();
        compareTheTwoIDS(senderID, receiverID);


        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseAuth.getUid());
        receRef = FirebaseDatabase.getInstance().getReference().child("Users").child(receiverID);

        getReceiverPhoto();

        sendMessageEditText = findViewById(R.id.sendMessageEditView);

        messageInstance.setmSenderID(senderID);
        setTimeInformation();
        getUserName();
        messageAdapter = new MessageAdapter(messageInstanceList,this);
        chatRecyclerView = findViewById(R.id.recycle);
        linearLayoutManager = new LinearLayoutManager(this);
        chatRecyclerView.setLayoutManager(linearLayoutManager);
        chatRecyclerView.setAdapter(messageAdapter);
        chatRecyclerView.scrollToPosition(View.FOCUS_DOWN);

        storageReference = FirebaseStorage.getInstance().getReference().child("Chat Messages");
        imageView = findViewById(R.id.galary);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent.createChooser(galleryIntent, " Select Photo"), galleryConstant);
            }
        });


    }

    private void getReceiverPhoto() {
        receRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("imageUri").exists())
                    messageInstance.setmReceiverPhoto((String) dataSnapshot.child("imageUri").getValue());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getUserName() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("name").exists()){
                    String name = dataSnapshot.child("name").getValue().toString();
                    messageInstance.setSender(name);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void compareTheTwoIDS(String uid, String receiverID) {
        if(uid.compareTo(receiverID) >=0){
            messageKeyRef = FirebaseDatabase.getInstance().getReference().child("messagesContent").child(uid+receiverID);
        }else
            messageKeyRef = FirebaseDatabase.getInstance().getReference().child("messagesContent").child(receiverID+uid);
    }
}