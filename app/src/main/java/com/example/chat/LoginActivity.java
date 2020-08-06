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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    EditText email;
    EditText password;
    private FirebaseAuth firebase;

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
            firebase.signInWithEmailAndPassword(userEmail, userPassword)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(LoginActivity.this, "Success to Login", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(LoginActivity.this , ProfileActivity.class);
                                intent.setFlags(intent.FLAG_ACTIVITY_NEW_TASK | intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
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
        firebase = FirebaseAuth.getInstance();
        email = findViewById(R.id.nametext);
        password =findViewById(R.id.editTextTextPassword);

        if(firebase.getCurrentUser() !=null)
            startActivity(new Intent(getApplicationContext(), ProfileActivity.class));


    }


}