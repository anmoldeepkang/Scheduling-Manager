package com.example.learning;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    EditText username,password;

    Button btn;
    FirebaseDatabase firebaseDatabase=FirebaseDatabase.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        username=findViewById(R.id.editTextEmail);
        password=findViewById(R.id.editTextPassword);
        btn=findViewById(R.id.cirLoginButton);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String user=username.getText().toString();
                final String pwd=password.getText().toString();
                firebaseDatabase.getReference().child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(!dataSnapshot.child(user).exists()) {
                            Toast.makeText(getApplicationContext(),"User doesn't exist",Toast.LENGTH_LONG).show();
                        } else if(!dataSnapshot.child(user).child("password").getValue(String.class).equals(pwd)) {
                            Toast.makeText(getApplicationContext(),"Incorrect password",Toast.LENGTH_LONG).show();
                        } else {
                            Intent intent=new Intent(MainActivity.this,Profile.class);
                            intent.putExtra("email",user);
                            startActivity(intent);
                            overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }
        });

    }




}
