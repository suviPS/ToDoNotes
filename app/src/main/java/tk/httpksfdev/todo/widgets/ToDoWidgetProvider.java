package tk.httpksfdev.todo.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import tk.httpksfdev.todo.R;

/**
 * Widget for To Do list
 */
public class ToDoWidgetProvider extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        // Construct the RemoteViews object
        RemoteViews views = getListViewRemoteView(context);


        // Instruct the widget manager to update the widget
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


    //helper to create RemoteViews
    private static RemoteViews getListViewRemoteView(Context context) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.to_do_widget);

        // Set the ToDoListViewService intent to act as the adapter for the ListView
        Intent intent = new Intent(context, ToDoListViewService.class);
        views.setRemoteAdapter(R.id.widget_todo_listview, intent);


        //add PendingIntents
        //for listView
        Intent tempIntent01 = new Intent(context, ToDoClickIntentService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, tempIntent01, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setPendingIntentTemplate(R.id.widget_todo_listview, pendingIntent);

        //for To Do text
        Intent tempIntent02 = new Intent(context, ToDoClickIntentService.class);
        tempIntent02.setAction(ToDoClickIntentService.ACTION_CLICK_TODO_TOOLBAR_TODO);
        PendingIntent pendingIntent02 = PendingIntent.getService(context, 0, tempIntent02, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_todo_toolbar_todo, pendingIntent02);

        //for add new entry
        Intent tempIntent03 = new Intent(context, ToDoClickIntentService.class);
        tempIntent03.setAction(ToDoClickIntentService.ACTION_CLICK_TODO_TOOLBAR_ADD);
        PendingIntent pendingIntent03 = PendingIntent.getService(context, 0, tempIntent03, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_todo_toolbar_add, pendingIntent03);


        return views;
    }
}

