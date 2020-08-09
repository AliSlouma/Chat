
package com.example.chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {
    public static final String USER_ID = "com.example.chat.user_id";
    FirebaseAuth mFirebaseAuth;
    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference mDatabaseReference;
    ListView listView;
    ArrayAdapter arrayAdapter;
    ArrayList<String> messages;
    private EditText mNameEditText;
    private Button mAddButton;
    private String mId;
    private int mNotificationscount;
    private TextView mTextNotificationItemCount;

    public void logout(){
        mFirebaseAuth.signOut();

        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu , menu);
        final MenuItem menuItem = menu.findItem(R.id.front_notifications);

        View actionView = menuItem.getActionView();
        mTextNotificationItemCount = (TextView) actionView.findViewById(R.id.cart_badge);
        mNotificationscount = 10;
        setupBadge();

        return super.onCreateOptionsMenu(menu);


    }
    private void setupBadge() {

        if (mTextNotificationItemCount != null) {
            if (mNotificationscount == 0) {
                if (mTextNotificationItemCount.getVisibility() != View.GONE) {
                    mTextNotificationItemCount.setVisibility(View.GONE);
                }
            } else {
                mTextNotificationItemCount.setText(String.valueOf(Math.min(mNotificationscount, 99)));
                if (mTextNotificationItemCount.getVisibility() != View.VISIBLE) {
                    mTextNotificationItemCount.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()){
            case R.id.logout:
                logout();
                return true;
            default:
                return false;
        }
    }
    public void add (View view){

    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();
        sendToSignin();
        mNameEditText = (EditText)findViewById(R.id.action_add_friend);
        mAddButton = (Button) findViewById(R.id.button_add_friend);
       // mDatabaseReference.child("Users").child(mFirebaseAuth.getUid()).child("friends").setValue("");
        FriendsHandler friendsHandler = new FriendsHandler(this);
        friendsHandler.addFriendsHandler(mFirebaseAuth.getUid());
        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = mNameEditText.getText().toString();
                sendRequest(name);
            }
        });
        //testfriends();
        // Chats child is for main activity showing the last message and time.
        // will store objects of ChatInstance class.
        mDatabaseReference.child("Chats").child(mFirebaseAuth.getCurrentUser().getUid()).setValue("");

        // Messages child of for chat activity showing all the messages.
        // will store objects of MessageInstance class.
        mDatabaseReference.child("Messages").child(mFirebaseAuth.getCurrentUser().getUid() + "DiWxZsUAqjMrCRU4QJp8lwLzjXJ2").setValue("");

        listView = findViewById(R.id.chat_list);
        messages = new ArrayList<>();
        arrayAdapterFunc();
        Intent intent = new Intent(this,UserProfileActivity.class);
        intent.putExtra(USER_ID,mFirebaseAuth.getUid());
        startActivity(intent);
    }

    private void sendRequest(final String name) {

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();

                while (iterator.hasNext()) {
                    DataSnapshot data = iterator.next();
                    if (data.child("name").getValue().equals(name))
                        mId = data.getKey();
                }
                if(mId != null)
                    mDatabaseReference.child("Users").child(mId).child("friends").child(mFirebaseAuth.getUid()).setValue("pending");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        mDatabaseReference.child("Users").addListenerForSingleValueEvent(valueEventListener);
    }


    private void arrayAdapterFunc() {
        messages.add("Test Chat");
        arrayAdapter = new ArrayAdapter<>(getApplicationContext(),android.R.layout.simple_list_item_1 , messages);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int postion, long id) {
                Intent intent = new Intent(getApplicationContext() , ChatActivity.class);
                startActivity(intent);
            }
        });
    }
    private void sendToSignin() {
        if (mFirebaseAuth.getCurrentUser() == null){
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    /*
    private void testfriends(){
        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        String id = user.getUid();
        mFirebaseDatabase = FirebaseDatabase.getInstance();

        Map<String,Boolean> map = new HashMap<>();

        map.put("ahmed",true);
        map.put("khaled",false);
        map.put("yasser",true);
        mDatabaseReference.child("Users").child(id).child("friends");
        mDatabaseReference.child("Users").child(id).child("friends").setValue(map);

        FriendsHandler friendsHandler = new FriendsHandler();
        friendsHandler.addFriends(id,"your new friend");
    }

     */
}