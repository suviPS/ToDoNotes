package tk.httpksfdev.todo.widgets.widget_note;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import tk.httpksfdev.todo.AddEntryActivity;
import tk.httpksfdev.todo.EditEntryActivity;
import tk.httpksfdev.todo.MyUtils;
import tk.httpksfdev.todo.widgets.WidgetUtils;

/**
 * IntentService to handle clicks on Note widgets
 */

public class NoteIntentService extends IntentService {

    // action
    public static final String ACTION_CLICK_NOTE_TOOLBAR_NAME = "tk.httpksfdev.todo.widgets.action.NOTE_TOOLBAR_NAME";
    public static final String ACTION_CLICK_NOTE_TOOLBAR_ADD = "tk.httpksfdev.todo.widgets.action.NOTE_TOOLBAR_ADD";
    public static final String ACTION_CLICK_NOTE_ITEM_CLICK = "tk.httpksfdev.todo.widgets.action.NOTE_ITEM_CLICK";

    // params
    public static final String EXTRA_NOTE_ITEM_ID = "tk.httpksfdev.todo.widgets.extra.NOTE_ID";
    public static final String EXTRA_NOTE_WIDGET_ID = "tk.httpksfdev.todo.widgets.extra.WIDGET_ID";

    //required constructor
    public NoteIntentService(){
        super(".NoteIntentService");
    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            Log.d("TAG+++", "action: " + action);

            if(ACTION_CLICK_NOTE_TOOLBAR_NAME.equals(action)){
                //choose note
                Intent intent01 = new Intent(getApplicationContext(), ChooseNoteForWidgetActivity.class);
                intent01.putExtra(WidgetUtils.EXTRA_WIDGET_ID, intent.getIntExtra(EXTRA_NOTE_WIDGET_ID, -1));
                intent01.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent01);

            } else if(ACTION_CLICK_NOTE_TOOLBAR_ADD.equals(action)){
                //start AddEntry
                Intent intent02 = new Intent(getApplicationContext(), AddEntryActivity.class);
                intent02.setAction(MyUtils.ACTION_ADD_NOTE);
                intent02.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent02);

            } else if(ACTION_CLICK_NOTE_ITEM_CLICK.equals(action)){
                //start EditEntry
                int id = intent.getIntExtra(EXTRA_NOTE_ITEM_ID, -1);
                if(id == -1){
                    //no data passed
                } else{
                    //all good
                    Intent intent03 = new Intent(getApplicationContext(), EditEntryActivity.class);
                    intent03.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent03.putExtra(MyUtils.EXTRA_ITEM_ID, "" + id);
                    startActivity(intent03);
                }

            } else{
                //won't happend
                Log.d("TAG+++", "Unsupported action in NoteIntentService");
            }
        }
    }



}
