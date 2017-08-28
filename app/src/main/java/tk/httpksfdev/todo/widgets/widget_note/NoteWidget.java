package tk.httpksfdev.todo.widgets.widget_note;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

import tk.httpksfdev.todo.R;
import tk.httpksfdev.todo.data.ToDoContract;
import tk.httpksfdev.todo.widgets.WidgetUtils;

/**
 * Widget for Note(s)
 */
public class NoteWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        Log.d("TAG+++", "updateAppWidget() | appWidgetId: " + appWidgetId);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.note_widget);

        //get id and query to see is there still entry in db
        int id = PreferenceManager.getDefaultSharedPreferences(context).getInt(WidgetUtils.PREF_WIDGET_NOTE + appWidgetId, -1);
        Uri uri = ToDoContract.ToDoEntry.CONTENT_URI.buildUpon().appendPath(""+id).build();
        Cursor mCursor = context.getContentResolver().query(uri, null, null, null, null);


        String name = "";
        String text = "";
        String itemAction = "";

        if(mCursor == null){
            //no match, wrong id
            name = context.getString(R.string.widget_note_name);
            text = context.getString(R.string.widget_note_text);
            itemAction = NoteIntentService.ACTION_CLICK_NOTE_ITEM_CANCEL;
            Log.d("TAG+++", "mCursor == null");

        } else if(mCursor.getCount() == 0){
            //no match, item removed
            name = context.getString(R.string.widget_note_name);
            text = context.getString(R.string.widget_note_text);
            itemAction = NoteIntentService.ACTION_CLICK_NOTE_ITEM_CANCEL;
            Log.d("TAG+++", "mCursor.getCount() == 0");

        } else {
            //all good
            mCursor.moveToFirst();
            name = mCursor.getString(mCursor.getColumnIndex(ToDoContract.ToDoEntry.COLUMN_INFO));
            text = mCursor.getString(mCursor.getColumnIndex(ToDoContract.ToDoEntry.COLUMN_DESC));
            itemAction = NoteIntentService.ACTION_CLICK_NOTE_ITEM_CLICK;
            mCursor.close();
        }

        views.setTextViewText(R.id.widget_note_toolbar_name, name);
        views.setTextViewText(R.id.widget_note_item_text, text);

        //Intent for item desc
        Intent intent05 = new Intent(context, NoteIntentService.class);
        intent05.setAction(itemAction);
        intent05.putExtra(NoteIntentService.EXTRA_NOTE_ITEM_ID, id);
        PendingIntent pendingIntent05 = PendingIntent.getService(context, appWidgetId, intent05, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_note_item_text, pendingIntent05);

        //intent for add action
        Intent intent01 = new Intent(context, NoteIntentService.class);
        intent01.setAction(NoteIntentService.ACTION_CLICK_NOTE_TOOLBAR_ADD);
        PendingIntent pendingIntent01 = PendingIntent.getService(context, 0, intent01, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_note_toolbar_add, pendingIntent01);

        //intent for chooser activity
        Intent intent02 = new Intent(context, NoteIntentService.class);
        intent02.setAction(NoteIntentService.ACTION_CLICK_NOTE_TOOLBAR_NAME);
        intent02.putExtra(NoteIntentService.EXTRA_NOTE_WIDGET_ID, appWidgetId);
        PendingIntent pendingIntent02 = PendingIntent.getService(context, appWidgetId, intent02, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_note_toolbar_name, pendingIntent02);


        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        //
    }

    @Override
    public void onDisabled(Context context) {
        //
    }
}

