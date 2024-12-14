package mobilesecurity.sit.project.mainpage.mainpage_backend

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent


class BootRecv : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val goToService = Intent(context, UpdateService::class.java)
        context.startService(goToService)
    }
}
