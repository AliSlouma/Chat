package com.example.chat;

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
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
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
    ArrayList<String> mChats;
    private EditText mNameEditText;
    private Button mAddButton;
    private String mId;
    private ArrayAdapter mChatsAdapter;
    private AdapterView.OnItemClickListener mChatsListner;
    private ArrayAdapter mFriendsAdapter;
    private AdapterView.OnItemClickListener mFriendsListner;
    private TextView mTextNotificationItemCount;
    private int mNotificationscount;
    private ArrayAdapter mProfilesAdapet;
    private Map<String,UserInstance> mUsers;
    private AdapterView.OnItemClickListener mProfilesListener;
    private DatabaseReference root;
    public static UserInstance mMyUSer;
    private List<UserInstance> mFriends;
    private List<UserInstance> mRequest;
    private ArrayAdapter mRequestAdapter;
    private AdapterView.OnItemClickListener mRequestsListener;
    private ArrayAdapter mProfilesAdapter;
    private List<UserInstance> mProfiles;
    // private AppBarConfiguration mAppBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_front);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();
        listView = findViewById(R.id.chat_list);

        sendToSignin();

        //mDatabaseReference.child("Users").child(mFirebaseAuth.getUid()).child("friends").setValue("");
       // FriendsHandler friendsHandler = new FriendsHandler(this);
       // friendsHandler.addFriendsHandler(mFirebaseAuth.getUid());
        mDatabaseReference.child("FriendRequests").child(mFirebaseAuth.getUid()).child(mFirebaseAuth.getUid())
        .setValue("");
        mDatabaseReference.child("friends").child(mFirebaseAuth.getUid()).child(mFirebaseAuth.getUid())
                .setValue("");



        root = mDatabaseReference.child("Users");
        root.child(mFirebaseAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mMyUSer = dataSnapshot.getValue(UserInstance.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mChats = new ArrayList<>();
        setChatAdapter();
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


    private void setChatAdapter() {
        mDatabaseReference.child("friends").child(mFirebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Iterator iterator = dataSnapshot.getChildren().iterator();
                    while (iterator.hasNext()){
                        String friend = (String) ((DataSnapshot)(iterator.next())).getKey();
                        mChats.add(friend);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }




    private void arrayAdapterFunc() {
        //mChats.add("Test Chat");
        mChatsAdapter = new ArrayAdapter<>(getApplicationContext(),android.R.layout.simple_list_item_1 , mChats);
       //listView.setAdapter(arrayAdapter);
        mChatsListner = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int postion, long id) {
                Intent intent = new Intent(getApplicationContext() , ChatActivity.class);
                String receiverID = (String) adapterView.getItemAtPosition(postion);
                intent.putExtra("receiverID",receiverID);
                startActivity(intent);
            }
        };
        showChats();
        initializeFriendsAdapter();
    //    initializeProfileAdapter();
        initializeRequestsAdapter();
    }


    private void sendToSignin() {
        if (mFirebaseAuth.getCurrentUser() == null){
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }
    private void initializeFriendsAdapter(){

        mFriends = new ArrayList<>();
        mFriendsAdapter = new ArrayAdapter<>(getApplicationContext(),android.R.layout.simple_list_item_1 , mFriends);
        mFriendsListner = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int postion, long id) {
                Intent intent = new Intent(getApplicationContext() , UserProfileActivity.class);
                intent.putExtra(USER_ID, mFriends.get(postion).getUId());
                startActivity(intent);
            }
        };
        mDatabaseReference.child("friends").child(mFirebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Iterable<DataSnapshot> iterable = dataSnapshot.getChildren();
                Iterator<DataSnapshot> iterator = iterable.iterator();
                while (iterator.hasNext()){
                    DataSnapshot data = iterator.next();
                    String id = data.getKey();
                    root.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            mFriends.add(dataSnapshot.getValue(UserInstance.class));
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
                mFriendsAdapter.notifyDataSetChanged();
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void initializeRequestsAdapter(){

        mRequest = new ArrayList<>();
        mRequestAdapter = new ArrayAdapter<>(getApplicationContext(),android.R.layout.simple_list_item_1 , mRequest);
        mRequestsListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int postion, long id) {
                Intent intent = new Intent(getApplicationContext() , UserProfileActivity.class);
                intent.putExtra(USER_ID, mFriends.get(postion).getUId());
                startActivity(intent);
            }
        };

        mDatabaseReference.child("FriendRequests").child(mFirebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> iterable = dataSnapshot.getChildren();
                Iterator<DataSnapshot> iterator = iterable.iterator();
                while (iterator.hasNext()){
                    DataSnapshot data = iterator.next();
                    String id = data.getKey();
                    root.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            mRequest.add(dataSnapshot.getValue(UserInstance.class));
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
/*
    private void initializeProfileAdapter(){
        mUsers = new HashMap<>();
        mProfiles = new ArrayList<>();
        mProfilesAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, mProfiles);
        mProfilesListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext() , UserProfileActivity.class);
                intent.putExtra(USER_ID,mProfiles.get(i).getUId());
                startActivity(intent);
            }
        };

        mDatabaseReference.child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> iterable = dataSnapshot.getChildren();
                Iterator<DataSnapshot> iterator = iterable.iterator();
                while (iterator.hasNext()) {
                   DataSnapshot dataSnapshotforItem = iterator.next();
                  //  UserInstance userInstance = dataSnapshot.getValue();
                   // mProfiles.add(userInstance);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

 */

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.nav_chats){
            showChats();
        }else if(id == R.id.nav_friends){
            showFriends();
        }else if(id == R.id.nav_profiles){
        showProfiles();
    }else if(id == R.id.nav_requests){
        showRequests();
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

    }
    private void showRequests() {

        listView.setAdapter(mRequestAdapter);
        listView.setOnItemClickListener(mRequestsListener);

    }
    private void showProfiles() {
        listView.setAdapter(mProfilesAdapter);
        listView.setOnItemClickListener(mProfilesListener);
    }





}