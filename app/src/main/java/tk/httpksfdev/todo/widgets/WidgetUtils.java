package tk.httpksfdev.todo.widgets;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import tk.httpksfdev.todo.R;
import tk.httpksfdev.todo.widgets.widget_note.NoteWidget;
import tk.httpksfdev.todo.widgets.widget_todo.ToDoWidgetProvider;


public class WidgetUtils {

    //[To Do Widget]

    //refresh listview in To Do widget
    public static void updateDataWidgetToDo(Context context){
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, ToDoWidgetProvider.class));
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_todo_listview);
    }


    //==================================================
    //[Note Widget]

    public static final String PREF_WIDGET_NOTE = "pref_widget_note_active_id";

    public static final String EXTRA_WIDGET_ID = "tk.httpksfdev.todo.widgets.note.extra.WIDGET_ID";

    //refresh data in Note widgets
    public static void updateDataWidgetNote(Context context){
        //get widgetIds
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, NoteWidget.class));

        //send broadcast to trigger widgets onUpdate()
        Intent intent = new Intent(context,NoteWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        // Use an array and EXTRA_APPWIDGET_IDS instead of AppWidgetManager.EXTRA_APPWIDGET_ID
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        context.sendBroadcast(intent);

    }



}
