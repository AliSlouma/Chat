package com.example.chat.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chat.R;
import com.example.chat.user.UserInstance;
import com.example.chat.user.UserProfileActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import static com.example.chat.FirebaseUtil.sDatabaseReference;
import static com.example.chat.FirebaseUtil.sFirebaseAuth;
import static com.example.chat.FrontActivity.*;

public class UsersRecyclerAdapter extends RecyclerView.Adapter<UsersRecyclerAdapter.UserViewHolder>{
    public List<UserInstance> mUsers;
    Context mContext;
    String mResult;
    private int mCnt;

    public UsersRecyclerAdapter(List<UserInstance> users , Context context){
        this.mContext = context;
        this.mUsers = users;
    }
    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.profile_view,parent,false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final UserViewHolder holder, int position) {
        UserInstance userInstance = mUsers.get(position);
        holder.name.setText(userInstance.getName());
        holder.status.setText(userInstance.getStatus());
        Glide.with(mContext).load(userInstance.getImageUri()).into(holder.image);
        holder.user_id = userInstance.getUId();
    }

    @Override
    public int getItemCount() {
        return (mUsers != null) ? mUsers.size() : 0;
    }

    class UserViewHolder extends RecyclerView.ViewHolder{
        TextView name;
        TextView status;
        ImageView image;
        String user_id;
        Button acceptBtn;
        Button rejectBtn;
        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.profile_name);
            status = itemView.findViewById(R.id.profile_status);
            image = itemView.findViewById(R.id.receiver_chat_photo);
            acceptBtn = (Button) itemView.findViewById(R.id.button_accept_request);
            rejectBtn = (Button) itemView.findViewById(R.id.button_reject_request);
            acceptBtn.setVisibility(Button.INVISIBLE);
            rejectBtn.setVisibility(Button.INVISIBLE);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sDatabaseReference.child("Users").child(user_id).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){
                                Intent intent = new Intent(mContext , UserProfileActivity.class);
                                intent.putExtra(USER_ID, user_id);
                                searchState(user_id,intent);
                            }else{
                                Toast.makeText(mContext,"Users deleted himself",Toast.LENGTH_SHORT).show();
                                delete(user_id);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }
            });
        }
    }

    private void delete(String user_id) {
        for(int i=0 ; i<mUsers.size() ; i++){
            if(mUsers.get(i).getUId().equals(user_id)) {
                mUsers.remove(i);
                break;
            }
        }
        notifyDataSetChanged();
    }

    private void searchState(final String userId,final Intent intent) {
        mCnt = 0;
        sDatabaseReference.child(PATH_FRIENDS).child(sFirebaseAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(userId).exists())
                    mResult = FRIENDS_ID;
                mCnt++;
                if(mCnt == CHECKS) {
                    intent.putExtra(STATE, mResult);
                    mContext.startActivity(intent);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        sDatabaseReference.child(PATH_REQUESTS).child(sFirebaseAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(userId).exists())
                    mResult = REQUEST_ID;
                mCnt++;
                if(mCnt == CHECKS) {
                    intent.putExtra(STATE, mResult);
                    mContext.startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        sDatabaseReference.child(PATH_REQUESTS).child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(sFirebaseAuth.getUid()).exists())
                    mResult = PENDING_ID;
                mCnt++;
                if(mCnt == CHECKS) {
                    intent.putExtra(STATE, mResult);
                    mContext.startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        sDatabaseReference.child(BLOCKED_USERS).child(sFirebaseAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(userId).exists()) {
                    mResult = BLOCKING_ID;
                }
                mCnt++;
                if(mCnt == CHECKS) {
                    intent.putExtra(STATE, mResult);
                    mContext.startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        sDatabaseReference.child(BLOCKED_USERS).child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(sFirebaseAuth.getUid()).exists()) {
                    mResult = BLOCKED_ID;
                }
                mCnt++;
                if(mCnt == CHECKS) {
                    intent.putExtra(STATE, mResult);
                    mContext.startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        mResult = NOT_FRIENDS_ID;
        mCnt++;
        if(mCnt == CHECKS) {
            intent.putExtra(STATE, mResult);
            mContext.startActivity(intent);
        }
    }
}
