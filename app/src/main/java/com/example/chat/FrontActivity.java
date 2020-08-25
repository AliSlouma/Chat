package com.example.chat;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class FrontActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String USER_ID = "com.example.chat.user_id";
    public static final String REQUEST_ID = "com.example.chat.request";
    public static final String FRIENDS_ID = "com.example.chat.friends";
    public static final String NOT_FRIENDS_ID = "com.example.chat.notFriends";
    public static final String PENDING_ID = "com.example.chat.pending";
    public static final String USER_PROFILE_ID = "com.example.chat.profileId";
    public static final String BLOCKED_ID = "com.example.chat.blockedId";
    public static final String BLOCKING_ID = "com.example.chat.blockingId";
    public static final String STATE = "state";
    public static final String PATH_FRIENDS = "friends";
    public static final String PATH_REQUESTS = "FriendRequests";
    public static final String BLOCKED_USERS = "Blocked users";
    private static final int CHECKS = 6;
    private static final String NAMES = "Names";
    private int cnt = 0;

    FirebaseAuth mFirebaseAuth;
    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference mDatabaseReference , forChats;
    ListView listView;
    ArrayAdapter arrayAdapter;
    List<String> mChats;
    private EditText mNameEditText;
    private Button mAddButton;
    private String mId;
    private ArrayAdapter mChatsAdapter;
    private AdapterView.OnItemClickListener mChatsListner;
    private ArrayAdapter mFriendsAdapter;
    private AdapterView.OnItemClickListener mFriendsListner;
    private TextView mTextNotificationItemCount;
    private int mNotificationscount;
    private AdapterView.OnItemClickListener mProfilesListener;
    private DatabaseReference root;
    public static UserInstance mMyUSer;
    private List<String> mFriends;
    private List<UserInstance> mRequest;
    private ArrayAdapter mRequestAdapter;
    private AdapterView.OnItemClickListener mRequestsListener;
    private ArrayAdapter mProfilesAdapter;
    private List<UserInstance> mProfiles;
    private Button mSearchButton;
    private EditText mSearchEditText;
    private String mResult;
    private HashMap<String,String > nameKeysMap;
    private FriendsRecyclerAdapter mFriendsRecyclerAdapter;
    private RecyclerView mFront_list;
    private LinearLayoutManager mFriendsLayoutManager;
    private List<String> mRequests;
    private RequestsRecyclerAdapter mRequestsRecyclerAdapter;
    private LinearLayoutManager mRequestsManager;
    private ProgressBar mProgressBar;
    private List<String> mTempFriends;

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
        forChats = FirebaseDatabase.getInstance().getReference();
        mFront_list = (RecyclerView)findViewById(R.id.chat_list);

        sendToSignin();

        //mDatabaseReference.child("Users").child(mFirebaseAuth.getUid()).child("friends").setValue("");
        // FriendsHandler friendsHandler = new FriendsHandler(this);
        // friendsHandler.addFriendsHandler(mFirebaseAuth.getUid());
      //  mDatabaseReference.child(PATH_FRIENDS).child(mFirebaseAuth.getUid()).child(mFirebaseAuth.getUid())
              //  .setValue();

      //  mDatabaseReference.child(PATH_FRIENDS).child(mFirebaseAuth.getUid()).child("zKAvAikUxUToVLGSbSD37jpRTj12")
              //  .setValue("");

        root = mDatabaseReference.child("Users");
        root.child(mFirebaseAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mMyUSer = dataSnapshot.getValue(UserInstance.class);
                mDatabaseReference.child(NAMES).child(mMyUSer.getName())
                        .setValue(mFirebaseAuth.getUid());
                mDatabaseReference.child(NAMES).child("lol")
                        .setValue("zKAvAikUxUToVLGSbSD37jpRTj12");

                mDatabaseReference.child(PATH_FRIENDS).child(mFirebaseAuth.getUid()).child(mFirebaseAuth.getUid())
                        .setValue(mMyUSer.getName());
                mDatabaseReference.child(PATH_FRIENDS).child(mFirebaseAuth.getUid()).child("zKAvAikUxUToVLGSbSD37jpRTj12")
                        .setValue("lol");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mProgressBar = (ProgressBar) findViewById(R.id.front_progress_bar);
    //    mSearchButton = (Button) findViewById(R.id.button_search);
        mSearchEditText = (EditText) findViewById(R.id.target_of_search);
        mSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String s = editable.toString();
                if(s.equals("")){
                    mFriendsRecyclerAdapter.mFriends = mFriends;
                    mFriendsRecyclerAdapter.notifyDataSetChanged();
                }else {
                    mTempFriends = new ArrayList<>();
                    mFriendsRecyclerAdapter.mFriends = mTempFriends;
                    mFriendsRecyclerAdapter.notifyDataSetChanged();
                    filterByName(s);
                }
            }
        });

       // nameKeysMap =null;
        nameKeysMap = new HashMap<>();
        mChats = new ArrayList<>();
       // setChatAdapter();
       initializeFriendsAdapter();
       showFriends();
       initializeRequestsAdapter();
    }

    private void filterByName(final String name){
        mDatabaseReference.child(PATH_FRIENDS).child(mFirebaseAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterator iterator = dataSnapshot.getChildren().iterator();
                while (iterator.hasNext()){
                    DataSnapshot dataSnapshot1 = (DataSnapshot)iterator.next();
                    if(((String)dataSnapshot1.getValue()).startsWith(name)) {
                       mTempFriends.add(dataSnapshot1.getKey());
                       mFriendsRecyclerAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void filterFriendsByName(final String name){
        mDatabaseReference.child(NAMES).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterator iterator = dataSnapshot.getChildren().iterator();
                while (iterator.hasNext()){
                    DataSnapshot dataSnapshot1 = (DataSnapshot)iterator.next();
                    if(dataSnapshot1.getKey().startsWith(name)) {

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

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
                        final String friendKey = (String) ((DataSnapshot)(iterator.next())).getKey();
                        root.child(friendKey).addValueEventListener(new ValueEventListener() {

                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    String friend = (String) dataSnapshot.child("name").getValue();
                                    mChats.add(friend);
                                    nameKeysMap.put(friend,friendKey);
                                    mChatsAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

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
        listView.setAdapter(mChatsAdapter);
        mChatsListner = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int postion, long id) {
                Intent intent = new Intent(getApplicationContext() , ChatActivity.class);
                String receiver = (String) adapterView.getItemAtPosition(postion);
                intent.putExtra("receiverID",nameKeysMap.get(receiver));
                startActivity(intent);
            }
        };
        showChats();
//        initializeFriendsAdapter();
//        initializeProfileAdapter();
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
        mFriendsLayoutManager = new LinearLayoutManager(this);
        mFriendsRecyclerAdapter = new FriendsRecyclerAdapter(mFriends,this);
        mDatabaseReference.child(PATH_FRIENDS).child(mFirebaseAuth.getUid()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                mProgressBar.setVisibility(View.VISIBLE);
                String user_id = (String)dataSnapshot.getKey();
                mFriends.add(user_id);
                if(!mSearchEditText.getText().toString().equals(""))
                    mTempFriends.add(user_id);
                mFriendsRecyclerAdapter.notifyDataSetChanged();
            }


            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                final String user_id = (String) dataSnapshot.getKey();
                mFriends.remove(user_id);
                if(!mSearchEditText.getText().equals(""))
                    mTempFriends.remove(user_id);
                mFriendsRecyclerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        mDatabaseReference.child(PATH_FRIENDS).child(mFirebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initializeRequestsAdapter(){
        mRequests = new ArrayList<>();
        mRequestsRecyclerAdapter = new RequestsRecyclerAdapter(mRequests,this);
        mRequestsManager = new LinearLayoutManager(this);
        mDatabaseReference.child(PATH_REQUESTS).child(mFirebaseAuth.getUid()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                mProgressBar.setVisibility(View.VISIBLE);
                String user_id = (String)dataSnapshot.getKey();
                mRequests.add(user_id);
                mRequestsRecyclerAdapter.notifyDataSetChanged();
            }


            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                final String user_id = (String) dataSnapshot.getKey();
                mRequests.remove(user_id);
                mRequestsRecyclerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        mDatabaseReference.child(PATH_REQUESTS).child(mFirebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initializeProfileAdapter(){
        mProfiles = new ArrayList<>();
        mProfilesAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, mProfiles);
        mProfilesListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                cnt = 0;
                Intent intent = new Intent(getApplicationContext(), UserProfileActivity.class);
                intent.putExtra(USER_ID, mProfiles.get(i).getUId());
                searchState(mProfiles.get(i).getUId(),intent);
            }
        };
    }

    private void searchState(final String userId,final Intent intent) {
        mDatabaseReference.child(PATH_FRIENDS).child(mFirebaseAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(userId).exists())
                    mResult = FRIENDS_ID;
                cnt++;
                if(cnt == CHECKS) {
                    intent.putExtra(STATE, mResult);
                    startActivity(intent);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mDatabaseReference.child(PATH_REQUESTS).child(mFirebaseAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(userId).exists())
                    mResult = REQUEST_ID;
                cnt++;
                if(cnt == CHECKS) {
                    intent.putExtra(STATE, mResult);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mDatabaseReference.child(PATH_REQUESTS).child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(mFirebaseAuth.getUid()).exists())
                    mResult = PENDING_ID;
                cnt++;
                if(cnt == CHECKS) {
                    intent.putExtra(STATE, mResult);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mDatabaseReference.child(BLOCKED_USERS).child(mFirebaseAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(userId).exists()) {
                    mResult = BLOCKING_ID;
                }
                cnt++;
                if(cnt == CHECKS) {
                    intent.putExtra(STATE, mResult);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mDatabaseReference.child(BLOCKED_USERS).child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(mFirebaseAuth.getUid()).exists()) {
                    mResult = BLOCKED_ID;
                }
                cnt++;
                if(cnt == CHECKS) {
                    intent.putExtra(STATE, mResult);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        mResult = NOT_FRIENDS_ID;
        cnt++;
        if(cnt == CHECKS) {
            intent.putExtra(STATE, mResult);
            startActivity(intent);
        }
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.nav_chats){
            showChats();
        }else if(id == R.id.nav_friends){
            showFriends();
            mSearchEditText.setVisibility(View.VISIBLE);
        }else if(id == R.id.nav_search_profiles){
             showProfiles();
         }else if(id == R.id.nav_requests){
              showRequests();
              mSearchEditText.setVisibility(View.INVISIBLE);
      } else if(id == R.id.nav_profile){
            Intent intent = new Intent(this,UserProfileActivity.class);
            intent.putExtra(USER_ID,mFirebaseAuth.getUid());
            intent.putExtra(STATE,USER_PROFILE_ID);
            startActivity(intent);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
       // item.setChecked(true);
        return true;
    }

    private void showFriends() {
        mFront_list.setLayoutManager(mFriendsLayoutManager);
        mFront_list.setAdapter(mFriendsRecyclerAdapter);
    }

    private void showChats() {

        listView.setAdapter(mChatsAdapter);
        listView.setOnItemClickListener(mChatsListner);

    }
    private void showRequests() {

       mFront_list.setLayoutManager(mRequestsManager);
       mFront_list.setAdapter(mRequestsRecyclerAdapter);

    }
    private void showProfiles() {
        listView.setAdapter(mProfilesAdapter);
        listView.setOnItemClickListener(mProfilesListener);
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String name = mSearchEditText.getText().toString();
                root.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        mProfiles.clear();
                        Iterator<DataSnapshot> itr = dataSnapshot.getChildren().iterator();
                        while (itr.hasNext()){
                            dataSnapshot = itr.next();
                            UserInstance user = dataSnapshot.getValue(UserInstance.class);
                            if(user.getName().matches(name + "\\w*")){
                               mProfiles.add(user);
                               mProfilesAdapter.notifyDataSetChanged();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
    }
}