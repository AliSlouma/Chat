package com.example.chat.chats;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.chat.FrontActivity;
import com.example.chat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder>{
    List<ChatInstance> mChatList;
    FirebaseAuth mFirebaseAuth;
    DatabaseReference mRoot;
    Context context;
    public ChatAdapter(List<ChatInstance> mChatList , Context context) {
        this.mChatList = mChatList;
        this.context = context;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_chat_list,parent,false);

        mFirebaseAuth = FirebaseAuth.getInstance();

        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ChatViewHolder holder, int position) {
        ChatInstance chatInstance = mChatList.get(position);

        mRoot = FirebaseDatabase.getInstance().getReference().child("Users").child(chatInstance.getReceiverUID());
        mRoot.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String test = dataSnapshot.getKey();
                if(test.equals("userState")){
                    holder.onlineStatus.setText((String) dataSnapshot.getValue());
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String test = dataSnapshot.getKey();
                if(test.equals("userState")){
                    holder.onlineStatus.setText((String) dataSnapshot.getValue());
                }
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


        if(!chatInstance.isSeen()){
            holder.lastMessage.setTypeface(Typeface.DEFAULT_BOLD);
            holder.lastMessage.setTextSize(20);
            holder.lastMessage.setTextColor(Color.BLACK);


        }else{
            holder.lastMessage.setTypeface(Typeface.DEFAULT);
            holder.lastMessage.setTextSize(20);
            holder.lastMessage.setTextColor(Color.LTGRAY);
        }

        holder.name.setText(chatInstance.getReceiver());



        holder.lastMessage.setText(chatInstance.getLastMessage());
        holder.forID.setText(chatInstance.getReceiverUID());
        holder.forTime.setText(chatInstance.getTime());
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.placeholder(R.drawable.profilepic);

        Glide.with(context)
                .setDefaultRequestOptions(requestOptions)
                .load(chatInstance.getFriendPhoto()).into(holder.friendPhoto);



    }

    @Override
    public int getItemCount() {
        return mChatList.size();
    }

    public class ChatViewHolder extends RecyclerView.ViewHolder{
        public TextView name , lastMessage ,forID ,forTime , onlineStatus;;
        public CircleImageView friendPhoto;


        public ChatViewHolder(@NonNull final View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.chat_name);
            lastMessage = itemView.findViewById(R.id.last_message);
            friendPhoto = itemView.findViewById(R.id.friend_chat_list_photo);
            forID = itemView.findViewById(R.id.forID);
            forTime = itemView.findViewById(R.id.forTime);
            onlineStatus = itemView.findViewById(R.id.online_status);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, ChatActivity.class);
                    intent.putExtra("receiverID",forID.getText());
                    FrontActivity.onpause = true;
                    context.startActivity(intent);

                }
            });
        }
    }


}