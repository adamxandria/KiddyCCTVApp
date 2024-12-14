package mobilesecurity.sit.project.mainpage.mainpage_backend

import android.app.Service
import android.content.Intent
import android.os.IBinder

class UpdateService : Service() {

    private var initialIntent: Intent? = null

    override fun onBind(intent: Intent): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (initialIntent == null) {
            initialIntent = intent
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        super.onCreate()
        println("On create")
        val thread = Thread(ServerConnection(this))
        thread.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        println("on destroy")
        baseContext.startService(Intent(baseContext, UpdateService::class.java))
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        println("ON task removed call")
        val intent = Intent(this, this::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        super.onTaskRemoved(rootIntent)
    }
}
