package tk.httpksfdev.todo.widgets.widget_note;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NoteBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //pass intent to job intent service
        NoteIntentService.enqueueWork(context, intent);
    }
}
