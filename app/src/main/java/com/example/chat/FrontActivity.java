package com.example.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class FrontActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

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
    private ArrayAdapter mChatsAdapter;
    private AdapterView.OnItemClickListener mChatsListner;
    private ArrayAdapter mFriendsAdapter;
    private AdapterView.OnItemClickListener mFriendsListner;
    private TextView mTextNotificationItemCount;
    private int mNotificationscount;
    // private AppBarConfiguration mAppBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_front);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

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

        navigationView.setNavigationItemSelectedListener(this);
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.front, menu);
        final MenuItem menuItem = menu.findItem(R.id.front_notifications);
        View actionView = menuItem.getActionView();
        mTextNotificationItemCount = (TextView) actionView.findViewById(R.id.cart_badge);
        mNotificationscount = 10;
        setupBadge();
        return true;
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

        mChatsAdapter = new ArrayAdapter<>(getApplicationContext(),android.R.layout.simple_list_item_1 , messages);
       //listView.setAdapter(arrayAdapter);
        mChatsListner = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int postion, long id) {
                Intent intent = new Intent(getApplicationContext() , ChatActivity.class);
                startActivity(intent);
            }
        };
        mDatabaseReference.child("Users").child(mFirebaseAuth.getUid()).child("friends").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final List<String> friends = new ArrayList<>();
                Map<String,String> map = (Map<String, String>) dataSnapshot.getValue();
                for(Map.Entry<String,String> entry : map.entrySet()){
                    if(entry.getValue().equals("yes"))
                            friends.add(entry.getKey());
                }
                mFriendsAdapter = new ArrayAdapter<>(getApplicationContext(),android.R.layout.simple_list_item_1 , friends);
                mFriendsListner = new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int postion, long id) {
                        Intent intent = new Intent(getApplicationContext() , UserProfileActivity.class);
                        intent.putExtra(USER_ID,friends.get(postion));
                        startActivity(intent);
                    }
                };
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        showChats();

    }
    private void sendToSignin() {
        if (mFirebaseAuth.getCurrentUser() == null){
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.nav_chats){
            showChats();
        }else if(id == R.id.nav_friends){
            showFriends();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
       // item.setChecked(true);
        return true;
    }

    private void showFriends() {
        listView.setAdapter(mFriendsAdapter);
        listView.setOnItemClickListener(mFriendsListner);
    }

    private void showChats() {

        listView.setAdapter(mChatsAdapter);
        listView.setOnItemClickListener(mChatsListner);
        createPopMenu();
    }


    public  void createPopMenu() {
        //PopupMenu menu = new PopupMenu(this, menuItem);
    }
}