package com.example.chat;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class FriendsRecyclerAdapter extends RecyclerView.Adapter<FriendsRecyclerAdapter.FriendViewHolder>{
    List<UserInstance> mFriends;
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
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        UserInstance userInstance = mFriends.get(position);
        holder.name.setText(userInstance.getName());
        holder.status.setText(userInstance.getStatus());
        Glide.with(mContext).load(userInstance.getImageUri()).into(holder.image);
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
        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.profile_name);
            status = itemView.findViewById(R.id.profile_status);
            image = itemView.findViewById(R.id.receiver_chat_photo);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext , UserProfileActivity.class);
                    intent.putExtra(FrontActivity.USER_ID, user_id);
                    intent.putExtra(FrontActivity.STATE,FrontActivity.FRIENDS_ID);
                    mContext.startActivity(intent);
                }
            });
        }
    }
}
