package tk.httpksfdev.todo.widgets.widget_todo;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import tk.httpksfdev.todo.AddEntryActivity;
import tk.httpksfdev.todo.EditEntryActivity;
import tk.httpksfdev.todo.MainActivity;
import tk.httpksfdev.todo.MyUtils;
import tk.httpksfdev.todo.data.ToDoContract;
import tk.httpksfdev.todo.notifications.MyNotificationUtil;
import tk.httpksfdev.todo.widgets.WidgetUtils;


/**
 * IntentService to handle clicks on To Do widget
 */

public class ToDoClickIntentService extends JobIntentService {

    // action
    public static final String ACTION_CLICK_TODO_TOOLBAR_TODO = "tk.httpksfdev.todo.widgets.action.TOOLBAR_TODO";
    public static final String ACTION_CLICK_TODO_TOOLBAR_ADD = "tk.httpksfdev.todo.widgets.action.TOOLBAR_ADD";
    public static final String ACTION_CLICK_TODO_ITEM_CLICK = "tk.httpksfdev.todo.widgets.action.ITEM_CLICK";
    public static final String ACTION_CLICK_TODO_ITEM_DONE = "tk.httpksfdev.todo.widgets.action.ITEM_DONE";

    // params
    public static final String EXTRA_TODO_ITEM_ID = "tk.httpksfdev.todo.widgets.extra.TODO_ID";
    public static final String EXTRA_TODO_ITEM_INFO = "tk.httpksfdev.todo.widgets.extra.TODO_INFO";


    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, ToDoClickIntentService.class, 22, work);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        final String action = intent.getAction();
        Log.d("TAG+++", "action: " + action);

        if (ACTION_CLICK_TODO_TOOLBAR_TODO.equals(action)) {
            // start main activity
            Intent tempIntent = new Intent(getApplicationContext(), MainActivity.class);
            tempIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(tempIntent);

        } else if (ACTION_CLICK_TODO_TOOLBAR_ADD.equals(action)) {
            //start AddEntryActivity activity
            Intent tempIntent = new Intent(getApplicationContext(), AddEntryActivity.class);
            tempIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(tempIntent);

        } else if (ACTION_CLICK_TODO_ITEM_CLICK.equals(action)) {
            //start editEntry activity
            String id = intent.getStringExtra(EXTRA_TODO_ITEM_ID);

            Intent tempIntent = new Intent(getApplicationContext(), EditEntryActivity.class);
            tempIntent.putExtra(MyUtils.EXTRA_ITEM_ID, id);
            tempIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(tempIntent);

        } else if (ACTION_CLICK_TODO_ITEM_DONE.equals(action)) {
            //delete from active, refresh data
            String id = intent.getStringExtra(EXTRA_TODO_ITEM_ID);

            Uri uri = ToDoContract.ToDoEntry.CONTENT_URI.buildUpon().appendPath(id).build();
            getContentResolver().delete(uri, null, null);

            WidgetUtils.updateDataWidgetToDo(getApplicationContext());
            MyNotificationUtil.cancelNotification(getApplicationContext(), id);
            MyNotificationUtil.removeNotification(getApplicationContext(), Integer.valueOf(id));
        } else {
            //won't happend
            Log.d("TAG+++", "Unsupported action in ToDoClickIntentService");
        }
    }


}
