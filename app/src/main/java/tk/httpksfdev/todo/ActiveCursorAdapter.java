package tk.httpksfdev.todo;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import tk.httpksfdev.todo.data.ToDoContract;

/**
 *  Adapter for Active To Dos / Notes
 */


class ActiveCursorAdapter extends RecyclerView.Adapter<ActiveCursorAdapter.ActiveViewHolder> {

    private Context mContext;
    private Cursor mCursor;

    public ActiveCursorAdapter(Context context){
        mContext = context;
    }

    @Override
    public ActiveViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the item_layout to a view
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_layout, parent, false);
        return new ActiveViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ActiveViewHolder holder, int position) {
        try{
            // get data
            mCursor.moveToPosition(position);

            final int id = mCursor.getInt(mCursor.getColumnIndex(ToDoContract.ToDoEntry._ID));
            String info = mCursor.getString(mCursor.getColumnIndex(ToDoContract.ToDoEntry.COLUMN_INFO));
            int priority = mCursor.getInt(mCursor.getColumnIndex(ToDoContract.ToDoEntry.COLUMN_PRIORITY));

            // set values
            holder.itemView.setTag(id);
            holder.infoTextView.setText(info);

            // get background color
            GradientDrawable priorityCircle = (GradientDrawable) holder.priorityView1.getBackground();
            int priorityColor = getPriorityColor(priority);
            priorityCircle.setColor(priorityColor);

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    // priority circle color
    //P1 = red, P2 = orange, P3 = yellow, P5 = blue
    private int getPriorityColor(int priority) {
        int priorityColor = 0;

        switch(priority) {
            case 1: priorityColor = ContextCompat.getColor(mContext, R.color.priority_1);
                break;
            case 2: priorityColor = ContextCompat.getColor(mContext, R.color.priority_2);
                break;
            case 3: priorityColor = ContextCompat.getColor(mContext, R.color.priority_3);
                break;
            case 5: priorityColor = ContextCompat.getColor(mContext, R.color.priority_5);
            default: break;
        }
        return priorityColor;
    }


    @Override
    public int getItemCount() {
        if (mCursor == null) {
            return 0;
        }
        return mCursor.getCount();
    }

    Cursor swapCursor(Cursor c) {
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


    class ActiveViewHolder extends RecyclerView.ViewHolder {
        // Class variables for the task description and priority TextViews
        TextView infoTextView;
        TextView priorityView1;

        ActiveViewHolder(View itemView) {
            super(itemView);

            infoTextView = itemView.findViewById(R.id.item_info);
            priorityView1 = itemView.findViewById(R.id.item_priority);

            //if clicked go to EditActivity
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //start EditEntryActivity with right item id
                    mCursor.moveToPosition(getAdapterPosition());
                    String id = "" + mCursor.getInt(mCursor.getColumnIndex(ToDoContract.ToDoEntry._ID));
                    Intent intent = new Intent(mContext, EditEntryActivity.class);
                    intent.putExtra(MyUtils.EXTRA_ITEM_ID, id);
                    mContext.startActivity(intent);
                }
            });
        }
    }
}
