package mobilesecurity.sit.project.chats.conversation_backend



import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import java.text.SimpleDateFormat
import android.app.Notification
import java.util.Date
import java.util.Locale


class ConversationStuff : NotificationListenerService() {
    private lateinit var db: FirestoreDatabaseHandler

    override fun onCreate() {
        super.onCreate()
        db = FirestoreDatabaseHandler(this)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val extras = sbn.notification.extras
        // Avoid group summary notifications
        if (sbn.notification.flags and Notification.FLAG_GROUP_SUMMARY != 0) {
            Log.d("error", "Ignore the notification FLAG_GROUP_SUMMARY")
            return
        }

        val title = extras.getString("android.title", "")
        val text = extras.getCharSequence("android.text", "").toString()

        val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        // Construct your notification object (consider creating a data class for this)
        val thisNotif = Conver(title, text, sbn.packageName, date, sbn.id)
        // Add notification to your database
        db.addNotification(thisNotif)
    }
}

