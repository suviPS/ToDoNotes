package tk.httpksfdev.todo;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import tk.httpksfdev.todo.data.ToDoContract;
import tk.httpksfdev.todo.widgets.WidgetUtils;

/**
 *  Adapter for Old(Done) To Dos / Notes
 */

class OldCursorAdapter extends RecyclerView.Adapter<OldCursorAdapter.OldViewHolder> {

    private Context mContext;
    private Cursor mCursor;

    public OldCursorAdapter(Context context){
        mContext = context;
    }


    @Override
    public OldViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the item_layout to a view
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_old_layout, parent, false);
        return new OldViewHolder(view);
    }


    @Override
    public void onBindViewHolder(OldViewHolder holder, int position) {
        try{
            // get data
            mCursor.moveToPosition(position);

            final int id = mCursor.getInt(mCursor.getColumnIndex(ToDoContract.ToDoEntryOld._ID));
            String info = mCursor.getString(mCursor.getColumnIndex(ToDoContract.ToDoEntryOld.COLUMN_INFO));
            String desc = mCursor.getString(mCursor.getColumnIndex(ToDoContract.ToDoEntryOld.COLUMN_DESC));

            // set values
            holder.itemView.setTag(id);
            holder.infoTextView.setText(info);
            holder.desc = desc;

        } catch (Exception e){
            e.printStackTrace();
        }
    }


    @Override
    public int getItemCount() {
        if (mCursor == null) {
            return 0;
        }
        return mCursor.getCount();
    }
    public Cursor swapCursor(Cursor c) {
        // check if this cursor is the same as the previous cursor (mCursor)
        if (mCursor == c) {
            return null;
        }
        Cursor temp = mCursor;
        this.mCursor = c; // new cursor value assigned

        //check if this is a valid cursor, then update the cursor
        if (c != null) {
            this.notifyDataSetChanged();
        }
        return temp;
    }



    class OldViewHolder extends RecyclerView.ViewHolder {
        TextView infoTextView;
        String desc = "";

        OldViewHolder(View itemView) {
            super(itemView);

            infoTextView = itemView.findViewById(R.id.item_info_old);

            //if clicked display info
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //get data
                    mCursor.moveToPosition(getAdapterPosition());
                    final String id = "" + mCursor.getInt(mCursor.getColumnIndex(ToDoContract.ToDoEntryOld._ID));
                    final String info  = mCursor.getString(mCursor.getColumnIndex(ToDoContract.ToDoEntryOld.COLUMN_INFO));
                    final String desc = mCursor.getString(mCursor.getColumnIndex(ToDoContract.ToDoEntryOld.COLUMN_DESC));
                    final int priority = mCursor.getInt(mCursor.getColumnIndex(ToDoContract.ToDoEntryOld.COLUMN_PRIORITY));

                    //check API level for AlertDialog theme
                    int theme = -1;
                    if(Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                        theme = android.R.style.Theme_Material_Dialog_NoActionBar;
                    } else{
                            //doesn't support cool dark theme :|
                            theme = android.R.style.Theme_Holo_Dialog_NoActionBar;
                    }
                    new AlertDialog.Builder(mContext, theme)
                                .setTitle("\t" + info)
                                .setMessage(desc)
                                .setPositiveButton("Cancel", null)
                                .setNeutralButton("To Active", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        //add to active ToDos
                                        ContentValues cv = new ContentValues();
                                        cv.put(ToDoContract.ToDoEntry.COLUMN_INFO, info);
                                        cv.put(ToDoContract.ToDoEntry.COLUMN_DESC, desc);
                                        cv.put(ToDoContract.ToDoEntry.COLUMN_PRIORITY, priority);
                                        cv.put(ToDoContract.ToDoEntry.COLUMN_REMINDER, -1);
                                        mContext.getContentResolver().insert(ToDoContract.ToDoEntry.CONTENT_URI, cv);

                                        //remove from ToDoOld
                                        Uri uri = ToDoContract.ToDoEntryOld.CONTENT_URI_OLD.buildUpon().appendPath(id).build();
                                        mContext.getContentResolver().delete(uri, null, null);

                                        //notify loader
                                        ((MainActivity) mContext).onResume();

                                        //update widget
                                        WidgetUtils.updateDataWidgetToDo(mContext);
                                        WidgetUtils.updateDataWidgetNote(mContext);
                                    }
                                })
                                .setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Uri uri = ToDoContract.ToDoEntryOld.CONTENT_URI_OLD.buildUpon().appendPath(id).build();
                                        int delete = mContext.getContentResolver().delete(uri, null, null);
                                        Log.d("TAG+++", "Deleted oldEntry: " + delete);

                                        //notify loader
                                        ((MainActivity) mContext).onResume();
                                    }
                                })
                                .show();
                }
            });
        }
    }

}
