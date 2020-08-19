package com.example.chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class ChatActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    DatabaseReference messageKeyRef , messageRef , userRef;
    EditText sendMessageEditText;
    TextView displayMessageView;
    MessageInstance messageInstance;
    ScrollView scrollView;



    public void sendButton(View view) {
        String getMessage = sendMessageEditText.getText().toString();

        String key = messageKeyRef.push().getKey();
        messageRef = messageKeyRef.child(key);
        messageInstance.setMessage(getMessage);
        setTimeInformation();
        scrollView.fullScroll(ScrollView.FOCUS_DOWN);
        messageRef.setValue(messageInstance);
        sendMessageEditText.setText("");
        displayMessageView.setText("");
        scrollView.postDelayed(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        }, 0);

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
        messageKeyRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                displayMessages(dataSnapshot);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void displayMessages(DataSnapshot dataSnapshot) {
        Iterable<DataSnapshot> iterable = dataSnapshot.getChildren();
        Iterator<DataSnapshot> iterator = iterable.iterator();
        MessageInstance messageInstance1;
        while (iterator.hasNext()){

            HashMap <String ,String> map = (HashMap<String, String>) iterator.next().getValue();

            displayMessageView.append(map.get("sender") +"\n" +
            map.get("message") +"\n" + map.get("time") + "\t" + map.get("date")+ "\n\n");

            scrollView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scrollView.fullScroll(View.FOCUS_DOWN);
                }
            }, 500);

        }


    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Intent intent = getIntent();
        String receiverID = intent.getStringExtra("receiverID");
        scrollView = (ScrollView) findViewById(R.id.chatScroll);
        scrollView.postDelayed(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        }, 0);


        firebaseAuth = FirebaseAuth.getInstance();

        compareTheTwoIDS(firebaseAuth.getUid() , receiverID);

        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseAuth.getUid());

        sendMessageEditText = findViewById(R.id.sendMessageEditView);
        displayMessageView = findViewById(R.id.displayMessageView);
        messageInstance = new MessageInstance();
        getUserName();


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
       // messageKeyRef.push().setValue("");
    }
}