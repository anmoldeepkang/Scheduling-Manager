package com.example.learning;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class UpcomingSchedule extends AppCompatActivity {

    MaterialCalendarView materialCalendarView;
    TableLayout shiftsTable;
    FirebaseDatabase firebaseDatabase=FirebaseDatabase.getInstance();
    TextView startTime,endTime;
    String user="";
    Button swapShiftButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upcoming_schedule);
        materialCalendarView=findViewById(R.id.calendarView);
        materialCalendarView.setDateSelected(new Date(),true);
        materialCalendarView.setAllowClickDaysOutsideCurrentMonth(true);
        materialCalendarView.state().edit().setMinimumDate(new Date()).commit();
        swapShiftButton=findViewById(R.id.swapShiftButton);
        user=getIntent().getStringExtra("email");
        materialCalendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                final String dateString=date.getDay()+"-"+(date.getMonth()+1)+"-"+date.getYear();
                DatabaseReference shiftNode = firebaseDatabase.getReference().child("users").child(user).child("shifts");
                shiftNode.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            startTime.setText(dataSnapshot.child(dateString).child("start").getValue(String.class));
                            endTime.setText(dataSnapshot.child(dateString).child("end").getValue(String.class));
                            updateSwapButtonVisibility(dateString,startTime.getText().toString(),endTime.getText().toString(),swapShiftButton);


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
        shiftsTable=findViewById(R.id.shiftsTable);
        startTime=findViewById(R.id.startTime);
        endTime=findViewById(R.id.endTime);
        final String date=materialCalendarView.getSelectedDate().getDay()+"-"+(materialCalendarView.getSelectedDate().getMonth()+1)+"-"+materialCalendarView.getSelectedDate().getYear();

        DatabaseReference shiftNode = firebaseDatabase.getReference().child("users").child(user).child("shifts");
           shiftNode.addValueEventListener(new ValueEventListener() {
               @Override
               public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(date).exists()) {
                    startTime.setText(dataSnapshot.child(date).child("start").getValue(String.class));
                    endTime.setText(dataSnapshot.child(date).child("end").getValue(String.class));
                }
               }

               @Override
               public void onCancelled(@NonNull DatabaseError databaseError) {

               }
           });
           swapShiftButton.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                   String dateStr=materialCalendarView.getSelectedDate().getDay()+"-"+(materialCalendarView.getSelectedDate().getMonth()+1)+"-"+materialCalendarView.getSelectedDate().getYear();
                   createDialogForSwapRequest(view,dateStr,startTime.getText().toString(),endTime.getText().toString());
               }
           });
           updateSwapButtonVisibility(date,startTime.getText().toString(),endTime.getText().toString(),swapShiftButton);
    }

    private void updateSwapButtonVisibility(final String date, final String startTime, final String endTime, final Button swapShiftButton) {
        if(startTime.equals("") && endTime.equals("")) {
            LinearLayout linearLayout=findViewById(R.id.containerLayout);
            linearLayout.removeView(swapShiftButton);

            TextView textView=findViewById(R.id.alreadyInitiatedText);
            textView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 0));

        } else {
            firebaseDatabase.getReference().child("users").child(user).child("shifts").child("swap_requests").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getValue()==null) {
                        LinearLayout linearLayout=findViewById(R.id.containerLayout);
                        linearLayout.addView(swapShiftButton);
                    } else {
                        Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                        for(DataSnapshot s:children) {
                            if(s.child("source_start_time").getValue(String.class).equals(startTime) && s.child("source_date").getValue(String.class).equals(date) && s.child("source_end_time").getValue(String.class).equals(endTime)) {
                                LinearLayout linearLayout=findViewById(R.id.containerLayout);
                                linearLayout.removeView(swapShiftButton);
                                TextView textView=findViewById(R.id.alreadyInitiatedText);
                                LinearLayout.LayoutParams layoutParams=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                layoutParams.setMargins(0,30,0,0);
                                textView.setLayoutParams(layoutParams);
                            }
                        }
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }

    private void createDialogForSwapRequest(View view, final String source_date, final String source_start_time, final String source_end_time) {
        // Creating alert Dialog with one Button
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(UpcomingSchedule.this);


        // Setting Dialog Title
        alertDialog.setTitle("Swap Shift");
        final LinearLayout linearLayout=new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParams=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.weight = 1.0f;
        layoutParams.gravity=Gravity.CENTER;
        linearLayout.setLayoutParams(layoutParams);


        final EditText preferredDate = new EditText(this);
        preferredDate.setHint("Date");
        //preferredDate.setGravity(Gravity.CENTER);
        preferredDate.setInputType(InputType.TYPE_DATETIME_VARIATION_TIME);
        preferredDate.setTextSize(20);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        //lp.setMargins(50,0,0,0);
        //preferredDate.setLayoutParams(lp);

        final Spinner shiftOptions=new Spinner(UpcomingSchedule.this);
        //shiftOptions.setLayoutParams(new LinearLayout.LayoutParams(500,LinearLayout.LayoutParams.WRAP_CONTENT));
        //shiftOptions.setGravity(Gravity.CENTER);
        final TextView employeeName=new TextView(UpcomingSchedule.this);
        employeeName.setTextSize(20);
        //employeeName.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT));
        //employeeName.setGravity(Gravity.CENTER);
        preferredDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get Current Time
                final Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int dayOfMonth = c.get(Calendar.DAY_OF_MONTH);
                int month=c.get(Calendar.MONTH);
                // Launch Time Picker Dialog
                DatePickerDialog datePickerDialog = new DatePickerDialog(UpcomingSchedule.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        preferredDate.setText(day+"-"+(month+1)+"-"+year);

                        final List<Map<String,String>> availableShifts=addShiftsToSpinner(shiftOptions,day+"-"+(month+1)+"-"+year);
                        shiftOptions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                String shiftSelected= (String) adapterView.getItemAtPosition(i);
                                String startTimeForShiftSelected=shiftSelected.split("-")[0];
                                String endTimeForShiftSelected=shiftSelected.split("-")[1];
                                for(Map<String,String> sh:availableShifts) {
                                    if(sh.get("start").equals(startTimeForShiftSelected) && sh.get("end").equals(endTimeForShiftSelected)) {
                                        employeeName.setText(sh.get("targetEmployee"));
                                    }
                                }
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> adapterView) {

                            }
                        });
                    }
                },year,month,dayOfMonth);
                datePickerDialog.show();
            }
        });

        TableLayout tableLayout=new TableLayout(this);
        LinearLayout.LayoutParams layoutParams1=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams1.gravity=Gravity.CENTER;
        tableLayout.setGravity(Gravity.CENTER);
        tableLayout.setLayoutParams(layoutParams1);
        tableLayout.setStretchAllColumns(true);

        TableRow row1=new TableRow(this);
        TextView textView=new TextView(this);
        textView.setTextSize(20);
        textView.setText("Preferred Date");
        row1.addView(textView);
        row1.addView(preferredDate);

        TableRow row2=new TableRow(this);
        TextView textView1=new TextView(this);
        textView1.setTextSize(20);
        textView1.setText("Preferred Shift");
        row2.addView(textView1);
        row2.addView(shiftOptions);

        TableRow row3=new TableRow(this);
        TextView textView2=new TextView(this);
        textView2.setTextSize(20);
        textView2.setText("Employee");
        row3.addView(textView2);
        row3.addView(employeeName);
        //linearLayout.addView(preferredDate);
        //linearLayout.addView(shiftOptions);
        //linearLayout.addView(employeeName);
        tableLayout.addView(row1);
        tableLayout.addView(row2);
        tableLayout.addView(row3);

        //linearLayout.addView(tableLayout);
        alertDialog.setView(tableLayout);
        alertDialog.setPositiveButton("Confirm",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int which) {
                        // Write your code here to execute after dialog
                        sendSwapRequestToFirebase(source_date,source_start_time,source_end_time,preferredDate.getText().toString(),shiftOptions.getSelectedItem().toString().split("-")[0],shiftOptions.getSelectedItem().toString().split("-")[1],employeeName.getText().toString());
                        Toast.makeText(getApplicationContext(),"Request placed for swapping shift", Toast.LENGTH_LONG).show();
                        dialog.cancel();
                    }
                });
        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Write your code here to execute after dialog
                        dialog.cancel();
                    }
                });

        alertDialog.show();
    }

    private void sendSwapRequestToFirebase(String source_date, String source_start_time, String source_end_time, String target_date, String target_start_time, String target_end_time,String target_employee) {
        //send to firebase
        String key=firebaseDatabase.getReference().child("users").child(user).child("shifts").child("swap_requests").push().getKey();
        Map<String,String> m=new LinkedHashMap<>();
        m.put("source_date",source_date);
        m.put("source_start_time",source_start_time);
        m.put("source_end_time",source_end_time);
        m.put("target_date",target_date);
        m.put("target_start_time",target_start_time);
        m.put("target_end_time",target_end_time);
        m.put("target_employee",target_employee);
        firebaseDatabase.getReference().child("users").child(user).child("shifts").child("swap_requests").child(key).setValue(m);
    }

    private List<Map<String,String>> addShiftsToSpinner(final Spinner shiftOptions, final String dateString) {
        DatabaseReference users = firebaseDatabase.getReference().child("users");
        final List<Map<String,String>> shiftsList=new ArrayList<>();
        final List<String> shiftTimesStrings=new ArrayList<>();
        users.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                for(DataSnapshot s:children) {
                    if(!s.getKey().equals(user)) {
                        Iterable<DataSnapshot> shifts=s.child("shifts").getChildren();
                        for(DataSnapshot shift:shifts) {
                            if(shift.getKey().toString().equals(dateString)) {
                                Map<String,String> shiftDetails=new LinkedHashMap<>();
                                shiftDetails.put("start",shift.child("start").getValue(String.class));
                                shiftDetails.put("end",shift.child("end").getValue(String.class));
                                shiftDetails.put("targetId",shift.child("id").getValue(String.class));
                                shiftDetails.put("targetEmployee",s.getKey());
                                shiftTimesStrings.add(shiftDetails.get("start")+"-"+shiftDetails.get("end"));
                                shiftsList.add(shiftDetails);
                            }
                        }
                    }

                }
                ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(UpcomingSchedule.this,
                        android.R.layout.simple_spinner_item, shiftTimesStrings);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                shiftOptions.setAdapter(dataAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return shiftsList;
    }
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }

}
