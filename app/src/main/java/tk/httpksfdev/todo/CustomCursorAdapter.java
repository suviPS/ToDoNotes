package tk.httpksfdev.todo;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import tk.httpksfdev.todo.data.ToDoContract;

public class CustomCursorAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Cursor mCursor;
    private Context mContext;

    private static final int TYPE_TODO = 1;
    private static final int TYPE_TODO_OLD = -1;

    //package private access
    static final int FINISHED_ADD = 10000;

    public CustomCursorAdapter(Context mContext) {
        this.mContext = mContext;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        switch (viewType){
            case TYPE_TODO:
                // Inflate the item_layout to a view
                View view1 = LayoutInflater.from(mContext)
                        .inflate(R.layout.item_layout, parent, false);
                return new TaskViewHolder1(view1);
            case TYPE_TODO_OLD:
                // Inflate the item_old_layout to a view
                View view2 = LayoutInflater.from(mContext)
                        .inflate(R.layout.item_old_layout, parent, false);
                return new TaskViewHolder2(view2);
            default:
                return null;

        }

    }



    //ToDos
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        //check is it Active or Done
        switch (holder.getItemViewType()){
            case TYPE_TODO:
                try{
                    Log.d("TAG++", "Type_ToDo detected");

                    // Indices for the _id, description, and priority columns
                    int idIndex1 = mCursor.getColumnIndex(ToDoContract.ToDoEntry._ID);
                    int descriptionIndex1 = mCursor.getColumnIndex(ToDoContract.ToDoEntry.COLUMN_INFO);
                    int priorityIndex = mCursor.getColumnIndex(ToDoContract.ToDoEntry.COLUMN_PRIORITY);

                    mCursor.moveToPosition(position); // get to the right location in the cursor

                    // Determine the values of the wanted data
                    final int id1 = mCursor.getInt(idIndex1);
                    String description1 = mCursor.getString(descriptionIndex1);
                    int priority = mCursor.getInt(priorityIndex);

                    //Set value
                    holder.itemView.setTag(id1);
                    ((TaskViewHolder1) holder).taskDescriptionView1.setText(description1);


                    GradientDrawable priorityCircle = (GradientDrawable) ((TaskViewHolder1) holder).priorityView1.getBackground();
                    // Get the appropriate background color based on the priority
                    int priorityColor = getPriorityColor(priority);
                    priorityCircle.setColor(priorityColor);

                } catch (Exception e){
                    e.printStackTrace();
                }
                break;

            case TYPE_TODO_OLD:
                try{
                    Log.d("TAG++", "Type_ToDo_Old detected");

                    // Indices for the _id, description, and priority columns
                    int idIndex2 = mCursor.getColumnIndex(ToDoContract.ToDoEntryOld._ID);
                    int descriptionIndex2 = mCursor.getColumnIndex(ToDoContract.ToDoEntryOld.COLUMN_INFO);

                    mCursor.moveToPosition(position); // get to the right location in the cursor

                    // Determine the values of the wanted data
                    final int id2 = mCursor.getInt(idIndex2) + FINISHED_ADD;
                    String description2 = mCursor.getString(descriptionIndex2);

                    //Set values
                    holder.itemView.setTag(id2);
                    ((TaskViewHolder2) holder).taskDescriptionView2.setText("Done: " + description2);

                } catch (Exception e){
                    e.printStackTrace();
                }
                break;
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



    @Override
    public int getItemViewType(int position) {
        //save current coursor position to navigate there later
        int temp = mCursor.getPosition();

        mCursor.moveToPosition(position);
        int type = mCursor.getInt(mCursor.getColumnIndex(ToDoContract.ToDoEntry.TYPE));
        if(type == 0)
            Log.e("TAG+++", "Error in type");

        mCursor.moveToPosition(temp);
        return type;
    }


    public Cursor swapCursor(Cursor c) {
        // check if this cursor is the same as the previous cursor (mCursor)
        if (mCursor == c) {
            return null; // bc nothing has changed
        }
        Cursor temp = mCursor;
        this.mCursor = c; // new cursor value assigned

        //check if this is a valid cursor, then update the cursor
        if (c != null) {
            this.notifyDataSetChanged();
        }
        return temp;
    }


    // ToDos
    class TaskViewHolder1 extends RecyclerView.ViewHolder {

        // Class variables for the task description and priority TextViews
        TextView taskDescriptionView1;
        TextView priorityView1;

        public TaskViewHolder1(View itemView) {
            super(itemView);

            taskDescriptionView1 = (TextView) itemView.findViewById(R.id.item_info);
            priorityView1 = (TextView) itemView.findViewById(R.id.item_priority);

            //Display description of ToDoItem if clicked
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

    // old ToDos
    class TaskViewHolder2 extends RecyclerView.ViewHolder {
        TextView taskDescriptionView2;

        public TaskViewHolder2(View itemView) {
            super(itemView);
            taskDescriptionView2 = (TextView) itemView.findViewById(R.id.item_info_old);
        }
    }


}