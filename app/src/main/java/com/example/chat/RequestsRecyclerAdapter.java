package com.example.chat;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static com.example.chat.FirebaseUtil.*;

public class RequestsRecyclerAdapter extends RecyclerView.Adapter<RequestsRecyclerAdapter.RequestViewHolder>{
    List<String> mRequests;
    Context mContext;


    public RequestsRecyclerAdapter(List<String> requests, Context context){
        this.mContext = context;
        this.mRequests = requests;
    }
    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.profile_view,parent,false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final RequestViewHolder holder, int position) {
        String User_id = mRequests.get(position);
        sDatabaseReference.child("Users").child(User_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserInstance userInstance = dataSnapshot.getValue(UserInstance.class);
                holder.name.setText(userInstance.getName());
                holder.status.setText(userInstance.getStatus());
                Glide.with(mContext).load(userInstance.getImageUri()).into(holder.image);
                holder.user_id = userInstance.getUId();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return (mRequests != null) ? mRequests.size() : 0;
    }

    class RequestViewHolder extends RecyclerView.ViewHolder{
        TextView name;
        TextView status;
        ImageView image;
        Button acceptBtn;
        Button rejectBtn;
        String user_id;
        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.profile_name);
            status = itemView.findViewById(R.id.profile_status);
            image = itemView.findViewById(R.id.receiver_chat_photo);
            acceptBtn = (Button) itemView.findViewById(R.id.button_accept_request);
            rejectBtn = (Button) itemView.findViewById(R.id.button_reject_request);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext , UserProfileActivity.class);
                    intent.putExtra(FrontActivity.USER_ID, user_id);
                    intent.putExtra(FrontActivity.STATE,FrontActivity.REQUEST_ID);
                    mContext.startActivity(intent);
                }
            });
            acceptBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sDatabaseReference.child(FrontActivity.PATH_FRIENDS).child(user_id).child(sFirebaseAuth.getUid()).setValue("");
                    sDatabaseReference.child(FrontActivity.PATH_FRIENDS).child(sFirebaseAuth.getUid()).child(user_id).setValue("");
                    sDatabaseReference.child(FrontActivity.PATH_REQUESTS).child(sFirebaseAuth.getUid()).child(user_id).removeValue();
                }
            });
            rejectBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sDatabaseReference.child(FrontActivity.PATH_REQUESTS).child(sFirebaseAuth.getUid()).child(user_id).removeValue();
                }
            });
        }
    }
}
