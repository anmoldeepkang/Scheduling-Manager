package com.example.learning;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class SwapRequests extends AppCompatActivity implements View.OnClickListener {
    String user="";
    FirebaseDatabase firebaseDatabase=FirebaseDatabase.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swap_requests);
        user=getIntent().getStringExtra("email");
        loadRequestsForMe();
        loadRequestsByMe();
    }

    private void loadRequestsByMe() {
        final DatabaseReference users = firebaseDatabase.getReference();
        users.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> swapRequests = dataSnapshot.child("users").child(user).child("shifts").child("swap_requests").getChildren();
                for(DataSnapshot swapRequest:swapRequests) {
                    displayRequestsByMe(swapRequest.child("source_date").getValue(String.class),swapRequest.child("source_start_time").getValue(String.class)
                    ,swapRequest.child("source_end_time").getValue(String.class),swapRequest.child("target_date").getValue(String.class),swapRequest.child("target_start_time").getValue(String.class),
                    swapRequest.child("target_end_time").getValue(String.class),swapRequest.child("target_employee").getValue(String.class),swapRequest.getKey());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void displayRequestsByMe(String source_date, String source_start_time, String source_end_time, String target_date, String target_start_time, String target_end_time, String target_employee, final String swapRequestId) {
        final GridLayout gridLayout=findViewById(R.id.gridView1);
        final ImageView imageView=new ImageView(this);
        TableRow.LayoutParams layoutParams=new TableRow.LayoutParams(250,250);
        imageView.setLayoutParams(layoutParams);
        layoutParams.setMargins(0,50,0,0);
        imageView.setPadding(5,5,5,5);
        imageView.setImageResource(R.drawable.petro_canada);

        gridLayout.addView(imageView);

        final TableLayout tableLayout=new TableLayout(this);
        TableLayout.LayoutParams layoutParamsTable=new TableLayout.LayoutParams(700,TableLayout.LayoutParams.WRAP_CONTENT);
        tableLayout.setLayoutParams(layoutParamsTable);
        gridLayout.addView(tableLayout);

        TableRow row1=new TableRow(this);
        row1.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,TableRow.LayoutParams.WRAP_CONTENT));
        TextView textView1=new TextView(this);
        //textView1.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT));
        textView1.setText(target_employee+"'s shift");
        textView1.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        textView1.setPadding(5,5,5,5);
        row1.addView(textView1);

        TextView textView2=new TextView(this);
        //textView2.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT));
        textView2.setText("Your shift");
        textView2.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        textView2.setPadding(5,5,5,5);
        row1.addView(textView2);

        TableRow row2=new TableRow(this);
        row2.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,TableRow.LayoutParams.WRAP_CONTENT));

        TextView textView3=new TextView(this);
        //textView3.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT));
        textView3.setText(target_date);
        textView3.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        textView3.setPadding(5,5,5,5);
        row2.addView(textView3);

        TextView textView4=new TextView(this);
        //textView4.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT));
        textView4.setText(source_date);
        textView4.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        textView4.setPadding(5,5,5,5);
        row2.addView(textView4);

        TableRow row3=new TableRow(this);
        row3.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,TableRow.LayoutParams.WRAP_CONTENT));

        TextView textView5=new TextView(this);
        //textView5.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT));
        textView5.setText(target_start_time+"-"+target_end_time);
        textView5.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        textView5.setPadding(5,5,5,5);
        row3.addView(textView5);

        TextView textView6=new TextView(this);
        //textView6.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT));
        textView6.setText(source_start_time+"-"+source_end_time);
        textView6.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        textView6.setPadding(5,5,5,5);
        row3.addView(textView6);

        TableRow row4=new TableRow(this);
        row4.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,TableRow.LayoutParams.WRAP_CONTENT));

        Button accept=new Button(this);
        //accept.setWidth(50);
        accept.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,TableRow.LayoutParams.WRAP_CONTENT));
        accept.setText("Accept");
        accept.setVisibility(View.INVISIBLE);
        Button reject=new Button(this);
        reject.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,TableRow.LayoutParams.WRAP_CONTENT));
        //reject.setWidth(50);
        reject.setText("Cancel");
        reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseDatabase.getReference().child("users").child(user).child("shifts").child("swap_requests").child(swapRequestId).removeValue();
                gridLayout.removeView(imageView);
                gridLayout.removeView(tableLayout);
            }
        });
        row4.addView(accept);
        row4.addView(reject);

        tableLayout.addView(row1);
        tableLayout.addView(row2);
        tableLayout.addView(row3);
        tableLayout.addView(row4);
    }

    private void loadRequestsForMe() {
        final DatabaseReference users = firebaseDatabase.getReference();

        users.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> users1 = dataSnapshot.child("users").getChildren();
                for(DataSnapshot dataSnapshot1:users1) {
                    if(!dataSnapshot1.getKey().equals(user)) {
                        final Iterable<DataSnapshot> shifts = dataSnapshot1.child("shifts").child("swap_requests").getChildren();
                        for(DataSnapshot swapRequest:shifts) {
                            if(swapRequest.child("target_employee").getValue(String.class).equals(user)) {
                                displayRequestForMe(dataSnapshot1.getKey(),swapRequest.child("source_date").getValue(String.class),swapRequest.child("source_start_time").getValue(String.class)
                                ,swapRequest.child("source_end_time").getValue(String.class),swapRequest.child("target_date").getValue(String.class),swapRequest.child("target_start_time").getValue(String.class),
                                        swapRequest.child("target_end_time").getValue(String.class),swapRequest.getKey());
                            }
                        }
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void displayRequestForMe(final String sourceEmployee, final String source_date, final String source_start_time, final String source_end_time, final String target_date, final String target_start_time, final String target_end_time, final String swapRequestId) {
        final GridLayout gridLayout=findViewById(R.id.gridView);
        final int numberOfChildren=gridLayout.getChildCount();
        final ImageView imageView=new ImageView(this);
        TableRow.LayoutParams layoutParams=new TableRow.LayoutParams(250,250);
        imageView.setLayoutParams(layoutParams);
        layoutParams.setMargins(0,50,0,0);
        imageView.setPadding(5,5,5,5);
        imageView.setImageResource(R.drawable.petro_canada);

        gridLayout.addView(imageView);

        final TableLayout tableLayout=new TableLayout(this);
        TableLayout.LayoutParams layoutParamsTable=new TableLayout.LayoutParams(700,TableLayout.LayoutParams.WRAP_CONTENT);
        tableLayout.setLayoutParams(layoutParamsTable);
        gridLayout.addView(tableLayout);

        TableRow row1=new TableRow(this);
        row1.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,TableRow.LayoutParams.WRAP_CONTENT));
        TextView textView1=new TextView(this);
        //textView1.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT));
        textView1.setText(sourceEmployee+"'s shift");
        textView1.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        textView1.setPadding(5,5,5,5);
        row1.addView(textView1);

        TextView textView2=new TextView(this);
        //textView2.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT));
        textView2.setText("Your shift");
        textView2.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        textView2.setPadding(5,5,5,5);
        row1.addView(textView2);

        TableRow row2=new TableRow(this);
        row2.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,TableRow.LayoutParams.WRAP_CONTENT));

        TextView textView3=new TextView(this);
        //textView3.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT));
        textView3.setText(target_date);
        textView3.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        textView3.setPadding(5,5,5,5);
        row2.addView(textView3);

        TextView textView4=new TextView(this);
        //textView4.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT));
        textView4.setText(source_date);
        textView4.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        textView4.setPadding(5,5,5,5);
        row2.addView(textView4);

        TableRow row3=new TableRow(this);
        row3.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,TableRow.LayoutParams.WRAP_CONTENT));

        TextView textView5=new TextView(this);
        //textView5.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT));
        textView5.setText(target_start_time+"-"+target_end_time);
        textView5.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        textView5.setPadding(5,5,5,5);
        row3.addView(textView5);

        TextView textView6=new TextView(this);
        //textView6.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT));
        textView6.setText(source_start_time+"-"+source_end_time);
        textView6.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        textView6.setPadding(5,5,5,5);
        row3.addView(textView6);

        TableRow row4=new TableRow(this);
        row4.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,TableRow.LayoutParams.WRAP_CONTENT));

        Button accept=new Button(this);
        //accept.setWidth(50);
        accept.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,TableRow.LayoutParams.WRAP_CONTENT));
        accept.setText("Accept");
        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<String,String> m=new LinkedHashMap<>();
                m.put("start",source_start_time);
                m.put("end",source_end_time);
                Map<String,String> m1=new LinkedHashMap<>();
                m1.put("start",target_start_time);
                m1.put("end",target_end_time);
                firebaseDatabase.getReference().child("users").child(user).child("shifts").child(source_date).setValue(m);
                firebaseDatabase.getReference().child("users").child(user).child("shifts").child(target_date).removeValue();
                firebaseDatabase.getReference().child("users").child(sourceEmployee).child("shifts").child(source_date).removeValue();
                firebaseDatabase.getReference().child("users").child(sourceEmployee).child("shifts").child(target_date).setValue(m1);
                firebaseDatabase.getReference().child("users").child(sourceEmployee).child("shifts").child("swap_requests").child(swapRequestId).removeValue();
                Toast.makeText(getApplicationContext(),"You accepted "+sourceEmployee+"'s request",Toast.LENGTH_LONG).show();
                String notKey=firebaseDatabase.getReference().child("users").child(sourceEmployee).child("notifications").push().getKey();
                firebaseDatabase.getReference().child("users").child(sourceEmployee).child("notifications").child(notKey).setValue("Your request for swapping shift on "+source_date+"("+source_start_time+"-"+source_end_time+") with "+user+"'s shift on "+target_date+"("+target_start_time+"-"+target_end_time+") has been accepted");
                gridLayout.removeView(imageView);
                tableLayout.removeAllViewsInLayout();
                gridLayout.removeView(tableLayout);
            }
        });
        Button reject=new Button(this);
        reject.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,TableRow.LayoutParams.WRAP_CONTENT));
        //reject.setWidth(50);
        reject.setText("Reject");
        reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseDatabase.getReference().child("users").child(sourceEmployee).child("shifts").child("swap_requests").child(swapRequestId).removeValue();
                String notKey=firebaseDatabase.getReference().child("users").child(sourceEmployee).child("notifications").push().getKey();
                firebaseDatabase.getReference().child("users").child(sourceEmployee).child("notifications").child(notKey).setValue("Your request for swapping shift on "+source_date+"("+source_start_time+"-"+source_end_time+") with "+user+"'s shift on "+target_date+"("+target_start_time+"-"+target_end_time+") has been rejected");
                gridLayout.removeView(imageView);
                gridLayout.removeView(tableLayout);
            }
        });
        row4.addView(accept);
        row4.addView(reject);

        tableLayout.addView(row1);
        tableLayout.addView(row2);
        tableLayout.addView(row3);
        tableLayout.addView(row4);
    }


    @Override
    public void onClick(View view) {

    }
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }
}
