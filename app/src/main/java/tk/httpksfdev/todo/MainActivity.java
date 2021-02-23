package tk.httpksfdev.todo;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageSwitcher;
import android.widget.Toast;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import tk.httpksfdev.todo.data.ToDoContract;
import tk.httpksfdev.todo.notifications.MyNotificationUtil;
import tk.httpksfdev.todo.widgets.WidgetUtils;

public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private SlidingUpPanelLayout slidingUpPanelLayout;
    private RecyclerView recyclerView;
    private RecyclerView recyclerViewOld;

    private ActiveCursorAdapter mAdapter;
    private OldCursorAdapter mAdapterOld;

    private final int LOADER_ACTIVE_ID = 0;
    private final int LOADER_OLD_ID = 1;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        slidingUpPanelLayout = findViewById(R.id.sliding_layout);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new ActiveCursorAdapter(this);
        recyclerView.setAdapter(mAdapter);

        recyclerViewOld = findViewById(R.id.recycler_view_old);
        recyclerViewOld.setLayoutManager(new LinearLayoutManager(this));
        mAdapterOld = new OldCursorAdapter(this);
        recyclerViewOld.setAdapter(mAdapterOld);


        // add helper to delete entry on swipe
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                //do nothing
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                //delete entry
                String id = "" + viewHolder.itemView.getTag();
                Uri uri = ToDoContract.ToDoEntry.CONTENT_URI.buildUpon().appendPath(id).build();
                getContentResolver().delete(uri, null, null);

                //cancel reminder if one is active
                MyNotificationUtil.cancelNotification(getApplicationContext(), ""+ id);

                //notify loaders
                LoaderManager.getInstance(MainActivity.this).restartLoader(LOADER_ACTIVE_ID, null, MainActivity.this);
                LoaderManager.getInstance(MainActivity.this).restartLoader(LOADER_OLD_ID, null, MainActivity.this);

                //update widget
                WidgetUtils.updateDataWidgetToDo(getApplicationContext());
                WidgetUtils.updateDataWidgetNote(getApplicationContext());
            }
        }).attachToRecyclerView(recyclerView);

        //listener for AddEntry
        findViewById(R.id.floating_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AddEntryActivity.class);
                startActivity(intent);
            }
        });

        //listener for arrow animation
        slidingUpPanelLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                //
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                // change arrow with animation
                if(SlidingUpPanelLayout.PanelState.EXPANDED.equals(newState)){
                    ((ImageSwitcher)findViewById(R.id.arrow_imageswitcher)).setImageResource(R.drawable.ic_arrow_down);
                } else if(SlidingUpPanelLayout.PanelState.COLLAPSED.equals(newState)){
                    ((ImageSwitcher)findViewById(R.id.arrow_imageswitcher)).setImageResource(R.drawable.ic_arrow_up);
                }
            }
        });


        LoaderManager.getInstance(MainActivity.this).initLoader(LOADER_ACTIVE_ID, null, this);
        LoaderManager.getInstance(MainActivity.this).initLoader(LOADER_OLD_ID, null, this);
    }


    @Override
    public Loader<Cursor> onCreateLoader(final int id, Bundle args) {
        return new AsyncTaskLoader<Cursor>(this) {
            Cursor mData = null;

            @Override
            protected void onStartLoading() {
                if(mData != null){
                    deliverResult(mData);
                } else {
                    forceLoad();
                }
            }

            @Override
            public Cursor loadInBackground() {
                try{
                    Cursor data = null;
                    if(id == LOADER_ACTIVE_ID){
                        data = getContentResolver().query(ToDoContract.ToDoEntry.CONTENT_URI,
                                null,
                                null,
                                null,
                                ToDoContract.ToDoEntry.COLUMN_PRIORITY);

                    } else if (id == LOADER_OLD_ID){
                        data = getContentResolver().query(ToDoContract.ToDoEntryOld.CONTENT_URI_OLD,
                                null,
                                null,
                                null,
                                ToDoContract.ToDoEntryOld._ID + " DESC");
                    }
                    return data;

                } catch (Exception e){
                    e.printStackTrace();
                    return null;
                }
            }

            // deliver result to the registered listener
            @Override
            public void deliverResult(Cursor data) {
                mData = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(loader.getId() == LOADER_ACTIVE_ID)
            mAdapter.swapCursor(data);
        else if(loader.getId() == LOADER_OLD_ID)
            mAdapterOld.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
        mAdapterOld.swapCursor(null);
    }

    @Override
    protected void onResume() {
        super.onResume();

        LoaderManager.getInstance(MainActivity.this).restartLoader(LOADER_ACTIVE_ID, null, this);
        LoaderManager.getInstance(MainActivity.this).restartLoader(LOADER_OLD_ID, null, this);
    }

    @Override
    public void onBackPressed() {
        if(slidingUpPanelLayout.getPanelState().equals(SlidingUpPanelLayout.PanelState.EXPANDED)){
            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            return;
        }

        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(R.id.main_menu_export == item.getItemId()){
            startActivity(new Intent(MainActivity.this, BackupActivity.class));
        } else {
            //Info option
            //check API level
            int theme = -1;
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                theme = android.R.style.Theme_Material_Dialog_NoActionBar;
            } else{
                //doesn't support cool theme :|
                theme = android.R.style.Theme_Holo_Dialog_NoActionBar;
            }
            new AlertDialog.Builder(this, theme)
                    .setTitle("\tAbout")
                    .setMessage(getResources().getString(R.string.info_text) + "\n\nCreated by: Petar Suvajac")
                    .setPositiveButton("Ok", null)
                    .setNegativeButton("GitHub", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            goToGithub();
                        }
                    })
                    .setNeutralButton("Send email", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            sendMeEmail();
                        }
                    })
                    .show();
        }


        return true;
    }

    private void sendMeEmail(){
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

    private void goToGithub(){
        String gitUrl = "https://github.com/suviPS/ToDoNotes";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(gitUrl));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(getApplicationContext(), "Visit us at: " + gitUrl, Toast.LENGTH_LONG).show();
        }
    }


}
