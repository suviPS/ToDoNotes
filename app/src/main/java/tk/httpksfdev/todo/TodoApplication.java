package tk.httpksfdev.todo;

import android.app.Application;

import com.evernote.android.job.JobManager;

import tk.httpksfdev.todo.notifications.NotificationJobCreator;

public class TodoApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        JobManager.create(this).addJobCreator(new NotificationJobCreator());
    }
}
