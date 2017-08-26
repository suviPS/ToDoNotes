package tk.httpksfdev.todo.notifications;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;

import java.util.Calendar;

import tk.httpksfdev.todo.MainActivity;
import tk.httpksfdev.todo.R;
import tk.httpksfdev.todo.data.ToDoContract;

/**
 *  Util class for notifications
 *
 *  We use Firebase JobDispatcher for delayed notifications, alternative would be evernote's android-job
 *
 *  Firebase JobDispatcher: https://github.com/firebase/firebase-jobdispatcher-android
 *  Evernote android-job:   https://github.com/evernote/android-job
 *
 */

public class MyNotificationUtil {

    public static final int FLEX_SECONDS = 30;

    //[Schedule Notification]
    public static void scheduleNotification(Context mContext, @NonNull String entryId){

        //get timeInSecond from db to schedule job
        Uri uri = ToDoContract.ToDoEntry.CONTENT_URI.buildUpon().appendPath(entryId).build();
        Cursor mCursor = mContext.getContentResolver().query(uri, null, null, null, null);
        if(mCursor == null){
            Log.d("TAG+++", "mCursor == null, no job scheduled");
            return;
        }
        mCursor.moveToFirst();
        long timeInMillis = mCursor.getLong(mCursor.getColumnIndex(ToDoContract.ToDoEntry.COLUMN_REMINDER));
        timeInMillis-= Calendar.getInstance().getTimeInMillis();
        timeInMillis/=1000;
        int timeInSeconds = (int) timeInMillis;

        if(timeInSeconds > 5){
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
    public static void cancelNotification(Context mContext, @NonNull String entryId){
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(mContext));
        dispatcher.cancel(entryId);
    }



    //[Send Notification]
    private static final int NOTIFICATION_BASE_ID = 4473;
    static void sendNotification(Context context, int id, String title, String desc, int color){

        int notificationId = NOTIFICATION_BASE_ID + id;
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.checkbox)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_list_notification_kk))
                .setColor(color)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentTitle(title)
                .setContentText(desc)
                .setAutoCancel(true)
                .setContentIntent(contentIntent)
        ;

        //set big style for long desc
        builder.setStyle(new android.support.v4.app.NotificationCompat.BigTextStyle().bigText(desc));

        //if SDK >= JELLY BEAN
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);

        Notification notification = builder.build();
        NotificationManagerCompat.from(context).notify(notificationId, notification);
    }




}
