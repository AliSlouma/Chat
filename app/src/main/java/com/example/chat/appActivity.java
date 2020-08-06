package com.example.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class appActivity extends AppCompatActivity {
    FirebaseAuth firebase;

    public void logout(View view){
        firebase.signOut();
        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app);

        firebase = FirebaseAuth.getInstance();

        FirebaseUser firebaseUser = firebase.getCurrentUser();

        if(firebaseUser == null)
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));

    }
}