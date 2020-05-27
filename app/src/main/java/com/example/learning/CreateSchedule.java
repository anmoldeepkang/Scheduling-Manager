package com.example.learning;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

public class CreateSchedule extends AppCompatActivity implements View.OnClickListener {
    MaterialCalendarView materialCalendarView;
    TableLayout shiftsTable;
    FloatingActionButton addButton;
    Button button;
    String user="";
    FirebaseDatabase firebaseDatabase=FirebaseDatabase.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_hours);
        materialCalendarView=findViewById(R.id.calendarView);
        materialCalendarView.setDateSelected(new Date(),true);
        materialCalendarView.setAllowClickDaysOutsideCurrentMonth(true);
        materialCalendarView.state().edit().setMinimumDate(new Date()).commit();
        shiftsTable=findViewById(R.id.shiftsTable);
        user=getIntent().getStringExtra("email");
        refreshShiftsTable(materialCalendarView.getSelectedDate().getDay()+"-"+(materialCalendarView.getSelectedDate().getMonth()+1)+"-"+materialCalendarView.getSelectedDate().getYear());
        materialCalendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                refreshShiftsTable(date.getDay()+"-"+(date.getMonth()+1)+"-"+date.getYear());
            }
        });
        addButton=findViewById(R.id.floatingActionButton);
        addButton.setOnClickListener(this);
        button=findViewById(R.id.saveSchedule);
        button.setOnClickListener(this);
    }

    private void refreshShiftsTable(final String dateSelected) {
        shiftsTable.removeAllViewsInLayout();
        final List<Map<String,String>> shifts=new ArrayList<>();
        firebaseDatabase.getReference().child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                for(DataSnapshot s:children) {
                    if(s.child("shifts").exists()) {
                        if(s.child("shifts").child(dateSelected).exists()) {
                            Map<String,String> m=new LinkedHashMap<>();
                            m.put("employee",s.getKey());
                            m.put("start",s.child("shifts").child(dateSelected).child("start").getValue(String.class));
                            m.put("end",s.child("shifts").child(dateSelected).child("end").getValue(String.class));
                            shifts.add(m);
                        }
                    }

                }
                populateShiftsTable(shifts);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    public void onClick(View view) {
        if(view==addButton) {
            addRowsToShiftsTable(view);
        } else if(view==button) {
            saveShiftsToFirebase(view);
            refreshShiftsTable(materialCalendarView.getSelectedDate().getDay()+"-"+(materialCalendarView.getSelectedDate().getMonth()+1)+"-"+materialCalendarView.getSelectedDate().getYear());
        }


    }

    private void saveShiftsToFirebase(View view) {
        int numberOfRows=shiftsTable.getChildCount();
        final CalendarDay selectedDate = materialCalendarView.getSelectedDate();
        String date=selectedDate.getDay()+"-"+(selectedDate.getMonth()+1)+"-"+selectedDate.getYear();
        final DatabaseReference users = firebaseDatabase.getReference().child("users");
        List<String> employeesAlreadyAssignedOnDate=new ArrayList<>();
        for(int i=0;i<numberOfRows;i++) {
            TableRow row= (TableRow) shiftsTable.getChildAt(i);
            String employeeAssigned=((Spinner)row.getChildAt(2)).getSelectedItem().toString();

            if(employeesAlreadyAssignedOnDate.contains(employeeAssigned)) {
                Toast.makeText(this,"The employee "+employeeAssigned+" already has shift on "+date,Toast.LENGTH_LONG).show();
                return;
            } else {
                employeesAlreadyAssignedOnDate.add(employeeAssigned);
            }
            String startTime=((EditText)row.getChildAt(0)).getText().toString();
            String endTime=((EditText)row.getChildAt(1)).getText().toString();
            if(!employeeAssigned.equals("") && !startTime.equals("") && !endTime.equals("")) {
                Map<String,Object> map=new LinkedHashMap<>();
                map.put("start",startTime);
                map.put("end",endTime);
                users.child(employeeAssigned).child("shifts").child(date).setValue(map);
            }

        }
        Toast.makeText(this,"Schedule has been saved",Toast.LENGTH_LONG).show();
    }

    private void addRowsToShiftsTable(View view) {
        TableRow row=new TableRow(this);
        row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT));
        final EditText fromTime=new EditText(this);
        fromTime.setWidth(230);
        fromTime.setHeight(TableRow.LayoutParams.WRAP_CONTENT);
        fromTime.setInputType(InputType.TYPE_DATETIME_VARIATION_TIME);
        fromTime.setHint("Start");
        fromTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Get Current Time
                final Calendar c = Calendar.getInstance();
                int mHour = c.get(Calendar.HOUR_OF_DAY);
                int mMinute = c.get(Calendar.MINUTE);
                // Launch Time Picker Dialog
                TimePickerDialog timePickerDialog = new TimePickerDialog(CreateSchedule.this,
                        new TimePickerDialog.OnTimeSetListener() {

                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay,
                                                  int minute) {

                                fromTime.setText(hourOfDay + ":" + minute);
                            }
                        }, mHour, mMinute, true);
                timePickerDialog.show();
            }
        });
        row.addView(fromTime);
        final EditText toTime=new EditText(this);
        toTime.setWidth(230);
        toTime.setHeight(TableRow.LayoutParams.WRAP_CONTENT);
        toTime.setInputType(InputType.TYPE_DATETIME_VARIATION_TIME);
        toTime.setHint("End");
        toTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get Current Time
                final Calendar c = Calendar.getInstance();
                int mHour = c.get(Calendar.HOUR_OF_DAY);
                int mMinute = c.get(Calendar.MINUTE);
                // Launch Time Picker Dialog
                TimePickerDialog timePickerDialog = new TimePickerDialog(CreateSchedule.this,
                        new TimePickerDialog.OnTimeSetListener() {

                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay,
                                                  int minute) {

                                toTime.setText(hourOfDay + ":" + minute);
                            }
                        }, mHour, mMinute, true);
                timePickerDialog.show();
            }
        });
        row.addView(toTime);
        Spinner spinner=new Spinner(this);
        spinner.setLayoutParams(new TableRow.LayoutParams(440,TableRow.LayoutParams.MATCH_PARENT));
        addEmployeesToSpinner(spinner, null);
        row.addView(spinner);
        shiftsTable.addView(row);
        button.setVisibility(View.VISIBLE);
    }

    private void populateShiftsTable(List<Map<String,String>> shifts) {
        shiftsTable.removeAllViewsInLayout();
        for(Map<String,String> shift:shifts) {
            TableRow row=new TableRow(this);
            row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT));
            final EditText fromTime=new EditText(this);
            fromTime.setWidth(230);
            fromTime.setHeight(TableRow.LayoutParams.WRAP_CONTENT);
            fromTime.setInputType(InputType.TYPE_DATETIME_VARIATION_TIME);
            fromTime.setHint("Start");
            fromTime.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // Get Current Time
                    final Calendar c = Calendar.getInstance();
                    int mHour = c.get(Calendar.HOUR_OF_DAY);
                    int mMinute = c.get(Calendar.MINUTE);
                    // Launch Time Picker Dialog
                    TimePickerDialog timePickerDialog = new TimePickerDialog(CreateSchedule.this,
                            new TimePickerDialog.OnTimeSetListener() {

                                @Override
                                public void onTimeSet(TimePicker view, int hourOfDay,
                                                      int minute) {

                                    fromTime.setText(hourOfDay + ":" + minute);
                                }
                            }, mHour, mMinute, true);
                    timePickerDialog.show();
                }
            });
            fromTime.setText(shift.get("start"));
            row.addView(fromTime);
            final EditText toTime=new EditText(this);
            toTime.setWidth(230);
            toTime.setHeight(TableRow.LayoutParams.WRAP_CONTENT);
            toTime.setInputType(InputType.TYPE_DATETIME_VARIATION_TIME);
            toTime.setHint("End");
            toTime.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Get Current Time
                    final Calendar c = Calendar.getInstance();
                    int mHour = c.get(Calendar.HOUR_OF_DAY);
                    int mMinute = c.get(Calendar.MINUTE);
                    // Launch Time Picker Dialog
                    TimePickerDialog timePickerDialog = new TimePickerDialog(CreateSchedule.this,
                            new TimePickerDialog.OnTimeSetListener() {

                                @Override
                                public void onTimeSet(TimePicker view, int hourOfDay,
                                                      int minute) {

                                    toTime.setText(hourOfDay + ":" + minute);
                                }
                            }, mHour, mMinute, true);
                    timePickerDialog.show();
                }
            });
            toTime.setText(shift.get("end"));
            row.addView(toTime);
            Spinner spinner=new Spinner(this);
            spinner.setLayoutParams(new TableRow.LayoutParams(440,TableRow.LayoutParams.MATCH_PARENT));
            addEmployeesToSpinner(spinner,shift.get("employee"));
            row.addView(spinner);
            shiftsTable.addView(row);
            button.setVisibility(View.VISIBLE);
        }
    }

    private void addEmployeesToSpinner(final Spinner spinner, final String defaultSelection) {
        DatabaseReference users = firebaseDatabase.getReference().child("users");
        final List<String> employees=new ArrayList<>();
        users.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                for(DataSnapshot s:children) {
                    employees.add(s.getKey());
                }
                ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(CreateSchedule.this,
                        android.R.layout.simple_spinner_item, employees);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(dataAdapter);
                if(defaultSelection!=null) {
                    spinner.setSelection(dataAdapter.getPosition(defaultSelection));
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
