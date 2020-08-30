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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    EditText email;
    EditText password;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference root;
    public void done (View view){
        String userEmail = email.getText().toString().trim();
        String userPassword = password.getText().toString().trim();

        if(TextUtils.isEmpty(userEmail))
            Toast.makeText(RegisterActivity.this, "Enter Your E-mail", Toast.LENGTH_SHORT).show();
        else if(TextUtils.isEmpty(userPassword))
            Toast.makeText(RegisterActivity.this, "Enter Your Password", Toast.LENGTH_SHORT).show();


        else{
            firebaseAuth.createUserWithEmailAndPassword(userEmail , userPassword)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                root.child("Users").child(firebaseAuth.getCurrentUser().getUid());
                               // root.push().child("Chats").setValue("");

                                Toast.makeText(RegisterActivity.this, "Success to Register", Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(RegisterActivity.this , ProfileActivity.class);
                                intent.setFlags(intent.FLAG_ACTIVITY_NEW_TASK | intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            }
                            else{
                                String errorMessage = task.getException().getMessage().toString();
                                Toast.makeText(RegisterActivity.this ,"Error : " + errorMessage , Toast.LENGTH_SHORT ).show();
                            }
                        }
                    });
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        firebaseAuth = FirebaseAuth.getInstance();
        root = FirebaseDatabase.getInstance().getReference();
        email = findViewById(R.id.email);
        password =findViewById(R.id.password);
    }
}
