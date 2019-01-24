package tk.httpksfdev.todo.notifications;

import android.database.Cursor;
import android.net.Uri;
import com.evernote.android.job.Job;

import androidx.annotation.NonNull;
import tk.httpksfdev.todo.data.ToDoContract;

public class NotificationJob extends Job {
    public static final String TAG = "NotificationJob";
    public static final String EXTRA_ENTRY_ID = "entry_id";

    @NonNull
    @Override
    protected Result onRunJob(@NonNull Params params) {
        String id = params.getExtras().getString(EXTRA_ENTRY_ID, "");
        if(id.isEmpty())
            return Result.SUCCESS;

        Uri uri = ToDoContract.ToDoEntry.CONTENT_URI.buildUpon().appendPath(id).build();
        Cursor mCursor = getContext().getContentResolver().query(uri, null, null, null, null);
        if(mCursor != null){
            if(mCursor.getCount() > 0){
                mCursor.moveToFirst();
                String info = mCursor.getString(mCursor.getColumnIndex(ToDoContract.ToDoEntry.COLUMN_INFO));
                String desc = mCursor.getString(mCursor.getColumnIndex(ToDoContract.ToDoEntry.COLUMN_DESC));
                int priority = mCursor.getInt(mCursor.getColumnIndex(ToDoContract.ToDoEntry.COLUMN_PRIORITY));
                //send notification
                MyNotificationUtil.sendNotification(getContext(), Integer.valueOf(id), info, desc, priority);
            }
            mCursor.close();
        }

        // run your job here
        return Result.SUCCESS;
    }
}
