package com.example.chat;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.net.URI;

import static com.example.chat.FirebaseUtil.sDatabaseReference;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class UploadPhotoService extends IntentService {
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mStorageRef;
    private StorageReference mProfileRef;
    public static final String IMAGE_REG = "com.example.chat.imageRef";
    public static final String USER_ID = "com.example.chat.userId";
    private Uri mDownloadUri;

    public UploadPhotoService() {
        super("UploadPhotoService");
    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if(intent != null){
            mFirebaseStorage = FirebaseStorage.getInstance();
            mStorageRef = mFirebaseStorage.getReferenceFromUrl("gs://chat-d4365.appspot.com");
            String ref = intent.getStringExtra(IMAGE_REG);
            Uri selectedImage = Uri.parse(ref);
            final String userId = intent.getStringExtra(USER_ID);
            mProfileRef = mStorageRef.child("images/profiles/"+userId+".jpg");
            final UploadTask uploadTask = mProfileRef.putFile(selectedImage);
            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    // Continue with the task to get the download URL
                    return mProfileRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        mDownloadUri = task.getResult();
                        Log.i("uri",mDownloadUri.toString());
                        sDatabaseReference.child("Users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                UserInstance user = dataSnapshot.getValue(UserInstance.class);
                                user.setImageUri(mDownloadUri.toString());
                                sDatabaseReference.child("Users").child(userId).setValue(user);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    } else {
                        // Handle failures
                        // ...
                    }
                }
            });
        }
    }
}