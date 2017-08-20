package tk.httpksfdev.todo;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import tk.httpksfdev.todo.data.ToDoContract;
import tk.httpksfdev.todo.widgets.WidgetUtils;

public class EditEntryActivity extends AppCompatActivity {

    private EditText editTextInfo;
    private EditText editTextDesc;

    private int mId;
    private String mInfo;
    private String mDesc;
    private int mPriority;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_entry);
        getSupportActionBar().setTitle("Edit item");
        editTextInfo = (EditText)findViewById(R.id.editentry_editText);
        editTextDesc = (EditText)findViewById(R.id.editentry_editTextDesc);

        Intent intent = getIntent();
        if(intent == null){
            //won't happend
            Log.d("TAG+++", "No data passed to EditEntryActivity");
            finish();
        }
        //get id from intent
        String id = intent.getStringExtra(MyUtils.EXTRA_ITEM_ID);

        //query database and set data
        Uri uri = ToDoContract.ToDoEntry.CONTENT_URI.buildUpon().appendPath(id).build();
        Cursor mCursor = getContentResolver().query(uri, null, null, null, null);
        mCursor.moveToFirst();

        mId = Integer.valueOf(id);
        mInfo = mCursor.getString(mCursor.getColumnIndex(ToDoContract.ToDoEntry.COLUMN_INFO));
        mDesc = mCursor.getString(mCursor.getColumnIndex(ToDoContract.ToDoEntry.COLUMN_DESC));
        mPriority =  mCursor.getInt(mCursor.getColumnIndex(ToDoContract.ToDoEntryOld.COLUMN_PRIORITY));

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

        //update item in db
        ContentValues cv = new ContentValues();
        cv.put(ToDoContract.ToDoEntry.COLUMN_INFO, mInfo);
        cv.put(ToDoContract.ToDoEntry.COLUMN_DESC, mDesc);
        cv.put(ToDoContract.ToDoEntry.COLUMN_PRIORITY, mPriority);

        Uri uri = ToDoContract.ToDoEntry.CONTENT_URI.buildUpon().appendPath(""+mId).build();
        int num = getContentResolver().update(uri, cv, null, new String[]{""+mId});
        Log.d("TAG+++", "num: " + num);


        //update widget
        WidgetUtils.updateDataWidgetToDo(getApplicationContext());

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
    }
}
