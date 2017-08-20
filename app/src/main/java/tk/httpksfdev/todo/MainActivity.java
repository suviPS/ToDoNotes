package tk.httpksfdev.todo;


import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import tk.httpksfdev.todo.data.ToDoContract;
import tk.httpksfdev.todo.widgets.WidgetUtils;

import static tk.httpksfdev.todo.CustomCursorAdapter.FINISHED_ADD;

public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<MergeCursor> {

    private RecyclerView recyclerView;
    private CustomCursorAdapter mAdapter;

    private final int LOADER_ID = 0;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        recyclerView = (RecyclerView) findViewById(R.id.recycler_viev);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new CustomCursorAdapter(this);
        recyclerView.setAdapter(mAdapter);

        // add ItemTouchHelper to handle swipe and drag of elements
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                //do nothing for now
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                //
                int id = (int) viewHolder.itemView.getTag();
                //if viewHolder is To Do
                if(id < FINISHED_ADD){
                    String stringId = Integer.toString(id);
                    Uri uri = ToDoContract.ToDoEntry.CONTENT_URI;
                    uri = uri.buildUpon().appendPath(stringId).build();
                    getContentResolver().delete(uri, null, null);


                } else {
                    final int idOld = id - FINISHED_ADD;
                    String stringId = Integer.toString(idOld);
                    Uri uri = ToDoContract.ToDoEntryOld.CONTENT_URI_OLD;
                    uri = uri.buildUpon().appendPath(stringId).build();
                    final Uri uriFinal = uri;

                    Cursor cursor = getContentResolver().query(uri, null, null, null, null);
                    cursor.moveToFirst();
                    final String name  = cursor.getString(cursor.getColumnIndex(ToDoContract.ToDoEntryOld.COLUMN_INFO));
                    final String desc = cursor.getString(cursor.getColumnIndex(ToDoContract.ToDoEntryOld.COLUMN_DESC));
                    final int priority = cursor.getInt(cursor.getColumnIndex(ToDoContract.ToDoEntryOld.COLUMN_PRIORITY));

                    new AlertDialog.Builder(mContext, android.R.style.Theme_Material_Dialog_NoActionBar)
                            .setTitle("\t" + name)
                            .setMessage(desc)
                            .setPositiveButton("Cancel", null)
                            .setNegativeButton("Move to active pool", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //add to active ToDos
                                    ContentValues cv = new ContentValues();
                                    cv.put(ToDoContract.ToDoEntry.COLUMN_INFO, name);
                                    cv.put(ToDoContract.ToDoEntry.COLUMN_DESC, desc);
                                    cv.put(ToDoContract.ToDoEntry.COLUMN_PRIORITY, priority);
                                    getContentResolver().insert(ToDoContract.ToDoEntry.CONTENT_URI, cv);

                                    //remove from ToDoOld
                                    getContentResolver().delete(uriFinal, null, null);

                                    //notify loader
                                    getSupportLoaderManager().restartLoader(LOADER_ID, null, MainActivity.this);
                                    //update widget
                                    WidgetUtils.updateDataWidgetToDo(getApplicationContext());
                                }
                            })
                            .show();
                }
                getSupportLoaderManager().restartLoader(LOADER_ID, null, MainActivity.this);
                //update widget
                WidgetUtils.updateDataWidgetToDo(getApplicationContext());
            }
        }).attachToRecyclerView(recyclerView);


        findViewById(R.id.floating_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AddEntryActivity.class);
                startActivity(intent);
            }
        });

        getSupportLoaderManager().initLoader(LOADER_ID, null, this);
    }


    @Override
    public Loader<MergeCursor> onCreateLoader(final int id, Bundle args) {
        return new AsyncTaskLoader<MergeCursor>(this) {

            MergeCursor mData = null;

            @Override
            protected void onStartLoading() {
                if(mData != null){
                    // Delivers any previously loaded data immediately
                    deliverResult(mData);
                } else {
                    // Force new load
                    forceLoad();
                }
            }

            @Override
            public MergeCursor loadInBackground() {
                try{
//                    String whereClause = null;
                            Cursor toDo =  getContentResolver().query(ToDoContract.ToDoEntry.CONTENT_URI,
                                    null,
                                    null,
                                    null,
                                    ToDoContract.ToDoEntry.COLUMN_PRIORITY);
                            Cursor toDoOld = getContentResolver().query(ToDoContract.ToDoEntryOld.CONTENT_URI_OLD,
                                    null,
                                    null,
                                    null,
                                    ToDoContract.ToDoEntryOld._ID + " DESC");

                    MergeCursor mergeCursor = new MergeCursor(new Cursor[] {toDo, toDoOld});

                    //loader will read data from MergeCursor
                    return mergeCursor;

                } catch (Exception e){
                    e.printStackTrace();
                    return null;
                }
            }

            // deliverResult sends the result of the load, a Cursor, to the registered listener
            @Override
            public void deliverResult(MergeCursor data) {
                mData = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<MergeCursor> loader, MergeCursor data) {
        // Update the data that the adapter uses to create ViewHolders
        if(loader.getId() == LOADER_ID)
         mAdapter.swapCursor(data);
    }


    @Override
    public void onLoaderReset(Loader<MergeCursor> loader) {
            mAdapter.swapCursor(null);
    }


    @Override
    protected void onResume() {
        super.onResume();

        // re-queries for all tasks
        getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_NoActionBar)
                .setTitle("\tAbout")
                .setMessage("Created by: Petar Suvajac")
                .setPositiveButton("Ok", null)
                .setNegativeButton("Send email", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendMeEmail();
                    }
                })
                .show();

        return true;
    }

    public void sendMeEmail(){
        //email intent
        String mail[]={"petars38@gmail.com"};
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setType("text/plain");
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, mail);
        intent.putExtra(Intent.EXTRA_SUBJECT, "ToDo / Note");
        //startActivity(intent);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(Intent.createChooser(intent, "Send email"));
        } else {
            Toast.makeText(this, "Mail me at: petars38@gmail.com", Toast.LENGTH_LONG).show();
        }
    }
}
