package com.example.learning;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Profile extends AppCompatActivity {

    private ImageView profileImage;
    private FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    private StorageReference storageReference=firebaseStorage.getReference().child("employee.jpg");
    private ImageView recordImage, historyImage,createSchedule,upcomingSchedule,logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        profileImage=findViewById(R.id.imageView);
        historyImage=findViewById(R.id.imageButton3);
        createSchedule=findViewById(R.id.imageButton4);
        upcomingSchedule=findViewById(R.id.imageButton5);
        logoutButton=findViewById(R.id.logoutButton);
        try {
            final File file= File.createTempFile("petro_profile_","jpg");
            storageReference.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Bitmap bitmap= BitmapFactory.decodeFile(file.getAbsolutePath());
                    profileImage.setImageBitmap(bitmap);
                }
            });
        } catch (IOException e) {
            //e.printStackTrace();
        }
        recordImage=findViewById(R.id.imageButton2);
        recordImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(Profile.this, SwapRequests.class);
                intent.putExtra("email",getIntent().getStringExtra("email"));
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
            }
        });
        historyImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(Profile.this,History.class);
                intent.putExtra("email",getIntent().getStringExtra("email"));
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
            }
        });
        createSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(Profile.this,CreateSchedule.class);
                intent.putExtra("email",getIntent().getStringExtra("email"));
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
            }
        });
        upcomingSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(Profile.this,UpcomingSchedule.class);
                intent.putExtra("email",getIntent().getStringExtra("email"));
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
            }
        });
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(Profile.this,MainActivity.class);
                startActivity(intent);
            }
        });
        displayNotifications();
    }

    private void displayNotifications() {

        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot1) {
                String user=getIntent().getStringExtra("email");
                Iterable<DataSnapshot> message=dataSnapshot1.child(user).child("notifications").getChildren();
                List<String> notifications=new ArrayList<>();
                for(DataSnapshot msg:message) {
                    notifications.add(msg.getValue(String.class));
                    msg.getRef().removeValue();
                }
                for(String notific:notifications) {
                    NotificationCompat.Builder builder=new NotificationCompat.Builder(Profile.this).setSmallIcon(R.drawable.ic_launcher_foreground).setContentTitle("Scheduling Manager").setContentText(notific).setStyle(new NotificationCompat.BigTextStyle().bigText("Your request for swapping shift on 10-5-2020(00:00-00:00) with anna's shift on 7-5-2020(00:00-00:00) has been accepted")).setAutoCancel(true);
                    NotificationManager notificationManager= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(0,builder.build());
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }
}
