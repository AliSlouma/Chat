package com.example.chat;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    List<MessageInstance> messagesList;
    FirebaseAuth mFirebaseAuth;
    DatabaseReference mRoot;
    public MessageAdapter(List<MessageInstance> messagesList) {
        this.messagesList = messagesList;
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{
        public TextView senderMessage , receiverMessage;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            senderMessage = itemView.findViewById(R.id.senderMessage);
            receiverMessage = itemView.findViewById(R.id.receiverMessage);
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_chat_view,parent,false);

        mFirebaseAuth = FirebaseAuth.getInstance();


        return new MessageViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        String senderID1 = mFirebaseAuth.getUid();


        MessageInstance messageInstance = messagesList.get(position);

        String senderID2 = messageInstance.getmSenderID();
        String message = messageInstance.getMessage();

        holder.receiverMessage.setVisibility(View.INVISIBLE);
        holder.senderMessage.setVisibility(View.INVISIBLE);

        if (senderID1.equals(senderID2)) {
            holder.senderMessage.setBackgroundResource(R.drawable.sender_custom_chat);
            holder.senderMessage.setText(message);
            holder.senderMessage.setVisibility(View.VISIBLE);
            Log.i("hi lol" , message);
            Log.i("hi lol" , String.valueOf(position));
        } else {
            holder.receiverMessage.setText(message);
            holder.receiverMessage.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }


}
