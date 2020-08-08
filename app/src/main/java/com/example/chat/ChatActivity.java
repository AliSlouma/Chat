package com.example.chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;

public class ChatActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    DatabaseReference messageKeyRef , messageRef , userRef;
    EditText sendMessageEditText;
    TextView displayMessageView;
    MessageInstance messageInstance;


    public void sendButton(View view) {
        String getMessage = sendMessageEditText.getText().toString();

        String key = messageKeyRef.push().getKey();
        messageRef = messageKeyRef.child(key);
        setMessageInformation();
        messageRef.child("messageContent").setValue(getMessage);
        messageRef.child("Time").setValue(messageInstance.getTime());
        messageRef.child("Date").setValue(messageInstance.getDate());
        messageRef.child("Sender").setValue(messageInstance.getSender());


        sendMessageEditText.setText("");
        displayMessageView.setText("");

    }

    private void setMessageInformation() {
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
              //  displayMessages(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void displayMessages(DataSnapshot dataSnapshot) {
        Iterator iterator = dataSnapshot.getChildren().iterator();
        while (iterator.hasNext()){
            String message = (String) ((DataSnapshot)iterator.next()).getValue();
            displayMessageView.append(message+"\n");
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        firebaseAuth = FirebaseAuth.getInstance();
        messageKeyRef = FirebaseDatabase.getInstance().getReference().child("Messages").child("eptsajHDYfYj7RvD8vy8OO8k4Ji1DiWxZsUAqjMrCRU4QJp8lwLzjXJ2");
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseAuth.getCurrentUser().getUid().toString());
        sendMessageEditText = findViewById(R.id.sendMessageEditView);
        displayMessageView = findViewById(R.id.displayMessageView);
        messageInstance = new MessageInstance();



    }
}