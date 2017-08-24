package tk.httpksfdev.todo.widgets.widget_note;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import tk.httpksfdev.todo.R;
import tk.httpksfdev.todo.data.ToDoContract;
import tk.httpksfdev.todo.widgets.WidgetUtils;

public class ChooseNoteForWidgetActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>{

    private RecyclerView recyclerView;
    private ThisSmallAdapter mAdapter;

    private final int LOADER_ID = 3;

    private int widgetId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_note_for_widget);

        //get id of widget that started chooser
        Intent intent = getIntent();
        if(intent != null)
            widgetId = intent.getIntExtra(WidgetUtils.EXTRA_WIDGET_ID, -1);
        if(widgetId == -1){
            //won't happend
            Log.d("TAG+++", "WidgetId not sent to ChooseNoteForWidgetActivity");
            finish();
        }


        recyclerView = (RecyclerView)findViewById(R.id.widget_note_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new ThisSmallAdapter(widgetId);
        recyclerView.setAdapter(mAdapter);

        getSupportLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }




    //Loader methods
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
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
                    String whereClause = ToDoContract.ToDoEntry.COLUMN_PRIORITY + "=5";
                    Cursor notes =  getContentResolver().query(ToDoContract.ToDoEntry.CONTENT_URI,
                            null,
                            whereClause,
                            null,
                            null);
                    return notes;

                } catch (Exception e){
                    e.printStackTrace();
                    return null;
                }
            }

            // deliverResult sends the result of the load, a Cursor, to the registered listener
            @Override
            public void deliverResult(Cursor data) {
                mData = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(loader.getId() == LOADER_ID)
            mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }




    //Adapter
    class ThisSmallAdapter extends RecyclerView.Adapter<ThisSmallAdapter.SmallViewHolder>{

        private Cursor mCursor;
        private int widgetId;

        public ThisSmallAdapter(int widgetID){
            widgetId = widgetID;
        }

        @Override
        public SmallViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view01 = LayoutInflater.from(getApplicationContext())
                    .inflate(R.layout.item_for_choose_note, parent, false);
            return new SmallViewHolder(view01);
        }

        @Override
        public void onBindViewHolder(SmallViewHolder holder, int position) {
            mCursor.moveToPosition(position);
            holder.textView.setText(mCursor.getString(mCursor.getColumnIndex(ToDoContract.ToDoEntry.COLUMN_INFO)));
            final int id = mCursor.getInt(mCursor.getColumnIndex(ToDoContract.ToDoEntry._ID));
            holder.textView.setTag(id);
            holder.textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //edit sp
                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putInt(WidgetUtils.PREF_WIDGET_NOTE + widgetId, (Integer) v.getTag()).commit();
                    WidgetUtils.updateDataWidgetNote(getApplicationContext());

                    //go to launcher
                    Intent startMain = new Intent(Intent.ACTION_MAIN);
                    startMain.addCategory(Intent.CATEGORY_HOME);
                    startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(startMain);
                }
            });
        }

        @Override
        public int getItemCount() {
            if(mCursor == null)
                return 0;
            return mCursor.getCount();
        }

        public Cursor swapCursor(Cursor c) {
            // check if this cursor is the same as the previous cursor (mCursor)
            if (mCursor == c) {
                return null;
            }
            Cursor temp = mCursor;
            this.mCursor = c;

            //check if this is a valid cursor, then update the cursor
            if (c != null) {
                this.notifyDataSetChanged();
            }
            return temp;
        }




        //ViewHolder for Adapter
        class SmallViewHolder extends RecyclerView.ViewHolder{
            TextView textView;
            public SmallViewHolder(View itemView) {
                super(itemView);
                textView = (TextView) itemView.findViewById(R.id.widget_note_choose_single_item);
            }
        }

    }
}
