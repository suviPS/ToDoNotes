package tk.httpksfdev.todo.notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import android.util.Log;

import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;

import java.util.Calendar;

import tk.httpksfdev.todo.EditEntryActivity;
import tk.httpksfdev.todo.MainActivity;
import tk.httpksfdev.todo.MyUtils;
import tk.httpksfdev.todo.R;
import tk.httpksfdev.todo.data.ToDoContract;
import tk.httpksfdev.todo.widgets.widget_todo.ToDoClickIntentService;

/**
 * Util class for notifications
 *
 * We use Firebase JobDispatcher for delayed notifications, alternative would be evernote's android-job
 *
 * Firebase JobDispatcher: https://github.com/firebase/firebase-jobdispatcher-android
 * Evernote android-job:   https://github.com/evernote/android-job
 */

public class MyNotificationUtil {

    public static final int FLEX_SECONDS = 30;

    public static final String CHANNEL_GROUP_NAME_TODO = "ToDo (All)";
    public static final String CHANNEL_GROUP_ID_TODO = "tk.httpksfdev.todo.notifications.channels.group.todo";

    public static final String CHANNEL_NAME_TODO_1 = "ToDo (High priority)";
    public static final String CHANNEL_ID_TODO_1 = "tk.httpksfdev.todo.notifications.channels.todo1";

    public static final String CHANNEL_NAME_TODO_2 = "ToDo (Medium priority)";
    public static final String CHANNEL_ID_TODO_2 = "tk.httpksfdev.todo.notifications.channels.todo2";

    public static final String CHANNEL_NAME_TODO_3 = "ToDo (Low priority)";
    public static final String CHANNEL_ID_TODO_3 = "tk.httpksfdev.todo.notifications.channels.todo3";

    public static final String CHANNEL_NAME_NOTE = "Note";
    public static final String CHANNEL_ID_NOTE = "tk.httpksfdev.todo.notifications.channels.note";


