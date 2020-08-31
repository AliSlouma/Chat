package com.example.chat.chats;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.chat.FirebaseUtil;
import com.example.chat.FrontActivity;
import com.example.chat.login.LoginActivity;
import com.example.chat.R;
import com.example.chat.user.UserProfileActivity;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
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
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    DatabaseReference messageKeyRef , messageRef , userRef ,receRef ,seenRef;
    EditText sendMessageEditText;
    MessageInstance messageInstance;
    String receiverID;
    String senderID;
    RecyclerView chatRecyclerView;
    LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private final List<MessageInstance> messageInstanceList = new ArrayList<>();
    public static final int galleryConstant = 5;
    StorageReference storageReference;
    ImageView imageView;
    String receiverName;
    String receiverPhoto;
    DatabaseReference userState;
    ConstraintLayout constraintLayout;



    public void sendButton(View view) {


        String getMessage = sendMessageEditText.getText().toString();
        if(!TextUtils.isEmpty(getMessage)){
            compareTheTwoIDS(senderID, receiverID , "messagesContent");
            String key = messageKeyRef.push().getKey();
            messageRef = messageKeyRef.child(key);
            messageInstance.setMessage(getMessage);
            messageInstance.setmType("text");
            messageRef.setValue(messageInstance);
            sendMessageEditText.setText("");

            initializedChatInstanceSender(messageInstance.getSender()+": "+getMessage);
            initializedChatInstanceReceiver(messageInstance.getSender()+": " + getMessage);





        }



    }

    private void initializedChatInstanceReceiver(String getMessage) {
        ChatInstance chatInstance2 = new ChatInstance();
        chatInstance2.setLastMessage(getMessage);
        chatInstance2.setReceiverUID(senderID);
        chatInstance2.setReceiver(messageInstance.getSender());
        chatInstance2.setFriendPhoto(messageInstance.getmReceiverPhoto());
        chatInstance2.setTime(messageInstance.getTime());
        chatInstance2.setSeen(false);
        DatabaseReference ref2 = FirebaseDatabase.getInstance().getReference().child("ChatsList").child(receiverID).child(senderID);
        ref2.setValue(chatInstance2);
    }

    private void initializedChatInstanceSender (String getMessage) {
        ChatInstance chatInstance = new ChatInstance();
        chatInstance.setLastMessage(getMessage);
        chatInstance.setReceiverUID(receiverID);
        chatInstance.setReceiver(receiverName);
        chatInstance.setFriendPhoto(receiverPhoto);
        chatInstance.setTime(messageInstance.getTime());
        chatInstance.setSeen(true);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("ChatsList").child(senderID).child(receiverID);
        ref.setValue(chatInstance);
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

                        initializedChatInstanceSender("Sent a photo");
                        initializedChatInstanceReceiver("sent a photo");


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


    protected void onStart() {
        super.onStart();
        if (firebaseAuth.getCurrentUser() == null){
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }  else{
            userState.child("userState").setValue("online");


        }


    }





    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        final Intent intent = getIntent();
        receiverID = intent.getStringExtra("receiverID");
        messageInstance = new MessageInstance();

        firebaseAuth = FirebaseAuth.getInstance();
        senderID = firebaseAuth.getUid();
        compareTheTwoIDS(senderID, receiverID , "messagesContent");

        constraintLayout = findViewById(R.id.linearLayout);


        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(senderID);
        receRef = FirebaseDatabase.getInstance().getReference().child("Users").child(receiverID);

        getReceiverPhoto();

        sendMessageEditText = findViewById(R.id.sendMessageEditView);

        messageInstance.setmSenderID(senderID);
        setTimeInformation();
        getUserName();
        getReceiverName();
        getSenderPhoto();
        messageAdapter = new MessageAdapter(messageInstanceList,this);
        chatRecyclerView = findViewById(R.id.recycle);
        linearLayoutManager = new LinearLayoutManager(this);
        chatRecyclerView.setLayoutManager(linearLayoutManager);
        chatRecyclerView.setAdapter(messageAdapter);

        seenRef = FirebaseDatabase.getInstance().getReference().child("ChatsList").child(senderID).child(receiverID);

        setSeen();
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

        messageKeyRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                MessageInstance message = dataSnapshot.getValue(MessageInstance.class);

                messageInstanceList.add(dataSnapshot.getValue(MessageInstance.class));
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


        userState = FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseAuth.getUid());


        FirebaseUtil.sDatabaseReference.child(FrontActivity.BLOCKED_USERS).child(receiverID).child(FirebaseUtil.sFirebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null)
                {
                    constraintLayout.setVisibility(View.INVISIBLE);
                }
                else
                    constraintLayout.setVisibility(View.VISIBLE);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();

        seenRef.child("seen").setValue(true);
    }

    private void setSeen() {
        //   DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("ChatList").child(receiverID);
        seenRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    ChatInstance instance = dataSnapshot.getValue(ChatInstance.class);
                    instance.setSeen(true);
                    seenRef.setValue(instance);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void getReceiverPhoto() {
        userRef.addValueEventListener(new ValueEventListener() {
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


    private void getSenderPhoto() {
        receRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("imageUri").exists())
                    receiverPhoto = (String) dataSnapshot.child("imageUri").getValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void getReceiverName() {
        receRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("name").exists()){
                    String name = dataSnapshot.child("name").getValue().toString();
                    receiverName = name;
                }
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

    private void compareTheTwoIDS(String uid, String receiverID , String place) {
        if(uid.compareTo(receiverID) >=0){
            messageKeyRef = FirebaseDatabase.getInstance().getReference().child(place).child(uid+receiverID);
        }else
            messageKeyRef = FirebaseDatabase.getInstance().getReference().child(place).child(receiverID+uid);
    }
}