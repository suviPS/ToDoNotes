package tk.httpksfdev.todo;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import tk.httpksfdev.todo.data.ToDoContract;
import tk.httpksfdev.todo.widgets.WidgetUtils;

public class AddEntryActivity extends AppCompatActivity {

    private EditText editTextInfo;
    private EditText editTextDesc;

    private int mPriority;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_entry);
        getSupportActionBar().setTitle("ToDo");

        ((RadioButton) findViewById(R.id.radio1)).setChecked(true);
        mPriority = 1;

        editTextInfo = (EditText)findViewById(R.id.editText);
        editTextDesc = (EditText)findViewById(R.id.editTextDesc);

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

        ContentValues cv = new ContentValues();
        cv.put(ToDoContract.ToDoEntry.COLUMN_INFO, input);
        cv.put(ToDoContract.ToDoEntry.COLUMN_DESC, editTextDesc.getText().toString().trim());
        cv.put(ToDoContract.ToDoEntry.COLUMN_PRIORITY, mPriority);

        Uri uri = getContentResolver().insert(ToDoContract.ToDoEntry.CONTENT_URI, cv);
//        if(uri != null){
//            Toast.makeText(this, "Added: " + uri.toString(), Toast.LENGTH_SHORT).show();
//        }

        //update To Do widgets
        WidgetUtils.updateDataWidgetToDo(getApplicationContext());

        finish();
    }


    private void todoRequest(){
        getSupportActionBar().setTitle("ToDo");
        mPriority = 1;
        ((LinearLayout)findViewById(R.id.linearLayout)).setVisibility(View.VISIBLE);
        ((RadioButton)findViewById(R.id.radio1)).setChecked(true);
    }

    private void noteRequest(){
        getSupportActionBar().setTitle("Note");
        mPriority = 5;
        ((LinearLayout)findViewById(R.id.linearLayout)).setVisibility(View.GONE);
    }


    public void onPriorityChanged(View v){
        int id = v.getId();

        switch (id){
            case R.id.radio1:
                mPriority = 1;
                break;
            case R.id.radio2:
                mPriority =2;
                break;
            case R.id.radio3:
                mPriority = 3;
                break;
            default:
                //
        }
    }

}