    //[Schedule Notification]
    public static void scheduleNotification(Context mContext, @NonNull String entryId) {

        //get timeInSecond from db to schedule job
        Uri uri = ToDoContract.ToDoEntry.CONTENT_URI.buildUpon().appendPath(entryId).build();
        Cursor mCursor = mContext.getContentResolver().query(uri, null, null, null, null);
        if (mCursor == null) {
            Log.d("TAG+++", "mCursor == null, no job scheduled");
            return;
        } else if(mCursor.getCount() == 0){
            Log.d("TAG+++", "mCursor.getCount() == 0, no job scheduled");
            return;
        }

        mCursor.moveToFirst();
        long timeInMillis = mCursor.getLong(mCursor.getColumnIndex(ToDoContract.ToDoEntry.COLUMN_REMINDER));
        timeInMillis -= Calendar.getInstance().getTimeInMillis();
        timeInMillis /= 1000;
        int timeInSeconds = (int) timeInMillis;

        if (timeInSeconds > 5) {
            //schedule job
            FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(mContext));
            Job myJob = dispatcher.newJobBuilder()
                    .setService(NotificationJobService.class)
                    .setTag(entryId)        // uniquely identifies the job with entry id from database
                    .setRecurring(false)
                    .setLifetime(Lifetime.FOREVER)
                    .setTrigger(Trigger.executionWindow(timeInSeconds, timeInSeconds + FLEX_SECONDS))
                    .setReplaceCurrent(true)
                    .setRetryStrategy(RetryStrategy.DEFAULT_LINEAR)
                    .build();

            dispatcher.mustSchedule(myJob);
            Log.d("TAG+++", "Job scheduled with " + timeInSeconds + " seconds");
        } else {
            //reminder probably -1
        }
    }

    //[Cancel Notification]
    public static void cancelNotification(Context mContext, @NonNull String entryId) {
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(mContext));
        dispatcher.cancel(entryId);
    }


    //[Send Notification]
    private static final int NOTIFICATION_BASE_ID = 4473;

    static void sendNotification(Context context, int id, String title, String desc, int priority) {

        //get color
        int color = -1;
        switch (priority) {
            case 1:
                color = ContextCompat.getColor(context, R.color.priority_1);
                break;
            case 2:
                color = ContextCompat.getColor(context, R.color.priority_2);
                break;
            case 3:
                color = ContextCompat.getColor(context, R.color.priority_3);
                break;
            case 5:
                color = ContextCompat.getColor(context, R.color.priority_5);
                break;
        }

        //create channel for Android O
        String tempNotificationChannelId = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel notificationChannel = null;
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            //create To Do group
            NotificationChannelGroup group = new NotificationChannelGroup(CHANNEL_GROUP_ID_TODO, CHANNEL_GROUP_NAME_TODO);
            notificationManager.createNotificationChannelGroup(group);

            //create channel
            switch (priority) {
                case 1:
                    notificationChannel = new NotificationChannel(CHANNEL_ID_TODO_1, CHANNEL_NAME_TODO_1, NotificationManager.IMPORTANCE_HIGH);
                    notificationChannel.enableLights(true);
                    notificationChannel.setLightColor(Color.RED);
                    notificationChannel.enableVibration(true);
                    notificationChannel.setBypassDnd(true);
                    notificationChannel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PRIVATE);
                    notificationChannel.setShowBadge(true);
                    notificationChannel.setGroup(CHANNEL_GROUP_ID_TODO);

                    notificationManager.createNotificationChannel(notificationChannel);
                    break;
                case 2:
                    notificationChannel = new NotificationChannel(CHANNEL_ID_TODO_2, CHANNEL_NAME_TODO_2, NotificationManager.IMPORTANCE_HIGH);
                    notificationChannel.enableLights(true);
                    notificationChannel.setLightColor(Color.RED);
                    notificationChannel.enableVibration(true);
                    notificationChannel.setBypassDnd(true);
                    notificationChannel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PRIVATE);
                    notificationChannel.setShowBadge(true);
                    notificationChannel.setGroup(CHANNEL_GROUP_ID_TODO);

                    notificationManager.createNotificationChannel(notificationChannel);
                    break;
                case 3:
                    notificationChannel = new NotificationChannel(CHANNEL_ID_TODO_3, CHANNEL_NAME_TODO_3, NotificationManager.IMPORTANCE_HIGH);
                    notificationChannel.enableLights(true);
                    notificationChannel.setLightColor(Color.YELLOW);
                    notificationChannel.enableVibration(true);
                    notificationChannel.setBypassDnd(true);
                    notificationChannel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PRIVATE);
                    notificationChannel.setShowBadge(true);
                    notificationChannel.setGroup(CHANNEL_GROUP_ID_TODO);

                    notificationManager.createNotificationChannel(notificationChannel);
                    break;
                case 5:
                    notificationChannel = new NotificationChannel(CHANNEL_ID_NOTE, CHANNEL_NAME_NOTE, NotificationManager.IMPORTANCE_HIGH);
                    notificationChannel.enableLights(true);
                    notificationChannel.setLightColor(Color.BLUE);
                    notificationChannel.enableVibration(true);
                    notificationChannel.setBypassDnd(true);
                    notificationChannel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PRIVATE);
                    notificationChannel.setShowBadge(true);

                    notificationManager.createNotificationChannel(notificationChannel);
                    break;
            }

            tempNotificationChannelId = notificationChannel.getId();
        }

        //create intents
        int notificationId = NOTIFICATION_BASE_ID + id;

        //main body
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        //action edit
        Intent intentEdit = new Intent(context, EditEntryActivity.class);
        intentEdit.putExtra(MyUtils.EXTRA_ITEM_ID, ""+id);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(EditEntryActivity.class);
        stackBuilder.addNextIntent(intentEdit);
        PendingIntent pendingIntentEdit = stackBuilder.getPendingIntent(notificationId, PendingIntent.FLAG_UPDATE_CURRENT);

        //action done
        Intent intentDone = new Intent(context, ToDoClickIntentService.class);
        intentDone.setAction(ToDoClickIntentService.ACTION_CLICK_TODO_ITEM_DONE);
        intentDone.putExtra(ToDoClickIntentService.EXTRA_TODO_ITEM_ID, ""+id);
        PendingIntent pendingIntentDone = PendingIntent.getService(context, notificationId, intentDone, PendingIntent.FLAG_UPDATE_CURRENT);

        //create notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, tempNotificationChannelId);
        builder.setSmallIcon(R.drawable.checkbox)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_list_notification_kk))
                .setColor(color)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentTitle(title)
                .setContentText(desc)
                .setAutoCancel(true)
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(contentIntent)
                .addAction(R.drawable.ic_edit, "Edit", pendingIntentEdit)
                .addAction(R.drawable.ic_done_02, "Done", pendingIntentDone)
        ;

        //set big style for long desc
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(desc));


        Notification notification = builder.build();
        NotificationManagerCompat.from(context).notify(notificationId, notification);
    }

    //[Remove notification (on action button clicked)]
    public static void removeNotification(Context context, int id){
        NotificationManagerCompat nm = NotificationManagerCompat.from(context);
        nm.cancel(NOTIFICATION_BASE_ID + id);
    }


}
