package com.example.chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    List<MessageInstance> messagesList;
    FirebaseAuth mFirebaseAuth;
    DatabaseReference mRoot;
    Context context;
    public MessageAdapter(List<MessageInstance> messagesList , Context context) {
        this.messagesList = messagesList;
        this.context = context;
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{
        public TextView senderMessage , receiverMessage ,sent_seen;
        public CircleImageView receiverPhoto;
        public ImageView receiverSendedphoto , senderSendedPhoto;


        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            senderMessage = itemView.findViewById(R.id.senderMessage);
            receiverMessage = itemView.findViewById(R.id.receiverMessage);
            receiverPhoto = itemView.findViewById(R.id.receiver_chat_photo);
            receiverSendedphoto = itemView.findViewById(R.id.receiverSendedPhoto);
            senderSendedPhoto = itemView.findViewById(R.id.senderSendedPhoto);
            sent_seen = itemView.findViewById(R.id.sent_seen);
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
        String uri = messageInstance.getmReceiverPhoto();
        String type = messageInstance.getmType();

        holder.receiverMessage.setVisibility(View.GONE);
        holder.senderMessage.setVisibility(View.GONE);
        holder.receiverPhoto.setVisibility(View.GONE);
        holder.senderSendedPhoto.setVisibility(View.GONE);
        holder.receiverSendedphoto.setVisibility(View.GONE);
        holder.sent_seen.setVisibility(View.GONE);

        if (type.equals("photo")) {

            if (senderID1.equals(senderID2)) {
                RequestOptions requestOptions = new RequestOptions();
                requestOptions.placeholder(R.drawable.profilepic);

                Glide.with(context)
                        .setDefaultRequestOptions(requestOptions)
                        .load(messageInstance.getmChatPhoto()).into(holder.senderSendedPhoto);
                holder.senderSendedPhoto.setVisibility(View.VISIBLE);
            }else{
                RequestOptions requestOptions = new RequestOptions();
                requestOptions.placeholder(R.drawable.profilepic);

                Glide.with(context)
                        .setDefaultRequestOptions(requestOptions)
                        .load(messageInstance.getmChatPhoto()).into(holder.receiverSendedphoto);
                holder.receiverSendedphoto.setVisibility(View.VISIBLE);
            }



        } else if(type.equals("text")) {
            if (senderID1.equals(senderID2)) {
                holder.senderMessage.setText(message);
                holder.senderMessage.setVisibility(View.VISIBLE);
                //holder.sent_seen.setVisibility(View.VISIBLE);


            } else {
            //    holder.sent_seen.setText("seen");
                holder.receiverMessage.setText(message);
                holder.receiverMessage.setVisibility(View.VISIBLE);
                RequestOptions requestOptions = new RequestOptions();
                requestOptions.placeholder(R.drawable.profilepic);


                Glide.with(context)
                        .setDefaultRequestOptions(requestOptions)
                        .load(uri).into(holder.receiverPhoto);


                holder.receiverPhoto.setVisibility(View.VISIBLE);
            }
        }
    }
    @Override
    public int getItemCount() {
        return messagesList.size();
    }


}
