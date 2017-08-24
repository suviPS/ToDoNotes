package tk.httpksfdev.todo.data;


import android.net.Uri;
import android.provider.BaseColumns;

public class ToDoContract {

    public static final String AUTHORITY = "tk.httpksfdev.todo.data";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final String PATH_TASKS = "mytodotable";

    public static final String PATH_TASKS_OLD = "mytodoold";

    //1. table for active entries
    public static final class ToDoEntry implements BaseColumns{

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_TASKS).build();


        public static final String TABLE_NAME = "mytodotable";
        public static final String TYPE = "type";
        public static final String COLUMN_PRIORITY = "priority";
        public static final String COLUMN_INFO = "ic_info";
        public static final String COLUMN_DESC = "description";
    }

    //2. table for old entries
    public static final class ToDoEntryOld implements BaseColumns{

        public static final String TABLE_NAME = "mytodooldtable";
        //must be same as TYPE in ToDoEntry
        public static final String TYPE = "type";
        public static final String COLUMN_INFO = "ic_info";
        public static final String COLUMN_PRIORITY = "priority";
        public static final String COLUMN_DESC = "description";

        public static final Uri CONTENT_URI_OLD = BASE_CONTENT_URI.buildUpon().appendPath(PATH_TASKS_OLD).build();
    }
}
