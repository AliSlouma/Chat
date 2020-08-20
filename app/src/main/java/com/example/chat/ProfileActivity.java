package com.example.chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {


    private FirebaseAuth firebaseAuth;
    private DatabaseReference root ;
    private EditText name ;
    private EditText status ;

    public void done(View view){
        String userName = name.getText().toString().trim();
        String userStatus = status.getText().toString().trim();

        if(TextUtils.isEmpty(userName))
            Toast.makeText(ProfileActivity.this, "Enter Your E-mail", Toast.LENGTH_SHORT).show();
        else if(TextUtils.isEmpty(userStatus))
            Toast.makeText(ProfileActivity.this, "Enter Your password", Toast.LENGTH_SHORT).show();

        else{

            UserInstance userInstance = new UserInstance(firebaseAuth.getUid(),"",name.getText().toString(),status.getText().toString(),"");

            root.child("Users").child(firebaseAuth.getCurrentUser().getUid()).setValue(userInstance).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful())
                    {
                        Toast.makeText(getApplicationContext(),"Complete",Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), FrontActivity.class));
                        finish();
                    }
                    else{
                        Toast.makeText(getApplicationContext(),"Error" + task.getException().getMessage(),Toast.LENGTH_SHORT).show();                    }
                }
            });
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        name = findViewById(R.id.personName);
        status = findViewById(R.id.personStatus);
        firebaseAuth = FirebaseAuth.getInstance();
        root = FirebaseDatabase.getInstance().getReference();
    }
}