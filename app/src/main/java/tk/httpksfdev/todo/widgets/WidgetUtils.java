package tk.httpksfdev.todo.widgets;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;

import tk.httpksfdev.todo.R;



public class WidgetUtils {


    //refresh listview in widget
    public static void updateDataWidgetToDo(Context context){
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, ToDoWidgetProvider.class));
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_todo_listview);
    }


}
