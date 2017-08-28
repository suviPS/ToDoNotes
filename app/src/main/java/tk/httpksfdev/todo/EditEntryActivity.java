package tk.httpksfdev.todo;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
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
import tk.httpksfdev.todo.notifications.MyNotificationUtil;
import tk.httpksfdev.todo.widgets.WidgetUtils;

public class EditEntryActivity extends AppCompatActivity {

    private EditText editTextInfo;
    private EditText editTextDesc;
    private CheckBox reminderCheckBox;

    private int mId;
    private String mInfo;
    private String mDesc;
    private int mPriority;
    private long mReminder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_entry);
        getSupportActionBar().setTitle("Edit item");
        editTextInfo = (EditText)findViewById(R.id.editentry_editText);
        editTextDesc = (EditText)findViewById(R.id.editentry_editTextDesc);
        reminderCheckBox = (CheckBox) findViewById(R.id.editentry_checkbox_notification);

        Intent intent = getIntent();
        if(intent == null){
            //won't happend
            Log.d("TAG+++", "No data passed to EditEntryActivity");
            finish();
        }
        //get id from intent
        String id = intent.getStringExtra(MyUtils.EXTRA_ITEM_ID);

        //query database
        Uri uri = ToDoContract.ToDoEntry.CONTENT_URI.buildUpon().appendPath(id).build();
        Cursor mCursor = getContentResolver().query(uri, null, null, null, null);

        if(mCursor == null || mCursor.getCount() == 0){
            //won't happend
            Log.d("TAG+++", "Wrong id passed to EditEntryActivity");
            finish();
        }

        //get data
        mCursor.moveToFirst();
        mId = Integer.valueOf(id);
        mInfo = mCursor.getString(mCursor.getColumnIndex(ToDoContract.ToDoEntry.COLUMN_INFO));
        mDesc = mCursor.getString(mCursor.getColumnIndex(ToDoContract.ToDoEntry.COLUMN_DESC));
        mPriority =  mCursor.getInt(mCursor.getColumnIndex(ToDoContract.ToDoEntry.COLUMN_PRIORITY));
        mReminder = mCursor.getLong(mCursor.getColumnIndex(ToDoContract.ToDoEntry.COLUMN_REMINDER));

        //setUp UI
        setUpUi();

    }


    public void onSaveButton(View v){
        //update item
        mInfo = editTextInfo.getText().toString();
        mDesc = editTextDesc.getText().toString().trim();

        if(mInfo.equals("")){
            //do nothing
            return;
        }

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
        } else{
            mReminder = -1;
            MyNotificationUtil.cancelNotification(getApplicationContext(), ""+ mId);
        }

        //update item in db
        ContentValues cv = new ContentValues();
        cv.put(ToDoContract.ToDoEntry.COLUMN_INFO, mInfo);
        cv.put(ToDoContract.ToDoEntry.COLUMN_DESC, mDesc);
        cv.put(ToDoContract.ToDoEntry.COLUMN_PRIORITY, mPriority);
        cv.put(ToDoContract.ToDoEntry.COLUMN_REMINDER, mReminder);

        Uri uri = ToDoContract.ToDoEntry.CONTENT_URI.buildUpon().appendPath(""+mId).build();
        int num = getContentResolver().update(uri, cv, null, new String[]{""+mId});
        Log.d("TAG+++", "Num: " + num);

        //reschedule job if notification is set
        MyNotificationUtil.scheduleNotification(getApplicationContext(), ""+mId);


        //update widget
        WidgetUtils.updateDataWidgetToDo(getApplicationContext());
        WidgetUtils.updateDataWidgetNote(getApplicationContext());

        finish();
    }


    public void onPriorityChanged(View v){
        int id = v.getId();

        switch (id){
            case R.id.editentry_radio1:
                mPriority = 1;
                break;
            case R.id.editentry_radio2:
                mPriority =2;
                break;
            case R.id.editentry_radio3:
                mPriority = 3;
                break;
            default:
                //
        }
    }

    private void setUpUi(){
        editTextInfo.setText(mInfo);
        editTextDesc.setText(mDesc);

        switch (mPriority){
            case 1:
                ((RadioButton) findViewById(R.id.editentry_radio1)).setChecked(true);
                break;
            case 2:
                ((RadioButton) findViewById(R.id.editentry_radio2)).setChecked(true);
                break;
            case 3:
                ((RadioButton) findViewById(R.id.editentry_radio3)).setChecked(true);
                break;
            case 5:
                ((LinearLayout)findViewById(R.id.editentry_linearLayout)).setVisibility(View.GONE);
                break;
        }

        //reminder setUp
        setUpReminder();
        if(mReminder != -1){
            reminderCheckBox.setChecked(true);
            reminderCheckboxClickedEdit(reminderCheckBox);
        }

    }


    //reminder methods

    //initial stuff for reminder, should be called only once
    private void setUpReminder(){
        final TextView dataTextView = (TextView) findViewById(R.id.editentry_datapicker_textview);
        final TextView timeTextView = (TextView) findViewById(R.id.editentry_timepicker_textview);

        Calendar calendar = Calendar.getInstance();
        if(mReminder != -1 && (calendar.getTimeInMillis() - mReminder) < 0){
            calendar.setTimeInMillis(mReminder);
        } else {
            calendar.setTimeInMillis(calendar.getTimeInMillis() + (1000 * 60 * 60));
        }
        final int mYear = calendar.get(Calendar.YEAR);
        final int mMonth = calendar.get(Calendar.MONTH);
        final int mDay = calendar.get(Calendar.DAY_OF_MONTH);
        final int mHour = calendar.get(Calendar.HOUR_OF_DAY);
        final int mMinutes = calendar.get(Calendar.MINUTE);

        //write to sp
        String dateString = mYear + "##" + mMonth + "##" + mDay;
        PreferenceManager.getDefaultSharedPreferences(EditEntryActivity.this).edit().putString(MyUtils.PREF_DATE_TEMP, dateString).commit();
        String timeString = mHour + "##" + mMinutes;
        PreferenceManager.getDefaultSharedPreferences(EditEntryActivity.this).edit().putString(MyUtils.PREF_TIME_TEMP, timeString).commit();

        //add starting info to textViews
        dataTextView.setText(mDay + "/" + mMonth + "/" + mYear);
        timeTextView.setText(mHour + ":" + mMinutes);

        //add listeners for date/time changes
        dataTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //display data picker
                DatePickerDialog datePickerDialog = new DatePickerDialog(EditEntryActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                //edit textView
                                dataTextView.setText(dayOfMonth + "/" + monthOfYear + "/" + year);

                                //write to sp
                                String dateString = year + "##" + monthOfYear + "##" + dayOfMonth;
                                PreferenceManager.getDefaultSharedPreferences(EditEntryActivity.this).edit().putString(MyUtils.PREF_DATE_TEMP, dateString).commit();
                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });

        timeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //display time picker
                TimePickerDialog timePickerDialog = new TimePickerDialog(EditEntryActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        //edit textView
                        timeTextView.setText(hourOfDay + ":" + minute);

                        //write to sp
                        String timeString = hourOfDay + "##" + minute;
                        PreferenceManager.getDefaultSharedPreferences(EditEntryActivity.this).edit().putString(MyUtils.PREF_TIME_TEMP, timeString).commit();
                    }
                }, mHour, mMinutes, true);
                timePickerDialog.show();
            }
        });
    }

    public void reminderCheckboxClickedEdit(View v){
        TextView dataTextView = (TextView) findViewById(R.id.editentry_datapicker_textview);
        TextView timeTextView = (TextView) findViewById(R.id.editentry_timepicker_textview);
        TextView atTextView = (TextView) findViewById(R.id.editentry_textview03);

        if(reminderCheckBox.isChecked()){
            //show textviews
            dataTextView.setVisibility(View.VISIBLE);
            timeTextView.setVisibility(View.VISIBLE);
            atTextView.setVisibility(View.VISIBLE);

        } else {
            //hide textviews
            dataTextView.setVisibility(View.GONE);
            timeTextView.setVisibility(View.GONE);
            atTextView.setVisibility(View.GONE);
            mReminder = -1;
        }
    }

    //menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.edit_menu_done) {
            //set done icon
            item.setIcon(R.drawable.ic_done_02);

            //delete item from active pool
            Uri uri = ToDoContract.ToDoEntry.CONTENT_URI.buildUpon().appendPath(""+ mId).build();
            getContentResolver().delete(uri, null, null);

            //notify data change
            WidgetUtils.updateDataWidgetToDo(this);
            WidgetUtils.updateDataWidgetNote(this);

            finish();
        }
        return true;
    }

}
