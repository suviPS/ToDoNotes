package tk.httpksfdev.todo.widgets.widget_todo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ToDoBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //pass intent to job intent service
        ToDoClickIntentService.enqueueWork(context, intent);
    }
}
