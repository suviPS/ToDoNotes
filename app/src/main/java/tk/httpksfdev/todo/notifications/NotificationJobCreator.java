package tk.httpksfdev.todo.notifications;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class NotificationJobCreator implements JobCreator {
    @Nullable
    @Override
    public Job create(@NonNull String tag) {
        switch (tag){
            case NotificationJob.TAG:
                return new NotificationJob();
            default:
                return null;
        }
    }
}
