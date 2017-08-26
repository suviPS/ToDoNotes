package tk.httpksfdev.todo;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;

import tk.httpksfdev.todo.data.ToDoContract;
import tk.httpksfdev.todo.widgets.WidgetUtils;

import static tk.httpksfdev.todo.notifications.MyNotificationUtil.scheduleNotification;

public class AddEntryActivity extends AppCompatActivity {

    private EditText editTextInfo;
    private EditText editTextDesc;
    private CheckBox reminderCheckBox;

    private int mPriority;
    private long mReminder = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_entry);
        getSupportActionBar().setTitle("ToDo");

        ((RadioButton) findViewById(R.id.addentry_radio1)).setChecked(true);
        mPriority = 1;

        editTextInfo = (EditText)findViewById(R.id.addentry_editText);
        editTextDesc = (EditText)findViewById(R.id.addentry_editTextDesc);
        reminderCheckBox = (CheckBox) findViewById(R.id.addentry_checkbox_notification);

        editTextInfo.setFocusableInTouchMode(true);
        editTextInfo.requestFocus();

        //is new entry a note?
        Intent intent = getIntent();
        if(intent != null){
            if(MyUtils.ACTION_ADD_NOTE.equals(intent.getAction()))
                noteRequest();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(getSupportActionBar().getTitle().equals("ToDo")) {
            //move to new note
            noteRequest();
        }
        else{
            //move to new To Do
            todoRequest();
        }
        return true;
    }




    public void onAddClicked(View v){
        String input = editTextInfo.getText().toString();

        if(input.length() == 0)
            return;

        if(reminderCheckBox.isChecked()){
            //get real reminder time
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String[] date = sp.getString(MyUtils.PREF_DATE_TEMP, "null##").split("##");
            String[] time = sp.getString(MyUtils.PREF_TIME_TEMP, "null##").split("##");

            if(date.length == 3 && time.length == 2){
                Calendar c2 = Calendar.getInstance();
                c2.set(Calendar.YEAR, Integer.parseInt(date[0]));
                c2.set(Calendar.MONTH, Integer.parseInt(date[1]));
                c2.set(Calendar.DAY_OF_MONTH, Integer.parseInt(date[2]));
                c2.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time[0]));
                c2.set(Calendar.MINUTE, Integer.parseInt(time[1]));
                mReminder = c2.getTimeInMillis();
                Log.d("TAG+++", "mReminder: " + mReminder);
            }
        }


        ContentValues cv = new ContentValues();
        cv.put(ToDoContract.ToDoEntry.COLUMN_INFO, input);
        cv.put(ToDoContract.ToDoEntry.COLUMN_DESC, editTextDesc.getText().toString().trim());
        cv.put(ToDoContract.ToDoEntry.COLUMN_PRIORITY, mPriority);
        cv.put(ToDoContract.ToDoEntry.COLUMN_REMINDER, mReminder);

        Uri uri = getContentResolver().insert(ToDoContract.ToDoEntry.CONTENT_URI, cv);
        String id = "";
        if(uri != null){
            String[] arr = uri.toString().split(ToDoContract.ToDoEntry.TABLE_NAME + "/");
            id = arr[1];
            Log.d("TAG+++", "Added: " + uri + " | id: " + id);

            if(id.equals("")){
                //won't happend
                Log.d("TAG+++", "insert() in AddEntryActivity failed");
            } else{
                //schedule job if notification is set
                scheduleNotification(getApplicationContext(), id);
            }
        }

        //update To Do widgets
        WidgetUtils.updateDataWidgetToDo(getApplicationContext());

        finish();
    }


    private void todoRequest(){
        getSupportActionBar().setTitle("ToDo");
        mPriority = 1;
        ((LinearLayout)findViewById(R.id.addentry_priority_linearLayout)).setVisibility(View.VISIBLE);
        ((RadioButton)findViewById(R.id.addentry_radio1)).setChecked(true);
    }

    private void noteRequest(){
        getSupportActionBar().setTitle("Note");
        mPriority = 5;
        ((LinearLayout)findViewById(R.id.addentry_priority_linearLayout)).setVisibility(View.GONE);
    }


    public void onPriorityChanged(View v){
        int id = v.getId();

        switch (id){
            case R.id.addentry_radio1:
                mPriority = 1;
                break;
            case R.id.addentry_radio2:
                mPriority =2;
                break;
            case R.id.addentry_radio3:
                mPriority = 3;
                break;
            default:
                //
        }
    }

    public void onEditTextClicked(View v){
        //remove hint
        EditText et = (EditText)v;
        et.setHint("");
    }


    public void reminderCheckboxClicked(View v){
        final TextView dataTextView = (TextView) findViewById(R.id.addentry_datapicker_textview);
        final TextView timeTextView = (TextView) findViewById(R.id.addentry_timepicker_textview);
        TextView atTextView = (TextView) findViewById(R.id.addentry_textview03);

        if(reminderCheckBox.isChecked()){
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(calendar.getTimeInMillis() + (1000 * 60 * 60));
            final int mYear = calendar.get(Calendar.YEAR);
            final int mMonth = calendar.get(Calendar.MONTH);
            final int mDay = calendar.get(Calendar.DAY_OF_MONTH);
            final int mHour = calendar.get(Calendar.HOUR_OF_DAY);
            final int mMinutes = calendar.get(Calendar.MINUTE);


            //display textViews
            dataTextView.setText(mDay + "/" + mMonth + "/" + mYear);
            timeTextView.setText(mHour + ":" + mMinutes);

            dataTextView.setVisibility(View.VISIBLE);
            timeTextView.setVisibility(View.VISIBLE);
            atTextView.setVisibility(View.VISIBLE);


            //write to sp
            String dateString = mYear + "##" + mMonth + "##" + mDay;
            PreferenceManager.getDefaultSharedPreferences(AddEntryActivity.this).edit().putString(MyUtils.PREF_DATE_TEMP, dateString).commit();
            String timeString = mHour + "##" + mMinutes;
            PreferenceManager.getDefaultSharedPreferences(AddEntryActivity.this).edit().putString(MyUtils.PREF_TIME_TEMP, timeString).commit();


            //add listeners for date/time changes
            dataTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //display data picker
                    DatePickerDialog datePickerDialog = new DatePickerDialog(AddEntryActivity.this,
                            new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                    //edit textView
                                    dataTextView.setText(dayOfMonth + "/" + monthOfYear + "/" + year);

                                    //write to sp
                                    String dateString = year + "##" + monthOfYear + "##" + dayOfMonth;
                                    PreferenceManager.getDefaultSharedPreferences(AddEntryActivity.this).edit().putString(MyUtils.PREF_DATE_TEMP, dateString).commit();
                                }
                            }, mYear, mMonth, mDay);
                    datePickerDialog.show();
                }
            });

            timeTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //display time picker
                    TimePickerDialog timePickerDialog = new TimePickerDialog(AddEntryActivity.this, new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            //edit textView
                            timeTextView.setText(hourOfDay + ":" + minute);

                            //write to sp
                            String timeString = hourOfDay + "##" + minute;
                            PreferenceManager.getDefaultSharedPreferences(AddEntryActivity.this).edit().putString(MyUtils.PREF_TIME_TEMP, timeString).commit();
                        }
                    }, mHour, mMinutes, true);
                    timePickerDialog.show();
                }
            });

        } else {
            //hide textviews
            dataTextView.setVisibility(View.GONE);
            timeTextView.setVisibility(View.GONE);
            atTextView.setVisibility(View.GONE);
            mReminder = -1;
        }

    }

}
