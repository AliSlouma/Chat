package com.example.chat;

import android.Manifest;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import android.widget.Toast;


import com.example.chat.adapters.RequestsRecyclerAdapter;
import com.example.chat.adapters.UsersRecyclerAdapter;
import com.example.chat.chats.ChatAdapter;
import com.example.chat.chats.ChatInstance;
import com.example.chat.adapters.FriendsRecyclerAdapter;
import com.example.chat.login.LoginActivity;
import com.example.chat.user.UserInstance;
import com.example.chat.user.UserProfileActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.example.chat.FirebaseUtil.sDatabaseReference;

public class FrontActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, LoaderManager.LoaderCallbacks<Cursor> {

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
    public  static final int CHECKS = 6;
    private static final String NAMES = "Names";
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
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
    private List<UserInstance> mProfiles;
    private Button mSearchButton;
    private EditText mSearchEditText;
    private String mResult;
    private HashMap<String,String > nameKeysMap;
    private FriendsRecyclerAdapter mFriendsRecyclerAdapter;
    private RecyclerView mFront_list;
    private LinearLayoutManager mFriendsLayoutManager;
    private List<UserInstance> mRequests;
    private RequestsRecyclerAdapter mRequestsRecyclerAdapter;
    private LinearLayoutManager mRequestsManager;
    private ProgressBar mProgressBar;
    private List<UserInstance> mTempFriends;
    final private String FCM_API = "https://fcm.googleapis.com/fcm/send";
    final private String serverKey = "key=" + "AAAAgnprc9w:APA91bE-f5o3ju0LZFvw_HrmPRgm6fBrwEs9jwQ4tBukzfh8sWv4ktm8EHwfNfl7GbJl2A7sYbl6R7tlZmCPJn0fEX8pGJqWyqK87Pe-bZYL0qIutr6LW57Z8lUhqDh9W8C7LAg8q1bc";
    final private String contentType = "application/json";
    final String TAG = "NOTIFICATION TAG";
    private String mTopic;
    private UsersRecyclerAdapter mProfilesAdapter;
    private Cursor mCursor;
    private Set<String> mContacts;
    boolean done = false;
    private List<UserInstance> mTempUsers;
    private boolean friendsInitialized = false;
    private boolean usersInitialized = false;
    private boolean chatInitialized = false;
    private boolean requestsInitialized = false;
    // private AppBarConfiguration mAppBarConfiguration;
    private RecyclerView chatListRecyclerView;
    private LinearLayoutManager linearLayoutManager;
    private ChatAdapter chatAdapter;
    private final List<ChatInstance> chatInstanceList = new ArrayList<>();
    private ChatInstance chatInstance;
    DatabaseReference chatListRef;

    private DatabaseReference userState;
    public static boolean onpause = false;
    private LinearLayoutManager profilesManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mFriends = new ArrayList<>();
        mFriendsLayoutManager = new LinearLayoutManager(this);
        mFriendsRecyclerAdapter = new FriendsRecyclerAdapter(mFriends,this);
        mProfiles = new ArrayList<>();
        mProfilesAdapter = new UsersRecyclerAdapter(mProfiles,this);
        profilesManager = new LinearLayoutManager (this);
        mContacts = new HashSet<>();
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
        sendToSignin();
        if(done)
            return;
        forChats = FirebaseDatabase.getInstance().getReference();
        mFront_list = (RecyclerView)findViewById(R.id.chat_list);


        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
//To do//
                            return;
                        }

