package tk.httpksfdev.todo.notifications;

import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

import tk.httpksfdev.todo.data.ToDoContract;


public class NotificationJobService extends JobService {

    // executed on main thread, but we have quite fast operation here; no need for background stuff (AsyncTask etc.)
    @Override
    public boolean onStartJob(JobParameters job) {

        String id = job.getTag();
        Log.d("TAG+++", "Job id: " + id);

        Uri uri = ToDoContract.ToDoEntry.CONTENT_URI.buildUpon().appendPath(id).build();
        Cursor mCursor = getContentResolver().query(uri, null, null, null, null);
        if(mCursor != null){
            mCursor.moveToFirst();
            String info = mCursor.getString(mCursor.getColumnIndex(ToDoContract.ToDoEntry.COLUMN_INFO));
            String desc = mCursor.getString(mCursor.getColumnIndex(ToDoContract.ToDoEntry.COLUMN_DESC));
            int priority = mCursor.getInt(mCursor.getColumnIndex(ToDoContract.ToDoEntry.COLUMN_PRIORITY));
            Log.d("TAG+++", "Info: " + info + " | Desc: " + desc + " | Priority: " + priority);

            //send notification
            MyNotificationUtil.sendNotification(getApplicationContext(), Integer.valueOf(id), info, desc, priority);
        }


        //is there anything else to do, any background work still running?
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        //if failed, should we retry?
        return true;
    }
}
