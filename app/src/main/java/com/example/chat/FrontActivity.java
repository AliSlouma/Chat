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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class FrontActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String USER_ID = "com.example.chat.user_id";
    public static final String REQUEST_ID = "com.example.chat.request";
    public static final String FRIENDS_ID = "com.example.chat.friends";
    public static final String NOT_FRIENDS_ID = "com.example.chat.notFriends";
    public static final String PENDING_ID = "com.example.chat.pending";
    public static final String USER_PROFILE_ID = "com.example.chat.profileId";
    public static final String STATE = "state";
    public static final String PATH_FRIENDS = "friends";
    public static final String PATH_REQUESTS = "FriendRequests";
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
    private List<UserInstance> mFriends;
    private List<UserInstance> mRequest;
    private ArrayAdapter mRequestAdapter;
    private AdapterView.OnItemClickListener mRequestsListener;
    private ArrayAdapter mProfilesAdapter;
    private List<UserInstance> mProfiles;
    private Button mSearchButton;
    private EditText mSearchEditText;
    private String mResult;
    private HashMap<String,String > nameKeysMap;
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
        listView = findViewById(R.id.chat_list);

        sendToSignin();

        //mDatabaseReference.child("Users").child(mFirebaseAuth.getUid()).child("friends").setValue("");
        // FriendsHandler friendsHandler = new FriendsHandler(this);
        // friendsHandler.addFriendsHandler(mFirebaseAuth.getUid());
        mDatabaseReference.child(PATH_REQUESTS).child(mFirebaseAuth.getUid()).child(mFirebaseAuth.getUid())
                .setValue("");
        mDatabaseReference.child(PATH_FRIENDS).child(mFirebaseAuth.getUid()).child(mFirebaseAuth.getUid())
                .setValue("");


        mSearchButton = (Button) findViewById(R.id.button_search);
        mSearchEditText = (EditText) findViewById(R.id.target_of_search);
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
       // nameKeysMap =null;
        nameKeysMap = new HashMap<>();
        mChats = new ArrayList<>();
        setChatAdapter();

        arrayAdapterFunc();
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
        initializeFriendsAdapter();
        initializeProfileAdapter();
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
                intent.putExtra(STATE,FRIENDS_ID);
                startActivity(intent);
            }
        };
        mDatabaseReference.child(PATH_FRIENDS).child(mFirebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mFriends.clear();
                Iterable<DataSnapshot> iterable = dataSnapshot.getChildren();
                Iterator<DataSnapshot> iterator = iterable.iterator();
                while (iterator.hasNext()){
                    DataSnapshot data = iterator.next();
                    String id = data.getKey();
                    root.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            mFriends.add(dataSnapshot.getValue(UserInstance.class));
                            mFriendsAdapter.notifyDataSetChanged();
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

    private void initializeRequestsAdapter(){

        mRequest = new ArrayList<>();
        mRequestAdapter = new ArrayAdapter<>(getApplicationContext(),android.R.layout.simple_list_item_1 , mRequest);
        mRequestsListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int postion, long id) {
                Intent intent = new Intent(getApplicationContext() , UserProfileActivity.class);
                intent.putExtra(USER_ID, mRequest.get(postion).getUId());
                intent.putExtra(STATE,REQUEST_ID);
                startActivity(intent);
            }
        };

        mDatabaseReference.child(PATH_REQUESTS).child(mFirebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mRequest.clear();
                Iterable<DataSnapshot> iterable = dataSnapshot.getChildren();
                Iterator<DataSnapshot> iterator = iterable.iterator();
                while (iterator.hasNext()){
                    DataSnapshot data = iterator.next();
                    String id = data.getKey();
                    if(data.getValue() != "done") {
                        root.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                mRequest.add(dataSnapshot.getValue(UserInstance.class));
                                mRequestAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }else{
                        mDatabaseReference.child("FriendRequests").child(mFirebaseAuth.getUid()).child(id).removeValue();
                        mRequestAdapter.notifyDataSetChanged();
                    }
                }

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
                if(cnt == 4) {
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
                if(cnt == 4) {
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
                if(cnt == 4) {
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
        if(cnt == 4) {
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
        }else if(id == R.id.nav_search_profiles){
             showProfiles();
         }else if(id == R.id.nav_requests){
              showRequests();
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
        listView.setAdapter(mFriendsAdapter);
        listView.setOnItemClickListener(mFriendsListner);
    }

    private void showChats() {

        listView.setAdapter(mChatsAdapter);
        listView.setOnItemClickListener(mChatsListner);

    }
    private void showRequests() {

        listView.setAdapter(mRequestAdapter);
       // mRequestAdapter.notifyDataSetChanged();
        listView.setOnItemClickListener(mRequestsListener);

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