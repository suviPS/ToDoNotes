package tk.httpksfdev.todo.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import static tk.httpksfdev.todo.data.ToDoContract.ToDoEntry.TABLE_NAME;


public class ToDoContentProvider extends ContentProvider {
    public static final String TAG = "tk.httpksfdev.todo";

    public static final int TASK = 100;
    public static final int TASK_WITH_ID = 101;
    public static final int TASK_OLD = 900;
    public static final int TASK_OLD_WITH_ID = 901;

    private DbHelper dbHelper;

    private UriMatcher mUriMatcher = buildUriMatcher();

    public static UriMatcher buildUriMatcher(){
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        //add matches with addUri(String authority, String path, int code);
        //directory
        uriMatcher.addURI(ToDoContract.AUTHORITY, ToDoContract.PATH_TASKS, TASK);
        //single item
        uriMatcher.addURI(ToDoContract.AUTHORITY, ToDoContract.PATH_TASKS + "/#", TASK_WITH_ID);
        //directory
        uriMatcher.addURI(ToDoContract.AUTHORITY, ToDoContract.PATH_TASKS_OLD, TASK_OLD);
        //single item
        uriMatcher.addURI(ToDoContract.AUTHORITY, ToDoContract.PATH_TASKS_OLD + "/#", TASK_OLD_WITH_ID);

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        dbHelper = new DbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        final SQLiteDatabase db = dbHelper.getReadableDatabase();
        int match = mUriMatcher.match(uri);
        Cursor cursor = null;

        switch (match){
            case TASK:
                cursor = db.query(TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case TASK_WITH_ID:
                //usin selection and selection Args
                //SELECT * Where selection = selectionArgs
                //URI: content://<authority>/tasks/1
                //tasks(0), 1(1)
                String id = uri.getPathSegments().get(1);
                String mSelection = "_id=?";
                String[] mSelectionArgs = new String[]{id};

                cursor = db.query(TABLE_NAME,
                        projection,
                        mSelection,
                        mSelectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case TASK_OLD:
                cursor = db.query(ToDoContract.ToDoEntryOld.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        ToDoContract.ToDoEntry._ID + " DESC");
                break;
            case TASK_OLD_WITH_ID:
                String id_old = uri.getPathSegments().get(1);
                String mSelection_old = "_id=?";
                String[] mSelectionArgs_old = new String[]{id_old};

                cursor = db.query(ToDoContract.ToDoEntryOld.TABLE_NAME,
                        projection,
                        mSelection_old,
                        mSelectionArgs_old,
                        null,
                        null,
                        ToDoContract.ToDoEntry._ID + " DESC");
                break;
            default:
                Log.e(TAG, "Unsupported query");
        }

        //notify cursor
        try {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        } catch (NullPointerException e){
            e.printStackTrace();
        }
        return cursor;
    }



    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        int match = mUriMatcher.match(uri);
        Uri returnUri = null;

        //insert TYPE for To Do
        values.put(ToDoContract.ToDoEntry.TYPE, 1);

        switch (match){
            case TASK:
                long id = db.insert(TABLE_NAME, null, values);
                if(id > 0){
                    //success
                    returnUri = ContentUris.withAppendedId(ToDoContract.ToDoEntry.CONTENT_URI, id);
                } else {
                    throw new UnsupportedOperationException("Failed to insert row into " + uri);
                }
                break;
            case TASK_WITH_ID:

                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);

        }

        //notify resolver
        try{
            getContext().getContentResolver().notifyChange(uri, null);
        } catch (NullPointerException e){
            e.printStackTrace();
        }

        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        int match = mUriMatcher.match(uri);

        switch (match) {
            case TASK:
                return db.delete(TABLE_NAME,
                        selection,
                        selectionArgs);
            //break;
            case TASK_WITH_ID:
                //usin selection and selection Args
                //SELECT * Where selection = selectionArgs
                //URI: content://<authority>/tasks/1
                //tasks(0), 1(1)
                String id = uri.getPathSegments().get(1);
                String mSelection = "_id=?";
                String[] mSelectionArgs = new String[]{id};

                if(helperDelete(uri, db))
                   Log.d(TAG, "Removed + Added :D");
                return db.delete(TABLE_NAME,
                        mSelection,
                        mSelectionArgs);
            // break;
            case TASK_OLD:
                return db.delete(ToDoContract.ToDoEntryOld.TABLE_NAME,
                        selection,
                        selectionArgs);

            case TASK_OLD_WITH_ID:
                String idOld = uri.getPathSegments().get(1);
                String mSelectionOld = "_id=?";
                String[] mSelectionArgsOld = new String[]{idOld};

                return db.delete(ToDoContract.ToDoEntryOld.TABLE_NAME,
                        mSelectionOld,
                        mSelectionArgsOld);
            //break;
            default:
                return 0;
        }
    }

    public boolean helperDelete(Uri uri, SQLiteDatabase db){
        Cursor cursor = query(uri, null, null, null, null);
        if(cursor.moveToFirst()){
            ContentValues cv = new ContentValues();
            //insert type for ToDo_Old
            cv.put(ToDoContract.ToDoEntryOld.TYPE, -1);
            cv.put(ToDoContract.ToDoEntryOld.COLUMN_INFO, cursor.getString(cursor.getColumnIndex(ToDoContract.ToDoEntry.COLUMN_INFO)));
            cv.put(ToDoContract.ToDoEntryOld.COLUMN_DESC, cursor.getString(cursor.getColumnIndex(ToDoContract.ToDoEntry.COLUMN_DESC)));
            cv.put(ToDoContract.ToDoEntryOld.COLUMN_PRIORITY, cursor.getString(cursor.getColumnIndex(ToDoContract.ToDoEntry.COLUMN_PRIORITY)));
            Log.d( "TAG++", ""+ db.insert(ToDoContract.ToDoEntryOld.TABLE_NAME,null, cv));
            return true;
        } else
            return false;
    }


    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        int match = mUriMatcher.match(uri);
        int num = -1;

        switch (match){
            case TASK:
                break;
            case TASK_WITH_ID:
                String id = uri.getPathSegments().get(1);
                String mSelection = "_id=?";
                String[] mSelectionArgs = new String[]{id};

                num = db.update(TABLE_NAME,
                        values,
                        mSelection,
                        mSelectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);

        }

        //notify resolver
        try{
            getContext().getContentResolver().notifyChange(uri, null);
        } catch (NullPointerException e){
            e.printStackTrace();
        }


        return num;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        //TODO: maybe move Type detection here??
        return null;
    }
}
