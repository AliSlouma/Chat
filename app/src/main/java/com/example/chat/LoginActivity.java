package com.example.chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    EditText email;
    EditText password;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference root;

    public void register(View view){
        startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
    }


    public void signIn(View view){

        String userEmail = email.getText().toString().trim();
        String userPassword = password.getText().toString().trim();

        if(TextUtils.isEmpty(userEmail))
            Toast.makeText(LoginActivity.this, "Enter Your E-mail", Toast.LENGTH_SHORT).show();
        else if(TextUtils.isEmpty(userPassword))
            Toast.makeText(LoginActivity.this, "Enter Your password", Toast.LENGTH_SHORT).show();

        else {
            firebaseAuth.signInWithEmailAndPassword(userEmail, userPassword)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(LoginActivity.this, "Success to Login", Toast.LENGTH_SHORT).show();
                                 sendToProfileOrMain();
                            } else {
                                String errorMessage = task.getException().getMessage().toString();
                                Toast.makeText(LoginActivity.this, "Error : " + errorMessage, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        firebaseAuth = FirebaseAuth.getInstance();
        root = FirebaseDatabase.getInstance().getReference();
        email = findViewById(R.id.nametext);
        password =findViewById(R.id.editTextTextPassword);

    }
    private void sendToProfileOrMain(){
        String ID = firebaseAuth.getCurrentUser().getUid();
        Log.i("Info" , ID);

        root.child("Users").child(ID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot){
                if (dataSnapshot.child("name").exists()) {
                    Toast.makeText(getApplicationContext(),"Welcome back ", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), FrontActivity.class));
                    finish();
                }
                else {
                    startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                    finish();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


}