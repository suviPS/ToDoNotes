package tk.httpksfdev.todo.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;



public class DbHelper extends SQLiteOpenHelper {

    public static String DATABASE_NAME = "toDoDatabase.db";
    public static int DATABASE_VERSION = 12;

    //constructor calls default counstructor, null???
    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        //SQL command to create 1. table
        final String SQL_CREATE_TODO_TABLE = "CREATE TABLE " +
                ToDoContract.ToDoEntry.TABLE_NAME + "(" +
                ToDoContract.ToDoEntry.TYPE + " INTEGER NOT NULL, "+
                ToDoContract.ToDoEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ToDoContract.ToDoEntry.COLUMN_INFO + " TEXT NOT NULL, "+
                ToDoContract.ToDoEntry.COLUMN_PRIORITY + " INTEGER NOT NULL, "+
                ToDoContract.ToDoEntry.COLUMN_DESC + " TEXT NOT NULL"+
                ");";

        //execute SQL command
        db.execSQL(SQL_CREATE_TODO_TABLE);

        //SQL command to create 2. table
        final String SQL_CREATE_TODO_TABLE_OLD = "CREATE TABLE " +
                ToDoContract.ToDoEntryOld.TABLE_NAME + "(" +
                ToDoContract.ToDoEntryOld.TYPE + " INTEGER NOT NULL, "+
                ToDoContract.ToDoEntryOld._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ToDoContract.ToDoEntryOld.COLUMN_INFO + " TEXT NOT NULL, "+
                ToDoContract.ToDoEntryOld.COLUMN_PRIORITY + " INTEGER NOT NULL, "+
                ToDoContract.ToDoEntryOld.COLUMN_DESC + " TEXT NOT NULL"+
                ");";

        //execute SQL command
        db.execSQL(SQL_CREATE_TODO_TABLE_OLD);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ToDoContract.ToDoEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ToDoContract.ToDoEntryOld.TABLE_NAME);
        onCreate(db);
    }
}
