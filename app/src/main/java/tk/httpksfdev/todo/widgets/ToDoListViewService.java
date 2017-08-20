package tk.httpksfdev.todo.widgets;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import tk.httpksfdev.todo.R;
import tk.httpksfdev.todo.data.ToDoContract;

/**
 * Class for ListView in To Do widget
 */

public class ToDoListViewService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ListViewRemoteListViewsFactory(getApplicationContext());
    }
}


    //Views Factory
    class ListViewRemoteListViewsFactory implements RemoteViewsService.RemoteViewsFactory {

        private Context mContext;
        private Cursor mCursor;

        public ListViewRemoteListViewsFactory(Context applicationContext) {
            mContext = applicationContext;
        }


        @Override
        public void onDataSetChanged() {
            //requery database and update views
            if(mCursor != null)
                mCursor.close();

            //selest just active To Do, priority <5
            String whereClause = ToDoContract.ToDoEntry.COLUMN_PRIORITY + "<5";
            mCursor = mContext.getContentResolver().query(ToDoContract.ToDoEntry.CONTENT_URI,
                    null,
                    whereClause,
                    null,
                    ToDoContract.ToDoEntry.COLUMN_PRIORITY);

        }



        //acts like the onBindViewHolder method in an Adapter
        @Override
        public RemoteViews getViewAt(int position) {

            if (mCursor == null || mCursor.getCount() == 0) return null;
            mCursor.moveToPosition(position);

            String id = ""+ mCursor.getInt(mCursor.getColumnIndex(ToDoContract.ToDoEntry._ID));
            String info = mCursor.getString(mCursor.getColumnIndex(ToDoContract.ToDoEntry.COLUMN_INFO));

            RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.to_do_widget_single_item);

            views.setTextViewText(R.id.widget_todo_item_textview, info);

            //add FillInIntents
            Bundle extras = new Bundle();
            extras.putString(ToDoClickIntentService.EXTRA_TODO_ITEM_ID, id);
            extras.putString(ToDoClickIntentService.EXTRA_TODO_ITEM_INFO, info);

            Intent intent01 = new Intent();
            Intent intent02 = new Intent();
            intent01.putExtras(extras);
            intent02.putExtras(extras);

            intent01.setAction(ToDoClickIntentService.ACTION_CLICK_ITEM_CLICK);
            views.setOnClickFillInIntent(R.id.widget_todo_item_textview, intent01);

            intent02.setAction(ToDoClickIntentService.ACTION_CLICK_TODO_ITEM_DONE);
            views.setOnClickFillInIntent(R.id.widget_todo_item_imageview, intent02);


            return views;
        }



        @Override
        public void onCreate() {
        }

        @Override
        public void onDestroy() {
            mCursor.close();
        }

        @Override
        public int getCount() {
            if(mCursor == null)
                return 0;
            return mCursor.getCount();
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

    }