// Get the Instance ID token//
                        String token = task.getResult().getToken();
                        Log.i("token", token);

                    }
                });

        root = mDatabaseReference.child("Users");
        root.child(mFirebaseAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mMyUSer = dataSnapshot.getValue(UserInstance.class);
                mDatabaseReference.child(NAMES).child(mMyUSer.getName())
                        .setValue(mFirebaseAuth.getUid());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        root.child("zKAvAikUxUToVLGSbSD37jpRTj12").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserInstance userInstance = dataSnapshot.getValue(UserInstance.class);
                userInstance.setNumber("+201125427841");
                root.child("zKAvAikUxUToVLGSbSD37jpRTj12").setValue(userInstance);
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
                if (mFront_list.getAdapter() == mFriendsRecyclerAdapter) {
                    String s = editable.toString();
                    if (s.equals("")) {
                        mFriendsRecyclerAdapter.mFriends = mFriends;
                        mFriendsRecyclerAdapter.notifyDataSetChanged();
                    } else {
                        mTempFriends = new ArrayList<>();
                        mFriendsRecyclerAdapter.mFriends = mTempFriends;
                        mFriendsRecyclerAdapter.notifyDataSetChanged();
                        filterByName(s);
                    }
                } else {
                    String s = editable.toString();
                    if(s.equals("")){
                        mProfilesAdapter.mUsers = mProfiles;
                        mProfilesAdapter.notifyDataSetChanged();
                    }else {
                        mTempUsers = new ArrayList<>();
                        mProfilesAdapter.mUsers = mTempUsers;
                        filterUsersByName(s);
                    }
                }
            }
        });


        showChats();
        String token = FirebaseInstanceId.getInstance().getToken();
        FirebaseMessaging.getInstance().subscribeToTopic(mFirebaseAuth.getUid());
        showContacts();
    }
    protected void onStart() {
        super.onStart();
        onpause = false;
        if (mFirebaseAuth.getCurrentUser() == null){
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }else{
            userState.child("userState").setValue("online");


        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(!onpause && mFirebaseAuth.getCurrentUser() != null){

            Calendar calendar = Calendar.getInstance();

            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");

            String currentTime = timeFormat.format(calendar.getTime());

            userState.child("userState").setValue(currentTime);

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mFirebaseAuth.getCurrentUser() != null){

            Calendar calendar = Calendar.getInstance();

            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");

            String currentTime = timeFormat.format(calendar.getTime());

            userState.child("userState").setValue(currentTime);

        }
    }



    private void filterByName(String name){
        for(UserInstance userInstance : mFriends) {
            if (userInstance.getName().startsWith(name))
                mTempFriends.add(userInstance);
        }
        mFriendsRecyclerAdapter.notifyDataSetChanged();
    }

    private void filterUsersByName(String name){
        for(UserInstance userInstance : mProfiles) {
            if (userInstance.getName().startsWith(name)) {
                if (userInstance.getNumber() != null && mContacts.contains(userInstance.getNumber()))
                    mTempUsers.add(0, userInstance);
                else mTempUsers.add(userInstance);
            }
        }
        mProfilesAdapter.notifyDataSetChanged();
    }



    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.front, menu);
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






    private void sendToSignin() {
        if (mFirebaseAuth.getCurrentUser() == null){
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            done = true;
        }
    }




    private void initializeFriendsAdapter(){

        mDatabaseReference.child(PATH_FRIENDS).child(mFirebaseAuth.getUid()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()) {
                    mProgressBar.setVisibility(View.VISIBLE);
                    String user_id = (String) dataSnapshot.getKey();
                    sDatabaseReference.child("Users").child(user_id).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                UserInstance userInstance = dataSnapshot.getValue(UserInstance.class);
                                mFriends.add(userInstance);
                                if (!mSearchEditText.getText().toString().equals(""))
                                    mTempFriends.add(userInstance);
                                mFriendsRecyclerAdapter.notifyDataSetChanged();
                            }else{
                                mDatabaseReference.child(PATH_FRIENDS).child(mFirebaseAuth.getUid()).child(dataSnapshot.getKey()).removeValue();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }



            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                final String user_id = (String) dataSnapshot.getKey();
                deleteFromList(user_id,mFriends);
                if(!mSearchEditText.getText().toString().equals(""))
                    deleteFromList(user_id,mTempFriends);
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
                mProgressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void deleteFromList(String user_id, List<UserInstance> friends) {
        for(int i=0 ; i<friends.size() ; i++){
            if(friends.get(i).getUId().equals(user_id)) {
                friends.remove(i);
                break;
            }
        }
    }
    private void initializeRequestsAdapter(){
        mRequests = new ArrayList<>();
        mRequestsRecyclerAdapter = new RequestsRecyclerAdapter(mRequests,this);
        mRequestsManager = new LinearLayoutManager(this);
        mDatabaseReference.child(PATH_REQUESTS).child(mFirebaseAuth.getUid()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists()){
                    mProgressBar.setVisibility(View.VISIBLE);
                    String user_id = (String) dataSnapshot.getKey();
                    sDatabaseReference.child("Users").child(user_id).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                UserInstance userInstance = dataSnapshot.getValue(UserInstance.class);
                                mRequests.add(userInstance);
                                mRequestsRecyclerAdapter.notifyDataSetChanged();
                            }
                            else{
                                mDatabaseReference.child(PATH_REQUESTS).child(mFirebaseAuth.getUid()).child(dataSnapshot.getKey()).removeValue();
                            }
                        }


                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }


            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                final String user_id = (String) dataSnapshot.getKey();
                deleteFromList(user_id,mRequests);
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
                mProgressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initializeProfileAdapter(){

        sDatabaseReference.child("Users").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists()) {
                    mProgressBar.setVisibility(View.VISIBLE);
                    UserInstance userInstance = dataSnapshot.getValue(UserInstance.class);
                    if(userInstance.getNumber() != null && mContacts.contains(userInstance.getNumber()))
                        mProfiles.add(0,userInstance);
                    else
                        mProfiles.add(userInstance);
                    mProfilesAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                String id =(String) dataSnapshot.getKey();
                deleteFromList(id,mProfiles);
                mProfilesAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        sDatabaseReference.child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mProgressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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
        }else if(id == R.id.nav_search_profiles){
            showProfiles();
        }else if(id == R.id.nav_delete_account){
            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            // Get auth credentials from the user for re-authentication. The example below shows
            // email and password credentials but there are multiple possible providers,
            // such as GoogleAuthProvider or FacebookAuthProvider.
            AuthCredential credential = EmailAuthProvider
                    .getCredential("asdasd@gmail.com", "123456");

            // Prompt the user to re-provide their sign-in credentials
            user.reauthenticate(credential)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            user.delete()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Intent intent = new Intent(getApplicationContext() , LoginActivity.class);
                                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid());
                                                ref.removeValue();
                                                startActivity(intent);
                                            }
                                        }
                                    });

                        }
                    });
        }else if(id == R.id.nav_logout){
            mFirebaseAuth.signOut();
            finish();
            Intent intent = new Intent(getBaseContext(),LoginActivity.class);
            Toast.makeText(getBaseContext(),"Signed out!",Toast.LENGTH_SHORT).show();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        // item.setChecked(true);
        return true;
    }

    private void showFriends() {
        if(!friendsInitialized) {
            friendsInitialized = true;
            initializeFriendsAdapter();
        }
        mSearchEditText.setVisibility(View.VISIBLE);
        mFront_list.setLayoutManager(mFriendsLayoutManager);
        mFront_list.setAdapter(mFriendsRecyclerAdapter);
    }

    private void showChats() {
        if(!chatInitialized){
            chatInitialized = true;
            initializeChatAdapter();
        }
        mSearchEditText.setVisibility(View.VISIBLE);
        mFront_list.setAdapter(chatAdapter);
        mFront_list.setLayoutManager(linearLayoutManager);
    }

    private void initializeChatAdapter() {
        chatInstance = new ChatInstance();
        chatAdapter = new ChatAdapter(chatInstanceList,this);
        chatListRecyclerView = findViewById(R.id.chat_list);
        linearLayoutManager = new LinearLayoutManager(this);

        chatListRef =FirebaseDatabase.getInstance().getReference().child("ChatsList").child(mFirebaseAuth.getUid());
        userState = FirebaseDatabase.getInstance().getReference().child("Users").child(mFirebaseAuth.getUid());

        if (mFirebaseAuth.getCurrentUser() == null){
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }else{
            userState.child("userState").setValue("online");

        }
        chatListRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                mProgressBar.setVisibility(View.VISIBLE);
                chatInstanceList.add(dataSnapshot.getValue(ChatInstance.class));
                chatAdapter.notifyDataSetChanged();

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                ChatInstance c = dataSnapshot.getValue(ChatInstance.class);
                for(int i =0;i<chatInstanceList.size();i++){
                    if(c.getReceiverUID().equals(chatInstanceList.get(i).getReceiverUID()))
                        chatInstanceList.set(i,c);
                }
                chatAdapter.notifyDataSetChanged();

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
        chatListRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mProgressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void showRequests() {
        mSearchEditText.setVisibility(View.INVISIBLE);
        if(!requestsInitialized){
            requestsInitialized = true;
            initializeRequestsAdapter();
        }
        mFront_list.setLayoutManager(mRequestsManager);
        mFront_list.setAdapter(mRequestsRecyclerAdapter);

    }
    private void showProfiles(){


        mSearchEditText.setVisibility(View.VISIBLE);
        if(!usersInitialized){
            usersInitialized = true;
            initializeProfileAdapter();
        }
        mFront_list.setLayoutManager(profilesManager);
        mFront_list.setAdapter(mProfilesAdapter);

    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_profile){
            Intent intent = new Intent(this, UserProfileActivity.class);
            intent.putExtra(USER_ID,mFirebaseAuth.getUid());
            intent.putExtra(STATE,USER_PROFILE_ID);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                showContacts();
            } else {
                Toast.makeText(this, "Until you grant the permission, we canot display the names", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void showContacts() {
        // Check the SDK version and whether the permission is already granted or not.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        } else {
            // Android version is lesser than 6.0 or the permission is already granted.
            //  mContacts = getContactNames();
            getLoaderManager().initLoader(0,null,this);
        }

    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        if(i == 0) {
            return new CursorLoader(this, ContactsContract.Contacts.CONTENT_URI, null,
                    null, null, null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        this.mCursor = cursor;
        if (mCursor.getCount() > 0) {
            while (mCursor.moveToNext()) {
                String id = mCursor.getString(mCursor.getColumnIndex(ContactsContract.Contacts._ID));
                String name = mCursor.getString(mCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                Log.i("Names", name);
                if (Integer.parseInt(mCursor.getString(mCursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0)
                {
                    // Query phone here. Covered next
                    Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ id,null, null);
                    while (phones.moveToNext()) {
                        String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        mContacts.add(phoneNumber);
                    }
                    phones.close();
                }

            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursor.close();

    }
}
