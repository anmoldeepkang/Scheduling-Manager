package com.example.learning;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class History extends AppCompatActivity implements View.OnClickListener {

    FirebaseDatabase firebaseDatabase=FirebaseDatabase.getInstance();
    DatabaseReference databaseReference=firebaseDatabase.getReference();
    TableLayout tableLayout;
    EditText fromDate,toDate;
    List<Map<String,Object>> data=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        fromDate=findViewById(R.id.startDatehistory);
        toDate=findViewById(R.id.endDatehistory);
        tableLayout=findViewById(R.id.shiftstable);

        fromDate.setOnClickListener(this);
        toDate.setOnClickListener(this);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()) {
                    Iterable<DataSnapshot> shitsChildren=dataSnapshot.child("users/jagdeep/shifts").getChildren();
                    for(DataSnapshot dataSnapshot1:shitsChildren) {
                        Map<String,Object> m=new LinkedHashMap<>();
                        TableRow row = new TableRow(History.this);
                        row.setLayoutParams(new TableRow.LayoutParams(
                                TableRow.LayoutParams.MATCH_PARENT,
                                TableRow.LayoutParams.WRAP_CONTENT));
                        row.addView(getTextView(dataSnapshot1.getKey()));
                        row.addView(getTextView(dataSnapshot1.child("start").getValue(String.class)));
                        row.addView(getTextView(dataSnapshot1.child("end").getValue(String.class)));
                        String st=makeTimestampString(dataSnapshot1,"start");
                        String et=makeTimestampString(dataSnapshot1,"end");

                        try {
                            float duration=(convertStringToTimestamp(et).getTime()- convertStringToTimestamp(st).getTime())/(1000*60*60);
                            m.put("date",new SimpleDateFormat("dd-MM-yyyy").parse(dataSnapshot1.getKey()));
                            m.put("start", convertStringToTimestamp(st));
                            m.put("end", convertStringToTimestamp(et));
                            data.add(m);
                            row.addView(getTextView(String.valueOf(duration)));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        tableLayout.addView(row);
                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private String makeTimestampString(DataSnapshot dataSnapshot,String key) {
        return dataSnapshot.getKey()+" "+dataSnapshot.child(key).getValue(String.class);
    }

    private View getTextView(String value) {
        TextView tv = new TextView(History.this);
        tv.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT));
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(18);
        tv.setPadding(0, 5, 0, 5);

        tv.setText(value);
        return tv;
    }

    private Date convertStringToTimestamp(String date) throws ParseException {
        DateFormat fmt=new SimpleDateFormat("dd-MM-yyyy HH:mm");
        return fmt.parse(date);
    }
    private Date convertStringToDate(String date) throws ParseException {
        DateFormat fmt=new SimpleDateFormat("dd-MM-yyyy");
        return fmt.parse(date);
    }
    @Override
    public void onClick(View view) {
        if(view==fromDate || view==toDate) {
            // Get Current Date
            final Calendar c = Calendar.getInstance();
            int mYear = c.get(Calendar.YEAR);
            int mMonth = c.get(Calendar.MONTH);
            int mDay = c.get(Calendar.DAY_OF_MONTH);
            final EditText txt=(view==fromDate)?fromDate:toDate;
            final int fto=(view==fromDate)?0:1;
            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth) {
                            String outputString=dayOfMonth + "-" + (monthOfYear + 1) + "-" + year;
                            txt.setText(outputString);
                            boolean flag=false;
                                for(Map<String,Object> m:data) {
                                    try {
                                        if(fto==0 && ((Date)m.get("date")).before(convertStringToDate(outputString))) {
                                            data.remove(m);
                                            flag=true;
                                        } else if(fto==1 && ((Date)m.get("date")).after(convertStringToDate(outputString))) {
                                            data.remove(m);
                                            flag=true;
                                        }
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                }
                            if(flag) {
                                refreshTable();
                            }
                        }
                    }, mYear, mMonth, mDay);
            datePickerDialog.show();
        }


    }

    private void refreshTable() {
        tableLayout.removeAllViewsInLayout();
        for(Map<String,Object> m:data) {
            TableRow row = new TableRow(History.this);
            row.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT));
            DateFormat ft=new SimpleDateFormat("HH:mm");

            row.addView(getTextView(new SimpleDateFormat("dd-MM-yyyy").format(m.get("date"))));
            row.addView(getTextView(ft.format(m.get("start"))));
            row.addView(getTextView(ft.format(m.get("end"))));

            float duration=(((Date) m.get("end")).getTime()-((Date)m.get("start")).getTime())/(1000*60*60);
            row.addView(getTextView(String.valueOf(duration)));
            tableLayout.addView(row);
        }
    }
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }
}
