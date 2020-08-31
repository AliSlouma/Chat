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
import com.bumptech.glide.request.RequestOptions;
import com.example.chat.FrontActivity;
import com.example.chat.R;
import com.example.chat.user.UserInstance;
import com.example.chat.user.UserProfileActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import static com.example.chat.FirebaseUtil.sDatabaseReference;

public class FriendsRecyclerAdapter extends RecyclerView.Adapter<FriendsRecyclerAdapter.FriendViewHolder>{
    public List<UserInstance> mFriends;
    Context mContext;
    public FriendsRecyclerAdapter(List<UserInstance> friends , Context context){
        this.mContext = context;
        this.mFriends = friends;
    }
    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.profile_view,parent,false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final FriendViewHolder holder, int position) {
        UserInstance userInstance = mFriends.get(position);
        holder.name.setText(userInstance.getName());
        holder.status.setText(userInstance.getStatus());
       // Glide.with(mContext).load(userInstance.getImageUri()).into(holder.image);
        if(userInstance.getImageUri().equals("")){
            holder.image.setImageDrawable(mContext.getResources().getDrawable(R.drawable.profilepic));
        }else {
            Glide.with(mContext).load(userInstance.getImageUri()).into(holder.image);
        }
        holder.user_id = userInstance.getUId();
    }

    @Override
    public int getItemCount() {
        return (mFriends != null) ? mFriends.size() : 0;
    }

    class FriendViewHolder extends RecyclerView.ViewHolder{
        TextView name;
        TextView status;
        ImageView image;
        String user_id;
        Button acceptBtn;
        Button rejectBtn;
        public FriendViewHolder(@NonNull View itemView) {
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
                                intent.putExtra(FrontActivity.USER_ID, user_id);
                                intent.putExtra(FrontActivity.STATE,FrontActivity.FRIENDS_ID);
                                mContext.startActivity(intent);
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
        for(int i=0 ; i<mFriends.size() ; i++){
            if(mFriends.get(i).getUId().equals(user_id)) {
                mFriends.remove(i);
                break;
            }
        }
        notifyDataSetChanged();
    }
}
