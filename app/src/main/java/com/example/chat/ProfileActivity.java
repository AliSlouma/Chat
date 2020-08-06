package com.example.chat;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ProfileActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private DatabaseReference root ;
    EditText name ;
    EditText status ;

    public void done(View view){
        String userName = name.getText().toString().trim();
        String userStatus = status.getText().toString().trim();

        if(TextUtils.isEmpty(userName))
            Toast.makeText(ProfileActivity.this, "Enter Your E-mail", Toast.LENGTH_SHORT).show();
        else if(TextUtils.isEmpty(userStatus))
            Toast.makeText(ProfileActivity.this, "Enter Your password", Toast.LENGTH_SHORT).show();

    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        firebaseAuth = FirebaseAuth.getInstance();
        root= FirebaseDatabase.getInstance().getReference();
        name = findViewById(R.id.name);
        status = findViewById(R.id.status);





    }
}